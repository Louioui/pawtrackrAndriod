package com.pawtrackr.app.core.search

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppSearchEmbeddingEngine @Inject constructor(
    private val embeddingProvider: TextEmbeddingProvider = KeywordHashTextEmbeddingProvider(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend fun searchText(
        query: String,
        items: List<SearchItemReference>,
        maxResults: Int = DefaultMaxResults,
        minScore: Float = DefaultMinScore
    ): SearchEmbeddingOutcome {
        if (items.isEmpty()) return SearchEmbeddingOutcome.EmptyDataset

        return guardedSearch {
            val queryEmbedding = embeddingProvider.embed(query)
            val embeddedItems = items.map { item ->
                EmbeddedSearchItemReference(
                    reference = item,
                    embedding = embeddingProvider.embed(item.searchableText)
                )
            }
            rankMatches(queryEmbedding, embeddedItems, maxResults, minScore)
        }
    }

    suspend fun searchEmbeddings(
        queryEmbedding: FloatArray,
        items: List<EmbeddedSearchItemReference>,
        maxResults: Int = DefaultMaxResults,
        minScore: Float = DefaultMinScore
    ): SearchEmbeddingOutcome {
        if (items.isEmpty()) return SearchEmbeddingOutcome.EmptyDataset

        return guardedSearch {
            rankMatches(queryEmbedding, items, maxResults, minScore)
        }
    }

    private suspend fun guardedSearch(
        block: suspend () -> SearchEmbeddingOutcome
    ): SearchEmbeddingOutcome =
        try {
            withContext(dispatcher) { block() }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            SearchEmbeddingOutcome.Failure(
                message = "Search embedding execution failed: ${throwable.message ?: throwable::class.java.simpleName}",
                cause = throwable
            )
        }

    private fun rankMatches(
        queryEmbedding: FloatArray,
        items: List<EmbeddedSearchItemReference>,
        maxResults: Int,
        minScore: Float
    ): SearchEmbeddingOutcome.Success {
        val resultLimit = maxResults.coerceAtLeast(0)
        if (resultLimit == 0) return SearchEmbeddingOutcome.Success(emptyList())

        val matches = items.asSequence()
            .map { item ->
                SearchEmbeddingMatch(
                    reference = item.reference,
                    score = VectorMath.cosineSimilarity(queryEmbedding, item.embedding)
                )
            }
            .filter { match -> match.score.isFiniteScore() && match.score >= minScore }
            .sortedWith(
                compareByDescending<SearchEmbeddingMatch> { it.score }
                    .thenBy { it.reference.title.lowercase() }
                    .thenBy { it.reference.id }
            )
            .take(resultLimit)
            .toList()

        return SearchEmbeddingOutcome.Success(matches)
    }

    private fun Float.isFiniteScore(): Boolean =
        !isNaN() && !isInfinite()

    private companion object {
        const val DefaultMaxResults = 10
        const val DefaultMinScore = 0f
    }
}
