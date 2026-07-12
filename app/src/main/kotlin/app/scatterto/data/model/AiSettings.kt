package app.scatterto.data.model

import kotlinx.serialization.Serializable

/** Verfügbare KI-Dienste (Reihenfolge = UI-Reihenfolge). */
enum class AiService(
    val key: String,
    val displayName: String,
    val defaultModel: String,
) {
    MAMMOUTH("mammouth", "Mammouth", ModelChoices.DEFAULT_KEY),
    CLAUDE("claude", "Claude", "claude-sonnet-4-5"),
    OPENAI("openai", "OpenAI", "gpt-4.1"),
    GEMINI("gemini", "Gemini", "gemini-2.5-flash");

    companion object {
        fun fromKey(key: String?): AiService = entries.firstOrNull { it.key == key } ?: MAMMOUTH
    }
}

/**
 * KI-Konfiguration (§4.1). Optional abschaltbar; pro Dienst eigener Token und eigenes Modell,
 * damit das Umschalten ohne Neu-Eingabe geht. Ersetzt das frühere MammouthConfig (mit Migration).
 *
 * Standard ist KI AUS: eine frische Installation startet ohne KI (Nutzer schreibt selbst). Wer aus
 * einem alten MammouthConfig migriert, behält KI an (siehe CredentialStore.loadAiSettings).
 */
@Serializable
data class AiSettings(
    val enabled: Boolean = false,
    val activeService: String = AiService.MAMMOUTH.key,
    val tokens: Map<String, String> = emptyMap(),
    val models: Map<String, String> = emptyMap(),
) {
    fun token(service: AiService): String = tokens[service.key].orEmpty()
    fun model(service: AiService): String = models[service.key]?.ifBlank { null } ?: service.defaultModel
    val active: AiService get() = AiService.fromKey(activeService)
    val hasActiveToken: Boolean get() = token(active).isNotBlank()
}
