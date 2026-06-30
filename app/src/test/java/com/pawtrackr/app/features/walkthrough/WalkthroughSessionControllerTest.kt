package com.pawtrackr.app.features.walkthrough

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkthroughSessionControllerTest {

    @Test
    fun start_showsFirstStep() {
        val controller = WalkthroughSessionController()
        val steps = listOf(
            WalkthroughStep(id = "clients", title = "Clients", message = "Manage people and pets."),
            WalkthroughStep(id = "checkout", title = "Checkout", message = "Finish visits quickly.")
        )

        controller.start(steps)

        val state = controller.uiState.value
        assertTrue(state.isVisible)
        assertEquals("clients", state.activeStep?.id)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun advance_movesForwardThenDismissesAfterLastStep() {
        val controller = WalkthroughSessionController()
        controller.start(
            listOf(
                WalkthroughStep(id = "clients", title = "Clients", message = "Manage people and pets."),
                WalkthroughStep(id = "checkout", title = "Checkout", message = "Finish visits quickly.")
            )
        )

        controller.advance()
        assertEquals("checkout", controller.uiState.value.activeStep?.id)

        controller.advance()
        assertFalse(controller.uiState.value.isVisible)
        assertNull(controller.uiState.value.activeStep)
    }

    @Test
    fun updateTargetBounds_tracksOnlyTheActiveStep() {
        val controller = WalkthroughSessionController()
        val activeBounds = Rect(left = 10f, top = 20f, right = 120f, bottom = 80f)
        controller.start(
            listOf(
                WalkthroughStep(id = "clients", title = "Clients", message = "Manage people and pets."),
                WalkthroughStep(id = "checkout", title = "Checkout", message = "Finish visits quickly.")
            )
        )

        controller.updateTargetBounds(stepId = "checkout", bounds = Rect.Zero)
        assertNull(controller.uiState.value.activeTargetBounds)

        controller.updateTargetBounds(stepId = "clients", bounds = activeBounds)
        assertEquals(activeBounds, controller.uiState.value.activeTargetBounds)
    }

    @Test
    fun dismiss_clearsStepsAndBounds() {
        val controller = WalkthroughSessionController()
        controller.start(listOf(WalkthroughStep(id = "clients", title = "Clients", message = "Manage people and pets.")))
        controller.updateTargetBounds("clients", Rect(left = 1f, top = 2f, right = 3f, bottom = 4f))

        controller.dismiss()

        val state = controller.uiState.value
        assertFalse(state.isVisible)
        assertEquals(emptyList<WalkthroughStep>(), state.steps)
        assertNull(state.activeTargetBounds)
    }
}
