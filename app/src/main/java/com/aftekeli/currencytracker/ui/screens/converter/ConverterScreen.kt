package com.aftekeli.currencytracker.ui.screens.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aftekeli.currencytracker.R
import com.aftekeli.currencytracker.domain.model.ConversionHistory
import com.aftekeli.currencytracker.domain.model.Currency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    navController: NavController,
    viewModel: ConverterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val availableCurrencies by viewModel.availableCurrencies.collectAsState()
    val fromCurrency by viewModel.fromCurrency.collectAsState()
    val toCurrency by viewModel.toCurrency.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val conversionHistory by viewModel.conversionHistory.collectAsState()
    
    // Dialog states
    var showFromCurrencyDialog by remember { mutableStateOf(false) }
    var showToCurrencyDialog by remember { mutableStateOf(false) }
    var showConversionHistory by remember { mutableStateOf(false) }
    
    // Calculate conversion result
    val conversionResult = viewModel.calculateConversion()
    
    // Load conversion history when showing history dialog
    LaunchedEffect(showConversionHistory) {
        if (showConversionHistory) {
            // History is loaded automatically via ViewModel when user is logged in
        }
    }
    
    // Currency Selection Dialog for "From" currency
    if (showFromCurrencyDialog) {
        CurrencySelectionDialog(
            currencies = availableCurrencies,
            onCurrencySelected = { 
                viewModel.setFromCurrency(it)
                showFromCurrencyDialog = false
            },
            onDismiss = { showFromCurrencyDialog = false }
        )
    }
    
    // Currency Selection Dialog for "To" currency
    if (showToCurrencyDialog) {
        CurrencySelectionDialog(
            currencies = availableCurrencies,
            onCurrencySelected = { 
                viewModel.setToCurrency(it)
                showToCurrencyDialog = false
            },
            onDismiss = { showToCurrencyDialog = false }
        )
    }
    
    // Conversion History Dialog
    if (showConversionHistory) {
        ConversionHistoryDialog(
            conversions = conversionHistory,
            onDismiss = { showConversionHistory = false },
            onClearHistory = { viewModel.clearConversionHistory() }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currency Converter") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // History Button
                    IconButton(onClick = { showConversionHistory = true }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Conversion History"
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
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Converter Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE9ECF0)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Convert Currency Title
                        Text(
                            text = "Convert Currency",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Currency Selection Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // From Currency
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "From",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showFromCurrencyDialog = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Currency Logo
                                    fromCurrency?.let { currency ->
                                        if (currency.logoUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(currency.logoUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Currency logo",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .padding(2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                    }
                                    
                                    Text(
                                        text = fromCurrency?.let { viewModel.getDisplaySymbol(it) } ?: "Select",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Currency"
                                    )
                                }
                                
                                Divider(
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = Color.Gray
                                )
                            }
                            
                            // Swap Button
                            IconButton(
                                onClick = { viewModel.swapCurrencies() },
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Swap Currencies"
                                )
                            }
                            
                            // To Currency
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "To",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showToCurrencyDialog = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Currency Logo
                                    toCurrency?.let { currency ->
                                        if (currency.logoUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(currency.logoUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Currency logo",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .padding(2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                    }
                                    
                                    Text(
                                        text = toCurrency?.let { viewModel.getDisplaySymbol(it) } ?: "Select",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select Currency"
                                    )
                                }
                                
                                Divider(
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Amount Input
                        Column {
                            Text(
                                text = "Amount",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { viewModel.setAmount(it) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Result
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Result",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                                            Text(
                                text = "$amount ${fromCurrency?.let { viewModel.getDisplaySymbol(it) } ?: ""} = ",
                                fontSize = 20.sp,
                                color = Color.Gray
                            )
                                
                                                            Text(
                                text = formatAmount(conversionResult.result) + " ${toCurrency?.let { viewModel.getDisplaySymbol(it) } ?: ""}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "1 ${fromCurrency?.let { viewModel.getDisplaySymbol(it) } ?: ""} = ${formatAmount(conversionResult.rate)} ${toCurrency?.let { viewModel.getDisplaySymbol(it) } ?: ""}",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Save Button
                            Button(
                                onClick = { viewModel.saveConversion() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                enabled = fromCurrency != null && toCurrency != null && 
                                         amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Conversion"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Conversion")
                            }
                        }
                    }
                }
            }
            
            // UI States
            when (uiState) {
                is ConverterUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                
                is ConverterUiState.Error -> {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(text = (uiState as ConverterUiState.Error).message)
                    }
                }
                
                is ConverterUiState.ConversionSaved -> {
                    Snackbar(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text("Conversion saved successfully!")
                    }
                }
                
                else -> {
                    // Nothing to show for Success state
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionDialog(
    currencies: List<Currency>,
    onCurrencySelected: (Currency) -> Unit,
    onDismiss: () -> Unit
) {
    // Group currencies by base symbol to avoid showing duplicates
    val groupedCurrencies = remember(currencies) {
        currencies.groupBy { 
            extractDisplaySymbol(it.symbol) 
        }.mapValues { entry ->
            // For each base symbol, pick the most liquid trading pair
            entry.value.maxByOrNull { it.volume24h ?: 0.0 } ?: entry.value.first()
        }
    }
    
    // Filter currencies into crypto and fiat categories
    val cryptoCurrencies = remember(groupedCurrencies) {
        groupedCurrencies.values.filter { 
            it.symbol.endsWith("USDT") && !isFiatCurrency(it.symbol)
        }.sortedBy { extractDisplaySymbol(it.symbol) }
    }
    
    val fiatCurrencies = remember(groupedCurrencies) {
        groupedCurrencies.values.filter { 
            isFiatCurrency(it.symbol) 
        }.sortedBy { extractDisplaySymbol(it.symbol) }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog header
                TopAppBar(
                    title = { Text("Select Currency") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                )
                
                // Currency list with sections
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Crypto section
                    item {
                        Text(
                            text = "Cryptocurrencies",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(cryptoCurrencies) { currency ->
                        CurrencyListItem(currency = currency, onClick = onCurrencySelected)
                    }
                    
                    // Fiat section
                    item {
                        Text(
                            text = "Fiat Currencies",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp).padding(PaddingValues(top = 16.dp))
                        )
                    }
                    
                    items(fiatCurrencies) { currency ->
                        CurrencyListItem(currency = currency, onClick = onCurrencySelected)
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyListItem(
    currency: Currency,
    onClick: (Currency) -> Unit
) {
    // Extract the base currency symbol for display
    val displaySymbol = extractDisplaySymbol(currency.symbol)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(currency) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Currency Logo
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
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
            )
        } else {
            // Placeholder if no logo
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displaySymbol.take(1),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Currency info - show base symbol instead of trading pair
        Column {
            Text(
                text = displaySymbol,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            if (currency.priceUsd != null) {
                Text(
                    text = String.format("$%.4f", currency.priceUsd),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

// Helper function to extract display symbol from trading pair
private fun extractDisplaySymbol(symbol: String): String {
    return when {
        // For crypto pairs like BTCUSDT -> BTC
        symbol.endsWith("USDT") && !symbol.startsWith("USDT") -> 
            symbol.removeSuffix("USDT")
        
        // For fiat pairs like USDTTRY -> TRY
        symbol.startsWith("USDT") && symbol != "USDT" -> 
            symbol.removePrefix("USDT")
            
        // Special case for USDT itself
        symbol == "USDT" -> "USDT"
            
        // Other cases
        else -> symbol
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionHistoryDialog(
    conversions: List<ConversionHistory>,
    onDismiss: () -> Unit,
    onClearHistory: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog header
                TopAppBar(
                    title = { Text("Conversion History") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onClearHistory) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear History"
                            )
                        }
                    }
                )
                
                // Conversion history list
                if (conversions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No conversion history yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(conversions) { conversion ->
                            // Extract display symbols from trading pairs
                            val fromDisplaySymbol = extractDisplaySymbol(conversion.fromCurrency)
                            val toDisplaySymbol = extractDisplaySymbol(conversion.toCurrency)
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${String.format("%.2f", conversion.fromAmount)} ${fromDisplaySymbol}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Converted to"
                                        )
                                        
                                        Text(
                                            text = "${String.format("%.2f", conversion.toAmount)} ${toDisplaySymbol}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Rate: ${formatAmount(conversion.rate)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                        
                                        Text(
                                            text = dateFormat.format(conversion.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to identify fiat currency pairs
private fun isFiatCurrency(symbol: String): Boolean {
    return symbol == "EURUSDT" || 
           symbol == "GBPUSDT" ||
           symbol.startsWith("USDT") && symbol != "USDT"  // Like USDTRUB, USDTTRY
}

private fun formatAmount(amount: Double): String {
    if (amount.isNaN() || !amount.isFinite()) {
        return "N/A"
    }
    
    return when {
        amount >= 1_000_000_000 -> String.format("%.2fB", amount / 1_000_000_000)
        amount >= 1_000_000 -> String.format("%.2fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.2fK", amount / 1_000)
        amount == 0.0 -> "0"
        // For very small positive numbers
        amount < 0.000001 && amount > 0 -> {
            val scientificFormat = String.format("%.8e", amount)
            val value = scientificFormat.split("e")[0].toDoubleOrNull()
            val exponent = scientificFormat.split("e")[1].toIntOrNull()
            
            if (value != null && exponent != null) {
                val formattedValue = String.format("%.4f", value).trimEnd('0').trimEnd('.')
                "${formattedValue}e${exponent}"
            } else {
                scientificFormat
            }
        }
        // For very small negative numbers
        amount > -0.000001 && amount < 0 -> {
            val scientificFormat = String.format("%.8e", amount)
            val value = scientificFormat.split("e")[0].toDoubleOrNull()
            val exponent = scientificFormat.split("e")[1].toIntOrNull()
            
            if (value != null && exponent != null) {
                val formattedValue = String.format("%.4f", value).trimEnd('0').trimEnd('.')
                "${formattedValue}e${exponent}"
            } else {
                scientificFormat
            }
        }
        // For reasonable sized numbers, show 2-8 decimal places depending on size
        amount < 1 -> {
            val decimalPlaces = when {
                amount < 0.000001 -> 8
                amount < 0.0001 -> 6
                amount < 0.01 -> 4
                else -> 2
            }
            String.format("%.${decimalPlaces}f", amount).trimEnd('0').trimEnd('.')
        }
        // For normal numbers, show up to 6 decimals
        else -> String.format("%.6f", amount).trimEnd('0').trimEnd('.')
    }
} 