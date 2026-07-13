package app.scatterto.data.mammouth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    private fun mastodon(budget: Int = 400) = GenTarget("mastodon", "Mastodon", "German", budget, wantsCard = false)
    private fun bluesky(budget: Int = 240) = GenTarget("bluesky", "Bluesky", "English", budget, wantsCard = true)

    @Test fun blueskyBudgetSubtractsUrlGraphemesAndReserve() {
        val url = "https://a.de/x" // 14 Zeichen
        assertEquals(236, PromptBuilder.blueskyTextBudget(url)) // 300 - 14 - 50
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
}
