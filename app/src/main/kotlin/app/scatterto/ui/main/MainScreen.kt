package app.scatterto.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import app.scatterto.R
import app.scatterto.core.blueskyLength
import app.scatterto.core.composePost
import app.scatterto.core.mastodonLength
import app.scatterto.ui.AppDrawerContent
import app.scatterto.ui.AppViewModelProvider
import app.scatterto.ui.PostStatus
import app.scatterto.ui.Routes
import app.scatterto.ui.theme.BlueskyBlue
import app.scatterto.ui.theme.MastodonViolet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    sharedText: String?,
    sharedSubject: String?,
    onSharedConsumed: () -> Unit,
    onOpen: (String) -> Unit,
    viewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state = viewModel.uiState
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var confirmRegenerate by remember { mutableStateOf(false) }

    // Zurück schließt zuerst das offene Panel (statt die App zu verlassen).
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            // Der Drawer ist saveable und käme sonst offen zurück — er würde die Einstiegsseite
            // mit dem frisch geteilten Link verdecken.
            drawerState.close()
            viewModel.onSharedText(sharedText, sharedSubject)
            onSharedConsumed()
        }
    }

    // Verbindungen nach Rückkehr aus den Einstellungen neu laden (Panel-Kopf + Auswahl aktuell halten).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshConnections()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Zusammengesetzte Posts + Limits einmal berechnen (Sektionen + Bottom-Bar nutzen sie).
    val mastodonPost = composePost(state.mastodon.text, state.mastodon.extraHashtags, state.mastodon.url)
    // Bluesky: URL nicht im Text — die Link-Karte trägt den Link (spart Budget).
    val blueskyPost = composePost(state.bluesky.text, state.bluesky.extraHashtags, state.bluesky.url, includeUrl = false)
    val mastodonCount = mastodonLength(mastodonPost)
    val blueskyCount = blueskyLength(blueskyPost)
    val mastoOver = state.mastodonSendable && mastodonCount > state.mastodonMaxChars
    val blueskyOver = state.blueskySendable && blueskyCount > 300
    val canSend = state.hasSendableTarget && !mastoOver && !blueskyOver

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Panel bewusst NICHT schließen beim Navigieren: so führt „Zurück" aus einer Unterseite
            // wieder ins Menü. Zum Verlassen dient das X im Kopf (onClose).
            AppDrawerContent(
                state = state,
                onClose = { scope.launch { drawerState.close() } },
                onOpen = { route -> onOpen(route) },
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Image(
                                painter = painterResource(R.drawable.ic_logo),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)),
                            )
                            Text(stringResource(R.string.app_name))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.menu_open))
                        }
                    },
                )
            },
            bottomBar = {
                // Primäraktionen immer erreichbar — kein Scrollen bis ans Listenende (UX-Audit).
                if (state.isDone) {
                    MainBottomBar(
                        allSent = state.allSent,
                        canSend = canSend,
                        showRegenerate = state.aiEnabled,
                        regenerateEnabled = !state.aiTokenMissing,
                        isGenerating = state.isGenerating,
                        onRegenerate = {
                            if (state.postsEdited) confirmRegenerate = true else viewModel.regenerate()
                        },
                        onSend = viewModel::onSendClick,
                        onNewArticle = viewModel::startNew,
                    )
                }
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
                if (state.aiTokenMissing) {
                    InfoBanner(stringResource(R.string.banner_no_ai)) { onOpen(Routes.AI) }
                }
                if (!state.hasAnyConnection) {
                    // Frischinstallation: statt nur Banner eine Erste-Schritte-Checkliste.
                    OnboardingCard(onOpen)
                } else if (state.urlInput.isBlank() && !state.isDone) {
                    Text(
                        stringResource(R.string.start_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedTextField(
                    value = state.urlInput,
                    onValueChange = viewModel::onUrlChange,
                    label = { Text(stringResource(R.string.article_url)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (state.urlInput.isBlank()) {
                    ClipboardSuggestion(onUrlFound = viewModel::onUrlChange)
                }

                if (state.showNetworkSelection) {
                    NetworkSelection(state, viewModel)
                }

                // Sichtbar bis zur ersten Generierung — und danach wieder, sobald eine NEUE URL
                // im Feld steht (sonst gäbe es keinen Weg zum nächsten Artikel; „Neu generieren"
                // würde mit den alten Metadaten arbeiten).
                if (!state.isDone || state.urlChanged) {
                    Button(
                        onClick = viewModel::onGenerateClick,
                        enabled = state.canGenerate && !state.isGenerating,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(stringResource(if (state.aiEnabled) R.string.generate else R.string.ai_continue)) }
                }

                if (state.generatingWith != null) {
                    GeneratingIndicator(state.generatingWith)
                }

                if (state.metadataPhase != MetadataPhase.Idle) {
                    MetadataCard(state, viewModel)
                }

                (state.generationPhase as? GenerationPhase.Error)?.let {
                    Text(it.message, color = MaterialTheme.colorScheme.error)
                }

                if (state.isDone) {
                    val mastodonSuccess = state.mastodonStatus as? PostStatus.Success
                    when {
                        state.activeMastodon && mastodonSuccess != null ->
                            SuccessCard("Mastodon", MastodonViolet, state.mastodonAvatarUrl, mastodonSuccess.url)
                        state.activeMastodon -> NetworkPostSection(
                            name = "Mastodon",
                            color = MastodonViolet,
                            avatarUrl = state.mastodonAvatarUrl,
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

                    val blueskySuccess = state.blueskyStatus as? PostStatus.Success
                    when {
                        state.activeBluesky && blueskySuccess != null ->
                            SuccessCard("Bluesky", BlueskyBlue, state.blueskyAvatarUrl, blueskySuccess.url)
                        state.activeBluesky -> NetworkPostSection(
                            name = "Bluesky",
                            color = BlueskyBlue,
                            avatarUrl = state.blueskyAvatarUrl,
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

                    if (state.showCardPreview) {
                        BlueskyCardPreview(
                            title = state.cardTitle,
                            description = state.cardDescription,
                            imageUrl = state.cardImageUrl,
                            onTitle = viewModel::onCardTitleChange,
                            onDescription = viewModel::onCardDescriptionChange,
                        )
                    }

                    if (!state.allSent) {
                        SendTargets(state)
                        if (state.isDone && state.hasPartialEmptyTarget) {
                            Text(
                                stringResource(R.string.partial_send_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmRegenerate) {
        AlertDialog(
            onDismissRequest = { confirmRegenerate = false },
            title = { Text(stringResource(R.string.regen_confirm_title)) },
            text = { Text(stringResource(R.string.regen_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmRegenerate = false
                    viewModel.regenerate()
                }) { Text(stringResource(R.string.regenerate)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRegenerate = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

/**
 * Bluesky-Link-Karte (§6): eingeklappt eine Zeile, aufgeklappt editierbar. Eingeklappt, weil die
 * Karte im Normalfall stimmt und die Hauptseite sonst zuwächst; editierbar, weil sie sonst die
 * einzige Sache wäre, die ungeprüft rausgeht.
 *
 * Nur Bluesky: Mastodon baut die Karte serverseitig aus den OG-Tags der URL, dort gibt es nichts
 * zu editieren.
 */
@Composable
private fun BlueskyCardPreview(
    title: String,
    description: String,
    imageUrl: String?,
    onTitle: (String) -> Unit,
    onDescription: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.card_preview_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        title.ifBlank { stringResource(R.string.card_preview_empty) },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = stringResource(
                        if (expanded) R.string.card_preview_collapse else R.string.card_preview_expand,
                    ),
                )
            }

            if (expanded) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitle,
                    label = { Text(stringResource(R.string.card_title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescription,
                    label = { Text(stringResource(R.string.card_description_label)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(R.string.card_preview_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Erste-Schritte-Checkliste auf der leeren Hauptseite, solange kein Konto verbunden ist. */
@Composable
private fun OnboardingCard(onOpen: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.titleMedium)
            OnboardingStep(
                title = stringResource(R.string.onboarding_connect),
                subtitle = stringResource(R.string.onboarding_connect_sub),
                onClick = { onOpen(Routes.ACCOUNTS) },
            )
            OnboardingStep(
                title = stringResource(R.string.onboarding_ai),
                subtitle = stringResource(R.string.onboarding_ai_sub),
                onClick = { onOpen(Routes.AI) },
            )
            Text(
                stringResource(R.string.onboarding_then),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun OnboardingStep(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

/** Fixe Aktionsleiste nach der Generierung: Senden dominant; nach Komplett-Erfolg „Neuer Artikel". */
@Composable
private fun MainBottomBar(
    allSent: Boolean,
    canSend: Boolean,
    showRegenerate: Boolean,
    regenerateEnabled: Boolean,
    isGenerating: Boolean,
    onRegenerate: () -> Unit,
    onSend: () -> Unit,
    onNewArticle: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (allSent) {
                Button(onClick = onNewArticle, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.new_article))
                }
            } else {
                if (showRegenerate) {
                    FilledTonalButton(
                        onClick = onRegenerate,
                        enabled = !isGenerating && regenerateEnabled,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.regenerate), maxLines = 1) }
                }
                Button(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.send), maxLines = 1) }
            }
        }
    }
}
