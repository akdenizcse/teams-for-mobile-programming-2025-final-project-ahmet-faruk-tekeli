package com.aftekeli.currencytracker.data.remote.dto

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

// Supported currencies list (simple string array)
typealias CoinGeckoSupportedCurrenciesDto = List<String>

// Basic coin info from /coins/list
data class CoinGeckoCoinDto(
    val id: String,
    val symbol: String,
    val name: String,
    @SerializedName("market_data")
    val marketData: CoinGeckoMarketDataDto? = null,
    val image: CoinGeckoImageDto? = null,
    val description: JsonObject? = null
)

// Coin images
data class CoinGeckoImageDto(
    val thumb: String? = null,
    val small: String? = null,
    val large: String? = null
)

// Market data for a coin
data class CoinGeckoMarketDataDto(
    @SerializedName("current_price")
    val currentPrice: Map<String, Double>? = null,
    @SerializedName("market_cap")
    val marketCap: Map<String, Double>? = null,
    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Double? = null,
    @SerializedName("price_change_percentage_7d")
    val priceChangePercentage7d: Double? = null
)

// Market data from /coins/markets
data class CoinGeckoMarketDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    @SerializedName("current_price")
    val currentPrice: Double,
    @SerializedName("market_cap")
    val marketCap: Double,
    @SerializedName("market_cap_rank")
    val marketCapRank: Int,
    @SerializedName("price_change_percentage_24h")
    val priceChangePercentage24h: Double?,
    @SerializedName("total_volume")
    val totalVolume: Double
)

// Price data is a map (coin ID to map of currencies to prices)
typealias CoinGeckoPriceDto = Map<String, Map<String, Double>> 