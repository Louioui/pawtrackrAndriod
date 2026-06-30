package com.example.pawtrackr.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pawtrackr.data.local.PawtrackrDatabase
import com.example.pawtrackr.data.local.entities.CheckoutTransactionEntity
import com.example.pawtrackr.data.local.entities.ClientEntity
import com.example.pawtrackr.data.local.entities.PetEntity
import com.example.pawtrackr.data.local.entities.ServiceEntity
import com.example.pawtrackr.data.local.entities.VisitEntity
import com.example.pawtrackr.domain.checkout.CheckoutRequest
import com.example.pawtrackr.domain.model.PaymentMethod
import com.pawtrackr.app.core.storage.CheckoutAuditBuffer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class CheckoutRepositoryBufferedAuditAndroidTest {
    private lateinit var database: PawtrackrDatabase
    private lateinit var auditBuffer: RecordingCheckoutAuditBuffer

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PawtrackrDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        auditBuffer = RecordingCheckoutAuditBuffer()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun processCheckout_offersSucceededTransactionToAuditBuffer() = runBlocking {
        seedCheckoutGraph()
        val repository = CheckoutRepository(
            db = database,
            summaryRepository = SummaryRepository(database, UserId),
            checkoutAuditBuffer = auditBuffer
        )

        repository.processCheckout(
            CheckoutRequest(
                visitId = VisitId,
                petId = PetId,
                clientId = ClientId,
                userId = UserId,
                selectedServiceIds = listOf(ServiceId),
                method = PaymentMethod.CASH
            )
        )

        assertEquals(1, auditBuffer.offered.size)
        val transaction = auditBuffer.offered.single()
        assertEquals("checkout:$VisitId", transaction.idempotencyKey)
        assertEquals(VisitId, transaction.visitUUID)
        assertEquals(PetId, transaction.petUUID)
        assertEquals(ClientId, transaction.clientUUID)
        assertEquals("succeeded", transaction.statusRaw)
        assertEquals(BigDecimal("30.00"), transaction.amount)
    }

    @Test
    fun processCheckout_doesNotOfferReplayToAuditBuffer() = runBlocking {
        seedCheckoutGraph()
        val repository = CheckoutRepository(
            db = database,
            summaryRepository = SummaryRepository(database, UserId),
            checkoutAuditBuffer = auditBuffer
        )
        val request = CheckoutRequest(
            visitId = VisitId,
            petId = PetId,
            clientId = ClientId,
            userId = UserId,
            selectedServiceIds = listOf(ServiceId),
            method = PaymentMethod.CASH
        )

        repository.processCheckout(request)
        auditBuffer.offered.clear()
        val replay = repository.processCheckout(request)

        assertTrue(replay.wasAlreadyComplete)
        assertEquals(emptyList<CheckoutTransactionEntity>(), auditBuffer.offered)
    }

    private suspend fun seedCheckoutGraph() {
        database.clientDao().upsertClient(
            ClientEntity(
                id = ClientId,
                userId = UserId,
                firstName = "Jen",
                lastName = "Walker"
            )
        )
        database.petDao().upsertPet(
            PetEntity(
                id = PetId,
                ownerId = ClientId,
                userId = UserId,
                name = "Whiskers"
            )
        )
        database.serviceDao().upsertService(
            ServiceEntity(
                id = ServiceId,
                name = "Bath",
                categoryRaw = "Grooming",
                basePrice = BigDecimal("30.00")
            )
        )
        database.visitDao().upsertVisit(
            VisitEntity(
                id = VisitId,
                petId = PetId,
                userId = UserId
            )
        )
    }

    private companion object {
        const val UserId = "local-user"
        const val ClientId = "client-1"
        const val PetId = "pet-1"
        const val VisitId = "visit-1"
        const val ServiceId = "service-1"
    }
}

private class RecordingCheckoutAuditBuffer : CheckoutAuditBuffer {
    val offered = mutableListOf<CheckoutTransactionEntity>()

    override fun offer(transaction: CheckoutTransactionEntity) {
        offered += transaction
    }
}
