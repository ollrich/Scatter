package app.scatterto.data.mammouth

import app.scatterto.core.domainOf
import app.scatterto.data.metadata.PageMetadata
import app.scatterto.data.model.GeneratedPosts
import app.scatterto.data.model.MammouthConfig
import app.scatterto.data.net.Network
import retrofit2.HttpException

/**
 * Kapselt die KI-Generierung (§5.3): genau ein Call pro Generierung, strukturiertes JSON,
 * defensives Parsing, response_format-/temperature-Fallback (§12.2 Nr. 5).
 * Die Modell-ID wird aus der Auswahl aufgelöst (Anbieter → aktuelles Flaggschiff via /v1/models).
 */
class MammouthRepository {

    // Eigener Client mit langem Read-Timeout — LLM-Calls brauchen ≥ 60 s (§12.1 Nr. 6).
    private val api: MammouthApi =
        Network.retrofit(BASE_URL, Network.okHttp(readTimeoutSeconds = 90))
            .create(MammouthApi::class.java)

    // Kurzlebiger Cache der Modell-Liste, damit nicht vor jeder Generierung neu geladen wird.
    private var modelCache: List<String> = emptyList()
    private var modelCacheAt = 0L

    /**
     * Generiert beide Post-Texte. [mastodonMaxChars] steuert das DE-Längenbudget (§12.2 Nr. 3).
     * @throws AiParseException bei unlesbarer Antwort (UI bietet Retry an).
     */
    suspend fun generate(
        config: MammouthConfig,
        metadata: PageMetadata,
        mastodonMaxChars: Int,
        blueskyUrl: String,
    ): GeneratedPosts {
        val model = resolveModelId(config)
        val request = ChatRequest(
            model = model,
            messages = listOf(
                ChatMessage("system", PromptBuilder.system),
                ChatMessage(
                    "user",
                    PromptBuilder.user(
                        medium = metadata.siteName ?: domainOf(blueskyUrl),
                        title = metadata.title,
                        description = metadata.description,
                        deBudget = PromptBuilder.mastodonTextBudget(mastodonMaxChars),
                        enBudget = PromptBuilder.blueskyTextBudget(blueskyUrl),
                    ),
                ),
            ),
            responseFormat = ResponseFormat("json_object"),
        )

        val content = try {
            api.chat(bearer(config.token), request).firstContent()
        } catch (e: HttpException) {
            // Manche Anbieter/Modelle lehnen response_format oder temperature ab -> ohne beides erneut.
            if (e.code() == 400) {
                api.chat(
                    bearer(config.token),
                    request.copy(responseFormat = null, temperature = null),
                ).firstContent()
            } else {
                throw e
            }
        }

        return AiResponseParser.parse(content)
    }

    /** Token-/Modell-Validierung (§4.1): prüft Erreichbarkeit; bei fester Custom-ID deren Existenz. */
    suspend fun validate(config: MammouthConfig): Boolean {
        val ids = runCatching { fetchModels(config.token) }.getOrDefault(emptyList())
        if (ids.isEmpty()) return false
        val fixed = config.fixedModelId
        return if (fixed != null && fixed != ModelResolver.RECOMMENDED_ID) fixed in ids else true
    }

    private suspend fun resolveModelId(config: MammouthConfig): String {
        config.fixedModelId?.let { return it }
        val provider = config.provider ?: ModelResolver.DEFAULT_PROVIDER
        return ModelResolver.resolve(provider, availableModels(config.token))
    }

    private suspend fun availableModels(token: String): List<String> {
        val now = System.currentTimeMillis()
        if (modelCache.isNotEmpty() && now - modelCacheAt < CACHE_TTL_MS) return modelCache
        val ids = runCatching { fetchModels(token) }.getOrDefault(emptyList())
        if (ids.isNotEmpty()) {
            modelCache = ids
            modelCacheAt = now
        }
        return ids
    }

    private suspend fun fetchModels(token: String): List<String> =
        api.models(bearer(token)).data.map { it.id }

    private fun bearer(token: String) = "Bearer $token"

    private companion object {
        const val BASE_URL = "https://api.mammouth.ai/v1/"
        const val CACHE_TTL_MS = 30 * 60 * 1000L
    }
}
