package com.aftekeli.currencytracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aftekeli.currencytracker.R
import com.aftekeli.currencytracker.data.model.PortfolioItem
import com.aftekeli.currencytracker.util.CoinLogoUtil
import java.text.DecimalFormat

@Composable
fun PortfolioItemCard(
    portfolioItem: PortfolioItem,
    currentPrice: Double,
    onBuy: () -> Unit,
    onSell: () -> Unit,
    modifier: Modifier = Modifier
) {
    val decimalFormatter = remember { DecimalFormat("#,##0.######") }
    val priceFormatter = remember { DecimalFormat("#,##0.00") }
    
    // Hesaplamaları yap
    val currentValue = portfolioItem.getCurrentValue(currentPrice)
    val profitLoss = portfolioItem.getProfitLoss(currentPrice)
    val profitLossPercentage = portfolioItem.getProfitLossPercentage(currentPrice)
    
    // Kar/zarar rengi
    val profitLossColor = if (profitLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    // Logo kaynağını al
    val logoResourceId = CoinLogoUtil.getCoinLogoResourceOrDefault(
        portfolioItem.baseAsset, 
        R.drawable.ic_crypto_default
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Üst kısım: Sembol ve mevcut değer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo, Sembol ve miktar
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = logoResourceId),
                            contentDescription = "${portfolioItem.baseAsset} logo",
                            modifier = Modifier
                                .size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Sembol ve miktar
                    Column {
                        Text(
                            text = portfolioItem.baseAsset,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${decimalFormatter.format(portfolioItem.amount)} ${portfolioItem.baseAsset}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Mevcut değer
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${priceFormatter.format(currentValue)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Kar/zarar gösterimi
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val profitLossPrefix = if (profitLoss >= 0) "+" else ""
                        Text(
                            text = "$profitLossPrefix$${priceFormatter.format(profitLoss)} (${profitLossPrefix}${String.format("%.2f", profitLossPercentage)}%)",
                            fontSize = 14.sp,
                            color = profitLossColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ortalama alış fiyatı ve mevcut fiyat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Ortalama Alış",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "$${priceFormatter.format(portfolioItem.averageBuyPrice)}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Mevcut Fiyat",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "$${priceFormatter.format(currentPrice)}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // İşlem butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBuy,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Al", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = onSell,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Sat", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
} 