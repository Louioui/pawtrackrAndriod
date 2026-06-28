package com.example.pawtrackr.data.repository

import androidx.room.withTransaction
import com.example.pawtrackr.data.local.PawtrackrDatabase
import com.example.pawtrackr.data.local.entities.CategoryDaySummaryEntity
import com.example.pawtrackr.data.local.entities.ClientInsightSummaryEntity
import com.example.pawtrackr.data.local.entities.DaySummaryEntity
import com.example.pawtrackr.data.local.entities.ServiceDaySummaryEntity
import com.example.pawtrackr.data.mapper.toDomain
import com.example.pawtrackr.domain.insights.SummaryRollup
import com.example.pawtrackr.domain.insights.SummaryRollup.ItemInput
import com.example.pawtrackr.domain.insights.SummaryRollup.VisitRollupInput
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId

/**
 * Maintains the derived summary tables (day/service/category/client-insight) that Insights
 * reads. Ports `SummaryUpdater.rebuildAllSummaries`: a deterministic full rebuild from the
 * canonical completed-visit data. Called after every checkout so Insights stays current.
 */
class SummaryRepository(
    private val db: PawtrackrDatabase,
    private val userId: String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun rebuildAll(now: Long = System.currentTimeMillis()) = withContext(ioDispatcher) {
        db.withTransaction {
            val completed = db.visitDao().getCompletedVisitsWithDetails()
            val inputs = completed.mapNotNull { vd ->
                val ended = vd.visit.endedAt ?: return@mapNotNull null
                VisitRollupInput(
                    day = startOfDay(ended),
                    total = vd.visit.total,
                    items = vd.items.map { ItemInput(it.name, it.serviceCategoryRaw) }
                )
            }
            val rollup = SummaryRollup.compute(inputs)

            db.daySummaryDao().clear()
            db.daySummaryDao().insertAll(rollup.days.map { (day, stat) ->
                DaySummaryEntity(day = day, revenue = round(stat.revenue), visitCount = stat.visitCount)
            })
            db.serviceDaySummaryDao().clear()
            db.serviceDaySummaryDao().insertAll(rollup.services.map { (k, c) ->
                ServiceDaySummaryEntity(day = k.day, serviceName = k.name, count = c)
            })
            db.categoryDaySummaryDao().clear()
            db.categoryDaySummaryDao().insertAll(rollup.categories.map { (k, c) ->
                CategoryDaySummaryEntity(day = k.day, categoryRaw = k.name, count = c)
            })

            // Per-client insights from the client graph.
            val clients = db.clientDao().getClientGraph(userId).map { it.toDomain() }
            db.clientInsightSummaryDao().clear()
            db.clientInsightSummaryDao().insertAll(clients.map { c ->
                val visitCount = c.pets.sumOf { it.completedVisitCount }
                val spent = c.pets.fold(BigDecimal.ZERO) { a, p -> a + p.lifetimeValue }
                val lastVisit = c.pets.flatMap { it.completedVisits }.mapNotNull { it.endedAt }.maxOrNull()
                ClientInsightSummaryEntity(
                    clientUUID = c.id,
                    clientName = c.fullName,
                    totalSpent = round(spent),
                    visitCount = visitCount,
                    isRecurring = visitCount > 1,
                    isChurnRisk = c.pets.any { it.isOverdue(now) },
                    lastVisitAt = lastVisit,
                    updatedAt = now
                )
            })
        }
    }

    private fun startOfDay(ms: Long): Long =
        Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun round(v: BigDecimal): BigDecimal = v.setScale(2, RoundingMode.HALF_UP)
}
