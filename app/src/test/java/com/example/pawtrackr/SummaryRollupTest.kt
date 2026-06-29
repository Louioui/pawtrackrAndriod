package com.example.pawtrackr

import com.example.pawtrackr.domain.insights.SummaryRollup
import com.example.pawtrackr.domain.insights.SummaryRollup.DayNameKey
import com.example.pawtrackr.domain.insights.SummaryRollup.ItemInput
import com.example.pawtrackr.domain.insights.SummaryRollup.VisitRollupInput
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class SummaryRollupTest {
    private fun bd(s: String) = BigDecimal(s)
    private val d1 = 1_700_000_000_000L
    private val d2 = d1 + 86_400_000L

    @Test fun rolls_revenue_and_counts_per_day() {
        val r = SummaryRollup.compute(
            listOf(
                VisitRollupInput(d1, bd("50.00"), listOf(ItemInput("Bath", "Grooming"), ItemInput("De-shedding", "Add-on"))),
                VisitRollupInput(d1, bd("85.00"), listOf(ItemInput("Full Package", "Package"))),
                VisitRollupInput(d2, bd("30.00"), listOf(ItemInput("Bath", "Grooming")))
            )
        )
        assertEquals(bd("135.00"), r.days[d1]!!.revenue)
        assertEquals(2, r.days[d1]!!.visitCount)
        assertEquals(bd("30.00"), r.days[d2]!!.revenue)
        assertEquals(1, r.days[d2]!!.visitCount)
    }

    @Test fun counts_services_and_categories_per_day() {
        val r = SummaryRollup.compute(
            listOf(
                VisitRollupInput(d1, bd("60.00"), listOf(ItemInput("Bath", "Grooming"), ItemInput("Bath", "Grooming"))),
                VisitRollupInput(d1, bd("40.00"), listOf(ItemInput("Haircut", "Grooming")))
            )
        )
        assertEquals(2, r.services[DayNameKey(d1, "Bath")])
        assertEquals(1, r.services[DayNameKey(d1, "Haircut")])
        // Grooming category appears on 3 line items that day.
        assertEquals(3, r.categories[DayNameKey(d1, "Grooming")])
    }

    @Test fun null_or_blank_category_is_skipped() {
        val r = SummaryRollup.compute(
            listOf(VisitRollupInput(d1, bd("10.00"), listOf(ItemInput("Custom", null), ItemInput("Other", ""))))
        )
        assertEquals(emptyMap<DayNameKey, Int>(), r.categories)
        assertEquals(1, r.services[DayNameKey(d1, "Custom")])
    }
}
