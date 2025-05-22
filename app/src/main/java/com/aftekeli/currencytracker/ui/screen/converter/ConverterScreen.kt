package com.aftekeli.currencytracker.ui.screen.converter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aftekeli.currencytracker.data.model.Coin
import com.aftekeli.currencytracker.ui.viewmodel.ConverterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.refreshData() }) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Currency Converter",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Source Currency Section
                                Text(
                                    text = "From",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CoinSelector(
                                        label = "Select Source",
                                        coins = uiState.availableCoins,
                                        selectedCoin = uiState.selectedSourceCoin,
                                        onCoinSelected = { viewModel.onSourceCoinSelected(it) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    OutlinedTextField(
                                        value = uiState.sourceAmount,
                                        onValueChange = { viewModel.onSourceAmountChanged(it) },
                                        label = { Text("Amount") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                // Swap Button
                                IconButton(
                                    onClick = { viewModel.swapCurrencies() },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Swap currencies",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                
                                // Target Currency Section
                                Text(
                                    text = "To",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                CoinSelector(
                                    label = "Select Target",
                                    coins = uiState.availableCoins,
                                    selectedCoin = uiState.selectedTargetCoin,
                                    onCoinSelected = { viewModel.onTargetCoinSelected(it) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Conversion Result
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Converted Amount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = uiState.convertedAmount?.let { amount ->
                                            val formattedAmount = when {
                                                amount < 0.000001 -> "%.8f".format(amount)
                                                amount < 0.001 -> "%.6f".format(amount)
                                                amount < 1 -> "%.4f".format(amount)
                                                amount < 1000 -> "%.2f".format(amount)
                                                amount < 1000000 -> "%,.2f".format(amount)
                                                else -> "%,.2f".format(amount)
                                            }
                                            "$formattedAmount ${uiState.selectedTargetCoin?.baseAsset ?: ""}"
                                        } ?: "---",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    if (uiState.selectedSourceCoin != null && uiState.selectedTargetCoin != null) {
                                        // Get base assets for display
                                        val sourceAsset = uiState.selectedSourceCoin?.baseAsset ?: ""
                                        val targetAsset = uiState.selectedTargetCoin?.baseAsset ?: ""
                                        
                                        // Use the calculated exchange rate from ViewModel
                                        val exchangeRate = uiState.exchangeRate
                                        val rateText = when {
                                            exchangeRate == null -> "N/A"
                                            exchangeRate < 0.000001 -> "%.8f".format(exchangeRate)
                                            exchangeRate < 0.001 -> "%.6f".format(exchangeRate)
                                            exchangeRate < 1 -> "%.4f".format(exchangeRate)
                                            exchangeRate < 1000 -> "%.2f".format(exchangeRate)
                                            else -> "%,.2f".format(exchangeRate)
                                        }
                                        
                                        Text(
                                            text = "1 $sourceAsset = $rateText $targetAsset",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
fun CoinSelector(
    label: String,
    coins: List<Coin>,
    selectedCoin: Coin?,
    onCoinSelected: (Coin) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedCoin?.let { coin ->
                // Standardize display format for selected coin
                when (coin.symbol) {
                    "USDT" -> "USDT (Stablecoin)"
                    else -> coin.baseAsset
                }
            } ?: label,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select coin"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) expanded = true }
        )
        
        // Transparent clickable overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // Sort coins to ensure consistent ordering
            val sortedCoins = coins.sortedBy { it.baseAsset }
            
            sortedCoins.forEach { coin ->
                DropdownMenuItem(
                    text = { 
                        // Standardized display format for all coins
                        when {
                            coin.symbol == "USDT" -> Text("USDT (Stablecoin)", style = MaterialTheme.typography.bodyMedium)
                            coin.baseAsset.isNotEmpty() -> Text(coin.baseAsset, style = MaterialTheme.typography.bodyMedium)
                            else -> Text(coin.symbol, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    onClick = {
                        onCoinSelected(coin)
                        expanded = false
                    }
                )
            }
        }
    }
} 