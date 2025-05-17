package com.aftekeli.currencytracker.data.mapper

import com.aftekeli.currencytracker.data.remote.dto.CoinGeckoCoinDto
import com.aftekeli.currencytracker.data.remote.dto.CoinGeckoMarketDto
import com.aftekeli.currencytracker.domain.model.CurrencyInfo

/**
 * Maps CoinGecko API responses to domain models
 */
object CoinGeckoMapper {
    
    /**
     * Maps a list of supported fiat currencies from CoinGecko to domain model
     */
    fun mapFiatCurrencies(supportedVsCurrencies: List<String>): List<CurrencyInfo> {
        // Convert lowercase symbols to readable format and mark as fiat
        return supportedVsCurrencies.map { currencyCode ->
            CurrencyInfo(
                id = currencyCode,
                symbol = currencyCode.uppercase(),
                name = getCurrencyName(currencyCode),
                isFiat = true
            )
        }
    }
    
    /**
     * Maps a list of coin DTOs to domain model
     */
    fun mapCryptoCurrencies(coins: List<CoinGeckoCoinDto>): List<CurrencyInfo> {
        return coins.map { coin ->
            CurrencyInfo(
                id = coin.id,
                symbol = coin.symbol.uppercase(),
                name = coin.name,
                isFiat = false,
                imageUrl = coin.image?.thumb
            )
        }
    }
    
    /**
     * Maps market data to domain model with prices
     */
    fun mapMarketDataToCurrencies(markets: List<CoinGeckoMarketDto>, vsCurrency: String): List<CurrencyInfo> {
        return markets.map { market ->
            CurrencyInfo(
                id = market.id,
                symbol = market.symbol.uppercase(),
                name = market.name,
                isFiat = false,
                imageUrl = market.image,
                currentPrice = market.currentPrice
            )
        }
    }
    
    /**
     * Get currency name from code
     */
    private fun getCurrencyName(code: String): String {
        return when (code.lowercase()) {
            "usd" -> "US Dollar"
            "eur" -> "Euro"
            "gbp" -> "British Pound"
            "jpy" -> "Japanese Yen"
            "try" -> "Turkish Lira"
            "rub" -> "Russian Ruble"
            "cny" -> "Chinese Yuan"
            "inr" -> "Indian Rupee"
            "brl" -> "Brazilian Real"
            "aud" -> "Australian Dollar"
            "cad" -> "Canadian Dollar"
            "chf" -> "Swiss Franc"
            "krw" -> "South Korean Won"
            "zar" -> "South African Rand"
            else -> code.uppercase()
        }
    }
} 