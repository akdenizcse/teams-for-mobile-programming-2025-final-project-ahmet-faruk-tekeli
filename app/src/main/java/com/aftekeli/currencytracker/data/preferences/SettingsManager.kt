package com.aftekeli.currencytracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension property for Context to get the DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    // Preference keys
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val PRICE_ALERTS_ENABLED = booleanPreferencesKey("price_alerts_enabled")
        val LAST_REFRESH_TIMESTAMP = longPreferencesKey("last_refresh_timestamp")
        
        // Default refresh interval - 10 minutes
        const val DEFAULT_REFRESH_INTERVAL = 10 * 60 * 1000L
    }

    // Get dark mode preference as Flow
    val darkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: false
    }

    // Set dark mode preference
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = enabled
        }
    }

    // Get default currency preference as Flow
    val defaultCurrency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_CURRENCY] ?: "USD"
    }

    // Set default currency preference
    suspend fun setDefaultCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_CURRENCY] = currency
        }
    }

    // Get price alerts enabled preference as Flow
    val priceAlertsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PRICE_ALERTS_ENABLED] ?: true
    }

    // Set price alerts enabled preference
    suspend fun setPriceAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRICE_ALERTS_ENABLED] = enabled
        }
    }
    
    // Get last refresh timestamp
    val lastRefreshTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_REFRESH_TIMESTAMP] ?: 0L
    }
    
    // Update last refresh timestamp to current time
    suspend fun updateLastRefreshTimestamp() {
        context.dataStore.edit { preferences ->
            preferences[LAST_REFRESH_TIMESTAMP] = System.currentTimeMillis()
        }
    }
    
    // Check if data refresh is needed based on interval
    suspend fun isDataRefreshNeeded(refreshIntervalMs: Long = DEFAULT_REFRESH_INTERVAL): Boolean {
        val lastRefresh = lastRefreshTimestamp.first()
        val currentTime = System.currentTimeMillis()
        
        return lastRefresh == 0L || (currentTime - lastRefresh) >= refreshIntervalMs
    }

    // Clear all cached data
    suspend fun clearCache() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 