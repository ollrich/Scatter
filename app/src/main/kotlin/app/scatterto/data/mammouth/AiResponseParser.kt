package app.scatterto.data.mammouth

import androidx.annotation.StringRes
import app.scatterto.R
import app.scatterto.core.normalizeHashtag
import app.scatterto.data.model.GeneratedPost
import app.scatterto.data.model.GeneratedPosts
import app.scatterto.data.net.Network
import kotlinx.serialization.SerializationException

/**
 * Wird geworfen, wenn die KI-Antwort nicht als erwartetes JSON interpretierbar ist (§5.3).
 * Trägt eine String-Ressource statt Text, damit die Meldung in der App-Sprache erscheint —
 * die (englische) Exception-Message ist nur für Stacktraces.
 */
class AiParseException(@StringRes val resId: Int, cause: Throwable? = null) :
    Exception("AI response not parseable", cause)

/**
 * Defensives Parsen der KI-Antwort (§5.3, §12.2 Nr. 4): extrahiert das JSON-Objekt auch aus
 * Markdown-Fences/Fließtext, dekodiert es und normalisiert die ergänzenden Hashtags. Rein → testbar.
 */
object AiResponseParser {

    /**
     * @param wantMastodon / [wantBluesky] welche Netzwerke angefordert wurden — nur diese müssen da sein.
     */
    fun parse(content: String?, wantMastodon: Boolean = true, wantBluesky: Boolean = true): GeneratedPosts {
        if (content.isNullOrBlank()) throw AiParseException(R.string.error_ai_empty)

        val jsonObject = extractJsonObject(content)
        val raw = try {
            Network.json.decodeFromString<AiResult>(jsonObject)
        } catch (e: SerializationException) {
            throw AiParseException(R.string.error_ai_bad_json, e)
        }

        if ((wantMastodon && raw.mastodon.text.isBlank()) || (wantBluesky && raw.bluesky.text.isBlank())) {
            throw AiParseException(R.string.error_ai_no_text)
        }

        return GeneratedPosts(
            mastodon = if (wantMastodon) {
                GeneratedPost(raw.mastodon.text.trim(), cleanTags(raw.mastodon.extraHashtags))
            } else EMPTY,
            bluesky = if (wantBluesky) {
                GeneratedPost(
                    text = raw.bluesky.text.trim(),
                    extraHashtags = cleanTags(raw.bluesky.extraHashtags),
                    cardTitle = raw.bluesky.cardTitle.trim().ifBlank { null },
                    cardDescription = raw.bluesky.cardDescription.trim().ifBlank { null },
                )
            } else EMPTY,
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
