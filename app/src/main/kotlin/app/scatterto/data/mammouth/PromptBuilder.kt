package app.scatterto.data.mammouth

import app.scatterto.core.MASTODON_URL_WEIGHT
import app.scatterto.core.graphemeCount

/**
 * Baut System- und User-Prompt für den einen KI-Call (§5.3). Sachlich-referierender Stil ohne
 * Wertung; Rahmen-Hashtags (Medium + Thema) stehen im Satz, ergänzende separat. Dynamisches
 * Längenbudget (§12.2 Nr. 3).
 *
 * Sprachbewusst: Ist nur ein Netzwerk aktiv, wird auch nur dessen Sprache angefordert — das Modell
 * konzentriert sich dann auf einen Text (DE = Mastodon, EN = Bluesky).
 */
object PromptBuilder {

    // Reserve für ergänzende Hashtags + Trenner/Leerzeilen, damit der Gesamt-Post das Limit hält.
    private const val RESERVE = 50
    private const val MIN_BUDGET = 60

    fun blueskyTextBudget(url: String): Int =
        (300 - graphemeCount(url) - RESERVE).coerceAtLeast(MIN_BUDGET)

    fun mastodonTextBudget(maxCharacters: Int): Int =
        (maxCharacters - MASTODON_URL_WEIGHT - RESERVE).coerceAtLeast(MIN_BUDGET)

    fun system(wantDe: Boolean, wantEn: Boolean): String {
        val schemaParts = buildList {
            if (wantDe) add(""""de":{"text":"...","extra_hashtags":["#..."]}""")
            if (wantEn) add(""""en":{"text":"...","extra_hashtags":["#..."]}""")
        }
        val targets = buildList {
            if (wantDe) add("Deutsch (Mastodon)")
            if (wantEn) add("Englisch (Bluesky)")
        }.joinToString(" und ")

        return buildString {
            appendLine("Du formulierst kurze, sachliche Hinweis-Posts zu einem Artikel — $targets.")
            appendLine("Antworte AUSSCHLIESSLICH mit diesem JSON, ohne weiteren Text:")
            appendLine("{${schemaParts.joinToString(",")}}")
            appendLine()
            appendLine("Aufbau von \"text\":")
            appendLine("- Nenne zuerst, WER worüber berichtet, mit zwei Hashtags IM Satz: (1) das Medium")
            appendLine("  als Hashtag (Eigenname/Kürzel, z. B. #NDR, #Tagesschau), (2) das Thema in EINEM Wort.")
            appendLine("  Danach GENAU EIN Satz, der den Inhalt zusammenfasst.")
            appendLine("- Muster (Formulierung jedes Mal leicht variieren, nicht stur wiederholen):")
            if (wantDe) appendLine("  DE: \"Bei #NDR wurde über #klima geschrieben. <ein Satz Inhalt>.\"")
            if (wantEn) appendLine("  EN: \"#NDR reported on #climate. <one sentence of content>.\"")
            appendLine("- Sachlich und referierend. KEINE Wertung, kein \"interessant/lesenswert/spannend\",")
            appendLine("  keine Empfehlung, keine Meinung, nicht persönlich (\"hab ich gefunden\").")
            appendLine()
            appendLine("Hashtags:")
            appendLine("- Genau zwei im Satz (Medium + Thema). \"extra_hashtags\": 0–2 ergänzende, thematisch")
            appendLine("  treffende Hashtags; leere Liste ist erlaubt. Kurze Relevanzprüfung, nichts Erfundenes.")
            appendLine("- Themen-Hashtags klein (#klima), Eigennamen/Kürzel wie üblich (#NDR, #EU).")
            if (wantDe && wantEn) {
                appendLine("- DE und EN müssen sich NICHT unterscheiden: Eigennamen bleiben meist gleich,")
                appendLine("  Themen-Hashtags oft übersetzt (#klima / #climate). Prüfe je Sprache kurz.")
            }
            append("- Keine URL in den Text schreiben.")
        }
    }

    fun user(
        medium: String?,
        title: String?,
        description: String?,
        deBudget: Int?,
        enBudget: Int?,
    ): String = buildString {
        appendLine("Medium: ${medium.orEmpty().ifBlank { "(unbekannt)" }}")
        appendLine("Artikel-Titel: ${title.orEmpty().ifBlank { "(unbekannt)" }}")
        appendLine("Artikel-Beschreibung: ${description.orEmpty().ifBlank { "(keine)" }}")
        appendLine()
        val budgets = buildList {
            if (deBudget != null) add("de_text max. $deBudget Zeichen")
            if (enBudget != null) add("en_text max. $enBudget Zeichen")
        }
        appendLine(budgets.joinToString(", ") + ".")
    }
}
