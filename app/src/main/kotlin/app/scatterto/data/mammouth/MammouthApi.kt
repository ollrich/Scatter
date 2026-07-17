package app.scatterto.data.mammouth

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

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

    // --- Guthaben (§4.1, nur Mammouth) ---
    //
    // Mammouth fährt seine API auf LiteLLM und legt dessen Verwaltungs-Endpoints offen. Die liegen
    // NICHT unter /v1 — der führende „/" macht den Pfad absolut zum Host, sonst hinge er unter der
    // Basis-URL. Die offizielle Doku zeigt hier fälschlich „0.0.0.0:4000" (unangepasstes
    // LiteLLM-Beispiel); das Antwort-Schema ist nirgends dokumentiert und kann sich ändern —
    // deshalb ist der Abruf überall best-effort und darf nie etwas blockieren.

    /** Liefert u. a. die `user_id` des Tokens; das Budget hängt am Account, nicht am Key. */
    @GET("/key/info")
    suspend fun keyInfo(
        @Header("Authorization") authorization: String,
    ): KeyInfoResponse

    /** Account-Budget: `max_budget`, aufsummierter `spend` und der Zeitpunkt des Resets. */
    @GET("/user/info")
    suspend fun userInfo(
        @Header("Authorization") authorization: String,
        @Query("user_id") userId: String,
    ): UserInfoResponse
}
