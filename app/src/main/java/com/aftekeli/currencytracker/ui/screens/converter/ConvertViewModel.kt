package com.aftekeli.currencytracker.ui.screens.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.domain.model.ConversionRate
import com.aftekeli.currencytracker.domain.model.ConversionResult
import com.aftekeli.currencytracker.domain.model.CurrencyInfo
import com.aftekeli.currencytracker.domain.repository.CurrencyConverterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Currency Converter screen
 */
@HiltViewModel
class ConvertViewModel @Inject constructor(
    private val repository: CurrencyConverterRepository
) : ViewModel() {
    
    // UI state
    private val _uiState = MutableStateFlow(ConvertUiState())
    val uiState: StateFlow<ConvertUiState> = _uiState.asStateFlow()
    
    // Available currencies
    private val _cryptoCurrencies = MutableStateFlow<List<CurrencyInfo>>(emptyList())
    val cryptoCurrencies: StateFlow<List<CurrencyInfo>> = _cryptoCurrencies.asStateFlow()
    
    private val _fiatCurrencies = MutableStateFlow<List<CurrencyInfo>>(emptyList())
    val fiatCurrencies: StateFlow<List<CurrencyInfo>> = _fiatCurrencies.asStateFlow()
    
    // Conversion amount input
    private val _amount = MutableStateFlow("1.0")
    val amount: StateFlow<String> = _amount.asStateFlow()
    
    // Conversion result
    private val _conversionResult = MutableStateFlow<ConversionResult>(ConversionResult.Loading(true))
    val conversionResult: StateFlow<ConversionResult> = _conversionResult.asStateFlow()
    
    // Keep track of the current conversion job to cancel if needed
    private var conversionJob: Job? = null
    
    // Keep track of the latest rate for display formatting
    private var currentConversionRate: ConversionRate? = null
    
    // Supported currency IDs from CoinGecko
    private val supportedCurrencies = listOf(
        "btc", "eth", "ltc", "bch", "bnb", "eos", "xrp", "xlm", "link", "dot", "yfi", 
        "usd", "aed", "ars", "aud", "bdt", "bhd", "bmd", "brl", "cad", "chf", "clp", 
        "cny", "czk", "dkk", "eur", "gbp", "gel", "hkd", "huf", "idr", "ils", "inr", 
        "jpy", "krw", "kwd", "lkr", "mmk", "mxn", "myr", "ngn", "nok", "nzd", "php", 
        "pkr", "pln", "rub", "sar", "sek", "sgd", "thb", "try", "twd", "uah", "vef", 
        "vnd", "zar", "xdr", "xag", "xau", "bits", "sats"
    )
    
    init {
        // Load popular currencies on startup
        loadPopularCurrencies()
    }
    
    /**
     * Load popular cryptocurrencies and fiat currencies
     */
    private fun loadPopularCurrencies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Load popular cryptocurrencies
                repository.getPopularCryptoCurrencies().collect { result ->
                    result.onSuccess { currencies ->
                        _cryptoCurrencies.value = currencies
                        
                        // Set default from currency if not set yet
                        if (_uiState.value.fromCurrency == null && currencies.isNotEmpty()) {
                            // Default to Bitcoin as the from currency
                            val defaultCrypto = currencies.find { it.id == "bitcoin" } ?: currencies.first()
                            setFromCurrency(defaultCrypto)
                        }
                    }.onFailure { error ->
                        Timber.e(error, "Failed to load cryptocurrencies")
                        _uiState.update { it.copy(error = "Failed to load cryptocurrencies") }
                    }
                }
                
                // Load popular fiat currencies
                repository.getPopularFiatCurrencies().collect { result ->
                    result.onSuccess { currencies ->
                        _fiatCurrencies.value = currencies
                        
                        // Set default to currency if not set yet
                        if (_uiState.value.toCurrency == null && currencies.isNotEmpty()) {
                            // Default to USD as the to currency
                            val defaultFiat = currencies.find { it.id == "usd" } ?: currencies.first()
                            setToCurrency(defaultFiat)
                        }
                    }.onFailure { error ->
                        Timber.e(error, "Failed to load fiat currencies")
                        _uiState.update { it.copy(error = "Failed to load fiat currencies") }
                    }
                }
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Timber.e(e, "Error in loadPopularCurrencies")
                _uiState.update { it.copy(isLoading = false, error = "Failed to load currencies") }
            }
        }
    }
    
    /**
     * Set the "from" currency and trigger a conversion
     */
    fun setFromCurrency(currency: CurrencyInfo) {
        if (_uiState.value.fromCurrency?.id != currency.id) {
            _uiState.update { it.copy(fromCurrency = currency) }
            updateConversion()
        }
    }
    
    /**
     * Set the "to" currency and trigger a conversion
     */
    fun setToCurrency(currency: CurrencyInfo) {
        if (_uiState.value.toCurrency?.id != currency.id) {
            _uiState.update { it.copy(toCurrency = currency) }
            updateConversion()
        }
    }
    
    /**
     * Swap the from and to currencies
     */
    fun swapCurrencies() {
        val from = _uiState.value.fromCurrency
        val to = _uiState.value.toCurrency
        
        if (from != null && to != null) {
            _uiState.update { it.copy(fromCurrency = to, toCurrency = from) }
            updateConversion()
        }
    }
    
    /**
     * Update the amount and trigger a conversion with debounce
     */
    fun setAmount(newAmount: String) {
        if (newAmount.isEmpty() || newAmount == ".") {
            _amount.value = "0"
        } else {
            // Validate that it's a valid number
            newAmount.toDoubleOrNull()?.let {
                _amount.value = newAmount
            } ?: return
        }
        
        // Update conversion with debounce
        updateConversionWithDebounce()
    }
    
    /**
     * Update conversion with debounce to avoid excessive API calls
     */
    private fun updateConversionWithDebounce() {
        conversionJob?.cancel()
        conversionJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            updateConversion()
        }
    }
    
    /**
     * Refresh the current conversion rate
     */
    fun refreshConversion() {
        updateConversion(forceRefresh = true)
    }
    
    /**
     * Update the conversion based on current from/to currencies and amount
     * Implements the two-step conversion through USD when needed
     */
    private fun updateConversion(forceRefresh: Boolean = false) {
        val fromCurrency = _uiState.value.fromCurrency ?: return
        val toCurrency = _uiState.value.toCurrency ?: return
        
        // Don't refresh if we already have a non-expired rate unless forced
        if (!forceRefresh && 
            currentConversionRate?.fromCurrency?.id == fromCurrency.id &&
            currentConversionRate?.toCurrency?.id == toCurrency.id &&
            currentConversionRate?.isExpired() == false) {
            
            // Just update the calculation with the existing rate
            calculateWithCurrentRate()
            return
        }
        
        // Set loading state
        _conversionResult.value = ConversionResult.Loading()
        
        conversionJob?.cancel()
        conversionJob = viewModelScope.launch {
            try {
                // Special case - if target currency is not USD, we need two-step conversion
                if (toCurrency.id != "usd" && fromCurrency.id != "usd") {
                    // Step 1: First convert from source to USD
                    val step1Flow = repository.getConversionRate(fromCurrency.id, "usd")
                    
                    step1Flow.collect { result ->
                        result.onSuccess { usdRate ->
                            // Step 2: Then convert from USD to target
                            val step2Flow = repository.getConversionRate("usd", toCurrency.id)
                            
                            step2Flow.collect { usdToTargetResult ->
                                usdToTargetResult.onSuccess { targetRate ->
                                    // Combine both rates
                                    val combinedRate = ConversionRate(
                                        fromCurrency = fromCurrency,
                                        toCurrency = toCurrency,
                                        rate = usdRate.rate * targetRate.rate,
                                        lastUpdated = System.currentTimeMillis()
                                    )
                                    
                                    currentConversionRate = combinedRate
                                    
                                    // Calculate the conversion result
                                    val amountValue = _amount.value.toDoubleOrNull() ?: 0.0
                                    val convertedAmount = combinedRate.convert(amountValue)
                                    
                                    _conversionResult.value = ConversionResult.Success(
                                        fromAmount = amountValue,
                                        toAmount = convertedAmount,
                                        rate = combinedRate
                                    )
                                }.onFailure { error ->
                                    Timber.e(error, "Step 2 conversion failed")
                                    _conversionResult.value = ConversionResult.Error(
                                        message = "Failed to get conversion rate: ${error.localizedMessage}"
                                    )
                                }
                            }
                        }.onFailure { error ->
                            Timber.e(error, "Step 1 conversion failed")
                            _conversionResult.value = ConversionResult.Error(
                                message = "Failed to get conversion rate: ${error.localizedMessage}"
                            )
                        }
                    }
                } else {
                    // Direct conversion (when one of the currencies is USD)
                    repository.getConversionRate(fromCurrency.id, toCurrency.id).collect { result ->
                        result.onSuccess { conversionRate ->
                            currentConversionRate = conversionRate
                            
                            // Calculate the conversion result
                            val amountValue = _amount.value.toDoubleOrNull() ?: 0.0
                            val convertedAmount = conversionRate.convert(amountValue)
                            
                            _conversionResult.value = ConversionResult.Success(
                                fromAmount = amountValue,
                                toAmount = convertedAmount,
                                rate = conversionRate
                            )
                            
                        }.onFailure { error ->
                            Timber.e(error, "Conversion failed")
                            _conversionResult.value = ConversionResult.Error(
                                message = "Failed to get conversion rate: ${error.localizedMessage}"
                            )
                        }
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error in updateConversion")
                _conversionResult.value = ConversionResult.Error(
                    message = "Conversion failed: ${e.localizedMessage}"
                )
            }
        }
    }
    
    /**
     * Calculate conversion with the current rate without API call
     */
    private fun calculateWithCurrentRate() {
        currentConversionRate?.let { rate ->
            val amountValue = _amount.value.toDoubleOrNull() ?: 0.0
            val convertedAmount = rate.convert(amountValue)
            
            _conversionResult.value = ConversionResult.Success(
                fromAmount = amountValue,
                toAmount = convertedAmount,
                rate = rate
            )
        }
    }
    
    /**
     * Load all available cryptocurrencies (for advanced selection)
     */
    fun loadAllCryptoCurrencies() {
        if (_cryptoCurrencies.value.size <= 15) { // Only load if we only have popular ones
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingAllCurrencies = true) }
                
                repository.getCryptoCurrencies().collect { result ->
                    result.onSuccess { currencies ->
                        _cryptoCurrencies.value = currencies
                    }
                    _uiState.update { it.copy(isLoadingAllCurrencies = false) }
                }
            }
        }
    }
    
    /**
     * Load all available fiat currencies (for advanced selection)
     */
    fun loadAllFiatCurrencies() {
        if (_fiatCurrencies.value.size <= 15) { // Only load if we only have popular ones
            viewModelScope.launch {
                _uiState.update { it.copy(isLoadingAllCurrencies = true) }
                
                repository.getFiatCurrencies().collect { result ->
                    result.onSuccess { currencies ->
                        _fiatCurrencies.value = currencies
                    }
                    _uiState.update { it.copy(isLoadingAllCurrencies = false) }
                }
            }
        }
    }
}

/**
 * UI state for the converter screen
 */
data class ConvertUiState(
    val fromCurrency: CurrencyInfo? = null,
    val toCurrency: CurrencyInfo? = null,
    val isLoading: Boolean = false,
    val isLoadingAllCurrencies: Boolean = false,
    val error: String? = null
) 