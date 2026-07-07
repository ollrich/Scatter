package app.scatterto.core

import org.junit.Assert.assertEquals
import org.junit.Test

class FacetsTest {

    @Test fun computesByteOffsetsWithUmlautBeforeFacets() {
        // "Hä\n\n#Test https://a.de"  — "Hä" = 3 UTF-8-Bytes (H=1, ä=2)
        val post = composePost("Hä", "#Test", "https://a.de")
        val facets = computeFacets(post, url = "https://a.de", hashtag = "#Test")

        assertEquals(2, facets.size)

        // Sortiert nach byteStart: erst Tag (#Test), dann Link.
        val tag = facets[0]
        assertEquals(FACET_TAG_TYPE, tag.features[0].type)
        assertEquals("Test", tag.features[0].tag) // ohne „#"
        assertEquals(5, tag.index.byteStart) // "Hä\n\n" = 3 + 2 = 5 Bytes
        assertEquals(10, tag.index.byteEnd)   // + "#Test" (5 Bytes)

        val link = facets[1]
        assertEquals(FACET_LINK_TYPE, link.features[0].type)
        assertEquals("https://a.de", link.features[0].uri)
        assertEquals(11, link.index.byteStart) // "Hä\n\n#Test " = 5 + 6 = 11 Bytes
        assertEquals(23, link.index.byteEnd)   // + URL (12 Bytes)
    }

    @Test fun computesByteOffsetsWithEmoji() {
        // "🎉\n\n#x https://y.z" — 🎉 = 4 UTF-8-Bytes, aber 2 UTF-16-Chars
        val post = composePost("🎉", "#x", "https://y.z")
        val facets = computeFacets(post, url = "https://y.z", hashtag = "#x")

        val tag = facets.first { it.features[0].type == FACET_TAG_TYPE }
        assertEquals(6, tag.index.byteStart) // "🎉\n\n" = 4 + 1 + 1 = 6 Bytes
        assertEquals(8, tag.index.byteEnd)   // + "#x" (2 Bytes)

        val link = facets.first { it.features[0].type == FACET_LINK_TYPE }
        assertEquals(9, link.index.byteStart) // "🎉\n\n#x " = 6 + 3 = 9 Bytes
    }

    @Test fun tagAtEndOfTextHasCorrectRange() {
        val post = composePost("Text", "#Ende", "https://a.de")
        val facets = computeFacets(post, url = "https://a.de", hashtag = "#Ende")
        val tag = facets.first { it.features[0].type == FACET_TAG_TYPE }
        // "Text\n\n" = 6 Bytes, "#Ende" = 5 Bytes
        assertEquals(6, tag.index.byteStart)
        assertEquals(11, tag.index.byteEnd)
    }

    @Test fun missingSubstringYieldsNoFacet() {
        val facets = computeFacets("kein link hier", url = "https://nope.de", hashtag = null)
        assertEquals(0, facets.size)
    }
}
