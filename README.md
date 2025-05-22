# CurrencyTracker

**Author:** Ahmet Faruk Tekeli

## Description

CurrencyTracker is a mobile application that allows users to track cryptocurrency markets, save their favorite coins, examine coin details with charts, and perform currency conversion operations. The application pulls current market data via the Binance API.

## Features

*   **Market Tracking:** Lists current cryptocurrency prices, percentage changes, and volumes. ([`MarketsScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/markets/MarketsScreen.kt))
*   **Coin Details:** Shows detailed information for a selected coin (24-hour high/low, volume, bid/ask prices, etc.) and a price history chart. ([`CoinDetailScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/detail/CoinDetailScreen.kt))
*   **Currency Converter:** Converts between different cryptocurrencies and USDT. ([`ConverterScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/converter/ConverterScreen.kt))
*   **Favorites:** Allows users to mark their favorite coins and track them on a separate screen (Login required). ([`FavoritesScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/favorites/FavoritesScreen.kt))
*   **User Login and Registration:** User login and registration processes with Firebase Authentication. ([`LoginScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/auth/LoginScreen.kt), [`RegisterScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/auth/RegisterScreen.kt))
*   **Profile Management:** View user information, change password, and adjust theme settings (Light, Dark, System). ([`ProfileScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/profile/ProfileScreen.kt))
*   **Search and Sort:** Search within coin lists and sort by name, price change, and volume. ([`MarketsViewModel.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/viewmodel/MarketsViewModel.kt))
*   **Data Refresh:** Manually refresh data with the "Pull-to-refresh" feature.
*   **Local Database:** Cache market data and favorite coins locally using Room Persistence Library. ([`AppDatabase.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/data/local/db/AppDatabase.kt))

## Technologies and Libraries Used

*   **Programming Language:** Kotlin
*   **UI:** Jetpack Compose
*   **Asynchronous Operations:** Kotlin Coroutines
*   **Networking:** Retrofit, OkHttp (for communication with Binance API - [`BinanceApiService.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/data/remote/api/BinanceApiService.kt))
*   **Dependency Injection:** Hilt ([`AppModule.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/di/AppModule.kt), [`NetworkModule.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/di/NetworkModule.kt), [`DatabaseModule.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/di/DatabaseModule.kt))
*   **Database:** Room Persistence Library
*   **Backend Services:** Firebase (Authentication, Firestore)
*   **Charts:** MPAndroidChart (used in [`CoinDetailScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/detail/CoinDetailScreen.kt))
*   **Image Loading:** Coil
*   **Architectural Pattern:** MVVM (Model-View-ViewModel)
*   **Navigation:** Jetpack Navigation Component ([`AppNavigation.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/navigation/AppNavigation.kt))
*   **Build System:** Gradle ([`build.gradle.kts (app)`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/build.gradle.kts), [`build.gradle.kts (project)`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/build.gradle.kts))

## Setup

1.  Clone the project: `git clone https://github.com/username/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli.git`
2.  Open the project in Android Studio.
3.  Add the necessary Firebase configurations (google-services.json) to your project.
4.  Build and run the project.

## API Integration

The application uses the [Binance API](https://github.com/binance/binance-spot-api-docs) to fetch current cryptocurrency data. API endpoints are defined in the [`BinanceApiService.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/data/remote/api/BinanceApiService.kt) file.

## Screenshots

*(Screenshots of the application's main screens can be added here.)*

## Future Enhancements

*   Add price alerts.
*   Portfolio tracking feature.
*   Integration with more cryptocurrency exchanges.
*   Widget support.