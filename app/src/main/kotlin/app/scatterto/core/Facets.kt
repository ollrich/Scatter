package app.scatterto.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bluesky-Facets (§6, §12.2 Nr. 6): machen URL und Hashtags im Plaintext klickbar.
 * Offsets sind **UTF-8-Byte-Offsets**, nicht Zeichen-Offsets — bei Umlauten/Emoji weichen sie ab.
 * Es werden ALLE Hashtags im Post erfasst (Rahmen-Tags im Satz + ergänzende), plus die URL.
 */

const val FACET_LINK_TYPE = "app.bsky.richtext.facet#link"
const val FACET_TAG_TYPE = "app.bsky.richtext.facet#tag"

// Hashtag = # plus Buchstaben/Ziffern/Unterstrich mit MINDESTENS einem Buchstaben: „#2026wahl"
// ist ein Tag, „#2026" allein nicht (rein numerisch verlinken weder Mastodon noch Bluesky).
private val HASHTAG_REGEX = Regex("""#[\p{N}_]*\p{L}[\p{L}\p{N}_]*""")

@Serializable
data class ByteSlice(val byteStart: Int, val byteEnd: Int)

@Serializable
data class FacetFeature(
    @SerialName("\$type") val type: String,
    val uri: String? = null, // nur bei #link
    val tag: String? = null, // nur bei #tag, OHNE führendes „#" (§12.2 Nr. 6)
)

@Serializable
data class Facet(val index: ByteSlice, val features: List<FacetFeature>)

private fun utf8ByteOffset(text: String, charIndex: Int): Int =
    text.substring(0, charIndex).toByteArray(Charsets.UTF_8).size

private fun facetAt(post: String, charStart: Int, length: Int, feature: FacetFeature): Facet {
    val byteStart = utf8ByteOffset(post, charStart)
    val byteEnd = utf8ByteOffset(post, charStart + length)
    return Facet(ByteSlice(byteStart, byteEnd), listOf(feature))
}

/**
 * Berechnet die Facets für den zusammengesetzten [post]: Link-Facet für [url] und je ein Tag-Facet
 * pro Hashtag im Text. Hashtags innerhalb der URL (z. B. `#fragment`) werden ausgenommen.
 * Die Byte-Range eines Tags umfasst das „#"; im `tag`-Feld steht der Tag ohne „#". Nach byteStart sortiert.
 */
fun computeFacets(post: String, url: String?): List<Facet> {
    val facets = mutableListOf<Facet>()

    var urlRange: IntRange? = null
    if (!url.isNullOrEmpty()) {
        val start = post.indexOf(url)
        if (start >= 0) {
            urlRange = start until (start + url.length)
            facets += facetAt(post, start, url.length, FacetFeature(FACET_LINK_TYPE, uri = url))
        }
    }

    for (match in HASHTAG_REGEX.findAll(post)) {
        // Hashtags, die zur URL gehören (Fragment), nicht als Tag verlinken.
        if (urlRange != null && match.range.first in urlRange) continue
        val tag = match.value.removePrefix("#")
        facets += facetAt(post, match.range.first, match.value.length, FacetFeature(FACET_TAG_TYPE, tag = tag))
    }

    return facets.sortedBy { it.index.byteStart }
}
