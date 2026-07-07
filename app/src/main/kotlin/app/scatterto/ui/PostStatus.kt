package app.scatterto.ui

/**
 * Sende-Status pro Netzwerk (§7). Wird getrennt getrackt, damit ein Retry nur das fehlgeschlagene
 * Netzwerk erneut bespielt und ein bereits erfolgreiches nicht doppelt postet.
 */
sealed interface PostStatus {
    data object Idle : PostStatus
    data object Pending : PostStatus
    data class Success(val url: String?) : PostStatus
    data class Failed(val reason: String) : PostStatus

    /** Bluesky-Timeout mit unklarem Ausgang: nicht automatisch retryen (§12.1 Nr. 5). */
    data class Uncertain(val message: String) : PostStatus
}
