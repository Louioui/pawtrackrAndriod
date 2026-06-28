package com.example.pawtrackr.data.repository

import com.example.pawtrackr.data.local.dao.VisitDao
import com.example.pawtrackr.data.local.entities.VisitEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Visit lifecycle. Ports the check-in half of iOS `VisitRepository`; the full checkout
 * (services + payment + close) is the idempotent [CheckoutRepository].
 */
class VisitRepository(
    private val visitDao: VisitDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /** Start a visit for a pet, or return the existing open one (idempotent check-in). */
    suspend fun checkIn(petId: String, userId: String?): String = withContext(ioDispatcher) {
        visitDao.getActiveVisitForPet(petId)?.let { return@withContext it.id }
        val visit = VisitEntity(petId = petId, userId = userId)
        visitDao.upsertVisit(visit)
        visit.id
    }

    suspend fun getActiveVisit(petId: String): VisitEntity? = withContext(ioDispatcher) {
        visitDao.getActiveVisitForPet(petId)
    }
}
