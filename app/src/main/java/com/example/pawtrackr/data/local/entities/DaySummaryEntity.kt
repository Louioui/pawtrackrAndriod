package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * Lightweight per-day aggregate to keep Insights fast at large scale.
 * Ports SwiftData `DaySummary`.
 *
 * The iOS model has no `uuid`, so a String UUID primary key ([id]) is synthesized
 * to match the locked cluster's PowerSync-compatible key convention. [day] is the
 * start-of-day (00:00) instant the aggregate was computed for, stored as epoch-millis.
 */
@Entity(
    tableName = "day_summaries",
    indices = [Index("day")]
)
data class DaySummaryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val day: Long = System.currentTimeMillis(),
    val revenue: BigDecimal = BigDecimal.ZERO,
    val visitCount: Int = 0
)
