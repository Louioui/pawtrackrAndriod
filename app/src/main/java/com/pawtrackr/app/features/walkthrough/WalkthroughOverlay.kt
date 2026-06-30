package com.pawtrackr.app.features.walkthrough

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.pawtrackr.ui.theme.PawtrackrTokens

@Composable
fun WalkthroughOverlay(
    controller: WalkthroughSessionController,
    modifier: Modifier = Modifier
) {
    val state by controller.uiState.collectAsState()
    val step = state.activeStep ?: return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .interceptWalkthroughTouches()
    ) {
        val targetBounds = state.activeTargetBounds?.inflate(FocusPaddingPx)
        val tooltipAlignment = tooltipAlignment(targetBounds)

        Canvas(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
        ) {
            drawRect(Color.Black.copy(alpha = 0.62f))
            targetBounds?.let { rect ->
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    cornerRadius = CornerRadius(FocusCornerRadiusPx, FocusCornerRadiusPx),
                    blendMode = BlendMode.Clear
                )
            }
        }

        WalkthroughCallout(
            step = step,
            isLastStep = state.isLastStep,
            onAdvance = controller::advance,
            onDismiss = controller::dismiss,
            modifier = Modifier
                .align(tooltipAlignment)
                .padding(PawtrackrTokens.xl)
        )
    }
}

@Composable
private fun WalkthroughCallout(
    step: WalkthroughStep,
    isLastStep: Boolean,
    onAdvance: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.widthIn(max = 420.dp),
        shape = RoundedCornerShape(PawtrackrTokens.lg),
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(PawtrackrTokens.xl),
            verticalArrangement = Arrangement.spacedBy(PawtrackrTokens.md)
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = step.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(PawtrackrTokens.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(step.dismissActionLabel)
                }
                Button(onClick = onAdvance) {
                    Text(if (isLastStep) step.completeActionLabel else step.primaryActionLabel)
                }
            }
        }
    }
}

private fun Modifier.interceptWalkthroughTouches(): Modifier =
    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                event.changes.forEach { change -> change.consume() }
            }
        }
    }

private fun tooltipAlignment(targetBounds: Rect?): Alignment =
    when {
        targetBounds == null -> Alignment.BottomCenter
        targetBounds.center.y < 900f -> Alignment.BottomCenter
        else -> Alignment.TopCenter
    }

private const val FocusPaddingPx = 18f
private const val FocusCornerRadiusPx = 28f
