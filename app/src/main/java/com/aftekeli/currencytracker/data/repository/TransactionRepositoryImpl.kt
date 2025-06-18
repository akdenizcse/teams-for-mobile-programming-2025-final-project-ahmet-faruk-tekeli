package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.Transaction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TransactionRepository {

    private val transactionCollection = firestore.collection("transactions")
    
    override fun getUserTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.toObjects<Transaction>() ?: emptyList()
                trySend(transactions)
            }
            
        awaitClose { listener.remove() }
    }
    
    override fun getUserTransactionsBySymbol(userId: String, symbol: String): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("symbol", symbol)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.toObjects<Transaction>() ?: emptyList()
                trySend(transactions)
            }
            
        awaitClose { listener.remove() }
    }
    
    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> = try {
        val docRef = transactionCollection.document()
        val transactionWithId = transaction.copy(id = docRef.id)
        
        docRef.set(transactionWithId).await()
        Result.success(transactionWithId)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun deleteTransaction(transactionId: String): Result<Unit> = try {
        transactionCollection.document(transactionId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 