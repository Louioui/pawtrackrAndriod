package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Aggregated service-category counts per day for fast Insights.
 * Ports the SwiftData `CategoryDaySummary` @Model. No `uuid` on the source model,
 * so a string primary key is synthesized here.
 */
@Entity(
    tableName = "category_day_summaries",
    indices = [Index("day"), Index("categoryRaw")]
)
data class CategoryDaySummaryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val day: Long = System.currentTimeMillis(),
    val categoryRaw: String = "",
    val count: Int = 0
)
