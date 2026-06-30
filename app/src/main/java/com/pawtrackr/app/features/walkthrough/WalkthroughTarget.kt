package com.pawtrackr.app.features.walkthrough

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned

fun Modifier.walkthroughTarget(
    stepId: String,
    controller: WalkthroughSessionController
): Modifier =
    onGloballyPositioned { coordinates ->
        controller.updateTargetBounds(stepId = stepId, coordinates = coordinates)
    }
