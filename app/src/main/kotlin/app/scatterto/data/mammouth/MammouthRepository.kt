package app.scatterto.data.mammouth

import app.scatterto.data.metadata.PageMetadata
import app.scatterto.data.model.GeneratedPosts
import app.scatterto.data.model.MammouthConfig
import app.scatterto.data.net.Network
import retrofit2.HttpException

/**
 * Kapselt die KI-Generierung (§5.3): genau ein Call pro Generierung, strukturiertes JSON,
 * defensives Parsing, response_format-Fallback (§12.2 Nr. 5).
 */
class MammouthRepository {

    // Eigener Client mit langem Read-Timeout — LLM-Calls brauchen ≥ 60 s (§12.1 Nr. 6).
    private val api: MammouthApi =
        Network.retrofit(BASE_URL, Network.okHttp(readTimeoutSeconds = 90))
            .create(MammouthApi::class.java)

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
        val request = ChatRequest(
            model = config.modelId,
            messages = listOf(
                ChatMessage("system", PromptBuilder.system),
                ChatMessage(
                    "user",
                    PromptBuilder.user(
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
            // Manche Anbieter hinter dem Proxy lehnen response_format ab -> einmal ohne wiederholen.
            if (e.code() == 400) {
                api.chat(bearer(config.token), request.copy(responseFormat = null)).firstContent()
            } else {
                throw e
            }
        }

        return AiResponseParser.parse(content)
    }

    /** Optionale Token-/Modell-Validierung (§4.1): prüft, ob die Modell-ID vorhanden ist. */
    suspend fun validate(config: MammouthConfig): Boolean {
        val ids = api.models(bearer(config.token)).data.map { it.id }
        return config.modelId in ids
    }

    private fun bearer(token: String) = "Bearer $token"

    private companion object {
        const val BASE_URL = "https://api.mammouth.ai/v1/"
    }
}
