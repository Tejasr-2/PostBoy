package com.webcamapp.mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WebcamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-level configurations here
    }
}