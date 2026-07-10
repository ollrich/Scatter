package app.scatterto.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.scatterto.data.log.EventLog
import app.scatterto.data.log.LogLevel
import app.scatterto.ui.AppViewModelProvider

/**
 * Protokoll der letzten Aktionen — zum Nachvollziehen und Teilen bei Problemen.
 * Enthält bewusst keine Credentials (§8).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    onBack: () -> Unit,
    viewModel: LogViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val entries = viewModel.entries.collectAsStateWithLifecycle().value
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Protokoll") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(viewModel.formatted())) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("In Zwischenablage kopieren") }
                OutlinedButton(
                    onClick = viewModel::clear,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Protokoll leeren") }
            }

            if (entries.isEmpty()) {
                Text("Noch keine Einträge.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(entries) { entry ->
                        Text(
                            text = EventLog.format(entry),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (entry.level == LogLevel.ERROR) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        }
    }
}
