package com.aftekeli.currencytracker.data.repository

import android.util.Log
import com.aftekeli.currencytracker.data.local.db.dao.FavoriteCoinDao
import com.aftekeli.currencytracker.data.local.db.entity.FavoriteCoinEntity
import com.aftekeli.currencytracker.util.Result
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val favoriteCoinDao: FavoriteCoinDao
) : UserRepository {

    companion object {
        private const val TAG = "UserRepository"
    }

    private val usersCollection = firestore.collection("users")
    
    override suspend fun addCoinToFavorites(userId: String, coinSymbol: String): Result<Unit> {
        // First add to local Room database
        try {
            favoriteCoinDao.addFavorite(FavoriteCoinEntity(userId, coinSymbol))
        } catch (e: Exception) {
            Log.e(TAG, "Error adding favorite to local database", e)
            return Result.Error(e)
        }
        
        // Then update Firestore
        return try {
            val userDocument = usersCollection.document(userId)
            
            // Add symbol to favorite_symbols array
            userDocument.update("favorite_symbols", FieldValue.arrayUnion(coinSymbol)).await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            // If document doesn't exist, create it with the favorite symbol
            if (e.message?.contains("No document to update") == true) {
                try {
                    val userData = hashMapOf("favorite_symbols" to listOf(coinSymbol))
                    usersCollection.document(userId).set(userData).await()
                    
                    Result.Success(Unit)
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating new favorites document in Firestore", e)
                    Result.Error(e)
                }
            } else {
                Log.e(TAG, "Error updating favorites in Firestore", e)
                Result.Error(e)
            }
        }
    }
    
    override suspend fun removeCoinFromFavorites(userId: String, coinSymbol: String): Result<Unit> {
        // First remove from local Room database
        try {
            favoriteCoinDao.removeFavorite(userId, coinSymbol)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing favorite from local database", e)
            return Result.Error(e)
        }
        
        // Then update Firestore
        return try {
            val userDocument = usersCollection.document(userId)
            
            // Remove symbol from favorite_symbols array
            userDocument.update("favorite_symbols", FieldValue.arrayRemove(coinSymbol)).await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing favorite from Firestore", e)
            Result.Error(e)
        }
    }
    
    override fun getFavoriteCoinSymbols(userId: String): Flow<Result<List<String>>> {
        // Read from local Room database first
        return favoriteCoinDao.getFavoriteCoins(userId).map { entities ->
            Result.Success(entities.map { it.symbol })
        }
    }
    
    override fun isCoinFavorite(userId: String, coinSymbol: String): Flow<Boolean> {
        return favoriteCoinDao.isFavorite(userId, coinSymbol)
    }
    
    override suspend fun syncFavoritesFromFirestore(userId: String): Result<Unit> {
        return try {
            val userDocument = usersCollection.document(userId).get().await()
            
            if (userDocument.exists()) {
                val favoriteSymbols = userDocument.get("favorite_symbols") as? List<String> ?: emptyList()
                
                // Clear existing favorites for this user and add the ones from Firestore
                favoriteCoinDao.clearUserFavorites(userId)
                
                favoriteSymbols.forEach { symbol ->
                    favoriteCoinDao.addFavorite(FavoriteCoinEntity(userId, symbol))
                }
                
                Log.d(TAG, "Synced ${favoriteSymbols.size} favorites from Firestore to local database")
                Result.Success(Unit)
            } else {
                // Document doesn't exist, nothing to sync
                Log.d(TAG, "No favorites to sync from Firestore (document doesn't exist)")
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing favorites from Firestore", e)
            Result.Error(e)
        }
    }
} 