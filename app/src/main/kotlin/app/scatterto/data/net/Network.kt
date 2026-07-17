package app.scatterto.data.net

import app.scatterto.BuildConfig
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Gemeinsame Netzwerk-Infrastruktur: JSON-Konfiguration, OkHttp mit differenzierten Timeouts
 * (§12.1 Nr. 6) und Retrofit-Builder. Logging nur im Debug-Build (§8), nie mit Credentials.
 *
 * Es gibt genau EINEN Basis-Client; Varianten (z. B. langes LLM-Read-Timeout) entstehen über
 * `newBuilder()` und teilen Threadpool + ConnectionPool — statt pro Aufruf neue Pools anzulegen.
 */
object Network {

    private const val DEFAULT_READ_TIMEOUT_S = 30L

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    private val jsonMediaType = "application/json".toMediaType()

    private val baseClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_READ_TIMEOUT_S, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                    // Auch im Debug-Log haben Credentials nichts verloren (§8) — alle drei
                    // Auth-Header-Varianten der angebundenen APIs schwärzen.
                    redactHeader("Authorization")
                    redactHeader("x-api-key")
                    redactHeader("x-goog-api-key")
                },
            )
        }
        builder.build()
    }

    /**
     * @param readTimeoutSeconds hoch für LLM-Calls (≥ 60 s, §12.1 Nr. 6), Standard sonst.
     */
    fun okHttp(readTimeoutSeconds: Long = DEFAULT_READ_TIMEOUT_S): OkHttpClient =
        if (readTimeoutSeconds == DEFAULT_READ_TIMEOUT_S) {
            baseClient
        } else {
            baseClient.newBuilder().readTimeout(readTimeoutSeconds, TimeUnit.SECONDS).build()
        }

    fun retrofit(baseUrl: String, client: OkHttpClient = okHttp()): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
}
