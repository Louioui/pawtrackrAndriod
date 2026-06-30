package com.pawtrackr.app.core.search

import java.text.Normalizer
import java.util.Locale
import javax.inject.Inject

class KeywordHashTextEmbeddingProvider @Inject constructor() : TextEmbeddingProvider {
    override suspend fun embed(text: String): FloatArray {
        val vector = FloatArray(Dimensions)
        normalizedTokens(text).forEach { token ->
            val bucket = positiveHash(token) % Dimensions
            vector[bucket] += 1f
        }
        return vector
    }

    private fun normalizedTokens(text: String): Sequence<String> {
        val folded = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(CombiningMarksRegex, "")
            .lowercase(Locale.US)

        return TokenRegex.findAll(folded).map { it.value }
    }

    private fun positiveHash(value: String): Int =
        (value.hashCode().toLong() and Int.MAX_VALUE.toLong()).toInt()

    private companion object {
        const val Dimensions = 256
        val CombiningMarksRegex = "\\p{Mn}+".toRegex()
        val TokenRegex = "[a-z0-9]+".toRegex()
    }
}
