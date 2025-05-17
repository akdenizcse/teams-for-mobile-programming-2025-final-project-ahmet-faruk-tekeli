package com.aftekeli.currencytracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a price alert set by a user
 */
@Entity(tableName = "price_alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val symbol: String,
    val targetPrice: Double,
    val isAboveTarget: Boolean, // true if alert triggers when price goes above target
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) 