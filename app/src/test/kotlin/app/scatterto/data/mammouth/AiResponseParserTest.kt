package app.scatterto.data.mammouth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AiResponseParserTest {

    @Test fun parsesNetworkSchema() {
        val json = """
            {"mastodon":{"text":"Ein Satz.","extra_hashtags":["#NDR","#klima","#moor"]},
             "bluesky":{"text":"One sentence.","extra_hashtags":["#NDR","#climate"],
              "card_title":"A ruling","card_description":"Short summary."}}
        """.trimIndent()
        val posts = AiResponseParser.parse(json)
        assertEquals("Ein Satz.", posts.mastodon.text)
        assertEquals(listOf("#NDR", "#klima", "#moor"), posts.mastodon.extraHashtags)
        assertEquals("One sentence.", posts.bluesky.text)
        assertEquals("A ruling", posts.bluesky.cardTitle)
        assertEquals("Short summary.", posts.bluesky.cardDescription)
    }

    @Test fun stripsFencesAndNormalizesExtraTags() {
        val content = """
            Klar:
            ```json
            {"mastodon":{"text":"T","extra_hashtags":["klima","#Wald"]},"bluesky":{"text":"T","extra_hashtags":["#a a"]}}
            ```
        """.trimIndent()
        val posts = AiResponseParser.parse(content)
        assertEquals(listOf("#klima", "#Wald"), posts.mastodon.extraHashtags) // # ergänzt, Case bleibt
        assertEquals(listOf("#aa"), posts.bluesky.extraHashtags)              // Leerzeichen entfernt
    }

    @Test fun throwsOnMissingText() {
        val json = """{"mastodon":{"text":"","extra_hashtags":[]},"bluesky":{"text":"ok","extra_hashtags":[]}}"""
        assertThrows(AiParseException::class.java) { AiResponseParser.parse(json) }
    }

    @Test fun singleNetworkIgnoresTheOther() {
        // Nur Mastodon angefordert: fehlendes/leeres bluesky ist kein Fehler.
        val json = """{"mastodon":{"text":"Ein Satz.","extra_hashtags":[]}}"""
        val posts = AiResponseParser.parse(json, wantMastodon = true, wantBluesky = false)
        assertEquals("Ein Satz.", posts.mastodon.text)
        assertEquals("", posts.bluesky.text)
    }

    @Test fun throwsOnGarbage() {
        assertThrows(AiParseException::class.java) { AiResponseParser.parse("kein json hier") }
    }
}
