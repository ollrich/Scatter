package app.scatterto.core

import org.junit.Assert.assertEquals
import org.junit.Test

class FacetsTest {

    @Test fun computesByteOffsetsWithUmlautBeforeTagAndUrl() {
        // "ä #tag https://a.de" — "ä " = 3 UTF-8-Bytes (ä=2, Leerzeichen=1)
        val post = "ä #tag https://a.de"
        val facets = computeFacets(post, url = "https://a.de")

        assertEquals(2, facets.size)

        val tag = facets.first { it.features[0].type == FACET_TAG_TYPE }
        assertEquals("tag", tag.features[0].tag) // ohne „#"
        assertEquals(3, tag.index.byteStart)     // "ä " = 3 Bytes
        assertEquals(7, tag.index.byteEnd)       // + "#tag" (4 Bytes)

        val link = facets.first { it.features[0].type == FACET_LINK_TYPE }
        assertEquals("https://a.de", link.features[0].uri)
        assertEquals(8, link.index.byteStart)    // "ä #tag " = 8 Bytes
    }

    @Test fun findsAllHashtagsInPost() {
        val post = composePost("Bei #NDR über #klima.", listOf("#moor"), "https://a.de")
        val facets = computeFacets(post, url = "https://a.de")

        val tags = facets.filter { it.features[0].type == FACET_TAG_TYPE }.mapNotNull { it.features[0].tag }
        assertEquals(setOf("NDR", "klima", "moor"), tags.toSet())
        assertEquals(1, facets.count { it.features[0].type == FACET_LINK_TYPE })
    }

    @Test fun excludesHashInsideUrlFragment() {
        val url = "https://a.de/x#kapitel"
        val post = "Text $url"
        val facets = computeFacets(post, url = url)
        // Das #kapitel gehört zur URL, ist also kein eigener Tag.
        assertEquals(1, facets.size)
        assertEquals(FACET_LINK_TYPE, facets[0].features[0].type)
    }

    @Test fun emojiByteOffsetForHashtag() {
        // "🎉 #x" — 🎉 = 4 UTF-8-Bytes, aber 2 UTF-16-Chars
        val post = "🎉 #x"
        val facets = computeFacets(post, url = null)
        val tag = facets.first { it.features[0].type == FACET_TAG_TYPE }
        assertEquals(5, tag.index.byteStart) // "🎉 " = 4 + 1 = 5 Bytes
        assertEquals(7, tag.index.byteEnd)   // + "#x" (2 Bytes)
    }
}
