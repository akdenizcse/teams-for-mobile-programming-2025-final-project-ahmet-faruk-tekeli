package com.aftekeli.currencytracker.domain.model

import java.util.Date

data class Alert(
    val id: Long = 0,
    val userId: String,
    val symbol: String,
    val targetPrice: Double,
    val isAboveTarget: Boolean, // true if alert triggers when price goes above target
    val isActive: Boolean = true,
    val createdAt: Date = Date()
) 