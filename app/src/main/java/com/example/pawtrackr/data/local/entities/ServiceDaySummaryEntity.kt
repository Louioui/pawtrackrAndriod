package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Aggregated service counts per day for fast Insights.
 * Ports the SwiftData `ServiceDaySummary` @Model. No `uuid` on the source model,
 * so a string primary key is synthesized here.
 */
@Entity(
    tableName = "service_day_summaries",
    indices = [Index("day"), Index("serviceName")]
)
data class ServiceDaySummaryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val day: Long = System.currentTimeMillis(),
    val serviceName: String = "",
    val count: Int = 0
)
