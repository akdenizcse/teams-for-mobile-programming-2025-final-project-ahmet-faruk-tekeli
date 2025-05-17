package com.aftekeli.currencytracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aftekeli.currencytracker.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(watchlistItem: WatchlistEntity): Long
    
    @Query("SELECT * FROM watchlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWatchlistByUserId(userId: String): Flow<List<WatchlistEntity>>
    
    @Query("SELECT symbol FROM watchlist WHERE userId = :userId")
    fun getWatchlistSymbolsByUserId(userId: String): Flow<List<String>>
    
    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE userId = :userId AND symbol = :symbol)")
    suspend fun isInWatchlist(userId: String, symbol: String): Boolean
    
    @Query("DELETE FROM watchlist WHERE userId = :userId AND symbol = :symbol")
    suspend fun removeFromWatchlist(userId: String, symbol: String)
    
    @Delete
    suspend fun deleteWatchlistItem(watchlistItem: WatchlistEntity)
    
    @Query("DELETE FROM watchlist WHERE userId = :userId")
    suspend fun clearWatchlistForUser(userId: String)
} 