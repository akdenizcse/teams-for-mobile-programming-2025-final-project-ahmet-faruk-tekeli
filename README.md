# CurrencyTracker

**Author:** Ahmet Faruk Tekeli

## Açıklama

CurrencyTracker, kullanıcıların kripto para piyasalarını takip etmelerine, favori coin'lerini kaydetmelerine, coin detaylarını grafiklerle incelemelerine ve para birimi dönüştürme işlemleri yapmalarına olanak tanıyan bir mobil uygulamadır. Uygulama, güncel piyasa verilerini Binance API üzerinden çekmektedir.

## Özellikler

*   **Piyasa Takibi:** Güncel kripto para fiyatlarını, değişim yüzdelerini ve hacimlerini listeler. ([`MarketsScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/markets/MarketsScreen.kt))
*   **Coin Detayları:** Seçilen bir coinin ayrıntılı bilgilerini (24 saatlik en yüksek/düşük, hacim, alış/satış fiyatları vb.) ve fiyat geçmişi grafiğini gösterir. ([`CoinDetailScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/detail/CoinDetailScreen.kt))
*   **Para Birimi Dönüştürücü:** Farklı kripto paralar ve USDT arasında dönüşüm yapar. ([`ConverterScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/converter/ConverterScreen.kt))
*   **Favoriler:** Kullanıcıların favori coin'lerini işaretleyip ayrı bir ekranda takip etmelerini sağlar (Giriş gerektirir). ([`FavoritesScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/favorites/FavoritesScreen.kt))
*   **Kullanıcı Girişi ve Kayıt:** Firebase Authentication ile kullanıcı girişi ve kayıt işlemleri. ([`LoginScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/auth/LoginScreen.kt), [`RegisterScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/auth/RegisterScreen.kt))
*   **Profil Yönetimi:** Kullanıcı bilgilerini görüntüleme, şifre değiştirme ve tema ayarlarını (Açık, Koyu, Sistem) yapabilme. ([`ProfileScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/profile/ProfileScreen.kt))
*   **Arama ve Sıralama:** Coin listelerinde arama yapma ve isme, fiyat değişimine, hacme göre sıralama. ([`MarketsViewModel.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/viewmodel/MarketsViewModel.kt))
*   **Veri Yenileme:** "Pull-to-refresh" özelliği ile verileri manuel olarak yenileme.
*   **Yerel Veritabanı:** Room Persistence Library ile piyasa verilerini ve favori coin'leri yerel olarak önbelleğe alma. ([`AppDatabase.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/data/local/db/AppDatabase.kt))

## Kullanılan Teknolojiler ve Kütüphaneler

*   **Programlama Dili:** Kotlin
*   **UI:** Jetpack Compose
*   **Asenkron İşlemler:** Kotlin Coroutines
*   **Networking:** Retrofit, OkHttp (Binance API ile iletişim için - [`BinanceApiService.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/data/remote/api/BinanceApiService.kt))
*   **Dependency Injection:** Hilt ([`AppModule.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/di/AppModule.kt), [`NetworkModule.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/di/NetworkModule.kt), [`DatabaseModule.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/di/DatabaseModule.kt))
*   **Veritabanı:** Room Persistence Library
*   **Backend Servisleri:** Firebase (Authentication, Firestore)
*   **Grafikler:** MPAndroidChart ([`CoinDetailScreen.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/screen/detail/CoinDetailScreen.kt) içinde kullanımı)
*   **Resim Yükleme:** Coil
*   **Mimari Desen:** MVVM (Model-View-ViewModel)
*   **Navigasyon:** Jetpack Navigation Component ([`AppNavigation.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/ui/navigation/AppNavigation.kt))
*   **Build Sistemi:** Gradle ([`build.gradle.kts (app)`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/build.gradle.kts), [`build.gradle.kts (project)`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/build.gradle.kts))

## Kurulum

1.  Projeyi klonlayın: `git clone https://github.com/kullaniciadi/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli.git`
2.  Android Studio'da projeyi açın.
3.  Gerekli Firebase yapılandırmalarını (google-services.json) projenize ekleyin.
4.  Projeyi derleyin ve çalıştırın.

## API Entegrasyonu

Uygulama, güncel kripto para verilerini çekmek için [Binance API](https://github.com/binance/binance-spot-api-docs) kullanmaktadır. API endpoint'leri [`BinanceApiService.kt`](/Users/ahmettekeli/Desktop/mobil teslim/teams-for-mobile-programming-2025-final-project-ahmet-faruk-tekeli/app/src/main/java/com/aftekeli/currencytracker/data/remote/api/BinanceApiService.kt) dosyasında tanımlanmıştır.

## Ekran Görüntüleri

*(Buraya uygulamanın temel ekranlarından görüntüler eklenebilir.)*

## Gelecekteki Geliştirmeler

*   Fiyat alarmları ekleme.
*   Portföy takibi özelliği.
*   Daha fazla kripto para borsası entegrasyonu.
*   Widget desteği.