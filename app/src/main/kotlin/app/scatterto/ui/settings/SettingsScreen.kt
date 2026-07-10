package app.scatterto.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.scatterto.data.model.ModelChoices
import app.scatterto.ui.AppViewModelProvider
import app.scatterto.ui.components.NetworkHeader
import app.scatterto.ui.theme.BlueskyBlue
import app.scatterto.ui.theme.MastodonViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenLog: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            MammouthCard(state, viewModel)
            HorizontalDivider()
            MastodonCard(state, viewModel)
            HorizontalDivider()
            BlueskyCard(state, viewModel)
            HorizontalDivider()
            OutlinedButton(onClick = onOpenLog, modifier = Modifier.fillMaxWidth()) {
                Text("Protokoll anzeigen")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MammouthCard(state: SettingsUiState, viewModel: SettingsViewModel) {
    var tokenVisible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    SectionCard(title = "Mammouth-KI") {
        OutlinedTextField(
            value = state.mammouthToken,
            onValueChange = viewModel::onMammouthTokenChange,
            label = { Text("API-Token") },
            singleLine = true,
            visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { tokenVisible = !tokenVisible }) {
                    Icon(
                        if (tokenVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Token anzeigen",
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        val selectedLabel = ModelChoices.entries.firstOrNull { it.key == state.modelChoiceKey }
            ?.label ?: state.modelChoiceKey

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Modell") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                ModelChoices.entries.forEach { entry ->
                    DropdownMenuItem(
                        text = { Text(entry.label) },
                        onClick = {
                            viewModel.onModelChoice(entry.key)
                            expanded = false
                        },
                    )
                }
            }
        }

        if (state.isCustomModel) {
            OutlinedTextField(
                value = state.customModelId,
                onValueChange = viewModel::onCustomModelChange,
                label = { Text("Eigene Modell-ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Button(onClick = viewModel::saveMammouth, modifier = Modifier.fillMaxWidth()) {
            Text("Speichern")
        }

        when (val v = state.mammouthValidation) {
            is ValidationState.Validating -> Text("Prüfe Token…")
            is ValidationState.Valid -> Text("Token gültig ✓")
            is ValidationState.Invalid -> Text(v.message)
            ValidationState.None -> {}
        }
    }
}

@Composable
private fun MastodonCard(state: SettingsUiState, viewModel: SettingsViewModel) {
    SectionCard(title = "") {
        NetworkHeader("Mastodon", MastodonViolet, state.mastodonAvatarUrl, state.mastodonHandle?.takeIf { state.mastodonConnected })

        if (state.mastodonConnected) {
            OutlinedButton(onClick = viewModel::disconnectMastodon, modifier = Modifier.fillMaxWidth()) {
                Text("Trennen")
            }
        } else {
            OutlinedTextField(
                value = state.mastodonInstance,
                onValueChange = viewModel::onMastodonInstanceChange,
                label = { Text("Instanz-URL (z. B. https://mastodon.example)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.mastodonToken,
                onValueChange = viewModel::onMastodonTokenChange,
                label = { Text("Access Token") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = viewModel::connectMastodon,
                enabled = !state.mastodonConnecting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.mastodonConnecting) "Verbinde…" else "Verbinden")
            }
            state.mastodonError?.let { Text(it) }
        }
    }
}

@Composable
private fun BlueskyCard(state: SettingsUiState, viewModel: SettingsViewModel) {
    SectionCard(title = "") {
        NetworkHeader("Bluesky", BlueskyBlue, state.blueskyAvatarUrl, state.blueskyHandle?.takeIf { state.blueskyConnected })

        if (state.blueskyConnected) {
            OutlinedButton(onClick = viewModel::disconnectBluesky, modifier = Modifier.fillMaxWidth()) {
                Text("Trennen")
            }
        } else {
            OutlinedTextField(
                value = state.blueskyIdentifier,
                onValueChange = viewModel::onBlueskyIdentifierChange,
                label = { Text("Handle (z. B. alice.bsky.social)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.blueskyAppPassword,
                onValueChange = viewModel::onBlueskyPasswordChange,
                label = { Text("App Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.blueskyPds,
                onValueChange = viewModel::onBlueskyPdsChange,
                label = { Text("PDS (Standard: https://bsky.social)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = viewModel::connectBluesky,
                enabled = !state.blueskyConnecting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.blueskyConnecting) "Verbinde…" else "Verbinden")
            }
            state.blueskyError?.let { Text(it) }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (title.isNotBlank()) Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
