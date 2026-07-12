package app.scatterto

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.scatterto.data.ThemeMode
import app.scatterto.ui.ScatterToApp
import app.scatterto.ui.theme.ScatterToTheme

/** Einsprungpunkt inkl. Share-Intent-Handling (§3, §5.1). */
class MainActivity : ComponentActivity() {

    private data class SharePayload(val text: String, val subject: String?)

    private var shared by mutableStateOf<SharePayload?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // §12.2 Nr. 9
        super.onCreate(savedInstanceState)
        // Share-Intent nur bei frischem Start konsumieren: nach Rotation/Theme-Wechsel
        // (savedInstanceState != null) oder Relaunch aus den Recents würde das alte Intent sonst
        // erneut verarbeitet und Nutzer-Eingaben überschreiben.
        val fromHistory = ((intent?.flags ?: 0) and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0
        shared = if (savedInstanceState == null && !fromHistory) intent?.toSharePayload() else null
        val themePreferences = (application as ScatterToApplication).container.themePreferences
        setContent {
            val mode by themePreferences.mode.collectAsStateWithLifecycle()
            val darkTheme = when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            ScatterToTheme(darkTheme = darkTheme) {
                ScatterToApp(
                    sharedText = shared?.text,
                    sharedSubject = shared?.subject,
                    onSharedConsumed = { shared = null },
                )
            }
        }
    }

    // singleTask: ein neuer Share ersetzt den aktuellen Zustand (§12.2 Nr. 8).
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shared = intent.toSharePayload()
    }

    private fun Intent.toSharePayload(): SharePayload? {
        if (action != Intent.ACTION_SEND || type != "text/plain") return null
        val text = getStringExtra(Intent.EXTRA_TEXT) ?: return null
        return SharePayload(text, getStringExtra(Intent.EXTRA_SUBJECT))
    }
}
