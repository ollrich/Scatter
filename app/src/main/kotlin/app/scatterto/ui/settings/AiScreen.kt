package app.scatterto.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.scatterto.R
import app.scatterto.data.mammouth.MammouthProvider
import app.scatterto.data.model.AiBudget
import app.scatterto.data.model.AiService
import app.scatterto.data.model.Tonality
import app.scatterto.data.util.DateDisplay
import app.scatterto.ui.AppViewModelProvider
import java.text.NumberFormat
import java.util.Currency

private const val GUIDE_URL = "https://github.com/ollrich/Scatter/blob/main/docs/ai-setup.md"

// Texte der Tonalitäten hier, nicht im Datenmodell: Tonality kennt den Prompt, nicht die Oberfläche.
private val Tonality.labelRes: Int
    get() = when (this) {
        Tonality.STANDARD -> R.string.tonality_standard
        Tonality.LOCKER -> R.string.tonality_locker
        Tonality.HAPE -> R.string.tonality_hape
        Tonality.HAZEL -> R.string.tonality_hazel
        Tonality.MARCEL -> R.string.tonality_marcel
    }

private val Tonality.descRes: Int
    get() = when (this) {
        Tonality.STANDARD -> R.string.tonality_standard_desc
        Tonality.LOCKER -> R.string.tonality_locker_desc
        Tonality.HAPE -> R.string.tonality_hape_desc
        Tonality.HAZEL -> R.string.tonality_hazel_desc
        Tonality.MARCEL -> R.string.tonality_marcel_desc
    }

private val Tonality.exampleRes: Int
    get() = when (this) {
        Tonality.STANDARD -> R.string.tonality_example_standard
        Tonality.LOCKER -> R.string.tonality_example_locker
        Tonality.HAPE -> R.string.tonality_example_hape
        Tonality.HAZEL -> R.string.tonality_example_hazel
        Tonality.MARCEL -> R.string.tonality_example_marcel
    }

/** Wo es den API-Token für den jeweiligen Dienst gibt (für den Info-Dialog). */
private fun consoleFor(service: AiService): String = when (service) {
    AiService.MAMMOUTH -> "mammouth.ai"
    AiService.CLAUDE -> "console.anthropic.com"
    AiService.OPENAI -> "platform.openai.com/api-keys"
    AiService.GEMINI -> "aistudio.google.com/apikey"
}

/** KI: Ein-/Ausschalter, Dienst-Auswahl (Dropdown + „?"), Token und Modell (§4.1). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state = viewModel.uiState
    val active = state.activeAiService
    var tokenVisible by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var showTonalityExamples by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_ai)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Master-Schalter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.ai_use), style = MaterialTheme.typography.titleMedium)
                Switch(checked = state.aiEnabled, onCheckedChange = viewModel::onAiEnabledChange)
            }

            if (!state.aiEnabled) {
                Text(
                    stringResource(R.string.ai_disabled_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                // KI-Dienst-Dropdown + adaptives „?"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChoiceDropdown(
                        label = stringResource(R.string.ai_service),
                        selectedText = active.displayName,
                        options = AiService.entries.map { it.key to it.displayName },
                        onSelect = viewModel::onServiceSelect,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showInfo = true }) {
                        Icon(Icons.AutoMirrored.Outlined.HelpOutline, contentDescription = stringResource(R.string.ai_info))
                    }
                }

                OutlinedTextField(
                    value = state.currentToken,
                    onValueChange = viewModel::onAiTokenChange,
                    label = { Text(stringResource(R.string.ai_token_label)) },
                    singleLine = true,
                    visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { tokenVisible = !tokenVisible }) {
                            Icon(
                                if (tokenVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = stringResource(R.string.ai_token_show),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Mammouth: zusätzlich die Anbieter-Wahl vor der Modell-Liste.
                if (state.isMammouth) {
                    ChoiceDropdown(
                        label = stringResource(R.string.ai_provider),
                        selectedText = MammouthProvider.fromKey(state.mammouthProvider).label,
                        options = MammouthProvider.entries.map { it.key to it.label },
                        onSelect = viewModel::onMammouthProviderSelect,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ModelField(state, viewModel)

                // Nur Mammouth liefert einen Guthabenstand; bei den anderen bleibt die Zeile weg.
                state.aiBudget?.let { BudgetRow(it) }

                Button(onClick = viewModel::saveAi, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.save))
                }

                when (val v = state.aiValidation) {
                    is ValidationState.Validating -> Text(stringResource(R.string.validate_checking))
                    is ValidationState.Valid -> Text(stringResource(R.string.validate_valid))
                    is ValidationState.Invalid -> Text(v.message)
                    ValidationState.None -> {}
                }

                HorizontalDivider()

                TonalitySection(
                    selected = state.tonality,
                    onSelect = viewModel::onTonalitySelect,
                    onHelp = { showTonalityExamples = true },
                )
            }
        }
    }

    if (showInfo) {
        val uriHandler = LocalUriHandler.current
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(active.displayName) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.ai_info_text, consoleFor(active)))
                    Text(
                        text = stringResource(R.string.ai_setup_guide),
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { uriHandler.openUri(GUIDE_URL) },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text(stringResource(android.R.string.ok)) }
            },
        )
    }

    if (showTonalityExamples) {
        TonalityExamplesDialog(onDismiss = { showTonalityExamples = false })
    }
}

/**
 * Guthaben des API-Tokens (§4.1) — es gibt ihn nur bei Mammouth, dessen LiteLLM-Backend den Stand
 * offenlegt. Die Beträge kommen als USD aus der API, unabhängig von der App-Sprache; formatiert
 * wird in der Anzeige-Locale (deutsch also „0,54 $", englisch „$0.54").
 */
