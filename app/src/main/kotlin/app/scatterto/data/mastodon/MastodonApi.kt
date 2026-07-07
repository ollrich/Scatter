package app.scatterto.data.mastodon

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/** Mastodon-API der jeweiligen Instanz (Basis = Instanz-URL, §4.2). */
interface MastodonApi {

    @GET("api/v1/accounts/verify_credentials")
    suspend fun verifyCredentials(
        @Header("Authorization") authorization: String,
    ): MastodonAccountDto

    @GET("api/v1/instance")
    suspend fun instance(): InstanceDto

    @POST("api/v1/statuses")
    suspend fun postStatus(
        @Header("Authorization") authorization: String,
        @Header("Idempotency-Key") idempotencyKey: String, // §12.1 Nr. 5
        @Body body: StatusRequest,
    ): StatusDto
}
