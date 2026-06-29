package com.example.pawtrackr

import com.example.pawtrackr.domain.loyalty.LoyaltyEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class LoyaltyEngineTest {
    private fun bd(s: String) = BigDecimal(s)

    @Test fun one_point_per_whole_dollar() {
        assertEquals(35, LoyaltyEngine.calculatePoints(bd("35.40")))
        assertEquals(85, LoyaltyEngine.calculatePoints(bd("85.00")))
        assertEquals(120, LoyaltyEngine.calculatePoints(bd("120.99")))
    }

    @Test fun zero_or_negative_earns_nothing() {
        assertEquals(0, LoyaltyEngine.calculatePoints(BigDecimal.ZERO))
        assertEquals(0, LoyaltyEngine.calculatePoints(bd("0.99")))
        assertEquals(0, LoyaltyEngine.calculatePoints(bd("-5.00")))
    }
}
