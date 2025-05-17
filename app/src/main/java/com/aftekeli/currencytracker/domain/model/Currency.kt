package com.aftekeli.currencytracker.domain.model

data class Currency(
    val symbol: String,
    val name: String = "", // Currency name (optional)
    val price: Double,
    val priceUsd: Double? = null, // Price in USD
    val changePercent24h: Double? = null, // Price change percentage in 24h
    val volume24h: Double? = null, // Trading volume in 24h
    val marketCap: Double? = null, // Market capitalization
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false // Whether this currency is in user's watchlist
) 