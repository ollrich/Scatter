package app.scatterto.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.scatterto.BuildConfig
import app.scatterto.R

private const val GITHUB_URL = "https://github.com/ollrich/ScatterTo"

/** Kurzinfo zur App, Autor, Quellcode und Version (§ Distribution). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)),
            )
            Text("ScatterTo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium)

            Text(
                "Teile einen Artikel-Link und ScatterTo erstellt per KI je einen kurzen Post für " +
                    "Mastodon (Deutsch) und Bluesky (Englisch) — prüfen, anpassen, absenden.",
                style = MaterialTheme.typography.bodyMedium,
            )
            val author = buildAnnotatedString {
                append("Ein privates Projekt von ")
                withLink(LinkAnnotation.Url("https://eichhof.me")) {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) { append("Olli") }
                }
            }
            Text(author, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Keine Telemetrie, keine Weitergabe an Dritte außer den genutzten APIs. " +
                    "Zugangsdaten liegen ausschließlich verschlüsselt auf dem Gerät.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedButton(
                onClick = { uriHandler.openUri(GITHUB_URL) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Quellcode auf GitHub") }
        }
    }
}
