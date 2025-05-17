package com.aftekeli.currencytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aftekeli.currencytracker.domain.model.Currency
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CurrencyItem(
    currency: Currency,
    onItemClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavoriteButton: Boolean = true,
    currencySymbol: String = "$",
    displaySymbolPrefix: String = "",
    flagEmoji: String = ""
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Symbol and name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (currency.logoUrl.isNotEmpty()) {
                    // Display logo from CoinGecko if available
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currency.logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Currency logo",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(2.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else if (flagEmoji.isNotEmpty()) {
                    // Fallback to flag emoji if no logo is available
                    Text(
                        text = flagEmoji,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                Column {
                    Text(
                        text = "$displaySymbolPrefix${currency.symbol}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (currency.name.isNotEmpty()) {
                        Text(
                            text = currency.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Price and change info
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                // Price
                Text(
                    text = formatPrice(currency.price, currencySymbol),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
                
                // Price change
                currency.changePercent24h?.let { change ->
                    val color = if (change >= 0) Color.Green else Color.Red
                    val prefix = if (change >= 0) "+" else ""
                    
                    Text(
                        text = "$prefix${String.format("%.2f", change)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        textAlign = TextAlign.End
                    )
                }
            }
            
            // Favorite button
            if (showFavoriteButton) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (currency.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (currency.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (currency.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun formatPrice(price: Double, currencySymbol: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.maximumFractionDigits = if (price < 1.0) 6 else 2
    format.currency = java.util.Currency.getInstance("USD")
    
    val formatted = format.format(price)
    return formatted.replace("$", currencySymbol)
} 