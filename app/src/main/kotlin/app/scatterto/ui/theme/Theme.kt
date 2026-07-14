package app.scatterto.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = SeedPrimary,
    secondary = SeedSecondary,
)

private val LightColors = lightColorScheme(
    primary = SeedPrimary,
    secondary = SeedSecondary,
)

/**
 * App-Theme. Dark Mode folgt der Systemeinstellung (§2). Da minSdk 34 ist, ist
 * Material-You-Dynamic-Color immer verfügbar; die statische Palette dient nur als Fallback.
 */
@Composable
fun ScatterToTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val base = when {
        dynamicColor && context is Activity ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    // Alle Blau-Akzente (Buttons, Switch, Radiobuttons, Links, Feld-Fokus) auf das Icon-Blau #1A80B6
    // festlegen — in Hell UND Dunkel, statt der aus dem Wallpaper abgeleiteten Primärfarbe. Die
    // Menü-Icons werden separat und nur im Hell-Modus getönt (siehe AppDrawer).
    val colorScheme = base.copy(primary = SeedPrimary, onPrimary = Color.White)

    // Systemleisten-Icons an das App-Theme koppeln (nicht ans System): sonst sind die Statusbar-Icons
    // im erzwungenen Hell-Modus unsichtbar, wenn das Gerät im Dunkel-Modus läuft.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).run {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}

/** true, wenn das aktuell aktive Schema hell ist (unabhängig von Dynamic Color). */
@Composable
fun isLightTheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() > 0.5f
