package app.scatterto.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareTextTest {

    @Test fun extractsUrlFromTitlePlusUrlShare() {
        assertEquals(
            "https://example.com/artikel",
            extractUrl("Spannender Titel https://example.com/artikel"),
        )
    }

    @Test fun trimsTrailingSentencePunctuation() {
        assertEquals(
            "https://example.com/artikel",
            extractUrl("Schau dir das an: https://example.com/artikel."),
        )
    }

    @Test fun returnsNullWhenNoUrlPresent() {
        assertNull(extractUrl("Nur Text ohne Link"))
    }

    @Test fun stripsUtmAndKnownTrackers() {
        assertEquals(
            "https://a.de/x?id=5",
            stripTrackingParams("https://a.de/x?utm_source=nl&id=5&fbclid=abc"),
        )
    }

    @Test fun stripsOnlyQueryKeepsFragment() {
        assertEquals(
            "https://a.de/x#kapitel",
            stripTrackingParams("https://a.de/x?utm_source=nl#kapitel"),
        )
    }

    @Test fun leavesCleanUrlUntouched() {
        assertEquals("https://a.de/x", stripTrackingParams("https://a.de/x"))
    }
}
