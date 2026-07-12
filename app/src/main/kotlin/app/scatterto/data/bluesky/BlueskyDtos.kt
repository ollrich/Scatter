package app.scatterto.data.bluesky

import app.scatterto.core.Facet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** AT-Protocol-XRPC-Wire-Modelle (§4.2, §6). */

@Serializable
data class CreateSessionRequest(val identifier: String, val password: String)

@Serializable
data class SessionResponse(
    val accessJwt: String,
    val refreshJwt: String,
    val did: String,
    val handle: String,
)

@Serializable
data class ProfileResponse(
    val did: String,
    val handle: String,
    val avatar: String? = null,
    val followersCount: Int? = null,
    val createdAt: String? = null,
)

@Serializable
data class AuthorFeedResponse(val feed: List<FeedItem> = emptyList())

@Serializable
data class FeedItem(val post: FeedPost)

@Serializable
data class FeedPost(val indexedAt: String? = null)

/** Blob wird opak durchgereicht: uploadBlob liefert ein Lexicon-Blob-Objekt, das der Post-Record 1:1 einbettet. */
@Serializable
data class UploadBlobResponse(val blob: JsonElement)

@Serializable
data class CreateRecordRequest(
    val repo: String,
    val record: PostRecord,
    val collection: String = "app.bsky.feed.post",
)

@Serializable
data class CreateRecordResponse(val uri: String, val cid: String)

@Serializable
data class PostRecord(
    val text: String,
    val createdAt: String,
    @SerialName("\$type") val type: String = "app.bsky.feed.post",
    val langs: List<String> = listOf("en"), // §12.2 Nr. 1
    val facets: List<Facet>? = null,
    val embed: ExternalEmbed? = null,
)

@Serializable
data class ExternalEmbed(
    val external: ExternalCard,
    @SerialName("\$type") val type: String = "app.bsky.embed.external",
)

@Serializable
data class ExternalCard(
    val uri: String,
    val title: String,
    val description: String,
    val thumb: JsonElement? = null, // Blob aus uploadBlob; null = Karte ohne Bild (§6-Fallback)
)

/** Domänen-Eingabe für die Link-Karte (§6). */
data class LinkCard(
    val uri: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
)
