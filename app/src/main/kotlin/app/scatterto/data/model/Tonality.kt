package app.scatterto.data.model

/**
 * Tonalität der KI-Texte (§5.3) — **global**, nicht je Netzwerk. Der Prompt-Baustein wird in
 * [app.scatterto.data.mammouth.PromptBuilder] eingesetzt und ersetzt die Stil-Regeln.
 *
 * Wichtig: Die Stil-Regel „keine Wertung" gehört NUR zu [STANDARD]/[LOCKER] — [MARCEL] ist per
 * Definition Wertung, [HAPE] färbt. Deshalb steckt sie im jeweiligen Baustein und nicht global.
 * Ebenso ist die Satzanzahl tonalitätsabhängig: ein Hape braucht Aufbau + Pointe, ein Marcel den
 * Hack-Rhythmus — mit „genau einem Satz" funktioniert beides nicht.
 */
enum class Tonality(val key: String) {
    STANDARD("standard"),
    LOCKER("locker"),
    HAPE("hape"),
    MARCEL("marcel");

    companion object {
        val DEFAULT = STANDARD
        fun fromKey(key: String?): Tonality = entries.firstOrNull { it.key == key } ?: DEFAULT
    }

    /**
     * Der Stil-Block für den System-Prompt. Bewusst auf Deutsch (wie der ganze Prompt); die
     * genannten Formulierungen sind **Beispiele für den Ton**, nicht wörtliche Vorgaben — die
     * Post-Sprache kann je Netzwerk eine andere sein (siehe PromptBuilder).
     */
    val promptBlock: String
        get() = when (this) {
            STANDARD -> """
                - Sachlich-referierend. KEINE Wertung, kein "interessant/lesenswert/spannend",
                  keine Empfehlung, keine Meinung, nicht persönlich.
                - Keine Emojis.
                - GENAU EIN Satz.
            """.trimIndent()

            LOCKER -> """
                - Zugewandt und freundlich, mit spürbarer, aber ruhiger Begeisterung. Einladend
                  formulieren, ohne Clickbait, ohne Superlativ-Gewitter, ohne Ausrufezeichen-Ketten.
                - 1 bis 3 passende Emojis, sparsam gesetzt: nicht gestapelt, nicht am Satzanfang.
                - Ein, höchstens zwei Sätze.
            """.trimIndent()

            HAPE -> """
                - Warmherzig, selbstironisch, leicht albern, im Stil eines gutmütigen norddeutschen
                  Entertainers. Beginne betont beiläufig (deutsch etwa "Ich sag mal so ..."),
                  steigere dann ins liebevoll Übertriebene.
                - Streue einen selbstironischen Einschub in Klammern ein. Staune kindlich
                  (deutsch etwa "Ist ja irre."). Ende mit einer freundlichen Pointe.
                - Nie zynisch, nie auf Kosten anderer, nicht herablassend gegenüber dem Medium.
                - Höchstens 1 Emoji, gern augenzwinkernd.
                - Zwei bis drei kurze Sätze (Aufbau + Pointe).
            """.trimIndent()

            MARCEL -> """
                - Strenger, leidenschaftlicher Kritiker. Fälle ein klares, apodiktisches Urteil ohne
                  Relativierung. Kurze, hackende Sätze: ein Gedanke, ein Satz.
                - Nutze eine rhetorische Dreifach-Wiederholung zur Steigerung (deutsch etwa
                  "Nein, nein, nein."). Bewerte emphatisch ("grandios", "ein Meisterwerk",
                  "unerträglich"). Sieze die Leserschaft.
                - Keine Weichmacher, keine Höflichkeitsfloskeln. Keine Emojis.
                - Zwei bis vier kurze Sätze.
                - Das Urteil gilt dem Artikel und seinem Thema, nie beleidigend gegenüber Personen.
            """.trimIndent()
        }
}
