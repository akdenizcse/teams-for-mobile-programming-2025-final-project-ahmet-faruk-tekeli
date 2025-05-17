package com.aftekeli.currencytracker.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.domain.repository.CurrencyRepository
import com.aftekeli.currencytracker.ui.screens.alerts.CreateAlertDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class CurrencyDetailViewModel @Inject constructor(
    val repository: CurrencyRepository
) : ViewModel() {
    
    private val _currency = MutableStateFlow<Currency?>(null)
    val currency: StateFlow<Currency?> = _currency
    
    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist: StateFlow<Boolean> = _isInWatchlist
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    fun loadCurrencyDetails(symbol: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Refresh latest data for this currency
                repository.refreshCurrency(symbol)
                
                // Get the currency data
                _currency.value = repository.getCurrencyBySymbol(symbol).firstOrNull()
                
                // Check if it's in watchlist
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                userId?.let {
                    _isInWatchlist.value = repository.isInWatchlist(it, symbol)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleWatchlist(symbol: String) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            
            if (_isInWatchlist.value) {
                repository.removeFromWatchlist(userId, symbol)
            } else {
                repository.addToWatchlist(userId, symbol)
            }
            _isInWatchlist.value = !_isInWatchlist.value
        }
    }
    
    fun createAlert(symbol: String, targetPrice: Double, isAboveTarget: Boolean) {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            repository.createAlert(userId, symbol, targetPrice, isAboveTarget)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDetailScreen(
    symbol: String,
    onBackClick: () -> Unit,
    navController: NavController? = null,
    viewModel: CurrencyDetailViewModel = hiltViewModel()
) {
    val currency by viewModel.currency.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showCreateAlertDialog by remember { mutableStateOf(false) }
    
    // Load data
    LaunchedEffect(symbol) {
        viewModel.loadCurrencyDetails(symbol)
    }
    
    if (showCreateAlertDialog) {
        currency?.let { curr ->
            CreateAlertDialog(
                currency = curr,
                onDismiss = { showCreateAlertDialog = false },
                onCreateAlert = { targetPrice, isAboveTarget ->
                    viewModel.createAlert(symbol, targetPrice, isAboveTarget)
                    showCreateAlertDialog = false
                }
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getFormattedSymbol(symbol)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // History button
                    IconButton(
                        onClick = {
                            // Navigate to history screen with this currency symbol
                            navController?.navigate("history")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "View History"
                        )
                    }
                    
                    // Alert button
                    IconButton(onClick = { showCreateAlertDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Set Price Alert"
                        )
                    }
                    
                    // Favorite button
                    IconButton(
                        onClick = { viewModel.toggleWatchlist(symbol) }
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isInWatchlist) "Remove from Watchlist" else "Add to Watchlist",
                            tint = if (isInWatchlist) Color.Red else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                currency?.let { curr ->
                    CurrencyDetailContent(currency = curr, navController = navController)
                } ?: run {
                    Text(
                        text = "Currency not found",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencyDetailContent(
    currency: Currency,
    navController: NavController? = null
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Price card
        PriceCard(currency)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats card
        StatsCard(currency)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // History button
        Button(
            onClick = {
                // Navigate to history screen with currency symbol
                navController?.navigate("history")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "View History"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Price History")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // About section
        Text(
            text = "About ${getFormattedSymbol(currency.symbol)}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Text(
            text = "This is a placeholder for detailed information about ${getFormattedSymbol(currency.symbol)}. " +
                   "In a complete app, this would include a description of the cryptocurrency, its use cases, technology, and other relevant information.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PriceCard(currency: Currency) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatPrice(currency.price, currency.symbol),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            currency.changePercent24h?.let { change ->
                val color = if (change >= 0) Color.Green else Color.Red
                val prefix = if (change >= 0) "+" else ""
                
                Text(
                    text = "$prefix${String.format("%.2f", change)}% (24h)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = color
                )
            }
        }
    }
}

@Composable
fun StatsCard(currency: Currency) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 24h Volume
            StatRow(
                label = "24h Volume",
                value = currency.volume24h?.let { formatLargeNumber(it) } ?: "-"
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Market Cap
            StatRow(
                label = "Market Cap",
                value = currency.marketCap?.let { formatLargeNumber(it) } ?: "-"
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Last Updated
            StatRow(
                label = "Last Updated",
                value = formatTimestamp(currency.lastUpdateTime)
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

// Helper functions
private fun getFormattedSymbol(symbol: String): String {
    return when {
        // Normal çiftler (BTCUSDT, EURUSDT)
        symbol.endsWith("USDT") -> {
            val base = symbol.removeSuffix("USDT")
            if (base.isEmpty()) "USDT/USD" else "$base/USD"
        }
        
        // Ters çiftler (USDTBRL, USDTRUB, USDTTRY)
        symbol.startsWith("USDT") -> {
            val counter = symbol.removePrefix("USDT")
            if (counter.isEmpty()) "USDT/USD" else "$counter/USD"
        }
        
        // BTC çiftleri
        symbol.endsWith("BTC") -> {
            val base = symbol.removeSuffix("BTC")
            "$base/BTC"
        }
        
        // EUR çiftleri
        symbol.endsWith("EUR") -> {
            val base = symbol.removeSuffix("EUR")
            "$base/EUR"
        }
        
        else -> symbol
    }
}

private fun formatPrice(price: Double, symbol: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    
    // Fiat para birimleri için sabit 3 ondalık basamak
    if (isFiatCurrency(symbol)) {
        format.maximumFractionDigits = 3
        format.minimumFractionDigits = 3
    } else {
        // Kripto para birimleri için dinamik ondalık basamak
        if (price < 0.01) {
            format.maximumFractionDigits = 6
        } else if (price < 1.0) {
            format.maximumFractionDigits = 4
        } else {
            format.maximumFractionDigits = 3
            format.minimumFractionDigits = 3
        }
    }
    
    return format.format(price)
}

private fun formatLargeNumber(number: Double): String {
    return when {
        number >= 1_000_000_000 -> String.format("$%.2fB", number / 1_000_000_000)
        number >= 1_000_000 -> String.format("$%.2fM", number / 1_000_000)
        number >= 1_000 -> String.format("$%.2fK", number / 1_000)
        else -> String.format("$%.2f", number)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    return java.text.SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(date)
}

// İlgili sembolün fiat para birimi olup olmadığını kontrol eder
private fun isFiatCurrency(symbol: String): Boolean {
    val fiatSymbols = listOf(
        "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", 
        "HKD", "NZD", "SEK", "KRW", "SGD", "NOK", "MXN", 
        "INR", "RUB", "ZAR", "TRY", "BRL", "IDR", "ARS", "UAH"
    )
    
    // Check for known stablecoins (treat as fiat for formatting)
    val stablecoins = listOf(
        "USDT", "USDC", "DAI", "BUSD", "TUSD"
    )
    
    if (stablecoins.contains(symbol)) {
        return true
    }
    
    return when {
        // Direkt fiat çiftleri (EURUSDT, GBPUSDT, AUDUSDT)
        symbol.endsWith("USDT") -> {
            val base = symbol.removeSuffix("USDT")
            fiatSymbols.contains(base)
        }
        
        // Ters fiat çiftleri (USDTRUB, USDTTRY, vb.)
        symbol.startsWith("USDT") -> {
            val counter = symbol.removePrefix("USDT")
            fiatSymbols.contains(counter)
        }
        
        // Diğer fiat sembollerine göre kontrol
        else -> {
            fiatSymbols.any { symbol.startsWith(it) }
        }
    }
} 