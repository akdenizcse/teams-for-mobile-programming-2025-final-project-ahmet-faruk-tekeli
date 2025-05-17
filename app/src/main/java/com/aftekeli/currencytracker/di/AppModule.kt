package com.aftekeli.currencytracker.di

import android.content.Context
import com.aftekeli.currencytracker.CurrencyTrackerApp
import com.aftekeli.currencytracker.data.local.CurrencyDatabase
import com.aftekeli.currencytracker.data.local.dao.AlertDao
import com.aftekeli.currencytracker.data.local.dao.CurrencyDao
import com.aftekeli.currencytracker.data.local.dao.WatchlistDao
import com.aftekeli.currencytracker.data.preferences.SessionManager
import com.aftekeli.currencytracker.data.preferences.SettingsManager
import com.aftekeli.currencytracker.data.remote.firestore.FirestoreService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Gson provider
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }
    
    // Database dependencies
    
    @Provides
    @Singleton
    fun provideCurrencyDatabase(@ApplicationContext context: Context): CurrencyDatabase {
        return CurrencyDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideCurrencyDao(database: CurrencyDatabase): CurrencyDao {
        return database.currencyDao()
    }
    
    @Provides
    @Singleton
    fun provideWatchlistDao(database: CurrencyDatabase): WatchlistDao {
        return database.watchlistDao()
    }
    
    @Provides
    @Singleton
    fun provideAlertDao(database: CurrencyDatabase): AlertDao {
        return database.alertDao()
    }
    
    // Service dependencies
    
    @Provides
    @Singleton
    fun provideFirestoreService(): FirestoreService {
        return FirestoreService()
    }
    
    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SettingsManager {
        return CurrencyTrackerApp.getInstance().settingsManager
    }
    
    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager {
        return SessionManager()
    }
} 