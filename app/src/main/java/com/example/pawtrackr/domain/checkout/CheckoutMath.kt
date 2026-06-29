package com.example.pawtrackr.domain.checkout

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max

/** A line being checked out: a catalog price snapshot and a quantity. */
data class LineInput(val unitPrice: BigDecimal, val quantity: Int)

/**
 * Pure money math for checkout — ported from `CheckoutTransactionActor.reconcileLineItemPrices`.
 *
 * CRITICAL precision note: line items persist as `unitPrice × quantity`, each rounded to 2dp.
 * For quantity > 1 the rounded lines cannot always sum to an arbitrary target, so the
 * **authoritative charged amount is `finalTotal`** (stored directly on the Payment and the
 * Visit, exactly as iOS `markCheckedOut(total:)` overrides it). The reconciled unit prices are
 * a best-effort breakdown — [persistedSubtotal] of them may differ from `finalTotal` by pennies
 * when quantities don't divide evenly. Never derive the charge from the summed lines.
 */
object CheckoutMath {

    fun round(v: BigDecimal): BigDecimal = v.setScale(2, RoundingMode.HALF_UP)

    /** Tip = round(subtotal × percent/100). 0% (or less) yields zero. */
    fun tipAmount(subtotal: BigDecimal, percent: Int): BigDecimal {
        if (percent <= 0) return BigDecimal.ZERO.setScale(2)
        return round(subtotal.multiply(BigDecimal(percent)).divide(BigDecimal(100)))
    }

    /** What actually lands in the DB for one line: round(unitPrice × quantity). */
    fun persistedLineTotal(unitPrice: BigDecimal, quantity: Int): BigDecimal =
        round(unitPrice.multiply(BigDecimal(max(1, quantity))))

    /** Σ of persisted line totals — the natural subtotal before any manual override. */
    fun persistedSubtotal(items: List<LineInput>): BigDecimal =
        items.fold(BigDecimal.ZERO) { acc, it -> acc + persistedLineTotal(it.unitPrice, it.quantity) }

    /**
     * Re-allocate unit prices so the line breakdown approximates [finalTotal] proportionally,
     * the last line absorbing the rounding remainder. Returns one unit price per input line.
     * If the natural subtotal already equals [finalTotal], unit prices are returned unchanged.
     */
    fun reconcileUnitPrices(items: List<LineInput>, finalTotal: BigDecimal): List<BigDecimal> {
        if (items.isEmpty()) return emptyList()
        val subtotal = persistedSubtotal(items)
        val target = round(finalTotal)
        if (subtotal.compareTo(target) == 0) return items.map { round(it.unitPrice) }

        var allocated = BigDecimal.ZERO
        val unitPrices = ArrayList<BigDecimal>(items.size)
        items.forEachIndexed { index, item ->
            val lineTotal: BigDecimal = when {
                index == items.lastIndex -> round(target - allocated)
                subtotal.signum() > 0 -> {
                    val share = persistedLineTotal(item.unitPrice, item.quantity)
                        .divide(subtotal, 10, RoundingMode.HALF_UP)
                        .multiply(target)
                    round(share).also { allocated += it }
                }
                else -> round(target.divide(BigDecimal(items.size), 10, RoundingMode.HALF_UP)).also { allocated += it }
            }
            val qty = BigDecimal(max(1, item.quantity))
            unitPrices.add(round(lineTotal.divide(qty, 10, RoundingMode.HALF_UP)))
        }
        return unitPrices
    }
}
