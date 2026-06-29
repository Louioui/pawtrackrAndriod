package com.example.pawtrackr.domain.loyalty

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Loyalty points earned on a checkout. Ports iOS `LoyaltyEngine.calculatePoints`:
 * 1 point per whole dollar of the visit total (the integer part, fraction discarded).
 * Pure / JVM-testable.
 */
object LoyaltyEngine {
    fun calculatePoints(total: BigDecimal): Int {
        if (total.signum() <= 0) return 0
        // Round to money scale, then take the integer part (truncates toward zero) — matches
        // NSDecimalNumber.intValue on iOS.
        return total.setScale(2, RoundingMode.HALF_UP).toInt()
    }
}
