package app.scatterto.data.mammouth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    @Test fun blueskyBudgetSubtractsUrlGraphemesAndReserve() {
        val url = "https://a.de/x" // 14 Zeichen
        // 300 - 14 - 50 = 236
        assertEquals(236, PromptBuilder.blueskyTextBudget(url))
    }

    @Test fun mastodonBudgetUsesFixedUrlWeight() {
        // 500 - 23 - 50 = 427
        assertEquals(427, PromptBuilder.mastodonTextBudget(500))
    }

    @Test fun budgetHasSensibleFloorForTinyLimits() {
        assertTrue(PromptBuilder.mastodonTextBudget(50) >= 60)
    }

    @Test fun userPromptIncludesMediumAndBudgets() {
        val prompt = PromptBuilder.user("NDR", "Titel", "Beschreibung", deBudget = 400, enBudget = 240)
        assertTrue(prompt.contains("NDR"))
        assertTrue(prompt.contains("400"))
        assertTrue(prompt.contains("240"))
        assertTrue(prompt.contains("Titel"))
    }
}
