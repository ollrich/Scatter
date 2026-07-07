package app.scatterto.data.mastodon

import app.scatterto.data.model.MastodonAccount
import app.scatterto.data.net.Network

/**
 * Mastodon-Anbindung (§4.2, §5.5, §7). Basis-URL ist instanzabhängig, daher wird der
 * Retrofit-Client pro Instanz gebaut.
 */
class MastodonRepository {

    private fun api(instanceUrl: String): MastodonApi =
        Network.retrofit(instanceUrl, Network.okHttp()).create(MastodonApi::class.java)

    /**
     * Verbindet den Account: verify_credentials (Handle/Avatar) + Instanz-Zeichenlimit (§12.2 Nr. 7).
     */
    suspend fun connect(instanceUrlInput: String, token: String): MastodonAccount {
        val instanceUrl = normalizeInstanceUrl(instanceUrlInput)
        val api = api(instanceUrl)
        val account = api.verifyCredentials(bearer(token))
        val maxChars = runCatching { api.instance().configuration?.statuses?.maxCharacters }
            .getOrNull() ?: DEFAULT_MAX_CHARACTERS

        return MastodonAccount(
            instanceUrl = instanceUrl,
            accessToken = token,
            handle = account.acct.ifBlank { account.username },
            avatarUrl = account.avatar,
            maxCharacters = maxChars,
        )
    }

    /**
     * Postet [text]. [idempotencyKey] bleibt über Retries stabil, damit ein verlorener Response
     * keinen Doppelpost erzeugt (§12.1 Nr. 5). Gibt die URL des Posts zurück.
     */
    suspend fun post(account: MastodonAccount, text: String, idempotencyKey: String): String? {
        val status = api(account.instanceUrl).postStatus(
            authorization = bearer(account.accessToken),
            idempotencyKey = idempotencyKey,
            body = StatusRequest(status = text, language = "de"),
        )
        return status.url
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
