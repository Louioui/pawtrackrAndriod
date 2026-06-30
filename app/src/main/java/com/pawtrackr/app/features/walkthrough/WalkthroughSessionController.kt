package com.pawtrackr.app.features.walkthrough

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class WalkthroughSessionController @Inject constructor() {
    private val _uiState = MutableStateFlow(WalkthroughUiState())
    val uiState: StateFlow<WalkthroughUiState> = _uiState.asStateFlow()

    fun start(steps: List<WalkthroughStep>) {
        _uiState.value = if (steps.isEmpty()) {
            WalkthroughUiState()
        } else {
            WalkthroughUiState(
                steps = steps,
                currentIndex = 0,
                activeTargetBounds = null,
                isVisible = true
            )
        }
    }

    fun advance() {
        _uiState.update { current ->
            if (!current.isVisible) return@update current
            if (current.currentIndex >= current.steps.lastIndex) {
                WalkthroughUiState()
            } else {
                current.copy(
                    currentIndex = current.currentIndex + 1,
                    activeTargetBounds = null
                )
            }
        }
    }

    fun dismiss() {
        _uiState.value = WalkthroughUiState()
    }

    fun updateTargetBounds(stepId: String, coordinates: LayoutCoordinates) {
        if (!coordinates.isAttached) return
        updateTargetBounds(stepId = stepId, bounds = coordinates.boundsInRoot())
    }

    fun updateTargetBounds(stepId: String, bounds: Rect) {
        _uiState.update { current ->
            if (current.activeStep?.id == stepId) {
                current.copy(activeTargetBounds = bounds)
            } else {
                current
            }
        }
    }
}
