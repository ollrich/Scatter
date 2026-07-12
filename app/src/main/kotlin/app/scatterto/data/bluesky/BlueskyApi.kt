package app.scatterto.data.bluesky

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/** AT-Protocol-XRPC-Endpunkte auf der PDS (§4.2, §6). */
interface BlueskyApi {

    @POST("xrpc/com.atproto.server.createSession")
    suspend fun createSession(@Body request: CreateSessionRequest): SessionResponse

    @POST("xrpc/com.atproto.server.refreshSession")
    suspend fun refreshSession(@Header("Authorization") authorization: String): SessionResponse

    @GET("xrpc/app.bsky.actor.getProfile")
    suspend fun getProfile(
        @Header("Authorization") authorization: String,
        @Query("actor") actor: String,
    ): ProfileResponse

    @GET("xrpc/app.bsky.feed.getAuthorFeed")
    suspend fun getAuthorFeed(
        @Header("Authorization") authorization: String,
        @Query("actor") actor: String,
        @Query("limit") limit: Int,
    ): AuthorFeedResponse

    /** Rohe Bild-Bytes; der Content-Type ergibt sich aus dem [RequestBody]. */
    @POST("xrpc/com.atproto.repo.uploadBlob")
    suspend fun uploadBlob(
        @Header("Authorization") authorization: String,
        @Body image: RequestBody,
    ): UploadBlobResponse

    @POST("xrpc/com.atproto.repo.createRecord")
    suspend fun createRecord(
        @Header("Authorization") authorization: String,
        @Body request: CreateRecordRequest,
    ): CreateRecordResponse
}
