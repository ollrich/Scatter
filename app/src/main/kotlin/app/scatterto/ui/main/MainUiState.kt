package app.scatterto.ui.main

import app.scatterto.ui.PostStatus

/** Editierbare Textbausteine eines Netzwerks (§5.4). */
data class NetworkPost(
    val text: String = "",
    val hashtag: String = "",
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
    val isFromShare: Boolean = false,

    val metadataPhase: MetadataPhase = MetadataPhase.Idle,
    val manualTitle: String = "",
    val manualDescription: String = "",

    val generationPhase: GenerationPhase = GenerationPhase.Idle,

    val mastodonConnected: Boolean = false,
    val blueskyConnected: Boolean = false,
    val mastodonMaxChars: Int = 500,

    val mastodon: NetworkPost = NetworkPost(),
    val bluesky: NetworkPost = NetworkPost(),

    val mastodonStatus: PostStatus = PostStatus.Idle,
    val blueskyStatus: PostStatus = PostStatus.Idle,

    /** true, wenn kein Mammouth-Token gespeichert ist → Generieren gesperrt (§12.2 Nr. 11). */
    val mammouthMissing: Boolean = false,
) {
    val hasAnyConnection: Boolean get() = mastodonConnected || blueskyConnected
    val canGenerate: Boolean get() = !mammouthMissing && urlInput.isNotBlank()
    val isGenerating: Boolean get() = generationPhase is GenerationPhase.Generating ||
        metadataPhase == MetadataPhase.Loading
}
