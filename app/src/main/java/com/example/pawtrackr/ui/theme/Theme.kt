package com.example.pawtrackr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Brand fallback palette (used when dynamic color is off or below Android 12).
private val BrandLight = lightColorScheme(
    primary = PawtrackrStaticColor.BrandPrimary,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0F766E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF042F2E),
    tertiary = PawtrackrSemanticColor.Female,
    tertiaryContainer = Color(0xFFFCE7F3),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outlineVariant = Color(0xFFE2E8F0),
    error = PawtrackrSemanticColor.Danger,
    onError = Color(0xFFFFFFFF)
)

private val BrandDark = darkColorScheme(
    primary = Color(0xFFC7D2FE),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF4338CA),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF5EEAD4),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFF9A8D4),
    tertiaryContainer = Color(0xFF9D174D),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE5E7EB),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF450A0A)
)

/**
 * App theme. On Android 12+ (default) it uses **Material You dynamic color** — the palette is
 * derived from the user's wallpaper, so the app adapts to each user's phone style. Below 12,
 * or when [dynamicColor] is false, it falls back to the Pawtrackr brand palette. Honors the
 * system light/dark setting.
 */
@Composable
fun PawtrackrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> BrandDark
        else -> BrandLight
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
