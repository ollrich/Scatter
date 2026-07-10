package app.scatterto.core

/**
 * URL-Extraktion aus geteiltem Text (§12.1 Nr. 3): Share-Intents liefern oft
 * „Titel + URL" oder Fließtext mit eingebetteter URL, nicht nur die nackte URL.
 */

/** Findet die erste http(s)-URL in einem String; matcht bis zum ersten Whitespace/Klammer-Ende. */
val URL_REGEX = Regex("""https?://[^\s<>"']+""")

/** Extrahiert den Host einer URL ohne „www." — als Fallback-Hinweis für den Medien-Hashtag. */
fun domainOf(url: String): String? {
    val host = Regex("""https?://([^/\s?#]+)""").find(url)?.groupValues?.get(1) ?: return null
    return host.removePrefix("www.").ifBlank { null }
}

/** Am Ende häufige Satzzeichen, die nicht zur URL gehören (z. B. „…/artikel." am Satzende). */
private const val TRAILING_PUNCTUATION = ".,;:!?)]}\"'»«"

/**
 * Extrahiert die erste URL aus [sharedText] oder `null`, wenn keine enthalten ist
 * (dann Hinweis anzeigen, URL-Feld leer lassen — §12.1 Nr. 3).
 */
fun extractUrl(sharedText: String): String? {
    val match = URL_REGEX.find(sharedText) ?: return null
    return match.value.trimEnd { it in TRAILING_PUNCTUATION }
}

// Bekannte Tracking-Parameter (§12.3 Nr. 1) — Präfix utm_* plus einzelne Netzwerk-Tracker.
private val TRACKING_EXACT = setOf(
    "fbclid", "gclid", "gclsrc", "dclid", "igshid", "mc_eid", "mc_cid",
    "yclid", "twclid", "wt_mc", "_hsenc", "_hsmi", "vero_id",
)

private fun isTrackingParam(key: String): Boolean =
    key.startsWith("utm_") || key.lowercase() in TRACKING_EXACT

/**
 * Entfernt bekannte Tracking-Parameter aus der URL, erhält Pfad und Fragment (§12.3 Nr. 1).
 * Rein string-basiert, damit ohne Android/Netzwerk testbar. Unbekannte Parameter bleiben erhalten.
 */
fun stripTrackingParams(url: String): String {
    val queryStart = url.indexOf('?')
    if (queryStart < 0) return url

    val base = url.substring(0, queryStart)
    val fragmentStart = url.indexOf('#', queryStart)
    val query = if (fragmentStart >= 0) url.substring(queryStart + 1, fragmentStart) else url.substring(queryStart + 1)
    val fragment = if (fragmentStart >= 0) url.substring(fragmentStart) else ""

    val kept = query.split("&")
        .filter { it.isNotEmpty() }
        .filterNot { isTrackingParam(it.substringBefore("=")) }

    val newQuery = if (kept.isEmpty()) "" else "?" + kept.joinToString("&")
    return base + newQuery + fragment
}
