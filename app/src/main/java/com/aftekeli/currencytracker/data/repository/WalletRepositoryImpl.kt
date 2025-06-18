package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.VirtualWallet
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WalletRepository {

    private val walletCollection = firestore.collection("wallets")

    override fun getUserWallet(userId: String): Flow<VirtualWallet?> = callbackFlow {
        val listener = walletCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val wallet = snapshot?.toObject<VirtualWallet>()
                trySend(wallet)
            }
            
        awaitClose { listener.remove() }
    }

    override suspend fun createOrUpdateWallet(wallet: VirtualWallet): Result<VirtualWallet> = try {
        val updatedWallet = wallet.copy(updatedAt = System.currentTimeMillis())
        walletCollection.document(wallet.userId).set(updatedWallet).await()
        Result.success(updatedWallet)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addFunds(userId: String, amount: Double): Result<VirtualWallet> = try {
        val walletRef = walletCollection.document(userId)
        
        // Cüzdan yoksa önce oluştur
        val wallet = walletRef.get().await().toObject<VirtualWallet>()
            ?: VirtualWallet(
                userId = userId,
                balance = 0.0
            )
        
        // Eğer cüzdan yeni oluşturulduysa kaydedelim
        if (wallet.id.isEmpty()) {
            walletRef.set(wallet).await()
        }
        
        // Bakiyeyi güncelle ve diğer alanları
        val updates = hashMapOf<String, Any>(
            "balance" to FieldValue.increment(amount),
            "updatedAt" to System.currentTimeMillis()
        )
        
        walletRef.update(updates).await()
        
        // Güncel cüzdanı al
        val updatedWallet = walletRef.get().await().toObject<VirtualWallet>()
            ?: throw IllegalStateException("Cüzdan güncellenemedi")
            
        Result.success(updatedWallet)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun spendFunds(userId: String, amount: Double): Result<VirtualWallet> = try {
        val walletRef = walletCollection.document(userId)
        val wallet = walletRef.get().await().toObject<VirtualWallet>()
            ?: throw IllegalStateException("Cüzdan bulunamadı")
            
        // Bakiye kontrolü
        if (wallet.balance < amount) {
            throw IllegalStateException("Yetersiz bakiye")
        }
        
        // Bakiyeyi azalt ve toplam yatırımı artır
        val updates = hashMapOf<String, Any>(
            "balance" to FieldValue.increment(-amount),
            "totalInvested" to FieldValue.increment(amount),
            "updatedAt" to System.currentTimeMillis()
        )
        
        walletRef.update(updates).await()
        
        // Güncel cüzdanı al
        val updatedWallet = walletRef.get().await().toObject<VirtualWallet>()
            ?: throw IllegalStateException("Cüzdan güncellenemedi")
            
        Result.success(updatedWallet)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun depositFunds(userId: String, amount: Double): Result<VirtualWallet> = try {
        val walletRef = walletCollection.document(userId)
        
        // Bakiyeyi artır
        val updates = hashMapOf<String, Any>(
            "balance" to FieldValue.increment(amount),
            "updatedAt" to System.currentTimeMillis()
        )
        
        walletRef.update(updates).await()
        
        // Güncel cüzdanı al
        val updatedWallet = walletRef.get().await().toObject<VirtualWallet>()
            ?: throw IllegalStateException("Cüzdan güncellenemedi")
            
        Result.success(updatedWallet)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 