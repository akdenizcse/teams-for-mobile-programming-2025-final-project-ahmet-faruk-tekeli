package com.aftekeli.currencytracker.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.repository.CurrencyRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: CurrencyRepositoryImpl
) : ViewModel() {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState
    
    // Kayıt olma
    fun register(email: String, password: String, displayName: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _registerState.value = RegisterState.Error("E-posta ve şifre gereklidir")
            return
        }
        
        if (password.length < 6) {
            _registerState.value = RegisterState.Error("Şifre en az 6 karakter olmalıdır")
            return
        }
        
        _registerState.value = RegisterState.Loading
        
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                result.user?.let { user ->
                    // Kullanıcı görünen adını güncelle
                    if (displayName.isNotEmpty()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
                        
                        user.updateProfile(profileUpdates).await()
                    }
                    
                    // Firestore'a kullanıcı bilgilerini kaydet
                    repository.saveUserToFirestore(user.uid, email, displayName)
                    
                    _registerState.value = RegisterState.Success(user)
                } ?: run {
                    _registerState.value = RegisterState.Error("Kayıt başarısız oldu, tekrar deneyin")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }
    
    // Durum sıfırlama
    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

// Kayıt durumu için sealed class
sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: FirebaseUser) : RegisterState()
    data class Error(val message: String) : RegisterState()
} 