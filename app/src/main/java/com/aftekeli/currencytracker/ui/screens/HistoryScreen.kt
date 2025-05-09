package com.aftekeli.currencytracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // History title
        Text(
            text = "Currency History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        // History items
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(10) { index ->
                val date = Calendar.getInstance()
                date.add(Calendar.DAY_OF_MONTH, -index)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(date.time)
                
                HistoryItem(
                    date = formattedDate,
                    currencyName = "USD/EUR",
                    rate = "$${1.15 - (index * 0.01)}",
                    change = if (index % 3 == 0) "+0.05%" else "-0.03%",
                    changePositive = index % 3 == 0
                )
            }
        }
    }
}

@Composable
fun HistoryItem(
    date: String,
    currencyName: String,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currencyName,
                    style = MaterialTheme.typography.titleMedium
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