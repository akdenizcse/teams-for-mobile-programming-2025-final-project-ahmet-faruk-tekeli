package com.aftekeli.currencytracker.data.remote.api

import com.aftekeli.currencytracker.data.remote.dto.CoinGeckoMarketDto
import com.aftekeli.currencytracker.data.remote.dto.CoinGeckoPriceDto
import com.aftekeli.currencytracker.data.remote.dto.CoinGeckoCoinDto
import com.aftekeli.currencytracker.data.remote.dto.CoinGeckoSupportedCurrenciesDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApiService {
    companion object {
        const val BASE_URL = "https://api.coingecko.com/api/v3/"
        
        // Common currency IDs
        val COMMON_CRYPTOS = listOf(
            "bitcoin", "ethereum", "ripple", "litecoin", "cardano",
            "polkadot", "dogecoin", "tether", "solana", "binancecoin",
            "tron", "chainlink", "stellar", "usd-coin", "matic-network"
        )
        
        // Common fiat currencies
        val COMMON_FIATS = listOf(
            "usd", "eur", "gbp", "jpy", "try", "cad", "aud", "rub", 
            "cny", "inr", "brl", "zar", "krw", "sgd"
        )
    }

    // Get list of supported currencies (both crypto and fiat)
    @GET("simple/supported_vs_currencies")
    suspend fun getSupportedVsCurrencies(): Response<CoinGeckoSupportedCurrenciesDto>
    
    // Get list of coins with basic info
    @GET("coins/list")
    suspend fun getCoinsList(): Response<List<CoinGeckoCoinDto>>
    
    // Get price data for specific coins in specific currencies
    @GET("simple/price")
    suspend fun getPrice(
        @Query("ids") coinIds: String,
        @Query("vs_currencies") vsCurrencies: String,
        @Query("include_market_cap") includeMarketCap: Boolean = true,
        @Query("include_24hr_vol") include24hrVol: Boolean = true,
        @Query("include_24hr_change") include24hrChange: Boolean = true
    ): Response<CoinGeckoPriceDto>
    
    // Get detailed market data for multiple coins
    @GET("coins/markets")
    suspend fun getCoinsMarkets(
        @Query("vs_currency") vsCurrency: String,
        @Query("ids") ids: String? = null,
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): Response<List<CoinGeckoMarketDto>>
    
    // Get single coin data
    @GET("coins/{id}")
    suspend fun getCoinById(
        @Path("id") id: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false
    ): Response<CoinGeckoCoinDto>
} 