package com.aftekeli.currencytracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.repository.AuthRepository
import com.aftekeli.currencytracker.data.repository.ThemeRepository
import com.aftekeli.currencytracker.data.repository.ThemeSetting
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        observeThemeSetting()
    }

    private fun loadUserProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        try {
            val currentUser = authRepository.getCurrentUser()
            _uiState.update { 
                it.copy(
                    userEmail = currentUser?.email,
                    isLoading = false
                ) 
            }
        } catch (e: Exception) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}" 
                ) 
            }
        }
    }

    private fun observeThemeSetting() {
        viewModelScope.launch {
            themeRepository.themeSetting.collect { setting ->
                _uiState.update { it.copy(themeSetting = setting) }
            }
        }
    }

    fun setThemeSetting(setting: ThemeSetting) {
        viewModelScope.launch {
            try {
                themeRepository.setThemeSetting(setting)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to update theme: ${e.message}")
                }
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch {
            try {
                authRepository.logout()
                _uiState.update { it.copy(isLoading = false) }
                onLogoutComplete()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Logout failed: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ProfileUiState(
    val userEmail: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val themeSetting: ThemeSetting = ThemeSetting.SYSTEM
) 