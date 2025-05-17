package com.aftekeli.currencytracker.ui.screens.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aftekeli.currencytracker.domain.model.Currency
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertDialog(
    currency: Currency,
    onDismiss: () -> Unit,
    onCreateAlert: (targetPrice: Double, isAboveTarget: Boolean) -> Unit
) {
    var priceText by remember { mutableStateOf(currency.price.toString()) }
    var isAboveTarget by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Price Alert for ${getFormattedSymbol(currency.symbol)}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Current price: ${formatPrice(currency.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Alert condition selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alert me when price goes:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Switch(
                        checked = isAboveTarget,
                        onCheckedChange = { isAboveTarget = it },
                        thumbContent = {
                            Text(
                                text = if (isAboveTarget) "↑" else "↓",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    
                    Text(
                        text = if (isAboveTarget) "Above" else "Below",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                // Target price input
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Target Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    singleLine = true
                )
                
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val targetPrice = priceText.toDoubleOrNull()
                    if (targetPrice == null) {
                        errorMessage = "Please enter a valid price"
                    } else {
                        // Validate the target price based on the alert condition
                        if (isAboveTarget && targetPrice <= currency.price) {
                            errorMessage = "Target price must be higher than current price"
                        } else if (!isAboveTarget && targetPrice >= currency.price) {
                            errorMessage = "Target price must be lower than current price"
                        } else {
                            onCreateAlert(targetPrice, isAboveTarget)
                        }
                    }
                }
            ) {
                Text("Create Alert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper functions
private fun getFormattedSymbol(symbol: String): String {
    return when {
        symbol.endsWith("USDT") -> {
            val base = symbol.removeSuffix("USDT")
            "$base/USDT"
        }
        symbol.endsWith("BTC") -> {
            val base = symbol.removeSuffix("BTC")
            "$base/BTC"
        }
        else -> symbol
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.maximumFractionDigits = if (price < 1.0) 6 else 2
    return format.format(price)
} 