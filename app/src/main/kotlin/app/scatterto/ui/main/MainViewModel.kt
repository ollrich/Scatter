package app.scatterto.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.scatterto.core.computeFacets
import app.scatterto.core.composePost
import app.scatterto.core.extractUrl
import app.scatterto.core.normalizeHashtag
import app.scatterto.core.stripTrackingParams
import app.scatterto.data.AppContainer
import app.scatterto.data.bluesky.LinkCard
import app.scatterto.data.metadata.PageMetadata
import app.scatterto.data.net.readableMessage
import app.scatterto.ui.PostStatus
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.UUID

/**
 * Orchestriert die Hauptseite (§5) und das getrennte Sende-/Retry-Handling (§7).
 * Der übergebene [savedStateHandle] rettet URL und generierte Inhalte über Process-Death (§12.3 Nr. 4).
 */
class MainViewModel(
    private val container: AppContainer,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var uiState by mutableStateOf(MainUiState())
        private set

    // Für die Bluesky-Link-Karte (§6) gemerkte Metadaten.
    private var cardTitle: String = ""
    private var cardDescription: String = ""
    private var cardImageUrl: String? = null

    // Stabiler Idempotency-Key für Mastodon pro Generierung (§12.1 Nr. 5).
    private var mastodonIdempotencyKey: String = UUID.randomUUID().toString()

    init {
        refreshConnections()
        savedStateHandle.get<String>(KEY_URL)?.let { restore(it) }
    }

    private fun refreshConnections() {
        val mastodon = container.credentialStore.loadMastodon()
        val bluesky = container.credentialStore.loadBluesky()
        uiState = uiState.copy(
            mastodonConnected = mastodon != null,
            blueskyConnected = bluesky != null,
            mastodonMaxChars = mastodon?.maxCharacters ?: 500,
            mammouthMissing = container.credentialStore.loadMammouth() == null,
        )
    }

    private fun restore(url: String) {
        uiState = uiState.copy(
            urlInput = url,
            mastodon = NetworkPost(
                text = savedStateHandle[KEY_M_TEXT] ?: "",
                extraHashtags = savedStateHandle.get<String>(KEY_M_TAGS)?.toTagList() ?: emptyList(),
                url = url,
            ),
            bluesky = NetworkPost(
                text = savedStateHandle[KEY_B_TEXT] ?: "",
                extraHashtags = savedStateHandle.get<String>(KEY_B_TAGS)?.toTagList() ?: emptyList(),
                url = url,
            ),
        )
    }

    // --- Einsprung ---

    /** Aus dem Share-Intent (§5.1): URL extrahieren, Metadaten laden und automatisch generieren. */
    fun onSharedText(sharedText: String, subject: String?) {
        refreshConnections()
        val url = extractUrl(sharedText)?.let(::stripTrackingParams)
        if (url == null) {
            uiState = uiState.copy(isFromShare = true, generationPhase = GenerationPhase.Error("Kein Link im geteilten Text gefunden."))
            return
        }
        uiState = uiState.copy(urlInput = url, isFromShare = true, manualTitle = subject.orEmpty())
        savedStateHandle[KEY_URL] = url
        loadMetadataThenGenerate()
    }

    fun onUrlChange(value: String) {
        uiState = uiState.copy(urlInput = value)
    }

    /** Manueller „Generieren"-Klick (§5.1). */
    fun onGenerateClick() {
        if (!uiState.canGenerate) return
        val url = stripTrackingParams(uiState.urlInput.trim())
        uiState = uiState.copy(urlInput = url)
        savedStateHandle[KEY_URL] = url
        if (uiState.metadataPhase == MetadataPhase.NeedsManual) {
            generate(PageMetadata(uiState.manualTitle, uiState.manualDescription, cardImageUrl))
        } else {
            loadMetadataThenGenerate()
        }
    }

    fun onManualTitleChange(value: String) { uiState = uiState.copy(manualTitle = value) }
    fun onManualDescriptionChange(value: String) { uiState = uiState.copy(manualDescription = value) }

    private fun loadMetadataThenGenerate() {
        uiState = uiState.copy(metadataPhase = MetadataPhase.Loading, generationPhase = GenerationPhase.Idle)
        viewModelScope.launch {
            val metadata = runCatching { container.metadataFetcher.fetch(uiState.urlInput) }.getOrNull()
            if (metadata != null && metadata.isUsable) {
                uiState = uiState.copy(metadataPhase = MetadataPhase.Ready)
                generate(metadata)
            } else {
                // Fallback: manuelle Felder anzeigen, NICHT automatisch generieren (§12.2 Nr. 2).
                uiState = uiState.copy(metadataPhase = MetadataPhase.NeedsManual)
            }
        }
    }

    private fun generate(metadata: PageMetadata) {
        val config = container.credentialStore.loadMammouth()
        if (config == null) {
            uiState = uiState.copy(mammouthMissing = true, generationPhase = GenerationPhase.Error("Kein Mammouth-Token gespeichert."))
            return
        }
        cardTitle = metadata.title.orEmpty()
        cardDescription = metadata.description.orEmpty()
        cardImageUrl = metadata.imageUrl

        uiState = uiState.copy(generationPhase = GenerationPhase.Generating)
        viewModelScope.launch {
            uiState = try {
                val posts = container.mammouthRepository.generate(
                    config = config,
                    metadata = metadata,
                    mastodonMaxChars = uiState.mastodonMaxChars,
                    blueskyUrl = uiState.urlInput,
                )
                mastodonIdempotencyKey = UUID.randomUUID().toString()
                val mastodon = NetworkPost(posts.de.text, posts.de.extraHashtags, uiState.urlInput)
                val bluesky = NetworkPost(posts.en.text, posts.en.extraHashtags, uiState.urlInput)
                persist(mastodon, bluesky)
                uiState.copy(
                    generationPhase = GenerationPhase.Done,
                    mastodon = mastodon,
                    bluesky = bluesky,
                    mastodonStatus = PostStatus.Idle,
                    blueskyStatus = PostStatus.Idle,
                )
            } catch (e: HttpException) {
                uiState.copy(generationPhase = GenerationPhase.Error(e.readableMessage()))
            } catch (e: Exception) {
                uiState.copy(generationPhase = GenerationPhase.Error(e.message ?: "Generierung fehlgeschlagen"))
            }
        }
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
        uiState = uiState.copy(mastodon = updated)
        savedStateHandle[KEY_M_TEXT] = updated.text
        savedStateHandle[KEY_M_TAGS] = updated.extraHashtags.toTagString()
    }

    private fun updateBluesky(block: (NetworkPost) -> NetworkPost) {
        val updated = block(uiState.bluesky)
        uiState = uiState.copy(bluesky = updated)
        savedStateHandle[KEY_B_TEXT] = updated.text
        savedStateHandle[KEY_B_TAGS] = updated.extraHashtags.toTagString()
    }

    // --- Absenden (§5.5, §7) ---

    fun onSendClick() {
        viewModelScope.launch {
            if (uiState.mastodonConnected && uiState.mastodonStatus !is PostStatus.Success) sendMastodon()
            if (uiState.blueskyConnected && uiState.blueskyStatus !is PostStatus.Success) sendBluesky()
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
        uiState = uiState.copy(mastodonStatus = PostStatus.Pending)
        uiState = try {
            val post = composePost(uiState.mastodon.text, uiState.mastodon.extraHashtags, uiState.mastodon.url)
            val url = container.mastodonRepository.post(account, post, mastodonIdempotencyKey)
            uiState.copy(mastodonStatus = PostStatus.Success(url))
        } catch (e: SocketTimeoutException) {
            // Idempotency-Key macht den Retry sicher (§12.1 Nr. 5).
            uiState.copy(mastodonStatus = PostStatus.Failed("Zeitüberschreitung – erneut versuchen ist sicher"))
        } catch (e: HttpException) {
            uiState.copy(mastodonStatus = PostStatus.Failed(e.readableMessage()))
        } catch (e: Exception) {
            uiState.copy(mastodonStatus = PostStatus.Failed(e.message ?: "Fehler beim Posten"))
        }
    }

    private suspend fun sendBluesky() {
        val account = container.credentialStore.loadBluesky() ?: return
        uiState = uiState.copy(blueskyStatus = PostStatus.Pending)
        uiState = try {
            val post = composePost(uiState.bluesky.text, uiState.bluesky.extraHashtags, uiState.bluesky.url)
            val facets = computeFacets(post, uiState.bluesky.url)
            val card = uiState.bluesky.url.takeIf { it.isNotBlank() }?.let {
                LinkCard(uri = it, title = cardTitle, description = cardDescription, imageUrl = cardImageUrl)
            }
            val url = container.blueskyRepository.post(account, post, facets, card)
            uiState.copy(blueskyStatus = PostStatus.Success(url))
        } catch (e: SocketTimeoutException) {
            // Kein Idempotenz-Mechanismus: unklarer Ausgang, nicht automatisch retryen (§12.1 Nr. 5).
            uiState.copy(blueskyStatus = PostStatus.Uncertain("Unklar – bitte im Bluesky-Profil prüfen"))
        } catch (e: HttpException) {
            uiState.copy(blueskyStatus = PostStatus.Failed(e.readableMessage()))
        } catch (e: Exception) {
            uiState.copy(blueskyStatus = PostStatus.Failed(e.message ?: "Fehler beim Posten"))
        }
    }

    private fun persist(mastodon: NetworkPost, bluesky: NetworkPost) {
        savedStateHandle[KEY_M_TEXT] = mastodon.text
        savedStateHandle[KEY_M_TAGS] = mastodon.extraHashtags.toTagString()
        savedStateHandle[KEY_B_TEXT] = bluesky.text
        savedStateHandle[KEY_B_TAGS] = bluesky.extraHashtags.toTagString()
    }

    private fun List<String>.toTagString() = joinToString(" ")
    private fun String.toTagList() = split(" ").filter { it.isNotBlank() }

    private companion object {
        const val KEY_URL = "url"
        const val KEY_M_TEXT = "m_text"
        const val KEY_M_TAGS = "m_tags"
        const val KEY_B_TEXT = "b_text"
        const val KEY_B_TAGS = "b_tags"
    }
}
