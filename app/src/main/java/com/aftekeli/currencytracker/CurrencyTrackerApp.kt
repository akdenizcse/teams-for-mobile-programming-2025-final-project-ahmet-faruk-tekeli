package com.aftekeli.currencytracker

import android.app.Application
import com.aftekeli.currencytracker.data.repository.WalletInitializer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CurrencyTrackerApp : Application() {
    
    @Inject
    lateinit var walletInitializer: WalletInitializer
    
    override fun onCreate() {
        super.onCreate()
        
        // Cüzdan başlatıcıyı çalıştır
        walletInitializer.initialize()
    }
} 