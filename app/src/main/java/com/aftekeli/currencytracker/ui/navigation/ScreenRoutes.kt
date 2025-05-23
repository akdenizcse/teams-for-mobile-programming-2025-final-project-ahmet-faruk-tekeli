package com.aftekeli.currencytracker.ui.navigation

sealed class ScreenRoutes(val route: String) {
    object LoginScreen : ScreenRoutes("login_screen")
    object RegisterScreen : ScreenRoutes("register_screen")
    object MainAppScreen : ScreenRoutes("main_app_screen")
    
    // Bottom navigation screens
    object MarketsScreen : ScreenRoutes("markets_screen")
    object FavoritesScreen : ScreenRoutes("favorites_screen")
    object ConverterScreen : ScreenRoutes("converter_screen")
    object ProfileScreen : ScreenRoutes("profile_screen")
    object AlarmsScreen : ScreenRoutes("alarms_screen")
    
    // Detail screens
    object CoinDetailScreen : ScreenRoutes("coin_detail_screen")
    
    // Helper function for navigating to coin detail with argument
    fun coinDetailRoute(coinSymbol: String): String {
        return "${CoinDetailScreen.route}/$coinSymbol"
    }
} 