package app.scatterto.core

/**
 * Setzt den finalen Post pro Netzwerk zusammen (§5.5):
 *
 *     <Text mit den Rahmen-Hashtags>
 *
 *     <URL>
 *
 *     <#Extra1> <#Extra2>
 *
 * Ergänzende Hashtags stehen bewusst **nach** der URL — im Feed liest sich das sauberer.
 * Reine Funktion → testbar; die Facet-Offsets (§6) passen exakt auf dieses Ergebnis.
 * Leere Bestandteile fallen weg.
 */
fun composePost(text: String, extraHashtags: List<String>, url: String): String {
    val tags = extraHashtags.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")

    return listOf(text.trim(), url.trim(), tags)
        .filter { it.isNotEmpty() }
        .joinToString("\n\n")
}
