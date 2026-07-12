package app.scatterto.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.scatterto.R
import app.scatterto.ui.PostStatus
import app.scatterto.ui.components.NetworkHeader

/** Volle Post-Sektion eines Netzwerks: Farbkante, Header mit Kopier-Icon, Text, URL, Pills, Zähler. */
@Composable
internal fun NetworkPostSection(
    name: String,
    color: Color,
    avatarUrl: String?,
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

    AccentCard(color) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                NetworkHeader(name, color, avatarUrl)
            }
            IconButton(onClick = { clipboard.setText(AnnotatedString(composedPost)) }) {
                Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.copy_post))
            }
        }

        OutlinedTextField(
            value = post.text,
            onValueChange = onText,
            label = { Text(stringResource(R.string.label_text)) },
            modifier = Modifier.fillMaxWidth(),
        )

        UrlRow(post.url, onUrl)
        HashtagPills(post.extraHashtags, onAddTag, onRemoveTag)

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

/** Kompakte Erfolgskarte: ersetzt die editierbare Sektion nach erfolgreichem Senden. */
@Composable
internal fun SuccessCard(name: String, color: Color, avatarUrl: String?, postUrl: String?) {
    val uriHandler = LocalUriHandler.current
    AccentCard(color) {
        NetworkHeader(name, color, avatarUrl)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.posted))
            postUrl?.let { url ->
                Text(
                    text = stringResource(R.string.open_post),
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { uriHandler.openUri(url) },
                )
            }
        }
    }
}

/** Netzwerk wurde nach der Generierung aktiviert — es gibt dafür noch keinen Text. */
@Composable
internal fun MissingTextHint(network: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.missing_text_hint, network),
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Card mit farbiger Kante links (Netzwerk-Farbe). Die Kante wird direkt gezeichnet (kein
 * IntrinsicSize + fillMaxHeight) — sonst blähen TextField-Intrinsics die Card auf (Leerraum-Bug).
 */
@Composable
private fun AccentCard(color: Color, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .drawBehind { drawRect(color = color, size = Size(4.dp.toPx(), size.height)) }
                .padding(start = 20.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { content() }
    }
}

/** URL kompakt als Textzeile; editieren nur auf Wunsch (Stift) — spart ein drittes volles Feld. */
@Composable
private fun UrlRow(url: String, onUrl: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    if (editing) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrl,
            label = { Text(stringResource(R.string.label_url)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { editing = false }),
            trailingIcon = {
                IconButton(onClick = { editing = false }) {
                    Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.cd_done))
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { editing = true }) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit_url))
            }
        }
    }
}

/** Pills + „+"-Chip; das Eingabefeld erscheint nur beim Hinzufügen. */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HashtagPills(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
) {
    var adding by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                InputChip(
                    selected = false,
                    onClick = { onRemoveTag(tag) },
                    label = { Text(tag) },
                    trailingIcon = { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_remove)) },
                )
            }
            AssistChip(
                onClick = { adding = true },
                label = { Text("#+") },
                leadingIcon = { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_hashtag)) },
            )
        }
        if (adding) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(stringResource(R.string.add_hashtag)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAddTag(input); input = ""; adding = false }),
                trailingIcon = {
                    IconButton(onClick = { onAddTag(input); input = ""; adding = false }) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.cd_add))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun StatusRow(status: PostStatus, onRetry: () -> Unit) {
    when (status) {
        PostStatus.Idle -> {}
        PostStatus.Pending -> Text(stringResource(R.string.sending))
        is PostStatus.Success -> Text(stringResource(R.string.posted)) // i. d. R. ersetzt SuccessCard die Sektion
        is PostStatus.Failed -> {
            Text(status.reason, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        }
        is PostStatus.Uncertain -> {
            Text(status.message, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry_uncertain)) }
        }
    }
}
