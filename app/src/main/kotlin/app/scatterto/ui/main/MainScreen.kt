package app.scatterto.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.scatterto.core.blueskyLength
import app.scatterto.core.composePost
import app.scatterto.core.mastodonLength
import app.scatterto.ui.AppViewModelProvider
import app.scatterto.ui.PostStatus
import app.scatterto.ui.components.NetworkHeader
import app.scatterto.ui.theme.BlueskyBlue
import app.scatterto.ui.theme.MastodonViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sharedText: String?,
    sharedSubject: String?,
    onSharedConsumed: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state = viewModel.uiState

    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            viewModel.onSharedText(sharedText, sharedSubject)
            onSharedConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ScatterTo") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Einstellungen")
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
            if (state.mammouthMissing) {
                InfoBanner("Kein Mammouth-Token gespeichert. Bitte in den Einstellungen hinterlegen.", onOpenSettings)
            }
            if (!state.hasAnyConnection) {
                InfoBanner("Kein Netzwerk verbunden. Bitte in den Einstellungen verbinden.", onOpenSettings)
            }

            OutlinedTextField(
                value = state.urlInput,
                onValueChange = viewModel::onUrlChange,
                label = { Text("Artikel-URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = viewModel::onGenerateClick,
                enabled = state.canGenerate && !state.isGenerating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Generieren")
            }

            if (state.isGenerating) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (state.metadataPhase == MetadataPhase.NeedsManual) {
                ManualMetadata(state, viewModel)
            }

            (state.generationPhase as? GenerationPhase.Error)?.let {
                Text(it.message, color = MaterialTheme.colorScheme.error)
            }

            if (state.generationPhase is GenerationPhase.Done) {
                val mastodonCount = mastodonLength(
                    composePost(state.mastodon.text, state.mastodon.extraHashtags, state.mastodon.url),
                )
                val blueskyCount = blueskyLength(
                    composePost(state.bluesky.text, state.bluesky.extraHashtags, state.bluesky.url),
                )

                if (state.mastodonConnected) {
                    NetworkPostSection(
                        name = "Mastodon",
                        color = MastodonViolet,
                        post = state.mastodon,
                        count = mastodonCount,
                        limit = state.mastodonMaxChars,
                        status = state.mastodonStatus,
                        onText = viewModel::onMastodonTextChange,
                        onAddTag = viewModel::addMastodonHashtag,
                        onRemoveTag = viewModel::removeMastodonHashtag,
                        onUrl = viewModel::onMastodonUrlChange,
                        onRetry = viewModel::retryMastodon,
                    )
                }
                if (state.blueskyConnected) {
                    NetworkPostSection(
                        name = "Bluesky",
                        color = BlueskyBlue,
                        post = state.bluesky,
                        count = blueskyCount,
                        limit = 300,
                        status = state.blueskyStatus,
                        onText = viewModel::onBlueskyTextChange,
                        onAddTag = viewModel::addBlueskyHashtag,
                        onRemoveTag = viewModel::removeBlueskyHashtag,
                        onUrl = viewModel::onBlueskyUrlChange,
                        onRetry = viewModel::retryBluesky,
                    )
                }

                val mastoOver = state.mastodonConnected && mastodonCount > state.mastodonMaxChars
                val blueskyOver = state.blueskyConnected && blueskyCount > 300
                Button(
                    onClick = viewModel::onSendClick,
                    enabled = state.hasAnyConnection && !mastoOver && !blueskyOver,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("An verbundene Netzwerke senden")
                }
            }
        }
    }
}

@Composable
private fun ManualMetadata(state: MainUiState, viewModel: MainViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Keine Metadaten gefunden – bitte manuell ergänzen:")
            OutlinedTextField(
                value = state.manualTitle,
                onValueChange = viewModel::onManualTitleChange,
                label = { Text("Titel") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.manualDescription,
                onValueChange = viewModel::onManualDescriptionChange,
                label = { Text("Beschreibung") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun NetworkPostSection(
    name: String,
    color: Color,
    post: NetworkPost,
    count: Int,
    limit: Int,
    status: PostStatus,
    onText: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onUrl: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NetworkHeader(name, color, avatarUrl = null)
            OutlinedTextField(
                value = post.text,
                onValueChange = onText,
                label = { Text("Text") },
                modifier = Modifier.fillMaxWidth(),
            )
            HashtagPills(post.extraHashtags, onAddTag, onRemoveTag)
            OutlinedTextField(
                value = post.url,
                onValueChange = onUrl,
                label = { Text("URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            val over = count > limit
            Text(
                text = "$count / $limit",
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
            StatusRow(status, onRetry)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HashtagPills(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Ergänzende Hashtags", style = MaterialTheme.typography.labelMedium)
        if (tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { onRemoveTag(tag) },
                        label = { Text(tag) },
                        trailingIcon = {
                            Icon(Icons.Filled.Close, contentDescription = "entfernen")
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Hashtag hinzufügen") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAddTag(input); input = "" }),
            trailingIcon = {
                IconButton(onClick = { onAddTag(input); input = "" }) {
                    Icon(Icons.Filled.Add, contentDescription = "hinzufügen")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StatusRow(status: PostStatus, onRetry: () -> Unit) {
    when (status) {
        PostStatus.Idle -> {}
        PostStatus.Pending -> Text("Sende…")
        is PostStatus.Success -> Text("Gepostet ✓" + (status.url?.let { "  ($it)" } ?: ""))
        is PostStatus.Failed -> {
            Text(status.reason, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text("Erneut versuchen") }
        }
        is PostStatus.Uncertain -> {
            Text(status.message, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text("Trotzdem erneut senden") }
        }
    }
}

@Composable
private fun InfoBanner(message: String, onOpenSettings: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(message)
            OutlinedButton(onClick = onOpenSettings) { Text("Zu den Einstellungen") }
        }
    }
}
