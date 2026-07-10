package app.scatterto.data.mammouth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

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

    @Test fun userPromptListsOnlyRequestedBudgets() {
        val both = PromptBuilder.user("NDR", "Titel", "Beschreibung", deBudget = 400, enBudget = 240)
        assertTrue(both.contains("de_text max. 400"))
        assertTrue(both.contains("en_text max. 240"))
        assertTrue(both.contains("NDR"))

        val deOnly = PromptBuilder.user("NDR", "T", "B", deBudget = 400, enBudget = null)
        assertTrue(deOnly.contains("de_text max. 400"))
        assertFalse(deOnly.contains("en_text"))
    }

    @Test fun systemSchemaMatchesRequestedLanguages() {
        assertTrue(PromptBuilder.system(wantDe = true, wantEn = true).contains(""""de""""))
        assertTrue(PromptBuilder.system(wantDe = true, wantEn = true).contains(""""en""""))

        val deOnly = PromptBuilder.system(wantDe = true, wantEn = false)
        assertTrue(deOnly.contains(""""de""""))
        assertFalse(deOnly.contains(""""en":{"""))
    }
}
