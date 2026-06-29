package com.example.pawtrackr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PawtrackrStaticColor {
    const val BrandPrimaryArgb: Int = -10262799 // 0xFF6366F1
    const val BrandPrimarySoftArgb: Int = -330241 // 0xFFFAF5FF
    const val SurfaceWarmArgb: Int = -65794 // 0xFFFEFEFE
    const val SurfaceGroupedArgb: Int = -460552 // 0xFFF8F8F8
    const val BorderArgb: Int = 0x26000000

    val BrandPrimary: Color = Color(BrandPrimaryArgb)
    val BrandPrimarySoft: Color = Color(BrandPrimarySoftArgb)
    val SurfaceWarm: Color = Color(SurfaceWarmArgb)
    val SurfaceGrouped: Color = Color(SurfaceGroupedArgb)
    val Border: Color = Color(BorderArgb)
}

object PawtrackrSemanticColor {
    const val SuccessArgb: Int = -15681151 // 0xFF10B981
    const val WarningArgb: Int = -680437 // 0xFFF59E0B
    const val DangerArgb: Int = -1096636 // 0xFFEF4444
    const val InfoArgb: Int = -12877066 // 0xFF3B82F6
    const val FemaleArgb: Int = -1292135 // 0xFFEC4899

    val Success: Color = Color(SuccessArgb)
    val Warning: Color = Color(WarningArgb)
    val Danger: Color = Color(DangerArgb)
    val Info: Color = Color(InfoArgb)
    val Female: Color = Color(FemaleArgb)
}

object PawtrackrSpacing {
    const val Xxs: Int = 4
    const val Xs: Int = 6
    const val Sm: Int = 8
    const val Md: Int = 12
    const val Lg: Int = 16
    const val Xl: Int = 20
    const val Xxl: Int = 24
}

object PawtrackrRadius {
    const val Sm: Int = 10
    const val Md: Int = 14
    const val Lg: Int = 18
    const val Pill: Int = 999
}

object PawtrackrElevation {
    val Resting: Dp = 1.dp
    val Raised: Dp = 3.dp
}

object PawtrackrTokens {
    val xxs: Dp = PawtrackrSpacing.Xxs.dp
    val xs: Dp = PawtrackrSpacing.Xs.dp
    val sm: Dp = PawtrackrSpacing.Sm.dp
    val md: Dp = PawtrackrSpacing.Md.dp
    val lg: Dp = PawtrackrSpacing.Lg.dp
    val xl: Dp = PawtrackrSpacing.Xl.dp
    val xxl: Dp = PawtrackrSpacing.Xxl.dp

    val cardShape = RoundedCornerShape(PawtrackrRadius.Md.dp)
    val largeCardShape = RoundedCornerShape(PawtrackrRadius.Lg.dp)
    val compactShape = RoundedCornerShape(PawtrackrRadius.Sm.dp)
    val pillShape = RoundedCornerShape(PawtrackrRadius.Pill.dp)

    val accentRailWidth: Dp = 4.dp
    val topAccentHeight: Dp = 3.dp
    val hairlineWidth: Dp = 1.dp
}
