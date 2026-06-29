package com.example.pawtrackr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import com.example.pawtrackr.ui.theme.PawtrackrTokens

@Composable
fun PawtrackrTimelineItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accentColor: Color = PawtrackrStaticColor.BrandPrimary,
    showConnector: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)
    ) {
        TimelineNode(accentColor = accentColor, showConnector = showConnector)
        Column(
            modifier = Modifier.weight(1f).padding(bottom = PawtrackrTokens.md),
            verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                trailing?.invoke()
            }
        }
    }
}

@Composable
private fun TimelineNode(
    accentColor: Color,
    showConnector: Boolean
) {
    Box(
        modifier = Modifier
            .width(18.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (showConnector) {
            Canvas(Modifier.fillMaxHeight().width(2.dp)) {
                drawLine(
                    color = accentColor.copy(alpha = 0.22f),
                    start = Offset(size.width / 2, 14.dp.toPx()),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        Canvas(Modifier.size(14.dp)) {
            drawCircle(color = accentColor.copy(alpha = 0.18f), radius = size.minDimension / 2)
            drawCircle(color = accentColor, radius = size.minDimension / 3)
        }
    }
}
