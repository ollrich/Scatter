package app.scatterto.core

/**
 * Setzt den finalen Post pro Netzwerk zusammen (§5.5): Text + Leerzeile + „Hashtag URL".
 * Reine Funktion, damit die Zusammensetzung testbar ist und Facet-Offsets (§6) exakt darauf passen.
 *
 * Ergebnisform:  `<Text>\n\n<#Hashtag> <URL>`
 * Leere Bestandteile fallen weg (z. B. fehlender Hashtag → `<Text>\n\n<URL>`).
 */
fun composePost(text: String, hashtag: String, url: String): String {
    val tail = listOf(hashtag.trim(), url.trim())
        .filter { it.isNotEmpty() }
        .joinToString(" ")

    return listOf(text.trim(), tail)
        .filter { it.isNotEmpty() }
        .joinToString("\n\n")
}
