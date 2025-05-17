package com.aftekeli.currencytracker.domain.model

data class UserPreferences(
    val defaultCurrency: String = "USD",
    val theme: String = "system",
    val refreshInterval: Int = 5,
    val notificationSettings: Map<String, Boolean> = mapOf(
        "priceAlerts" to true,
        "newsAlerts" to false
    )
) 