package com.pawtrackr.app.features.walkthrough

object PawtrackrWalkthrough {
    const val TargetDashboard: String = "nav_dashboard"
    const val TargetClients: String = "nav_clients"
    const val TargetInsights: String = "nav_insights"
    const val TargetSettings: String = "nav_settings"

    fun navigationSteps(): List<WalkthroughStep> =
        listOf(
            WalkthroughStep(
                id = TargetDashboard,
                title = "Dashboard",
                message = "Track today, open sessions, attention flags, and recent revenue in one place."
            ),
            WalkthroughStep(
                id = TargetClients,
                title = "Clients",
                message = "Manage owners, pets, visit history, safety notes, messages, and checkout."
            ),
            WalkthroughStep(
                id = TargetInsights,
                title = "Insights",
                message = "Review revenue, services, categories, and client patterns from completed visits."
            ),
            WalkthroughStep(
                id = TargetSettings,
                title = "Settings",
                message = "Keep the business profile and guided setup controls ready when details change.",
                completeActionLabel = "Done"
            )
        )

    fun destinationKeyFor(stepId: String?): String? =
        when (stepId) {
            TargetDashboard -> "dashboard"
            TargetClients -> "clients"
            TargetInsights -> "insights"
            TargetSettings -> "settings"
            else -> null
        }
}
