package app.scatterto.ui.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.scatterto.data.ThemeMode
import app.scatterto.ui.AppViewModelProvider

/** Anzeige-Einstellungen (§2): Theme-Wahl. Später: dynamische Farben abschaltbar + Akzentfarbe. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(
    onBack: () -> Unit,
    viewModel: DisplayViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val mode by viewModel.mode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anzeige") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Design", style = MaterialTheme.typography.titleMedium)
            ThemeOption("System", ThemeMode.SYSTEM, mode, viewModel::setMode)
            ThemeOption("Hell", ThemeMode.LIGHT, mode, viewModel::setMode)
            ThemeOption("Dunkel", ThemeMode.DARK, mode, viewModel::setMode)
        }
    }
}

@Composable
private fun ThemeOption(label: String, value: ThemeMode, selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = value == selected, onClick = { onSelect(value) })
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = value == selected, onClick = { onSelect(value) })
        Text(label)
    }
}
