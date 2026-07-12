package app.scatterto.data.mammouth

import app.scatterto.core.mediumNameFrom
import app.scatterto.data.log.EventLog
import app.scatterto.data.metadata.PageMetadata
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
        mastodonMaxChars: Int,
        blueskyUrl: String,
        wantDe: Boolean,
        wantEn: Boolean,
    ): GeneratedPosts {
        val service = settings.active
        val token = settings.token(service)
        val model = resolveModel(service, settings, token)
        val medium = metadata.siteName ?: mediumNameFrom(blueskyUrl)
        log.info("KI: ${service.displayName} / $model, Medium ${medium ?: "unbekannt"}")

        val system = PromptBuilder.system(wantDe, wantEn)
        val user = PromptBuilder.user(
            medium = medium,
            title = metadata.title,
            description = metadata.description,
            deBudget = if (wantDe) PromptBuilder.mastodonTextBudget(mastodonMaxChars) else null,
            enBudget = if (wantEn) PromptBuilder.blueskyTextBudget(blueskyUrl) else null,
        )

        val content = when (service) {
            AiService.MAMMOUTH -> openAiComplete(mammouthApi, token, model, system, user)
            AiService.OPENAI -> openAiComplete(openAiApi, token, model, system, user)
            AiService.CLAUDE -> claudeComplete(token, model, system, user)
            AiService.GEMINI -> geminiComplete(token, model, system, user)
        }
        return AiResponseParser.parse(content, wantDe, wantEn)
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
                if (ids.isEmpty()) return false
                val model = settings.model(service)
                if (service == AiService.MAMMOUTH || model in ids) true else model in ids || model.isBlank()
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
        return try {
            apiCall { api.chat(bearer(token), request).firstContent() }
        } catch (e: HttpException) {
            if (e.code() == 400) {
                log.info("KI: HTTP 400 – Retry ohne response_format/temperature")
                apiCall { api.chat(bearer(token), request.copy(responseFormat = null, temperature = null)).firstContent() }
            } else {
                throw e
            }
        }
    }

    // --- Anthropic (Claude) ---

    private suspend fun claudeComplete(token: String, model: String, system: String, user: String): String? = apiCall {
        anthropicApi.messages(
            apiKey = token,
            version = "2023-06-01",
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

    // --- Modellauflösung ---

    private suspend fun resolveModel(service: AiService, settings: AiSettings, token: String): String {
        if (service != AiService.MAMMOUTH) return settings.model(service)
        // Mammouth: Provider-Schlüssel -> aktuelles Flaggschiff via /v1/models, sonst feste ID.
        val choice = settings.model(AiService.MAMMOUTH)
        return if (choice in MAMMOUTH_PROVIDERS) {
            ModelResolver.resolve(choice, availableMammouthModels(token))
        } else {
            choice
        }
    }

    private suspend fun availableMammouthModels(token: String): List<String> {
        val now = System.currentTimeMillis()
        if (mammouthModelCache.isNotEmpty() && now - mammouthCacheAt < CACHE_TTL_MS) return mammouthModelCache
        val ids = runCatching { mammouthApi.models(bearer(token)).data.map { it.id } }.getOrDefault(emptyList())
        if (ids.isNotEmpty()) {
            mammouthModelCache = ids
            mammouthCacheAt = now
        }
        return ids
    }

    private fun bearer(token: String) = "Bearer $token"

    private companion object {
        val MAMMOUTH_PROVIDERS = setOf("mistral", "claude", "gpt", "gemini")
        const val CACHE_TTL_MS = 30 * 60 * 1000L
    }
}
