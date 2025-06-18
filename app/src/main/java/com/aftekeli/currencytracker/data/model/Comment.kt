package com.aftekeli.currencytracker.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Comment(
    @DocumentId
    val id: String = "",
    
    @PropertyName("coinId")
    val coinId: String = "",
    
    @PropertyName("userId")
    val userId: String = "",
    
    @PropertyName("userName")
    val userName: String = "",
    
    @PropertyName("userEmail")
    val userEmail: String = "",
    
    @PropertyName("text")
    val text: String = "",
    
    @ServerTimestamp
    @PropertyName("timestamp")
    val timestamp: Timestamp? = null,
    
    @PropertyName("isEdited")
    val isEdited: Boolean = false,
    
    @PropertyName("editedAt")
    val editedAt: Timestamp? = null
) {
    companion object {
        fun createNew(
            coinId: String,
            userId: String,
            userName: String,
            userEmail: String,
            text: String
        ) = Comment(
            coinId = coinId,
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            text = text
        )
    }
} 