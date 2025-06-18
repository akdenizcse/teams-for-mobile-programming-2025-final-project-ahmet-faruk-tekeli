package com.aftekeli.currencytracker.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Alım-satım işlem çeşitleri
 */
enum class TransactionType {
    BUY, SELL
}

/**
 * Bir alım-satım işlemini temsil eden model
 */
data class Transaction(
    @DocumentId
    val id: String = "",
    
    @PropertyName("userId")
    val userId: String = "",
    
    @PropertyName("symbol")
    val symbol: String = "",  // Örn: "BTCUSDT"
    
    @PropertyName("baseAsset") 
    val baseAsset: String = "", // Örn: "BTC"
    
    @PropertyName("quoteAsset")
    val quoteAsset: String = "USDT", // Varsayılan olarak USDT
    
    @PropertyName("type")
    val type: TransactionType = TransactionType.BUY,
    
    @PropertyName("amount")
    val amount: Double = 0.0,  // İşlem miktarı (coin)
    
    @PropertyName("price")
    val price: Double = 0.0,   // İşlem fiyatı
    
    @PropertyName("totalValue")
    val totalValue: Double = 0.0, // Toplam değer (USDT cinsinden)
    
    @PropertyName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun createBuyTransaction(
            userId: String,
            symbol: String,
            baseAsset: String,
            quoteAsset: String = "USDT",
            amount: Double,
            price: Double
        ): Transaction {
            val total = amount * price
            return Transaction(
                userId = userId,
                symbol = symbol,
                baseAsset = baseAsset, 
                quoteAsset = quoteAsset,
                type = TransactionType.BUY,
                amount = amount,
                price = price,
                totalValue = total
            )
        }
        
        fun createSellTransaction(
            userId: String,
            symbol: String,
            baseAsset: String,
            quoteAsset: String = "USDT",
            amount: Double,
            price: Double
        ): Transaction {
            val total = amount * price
            return Transaction(
                userId = userId,
                symbol = symbol,
                baseAsset = baseAsset,
                quoteAsset = quoteAsset,
                type = TransactionType.SELL,
                amount = amount,
                price = price,
                totalValue = total
            )
        }
    }
} 