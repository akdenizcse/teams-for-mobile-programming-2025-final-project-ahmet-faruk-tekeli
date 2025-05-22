package com.aftekeli.currencytracker.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        // Send the current user initially
        trySend(firebaseAuth.currentUser)
        
        // Listen for auth state changes
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        
        firebaseAuth.addAuthStateListener(authStateListener)
        
        // Remove the listener when the flow is closed
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun changeUserPassword(currentPassword: String, newPassword: String): Result<Unit> {
        val user = firebaseAuth.currentUser ?: return Result.failure(Exception("User not logged in"))
        val email = user.email ?: return Result.failure(Exception("User email not available"))
        
        return try {
            // Create credentials with current email and password
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            
            // Re-authenticate user with current credentials
            user.reauthenticate(credential).await()
            
            // Update the password
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Different error messages for different exceptions
            val errorMessage = when {
                e.message?.contains("password is invalid", ignoreCase = true) == true -> 
                    "Current password is incorrect"
                e.message?.contains("weak password", ignoreCase = true) == true -> 
                    "New password is too weak. Please use at least 6 characters"
                else -> e.message ?: "Failed to change password"
            }
            Result.failure(Exception(errorMessage))
        }
    }
} 