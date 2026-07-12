package app.scatterto.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.scatterto.data.AppContainer
import app.scatterto.R
import app.scatterto.data.model.MammouthConfig
import app.scatterto.data.model.ModelChoices
import app.scatterto.data.net.ApiException
import kotlinx.coroutines.launch

/**
 * ViewModel für das Einstellungsmenü (§4): Mammouth-Token/Modellauswahl speichern,
 * Accounts verbinden/trennen.
 */
class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    private fun str(id: Int): String = container.appContext.getString(id)

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
            // Die alte Auswahl „Mammouth-Empfohlen" ist kein aufrufbares Modell -> auf Standard migrieren.
            mammouth.fixedModelId == ModelChoices.LEGACY_RECOMMENDED_ID -> ModelChoices.DEFAULT_KEY
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
                    uiState.copy(mammouthValidation = ValidationState.Invalid(str(R.string.validate_invalid)))
                }
            } catch (e: Exception) {
                uiState.copy(mammouthValidation = ValidationState.Invalid(str(R.string.validate_offline)))
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
            } catch (e: ApiException) {
                uiState.copy(mastodonConnecting = false, mastodonError = e.error.readable)
            } catch (e: Exception) {
                uiState.copy(mastodonConnecting = false, mastodonError = e.message ?: str(R.string.error_connect_failed))
            }
        }
    }

    fun disconnectMastodon() {
        container.credentialStore.clearMastodon()
        uiState = uiState.copy(
            mastodonConnected = false,
            mastodonHandle = null,
            mastodonAvatarUrl = null,
            mastodonInfo = null,
        )
    }

    /** Lädt Live-Detailinfos beider verbundener Konten (Follower, Mitglied seit, letztes Posting). */
    fun loadAccountInfo() {
        val mastodon = container.credentialStore.loadMastodon()
        val bluesky = container.credentialStore.loadBluesky()
        if (mastodon == null && bluesky == null) return
        uiState = uiState.copy(accountInfoLoading = true)
        viewModelScope.launch {
            val mInfo = mastodon?.let {
                runCatching { container.mastodonRepository.accountInfo(it) }.getOrNull()
            }
            val bInfo = bluesky?.let {
                runCatching { container.blueskyRepository.accountInfo(it) }.getOrNull()
            }
            uiState = uiState.copy(
                mastodonInfo = mInfo,
                blueskyInfo = bInfo,
                accountInfoLoading = false,
            )
        }
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
            } catch (e: ApiException) {
                uiState.copy(blueskyConnecting = false, blueskyError = e.error.readable)
            } catch (e: Exception) {
                uiState.copy(blueskyConnecting = false, blueskyError = e.message ?: str(R.string.error_connect_failed))
            }
        }
    }

    fun disconnectBluesky() {
        container.credentialStore.clearBluesky()
        uiState = uiState.copy(
            blueskyConnected = false,
            blueskyHandle = null,
            blueskyAvatarUrl = null,
            blueskyInfo = null,
        )
    }
}
