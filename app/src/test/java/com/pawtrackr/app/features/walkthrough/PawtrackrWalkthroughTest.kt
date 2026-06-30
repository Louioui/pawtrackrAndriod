package com.pawtrackr.app.features.walkthrough

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PawtrackrWalkthroughTest {

    @Test
    fun navigationSteps_matchAppShellTargetIdsInOrder() {
        val steps = PawtrackrWalkthrough.navigationSteps()

        assertEquals(
            listOf(
                PawtrackrWalkthrough.TargetDashboard,
                PawtrackrWalkthrough.TargetClients,
                PawtrackrWalkthrough.TargetInsights,
                PawtrackrWalkthrough.TargetSettings
            ),
            steps.map { it.id }
        )
    }

    @Test
    fun destinationKeyFor_mapsNavigationTargets() {
        assertEquals("dashboard", PawtrackrWalkthrough.destinationKeyFor(PawtrackrWalkthrough.TargetDashboard))
        assertEquals("clients", PawtrackrWalkthrough.destinationKeyFor(PawtrackrWalkthrough.TargetClients))
        assertEquals("insights", PawtrackrWalkthrough.destinationKeyFor(PawtrackrWalkthrough.TargetInsights))
        assertEquals("settings", PawtrackrWalkthrough.destinationKeyFor(PawtrackrWalkthrough.TargetSettings))
        assertEquals(null, PawtrackrWalkthrough.destinationKeyFor("unknown"))
    }

    @Test
    fun navigationSteps_haveCompleteReadableCopy() {
        val steps = PawtrackrWalkthrough.navigationSteps()

        assertEquals(4, steps.size)
        steps.forEach { step ->
            assertFalse(step.title.isBlank())
            assertFalse(step.message.isBlank())
            assertFalse(step.primaryActionLabel.isBlank())
            assertFalse(step.completeActionLabel.isBlank())
            assertFalse(step.dismissActionLabel.isBlank())
        }
        assertEquals("Done", steps.last().completeActionLabel)
    }
}
