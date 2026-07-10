package app.scatterto.data.net

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.HttpException

/**
 * Strukturierter API-Fehler. Retrofit liefert von sich aus nur „HTTP 400 Bad Request" — die
 * Begründung steckt im Fehler-Body, der sich nur **einmal** lesen lässt. Deshalb wird er hier
 * genau einmal ausgewertet und weitergereicht.
 *
 * Unterstützt beide gängigen Formen:
 *  - Bluesky/atproto: {"error":"ExpiredToken","message":"…"}
 *  - OpenAI-kompatibel (Mammouth): {"error":{"message":"…","type":"…"}}
 */
data class ApiError(val status: Int, val errorName: String?, val detail: String?) {

    val readable: String
        get() {
            val parts = listOfNotNull(errorName, detail).joinToString(": ")
            return if (parts.isBlank()) "HTTP $status" else "HTTP $status – $parts"
        }

    /**
     * true, wenn die Session erneuert werden muss. Achtung: Das AT-Protokoll meldet einen
     * abgelaufenen Access-Token als **400 mit error="ExpiredToken"**, nicht als 401 (§12.1 Nr. 4).
     */
    val isAuthExpired: Boolean
        get() = status == 401 || errorName in AUTH_ERRORS

    private companion object {
        val AUTH_ERRORS = setOf("ExpiredToken", "InvalidToken", "AuthMissing")
    }
}

/** Fehler mit bereits ausgelesener Begründung — der Body ist danach verbraucht. */
class ApiException(val error: ApiError) : Exception(error.readable)

/** Liest den Fehler-Body genau einmal aus und wertet ihn aus. */
fun HttpException.toApiError(): ApiError {
    val body = runCatching { response()?.errorBody()?.string() }.getOrNull().orEmpty()
    val (name, detail) = parseErrorParts(body)
    return ApiError(code(), name, detail ?: body.trim().take(300).ifBlank { null })
}

fun HttpException.readableMessage(): String = toApiError().readable

/** Reine Extraktion aus dem Body — ohne Netzwerk, daher unit-testbar. */
internal fun parseErrorParts(body: String): Pair<String?, String?> {
    if (body.isBlank()) return null to null
    val root = runCatching { Network.json.parseToJsonElement(body) }.getOrNull() as? JsonObject
        ?: return null to null

    val errorField = root["error"]
    val errorName = (errorField as? JsonPrimitive)?.contentOrNull
    val nestedMessage = (errorField as? JsonObject)?.get("message")
        ?.let { (it as? JsonPrimitive)?.contentOrNull }
    val detail = (root["message"] as? JsonPrimitive)?.contentOrNull ?: nestedMessage

    return errorName to detail
}

internal fun parseApiError(body: String): String? {
    val (name, detail) = parseErrorParts(body)
    return listOfNotNull(name, detail).joinToString(": ").ifBlank { null }
}
