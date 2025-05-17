package com.aftekeli.currencytracker.domain.model

// Currency information with support for both crypto and fiat
data class CurrencyInfo(
    val id: String,          // CoinGecko ID: bitcoin, ethereum, usd, eur
    val symbol: String,      // Short symbol: BTC, ETH, USD, EUR
    val name: String,        // Full name: Bitcoin, Ethereum, US Dollar
    val isFiat: Boolean,     // Whether it's a fiat currency
    val imageUrl: String? = null,  // Image URL (mostly for cryptocurrencies)
    val currentPrice: Double? = null  // Current price in base currency if available
)

// Conversion rate between two currencies
data class ConversionRate(
    val fromCurrency: CurrencyInfo,
    val toCurrency: CurrencyInfo,
    val rate: Double,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun convert(amount: Double): Double = amount * rate
    
    // Whether the rate is expired (older than 1 minute)
    fun isExpired(): Boolean = System.currentTimeMillis() - lastUpdated > 60_000
}

// Result of a conversion operation
sealed class ConversionResult {
    data class Success(
        val fromAmount: Double,
        val toAmount: Double,
        val rate: ConversionRate
    ) : ConversionResult()
    
    data class Loading(val isInitialLoad: Boolean = false) : ConversionResult()
    
    data class Error(val message: String) : ConversionResult()
} 