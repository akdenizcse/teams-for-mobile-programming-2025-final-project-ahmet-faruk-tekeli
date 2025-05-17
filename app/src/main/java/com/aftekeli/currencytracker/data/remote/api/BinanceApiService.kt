package com.aftekeli.currencytracker.data.remote.api

import com.aftekeli.currencytracker.data.remote.dto.PriceDto
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApiService {
    
    @GET("api/v3/ticker/price")
    suspend fun getAllPrices(): List<PriceDto>
    
    @GET("api/v3/ticker/price")
    suspend fun getPriceBySymbol(@Query("symbol") symbol: String): PriceDto
    
    @GET("api/v3/klines")
    suspend fun getCandlestickData(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int = 500
    ): List<List<Any>>
    
    companion object {
        const val BASE_URL = "https://api.binance.com/"
        
        // Common cryptocurrency symbols paired with USDT
        val SUPPORTED_CRYPTO_SYMBOLS = listOf(
            "BTCUSDT", "ETHUSDT", "BNBUSDT", "XRPUSDT", "ADAUSDT",
            "DOGEUSDT", "SOLUSDT", "DOTUSDT", "SHIBUSDT", "MATICUSDT",
            "LTCUSDT", "AVAXUSDT", "LINKUSDT", "UNIUSDT", "ATOMUSDT"
        )
        
        // Fiat currency symbols paired with BTC or USDT
        val SUPPORTED_FIAT_SYMBOLS = listOf(
            "BTCEUR", "BTCGBP", "BTCAUD", "BTCBRL",
            "BTCRUB", "BTCTRY", "BTCUAH", "EURUSDT",
            "GBPUSDT", "USDTBRL", "USDTRUB"
        )
        
        // Available candlestick intervals
        val INTERVALS = mapOf(
            "1m" to "1 minute",
            "3m" to "3 minutes",
            "5m" to "5 minutes",
            "15m" to "15 minutes",
            "30m" to "30 minutes",
            "1h" to "1 hour",
            "2h" to "2 hours",
            "4h" to "4 hours",
            "6h" to "6 hours",
            "8h" to "8 hours",
            "12h" to "12 hours",
            "1d" to "1 day",
            "3d" to "3 days",
            "1w" to "1 week",
            "1M" to "1 month"
        )
    }
} 