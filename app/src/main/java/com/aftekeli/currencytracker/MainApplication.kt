package com.aftekeli.currencytracker

import android.app.Application
import com.aftekeli.currencytracker.data.SettingsManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {
    // Lazy initialize the SettingsManager
    val settingsManager by lazy { SettingsManager(applicationContext) }

    companion object {
        private lateinit var instance: MainApplication

        fun getInstance(): MainApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
} 