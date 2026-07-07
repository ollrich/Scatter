package app.scatterto.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
    val colorScheme = when {
        dynamicColor && context is Activity ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
