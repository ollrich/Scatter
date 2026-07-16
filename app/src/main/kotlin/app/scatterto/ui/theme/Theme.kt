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

/**
 * Festes Marken-Schema für „dynamische Farben AUS" — vollständig aus dem Icon-Blau #1A80B6
 * (Hue ≈ 201°) abgeleitet, damit keine Material-Baseline-Violetttöne in Containern/Tertiär
 * durchschlagen. Flächen bewusst neutral-kühl statt violett getönt.
 */
private val LightColors = lightColorScheme(
    primary = SeedPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCAE6FF),
    onPrimaryContainer = Color(0xFF001E2F),
    secondary = Color(0xFF4E616D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1E5F4),
    onSecondaryContainer = Color(0xFF0A1E28),
    tertiary = Color(0xFF00687B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFAEECFF),
    onTertiaryContainer = Color(0xFF001F26),
    background = Color(0xFFFBFCFE),
    onBackground = Color(0xFF191C1E),
    surface = Color(0xFFFBFCFE),
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE),
)

/**
 * Dunkel-Variante desselben Marken-Blaus — bewusst der **exakte** Icon-Hex #1A80B6 (Nutzer-
 * Entscheidung 2026-07-16), obwohl er auf dunklem Grund nur ~3,9:1 Kontrast erreicht und damit
 * für Link-Text unter AA (4,5:1) liegt. Markentreue schlägt hier den Normwert; wer vollen
 * Kontrast will, schaltet „dynamische Farben" an (Material You wählt lesbare Tonstufen).
 * `onPrimary` bleibt Weiß (~4,4:1 auf dem Blau) — für Button-Beschriftung ausreichend.
 */
private val DarkColors = darkColorScheme(
    primary = SeedPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF004C6F),
    onPrimaryContainer = Color(0xFFCAE6FF),
    secondary = Color(0xFFB5C9D7),
    onSecondary = Color(0xFF20333E),
    secondaryContainer = Color(0xFF374955),
    onSecondaryContainer = Color(0xFFD1E5F4),
    tertiary = Color(0xFF54D7F2),
    onTertiary = Color(0xFF003641),
    tertiaryContainer = Color(0xFF004E5D),
    onTertiaryContainer = Color(0xFFAEECFF),
    background = Color(0xFF191C1E),
    onBackground = Color(0xFFE1E2E5),
    surface = Color(0xFF191C1E),
    onSurface = Color(0xFFE1E2E5),
    surfaceVariant = Color(0xFF41484D),
    onSurfaceVariant = Color(0xFFC1C7CE),
    outline = Color(0xFF8B9198),
    outlineVariant = Color(0xFF41484D),
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
    // Schalter AN: volles Material You (Akzent UND Flächen aus dem Wallpaper).
    // Schalter AUS: festes Marken-Schema im Icon-Blau. Kein Primär-Override mehr — sonst wäre der
    // Schalter wirkungslos, weil der Akzent in beiden Zuständen gleich aussähe.
    val colorScheme = when {
        dynamicColor && context is Activity ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

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
