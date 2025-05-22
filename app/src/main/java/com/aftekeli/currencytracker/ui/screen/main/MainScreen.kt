package com.aftekeli.currencytracker.ui.screen.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aftekeli.currencytracker.ui.navigation.ScreenRoutes
import com.aftekeli.currencytracker.ui.screen.converter.ConverterScreen
import com.aftekeli.currencytracker.ui.screen.favorites.FavoritesScreen
import com.aftekeli.currencytracker.ui.screen.markets.MarketsScreen
import com.aftekeli.currencytracker.ui.screen.profile.ProfileScreen
import com.aftekeli.currencytracker.ui.viewmodel.AuthViewModel

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    parentNavController: NavController
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoutes.MarketsScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenRoutes.MarketsScreen.route) {
                MarketsScreen(navController = parentNavController)
            }
            
            composable(ScreenRoutes.FavoritesScreen.route) {
                FavoritesScreen(
                    navController = parentNavController,
                    authViewModel = authViewModel
                )
            }
            
            composable(ScreenRoutes.ConverterScreen.route) {
                ConverterScreen(navController = parentNavController)
            }
            
            composable(ScreenRoutes.ProfileScreen.route) {
                ProfileScreen(
                    navController = parentNavController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    var selectedItem by remember { mutableStateOf(0) }
    
    val items = listOf(
        BottomNavItem(
            title = "Markets",
            icon = Icons.AutoMirrored.Filled.List,
            route = ScreenRoutes.MarketsScreen.route
        ),
        BottomNavItem(
            title = "Favorites",
            icon = Icons.Filled.Favorite,
            route = ScreenRoutes.FavoritesScreen.route
        ),
        BottomNavItem(
            title = "Converter",
            icon = Icons.Filled.SwapVert,
            route = ScreenRoutes.ConverterScreen.route
        ),
        BottomNavItem(
            title = "Profile",
            icon = Icons.Filled.AccountCircle,
            route = ScreenRoutes.ProfileScreen.route
        )
    )
    
    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title, style = MaterialTheme.typography.bodySmall) },
                selected = currentRoute == item.route,
                onClick = {
                    selectedItem = index
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) 