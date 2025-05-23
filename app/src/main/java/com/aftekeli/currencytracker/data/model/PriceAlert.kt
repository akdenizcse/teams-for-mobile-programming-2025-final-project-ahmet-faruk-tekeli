package com.aftekeli.currencytracker.data.model

import com.google.firebase.Timestamp

enum class AlertCondition {
    ABOVE, BELOW;
    
    override fun toString(): String {
        return name
    }
    
    companion object {
        fun fromString(value: String?): AlertCondition {
            return try {
                value?.let { valueOf(it) } ?: ABOVE
            } catch (e: Exception) {
                ABOVE
            }
        }
    }
}

data class PriceAlert(
    val id: String = "",
    val userId: String = "",
    val coinSymbol: String = "",
    val baseAsset: String = "",
    val targetPrice: Double = 0.0,
    val condition: AlertCondition = AlertCondition.ABOVE,
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val triggeredAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "coinSymbol" to coinSymbol,
            "baseAsset" to baseAsset,
            "targetPrice" to targetPrice,
            "condition" to condition.toString(),
            "isActive" to isActive,
            "createdAt" to (createdAt ?: Timestamp.now()),
            "triggeredAt" to triggeredAt
        )
    }
} 