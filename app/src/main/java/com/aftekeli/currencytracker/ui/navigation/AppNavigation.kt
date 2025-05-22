package com.aftekeli.currencytracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aftekeli.currencytracker.ui.screen.auth.LoginScreen
import com.aftekeli.currencytracker.ui.screen.auth.RegisterScreen
import com.aftekeli.currencytracker.ui.screen.detail.CoinDetailScreen
import com.aftekeli.currencytracker.ui.screen.main.MainScreen
import com.aftekeli.currencytracker.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    viewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    // Always start with the main app screen to allow guest access
    NavHost(
        navController = navController,
        startDestination = ScreenRoutes.MainAppScreen.route
    ) {
        composable(ScreenRoutes.LoginScreen.route) {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate(ScreenRoutes.RegisterScreen.route)
                },
                onNavigateToMain = {
                    navController.navigate(ScreenRoutes.MainAppScreen.route) {
                        // Clear the back stack when navigating to main after login
                        popUpTo(ScreenRoutes.MainAppScreen.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(ScreenRoutes.RegisterScreen.route) {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    navController.navigate(ScreenRoutes.LoginScreen.route)
                },
                onNavigateToMain = {
                    navController.navigate(ScreenRoutes.MainAppScreen.route) {
                        // Clear the back stack when navigating to main after registration
                        popUpTo(ScreenRoutes.MainAppScreen.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(ScreenRoutes.MainAppScreen.route) {
            MainScreen(
                authViewModel = viewModel,
                // The onLogout callback no longer navigates to the login screen
                onLogout = {
                    viewModel.logoutUser()
                },
                parentNavController = navController
            )
        }
        
        // Coin Detail Screen
        composable(
            route = "${ScreenRoutes.CoinDetailScreen.route}/{coinSymbol}",
            arguments = listOf(
                navArgument("coinSymbol") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            CoinDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
} 