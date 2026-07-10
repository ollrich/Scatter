package app.scatterto.data.mammouth

import app.scatterto.core.MASTODON_URL_WEIGHT
import app.scatterto.core.graphemeCount

/**
 * Baut System- und User-Prompt für den einen KI-Call (§5.3). Sachlich-referierender Stil ohne
 * Wertung; Rahmen-Hashtags (Medium + Thema) stehen im Satz, ergänzende separat. Dynamisches
 * Längenbudget (§12.2 Nr. 3), da die volle URL im Text steht.
 */
object PromptBuilder {

    // Reserve für ergänzende Hashtags + Trenner/Leerzeilen, damit der Gesamt-Post das Limit hält.
    private const val RESERVE = 50
    private const val MIN_BUDGET = 60

    /** Bluesky (EN): 300 Grapheme − volle URL − Reserve. */
    fun blueskyTextBudget(url: String): Int =
        (300 - graphemeCount(url) - RESERVE).coerceAtLeast(MIN_BUDGET)

    /** Mastodon (DE): Instanz-Limit − 23 (URL zählt fix) − Reserve. */
    fun mastodonTextBudget(maxCharacters: Int): Int =
        (maxCharacters - MASTODON_URL_WEIGHT - RESERVE).coerceAtLeast(MIN_BUDGET)

    val system: String = """
        Du formulierst kurze, sachliche Hinweis-Posts zu einem Artikel — je einen auf Deutsch
        (Mastodon) und Englisch (Bluesky). Antworte AUSSCHLIESSLICH mit diesem JSON, ohne weiteren Text:
        {"de":{"text":"...","extra_hashtags":["#..."]},"en":{"text":"...","extra_hashtags":["#..."]}}

        Aufbau von "text":
        - Nenne zuerst, WER worüber berichtet, mit zwei Hashtags IM Satz: (1) das Medium als Hashtag
          (Eigenname/Kürzel, z. B. #NDR, #Tagesschau), (2) das Thema in EINEM Wort als Hashtag.
          Danach GENAU EIN Satz, der den Inhalt zusammenfasst.
        - Muster (Formulierung jedes Mal leicht variieren, nicht stur wiederholen):
          DE: "Bei #NDR wurde über #klima geschrieben. <ein Satz Inhalt>."
          EN: "#NDR reported on #climate. <one sentence of content>."
        - Sachlich und referierend. KEINE Wertung, kein "interessant/lesenswert/spannend", keine
          Empfehlung, keine Meinung, nicht persönlich ("hab ich gefunden").

        Hashtags:
        - Genau zwei im Satz (Medium + Thema). "extra_hashtags": 0–2 ergänzende, thematisch treffende
          Hashtags; leere Liste ist erlaubt.
        - Kurze Relevanzprüfung: Hashtags müssen den Kern des Artikels treffen, keine beliebigen oder
          erfundenen Tags.
        - Schreibweise: Themen-Hashtags klein (#klima, #wahlrecht). Eigennamen/Kürzel in üblicher
          Schreibweise (#NDR, #EU, #Bundestag).
        - DE und EN müssen sich NICHT unterscheiden: Eigennamen bleiben meist gleich, Themen-Hashtags
          oft übersetzt (#klima / #climate). Prüfe je Sprache kurz, was passt.
        - Keine URL in den Text schreiben.
    """.trimIndent()

    fun user(
        medium: String?,
        title: String?,
        description: String?,
        deBudget: Int,
        enBudget: Int,
    ): String = buildString {
        appendLine("Medium: ${medium.orEmpty().ifBlank { "(unbekannt)" }}")
        appendLine("Artikel-Titel: ${title.orEmpty().ifBlank { "(unbekannt)" }}")
        appendLine("Artikel-Beschreibung: ${description.orEmpty().ifBlank { "(keine)" }}")
        appendLine()
        appendLine("de_text max. $deBudget Zeichen, en_text max. $enBudget Zeichen.")
    }
}
