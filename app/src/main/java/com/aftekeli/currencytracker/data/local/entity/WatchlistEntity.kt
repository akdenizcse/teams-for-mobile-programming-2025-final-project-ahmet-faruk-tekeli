package com.aftekeli.currencytracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a currency added to a user's watchlist
 */
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val symbol: String,
    val addedAt: Long = System.currentTimeMillis()
) 