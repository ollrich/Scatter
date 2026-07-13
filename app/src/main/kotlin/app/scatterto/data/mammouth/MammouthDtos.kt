package app.scatterto.data.mammouth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** OpenAI-kompatible Wire-Modelle für die Mammouth-API (§4.1, §5.3). */

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ResponseFormat(val type: String) // "json_object" (§5.3)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
    // Nullable, damit der Fallback temperature komplett weglassen kann (manche neuen Modelle lehnen sie ab).
    val temperature: Double? = 0.7,
)

@Serializable
data class ChatResponse(val choices: List<Choice> = emptyList()) {
    fun firstContent(): String? = choices.firstOrNull()?.message?.content
}

@Serializable
data class Choice(val message: ChatMessage)

@Serializable
data class ModelsResponse(val data: List<ModelData> = emptyList())

@Serializable
data class ModelData(val id: String)

/**
 * Rohschema der KI-Antwort (§5.3): je Sprache ein Text mit Inline-Hashtags + optionale Ergänzungen.
 * Für EN zusätzlich Titel/Beschreibung der englischen Bluesky-Link-Vorschau (§6).
 */
@Serializable
data class AiLangResult(
    val text: String = "",
    @SerialName("extra_hashtags") val extraHashtags: List<String> = emptyList(),
    @SerialName("card_title") val cardTitle: String = "",
    @SerialName("card_description") val cardDescription: String = "",
)

@Serializable
data class AiResult(
    val mastodon: AiLangResult = AiLangResult(),
    val bluesky: AiLangResult = AiLangResult(),
)
