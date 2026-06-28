package com.example.pawtrackr.data.repository

import com.example.pawtrackr.data.local.dao.PetDao
import com.example.pawtrackr.data.local.entities.PetEntity
import com.example.pawtrackr.domain.model.PetGender
import com.example.pawtrackr.domain.model.Species
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Repository for pets. Writes run on [Dispatchers.IO]; reads flow through the client graph. */
class PetRepository(
    private val petDao: PetDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun addPet(
        ownerId: String,
        userId: String?,
        name: String,
        species: Species,
        gender: PetGender,
        breed: String? = null
    ): String = withContext(ioDispatcher) {
        val entity = PetEntity(
            ownerId = ownerId,
            userId = userId,
            name = name.trim(),
            speciesRaw = species.raw,
            genderRaw = gender.raw,
            breed = breed?.trim()?.ifBlank { null }
        )
        petDao.upsertPet(entity)
        entity.id
    }

    suspend fun updatePet(
        id: String,
        name: String,
        species: Species,
        gender: PetGender,
        breed: String?
    ) = withContext(ioDispatcher) {
        val existing = petDao.getPetById(id) ?: return@withContext
        petDao.upsertPet(
            existing.copy(
                name = name.trim(),
                speciesRaw = species.raw,
                genderRaw = gender.raw,
                breed = breed?.trim()?.ifBlank { null },
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deletePet(id: String) = withContext(ioDispatcher) {
        petDao.deletePetById(id)
    }
}
