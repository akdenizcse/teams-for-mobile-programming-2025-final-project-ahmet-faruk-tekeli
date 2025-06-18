package com.aftekeli.currencytracker.data.repository

import android.util.Log
import com.aftekeli.currencytracker.data.model.Comment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommentRepository {
    
    companion object {
        private const val TAG = "CommentRepository"
        private const val COMMENTS_COLLECTION = "comments"
    }
    
    override fun getCommentsForCoin(coinId: String): Flow<Result<List<Comment>>> = callbackFlow {
        Log.d(TAG, "Starting to listen for comments for coin: $coinId")
        
        val listener = firestore.collection(COMMENTS_COLLECTION)
            .whereEqualTo("coinId", coinId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error listening to comments: ${exception.message}")
                    trySend(Result.failure(exception))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    try {
                        val comments = snapshot.documents.mapNotNull { document ->
                            val comment = document.toObject(Comment::class.java)
                            comment?.copy(id = document.id)
                        }
                        Log.d(TAG, "Received ${comments.size} comments for coin $coinId")
                        trySend(Result.success(comments))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing comments: ${e.message}")
                        trySend(Result.failure(e))
                    }
                } else {
                    Log.d(TAG, "No comments found for coin $coinId")
                    trySend(Result.success(emptyList()))
                }
            }
        
        awaitClose {
            Log.d(TAG, "Removing comment listener for coin: $coinId")
            listener.remove()
        }
    }
    
    override suspend fun addComment(comment: Comment): Result<Unit> {
        return try {
            Log.d(TAG, "Adding comment for coin: ${comment.coinId} by user: ${comment.userId}")
            
            val commentData = hashMapOf(
                "coinId" to comment.coinId,
                "userId" to comment.userId,
                "userName" to comment.userName,
                "userEmail" to comment.userEmail,
                "text" to comment.text,
                "timestamp" to Timestamp.now(),
                "isEdited" to false,
                "editedAt" to null
            )
            
            firestore.collection(COMMENTS_COLLECTION)
                .add(commentData)
                .await()
            
            Log.d(TAG, "Comment added successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun deleteComment(commentId: String, userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting comment: $commentId by user: $userId")
            
            // First verify the comment belongs to the user
            val document = firestore.collection(COMMENTS_COLLECTION)
                .document(commentId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.failure(Exception("Comment not found"))
            }
            
            val comment = document.toObject(Comment::class.java)
            if (comment?.userId != userId) {
                return Result.failure(Exception("Unauthorized: Cannot delete comment from another user"))
            }
            
            // Delete the comment
            firestore.collection(COMMENTS_COLLECTION)
                .document(commentId)
                .delete()
                .await()
            
            Log.d(TAG, "Comment deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun updateComment(commentId: String, userId: String, newText: String): Result<Unit> {
        return try {
            Log.d(TAG, "Updating comment: $commentId by user: $userId")
            
            // First verify the comment belongs to the user
            val document = firestore.collection(COMMENTS_COLLECTION)
                .document(commentId)
                .get()
                .await()
            
            if (!document.exists()) {
                return Result.failure(Exception("Comment not found"))
            }
            
            val comment = document.toObject(Comment::class.java)
            if (comment?.userId != userId) {
                return Result.failure(Exception("Unauthorized: Cannot edit comment from another user"))
            }
            
            // Update the comment
            val updateData = hashMapOf<String, Any>(
                "text" to newText,
                "isEdited" to true,
                "editedAt" to Timestamp.now()
            )
            
            firestore.collection(COMMENTS_COLLECTION)
                .document(commentId)
                .update(updateData)
                .await()
            
            Log.d(TAG, "Comment updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating comment: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun getCommentCount(coinId: String): Result<Int> {
        return try {
            Log.d(TAG, "Getting comment count for coin: $coinId")
            
            val snapshot = firestore.collection(COMMENTS_COLLECTION)
                .whereEqualTo("coinId", coinId)
                .get()
                .await()
            
            val count = snapshot.size()
            Log.d(TAG, "Comment count for coin $coinId: $count")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting comment count: ${e.message}")
            Result.failure(e)
        }
    }
} 