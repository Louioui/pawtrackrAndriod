package com.example.pawtrackr

import com.example.pawtrackr.ui.theme.PawtrackrRadius
import com.example.pawtrackr.ui.theme.PawtrackrSemanticColor
import com.example.pawtrackr.ui.theme.PawtrackrSpacing
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PawtrackrDesignTest {
    @Test
    fun brandPrimaryMatchesIosDesignSystem() {
        assertEquals(0xFF6366F1.toInt(), PawtrackrStaticColor.BrandPrimaryArgb)
    }

    @Test
    fun spacingScaleMatchesIosDesignSystem() {
        assertEquals(4, PawtrackrSpacing.Xxs)
        assertEquals(6, PawtrackrSpacing.Xs)
        assertEquals(8, PawtrackrSpacing.Sm)
        assertEquals(12, PawtrackrSpacing.Md)
        assertEquals(16, PawtrackrSpacing.Lg)
        assertEquals(20, PawtrackrSpacing.Xl)
        assertEquals(24, PawtrackrSpacing.Xxl)
    }

    @Test
    fun radiiKeepCardsTighterThanPills() {
        assertEquals(10, PawtrackrRadius.Sm)
        assertEquals(14, PawtrackrRadius.Md)
        assertEquals(18, PawtrackrRadius.Lg)
        assertTrue(PawtrackrRadius.Pill > PawtrackrRadius.Lg)
    }

    @Test
    fun semanticColorsUseDistinctChannels() {
        val semanticColors = setOf(
            PawtrackrSemanticColor.SuccessArgb,
            PawtrackrSemanticColor.WarningArgb,
            PawtrackrSemanticColor.DangerArgb,
            PawtrackrSemanticColor.InfoArgb
        )

        assertEquals(4, semanticColors.size)
        assertEquals(0xFFEF4444.toInt(), PawtrackrSemanticColor.DangerArgb)
        assertEquals(0xFF10B981.toInt(), PawtrackrSemanticColor.SuccessArgb)
    }
}
