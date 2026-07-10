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
    val temperature: Double = 0.7,
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

/** Rohschema der KI-Antwort (§5.3): je Sprache ein Text mit Inline-Hashtags + optionale Ergänzungen. */
@Serializable
data class AiLangResult(
    val text: String = "",
    @SerialName("extra_hashtags") val extraHashtags: List<String> = emptyList(),
)

@Serializable
data class AiResult(
    val de: AiLangResult = AiLangResult(),
    val en: AiLangResult = AiLangResult(),
)
