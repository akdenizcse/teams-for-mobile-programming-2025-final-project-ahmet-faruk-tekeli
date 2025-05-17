// Simplified MainActivity to get the app running
package com.aftekeli.currencytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aftekeli.currencytracker.ui.screens.alerts.AlertsScreen
import com.aftekeli.currencytracker.ui.screens.converter.ConvertScreen
import com.aftekeli.currencytracker.ui.screens.home.HomeScreen
import com.aftekeli.currencytracker.ui.screens.auth.LoginScreen
import com.aftekeli.currencytracker.ui.screens.profile.ProfileScreen
import com.aftekeli.currencytracker.ui.screens.auth.RegisterScreen
import com.aftekeli.currencytracker.ui.screens.settings.SettingsScreen
import com.aftekeli.currencytracker.ui.screens.profile.AccountSettingsScreen
import com.aftekeli.currencytracker.ui.theme.CurrencyTrackerTheme
import com.aftekeli.currencytracker.ui.screens.market.CurrencyDetailScreen
import com.aftekeli.currencytracker.ui.screens.history.HistoryScreen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Get the SettingsManager instance
            val settingsManager = remember { (application as CurrencyTrackerApp).settingsManager }
            
            // Collect the dark mode preference
            val darkMode by settingsManager.darkMode.collectAsState(initial = false)
            
            CurrencyTrackerTheme(darkTheme = darkMode) {
                CurTracApp()
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object History : BottomNavItem("history", Icons.Default.History, "History")
    object Converter : BottomNavItem("converter", Icons.Default.CurrencyExchange, "Converter")
    object Alerts : BottomNavItem("alerts", Icons.Default.Notifications, "Alerts")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}

@Composable
fun CurTracApp() {
    val navController = rememberNavController()
    val auth = remember { Firebase.auth }
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
    
    // Listen for auth changes
    LaunchedEffect(auth) {
        auth.addAuthStateListener { firebaseAuth ->
            isLoggedIn = firebaseAuth.currentUser != null
        }
    }
    
    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Converter,
        BottomNavItem.Alerts,
        BottomNavItem.Profile
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute in navItems.map { it.route }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) BottomNavItem.Home.route else "login"
            ) {
                composable("login") {
                    LoginScreen(navController = navController)
                }
                composable("register") {
                    RegisterScreen(navController = navController)
                }
                
                // Main navigation items
                composable(BottomNavItem.Home.route) {
                    HomeScreen(navController = navController)
                }
                composable(BottomNavItem.History.route) {
                    HistoryScreen(navController = navController)
                }
                composable(BottomNavItem.Converter.route) {
                    ConvertScreen(navController = navController)
                }
                composable(BottomNavItem.Alerts.route) {
                    AlertsScreen(navController = navController)
                }
                composable(BottomNavItem.Profile.route) {
                    ProfileScreen(navController = navController)
                }
                
                // Secondary screens
                composable("settings") {
                    SettingsScreen(navController = navController)
                }
                
                // Account settings screen
                composable("account_settings") {
                    AccountSettingsScreen(navController = navController)
                }
                
                // Currency detail screen
                composable("currency_detail/{symbol}") { backStackEntry ->
                    val symbol = backStackEntry.arguments?.getString("symbol")
                    if (symbol != null) {
                        CurrencyDetailScreen(
                            symbol = symbol,
                            onBackClick = { navController.popBackStack() },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
