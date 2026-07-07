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

/** Rohschema der KI-Antwort (§5.3), vor Normalisierung. */
@Serializable
data class AiResult(
    @SerialName("de_text") val deText: String,
    @SerialName("de_hashtag") val deHashtag: String = "",
    @SerialName("en_text") val enText: String,
    @SerialName("en_hashtag") val enHashtag: String = "",
)
