package com.aftekeli.currencytracker.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Kullanıcının sanal parasını (USDT) temsil eden cüzdan modeli
 */
data class VirtualWallet(
    @DocumentId
    val id: String = "",
    
    @PropertyName("userId")
    val userId: String = "",
    
    @PropertyName("balance")
    val balance: Double = 1000.0, // Varsayılan başlangıç bakiyesi: 1000 USDT
    
    @PropertyName("totalInvested")
    val totalInvested: Double = 0.0,
    
    @PropertyName("totalProfit")
    val totalProfit: Double = 0.0,
    
    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),
    
    @PropertyName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) 