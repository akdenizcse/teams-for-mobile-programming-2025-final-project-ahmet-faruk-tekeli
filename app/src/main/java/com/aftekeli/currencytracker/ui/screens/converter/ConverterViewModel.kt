package com.aftekeli.currencytracker.ui.screens.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.preferences.SessionManager
import com.aftekeli.currencytracker.data.remote.firestore.FirestoreService
import com.aftekeli.currencytracker.domain.model.ConversionHistory
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val firestoreService: FirestoreService,
    private val sessionManager: SessionManager
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<ConverterUiState>(ConverterUiState.Loading)
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()
    
    // Available currencies
    private val _availableCurrencies = MutableStateFlow<List<Currency>>(emptyList())
    val availableCurrencies: StateFlow<List<Currency>> = _availableCurrencies.asStateFlow()
    
    // Selected currencies
    private val _fromCurrency = MutableStateFlow<Currency?>(null)
    val fromCurrency: StateFlow<Currency?> = _fromCurrency.asStateFlow()
    
    private val _toCurrency = MutableStateFlow<Currency?>(null)
    val toCurrency: StateFlow<Currency?> = _toCurrency.asStateFlow()
    
    // Conversion amount
    private val _amount = MutableStateFlow("1.0")
    val amount: StateFlow<String> = _amount.asStateFlow()
    
    // Conversion history
    private val _conversionHistory = MutableStateFlow<List<ConversionHistory>>(emptyList())
    val conversionHistory: StateFlow<List<ConversionHistory>> = _conversionHistory.asStateFlow()
    
    init {
        loadCurrencies()
        
        // Kullanıcı oturum durumunu kontrol et ve dönüşüm geçmişini yükle
        viewModelScope.launch {
            sessionManager.isLoggedIn.collectLatest { isLoggedIn ->
                if (isLoggedIn) {
                    sessionManager.getCurrentUserId()?.let { userId ->
                        loadConversionHistory(userId)
                    }
                }
            }
        }
    }
    
    private fun loadCurrencies() {
        viewModelScope.launch {
            _uiState.value = ConverterUiState.Loading
            
            try {
                currencyRepository.getAllCurrencies().collect { currencies ->
                    _availableCurrencies.value = currencies
                    
                    // Set default currencies if not set yet
                    if (_fromCurrency.value == null) {
                        _fromCurrency.value = currencies.find { it.symbol == "BTCUSDT" }
                            ?: currencies.find { it.symbol.endsWith("USDT") }
                            ?: currencies.firstOrNull()
                    }
                    
                    if (_toCurrency.value == null) {
                        _toCurrency.value = currencies.find { it.symbol == "USDTTRY" }
                            ?: currencies.find { it.symbol.startsWith("USDT") }
                            ?: currencies.getOrNull(1)
                    }
                    
                    _uiState.value = ConverterUiState.Success
                }
            } catch (e: Exception) {
                _uiState.value = ConverterUiState.Error(e.message ?: "Failed to load currencies")
            }
        }
    }
    
    fun setFromCurrency(currency: Currency) {
        _fromCurrency.value = currency
    }
    
    fun setToCurrency(currency: Currency) {
        _toCurrency.value = currency
    }
    
    fun setAmount(newAmount: String) {
        _amount.value = newAmount
    }
    
    fun swapCurrencies() {
        val temp = _fromCurrency.value
        _fromCurrency.value = _toCurrency.value
        _toCurrency.value = temp
    }
    
    fun calculateConversion(): ConversionResult {
        val from = _fromCurrency.value
        val to = _toCurrency.value
        
        if (from == null || to == null) {
            return ConversionResult(0.0, 0.0, "$")
        }
        
        // Doğru dönüşüm oranını hesapla
        val rate = calculateExchangeRate(from, to)
        
        val inputAmount = _amount.value.toDoubleOrNull() ?: 0.0
        val result = inputAmount * rate
        
        return ConversionResult(
            result = result,
            rate = rate,
            symbol = getCurrencySymbol(to.symbol)
        )
    }
    
    // Para birimleri arasındaki dönüşüm oranını hesapla
    private fun calculateExchangeRate(fromCurrency: Currency, toCurrency: Currency): Double {
        val fromSymbol = fromCurrency.symbol
        val toSymbol = toCurrency.symbol
        
        // Extract base currencies from trading pairs
        val fromBase = extractBaseCurrency(fromSymbol)
        val toBase = extractBaseCurrency(toSymbol)
        
        // Check if we're dealing with fiat currencies with reversed pairs
        val isFromFiatReversed = fromSymbol.startsWith("USDT") && fromSymbol != "USDT"
        val isToFiatReversed = toSymbol.startsWith("USDT") && toSymbol != "USDT"
        
        // Get actual prices (adjusted for reversed pairs)
        val fromPriceActual = if (isFromFiatReversed) 1.0 / fromCurrency.price else fromCurrency.price
        val toPriceActual = if (isToFiatReversed) 1.0 / toCurrency.price else toCurrency.price
        
        // Log for debugging
        println("Converting $fromSymbol ($fromBase) → $toSymbol ($toBase)")
        println("Original prices - From: ${fromCurrency.price}, To: ${toCurrency.price}")
        println("Adjusted prices - From: $fromPriceActual, To: $toPriceActual")
        
        // Special case: If converting between the same base currency
        if (fromBase == toBase) {
            return 1.0
        }
        
        // CASE 1: Direct conversion between two regular cryptocurrencies (BTC → ETH)
        if (!isFromFiatReversed && !isToFiatReversed && 
            fromSymbol.endsWith("USDT") && toSymbol.endsWith("USDT")) {
            // Both are crypto-to-USDT pairs
            println("CASE 1: Crypto to Crypto: $fromBase → $toBase")
            // Example: BTCUSDT = 50000, ETHUSDT = 2500
            // 1 BTC = 50000 USDT, 1 ETH = 2500 USDT
            // Therefore 1 BTC = 50000/2500 = 20 ETH
            return fromPriceActual / toPriceActual
        }
        
        // CASE 2: Direct conversion between two fiat currencies (TRY → EUR)
        if (isFromFiatReversed && isToFiatReversed) {
            println("CASE 2: Fiat to Fiat: $fromBase → $toBase")
            // Example: USDTTRY = 30, USDTEUR = 0.85
            // 1 USD = 30 TRY, 1 USD = 0.85 EUR
            // Therefore 1 TRY = 0.85/30 = 0.028 EUR
            return toCurrency.price / fromCurrency.price  // Direct ratio of USDT-Fiat pairs
        }
        
        // CASE 3: Cryptocurrency to fiat currency (BTC → TRY)
        if (!isFromFiatReversed && isToFiatReversed) {
            println("CASE 3: Crypto to Fiat: $fromBase → $toBase")
            // Example: BTCUSDT = 50000, USDTTRY = 30
            // 1 BTC = 50000 USDT, 1 USDT = 30 TRY
            // Therefore 1 BTC = 50000 * 30 = 1500000 TRY
            return fromPriceActual * toCurrency.price  // Multiply crypto-USDT price by USDT-Fiat price
        }
        
        // CASE 4: Fiat currency to cryptocurrency (TRY → BTC)
        if (isFromFiatReversed && !isToFiatReversed) {
            println("CASE 4: Fiat to Crypto: $fromBase → $toBase")
            // Example: USDTTRY = 30, BTCUSDT = 50000
            // 1 USD = 30 TRY, 1 BTC = 50000 USD
            // Therefore 1 TRY = 1/30 USD, and you get (1/30)/50000 = 1/1500000 BTC
            return 1.0 / (fromCurrency.price * toPriceActual)  // Divide 1 by (USDT-Fiat price * crypto-USDT price)
        }
        
        // CASE 5: Direct USDT to crypto or fiat
        if (fromBase == "USDT") {
            if (isToFiatReversed) {
                // USDT to fiat (USDT → TRY)
                println("CASE 5A: USDT to Fiat: $fromBase → $toBase")
                // 1 USDT = USDTTRY TRY
                return toCurrency.price
            } else if (toSymbol.endsWith("USDT")) {
                // USDT to crypto (USDT → BTC)
                println("CASE 5B: USDT to Crypto: $fromBase → $toBase")
                // 1 USDT = (1/BTCUSDT) BTC
                return 1.0 / toPriceActual
            }
        }
        
        // CASE 6: Direct crypto or fiat to USDT
        if (toBase == "USDT") {
            if (isFromFiatReversed) {
                // Fiat to USDT (TRY → USDT)
                println("CASE 6A: Fiat to USDT: $fromBase → $toBase")
                // 1 TRY = (1/USDTTRY) USDT
                return 1.0 / fromCurrency.price
            } else if (fromSymbol.endsWith("USDT")) {
                // Crypto to USDT (BTC → USDT)
                println("CASE 6B: Crypto to USDT: $fromBase → $toBase")
                // 1 BTC = BTCUSDT USDT
                return fromPriceActual
            }
        }
        
        // Fallback - direct price ratio if USD prices available
        if (fromCurrency.priceUsd != null && toCurrency.priceUsd != null && fromCurrency.priceUsd > 0) {
            println("FALLBACK: Using USD prices")
            return toCurrency.priceUsd / fromCurrency.priceUsd
        }
        
        // Last resort - general price ratio (adjusted for reversed pairs)
        println("LAST RESORT: Using general price ratio")
        return toPriceActual / fromPriceActual
    }
    
    fun saveConversion() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getCurrentUserId() ?: return@launch
                val from = _fromCurrency.value ?: return@launch
                val to = _toCurrency.value ?: return@launch
                val inputAmount = _amount.value.toDoubleOrNull() ?: 0.0
                
                val conversion = calculateConversion()
                
                firestoreService.saveConversion(
                    userId = userId,
                    fromCurrency = from.symbol,
                    toCurrency = to.symbol,
                    fromAmount = inputAmount,
                    toAmount = conversion.result,
                    rate = conversion.rate
                )
                
                // Update UI state to show success message
                _uiState.value = ConverterUiState.ConversionSaved
                // Change back to success state after a brief period
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = ConverterUiState.Success
                }
                
                // Yeni listeyi yükle
                loadConversionHistory(userId)
                
            } catch (e: Exception) {
                _uiState.value = ConverterUiState.Error(e.message ?: "Failed to save conversion")
            }
        }
    }
    
    fun loadConversionHistory(userId: String) {
        viewModelScope.launch {
            try {
                firestoreService.getConversionHistory(userId).collect { conversionMaps ->
                    val conversions = conversionMaps.map { map ->
                        ConversionHistory(
                            id = map["id"]?.toString() ?: "",
                            userId = userId,
                            fromCurrency = map["fromCurrency"]?.toString() ?: "",
                            toCurrency = map["toCurrency"]?.toString() ?: "",
                            fromAmount = (map["fromAmount"] as? Number)?.toDouble() ?: 0.0,
                            toAmount = (map["toAmount"] as? Number)?.toDouble() ?: 0.0,
                            rate = (map["rate"] as? Number)?.toDouble() ?: 0.0,
                            timestamp = (map["timestamp"] as? com.google.firebase.Timestamp)?.toDate() ?: Date()
                        )
                    }
                    _conversionHistory.value = conversions
                }
            } catch (e: Exception) {
                // Dönüşüm geçmişi yüklenemediğinde sessizce geç
                // _uiState.value = ConverterUiState.Error(e.message ?: "Failed to load conversion history")
            }
        }
    }
    
    fun clearConversionHistory() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getCurrentUserId() ?: return@launch
                firestoreService.clearConversionHistory(userId)
                _conversionHistory.value = emptyList()
            } catch (e: Exception) {
                _uiState.value = ConverterUiState.Error(e.message ?: "Failed to clear history")
            }
        }
    }
    
    // Helper to get currency symbol for display
    private fun getCurrencySymbol(currencyCode: String): String {
        return when {
            currencyCode.contains("USDT") || currencyCode.contains("USD") -> "$"
            currencyCode.contains("EUR") -> "€"
            currencyCode.contains("GBP") -> "£"
            currencyCode.contains("TRY") -> "₺"
            currencyCode.contains("JPY") -> "¥"
            currencyCode.contains("BTC") -> "₿"
            else -> ""
        }
    }
    
    // Para birimi sembolünden asıl para birimini çıkar
    private fun extractBaseCurrency(symbol: String): String {
        return when {
            // BTCUSDT -> BTC
            symbol.endsWith("USDT") && !symbol.startsWith("USDT") -> 
                symbol.removeSuffix("USDT")
            
            // USDTTRY -> TRY
            symbol.startsWith("USDT") && symbol != "USDT" -> 
                symbol.removePrefix("USDT")
                
            // Diğer durumlar için olduğu gibi bırak
            else -> symbol
        }
    }
    
    // UI'da gösterilecek temiz sembol
    fun getDisplaySymbol(currency: Currency?): String {
        if (currency == null) return ""
        return extractBaseCurrency(currency.symbol)
    }
}

// UI state for the converter screen
sealed class ConverterUiState {
    object Loading : ConverterUiState()
    object Success : ConverterUiState()
    object ConversionSaved : ConverterUiState()
    data class Error(val message: String) : ConverterUiState()
}

// Result of a conversion calculation
data class ConversionResult(
    val result: Double,
    val rate: Double,
    val symbol: String
) 