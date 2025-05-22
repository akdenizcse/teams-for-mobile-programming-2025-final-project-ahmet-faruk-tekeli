package com.aftekeli.currencytracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.model.ChartDataPoint
import com.aftekeli.currencytracker.data.model.Coin
import com.aftekeli.currencytracker.data.repository.CoinRepository
import com.aftekeli.currencytracker.data.repository.UserRepository
import com.aftekeli.currencytracker.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.supervisorScope

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val coinRepository: CoinRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoinDetailUiState())
    val uiState: StateFlow<CoinDetailUiState> = _uiState.asStateFlow()

    private val coinSymbol: String = checkNotNull(savedStateHandle["coinSymbol"])
    private var refreshJob: Job? = null
    
    init {
        _uiState.update { it.copy(coinSymbol = coinSymbol) }
        fetchCoinDetails(coinSymbol)
        fetchHistoricalData(coinSymbol, DEFAULT_INTERVAL)
        observeFavoriteStatus()
    }
    
    private fun observeFavoriteStatus() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            userRepository.isCoinFavorite(userId, coinSymbol).collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }
    
    fun toggleFavorite() {
        val userId = auth.currentUser?.uid ?: return
        val currentSymbol = _uiState.value.coinSymbol
        val currentlyFavorite = _uiState.value.isFavorite
        
        viewModelScope.launch {
            if (currentlyFavorite) {
                userRepository.removeCoinFromFavorites(userId, currentSymbol)
            } else {
                userRepository.addCoinToFavorites(userId, currentSymbol)
            }
            // The UI state will be updated via the Flow collection in observeFavoriteStatus
        }
    }
    
    fun fetchCoinDetails(symbol: String) {
        viewModelScope.launch {
            try {
                // Update UI state to show loading
                _uiState.update { it.copy(isLoadingCoin = true, errorMessage = null) }
                
                Log.d(TAG, "Fetching coin details for $symbol")
                coinRepository.getCoinDetail(symbol).fold(
                    onSuccess = { coin ->
                        Log.d(TAG, "Successfully fetched details for $symbol")
                        _uiState.update { 
                            it.copy(
                                currentCoin = coin,
                                isLoadingCoin = false
                            )
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to fetch coin details: ${exception.message}")
                        _uiState.update { 
                            it.copy(
                                errorMessage = exception.message ?: "Failed to fetch coin details",
                                isLoadingCoin = false
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in fetchCoinDetails: ${e.message}")
                _uiState.update { 
                    it.copy(
                        errorMessage = e.message ?: "Unknown error", 
                        isLoadingCoin = false
                    ) 
                }
            }
        }
    }
    
    fun fetchHistoricalData(symbol: String, interval: String) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoadingChart = true, 
                    errorMessage = null,
                    selectedInterval = interval
                ) 
            }
            
            try {
                Log.d(TAG, "Fetching historical data for $symbol with interval $interval")
                coinRepository.getHistoricalData(symbol, getApiInterval(interval), CHART_DATA_LIMIT).fold(
                    onSuccess = { chartData ->
                        Log.d(TAG, "Successfully fetched ${chartData.size} data points")
                        _uiState.update { 
                            it.copy(
                                chartData = chartData,
                                isLoadingChart = false
                            ) 
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Failed to fetch chart data: ${exception.message}")
                        _uiState.update { 
                            it.copy(
                                isLoadingChart = false,
                                chartErrorMessage = exception.message ?: "Failed to fetch chart data"
                            ) 
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in fetchHistoricalData: ${e.message}")
                _uiState.update { 
                    it.copy(
                        isLoadingChart = false,
                        chartErrorMessage = e.message ?: "Unknown error"
                    ) 
                }
            }
        }
    }
    
    fun onIntervalSelected(interval: String) {
        if (interval != _uiState.value.selectedInterval) {
            fetchHistoricalData(coinSymbol, interval)
        }
    }
    
    fun refreshCoinData() {
        // Don't start a new refresh if one is already in progress
        if (refreshJob?.isActive == true) {
            Log.d(TAG, "Refresh already in progress, ignoring new request")
            return
        }
        
        // Set refreshing state to true
        _uiState.update { it.copy(isRefreshing = true) }
        
        refreshJob = viewModelScope.launch {
            try {
                Log.d(TAG, "Refreshing all coin data")
                
                // Use a supervisor scope to ensure both operations run even if one fails
                supervisorScope {
                    // Create the jobs but don't await them yet
                    val detailsJob = async { 
                        coinRepository.getCoinDetail(coinSymbol).fold(
                            onSuccess = { coin ->
                                Log.d(TAG, "Successfully fetched details for $coinSymbol")
                                _uiState.update { 
                                    it.copy(
                                        currentCoin = coin,
                                        isLoadingCoin = false
                                    )
                                }
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Failed to fetch coin details: ${exception.message}")
                                _uiState.update { 
                                    it.copy(
                                        errorMessage = exception.message ?: "Failed to fetch coin details",
                                        isLoadingCoin = false
                                    ) 
                                }
                            }
                        )
                    }
                    
                    val historicalDataJob = async {
                        val interval = _uiState.value.selectedInterval
                        coinRepository.getHistoricalData(coinSymbol, getApiInterval(interval), CHART_DATA_LIMIT).fold(
                            onSuccess = { chartData ->
                                Log.d(TAG, "Successfully fetched ${chartData.size} data points")
                                _uiState.update { 
                                    it.copy(
                                        chartData = chartData,
                                        isLoadingChart = false
                                    ) 
                                }
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Failed to fetch chart data: ${exception.message}")
                                _uiState.update { 
                                    it.copy(
                                        isLoadingChart = false,
                                        chartErrorMessage = exception.message ?: "Failed to fetch chart data"
                                    ) 
                                }
                            }
                        )
                    }
                    
                    // Wait for both jobs to complete
                    detailsJob.await()
                    historicalDataJob.await()
                }
                
                Log.d(TAG, "Refresh completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error during refresh: ${e.message}")
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "Error refreshing data",
                        isLoadingCoin = false,
                        isLoadingChart = false
                    )
                }
            } finally {
                // Always reset refreshing state to false when done
                _uiState.update { it.copy(isRefreshing = false) }
            }
        }
    }
    
    private fun getApiInterval(displayInterval: String): String {
        return when (displayInterval) {
            "1H" -> "1h"
            "1D" -> "1d"
            "1W" -> "1w"
            "1M" -> "1M"
            else -> "1d" // Default to 1 day
        }
    }
    
    companion object {
        private const val TAG = "CoinDetailViewModel"
        private const val DEFAULT_INTERVAL = "1D"
        private const val CHART_DATA_LIMIT = 200
    }
}

data class CoinDetailUiState(
    val coinSymbol: String = "",
    val currentCoin: Coin? = null,
    val chartData: List<ChartDataPoint> = emptyList(),
    val selectedInterval: String = "1D",
    val availableIntervals: List<String> = listOf("1H", "1D", "1W", "1M"),
    val isLoadingCoin: Boolean = false,
    val isLoadingChart: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val chartErrorMessage: String? = null,
    val isFavorite: Boolean = false
) 