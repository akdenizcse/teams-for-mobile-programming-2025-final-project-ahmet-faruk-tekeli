package com.aftekeli.currencytracker.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aftekeli.currencytracker.data.local.db.dao.CoinTickerDao
import com.aftekeli.currencytracker.data.local.db.dao.FavoriteCoinDao
import com.aftekeli.currencytracker.data.local.db.entity.CoinTickerEntity
import com.aftekeli.currencytracker.data.local.db.entity.FavoriteCoinEntity

@Database(
    entities = [
        CoinTickerEntity::class,
        FavoriteCoinEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coinTickerDao(): CoinTickerDao
    abstract fun favoriteCoinDao(): FavoriteCoinDao
} 