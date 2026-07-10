package app.scatterto.core

/**
 * Setzt den finalen Post pro Netzwerk zusammen (§5.5): Text (mit bereits eingebetteten
 * Rahmen-Hashtags) + Leerzeile + ergänzende Hashtags + URL. Reine Funktion → testbar; die
 * Facet-Offsets (§6) passen exakt auf dieses Ergebnis.
 *
 * Ergebnisform:  `<Text>\n\n<#Extra1> <#Extra2> <URL>`
 * Leere Bestandteile fallen weg (keine Extra-Tags → `<Text>\n\n<URL>`).
 */
fun composePost(text: String, extraHashtags: List<String>, url: String): String {
    val tags = extraHashtags.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")

    val tail = listOf(tags, url.trim())
        .filter { it.isNotEmpty() }
        .joinToString(" ")

    return listOf(text.trim(), tail)
        .filter { it.isNotEmpty() }
        .joinToString("\n\n")
}
