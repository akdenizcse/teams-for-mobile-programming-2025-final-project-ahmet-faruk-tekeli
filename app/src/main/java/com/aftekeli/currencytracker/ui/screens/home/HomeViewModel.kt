package com.aftekeli.currencytracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState
    
    init {
        loadCurrencies()
    }
    
    fun loadCurrencies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Refresh currency data from API
                currencyRepository.refreshCurrencies()
                
                // Collect currencies from local database
                currencyRepository.getAllCurrencies()
                    .catch { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Failed to load currencies"
                            ) 
                        }
                    }
                    .collectLatest { currencies ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                currencies = currencies,
                                error = null
                            ) 
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    ) 
                }
            }
        }
    }
    
    fun addToWatchlist(userId: String, symbol: String) {
        viewModelScope.launch {
            try {
                currencyRepository.addToWatchlist(userId, symbol)
                
                // Update the favorite status in the UI
                val updatedList = uiState.value.currencies.map { currency ->
                    if (currency.symbol == symbol) {
                        currency.copy(isFavorite = true)
                    } else {
                        currency
                    }
                }
                
                _uiState.update { it.copy(currencies = updatedList) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to add to watchlist") }
            }
        }
    }
    
    fun removeFromWatchlist(userId: String, symbol: String) {
        viewModelScope.launch {
            try {
                currencyRepository.removeFromWatchlist(userId, symbol)
                
                // Update the favorite status in the UI
                val updatedList = uiState.value.currencies.map { currency ->
                    if (currency.symbol == symbol) {
                        currency.copy(isFavorite = false)
                    } else {
                        currency
                    }
                }
                
                _uiState.update { it.copy(currencies = updatedList) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to remove from watchlist") }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val currencies: List<Currency> = emptyList(),
    val error: String? = null
) 