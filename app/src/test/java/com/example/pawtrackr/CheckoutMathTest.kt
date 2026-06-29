package com.example.pawtrackr

import com.example.pawtrackr.domain.checkout.CheckoutMath
import com.example.pawtrackr.domain.checkout.LineInput
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CheckoutMathTest {

    private fun bd(s: String) = BigDecimal(s)

    @Test fun persistedSubtotal_sums_rounded_lines() {
        val items = listOf(LineInput(bd("50.00"), 1), LineInput(bd("30.00"), 1))
        assertEquals(bd("80.00"), CheckoutMath.persistedSubtotal(items))
    }

    @Test fun reconcile_unchanged_when_subtotal_equals_target() {
        val items = listOf(LineInput(bd("50.00"), 1), LineInput(bd("30.00"), 1))
        assertEquals(listOf(bd("50.00"), bd("30.00")), CheckoutMath.reconcileUnitPrices(items, bd("80.00")))
    }

    @Test fun reconcile_allocates_proportionally_last_absorbs_remainder() {
        val items = listOf(LineInput(bd("50.00"), 1), LineInput(bd("30.00"), 1))
        val prices = CheckoutMath.reconcileUnitPrices(items, bd("90.00"))
        assertEquals(listOf(bd("56.25"), bd("33.75")), prices)
        // qty == 1, so persisted lines sum EXACTLY to the override total.
        val persisted = prices.mapIndexed { i, p -> CheckoutMath.persistedLineTotal(p, items[i].quantity) }
            .fold(BigDecimal.ZERO) { a, b -> a + b }
        assertEquals(bd("90.00"), persisted)
    }

    @Test fun quantity_gt_1_documents_indivisible_drift() {
        // $100 over a single qty-3 line is NOT representable as round(unitPrice)*3.
        val items = listOf(LineInput(bd("33.33"), 3))
        val prices = CheckoutMath.reconcileUnitPrices(items, bd("100.00"))
        assertEquals(listOf(bd("33.33")), prices)
        // Persisted breakdown is 99.99 — proof the lines CANNOT be trusted as the charge.
        assertEquals(bd("99.99"), CheckoutMath.persistedLineTotal(prices[0], 3))
        // The authoritative charge stays finalTotal (100.00); see CheckoutRepository which
        // writes Payment.amount = finalTotal, never the summed lines.
    }

    @Test fun tip_is_percentage_of_subtotal_rounded() {
        assertEquals(bd("9.00"), CheckoutMath.tipAmount(bd("50.00"), 18))   // 50 * .18
        assertEquals(bd("15.00"), CheckoutMath.tipAmount(bd("75.00"), 20))
        assertEquals(bd("0.00"), CheckoutMath.tipAmount(bd("50.00"), 0))
        // 33.33 * 15% = 4.9995 -> 5.00 (HALF_UP)
        assertEquals(bd("5.00"), CheckoutMath.tipAmount(bd("33.33"), 15))
    }

    @Test fun rounding_is_half_up_2dp() {
        assertEquals(bd("16.67"), CheckoutMath.round(bd("16.665")))
        assertEquals(bd("0.00"), CheckoutMath.round(BigDecimal.ZERO))
    }
}
