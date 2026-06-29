package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * A stockable inventory item (e.g. shampoo, bottles, sets). Ports SwiftData `InventoryItem`.
 *
 * `id` is ported from the iOS `uuid`. Stock, reorder level, and cost are money/quantity
 * decimals, so they use [BigDecimal] (never Double/Float).
 *
 * The iOS `transactions` to-many relationship is not a column — the ledger is read via a
 * `@Relation` from [InventoryTransactionEntity] (which carries the `itemId` FK).
 */
@Entity(
    tableName = "inventory_items",
    indices = [Index("name"), Index("category")]
)
data class InventoryItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = "",
    val currentStock: BigDecimal = BigDecimal.ZERO,
    val unit: String = "",
    val reorderLevel: BigDecimal = BigDecimal(5),
    val costPerUnit: BigDecimal = BigDecimal.ZERO
)
