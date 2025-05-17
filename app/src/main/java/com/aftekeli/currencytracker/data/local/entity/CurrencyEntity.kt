package com.aftekeli.currencytracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a currency's current price and data in the local database
 */
@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey
    val symbol: String,
    val price: Double,
    val priceUsd: Double? = null,
    val changePercent24h: Double? = null,
    val volume24h: Double? = null,
    val marketCap: Double? = null,
    val lastUpdateTime: Long = System.currentTimeMillis()
) 