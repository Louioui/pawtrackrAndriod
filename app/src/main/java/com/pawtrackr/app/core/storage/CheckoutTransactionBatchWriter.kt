package com.pawtrackr.app.core.storage

import androidx.room.withTransaction
import com.example.pawtrackr.data.local.PawtrackrDatabase
import com.example.pawtrackr.data.local.entities.CheckoutTransactionEntity
import javax.inject.Inject

class CheckoutTransactionBatchWriter @Inject constructor(
    private val db: PawtrackrDatabase
) : TransactionBatchWriter<CheckoutTransactionEntity> {
    override suspend fun writeBatch(payloads: List<CheckoutTransactionEntity>) {
        if (payloads.isEmpty()) return

        db.withTransaction {
            db.checkoutTransactionDao().upsertAll(payloads)
        }
    }
}
