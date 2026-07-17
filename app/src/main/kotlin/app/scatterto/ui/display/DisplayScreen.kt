package app.scatterto.ui.display

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.scatterto.R
import app.scatterto.data.ThemeMode
import app.scatterto.ui.AppLocale
import app.scatterto.ui.AppViewModelProvider

/** Anzeige-Einstellungen (§2): Theme, dynamische Farben und App-Sprache. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayScreen(
    onBack: () -> Unit,
    viewModel: DisplayViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Setzen der App-Sprache startet die Activity neu; Zustand wird beim Öffnen frisch gelesen.
    var language by remember { mutableStateOf(AppLocale.current(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_display)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.theme_heading), style = MaterialTheme.typography.titleMedium)
            RadioRow(stringResource(R.string.theme_system), mode == ThemeMode.SYSTEM) { viewModel.setMode(ThemeMode.SYSTEM) }
            RadioRow(stringResource(R.string.theme_light), mode == ThemeMode.LIGHT) { viewModel.setMode(ThemeMode.LIGHT) }
            RadioRow(stringResource(R.string.theme_dark), mode == ThemeMode.DARK) { viewModel.setMode(ThemeMode.DARK) }

            val dynamic by viewModel.dynamicColor.collectAsStateWithLifecycle()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.dynamic_colors_label))
                    Text(
                        stringResource(R.string.dynamic_colors_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = dynamic, onCheckedChange = viewModel::setDynamicColor)
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Text(stringResource(R.string.language_heading), style = MaterialTheme.typography.titleMedium)
            AppLocale.options.forEach { (tag, nativeName) ->
                val label = if (tag == null) stringResource(R.string.language_system) else nativeName
                RadioRow(label, language == tag) {
                    language = tag
                    AppLocale.set(context, tag)
                }
            }
        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Role.RadioButton + onClick=null am Button: für TalkBack ist die ganze Zeile EIN
            // Radio-Element statt zwei getrennt fokussierbarer Ziele.
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label)
    }
}
