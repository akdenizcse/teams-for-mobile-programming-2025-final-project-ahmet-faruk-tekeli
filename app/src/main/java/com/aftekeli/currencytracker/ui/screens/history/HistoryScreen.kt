package com.aftekeli.currencytracker.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    var selectedFilter by remember { mutableStateOf("All Time") }
    val filters = listOf("Today", "This Week", "This Month", "All Time")
    var showFilterMenu by remember { mutableStateOf(false) }
    
    // Scaffold with top bar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                actions = {
                    // Filter button
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    // Date picker would be here in a real app
                    IconButton(onClick = { /* Show date picker */ }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Dates"
                        )
                    }
                    
                    // Filter dropdown menu
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        filters.forEach { filter ->
                            DropdownMenuItem(
                                text = { Text(filter) },
                                onClick = {
                                    selectedFilter = filter
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Market summary section with chart
            MarketSummarySection()
            
            // Divider
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Recent views title
            Text(
                text = "Recently Viewed",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )
            
            // History items
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(10) { index ->
                    val date = Calendar.getInstance()
                    date.add(Calendar.DAY_OF_MONTH, -index)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val formattedDate = dateFormat.format(date.time)
                    
                    HistoryItem(
                        date = formattedDate,
                        currencyName = "BTC/USD",
                        rate = "$${35000 + (index * 100)}",
                        change = if (index % 3 == 0) "+1.2%" else "-0.8%",
                        changePositive = index % 3 == 0,
                        onItemClick = { 
                            navController.navigate("currency_detail/BTCUSDT")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MarketSummarySection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Market Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sample chart data for BTC
            val chartEntryModel = entryModelOf(43500.0, 44200.0, 43800.0, 45100.0, 44700.0, 46200.0, 45800.0)
            
            // Chart component (simplified placeholder)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0x1F4CAF50), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Price Chart", style = MaterialTheme.typography.bodyMedium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Key stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "BTC", value = "$45,800", isPositive = true)
                StatItem(label = "ETH", value = "$3,250", isPositive = false)
                StatItem(label = "USD", value = "€0.92", isPositive = true)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, isPositive: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = if (isPositive) "+2.4%" else "-1.2%",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE53935)
        )
    }
}

@Composable
fun HistoryItem(
    date: String,
    currencyName: String,
    rate: String,
    change: String,
    changePositive: Boolean,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onItemClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Currency info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currencyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Mini chart - simplified representation
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (changePositive) Color(0x1F4CAF50) else Color(0x1FE53935))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Price info
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = rate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = change,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (changePositive) 
                        Color(0xFF4CAF50)
                    else 
                        Color(0xFFE53935)
                )
            }
        }
    }
}