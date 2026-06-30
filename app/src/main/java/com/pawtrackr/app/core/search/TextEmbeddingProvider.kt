package com.pawtrackr.app.core.search

interface TextEmbeddingProvider {
    suspend fun embed(text: String): FloatArray
}
