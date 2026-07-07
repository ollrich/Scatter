package app.scatterto.core

/**
 * Normalisiert einen von der KI gelieferten Hashtag (§12.2 Nr. 4):
 * genau ein führendes „#", keine Leerzeichen (mehrere Wörter → CamelCase zusammengezogen).
 * Leere Eingabe ergibt "".
 */
fun normalizeHashtag(raw: String): String {
    val body = raw.trim().trimStart('#').trim()
    if (body.isEmpty()) return ""

    val words = body.split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when (words.size) {
        0 -> ""
        1 -> "#${words[0]}" // Einzelwort: Original-Schreibweise beibehalten
        else -> "#" + words.joinToString("") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        }
    }
}
