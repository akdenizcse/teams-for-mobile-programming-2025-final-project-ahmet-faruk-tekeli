package com.aftekeli.currencytracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aftekeli.currencytracker.data.local.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long
    
    @Update
    suspend fun updateAlert(alert: AlertEntity)
    
    @Delete
    suspend fun deleteAlert(alert: AlertEntity)
    
    @Query("DELETE FROM price_alerts WHERE id = :alertId")
    suspend fun deleteAlertById(alertId: Long)
    
    @Query("SELECT * FROM price_alerts WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveAlertsByUserId(userId: String): Flow<List<AlertEntity>>
    
    @Query("SELECT * FROM price_alerts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllAlertsByUserId(userId: String): Flow<List<AlertEntity>>
    
    @Query("SELECT * FROM price_alerts WHERE id = :alertId")
    suspend fun getAlertById(alertId: Long): AlertEntity?
    
    @Query("SELECT * FROM price_alerts WHERE userId = :userId AND symbol = :symbol")
    fun getAlertsBySymbol(userId: String, symbol: String): Flow<List<AlertEntity>>
    
    @Query("UPDATE price_alerts SET isActive = :isActive WHERE id = :alertId")
    suspend fun updateAlertActiveStatus(alertId: Long, isActive: Boolean)
    
    @Query("DELETE FROM price_alerts WHERE userId = :userId")
    suspend fun deleteAllAlertsForUser(userId: String)
} 