package app.scatterto.data.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formatiert ISO-Zeitstempel für die Anzeige. Verträgt volle Instants (`2026-07-10T12:00:00Z`)
 * und reine Datumsangaben (`2026-07-10`, Mastodon `last_status_at`).
 *
 * Die Formatter entstehen pro Aufruf mit der AKTUELLEN Locale: die App-Sprache ist umschaltbar,
 * und ein statisch gecachter Formatter überlebte den Wechsel (der Prozess startet dabei nicht
 * neu) — dann stünden deutsche Monatsnamen in der englischen UI.
 */
object DateDisplay {

    fun monthYear(iso: String?): String? = parse(iso)?.let {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
            .withZone(ZoneId.systemDefault()).format(it)
    }

    fun date(iso: String?): String? = parse(iso)?.let {
        DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
            .withZone(ZoneId.systemDefault()).format(it)
    }

    private fun parse(iso: String?): Instant? {
        if (iso.isNullOrBlank()) return null
        return runCatching { Instant.parse(iso) }.getOrNull()
            ?: runCatching { LocalDate.parse(iso).atStartOfDay(ZoneId.systemDefault()).toInstant() }.getOrNull()
    }
}
