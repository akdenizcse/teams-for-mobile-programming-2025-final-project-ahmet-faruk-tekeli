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

// Filtre seçenekleri için enum sınıfları
enum class SortOption {
    NAME_ASC, NAME_DESC, PRICE_ASC, PRICE_DESC, CHANGE_ASC, CHANGE_DESC
}

enum class FilterType {
    ALL, FAVORITES_ONLY, CRYPTO_ONLY, FIAT_ONLY
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState
    
    // Filtreleme durumu için state'ler
    private val _sortOption = MutableStateFlow(SortOption.NAME_ASC)
    val sortOption: StateFlow<SortOption> = _sortOption
    
    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType
    
    private val _showFavorites = MutableStateFlow(false)
    val showFavorites: StateFlow<Boolean> = _showFavorites
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    init {
        loadCurrencies()
    }
    
    fun loadCurrencies() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            try {
                // Only refresh data from API if needed, not on every filter change
                currencyRepository.refreshCurrenciesIfNeeded()
                
                // Observe database updates
                currencyRepository.getAllCurrencies()
                    .catch { e ->
                        _uiState.value = HomeUiState.Error(e.message ?: "Failed to load currencies")
                    }
                    .collect { currencies ->
                        if (currencies.isEmpty()) {
                            _uiState.value = HomeUiState.Empty
                        } else {
                            _uiState.value = HomeUiState.Success(applyFiltersAndSort(currencies))
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load currencies")
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
                
                // Update the favorite status in the UI
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    val updatedList = currentState.currencies.map { currency ->
                        if (currency.symbol == symbol) {
                            currency.copy(isFavorite = true)
                        } else {
                            currency
                        }
                    }
                    
                    _uiState.value = HomeUiState.Success(applyFiltersAndSort(updatedList))
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to add to watchlist")
            }
        }
    }
    
    fun removeFromWatchlist(userId: String, symbol: String) {
        viewModelScope.launch {
            try {
                currencyRepository.removeFromWatchlist(userId, symbol)
                
                // Update the favorite status in the UI
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    val updatedList = currentState.currencies.map { currency ->
                        if (currency.symbol == symbol) {
                            currency.copy(isFavorite = false)
                        } else {
                            currency
                        }
                    }
                    
                    _uiState.value = HomeUiState.Success(applyFiltersAndSort(updatedList))
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to remove from watchlist")
            }
        }
    }
    
    // Filtreleme özelliklerini ayarlamak için fonksiyonlar
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
        reapplyFilters()
    }
    
    fun setFilterType(filter: FilterType) {
        _filterType.value = filter
        reapplyFilters()
    }
    
    fun setShowFavorites(show: Boolean) {
        _showFavorites.value = show
        reapplyFilters()
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        reapplyFilters()
    }
    
    // Mevcut filtreleri uygula
    private fun reapplyFilters() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = HomeUiState.Success(applyFiltersAndSort(currentState.currencies))
        }
    }
    
    // Filtreleme ve sıralama işlemini uygula
    private fun applyFiltersAndSort(currencies: List<Currency>): List<Currency> {
        // Önce filtreleme
        var filtered = currencies
        
        // Sadece favorileri gösterme filtreleme
        if (_showFavorites.value) {
            filtered = filtered.filter { it.isFavorite }
        }
        
        // Filtre tipine göre filtreleme
        filtered = when (_filterType.value) {
            FilterType.ALL -> filtered
            FilterType.FAVORITES_ONLY -> filtered.filter { it.isFavorite }
            FilterType.CRYPTO_ONLY -> filtered.filter { !isFiatCurrency(it.symbol) }
            FilterType.FIAT_ONLY -> filtered.filter { isFiatCurrency(it.symbol) }
        }
        
        // Arama sorgusuna göre filtreleme
        if (_searchQuery.value.isNotBlank()) {
            val query = _searchQuery.value.lowercase()
            filtered = filtered.filter {
                it.symbol.lowercase().contains(query) ||
                formatCurrencyPair(it.symbol).lowercase().contains(query)
            }
        }
        
        // Sonra sıralama
        return when (_sortOption.value) {
            SortOption.NAME_ASC -> filtered.sortedBy { formatCurrencyPair(it.symbol) }
            SortOption.NAME_DESC -> filtered.sortedByDescending { formatCurrencyPair(it.symbol) }
            SortOption.PRICE_ASC -> filtered.sortedBy { it.price }
            SortOption.PRICE_DESC -> filtered.sortedByDescending { it.price }
            SortOption.CHANGE_ASC -> filtered.sortedBy { it.changePercent24h ?: 0.0 }
            SortOption.CHANGE_DESC -> filtered.sortedByDescending { it.changePercent24h ?: 0.0 }
        }
    }
    
    // Helper fonksiyonu: isFiatCurrency
    private fun isFiatCurrency(symbol: String): Boolean {
        val fiatSymbols = listOf(
            "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", 
            "HKD", "NZD", "SEK", "KRW", "SGD", "NOK", "MXN", 
            "INR", "RUB", "ZAR", "TRY", "BRL", "IDR", "ARS", "UAH"
        )
        
        val stablecoins = listOf("USDT", "USDC", "DAI", "BUSD", "TUSD")
        
        if (stablecoins.contains(symbol)) {
            return true
        }
        
        return when {
            symbol.endsWith("USDT") -> {
                val base = symbol.removeSuffix("USDT")
                fiatSymbols.contains(base)
            }
            symbol.startsWith("USDT") -> {
                val counter = symbol.removePrefix("USDT")
                fiatSymbols.contains(counter)
            }
            else -> {
                fiatSymbols.any { symbol.startsWith(it) }
            }
        }
    }
    
    // Helper fonksiyonu: formatCurrencyPair
    private fun formatCurrencyPair(symbol: String): String {
        return when {
            symbol.endsWith("USDT") -> {
                val base = symbol.removeSuffix("USDT")
                if (base.isEmpty()) "USDT/USD" else "$base/USD"
            }
            symbol.startsWith("USDT") -> {
                val counter = symbol.removePrefix("USDT")
                if (counter.isEmpty()) "USDT/USD" else "$counter/USD"
            }
            symbol.endsWith("BTC") -> {
                val base = symbol.removeSuffix("BTC")
                "$base/BTC"
            }
            symbol.endsWith("EUR") -> {
                val base = symbol.removeSuffix("EUR")
                "$base/EUR"
            }
            else -> symbol
        }
    }
    
    fun clearError() {
        _uiState.value = HomeUiState.Loading
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(val currencies: List<Currency>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
} 