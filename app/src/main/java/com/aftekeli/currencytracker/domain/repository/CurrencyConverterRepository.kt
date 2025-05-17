package com.aftekeli.currencytracker.domain.repository

import com.aftekeli.currencytracker.domain.model.ConversionRate
import com.aftekeli.currencytracker.domain.model.CurrencyInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for the currency converter feature
 */
interface CurrencyConverterRepository {
    
    /**
     * Get all available crypto currencies
     */
    suspend fun getCryptoCurrencies(): Flow<Result<List<CurrencyInfo>>>
    
    /**
     * Get all available fiat currencies
     */
    suspend fun getFiatCurrencies(): Flow<Result<List<CurrencyInfo>>>
    
    /**
     * Get conversion rate between two currencies
     */
    suspend fun getConversionRate(
        fromCurrencyId: String,
        toCurrencyId: String
    ): Flow<Result<ConversionRate>>
    
    /**
     * Get detailed information about a specific currency
     */
    suspend fun getCurrencyInfo(currencyId: String): Flow<Result<CurrencyInfo>>
    
    /**
     * Get list of popular/common cryptocurrencies
     */
    suspend fun getPopularCryptoCurrencies(): Flow<Result<List<CurrencyInfo>>>
    
    /**
     * Get list of popular/common fiat currencies
     */
    suspend fun getPopularFiatCurrencies(): Flow<Result<List<CurrencyInfo>>>
} 