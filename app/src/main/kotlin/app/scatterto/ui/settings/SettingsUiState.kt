package app.scatterto.ui.settings

import app.scatterto.data.model.AccountInfo
import app.scatterto.data.model.AiService
import app.scatterto.data.model.ModelChoices

/** UI-Zustand des Einstellungsmenüs (§4). */
data class SettingsUiState(
    val mastodonInfo: AccountInfo? = null,
    val blueskyInfo: AccountInfo? = null,
    val accountInfoLoading: Boolean = false,

    // KI
    val aiEnabled: Boolean = true,
    val aiService: String = AiService.MAMMOUTH.key,
    val aiTokens: Map<String, String> = emptyMap(),
    val aiModels: Map<String, String> = emptyMap(),
    val mammouthChoiceKey: String = ModelChoices.DEFAULT_KEY,
    val mammouthCustomId: String = "",
    val aiValidation: ValidationState = ValidationState.None,

    // Mastodon
    val mastodonInstance: String = "",
    val mastodonToken: String = "",
    val mastodonConnected: Boolean = false,
    val mastodonHandle: String? = null,
    val mastodonAvatarUrl: String? = null,
    val mastodonError: String? = null,
    val mastodonConnecting: Boolean = false,

    // Bluesky
    val blueskyIdentifier: String = "",
    val blueskyAppPassword: String = "",
    val blueskyPds: String = "https://bsky.social",
    val blueskyConnected: Boolean = false,
    val blueskyHandle: String? = null,
    val blueskyAvatarUrl: String? = null,
    val blueskyError: String? = null,
    val blueskyConnecting: Boolean = false,
) {
    val activeAiService: AiService get() = AiService.fromKey(aiService)
    val currentToken: String get() = aiTokens[aiService].orEmpty()
    fun modelText(service: AiService): String = aiModels[service.key] ?: service.defaultModel

    /** true, wenn beim Mammouth-Dienst „Eigene Modell-ID" gewählt ist. */
    val isMammouthCustom: Boolean get() = mammouthChoiceKey == ModelChoices.CUSTOM_KEY
}

sealed interface ValidationState {
    data object None : ValidationState
    data object Validating : ValidationState
    data object Valid : ValidationState
    data class Invalid(val message: String) : ValidationState
}
