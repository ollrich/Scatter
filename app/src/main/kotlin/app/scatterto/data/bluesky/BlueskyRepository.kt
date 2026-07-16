package app.scatterto.data.bluesky

import app.scatterto.R
import app.scatterto.core.Facet
import app.scatterto.core.computeFacets
import app.scatterto.core.domainOf
import app.scatterto.data.CredentialStore
import app.scatterto.data.log.EventLog
import app.scatterto.data.model.AccountInfo
import app.scatterto.data.model.BlueskyAccount
import app.scatterto.data.net.ApiException
import app.scatterto.data.net.Network
import app.scatterto.data.net.apiCall
import app.scatterto.data.net.toApiError
import app.scatterto.data.util.DateDisplay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Bluesky-Anbindung (§4.2, §6, §7, §12.1 Nr. 4). Session wird persistiert und bei 401 transparent
 * erneuert; scheitert der Refresh, wird aus dem gespeicherten App-Password eine neue Session erzeugt.
 */
class BlueskyRepository(
    private val credentialStore: CredentialStore,
    private val log: EventLog,
) {

    private val thumbnailer = ImageThumbnailer(Network.okHttp())

    private val apis = ConcurrentHashMap<String, BlueskyApi>()

    private fun api(pdsUrl: String): BlueskyApi =
        apis.getOrPut(normalizePds(pdsUrl)) {
            Network.retrofit(normalizePds(pdsUrl)).create(BlueskyApi::class.java)
        }

    /** Verbindet den Account: createSession + Profil (Avatar/Handle). */
    suspend fun connect(identifier: String, appPassword: String, pdsUrl: String): BlueskyAccount = apiCall {
        val api = api(pdsUrl)
        val session = api.createSession(CreateSessionRequest(identifier.trim(), appPassword))
        val profile = runCatching {
            api.getProfile("Bearer ${session.accessJwt}", session.did)
        }.getOrNull()

        BlueskyAccount(
            identifier = identifier.trim(),
            appPassword = appPassword,
            pdsUrl = pdsUrl,
            did = session.did,
            handle = session.handle,
            avatarUrl = profile?.avatar,
            accessJwt = session.accessJwt,
            refreshJwt = session.refreshJwt,
        )
    }

    /**
     * Postet [text] mit [facets] und optionaler Link-Karte [card]. Gibt die Web-URL des Posts zurück.
     * Die Karte darf das Posten nie blockieren (§6): scheitert Bild/Blob, wird ohne Bild gepostet.
     */
    suspend fun post(
        account: BlueskyAccount,
        text: String,
        facets: List<Facet>,
        card: LinkCard?,
        langs: List<String> = listOf("en"), // BCP-47, Vorbereitung Multi-Language
    ): String {
        val api = api(account.pdsUrl)
        var current = account

        // Transparenter Session-Refresh. Wichtig: atproto meldet abgelaufene Access-Token als
        // 400/ExpiredToken, nicht als 401 — beides muss den Refresh auslösen (§12.1 Nr. 4).
        suspend fun <T> authed(call: suspend (String) -> T): T = try {
            call("Bearer ${current.accessJwt}")
        } catch (e: HttpException) {
            val error = e.toApiError()
            if (!error.isAuthExpired) throw ApiException(error)
            current = refreshSession(api, current)
            credentialStore.saveBluesky(current)
            try {
                call("Bearer ${current.accessJwt}")
            } catch (retry: HttpException) {
                throw ApiException(retry.toApiError())
            }
        }

        val thumbBlob = card?.imageUrl?.let { imageUrl ->
            runCatching {
                val jpeg = thumbnailer.downloadAsJpeg(imageUrl) ?: return@runCatching null
                val body = jpeg.toRequestBody("image/jpeg".toMediaType())
                authed { auth -> api.uploadBlob(auth, body).blob }
            }.onFailure {
                log.info(R.string.log_bsky_thumb_failed)
            }.getOrNull()
        }

        val embed = card?.let {
            ExternalEmbed(external = ExternalCard(it.uri, it.title, it.description, thumbBlob))
        }

        val record = PostRecord(
            text = text,
            createdAt = Instant.now().toString(),
            langs = langs,
            facets = facets.ifEmpty { null },
            embed = embed,
        )

        val result = try {
            authed { auth ->
                api.createRecord(auth, CreateRecordRequest(repo = current.did!!, record = record))
            }
        } catch (e: ApiException) {
            // §6: Die Link-Karte darf das Posten nie blockieren — lehnt der Server das Embed ab,
            // denselben Post ohne Karte absetzen. Auth-Fehler sind hier bereits behandelt.
            if (e.error.status == 400 && record.embed != null) {
                log.error(R.string.log_bsky_embed_rejected, e.error.readable)
                // Die URL steht bei Bluesky NICHT im Text (die Karte trägt sie). Fällt die Karte weg,
                // käme sonst ein Post ganz ohne Link raus — also URL anhängen und die Facets neu
                // berechnen, damit sie klickbar ist.
                val fallback = card?.uri?.takeIf { it.isNotBlank() && !text.contains(it) }
                    ?.let { uri ->
                        val withUrl = "$text\n\n$uri"
                        record.copy(embed = null, text = withUrl, facets = computeFacets(withUrl, uri).ifEmpty { null })
                    }
                    ?: record.copy(embed = null)
                authed { auth ->
                    api.createRecord(auth, CreateRecordRequest(repo = current.did!!, record = fallback))
                }
            } else {
                throw e
            }
        }
        return atUriToWebUrl(result.uri, current.handle ?: current.did!!)
    }

    /** Live-Detailinfos fürs Konten-Menü. Erneuert bei Bedarf die Session (§12.1 Nr. 4). */
    suspend fun accountInfo(account: BlueskyAccount): AccountInfo {
        val api = api(account.pdsUrl)
        var current = account

        suspend fun <T> authed(call: suspend (String) -> T): T = try {
            apiCall { call("Bearer ${current.accessJwt}") }
        } catch (e: ApiException) {
            if (!e.error.isAuthExpired) throw e
            current = refreshSession(api, current)
            credentialStore.saveBluesky(current)
            apiCall { call("Bearer ${current.accessJwt}") }
        }

        val did = current.did ?: error("Bluesky-Account ohne DID")
        val profile = authed { auth -> api.getProfile(auth, did) }
        // Letztes Posting best-effort — schlägt es fehl, bleibt das Feld leer.
        val lastPost = runCatching {
            authed { auth -> api.getAuthorFeed(auth, did, 1) }.feed.firstOrNull()?.post?.indexedAt
        }.getOrNull()

        return AccountInfo(
            server = domainOf(current.pdsUrl) ?: current.pdsUrl,
            profileUrl = "https://bsky.app/profile/${current.handle ?: did}",
            followersCount = profile.followersCount,
            memberSince = DateDisplay.monthYear(profile.createdAt),
            lastPost = DateDisplay.date(lastPost),
        )
    }

    /** Refresh über refreshJwt; scheitert das, neue Session aus dem App-Password (§12.1 Nr. 4). */
    private suspend fun refreshSession(api: BlueskyApi, account: BlueskyAccount): BlueskyAccount {
        log.info(R.string.log_bsky_token_expired)
        val session = runCatching {
            api.refreshSession("Bearer ${account.refreshJwt}")
        }.getOrElse {
            log.info(R.string.log_bsky_refresh_failed)
            // Auch dieser Pfad meldet HTTP-Fehler einheitlich als ApiException.
            apiCall { api.createSession(CreateSessionRequest(account.identifier, account.appPassword)) }
        }
        return account.copy(
            did = session.did,
            handle = session.handle,
            accessJwt = session.accessJwt,
            refreshJwt = session.refreshJwt,
        )
    }

    companion object {
        fun normalizePds(input: String): String {
            val trimmed = input.trim().removeSuffix("/")
            val withScheme = if (trimmed.startsWith("http")) trimmed else "https://$trimmed"
            return "$withScheme/"
        }

        /** Wandelt at://did/app.bsky.feed.post/rkey in eine bsky.app-Web-URL um. */
        fun atUriToWebUrl(atUri: String, handleOrDid: String): String {
            val rkey = atUri.substringAfterLast('/')
            return "https://bsky.app/profile/$handleOrDid/post/$rkey"
        }
    }
}
