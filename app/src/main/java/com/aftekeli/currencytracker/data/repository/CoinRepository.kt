package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.ChartDataPoint
import com.aftekeli.currencytracker.data.model.Coin
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun getMarketTickers(): Flow<Result<List<Coin>>>
    suspend fun refreshMarketTickers(): Result<Unit>
    suspend fun getHistoricalData(symbol: String, interval: String, limit: Int): Result<List<ChartDataPoint>>
    suspend fun getCoinDetail(symbol: String): Result<Coin>
} 