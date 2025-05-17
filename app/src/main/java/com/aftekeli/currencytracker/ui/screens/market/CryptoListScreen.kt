package com.aftekeli.currencytracker.ui.screens.market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.ui.components.CurrencyItem
import com.aftekeli.currencytracker.ui.screens.home.CryptoUiState
import com.aftekeli.currencytracker.ui.screens.home.CryptoCurrencyViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoListScreen(
    onCryptoSelected: (String) -> Unit,
    viewModel: CryptoCurrencyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val isRefreshing = remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing.value)
    
    val currentUser = FirebaseAuth.getInstance().currentUser
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Cryptocurrencies") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            singleLine = true
        )
        
        // Content
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = {
                isRefreshing.value = true
                viewModel.refreshData()
                isRefreshing.value = false
            }
        ) {
            when (val state = uiState) {
                is CryptoUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is CryptoUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No cryptocurrencies found",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                
                is CryptoUiState.Success -> {
                    val filteredCurrencies = if (searchQuery.isBlank()) {
                        state.currencies
                    } else {
                        state.currencies.filter {
                            it.symbol.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    
                    if (filteredCurrencies.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found for '$searchQuery'",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        CryptoList(
                            currencies = filteredCurrencies,
                            onCryptoSelected = onCryptoSelected,
                            onFavoriteToggle = { symbol, isFavorite ->
                                currentUser?.uid?.let { userId ->
                                    if (isFavorite) {
                                        viewModel.removeFromWatchlist(userId, symbol)
                                    } else {
                                        viewModel.addToWatchlist(userId, symbol)
                                    }
                                }
                            }
                        )
                    }
                }
                
                is CryptoUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoList(
    currencies: List<Currency>,
    onCryptoSelected: (String) -> Unit,
    onFavoriteToggle: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(currencies) { currency ->
            CurrencyItem(
                currency = currency,
                onItemClick = { onCryptoSelected(currency.symbol) },
                onFavoriteClick = { onFavoriteToggle(currency.symbol, currency.isFavorite) },
                displaySymbolPrefix = getSymbolPrefix(currency.symbol)
            )
        }
    }
}

// Helper function to format crypto symbol display
private fun getSymbolPrefix(symbol: String): String {
    return when {
        symbol.endsWith("USDT") -> {
            val base = symbol.removeSuffix("USDT")
            "$base/"
        }
        symbol.endsWith("BTC") -> {
            val base = symbol.removeSuffix("BTC")
            "$base/"
        }
        else -> ""
    }
} 