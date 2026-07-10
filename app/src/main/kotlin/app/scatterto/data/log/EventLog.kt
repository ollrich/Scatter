package app.scatterto.data.log

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class LogLevel { INFO, ERROR }

data class LogEntry(val timestamp: Long, val level: LogLevel, val message: String)

/**
 * Ringpuffer der letzten Aktionen — als Diagnosehilfe im Protokoll-Screen sicht- und teilbar.
 *
 * **Niemals Credentials protokollieren** (§8): keine Tokens, App-Passwörter oder JWTs.
 * Erlaubt sind URLs, Modell-IDs, HTTP-Status und API-Fehlermeldungen.
 */
class EventLog(private val capacity: Int = 50) {

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    fun info(message: String) = add(LogLevel.INFO, message)
    fun error(message: String) = add(LogLevel.ERROR, message)

    @Synchronized
    private fun add(level: LogLevel, message: String) {
        val next = _entries.value + LogEntry(System.currentTimeMillis(), level, message)
        _entries.value = if (next.size > capacity) next.takeLast(capacity) else next
    }

    fun clear() {
        _entries.value = emptyList()
    }

    /** Zum Kopieren/Teilen aufbereitet. */
    fun formatted(): String = _entries.value.joinToString("\n") { format(it) }

    companion object {
        private val TIME: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

        fun format(entry: LogEntry): String {
            val time = TIME.format(Instant.ofEpochMilli(entry.timestamp))
            val tag = if (entry.level == LogLevel.ERROR) "FEHLER" else "INFO  "
            return "$time  $tag  ${entry.message}"
        }
    }
}
