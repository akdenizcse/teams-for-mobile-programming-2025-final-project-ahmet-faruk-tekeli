package com.aftekeli.currencytracker.ui.screens.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun CurrencySelectionDialog(
    selectedCurrency: String,
    onSelectCurrency: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currencies = listOf(
        Currency("USD", "US Dollar", "🇺🇸"),
        Currency("EUR", "Euro", "🇪🇺"),
        Currency("GBP", "British Pound", "🇬🇧"),
        Currency("JPY", "Japanese Yen", "🇯🇵"),
        Currency("CAD", "Canadian Dollar", "🇨🇦"),
        Currency("AUD", "Australian Dollar", "🇦🇺"),
        Currency("CHF", "Swiss Franc", "🇨🇭"),
        Currency("CNY", "Chinese Yuan", "🇨🇳"),
        Currency("TRY", "Turkish Lira", "🇹🇷"),
        Currency("RUB", "Russian Ruble", "🇷🇺")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Default Currency") },
        text = {
            LazyColumn {
                items(currencies) { currency ->
                    CurrencyItem(
                        currency = currency,
                        isSelected = currency.code == selectedCurrency,
                        onClick = {
                            onSelectCurrency(currency.code)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

@Composable
private fun CurrencyItem(
    currency: Currency,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currency.flag,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = currency.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
        
        Divider()
    }
}

data class Currency(
    val code: String,
    val name: String,
    val flag: String
) 