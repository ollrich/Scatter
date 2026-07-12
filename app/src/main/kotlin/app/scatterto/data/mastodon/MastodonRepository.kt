package app.scatterto.data.mastodon

import app.scatterto.data.model.MastodonAccount
import app.scatterto.data.net.Network
import app.scatterto.data.net.apiCall
import java.util.concurrent.ConcurrentHashMap

/**
 * Mastodon-Anbindung (§4.2, §5.5, §7). Basis-URL ist instanzabhängig; die API-Instanz wird pro
 * Instanz-URL gecacht (teilt den gemeinsamen OkHttp-Client). HTTP-Fehler verlassen das Repository
 * einheitlich als [app.scatterto.data.net.ApiException].
 */
class MastodonRepository {

    private val apis = ConcurrentHashMap<String, MastodonApi>()

    private fun api(instanceUrl: String): MastodonApi =
        apis.getOrPut(instanceUrl) {
            Network.retrofit(instanceUrl).create(MastodonApi::class.java)
        }

    /**
     * Verbindet den Account: verify_credentials (Handle/Avatar) + Instanz-Zeichenlimit (§12.2 Nr. 7).
     */
    suspend fun connect(instanceUrlInput: String, token: String): MastodonAccount = apiCall {
        val instanceUrl = normalizeInstanceUrl(instanceUrlInput)
        val api = api(instanceUrl)
        val account = api.verifyCredentials(bearer(token))
        val maxChars = runCatching { api.instance().configuration?.statuses?.maxCharacters }
            .getOrNull() ?: DEFAULT_MAX_CHARACTERS

        MastodonAccount(
            instanceUrl = instanceUrl,
            accessToken = token,
            handle = account.acct.ifBlank { account.username },
            avatarUrl = account.avatar,
            maxCharacters = maxChars,
        )
    }

    /**
     * Postet [text]. [idempotencyKey] bleibt über Retries desselben Inhalts stabil, damit ein
     * verlorener Response keinen Doppelpost erzeugt (§12.1 Nr. 5). Gibt die URL des Posts zurück.
     * [language] ist der BCP-47-Code des Post-Texts (Vorbereitung Multi-Language).
     */
    suspend fun post(
        account: MastodonAccount,
        text: String,
        idempotencyKey: String,
        language: String = "de",
    ): String? = apiCall {
        api(account.instanceUrl).postStatus(
            authorization = bearer(account.accessToken),
            idempotencyKey = idempotencyKey,
            body = StatusRequest(status = text, language = language),
        ).url
    }

    private fun bearer(token: String) = "Bearer $token"

    companion object {
        const val DEFAULT_MAX_CHARACTERS = 500

        /** Ergänzt https:// und einen abschließenden „/" (Retrofit-Basis-URL-Anforderung). */
        fun normalizeInstanceUrl(input: String): String {
            val trimmed = input.trim().removeSuffix("/")
            val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed
            } else {
                "https://$trimmed"
            }
            return "$withScheme/"
        }
    }
}
