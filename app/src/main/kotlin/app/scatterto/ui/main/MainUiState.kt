package app.scatterto.ui.main

import app.scatterto.ui.PostStatus

/**
 * Editierbare Bausteine eines Netzwerks (§5.4): Text mit eingebetteten Rahmen-Hashtags,
 * ergänzende Hashtags als Pill-Liste, sowie die URL.
 */
data class NetworkPost(
    val text: String = "",
    val extraHashtags: List<String> = emptyList(),
    val url: String = "",
)

enum class MetadataPhase { Idle, Loading, NeedsManual, Ready }

sealed interface GenerationPhase {
    data object Idle : GenerationPhase
    data object Generating : GenerationPhase
    data object Done : GenerationPhase
    data class Error(val message: String) : GenerationPhase
}

/** Gesamter UI-Zustand der Hauptseite (§5). */
data class MainUiState(
    val urlInput: String = "",

    val metadataPhase: MetadataPhase = MetadataPhase.Idle,
    /** Titel/Beschreibung sind IMMER editierbar — sie sind der Input für die KI und die Link-Karte. */
    val metaTitle: String = "",
    val metaDescription: String = "",

    val generationPhase: GenerationPhase = GenerationPhase.Idle,

    val mastodonConnected: Boolean = false,
    val blueskyConnected: Boolean = false,
    // Pro-Post-Auswahl (§5): standardmäßig beide an, einzeln abwählbar.
    val mastodonEnabled: Boolean = true,
    val blueskyEnabled: Boolean = true,
    val mastodonHandle: String? = null,
    val blueskyHandle: String? = null,
    val mastodonAvatarUrl: String? = null,
    val blueskyAvatarUrl: String? = null,
    val mastodonMaxChars: Int = 500,

    val mastodon: NetworkPost = NetworkPost(),
    val bluesky: NetworkPost = NetworkPost(),

    val mastodonStatus: PostStatus = PostStatus.Idle,
    val blueskyStatus: PostStatus = PostStatus.Idle,

    /** true, wenn kein Mammouth-Token gespeichert ist → Generieren gesperrt (§12.2 Nr. 11). */
    val mammouthMissing: Boolean = false,
) {
    val hasAnyConnection: Boolean get() = mastodonConnected || blueskyConnected

    /** Ein Netzwerk ist Ziel, wenn verbunden UND ausgewählt. */
    val activeMastodon: Boolean get() = mastodonConnected && mastodonEnabled
    val activeBluesky: Boolean get() = blueskyConnected && blueskyEnabled
    val hasActiveTarget: Boolean get() = activeMastodon || activeBluesky

    /**
     * Sendbar = aktiv UND es wurde Text generiert. Ein nach der Generierung aktivierter Chip
     * hat (noch) keinen Inhalt — sonst ginge ein leerer Nur-URL-Post raus.
     */
    val mastodonSendable: Boolean get() = activeMastodon && mastodon.text.isNotBlank()
    val blueskySendable: Boolean get() = activeBluesky && bluesky.text.isNotBlank()
    val hasSendableTarget: Boolean get() = mastodonSendable || blueskySendable

    /** Auswahl-Chips nur zeigen, wenn es überhaupt etwas zu wählen gibt (beide verbunden). */
    val showNetworkSelection: Boolean get() = mastodonConnected && blueskyConnected

    val hasMetadata: Boolean get() = metaTitle.isNotBlank() || metaDescription.isNotBlank()
    val isGenerating: Boolean get() = generationPhase is GenerationPhase.Generating ||
        metadataPhase == MetadataPhase.Loading

    /** Ohne Metadaten hat die KI keine Grundlage — im Fallback erst nach manueller Eingabe (§12.2 Nr. 2). */
    val canGenerate: Boolean get() = !mammouthMissing && urlInput.isNotBlank() && hasActiveTarget &&
        (metadataPhase != MetadataPhase.NeedsManual || hasMetadata)

    val isDone: Boolean get() = generationPhase is GenerationPhase.Done
}
