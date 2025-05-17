package com.aftekeli.currencytracker.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CoinListItemDto(
    val id: String,
    val symbol: String,
    val name: String
)

data class CoinDetailDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: CoinImageDto,
    val market_data: MarketDataDto?
)

data class CoinImageDto(
    val thumb: String,
    val small: String,
    val large: String
)

data class MarketDataDto(
    @SerializedName("current_price")
    val currentPrice: Map<String, Double>,
    
    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Double?
) 