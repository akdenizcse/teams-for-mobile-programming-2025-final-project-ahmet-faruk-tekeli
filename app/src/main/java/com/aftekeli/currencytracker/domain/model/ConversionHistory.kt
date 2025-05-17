package com.aftekeli.currencytracker.domain.model

import java.util.Date

data class ConversionHistory(
    val id: String = "",
    val userId: String,
    val fromCurrency: String,
    val toCurrency: String,
    val fromAmount: Double,
    val toAmount: Double,
    val rate: Double,
    val timestamp: Date = Date()
) 