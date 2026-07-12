package app.scatterto.data.mastodon

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mastodon-REST-Wire-Modelle (§4.2, §5.5). */

@Serializable
data class MastodonAccountDto(
    val username: String = "",
    val acct: String = "",
    val avatar: String? = null,
    val url: String? = null,
    @SerialName("followers_count") val followersCount: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_status_at") val lastStatusAt: String? = null,
)

@Serializable
data class InstanceDto(val configuration: ConfigurationDto? = null)

@Serializable
data class ConfigurationDto(val statuses: StatusesConfigDto? = null)

@Serializable
data class StatusesConfigDto(
    @SerialName("max_characters") val maxCharacters: Int? = null,
)

@Serializable
data class StatusRequest(
    val status: String,
    val language: String = "de", // §12.2 Nr. 1
)

@Serializable
data class StatusDto(
    val id: String,
    val url: String? = null,
)
