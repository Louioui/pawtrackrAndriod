package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * A single inventory ledger entry (usage or restock). Ports SwiftData `InventoryTransaction`.
 *
 * `id` is ported from the iOS `uuid`. `itemId` mirrors the iOS cascade: deleting an
 * [InventoryItemEntity] deletes its transactions (SwiftData `InventoryItem.transactions`
 * deleteRule `.cascade`). `quantityChange` is negative for usage, positive for restock,
 * stored as [BigDecimal] (never Double/Float).
 */
@Entity(
    tableName = "inventory_transactions",
    foreignKeys = [
        ForeignKey(
            entity = InventoryItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("itemId")]
)
data class InventoryTransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val itemId: String? = null,
    val date: Long = System.currentTimeMillis(),
    val quantityChange: BigDecimal = BigDecimal.ZERO,
    val note: String? = ""
)
