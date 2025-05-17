package com.aftekeli.currencytracker.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.aftekeli.currencytracker.CurrencyTrackerApp
import com.aftekeli.currencytracker.ui.screens.settings.dialogs.CurrencySelectionDialog
import com.aftekeli.currencytracker.ui.theme.CurrencyTrackerTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val settingsManager = remember { CurrencyTrackerApp.getInstance().settingsManager }
    val coroutineScope = rememberCoroutineScope()
    
    // Collect settings from DataStore preferences
    val darkMode by settingsManager.darkMode.collectAsState(initial = false)
    val defaultCurrency by settingsManager.defaultCurrency.collectAsState(initial = "USD")
    val priceAlertsEnabled by settingsManager.priceAlertsEnabled.collectAsState(initial = true)
    
    // Dialog state
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            selectedCurrency = defaultCurrency,
            onSelectCurrency = { currency ->
                coroutineScope.launch {
                    settingsManager.setDefaultCurrency(currency)
                }
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
    
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache") },
            text = { Text("Are you sure you want to clear all cached data? This will reset your settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            settingsManager.clearCache()
                            snackbarHostState.showSnackbar("Cache cleared successfully")
                            showClearCacheDialog = false
                        }
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsCategory(title = "Appearance")
            
            SettingsSwitch(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Toggle dark or light theme",
                checked = darkMode,
                onCheckedChange = { enabled ->
                    coroutineScope.launch {
                        settingsManager.setDarkMode(enabled)
                    }
                }
            )
            
            SettingsCategory(title = "Currencies")
            
            SettingsItem(
                icon = Icons.Default.Favorite,
                title = "Default Currency",
                subtitle = defaultCurrency,
                onClick = { showCurrencyDialog = true }
            )
            
            SettingsCategory(title = "Notifications")
            
            SettingsSwitch(
                icon = Icons.Default.Notifications,
                title = "Price Alerts",
                subtitle = "Receive notifications when prices change significantly",
                checked = priceAlertsEnabled,
                onCheckedChange = { enabled ->
                    coroutineScope.launch {
                        settingsManager.setPriceAlertsEnabled(enabled)
                    }
                }
            )
            
            SettingsCategory(title = "Data")
            
            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Clear Cache",
                subtitle = "Clear locally stored currency data",
                onClick = { showClearCacheDialog = true }
            )
        }
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    Divider(modifier = Modifier.padding(start = 56.dp))
}

@Composable
fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
    
    Divider(modifier = Modifier.padding(start = 56.dp))
} 