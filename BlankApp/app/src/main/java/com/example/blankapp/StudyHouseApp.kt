package com.example.blankapp

import android.app.Application
import com.example.blankapp.data.CrashReporter
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for A+ Study House.
 * Initializes crash reporter and other app-wide services.
 */
@HiltAndroidApp
class StudyHouseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize crash reporter (local file logging + uncaught exception handler)
        CrashReporter.init(this)

        // Log app initialization
        CrashReporter.logEvent("APP_INIT", "StudyHouseApp.onCreate() completed")
    }
}
