package com.aftekeli.currencytracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aftekeli.currencytracker.data.local.dao.AlertDao
import com.aftekeli.currencytracker.data.local.dao.CurrencyDao
import com.aftekeli.currencytracker.data.local.dao.WatchlistDao
import com.aftekeli.currencytracker.data.local.entity.AlertEntity
import com.aftekeli.currencytracker.data.local.entity.CurrencyEntity
import com.aftekeli.currencytracker.data.local.entity.WatchlistEntity

@Database(
    entities = [
        CurrencyEntity::class,
        WatchlistEntity::class,
        AlertEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CurrencyDatabase : RoomDatabase() {
    
    abstract fun currencyDao(): CurrencyDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun alertDao(): AlertDao
    
    companion object {
        private const val DATABASE_NAME = "currency_database"
        
        @Volatile
        private var INSTANCE: CurrencyDatabase? = null
        
        fun getInstance(context: Context): CurrencyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CurrencyDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
} 