package com.aftekeli.currencytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun WatchlistScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Watchlist title
        Text(
            text = "Your Watchlist",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        // Watchlist items
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(5) { index ->
                WatchlistItem(
                    name = "Favorite Currency ${index + 1}",
                    symbol = "FAV${index + 1}",
                    rate = "$${7500 + index * 100}.00",
                    change = if (index % 2 == 0) "+3.2%" else "-2.1%",
                    changePositive = index % 2 == 0
                )
            }
        }
    }
}

@Composable
fun WatchlistItem(
    name: String,
    symbol: String,
    rate: String,
    change: String,
    changePositive: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = rate,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = change,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (changePositive) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}