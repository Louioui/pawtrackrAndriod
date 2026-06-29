package com.example.pawtrackr.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.UUID

/**
 * Derived per-client metrics cached to keep Insights fast on large stores.
 * Ports SwiftData `ClientInsightSummary`.
 *
 * NOTE: the iOS model has NO `uuid` — SwiftData identifies it by persistentModelID.
 * Room requires an explicit primary key, so [id] is synthesized.
 *
 * [clientUUID] is the UUID of the source client. It is a plain reference (the iOS
 * `clientUUID` is a stored value, not a SwiftData relationship), so it is an indexed
 * String column with NO foreign key — the summary is a denormalized cache that may
 * outlive or precede its client row. Money is BigDecimal, never Double.
 */
@Entity(
    tableName = "client_insight_summaries",
    indices = [Index("clientUUID")]
)
data class ClientInsightSummaryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val clientUUID: String = UUID.randomUUID().toString(),
    val updatedAt: Long = System.currentTimeMillis(),
    val clientName: String = "",
    val totalSpent: BigDecimal = BigDecimal.ZERO,
    val visitCount: Int = 0,
    val isRecurring: Boolean = false,
    val isChurnRisk: Boolean = false,
    val lastVisitAt: Long? = null
)
