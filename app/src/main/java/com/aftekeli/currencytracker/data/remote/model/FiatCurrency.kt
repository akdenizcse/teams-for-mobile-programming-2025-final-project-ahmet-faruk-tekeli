package com.aftekeli.currencytracker.data.remote.model

import com.google.gson.annotations.SerializedName

data class FiatCurrency(
    @SerializedName("symbol")
    val symbol: String,
    
    @SerializedName("price")
    val price: String,
    
    val name: String = "",
    val country: String = "",
    val flag: String = ""
) {
    fun getCode(): String {
        // EURUSDT -> EUR
        return symbol.replace("USDT", "")
    }
    
    fun getFormattedPrice(): String {
        return try {
            val priceValue = price.toDouble()
            if (priceValue > 1.0) {
                "$${String.format("%,.2f", priceValue)}"
            } else {
                "$${String.format("%,.4f", priceValue)}"
            }
        } catch (e: Exception) {
            price
        }
    }
    
    fun getCountryFlag(): String {
        // Para birimi koduna göre ülke bayrağı emoji'si döndürür
        return when(getCode()) {
            "USD" -> "🇺🇸"
            "EUR" -> "🇪🇺"
            "GBP" -> "🇬🇧"
            "TRY" -> "🇹🇷"
            "JPY" -> "🇯🇵"
            "CNY" -> "🇨🇳"
            "RUB" -> "🇷🇺"
            "AUD" -> "🇦🇺"
            "CAD" -> "🇨🇦"
            "CHF" -> "🇨🇭"
            "SEK" -> "🇸🇪"
            "NOK" -> "🇳🇴"
            "DKK" -> "🇩🇰"
            "INR" -> "🇮🇳"
            "MXN" -> "🇲🇽"
            else -> "🏳️"
        }
    }
    
    fun getCurrencyName(): String {
        // Para birimi koduna göre isim döndürür
        return when(getCode()) {
            "USD" -> "US Dollar"
            "EUR" -> "Euro"
            "GBP" -> "British Pound"
            "TRY" -> "Turkish Lira"
            "JPY" -> "Japanese Yen"
            "CNY" -> "Chinese Yuan"
            "RUB" -> "Russian Ruble"
            "AUD" -> "Australian Dollar"
            "CAD" -> "Canadian Dollar"
            "CHF" -> "Swiss Franc"
            "SEK" -> "Swedish Krona"
            "NOK" -> "Norwegian Krone"
            "DKK" -> "Danish Krone"
            "INR" -> "Indian Rupee"
            "MXN" -> "Mexican Peso"
            else -> getCode()
        }
    }
    
    fun getCountryName(): String {
        // Para birimi koduna göre ülke ismi döndürür
        return when(getCode()) {
            "USD" -> "United States"
            "EUR" -> "European Union"
            "GBP" -> "United Kingdom"
            "TRY" -> "Turkey"
            "JPY" -> "Japan"
            "CNY" -> "China"
            "RUB" -> "Russia"
            "AUD" -> "Australia"
            "CAD" -> "Canada"
            "CHF" -> "Switzerland"
            "SEK" -> "Sweden"
            "NOK" -> "Norway"
            "DKK" -> "Denmark"
            "INR" -> "India"
            "MXN" -> "Mexico"
            else -> ""
        }
    }
} 