package com.aftekeli.currencytracker

import androidx.multidex.MultiDexApplication
import com.aftekeli.currencytracker.data.preferences.SettingsManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CurrencyTrackerApp : MultiDexApplication() {
    // Lazy initialize the SettingsManager
    val settingsManager by lazy { SettingsManager(applicationContext) }

    companion object {
        private lateinit var instance: CurrencyTrackerApp

        fun getInstance(): CurrencyTrackerApp {
            return instance
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
    }
} 