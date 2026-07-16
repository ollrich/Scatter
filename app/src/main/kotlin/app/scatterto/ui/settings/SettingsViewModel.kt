package app.scatterto.ui.settings

import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.scatterto.data.AppContainer
import app.scatterto.R
import app.scatterto.data.mammouth.MammouthProvider
import app.scatterto.data.mammouth.ModelCatalog
import app.scatterto.data.model.AiService
import app.scatterto.data.model.AiSettings
import app.scatterto.data.model.PostLanguages
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

    /** Rohe Mammouth-Modell-Liste (alle Anbieter) — zum Umfiltern bei Anbieterwechsel ohne Neuabruf. */
    private var mammouthAllModels: List<String> = emptyList()

    private fun load() {
        val ai = container.credentialStore.loadAiSettings()
        val mastodon = container.credentialStore.loadMastodon()
        val bluesky = container.credentialStore.loadBluesky()

        val mammouthModel = ai.models[AiService.MAMMOUTH.key].orEmpty()
        val provider = MammouthProvider.ofModel(mammouthModel) ?: MammouthProvider.DEFAULT

        uiState = uiState.copy(
            aiEnabled = ai.enabled,
            aiService = ai.activeService,
            aiTokens = ai.tokens,
            aiModels = ai.models,
            tonality = ai.activeTonality.key,
            mammouthProvider = provider.key,
            mastodonInstance = mastodon?.instanceUrl.orEmpty(),
            mastodonConnected = mastodon != null,
            mastodonHandle = mastodon?.handle,
            mastodonAvatarUrl = mastodon?.avatarUrl,
            mastodonLanguage = mastodon?.effectiveLanguage ?: "de",
            blueskyIdentifier = bluesky?.identifier.orEmpty(),
            blueskyPds = bluesky?.pdsUrl ?: uiState.blueskyPds,
            blueskyConnected = bluesky != null,
            blueskyHandle = bluesky?.handle,
            blueskyAvatarUrl = bluesky?.avatarUrl,
            blueskyLanguage = bluesky?.effectiveLanguage ?: "en",
        )
        // Modelle des aktiven Dienstes laden, wenn schon ein Token gespeichert ist.
        if (uiState.currentToken.isNotBlank()) refreshModels()
    }

    // --- KI ---

    /**
     * Master-Schalter: nur den enabled-Flag persistieren (nicht den halb editierten UI-Zustand mit
     * Token/Modell). Der Hauptschirm braucht den Zustand sofort; alles andere geht über „Speichern".
     */
    fun onAiEnabledChange(enabled: Boolean) {
        uiState = uiState.copy(aiEnabled = enabled)
        val stored = container.credentialStore.loadAiSettings()
        container.credentialStore.saveAiSettings(stored.copy(enabled = enabled))
    }

    /**
     * Tonalität wirkt sofort und wird sofort persistiert — wie der Master-Schalter. Eine Radio-
     * Auswahl, die erst nach „Speichern" gilt, wäre nicht erwartbar; sie hängt auch an keinem
     * halb editierten Token.
     */
    fun onTonalitySelect(key: String) {
        uiState = uiState.copy(tonality = key)
        val stored = container.credentialStore.loadAiSettings()
        container.credentialStore.saveAiSettings(stored.copy(tonality = key))
    }

    fun onServiceSelect(key: String) {
        if (key == uiState.aiService) return
        mammouthAllModels = emptyList()
        uiState = uiState.copy(
            aiService = key,
            aiValidation = ValidationState.None,
            availableModels = emptyList(),
            modelsError = false,
        )
        if (uiState.currentToken.isNotBlank()) refreshModels()
    }

    fun onAiTokenChange(value: String) {
        uiState = uiState.copy(aiTokens = uiState.aiTokens + (uiState.aiService to value))
    }

    fun onMammouthProviderSelect(key: String) {
        uiState = uiState.copy(mammouthProvider = key)
        uiState = withModels(ModelCatalog.mammouthModels(MammouthProvider.fromKey(key), mammouthAllModels))
    }

    fun onModelSelect(id: String) {
        uiState = uiState.copy(aiModels = uiState.aiModels + (uiState.aiService to id))
    }

    /** Modell-Liste des aktiven Dienstes laden — nur nach Token-Eingabe (§4.1). */
    fun refreshModels() {
        val service = uiState.activeAiService
        val token = uiState.currentToken.trim()
        if (token.isBlank()) return
        uiState = uiState.copy(modelsLoading = true, modelsError = false)
        viewModelScope.launch {
            val result = runCatching { container.aiRepository.availableModels(service, token) }
            // Dienst zwischenzeitlich gewechselt? Dann veraltetes Ergebnis verwerfen.
            if (uiState.aiService != service.key) return@launch
            uiState = result.fold(
                onSuccess = { ids ->
                    val filtered = if (service == AiService.MAMMOUTH) {
                        mammouthAllModels = ids
                        ModelCatalog.mammouthModels(MammouthProvider.fromKey(uiState.mammouthProvider), ids)
                    } else {
                        ModelCatalog.directModels(service, ids)
                    }
                    withModels(filtered)
                },
                onFailure = {
                    uiState.copy(modelsLoading = false, modelsError = true, availableModels = emptyList())
                },
            )
        }
    }

    /** Übernimmt die gefilterte Liste und wählt ein Modell vor (Bestand behalten, sonst neuestes). */
    private fun withModels(models: List<String>): SettingsUiState {
        // Leere Liste (noch nicht geladen bzw. gefiltert leer): die bestehende Auswahl NICHT
        // überschreiben — sonst geht beim Anbieter-Durchklicken ohne Liste das Modell verloren.
        if (models.isEmpty()) {
            return uiState.copy(availableModels = emptyList(), modelsLoading = false, modelsError = false)
        }
        val current = uiState.currentModel
        val selected = if (current.isNotBlank() && current in models) current else models.first()
        return uiState.copy(
            availableModels = models,
            modelsLoading = false,
            modelsError = false,
            aiModels = uiState.aiModels + (uiState.aiService to selected),
        )
    }

    fun saveAi() {
        val settings = buildAiSettings()
        container.credentialStore.saveAiSettings(settings)
        // Nach dem Speichern (Token ist jetzt fertig) die Modell-Liste laden, falls noch keine da ist.
        if (settings.hasActiveToken && uiState.availableModels.isEmpty()) refreshModels()
        // Optionale Validierung (§4.1): Fehlschlag hindert das Speichern nicht (offline-tolerant).
        if (settings.enabled && settings.hasActiveToken) {
            validateAi(settings)
        } else {
            uiState = uiState.copy(aiValidation = ValidationState.None)
        }
    }

    private fun buildAiSettings(): AiSettings {
        val stored = container.credentialStore.loadAiSettings()
        val tokens = uiState.aiTokens
            .mapValues { it.value.trim() }
            .filterValues { it.isNotBlank() }
        // Gespeicherte Modelle als Basis; nur nicht-leere UI-Werte überschreiben — eine leere
        // Auswahl (Liste noch nicht geladen) darf einen gespeicherten Wert nie löschen.
        val models = stored.models.toMutableMap()
        uiState.aiModels.forEach { (service, value) ->
            val trimmed = value.trim()
            if (trimmed.isNotBlank()) models[service] = trimmed
        }
        return AiSettings(
            enabled = uiState.aiEnabled,
            activeService = uiState.aiService,
            tokens = tokens,
            models = models,
            tonality = uiState.tonality,
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
                    .copy(postLanguage = deviceLanguage())
                container.credentialStore.saveMastodon(account)
                uiState.copy(
                    mastodonConnecting = false,
                    mastodonConnected = true,
                    mastodonHandle = account.handle,
                    mastodonAvatarUrl = account.avatarUrl,
                    mastodonLanguage = account.effectiveLanguage,
                    mastodonToken = "",
                )
            } catch (e: ApiException) {
                uiState.copy(mastodonConnecting = false, mastodonError = e.error.readable)
            } catch (e: Exception) {
                uiState.copy(mastodonConnecting = false, mastodonError = e.message ?: str(R.string.error_connect_failed))
            }
        }
    }

    /** Post-Sprache eines verbundenen Mastodon-Kontos ändern (persistiert sofort). */
    fun onMastodonLanguageChange(tag: String) {
        val account = container.credentialStore.loadMastodon() ?: return
        container.credentialStore.saveMastodon(account.copy(postLanguage = tag))
        uiState = uiState.copy(mastodonLanguage = tag)
    }

    private fun deviceLanguage(): String =
        PostLanguages.normalizedOrEnglish(
            Resources.getSystem().configuration.locales.get(0).toLanguageTag(),
        )

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
                ).copy(postLanguage = deviceLanguage())
                container.credentialStore.saveBluesky(account)
                uiState.copy(
                    blueskyConnecting = false,
                    blueskyConnected = true,
                    blueskyHandle = account.handle,
                    blueskyAvatarUrl = account.avatarUrl,
                    blueskyLanguage = account.effectiveLanguage,
                    blueskyAppPassword = "",
                )
            } catch (e: ApiException) {
                uiState.copy(blueskyConnecting = false, blueskyError = e.error.readable)
            } catch (e: Exception) {
                uiState.copy(blueskyConnecting = false, blueskyError = e.message ?: str(R.string.error_connect_failed))
            }
        }
    }

    /** Post-Sprache eines verbundenen Bluesky-Kontos ändern (persistiert sofort). */
    fun onBlueskyLanguageChange(tag: String) {
        val account = container.credentialStore.loadBluesky() ?: return
        container.credentialStore.saveBluesky(account.copy(postLanguage = tag))
        uiState = uiState.copy(blueskyLanguage = tag)
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
