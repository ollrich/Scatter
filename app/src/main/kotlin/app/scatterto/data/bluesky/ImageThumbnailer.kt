package app.scatterto.data.bluesky

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream

/**
 * Lädt das og:image herunter und bereitet es als JPEG für uploadBlob auf (§6):
 * herunterskalieren und komprimieren, bis das Blob-Größenlimit (~1 MB) eingehalten wird.
 * Schlägt irgendetwas fehl, liefert die Funktion null — die Karte wird dann ohne Bild gesendet.
 */
class ImageThumbnailer(private val client: OkHttpClient) {

    suspend fun downloadAsJpeg(imageUrl: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = fetch(imageUrl) ?: return@runCatching null
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return@runCatching null
            val scaled = scaleDown(bitmap, MAX_DIMENSION)
            compressUnderLimit(scaled)
        }.getOrNull()
    }

    private fun fetch(url: String): ByteArray? {
        val response = client.newCall(Request.Builder().url(url).build()).execute()
        response.use {
            if (!it.isSuccessful) return null
            val body = it.body
            // Download begrenzen: riesige og:image-Dateien würden sonst komplett in den Speicher
            // geladen (OOM-Risiko). Über dem Limit -> Karte ohne Bild (Fallback existiert).
            if (body.contentLength() > MAX_DOWNLOAD_BYTES) return null
            val source = body.source()
            if (source.request(MAX_DOWNLOAD_BYTES + 1)) return null // mehr Daten als erlaubt
            return source.readByteArray()
        }
    }

    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)
        if (largest <= maxDimension) return bitmap
        val ratio = maxDimension.toFloat() / largest
        val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return bitmap.scale(width, height)
    }

    private fun compressUnderLimit(bitmap: Bitmap): ByteArray {
        var quality = 90
        var data: ByteArray
        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            data = stream.toByteArray()
            quality -= 15
        } while (data.size > MAX_BLOB_BYTES && quality >= 30)
        return data
    }

    private companion object {
        const val MAX_DIMENSION = 1000
        const val MAX_BLOB_BYTES = 1_000_000 // Bluesky-Blob-Limit ≈ 1 MB (§6)
        const val MAX_DOWNLOAD_BYTES = 15L * 1024 * 1024 // Quellbild-Limit gegen OOM
    }
}
