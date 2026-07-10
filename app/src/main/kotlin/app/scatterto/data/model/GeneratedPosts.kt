package app.scatterto.data.model

/**
 * Ergebnis eines KI-Calls (§5.3). Je Sprache ein Text mit bereits eingebetteten
 * Rahmen-Hashtags (Medium + Thema) plus optionale ergänzende Hashtags (als Pills editierbar).
 */
data class GeneratedPost(
    val text: String,
    val extraHashtags: List<String>,
)

data class GeneratedPosts(
    val de: GeneratedPost,
    val en: GeneratedPost,
)
