# Firebase Güvenlik Kuralları Kılavuzu

Bu kılavuz, Currency Tracker uygulaması için Firebase Firestore güvenlik kurallarını yapılandırmanıza yardımcı olacaktır.

## Karşılaştığınız Hata

```
PERMISSION_DENIED: Missing or insufficient permissions.
```

Bu hata, uygulamanızın Firebase Firestore veritabanına erişim için gerekli izinlere sahip olmadığını gösteriyor. Firebase Firestore'un varsayılan güvenlik kuralları, kimlik doğrulaması yapmış kullanıcıların bile verilere erişmesini kısıtlamaktadır.

## Çözümü

### 1. Firebase Konsoline Erişin

- [Firebase Console](https://console.firebase.google.com/) adresine gidin
- Projenizi seçin

### 2. Firestore Güvenlik Kurallarını Düzenleyin

- Sol menüden "Firestore Database" seçeneğine tıklayın
- "Rules" sekmesine geçin
- Aşağıdaki kuralları ekleyin:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      // Kullanıcı sadece kendi verilerine erişebilir
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Alt koleksiyonlar için de aynı kural geçerli
      match /{collection}/{docId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

Bu kurallar şunları sağlayacaktır:
- Kullanıcılar yalnızca kendi verilerine erişebilirler (`users/{userId}` ve tüm alt koleksiyonları)
- Kimlik doğrulaması yapılmadan verilere erişim engellenir
- Kullanıcı merkezli yapı sayesinde güvenlik kuralları daha basit ve anlaşılır

### 3. Veritabanı Yapısı

Firestore veritabanımız aşağıdaki kullanıcı merkezli yapıya sahiptir:

```
firestore/
|-- users/
|   |-- {userId}/  # Kullanıcı ID'si (Firebase Auth'tan)
|       |-- displayName: string
|       |-- email: string
|       |-- createdAt: timestamp
|       |-- lastLogin: timestamp
|       |-- preferences/  # Alt koleksiyon
|           |-- userPrefs/  # Döküman
|               |-- defaultCurrency: string
|               |-- theme: string (light/dark/system)
|               |-- refreshInterval: number
|       |-- watchlist/  # Alt koleksiyon
|           |-- {symbol}/  # Örn: "BTCUSDT"
|               |-- symbol: string
|               |-- addedAt: timestamp
|               |-- isCrypto: boolean
|               |-- notes: string
|       |-- alerts/  # Alt koleksiyon
|           |-- {alertId}/
|               |-- symbol: string
|               |-- targetPrice: number
|               |-- isAboveTarget: boolean
|               |-- isActive: boolean
|               |-- createdAt: timestamp
|       |-- conversions/  # Alt koleksiyon
|           |-- {conversionId}/
|               |-- fromCurrency: string
|               |-- toCurrency: string
|               |-- fromAmount: number
|               |-- toAmount: number
|               |-- rate: number
|               |-- timestamp: timestamp
|       |-- searchHistory/  # Alt koleksiyon
|           |-- {searchId}/
|               |-- query: string
|               |-- category: string
|               |-- timestamp: timestamp
```

### 4. Değişiklikleri Yayınlayın

- "Publish" butonuna tıklayarak kuralları uygulayın

## Geçici Çözüm

Uygulamada, kalıcı çözüm olarak güvenlik kurallarını ayarlayana kadar Firebase hatalarını ele almak için bir hata yönetimi mekanizması eklenmiştir. Bu sayede, Firebase bağlantısı olmasa bile uygulama çalışmaya devam edecek ve verileri yerel veritabanında saklayacaktır.

## Notlar

- Bu kurallar, kimlik doğrulaması yapmış kullanıcıların yalnızca kendi verilerine erişmesine izin verir
- Uygulamanın tam işlevselliği için, kullanıcının Firebase Authentication ile oturum açmış olması gerekir
- Güvenlik kurallarının yayınlanması birkaç dakika sürebilir
- Kurallar değiştikten sonra uygulamayı yeniden başlatın
- Çevrimdışı kullanım için FirestoreService'de offline persistence etkinleştirilmiştir 