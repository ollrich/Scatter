package app.scatterto.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Bluesky-Facets (§6, §12.2 Nr. 6): machen URL und Hashtag im Plaintext klickbar.
 * Offsets sind **UTF-8-Byte-Offsets**, nicht Zeichen-Offsets — bei Umlauten/Emoji weichen sie ab.
 * Modell ist bereits im Bluesky-Wire-Format (`$type`-Diskriminator) für die spätere Serialisierung.
 */

const val FACET_LINK_TYPE = "app.bsky.richtext.facet#link"
const val FACET_TAG_TYPE = "app.bsky.richtext.facet#tag"

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

private fun facetFor(post: String, substring: String, feature: FacetFeature): Facet? {
    val charStart = post.indexOf(substring)
    if (charStart < 0) return null
    val byteStart = utf8ByteOffset(post, charStart)
    val byteEnd = utf8ByteOffset(post, charStart + substring.length)
    return Facet(ByteSlice(byteStart, byteEnd), listOf(feature))
}

/**
 * Berechnet die Facets für den zusammengesetzten [post]. Die Byte-Range des Tags umfasst das „#";
 * im `tag`-Feld steht der Hashtag ohne „#". Ergebnis nach byteStart sortiert.
 */
fun computeFacets(post: String, url: String?, hashtag: String?): List<Facet> {
    val facets = mutableListOf<Facet>()

    if (!url.isNullOrEmpty()) {
        facetFor(post, url, FacetFeature(FACET_LINK_TYPE, uri = url))?.let { facets += it }
    }
    if (!hashtag.isNullOrEmpty()) {
        val withHash = if (hashtag.startsWith("#")) hashtag else "#$hashtag"
        facetFor(post, withHash, FacetFeature(FACET_TAG_TYPE, tag = withHash.removePrefix("#")))
            ?.let { facets += it }
    }

    return facets.sortedBy { it.index.byteStart }
}
