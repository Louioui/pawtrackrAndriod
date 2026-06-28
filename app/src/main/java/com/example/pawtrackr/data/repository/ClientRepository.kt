package com.example.pawtrackr.data.repository

import com.example.pawtrackr.data.local.dao.ClientDao
import com.example.pawtrackr.data.local.entities.ClientEntity
import com.example.pawtrackr.data.mapper.toDomain
import com.example.pawtrackr.domain.model.Client
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * MVVM-R repository for clients. Exposes domain [Client]s (with pets + visit summaries)
 * as a reactive stream and routes writes onto [Dispatchers.IO]. The UI never sees Room
 * entities — only mapped domain models.
 */
class ClientRepository(
    private val clientDao: ClientDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun watchClients(userId: String): Flow<List<Client>> =
        clientDao.watchClientGraph(userId)
            .map { graphs -> graphs.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    suspend fun addClient(
        userId: String,
        firstName: String,
        lastName: String,
        phone: String? = null,
        email: String? = null
    ): String = withContext(ioDispatcher) {
        val entity = ClientEntity(
            userId = userId,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            phone = phone?.trim()?.ifBlank { null },
            email = email?.trim()?.lowercase()?.ifBlank { null }
        )
        clientDao.upsertClient(entity)
        entity.id
    }

    suspend fun deleteClient(id: String) = withContext(ioDispatcher) {
        clientDao.deleteClientById(id)
    }
}
