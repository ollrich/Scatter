package app.scatterto.data.mammouth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    @Test fun blueskyBudgetSubtractsUrlGraphemesAndReserve() {
        val url = "https://a.de/x" // 14 Zeichen
        // 300 - 14 - 40 = 246
        assertEquals(246, PromptBuilder.blueskyTextBudget(url))
    }

    @Test fun mastodonBudgetUsesFixedUrlWeight() {
        // 500 - 23 - 40 = 437
        assertEquals(437, PromptBuilder.mastodonTextBudget(500))
    }

    @Test fun budgetHasSensibleFloorForTinyLimits() {
        assertTrue(PromptBuilder.mastodonTextBudget(50) >= 60)
    }

    @Test fun userPromptIncludesBudgets() {
        val prompt = PromptBuilder.user("Titel", "Beschreibung", deBudget = 400, enBudget = 240)
        assertTrue(prompt.contains("400"))
        assertTrue(prompt.contains("240"))
        assertTrue(prompt.contains("Titel"))
    }
}
