package app.scatterto.data.metadata

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OgMetadataParserTest {

    private fun parse(html: String, baseUrl: String = "https://example.com/") =
        OgMetadataFetcher.parse(Jsoup.parse(html, baseUrl))

    @Test fun prefersOpenGraphTags() {
        val html = """
            <html><head>
              <title>Fallback-Titel</title>
              <meta property="og:title" content="OG Titel"/>
              <meta property="og:description" content="OG Beschreibung"/>
              <meta property="og:image" content="/bild.jpg"/>
            </head><body></body></html>
        """.trimIndent()

        val meta = parse(html)
        assertEquals("OG Titel", meta.title)
        assertEquals("OG Beschreibung", meta.description)
        assertEquals("https://example.com/bild.jpg", meta.imageUrl) // zu absoluter URL aufgelöst
    }

    @Test fun fallsBackToTitleAndMetaDescription() {
        val html = """
            <html><head>
              <title>Nur Titel</title>
              <meta name="description" content="Standard-Beschreibung"/>
            </head><body></body></html>
        """.trimIndent()

        val meta = parse(html)
        assertEquals("Nur Titel", meta.title)
        assertEquals("Standard-Beschreibung", meta.description)
        assertNull(meta.imageUrl)
    }

    @Test fun emptyPageIsNotUsable() {
        val meta = parse("<html><head></head><body></body></html>")
        assertNull(meta.title)
        assertEquals(false, meta.isUsable)
    }

    @Test fun extractsLanguageFromHtmlLang() {
        val meta = parse("""<html lang="de-DE"><head><title>T</title></head><body></body></html>""")
        assertEquals("de", meta.language)
    }

    @Test fun fallsBackToOgLocaleForLanguage() {
        val html = """
            <html><head>
              <title>T</title>
              <meta property="og:locale" content="en_GB"/>
            </head><body></body></html>
        """.trimIndent()
        assertEquals("en", parse(html).language)
    }

    @Test fun languageIsNullWhenAbsent() {
        assertNull(parse("<html><head><title>T</title></head><body></body></html>").language)
    }
}
