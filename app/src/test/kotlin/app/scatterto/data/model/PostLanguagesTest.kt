package app.scatterto.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class PostLanguagesTest {

    @Test fun primaryStripsRegion() {
        assertEquals("de", PostLanguages.primary("de-DE"))
        assertEquals("en", PostLanguages.primary("EN"))
    }

    @Test fun normalizedFallsBackToEnglish() {
        assertEquals("de", PostLanguages.normalizedOrEnglish("de-DE"))
        assertEquals("fr", PostLanguages.normalizedOrEnglish("fr"))
        assertEquals("en", PostLanguages.normalizedOrEnglish("xx")) // nicht in der Liste
    }

    @Test fun englishNameForPrompt() {
        assertEquals("German", PostLanguages.englishName("de"))
        assertEquals("Danish", PostLanguages.englishName("da"))
        assertTrue(PostLanguages.TAGS.all { PostLanguages.englishName(it).isNotBlank() })
    }

    @Test fun displayNameLocalized() {
        assertEquals("German", PostLanguages.displayName("de", Locale.ENGLISH))
        assertEquals("Englisch", PostLanguages.displayName("en", Locale.GERMAN))
    }

    // --- Sprachhinweis auf der Bluesky-Karte ---

    /** Der Hinweis muss VORNE stehen: Bluesky kürzt hinten weg, dort wäre er unsichtbar. */
    @Test fun noteIsPrependedWhenArticleLanguageDiffers() {
        assertEquals(
            "(in German) A summary.",
            withSourceLanguageNote("A summary.", articleLanguage = "de", postLanguage = "en"),
        )
    }

    @Test fun noSuchNoteWhenLanguagesMatch() {
        assertEquals("A summary.", withSourceLanguageNote("A summary.", "en-US", "en"))
        assertEquals("Eine Zusammenfassung.", withSourceLanguageNote("Eine Zusammenfassung.", "de", "de-DE"))
    }

    /** Ohne `<html lang>`/`og:locale` ist die Artikelsprache unbekannt — dann lieber kein Hinweis. */
    @Test fun noNoteWhenArticleLanguageUnknown() {
        assertEquals("A summary.", withSourceLanguageNote("A summary.", null, "en"))
        assertEquals("A summary.", withSourceLanguageNote("A summary.", "", "en"))
    }

    @Test fun noteStandsAloneWhenDescriptionIsEmpty() {
        assertEquals("(in German)", withSourceLanguageNote("", "de", "en"))
    }
}
