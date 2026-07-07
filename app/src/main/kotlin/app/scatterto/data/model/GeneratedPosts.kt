package app.scatterto.data.model

/**
 * Ergebnis eines KI-Calls (§5.3): zwei eigenständige Texte + je ein Hashtag.
 * Bereits normalisiert (Hashtags mit „#", getrimmt).
 */
data class GeneratedPosts(
    val deText: String,
    val deHashtag: String,
    val enText: String,
    val enHashtag: String,
)
