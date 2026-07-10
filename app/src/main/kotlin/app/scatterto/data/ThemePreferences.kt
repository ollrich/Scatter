package app.scatterto.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Anzeige-Theme (§2). „System" folgt der Geräteeinstellung, bleibt Standard. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Persistiert die Theme-Wahl in normalen SharedPreferences — bewusst **nicht** verschlüsselt,
 * da es keine Credentials sind. Wird von der [AppContainer] gehalten und von der MainActivity gelesen.
 */
class ThemePreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("scatterto_display", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(load())
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        _mode.value = mode
        prefs.edit().putString(KEY, mode.name).apply()
    }

    private fun load(): ThemeMode =
        prefs.getString(KEY, null)?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM

    private companion object {
        const val KEY = "theme_mode"
    }
}
