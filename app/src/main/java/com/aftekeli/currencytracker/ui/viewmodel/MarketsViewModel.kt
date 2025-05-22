package com.aftekeli.currencytracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.model.Coin
import com.aftekeli.currencytracker.data.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

// Enum defining available sort options
enum class SortOption(val displayName: String) {
    NAME("Name"),
    PRICE_CHANGE("Price Change"),
    VOLUME("Volume")
}

// Enum defining sort directions
enum class SortDirection(val displayName: String) {
    ASCENDING("Ascending"),
    DESCENDING("Descending")
}

@HiltViewModel
class MarketsViewModel @Inject constructor(
    private val coinRepository: CoinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketsUiState())
    val uiState: StateFlow<MarketsUiState> = _uiState.asStateFlow()
    
    private var refreshJob: Job? = null
    private var currentCoins: List<Coin> = emptyList()

    init {
        observeMarketTickers()
        refreshMarketTickers()
    }

    private fun observeMarketTickers() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting to observe market tickers")
                coinRepository.getMarketTickers()
                    .onStart { 
                        if (_uiState.value.tickers.isEmpty()) {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                    .catch { exception ->
                        Log.e(TAG, "Error collecting market tickers", exception)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = exception.message ?: "Error collecting data"
                            ) 
                        }
                    }
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { coins ->
                                Log.d(TAG, "Successfully collected ${coins.size} tickers")
                                // Store the unsorted list
                                currentCoins = coins
                                // Apply current sort preferences and filtering
                                updateDisplayedCoins()
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Error in ticker result", exception)
                                _uiState.update { 
                                    it.copy(
                                        isLoading = false,
                                        isRefreshing = false,
                                        errorMessage = if (it.tickers.isEmpty()) 
                                            exception.message ?: "Unknown error" 
                                        else null
                                    ) 
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in observeMarketTickers", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = if (it.tickers.isEmpty()) 
                            e.message ?: "Unknown error" 
                        else null
                    ) 
                }
            }
        }
    }

    fun refreshMarketTickers() {
        // Don't start a new refresh if one is already in progress
        if (refreshJob?.isActive == true) {
            Log.d(TAG, "Refresh already in progress, ignoring new request")
            return
        }
        
        // Set the refreshing state
        if (_uiState.value.tickers.isEmpty()) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        } else {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        }
        
        refreshJob = viewModelScope.launch {
            try {
                Log.d(TAG, "Refreshing market tickers")
                
                coinRepository.refreshMarketTickers().fold(
                    onSuccess = { 
                        Log.d(TAG, "Successfully refreshed market tickers")
                        // The Flow collection in observeMarketTickers will update the UI with new data
                    },
                    onFailure = { exception ->
                        if (exception is CancellationException) {
                            Log.d(TAG, "Refresh operation was cancelled")
                        } else {
                            Log.e(TAG, "Error refreshing market tickers", exception)
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    isRefreshing = false,
                                    errorMessage = if (it.tickers.isEmpty()) 
                                        exception.message ?: "Unknown error" 
                                    else null
                                ) 
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Log.d(TAG, "Refresh operation was cancelled")
                } else {
                    Log.e(TAG, "Exception in refreshMarketTickers", e)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = if (it.tickers.isEmpty()) 
                                e.message ?: "Unknown error" 
                            else null
                        ) 
                    }
                }
            } finally {
                // Ensure the loading/refreshing state is reset
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isRefreshing = false
                    ) 
                }
            }
        }
    }
    
    // Function to update search query
    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
        updateDisplayedCoins()
    }
    
    // Function to clear search query
    fun clearSearchQuery() {
        _uiState.update {
            it.copy(searchQuery = "")
        }
        updateDisplayedCoins()
    }
    
    // Function to change the sorting option
    fun setSortOption(option: SortOption) {
        _uiState.update {
            it.copy(sortOption = option)
        }
        // Apply the new sorting
        updateDisplayedCoins()
    }
    
    // Function to change the sorting direction
    fun setSortDirection(direction: SortDirection) {
        _uiState.update {
            it.copy(sortDirection = direction)
        }
        // Apply the new sorting
        updateDisplayedCoins()
    }
    
    // Apply sorting and filtering based on current preferences and update UI state
    private fun updateDisplayedCoins() {
        val sortedCoins = applySorting(currentCoins)
        val filteredCoins = applyFiltering(sortedCoins, _uiState.value.searchQuery)
        
        _uiState.update {
            it.copy(
                tickers = filteredCoins,
                isLoading = false,
                isRefreshing = false,
                errorMessage = null,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
    
    // Apply filtering to the sorted list based on search query
    private fun applyFiltering(coins: List<Coin>, searchQuery: String): List<Coin> {
        if (searchQuery.isBlank()) {
            return coins
        }
        
        return coins.filter { coin ->
            coin.baseAsset.contains(searchQuery, ignoreCase = true) ||
            coin.symbol.contains(searchQuery, ignoreCase = true)
        }
    }
    
    // Apply sorting to the given list based on current sort preferences
    private fun applySorting(coins: List<Coin>): List<Coin> {
        val currentState = _uiState.value
        val sortOption = currentState.sortOption
        val isAscending = currentState.sortDirection == SortDirection.ASCENDING
        
        return when (sortOption) {
            SortOption.NAME -> {
                if (isAscending) {
                    coins.sortedWith(compareBy<Coin> { 
                        it.baseAsset.lowercase().ifEmpty { "zzz" } // Handle empty baseAsset
                    })
                } else {
                    coins.sortedWith(compareByDescending<Coin> { 
                        it.baseAsset.lowercase().ifEmpty { "" } // Handle empty baseAsset
                    })
                }
            }
            SortOption.PRICE_CHANGE -> {
                if (isAscending) {
                    coins.sortedBy { it.priceChangePercent }
                } else {
                    coins.sortedByDescending { it.priceChangePercent }
                }
            }
            SortOption.VOLUME -> {
                if (isAscending) {
                    // Use quoteVolume first if available, fall back to volume
                    coins.sortedWith(compareBy<Coin> { 
                        if (it.quoteVolume > 0) it.quoteVolume else it.volume 
                    })
                } else {
                    // Use quoteVolume first if available, fall back to volume
                    coins.sortedWith(compareByDescending<Coin> { 
                        if (it.quoteVolume > 0) it.quoteVolume else it.volume 
                    })
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "MarketsViewModel"
    }
}

data class MarketsUiState(
    val tickers: List<Coin> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdated: Long = 0,
    val sortOption: SortOption = SortOption.VOLUME,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val searchQuery: String = ""
) 