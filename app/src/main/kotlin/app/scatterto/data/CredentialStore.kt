package app.scatterto.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.scatterto.data.model.BlueskyAccount
import app.scatterto.data.model.MammouthConfig
import app.scatterto.data.model.MastodonAccount
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Verschlüsselter Credential-Speicher (§8). Alle Tokens/Passwörter/JWTs liegen ausschließlich hier,
 * nie im Klartext, nie im Log.
 *
 * Nutzt bewusst EncryptedSharedPreferences trotz Deprecation (§12.1 Nr. 1): für einen
 * Ein-Nutzer-Client stabil und risikoarm; eine selbstgebaute Keystore-Lösung wäre fehleranfälliger.
 * Eine spätere Migration (AES/GCM + DataStore) bleibt möglich.
 */
class CredentialStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    fun loadMammouth(): MammouthConfig? = read(KEY_MAMMOUTH)
    fun saveMammouth(config: MammouthConfig) = write(KEY_MAMMOUTH, config)
    fun clearMammouth() = remove(KEY_MAMMOUTH)

    fun loadMastodon(): MastodonAccount? = read(KEY_MASTODON)
    fun saveMastodon(account: MastodonAccount) = write(KEY_MASTODON, account)
    fun clearMastodon() = remove(KEY_MASTODON)

    fun loadBluesky(): BlueskyAccount? = read(KEY_BLUESKY)
    fun saveBluesky(account: BlueskyAccount) = write(KEY_BLUESKY, account)
    fun clearBluesky() = remove(KEY_BLUESKY)

    private inline fun <reified T> read(key: String): T? =
        prefs.getString(key, null)?.let { json.decodeFromString<T>(it) }

    private inline fun <reified T> write(key: String, value: T) {
        prefs.edit().putString(key, json.encodeToString(value)).apply()
    }

    private fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    private companion object {
        const val FILE_NAME = "scatterto_credentials"
        const val KEY_MAMMOUTH = "mammouth"
        const val KEY_MASTODON = "mastodon"
        const val KEY_BLUESKY = "bluesky"
    }
}
