package com.aftekeli.currencytracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.model.Coin
import com.aftekeli.currencytracker.data.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val coinRepository: CoinRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConverterUiState())
    val uiState: StateFlow<ConverterUiState> = _uiState.asStateFlow()

    init {
        loadCoins()
    }
    
    private fun loadCoins() {
        _uiState.update { currentState -> currentState.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                Log.d("ConverterViewModel", "Loading coins")
                coinRepository.getMarketTickers().collect { result ->
                    result.fold(
                        onSuccess = { coins ->
                            Log.d("ConverterViewModel", "Successfully loaded ${coins.size} coins")
                            if (coins.isNotEmpty()) {
                                // Filter to coins with USDT as quote asset for consistent pricing
                                val usdtPairs = coins.filter { coin -> 
                                    coin.symbol.endsWith("USDT") && 
                                    coin.lastPrice != 0.0
                                }
                                
                                if (usdtPairs.isEmpty()) {
                                    _uiState.update { currentState -> 
                                        currentState.copy(
                                            isLoading = false,
                                            errorMessage = "No valid USDT trading pairs found"
                                        ) 
                                    }
                                    return@fold
                                }
                                
                                // Find BTC/USDT as it's a common reference
                                val btcUsdtPair = usdtPairs.find { coin -> coin.symbol == "BTCUSDT" }
                                
                                // Create a dummy USDT coin for conversion to USDT
                                val usdtCoin = Coin(
                                    symbol = "USDT",
                                    baseAsset = "USDT",
                                    quoteAsset = "USD",
                                    lastPrice = 1.0, // USDT is valued at 1 for conversion purposes
                                    priceChangePercent = 0.0,
                                    volume = 0.0,
                                    quoteVolume = 0.0,
                                    high24h = 0.0,
                                    low24h = 0.0
                                )
                                
                                // Default source to BTC, target to USDT or ETH, or use first two coins otherwise
                                val sourceCoin = btcUsdtPair ?: usdtPairs.firstOrNull()
                                val targetCoin = usdtCoin // Default target to USDT
                                
                                // Create available coins list with USDT added for target selection
                                val availableCoinsWithUsdt = usdtPairs.distinctBy { it.baseAsset } + usdtCoin
                                
                                _uiState.update { currentState -> 
                                    currentState.copy(
                                        availableCoins = availableCoinsWithUsdt,
                                        selectedSourceCoin = sourceCoin,
                                        selectedTargetCoin = targetCoin,
                                        isLoading = false
                                    ) 
                                }
                                calculateConversion()
                            } else {
                                Log.e("ConverterViewModel", "Empty coins list")
                                _uiState.update { currentState -> 
                                    currentState.copy(
                                        availableCoins = emptyList(),
                                        isLoading = false,
                                        errorMessage = "No coins available for conversion"
                                    ) 
                                }
                            }
                        },
                        onFailure = { exception ->
                            Log.e("ConverterViewModel", "Failed to load coins", exception)
                            _uiState.update { currentState -> 
                                currentState.copy(
                                    isLoading = false, 
                                    errorMessage = "Failed to load coins: ${exception.message}"
                                ) 
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("ConverterViewModel", "Exception in loadCoins", e)
                _uiState.update { currentState -> 
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "Error: ${e.message ?: "Unknown error"}"
                    ) 
                }
            }
        }
    }
    
    fun onSourceCoinSelected(coin: Coin) {
        // Don't allow same coin for source and target
        if (coin.baseAsset == _uiState.value.selectedTargetCoin?.baseAsset) {
            val availableCoins = _uiState.value.availableCoins
            // Find an alternative target coin
            val newTargetCoin = availableCoins.find { it.baseAsset != coin.baseAsset } 
                ?: availableCoins.firstOrNull()
            
            if (newTargetCoin != null && newTargetCoin.baseAsset != coin.baseAsset) {
                _uiState.update { currentState -> 
                    currentState.copy(
                        selectedSourceCoin = coin,
                        selectedTargetCoin = newTargetCoin
                    ) 
                }
            } else {
                // Just update source and keep same target if no alternative
                _uiState.update { currentState -> 
                    currentState.copy(selectedSourceCoin = coin) 
                }
            }
        } else {
            // Normal case - just update source
            _uiState.update { currentState -> 
                currentState.copy(selectedSourceCoin = coin) 
            }
        }
        calculateConversion()
    }
    
    fun onTargetCoinSelected(coin: Coin) {
        // Don't allow same coin for source and target
        if (coin.baseAsset == _uiState.value.selectedSourceCoin?.baseAsset) {
            val availableCoins = _uiState.value.availableCoins
            // Find an alternative source coin
            val newSourceCoin = availableCoins.find { it.baseAsset != coin.baseAsset } 
                ?: availableCoins.firstOrNull()
            
            if (newSourceCoin != null && newSourceCoin.baseAsset != coin.baseAsset) {
                _uiState.update { currentState -> 
                    currentState.copy(
                        selectedTargetCoin = coin,
                        selectedSourceCoin = newSourceCoin
                    ) 
                }
            } else {
                // Just update target and keep same source if no alternative
                _uiState.update { currentState -> 
                    currentState.copy(selectedTargetCoin = coin) 
                }
            }
        } else {
            // Normal case - just update target
            _uiState.update { currentState -> 
                currentState.copy(selectedTargetCoin = coin) 
            }
        }
        calculateConversion()
    }
    
    fun onSourceAmountChanged(amount: String) {
        // Validate input is a valid number or empty
        val cleanAmount = amount.replace(",", "").trim()
        val isValid = cleanAmount.isEmpty() || cleanAmount.toDoubleOrNull() != null
        
        if (isValid) {
            _uiState.update { currentState -> currentState.copy(sourceAmount = cleanAmount) }
            if (cleanAmount.isNotEmpty()) {
                calculateConversion()
            } else {
                _uiState.update { currentState -> currentState.copy(convertedAmount = null) }
            }
        }
    }
    
    fun swapCurrencies() {
        val currentSource = _uiState.value.selectedSourceCoin
        val currentTarget = _uiState.value.selectedTargetCoin
        
        if (currentSource != null && currentTarget != null) {
            _uiState.update { currentState -> 
                currentState.copy(
                    selectedSourceCoin = currentTarget,
                    selectedTargetCoin = currentSource
                )
            }
            calculateConversion()
        }
    }
    
    fun calculateConversion() {
        val sourceCoin = _uiState.value.selectedSourceCoin
        val targetCoin = _uiState.value.selectedTargetCoin
        
        // Validate we have both coins
        if (sourceCoin == null || targetCoin == null) {
            _uiState.update { currentState -> 
                currentState.copy(
                    convertedAmount = null,
                    errorMessage = "Source or target currency not selected"
                ) 
            }
            return
        }
        
        // Validate amount is valid
        val amountToConvert = _uiState.value.sourceAmount.replace(",", "").toDoubleOrNull()
        if (amountToConvert == null) {
            _uiState.update { currentState -> 
                currentState.copy(convertedAmount = null) 
            }
            return
        }
        
        // Get prices in USDT
        val sourcePriceUsdt = sourceCoin.lastPrice
        
        // Special handling for USDT as target (its price is always 1.0 USDT)
        val targetPriceUsdt = if (targetCoin.symbol == "USDT") 1.0 else targetCoin.lastPrice
        
        // Validate prices
        if (sourcePriceUsdt <= 0.0) {
            _uiState.update { currentState -> 
                currentState.copy(
                    convertedAmount = null,
                    errorMessage = "Source currency has invalid price"
                ) 
            }
            return
        }
        
        if (targetPriceUsdt <= 0.0) {
            _uiState.update { currentState -> 
                currentState.copy(
                    convertedAmount = null,
                    errorMessage = "Target currency has invalid price"
                ) 
            }
            return
        }
        
        // Calculate conversion: (amount * sourcePriceUsdt) / targetPriceUsdt
        val resultValue = (amountToConvert * sourcePriceUsdt) / targetPriceUsdt
        
        // Calculate exchange rate for display: sourcePriceUsdt / targetPriceUsdt
        val exchangeRate = sourcePriceUsdt / targetPriceUsdt
        
        _uiState.update { currentState -> 
            currentState.copy(
                convertedAmount = resultValue,
                exchangeRate = exchangeRate,
                errorMessage = null
            ) 
        }
    }
    
    fun refreshData() {
        loadCoins()
    }
}

data class ConverterUiState(
    val availableCoins: List<Coin> = emptyList(),
    val sourceAmount: String = "1", // Default input amount
    val convertedAmount: Double? = null,
    val exchangeRate: Double? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedSourceCoin: Coin? = null,
    val selectedTargetCoin: Coin? = null
) 