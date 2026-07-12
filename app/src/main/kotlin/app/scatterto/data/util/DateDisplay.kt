package app.scatterto.data.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formatiert ISO-Zeitstempel für die Anzeige. Verträgt volle Instants (`2026-07-10T12:00:00Z`)
 * und reine Datumsangaben (`2026-07-10`, Mastodon `last_status_at`).
 */
object DateDisplay {

    private val MONTH_YEAR = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.GERMAN)
        .withZone(ZoneId.systemDefault())
    private val DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMAN)
        .withZone(ZoneId.systemDefault())

    fun monthYear(iso: String?): String? = parse(iso)?.let(MONTH_YEAR::format)
    fun date(iso: String?): String? = parse(iso)?.let(DATE::format)

    private fun parse(iso: String?): Instant? {
        if (iso.isNullOrBlank()) return null
        return runCatching { Instant.parse(iso) }.getOrNull()
            ?: runCatching { LocalDate.parse(iso).atStartOfDay(ZoneId.systemDefault()).toInstant() }.getOrNull()
    }
}
