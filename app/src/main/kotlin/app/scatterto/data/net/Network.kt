package app.scatterto.data.net

import app.scatterto.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Gemeinsame Netzwerk-Infrastruktur: JSON-Konfiguration, OkHttp-Clients mit
 * differenzierten Timeouts (§12.1 Nr. 6) und Retrofit-Builder.
 * Logging nur im Debug-Build (CLAUDE.md / §8) und nie auf Header-Ebene mit Credentials.
 */
object Network {

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    private val jsonMediaType = "application/json".toMediaType()

    /**
     * @param readTimeoutSeconds hoch für LLM-Calls (≥ 60 s, §12.1 Nr. 6), kurz für OG-Fetch.
     */
    fun okHttp(readTimeoutSeconds: Long = 30): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
            )
        }
        return builder.build()
    }

    fun retrofit(baseUrl: String, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
}
