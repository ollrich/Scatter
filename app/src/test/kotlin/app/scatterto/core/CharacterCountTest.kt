package app.scatterto.core

import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterCountTest {

    @Test fun mastodonCountsAnyUrlAs23() {
        // "Hallo " = 6, URL fix 23 -> 29, egal wie lang die URL wirklich ist.
        val post = "Hallo https://example.com/ein/sehr/langer/pfad/der/egal/ist"
        assertEquals(29, mastodonLength(post))
    }

    @Test fun mastodonCountsUmlautsAsOne() {
        assertEquals(4, mastodonLength("Süß!")) // S ü ß !
    }

    @Test fun blueskyCountsFullUrlAsGraphemes() {
        // Bluesky zählt die volle URL mit (§12.4 Nr. 2): "hi " = 3 + URL-Länge 15.
        val url = "https://a.de/xyz"
        assertEquals(3 + url.length, blueskyLength("hi $url"))
    }

    @Test fun emojiIsOneGrapheme() {
        assertEquals(1, graphemeCount("🎉"))
        assertEquals(2, graphemeCount("🎉🎉"))
    }

    @Test fun umlautIsOneGrapheme() {
        assertEquals(3, graphemeCount("äöü"))
    }
}
