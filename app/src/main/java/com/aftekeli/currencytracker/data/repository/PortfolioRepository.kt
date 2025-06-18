package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.PortfolioItem
import kotlinx.coroutines.flow.Flow

/**
 * Kullanıcının coin portföyünü yönetmek için repository arayüzü
 */
interface PortfolioRepository {
    /**
     * Kullanıcının tüm portföyünü izler
     */
    fun getUserPortfolio(userId: String): Flow<List<PortfolioItem>>
    
    /**
     * Belirli bir coinin portföy bilgisini izler
     */
    fun getPortfolioItem(userId: String, symbol: String): Flow<PortfolioItem?>
    
    /**
     * Portföye yeni bir coin ekler veya mevcut coini günceller
     */
    suspend fun createOrUpdatePortfolioItem(item: PortfolioItem): Result<PortfolioItem>
    
    /**
     * Portföye coin alır (miktar ve fiyat ile)
     */
    suspend fun buyCoin(
        userId: String,
        symbol: String,
        baseAsset: String,
        amount: Double,
        price: Double
    ): Result<PortfolioItem>
    
    /**
     * Portföyden coin satar (miktar ve fiyat ile)
     */
    suspend fun sellCoin(
        userId: String,
        symbol: String,
        baseAsset: String,
        amount: Double,
        price: Double
    ): Result<PortfolioItem>
} 