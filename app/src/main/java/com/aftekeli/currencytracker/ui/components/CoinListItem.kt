package com.aftekeli.currencytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aftekeli.currencytracker.data.model.Coin
import com.aftekeli.currencytracker.util.getCoinLogoResource
import androidx.compose.foundation.Image
import kotlin.math.abs

@Composable
fun CoinListItem(
    coin: Coin,
    onItemClick: (Coin) -> Unit
) {
    // Get clean base asset for display
    val baseAsset = coin.baseAsset.trim()
    val firstLetter = if (baseAsset.isNotEmpty()) {
        baseAsset[0].toString().uppercase()
    } else if (coin.symbol.isNotEmpty()) {
        coin.symbol[0].toString().uppercase()
    } else {
        "?"
    }

    // Get logo resource if available
    val logoResourceId = getCoinLogoResource(baseAsset)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(coin) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coin Logo or Letter Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (logoResourceId != null) {
                    // Display the coin logo if available
                    Image(
                        painter = painterResource(id = logoResourceId),
                        contentDescription = "${coin.baseAsset} logo",
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    // Fallback to letter placeholder
                    Text(
                        text = firstLetter,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Coin Symbol and Name
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${baseAsset}/${coin.quoteAsset}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Vol: ${formatVolume(if (coin.quoteVolume > 0) coin.quoteVolume else coin.volume)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Price and Change
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = formatPrice(coin.lastPrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val priceChangePercent = coin.priceChangePercent
                val color = if (priceChangePercent >= 0) Color(0xFF00C853) else Color(0xFFD50000)
                val sign = if (priceChangePercent >= 0) "+" else ""
                
                Text(
                    text = "$sign${formatPercentage(priceChangePercent)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    textAlign = TextAlign.End
                )
            }
        }
    }
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