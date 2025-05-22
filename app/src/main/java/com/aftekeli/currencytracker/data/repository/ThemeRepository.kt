package com.aftekeli.currencytracker.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Define an enum for theme settings
enum class ThemeSetting(val value: Int) {
    LIGHT(0),
    DARK(1),
    SYSTEM(2);
    
    companion object {
        fun fromValue(value: Int): ThemeSetting = when (value) {
            0 -> LIGHT
            1 -> DARK
            else -> SYSTEM
        }
    }
}

// Extension property for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeKey = intPreferencesKey("theme_setting")
    
    // Get the current theme setting as a Flow
    val themeSetting: Flow<ThemeSetting> = context.dataStore.data
        .map { preferences ->
            val themeValue = preferences[themeKey] ?: ThemeSetting.SYSTEM.value
            ThemeSetting.fromValue(themeValue)
        }
    
    // Set the theme setting
    suspend fun setThemeSetting(setting: ThemeSetting) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = setting.value
        }
    }
} 