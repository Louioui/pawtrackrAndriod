package com.example.pawtrackr

import android.app.Application
import com.example.pawtrackr.di.AppContainer

/** Holds the app-wide [AppContainer]. Registered as android:name in the manifest. */
class PawtrackrApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.runtimeServices.start()
    }

    override fun onTerminate() {
        container.runtimeServices.stop()
        super.onTerminate()
    }
}
