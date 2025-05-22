package com.aftekeli.currencytracker.data.local.db.entity

import androidx.room.Entity

@Entity(
    tableName = "favorite_coins", 
    primaryKeys = ["userId", "symbol"]
)
data class FavoriteCoinEntity(
    val userId: String,
    val symbol: String,
    val timestamp: Long = System.currentTimeMillis()
) 