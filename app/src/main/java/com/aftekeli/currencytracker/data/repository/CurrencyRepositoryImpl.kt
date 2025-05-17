package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.local.dao.AlertDao
import com.aftekeli.currencytracker.data.local.dao.CurrencyDao
import com.aftekeli.currencytracker.data.local.dao.WatchlistDao
import com.aftekeli.currencytracker.data.local.entity.AlertEntity
import com.aftekeli.currencytracker.data.local.entity.CurrencyEntity
import com.aftekeli.currencytracker.data.local.entity.WatchlistEntity
import com.aftekeli.currencytracker.data.remote.api.BinanceApiService
import com.aftekeli.currencytracker.domain.model.Alert
import com.aftekeli.currencytracker.domain.model.Candlestick
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.domain.model.WatchlistItem
import com.aftekeli.currencytracker.domain.repository.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val binanceApiService: BinanceApiService,
    private val currencyDao: CurrencyDao,
    private val watchlistDao: WatchlistDao,
    private val alertDao: AlertDao
) : CurrencyRepository {
    
    // Currency operations
    override suspend fun getAllCurrencies(): Flow<List<Currency>> {
        return currencyDao.getAllCurrencies().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getCurrencyBySymbol(symbol: String): Flow<Currency?> {
        return flow {
            val currency = currencyDao.getCurrencyBySymbol(symbol)
            emit(currency?.toDomainModel())
        }
    }
    
    override suspend fun refreshCurrencies() {
        try {
            val prices = binanceApiService.getAllPrices()
            val currencyEntities = prices.map {
                CurrencyEntity(
                    symbol = it.symbol,
                    price = it.price.toDoubleOrNull() ?: 0.0,
                    lastUpdateTime = System.currentTimeMillis()
                )
            }
            currencyDao.insertCurrencies(currencyEntities)
        } catch (e: Exception) {
            // Handle error
            e.printStackTrace()
        }
    }
    
    override suspend fun refreshCurrency(symbol: String) {
        try {
            val price = binanceApiService.getPriceBySymbol(symbol)
            val currencyEntity = CurrencyEntity(
                symbol = price.symbol,
                price = price.price.toDoubleOrNull() ?: 0.0,
                lastUpdateTime = System.currentTimeMillis()
            )
            currencyDao.insertCurrency(currencyEntity)
        } catch (e: Exception) {
            // Handle error
            e.printStackTrace()
        }
    }
    
    // Candlestick chart data
    override suspend fun getCandlestickData(symbol: String, interval: String): Flow<List<Candlestick>> {
        return flow {
            try {
                val candlestickData = binanceApiService.getCandlestickData(symbol, interval)
                val candlesticks = candlestickData.map {
                    Candlestick(
                        openTime = Date((it[0] as Number).toLong()),
                        open = (it[1] as String).toDouble(),
                        high = (it[2] as String).toDouble(),
                        low = (it[3] as String).toDouble(),
                        close = (it[4] as String).toDouble(),
                        volume = (it[5] as String).toDouble(),
                        closeTime = Date((it[6] as Number).toLong()),
                        quoteAssetVolume = (it[7] as String).toDouble(),
                        numberOfTrades = (it[8] as Number).toInt()
                    )
                }
                emit(candlesticks)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(emptyList())
            }
        }
    }
    
    // Watchlist operations
    override suspend fun getWatchlist(userId: String): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlistByUserId(userId).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getWatchlistSymbols(userId: String): Flow<List<String>> {
        return watchlistDao.getWatchlistSymbolsByUserId(userId)
    }
    
    override suspend fun addToWatchlist(userId: String, symbol: String) {
        val watchlistItem = WatchlistEntity(
            userId = userId,
            symbol = symbol,
            addedAt = System.currentTimeMillis()
        )
        watchlistDao.insertWatchlistItem(watchlistItem)
    }
    
    override suspend fun removeFromWatchlist(userId: String, symbol: String) {
        watchlistDao.removeFromWatchlist(userId, symbol)
    }
    
    override suspend fun isInWatchlist(userId: String, symbol: String): Boolean {
        return watchlistDao.isInWatchlist(userId, symbol)
    }
    
    // Alert operations
    override suspend fun getAlerts(userId: String): Flow<List<Alert>> {
        return alertDao.getAllAlertsByUserId(userId).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getActiveAlerts(userId: String): Flow<List<Alert>> {
        return alertDao.getActiveAlertsByUserId(userId).map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }
    
    override suspend fun createAlert(userId: String, symbol: String, targetPrice: Double, isAboveTarget: Boolean): Long {
        val alertEntity = AlertEntity(
            userId = userId,
            symbol = symbol,
            targetPrice = targetPrice,
            isAboveTarget = isAboveTarget,
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
        return alertDao.insertAlert(alertEntity)
    }
    
    override suspend fun updateAlert(alert: Alert) {
        val alertEntity = AlertEntity(
            id = alert.id,
            userId = alert.userId,
            symbol = alert.symbol,
            targetPrice = alert.targetPrice,
            isAboveTarget = alert.isAboveTarget,
            isActive = alert.isActive,
            createdAt = alert.createdAt.time
        )
        alertDao.updateAlert(alertEntity)
    }
    
    override suspend fun deleteAlert(alertId: Long) {
        alertDao.deleteAlertById(alertId)
    }
    
    override suspend fun setAlertActive(alertId: Long, isActive: Boolean) {
        alertDao.updateAlertActiveStatus(alertId, isActive)
    }
    
    // Helper extension functions for mapping between entity and domain models
    private fun CurrencyEntity.toDomainModel(): Currency {
        return Currency(
            symbol = symbol,
            price = price,
            priceUsd = priceUsd,
            changePercent24h = changePercent24h,
            volume24h = volume24h,
            marketCap = marketCap,
            lastUpdateTime = lastUpdateTime
        )
    }
    
    private fun WatchlistEntity.toDomainModel(): WatchlistItem {
        return WatchlistItem(
            id = id,
            userId = userId,
            symbol = symbol,
            addedAt = Date(addedAt)
        )
    }
    
    private fun AlertEntity.toDomainModel(): Alert {
        return Alert(
            id = id,
            userId = userId,
            symbol = symbol,
            targetPrice = targetPrice,
            isAboveTarget = isAboveTarget,
            isActive = isActive,
            createdAt = Date(createdAt)
        )
    }
} 