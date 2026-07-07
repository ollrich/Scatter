package app.scatterto.data.bluesky

import app.scatterto.core.Facet
import app.scatterto.data.CredentialStore
import app.scatterto.data.model.BlueskyAccount
import app.scatterto.data.net.Network
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.time.Instant

/**
 * Bluesky-Anbindung (§4.2, §6, §7, §12.1 Nr. 4). Session wird persistiert und bei 401 transparent
 * erneuert; scheitert der Refresh, wird aus dem gespeicherten App-Password eine neue Session erzeugt.
 */
class BlueskyRepository(private val credentialStore: CredentialStore) {

    private val thumbnailer = ImageThumbnailer(Network.okHttp())

    private fun api(pdsUrl: String): BlueskyApi =
        Network.retrofit(normalizePds(pdsUrl), Network.okHttp()).create(BlueskyApi::class.java)

    /** Verbindet den Account: createSession + Profil (Avatar/Handle). */
    suspend fun connect(identifier: String, appPassword: String, pdsUrl: String): BlueskyAccount {
        val api = api(pdsUrl)
        val session = api.createSession(CreateSessionRequest(identifier.trim(), appPassword))
        val profile = runCatching {
            api.getProfile("Bearer ${session.accessJwt}", session.did)
        }.getOrNull()

        return BlueskyAccount(
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
    ): String {
        val api = api(account.pdsUrl)
        var current = account

        // 401-Retry mit transparentem Session-Refresh; aktualisiert & persistiert bei Bedarf.
        suspend fun <T> authed(call: suspend (String) -> T): T = try {
            call("Bearer ${current.accessJwt}")
        } catch (e: HttpException) {
            if (e.code() != 401) throw e
            current = refreshSession(api, current)
            credentialStore.saveBluesky(current)
            call("Bearer ${current.accessJwt}")
        }

        val thumbBlob = card?.imageUrl?.let { imageUrl ->
            runCatching {
                val jpeg = thumbnailer.downloadAsJpeg(imageUrl) ?: return@runCatching null
                val body = jpeg.toRequestBody("image/jpeg".toMediaType())
                authed { auth -> api.uploadBlob(auth, body).blob }
            }.getOrNull()
        }

        val embed = card?.let {
            ExternalEmbed(external = ExternalCard(it.uri, it.title, it.description, thumbBlob))
        }

        val record = PostRecord(
            text = text,
            createdAt = Instant.now().toString(),
            facets = facets.ifEmpty { null },
            embed = embed,
        )

        val result = authed { auth ->
            api.createRecord(auth, CreateRecordRequest(repo = current.did!!, record = record))
        }
        return atUriToWebUrl(result.uri, current.handle ?: current.did!!)
    }

    /** Refresh über refreshJwt; scheitert das, neue Session aus dem App-Password (§12.1 Nr. 4). */
    private suspend fun refreshSession(api: BlueskyApi, account: BlueskyAccount): BlueskyAccount {
        val session = runCatching {
            api.refreshSession("Bearer ${account.refreshJwt}")
        }.getOrElse {
            api.createSession(CreateSessionRequest(account.identifier, account.appPassword))
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
