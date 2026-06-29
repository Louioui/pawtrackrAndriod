package com.example.pawtrackr.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Loads an image from a content [Uri] and produces downsized JPEG bytes — a full-size
 * version (for the visit record) and a small thumbnail (for list display). Mirrors the
 * intent of iOS `CloudMediaPolicy.optimizedFullImageData`/`optimizedThumbnailData`:
 * never store raw camera blobs. All decode/compress work runs off the main thread.
 */
object ImageUtils {

    data class PhotoBytes(val full: ByteArray, val thumb: ByteArray)

    suspend fun loadDownsized(
        context: Context,
        uri: Uri,
        fullMaxPx: Int = 1280,
        thumbMaxPx: Int = 240
    ): PhotoBytes? = withContext(Dispatchers.IO) {
        try {
            val source = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: return@withContext null

            val full = scaleDown(source, fullMaxPx)
            val thumb = scaleDown(source, thumbMaxPx)
            PhotoBytes(full = toJpeg(full, 85), thumb = toJpeg(thumb, 80))
        } catch (_: Exception) {
            null
        }
    }

    private fun scaleDown(src: Bitmap, maxPx: Int): Bitmap {
        val w = src.width
        val h = src.height
        val longest = maxOf(w, h)
        if (longest <= maxPx) return src
        val scale = maxPx.toFloat() / longest
        return Bitmap.createScaledBitmap(src, (w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1), true)
    }

    private fun toJpeg(bmp: Bitmap, quality: Int): ByteArray =
        ByteArrayOutputStream().use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }
}
