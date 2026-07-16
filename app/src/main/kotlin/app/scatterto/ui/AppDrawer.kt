package app.scatterto.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import app.scatterto.R
import app.scatterto.ui.components.NetworkHeader
import app.scatterto.ui.main.MainUiState
import app.scatterto.ui.theme.BlueskyBlue
import app.scatterto.ui.theme.MastodonViolet
import app.scatterto.ui.theme.isLightTheme

/** Inhalt des Slide-Panels: Account-Kopf + Menüpunkte (§3). [onOpen] navigiert per Route. */
@Composable
fun AppDrawerContent(state: MainUiState, onClose: () -> Unit, onOpen: (String) -> Unit) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)),
                )
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.menu_close))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Verbundene Accounts — tippbar zur Accounts-Unterseite.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpen(Routes.ACCOUNTS) }
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.mastodonConnected) {
                    NetworkHeader("Mastodon", MastodonViolet, state.mastodonAvatarUrl, state.mastodonHandle)
                }
                if (state.blueskyConnected) {
                    NetworkHeader("Bluesky", BlueskyBlue, state.blueskyAvatarUrl, state.blueskyHandle)
                }
                if (!state.hasAnyConnection) {
                    Text(stringResource(R.string.no_account_hint))
                }
            }

            HorizontalDivider()

            DrawerItem(stringResource(R.string.menu_accounts), Icons.Filled.AccountCircle) { onOpen(Routes.ACCOUNTS) }
            DrawerItem(stringResource(R.string.menu_display), Icons.Filled.Palette) { onOpen(Routes.DISPLAY) }
            DrawerItem(stringResource(R.string.menu_ai), Icons.Filled.SmartToy) { onOpen(Routes.AI) }
            DrawerItem(stringResource(R.string.menu_log), Icons.Filled.History) { onOpen(Routes.LOG) }
            DrawerItem(stringResource(R.string.menu_about), Icons.Filled.Info) { onOpen(Routes.ABOUT) }
        }
    }
}

@Composable
private fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    // Menü-Icons im Hell-Modus in der Primärfarbe (Marken-Blau bzw. Material You, je nach Schalter);
    // im Dunkel-Modus unverändert die Standard-Icon-Farbe.
    val iconTint = if (isLightTheme()) MaterialTheme.colorScheme.primary else LocalContentColor.current
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(icon, contentDescription = null, tint = iconTint) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    )
}
