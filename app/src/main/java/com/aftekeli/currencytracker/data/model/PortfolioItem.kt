package com.aftekeli.currencytracker.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Kullanıcı portföyündeki bir kripto varlığını temsil eden model
 */
data class PortfolioItem(
    @DocumentId
    val id: String = "",
    
    @PropertyName("userId")
    val userId: String = "",
    
    @PropertyName("symbol")
    val symbol: String = "",  // Örn: "BTCUSDT"
    
    @PropertyName("baseAsset")
    val baseAsset: String = "", // Örn: "BTC"
    
    @PropertyName("amount")
    val amount: Double = 0.0,  // Sahip olunan miktar
    
    @PropertyName("averageBuyPrice")
    val averageBuyPrice: Double = 0.0, // Ortalama alım fiyatı
    
    @PropertyName("totalInvested")
    val totalInvested: Double = 0.0, // Toplam yatırılan miktar (USDT)
    
    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Canlı fiyata göre güncel değeri hesaplar
     */
    fun getCurrentValue(currentPrice: Double): Double {
        return amount * currentPrice
    }
    
    /**
     * Canlı fiyata göre kar/zarar hesaplar
     */
    fun getProfitLoss(currentPrice: Double): Double {
        val currentValue = getCurrentValue(currentPrice)
        return currentValue - totalInvested
    }
    
    /**
     * Canlı fiyata göre kar/zarar yüzdesini hesaplar
     */
    fun getProfitLossPercentage(currentPrice: Double): Double {
        if (totalInvested == 0.0) return 0.0
        return (getProfitLoss(currentPrice) / totalInvested) * 100
    }
} 