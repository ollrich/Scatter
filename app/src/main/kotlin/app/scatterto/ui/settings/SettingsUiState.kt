package app.scatterto.ui.settings

import app.scatterto.data.mammouth.MammouthProvider
import app.scatterto.data.model.AccountInfo
import app.scatterto.data.model.AiBudget
import app.scatterto.data.model.AiService
import app.scatterto.data.model.Tonality

/** UI-Zustand des Einstellungsmenüs (§4). */
data class SettingsUiState(
    val mastodonInfo: AccountInfo? = null,
    val blueskyInfo: AccountInfo? = null,
    val accountInfoLoading: Boolean = false,

    // KI
    val aiEnabled: Boolean = false,
    val aiService: String = AiService.MAMMOUTH.key,
    val aiTokens: Map<String, String> = emptyMap(),
    val aiModels: Map<String, String> = emptyMap(),          // gewähltes Modell je Dienst
    val mammouthProvider: String = MammouthProvider.DEFAULT.key,
    val availableModels: List<String> = emptyList(),         // gefiltert für den aktuellen Kontext
    val modelsLoading: Boolean = false,
    val modelsError: Boolean = false,
    val aiValidation: ValidationState = ValidationState.None,
    val tonality: String = Tonality.DEFAULT.key,
    /** Guthaben des aktiven Dienstes; null = nicht abrufbar (alle außer Mammouth) oder noch nicht da. */
    val aiBudget: AiBudget? = null,

    // Mastodon
    val mastodonInstance: String = "",
    val mastodonToken: String = "",
    val mastodonConnected: Boolean = false,
    val mastodonHandle: String? = null,
    val mastodonAvatarUrl: String? = null,
    val mastodonError: String? = null,
    val mastodonConnecting: Boolean = false,
    val mastodonLanguage: String = "de",

    // Bluesky
    val blueskyIdentifier: String = "",
    val blueskyAppPassword: String = "",
    val blueskyPds: String = "https://bsky.social",
    val blueskyConnected: Boolean = false,
    val blueskyHandle: String? = null,
    val blueskyAvatarUrl: String? = null,
    val blueskyError: String? = null,
    val blueskyConnecting: Boolean = false,
    val blueskyLanguage: String = "en",
) {
    val activeAiService: AiService get() = AiService.fromKey(aiService)
    val currentToken: String get() = aiTokens[aiService].orEmpty()
    val currentModel: String get() = aiModels[aiService].orEmpty()
    val isMammouth: Boolean get() = aiService == AiService.MAMMOUTH.key
}

sealed interface ValidationState {
    data object None : ValidationState
    data object Validating : ValidationState
    data object Valid : ValidationState
    data class Invalid(val message: String) : ValidationState
}
