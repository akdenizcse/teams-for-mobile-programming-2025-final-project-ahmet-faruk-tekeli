package com.aftekeli.currencytracker.di

import com.aftekeli.currencytracker.data.repository.CurrencyConverterRepositoryImpl
import com.aftekeli.currencytracker.data.repository.CurrencyRepositoryImpl
import com.aftekeli.currencytracker.domain.repository.CurrencyConverterRepository
import com.aftekeli.currencytracker.domain.repository.CurrencyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(
        currencyRepositoryImpl: CurrencyRepositoryImpl
    ): CurrencyRepository
    
    @Binds
    @Singleton
    abstract fun bindCurrencyConverterRepository(
        currencyConverterRepositoryImpl: CurrencyConverterRepositoryImpl
    ): CurrencyConverterRepository
} 