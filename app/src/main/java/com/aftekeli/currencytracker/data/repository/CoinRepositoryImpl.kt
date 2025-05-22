package com.aftekeli.currencytracker.data.repository

import android.util.Log
import com.aftekeli.currencytracker.data.local.db.dao.CoinTickerDao
import com.aftekeli.currencytracker.data.local.db.entity.CoinTickerEntity
import com.aftekeli.currencytracker.data.local.db.mapper.toDomain
import com.aftekeli.currencytracker.data.local.db.mapper.toEntityList
import com.aftekeli.currencytracker.data.local.db.mapper.toDomainList
import com.aftekeli.currencytracker.data.model.ChartDataPoint
import com.aftekeli.currencytracker.data.model.Coin
import com.aftekeli.currencytracker.data.remote.api.BinanceApiService
import com.aftekeli.currencytracker.data.remote.dto.TickerDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinRepositoryImpl @Inject constructor(
    private val apiService: BinanceApiService,
    private val coinTickerDao: CoinTickerDao
) : CoinRepository {

    companion object {
        private const val TAG = "CoinRepository"
        private const val DB_TIMEOUT_MS = 3000L // 3 seconds timeout for database operations
    }

    override fun getMarketTickers(): Flow<Result<List<Coin>>> {
        return coinTickerDao.getAllTickers().map { entityList ->
            try {
                Result.success(entityList.toDomainList())
            } catch (e: Exception) {
                Log.e(TAG, "Error mapping ticker entities to domain", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshMarketTickers(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Refreshing market tickers from API")
                val response = apiService.getTicker24hr()
                
                if (response.isSuccessful) {
                    val tickers = response.body()
                    if (tickers == null) {
                        Log.e(TAG, "API returned null body")
                        return@withContext Result.failure(Exception("API returned empty response"))
                    }
                    
                    if (tickers.isEmpty()) {
                        Log.e(TAG, "API returned empty list")
                        return@withContext Result.failure(Exception("API returned empty list"))
                    }
                    
                    // Check if the coroutine has been cancelled
                    ensureActive()
                    
                    Log.d(TAG, "Received ${tickers.size} tickers")
                    
                    val filteredTickers = tickers.filter { it.symbol.endsWith("USDT") }
                    Log.d(TAG, "Filtered to ${filteredTickers.size} USDT pairs")
                    
                    // Check if major coins like BTC and ETH are present
                    val btcPresent = filteredTickers.any { it.symbol == "BTCUSDT" }
                    val ethPresent = filteredTickers.any { it.symbol == "ETHUSDT" }
                    Log.d(TAG, "Major coins present: BTC=$btcPresent, ETH=$ethPresent")
                    
                    if (filteredTickers.isEmpty()) {
                        Log.e(TAG, "No USDT pairs found")
                        return@withContext Result.failure(Exception("No USDT trading pairs found"))
                    }
                    
                    // Check if the coroutine has been cancelled
                    ensureActive()
                    
                    // Sort by quoteVolume instead of volume
                    val sortedTickers = filteredTickers.sortedByDescending { 
                        it.quoteVolume.toDoubleOrNull() ?: 0.0 
                    }.take(50) // Limit to top 50 by quote volume
                    
                    // Log the top 5 tickers by quoteVolume for debugging
                    sortedTickers.take(5).forEachIndexed { index, ticker ->
                        Log.d(TAG, "Top ${index + 1}: ${ticker.symbol} - Quote Volume: ${ticker.quoteVolume}")
                    }
                    
                    Log.d(TAG, "Sorted ${sortedTickers.size} tickers by quote volume in descending order")
                    
                    // Convert to entity and save to database
                    val tickerEntities = sortedTickers.toEntityList()
                    
                    // Check if the coroutine has been cancelled
                    ensureActive()
                    
                    // Use try-catch to handle database operations separately
                    try {
                        coinTickerDao.insertAllTickers(tickerEntities)
                        Log.d(TAG, "Saved ${tickerEntities.size} ticker entities to database")
                        Result.success(Unit)
                    } catch (e: Exception) {
                        if (e is CancellationException) {
                            throw e // Re-throw cancellation exceptions
                        }
                        Log.e(TAG, "Database error while saving tickers", e)
                        Result.failure(e)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "API error: ${response.code()} ${response.message()} - $errorBody")
                    Result.failure(Exception("API error: ${response.code()} ${response.message()} - $errorBody"))
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d(TAG, "refreshMarketTickers operation was cancelled")
                    throw e // Re-throw cancellation exceptions to properly cancel the coroutine
                }
                Log.e(TAG, "Exception in refreshMarketTickers", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getHistoricalData(symbol: String, interval: String, limit: Int): Result<List<ChartDataPoint>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getKlines(symbol, interval, limit)
                
                if (response.isSuccessful) {
                    val klines = response.body() ?: emptyList()
                    
                    val chartDataPoints = klines.mapNotNull { kline ->
                        try {
                            // Binance kline format: 
                            // [0] = Open time, [1] = Open, [2] = High, [3] = Low, [4] = Close, [5] = Volume, ...
                            val openTime = (kline[0] as? Double)?.toLong() ?: (kline[0] as? Long) ?: return@mapNotNull null
                            val closePrice = (kline[4] as? String)?.toFloatOrNull() ?: return@mapNotNull null
                            
                            ChartDataPoint(openTime, closePrice)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    Result.success(chartDataPoints)
                } else {
                    Result.failure(Exception("API error: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getCoinDetail(symbol: String): Result<Coin> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting coin details for $symbol")
                // First try to get from local database with a timeout to prevent UI hanging
                val localCoinEntity = withTimeoutOrNull(DB_TIMEOUT_MS) {
                    coinTickerDao.getTickerBySymbol(symbol).firstOrNull()
                }
                
                // If found in local database, return it
                if (localCoinEntity != null) {
                    Log.d(TAG, "Found coin $symbol in local database")
                    return@withContext Result.success(localCoinEntity.toDomain())
                }
                
                Log.d(TAG, "Coin $symbol not in database, fetching from API")
                // Otherwise fetch from API
                val response = apiService.getTicker24hr()
                
                if (response.isSuccessful) {
                    val tickers = response.body() ?: emptyList()
                    val ticker = tickers.find { it.symbol == symbol }
                    
                    if (ticker != null) {
                        // Map to domain model
                        val baseAsset = ticker.symbol.removeSuffix("USDT")
                        val quoteAsset = "USDT"
                        
                        // Create the coin entity to save to database
                        val coinEntity = CoinTickerEntity(
                            symbol = ticker.symbol,
                            baseAsset = baseAsset,
                            quoteAsset = quoteAsset,
                            lastPrice = ticker.lastPrice,
                            priceChangePercent = ticker.priceChangePercent,
                            volume = ticker.volume,
                            quoteVolume = ticker.quoteVolume,
                            high24h = ticker.highPrice,
                            low24h = ticker.lowPrice,
                            timestamp = System.currentTimeMillis()
                        )
                        
                        // Save to database for future use
                        coinTickerDao.insertTicker(coinEntity)
                        
                        // Convert to domain model
                        val coin = coinEntity.toDomain()
                        
                        Log.d(TAG, "Successfully fetched and saved coin $symbol")
                        return@withContext Result.success(coin)
                    } else {
                        Log.e(TAG, "Coin not found in API response: $symbol")
                        return@withContext Result.failure(Exception("Coin not found: $symbol"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e(TAG, "API error: ${response.code()} ${response.message()} - $errorBody")
                    return@withContext Result.failure(Exception("API error: ${response.code()} ${response.message()} - $errorBody"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in getCoinDetail for symbol $symbol", e)
                return@withContext Result.failure(e)
            }
        }
    }
} 