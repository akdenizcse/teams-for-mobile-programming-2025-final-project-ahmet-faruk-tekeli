package com.aftekeli.currencytracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aftekeli.currencytracker.R
import com.aftekeli.currencytracker.data.model.Transaction
import com.aftekeli.currencytracker.data.model.TransactionType
import com.aftekeli.currencytracker.util.CoinLogoUtil
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val decimalFormatter = remember { DecimalFormat("#,##0.######") }
    val priceFormatter = remember { DecimalFormat("#,##0.00") }
    
    val formattedDate = remember(transaction) { 
        dateFormatter.format(Date(transaction.timestamp)) 
    }
    
    val isBuy = transaction.type == TransactionType.BUY
    val typeColor = if (isBuy) Color(0xFF4CAF50) else Color(0xFFF44336)
    val typeText = if (isBuy) "ALIŞ" else "SATIŞ"
    
    // Logo kaynağını al
    val logoResourceId = CoinLogoUtil.getCoinLogoResourceOrDefault(
        transaction.baseAsset, 
        R.drawable.ic_crypto_default
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tip göstergesi (Alış/Satış)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    color = typeColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = typeText,
                        color = typeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.wrapContentSize(Alignment.Center)
                    )
                }
            }
            
            // Kripto para logosu
            Image(
                painter = painterResource(id = logoResourceId),
                contentDescription = "${transaction.baseAsset} logo",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .padding(end = 8.dp)
            )
            
            // İşlem detayları
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Sembol ve tarih
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = transaction.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Miktar ve fiyat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${decimalFormatter.format(transaction.amount)} ${transaction.baseAsset}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    Text(
                        text = "Fiyat: $${priceFormatter.format(transaction.price)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Toplam değer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Toplam: $${priceFormatter.format(transaction.totalValue)} ${transaction.quoteAsset}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = typeColor
                    )
                }
            }
        }
    }
} 