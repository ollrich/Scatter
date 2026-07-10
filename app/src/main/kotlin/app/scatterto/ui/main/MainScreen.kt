package app.scatterto.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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

            if (state.showNetworkSelection) {
                NetworkSelection(state, viewModel)
            }

            if (!state.isDone) {
                Button(
                    onClick = viewModel::onGenerateClick,
                    enabled = state.canGenerate && !state.isGenerating,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Generieren") }
            }

            if (state.isGenerating) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            // Metadaten sind immer editierbar — sie speisen KI-Prompt und Bluesky-Link-Karte.
            if (state.metadataPhase != MetadataPhase.Idle) {
                MetadataCard(state, viewModel)
            }

            (state.generationPhase as? GenerationPhase.Error)?.let {
                Text(it.message, color = MaterialTheme.colorScheme.error)
            }

            if (state.isDone) {
                val mastodonPost = composePost(state.mastodon.text, state.mastodon.extraHashtags, state.mastodon.url)
                val blueskyPost = composePost(state.bluesky.text, state.bluesky.extraHashtags, state.bluesky.url)
                val mastodonCount = mastodonLength(mastodonPost)
                val blueskyCount = blueskyLength(blueskyPost)

                if (state.activeMastodon) {
                    NetworkPostSection(
                        name = "Mastodon",
                        color = MastodonViolet,
                        post = state.mastodon,
                        composedPost = mastodonPost,
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
                if (state.activeBluesky) {
                    NetworkPostSection(
                        name = "Bluesky",
                        color = BlueskyBlue,
                        post = state.bluesky,
                        composedPost = blueskyPost,
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

                SendTargets(state)

                val mastoOver = state.activeMastodon && mastodonCount > state.mastodonMaxChars
                val blueskyOver = state.activeBluesky && blueskyCount > 300
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = viewModel::regenerate,
                        enabled = !state.isGenerating,
                        modifier = Modifier.weight(1f),
                    ) { Text("Neu generieren") }
                    Button(
                        onClick = viewModel::onSendClick,
                        enabled = state.hasActiveTarget && !mastoOver && !blueskyOver,
                        modifier = Modifier.weight(1f),
                    ) { Text("Senden") }
                }
            }
        }
    }
}

/** Auswahl, welche verbundenen Netzwerke bespielt werden (§5). Mindestens eins bleibt aktiv. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun NetworkSelection(state: MainUiState, viewModel: MainViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Netzwerke", style = MaterialTheme.typography.labelMedium)
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
private fun SendTargets(state: MainUiState) {
    if (!state.hasActiveTarget) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Sendet an", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.activeMastodon) {
                TargetLabel("Mastodon", state.mastodonHandle, MastodonViolet)
            }
            if (state.activeBluesky) {
                TargetLabel("Bluesky", state.blueskyHandle, BlueskyBlue)
            }
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

@Composable
private fun MetadataCard(state: MainUiState, viewModel: MainViewModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.metadataPhase == MetadataPhase.NeedsManual) {
                Text(
                    "Keine Metadaten gefunden – bitte manuell ergänzen:",
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Text("Metadaten (Grundlage für KI und Link-Karte)", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedTextField(
                value = state.metaTitle,
                onValueChange = viewModel::onMetaTitleChange,
                label = { Text("Titel") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.metaDescription,
                onValueChange = viewModel::onMetaDescriptionChange,
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
    composedPost: String,
    count: Int,
    limit: Int,
    status: PostStatus,
    onText: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onUrl: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NetworkHeader(name, color, avatarUrl = null)
            OutlinedTextField(
                value = post.text,
                onValueChange = onText,
                label = { Text("Text") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = post.url,
                onValueChange = onUrl,
                label = { Text("URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            HashtagPills(post.extraHashtags, onAddTag, onRemoveTag)

            val over = count > limit
            Text(
                text = "$count / $limit",
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )

            OutlinedButton(
                onClick = { clipboard.setText(AnnotatedString(composedPost)) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Post kopieren") }

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
                        trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "entfernen") },
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
    val uriHandler = LocalUriHandler.current
    when (status) {
        PostStatus.Idle -> {}
        PostStatus.Pending -> Text("Sende…")
        is PostStatus.Success -> {
            Text("Gepostet ✓")
            status.url?.let { url ->
                Text(
                    text = url,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { uriHandler.openUri(url) },
                )
            }
        }
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
