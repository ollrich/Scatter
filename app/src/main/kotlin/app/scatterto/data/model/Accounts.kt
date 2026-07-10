package app.scatterto.data.model

import kotlinx.serialization.Serializable

/**
 * Domänenmodelle für die drei externen Dienste. Werden vom [app.scatterto.data.CredentialStore]
 * als JSON verschlüsselt abgelegt — daher @Serializable. Enthalten bewusst Credentials;
 * niemals loggen (§8).
 */

/** Die vier Anbieter, hinter denen die App dynamisch das aktuelle Flaggschiff auflöst (§4.1). */
enum class ModelProvider(val key: String, val displayName: String) {
    MISTRAL("mistral", "Mistral (Large)"),
    CLAUDE("claude", "Claude (Opus)"),
    GPT("gpt", "GPT"),
    GEMINI("gemini", "Gemini (Pro)"),
}

/** Ein Eintrag im Modell-Dropdown (§12.4 Nr. 3): Anbieter, „Empfohlen" oder Freitext. */
data class ModelChoiceEntry(val key: String, val label: String)

object ModelChoices {
    const val CUSTOM_KEY = "custom"
    const val DEFAULT_KEY = "mistral"

    /**
     * `mammouth-recommended` steht zwar in /v1/models, ist aber kein aufrufbares Modell, sondern ein
     * OpenRouter-Preset (404 „preset_not_found"). Daher kein Dropdown-Eintrag — die Konstante bleibt
     * nur, um alte gespeicherte Einstellungen darauf zu migrieren.
     */
    const val LEGACY_RECOMMENDED_ID = "mammouth-recommended"

    val entries: List<ModelChoiceEntry> =
        ModelProvider.entries.map { ModelChoiceEntry(it.key, it.displayName) } +
            ModelChoiceEntry(CUSTOM_KEY, "Eigene Modell-ID…")
}

/**
 * Modellauswahl: entweder ein [provider] (App löst zur Laufzeit das aktuelle Flaggschiff auf)
 * oder eine feste [fixedModelId] („Empfohlen" bzw. Custom-ID). fixedModelId hat Vorrang.
 */
@Serializable
data class MammouthConfig(
    val token: String,
    val provider: String? = ModelChoices.DEFAULT_KEY,
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
)

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
)
