package com.aftekeli.currencytracker.data.repository

import android.util.Log
import com.aftekeli.currencytracker.data.model.AlertCondition
import com.aftekeli.currencytracker.data.model.PriceAlert
import com.aftekeli.currencytracker.util.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AlertRepository {

    companion object {
        private const val TAG = "AlertRepository"
    }

    override suspend fun addPriceAlert(userId: String, alertData: Map<String, Any>): Result<String> {
        return try {
            val alertsCollection = firestore.collection("users").document(userId)
                .collection("priceAlerts")
            
            val documentRef = alertsCollection.add(alertData).await()
            Result.Success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding price alert to Firestore", e)
            Result.Error(e)
        }
    }

    override suspend fun deletePriceAlert(userId: String, alertId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("priceAlerts")
                .document(alertId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting price alert from Firestore", e)
            Result.Error(e)
        }
    }

    override fun getPriceAlerts(userId: String): Flow<Result<List<PriceAlert>>> = callbackFlow {
        val alertsCollection = firestore.collection("users").document(userId)
            .collection("priceAlerts")
            
        val subscription = alertsCollection
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to price alerts", error)
                    trySend(Result.Error(Exception(error.message ?: "Error fetching alerts")))
                    return@addSnapshotListener
                }
                
                try {
                    val alerts = parseAlertsSnapshot(snapshot)
                    trySend(Result.Success(alerts))
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing price alerts data", e)
                    trySend(Result.Error(e))
                }
            }
        
        awaitClose {
            subscription.remove()
        }
    }
    
    private fun parseAlertsSnapshot(snapshot: QuerySnapshot?): List<PriceAlert> {
        return snapshot?.documents?.map { document ->
            PriceAlert(
                id = document.id,
                userId = document.getString("userId") ?: "",
                coinSymbol = document.getString("coinSymbol") ?: "",
                baseAsset = document.getString("baseAsset") ?: "",
                targetPrice = document.getDouble("targetPrice") ?: 0.0,
                condition = AlertCondition.fromString(document.getString("condition")),
                isActive = document.getBoolean("isActive") ?: true,
                createdAt = document.getTimestamp("createdAt"),
                triggeredAt = document.getTimestamp("triggeredAt")
            )
        } ?: emptyList()
    }
} 