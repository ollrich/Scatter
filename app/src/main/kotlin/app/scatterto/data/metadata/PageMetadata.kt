package app.scatterto.data.metadata

/**
 * Aus der Artikelseite extrahierte Metadaten (§5.2). Alle Felder optional —
 * fehlt Brauchbares, greift der manuelle Fallback in der UI.
 */
data class PageMetadata(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null, // og:image, für die Bluesky-Link-Karte (§6)
    val siteName: String? = null, // og:site_name, Hinweis für den Medien-Hashtag im Prompt
    val language: String? = null, // ISO-639-1 aus <html lang>/og:locale — Basis für den geplanten „Artikel auf Deutsch"-Hinweis
) {
    val isUsable: Boolean get() = !title.isNullOrBlank() || !description.isNullOrBlank()
}
