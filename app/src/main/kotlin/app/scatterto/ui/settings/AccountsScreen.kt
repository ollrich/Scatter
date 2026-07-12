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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.scatterto.R
import app.scatterto.data.model.AccountInfo
import app.scatterto.ui.AppViewModelProvider
import app.scatterto.ui.components.NetworkHeader
import app.scatterto.ui.theme.BlueskyBlue
import app.scatterto.ui.theme.MastodonViolet
import java.text.NumberFormat
import java.util.Locale

/** Account-Verbindungen (§4.2). Trennen mit Rückfrage, da destruktiv (Token geht verloren). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state = viewModel.uiState
    var confirmDisconnect by remember { mutableStateOf<String?>(null) }

    // Live-Detailinfos laden, sobald sich der Verbindungsstatus (Konten) ändert.
    LaunchedEffect(state.mastodonConnected, state.blueskyConnected) {
        viewModel.loadAccountInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_accounts)) },
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
            MastodonCard(state, viewModel) { confirmDisconnect = "mastodon" }
            HorizontalDivider()
            BlueskyCard(state, viewModel) { confirmDisconnect = "bluesky" }
        }
    }

    confirmDisconnect?.let { which ->
        AlertDialog(
            onDismissRequest = { confirmDisconnect = null },
            title = { Text("Account trennen?") },
            text = { Text("Der gespeicherte Zugang wird entfernt. Zum Wiederverbinden musst du ihn neu eingeben.") },
            confirmButton = {
                TextButton(onClick = {
                    if (which == "mastodon") viewModel.disconnectMastodon() else viewModel.disconnectBluesky()
                    confirmDisconnect = null
                }) { Text("Trennen") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDisconnect = null }) { Text("Abbrechen") }
            },
        )
    }
}

@Composable
private fun MastodonCard(state: SettingsUiState, viewModel: SettingsViewModel, onDisconnect: () -> Unit) {
    SectionCard {
        NetworkHeader("Mastodon", MastodonViolet, state.mastodonAvatarUrl, state.mastodonHandle?.takeIf { state.mastodonConnected })
        if (state.mastodonConnected) {
            AccountDetails("Instanz", state.mastodonInfo, state.accountInfoLoading)
            OutlinedButton(onClick = onDisconnect, modifier = Modifier.fillMaxWidth()) { Text("Trennen") }
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
            ) { Text(if (state.mastodonConnecting) "Verbinde…" else "Verbinden") }
            state.mastodonError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun BlueskyCard(state: SettingsUiState, viewModel: SettingsViewModel, onDisconnect: () -> Unit) {
    SectionCard {
        NetworkHeader("Bluesky", BlueskyBlue, state.blueskyAvatarUrl, state.blueskyHandle?.takeIf { state.blueskyConnected })
        if (state.blueskyConnected) {
            AccountDetails("PDS", state.blueskyInfo, state.accountInfoLoading)
            OutlinedButton(onClick = onDisconnect, modifier = Modifier.fillMaxWidth()) { Text("Trennen") }
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
            ) { Text(if (state.blueskyConnecting) "Verbinde…" else "Verbinden") }
            state.blueskyError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}

/** Detailzeilen eines verbundenen Kontos: Server, Follower, Mitglied seit, letztes Posting, Profil. */
@Composable
private fun AccountDetails(serverLabel: String, info: AccountInfo?, loading: Boolean) {
    val uriHandler = LocalUriHandler.current
    when {
        info != null -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            DetailRow(serverLabel, info.server)
            info.followersCount?.let {
                DetailRow("Follower", NumberFormat.getInstance(Locale.GERMAN).format(it))
            }
            info.memberSince?.let { DetailRow("Mitglied seit", it) }
            info.lastPost?.let { DetailRow("Letztes Posting", it) }
            Text(
                text = "Profil öffnen",
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { uriHandler.openUri(info.profileUrl) },
            )
        }
        loading -> Text("Details werden geladen …", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun SectionCard(content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) { content() }
    }
}
