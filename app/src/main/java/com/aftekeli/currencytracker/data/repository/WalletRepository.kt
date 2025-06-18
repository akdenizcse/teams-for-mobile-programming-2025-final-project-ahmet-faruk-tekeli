package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.VirtualWallet
import kotlinx.coroutines.flow.Flow

/**
 * Kullanıcının sanal cüzdanını yönetmek için repository arayüzü
 */
interface WalletRepository {
    /**
     * Kullanıcının cüzdanını izler
     */
    fun getUserWallet(userId: String): Flow<VirtualWallet?>
    
    /**
     * Kullanıcı için cüzdan oluşturur veya günceller
     */
    suspend fun createOrUpdateWallet(wallet: VirtualWallet): Result<VirtualWallet>
    
    /**
     * Cüzdana para ekler (USDT)
     */
    suspend fun addFunds(userId: String, amount: Double): Result<VirtualWallet>
    
    /**
     * Cüzdandan para harcar (USDT) - Al işlemleri için
     */
    suspend fun spendFunds(userId: String, amount: Double): Result<VirtualWallet>
    
    /**
     * Cüzdana para ekler (USDT) - Sat işlemleri için
     */
    suspend fun depositFunds(userId: String, amount: Double): Result<VirtualWallet>
} 