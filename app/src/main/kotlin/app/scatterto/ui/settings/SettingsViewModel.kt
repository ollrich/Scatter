package app.scatterto.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.scatterto.data.AppContainer
import app.scatterto.R
import app.scatterto.data.model.AiService
import app.scatterto.data.model.AiSettings
import app.scatterto.data.model.ModelChoices
import app.scatterto.data.model.ModelProvider
import app.scatterto.data.net.ApiException
import kotlinx.coroutines.launch

/**
 * ViewModel für das Einstellungsmenü (§4): KI-Dienst/Token/Modell speichern,
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
        val ai = container.credentialStore.loadAiSettings()
        val mastodon = container.credentialStore.loadMastodon()
        val bluesky = container.credentialStore.loadBluesky()

        val mm = ai.models[AiService.MAMMOUTH.key]
        val providerKeys = ModelProvider.entries.map { it.key }
        val choiceKey = when {
            mm.isNullOrBlank() -> ModelChoices.DEFAULT_KEY
            mm in providerKeys -> mm
            // Die alte Auswahl „Mammouth-Empfohlen" ist kein aufrufbares Modell -> auf Standard migrieren.
            mm == ModelChoices.LEGACY_RECOMMENDED_ID -> ModelChoices.DEFAULT_KEY
            else -> ModelChoices.CUSTOM_KEY
        }
        val custom = if (choiceKey == ModelChoices.CUSTOM_KEY) mm.orEmpty() else ""

        uiState = uiState.copy(
            aiEnabled = ai.enabled,
            aiService = ai.activeService,
            aiTokens = ai.tokens,
            aiModels = ai.models,
            mammouthChoiceKey = choiceKey,
            mammouthCustomId = custom,
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

    // --- KI ---

    /** Master-Schalter: sofort persistiert, damit der Hauptschirm den Zustand kennt. */
    fun onAiEnabledChange(enabled: Boolean) {
        uiState = uiState.copy(aiEnabled = enabled)
        container.credentialStore.saveAiSettings(buildAiSettings())
    }

    fun onServiceSelect(key: String) {
        uiState = uiState.copy(aiService = key, aiValidation = ValidationState.None)
    }

    fun onAiTokenChange(value: String) {
        uiState = uiState.copy(aiTokens = uiState.aiTokens + (uiState.aiService to value))
    }

    fun onAiModelChange(value: String) {
        uiState = uiState.copy(aiModels = uiState.aiModels + (uiState.aiService to value))
    }

    fun onMammouthChoice(key: String) { uiState = uiState.copy(mammouthChoiceKey = key) }
    fun onMammouthCustomChange(value: String) { uiState = uiState.copy(mammouthCustomId = value) }

    fun saveAi() {
        val settings = buildAiSettings()
        container.credentialStore.saveAiSettings(settings)
        // Optionale Validierung (§4.1): Fehlschlag hindert das Speichern nicht (offline-tolerant).
        if (settings.enabled && settings.hasActiveToken) {
            validateAi(settings)
        } else {
            uiState = uiState.copy(aiValidation = ValidationState.None)
        }
    }

    private fun buildAiSettings(): AiSettings {
        val tokens = uiState.aiTokens
            .mapValues { it.value.trim() }
            .filterValues { it.isNotBlank() }
        val models = uiState.aiModels
            .mapValues { it.value.trim() }
            .filterValues { it.isNotBlank() }
            .toMutableMap()
        // Mammouth-Modell aus Dropdown/Custom-Feld ableiten.
        val mammouthModel = if (uiState.mammouthChoiceKey == ModelChoices.CUSTOM_KEY) {
            uiState.mammouthCustomId.trim()
        } else {
            uiState.mammouthChoiceKey
        }
        if (mammouthModel.isNotBlank()) {
            models[AiService.MAMMOUTH.key] = mammouthModel
        } else {
            models.remove(AiService.MAMMOUTH.key)
        }
        return AiSettings(
            enabled = uiState.aiEnabled,
            activeService = uiState.aiService,
            tokens = tokens,
            models = models,
        )
    }

    private fun validateAi(settings: AiSettings) {
        uiState = uiState.copy(aiValidation = ValidationState.Validating)
        viewModelScope.launch {
            uiState = try {
                if (container.aiRepository.validate(settings)) {
                    uiState.copy(aiValidation = ValidationState.Valid)
                } else {
                    uiState.copy(aiValidation = ValidationState.Invalid(str(R.string.validate_invalid)))
                }
            } catch (e: Exception) {
                uiState.copy(aiValidation = ValidationState.Invalid(str(R.string.validate_offline)))
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
