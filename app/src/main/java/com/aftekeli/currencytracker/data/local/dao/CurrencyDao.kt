package com.aftekeli.currencytracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aftekeli.currencytracker.data.local.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: CurrencyEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>)
    
    @Query("SELECT * FROM currencies WHERE symbol = :symbol")
    suspend fun getCurrencyBySymbol(symbol: String): CurrencyEntity?
    
    @Query("SELECT * FROM currencies ORDER BY symbol ASC")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>
    
    @Query("SELECT * FROM currencies WHERE symbol IN (:symbols)")
    fun getCurrenciesBySymbols(symbols: List<String>): Flow<List<CurrencyEntity>>
    
    @Query("DELETE FROM currencies WHERE symbol = :symbol")
    suspend fun deleteCurrency(symbol: String)
    
    @Query("DELETE FROM currencies")
    suspend fun deleteAllCurrencies()
} 