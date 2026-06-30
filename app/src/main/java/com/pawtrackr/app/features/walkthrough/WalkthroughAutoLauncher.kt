package com.pawtrackr.app.features.walkthrough

import javax.inject.Inject

class WalkthroughAutoLauncher @Inject constructor(
    private val launchStore: WalkthroughLaunchStore,
    private val sessionController: WalkthroughSessionController
) {
    fun startAfterSetupIfNeeded(isSetupComplete: Boolean): Boolean {
        if (!isSetupComplete || launchStore.hasSeenNavigationWalkthrough) return false

        launchStore.hasSeenNavigationWalkthrough = true
        sessionController.start(PawtrackrWalkthrough.navigationSteps())
        return true
    }
}
