package com.example.pawtrackr.domain.insights

import java.math.BigDecimal

/**
 * Pure rollup of completed visits into per-day / per-service / per-category aggregates.
 * Ported from the essence of iOS `SummaryUpdater.rebuildAllSummaries` (minus the CloudKit
 * dedup machinery, which is irrelevant to a single-user local Room store). [day] is expected
 * to already be a start-of-day epoch — keeping this clock-free and JVM-testable.
 */
object SummaryRollup {

    data class ItemInput(val name: String, val categoryRaw: String?)
    data class VisitRollupInput(val day: Long, val total: BigDecimal, val items: List<ItemInput>)

    data class DayNameKey(val day: Long, val name: String)
    data class DayStat(val revenue: BigDecimal, val visitCount: Int)

    data class Result(
        val days: Map<Long, DayStat>,
        val services: Map<DayNameKey, Int>,
        val categories: Map<DayNameKey, Int>
    )

    fun compute(visits: List<VisitRollupInput>): Result {
        val days = HashMap<Long, DayStat>()
        val services = HashMap<DayNameKey, Int>()
        val categories = HashMap<DayNameKey, Int>()

        for (v in visits) {
            val prev = days[v.day] ?: DayStat(BigDecimal.ZERO, 0)
            days[v.day] = DayStat(prev.revenue + v.total, prev.visitCount + 1)
            for (item in v.items) {
                val sk = DayNameKey(v.day, item.name)
                services[sk] = (services[sk] ?: 0) + 1
                item.categoryRaw?.takeIf { it.isNotBlank() }?.let { raw ->
                    val ck = DayNameKey(v.day, raw)
                    categories[ck] = (categories[ck] ?: 0) + 1
                }
            }
        }
        return Result(days, services, categories)
    }
}
