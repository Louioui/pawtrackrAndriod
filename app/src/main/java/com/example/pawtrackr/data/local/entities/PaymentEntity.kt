package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * Payment for a [VisitEntity] (1:1). Ports SwiftData `Payment`.
 *
 * NOTE: the iOS `Payment` model has NO uuid — SwiftData identifies it by
 * persistentModelID. Room requires an explicit primary key, so [id] is synthesized.
 *
 * `visitId` cascade mirrors `Visit.payment` deleteRule `.cascade`; the unique index
 * enforces the one-payment-per-visit relationship.
 */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = VisitEntity::class,
            parentColumns = ["id"],
            childColumns = ["visitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["visitId"], unique = true)]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val visitId: String? = null,
    val amount: BigDecimal = BigDecimal.ZERO,
    val methodRaw: String = "cash",
    val paidAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val note: String? = null,
    val externalReference: String? = null,
    val lastModifiedBy: String = "",
    val lastModifiedAt: Long = System.currentTimeMillis()
)
