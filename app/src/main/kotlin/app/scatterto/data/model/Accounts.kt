package app.scatterto.data.model

import kotlinx.serialization.Serializable

/**
 * Domänenmodelle für die drei externen Dienste. Werden vom [app.scatterto.data.CredentialStore]
 * als JSON verschlüsselt abgelegt — daher @Serializable. Enthalten bewusst Credentials;
 * niemals loggen (§8).
 */

/** Ein auswählbares Mammouth-Modell (§4.1). */
data class ModelOption(val displayName: String, val modelId: String)

object MammouthModels {
    /** Die vier vordefinierten Optionen (§4.1). */
    val presets: List<ModelOption> = listOf(
        ModelOption("GPT-4.1 mini", "gpt-4.1-mini"),
        ModelOption("Mistral Medium 3.1", "mistral-medium-3.1"),
        ModelOption("Gemini 2.5 Flash", "gemini-2.5-flash"),
        ModelOption("Claude Haiku 4.5", "claude-haiku-4-5"),
    )

    /** Default: Mistral Medium 3.1 (EU-Anbieter, §4.1). */
    val default: ModelOption = presets[1]

    /** true, wenn [modelId] keinem Preset entspricht -> „Eigene Modell-ID" (§12.4 Nr. 3). */
    fun isCustom(modelId: String): Boolean = presets.none { it.modelId == modelId }
}

@Serializable
data class MammouthConfig(
    val token: String,
    val modelId: String = MammouthModels.default.modelId,
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
