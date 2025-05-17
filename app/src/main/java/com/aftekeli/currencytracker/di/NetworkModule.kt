package com.aftekeli.currencytracker.di

import com.aftekeli.currencytracker.data.remote.api.BinanceApiService
import com.aftekeli.currencytracker.data.remote.api.CoinGeckoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply { 
            level = HttpLoggingInterceptor.Level.BODY 
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Singleton
    @Provides
    fun provideBinanceApiService(okHttpClient: OkHttpClient): BinanceApiService {
        return Retrofit.Builder()
            .baseUrl(BinanceApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BinanceApiService::class.java)
    }
    
    @Singleton
    @Provides
    fun provideCoinGeckoApiService(okHttpClient: OkHttpClient): CoinGeckoApiService {
        // Create a custom OkHttpClient with CoinGecko API key
        val coinGeckoClient = okHttpClient.newBuilder()
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val originalUrl = original.url
                
                // Add API key as query parameter
                val url = originalUrl.newBuilder()
                    .addQueryParameter("x_cg_demo_api_key", "CG-AXSs2eJR3m9cn9j3ftH5bj1r")
                    .build()
                
                val request = original.newBuilder()
                    .url(url)
                    .build()
                
                chain.proceed(request)
            })
            .build()
            
        return Retrofit.Builder()
            .baseUrl(CoinGeckoApiService.BASE_URL)
            .client(coinGeckoClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApiService::class.java)
    }
} 