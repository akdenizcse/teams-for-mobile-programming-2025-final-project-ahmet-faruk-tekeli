package com.aftekeli.currencytracker.ui.screen.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.aftekeli.currencytracker.data.model.PortfolioItem
import com.aftekeli.currencytracker.data.model.Transaction
import com.aftekeli.currencytracker.ui.components.PortfolioItemCard
import com.aftekeli.currencytracker.ui.components.TradingDialog
import com.aftekeli.currencytracker.ui.components.TransactionItem
import com.aftekeli.currencytracker.ui.viewmodel.PortfolioEvent
import com.aftekeli.currencytracker.ui.viewmodel.PortfolioViewModel
import java.text.DecimalFormat
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    userId: String,
    marketPrices: Map<String, Double>,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState.collectAsState().value
    val priceFormatter = remember { DecimalFormat("#,##0.00") }
    
    // Pull-to-refresh state
    val pullRefreshState = rememberPullToRefreshState()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // Trading dialog state
    var showBuyDialog by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }
    var selectedSymbol by remember { mutableStateOf("") }
    var selectedBaseAsset by remember { mutableStateOf("") }
    var selectedPrice by remember { mutableDoubleStateOf(0.0) }
    var selectedAvailableAmount by remember { mutableDoubleStateOf(0.0) }
    
    // İlk yükleme için
    LaunchedEffect(userId) {
        viewModel.loadUserData(userId)
    }
    
    // Market fiyatlarını güncelle
    LaunchedEffect(marketPrices) {
        viewModel.updateMarketPrices(marketPrices)
    }
    
    // Pull-to-refresh işlemi
    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) {
            viewModel.refreshData(userId)
            // Yenileme işlemi tamamlandı
            pullRefreshState.endRefresh()
        }
    }
    
    // Event flow'u dinle
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is PortfolioEvent.Success -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is PortfolioEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Üst kısım - Toplam değer ve bakiye
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(55.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Toplam Değer
                    Text(
                        text = "$${priceFormatter.format(state.totalValue)}",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = "Toplam Değer",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // USDT Bakiyesi ve Portföy Değeri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "USDT Bakiyesi",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "$${priceFormatter.format(state.wallet.balance)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Portföy Değeri",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "$${priceFormatter.format(state.totalPortfolioValue)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Sekme başlıkları
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                height = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        modifier = Modifier.padding(vertical = 16.dp),
                        text = { 
                            Text(
                                "Portföyüm", 
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 16.sp
                            ) 
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        modifier = Modifier.padding(vertical = 16.dp),
                        text = { 
                            Text(
                                "İşlemlerim", 
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 16.sp
                            ) 
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Sekme içeriği
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> PortfolioContent(
                        userId = userId,
                        portfolioItems = state.portfolio,
                        marketPrices = marketPrices,
                        onBuy = { symbol, baseAsset, price ->
                            selectedSymbol = symbol
                            selectedBaseAsset = baseAsset
                            selectedPrice = price
                            showBuyDialog = true
                        },
                        onSell = { symbol, baseAsset, price, availableAmount ->
                            selectedSymbol = symbol
                            selectedBaseAsset = baseAsset
                            selectedPrice = price
                            selectedAvailableAmount = availableAmount
                            showSellDialog = true
                        }
                    )
                    1 -> TransactionsContent(
                        transactions = state.transactions
                    )
                }
            }
        }
        
        // PullToRefreshContainer
        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullRefreshState,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
    
    // Al Dialog
    if (showBuyDialog) {
        TradingDialog(
            symbol = selectedSymbol,
            baseAsset = selectedBaseAsset,
            currentPrice = selectedPrice,
            walletBalance = state.wallet.balance,
            isBuy = true,
            onDismiss = { showBuyDialog = false },
            onConfirm = { amount ->
                viewModel.buyCoin(
                    userId = userId,
                    symbol = selectedSymbol,
                    baseAsset = selectedBaseAsset,
                    amount = amount,
                    price = selectedPrice
                )
                showBuyDialog = false
            }
        )
    }
    
    // Sat Dialog
    if (showSellDialog) {
        TradingDialog(
            symbol = selectedSymbol,
            baseAsset = selectedBaseAsset,
            currentPrice = selectedPrice,
            availableAmount = selectedAvailableAmount,
            isBuy = false,
            onDismiss = { showSellDialog = false },
            onConfirm = { amount ->
                viewModel.sellCoin(
                    userId = userId,
                    symbol = selectedSymbol,
                    baseAsset = selectedBaseAsset,
                    amount = amount,
                    price = selectedPrice
                )
                showSellDialog = false
            }
        )
    }
}

@Composable
fun PortfolioContent(
    userId: String,
    portfolioItems: List<PortfolioItem>,
    marketPrices: Map<String, Double>,
    onBuy: (symbol: String, baseAsset: String, price: Double) -> Unit,
    onSell: (symbol: String, baseAsset: String, price: Double, availableAmount: Double) -> Unit
) {
    if (portfolioItems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Henüz portföyünüzde coin bulunmamaktadır.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Coin detay sayfasından alım yaparak portföyünüzü oluşturabilirsiniz.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(portfolioItems.sortedByDescending { item -> 
                val currentPrice = marketPrices[item.symbol] ?: item.averageBuyPrice
                item.getCurrentValue(currentPrice)
            }) { item ->
                val currentPrice = marketPrices[item.symbol] ?: item.averageBuyPrice
                
                PortfolioItemCard(
                    portfolioItem = item,
                    currentPrice = currentPrice,
                    onBuy = {
                        onBuy(item.symbol, item.baseAsset, currentPrice)
                    },
                    onSell = {
                        onSell(item.symbol, item.baseAsset, currentPrice, item.amount)
                    }
                )
            }
        }
    }
}

@Composable
fun TransactionsContent(
    transactions: List<Transaction>
) {
    if (transactions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Henüz işlem yapmadınız.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Coin detay sayfasından alım/satım yaparak işlem geçmişi oluşturabilirsiniz.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(transactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}