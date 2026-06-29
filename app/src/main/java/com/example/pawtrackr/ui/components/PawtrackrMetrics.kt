package com.example.pawtrackr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import com.example.pawtrackr.ui.theme.PawtrackrTokens

data class PawtrackrBarItem(
    val label: String,
    val value: Int,
    val accentColor: Color = PawtrackrStaticColor.BrandPrimary
)

@Composable
fun PawtrackrSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

@Composable
fun PawtrackrKpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    subtitle: String? = null
) {
    PawtrackrCard(
        modifier = modifier,
        accentColor = accentColor,
        accentEdge = PawtrackrAccentEdge.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PawtrackrBarList(
    items: List<PawtrackrBarItem>,
    modifier: Modifier = Modifier
) {
    val max = (items.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)

    PawtrackrCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)) {
            items.forEach { item ->
                PawtrackrBarRow(item = item, max = max)
            }
        }
    }
}

@Composable
private fun PawtrackrBarRow(item: PawtrackrBarItem, max: Int) {
    val fraction = (item.value.toFloat() / max.toFloat()).coerceIn(0.04f, 1f)

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(item.label, style = MaterialTheme.typography.bodyMedium)
            Text(
                item.value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(PawtrackrTokens.xs))
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(item.accentColor.copy(alpha = 0.13f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(item.accentColor)
            )
        }
    }
}

@Composable
fun PawtrackrEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(PawtrackrTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
