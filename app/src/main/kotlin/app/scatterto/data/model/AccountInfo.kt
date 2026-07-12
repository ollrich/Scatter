package app.scatterto.data.model

/**
 * Live abgerufene Zusatzinfos zu einem verbundenen Konto (Konten-Screen). Alle Felder optional —
 * fehlt etwas (API liefert es nicht, Call scheitert), wird die Zeile weggelassen.
 */
data class AccountInfo(
    val server: String, // Instanz-Host (Mastodon) bzw. PDS-Host (Bluesky)
    val profileUrl: String,
    val followersCount: Int? = null,
    val memberSince: String? = null, // z. B. „Juli 2024"
    val lastPost: String? = null, // z. B. „10.07.2026"
)
