package app.scatterto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import app.scatterto.ui.theme.ScatterToTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // targetSdk 35 erzwingt Edge-to-Edge (§12.2 Nr. 9)
        super.onCreate(savedInstanceState)
        setContent {
            ScatterToTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Platzhalter — die echte Hauptseite folgt in der UI-Schicht.
                    Text(
                        text = "ScatterTo",
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
