package app.scatterto.ui.main

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.scatterto.R
import app.scatterto.core.extractUrl
import app.scatterto.ui.theme.BlueskyBlue
import app.scatterto.ui.theme.MastodonViolet
import kotlinx.coroutines.launch

/** Auswahl, welche verbundenen Netzwerke bespielt werden (§5). Mindestens eins bleibt aktiv. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun NetworkSelection(state: MainUiState, viewModel: MainViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.networks), style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.mastodonEnabled,
                onClick = viewModel::toggleMastodon,
                label = { Text("Mastodon") },
            )
            FilterChip(
                selected = state.blueskyEnabled,
                onClick = viewModel::toggleBluesky,
                label = { Text("Bluesky") },
            )
        }
    }
}

/** Zeigt vor dem Absenden, an welche Accounts gesendet wird (§4.2-Farben). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SendTargets(state: MainUiState) {
    if (!state.hasSendableTarget) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.sends_to), style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.mastodonSendable) TargetLabel("Mastodon", state.mastodonHandle, MastodonViolet)
            if (state.blueskySendable) TargetLabel("Bluesky", state.blueskyHandle, BlueskyBlue)
        }
    }
}

@Composable
private fun TargetLabel(network: String, handle: String?, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(network, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        if (!handle.isNullOrBlank()) {
            Text("@$handle", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Metadaten (Input für KI + Link-Karte). Nach erfolgreichem Laden eingeklappt — man braucht sie
 * selten; im manuellen Fallback (§12.2 Nr. 2) zwangsweise ausgeklappt.
 */
@Composable
internal fun MetadataCard(state: MainUiState, viewModel: MainViewModel) {
    // Pro Artikel gemerkt (Key = geladene URL): Aufklappen für Artikel A soll Artikel B
    // nicht aufgeklappt starten lassen.
    var expandedByUser by rememberSaveable(state.fetchedUrl) { mutableStateOf(false) }
    val expanded = expandedByUser || state.metadataPhase == MetadataPhase.NeedsManual

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedByUser = !expandedByUser },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    if (state.metadataPhase == MetadataPhase.NeedsManual) {
                        Text(
                            stringResource(R.string.metadata_missing),
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Text(stringResource(R.string.metadata_title), style = MaterialTheme.typography.labelMedium)
                        if (!expanded && state.metaTitle.isNotBlank()) {
                            Text(
                                state.metaTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                )
            }

            if (expanded) {
                OutlinedTextField(
                    value = state.metaTitle,
                    onValueChange = viewModel::onMetaTitleChange,
                    label = { Text(stringResource(R.string.label_meta_title)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.metaDescription,
                    onValueChange = viewModel::onMetaDescriptionChange,
                    label = { Text(stringResource(R.string.label_meta_description)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (state.metadataPhase == MetadataPhase.NeedsManual) {
                    // Zweiter Abruf derselben URL (z. B. Funkloch) — sonst gäbe es dafür
                    // keinen Weg außer Handeingabe oder URL-Änderung.
                    OutlinedButton(onClick = viewModel::retryMetadata) {
                        Text(stringResource(R.string.metadata_retry))
                    }
                }
            }
        }
    }
}

/** Feedback während des KI-Calls: Spinner + welches Modell gerade schreibt. */
@Composable
internal fun GeneratingIndicator(modelLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp))
        Text(
            stringResource(R.string.generating_with, modelLabel),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Bietet an, eine URL aus der Zwischenablage zu übernehmen (Zugriff erst beim Tippen). */
@Composable
internal fun ClipboardSuggestion(onUrlFound: (String) -> Unit) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val missMessage = stringResource(R.string.clipboard_no_url)

    AssistChip(
        onClick = {
            scope.launch {
                val text = clipboard.getClipEntry()?.clipData
                    ?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()
                val url = text?.let(::extractUrl)
                if (url != null) {
                    onUrlFound(url)
                } else {
                    Toast.makeText(context, missMessage, Toast.LENGTH_SHORT).show()
                }
            }
        },
        label = { Text(stringResource(R.string.paste_from_clipboard)) },
        leadingIcon = { Icon(Icons.Filled.ContentPaste, contentDescription = null) },
    )
}

@Composable
internal fun InfoBanner(message: String, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(message)
            OutlinedButton(onClick = onAction) { Text(stringResource(R.string.to_settings)) }
        }
    }
}
