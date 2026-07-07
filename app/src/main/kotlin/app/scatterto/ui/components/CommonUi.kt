package app.scatterto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/** Eingefärbter Netzwerk-Titel mit Avatar (§4.2, §5.4). */
@Composable
fun NetworkHeader(
    name: String,
    color: Color,
    avatarUrl: String?,
    handle: String? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NetworkAvatar(avatarUrl, color)
        Text(text = name, color = color, fontWeight = FontWeight.Bold)
        if (handle != null) Text(text = "@$handle")
    }
}

@Composable
fun NetworkAvatar(url: String?, tint: Color) {
    if (url.isNullOrBlank()) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(tint))
    } else {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.size(32.dp).clip(CircleShape),
        )
    }
}
