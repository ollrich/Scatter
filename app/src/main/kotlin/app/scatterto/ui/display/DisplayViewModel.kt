package app.scatterto.ui.display

import androidx.lifecycle.ViewModel
import app.scatterto.data.AppContainer
import app.scatterto.data.ThemeMode
import kotlinx.coroutines.flow.StateFlow

/** Anzeige-Einstellungen (§2): Theme-Wahl + dynamische Farben. */
class DisplayViewModel(private val container: AppContainer) : ViewModel() {
    val mode: StateFlow<ThemeMode> = container.themePreferences.mode
    fun setMode(mode: ThemeMode) = container.themePreferences.setMode(mode)

    val dynamicColor: StateFlow<Boolean> = container.themePreferences.dynamicColor
    fun setDynamicColor(enabled: Boolean) = container.themePreferences.setDynamicColor(enabled)
}
