package com.example.pawtrackr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PawtrackrPurple = Color(0xFF6200EE)
private val PawtrackrPurpleDark = Color(0xFF3700B3)
private val PawtrackrTeal = Color(0xFF03DAC5)

private val LightColors = lightColorScheme(
    primary = PawtrackrPurple,
    secondary = PawtrackrTeal,
    tertiary = PawtrackrPurpleDark
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = PawtrackrTeal,
    tertiary = PawtrackrPurpleDark
)

@Composable
fun PawtrackrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
