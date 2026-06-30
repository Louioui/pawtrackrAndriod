package com.pawtrackr.app.features.walkthrough

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalkthroughAutoLauncherTest {

    @Test
    fun startAfterSetup_startsNavigationWalkthroughAndMarksItSeenOnce() {
        val controller = WalkthroughSessionController()
        val store = FakeWalkthroughLaunchStore()
        val launcher = WalkthroughAutoLauncher(store, controller)

        assertTrue(launcher.startAfterSetupIfNeeded(isSetupComplete = true))

        assertTrue(store.hasSeenNavigationWalkthrough)
        assertTrue(controller.uiState.value.isVisible)
        assertEquals(PawtrackrWalkthrough.TargetDashboard, controller.uiState.value.activeStep?.id)
    }

    @Test
    fun startAfterSetup_doesNotRestartWhenAlreadySeen() {
        val controller = WalkthroughSessionController()
        val store = FakeWalkthroughLaunchStore(hasSeenNavigationWalkthrough = true)
        val launcher = WalkthroughAutoLauncher(store, controller)

        assertFalse(launcher.startAfterSetupIfNeeded(isSetupComplete = true))

        assertFalse(controller.uiState.value.isVisible)
    }

    @Test
    fun startAfterSetup_waitsUntilSetupIsComplete() {
        val controller = WalkthroughSessionController()
        val store = FakeWalkthroughLaunchStore()
        val launcher = WalkthroughAutoLauncher(store, controller)

        assertFalse(launcher.startAfterSetupIfNeeded(isSetupComplete = false))

        assertFalse(store.hasSeenNavigationWalkthrough)
        assertFalse(controller.uiState.value.isVisible)
    }

    private class FakeWalkthroughLaunchStore(
        override var hasSeenNavigationWalkthrough: Boolean = false
    ) : WalkthroughLaunchStore
}
