package app.scatterto.ui.display

import androidx.lifecycle.ViewModel
import app.scatterto.data.AppContainer
import app.scatterto.data.ThemeMode
import kotlinx.coroutines.flow.StateFlow

/** Anzeige-Einstellungen (§2). Zunächst nur die Theme-Wahl (System/Hell/Dunkel). */
class DisplayViewModel(private val container: AppContainer) : ViewModel() {
    val mode: StateFlow<ThemeMode> = container.themePreferences.mode
    fun setMode(mode: ThemeMode) = container.themePreferences.setMode(mode)
}
