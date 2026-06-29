package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * A line item on a [VisitEntity] — a price/name snapshot of a service at visit time.
 * Ports SwiftData `VisitItem`.
 *
 *  - `visitId` cascade: deleting a Visit deletes its items (`Visit.items` `.cascade`).
 *  - `serviceId` SET_NULL: deleting the catalog Service nulls the link but keeps the
 *    historical line (`Service.visitItems` deleteRule `.nullify`).
 */
@Entity(
    tableName = "visit_items",
    foreignKeys = [
        ForeignKey(
            entity = VisitEntity::class,
            parentColumns = ["id"],
            childColumns = ["visitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("visitId"), Index("serviceId")]
)
data class VisitItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val visitId: String? = null,
    val serviceId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastModifiedBy: String = "",
    val name: String = "",
    val serviceCategoryRaw: String? = null,
    val unitPrice: BigDecimal = BigDecimal.ZERO,
    val quantity: Int = 1,
    val note: String? = null
)
