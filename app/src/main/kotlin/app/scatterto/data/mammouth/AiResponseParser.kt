package app.scatterto.data.mammouth

import app.scatterto.core.normalizeHashtag
import app.scatterto.data.model.GeneratedPosts
import app.scatterto.data.net.Network
import kotlinx.serialization.SerializationException

/** Wird geworfen, wenn die KI-Antwort nicht als erwartetes JSON interpretierbar ist (§5.3). */
class AiParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Defensives Parsen der KI-Antwort (§5.3, §12.2 Nr. 4): extrahiert das JSON-Objekt auch aus
 * Markdown-Fences/Fließtext, dekodiert es und normalisiert die Hashtags. Reine Funktion → testbar.
 */
object AiResponseParser {

    fun parse(content: String?): GeneratedPosts {
        if (content.isNullOrBlank()) throw AiParseException("Leere KI-Antwort")

        val jsonObject = extractJsonObject(content)
        val raw = try {
            Network.json.decodeFromString<AiResult>(jsonObject)
        } catch (e: SerializationException) {
            throw AiParseException("Antwort nicht im erwarteten JSON-Format", e)
        }

        if (raw.deText.isBlank() || raw.enText.isBlank()) {
            throw AiParseException("KI-Antwort ohne Post-Text")
        }

        return GeneratedPosts(
            deText = raw.deText.trim(),
            deHashtag = normalizeHashtag(raw.deHashtag),
            enText = raw.enText.trim(),
            enHashtag = normalizeHashtag(raw.enHashtag),
        )
    }

    /** Nimmt vom ersten `{` bis zum letzten `}` — robust gegen ```json-Fences und Begleittext. */
    fun extractJsonObject(raw: String): String {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        return if (start >= 0 && end > start) raw.substring(start, end + 1) else raw.trim()
    }
}
