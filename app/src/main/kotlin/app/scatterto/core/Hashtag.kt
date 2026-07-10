package app.scatterto.core

/**
 * Normalisiert einen Hashtag (§12.2 Nr. 4): genau ein führendes „#", keine Leerzeichen.
 * Die Groß-/Kleinschreibung bleibt erhalten — die Fallunterscheidung (Themen klein, Eigennamen
 * wie #NDR) trifft das KI-Modell im Prompt, nicht der Code. Leere Eingabe ergibt "".
 */
fun normalizeHashtag(raw: String): String {
    val body = raw.trim().trimStart('#').trim()
    if (body.isEmpty()) return ""
    val joined = body.split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString("")
    return if (joined.isEmpty()) "" else "#$joined"
}
