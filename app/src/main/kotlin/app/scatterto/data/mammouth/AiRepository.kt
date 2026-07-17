package app.scatterto.data.mammouth

import app.scatterto.R
import app.scatterto.data.log.EventLog
import app.scatterto.data.metadata.PageMetadata
import app.scatterto.data.model.AiBudget
import app.scatterto.data.model.AiService
import app.scatterto.data.model.AiSettings
import app.scatterto.data.model.GeneratedPosts
import app.scatterto.data.net.Network
import app.scatterto.data.net.apiCall
import retrofit2.HttpException

/**
 * KI-Generierung über den gewählten Dienst (§5.3). Mammouth und OpenAI nutzen dasselbe
 * OpenAI-kompatible Schema, Claude die Anthropic-Messages-API, Gemini die generateContent-API.
 * Prompt-Bau ([PromptBuilder]) und JSON-Parsing ([AiResponseParser]) sind dienstunabhängig.
 */
class AiRepository(private val log: EventLog) {

    private val client by lazy { Network.okHttp(readTimeoutSeconds = 90) }

    private val mammouthApi by lazy { openAiApi("https://api.mammouth.ai/v1/") }
    private val openAiApi by lazy { openAiApi("https://api.openai.com/v1/") }
    private val anthropicApi by lazy {
        Network.retrofit("https://api.anthropic.com/", client).create(AnthropicApi::class.java)
    }
    private val geminiApi by lazy {
        Network.retrofit("https://generativelanguage.googleapis.com/", client).create(GeminiApi::class.java)
    }

    private fun openAiApi(baseUrl: String): MammouthApi =
        Network.retrofit(baseUrl, client).create(MammouthApi::class.java)

    private var mammouthModelCache: List<String> = emptyList()
    private var mammouthCacheAt = 0L

    suspend fun generate(
        settings: AiSettings,
        metadata: PageMetadata,
        medium: String?,
        targets: List<GenTarget>,
    ): GeneratedPosts {
        val service = settings.active
        val token = settings.token(service)
        val model = resolveModel(service, settings, token)
        log.info(R.string.log_ai_generating, service.displayName, model, medium ?: log.string(R.string.log_unknown))

        val system = PromptBuilder.system(targets, settings.activeTonality)
        val user = PromptBuilder.user(medium, metadata.title, metadata.description, targets)

        val content = when (service) {
            AiService.MAMMOUTH -> openAiComplete(mammouthApi, token, model, system, user)
            AiService.OPENAI -> openAiComplete(openAiApi, token, model, system, user)
            AiService.CLAUDE -> claudeComplete(token, model, system, user)
            AiService.GEMINI -> geminiComplete(token, model, system, user)
        }
        return AiResponseParser.parse(
            content,
            wantMastodon = targets.any { it.key == "mastodon" },
            wantBluesky = targets.any { it.key == "bluesky" },
        )
    }

    /** Token-/Modell-Validierung (§4.1); für Mammouth/OpenAI via Modell-Liste, sonst nur Präsenz. */
    suspend fun validate(settings: AiSettings): Boolean {
        val service = settings.active
        val token = settings.token(service)
        if (token.isBlank()) return false
        return when (service) {
            AiService.MAMMOUTH, AiService.OPENAI -> {
                val api = if (service == AiService.MAMMOUTH) mammouthApi else openAiApi
                val ids = runCatching { api.models(bearer(token)).data.map { it.id } }.getOrDefault(emptyList())
                val model = settings.model(service)
                // Liste abrufbar = Token gültig; bei OpenAI zusätzlich das gewählte Modell prüfen
                // (leer = noch keins gewählt, zählt nicht als Fehler).
                ids.isNotEmpty() && (service == AiService.MAMMOUTH || model.isBlank() || model in ids)
            }
            else -> true // Claude/Gemini: erst beim Generieren geprüft
        }
    }

    // --- OpenAI-kompatibel (Mammouth, OpenAI) ---

    private suspend fun openAiComplete(api: MammouthApi, token: String, model: String, system: String, user: String): String? {
        val request = ChatRequest(
            model = model,
            messages = listOf(ChatMessage("system", system), ChatMessage("user", user)),
            responseFormat = ResponseFormat("json_object"),
        )
        // apiCall MUSS außen liegen: sonst ist die HttpException schon zur ApiException geworden und
        // der 400-Fallback (Retry ohne response_format/temperature) griffe nie.
        return apiCall {
            try {
                api.chat(bearer(token), request).firstContent()
            } catch (e: HttpException) {
                if (e.code() == 400) {
                    log.info(R.string.log_ai_retry_400)
                    api.chat(bearer(token), request.copy(responseFormat = null, temperature = null)).firstContent()
                } else {
                    throw e
                }
            }
        }
    }

