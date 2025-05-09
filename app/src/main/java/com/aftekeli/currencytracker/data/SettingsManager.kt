package com.aftekeli.currencytracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for Context to get the DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    // Preference keys
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val PRICE_ALERTS_ENABLED = booleanPreferencesKey("price_alerts_enabled")
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

    // Clear all cached data
    suspend fun clearCache() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 