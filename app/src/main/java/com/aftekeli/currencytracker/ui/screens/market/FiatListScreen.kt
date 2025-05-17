package com.aftekeli.currencytracker.ui.screens.market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.ui.components.CurrencyItem
import com.aftekeli.currencytracker.ui.screens.home.FiatCurrencyViewModel
import com.aftekeli.currencytracker.ui.screens.home.FiatUiState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun FiatListScreen(
    onFiatSelected: (String) -> Unit,
    viewModel: FiatCurrencyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing = remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing.value)
    
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            isRefreshing.value = true
            viewModel.refreshData()
            isRefreshing.value = false
        }
    ) {
        when (val state = uiState) {
            is FiatUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is FiatUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No fiat currencies found",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            is FiatUiState.Success -> {
                FiatList(
                    currencies = state.currencies,
                    onFiatSelected = onFiatSelected,
                    viewModel = viewModel
                )
            }
            
            is FiatUiState.Error -> {
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

@Composable
fun FiatList(
    currencies: List<Currency>,
    onFiatSelected: (String) -> Unit,
    viewModel: FiatCurrencyViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(currencies) { currency ->
            val currencyCode = viewModel.getCurrencyCode(currency.symbol)
            val flagEmoji = viewModel.getFlagEmoji(currency.symbol)
            
            CurrencyItem(
                currency = currency.copy(name = getCurrencyName(currencyCode)),
                onItemClick = { onFiatSelected(currency.symbol) },
                onFavoriteClick = { /* No favorite functionality for fiat currencies */ },
                showFavoriteButton = false,
                flagEmoji = flagEmoji
            )
        }
    }
}

// Helper function to get currency name
private fun getCurrencyName(code: String): String {
    return when(code) {
        "USD" -> "US Dollar"
        "EUR" -> "Euro"
        "GBP" -> "British Pound"
        "AUD" -> "Australian Dollar"
        "BRL" -> "Brazilian Real"
        "RUB" -> "Russian Ruble"
        "TRY" -> "Turkish Lira"
        "UAH" -> "Ukrainian Hryvnia"
        else -> code
    }
} 