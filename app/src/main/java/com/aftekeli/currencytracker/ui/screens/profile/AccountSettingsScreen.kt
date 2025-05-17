package com.aftekeli.currencytracker.ui.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = remember { Firebase.auth }
    val currentUser = remember { auth.currentUser }
    val storage = remember { Firebase.storage }
    val coroutineScope = rememberCoroutineScope()
    
    // UI states
    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Force recomposition when profile photo updates
    var currentPhotoUrl by remember { mutableStateOf(currentUser?.photoUrl) }
    
    // Separate loading states for different operations
    var isPhotoUploading by remember { mutableStateOf(false) }
    var isNameUpdating by remember { mutableStateOf(false) }
    var isPasswordUpdating by remember { mutableStateOf(false) }
    
    // Check for updates to profile
    LaunchedEffect(currentUser) {
        currentPhotoUrl = currentUser?.photoUrl
        displayName = currentUser?.displayName ?: ""
    }
    
    // Password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Validation states
    var isNameValid by remember { mutableStateOf(true) }
    var isCurrentPasswordValid by remember { mutableStateOf(true) }
    var isNewPasswordValid by remember { mutableStateOf(true) }
    var isPasswordsMatch by remember { mutableStateOf(true) }
    
    // Error messages
    var nameError by remember { mutableStateOf("") }
    var currentPasswordError by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    
    // Validation functions
    val validateDisplayName = {
        if (displayName.isBlank()) {
            isNameValid = false
            nameError = "Display name cannot be empty"
        } else if (displayName.length < 3) {
            isNameValid = false
            nameError = "Display name must be at least 3 characters"
        } else {
            isNameValid = true
            nameError = ""
        }
        isNameValid
    }
    
    val validatePasswordMatch = {
        if (newPassword != confirmPassword) {
            isPasswordsMatch = false
            confirmPasswordError = "Passwords do not match"
        } else {
            isPasswordsMatch = true
            confirmPasswordError = ""
        }
        isPasswordsMatch
    }
    
    val validateNewPassword = {
        if (newPassword.isNotEmpty() && newPassword.length < 6) {
            isNewPasswordValid = false
            newPasswordError = "Password must be at least 6 characters"
        } else {
            isNewPasswordValid = true
            newPasswordError = ""
        }
        isNewPasswordValid
    }
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            profileImageUri = it
        }
    }
    
    // Actions to perform
    val updateProfilePhoto = {
        coroutineScope.launch {
            try {
                isPhotoUploading = true
                
                profileImageUri?.let { uri ->
                    // Create a storage reference
                    val storageRef = storage.reference
                    val photoRef = storageRef.child("profile_images/${auth.currentUser?.uid ?: UUID.randomUUID()}.jpg")
                    
                    // Upload photo
                    photoRef.putFile(uri).await()
                    
                    // Get download URL
                    val downloadUrl = photoRef.downloadUrl.await()
                    
                    // Update user profile with new photo URL
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setPhotoUri(downloadUrl)
                        .build()
                    
                    // Update user profile
                    currentUser?.updateProfile(profileUpdates)?.await()
                    
                    // Reload user data to ensure we have latest profile info
                    currentUser?.reload()?.await()
                    
                    // Clear the selected image after successful upload
                    profileImageUri = null
                    
                    Toast.makeText(context, "Profile photo updated successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error updating profile photo: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isPhotoUploading = false
            }
        }
    }
    
    val updateDisplayName = {
        if (validateDisplayName()) {
            coroutineScope.launch {
                try {
                    isNameUpdating = true
                    
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    
                    currentUser?.updateProfile(profileUpdates)?.await()
                    
                    Toast.makeText(context, "Display name updated successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error updating display name: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isNameUpdating = false
                }
            }
        }
    }
    
    val updatePassword = {
        if (currentPassword.isNotEmpty() && validateNewPassword() && validatePasswordMatch()) {
            coroutineScope.launch {
                try {
                    isPasswordUpdating = true
                    
                    // Re-authenticate user before changing password
                    val credential = EmailAuthProvider.getCredential(
                        currentUser?.email ?: "", 
                        currentPassword
                    )
                    
                    // Re-authenticate
                    try {
                        currentUser?.reauthenticate(credential)?.await()
                        
                        // Update password
                        currentUser?.updatePassword(newPassword)?.await()
                        
                        // Clear password fields
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                        
                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        isCurrentPasswordValid = false
                        currentPasswordError = "Current password is incorrect"
                        Toast.makeText(context, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    
                } catch (e: Exception) {
                    Toast.makeText(context, "Error updating password: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isPasswordUpdating = false
                }
            }
        } else if (currentPassword.isEmpty() && (newPassword.isNotEmpty() || confirmPassword.isNotEmpty())) {
            isCurrentPasswordValid = false
            currentPasswordError = "Current password is required"
        } else {
            // Do nothing for other cases
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    profileImageUri != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profileImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    currentPhotoUrl != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentPhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Change Photo",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            if (profileImageUri != null || isPhotoUploading) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { 
                        if (!isPhotoUploading) updateProfilePhoto() 
                    },
                    enabled = !isPhotoUploading && profileImageUri != null
                ) {
                    if (isPhotoUploading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Uploading...")
                        }
                    } else {
                        Text("Upload Photo")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Display Name Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Display Name",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        isError = !isNameValid,
                        supportingText = {
                            if (!isNameValid) {
                                Text(nameError)
                            } else {
                                null
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { updateDisplayName() },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !isNameUpdating
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isNameUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                // No loading indicator when not loading
                            }
                            Text("Update Name")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Change Password",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Current Password
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { 
                            currentPassword = it
                            isCurrentPasswordValid = true
                        },
                        label = { Text("Current Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    imageVector = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (currentPasswordVisible) "Hide Password" else "Show Password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = !isCurrentPasswordValid,
                        supportingText = {
                            if (!isCurrentPasswordValid) {
                                Text(currentPasswordError)
                            } else {
                                null
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // New Password
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            newPassword = it
                            validateNewPassword()
                            if (confirmPassword.isNotEmpty()) {
                                validatePasswordMatch()
                            }
                        },
                        label = { Text("New Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = !isNewPasswordValid,
                        supportingText = {
                            if (!isNewPasswordValid) {
                                Text(newPasswordError)
                            } else {
                                null
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            if (newPassword.isNotEmpty()) {
                                validatePasswordMatch()
                            }
                        },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Hide Password" else "Show Password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = !isPasswordsMatch,
                        supportingText = {
                            if (!isPasswordsMatch) {
                                Text(confirmPasswordError)
                            } else {
                                null
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { updatePassword() },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !isPasswordUpdating && (currentPassword.isNotEmpty() || (newPassword.isEmpty() && confirmPassword.isEmpty()))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isPasswordUpdating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                // No loading indicator when not loading
                            }
                            Text("Update Password")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Delete Account Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Delete Account",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Permanently delete your account and all associated data. This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            // Show confirmation dialog (could be implemented as a separate composable)
                            Toast.makeText(context, "This feature is not yet implemented", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Delete Account")
                    }
                }
            }
        }
    }
} 