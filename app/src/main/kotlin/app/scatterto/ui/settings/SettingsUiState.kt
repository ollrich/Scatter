package app.scatterto.ui.settings

import app.scatterto.data.model.MammouthModels

/** UI-Zustand des Einstellungsmenüs (§4). */
data class SettingsUiState(
    // Mammouth
    val mammouthToken: String = "",
    val selectedModelId: String = MammouthModels.default.modelId,
    val customModelId: String = "",
    val mammouthValidation: ValidationState = ValidationState.None,

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
    /** true, wenn ein Modell außerhalb der Presets gewählt ist → Freitextfeld zeigen (§12.4 Nr. 3). */
    val isCustomModel: Boolean get() = MammouthModels.isCustom(selectedModelId) || selectedModelId == CUSTOM_SENTINEL

    companion object {
        const val CUSTOM_SENTINEL = "__custom__"
    }
}

sealed interface ValidationState {
    data object None : ValidationState
    data object Validating : ValidationState
    data object Valid : ValidationState
    data class Invalid(val message: String) : ValidationState
}
