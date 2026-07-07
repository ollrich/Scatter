package app.scatterto.data.mammouth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AiResponseParserTest {

    @Test fun parsesPlainJson() {
        val json = """
            {"de_text":"Guter Fund","de_hashtag":"#Klima","en_text":"Nice find","en_hashtag":"#Climate"}
        """.trimIndent()
        val posts = AiResponseParser.parse(json)
        assertEquals("Guter Fund", posts.deText)
        assertEquals("#Klima", posts.deHashtag)
        assertEquals("Nice find", posts.enText)
        assertEquals("#Climate", posts.enHashtag)
    }

    @Test fun stripsMarkdownFencesAndProse() {
        val content = """
            Klar, hier ist das JSON:
            ```json
            {"de_text":"A","de_hashtag":"tag eins","en_text":"B","en_hashtag":"Tag"}
            ```
        """.trimIndent()
        val posts = AiResponseParser.parse(content)
        assertEquals("A", posts.deText)
        assertEquals("#TagEins", posts.deHashtag) // normalisiert: # + CamelCase
        assertEquals("#Tag", posts.enHashtag)
    }

    @Test fun throwsOnMissingTextField() {
        val json = """{"de_text":"","de_hashtag":"#x","en_text":"ok","en_hashtag":"#y"}"""
        assertThrows(AiParseException::class.java) { AiResponseParser.parse(json) }
    }

    @Test fun throwsOnGarbage() {
        assertThrows(AiParseException::class.java) { AiResponseParser.parse("kein json hier") }
    }
}
