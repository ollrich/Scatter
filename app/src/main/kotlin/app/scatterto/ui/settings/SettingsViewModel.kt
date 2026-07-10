package app.scatterto.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.scatterto.data.AppContainer
import app.scatterto.data.model.MammouthConfig
import app.scatterto.data.model.ModelChoices
import kotlinx.coroutines.launch

/**
 * ViewModel für das Einstellungsmenü (§4): Mammouth-Token/Modellauswahl speichern,
 * Accounts verbinden/trennen.
 */
class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        load()
    }

    private fun load() {
        val mammouth = container.credentialStore.loadMammouth()
        val mastodon = container.credentialStore.loadMastodon()
        val bluesky = container.credentialStore.loadBluesky()

        val choiceKey = when {
            mammouth == null -> ModelChoices.DEFAULT_KEY
            mammouth.provider != null -> mammouth.provider
            mammouth.fixedModelId == ModelChoices.RECOMMENDED_ID -> ModelChoices.RECOMMENDED_KEY
            else -> ModelChoices.CUSTOM_KEY
        }
        val custom = if (choiceKey == ModelChoices.CUSTOM_KEY) mammouth?.fixedModelId.orEmpty() else ""

        uiState = uiState.copy(
            mammouthToken = mammouth?.token.orEmpty(),
            modelChoiceKey = choiceKey,
            customModelId = custom,
            mastodonInstance = mastodon?.instanceUrl.orEmpty(),
            mastodonConnected = mastodon != null,
            mastodonHandle = mastodon?.handle,
            mastodonAvatarUrl = mastodon?.avatarUrl,
            blueskyIdentifier = bluesky?.identifier.orEmpty(),
            blueskyPds = bluesky?.pdsUrl ?: uiState.blueskyPds,
            blueskyConnected = bluesky != null,
            blueskyHandle = bluesky?.handle,
            blueskyAvatarUrl = bluesky?.avatarUrl,
        )
    }

    // --- Mammouth ---

    fun onMammouthTokenChange(value: String) { uiState = uiState.copy(mammouthToken = value) }
    fun onModelChoice(key: String) { uiState = uiState.copy(modelChoiceKey = key) }
    fun onCustomModelChange(value: String) { uiState = uiState.copy(customModelId = value) }

    fun saveMammouth() {
        if (uiState.mammouthToken.isBlank()) return
        val token = uiState.mammouthToken.trim()
        val config = when (uiState.modelChoiceKey) {
            ModelChoices.RECOMMENDED_KEY ->
                MammouthConfig(token, provider = null, fixedModelId = ModelChoices.RECOMMENDED_ID)
            ModelChoices.CUSTOM_KEY -> {
                if (uiState.customModelId.isBlank()) return
                MammouthConfig(token, provider = null, fixedModelId = uiState.customModelId.trim())
            }
            else -> MammouthConfig(token, provider = uiState.modelChoiceKey, fixedModelId = null)
        }
        container.credentialStore.saveMammouth(config)
        // Optionale Validierung (§4.1): Fehlschlag hindert das Speichern nicht (offline-tolerant).
        validateMammouth(config)
    }

    private fun validateMammouth(config: MammouthConfig) {
        uiState = uiState.copy(mammouthValidation = ValidationState.Validating)
        viewModelScope.launch {
            uiState = try {
                if (container.mammouthRepository.validate(config)) {
                    uiState.copy(mammouthValidation = ValidationState.Valid)
                } else {
                    uiState.copy(mammouthValidation = ValidationState.Invalid("Token/Modell nicht prüfbar"))
                }
            } catch (e: Exception) {
                uiState.copy(mammouthValidation = ValidationState.Invalid("Nicht prüfbar (offline?)"))
            }
        }
    }

    // --- Mastodon ---

    fun onMastodonInstanceChange(value: String) { uiState = uiState.copy(mastodonInstance = value) }
    fun onMastodonTokenChange(value: String) { uiState = uiState.copy(mastodonToken = value) }

    fun connectMastodon() {
        if (uiState.mastodonInstance.isBlank() || uiState.mastodonToken.isBlank()) return
        uiState = uiState.copy(mastodonConnecting = true, mastodonError = null)
        viewModelScope.launch {
            uiState = try {
                val account = container.mastodonRepository
                    .connect(uiState.mastodonInstance, uiState.mastodonToken.trim())
                container.credentialStore.saveMastodon(account)
                uiState.copy(
                    mastodonConnecting = false,
                    mastodonConnected = true,
                    mastodonHandle = account.handle,
                    mastodonAvatarUrl = account.avatarUrl,
                    mastodonToken = "",
                )
            } catch (e: Exception) {
                uiState.copy(mastodonConnecting = false, mastodonError = e.message ?: "Verbindung fehlgeschlagen")
            }
        }
    }

    fun disconnectMastodon() {
        container.credentialStore.clearMastodon()
        uiState = uiState.copy(mastodonConnected = false, mastodonHandle = null, mastodonAvatarUrl = null)
    }

    // --- Bluesky ---

    fun onBlueskyIdentifierChange(value: String) { uiState = uiState.copy(blueskyIdentifier = value) }
    fun onBlueskyPasswordChange(value: String) { uiState = uiState.copy(blueskyAppPassword = value) }
    fun onBlueskyPdsChange(value: String) { uiState = uiState.copy(blueskyPds = value) }

    fun connectBluesky() {
        if (uiState.blueskyIdentifier.isBlank() || uiState.blueskyAppPassword.isBlank()) return
        uiState = uiState.copy(blueskyConnecting = true, blueskyError = null)
        viewModelScope.launch {
            uiState = try {
                val account = container.blueskyRepository.connect(
                    identifier = uiState.blueskyIdentifier,
                    appPassword = uiState.blueskyAppPassword,
                    pdsUrl = uiState.blueskyPds.ifBlank { "https://bsky.social" },
                )
                container.credentialStore.saveBluesky(account)
                uiState.copy(
                    blueskyConnecting = false,
                    blueskyConnected = true,
                    blueskyHandle = account.handle,
                    blueskyAvatarUrl = account.avatarUrl,
                    blueskyAppPassword = "",
                )
            } catch (e: Exception) {
                uiState.copy(blueskyConnecting = false, blueskyError = e.message ?: "Verbindung fehlgeschlagen")
            }
        }
    }

    fun disconnectBluesky() {
        container.credentialStore.clearBluesky()
        uiState = uiState.copy(blueskyConnected = false, blueskyHandle = null, blueskyAvatarUrl = null)
    }
}
