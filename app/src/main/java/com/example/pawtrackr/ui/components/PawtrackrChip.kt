package com.example.pawtrackr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawtrackr.ui.theme.PawtrackrSemanticColor
import com.example.pawtrackr.ui.theme.PawtrackrStaticColor
import com.example.pawtrackr.ui.theme.PawtrackrTokens

enum class PawtrackrChipTone {
    Neutral,
    Brand,
    Success,
    Warning,
    Danger,
    Info,
    Female
}

enum class PawtrackrChipStyle {
    Tinted,
    Outline,
    Filled
}

@Composable
fun PawtrackrChip(
    label: String,
    modifier: Modifier = Modifier,
    tone: PawtrackrChipTone = PawtrackrChipTone.Neutral,
    style: PawtrackrChipStyle = PawtrackrChipStyle.Tinted,
    leadingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    val base = tone.baseColor()
    val container = when (style) {
        PawtrackrChipStyle.Tinted -> base.copy(alpha = 0.12f)
        PawtrackrChipStyle.Outline -> Color.Transparent
        PawtrackrChipStyle.Filled -> base
    }
    val content = when (style) {
        PawtrackrChipStyle.Filled -> Color.White
        else -> base
    }
    val border = when (style) {
        PawtrackrChipStyle.Outline -> BorderStroke(1.dp, base.copy(alpha = 0.55f))
        else -> null
    }

    Surface(
        modifier = modifier
            .heightIn(min = 32.dp)
            .then(if (border != null) Modifier.border(border, PawtrackrTokens.pillShape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = PawtrackrTokens.pillShape,
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PawtrackrTokens.md, vertical = PawtrackrTokens.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.xs)
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PawtrackrChipTone.baseColor(): Color =
    when (this) {
        PawtrackrChipTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        PawtrackrChipTone.Brand -> PawtrackrStaticColor.BrandPrimary
        PawtrackrChipTone.Success -> PawtrackrSemanticColor.Success
        PawtrackrChipTone.Warning -> PawtrackrSemanticColor.Warning
        PawtrackrChipTone.Danger -> PawtrackrSemanticColor.Danger
        PawtrackrChipTone.Info -> PawtrackrSemanticColor.Info
        PawtrackrChipTone.Female -> PawtrackrSemanticColor.Female
    }
