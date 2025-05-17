package com.aftekeli.currencytracker.ui.screens.converter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.aftekeli.currencytracker.domain.model.ConversionResult
import com.aftekeli.currencytracker.domain.model.CurrencyInfo
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertScreen(
    navController: NavController,
    viewModel: ConvertViewModel = hiltViewModel()
) {
    // Collect states from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val cryptoCurrencies by viewModel.cryptoCurrencies.collectAsState()
    val fiatCurrencies by viewModel.fiatCurrencies.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val conversionResult by viewModel.conversionResult.collectAsState()
    
    // Dialog states
    var showFromCurrencyDialog by remember { mutableStateOf(false) }
    var showToCurrencyDialog by remember { mutableStateOf(false) }
    var showAllCurrencies by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Loading full currency lists if showing all currencies
    LaunchedEffect(showAllCurrencies) {
        if (showAllCurrencies) {
            viewModel.loadAllCryptoCurrencies()
            viewModel.loadAllFiatCurrencies()
        }
    }
    
    // Currency Selection Dialog for "From" currency
    if (showFromCurrencyDialog) {
        CurrencySelectionDialog(
            title = "Select From Currency",
            cryptoCurrencies = cryptoCurrencies,
            fiatCurrencies = fiatCurrencies,
            onCurrencySelected = { 
                viewModel.setFromCurrency(it)
                showFromCurrencyDialog = false
                searchQuery = ""
            },
            onDismiss = { 
                showFromCurrencyDialog = false 
                searchQuery = ""
            },
            showAllCurrencies = showAllCurrencies,
            onToggleShowAll = { showAllCurrencies = it },
            searchQuery = searchQuery,
            onSearchQueryChanged = { searchQuery = it },
            isLoadingMore = uiState.isLoadingAllCurrencies
        )
    }
    
    // Currency Selection Dialog for "To" currency
    if (showToCurrencyDialog) {
        CurrencySelectionDialog(
            title = "Select To Currency",
            cryptoCurrencies = cryptoCurrencies,
            fiatCurrencies = fiatCurrencies,
            onCurrencySelected = { 
                viewModel.setToCurrency(it)
                showToCurrencyDialog = false 
                searchQuery = ""
            },
            onDismiss = { 
                showToCurrencyDialog = false 
                searchQuery = ""
            },
            showAllCurrencies = showAllCurrencies,
            onToggleShowAll = { showAllCurrencies = it },
            searchQuery = searchQuery,
            onSearchQueryChanged = { searchQuery = it },
            isLoadingMore = uiState.isLoadingAllCurrencies
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
                    // Refresh button
                    IconButton(onClick = { viewModel.refreshConversion() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Converter Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Currency Selection Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // From Currency
                            CurrencySelector(
                                label = "From",
                                currency = uiState.fromCurrency,
                                onClick = { showFromCurrencyDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Swap Button
                            IconButton(
                                onClick = { viewModel.swapCurrencies() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Swap Currencies",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // To Currency
                            CurrencySelector(
                                label = "To",
                                currency = uiState.toCurrency,
                                onClick = { showToCurrencyDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Amount Input
                        Column {
                            Text(
                                text = "Amount",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { viewModel.setAmount(it) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                leadingIcon = {
                                    val fromCurrency = uiState.fromCurrency
                                    if (fromCurrency != null) {
                                        Text(
                                            text = fromCurrency.symbol,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Result
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Show different UI based on conversion result
                            when (val result = conversionResult) {
                                is ConversionResult.Loading -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(48.dp)
                                    )
                                    
                                    if (result.isInitialLoad) {
                                        Text(
                                            text = "Loading initial data...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 16.dp)
                                        )
                                    }
                                }
                                
                                is ConversionResult.Error -> {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    
                                    Text(
                                        text = result.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 16.dp)
                                    )
                                }
                                
                                is ConversionResult.Success -> {
                                    Text(
                                        text = "Result",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Result Amount
                                    Text(
                                        text = formatAmount(result.toAmount),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Text(
                                        text = result.rate.toCurrency.symbol,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Exchange Rate
                                    Text(
                                        text = "1 ${result.rate.fromCurrency.symbol} = ${formatAmount(result.rate.rate)} ${result.rate.toCurrency.symbol}",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Information chips
                AnimatedVisibility(
                    visible = conversionResult is ConversionResult.Success,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Data from CoinGecko API",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = "Auto-refreshes every minute",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Error state
            uiState.error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(text = errorMessage)
                }
            }
            
            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun CurrencySelector(
    label: String,
    currency: CurrencyInfo?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Currency Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (currency != null && currency.imageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currency.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Currency logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                } else if (currency != null) {
                    val firstChar = currency.symbol.take(1)
                    Text(
                        text = firstChar,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = "Select Currency",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Currency Name/Symbol
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currency?.symbol ?: "Select",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (currency != null) {
                    Text(
                        text = currency.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectionDialog(
    title: String,
    cryptoCurrencies: List<CurrencyInfo>,
    fiatCurrencies: List<CurrencyInfo>,
    onCurrencySelected: (CurrencyInfo) -> Unit,
    onDismiss: () -> Unit,
    showAllCurrencies: Boolean,
    onToggleShowAll: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    isLoadingMore: Boolean
) {
    // Filter currencies by search query
    val filteredCrypto = remember(cryptoCurrencies, searchQuery) {
        if (searchQuery.isBlank()) {
            cryptoCurrencies
        } else {
            cryptoCurrencies.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.symbol.contains(searchQuery, ignoreCase = true) 
            }
        }
    }
    
    val filteredFiat = remember(fiatCurrencies, searchQuery) {
        if (searchQuery.isBlank()) {
            fiatCurrencies
        } else {
            fiatCurrencies.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.symbol.contains(searchQuery, ignoreCase = true) 
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog header
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    },
                    actions = {
                        Switch(
                            checked = showAllCurrencies,
                            onCheckedChange = onToggleShowAll,
                            thumbContent = {
                                Icon(
                                    imageVector = if (showAllCurrencies) 
                                        Icons.Default.Done else Icons.Default.MoreVert,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        )
                        
                        Text(
                            text = if (showAllCurrencies) "All" else "Popular",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                )
                
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search currencies...") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = "Search"
                        ) 
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
                
                // Show loading indicator if loading more currencies
                if (isLoadingMore) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                
                // Currency list with sections
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Crypto section
                    if (filteredCrypto.isNotEmpty()) {
                        item {
                            Text(
                                text = "Cryptocurrencies",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(filteredCrypto) { currency ->
                            CurrencyListItem(currency = currency, onClick = onCurrencySelected)
                        }
                    }
                    
                    // Fiat section
                    if (filteredFiat.isNotEmpty()) {
                        item {
                            Text(
                                text = "Fiat Currencies",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .padding(top = 16.dp)
                            )
                        }
                        
                        items(filteredFiat) { currency ->
                            CurrencyListItem(currency = currency, onClick = onCurrencySelected)
                        }
                    }
                    
                    // No results message
                    if (filteredCrypto.isEmpty() && filteredFiat.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No currencies match your search",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyListItem(
    currency: CurrencyInfo,
    onClick: (CurrencyInfo) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(currency) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Currency Logo
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (currency.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currency.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Currency logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = currency.symbol.take(1),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Currency info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.symbol,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = currency.name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        // Currency tag
        Box(
            modifier = Modifier
                .background(
                    if (currency.isFiat) 
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f) 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (currency.isFiat) "Fiat" else "Crypto",
                style = MaterialTheme.typography.labelSmall,
                color = if (currency.isFiat) 
                    MaterialTheme.colorScheme.tertiary
                else 
                    MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Format amount for display
 */
private fun formatAmount(amount: Double): String {
    val formatter = NumberFormat.getInstance(Locale.getDefault())
    
    return when {
        amount >= 1_000_000_000 -> {
            formatter.maximumFractionDigits = 2
            "${formatter.format(amount / 1_000_000_000)}B"
        }
        amount >= 1_000_000 -> {
            formatter.maximumFractionDigits = 2
            "${formatter.format(amount / 1_000_000)}M"
        }
        amount >= 1_000 -> {
            formatter.maximumFractionDigits = 2
            "${formatter.format(amount / 1_000)}K"
        }
        amount == 0.0 -> "0"
        // For very small numbers
        amount < 0.000001 -> {
            val scientificFormat = String.format("%.8e", amount)
            val parts = scientificFormat.split("e")
            val value = parts[0].toDoubleOrNull()
            val exponent = parts[1].toIntOrNull() ?: 0
            
            formatter.maximumFractionDigits = 4
            "${formatter.format(value)}e$exponent"
        }
        // For normal decimal numbers
        else -> {
            val digits = when {
                amount < 0.0001 -> 8
                amount < 0.01 -> 6
                amount < 1 -> 4
                else -> 2
            }
            formatter.maximumFractionDigits = digits
            formatter.format(amount)
        }
    }
} 