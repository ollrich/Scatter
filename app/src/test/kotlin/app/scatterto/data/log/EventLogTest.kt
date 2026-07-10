package app.scatterto.data.log

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventLogTest {

    @Test fun keepsOnlyTheMostRecentEntries() {
        val log = EventLog(capacity = 5)
        repeat(8) { log.info("Eintrag $it") }

        val entries = log.entries.value
        assertEquals(5, entries.size)
        assertEquals("Eintrag 3", entries.first().message) // 0..2 sind rausgefallen
        assertEquals("Eintrag 7", entries.last().message)
    }

    @Test fun recordsLevels() {
        val log = EventLog()
        log.info("ok")
        log.error("kaputt")
        assertEquals(listOf(LogLevel.INFO, LogLevel.ERROR), log.entries.value.map { it.level })
    }

    @Test fun formattedContainsLevelAndMessage() {
        val log = EventLog()
        log.error("Bluesky abgelehnt")
        val text = log.formatted()
        assertTrue(text.contains("FEHLER"))
        assertTrue(text.contains("Bluesky abgelehnt"))
    }

    @Test fun clearEmptiesTheBuffer() {
        val log = EventLog()
        log.info("a")
        log.clear()
        assertTrue(log.entries.value.isEmpty())
    }
}
