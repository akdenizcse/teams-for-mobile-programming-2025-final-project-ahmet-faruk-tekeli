package com.aftekeli.currencytracker.ui.screens.market

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun MarketScreen(
    onCurrencySelected: (String) -> Unit
) {
    val tabs = listOf("Cryptocurrencies", "Fiat Currencies")
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            // Use coroutine to animate the tab change
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            // Content
            HorizontalPager(
                count = tabs.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when(page) {
                    0 -> CryptoListScreen(onCryptoSelected = onCurrencySelected)
                    1 -> FiatListScreen(onFiatSelected = onCurrencySelected)
                }
            }
        }
    }
} 