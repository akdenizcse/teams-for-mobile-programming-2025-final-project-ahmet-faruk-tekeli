package com.aftekeli.currencytracker.data.model

data class Coin(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val lastPrice: Double,
    val priceChangePercent: Double,
    val volume: Double,
    val quoteVolume: Double,
    val high24h: Double,
    val low24h: Double,
    val openPrice: Double = 0.0,
    val weightedAvgPrice: Double = 0.0,
    val priceChange: Double = 0.0,
    val count: Long = 0,
    val bidPrice: Double = 0.0,
    val askPrice: Double = 0.0
) 