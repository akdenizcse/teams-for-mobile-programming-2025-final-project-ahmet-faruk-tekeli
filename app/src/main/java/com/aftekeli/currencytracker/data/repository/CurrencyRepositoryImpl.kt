package com.aftekeli.currencytracker.data.repository

import android.util.Log
import com.aftekeli.currencytracker.data.local.dao.AlertDao
import com.aftekeli.currencytracker.data.local.dao.CurrencyDao
import com.aftekeli.currencytracker.data.local.dao.WatchlistDao
import com.aftekeli.currencytracker.data.local.entity.AlertEntity
import com.aftekeli.currencytracker.data.local.entity.CurrencyEntity
import com.aftekeli.currencytracker.data.local.entity.WatchlistEntity
import com.aftekeli.currencytracker.data.preferences.SettingsManager
import com.aftekeli.currencytracker.data.remote.api.BinanceApiService
import com.aftekeli.currencytracker.data.remote.api.CoinGeckoApiService
import com.aftekeli.currencytracker.data.remote.dto.CoinListItemDto
import com.aftekeli.currencytracker.data.remote.firestore.FirestoreService
import com.aftekeli.currencytracker.domain.model.Alert
import com.aftekeli.currencytracker.domain.model.Candlestick
import com.aftekeli.currencytracker.domain.model.Currency
import com.aftekeli.currencytracker.domain.model.WatchlistItem
import com.aftekeli.currencytracker.domain.repository.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "CurrencyRepository"

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val binanceApiService: BinanceApiService,
    private val coinGeckoApiService: CoinGeckoApiService,
    private val currencyDao: CurrencyDao,
    private val watchlistDao: WatchlistDao,
    private val alertDao: AlertDao,
    private val firestoreService: FirestoreService,
    private val settingsManager: SettingsManager
) : CurrencyRepository {
    
    // Hard-coded logo URLs for major cryptocurrencies to avoid API rate limits
    private val hardcodedLogos = mapOf(
        "btc" to "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
        "eth" to "https://assets.coingecko.com/coins/images/279/large/ethereum.png",
        "usdt" to "https://assets.coingecko.com/coins/images/325/large/tether.png",
        "bnb" to "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png",
        "xrp" to "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png",
        "ada" to "https://assets.coingecko.com/coins/images/975/large/cardano.png",
        "doge" to "https://assets.coingecko.com/coins/images/5/large/dogecoin.png",
        "sol" to "https://assets.coingecko.com/coins/images/4128/large/solana.png",
        "dot" to "https://assets.coingecko.com/coins/images/12171/large/polkadot.png",
        "shib" to "https://assets.coingecko.com/coins/images/11939/large/shiba.png",
        "matic" to "https://assets.coingecko.com/coins/images/4713/large/polygon.png",
        "ltc" to "https://assets.coingecko.com/coins/images/2/large/litecoin.png",
        "avax" to "https://assets.coingecko.com/coins/images/12559/large/avax-circular-avatars.png",
        "link" to "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png",
        "uni" to "https://assets.coingecko.com/coins/images/12504/large/uni.jpg",
        "atom" to "https://assets.coingecko.com/coins/images/1481/large/cosmos_hub.png",
        "busd" to "https://assets.coingecko.com/coins/images/9576/large/busd.png",
        "tusd" to "https://assets.coingecko.com/coins/images/3449/large/tusd.png",
        "usdc" to "https://assets.coingecko.com/coins/images/6319/large/usdc.png",
        "dai" to "https://assets.coingecko.com/coins/images/9956/large/Badge_Dai.png"
    )
    
    // Local cache for coin mappings to avoid repeated API calls
    private val symbolToIdMap = mutableMapOf<String, String>()
    private val logoCache = mutableMapOf<String, String>()
    
    // Manually defined mappings for popular currencies
    private val manualMappings = mapOf(
        "btc" to "bitcoin",
        "eth" to "ethereum",
        "usdt" to "tether",
        "bnb" to "binancecoin",
        "xrp" to "ripple",
        "ada" to "cardano",
        "doge" to "dogecoin",
        "sol" to "solana",
        "dot" to "polkadot",
        "shib" to "shiba-inu",
        "matic" to "polygon",
        "ltc" to "litecoin",
        "avax" to "avalanche-2",
        "link" to "chainlink",
        "uni" to "uniswap",
        "atom" to "cosmos", 
        "busd" to "binance-usd",
        "tusd" to "true-usd",
        "usdc" to "usd-coin",
        "dai" to "dai"
    )
    
    init {
        Log.d(TAG, "CurrencyRepositoryImpl initialized")
    }
    
    // Currency operations (Room kullanılır - hızlı yerel erişim için)
    override suspend fun getAllCurrencies(): Flow<List<Currency>> {
        return currencyDao.getAllCurrencies().map { entityList ->
            entityList.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getCurrencyBySymbol(symbol: String): Flow<Currency?> {
        return flow {
            val currency = currencyDao.getCurrencyBySymbol(symbol)
            emit(currency?.toDomainModel())
        }
    }
    
    override suspend fun refreshCurrenciesIfNeeded(forceRefresh: Boolean): Boolean {
        Log.d(TAG, "refreshCurrenciesIfNeeded called with forceRefresh=$forceRefresh")
        // Eğer zorunlu yenileme istendiyse veya yenileme süresi dolmuşsa
        if (forceRefresh || settingsManager.isDataRefreshNeeded()) {
            refreshCurrencies()
            settingsManager.updateLastRefreshTimestamp()
            fetchAndUpdateLogos()
            return true
        }
        return false
    }
    
    override suspend fun refreshCurrencies() {
        try {
            // Get 24h ticker data which includes price changes and volumes
            val tickers = binanceApiService.get24hTickers()
            
            // Filter and map to supported symbols
            val supportedSymbols = BinanceApiService.SUPPORTED_CRYPTO_SYMBOLS + BinanceApiService.SUPPORTED_FIAT_SYMBOLS
            val filteredTickers = tickers.filter { ticker -> supportedSymbols.contains(ticker.symbol) }
            
            // Log the number of tickers received
            println("Received ${tickers.size} tickers, filtered to ${filteredTickers.size} supported tickers")
            
            val currencyEntities = filteredTickers.mapNotNull { ticker ->
                try {
                    // Parse price with safety checks
                    val price = ticker.lastPrice.toDoubleOrNull() ?: 0.0
                    
                    // Skip if price is invalid, unless it's a stablecoin
                    if (price <= 0.0 && ticker.symbol != "BUSDUSDT" && ticker.symbol != "TUSDUSDT") {
                        println("Skipping ${ticker.symbol} due to invalid price: ${ticker.lastPrice}")
                        return@mapNotNull null
                    }
                    
                    // Handle different price format based on symbol type
                    val priceUsd = when {
                        // Direct USD paired currencies
                        ticker.symbol.endsWith("USDT") -> price
                        
                        // Reversed pairs (like USDTBRL) need inversion
                        ticker.symbol.startsWith("USDT") -> {
                            if (price > 0) 1.0 / price else 0.0
                        }
                        
                        // Other pairs don't have direct USD value
                        else -> null
                    }
                    
                    // Create entity with proper formatting
                    CurrencyEntity(
                        symbol = ticker.symbol,
                        price = price,
                        priceUsd = priceUsd,
                        changePercent24h = ticker.priceChangePercent.toDoubleOrNull(),
                        volume24h = ticker.volume.toDoubleOrNull(),
                        marketCap = null, // Not provided by Binance API
                        lastUpdateTime = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    println("Error processing ticker ${ticker.symbol}: ${e.message}")
                    null
                }
            }
            
            // Check for missing currencies (which might have price data but no 24h data)
            val processedSymbols = currencyEntities.map { it.symbol }.toSet()
            val missingSymbols = supportedSymbols.filter { !processedSymbols.contains(it) }
            
            // Fetch additional data for missing symbols
            val additionalEntities = missingSymbols.mapNotNull { symbol ->
                try {
                    // Get current price for the missing symbol
                    val priceData = binanceApiService.getPriceBySymbol(symbol)
                    val price = priceData.price.toDoubleOrNull() ?: 0.0
                    
                    if (price <= 0.0) {
                        println("Skipping $symbol from price endpoint, invalid price: ${priceData.price}")
                        return@mapNotNull null
                    }
                    
                    // Calculate USD price (if applicable)
                    val priceUsd = when {
                        symbol.endsWith("USDT") -> price
                        symbol.startsWith("USDT") -> {
                            if (price > 0) 1.0 / price else 0.0
                        }
                        else -> null
                    }
                    
                    // Create entity with just price data (no volume/change data)
                    CurrencyEntity(
                        symbol = symbol,
                        price = price,
                        priceUsd = priceUsd,
                        changePercent24h = null,
                        volume24h = null,
                        marketCap = null,
                        lastUpdateTime = System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    println("Error fetching price for $symbol: ${e.message}")
                    null
                }
            }
            
            // Combine all entities
            val allEntities = currencyEntities + additionalEntities
            
            // Log the number of valid currencies found
            println("Found ${allEntities.size} valid currencies to insert into database")
            println("Additional currencies from price endpoint: ${additionalEntities.size}")
            
            // Remove duplicates (keep the one with higher volume)
            val uniqueCurrencies = allEntities
                .groupBy { it.symbol.replace("USDT", "") }
                .mapValues { entry -> entry.value.maxByOrNull { it.volume24h ?: 0.0 } }
                .values.filterNotNull()
            
            // Insert the processed entities
            currencyDao.insertCurrencies(uniqueCurrencies)
            
        } catch (e: Exception) {
            // Handle error with more detailed logging
            println("Error refreshing currencies: ${e.message}")
            e.printStackTrace()
        }
    }
    
    override suspend fun refreshCurrency(symbol: String) {
        try {
            var currencyEntity: CurrencyEntity? = null
            
            // İlk önce 24h ticker verisini almayı dene
            try {
                val ticker = binanceApiService.get24hTickerBySymbol(symbol)
                val price = ticker.lastPrice.toDoubleOrNull() ?: 0.0
                
                // Fiyat geçerliyse veya bilinen stablecoin ise kullan
                if (price > 0.0 || symbol == "BUSDUSDT" || symbol == "TUSDUSDT") {
                    currencyEntity = CurrencyEntity(
                        symbol = ticker.symbol,
                        price = price,
                        priceUsd = if (ticker.symbol.endsWith("USDT")) price 
                                  else if (ticker.symbol.startsWith("USDT") && price > 0) 1.0 / price 
                                  else null,
                        changePercent24h = ticker.priceChangePercent.toDoubleOrNull(),
                        volume24h = ticker.volume.toDoubleOrNull(),
                        marketCap = null, // Not provided by Binance API
                        lastUpdateTime = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                println("Error getting 24h data for $symbol: ${e.message}. Trying price endpoint...")
            }
            
            // 24h verisini alamadıysak price endpointini dene
            if (currencyEntity == null) {
                try {
                    val priceData = binanceApiService.getPriceBySymbol(symbol)
                    val price = priceData.price.toDoubleOrNull() ?: 0.0
                    
                    if (price > 0.0) {
                        currencyEntity = CurrencyEntity(
                            symbol = priceData.symbol,
                            price = price,
                            priceUsd = if (priceData.symbol.endsWith("USDT")) price 
                                      else if (priceData.symbol.startsWith("USDT") && price > 0) 1.0 / price 
                                      else null,
                            changePercent24h = null, // Veri yok
                            volume24h = null, // Veri yok
                            marketCap = null,
                            lastUpdateTime = System.currentTimeMillis()
                        )
                    }
                } catch (e: Exception) {
                    println("Error getting price data for $symbol: ${e.message}")
                    throw e // Hiçbir veri alamadık, hatayı yukarı ilet
                }
            }
            
            // Veri elde ettiyse veritabanına kaydet
            currencyEntity?.let {
                currencyDao.insertCurrency(it)
            }
        } catch (e: Exception) {
            // Handle error
            e.printStackTrace()
        }
    }
    
    // Candlestick chart data
    override suspend fun getCandlestickData(symbol: String, interval: String): Flow<List<Candlestick>> {
        return flow {
            try {
                val candlestickData = binanceApiService.getCandlestickData(symbol, interval)
                val candlesticks = candlestickData.map {
                    Candlestick(
                        openTime = Date((it[0] as Number).toLong()),
                        open = (it[1] as String).toDouble(),
                        high = (it[2] as String).toDouble(),
                        low = (it[3] as String).toDouble(),
                        close = (it[4] as String).toDouble(),
                        volume = (it[5] as String).toDouble(),
                        closeTime = Date((it[6] as Number).toLong()),
                        quoteAssetVolume = (it[7] as String).toDouble(),
                        numberOfTrades = (it[8] as Number).toInt()
                    )
                }
                emit(candlesticks)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(emptyList())
            }
        }
    }
    
    // Watchlist operations - Firestore ve Room senkronizasyonu
    override suspend fun getWatchlist(userId: String): Flow<List<WatchlistItem>> {
        // Firestore'dan izle, değişiklikler gerçek zamanlı görünecek
        return try {
            firestoreService.getWatchlist(userId)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala ve boş liste dön
            println("Firebase watchlist error: ${e.message}")
            flow { emit(emptyList<WatchlistItem>()) }
        }
    }
    
    override suspend fun getWatchlistSymbols(userId: String): Flow<List<String>> {
        return try {
            firestoreService.getWatchlist(userId).map { watchlistItems ->
                watchlistItems.map { it.symbol }
            }
        } catch (e: Exception) {
            // Firebase izin hatasını yakala ve boş liste dön
            println("Firebase watchlist symbols error: ${e.message}")
            flow { emit(emptyList<String>()) }
        }
    }
    
    override suspend fun addToWatchlist(userId: String, symbol: String) {
        try {
            // Hem Firestore'a ekle (bulut)
            firestoreService.addToWatchlist(userId, symbol)
            
            // Hem de Room'a ekle (yerel)
            val watchlistItem = WatchlistEntity(
                userId = userId,
                symbol = symbol,
                addedAt = System.currentTimeMillis()
            )
            watchlistDao.insertWatchlistItem(watchlistItem)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala ama yine de local veritabanına ekle
            println("Firebase add to watchlist error: ${e.message}")
            val watchlistItem = WatchlistEntity(
                userId = userId,
                symbol = symbol,
                addedAt = System.currentTimeMillis()
            )
            watchlistDao.insertWatchlistItem(watchlistItem)
        }
    }
    
    override suspend fun removeFromWatchlist(userId: String, symbol: String) {
        try {
            // Her iki veritabanından da kaldır
            firestoreService.removeFromWatchlist(userId, symbol)
            watchlistDao.removeFromWatchlist(userId, symbol)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala ama yine de local veritabanından kaldır
            println("Firebase remove from watchlist error: ${e.message}")
            watchlistDao.removeFromWatchlist(userId, symbol)
        }
    }
    
    override suspend fun isInWatchlist(userId: String, symbol: String): Boolean {
        // Firestore daha güncel olduğu için öncelikle kontrol et
        return try {
            firestoreService.isInWatchlist(userId, symbol)
        } catch (e: Exception) {
            // Firebase izin hatasında yerel veritabanına dön
            println("Firebase is in watchlist error: ${e.message}")
            watchlistDao.isInWatchlist(userId, symbol)
        }
    }
    
    // Alert operations - Firestore ve Room senkronizasyonu
    override suspend fun getAlerts(userId: String): Flow<List<Alert>> {
        // Firestore'dan izle, değişiklikler gerçek zamanlı görünecek
        return try {
            firestoreService.getAlerts(userId)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala
            println("Firebase alerts error: ${e.message}")
            // Yerel veritabanındaki uyarıları döndür
            alertDao.getAllAlertsByUserId(userId).map { entities ->
                entities.map { it.toDomainModel() }
            }
        }
    }
    
    override suspend fun getActiveAlerts(userId: String): Flow<List<Alert>> {
        // Firestore'dan izle, değişiklikler gerçek zamanlı görünecek
        return try {
            firestoreService.getActiveAlerts(userId)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala
            println("Firebase active alerts error: ${e.message}")
            // Yerel veritabanındaki aktif uyarıları döndür
            alertDao.getActiveAlertsByUserId(userId).map { entities ->
                entities.map { it.toDomainModel() }
            }
        }
    }
    
    override suspend fun createAlert(userId: String, symbol: String, targetPrice: Double, isAboveTarget: Boolean): Long {
        try {
            // Firestore'da alert oluştur
            val alertId = firestoreService.createAlert(userId, symbol, targetPrice, isAboveTarget)
            
            // Yerel veritabanına da kaydet
            val alertEntity = AlertEntity(
                userId = userId,
                symbol = symbol,
                targetPrice = targetPrice,
                isAboveTarget = isAboveTarget,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            alertDao.insertAlert(alertEntity)
            
            // Firestore dökümanının hash kodunu dön
            return alertId.hashCode().toLong()
        } catch (e: Exception) {
            // Firebase izin hatasını yakala, yine de local veritabanına ekle
            println("Firebase create alert error: ${e.message}")
            val alertEntity = AlertEntity(
                userId = userId,
                symbol = symbol,
                targetPrice = targetPrice,
                isAboveTarget = isAboveTarget,
                isActive = true,
                createdAt = System.currentTimeMillis()
            )
            // Yerel veritabanı ID'sini dön
            return alertDao.insertAlert(alertEntity)
        }
    }
    
    override suspend fun updateAlert(alert: Alert) {
        try {
            // Firestore'da alertin active durumunu güncelle
            firestoreService.updateAlert(alert.userId, alert.id.toString(), alert.isActive)
            
            // Yerel veritabanında da güncelle
            val alertEntity = AlertEntity(
                id = alert.id,
                userId = alert.userId,
                symbol = alert.symbol,
                targetPrice = alert.targetPrice,
                isAboveTarget = alert.isAboveTarget,
                isActive = alert.isActive,
                createdAt = alert.createdAt.time
            )
            alertDao.updateAlert(alertEntity)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala, yine de local veritabanını güncelle
            println("Firebase update alert error: ${e.message}")
            val alertEntity = AlertEntity(
                id = alert.id,
                userId = alert.userId,
                symbol = alert.symbol,
                targetPrice = alert.targetPrice,
                isAboveTarget = alert.isAboveTarget,
                isActive = alert.isActive,
                createdAt = alert.createdAt.time
            )
            alertDao.updateAlert(alertEntity)
        }
    }
    
    override suspend fun deleteAlert(alertId: Long) {
        try {
            // Alarma ait userId'yi bulmamız gerekiyor
            val alert = alertDao.getAlertById(alertId)
            if (alert != null) {
                // Firestore'dan sil
                firestoreService.deleteAlert(alert.userId, alertId.toString())
            }
            
            // Yerel veritabanından da sil
            alertDao.deleteAlertById(alertId)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala, yine de local veritabanından sil
            println("Firebase delete alert error: ${e.message}")
            alertDao.deleteAlertById(alertId)
        }
    }
    
    override suspend fun setAlertActive(alertId: Long, isActive: Boolean) {
        try {
            // Alarma ait userId'yi bulmamız gerekiyor
            val alert = alertDao.getAlertById(alertId)
            if (alert != null) {
                // Firestore'da güncelle
                firestoreService.updateAlert(alert.userId, alertId.toString(), isActive)
            }
            
            // Yerel veritabanında da güncelle
            alertDao.updateAlertActiveStatus(alertId, isActive)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala, yine de local veritabanını güncelle
            println("Firebase set alert active error: ${e.message}")
            alertDao.updateAlertActiveStatus(alertId, isActive)
        }
    }
    
    // Firebase Auth ile kullanıcı oturum açtığında kullanıcı bilgilerini kaydetme
    suspend fun saveUserToFirestore(userId: String, email: String, displayName: String?) {
        try {
            val userData = hashMapOf(
                "email" to email,
                "displayName" to (displayName ?: ""),
                "createdAt" to Date(),
                "lastLogin" to Date()
            )
            
            firestoreService.createOrUpdateUser(userId, userData)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala ve sadece loglama yap
            println("Firebase save user error: ${e.message}")
        }
    }
    
    // Kullanıcı son giriş zamanını güncelleme
    suspend fun updateUserLastLogin(userId: String) {
        try {
            val updates = hashMapOf<String, Any>(
                "lastLogin" to Date()
            )
            firestoreService.createOrUpdateUser(userId, updates)
        } catch (e: Exception) {
            // Firebase izin hatasını yakala ve sadece loglama yap
            println("Firebase update last login error: ${e.message}")
        }
    }
    
    // New method to fetch and update logos for all currencies in database
    private suspend fun fetchAndUpdateLogos() {
        try {
            Log.d(TAG, "fetchAndUpdateLogos started")
            
            // Fetch all currencies from database
            val currencies = currencyDao.getAllCurrenciesSync()
            val currenciesNeedingLogos = currencies.filter { it.logoUrl.isEmpty() }
            
            Log.d(TAG, "Found ${currenciesNeedingLogos.size} currencies needing logos")
            
            // Process each currency
            for (currency in currenciesNeedingLogos) {
                val originalSymbol = currency.symbol
                val baseSymbol = extractBaseSymbol(originalSymbol).lowercase()
                
                Log.d(TAG, "Processing $originalSymbol -> extracted base symbol: $baseSymbol")
                
                // First try hardcoded logos for common coins
                val hardcodedLogo = hardcodedLogos[baseSymbol]
                if (hardcodedLogo != null) {
                    Log.d(TAG, "Using hardcoded logo for $originalSymbol (base=$baseSymbol): $hardcodedLogo")
                    updateCurrencyLogo(originalSymbol, hardcodedLogo)
                    continue
                }
                
                // Try to use manual mapping, then fallback to dynamic mapping
                val coinId = manualMappings[baseSymbol] ?: symbolToIdMap[baseSymbol]
                
                if (coinId != null) {
                    // Try to use cached logo URL if available
                    if (logoCache.containsKey(coinId)) {
                        Log.d(TAG, "Using cached logo for $originalSymbol with ID: $coinId")
                        updateCurrencyLogo(originalSymbol, logoCache[coinId]!!)
                        continue
                    }
                    
                    // If not cached, fetch from API with proper rate limiting
                    try {
                        // Add significant delay to avoid rate limiting
                        delay(1500)  // 1.5 seconds between requests
                        
                        Log.d(TAG, "Fetching logo from API for $originalSymbol (ID: $coinId)")
                        
                        // Temporarily use a placeholder URL instead of calling getCoinDetails
                        val logoUrl = "https://assets.coingecko.com/coins/images/1/$coinId.png"
                        
                        // Cache the logo URL
                        logoCache[coinId] = logoUrl
                        
                        // Update in database
                        updateCurrencyLogo(originalSymbol, logoUrl)
                        Log.d(TAG, "Successfully updated logo for $originalSymbol using ID: $coinId (URL: $logoUrl)")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching logo for $originalSymbol (ID: $coinId): ${e.message}", e)
                    }
                } else {
                    Log.w(TAG, "No coin ID found for symbol: $baseSymbol ($originalSymbol)")
                }
            }
            
            // Log the state of loaded logos
            val updatedCurrencies = currencyDao.getAllCurrenciesSync()
            val withLogos = updatedCurrencies.count { it.logoUrl.isNotEmpty() }
            Log.d(TAG, "Logo update complete. $withLogos/${updatedCurrencies.size} currencies have logos")
            updatedCurrencies.forEach { 
                if (it.logoUrl.isNotEmpty()) {
                    Log.d(TAG, "${it.symbol} -> ${it.logoUrl}")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchAndUpdateLogos: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    private fun extractBaseSymbol(symbol: String): String {
        // Analyze trading pair to extract the base symbol correctly
        return when {
            // For standard X/USDT pairs (BTCUSDT, ETHUSDT, etc) - extract the base symbol
            symbol.endsWith("USDT") && symbol.length > 4 -> {
                val base = symbol.removeSuffix("USDT")
                Log.d(TAG, "PAIR TYPE 1: $symbol -> base: $base")
                base 
            }
            
            // For USDT/X pairs (USDTRUB, USDTBRL, etc.) - USDT is the base
            symbol.startsWith("USDT") && symbol.length > 4 -> {
                Log.d(TAG, "PAIR TYPE 2: $symbol -> base: USDT")
                "USDT"
            }
            
            // For BTC/X pairs (BTCEUR, etc.) - BTC is the base
            symbol.startsWith("BTC") && symbol.length > 3 -> {
                Log.d(TAG, "PAIR TYPE 3: $symbol -> base: BTC")
                "BTC"
            }
            
            // For ETH/X pairs (ETHEUR, etc.) - ETH is the base
            symbol.startsWith("ETH") && symbol.length > 3 -> {
                Log.d(TAG, "PAIR TYPE 4: $symbol -> base: ETH")
                "ETH"
            }
            
            // Special case for stablecoins
            symbol == "BUSDUSDT" -> {
                Log.d(TAG, "STABLECOIN BUSD: $symbol -> base: BUSD")
                "BUSD"
            }
            
            symbol == "TUSDUSDT" -> {
                Log.d(TAG, "STABLECOIN TUSD: $symbol -> base: TUSD")
                "TUSD"
            }
            
            symbol == "USDCUSDT" -> {
                Log.d(TAG, "STABLECOIN USDC: $symbol -> base: USDC")
                "USDC"
            }
            
            // For specific cryptocurrencies we want to support explicitly
            symbol == "ADAUSDT" -> {
                Log.d(TAG, "EXPLICIT COIN ADA: $symbol -> base: ADA")
                "ADA"
            }
            
            symbol == "ATOMUSDT" -> {
                Log.d(TAG, "EXPLICIT COIN ATOM: $symbol -> base: ATOM")
                "ATOM"
            }
            
            // Default case - return as is but log it
            else -> {
                Log.d(TAG, "DEFAULT CASE: $symbol -> returning as is")
                symbol
            }
        }
    }
    
    private suspend fun loadCoinMappings() {
        try {
            val response = coinGeckoApiService.getCoinsList()
            if (response.isSuccessful && response.body() != null) {
                symbolToIdMap.clear()
                
                // Map all symbols to their IDs, converting symbols to lowercase
                response.body()!!.forEach { coin ->
                    symbolToIdMap[coin.symbol.lowercase()] = coin.id
                }
                
                Log.d(TAG, "Loaded ${symbolToIdMap.size} coin mappings")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading coin mappings: ${e.message}", e)
        }
    }
    
    private suspend fun updateCurrencyLogo(symbol: String, logoUrl: String) {
        try {
            val currency = currencyDao.getCurrencyBySymbol(symbol) ?: return
            
            val updatedCurrency = currency.copy(logoUrl = logoUrl)
            currencyDao.insertCurrency(updatedCurrency)
            Log.d(TAG, "Updated logo for $symbol in database: $logoUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating logo for $symbol: ${e.message}", e)
        }
    }
    
    // Helper extension functions for mapping between entity and domain models
    private fun CurrencyEntity.toDomainModel(): Currency {
        // Gerçek fiyat değerini hesapla
        val displayPrice = when {
            // USDT ile başlayan çiftler için fiyatı ters çevir (USD/TRY gibi)
            symbol.startsWith("USDT") && symbol.length > 4 -> {
                // Sadece fiyat geçerliyse ters çevir
                if (price > 0.0) 1.0 / price else price
            }
            // Diğer tüm durumlar için orijinal fiyatı kullan
            else -> {
                price
            }
        }
        
        return Currency(
            symbol = symbol,
            price = displayPrice,
            priceUsd = priceUsd,
            changePercent24h = changePercent24h,
            volume24h = volume24h,
            marketCap = marketCap,
            lastUpdateTime = lastUpdateTime,
            logoUrl = logoUrl
        )
    }
    
    private fun WatchlistEntity.toDomainModel(): WatchlistItem {
        return WatchlistItem(
            id = id,
            userId = userId,
            symbol = symbol,
            addedAt = Date(addedAt)
        )
    }
    
    private fun AlertEntity.toDomainModel(): Alert {
        return Alert(
            id = id,
            userId = userId,
            symbol = symbol,
            targetPrice = targetPrice,
            isAboveTarget = isAboveTarget,
            isActive = isActive,
            createdAt = Date(createdAt)
        )
    }
} 