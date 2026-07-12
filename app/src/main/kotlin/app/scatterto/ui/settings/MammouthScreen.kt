package app.scatterto.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.scatterto.R
import app.scatterto.data.model.ModelChoices
import app.scatterto.ui.AppViewModelProvider

/** Mammouth-Token + Modellauswahl (§4.1). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MammouthScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state = viewModel.uiState
    var tokenVisible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

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
            OutlinedTextField(
                value = state.mammouthToken,
                onValueChange = viewModel::onMammouthTokenChange,
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

            val selectedLabel = ModelChoices.entries.firstOrNull { it.key == state.modelChoiceKey }
                ?.label ?: state.modelChoiceKey

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.model_label)) },
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
                    label = { Text(stringResource(R.string.custom_model_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Button(onClick = viewModel::saveMammouth, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.save))
            }

            when (val v = state.mammouthValidation) {
                is ValidationState.Validating -> Text(stringResource(R.string.validate_checking))
                is ValidationState.Valid -> Text(stringResource(R.string.validate_valid))
                is ValidationState.Invalid -> Text(v.message)
                ValidationState.None -> {}
            }
        }
    }
}
