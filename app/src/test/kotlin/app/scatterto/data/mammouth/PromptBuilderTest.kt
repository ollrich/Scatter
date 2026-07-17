package app.scatterto.data.mammouth

import app.scatterto.data.model.Tonality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    private fun mastodon(budget: Int = 400) = GenTarget("mastodon", "Mastodon", "German", budget, wantsCard = false)
    private fun bluesky(budget: Int = 240) = GenTarget("bluesky", "Bluesky", "English", budget, wantsCard = true)

    /** Die URL steht bei Bluesky nur in der Link-Karte, nicht im Text — sie kostet kein Budget. */
    @Test fun blueskyBudgetSubtractsOnlyReserve() {
        assertEquals(250, PromptBuilder.blueskyTextBudget()) // 300 - 50
    }

    @Test fun mastodonBudgetUsesFixedUrlWeight() {
        assertEquals(427, PromptBuilder.mastodonTextBudget(500)) // 500 - 23 - 50
    }

    @Test fun budgetHasSensibleFloorForTinyLimits() {
        assertTrue(PromptBuilder.mastodonTextBudget(50) >= 60)
    }

    @Test fun userPromptListsRequestedBudgets() {
        val both = PromptBuilder.user("NDR", "Titel", "Beschreibung", listOf(mastodon(400), bluesky(240)))
        assertTrue(both.contains("mastodon_text max. 400"))
        assertTrue(both.contains("bluesky_text max. 240"))
        assertTrue(both.contains("NDR"))

        val onlyMastodon = PromptBuilder.user("NDR", "T", "B", listOf(mastodon(400)))
        assertTrue(onlyMastodon.contains("mastodon_text max. 400"))
        assertFalse(onlyMastodon.contains("bluesky_text"))
    }

    @Test fun systemSchemaAndLanguagesMatchTargets() {
        val both = PromptBuilder.system(listOf(mastodon(), bluesky()))
        assertTrue(both.contains(""""mastodon""""))
        assertTrue(both.contains(""""bluesky""""))
        assertTrue(both.contains("German"))
        assertTrue(both.contains("English"))
        assertTrue(both.contains("card_title")) // Bluesky verlangt eine Link-Karte

        val onlyMastodon = PromptBuilder.system(listOf(mastodon()))
        assertTrue(onlyMastodon.contains(""""mastodon""""))
        assertFalse(onlyMastodon.contains(""""bluesky":{"""))
        assertFalse(onlyMastodon.contains("card_title")) // ohne Bluesky keine Karte
    }

    @Test fun systemCarriesTheSelectedTonalityOnly() {
        val marcel = PromptBuilder.system(listOf(mastodon()), Tonality.MARCEL)
        assertTrue(marcel.contains(Tonality.MARCEL.promptBlock))
        assertFalse(marcel.contains(Tonality.HAPE.promptBlock))
    }

    /** Jede Tonalität muss einen Stil-Block liefern (sonst käme ein leerer Tonfall-Abschnitt). */
    @Test fun everyTonalityHasAPromptBlock() {
        assertTrue(Tonality.entries.all { it.promptBlock.isNotBlank() })
    }

    @Test fun systemCarriesHazelWhenSelected() {
        assertTrue(PromptBuilder.system(listOf(mastodon()), Tonality.HAZEL).contains(Tonality.HAZEL.promptBlock))
    }

    /** Standard ist die Vorgabe — ohne Argument darf kein anderer Ton in den Prompt geraten. */
    @Test fun systemDefaultsToStandardTonality() {
        assertTrue(PromptBuilder.system(listOf(mastodon())).contains(Tonality.STANDARD.promptBlock))
    }

    /** Die Karte beschreibt den Artikel, nicht den Poster: sie bleibt auch bei Marcel sachlich. */
    @Test fun cardStaysNeutralRegardlessOfTonality() {
        val marcel = PromptBuilder.system(listOf(mastodon(), bluesky()), Tonality.MARCEL)
        assertTrue(marcel.contains("Die Karte gehört zum ARTIKEL"))
    }
}
