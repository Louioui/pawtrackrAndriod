package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * Durable checkout audit/idempotency record. Ports SwiftData `CheckoutTransaction`.
 *
 * [id] is ported from the iOS `uuid`. The UUID-valued reference columns
 * ([visitUUID], [petUUID], [clientUUID], [deviceID]) are plain indexed sync tags,
 * NOT relationships — the iOS model stores them as raw `UUID` values rather than
 * SwiftData relationship links, so they carry no hard `@ForeignKey`.
 *
 * `methodRaw`/`statusRaw` are enum rawValue strings kept verbatim (no Room enum
 * converter). [amount] is money — BigDecimal, never Double.
 *
 * The iOS `#Index` macro is non-unique, so [idempotencyKey] is a plain index.
 */
@Entity(
    tableName = "checkout_transactions",
    indices = [
        Index("visitUUID"),
        Index("petUUID"),
        Index("clientUUID"),
        Index("deviceID"),
        Index("idempotencyKey"),
        Index("createdAt")
    ]
)
data class CheckoutTransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val visitUUID: String = UUID.randomUUID().toString(),
    val petUUID: String = UUID.randomUUID().toString(),
    val clientUUID: String? = null,
    val deviceID: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val idempotencyKey: String = "",
    val amount: BigDecimal = BigDecimal.ZERO,
    val methodRaw: String = "cash",
    val externalReference: String? = null,
    val statusRaw: String = "processing",
    val attemptCount: Int = 0,
    val failureMessage: String? = null
)
