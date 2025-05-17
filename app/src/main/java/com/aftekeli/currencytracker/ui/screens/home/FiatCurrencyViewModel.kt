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
class FiatCurrencyViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<FiatUiState>(FiatUiState.Loading)
    val uiState: StateFlow<FiatUiState> = _uiState
    
    init {
        loadFiatCurrencies()
    }
    
    fun loadFiatCurrencies() {
        viewModelScope.launch {
            _uiState.value = FiatUiState.Loading
            
            try {
                // Only refresh data if needed
                currencyRepository.refreshCurrenciesIfNeeded()
                
                // Observe database updates - filtering for fiat currencies
                currencyRepository.getAllCurrencies()
                    .catch { e ->
                        _uiState.value = FiatUiState.Error(e.message ?: "Failed to load fiat currencies")
                    }
                    .collect { currencies ->
                        // Filter for fiat currency symbols
                        val fiatCurrencies = currencies.filter { isFiatCurrencyPair(it.symbol) }
                        
                        if (fiatCurrencies.isEmpty()) {
                            _uiState.value = FiatUiState.Empty
                        } else {
                            _uiState.value = FiatUiState.Success(fiatCurrencies)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = FiatUiState.Error(e.message ?: "Failed to load fiat currencies")
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
    
    // Helper function to identify fiat currency pairs
    private fun isFiatCurrencyPair(symbol: String): Boolean {
        // Fiat currencies paired with USDT or BTC
        val fiatWithUsdt = listOf("EURUSDT", "GBPUSDT", "USDTBRL", "USDTRUB")
        val fiatWithBtc = listOf("BTCEUR", "BTCGBP", "BTCAUD", "BTCBRL", "BTCRUB", "BTCTRY", "BTCUAH")
        
        return fiatWithUsdt.contains(symbol) || fiatWithBtc.contains(symbol)
    }
    
    // Helper function to get currency code from symbol
    fun getCurrencyCode(symbol: String): String {
        return when {
            symbol.startsWith("BTC") -> symbol.substring(3) // BTCEUR -> EUR
            symbol.endsWith("USDT") -> symbol.removeSuffix("USDT") // EURUSDT -> EUR
            symbol.startsWith("USDT") -> symbol.substring(4) // USDTBRL -> BRL
            else -> symbol
        }
    }
    
    // Helper function to get a flag emoji for the currency
    fun getFlagEmoji(symbol: String): String {
        val code = getCurrencyCode(symbol)
        return when(code) {
            "USD" -> "🇺🇸"
            "EUR" -> "🇪🇺"
            "GBP" -> "🇬🇧"
            "TRY" -> "🇹🇷"
            "JPY" -> "🇯🇵"
            "CNY" -> "🇨🇳"
            "RUB" -> "🇷🇺"
            "AUD" -> "🇦🇺"
            "CAD" -> "🇨🇦"
            "CHF" -> "🇨🇭"
            "BRL" -> "🇧🇷"
            "UAH" -> "🇺🇦"
            else -> "🏳️"
        }
    }
}

sealed class FiatUiState {
    object Loading : FiatUiState()
    object Empty : FiatUiState()
    data class Success(val currencies: List<Currency>) : FiatUiState()
    data class Error(val message: String) : FiatUiState()
} 