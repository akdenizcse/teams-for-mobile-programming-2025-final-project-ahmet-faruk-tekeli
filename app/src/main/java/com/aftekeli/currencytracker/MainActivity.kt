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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.aftekeli.currencytracker.ui.screens.AlertsScreen
import com.aftekeli.currencytracker.ui.screens.ConverterScreen
import com.aftekeli.currencytracker.ui.screens.HomeScreen
import com.aftekeli.currencytracker.ui.screens.LoginScreen
import com.aftekeli.currencytracker.ui.screens.ProfileScreen
import com.aftekeli.currencytracker.ui.screens.RegisterScreen
import com.aftekeli.currencytracker.ui.screens.WatchlistScreen
import com.aftekeli.currencytracker.ui.theme.CurrencyTrackerTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CurrencyTrackerTheme {
                CurTracApp()
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Watchlist : BottomNavItem("watchlist", Icons.Default.Star, "Watchlist")
    object Converter : BottomNavItem("converter", Icons.Default.CurrencyExchange, "Converter")
    object Alerts : BottomNavItem("alerts", Icons.Default.Notifications, "Alerts")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profil")
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
        BottomNavItem.Watchlist,
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
                composable(BottomNavItem.Watchlist.route) {
                    WatchlistScreen(navController = navController)
                }
                composable(BottomNavItem.Converter.route) {
                    ConverterScreen(navController = navController)
                }
                composable(BottomNavItem.Alerts.route) {
                    AlertsScreen(navController = navController)
                }
                composable(BottomNavItem.Profile.route) {
                    ProfileScreen(navController = navController)
                }
            }
        }
    }
}
