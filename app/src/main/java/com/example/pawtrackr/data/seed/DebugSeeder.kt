package com.example.pawtrackr.data.seed

import com.example.pawtrackr.data.local.PawtrackrDatabase
import com.example.pawtrackr.data.local.entities.ClientEntity
import com.example.pawtrackr.data.local.entities.PetEntity
import com.example.pawtrackr.data.local.entities.VisitEntity
import com.example.pawtrackr.domain.pet.BehaviorTags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

/**
 * Debug-only sample data so the Clients screen shows something real on first run.
 * Inserts in FK order (client → pet → visit), which also verifies the cascade graph
 * accepts a realistic insert. No-ops if the database already has clients.
 */
class DebugSeeder(
    private val db: PawtrackrDatabase,
    private val userId: String
) {
    private val day = 86_400_000L

    suspend fun seedIfEmpty(now: Long) = withContext(Dispatchers.IO) {
        if (db.clientDao().count() > 0) return@withContext

        val clientDao = db.clientDao()
        val petDao = db.petDao()
        val visitDao = db.visitDao()

        // 1) Active client — pet currently in session.
        val maria = ClientEntity(
            userId = userId, firstName = "María", lastName = "González",
            phone = "+15551234567", email = "maria@example.com", lastVisitDate = now - 2 * day
        )
        clientDao.upsertClient(maria)
        val luna = PetEntity(
            ownerId = maria.id, userId = userId, name = "Luna",
            speciesRaw = "dog", genderRaw = "female", breed = "Poodle",
            birthdate = now - 3 * 365 * day, preferredGroomingFrequencyRaw = "monthly"
        )
        petDao.upsertPet(luna)
        // one completed visit + one active (in-session) visit
        visitDao.upsertVisit(VisitEntity(petId = luna.id, userId = userId, startedAt = now - 30 * day, endedAt = now - 30 * day + 3_600_000L, total = BigDecimal("85.00")))
        visitDao.upsertVisit(VisitEntity(petId = luna.id, userId = userId, startedAt = now - 3_600_000L, endedAt = null, total = BigDecimal.ZERO))

        // 2) Client with an AGGRESSIVE pet (Spanish tag) — exercises the bilingual safety flag.
        val carlos = ClientEntity(
            userId = userId, firstName = "Carlos", lastName = "Ramírez",
            phone = "+15559876543", email = "carlos@example.com", lastVisitDate = now - 40 * day
        )
        clientDao.upsertClient(carlos)
        val rocky = PetEntity(
            ownerId = carlos.id, userId = userId, name = "Rocky",
            speciesRaw = "dog", genderRaw = "male", breed = "Rottweiler",
            birthdate = now - 5 * 365 * day,
            behaviorTagsRaw = BehaviorTags.encode(listOf("Agresivo", "Nervioso")),
            preferredGroomingFrequencyRaw = "monthly"
        )
        petDao.upsertPet(rocky)
        // last completed visit is old -> overdue / needs attention
        visitDao.upsertVisit(VisitEntity(petId = rocky.id, userId = userId, startedAt = now - 80 * day, endedAt = now - 80 * day + 3_600_000L, total = BigDecimal("120.00")))

        // 3) Client missing contact info (no email) + a cat.
        val jen = ClientEntity(
            userId = userId, firstName = "Jen", lastName = "Walker", phone = "+15552223333", email = null
        )
        clientDao.upsertClient(jen)
        val whiskers = PetEntity(
            ownerId = jen.id, userId = userId, name = "Whiskers",
            speciesRaw = "cat", genderRaw = "female", birthdate = now - 365 * day
        )
        petDao.upsertPet(whiskers)
        visitDao.upsertVisit(VisitEntity(petId = whiskers.id, userId = userId, startedAt = now - 10 * day, endedAt = now - 10 * day + 1_800_000L, total = BigDecimal("45.50")))
    }
}
