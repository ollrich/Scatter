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
    /** URL, deren Metadaten (zuletzt) geladen wurden — Basis für „Generieren bei neuer URL". */
    val fetchedUrl: String? = null,
    /** true, sobald der Nutzer generierte Post-Texte bearbeitet hat (Warnung vor Neu-Generieren). */
    val postsEdited: Boolean = false,
    /** Anzeigename des Modells während der Generierung (z. B. „Claude (Opus)"). */
    val generatingWith: String? = null,

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
    // Post-Sprache je Netzwerk (BCP-47), aus dem verbundenen Konto (§4.2).
    val mastodonLanguage: String = "de",
    val blueskyLanguage: String = "en",

    val mastodon: NetworkPost = NetworkPost(),
    val bluesky: NetworkPost = NetworkPost(),

    val mastodonStatus: PostStatus = PostStatus.Idle,
    val blueskyStatus: PostStatus = PostStatus.Idle,

    /** KI aktiv? Ist sie aus, schreibt der Nutzer die Texte selbst (§4.1). */
    val aiEnabled: Boolean = true,
    /** true, wenn KI aktiv, aber kein Token für den gewählten Dienst gespeichert ist (§12.2 Nr. 11). */
    val aiTokenMissing: Boolean = false,
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

    /**
     * Ein aktives Netzwerk hat keinen Text (wird beim Senden übersprungen), während ein anderes
     * sendbar ist — Grundlage für einen Hinweis, dass nicht alles Aktive rausgeht (§7).
     */
    val hasPartialEmptyTarget: Boolean get() = hasSendableTarget &&
        ((activeMastodon && mastodon.text.isBlank()) || (activeBluesky && bluesky.text.isBlank()))

    /** URL im Feld weicht von der zuletzt geladenen ab → „Generieren" wieder anbieten. */
    val urlChanged: Boolean get() = urlInput.isNotBlank() && urlInput.trim() != fetchedUrl

    /** Alle sendbaren Ziele erfolgreich → Abschluss-Zustand (Neuer-Artikel-Aktion). */
    val allSent: Boolean get() = hasSendableTarget &&
        (!mastodonSendable || mastodonStatus is PostStatus.Success) &&
        (!blueskySendable || blueskyStatus is PostStatus.Success)

    /** Auswahl-Chips nur zeigen, wenn es überhaupt etwas zu wählen gibt (beide verbunden). */
    val showNetworkSelection: Boolean get() = mastodonConnected && blueskyConnected

    val hasMetadata: Boolean get() = metaTitle.isNotBlank() || metaDescription.isNotBlank()
    val isGenerating: Boolean get() = generationPhase is GenerationPhase.Generating ||
        metadataPhase == MetadataPhase.Loading

    /**
     * Ohne Metadaten hat die KI keine Grundlage — im Fallback erst nach manueller Eingabe (§12.2 Nr. 2).
     * Bei aktiver KI ist zusätzlich ein Token nötig; bei ausgeschalteter KI nicht.
     */
    val canGenerate: Boolean get() = !aiTokenMissing && urlInput.isNotBlank() && hasActiveTarget &&
        // Metadaten-Pflicht nur bei aktiver KI (die braucht Grundlage); wer selbst schreibt, nicht.
        (!aiEnabled || metadataPhase != MetadataPhase.NeedsManual || hasMetadata)

    val isDone: Boolean get() = generationPhase is GenerationPhase.Done
}
