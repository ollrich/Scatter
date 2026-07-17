package app.scatterto.data.mammouth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** OpenAI-kompatible Wire-Modelle für die Mammouth-API (§4.1, §5.3). */

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ResponseFormat(val type: String) // "json_object" (§5.3)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
    // Nullable, damit der Fallback temperature komplett weglassen kann (manche neuen Modelle lehnen sie ab).
    val temperature: Double? = 0.7,
)

@Serializable
data class ChatResponse(val choices: List<Choice> = emptyList()) {
    fun firstContent(): String? = choices.firstOrNull()?.message?.content
}

@Serializable
data class Choice(val message: ChatMessage)

@Serializable
data class ModelsResponse(val data: List<ModelData> = emptyList())

@Serializable
data class ModelData(val id: String)

// --- LiteLLM-Verwaltung (nur Mammouth): Guthaben ---
// Bewusst nur die Felder, die wir brauchen — die echte Antwort hat gut 40, und
// `ignoreUnknownKeys` lässt den Rest fallen.

@Serializable
data class KeyInfoResponse(val info: KeyInfo = KeyInfo())

@Serializable
data class KeyInfo(@SerialName("user_id") val userId: String? = null)

@Serializable
data class UserInfoResponse(@SerialName("user_info") val userInfo: UserInfo = UserInfo())

/**
 * `max_budget` ist das Limit der laufenden Periode (bei Mammouth Abo-Kontingent plus zugekaufte
 * Credits), `spend` der über ALLE Keys des Accounts aufsummierte Verbrauch — deshalb ist das hier
 * und nicht `/key/info` die richtige Quelle.
 */
@Serializable
data class UserInfo(
    @SerialName("max_budget") val maxBudget: Double? = null,
    val spend: Double = 0.0,
    @SerialName("budget_reset_at") val budgetResetAt: String? = null,
)

/**
 * Rohschema der KI-Antwort (§5.3): je Netzwerk ein Post-Text plus ergänzende Hashtags (werden
 * hinten angehängt). Für Bluesky zusätzlich Titel/Beschreibung der Link-Vorschau (§6),
 * in der dort konfigurierten Post-Sprache.
 */
@Serializable
data class AiLangResult(
    val text: String = "",
    @SerialName("extra_hashtags") val extraHashtags: List<String> = emptyList(),
    @SerialName("card_title") val cardTitle: String = "",
    @SerialName("card_description") val cardDescription: String = "",
)

@Serializable
data class AiResult(
    val mastodon: AiLangResult = AiLangResult(),
    val bluesky: AiLangResult = AiLangResult(),
)
