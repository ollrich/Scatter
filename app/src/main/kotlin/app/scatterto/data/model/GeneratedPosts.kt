package app.scatterto.data.model

/**
 * Ergebnis eines KI-Calls (§5.3). Je Sprache ein Text mit bereits eingebetteten
 * Rahmen-Hashtags (Medium + Thema) plus optionale ergänzende Hashtags (als Pills editierbar).
 * Für EN zusätzlich Titel/Beschreibung der englischen Bluesky-Link-Vorschau (§6); sonst null.
 */
data class GeneratedPost(
    val text: String,
    val extraHashtags: List<String>,
    val cardTitle: String? = null,
    val cardDescription: String? = null,
)

data class GeneratedPosts(
    val de: GeneratedPost,
    val en: GeneratedPost,
)
