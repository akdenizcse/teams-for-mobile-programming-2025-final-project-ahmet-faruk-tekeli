package com.aftekeli.currencytracker.domain.repository

import com.aftekeli.currencytracker.domain.model.Alert
import com.aftekeli.currencytracker.domain.model.Candlestick
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.domain.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    
    // Currency operations
    suspend fun getAllCurrencies(): Flow<List<Currency>>
    suspend fun getCurrencyBySymbol(symbol: String): Flow<Currency?>
    suspend fun refreshCurrencies()
    suspend fun refreshCurrency(symbol: String)
    
    // Candlestick chart data
    suspend fun getCandlestickData(symbol: String, interval: String): Flow<List<Candlestick>>
    
    // Watchlist operations
    suspend fun getWatchlist(userId: String): Flow<List<WatchlistItem>>
    suspend fun getWatchlistSymbols(userId: String): Flow<List<String>>
    suspend fun addToWatchlist(userId: String, symbol: String)
    suspend fun removeFromWatchlist(userId: String, symbol: String)
    suspend fun isInWatchlist(userId: String, symbol: String): Boolean
    
    // Alert operations
    suspend fun getAlerts(userId: String): Flow<List<Alert>>
    suspend fun getActiveAlerts(userId: String): Flow<List<Alert>>
    suspend fun createAlert(userId: String, symbol: String, targetPrice: Double, isAboveTarget: Boolean): Long
    suspend fun updateAlert(alert: Alert)
    suspend fun deleteAlert(alertId: Long)
    suspend fun setAlertActive(alertId: Long, isActive: Boolean)
} 