package app.scatterto.ui.log

import androidx.lifecycle.ViewModel
import app.scatterto.data.AppContainer
import app.scatterto.data.log.LogEntry
import kotlinx.coroutines.flow.StateFlow

/** Stellt das Protokoll (§ Diagnose) zum Anzeigen, Kopieren und Leeren bereit. */
class LogViewModel(private val container: AppContainer) : ViewModel() {
    val entries: StateFlow<List<LogEntry>> = container.eventLog.entries
    fun formatted(): String = container.eventLog.formatted()
    fun clear() = container.eventLog.clear()
}
