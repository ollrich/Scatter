package app.scatterto.data.mammouth

import app.scatterto.core.MASTODON_URL_WEIGHT
import app.scatterto.core.graphemeCount

/**
 * Baut System- und User-Prompt für den einen KI-Call (§5.3). Sachlich-referierender Stil ohne
 * Wertung; der Satz enthält NUR den Inhalt (kein Vorspann), sämtliche Hashtags (Medium + Thema +
 * Ergänzungen) stehen in `extra_hashtags` und werden hinten angehängt. Dynamisches
 * Längenbudget (§12.2 Nr. 3).
 *
 * Sprachbewusst: Ist nur ein Netzwerk aktiv, wird auch nur dessen Sprache angefordert — das Modell
 * konzentriert sich dann auf einen Text (DE = Mastodon, EN = Bluesky).
 */
object PromptBuilder {

    // Reserve für die angehängten Hashtags + Trenner/Leerzeilen, damit der Gesamt-Post das Limit hält.
    private const val RESERVE = 50
    private const val MIN_BUDGET = 60

    fun blueskyTextBudget(url: String): Int =
        (300 - graphemeCount(url) - RESERVE).coerceAtLeast(MIN_BUDGET)

    fun mastodonTextBudget(maxCharacters: Int): Int =
        (maxCharacters - MASTODON_URL_WEIGHT - RESERVE).coerceAtLeast(MIN_BUDGET)

    fun system(wantDe: Boolean, wantEn: Boolean): String {
        val schemaParts = buildList {
            if (wantDe) add(""""de":{"text":"...","extra_hashtags":["#..."]}""")
            if (wantEn) add(""""en":{"text":"...","extra_hashtags":["#..."],"card_title":"...","card_description":"..."}""")
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
            appendLine("- GENAU EIN sachlicher Satz, der den INHALT des Artikels zusammenfasst.")
            appendLine("- KEIN einleitender Vorspann wie \"Bei #Medium wurde über #Thema geschrieben\" und")
            appendLine("  keine Quellenformel (\"berichtet über\") — beginne direkt mit dem Inhalt.")
            appendLine("- Referierend, KEINE Wertung, kein \"interessant/lesenswert/spannend\", keine")
            appendLine("  Empfehlung, keine Meinung, nicht persönlich (\"hab ich gefunden\").")
            appendLine("- KEINE Hashtags und KEINE URL im Satz.")
            if (wantDe) appendLine("  DE-Beispiel: \"Ein neues EuGH-Urteil verändert die rechtliche Einordnung von Streaming-Abos.\"")
            if (wantEn) appendLine("  EN-Beispiel: \"A new EU court ruling changes how streaming subscriptions are classified.\"")
            appendLine()
            appendLine("Hashtags — ALLE in \"extra_hashtags\" (werden hinten angehängt, NICHT in den Satz):")
            appendLine("- Reihenfolge: (1) Medium als Hashtag (Eigenname/Kürzel, z. B. #NDR, #mobiFlip),")
            appendLine("  (2) Thema in EINEM Wort (klein, z. B. #streaming), (3) 0–2 weitere treffende Hashtags.")
            appendLine("- Kurze Relevanzprüfung, nichts Erfundenes. Themen klein, Eigennamen/Kürzel wie üblich.")
            if (wantDe && wantEn) {
                appendLine("- DE und EN müssen sich NICHT unterscheiden: Eigennamen bleiben meist gleich,")
                appendLine("  Themen-Hashtags oft übersetzt (#klima / #climate). Prüfe je Sprache kurz.")
            }
            if (wantEn) {
                appendLine()
                appendLine("Englische Link-Vorschau (nur EN), für die Bluesky-Karte:")
                appendLine("- \"card_title\": knapper englischer Titel des Artikels (aus dem deutschen")
                appendLine("  Original übersetzt/adaptiert), höchstens rund 70 Zeichen, ohne Hashtags.")
                appendLine("- \"card_description\": EIN englischer Satz, der den Artikel zusammenfasst,")
                appendLine("  höchstens rund 150 Zeichen, ohne Hashtags.")
                append("- Der verlinkte Artikel ist meist auf Deutsch; Karte trotzdem auf Englisch.")
            }
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
