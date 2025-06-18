package com.aftekeli.currencytracker.ui.screen.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aftekeli.currencytracker.data.model.PortfolioItem
import com.aftekeli.currencytracker.data.model.Transaction
import com.aftekeli.currencytracker.ui.components.PortfolioItemCard
import com.aftekeli.currencytracker.ui.components.TransactionItem
import com.aftekeli.currencytracker.ui.viewmodel.PortfolioEvent
import com.aftekeli.currencytracker.ui.viewmodel.PortfolioViewModel
import java.text.DecimalFormat

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
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // İlk yükleme için
    LaunchedEffect(userId) {
        viewModel.loadUserData(userId)
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
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Üst kısım - Toplam değer ve bakiye
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Toplam Değer",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "$${priceFormatter.format(state.totalValue)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "USDT Bakiyesi",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        
                        Text(
                            text = "$${priceFormatter.format(state.wallet.balance)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Portföy Değeri",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        
                        Text(
                            text = "$${priceFormatter.format(state.totalPortfolioValue)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        // Sekme başlıkları
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Portföyüm") }
            )
            
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("İşlemlerim") }
            )
        }
        
        // Sekme içeriği
        when (selectedTabIndex) {
            0 -> PortfolioContent(
                userId = userId,
                portfolioItems = state.portfolio,
                marketPrices = marketPrices,
                onBuy = { symbol, baseAsset, price ->
                    // Al dialog göster
                },
                onSell = { symbol, baseAsset, price, availableAmount ->
                    // Sat dialog göster
                }
            )
            1 -> TransactionsContent(
                transactions = state.transactions
            )
        }
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Henüz portföyünüzde coin bulunmamaktadır.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(portfolioItems) { item ->
                val currentPrice = marketPrices[item.symbol] ?: 0.0
                
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Henüz işlem yapmadınız.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(transactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
} 