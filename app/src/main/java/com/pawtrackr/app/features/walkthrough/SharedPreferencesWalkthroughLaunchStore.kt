package com.pawtrackr.app.features.walkthrough

import android.content.Context
import javax.inject.Inject

class SharedPreferencesWalkthroughLaunchStore @Inject constructor(
    context: Context
) : WalkthroughLaunchStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE
    )

    override var hasSeenNavigationWalkthrough: Boolean
        get() = preferences.getBoolean(KeyNavigationWalkthroughSeen, false)
        set(value) {
            preferences.edit().putBoolean(KeyNavigationWalkthroughSeen, value).apply()
        }

    private companion object {
        const val PreferencesName = "pawtrackr_walkthrough_launches"
        const val KeyNavigationWalkthroughSeen = "navigation_walkthrough_seen"
    }
}
