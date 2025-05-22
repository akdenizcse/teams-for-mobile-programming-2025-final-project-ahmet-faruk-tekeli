package com.aftekeli.currencytracker.data.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun register(email: String, password: String): Result<FirebaseUser>
    fun logout()
    fun getCurrentUser(): FirebaseUser?
    
    // Password reset functionality
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    // Change password functionality
    suspend fun changeUserPassword(currentPassword: String, newPassword: String): Result<Unit>
    
    // Flow to observe the current user
    val currentUserFlow: Flow<FirebaseUser?>
} 