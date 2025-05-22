package com.aftekeli.currencytracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.local.db.dao.CoinTickerDao
import com.aftekeli.currencytracker.data.local.db.mapper.toDomain
import com.aftekeli.currencytracker.data.local.db.mapper.toDomainList
import com.aftekeli.currencytracker.data.model.Coin
import com.aftekeli.currencytracker.data.repository.CoinRepository
import com.aftekeli.currencytracker.data.repository.UserRepository
import com.aftekeli.currencytracker.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val coinRepository: CoinRepository,
    private val coinTickerDao: CoinTickerDao,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    private var refreshJob: Job? = null

    init {
        observeFavorites()
        syncFavoritesFromFirestore()
    }

    private fun observeFavorites() {
        val userId = auth.currentUser?.uid
        
        if (userId == null) {
            Log.d(TAG, "User not logged in")
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = "You must be logged in to view favorites"
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting to observe favorites for user $userId")
                userRepository.getFavoriteCoinSymbols(userId)
                    .catch { exception ->
                        Log.e(TAG, "Error getting favorite symbols", exception)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = "Failed to load favorites: ${exception.message}"
                            )
                        }
                    }
                    .collectLatest { favoriteSymbolsResult ->
                        when (favoriteSymbolsResult) {
                            is Result.Success -> {
                                val symbols = favoriteSymbolsResult.data
                                Log.d(TAG, "Got ${symbols.size} favorite symbols")
                                
                                if (symbols.isEmpty()) {
                                    _uiState.update { 
                                        it.copy(
                                            isLoading = false,
                                            isRefreshing = false,
                                            noFavorites = true,
                                            favoriteMarketData = emptyList(),
                                            lastUpdated = System.currentTimeMillis()
                                        )
                                    }
                                } else {
                                    // Observe data from Room
                                    observeFavoriteCoinsData(symbols)
                                }
                            }
                            is Result.Error -> {
                                Log.e(TAG, "Error in favorite symbols result", favoriteSymbolsResult.exception)
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        isRefreshing = false,
                                        errorMessage = favoriteSymbolsResult.exception.message ?: "Failed to load favorites"
                                    )
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in observeFavorites", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Error loading favorites: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun observeFavoriteCoinsData(symbols: List<String>) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting to observe market data for ${symbols.size} favorite coins")
                coinTickerDao.getTickersBySymbols(symbols)
                    .catch { exception ->
                        Log.e(TAG, "Error getting ticker data for favorites", exception)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = "Failed to load market data: ${exception.message}"
                            )
                        }
                    }
                    .collectLatest { entities ->
                        val favoriteCoins = entities.toDomainList()
                        Log.d(TAG, "Got ${favoriteCoins.size} favorite coins from database")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                noFavorites = favoriteCoins.isEmpty(),
                                favoriteMarketData = favoriteCoins,
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in observeFavoriteCoinsData", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = "Error loading favorite markets: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun syncFavoritesFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        
        // Don't start a new sync if one is already in progress
        if (refreshJob?.isActive == true) {
            Log.d(TAG, "Sync already in progress, ignoring new request")
            return
        }
        
        // Set refreshing state
        if (!_uiState.value.isLoading) {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        }
        
        refreshJob = viewModelScope.launch {
            try {
                Log.d(TAG, "Syncing favorites from Firestore for user $userId")
                userRepository.syncFavoritesFromFirestore(userId)
                
                // Also refresh market data
                Log.d(TAG, "Refreshing market tickers")
                val refreshResult = coinRepository.refreshMarketTickers()
                refreshResult.fold(
                    onSuccess = {
                        Log.d(TAG, "Successfully refreshed market tickers")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to refresh market tickers", exception)
                        _uiState.update { 
                            it.copy(
                                errorMessage = if (it.favoriteMarketData.isEmpty())
                                    "Could not refresh market data: ${exception.message}"
                                else null
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d(TAG, "Sync operation was cancelled")
                } else {
                    Log.e(TAG, "Exception in syncFavoritesFromFirestore", e)
                    _uiState.update { 
                        it.copy(
                            errorMessage = if (_uiState.value.favoriteMarketData.isEmpty()) 
                                "Could not sync favorites: ${e.message}" 
                            else null
                        )
                    }
                }
            } finally {
                // Always reset the refreshing state
                _uiState.update { 
                    it.copy(isRefreshing = false)
                }
            }
        }
    }
    
    fun refreshFavorites() {
        val userId = auth.currentUser?.uid ?: return
        
        // Don't start a new refresh if one is already in progress
        if (refreshJob?.isActive == true) {
            Log.d(TAG, "Refresh already in progress, ignoring new request")
            return
        }
        
        // Set refreshing state immediately
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        
        refreshJob = viewModelScope.launch {
            try {
                Log.d(TAG, "Starting refresh for favorites data")
                
                // Step 1: Sync favorites from Firestore
                Log.d(TAG, "Syncing favorites from Firestore for user $userId")
                val syncResult = userRepository.syncFavoritesFromFirestore(userId)
                
                if (syncResult is Result.Error) {
                    Log.e(TAG, "Failed to sync favorites from Firestore", syncResult.exception)
                    _uiState.update { 
                        it.copy(
                            errorMessage = "Failed to sync favorites: ${syncResult.exception.message}"
                        )
                    }
                    return@launch
                }
                
                // Step 2: Refresh market data
                Log.d(TAG, "Refreshing market tickers")
                val refreshResult = coinRepository.refreshMarketTickers()
                
                if (refreshResult is Result.Error) {
                    Log.e(TAG, "Failed to refresh market tickers", refreshResult.exception)
                    _uiState.update { 
                        it.copy(
                            errorMessage = "Failed to refresh market data: ${refreshResult.exception.message}"
                        )
                    }
                    return@launch
                }
                
                // Step 3: Get updated favorite symbols list
                Log.d(TAG, "Getting updated favorite symbols")
                val favoritesResult = withTimeoutOrNull(3000) {
                    userRepository.getFavoriteCoinSymbols(userId).first()
                }
                
                if (favoritesResult == null) {
                    Log.e(TAG, "Timeout getting favorite symbols")
                    _uiState.update { 
                        it.copy(
                            errorMessage = "Timeout getting favorite symbols"
                        )
                    }
                    return@launch
                } else if (favoritesResult is Result.Error) {
                    Log.e(TAG, "Failed to get favorite symbols", favoritesResult.exception)
                    _uiState.update { 
                        it.copy(
                            errorMessage = "Failed to get favorites: ${favoritesResult.exception.message}"
                        )
                    }
                    return@launch
                }
                
                // Step 4: Update UI with fresh data
                val symbols = (favoritesResult as Result.Success).data
                if (symbols.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            noFavorites = true,
                            favoriteMarketData = emptyList(),
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                } else {
                    // Get fresh coin data from Room (now updated with latest market data)
                    Log.d(TAG, "Getting fresh data for ${symbols.size} favorite coins")
                    try {
                        val freshCoins = withTimeoutOrNull(2000) {
                            coinTickerDao.getTickersBySymbols(symbols).first().toDomainList()
                        } ?: emptyList()
                        
                        _uiState.update { 
                            it.copy(
                                noFavorites = freshCoins.isEmpty(),
                                favoriteMarketData = freshCoins,
                                lastUpdated = System.currentTimeMillis()
                            )
                        }
                        Log.d(TAG, "Refresh completed successfully with ${freshCoins.size} coins")
                    } catch (e: Exception) {
                        if (e is CancellationException) {
                            Log.d(TAG, "Getting fresh coin data was cancelled")
                        } else {
                            Log.e(TAG, "Failed to get fresh coin data", e)
                            _uiState.update { 
                                it.copy(
                                    errorMessage = "Failed to update market data: ${e.message}"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d(TAG, "Refresh operation was cancelled")
                } else {
                    Log.e(TAG, "Exception during refresh operation", e)
                    _uiState.update { 
                        it.copy(
                            errorMessage = "Failed to refresh: ${e.message}"
                        )
                    }
                }
            } finally {
                // Always reset the refreshing state when done
                _uiState.update { 
                    it.copy(isRefreshing = false)
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "FavoritesViewModel"
    }
}

data class FavoritesUiState(
    val favoriteMarketData: List<Coin> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val noFavorites: Boolean = false,
    val lastUpdated: Long = 0
) 