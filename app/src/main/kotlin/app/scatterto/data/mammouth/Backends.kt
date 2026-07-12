package app.scatterto.data.mammouth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Wire-Modelle und Retrofit-Interfaces der KI-Backends. Mammouth und OpenAI teilen das
 * OpenAI-kompatible Schema (siehe [MammouthApi]); Claude und Gemini haben eigene.
 */

// --- Anthropic (Claude): POST /v1/messages ---

@Serializable
data class AnthropicMessage(val role: String, val content: String)

@Serializable
data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<AnthropicMessage>,
)

@Serializable
data class AnthropicContent(val type: String = "", val text: String? = null)

@Serializable
data class AnthropicResponse(val content: List<AnthropicContent> = emptyList()) {
    fun firstText(): String? = content.firstOrNull { it.type == "text" }?.text
}

interface AnthropicApi {
    @POST("v1/messages")
    suspend fun messages(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String,
        @Body request: AnthropicRequest,
    ): AnthropicResponse
}

// --- Google Gemini: POST /v1beta/models/{model}:generateContent ---

@Serializable
data class GeminiPart(val text: String)

@Serializable
data class GeminiContent(val parts: List<GeminiPart> = emptyList(), val role: String? = null)

@Serializable
data class GeminiGenerationConfig(
    @SerialName("responseMimeType") val responseMimeType: String = "application/json",
)

@Serializable
data class GeminiRequest(
    @SerialName("system_instruction") val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>,
    @SerialName("generationConfig") val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig(),
)

@Serializable
data class GeminiCandidate(val content: GeminiContent? = null)

@Serializable
data class GeminiResponse(val candidates: List<GeminiCandidate> = emptyList()) {
    fun firstText(): String? = candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
}

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generate(
        @Header("x-goog-api-key") apiKey: String,
        @Path("model") model: String,
        @Body request: GeminiRequest,
    ): GeminiResponse

    @GET("v1beta/models")
    suspend fun models(@Header("x-goog-api-key") apiKey: String): GeminiModelsResponse
}

@Serializable
data class GeminiModelsResponse(val models: List<GeminiModel> = emptyList())

@Serializable
data class GeminiModel(val name: String = "")
