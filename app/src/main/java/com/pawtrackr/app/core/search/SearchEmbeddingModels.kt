package com.pawtrackr.app.core.search

data class SearchItemReference(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val body: String? = null,
    val entityType: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    val searchableText: String
        get() = listOfNotNull(title, subtitle, body)
            .plus(metadata.values)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
}

data class EmbeddedSearchItemReference(
    val reference: SearchItemReference,
    val embedding: FloatArray
) {
    override fun equals(other: Any?): Boolean =
        other is EmbeddedSearchItemReference &&
            reference == other.reference &&
            embedding.contentEquals(other.embedding)

    override fun hashCode(): Int =
        31 * reference.hashCode() + embedding.contentHashCode()
}

data class SearchEmbeddingMatch(
    val reference: SearchItemReference,
    val score: Float
)

sealed interface SearchEmbeddingOutcome {
    val matches: List<SearchEmbeddingMatch>

    data class Success(
        override val matches: List<SearchEmbeddingMatch>
    ) : SearchEmbeddingOutcome

    data object EmptyDataset : SearchEmbeddingOutcome {
        override val matches: List<SearchEmbeddingMatch> = emptyList()
    }

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : SearchEmbeddingOutcome {
        override val matches: List<SearchEmbeddingMatch> = emptyList()
    }
}
