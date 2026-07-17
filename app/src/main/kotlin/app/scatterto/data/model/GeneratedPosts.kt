package app.scatterto.data.model

/**
 * Ergebnis eines KI-Calls (§5.3). Je Netzwerk ein reiner Post-Text (ohne Hashtags, ohne URL)
 * plus die Hashtags als Liste (als Pills editierbar, werden beim Zusammensetzen angehängt).
 * Für Bluesky zusätzlich Titel/Beschreibung der Link-Vorschau (§6); sonst null.
 */
data class GeneratedPost(
    val text: String,
    val extraHashtags: List<String>,
    val cardTitle: String? = null,
    val cardDescription: String? = null,
)

data class GeneratedPosts(
    val mastodon: GeneratedPost,
    val bluesky: GeneratedPost,
)
