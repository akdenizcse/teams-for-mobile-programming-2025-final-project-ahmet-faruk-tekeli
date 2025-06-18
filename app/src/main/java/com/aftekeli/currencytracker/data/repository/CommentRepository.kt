package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.model.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    
    /**
     * Get all comments for a specific coin in real-time (newest first)
     */
    fun getCommentsForCoin(coinId: String): Flow<Result<List<Comment>>>
    
    /**
     * Add a new comment for a coin
     */
    suspend fun addComment(comment: Comment): Result<Unit>
    
    /**
     * Delete a comment (only by comment owner)
     */
    suspend fun deleteComment(commentId: String, userId: String): Result<Unit>
    
    /**
     * Update/edit a comment (only by comment owner)
     */
    suspend fun updateComment(commentId: String, userId: String, newText: String): Result<Unit>
    
    /**
     * Get total comment count for a coin
     */
    suspend fun getCommentCount(coinId: String): Result<Int>
} 