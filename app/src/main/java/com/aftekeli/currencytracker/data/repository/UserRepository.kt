package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.util.Result
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun addCoinToFavorites(userId: String, coinSymbol: String): Result<Unit>
    suspend fun removeCoinFromFavorites(userId: String, coinSymbol: String): Result<Unit>
    fun getFavoriteCoinSymbols(userId: String): Flow<Result<List<String>>>
    fun isCoinFavorite(userId: String, coinSymbol: String): Flow<Boolean>
    suspend fun syncFavoritesFromFirestore(userId: String): Result<Unit>
} 