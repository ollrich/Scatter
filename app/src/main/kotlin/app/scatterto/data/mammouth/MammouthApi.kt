package app.scatterto.data.mammouth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/** Mammouth-API (OpenAI-kompatibel), Basis https://api.mammouth.ai/v1/ (§4.1). */
interface MammouthApi {

    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest,
    ): ChatResponse

    @GET("models")
    suspend fun models(
        @Header("Authorization") authorization: String,
    ): ModelsResponse
}
