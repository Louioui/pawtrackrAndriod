package com.example.pawtrackr.domain.search

import com.example.pawtrackr.domain.model.Client
import com.example.pawtrackr.domain.model.Pet
import com.pawtrackr.app.core.search.AppSearchEmbeddingEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class ClientSemanticSearchTest {

    @Test
    fun rank_returnsClientWhosePetSpeciesMatchesQueryWhenNameDoesNot() = runBlocking {
        val engine = AppSearchEmbeddingEngine()
        val catClient = client(
            id = "jen",
            firstName = "Jen",
            lastName = "Walker",
            pets = listOf(pet(name = "Whiskers", speciesRaw = "cat"))
        )
        val dogClient = client(
            id = "carlos",
            firstName = "Carlos",
            lastName = "Ramírez",
            pets = listOf(pet(name = "Rocky", speciesRaw = "dog"))
        )

        val ranked = ClientSemanticSearch.rank(
            query = "cat",
            clients = listOf(dogClient, catClient),
            searchEmbeddingEngine = engine
        )

        assertEquals(listOf("jen"), ranked.map { it.id })
    }

    @Test
    fun rank_usesPetBreedAndNotesAsSearchableContext() = runBlocking {
        val engine = AppSearchEmbeddingEngine()
        val seniorClient = client(
            id = "senior",
            firstName = "Ana",
            lastName = "Pérez",
            notes = "Prefers quiet appointments",
            pets = listOf(pet(name = "Luna", breed = "Senior poodle"))
        )
        val puppyClient = client(
            id = "puppy",
            firstName = "Maya",
            lastName = "Stone",
            pets = listOf(pet(name = "Bean", breed = "Puppy terrier"))
        )

        val ranked = ClientSemanticSearch.rank(
            query = "quiet senior",
            clients = listOf(puppyClient, seniorClient),
            searchEmbeddingEngine = engine
        )

        assertEquals("senior", ranked.first().id)
    }

    @Test
    fun rank_returnsEmptyListForBlankQueryOrRuntimeFailure() = runBlocking {
        val failingEngine = AppSearchEmbeddingEngine(
            embeddingProvider = ThrowingEmbeddingProvider
        )

        assertEquals(
            emptyList<Client>(),
            ClientSemanticSearch.rank(
                query = "   ",
                clients = listOf(client(id = "one")),
                searchEmbeddingEngine = AppSearchEmbeddingEngine()
            )
        )
        assertEquals(
            emptyList<Client>(),
            ClientSemanticSearch.rank(
                query = "cat",
                clients = listOf(client(id = "one")),
                searchEmbeddingEngine = failingEngine
            )
        )
    }
}

private fun client(
    id: String,
    firstName: String = "First",
    lastName: String = "Last",
    notes: String? = null,
    pets: List<Pet> = emptyList()
) = Client(
    id = id,
    userId = "local-user",
    firstName = firstName,
    lastName = lastName,
    phone = null,
    email = null,
    address = null,
    notes = notes,
    lastVisitDate = null,
    loyaltyPoints = 0,
    createdAt = 1_700_000_000_000L,
    pets = pets
)

private fun pet(
    name: String,
    speciesRaw: String = "dog",
    breed: String? = null
) = Pet(
    id = "pet-$name",
    ownerId = "owner",
    name = name,
    speciesRaw = speciesRaw,
    genderRaw = "male",
    breed = breed,
    birthdate = null,
    behaviorTagsRaw = "[]",
    preferredGroomingFrequencyRaw = null,
    lastAttentionOutreachAt = null,
    visits = emptyList()
)

private object ThrowingEmbeddingProvider : com.pawtrackr.app.core.search.TextEmbeddingProvider {
    override suspend fun embed(text: String): FloatArray =
        error("embedding provider unavailable")
}
