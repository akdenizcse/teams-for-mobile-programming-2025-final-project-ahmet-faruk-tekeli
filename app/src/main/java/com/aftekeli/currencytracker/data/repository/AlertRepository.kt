package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.PriceAlert
import com.aftekeli.currencytracker.util.Result
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    suspend fun addPriceAlert(userId: String, alertData: Map<String, Any>): Result<String>
    suspend fun deletePriceAlert(userId: String, alertId: String): Result<Unit>
    fun getPriceAlerts(userId: String): Flow<Result<List<PriceAlert>>>
} 