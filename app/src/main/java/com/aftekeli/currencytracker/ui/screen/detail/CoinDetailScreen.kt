package com.aftekeli.currencytracker.ui.screen.detail

import android.graphics.Color
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aftekeli.currencytracker.data.model.AlertCondition
import com.aftekeli.currencytracker.ui.viewmodel.CoinDetailViewModel
import com.aftekeli.currencytracker.util.getCoinLogoResource
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    viewModel: CoinDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Price alert success message
    LaunchedEffect(uiState.showAlertSuccessMessage) {
        if (uiState.showAlertSuccessMessage) {
            snackbarHostState.showSnackbar(
                message = "Price alert set successfully!",
                actionLabel = "OK"
            )
            viewModel.clearAlertSuccessMessage()
        }
    }
    
    // Pull to refresh state
    val pullRefreshState = rememberPullToRefreshState()
    
    // Handle manual pull-to-refresh gesture
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshCoinData()
        }
    }
    
    // Update pull refresh state based on the UI state's refreshing status
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing && pullRefreshState.isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }
    
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    // Alert Dialog for setting price alert
    if (uiState.showAlertDialog) {
        PriceAlertDialog(
            coinSymbol = uiState.coinSymbol,
            coinBaseAsset = uiState.currentCoin?.baseAsset ?: "",
            currentPrice = uiState.currentCoin?.lastPrice ?: 0.0,
            targetPrice = uiState.alertTargetPrice,
            onTargetPriceChange = { viewModel.onAlertTargetPriceChanged(it) },
            selectedCondition = uiState.alertCondition,
            onConditionChange = { viewModel.onAlertConditionChanged(it) },
            errorMessage = uiState.alertDialogError,
            isLoading = uiState.isSubmittingAlert,
            onConfirm = { viewModel.createPriceAlert() },
            onDismiss = { viewModel.dismissSetAlertDialog() }
        )
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "${uiState.currentCoin?.baseAsset ?: uiState.coinSymbol}"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (uiState.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            tint = if (uiState.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (!uiState.isLoadingCoin && uiState.currentCoin != null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showSetAlertDialog() },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Set Price Alert") },
                    text = { Text("Set Price Alert") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Coin Price Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState.isLoadingCoin && uiState.currentCoin == null) {
                        // Show loading state for the coin header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else if (uiState.errorMessage != null && uiState.currentCoin == null) {
                        // Show error state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = uiState.errorMessage ?: "Error loading coin details",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Show coin data if available
                        uiState.currentCoin?.let { coin ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Add coin logo if available
                                    val logoResourceId = getCoinLogoResource(coin.baseAsset)
                                    if (logoResourceId != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = logoResourceId),
                                                contentDescription = "${coin.baseAsset} logo",
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }
                                    
                                    Column {
                                        Text(
                                            text = "${coin.baseAsset}/${coin.quoteAsset}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Text(
                                            text = formatPrice(coin.lastPrice),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "24h Change",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    val priceChangePercent = coin.priceChangePercent
                                    val color = if (priceChangePercent >= 0) 
                                        androidx.compose.ui.graphics.Color(0xFF00C853) 
                                    else 
                                        androidx.compose.ui.graphics.Color(0xFFD50000)
                                    val sign = if (priceChangePercent >= 0) "+" else ""
                                    
                                    Text(
                                        text = "$sign${formatPercentage(priceChangePercent)}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "24h High",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = formatPrice(coin.high24h),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "24h Low",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = formatPrice(coin.low24h),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "24h Volume",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = formatVolume(coin.volume),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "24h Open",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = formatPrice(coin.openPrice),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "24h Avg Price",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = formatPrice(coin.weightedAvgPrice),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "24h Change (Abs)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    val priceChange = coin.priceChange
                                    val color = if (priceChange >= 0) 
                                        androidx.compose.ui.graphics.Color(0xFF00C853) 
                                    else 
                                        androidx.compose.ui.graphics.Color(0xFFD50000)
                                    val sign = if (priceChange >= 0) "+" else ""
                                    
                                    Text(
                                        text = "$sign${formatPrice(priceChange)} ${coin.quoteAsset}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "24h Trades",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = formatCount(coin.count),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                // Add Bid/Ask prices
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Best Bid",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = formatPrice(coin.bidPrice),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Best Ask",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = formatPrice(coin.askPrice),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Interval Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableIntervals.forEach { interval ->
                        val selected = interval == uiState.selectedInterval
                        
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onIntervalSelected(interval) },
                            label = { Text(interval) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoadingChart) {
                            CircularProgressIndicator()
                        } else if (uiState.chartErrorMessage != null) {
                            Text(
                                text = uiState.chartErrorMessage ?: "Error loading chart data",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (uiState.chartData.isEmpty()) {
                            Text(
                                text = "No chart data available",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            PriceChart(
                                chartData = uiState.chartData,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        }
                    }
                }
                
                // Adding some bottom padding for the FAB
                Spacer(modifier = Modifier.height(80.dp))
            }
            
            // Pull to refresh indicator
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun PriceAlertDialog(
    coinSymbol: String,
    coinBaseAsset: String,
    currentPrice: Double,
    targetPrice: String,
    onTargetPriceChange: (String) -> Unit,
    selectedCondition: AlertCondition,
    onConditionChange: (AlertCondition) -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(text = "Set Price Alert for $coinBaseAsset") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Current price: ${formatPrice(currentPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Alert me when price:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Alert condition selection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedCondition == AlertCondition.ABOVE,
                        onClick = { onConditionChange(AlertCondition.ABOVE) }
                    )
                    Text(
                        text = "Rises above",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedCondition == AlertCondition.BELOW,
                        onClick = { onConditionChange(AlertCondition.BELOW) }
                    )
                    Text(
                        text = "Falls below",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Target price input
                OutlinedTextField(
                    value = targetPrice,
                    onValueChange = onTargetPriceChange,
                    label = { Text("Target Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    singleLine = true
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                Text("Set Alert")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PriceChart(
    chartData: List<com.aftekeli.currencytracker.data.model.ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    var textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()
    
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
                
                // X Axis setup
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setTextColor(textColor)
                    textSize = 10f
                    valueFormatter = object : ValueFormatter() {
                        private val dateFormat = SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
                        
                        override fun getFormattedValue(value: Float): String {
                            return dateFormat.format(Date(value.toLong()))
                        }
                    }
                    setDrawGridLines(true)
                    setGridColor(gridColor)
                    setDrawAxisLine(true)
                }
                
                // Y Axis setup
                axisLeft.apply {
                    setTextColor(textColor)
                    textSize = 10f
                    setDrawGridLines(true)
                    setGridColor(gridColor)
                    setDrawZeroLine(false)
                }
                
                // Disable right axis
                axisRight.isEnabled = false
                
                // Legend setup
                legend.apply {
                    form = Legend.LegendForm.LINE
                    this.textColor = textColor
                    textSize = 12f
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                }
            }
        },
        update = { lineChart ->
            // Convert chart data to MPAndroidChart entries
            val entries = chartData.map { Entry(it.timestamp.toFloat(), it.price) }
            
            if (entries.isNotEmpty()) {
                val dataSet = LineDataSet(entries, "Price History").apply {
                    color = primaryColor
                    lineWidth = 2f
                    setDrawCircles(false)
                    setDrawValues(false)
                    setDrawFilled(true)
                    fillColor = primaryColor
                    fillAlpha = 30 // 30% opacity
                    mode = LineDataSet.Mode.CUBIC_BEZIER // smooth curves
                    
                    // Highlight
                    highLightColor = Color.WHITE
                    setDrawHorizontalHighlightIndicator(false)
                    setDrawVerticalHighlightIndicator(true)
                }
                
                lineChart.data = LineData(dataSet)
                lineChart.invalidate() // Refresh chart
            }
        }
    )
}

private fun formatPrice(price: Double): String {
    return when {
        price < 0.01 -> "%.8f".format(price)
        price < 1 -> "%.6f".format(price)
        price < 1000 -> "%.2f".format(price)
        else -> "%.2f".format(price)
    }
}

private fun formatPercentage(percent: Double): String {
    return "%.2f".format(abs(percent))
}

private fun formatVolume(volume: Double): String {
    return when {
        volume >= 1_000_000_000 -> "%.2fB".format(volume / 1_000_000_000)
        volume >= 1_000_000 -> "%.2fM".format(volume / 1_000_000)
        volume >= 1_000 -> "%.2fK".format(volume / 1_000)
        else -> "%.2f".format(volume)
    }
}

private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> "%.2fM".format(count / 1_000_000.0)
        count >= 1_000 -> "%.2fK".format(count / 1_000.0)
        else -> count.toString()
    }
} 