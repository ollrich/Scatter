package app.scatterto.data.mammouth

import app.scatterto.core.MASTODON_URL_WEIGHT
import app.scatterto.core.graphemeCount

/**
 * Baut System- und User-Prompt für den einen KI-Call (§5.3). Knapp gehalten — das ist der
 * Token-Hebel. Enthält ein dynamisches Längenbudget (§12.2 Nr. 3), da die volle URL im Text steht.
 */
object PromptBuilder {

    // Reserve für Hashtag + Trenner/Leerzeilen, damit der Gesamt-Post das Limit nicht sprengt.
    private const val RESERVE = 40
    private const val MIN_BUDGET = 60

    /** Bluesky (EN): 300 Grapheme − volle URL − Reserve. */
    fun blueskyTextBudget(url: String): Int =
        (300 - graphemeCount(url) - RESERVE).coerceAtLeast(MIN_BUDGET)

    /** Mastodon (DE): Instanz-Limit − 23 (URL zählt fix) − Reserve. */
    fun mastodonTextBudget(maxCharacters: Int): Int =
        (maxCharacters - MASTODON_URL_WEIGHT - RESERVE).coerceAtLeast(MIN_BUDGET)

    val system: String = """
        Du schreibst Social-Media-Posts, die eine Person zu einem Artikel absetzt.
        Antworte AUSSCHLIESSLICH mit einem JSON-Objekt, ohne Fließtext davor oder danach:
        {"de_text": "...", "de_hashtag": "#...", "en_text": "...", "en_hashtag": "#..."}

        Regeln:
        - de_text ist Deutsch (für Mastodon), en_text ist Englisch (für Bluesky).
          KEINE Übersetzung voneinander — zwei eigenständige Texte.
        - Ton: locker, idiomatisch, wie ein persönlicher Fund ("hab das gerade gefunden").
          Kein Marketing-Sprech, kein generisches "Interessanter Artikel über …".
        - Genau ein Hashtag pro Netzwerk, aus dem Inhalt (primär Headline) abgeleitet;
          DE und EN dürfen sich unterscheiden. Ein Wort, mit führendem #.
        - Keine URL in den Text schreiben (die wird separat angehängt).
    """.trimIndent()

    fun user(
        title: String?,
        description: String?,
        deBudget: Int,
        enBudget: Int,
    ): String = buildString {
        appendLine("Artikel-Titel: ${title.orEmpty().ifBlank { "(unbekannt)" }}")
        appendLine("Artikel-Beschreibung: ${description.orEmpty().ifBlank { "(keine)" }}")
        appendLine()
        appendLine("de_text max. $deBudget Zeichen, en_text max. $enBudget Zeichen.")
    }
}
