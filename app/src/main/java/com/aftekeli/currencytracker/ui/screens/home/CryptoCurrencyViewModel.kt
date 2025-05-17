package com.aftekeli.currencytracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CryptoCurrencyViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CryptoUiState>(CryptoUiState.Loading)
    val uiState: StateFlow<CryptoUiState> = _uiState
    
    init {
        loadCryptoCurrencies()
    }
    
    fun loadCryptoCurrencies() {
        viewModelScope.launch {
            _uiState.value = CryptoUiState.Loading
            
            try {
                // Only refresh data if needed
                currencyRepository.refreshCurrenciesIfNeeded()
                
                // Observe database updates - filtering for crypto currencies
                currencyRepository.getAllCurrencies()
                    .catch { e ->
                        _uiState.value = CryptoUiState.Error(e.message ?: "Failed to load currencies")
                    }
                    .collect { currencies ->
                        // Filter for cryptocurrency symbols (ending with USDT)
                        val cryptoCurrencies = currencies.filter { 
                            it.symbol.endsWith("USDT") && !isFiatCurrencyPair(it.symbol)
                        }
                        
                        if (cryptoCurrencies.isEmpty()) {
                            _uiState.value = CryptoUiState.Empty
                        } else {
                            _uiState.value = CryptoUiState.Success(cryptoCurrencies)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = CryptoUiState.Error(e.message ?: "Failed to load currencies")
            }
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            try {
                // Force refresh when user explicitly requests it
                currencyRepository.refreshCurrenciesIfNeeded(forceRefresh = true)
            } catch (e: Exception) {
                // Handle error silently or update UI state as needed
            }
        }
    }
    
    fun addToWatchlist(userId: String, symbol: String) {
        viewModelScope.launch {
            try {
                currencyRepository.addToWatchlist(userId, symbol)
            } catch (e: Exception) {
                _uiState.value = CryptoUiState.Error("Failed to add to watchlist")
            }
        }
    }
    
    fun removeFromWatchlist(userId: String, symbol: String) {
        viewModelScope.launch {
            try {
                currencyRepository.removeFromWatchlist(userId, symbol)
            } catch (e: Exception) {
                _uiState.value = CryptoUiState.Error("Failed to remove from watchlist")
            }
        }
    }
    
    // Helper function to identify if a USDT pair is actually a fiat currency
    private fun isFiatCurrencyPair(symbol: String): Boolean {
        val fiatCodes = listOf("EUR", "GBP", "AUD", "BRL", "RUB", "TRY", "UAH")
        return fiatCodes.any { symbol == "${it}USDT" }
    }
}

sealed class CryptoUiState {
    object Loading : CryptoUiState()
    object Empty : CryptoUiState()
    data class Success(val currencies: List<Currency>) : CryptoUiState()
    data class Error(val message: String) : CryptoUiState()
} 