package com.aftekeli.currencytracker.domain.model

import java.util.Date

data class WatchlistItem(
    val id: Long = 0,
    val userId: String,
    val symbol: String,
    val addedAt: Date = Date(),
    val isCrypto: Boolean = true,
    val notes: String = ""
) 