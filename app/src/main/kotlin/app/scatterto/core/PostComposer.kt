package app.scatterto.core

/**
 * Setzt den finalen Post pro Netzwerk zusammen (§5.5):
 *
 *     <Text>
 *
 *     <URL>            // nur wenn includeUrl = true
 *
 *     <#Extra1> <#Extra2>
 *
 * Hashtags stehen bewusst **nach** der URL — im Feed liest sich das sauberer.
 * Reine Funktion → testbar; die Facet-Offsets (§6) passen exakt auf dieses Ergebnis.
 * Leere Bestandteile fallen weg.
 *
 * [includeUrl] unterscheidet die Netzwerke:
 * - **Mastodon (true):** braucht die URL im Text, weil der Server die Link-Karte daraus baut.
 * - **Bluesky (false):** die Karte ist ein client-geliefertes `app.bsky.embed.external` und
 *   unabhängig vom Text. Die URL wegzulassen spart ~30–40 % des 300-Zeichen-Budgets und
 *   entspricht der dortigen Konvention.
 */
fun composePost(
    text: String,
    extraHashtags: List<String>,
    url: String,
    includeUrl: Boolean = true,
): String {
    val tags = extraHashtags.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")
    val parts = if (includeUrl) listOf(text.trim(), url.trim(), tags) else listOf(text.trim(), tags)

    return parts.filter { it.isNotEmpty() }.joinToString("\n\n")
}