    // --- Anthropic (Claude) ---

    private suspend fun claudeComplete(token: String, model: String, system: String, user: String): String? = apiCall {
        anthropicApi.messages(
            apiKey = token,
            version = ANTHROPIC_VERSION,
            request = AnthropicRequest(
                model = model,
                maxTokens = 1024,
                system = system,
                messages = listOf(AnthropicMessage("user", user)),
            ),
        ).firstText()
    }

    // --- Google Gemini ---

    private suspend fun geminiComplete(token: String, model: String, system: String, user: String): String? = apiCall {
        geminiApi.generate(
            apiKey = token,
            model = model,
            request = GeminiRequest(
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(system))),
                contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(user)))),
            ),
        ).firstText()
    }

    // --- Guthaben (nur Mammouth) ---

    /**
     * Guthabenstand des Accounts, oder null wenn der Dienst das nicht hergibt bzw. der Abruf
     * scheitert. Bewusst nicht werfend: die Anzeige ist Beiwerk und darf nie das Menü stören —
     * das Schema ist undokumentiert und kann jederzeit brechen.
     *
     * Zwei Calls, weil das Budget am Account hängt und nicht am Key: erst die `user_id` aus
     * `/key/info` holen, dann damit `/user/info` abfragen.
     */
    suspend fun budget(service: AiService, token: String): AiBudget? {
        if (service != AiService.MAMMOUTH || token.isBlank()) return null
        return runCatching {
            val userId = mammouthApi.keyInfo(bearer(token)).info.userId ?: return null
            val info = mammouthApi.userInfo(bearer(token), userId).userInfo
            val max = info.maxBudget ?: return null // ohne Limit gibt es nichts anzuzeigen
            AiBudget(spent = info.spend, max = max, resetAt = info.budgetResetAt)
        }.getOrNull()
    }

    // --- Modell-Listen (für die Einstellungs-Dropdowns) ---

    /**
     * Rohe Modell-Liste eines Dienstes (ungefiltert — die UI filtert via [ModelCatalog]).
     * Fehler werden durchgereicht, damit das Menü einen Ladefehler anzeigen kann.
     */
    suspend fun availableModels(service: AiService, token: String): List<String> = when (service) {
        AiService.MAMMOUTH -> fetchMammouthModels(token)
        AiService.OPENAI -> openAiApi.models(bearer(token)).data.map { it.id }
        AiService.CLAUDE -> anthropicApi.models(token, ANTHROPIC_VERSION).data.map { it.id }
        AiService.GEMINI -> geminiApi.models(token).models
            .filter { "generateContent" in it.supportedGenerationMethods }
            .map { it.name.removePrefix("models/") }
    }

    // --- Modellauflösung ---

    private suspend fun resolveModel(service: AiService, settings: AiSettings, token: String): String {
        val stored = settings.model(service).takeIf { it.isNotBlank() }
        if (service != AiService.MAMMOUTH) return stored ?: service.defaultModel.ifBlank { FALLBACK_MODEL }
        // Mammouth: gespeicherte konkrete ID nutzen, solange sie im Katalog steht — sonst das neueste
        // Text-Modell des abgeleiteten Anbieters (Selbstheilung, falls Mammouth ein Modell entfernt).
        val all = availableMammouthModels(token)
        if (all.isEmpty()) return stored ?: FALLBACK_MODEL
        if (stored != null && stored in all && ModelCatalog.isTextModel(stored)) return stored
        val provider = stored?.let { MammouthProvider.ofModel(it) } ?: MammouthProvider.DEFAULT
        return ModelCatalog.mammouthModels(provider, all).firstOrNull() ?: stored ?: FALLBACK_MODEL
    }

    /** Nicht-werfende, gecachte Variante für die Generierung (Selbstheilung darf nie blockieren). */
    private suspend fun availableMammouthModels(token: String): List<String> {
        val now = System.currentTimeMillis()
        if (mammouthModelCache.isNotEmpty() && now - mammouthCacheAt < CACHE_TTL_MS) return mammouthModelCache
        return runCatching { fetchMammouthModels(token) }.getOrDefault(emptyList())
    }

    private suspend fun fetchMammouthModels(token: String): List<String> {
        val ids = mammouthApi.models(bearer(token)).data.map { it.id }
        if (ids.isNotEmpty()) {
            mammouthModelCache = ids
            mammouthCacheAt = System.currentTimeMillis()
        }
        return ids
    }

    private fun bearer(token: String) = "Bearer $token"

    private companion object {
        const val CACHE_TTL_MS = 30 * 60 * 1000L
        const val ANTHROPIC_VERSION = "2023-06-01"
        const val FALLBACK_MODEL = "gpt-4o" // extrem seltener Offline-Erstfall ohne je geladene Liste
    }
}
