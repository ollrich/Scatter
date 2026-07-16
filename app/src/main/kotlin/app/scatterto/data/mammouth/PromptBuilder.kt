package app.scatterto.data.mammouth

import app.scatterto.core.MASTODON_URL_WEIGHT
import app.scatterto.data.model.Tonality

/**
 * Ein Ziel-Netzwerk der Generierung: sein Schema-Key, Anzeigename, die Post-Sprache (englischer
 * Name für die KI-Anweisung), das Zeichenbudget und ob eine Link-Karte gewünscht ist.
 */
data class GenTarget(
    val key: String,          // "mastodon" | "bluesky" — zugleich JSON-Schlüssel der Antwort
    val label: String,        // "Mastodon" | "Bluesky"
    val languageName: String, // englischer Sprachname, z. B. "German", "Danish"
    val budget: Int,          // Zeichenbudget für den Satz
    val wantsCard: Boolean,   // Link-Vorschau (nur Bluesky)
)

/**
 * Baut System- und User-Prompt für den einen KI-Call (§5.3). Der Satz enthält NUR den Inhalt
 * (kein Vorspann), sämtliche Hashtags stehen in `extra_hashtags` und werden hinten angehängt.
 * Sprache und Budget je Netzwerk konfigurierbar.
 *
 * Sprachbewusst: Es werden nur die aktiven Netzwerke angefordert, jeweils in ihrer Post-Sprache.
 * Der Stil kommt aus der global gewählten [Tonality] und gilt für alle Netzwerke gleich.
 */
object PromptBuilder {

    // Reserve für die angehängten Hashtags + Trenner/Leerzeilen, damit der Gesamt-Post das Limit hält.
    private const val RESERVE = 50
    private const val MIN_BUDGET = 60

    /**
     * Bluesky zählt Grapheme, Limit 300. Die URL wird **nicht** abgezogen: sie steht nicht im Text,
     * sondern nur in der Link-Karte (`app.bsky.embed.external`) — das gibt ~30–40 % mehr Platz.
     */
    fun blueskyTextBudget(): Int = 300 - RESERVE

    fun mastodonTextBudget(maxCharacters: Int): Int =
        (maxCharacters - MASTODON_URL_WEIGHT - RESERVE).coerceAtLeast(MIN_BUDGET)

    fun system(targets: List<GenTarget>, tonality: Tonality = Tonality.DEFAULT): String {
        val schemaParts = targets.map { t ->
            val base = """"${t.key}":{"text":"...","extra_hashtags":["#..."]"""
            if (t.wantsCard) "$base,\"card_title\":\"...\",\"card_description\":\"...\"}" else "$base}"
        }
        val targetsDesc = targets.joinToString(" und ") { "${it.label} (${it.languageName})" }

        return buildString {
            appendLine("Du formulierst kurze Hinweis-Posts zu einem Artikel — $targetsDesc.")
            appendLine("Antworte AUSSCHLIESSLICH mit diesem JSON, ohne weiteren Text:")
            appendLine("{${schemaParts.joinToString(",")}}")
            appendLine()
            appendLine("Sprache je Netzwerk (Text, Hashtags und ggf. Karte in DIESER Sprache):")
            targets.forEach { appendLine("- ${it.key}: ${it.languageName}") }
            appendLine()
            appendLine("Aufbau von \"text\":")
            appendLine("- Gib den INHALT des Artikels wieder.")
            appendLine("- KEIN einleitender Vorspann wie \"Bei #Medium wurde über #Thema geschrieben\" und")
            appendLine("  keine Quellenformel — beginne direkt mit dem Inhalt.")
            appendLine("- KEINE Hashtags und KEINE URL im Satz.")
            appendLine()
            appendLine("Tonfall (gilt für alle Netzwerke):")
            appendLine(tonality.promptBlock)
            appendLine()
            appendLine("Hashtags — ALLE in \"extra_hashtags\" (werden hinten angehängt, NICHT in den Satz):")
            appendLine("- Reihenfolge: (1) Medium als Hashtag (Eigenname/Kürzel, z. B. #NDR, #mobiFlip),")
            appendLine("  (2) Thema in EINEM Wort (klein, z. B. #streaming), (3) 0–2 weitere treffende Hashtags.")
            appendLine("- Themen klein, Eigennamen/Kürzel wie üblich. Hashtags in der jeweiligen Netzwerk-Sprache")
            appendLine("  (Eigennamen bleiben meist gleich, Themen-Hashtags werden übersetzt).")
            if (targets.any { it.wantsCard }) {
                appendLine()
                appendLine("Link-Vorschau (nur wo im Schema \"card_...\" steht):")
                appendLine("- Die Karte gehört zum ARTIKEL, nicht zum Post: immer sachlich-neutral,")
                appendLine("  ohne Emojis, unabhängig vom Tonfall oben.")
                appendLine("- \"card_title\": knapper Titel des Artikels in der Netzwerk-Sprache,")
                appendLine("  höchstens rund 70 Zeichen, ohne Hashtags.")
                appendLine("- \"card_description\": EIN Satz in der Netzwerk-Sprache, der den Artikel")
                appendLine("  zusammenfasst, höchstens rund 150 Zeichen, ohne Hashtags.")
                append("- Der verlinkte Artikel kann in einer anderen Sprache sein; Karte trotzdem in der Netzwerk-Sprache.")
            }
        }
    }

    fun user(
        medium: String?,
        title: String?,
        description: String?,
        targets: List<GenTarget>,
    ): String = buildString {
        appendLine("Medium: ${medium.orEmpty().ifBlank { "(unbekannt)" }}")
        appendLine("Artikel-Titel: ${title.orEmpty().ifBlank { "(unbekannt)" }}")
        appendLine("Artikel-Beschreibung: ${description.orEmpty().ifBlank { "(keine)" }}")
        appendLine()
        appendLine(targets.joinToString(", ") { "${it.key}_text max. ${it.budget} Zeichen" } + ".")
    }
}
