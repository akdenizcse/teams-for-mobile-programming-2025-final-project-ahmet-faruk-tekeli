package com.aftekeli.currencytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aftekeli.currencytracker.R
import com.aftekeli.currencytracker.data.model.Transaction
import com.aftekeli.currencytracker.data.model.TransactionType
import java.text.DecimalFormat

@Composable
fun TradingDialog(
    symbol: String,
    baseAsset: String, 
    currentPrice: Double,
    availableAmount: Double = 0.0, // Satış için mevcut coin miktarı
    walletBalance: Double = 0.0,   // Alış için mevcut cüzdan bakiyesi
    isBuy: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var spendAmount by remember { mutableDoubleStateOf(0.0) }
    val decimalFormatter = remember { DecimalFormat("#,##0.######") }
    val priceFormatter = remember { DecimalFormat("#,##0.00") }
    
    // Kullanıcı miktar girdiğinde hesaplamaları yap
    LaunchedEffect(amount, currentPrice) {
        spendAmount = try {
            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
            parsedAmount * currentPrice
        } catch (e: Exception) {
            0.0
        }
    }
    
    // İşlem için yeterli bakiye/coin var mı kontrolü
    val hasEnoughFunds = if (isBuy) {
        spendAmount <= walletBalance
    } else {
        amount.toDoubleOrNull() ?: 0.0 <= availableAmount
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialog Başlığı
                Text(
                    text = if (isBuy) "$baseAsset Al" else "$baseAsset Sat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (isBuy) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mevcut fiyat gösterimi
                Text(
                    text = "Mevcut Fiyat",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Text(
                    text = "$${priceFormatter.format(currentPrice)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Kullanılabilir bakiye veya miktar bilgisi
                val availableText = if (isBuy) {
                    "Bakiye: $${priceFormatter.format(walletBalance)} USDT"
                } else {
                    "Mevcut: ${decimalFormatter.format(availableAmount)} $baseAsset"
                }
                
                Text(
                    text = availableText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Miktar girişi
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Miktar ($baseAsset)",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { newValue ->
                            // Sadece sayısal değerlere izin ver
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amount = newValue
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = !hasEnoughFunds && amount.isNotEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isBuy) Color(0xFF4CAF50) else Color(0xFFF44336),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        placeholder = { Text("0.0") }
                    )
                    
                    if (!hasEnoughFunds && amount.isNotEmpty()) {
                        Text(
                            text = if (isBuy) "Yetersiz bakiye" else "Yetersiz $baseAsset miktarı",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Hızlı seçim butonları
                if (isBuy) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // %25, %50, %75, %100 butonları
                        arrayOf(0.25, 0.5, 0.75, 1.0).forEach { percentage ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        val maxAmount = walletBalance / currentPrice
                                        val calculatedAmount = maxAmount * percentage
                                        amount = decimalFormatter.format(calculatedAmount)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${(percentage * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    // Satış için hızlı seçim butonları
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // %25, %50, %75, %100 butonları
                        arrayOf(0.25, 0.5, 0.75, 1.0).forEach { percentage ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        val calculatedAmount = availableAmount * percentage
                                        amount = decimalFormatter.format(calculatedAmount)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${(percentage * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Toplam değer gösterimi
                Text(
                    text = "Toplam",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Text(
                    text = "$${priceFormatter.format(spendAmount)} USDT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // İşlem butonları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // İptal butonu
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("İptal")
                    }
                    
                    // Onay butonu
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull() ?: 0.0
                            if (amountValue > 0 && hasEnoughFunds) {
                                onConfirm(amountValue)
                            }
                        },
                        enabled = amount.isNotEmpty() && hasEnoughFunds && (amount.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBuy) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    ) {
                        Text(
                            text = if (isBuy) "Satın Al" else "Sat",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
} 