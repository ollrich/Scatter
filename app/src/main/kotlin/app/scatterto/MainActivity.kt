package app.scatterto

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.scatterto.ui.ScatterToApp
import app.scatterto.ui.theme.ScatterToTheme

/** Einsprungpunkt inkl. Share-Intent-Handling (§3, §5.1). */
class MainActivity : ComponentActivity() {

    private data class SharePayload(val text: String, val subject: String?)

    private var shared by mutableStateOf<SharePayload?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // §12.2 Nr. 9
        super.onCreate(savedInstanceState)
        shared = intent?.toSharePayload()
        setContent {
            ScatterToTheme {
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
