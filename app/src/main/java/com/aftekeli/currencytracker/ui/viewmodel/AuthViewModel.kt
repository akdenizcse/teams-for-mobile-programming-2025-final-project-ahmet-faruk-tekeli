package com.aftekeli.currencytracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

data class PasswordChangeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val authState: StateFlow<AuthUiState> = _authState
    
    private val _passwordChangeState = MutableStateFlow(PasswordChangeState())
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()
    
    // This will hold the current Firebase user
    val currentUser = authRepository.currentUserFlow
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun loginUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)
                _authState.value = if (result.isSuccess) {
                    AuthUiState.Success("Login successful")
                } else {
                    AuthUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun registerUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthUiState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = authRepository.register(email, password)
                _authState.value = if (result.isSuccess) {
                    AuthUiState.Success("Registration successful")
                } else {
                    AuthUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                authRepository.logout()
                _authState.value = AuthUiState.Initial
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Failed to logout")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthUiState.Error("Email cannot be empty")
            return
        }
        
        _authState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = authRepository.sendPasswordResetEmail(email)
                _authState.value = if (result.isSuccess) {
                    AuthUiState.Success("Password reset email sent successfully")
                } else {
                    AuthUiState.Error(result.exceptionOrNull()?.message ?: "Failed to send password reset email")
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        // Reset the password change state
        _passwordChangeState.value = PasswordChangeState(isLoading = true)
        
        // Validate inputs
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _passwordChangeState.value = PasswordChangeState(error = "All fields are required")
            return
        }
        
        if (newPassword != confirmPassword) {
            _passwordChangeState.value = PasswordChangeState(error = "New passwords don't match")
            return
        }
        
        if (newPassword.length < 6) {
            _passwordChangeState.value = PasswordChangeState(error = "Password must be at least 6 characters")
            return
        }
        
        viewModelScope.launch {
            try {
                val result = authRepository.changeUserPassword(currentPassword, newPassword)
                if (result.isSuccess) {
                    _passwordChangeState.value = PasswordChangeState(isSuccess = true)
                } else {
                    _passwordChangeState.value = PasswordChangeState(
                        error = result.exceptionOrNull()?.message ?: "Failed to change password"
                    )
                }
            } catch (e: Exception) {
                _passwordChangeState.value = PasswordChangeState(error = e.message ?: "Unknown error")
            }
        }
    }
    
    fun resetPasswordChangeState() {
        _passwordChangeState.value = PasswordChangeState()
    }

    fun resetAuthState() {
        _authState.value = AuthUiState.Initial
    }
} 