package app.scatterto.data.mammouth

import app.scatterto.core.normalizeHashtag
import app.scatterto.data.model.GeneratedPost
import app.scatterto.data.model.GeneratedPosts
import app.scatterto.data.net.Network
import kotlinx.serialization.SerializationException

/** Wird geworfen, wenn die KI-Antwort nicht als erwartetes JSON interpretierbar ist (§5.3). */
class AiParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Defensives Parsen der KI-Antwort (§5.3, §12.2 Nr. 4): extrahiert das JSON-Objekt auch aus
 * Markdown-Fences/Fließtext, dekodiert es und normalisiert die ergänzenden Hashtags. Rein → testbar.
 */
object AiResponseParser {

    /**
     * @param wantDe / [wantEn] welche Sprachen angefordert wurden — nur diese müssen vorhanden sein.
     */
    fun parse(content: String?, wantDe: Boolean = true, wantEn: Boolean = true): GeneratedPosts {
        if (content.isNullOrBlank()) throw AiParseException("Leere KI-Antwort")

        val jsonObject = extractJsonObject(content)
        val raw = try {
            Network.json.decodeFromString<AiResult>(jsonObject)
        } catch (e: SerializationException) {
            throw AiParseException("Antwort nicht im erwarteten JSON-Format", e)
        }

        if ((wantDe && raw.de.text.isBlank()) || (wantEn && raw.en.text.isBlank())) {
            throw AiParseException("KI-Antwort ohne Post-Text")
        }

        return GeneratedPosts(
            de = if (wantDe) GeneratedPost(raw.de.text.trim(), cleanTags(raw.de.extraHashtags)) else EMPTY,
            en = if (wantEn) GeneratedPost(raw.en.text.trim(), cleanTags(raw.en.extraHashtags)) else EMPTY,
        )
    }

    private val EMPTY = GeneratedPost("", emptyList())

    // Ergänzende Hashtags normalisieren (# sicherstellen, Leerzeichen raus), Schreibweise bleibt.
    private fun cleanTags(tags: List<String>): List<String> =
        tags.map(::normalizeHashtag).filter { it.isNotEmpty() }.distinct()

    /** Nimmt vom ersten `{` bis zum letzten `}` — robust gegen ```json-Fences und Begleittext. */
    fun extractJsonObject(raw: String): String {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        return if (start >= 0 && end > start) raw.substring(start, end + 1) else raw.trim()
    }
}
