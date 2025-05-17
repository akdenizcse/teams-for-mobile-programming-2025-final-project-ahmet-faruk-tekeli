package com.aftekeli.currencytracker.data.remote.model

import com.google.gson.annotations.SerializedName

data class CryptoCurrency(
    @SerializedName("symbol")
    val symbol: String,
    
    @SerializedName("price")
    val price: String,
    
    @SerializedName("priceChangePercent")
    val priceChangePercent: String,
    
    @SerializedName("weightedAvgPrice")
    val weightedAvgPrice: String,
    
    @SerializedName("lastQty")
    val lastQty: String,
    
    @SerializedName("bidPrice")
    val bidPrice: String,
    
    @SerializedName("askPrice")
    val askPrice: String,
    
    @SerializedName("volume")
    val volume: String,
    
    @SerializedName("quoteVolume")
    val quoteVolume: String
) {
    // UI'da göstermek için ek özellikler
    fun getName(): String {
        // Symbol'den isim elde etme (BTCUSDT -> BTC)
        return if (symbol.endsWith("USDT")) {
            symbol.removeSuffix("USDT")
        } else {
            symbol
        }
    }
    
    fun getFormattedPrice(): String {
        return try {
            val priceValue = price.toDouble()
            if (priceValue > 1.0) {
                "$${String.format("%,.2f", priceValue)}"
            } else {
                "$${String.format("%,.6f", priceValue)}"
            }
        } catch (e: Exception) {
            "$${price}"
        }
    }
    
    fun getFormattedPriceChange(): String {
        return try {
            val changeValue = priceChangePercent.toDouble()
            if (changeValue >= 0) {
                "+${String.format("%.2f", changeValue)}%"
            } else {
                "${String.format("%.2f", changeValue)}%"
            }
        } catch (e: Exception) {
            "${priceChangePercent}%"
        }
    }
    
    fun isPriceUp(): Boolean {
        return try {
            priceChangePercent.toDouble() >= 0
        } catch (e: Exception) {
            false
        }
    }
} 