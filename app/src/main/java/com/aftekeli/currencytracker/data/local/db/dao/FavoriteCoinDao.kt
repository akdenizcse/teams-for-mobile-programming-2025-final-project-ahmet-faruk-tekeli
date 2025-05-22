package com.aftekeli.currencytracker.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aftekeli.currencytracker.data.local.db.entity.FavoriteCoinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCoinDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteCoinEntity)
    
    @Query("DELETE FROM favorite_coins WHERE userId = :userId AND symbol = :symbol")
    suspend fun removeFavorite(userId: String, symbol: String)
    
    @Query("SELECT * FROM favorite_coins WHERE userId = :userId")
    fun getFavoriteCoins(userId: String): Flow<List<FavoriteCoinEntity>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_coins WHERE userId = :userId AND symbol = :symbol LIMIT 1)")
    fun isFavorite(userId: String, symbol: String): Flow<Boolean>
    
    @Query("DELETE FROM favorite_coins WHERE userId = :userId")
    suspend fun clearUserFavorites(userId: String)
} 