package com.pawtrackr.app.core.search

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.CoroutineContext

class AppSearchEmbeddingEngineTest {

    @Test
    fun searchEmbeddings_returnsEmptyDatasetForNoItems() = runBlocking {
        val engine = AppSearchEmbeddingEngine(dispatcher = ImmediateDispatcher)

        val outcome = engine.searchEmbeddings(
            queryEmbedding = floatArrayOf(1f, 0f),
            items = emptyList()
        )

        assertEquals(SearchEmbeddingOutcome.EmptyDataset, outcome)
    }

    @Test
    fun searchEmbeddings_scoresRawVectorsAndSortsDescending() = runBlocking {
        val engine = AppSearchEmbeddingEngine(dispatcher = ImmediateDispatcher)

        val outcome = engine.searchEmbeddings(
            queryEmbedding = floatArrayOf(1f, 0f),
            items = listOf(
                EmbeddedSearchItemReference(
                    reference = SearchItemReference(id = "sideways", title = "Nail trim"),
                    embedding = floatArrayOf(0f, 1f)
                ),
                EmbeddedSearchItemReference(
                    reference = SearchItemReference(id = "close", title = "Bath"),
                    embedding = floatArrayOf(0.9f, 0.1f)
                ),
                EmbeddedSearchItemReference(
                    reference = SearchItemReference(id = "exact", title = "Full groom"),
                    embedding = floatArrayOf(1f, 0f)
                )
            ),
            maxResults = 2,
            minScore = 0.1f
        )

        assertTrue(outcome is SearchEmbeddingOutcome.Success)
        val matches = (outcome as SearchEmbeddingOutcome.Success).matches
        assertEquals(listOf("exact", "close"), matches.map { it.reference.id })
        assertEquals(1f, matches.first().score, 0.0001f)
        assertTrue(matches[0].score > matches[1].score)
    }

    @Test
    fun searchText_usesProviderForQueryAndItems() = runBlocking {
        val engine = AppSearchEmbeddingEngine(
            embeddingProvider = MapEmbeddingProvider(
                mapOf(
                    "grooming" to floatArrayOf(1f, 0f),
                    "Bath service Warm water" to floatArrayOf(0.95f, 0.05f),
                    "Vaccination Health record" to floatArrayOf(0f, 1f)
                )
            ),
            dispatcher = ImmediateDispatcher
        )

        val outcome = engine.searchText(
            query = "grooming",
            items = listOf(
                SearchItemReference(id = "vaccine", title = "Vaccination", subtitle = "Health record"),
                SearchItemReference(id = "bath", title = "Bath service", subtitle = "Warm water")
            ),
            maxResults = 1
        )

        assertTrue(outcome is SearchEmbeddingOutcome.Success)
        assertEquals("bath", (outcome as SearchEmbeddingOutcome.Success).matches.single().reference.id)
    }

    @Test
    fun searchText_wrapsRuntimeFaultsAsFailureOutcome() = runBlocking {
        val engine = AppSearchEmbeddingEngine(
            embeddingProvider = ThrowingEmbeddingProvider,
            dispatcher = ImmediateDispatcher
        )

        val outcome = engine.searchText(
            query = "anything",
            items = listOf(SearchItemReference(id = "client-1", title = "Client"))
        )

        assertTrue(outcome is SearchEmbeddingOutcome.Failure)
        assertTrue((outcome as SearchEmbeddingOutcome.Failure).message.contains("embedding", ignoreCase = true))
    }

    @Test
    fun cosineSimilarity_handlesZeroVectorsAndDimensionMismatch() {
        assertEquals(0f, VectorMath.cosineSimilarity(floatArrayOf(), floatArrayOf(1f)), 0.0001f)
        assertEquals(0f, VectorMath.cosineSimilarity(floatArrayOf(0f, 0f), floatArrayOf(1f, 0f)), 0.0001f)
        assertEquals(0f, VectorMath.cosineSimilarity(floatArrayOf(1f), floatArrayOf(1f, 0f)), 0.0001f)
    }
}

private object ImmediateDispatcher : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }
}

private class MapEmbeddingProvider(
    private val vectors: Map<String, FloatArray>
) : TextEmbeddingProvider {
    override suspend fun embed(text: String): FloatArray =
        vectors.getValue(text)
}

private object ThrowingEmbeddingProvider : TextEmbeddingProvider {
    override suspend fun embed(text: String): FloatArray =
        error("embedding provider unavailable")
}
