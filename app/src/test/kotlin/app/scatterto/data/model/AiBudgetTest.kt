package app.scatterto.data.model

import app.scatterto.data.mammouth.UserInfoResponse
import app.scatterto.data.net.Network
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiBudgetTest {

    @Test fun fractionIsSpentOverMax() {
        assertEquals(0.25f, AiBudget(spent = 0.5, max = 2.0).fraction, 0.001f)
    }

    /** Ein Überschreiten darf den Balken nicht sprengen. */
    @Test fun fractionIsCappedAtFull() {
        assertEquals(1f, AiBudget(spent = 3.0, max = 2.0).fraction, 0.001f)
    }

    /** max = 0 wäre eine Division durch null. */
    @Test fun fractionIsZeroWithoutBudget() {
        assertEquals(0f, AiBudget(spent = 1.0, max = 0.0).fraction, 0.001f)
    }

    @Test fun lowOnlyFromNinetyPercent() {
        assertFalse(AiBudget(spent = 1.7, max = 2.0).isLow)  // 85 %
        assertTrue(AiBudget(spent = 1.8, max = 2.0).isLow)   // 90 %
    }

    /**
     * Echter Payload von `GET /user/info` (Mammouth/LiteLLM, gekürzt auf die relevanten Felder plus
     * ein paar der ~40 ignorierten). Bricht, wenn sich die Feldnamen ändern.
     */
    @Test fun parsesRealUserInfoPayload() {
        val json = """
            {"user_id":"247670","user_info":{"user_id":"247670","max_budget":2.0,
             "spend":0.5414049000000001,"metadata":{"base_budget":2.0,"additional_credits":0.0},
             "budget_duration":"30d","budget_reset_at":"2026-08-01T00:00:00Z","teams":[]}}
        """.trimIndent()
        val info = Network.json.decodeFromString<UserInfoResponse>(json).userInfo
        assertEquals(2.0, info.maxBudget!!, 0.0001)
        assertEquals(0.5414049, info.spend, 0.0001)
        assertEquals("2026-08-01T00:00:00Z", info.budgetResetAt)
    }

    /** Ohne gesetztes Limit gibt es nichts anzuzeigen — das darf kein Fehler sein. */
    @Test fun nullBudgetSurvivesParsing() {
        val json = """{"user_info":{"max_budget":null,"spend":0.3}}"""
        assertNull(Network.json.decodeFromString<UserInfoResponse>(json).userInfo.maxBudget)
    }
}
