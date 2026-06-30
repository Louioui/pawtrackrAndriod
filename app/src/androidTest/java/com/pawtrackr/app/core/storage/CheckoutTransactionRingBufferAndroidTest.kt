package com.pawtrackr.app.core.storage

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pawtrackr.data.local.PawtrackrDatabase
import com.example.pawtrackr.data.local.entities.CheckoutTransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class CheckoutTransactionRingBufferAndroidTest {
    private lateinit var database: PawtrackrDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PawtrackrDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun drainOnce_flushesCheckoutTransactionBatchIntoRoom() = runBlocking {
        val buffer = TransactionRingBuffer(
            capacity = 4,
            batchWriter = CheckoutTransactionBatchWriter(database),
            dispatcher = Dispatchers.IO
        )
        val transaction = checkoutTransaction(id = "txn-drain-once")

        buffer.offer(transaction)
        val drain = buffer.drainOnce()

        assertEquals(1, drain.flushedCount)
        val persisted = database.checkoutTransactionDao().getById("txn-drain-once")
        assertNotNull(persisted)
        assertEquals("checkout:visit-drain-once", persisted?.idempotencyKey)
        assertEquals(BigDecimal("42.50"), persisted?.amount)
    }

    @Test
    fun automaticDrainLoop_flushesCheckoutTransactionBatchIntoRoom() = runBlocking {
        val buffer = TransactionRingBuffer(
            capacity = 4,
            batchWriter = CheckoutTransactionBatchWriter(database),
            dispatcher = Dispatchers.IO,
            drainIntervalMillis = 20L
        )
        val transaction = checkoutTransaction(id = "txn-auto-drain")

        try {
            buffer.start()
            buffer.offer(transaction)

            val persisted = withTimeout(2_000L) {
                var found: CheckoutTransactionEntity?
                do {
                    found = database.checkoutTransactionDao().getById("txn-auto-drain")
                    if (found == null) delay(25L)
                } while (found == null)
                found
            }

            assertEquals("checkout:visit-auto-drain", persisted.idempotencyKey)
        } finally {
            buffer.stop()
        }
    }

    private fun checkoutTransaction(id: String): CheckoutTransactionEntity {
        val suffix = id.removePrefix("txn-")
        return CheckoutTransactionEntity(
            id = id,
            visitUUID = "visit-$suffix",
            petUUID = "pet-$suffix",
            clientUUID = "client-$suffix",
            deviceID = "device-test",
            idempotencyKey = "checkout:visit-$suffix",
            amount = BigDecimal("42.50"),
            methodRaw = "cash",
            statusRaw = "processing"
        )
    }
}
