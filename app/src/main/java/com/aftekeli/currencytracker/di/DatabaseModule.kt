package com.aftekeli.currencytracker.di

import android.content.Context
import androidx.room.Room
import com.aftekeli.currencytracker.data.local.db.AppDatabase
import com.aftekeli.currencytracker.data.local.db.dao.CoinTickerDao
import com.aftekeli.currencytracker.data.local.db.dao.FavoriteCoinDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "currency_tracker_db"
        )
            .fallbackToDestructiveMigration() // For development, replace with proper migrations in production
            .build()
    }

    @Provides
    fun provideCoinTickerDao(appDatabase: AppDatabase): CoinTickerDao {
        return appDatabase.coinTickerDao()
    }

    @Provides
    fun provideFavoriteCoinDao(appDatabase: AppDatabase): FavoriteCoinDao {
        return appDatabase.favoriteCoinDao()
    }
} 