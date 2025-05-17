package com.aftekeli.currencytracker.data.remote.firestore

import android.util.Log
import com.aftekeli.currencytracker.domain.model.Alert
import com.aftekeli.currencytracker.domain.model.WatchlistItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirestoreService"

@Singleton
class FirestoreService @Inject constructor() {
    
    private val firestore = FirebaseFirestore.getInstance()
    
    init {
        // Basit ayarları kullan, deprecated metotları kullanmadan
        val settings = FirebaseFirestoreSettings.Builder().build()
        firestore.firestoreSettings = settings
        
        Log.d(TAG, "FirestoreService initialized")
    }
    
    // ===== KULLANICI PROFİLİ İŞLEMLERİ =====
    
    suspend fun createOrUpdateUser(userId: String, data: Map<String, Any>) {
        try {
            firestore.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "User profile updated for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile: ${e.message}")
            throw e
        }
    }
    
    suspend fun getUserData(userId: String): Map<String, Any>? {
        return try {
            val document = firestore.collection("users").document(userId)
                .get()
                .await()
            
            if (document.exists()) {
                document.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user data: ${e.message}")
            null
        }
    }
    
    // ===== KULLANICI TERCİHLERİ =====
    
    suspend fun updateUserPreferences(userId: String, preferences: Map<String, Any>) {
        try {
            firestore.collection("users").document(userId)
                .collection("preferences").document("userPrefs")
                .set(preferences, SetOptions.merge())
                .await()
            Log.d(TAG, "User preferences updated for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user preferences: ${e.message}")
            throw e
        }
    }
    
    fun getUserPreferences(userId: String): Flow<Map<String, Any>?> = callbackFlow {
        val listenerRegistration = firestore.collection("users").document(userId)
            .collection("preferences").document("userPrefs")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening for user preferences: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.data)
                } else {
                    trySend(null)
                }
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    // ===== WATCHLIST İŞLEMLERİ =====
    
    suspend fun addToWatchlist(userId: String, symbol: String, isCrypto: Boolean = true, notes: String = "") {
        try {
            val watchlistItem = hashMapOf(
                "symbol" to symbol,
                "addedAt" to Date(),
                "isCrypto" to isCrypto,
                "notes" to notes
            )
            
            firestore.collection("users").document(userId)
                .collection("watchlist").document(symbol)
                .set(watchlistItem)
                .await()
            
            Log.d(TAG, "Added $symbol to watchlist for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to watchlist: ${e.message}")
            throw e
        }
    }
    
    suspend fun updateWatchlistNotes(userId: String, symbol: String, notes: String) {
        try {
            firestore.collection("users").document(userId)
                .collection("watchlist").document(symbol)
                .update("notes", notes)
                .await()
            Log.d(TAG, "Updated notes for $symbol")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating watchlist notes: ${e.message}")
            throw e
        }
    }
    
    suspend fun removeFromWatchlist(userId: String, symbol: String) {
        try {
            firestore.collection("users").document(userId)
                .collection("watchlist").document(symbol)
                .delete()
                .await()
            
            Log.d(TAG, "Removed $symbol from watchlist for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing from watchlist: ${e.message}")
            throw e
        }
    }
    
    suspend fun isInWatchlist(userId: String, symbol: String): Boolean {
        return try {
            val document = firestore.collection("users").document(userId)
                .collection("watchlist").document(symbol)
                .get()
                .await()
            
            document.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking watchlist: ${e.message}")
            false
        }
    }
    
    fun getWatchlist(userId: String): Flow<List<WatchlistItem>> = callbackFlow {
        val listenerRegistration = firestore.collection("users").document(userId)
            .collection("watchlist")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening for watchlist: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                val watchlistItems = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val symbol = doc.getString("symbol") ?: doc.id
                        val addedAtTimestamp = doc.getTimestamp("addedAt")
                        val addedAt = addedAtTimestamp?.toDate() ?: Date()
                        val isCrypto = doc.getBoolean("isCrypto") ?: true
                        val notes = doc.getString("notes") ?: ""
                        
                        WatchlistItem(
                            id = doc.id.hashCode().toLong(),
                            userId = userId,
                            symbol = symbol,
                            addedAt = addedAt,
                            isCrypto = isCrypto,
                            notes = notes
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing watchlist item: ${e.message}")
                        null
                    }
                } ?: emptyList()
                
                trySend(watchlistItems)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    // ===== ALARM İŞLEMLERİ =====
    
    suspend fun createAlert(userId: String, symbol: String, targetPrice: Double, isAboveTarget: Boolean): String {
        try {
            val alertData = hashMapOf(
                "symbol" to symbol,
                "targetPrice" to targetPrice,
                "isAboveTarget" to isAboveTarget,
                "isActive" to true,
                "createdAt" to Date(),
                "notificationSent" to false
            )
            
            val alertRef = firestore.collection("users").document(userId)
                .collection("alerts").document()
            
            alertRef.set(alertData).await()
            return alertRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating alert: ${e.message}")
            throw e
        }
    }
    
    suspend fun updateAlert(userId: String, alertId: String, isActive: Boolean) {
        try {
            firestore.collection("users").document(userId)
                .collection("alerts").document(alertId)
                .update("isActive", isActive)
                .await()
            
            Log.d(TAG, "Alert $alertId updated, isActive=$isActive")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating alert: ${e.message}")
            throw e
        }
    }
    
    suspend fun deleteAlert(userId: String, alertId: String) {
        try {
            firestore.collection("users").document(userId)
                .collection("alerts").document(alertId)
                .delete()
                .await()
            
            Log.d(TAG, "Alert $alertId deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting alert: ${e.message}")
            throw e
        }
    }
    
    fun getAlerts(userId: String): Flow<List<Alert>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("alerts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening for alerts: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                val alerts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val symbol = doc.getString("symbol") ?: return@mapNotNull null
                        val targetPrice = doc.getDouble("targetPrice") ?: return@mapNotNull null
                        val isAboveTarget = doc.getBoolean("isAboveTarget") ?: false
                        val isActive = doc.getBoolean("isActive") ?: true
                        val createdAtTimestamp = doc.getTimestamp("createdAt")
                        val createdAt = createdAtTimestamp?.toDate() ?: Date()
                        
                        Alert(
                            id = doc.id.hashCode().toLong(),
                            userId = userId,
                            symbol = symbol,
                            targetPrice = targetPrice,
                            isAboveTarget = isAboveTarget,
                            isActive = isActive,
                            createdAt = createdAt
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing alert: ${e.message}")
                        null
                    }
                } ?: emptyList()
                
                trySend(alerts)
            }
        
        awaitClose { listener.remove() }
    }
    
    fun getActiveAlerts(userId: String): Flow<List<Alert>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("alerts")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening for active alerts: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                val alerts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val symbol = doc.getString("symbol") ?: return@mapNotNull null
                        val targetPrice = doc.getDouble("targetPrice") ?: return@mapNotNull null
                        val isAboveTarget = doc.getBoolean("isAboveTarget") ?: false
                        val createdAtTimestamp = doc.getTimestamp("createdAt")
                        val createdAt = createdAtTimestamp?.toDate() ?: Date()
                        
                        Alert(
                            id = doc.id.hashCode().toLong(),
                            userId = userId,
                            symbol = symbol,
                            targetPrice = targetPrice,
                            isAboveTarget = isAboveTarget,
                            isActive = true,
                            createdAt = createdAt
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing active alert: ${e.message}")
                        null
                    }
                } ?: emptyList()
                
                trySend(alerts)
            }
        
        awaitClose { listener.remove() }
    }
    
    // ===== DÖVİZ DÖNÜŞÜM GEÇMİŞİ =====
    
    suspend fun saveConversion(
        userId: String, 
        fromCurrency: String, 
        toCurrency: String, 
        fromAmount: Double, 
        toAmount: Double, 
        rate: Double
    ): String {
        try {
            val conversionData = hashMapOf(
                "fromCurrency" to fromCurrency,
                "toCurrency" to toCurrency,
                "fromAmount" to fromAmount,
                "toAmount" to toAmount,
                "rate" to rate,
                "timestamp" to Date()
            )
            
            val conversionRef = firestore.collection("users").document(userId)
                .collection("conversions").document()
            
            conversionRef.set(conversionData).await()
            Log.d(TAG, "Saved conversion from $fromCurrency to $toCurrency for user $userId")
            return conversionRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error saving conversion: ${e.message}")
            throw e
        }
    }
    
    fun getConversionHistory(userId: String, limit: Int = 20): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("conversions")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening for conversion history: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                val conversions = snapshot?.documents?.mapNotNull { doc ->
                    doc.data
                } ?: emptyList()
                
                trySend(conversions)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun clearConversionHistory(userId: String) {
        try {
            val batch = firestore.batch()
            val snapshot = firestore.collection("users").document(userId)
                .collection("conversions")
                .limit(500) // Firestore'da batch işlemi limiti 500
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                return
            }
            
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            batch.commit().await()
            Log.d(TAG, "Cleared conversion history for user $userId")
            
            // 500'den fazla belge olabilir, tekrar kontrol et ve sil
            if (snapshot.size() == 500) {
                clearConversionHistory(userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing conversion history: ${e.message}")
            throw e
        }
    }
    
    // ===== ARAMA GEÇMİŞİ =====
    
    suspend fun addSearchQuery(userId: String, query: String, category: String = "all") {
        try {
            val searchData = hashMapOf(
                "query" to query,
                "category" to category,
                "timestamp" to Date()
            )
            
            firestore.collection("users").document(userId)
                .collection("searchHistory").document()
                .set(searchData)
                .await()
            
            Log.d(TAG, "Search query saved: $query")
        } catch (e: Exception) {
            // Arama geçmişi kritik olmadığı için sessizce hata geçelim
            Log.w(TAG, "Error adding search query: ${e.message}")
        }
    }
    
    fun getRecentSearches(userId: String, limit: Int = 10): Flow<List<Map<String, Any>>> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .collection("searchHistory")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening for search history: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                val searches = snapshot?.documents?.mapNotNull { doc ->
                    doc.data
                } ?: emptyList()
                
                trySend(searches)
            }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun clearSearchHistory(userId: String) {
        try {
            val batch = firestore.batch()
            val snapshot = firestore.collection("users").document(userId)
                .collection("searchHistory")
                .limit(500) // Firestore batch limiti
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                return
            }
            
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            batch.commit().await()
            Log.d(TAG, "Cleared search history for user $userId")
            
            // 500'den fazla belge varsa tekrar çağır
            if (snapshot.size() == 500) {
                clearSearchHistory(userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing search history: ${e.message}")
            throw e
        }
    }
} 