package com.aftekeli.currencytracker.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aftekeli.currencytracker.data.local.db.entity.CoinTickerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinTickerDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTickers(tickers: List<CoinTickerEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicker(ticker: CoinTickerEntity)
    
    @Query("SELECT * FROM coin_tickers ORDER BY quoteVolume DESC")
    fun getAllTickers(): Flow<List<CoinTickerEntity>>
    
    @Query("SELECT * FROM coin_tickers WHERE symbol = :symbol")
    fun getTickerBySymbol(symbol: String): Flow<CoinTickerEntity?>
    
    @Query("SELECT * FROM coin_tickers WHERE symbol IN (:symbols)")
    fun getTickersBySymbols(symbols: List<String>): Flow<List<CoinTickerEntity>>
    
    @Query("DELETE FROM coin_tickers")
    suspend fun clearAllTickers()
} 