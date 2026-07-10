package app.scatterto.data.mammouth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class AiResponseParserTest {

    @Test fun parsesNewSchema() {
        val json = """
            {"de":{"text":"Bei #NDR über #klima. Ein Satz.","extra_hashtags":["#moor"]},
             "en":{"text":"#NDR reported on #climate. One sentence.","extra_hashtags":[]}}
        """.trimIndent()
        val posts = AiResponseParser.parse(json)
        assertEquals("Bei #NDR über #klima. Ein Satz.", posts.de.text)
        assertEquals(listOf("#moor"), posts.de.extraHashtags)
        assertTrue(posts.en.extraHashtags.isEmpty())
    }

    @Test fun stripsFencesAndNormalizesExtraTags() {
        val content = """
            Klar:
            ```json
            {"de":{"text":"T","extra_hashtags":["klima","#Wald"]},"en":{"text":"T","extra_hashtags":["#a a"]}}
            ```
        """.trimIndent()
        val posts = AiResponseParser.parse(content)
        assertEquals(listOf("#klima", "#Wald"), posts.de.extraHashtags) // # ergänzt, Case bleibt
        assertEquals(listOf("#aa"), posts.en.extraHashtags)             // Leerzeichen entfernt
    }

    @Test fun throwsOnMissingText() {
        val json = """{"de":{"text":"","extra_hashtags":[]},"en":{"text":"ok","extra_hashtags":[]}}"""
        assertThrows(AiParseException::class.java) { AiResponseParser.parse(json) }
    }

    @Test fun singleLanguageIgnoresTheOther() {
        // Nur DE angefordert: fehlendes/leeres en ist kein Fehler.
        val json = """{"de":{"text":"Bei #NDR über #klima. Satz.","extra_hashtags":[]}}"""
        val posts = AiResponseParser.parse(json, wantDe = true, wantEn = false)
        assertEquals("Bei #NDR über #klima. Satz.", posts.de.text)
        assertEquals("", posts.en.text)
    }

    @Test fun throwsOnGarbage() {
        assertThrows(AiParseException::class.java) { AiResponseParser.parse("kein json hier") }
    }
}
