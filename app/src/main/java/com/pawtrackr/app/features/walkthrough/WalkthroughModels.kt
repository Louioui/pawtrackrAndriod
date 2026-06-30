package com.pawtrackr.app.features.walkthrough

import androidx.compose.ui.geometry.Rect

data class WalkthroughStep(
    val id: String,
    val title: String,
    val message: String,
    val primaryActionLabel: String = "Next",
    val completeActionLabel: String = "Done",
    val dismissActionLabel: String = "Skip"
)

data class WalkthroughUiState(
    val steps: List<WalkthroughStep> = emptyList(),
    val currentIndex: Int = 0,
    val activeTargetBounds: Rect? = null,
    val isVisible: Boolean = false
) {
    val activeStep: WalkthroughStep?
        get() = if (isVisible) steps.getOrNull(currentIndex) else null

    val isLastStep: Boolean
        get() = currentIndex >= steps.lastIndex
}
