package app.scatterto.data.log

import androidx.annotation.StringRes
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
 * Neue Meldungen werden über String-Ressourcen in der App-Sprache protokolliert (Alt-Einträge
 * bleiben in ihrer Ursprungssprache, da sie als fertiger Text gespeichert sind).
 *
 * **Niemals Credentials protokollieren** (§8): keine Tokens, App-Passwörter oder JWTs.
 * Erlaubt sind URLs, Modell-IDs, HTTP-Status und API-Fehlermeldungen.
 */
class EventLog(
    private val capacity: Int = 50,
    /** Löst String-Ressourcen in der App-Sprache auf (im Test standardmäßig No-op). */
    private val resolve: (Int, Array<out Any>) -> String = { _, _ -> "" },
) {

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    fun info(message: String) = add(LogLevel.INFO, message)
    fun error(message: String) = add(LogLevel.ERROR, message)

    fun info(@StringRes resId: Int, vararg args: Any) = add(LogLevel.INFO, resolve(resId, args))
    fun error(@StringRes resId: Int, vararg args: Any) = add(LogLevel.ERROR, resolve(resId, args))

    /** Lokalisiertes Wort/Fragment für Log-Argumente (z. B. Fallback „unbekannt"). */
    fun string(@StringRes resId: Int): String = resolve(resId, emptyArray())

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
            val tag = if (entry.level == LogLevel.ERROR) "ERROR" else "INFO "
            return "$time  $tag  ${entry.message}"
        }
    }
}
