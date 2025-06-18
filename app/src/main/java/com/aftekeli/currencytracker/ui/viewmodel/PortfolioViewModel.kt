package com.aftekeli.currencytracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.model.PortfolioItem
import com.aftekeli.currencytracker.data.model.Transaction
import com.aftekeli.currencytracker.data.model.VirtualWallet
import com.aftekeli.currencytracker.data.repository.PortfolioRepository
import com.aftekeli.currencytracker.data.repository.TransactionRepository
import com.aftekeli.currencytracker.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Portföy sayfası için ViewModel
 */
@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val portfolioRepository: PortfolioRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()
    
    private val _eventFlow = MutableSharedFlow<PortfolioEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    
    fun loadUserData(userId: String) {
        viewModelScope.launch {
            // Kullanıcının cüzdanını izle
            walletRepository.getUserWallet(userId).collectLatest { wallet ->
                _uiState.update { state ->
                    state.copy(
                        wallet = wallet ?: VirtualWallet(userId = userId)
                    )
                }
            }
        }
        
        viewModelScope.launch {
            // Kullanıcının portföyünü izle
            portfolioRepository.getUserPortfolio(userId).collectLatest { portfolioItems ->
                _uiState.update { state ->
                    state.copy(
                        portfolio = portfolioItems
                    )
                }
            }
        }
        
        viewModelScope.launch {
            // Kullanıcının işlemlerini izle
            transactionRepository.getUserTransactions(userId).collectLatest { transactions ->
                _uiState.update { state ->
                    state.copy(
                        transactions = transactions
                    )
                }
            }
        }
    }
    
    /**
     * Kullanıcı verilerini yeniler (pull-to-refresh için)
     */
    fun refreshData(userId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // Cüzdan verisini yenile
                val wallet = walletRepository.getUserWallet(userId).first()
                
                // Portföy verilerini yenile
                val portfolio = portfolioRepository.getUserPortfolio(userId).first()
                
                // İşlem verilerini yenile
                val transactions = transactionRepository.getUserTransactions(userId).first()
                
                // UI state'i güncelle
                _uiState.update { state ->
                    state.copy(
                        wallet = wallet ?: VirtualWallet(userId = userId),
                        portfolio = portfolio,
                        transactions = transactions,
                        isLoading = false
                    )
                }
                
                _eventFlow.emit(PortfolioEvent.Success("Veriler güncellendi"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _eventFlow.emit(PortfolioEvent.Error("Veriler güncellenirken hata oluştu: ${e.message}"))
            }
        }
    }
    
    /**
     * Market fiyatlarını günceller
     */
    fun updateMarketPrices(prices: Map<String, Double>) {
        _uiState.update { state ->
            state.copy(
                marketPrices = prices
            )
        }
    }
    
    fun buyCoin(
        userId: String,
        symbol: String,
        baseAsset: String,
        amount: Double,
        price: Double
    ) {
        viewModelScope.launch {
            try {
                // Toplam harcama
                val totalSpend = amount * price
                
                // 1. Cüzdandan para harca
                val walletResult = walletRepository.spendFunds(userId, totalSpend)
                
                if (walletResult.isFailure) {
                    _eventFlow.emit(PortfolioEvent.Error(walletResult.exceptionOrNull()?.message ?: "İşlem başarısız"))
                    return@launch
                }
                
                // 2. Portföye coin ekle
                val portfolioResult = portfolioRepository.buyCoin(
                    userId = userId,
                    symbol = symbol,
                    baseAsset = baseAsset,
                    amount = amount,
                    price = price
                )
                
                if (portfolioResult.isFailure) {
                    // Hata durumunda cüzdana parayı geri yükle
                    walletRepository.addFunds(userId, totalSpend)
                    _eventFlow.emit(PortfolioEvent.Error(portfolioResult.exceptionOrNull()?.message ?: "İşlem başarısız"))
                    return@launch
                }
                
                // 3. İşlemi kaydet
                val transaction = Transaction.createBuyTransaction(
                    userId = userId,
                    symbol = symbol,
                    baseAsset = baseAsset,
                    amount = amount,
                    price = price
                )
                
                val transactionResult = transactionRepository.addTransaction(transaction)
                
                if (transactionResult.isFailure) {
                    _eventFlow.emit(PortfolioEvent.Error(transactionResult.exceptionOrNull()?.message ?: "İşlem kaydedilemedi"))
                } else {
                    _eventFlow.emit(PortfolioEvent.Success("Alım işlemi başarıyla tamamlandı"))
                }
                
            } catch (e: Exception) {
                _eventFlow.emit(PortfolioEvent.Error(e.message ?: "İşlem sırasında bir hata oluştu"))
            }
        }
    }
    
    fun sellCoin(
        userId: String,
        symbol: String,
        baseAsset: String,
        amount: Double,
        price: Double
    ) {
        viewModelScope.launch {
            try {
                // Toplam kazanç
                val totalGain = amount * price
                
                // 1. Portföyden coin çıkar
                val portfolioResult = portfolioRepository.sellCoin(
                    userId = userId,
                    symbol = symbol,
                    baseAsset = baseAsset,
                    amount = amount,
                    price = price
                )
                
                if (portfolioResult.isFailure) {
                    _eventFlow.emit(PortfolioEvent.Error(portfolioResult.exceptionOrNull()?.message ?: "İşlem başarısız"))
                    return@launch
                }
                
                // 2. Cüzdana para ekle
                val walletResult = walletRepository.depositFunds(userId, totalGain)
                
                if (walletResult.isFailure) {
                    _eventFlow.emit(PortfolioEvent.Error(walletResult.exceptionOrNull()?.message ?: "İşlem başarısız"))
                    return@launch
                }
                
                // 3. İşlemi kaydet
                val transaction = Transaction.createSellTransaction(
                    userId = userId,
                    symbol = symbol,
                    baseAsset = baseAsset,
                    amount = amount,
                    price = price
                )
                
                val transactionResult = transactionRepository.addTransaction(transaction)
                
                if (transactionResult.isFailure) {
                    _eventFlow.emit(PortfolioEvent.Error(transactionResult.exceptionOrNull()?.message ?: "İşlem kaydedilemedi"))
                } else {
                    _eventFlow.emit(PortfolioEvent.Success("Satış işlemi başarıyla tamamlandı"))
                }
                
            } catch (e: Exception) {
                _eventFlow.emit(PortfolioEvent.Error(e.message ?: "İşlem sırasında bir hata oluştu"))
            }
        }
    }
    
    fun addFunds(userId: String, amount: Double) {
        viewModelScope.launch {
            try {
                val result = walletRepository.addFunds(userId, amount)
                
                if (result.isFailure) {
                    _eventFlow.emit(PortfolioEvent.Error(result.exceptionOrNull()?.message ?: "İşlem başarısız"))
                } else {
                    _eventFlow.emit(PortfolioEvent.Success("$${amount} başarıyla eklendi"))
                }
            } catch (e: Exception) {
                _eventFlow.emit(PortfolioEvent.Error(e.message ?: "Para ekleme işlemi başarısız"))
            }
        }
    }
}

/**
 * Portföy ekranı durum sınıfı
 */
data class PortfolioUiState(
    val wallet: VirtualWallet = VirtualWallet(),
    val portfolio: List<PortfolioItem> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val marketPrices: Map<String, Double> = emptyMap()
) {
    val totalPortfolioValue: Double
        get() = portfolio.sumOf { item ->
            val currentPrice = marketPrices[item.symbol] ?: item.averageBuyPrice
            item.getCurrentValue(currentPrice)
        }
    
    val totalValue: Double
        get() = wallet.balance + totalPortfolioValue
}

/**
 * Portföy ekranı olayları
 */
sealed class PortfolioEvent {
    data class Success(val message: String) : PortfolioEvent()
    data class Error(val message: String) : PortfolioEvent()
} 