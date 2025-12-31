package com.umc.umc

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ScoiApp: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}