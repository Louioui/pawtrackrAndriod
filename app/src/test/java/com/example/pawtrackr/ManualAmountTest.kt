package com.example.pawtrackr

import com.example.pawtrackr.ui.checkout.parseManualAmount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import java.math.BigDecimal
import org.junit.Test

/** The checkout custom-total override parsing (field text -> CheckoutRequest.amount). */
class ManualAmountTest {
    @Test fun valid_amounts_parse_and_round() {
        assertEquals(BigDecimal("75.00"), parseManualAmount("75"))
        assertEquals(BigDecimal("60.50"), parseManualAmount(" 60.5 "))
        assertEquals(BigDecimal("0.00"), parseManualAmount("0"))
    }

    @Test fun blank_or_invalid_or_negative_is_null() {
        assertNull(parseManualAmount(""))
        assertNull(parseManualAmount("   "))
        assertNull(parseManualAmount("abc"))
        assertNull(parseManualAmount("-5"))
    }
}
