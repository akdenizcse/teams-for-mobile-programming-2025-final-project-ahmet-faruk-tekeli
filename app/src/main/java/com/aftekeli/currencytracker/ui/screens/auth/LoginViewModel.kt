package com.aftekeli.currencytracker.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.repository.CurrencyRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: CurrencyRepositoryImpl
) : ViewModel() {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    
    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState
    
    // Giriş yapma
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = LoginState.Error("E-posta ve şifre gereklidir")
            return
        }
        
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                
                result.user?.let { user ->
                    // Firestore'da kullanıcının son giriş zamanını güncelle
                    repository.updateUserLastLogin(user.uid)
                    
                    _loginState.value = LoginState.Success(user)
                } ?: run {
                    _loginState.value = LoginState.Error("Giriş başarısız oldu, tekrar deneyin")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }
    
    // Şifre sıfırlama e-postası gönderme
    fun sendPasswordResetEmail(email: String) {
        if (email.isEmpty()) {
            _resetPasswordState.value = ResetPasswordState.Error("E-posta adresi gereklidir")
            return
        }
        
        _resetPasswordState.value = ResetPasswordState.Loading
        
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _resetPasswordState.value = ResetPasswordState.Success
            } catch (e: Exception) {
                _resetPasswordState.value = ResetPasswordState.Error(e.message ?: "E-posta gönderimi başarısız oldu")
            }
        }
    }
    
    // Durum sıfırlama
    fun resetState() {
        _loginState.value = LoginState.Idle
        _resetPasswordState.value = ResetPasswordState.Idle
    }
}

// Giriş durumu için sealed class
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: FirebaseUser) : LoginState()
    data class Error(val message: String) : LoginState()
}

// Şifre sıfırlama durumu için sealed class
sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
} 