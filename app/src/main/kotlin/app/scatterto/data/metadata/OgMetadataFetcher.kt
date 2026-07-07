package app.scatterto.data.metadata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Lädt und parst Open-Graph-Metadaten der Artikel-URL (§5.2, Variante c).
 * Kurzes Timeout, Redirects folgen, realistischer User-Agent (§12.1 Nr. 6).
 * Es wird nur der `<head>` gebraucht — kein voller Artikeltext.
 */
class OgMetadataFetcher {

    suspend fun fetch(url: String): PageMetadata = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url)
            .userAgent(USER_AGENT)
            .timeout(FETCH_TIMEOUT_MS)
            .followRedirects(true)
            .maxBodySize(MAX_BODY_BYTES)
            .get()
        parse(doc)
    }

    companion object {
        // Realistischer Browser-UA: manche Seiten liefern Default-UAs keine OG-Tags (§12.1 Nr. 6).
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.0.0 Mobile Safari/537.36"
        private const val FETCH_TIMEOUT_MS = 10_000
        private const val MAX_BODY_BYTES = 2 * 1024 * 1024 // 2 MB genügen für den <head>

        /**
         * Reine Extraktion aus einem geparsten Dokument — ohne Netzwerk, daher unit-testbar.
         * Fallback-Reihenfolge: og:title -> <title>; og:description -> meta[name=description].
         */
        fun parse(doc: Document): PageMetadata {
            val title = doc.ogProperty("og:title") ?: doc.title().ifBlank { null }
            val description = doc.ogProperty("og:description")
                ?: doc.selectFirst("meta[name=description]")?.attr("content")?.ifBlank { null }
            val image = doc.selectFirst("meta[property=og:image]")?.absUrl("content")?.ifBlank { null }
            return PageMetadata(title = title?.trim(), description = description?.trim(), imageUrl = image)
        }

        private fun Document.ogProperty(property: String): String? =
            selectFirst("meta[property=$property]")?.attr("content")?.ifBlank { null }
    }
}
