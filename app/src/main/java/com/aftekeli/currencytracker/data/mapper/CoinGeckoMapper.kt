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
                isFiat = true,
                imageUrl = getFiatImageUrl(currencyCode)
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
    fun mapMarketDataToCurrencies(markets: List<CoinGeckoMarketDto>): List<CurrencyInfo> {
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
     * Get fiat currency image URL
     */
    private fun getFiatImageUrl(code: String): String {
        return when (code.lowercase()) {
            "usd" -> "https://wise.com/public-resources/assets/flags/rectangle/usd.png" 
            "eur" -> "https://wise.com/public-resources/assets/flags/rectangle/eur.png"
            "gbp" -> "https://wise.com/public-resources/assets/flags/rectangle/gbp.png"
            "jpy" -> "https://wise.com/public-resources/assets/flags/rectangle/jpy.png"
            "aud" -> "https://wise.com/public-resources/assets/flags/rectangle/aud.png"
            "cad" -> "https://wise.com/public-resources/assets/flags/rectangle/cad.png"
            "chf" -> "https://wise.com/public-resources/assets/flags/rectangle/chf.png"
            "cny" -> "https://wise.com/public-resources/assets/flags/rectangle/cny.png"
            "try" -> "https://wise.com/public-resources/assets/flags/rectangle/try.png"
            "rub" -> "https://wise.com/public-resources/assets/flags/rectangle/rub.png"
            "inr" -> "https://wise.com/public-resources/assets/flags/rectangle/inr.png"
            "brl" -> "https://wise.com/public-resources/assets/flags/rectangle/brl.png"
            "krw" -> "https://wise.com/public-resources/assets/flags/rectangle/krw.png"
            "idr" -> "https://wise.com/public-resources/assets/flags/rectangle/idr.png"
            "hkd" -> "https://wise.com/public-resources/assets/flags/rectangle/hkd.png"
            "mxn" -> "https://wise.com/public-resources/assets/flags/rectangle/mxn.png"
            "sgd" -> "https://wise.com/public-resources/assets/flags/rectangle/sgd.png"
            "zar" -> "https://wise.com/public-resources/assets/flags/rectangle/zar.png"
            "sek" -> "https://wise.com/public-resources/assets/flags/rectangle/sek.png"
            "nok" -> "https://wise.com/public-resources/assets/flags/rectangle/nok.png"
            "nzd" -> "https://wise.com/public-resources/assets/flags/rectangle/nzd.png"
            "pln" -> "https://wise.com/public-resources/assets/flags/rectangle/pln.png"
            "uah" -> "https://wise.com/public-resources/assets/flags/rectangle/uah.png"
            "ngn" -> "https://wise.com/public-resources/assets/flags/rectangle/ngn.png"
            "ars" -> "https://wise.com/public-resources/assets/flags/rectangle/ars.png"
            "vnd" -> "https://wise.com/public-resources/assets/flags/rectangle/vnd.png"
            else -> "" // Default: empty string if no logo found
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