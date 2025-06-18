package com.aftekeli.currencytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aftekeli.currencytracker.data.model.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String?,
    onDeleteComment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // User info and timestamp row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // User avatar - First letter of username in a circle
                    val displayName = extractUsername(comment.userEmail)
                    val initial = displayName.firstOrNull()?.uppercase() ?: "?"
                    
                    // Choose a consistent color based on the username
                    val avatarColor = getColorForUsername(displayName)
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(avatarColor)
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTimestamp(comment.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            if (comment.isEdited) {
                                Text(
                                    text = " • düzenlendi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                }
                
                // Delete button for own comments
                if (currentUserId == comment.userId) {
                    IconButton(
                        onClick = { onDeleteComment(comment.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Yorumu sil",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Comment text without explicit border, just subtle background
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

// E-posta adresinden kullanıcı adını çıkaran yardımcı fonksiyon
private fun extractUsername(email: String): String {
    return if (email.isBlank()) {
        "Anonim"
    } else {
        email.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Anonim"
    }
}

// Kullanıcı adına göre tutarlı bir renk döndüren fonksiyon
private fun getColorForUsername(username: String): Color {
    val colors = listOf(
        Color(0xFF1E88E5), // Mavi
        Color(0xFF43A047), // Yeşil
        Color(0xFF8E24AA), // Mor
        Color(0xFFE53935), // Kırmızı
        Color(0xFFFF9800), // Turuncu
        Color(0xFF3949AB), // Koyu Mavi
        Color(0xFF00ACC1), // Açık Mavi
        Color(0xFF5E35B1), // Mor
        Color(0xFFD81B60), // Pembe
        Color(0xFF00897B)  // Turkuaz
    )
    
    // Kullanıcı adından basit bir hash kodu oluştur
    val hashCode = username.hashCode().absoluteValue
    
    // Hash kodunu renk listesi boyutuna göre modülünü alarak tutarlı bir renk seç
    return colors[hashCode % colors.size]
}

private fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "şimdi"
    
    val now = System.currentTimeMillis()
    val commentTime = timestamp.toDate().time
    val diffInMillis = now - commentTime
    
    return when {
        diffInMillis < TimeUnit.MINUTES.toMillis(1) -> "şimdi"
        diffInMillis < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
            "${minutes}d önce"
        }
        diffInMillis < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
            "${hours}s önce"
        }
        diffInMillis < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
            "${days}g önce"
        }
        else -> {
            SimpleDateFormat("d MMM yyyy", Locale("tr")).format(Date(commentTime))
        }
    }
} 