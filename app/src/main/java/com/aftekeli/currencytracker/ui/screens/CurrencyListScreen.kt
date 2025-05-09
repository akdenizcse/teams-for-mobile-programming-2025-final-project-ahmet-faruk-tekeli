package com.aftekeli.currencytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyListScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search bar
        OutlinedTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search currencies...") },
            singleLine = true
        )
        
        // Currency list
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(20) { index ->
                CurrencyItem(
                    name = "Currency $index",
                    symbol = "SYM$index",
                    rate = "$${5000 + index}.00",
                    change = if (index % 2 == 0) "+2.5%" else "-1.8%",
                    changePositive = index % 2 == 0
                )
            }
        }
    }
}

@Composable
fun CurrencyItem(
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