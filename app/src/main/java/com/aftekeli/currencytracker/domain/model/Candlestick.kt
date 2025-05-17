package com.aftekeli.currencytracker.domain.model

import java.util.Date

data class Candlestick(
    val openTime: Date,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val closeTime: Date,
    val quoteAssetVolume: Double,
    val numberOfTrades: Int
) 