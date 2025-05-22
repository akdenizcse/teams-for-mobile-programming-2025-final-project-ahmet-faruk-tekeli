package com.aftekeli.currencytracker.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coin_tickers")
data class CoinTickerEntity(
    @PrimaryKey
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val lastPrice: String,
    val priceChangePercent: String,
    val volume: String,
    val quoteVolume: String,
    val high24h: String,
    val low24h: String,
    val openPrice: String = "0.0",
    val weightedAvgPrice: String = "0.0",
    val priceChange: String = "0.0",
    val count: Long = 0,
    val bidPrice: String = "0.0",
    val askPrice: String = "0.0",
    val timestamp: Long = System.currentTimeMillis()
) 