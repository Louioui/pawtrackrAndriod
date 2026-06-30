package com.example.pawtrackr.domain.search

import com.example.pawtrackr.domain.model.Client
import com.pawtrackr.app.core.search.AppSearchEmbeddingEngine
import com.pawtrackr.app.core.search.SearchEmbeddingOutcome
import com.pawtrackr.app.core.search.SearchItemReference

object ClientSemanticSearch {
    suspend fun rank(
        query: String,
        clients: List<Client>,
        searchEmbeddingEngine: AppSearchEmbeddingEngine,
        maxResults: Int = clients.size
    ): List<Client> {
        if (query.isBlank() || clients.isEmpty() || maxResults <= 0) return emptyList()

        val clientsById = clients.associateBy { it.id }
        val references = clients.map { it.toSearchReference() }

        return when (
            val outcome = searchEmbeddingEngine.searchText(
                query = query,
                items = references,
                maxResults = maxResults,
                minScore = MinimumSemanticScore
            )
        ) {
            SearchEmbeddingOutcome.EmptyDataset -> emptyList()
            is SearchEmbeddingOutcome.Failure -> emptyList()
            is SearchEmbeddingOutcome.Success -> outcome.matches.mapNotNull { match ->
                clientsById[match.reference.id]
            }
        }
    }

    private fun Client.toSearchReference(): SearchItemReference =
        SearchItemReference(
            id = id,
            title = fullName,
            subtitle = primaryContact,
            body = searchableContext(),
            entityType = "client"
        )

    private fun Client.searchableContext(): String {
        val petContext = pets.flatMap { pet ->
            listOfNotNull(
                pet.name,
                pet.species.displayName,
                pet.breed,
                pet.gender.displayName,
                pet.shortDescriptor
            ) + pet.behaviorTags
        }

        return listOfNotNull(
            firstName,
            lastName,
            phone,
            email,
            address,
            notes
        )
            .plus(petContext)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")
    }

    private const val MinimumSemanticScore = 0.1f
}
