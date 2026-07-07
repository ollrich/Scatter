package app.scatterto.core

import java.text.BreakIterator
import java.util.Locale

/**
 * Netzwerkspezifische Zeichenzählung (§12.1 Nr. 2). Die beiden Netzwerke zählen
 * grundverschieden — der Zähler muss den zusammengesetzten Gesamt-Post bewerten.
 */

/** Mastodon zählt jede URL pauschal als 23 Zeichen, unabhängig von der echten Länge. */
const val MASTODON_URL_WEIGHT = 23

private fun String.codePointLength(): Int =
    if (isEmpty()) 0 else codePointCount(0, length)

/**
 * Zählt echte Grapheme (Bluesky, §12.1 Nr. 2). Nutzt [BreakIterator] aus der JDK/Android-Standard-
 * bibliothek (getCharacterInstance folgt Unicode-Grapheme-Grenzen) — so ohne Android-Kontext testbar.
 * Ein Emoji zählt als 1, nicht als 2 UTF-16-Einheiten.
 */
fun graphemeCount(text: String): Int {
    if (text.isEmpty()) return 0
    val iterator = BreakIterator.getCharacterInstance(Locale.ROOT)
    iterator.setText(text)
    var count = 0
    var end = iterator.next()
    while (end != BreakIterator.DONE) {
        count++
        end = iterator.next()
    }
    return count
}

/**
 * Mastodon-Zeichenzahl des Gesamt-Posts: Nicht-URL-Anteile als Code-Points, jede URL fix als 23.
 */
fun mastodonLength(post: String): Int {
    var total = 0
    var lastEnd = 0
    for (match in URL_REGEX.findAll(post)) {
        total += post.substring(lastEnd, match.range.first).codePointLength()
        total += MASTODON_URL_WEIGHT
        lastEnd = match.range.last + 1
    }
    total += post.substring(lastEnd).codePointLength()
    return total
}

/** Bluesky-Länge des Gesamt-Posts: echte Grapheme inklusive voller URL (§12.4 Nr. 2). */
fun blueskyLength(post: String): Int = graphemeCount(post)
