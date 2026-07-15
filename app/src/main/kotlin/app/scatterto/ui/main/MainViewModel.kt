package app.scatterto.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.scatterto.core.composePost
import app.scatterto.core.computeFacets
import app.scatterto.core.extractUrl
import app.scatterto.core.mediumNameFrom
import app.scatterto.core.normalizeHashtag
import app.scatterto.core.stripTrackingParams
import app.scatterto.data.AppContainer
import app.scatterto.data.bluesky.LinkCard
import app.scatterto.R
import app.scatterto.data.mammouth.GenTarget
import app.scatterto.data.mammouth.PromptBuilder
import app.scatterto.data.metadata.PageMetadata
import app.scatterto.data.model.PostLanguages
import app.scatterto.data.net.ApiException
import app.scatterto.ui.PostStatus
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.util.UUID

/**
 * Orchestriert die Hauptseite (§5) und das getrennte Sende-/Retry-Handling (§7).
 * Der übergebene [savedStateHandle] rettet URL, Metadaten und generierte Inhalte über
 * Process-Death (§12.3 Nr. 4).
 */
class MainViewModel(
    private val container: AppContainer,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var uiState by mutableStateOf(MainUiState())
        private set

    // Protokoll für die Diagnose (§8: niemals Credentials hineinschreiben).
    private val log = container.eventLog

    private fun str(id: Int): String = container.appContext.getString(id)

    // Aus den Seiten-Metadaten gemerkt; Titel/Beschreibung leben editierbar im UI-State.
    private var siteName: String? = null
    private var imageUrl: String? = null
    private var articleLanguage: String? = null

    // Englische Bluesky-Link-Vorschau aus der KI-Generierung (§6); null -> Fallback auf Metadaten.
    private var blueskyCardTitle: String? = null
    private var blueskyCardDescription: String? = null

    // Stabiler Idempotency-Key für Mastodon pro Generierung (§12.1 Nr. 5).
    private var mastodonIdempotencyKey: String = UUID.randomUUID().toString()

    init {
        refreshConnections()
        savedStateHandle.get<String>(KEY_URL)?.let { restore(it) }
    }

    /** Öffentlich, damit die Hauptseite nach Rückkehr aus den Einstellungen neu laden kann. */
    fun refreshConnections() {
        val mastodon = container.credentialStore.loadMastodon()
        val bluesky = container.credentialStore.loadBluesky()
        val ai = container.credentialStore.loadAiSettings()
        uiState = uiState.copy(
            mastodonConnected = mastodon != null,
            blueskyConnected = bluesky != null,
            mastodonHandle = mastodon?.handle,
            blueskyHandle = bluesky?.handle,
            mastodonAvatarUrl = mastodon?.avatarUrl,
            blueskyAvatarUrl = bluesky?.avatarUrl,
            mastodonMaxChars = mastodon?.maxCharacters ?: 500,
            mastodonLanguage = mastodon?.effectiveLanguage ?: "de",
            blueskyLanguage = bluesky?.effectiveLanguage ?: "en",
            aiEnabled = ai.enabled,
            aiTokenMissing = ai.enabled && !ai.hasActiveToken,
        )
    }

    private fun restore(url: String) {
        val metaTitle = savedStateHandle.get<String>(KEY_META_TITLE) ?: ""
        val metaDescription = savedStateHandle.get<String>(KEY_META_DESC) ?: ""
        val mastodon = NetworkPost(
            text = savedStateHandle[KEY_M_TEXT] ?: "",
            extraHashtags = savedStateHandle.get<String>(KEY_M_TAGS)?.toTagList() ?: emptyList(),
            url = url,
        )
        val bluesky = NetworkPost(
            text = savedStateHandle[KEY_B_TEXT] ?: "",
            extraHashtags = savedStateHandle.get<String>(KEY_B_TAGS)?.toTagList() ?: emptyList(),
            url = url,
        )

        siteName = savedStateHandle[KEY_SITE]
        imageUrl = savedStateHandle[KEY_IMAGE]
        articleLanguage = savedStateHandle[KEY_LANG]
        blueskyCardTitle = savedStateHandle[KEY_CARD_TITLE]
        blueskyCardDescription = savedStateHandle[KEY_CARD_DESC]

        // Phasen mit wiederherstellen (§12.3 Nr. 4): Sonst wären die geretteten Texte im State,
        // aber unsichtbar — und der nächste Klick würde den bezahlten KI-Call wiederholen.
        val hasPosts = mastodon.text.isNotBlank() || bluesky.text.isNotBlank()
        val hasMeta = metaTitle.isNotBlank() || metaDescription.isNotBlank()

        uiState = uiState.copy(
            urlInput = url,
            fetchedUrl = if (hasMeta) url else null,
            metaTitle = metaTitle,
            metaDescription = metaDescription,
            metadataPhase = if (hasMeta) MetadataPhase.Ready else MetadataPhase.Idle,
            generationPhase = if (hasPosts) GenerationPhase.Done else GenerationPhase.Idle,
            mastodon = mastodon,
            bluesky = bluesky,
        )
    }

    // --- Einsprung ---

    /** Aus dem Share-Intent (§5.1): URL extrahieren, Metadaten laden und automatisch generieren. */
    fun onSharedText(sharedText: String, subject: String?) {
        refreshConnections()
        val url = extractUrl(sharedText)?.let(::stripTrackingParams)
        if (url == null) {
            uiState = uiState.copy(
                generationPhase = GenerationPhase.Error(str(R.string.error_no_link)),
            )
            return
        }
        // Kein Auto-Start: erst Netzwerkauswahl treffen, dann „Generieren" (Nutzer-Entscheidung).
        // Ein neuer Share ersetzt den kompletten Zustand (§12.2 Nr. 8) — sonst blieben die
        // generierten Texte des VORHERIGEN Artikels stehen und wären sendbar.
        siteName = null
        imageUrl = null
        resetCard()
        val empty = NetworkPost()
        uiState = uiState.copy(
            urlInput = url,
            fetchedUrl = null,
            postsEdited = false,
            metaTitle = subject.orEmpty(),
            metaDescription = "",
            metadataPhase = MetadataPhase.Idle,
            generationPhase = GenerationPhase.Idle,
            mastodon = empty,
            bluesky = empty,
            mastodonStatus = PostStatus.Idle,
            blueskyStatus = PostStatus.Idle,
        )
        savedStateHandle[KEY_URL] = url
        savedStateHandle[KEY_SITE] = null
        savedStateHandle[KEY_IMAGE] = null
        persistMeta()
        persistPosts(empty, empty)
    }

    fun onUrlChange(value: String) {
        uiState = uiState.copy(urlInput = value)
    }

    fun toggleMastodon() {
        // Mindestens ein aktives Ziel muss bleiben.
        if (uiState.mastodonEnabled && !uiState.activeBluesky) return
        uiState = uiState.copy(mastodonEnabled = !uiState.mastodonEnabled)
    }

    fun toggleBluesky() {
        if (uiState.blueskyEnabled && !uiState.activeMastodon) return
        uiState = uiState.copy(blueskyEnabled = !uiState.blueskyEnabled)
    }

    /** „Generieren"-Klick: Metadaten nur laden, wenn die URL neu ist (§5.1). */
    fun onGenerateClick() {
        if (!uiState.canGenerate) return
        val url = stripTrackingParams(uiState.urlInput.trim())
        uiState = uiState.copy(urlInput = url)
        savedStateHandle[KEY_URL] = url

        if (url != uiState.fetchedUrl) loadMetadataThenGenerate() else generate()
    }

    /** Setzt alles für einen neuen Artikel zurück (Abschluss-Aktion nach erfolgreichem Senden). */
    fun startNew() {
        siteName = null
        imageUrl = null
        resetCard()
        val empty = NetworkPost()
        uiState = uiState.copy(
            urlInput = "",
            fetchedUrl = null,
            postsEdited = false,
            generatingWith = null,
            metaTitle = "",
            metaDescription = "",
            metadataPhase = MetadataPhase.Idle,
            generationPhase = GenerationPhase.Idle,
            mastodon = empty,
            bluesky = empty,
            mastodonStatus = PostStatus.Idle,
            blueskyStatus = PostStatus.Idle,
        )
        listOf(
            KEY_URL, KEY_META_TITLE, KEY_META_DESC, KEY_SITE, KEY_IMAGE, KEY_LANG,
            KEY_CARD_TITLE, KEY_CARD_DESC,
            KEY_M_TEXT, KEY_M_TAGS, KEY_B_TEXT, KEY_B_TAGS,
        ).forEach { savedStateHandle[it] = null }
    }

    /** „Neu generieren": nutzt die vorhandenen (ggf. editierten) Metadaten, ohne erneuten OG-Abruf. */
    fun regenerate() {
        if (!uiState.aiEnabled || !uiState.canGenerate || uiState.isGenerating) return
        generate()
    }

    fun onMetaTitleChange(value: String) {
        uiState = uiState.copy(metaTitle = value)
        savedStateHandle[KEY_META_TITLE] = value
    }

    fun onMetaDescriptionChange(value: String) {
        uiState = uiState.copy(metaDescription = value)
        savedStateHandle[KEY_META_DESC] = value
    }

    private fun loadMetadataThenGenerate() {
        uiState = uiState.copy(metadataPhase = MetadataPhase.Loading, generationPhase = GenerationPhase.Idle)
        viewModelScope.launch {
            log.info(R.string.log_meta_loading, uiState.urlInput)
            val metadata = runCatching { container.metadataFetcher.fetch(uiState.urlInput) }.getOrNull()
            uiState = uiState.copy(fetchedUrl = uiState.urlInput)
            siteName = metadata?.siteName
            imageUrl = metadata?.imageUrl
            articleLanguage = metadata?.language
            savedStateHandle[KEY_SITE] = siteName
            savedStateHandle[KEY_IMAGE] = imageUrl
            savedStateHandle[KEY_LANG] = articleLanguage

            if (metadata != null && metadata.isUsable) {
                log.info(R.string.log_meta_ok, metadata.siteName ?: str(R.string.log_site_missing))
                uiState = uiState.copy(
                    metadataPhase = MetadataPhase.Ready,
                    metaTitle = metadata.title ?: uiState.metaTitle,
                    metaDescription = metadata.description.orEmpty(),
                )
                persistMeta()
                generate()
            } else {
                // Fallback: manuelle Felder anzeigen, NICHT automatisch generieren (§12.2 Nr. 2).
                log.error(R.string.log_meta_unusable)
                uiState = uiState.copy(metadataPhase = MetadataPhase.NeedsManual)
            }
        }
    }

    private fun generate() {
        val settings = container.credentialStore.loadAiSettings()
        // KI aus: Nutzer schreibt selbst -> leere, editierbare Bereiche anzeigen.
        if (!settings.enabled) {
            enterManualMode()
            return
        }
        if (!settings.hasActiveToken) {
            uiState = uiState.copy(
                aiTokenMissing = true,
                generationPhase = GenerationPhase.Error(str(R.string.error_no_ai_token)),
            )
            return
        }

        uiState = uiState.copy(
            generationPhase = GenerationPhase.Generating,
            generatingWith = settings.active.displayName,
        )
        viewModelScope.launch {
            uiState = try {
                val metadata = PageMetadata(
                    title = uiState.metaTitle.ifBlank { null },
                    description = uiState.metaDescription.ifBlank { null },
                    imageUrl = imageUrl,
                    siteName = siteName,
                )
                val targets = buildList {
                    if (uiState.activeMastodon) add(
                        GenTarget(
                            key = "mastodon", label = "Mastodon",
                            languageName = PostLanguages.englishName(uiState.mastodonLanguage)
                                .ifBlank { uiState.mastodonLanguage },
                            budget = PromptBuilder.mastodonTextBudget(uiState.mastodonMaxChars),
                            wantsCard = false,
                        ),
                    )
                    if (uiState.activeBluesky) add(
                        GenTarget(
                            key = "bluesky", label = "Bluesky",
                            languageName = PostLanguages.englishName(uiState.blueskyLanguage)
                                .ifBlank { uiState.blueskyLanguage },
                            budget = PromptBuilder.blueskyTextBudget(uiState.urlInput),
                            wantsCard = true,
                        ),
                    )
                }
                val medium = siteName ?: mediumNameFrom(uiState.urlInput)
                val posts = container.aiRepository.generate(settings, metadata, medium, targets)
                mastodonIdempotencyKey = UUID.randomUUID().toString()
                val mastodon = NetworkPost(posts.mastodon.text, posts.mastodon.extraHashtags, uiState.urlInput)
                val bluesky = NetworkPost(posts.bluesky.text, posts.bluesky.extraHashtags, uiState.urlInput)
                persistPosts(mastodon, bluesky)
                setCard(posts.bluesky.cardTitle, posts.bluesky.cardDescription)
                uiState.copy(
                    generationPhase = GenerationPhase.Done,
                    generatingWith = null,
                    postsEdited = false,
                    // Manuell eingegebene Metadaten haben funktioniert -> Warnhinweis auflösen.
                    metadataPhase = if (uiState.metadataPhase == MetadataPhase.NeedsManual) {
                        MetadataPhase.Ready
                    } else {
                        uiState.metadataPhase
                    },
                    mastodon = mastodon,
                    bluesky = bluesky,
                    mastodonStatus = PostStatus.Idle,
                    blueskyStatus = PostStatus.Idle,
                )
            } catch (e: ApiException) {
                log.error(R.string.log_ai_error, e.error.readable)
                uiState.copy(generationPhase = GenerationPhase.Error(e.error.readable), generatingWith = null)
            } catch (e: Exception) {
                log.error(R.string.log_ai_error, e.message.orEmpty())
                uiState.copy(
                    generationPhase = GenerationPhase.Error(e.message ?: str(R.string.error_generation_failed)),
                    generatingWith = null,
                )
            }
        }
    }

    /** KI aus: leere, editierbare Bereiche zum Selbstschreiben (Metadaten sind bereits geladen). */
    private fun enterManualMode() {
        val mastodon = NetworkPost(url = uiState.urlInput)
        val bluesky = NetworkPost(url = uiState.urlInput)
        persistPosts(mastodon, bluesky)
        resetCard()
        uiState = uiState.copy(
            generationPhase = GenerationPhase.Done,
            generatingWith = null,
            postsEdited = false,
            metadataPhase = if (uiState.metadataPhase == MetadataPhase.NeedsManual) {
                MetadataPhase.Ready
            } else {
                uiState.metadataPhase
            },
            mastodon = mastodon,
            bluesky = bluesky,
            mastodonStatus = PostStatus.Idle,
            blueskyStatus = PostStatus.Idle,
        )
    }

    // --- Editieren ---

    fun onMastodonTextChange(value: String) = updateMastodon { it.copy(text = value) }
    fun onMastodonUrlChange(value: String) = updateMastodon { it.copy(url = value) }
    fun addMastodonHashtag(tag: String) = updateMastodon { it.copy(extraHashtags = it.extraHashtags.addTag(tag)) }
    fun removeMastodonHashtag(tag: String) = updateMastodon { it.copy(extraHashtags = it.extraHashtags - tag) }

    fun onBlueskyTextChange(value: String) = updateBluesky { it.copy(text = value) }
    fun onBlueskyUrlChange(value: String) = updateBluesky { it.copy(url = value) }
    fun addBlueskyHashtag(tag: String) = updateBluesky { it.copy(extraHashtags = it.extraHashtags.addTag(tag)) }
    fun removeBlueskyHashtag(tag: String) = updateBluesky { it.copy(extraHashtags = it.extraHashtags - tag) }

    private fun List<String>.addTag(raw: String): List<String> {
        val tag = normalizeHashtag(raw)
        return if (tag.isEmpty() || tag in this) this else this + tag
    }

    private fun updateMastodon(block: (NetworkPost) -> NetworkPost) {
        val updated = block(uiState.mastodon)
        val changed = updated != uiState.mastodon
        // Geänderter Inhalt = neuer Post -> neuer Idempotency-Key. Nur der Retry DESSELBEN
        // Inhalts (z. B. nach Timeout) behält den Key, sonst käme nach einem Edit der alte
        // Post als vermeintlicher Erfolg zurück (§12.1 Nr. 5).
        if (changed) {
            mastodonIdempotencyKey = UUID.randomUUID().toString()
        }
        uiState = uiState.copy(mastodon = updated, postsEdited = uiState.postsEdited || changed)
        savedStateHandle[KEY_M_TEXT] = updated.text
        savedStateHandle[KEY_M_TAGS] = updated.extraHashtags.toTagString()
    }

    private fun updateBluesky(block: (NetworkPost) -> NetworkPost) {
        val updated = block(uiState.bluesky)
        val changed = updated != uiState.bluesky
        uiState = uiState.copy(bluesky = updated, postsEdited = uiState.postsEdited || changed)
        savedStateHandle[KEY_B_TEXT] = updated.text
        savedStateHandle[KEY_B_TAGS] = updated.extraHashtags.toTagString()
    }

    // --- Absenden (§5.5, §7) ---

    fun onSendClick() {
        viewModelScope.launch {
            // Nur Netzwerke mit generiertem Text: Ein nach der Generierung aktivierter Chip hat
            // noch keinen Inhalt und darf keinen leeren (Nur-URL-)Post absetzen.
            if (uiState.mastodonSendable && uiState.mastodonStatus !is PostStatus.Success) sendMastodon()
            if (uiState.blueskySendable && uiState.blueskyStatus !is PostStatus.Success) sendBluesky()
        }
    }

    fun retryMastodon() {
        if (uiState.mastodonStatus is PostStatus.Success) return
        viewModelScope.launch { sendMastodon() }
    }

    fun retryBluesky() {
        if (uiState.blueskyStatus is PostStatus.Success) return
        viewModelScope.launch { sendBluesky() }
    }

    private suspend fun sendMastodon() {
        val account = container.credentialStore.loadMastodon() ?: return
        if (uiState.mastodon.text.isBlank()) return
        uiState = uiState.copy(mastodonStatus = PostStatus.Pending)
        log.info(R.string.log_masto_sending)
        uiState = try {
            val post = composePost(uiState.mastodon.text, uiState.mastodon.extraHashtags, uiState.mastodon.url)
            val url = container.mastodonRepository.post(account, post, mastodonIdempotencyKey, language = account.effectiveLanguage)
            log.info(R.string.log_masto_posted)
            uiState.copy(mastodonStatus = PostStatus.Success(url))
        } catch (e: SocketTimeoutException) {
            // Idempotency-Key macht den Retry sicher (§12.1 Nr. 5).
            log.error(R.string.log_masto_timeout)
            uiState.copy(mastodonStatus = PostStatus.Failed(str(R.string.status_timeout_safe)))
        } catch (e: ApiException) {
            log.error(R.string.log_masto_error, e.error.readable)
            uiState.copy(mastodonStatus = PostStatus.Failed(e.error.readable))
        } catch (e: Exception) {
            log.error(R.string.log_masto_error, e.message.orEmpty())
            uiState.copy(mastodonStatus = PostStatus.Failed(e.message ?: str(R.string.error_post_failed)))
        }
    }

    private suspend fun sendBluesky() {
        val account = container.credentialStore.loadBluesky() ?: return
        if (uiState.bluesky.text.isBlank()) return
        uiState = uiState.copy(blueskyStatus = PostStatus.Pending)
        log.info(R.string.log_bsky_sending)
        uiState = try {
            val post = composePost(uiState.bluesky.text, uiState.bluesky.extraHashtags, uiState.bluesky.url)
            val facets = computeFacets(post, uiState.bluesky.url)
            val lang = account.effectiveLanguage
            // Link-Vorschau aus der KI (§6, in der Bluesky-Sprache); ohne KI Fallback auf die Metadaten.
            val card = uiState.bluesky.url.takeIf { it.isNotBlank() }?.let {
                LinkCard(
                    uri = it,
                    title = blueskyCardTitle?.ifBlank { null } ?: uiState.metaTitle,
                    description = withSourceLanguageNote(
                        blueskyCardDescription?.ifBlank { null } ?: uiState.metaDescription,
                        lang,
                    ),
                    imageUrl = imageUrl,
                )
            }
            val url = container.blueskyRepository.post(account, post, facets, card, langs = listOf(lang))
            log.info(R.string.log_bsky_posted, facets.size)
            uiState.copy(blueskyStatus = PostStatus.Success(url))
        } catch (e: SocketTimeoutException) {
            // Kein Idempotenz-Mechanismus: unklarer Ausgang, nicht automatisch retryen (§12.1 Nr. 5).
            log.error(R.string.log_bsky_timeout)
            uiState.copy(blueskyStatus = PostStatus.Uncertain(str(R.string.status_uncertain)))
        } catch (e: ApiException) {
            log.error(R.string.log_bsky_error, e.error.readable)
            uiState.copy(blueskyStatus = PostStatus.Failed(e.error.readable))
        } catch (e: Exception) {
            log.error(R.string.log_bsky_error, e.message.orEmpty())
            uiState.copy(blueskyStatus = PostStatus.Failed(e.message ?: str(R.string.error_post_failed)))
        }
    }

    private fun persistMeta() {
        savedStateHandle[KEY_META_TITLE] = uiState.metaTitle
        savedStateHandle[KEY_META_DESC] = uiState.metaDescription
    }

    private fun setCard(title: String?, description: String?) {
        blueskyCardTitle = title
        blueskyCardDescription = description
        savedStateHandle[KEY_CARD_TITLE] = title
        savedStateHandle[KEY_CARD_DESC] = description
    }

    private fun resetCard() = setCard(null, null)

    /**
     * Hängt an die Karten-Beschreibung einen kurzen Sprachhinweis an (z. B. „(in German)"), wenn der
     * verlinkte Artikel in einer ANDEREN Sprache ist als der Bluesky-Post — Orientierung für die
     * Leserschaft. Der Hinweis bleibt bewusst englisch (kompakte, breit verständliche Konvention).
     */
    private fun withSourceLanguageNote(description: String, postLanguage: String): String {
        val article = articleLanguage?.let { PostLanguages.primary(it) } ?: return description
        if (article == PostLanguages.primary(postLanguage)) return description
        val name = PostLanguages.englishName(article).ifBlank { return description }
        val note = "(in $name)"
        return if (description.isBlank()) note else "$description $note"
    }

    private fun persistPosts(mastodon: NetworkPost, bluesky: NetworkPost) {
        savedStateHandle[KEY_M_TEXT] = mastodon.text
        savedStateHandle[KEY_M_TAGS] = mastodon.extraHashtags.toTagString()
        savedStateHandle[KEY_B_TEXT] = bluesky.text
        savedStateHandle[KEY_B_TAGS] = bluesky.extraHashtags.toTagString()
    }

    private fun List<String>.toTagString() = joinToString(" ")
    private fun String.toTagList() = split(" ").filter { it.isNotBlank() }

    private companion object {
        const val KEY_URL = "url"
        const val KEY_META_TITLE = "meta_title"
        const val KEY_META_DESC = "meta_desc"
        const val KEY_SITE = "site_name"
        const val KEY_IMAGE = "image_url"
        const val KEY_LANG = "article_lang"
        const val KEY_CARD_TITLE = "card_title"
        const val KEY_CARD_DESC = "card_desc"
        const val KEY_M_TEXT = "m_text"
        const val KEY_M_TAGS = "m_tags"
        const val KEY_B_TEXT = "b_text"
        const val KEY_B_TAGS = "b_tags"
    }
}
