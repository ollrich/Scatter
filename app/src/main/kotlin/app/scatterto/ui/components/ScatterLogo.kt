package app.scatterto.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Das Scatter-Motiv ohne Hintergrund für die App selbst (Titelleiste, Menü, Über). Das App-Icon
 * auf dem Homescreen trägt den dunklen Hintergrund; in der App würde der im Dark Theme als dunkle
 * Kachel auf dunkler Fläche stehen.
 *
 * Gezeichnet statt als Bild, weil der Quellpunkt themenabhängig sein muss: im Icon ist er weiß,
 * im hellen Theme verschwände Weiß auf der hellen Fläche. Er nimmt deshalb `onSurface`, die beiden
 * Bahnen behalten ihre festen Netzwerk-Töne.
 *
 * Geometrie 1:1 aus `design/icon-source.svg` (Motiv-Box x 264–890, y 150–776, Kantenlänge 626).
 * Wer das Motiv dort ändert, muss die Kreise hier nachziehen.
 */
@Composable
fun ScatterLogo(modifier: Modifier = Modifier) {
    val source = MaterialTheme.colorScheme.onSurface
    Canvas(modifier) {
        val scale = minOf(size.width, size.height) / BOX
        val dx = (size.width - BOX * scale) / 2
        val dy = (size.height - BOX * scale) / 2
        for ((cx, cy, r, tone) in DOTS) {
            drawCircle(
                color = tone ?: source,
                radius = r * scale,
                center = Offset(dx + (cx - MIN_X) * scale, dy + (cy - MIN_Y) * scale),
            )
        }
    }
}

private const val MIN_X = 264f
private const val MIN_Y = 150f
private const val BOX = 626f

private val Violet = Color(0xFF9FA0FF) // Mastodon-Bahn
private val SkyBlue = Color(0xFF7CC0FF) // Bluesky-Bahn

private data class Dot(val cx: Float, val cy: Float, val r: Float, val tone: Color?)

/** tone = null → Quellpunkt in der Theme-Textfarbe. */
private val DOTS = listOf(
    Dot(380f, 660f, 116f, null),
    Dot(466f, 457f, 64f, Violet),
    Dot(527f, 314f, 52f, Violet),
    Dot(579f, 191f, 41f, Violet),
    Dot(583f, 574f, 64f, SkyBlue),
    Dot(726f, 513f, 52f, SkyBlue),
    Dot(849f, 461f, 41f, SkyBlue),
)
