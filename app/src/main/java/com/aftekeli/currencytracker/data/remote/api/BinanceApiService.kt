package com.aftekeli.currencytracker.data.remote.api

import com.aftekeli.currencytracker.data.remote.dto.TickerDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApiService {
    @GET("api/v3/ticker/24hr")
    suspend fun getTicker24hr(): Response<List<TickerDto>>
    
    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int
    ): Response<List<List<Any>>>
} 