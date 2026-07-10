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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import app.scatterto.R
import app.scatterto.ui.components.NetworkHeader
import app.scatterto.ui.main.MainUiState
import app.scatterto.ui.theme.BlueskyBlue
import app.scatterto.ui.theme.MastodonViolet

/** Inhalt des Slide-Panels: Account-Kopf + Menüpunkte (§3). [onOpen] navigiert per Route. */
@Composable
fun AppDrawerContent(state: MainUiState, onOpen: (String) -> Unit) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)),
                )
                Text("ScatterTo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                    Text("Kein Account verbunden – tippen zum Verbinden.")
                }
            }

            HorizontalDivider()

            DrawerItem("Accounts") { onOpen(Routes.ACCOUNTS) }
            DrawerItem("Mammouth-KI") { onOpen(Routes.MAMMOUTH) }
            DrawerItem("Anzeige") { onOpen(Routes.DISPLAY) }
            DrawerItem("Logs") { onOpen(Routes.LOG) }
            DrawerItem("About") { onOpen(Routes.ABOUT) }
        }
    }
}

@Composable
private fun DrawerItem(label: String, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    )
}
