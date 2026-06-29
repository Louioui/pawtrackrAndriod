package com.example.pawtrackr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pawtrackr.ui.theme.PawtrackrElevation
import com.example.pawtrackr.ui.theme.PawtrackrTokens

enum class PawtrackrAccentEdge {
    Leading,
    Top
}

@Composable
fun PawtrackrCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    accentEdge: PawtrackrAccentEdge = PawtrackrAccentEdge.Leading,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentPadding: PaddingValues = PaddingValues(PawtrackrTokens.md),
    elevation: Dp = PawtrackrElevation.Resting,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = PawtrackrTokens.cardShape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(
            width = PawtrackrTokens.hairlineWidth,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .drawPawtrackrAccent(accentColor, accentEdge)
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun PawtrackrSurfaceBand(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)),
        content = content
    )
}

private fun Modifier.drawPawtrackrAccent(
    accentColor: Color?,
    accentEdge: PawtrackrAccentEdge
): Modifier {
    if (accentColor == null) return this

    return drawBehind {
        when (accentEdge) {
            PawtrackrAccentEdge.Leading -> drawRect(
                color = accentColor,
                topLeft = Offset.Zero,
                size = Size(PawtrackrTokens.accentRailWidth.toPx(), size.height)
            )
            PawtrackrAccentEdge.Top -> drawRect(
                color = accentColor,
                topLeft = Offset.Zero,
                size = Size(size.width, PawtrackrTokens.topAccentHeight.toPx())
            )
        }
    }
}
