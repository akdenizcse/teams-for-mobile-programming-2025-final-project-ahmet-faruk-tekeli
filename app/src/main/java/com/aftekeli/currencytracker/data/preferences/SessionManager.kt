package com.aftekeli.currencytracker.data.preferences

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SessionManager, kullanıcı oturum durumunu ve kimliğini yöneten bir yardımcı sınıftır.
 * Firebase Auth ile entegre çalışarak oturum durumunu takip eder.
 */
@Singleton
class SessionManager @Inject constructor() {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    
    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(firebaseAuth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    init {
        // Firebase Auth durumunu dinle
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
            _isLoggedIn.value = auth.currentUser != null
        }
    }
    
    /**
     * Mevcut kimliği doğrulanmış kullanıcının UID'sini döndürür.
     * Eğer oturum açılmamışsa null döndürür.
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
    
    /**
     * Mevcut kimliği doğrulanmış kullanıcının email adresini döndürür.
     * Eğer oturum açılmamışsa null döndürür.
     */
    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }
    
    /**
     * Mevcut kullanıcı için ID token'ını alır.
     * Bu token, API isteklerinde kimlik doğrulama için kullanılabilir.
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        return try {
            firebaseAuth.currentUser?.getIdToken(forceRefresh)?.result?.token
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Kullanıcı oturumunu sonlandırır.
     */
    fun signOut() {
        firebaseAuth.signOut()
    }
} 