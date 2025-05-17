package com.aftekeli.currencytracker.di

import android.content.Context
import com.aftekeli.currencytracker.data.local.CurrencyDatabase
import com.aftekeli.currencytracker.data.local.dao.AlertDao
import com.aftekeli.currencytracker.data.local.dao.CurrencyDao
import com.aftekeli.currencytracker.data.local.dao.WatchlistDao
import com.aftekeli.currencytracker.data.remote.api.BinanceApiService
import com.aftekeli.currencytracker.data.repository.CurrencyRepositoryImpl
import com.aftekeli.currencytracker.domain.repository.CurrencyRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Network dependencies
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder().create()
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBinanceApiService(okHttpClient: OkHttpClient, gson: Gson): BinanceApiService {
        return Retrofit.Builder()
            .baseUrl(BinanceApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(BinanceApiService::class.java)
    }
    
    // Database dependencies
    
    @Provides
    @Singleton
    fun provideCurrencyDatabase(@ApplicationContext context: Context): CurrencyDatabase {
        return CurrencyDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideCurrencyDao(database: CurrencyDatabase): CurrencyDao {
        return database.currencyDao()
    }
    
    @Provides
    @Singleton
    fun provideWatchlistDao(database: CurrencyDatabase): WatchlistDao {
        return database.watchlistDao()
    }
    
    @Provides
    @Singleton
    fun provideAlertDao(database: CurrencyDatabase): AlertDao {
        return database.alertDao()
    }
    
    // Repository dependencies
    
    @Provides
    @Singleton
    fun provideCurrencyRepository(
        binanceApiService: BinanceApiService,
        currencyDao: CurrencyDao,
        watchlistDao: WatchlistDao,
        alertDao: AlertDao
    ): CurrencyRepository {
        return CurrencyRepositoryImpl(
            binanceApiService = binanceApiService,
            currencyDao = currencyDao,
            watchlistDao = watchlistDao,
            alertDao = alertDao
        )
    }
} 