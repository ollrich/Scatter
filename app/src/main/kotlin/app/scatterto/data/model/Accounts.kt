package app.scatterto.data.model

import kotlinx.serialization.Serializable

/**
 * Domänenmodelle für die drei externen Dienste. Werden vom [app.scatterto.data.CredentialStore]
 * als JSON verschlüsselt abgelegt — daher @Serializable. Enthalten bewusst Credentials;
 * niemals loggen (§8).
 */

/**
 * Alt-Format der Mammouth-Konfiguration (bis v0.7.x): entweder ein [provider]-Schlüssel oder eine
 * feste [fixedModelId]. Bleibt nur, damit [app.scatterto.data.CredentialStore] gespeicherte
 * Alt-Einstellungen nach [AiSettings] migrieren kann; neu geschrieben wird es nicht mehr.
 */
@Serializable
data class MammouthConfig(
    val token: String,
    val provider: String? = "mistral",
    val fixedModelId: String? = null,
)

@Serializable
data class MastodonAccount(
    val instanceUrl: String,
    val accessToken: String,
    val handle: String? = null,
    val avatarUrl: String? = null,
    /** Aus GET /api/v1/instance ausgelesen; Fallback 500 (§12.2 Nr. 7). */
    val maxCharacters: Int = 500,
    /** Post-Sprache (BCP-47). Leer = Alt-Konto -> Fallback Deutsch. */
    val postLanguage: String = "",
) {
    val effectiveLanguage: String get() = postLanguage.ifBlank { "de" }
}

@Serializable
data class BlueskyAccount(
    val identifier: String,
    val appPassword: String,
    /** Konfigurierbar, da eigene PDS möglich (§4.2). */
    val pdsUrl: String = "https://bsky.social",
    val did: String? = null,
    val handle: String? = null,
    val avatarUrl: String? = null,
    // Persistierte Session (§12.1 Nr. 4): nicht bei jedem Start neu erzeugen.
    val accessJwt: String? = null,
    val refreshJwt: String? = null,
    /** Post-Sprache (BCP-47). Leer = Alt-Konto -> Fallback Englisch. */
    val postLanguage: String = "",
) {
    val effectiveLanguage: String get() = postLanguage.ifBlank { "en" }
}
