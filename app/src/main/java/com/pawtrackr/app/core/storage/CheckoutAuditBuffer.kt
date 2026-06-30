package com.pawtrackr.app.core.storage

import com.example.pawtrackr.data.local.entities.CheckoutTransactionEntity
import javax.inject.Inject

fun interface CheckoutAuditBuffer {
    fun offer(transaction: CheckoutTransactionEntity)

    companion object {
        val NoOp = CheckoutAuditBuffer { }
    }
}

class CheckoutTransactionRingBufferAuditBuffer @Inject constructor(
    private val ringBuffer: TransactionRingBuffer<CheckoutTransactionEntity>
) : CheckoutAuditBuffer {
    override fun offer(transaction: CheckoutTransactionEntity) {
        ringBuffer.offer(transaction)
    }
}
