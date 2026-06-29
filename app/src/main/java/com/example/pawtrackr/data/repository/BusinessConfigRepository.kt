package com.example.pawtrackr.data.repository

import com.example.pawtrackr.data.local.dao.BusinessConfigDao
import com.example.pawtrackr.data.local.entities.BusinessConfigEntity
import com.example.pawtrackr.data.mapper.toDomain
import com.example.pawtrackr.domain.model.BusinessConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Single-row business profile. Drives the first-run onboarding gate via [isSetupComplete]. */
class BusinessConfigRepository(
    private val dao: BusinessConfigDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun watchConfig(): Flow<BusinessConfig?> =
        dao.watchAll().map { it.firstOrNull()?.toDomain() }.flowOn(ioDispatcher)

    suspend fun completeSetup(name: String, email: String?, phone: String?, address: String?) =
        withContext(ioDispatcher) {
            val existing = dao.getById(PRIMARY_ID)
            dao.upsert(
                (existing ?: BusinessConfigEntity(id = PRIMARY_ID)).copy(
                    id = PRIMARY_ID,
                    name = name.trim(),
                    email = email?.trim()?.ifBlank { null },
                    phone = phone?.trim()?.ifBlank { null },
                    address = address?.trim()?.ifBlank { null },
                    isSetupComplete = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

    private companion object {
        const val PRIMARY_ID = "primary-business-config"
    }
}
