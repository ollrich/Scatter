package app.scatterto.data.net

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import retrofit2.HttpException

/**
 * Macht API-Fehler lesbar. Retrofit liefert von sich aus nur „HTTP 400 Bad Request" — die
 * eigentliche Begründung steckt im Fehler-Body und ist für die Diagnose entscheidend.
 *
 * Unterstützt beide gängigen Formen:
 *  - Bluesky/atproto: {"error":"InvalidRequest","message":"…"}
 *  - OpenAI-kompatibel (Mammouth): {"error":{"message":"…","type":"…"}}
 */
fun HttpException.readableMessage(): String {
    val body = runCatching { response()?.errorBody()?.string() }.getOrNull().orEmpty()
    val detail = parseApiError(body) ?: body.trim().take(300).ifBlank { null }
    return if (detail != null) "HTTP ${code()} – $detail" else "HTTP ${code()}"
}

/** Reine Extraktion aus dem Body — ohne Netzwerk, daher unit-testbar. */
internal fun parseApiError(body: String): String? {
    if (body.isBlank()) return null
    val root = runCatching { Network.json.parseToJsonElement(body) }.getOrNull() as? JsonObject
        ?: return null

    val errorField = root["error"]
    val errorName = (errorField as? JsonPrimitive)?.contentOrNull
    val nestedMessage = (errorField as? JsonObject)?.get("message")
        ?.let { (it as? JsonPrimitive)?.contentOrNull }
    val message = (root["message"] as? JsonPrimitive)?.contentOrNull ?: nestedMessage

    return listOfNotNull(errorName, message).joinToString(": ").ifBlank { null }
}
