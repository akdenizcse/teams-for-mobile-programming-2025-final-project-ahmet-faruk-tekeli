package com.aftekeli.currencytracker.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.ui.screens.home.HomeUiState
import com.aftekeli.currencytracker.ui.screens.home.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyListScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Content based on state
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is HomeUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hiç para birimi bulunamadı. Yenilemek için ekranı çekin.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            is HomeUiState.Success -> {
                if (state.currencies.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bu filtrelere uygun para birimi bulunamadı.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    CurrencyList(
                        currencies = state.currencies,
                        onCurrencyClick = { symbol -> 
                            navController.navigate("currency_detail/$symbol")
                        },
                        onFavoriteToggle = { symbol, isFavorite ->
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            userId?.let {
                                if (isFavorite) {
                                    viewModel.removeFromWatchlist(it, symbol)
                                } else {
                                    viewModel.addToWatchlist(it, symbol)
                                }
                            }
                        }
                    )
                }
            }
            
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(onClick = { viewModel.loadCurrencies() }) {
                            Text("Yeniden Dene")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyList(
    currencies: List<Currency>,
    onCurrencyClick: (String) -> Unit,
    onFavoriteToggle: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(currencies) { currency ->
            EnhancedCurrencyItem(
                currency = currency,
                onClick = { onCurrencyClick(currency.symbol) },
                onFavoriteClick = { onFavoriteToggle(currency.symbol, currency.isFavorite) },
                isCrypto = !isFiatCurrency(currency.symbol)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCurrencyItem(
    currency: Currency,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    isCrypto: Boolean
) {
    val changePercent = currency.changePercent24h ?: 0.0
    val changePositive = changePercent >= 0
    
    // Different background colors for crypto and fiat currencies
    val cardBackground = if (isCrypto) {
        Color(0xFF1F2937) // Darker background for crypto
    } else {
        Color(0xFFF0F1F6) // Lighter background for fiat
    }
    
    // Different text colors based on card background
    val textColor = if (isCrypto) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = cardBackground,
            contentColor = textColor,
            disabledContainerColor = cardBackground,
            disabledContentColor = textColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header (Symbol and Favorite)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo ve Para Birimi
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo from internet using Coil
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isCrypto) Color(0xFF2D3748) else Color(0xFFE7E5E5))
                            .border(1.dp, Color(0x20000000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Use currency.logoUrl directly from database instead of getCurrencyLogoUrl function
                        if (currency.logoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currency.logoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Currency logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(2.dp)
                            )
                        } else {
                            // Fallback if no logo in database, then try getCurrencyLogoUrl
                            val logoUrl = getCurrencyLogoUrl(currency.symbol, isCrypto)
                            
                            if (logoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(logoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Currency logo",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(2.dp)
                                )
                            } else {
                                // Final fallback - show text
                                Text(
                                    text = getCurrencySymbolText(currency.symbol),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCrypto) Color.White else textColor
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Currency pair
            Column {
                Text(
                            text = formatCurrencyPair(currency.symbol),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        
                        // Tam isim - daha açıklayıcı 
                Text(
                            text = getCurrencyName(currency.symbol),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCrypto) Color(0xBBFFFFFF) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Favorite Icon button
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (currency.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (currency.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (currency.isFavorite) Color(0xFFFF6B6B) else if (isCrypto) Color(0xFFD1D5DB) else Color(0xFF616161),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Bottom row with enhanced data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini chart - simplified representation
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (changePositive) Color(0x1F4CAF50) else Color(0x1FE53935)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (changePositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = if (changePositive) "Uptrend" else "Downtrend",
                        tint = if (changePositive) Color(0xFF4CAF50) else Color(0xFFE53935),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Price and change column
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                Text(
                        text = formatPrice(currency.price, currency.symbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    
                Text(
                        text = formatChangePercent(changePercent),
                    style = MaterialTheme.typography.bodyMedium,
                        color = if (changePositive) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                    
                    // Today's volume or market cap if available
                    currency.volume24h?.let { volume ->
                        Text(
                            text = "Vol: ${formatCompactNumber(volume)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCrypto) Color(0xBBFFFFFF) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Helper function to format numbers in a compact way
private fun formatCompactNumber(number: Double): String {
    return when {
        number >= 1_000_000_000 -> String.format("$%.2fB", number / 1_000_000_000)
        number >= 1_000_000 -> String.format("$%.2fM", number / 1_000_000)
        number >= 1_000 -> String.format("$%.2fK", number / 1_000)
        else -> String.format("$%.2f", number)
    }
}

// Get short text for display in the circle (instead of emojis)
private fun getCurrencySymbolText(symbol: String): String {
    return when {
        // Extract the first 2-3 characters for the symbol
        symbol.startsWith("BTC") -> "BTC"
        symbol.startsWith("ETH") -> "ETH"
        symbol.startsWith("XRP") -> "XRP"
        symbol.startsWith("LTC") -> "LTC"
        symbol.startsWith("BCH") -> "BCH"
        symbol.startsWith("ADA") -> "ADA"
        symbol.startsWith("DOT") -> "DOT"
        symbol.startsWith("LINK") -> "LINK"
        symbol.startsWith("BNB") -> "BNB"
        symbol.startsWith("XLM") -> "XLM"
        symbol.startsWith("DOGE") -> "DOGE"
        
        // Fiat currencies
        symbol.startsWith("USD") || symbol.endsWith("USDT") -> "USD"
        symbol.startsWith("EUR") -> "EUR"
        symbol.startsWith("GBP") -> "GBP"
        symbol.startsWith("JPY") -> "JPY"
        symbol.startsWith("AUD") -> "AUD"
        symbol.startsWith("CAD") -> "CAD"
        symbol.startsWith("CHF") -> "CHF"
        symbol.startsWith("CNY") -> "CNY"
        symbol.startsWith("HKD") -> "HKD"
        symbol.startsWith("NZD") -> "NZD"
        symbol.startsWith("SEK") -> "SEK"
        symbol.startsWith("KRW") -> "KRW"
        symbol.startsWith("SGD") -> "SGD"
        symbol.startsWith("NOK") -> "NOK"
        symbol.startsWith("MXN") -> "MXN"
        symbol.startsWith("INR") -> "INR"
        symbol.startsWith("RUB") -> "RUB"
        symbol.startsWith("ZAR") -> "ZAR"
        symbol.startsWith("TRY") -> "TRY"
        symbol.startsWith("BRL") -> "BRL"
        symbol.startsWith("IDR") -> "IDR"
        
        // For crypto pairs, extract first part of the pair
        else -> symbol.take(4)
    }
}

// Function to determine if a currency is fiat or crypto
private fun isFiatCurrency(symbol: String): Boolean {
    val fiatSymbols = listOf(
        "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", 
        "HKD", "NZD", "SEK", "KRW", "SGD", "NOK", "MXN", 
        "INR", "RUB", "ZAR", "TRY", "BRL", "IDR", "ARS", "UAH"
    )
    
    // Check for known stablecoins
    val stablecoins = listOf(
        "USDT", "USDC", "DAI", "BUSD", "TUSD"
    )
    
    // Consider stablecoins as fiat currencies for UI display purposes
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

// Helper functions for currency formatting
private fun getCurrencyFlag(symbol: String): String {
    return when {
        // Crypto currency symbols
        symbol.startsWith("BTC") -> "₿" // Bitcoin symbol
        symbol.startsWith("ETH") -> "Ξ" // Ethereum symbol
        symbol.startsWith("XRP") -> "✕" // Ripple symbol
        symbol.startsWith("LTC") -> "Ł" // Litecoin symbol
        symbol.startsWith("BCH") -> "₿" // Bitcoin Cash
        symbol.startsWith("ADA") -> "₳" // Cardano
        symbol.startsWith("DOT") -> "◉" // Polkadot
        symbol.startsWith("LINK") -> "⬡" // Chainlink
        symbol.startsWith("BNB") -> "BNB" // Binance Coin
        symbol.startsWith("XLM") -> "✻" // Stellar
        symbol.startsWith("DOGE") -> "Ð" // Dogecoin
        
        // Fiat currency flags
        symbol.startsWith("USD") || symbol.endsWith("USDT") -> "🇺🇸" // US Dollar
        symbol.startsWith("EUR") -> "🇪🇺" // Euro
        symbol.startsWith("GBP") -> "🇬🇧" // British Pound
        symbol.startsWith("JPY") -> "🇯🇵" // Japanese Yen
        symbol.startsWith("AUD") -> "🇦🇺" // Australian Dollar
        symbol.startsWith("CAD") -> "🇨🇦" // Canadian Dollar
        symbol.startsWith("CHF") -> "🇨🇭" // Swiss Franc
        symbol.startsWith("CNY") -> "🇨🇳" // Chinese Yuan
        symbol.startsWith("HKD") -> "🇭🇰" // Hong Kong Dollar
        symbol.startsWith("NZD") -> "🇳🇿" // New Zealand Dollar
        symbol.startsWith("SEK") -> "🇸🇪" // Swedish Krona
        symbol.startsWith("KRW") -> "🇰🇷" // South Korean Won
        symbol.startsWith("SGD") -> "🇸🇬" // Singapore Dollar
        symbol.startsWith("NOK") -> "🇳🇴" // Norwegian Krone
        symbol.startsWith("MXN") -> "🇲🇽" // Mexican Peso
        symbol.startsWith("INR") -> "🇮🇳" // Indian Rupee
        symbol.startsWith("RUB") -> "🇷🇺" // Russian Ruble
        symbol.startsWith("ZAR") -> "🇿🇦" // South African Rand
        symbol.startsWith("TRY") -> "🇹🇷" // Turkish Lira
        symbol.startsWith("BRL") -> "🇧🇷" // Brazilian Real
        symbol.startsWith("IDR") -> "🇮🇩" // Indonesian Rupiah
        
        // Use crypto symbol for other crypto currencies
        else -> "₵" // Generic crypto symbol
    }
}

private fun getCurrencyName(symbol: String): String {
    return when {
        // USDT ile başlayan veya biten özel durumları kontrol et
        symbol.startsWith("USDT") -> {
            val counterCurrency = symbol.removePrefix("USDT")
            return when (counterCurrency) {
                "BRL" -> "Brazilian Real"
                "RUB" -> "Russian Ruble"
                "TRY" -> "Turkish Lira"
                "BIDR" -> "Indonesian Rupiah"
                "UAH" -> "Ukrainian Hryvnia"
                "ARS" -> "Argentine Peso"
                "BKRW" -> "South Korean Won"
                "VND" -> "Vietnamese Dong"
                else -> counterCurrency
            }
        }
        
        // Özel çift başına göre adlandırma
        symbol == "TRYUSDT" -> "Turkish Lira"
        symbol == "EURUSDT" -> "Euro"
        symbol == "GBPUSDT" -> "British Pound"
        symbol == "JPYUSDT" -> "Japanese Yen"
        symbol == "RUBUSDT" -> "Russian Ruble"
        symbol == "BRLUSDT" -> "Brazilian Real"
        symbol == "AUDUSDT" -> "Australian Dollar"
        symbol == "MXNUSDT" -> "Mexican Peso"
        symbol == "ZARUSDT" -> "South African Rand"
        symbol == "INRUSDT" -> "Indian Rupee"
        symbol == "CADUSDT" -> "Canadian Dollar"
        symbol == "CHFUSDT" -> "Swiss Franc"
        symbol == "PLNUSDT" -> "Polish Zloty"
        symbol == "UAHUSDT" -> "Ukrainian Hryvnia"
        symbol == "NGNUSDT" -> "Nigerian Naira"
        
        // Kripto paralar
        symbol.startsWith("BTC") -> "Bitcoin"
        symbol.startsWith("ETH") -> "Ethereum"
        symbol.startsWith("XRP") -> "Ripple"
        symbol.startsWith("LTC") -> "Litecoin"
        symbol.startsWith("BCH") -> "Bitcoin Cash"
        symbol.startsWith("ADA") -> "Cardano"
        symbol.startsWith("DOT") -> "Polkadot"
        symbol.startsWith("LINK") -> "Chainlink"
        symbol.startsWith("BNB") -> "Binance Coin"
        symbol.startsWith("XLM") -> "Stellar"
        symbol.startsWith("DOGE") -> "Dogecoin"
        
        // Stablecoin'ler
        symbol.startsWith("USDT") -> "Tether"
        symbol.startsWith("USDC") -> "USD Coin"
        symbol.startsWith("BUSD") -> "Binance USD"
        symbol.startsWith("TUSD") -> "TrueUSD"
        symbol.startsWith("DAI") -> "Dai"
        
        // Bilinen fiat para birimleri
        symbol.startsWith("USD") || symbol.endsWith("USDT") -> "US Dollar"
        symbol.startsWith("EUR") -> "Euro"
        symbol.startsWith("GBP") -> "British Pound"
        symbol.startsWith("JPY") -> "Japanese Yen"
        symbol.startsWith("AUD") -> "Australian Dollar"
        symbol.startsWith("CAD") -> "Canadian Dollar"
        symbol.startsWith("CHF") -> "Swiss Franc"
        symbol.startsWith("CNY") -> "Chinese Yuan"
        symbol.startsWith("HKD") -> "Hong Kong Dollar"
        symbol.startsWith("NZD") -> "New Zealand Dollar"
        symbol.startsWith("SEK") -> "Swedish Krona"
        symbol.startsWith("KRW") -> "South Korean Won"
        symbol.startsWith("SGD") -> "Singapore Dollar"
        symbol.startsWith("NOK") -> "Norwegian Krone"
        symbol.startsWith("MXN") -> "Mexican Peso"
        symbol.startsWith("INR") -> "Indian Rupee"
        symbol.startsWith("RUB") -> "Russian Ruble"
        symbol.startsWith("ZAR") -> "South African Rand"
        symbol.startsWith("TRY") -> "Turkish Lira"
        symbol.startsWith("BRL") -> "Brazilian Real"
        symbol.startsWith("IDR") -> "Indonesian Rupiah"
        symbol.startsWith("PLN") -> "Polish Zloty"
        symbol.startsWith("UAH") -> "Ukrainian Hryvnia"
        symbol.startsWith("NGN") -> "Nigerian Naira"
        
        // Diğer durumlar için sembolden çıkar
        symbol.endsWith("USDT") -> formatCurrencyName(symbol)
        symbol.endsWith("BTC") -> formatCurrencyName(symbol)
        else -> formatCurrencyName(symbol)
    }
}

private fun getCurrencyCode(symbol: String): String {
    return when {
        // For cryptocurrencies, show in format CRYPTO/USD
        symbol.endsWith("USDT") -> {
            val base = symbol.removeSuffix("USDT")
            "$base/USD"
        }
        symbol.endsWith("BTC") -> {
            val base = symbol.removeSuffix("BTC")
            "$base/BTC"
        }
        // For fiat currencies
        symbol.startsWith("USD") -> "USD"
        symbol.startsWith("EUR") -> "EUR"
        symbol.startsWith("GBP") -> "GBP"
        symbol.startsWith("JPY") -> "JPY"
        symbol.startsWith("AUD") -> "AUD"
        symbol.startsWith("CAD") -> "CAD"
        symbol.startsWith("CHF") -> "CHF"
        symbol.startsWith("CNY") -> "CNY"
        symbol.startsWith("TRY") -> "TRY"
        // For other symbols
        else -> symbol
    }
}

private fun formatCurrencyName(symbol: String): String {
    // Extract base currency name from the symbol
    return when {
        symbol.endsWith("USDT") -> symbol.removeSuffix("USDT")
        symbol.endsWith("BTC") -> symbol.removeSuffix("BTC")
        else -> symbol
    }
}

// Format price with appropriate precision
private fun formatPrice(price: Double, symbol: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    
    // Fiat ve kripto para birimleri için farklı ondalık gösterimi
    if (isFiatCurrency(symbol)) {
        // Fiat para birimleri için sabit 2 ondalık basamak
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = 2
    } else {
        // Kripto para birimleri için dinamik ondalık basamak
        if (price < 0.01) {
            format.maximumFractionDigits = 6
        } else if (price < 1.0) {
            format.maximumFractionDigits = 4
        } else if (price > 1000) {
            format.maximumFractionDigits = 0
        } else {
            format.maximumFractionDigits = 2
        }
    }
    
    return format.format(price)
}

private fun formatChangePercent(percent: Double): String {
    val prefix = if (percent >= 0) "+" else ""
    return "$prefix${String.format("%.2f", percent)}%"
}

// Format currency pair - API formatını kullanıcı dostu formata çevirir
private fun formatCurrencyPair(symbol: String): String {
    return when {
        // Normal çiftler (BTCUSDT, EURUSDT, GBPUSDT, AUDUSDT)
        symbol.endsWith("USDT") -> {
            val base = symbol.removeSuffix("USDT")
            if (base.isEmpty()) "USDT/USD" else "$base/USD"
        }
        
        // Ters çiftler (USDTBRL, USDTRUB, USDTTRY, vs.)
        symbol.startsWith("USDT") -> {
            val counter = symbol.removePrefix("USDT")
            if (counter.isEmpty()) "USDT/USD" else "$counter/USD"
        }
        
        // BTC ile biten çiftler
        symbol.endsWith("BTC") -> {
            val base = symbol.removeSuffix("BTC")
            "$base/BTC"
        }
        
        // EUR ile biten çiftler
        symbol.endsWith("EUR") -> {
            val base = symbol.removeSuffix("EUR")
            "$base/EUR"
        }
        
        // Diğer tüm durumlar için sembolü olduğu gibi göster
        else -> symbol
    }
}

// Get currency logo URL with enhanced flag support
private fun getCurrencyLogoUrl(symbol: String, isCrypto: Boolean): String {
    // Extract base currency code
    val currencyCode = when {
        // For pairs that end with USDT, extract the base currency
        symbol.endsWith("USDT") -> symbol.removeSuffix("USDT").lowercase()
        
        // For reversed pairs like USDTBRL, extract the counter currency
        symbol.startsWith("USDT") -> symbol.removePrefix("USDT").lowercase()
        
        // For BTC pairs
        symbol.endsWith("BTC") -> symbol.removeSuffix("BTC").lowercase()
        
        // For EUR pairs
        symbol.endsWith("EUR") -> symbol.removeSuffix("EUR").lowercase()
        
        // Default case, just take the first part of the symbol
        else -> symbol.lowercase().take(3)
    }
    
    // For cryptocurrencies, use CryptoLogos - high quality logos
    if (isCrypto) {
        return when (currencyCode) {
            "btc" -> "https://cryptologos.cc/logos/bitcoin-btc-logo.png?v=029"
            "eth" -> "https://cryptologos.cc/logos/ethereum-eth-logo.png?v=029"
            "xrp" -> "https://cryptologos.cc/logos/xrp-xrp-logo.png?v=029"
            "ltc" -> "https://cryptologos.cc/logos/litecoin-ltc-logo.png?v=029"
            "bch" -> "https://cryptologos.cc/logos/bitcoin-cash-bch-logo.png?v=029"
            "bnb" -> "https://cryptologos.cc/logos/bnb-bnb-logo.png?v=029"
            "doge" -> "https://cryptologos.cc/logos/dogecoin-doge-logo.png?v=029"
            "ada" -> "https://cryptologos.cc/logos/cardano-ada-logo.png?v=029"
            "sol" -> "https://cryptologos.cc/logos/solana-sol-logo.png?v=029"
            "dot" -> "https://cryptologos.cc/logos/polkadot-new-dot-logo.png?v=029"
            "shib" -> "https://cryptologos.cc/logos/shiba-inu-shib-logo.png?v=029"
            "matic" -> "https://cryptologos.cc/logos/polygon-matic-logo.png?v=029"
            "avax" -> "https://cryptologos.cc/logos/avalanche-avax-logo.png?v=029"
            "link" -> "https://cryptologos.cc/logos/chainlink-link-logo.png?v=029"
            "uni" -> "https://cryptologos.cc/logos/uniswap-uni-logo.png?v=029"
            "atom" -> "https://cryptologos.cc/logos/cosmos-atom-logo.png?v=029"
            "usdt" -> "https://cryptologos.cc/logos/tether-usdt-logo.png?v=029"
            "usdc" -> "https://cryptologos.cc/logos/usd-coin-usdc-logo.png?v=029"
            "busd" -> "https://cryptologos.cc/logos/binance-usd-busd-logo.png?v=029"
            "dai" -> "https://cryptologos.cc/logos/multi-collateral-dai-dai-logo.png?v=029"
            "tusd" -> "https://cryptologos.cc/logos/trueusd-tusd-logo.png?v=029"
            else -> "" // Fallback - empty string if no logo found
        }
    }
    
    // For fiat currencies, use country flag icons
    return when (currencyCode) {
        "usd" -> "https://wise.com/public-resources/assets/flags/rectangle/usd.png" 
        "eur" -> "https://wise.com/public-resources/assets/flags/rectangle/eur.png"
        "gbp" -> "https://wise.com/public-resources/assets/flags/rectangle/gbp.png"
        "jpy" -> "https://wise.com/public-resources/assets/flags/rectangle/jpy.png"
        "aud" -> "https://wise.com/public-resources/assets/flags/rectangle/aud.png"
        "cad" -> "https://wise.com/public-resources/assets/flags/rectangle/cad.png"
        "chf" -> "https://wise.com/public-resources/assets/flags/rectangle/chf.png"
        "cny" -> "https://wise.com/public-resources/assets/flags/rectangle/cny.png"
        "try" -> "https://wise.com/public-resources/assets/flags/rectangle/try.png"
        "rub" -> "https://wise.com/public-resources/assets/flags/rectangle/rub.png"
        "inr" -> "https://wise.com/public-resources/assets/flags/rectangle/inr.png"
        "brl" -> "https://wise.com/public-resources/assets/flags/rectangle/brl.png"
        "krw" -> "https://wise.com/public-resources/assets/flags/rectangle/krw.png"
        "idr" -> "https://wise.com/public-resources/assets/flags/rectangle/idr.png"
        "hkd" -> "https://wise.com/public-resources/assets/flags/rectangle/hkd.png"
        "mxn" -> "https://wise.com/public-resources/assets/flags/rectangle/mxn.png"
        "sgd" -> "https://wise.com/public-resources/assets/flags/rectangle/sgd.png"
        "zar" -> "https://wise.com/public-resources/assets/flags/rectangle/zar.png"
        "sek" -> "https://wise.com/public-resources/assets/flags/rectangle/sek.png"
        "nok" -> "https://wise.com/public-resources/assets/flags/rectangle/nok.png"
        "nzd" -> "https://wise.com/public-resources/assets/flags/rectangle/nzd.png"
        "pln" -> "https://wise.com/public-resources/assets/flags/rectangle/pln.png"
        "uah" -> "https://wise.com/public-resources/assets/flags/rectangle/uah.png"
        "ngn" -> "https://wise.com/public-resources/assets/flags/rectangle/ngn.png"
        "ars" -> "https://wise.com/public-resources/assets/flags/rectangle/ars.png"
        "vnd" -> "https://wise.com/public-resources/assets/flags/rectangle/vnd.png"
        "bidr" -> "https://wise.com/public-resources/assets/flags/rectangle/idr.png" // BIDR is Indonesian Rupiah
        "bkrw" -> "https://wise.com/public-resources/assets/flags/rectangle/krw.png" // BKRW is Korean Won
        else -> "" // Return empty string if not found
    }
}

// Helper function to check if a currency code is a cryptocurrency
private fun isCryptoCurrency(currencyCode: String): Boolean {
    val fiatCodes = listOf(
        "usd", "eur", "gbp", "jpy", "aud", "cad", "chf", "cny", 
        "hkd", "nzd", "sek", "krw", "sgd", "nok", "mxn", 
        "inr", "rub", "zar", "try", "brl", "idr"
    )
    return !fiatCodes.contains(currencyCode)
}