@Composable
private fun BudgetRow(budget: AiBudget) {
    val locale = LocalConfiguration.current.locales[0]
    val money = remember(locale) {
        NumberFormat.getCurrencyInstance(locale).apply { currency = Currency.getInstance("USD") }
    }
    val color = if (budget.isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.ai_budget_label), style = MaterialTheme.typography.labelMedium)
            Text(
                stringResource(R.string.ai_budget_value, money.format(budget.spent), money.format(budget.max)),
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
        LinearProgressIndicator(
            progress = { budget.fraction },
            color = color,
            modifier = Modifier.fillMaxWidth(),
        )
        DateDisplay.date(budget.resetAt)?.let {
            Text(
                stringResource(R.string.ai_budget_reset, it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Tonalität (§5.3): Radio-Liste statt Dropdown, weil die Kurzbeschreibung mitlaufen muss — bei
 * fünf Optionen ist die volle Liste kompakt genug. Ein einzelnes „?" für alle: die Tonalitäten
 * versteht man im Vergleich, nicht einzeln.
 */
@Composable
private fun TonalitySection(
    selected: String,
    onSelect: (String) -> Unit,
    onHelp: () -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.tonality_heading),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onHelp) {
                Icon(
                    Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = stringResource(R.string.tonality_help),
                )
            }
        }
        Tonality.entries.forEach { tonality ->
            val isSelected = tonality.key == selected
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    // Auswahl auf der ganzen Zeile, nicht nur auf dem Radio-Punkt. selectable() mit
                    // Role.RadioButton macht daraus für TalkBack eine echte Radio-Gruppe.
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(tonality.key) },
                    )
                    .padding(vertical = 4.dp),
            ) {
                RadioButton(selected = isSelected, onClick = null)
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(stringResource(tonality.labelRes), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(tonality.descRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Beispiel-Dialog: derselbe Artikel in allen vier Tonalitäten, damit der Unterschied sichtbar wird. */
@Composable
private fun TonalityExamplesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tonality_examples_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.tonality_examples_intro),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Tonality.entries.forEach { tonality ->
                    Column {
                        Text(
                            stringResource(tonality.labelRes),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(stringResource(tonality.exampleRes), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
    )
}

/** Read-only-Dropdown über eine feste Optionsliste (Dienst bzw. Mammouth-Anbieter). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceDropdown(
    label: String,
    selectedText: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(key); expanded = false })
            }
        }
    }
}

/** Live-Modell-Dropdown mit Nachlade-Button und Statushinweis (erst nach gültigem Token). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelField(state: SettingsUiState, viewModel: SettingsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val hasToken = state.currentToken.isNotBlank()
    val models = state.availableModels
    val menuOpen = expanded && models.isNotEmpty()

    Row(verticalAlignment = Alignment.CenterVertically) {
        ExposedDropdownMenuBox(
            expanded = menuOpen,
            onExpandedChange = {
                when {
                    models.isNotEmpty() -> expanded = it
                    // Leeres Dropdown angetippt, Token da: Liste laden statt leeres Menü öffnen.
                    hasToken && !state.modelsLoading -> viewModel.refreshModels()
                }
            },
            modifier = Modifier.weight(1f),
        ) {
            OutlinedTextField(
                value = state.currentModel,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.model_label)) },
                placeholder = {
                    // Vor dem ersten Listen-Laden gilt das eingebaute Default-Modell — das Feld
                    // soll nicht leer wirken, wenn tatsächlich eines wirksam ist.
                    state.activeAiService.defaultModel.takeIf { it.isNotBlank() }?.let { Text(it) }
                },
                trailingIcon = {
                    if (state.modelsLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = menuOpen, onDismissRequest = { expanded = false }) {
                models.forEach { id ->
                    DropdownMenuItem(
                        text = { Text(id) },
                        onClick = { viewModel.onModelSelect(id); expanded = false },
                    )
                }
            }
        }
        IconButton(onClick = viewModel::refreshModels, enabled = hasToken && !state.modelsLoading) {
            Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.models_reload))
        }
    }

    val hint = when {
        !hasToken -> stringResource(R.string.models_need_token)
        state.modelsLoading -> stringResource(R.string.models_loading)
        state.modelsError -> stringResource(R.string.models_error)
        models.isEmpty() -> stringResource(R.string.models_empty)
        else -> null
    }
    hint?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodySmall,
            color = if (state.modelsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
