package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * İşlem geçmişini yönetmek için repository arayüzü
 */
interface TransactionRepository {
    /**
     * Kullanıcının tüm işlemlerini izler
     */
    fun getUserTransactions(userId: String): Flow<List<Transaction>>
    
    /**
     * Belirli bir sembol için kullanıcının işlemlerini izler
     */
    fun getUserTransactionsBySymbol(userId: String, symbol: String): Flow<List<Transaction>>
    
    /**
     * Yeni bir işlem ekler
     */
    suspend fun addTransaction(transaction: Transaction): Result<Transaction>
    
    /**
     * Belirli bir işlemi siler
     */
    suspend fun deleteTransaction(transactionId: String): Result<Unit>
} 