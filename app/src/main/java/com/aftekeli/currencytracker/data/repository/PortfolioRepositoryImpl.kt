package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.PortfolioItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PortfolioRepository {

    private val portfolioCollection = firestore.collection("portfolios")
    
    override fun getUserPortfolio(userId: String): Flow<List<PortfolioItem>> = callbackFlow {
        val listener = portfolioCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val portfolio = snapshot?.toObjects<PortfolioItem>() ?: emptyList()
                trySend(portfolio)
            }
            
        awaitClose { listener.remove() }
    }
    
    override fun getPortfolioItem(userId: String, symbol: String): Flow<PortfolioItem?> = callbackFlow {
        val listener = portfolioCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("symbol", symbol)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val items = snapshot?.toObjects<PortfolioItem>() ?: emptyList()
                trySend(items.firstOrNull())
            }
            
        awaitClose { listener.remove() }
    }
    
    override suspend fun createOrUpdatePortfolioItem(item: PortfolioItem): Result<PortfolioItem> = try {
        val itemId = if (item.id.isNotEmpty()) {
            item.id
        } else {
            "${item.userId}_${item.symbol}"
        }
        
        val updatedItem = item.copy(
            updatedAt = System.currentTimeMillis()
        )
        
        portfolioCollection.document(itemId).set(updatedItem).await()
        Result.success(updatedItem)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun buyCoin(
        userId: String,
        symbol: String,
        baseAsset: String,
        amount: Double,
        price: Double
    ): Result<PortfolioItem> = try {
        val itemId = "${userId}_${symbol}"
        val itemRef = portfolioCollection.document(itemId)
        
        val existingItem = itemRef.get().await().toObject<PortfolioItem>()
        
        if (existingItem == null) {
            // Yeni coin alımı
            val newItem = PortfolioItem(
                id = itemId,
                userId = userId,
                symbol = symbol,
                baseAsset = baseAsset,
                amount = amount,
                averageBuyPrice = price,
                totalInvested = amount * price
            )
            itemRef.set(newItem).await()
            Result.success(newItem)
        } else {
            // Mevcut coine ekleme
            val currentAmount = existingItem.amount
            val currentTotal = existingItem.totalInvested
            val newAmount = currentAmount + amount
            val newTotal = currentTotal + (amount * price)
            val newAvgPrice = newTotal / newAmount
            
            val updates = hashMapOf<String, Any>(
                "amount" to newAmount,
                "averageBuyPrice" to newAvgPrice,
                "totalInvested" to newTotal,
                "updatedAt" to System.currentTimeMillis()
            )
            
            itemRef.update(updates).await()
            
            val updatedItem = itemRef.get().await().toObject<PortfolioItem>()
                ?: throw IllegalStateException("Portföy güncellenemedi")
                
            Result.success(updatedItem)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun sellCoin(
        userId: String,
        symbol: String,
        baseAsset: String,
        amount: Double,
        price: Double
    ): Result<PortfolioItem> = try {
        val itemId = "${userId}_${symbol}"
        val itemRef = portfolioCollection.document(itemId)
        
        val existingItem = itemRef.get().await().toObject<PortfolioItem>()
            ?: throw IllegalStateException("Portföyde bu coin bulunamadı")
            
        // Miktar kontrolü
        if (existingItem.amount < amount) {
            throw IllegalStateException("Portföyünüzde yeterli miktar yok")
        }
        
        // Satıştan sonra kalan miktar
        val newAmount = existingItem.amount - amount
        
        // Satıştan sonraki toplam yatırım
        // Ortalama alım fiyatı üzerinden satılan miktarın değerini düşüyoruz
        val soldValue = amount * existingItem.averageBuyPrice
        val newTotal = existingItem.totalInvested - soldValue
        
        if (newAmount <= 0.0000001) {
            // Tüm coin satıldı, öğeyi silelim
            itemRef.delete().await()
            Result.success(PortfolioItem(
                id = itemId,
                userId = userId,
                symbol = symbol,
                baseAsset = baseAsset,
                amount = 0.0,
                averageBuyPrice = 0.0,
                totalInvested = 0.0
            ))
        } else {
            // Kısmen satış, sadece miktarları güncelle
            val updates = hashMapOf<String, Any>(
                "amount" to newAmount,
                "totalInvested" to newTotal,
                "updatedAt" to System.currentTimeMillis()
            )
            
            itemRef.update(updates).await()
            
            val updatedItem = itemRef.get().await().toObject<PortfolioItem>()
                ?: throw IllegalStateException("Portföy güncellenemedi")
                
            Result.success(updatedItem)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
} 