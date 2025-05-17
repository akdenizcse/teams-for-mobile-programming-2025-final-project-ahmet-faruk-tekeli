package com.aftekeli.currencytracker.data.repository

import com.aftekeli.currencytracker.data.mapper.CoinGeckoMapper
import com.aftekeli.currencytracker.data.remote.api.CoinGeckoApiService
import com.aftekeli.currencytracker.domain.model.ConversionRate
import com.aftekeli.currencytracker.domain.model.CurrencyInfo
import com.aftekeli.currencytracker.domain.repository.CurrencyConverterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyConverterRepositoryImpl @Inject constructor(
    private val coinGeckoApiService: CoinGeckoApiService
) : CurrencyConverterRepository {

    // Cache for currencies to reduce API calls
    private val cryptoCurrenciesCache = mutableListOf<CurrencyInfo>()
    private val fiatCurrenciesCache = mutableListOf<CurrencyInfo>()
    private val conversionRateCache = mutableMapOf<Pair<String, String>, ConversionRate>()
    
    // Supported CoinGecko IDs
    private val supportedIds = listOf(
        "bitcoin", "ethereum", "litecoin", "bitcoin-cash", "binancecoin", "eos", "ripple", "stellar", "chainlink", 
        "polkadot", "yearn-finance", "usd", "aed", "ars", "aud", "bdt", "bhd", "bmd", "brl", "cad", "chf", "clp", 
        "cny", "czk", "dkk", "eur", "gbp", "gel", "hkd", "huf", "idr", "ils", "inr", "jpy", "krw", "kwd", "lkr", 
        "mmk", "mxn", "myr", "ngn", "nok", "nzd", "php", "pkr", "pln", "rub", "sar", "sek", "sgd", "thb", "try", 
        "twd", "uah", "vef", "vnd", "zar", "xdr", "xag", "xau", "bits", "sats"
    )
    
    override suspend fun getCryptoCurrencies(): Flow<Result<List<CurrencyInfo>>> = flow {
        try {
            // Return cached data if available
            if (cryptoCurrenciesCache.isNotEmpty()) {
                emit(Result.success(cryptoCurrenciesCache))
                return@flow
            }
            
            val response = coinGeckoApiService.getCoinsList()
            if (response.isSuccessful && response.body() != null) {
                val allCoins = response.body()!!
                // Filter to only supported cryptocurrencies
                val supportedCoins = allCoins.filter { coin -> 
                    supportedIds.contains(coin.id) && !isFiatId(coin.id)
                }
                
                val currencies = CoinGeckoMapper.mapCryptoCurrencies(supportedCoins)
                cryptoCurrenciesCache.clear()
                cryptoCurrenciesCache.addAll(currencies)
                emit(Result.success(currencies))
            } else {
                emit(Result.failure(Exception("Failed to load cryptocurrencies: ${response.message()}")))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching crypto currencies")
            emit(Result.failure(e))
        }
    }
    
    override suspend fun getFiatCurrencies(): Flow<Result<List<CurrencyInfo>>> = flow {
        try {
            // Return cached data if available
            if (fiatCurrenciesCache.isNotEmpty()) {
                emit(Result.success(fiatCurrenciesCache))
                return@flow
            }
            
            val response = coinGeckoApiService.getSupportedVsCurrencies()
            if (response.isSuccessful && response.body() != null) {
                val allCurrencies = response.body()!!
                // Filter to only include fiat currencies
                val fiatCurrencies = allCurrencies.filter { isFiatCode(it) }
                
                val currencies = CoinGeckoMapper.mapFiatCurrencies(fiatCurrencies)
                fiatCurrenciesCache.clear()
                fiatCurrenciesCache.addAll(currencies)
                emit(Result.success(currencies))
            } else {
                emit(Result.failure(Exception("Failed to load fiat currencies: ${response.message()}")))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching fiat currencies")
            emit(Result.failure(e))
        }
    }
    
    override suspend fun getConversionRate(
        fromCurrencyId: String,
        toCurrencyId: String
    ): Flow<Result<ConversionRate>> = flow {
        try {
            val cacheKey = Pair(fromCurrencyId, toCurrencyId)
            
            // Check cache first
            conversionRateCache[cacheKey]?.let { cachedRate ->
                if (!cachedRate.isExpired()) {
                    emit(Result.success(cachedRate))
                    return@flow
                }
            }
            
            // Fetch from and to currency info if not in cache
            val fromCurrency = getOrFetchCurrencyInfo(fromCurrencyId)
            val toCurrency = getOrFetchCurrencyInfo(toCurrencyId)
            
            if (fromCurrency == null || toCurrency == null) {
                emit(Result.failure(Exception("Currency not found")))
                return@flow
            }
            
            Timber.d("Getting conversion rate from ${fromCurrency.id} to ${toCurrency.id}")
            
            // Determine if we're dealing with crypto->crypto, crypto->fiat, fiat->crypto, or fiat->fiat
            val fromIsFiat = isFiatId(fromCurrency.id)
            val toIsFiat = isFiatId(toCurrency.id)
            
            // Format ids to CoinGecko's expected format
            val cgFromId = normalizeId(fromCurrency.id)
            val cgToId = normalizeCurrencyCode(toCurrency.id)
            
            var rate = 0.0
            
            when {
                // Case 1: crypto -> fiat (direct supported through CoinGecko)
                !fromIsFiat && toIsFiat -> {
                    val response = coinGeckoApiService.getPrice(
                        coinIds = cgFromId,
                        vsCurrencies = cgToId
                    )
                    
                    if (response.isSuccessful && response.body() != null) {
                        val priceData = response.body()!!
                        if (priceData.containsKey(cgFromId)) {
                            val prices = priceData[cgFromId]
                            if (prices?.containsKey(cgToId) == true) {
                                rate = prices[cgToId] ?: 0.0
                            }
                        }
                    }
                }
                
                // Case 2: fiat -> crypto (need inverse of crypto -> fiat)
                fromIsFiat && !toIsFiat -> {
                    val response = coinGeckoApiService.getPrice(
                        coinIds = normalizeId(toCurrency.id),
                        vsCurrencies = normalizeCurrencyCode(fromCurrency.id)
                    )
                    
                    if (response.isSuccessful && response.body() != null) {
                        val priceData = response.body()!!
                        val toId = normalizeId(toCurrency.id)
                        val fromCode = normalizeCurrencyCode(fromCurrency.id)
                        
                        if (priceData.containsKey(toId)) {
                            val prices = priceData[toId]
                            if (prices?.containsKey(fromCode) == true) {
                                val inverseRate = prices[fromCode] ?: 0.0
                                if (inverseRate > 0) {
                                    rate = 1.0 / inverseRate
                                }
                            }
                        }
                    }
                }
                
                // Case 3: crypto -> crypto (get both prices in USD, then calculate ratio)
                !fromIsFiat && !toIsFiat -> {
                    val response = coinGeckoApiService.getPrice(
                        coinIds = "${cgFromId},${normalizeId(toCurrency.id)}",
                        vsCurrencies = "usd"
                    )
                    
                    if (response.isSuccessful && response.body() != null) {
                        val priceData = response.body()!!
                        val fromUsdPrice = priceData[cgFromId]?.get("usd") ?: 0.0
                        val toUsdPrice = priceData[normalizeId(toCurrency.id)]?.get("usd") ?: 0.0
                        
                        if (fromUsdPrice > 0) {
                            rate = toUsdPrice / fromUsdPrice
                        }
                    }
                }
                
                // Case 4: fiat -> fiat (convert via USD)
                fromIsFiat && toIsFiat -> {
                    // For fiat-to-fiat, we need to use a crypto (like BTC) as an intermediate
                    val response = coinGeckoApiService.getPrice(
                        coinIds = "bitcoin",
                        vsCurrencies = "${normalizeCurrencyCode(fromCurrency.id)},${cgToId}"
                    )
                    
                    if (response.isSuccessful && response.body() != null) {
                        val priceData = response.body()!!
                        if (priceData.containsKey("bitcoin")) {
                            val prices = priceData["bitcoin"]
                            val fromRate = prices?.get(normalizeCurrencyCode(fromCurrency.id)) ?: 0.0
                            val toRate = prices?.get(cgToId) ?: 0.0
                            
                            if (fromRate > 0) {
                                // Calculate cross rate
                                rate = toRate / fromRate
                            }
                        }
                    }
                }
            }
            
            if (rate <= 0) {
                emit(Result.failure(Exception("Failed to get conversion rate")))
            } else {
                val conversionRate = ConversionRate(
                    fromCurrency = fromCurrency,
                    toCurrency = toCurrency,
                    rate = rate
                )
                
                // Cache the result
                conversionRateCache[cacheKey] = conversionRate
                
                emit(Result.success(conversionRate))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error getting conversion rate")
            emit(Result.failure(e))
        }
    }
    
    override suspend fun getCurrencyInfo(currencyId: String): Flow<Result<CurrencyInfo>> = flow {
        try {
            val cachedCurrency = findCachedCurrency(currencyId)
            if (cachedCurrency != null) {
                emit(Result.success(cachedCurrency))
                return@flow
            }
            
            // Try to determine if it's a crypto or fiat
            val isFiat = isFiatId(currencyId)
            
            if (isFiat) {
                // Get fiat currency info
                val response = coinGeckoApiService.getSupportedVsCurrencies()
                if (response.isSuccessful && response.body() != null) {
                    val currencies = CoinGeckoMapper.mapFiatCurrencies(response.body()!!)
                    val currency = currencies.find { it.id == currencyId }
                    if (currency != null) {
                        emit(Result.success(currency))
                        fiatCurrenciesCache.add(currency)
                    } else {
                        emit(Result.failure(Exception("Currency not found")))
                    }
                }
            } else {
                // Get crypto currency info
                val response = coinGeckoApiService.getCoinById(normalizeId(currencyId))
                if (response.isSuccessful && response.body() != null) {
                    val coin = response.body()!!
                    val currency = CurrencyInfo(
                        id = currencyId,
                        symbol = coin.symbol.uppercase(),
                        name = coin.name,
                        isFiat = false,
                        imageUrl = coin.image?.small
                    )
                    emit(Result.success(currency))
                    cryptoCurrenciesCache.add(currency)
                } else {
                    emit(Result.failure(Exception("Currency not found")))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching currency info")
            emit(Result.failure(e))
        }
    }
    
    override suspend fun getPopularCryptoCurrencies(): Flow<Result<List<CurrencyInfo>>> = flow {
        try {
            // Use common cryptos from CoinGeckoApiService
            val cryptoIds = CoinGeckoApiService.COMMON_CRYPTOS.joinToString(",")
            val response = coinGeckoApiService.getCoinsMarkets(
                vsCurrency = "usd",
                ids = cryptoIds
            )
            
            if (response.isSuccessful && response.body() != null) {
                val currencies = CoinGeckoMapper.mapMarketDataToCurrencies(response.body()!!, "usd")
                emit(Result.success(currencies))
                
                // Update cache
                currencies.forEach { currency ->
                    val index = cryptoCurrenciesCache.indexOfFirst { it.id == currency.id }
                    if (index >= 0) {
                        cryptoCurrenciesCache[index] = currency
                    } else {
                        cryptoCurrenciesCache.add(currency)
                    }
                }
            } else {
                emit(Result.failure(Exception("Failed to load popular cryptocurrencies")))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching popular crypto currencies")
            emit(Result.failure(e))
        }
    }
    
    override suspend fun getPopularFiatCurrencies(): Flow<Result<List<CurrencyInfo>>> = flow {
        try {
            // For now we'll just filter the cached fiat currencies using COMMON_FIATS
            if (fiatCurrenciesCache.isNotEmpty()) {
                val popularFiats = fiatCurrenciesCache.filter { 
                    CoinGeckoApiService.COMMON_FIATS.contains(it.id)
                }
                emit(Result.success(popularFiats))
            } else {
                // Fetch all fiats first, then filter
                getFiatCurrencies().collect { result ->
                    if (result.isSuccess) {
                        val popularFiats = result.getOrDefault(emptyList()).filter {
                            CoinGeckoApiService.COMMON_FIATS.contains(it.id)
                        }
                        emit(Result.success(popularFiats))
                    } else {
                        emit(result)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching popular fiat currencies")
            emit(Result.failure(e))
        }
    }
    
    // Helper function to find a currency in the caches
    private fun findCachedCurrency(currencyId: String): CurrencyInfo? {
        return cryptoCurrenciesCache.find { it.id == currencyId }
            ?: fiatCurrenciesCache.find { it.id == currencyId }
    }
    
    // Helper function to get or fetch currency info
    private suspend fun getOrFetchCurrencyInfo(currencyId: String): CurrencyInfo? {
        val cachedCurrency = findCachedCurrency(currencyId)
        if (cachedCurrency != null) {
            return cachedCurrency
        }
        
        var result: CurrencyInfo? = null
        getCurrencyInfo(currencyId).collect { 
            if (it.isSuccess) {
                result = it.getOrNull()
            }
        }
        return result
    }
    
    // Helper function to check if a currency ID is a fiat currency
    private fun isFiatId(id: String): Boolean {
        val fiatIds = listOf(
            "usd", "eur", "gbp", "jpy", "try", "cad", "aud", "rub", "cny", "inr",
            "brl", "zar", "krw", "sgd", "aed", "ars", "bdt", "bhd", "bmd", 
            "chf", "clp", "czk", "dkk", "gel", "hkd", "huf", "idr", "ils", 
            "kwd", "lkr", "mmk", "mxn", "myr", "ngn", "nok", "nzd", "php", 
            "pkr", "pln", "sar", "sek", "thb", "twd", "uah", "vef", "vnd", 
            "xdr", "xag", "xau"
        )
        
        return fiatIds.contains(id.lowercase())
    }
    
    // Helper function to check if a currency code is a fiat currency
    private fun isFiatCode(code: String): Boolean {
        return code != "btc" && code != "eth" && code != "ltc" && code != "bch" && 
               code != "bnb" && code != "eos" && code != "xrp" && code != "xlm" && 
               code != "link" && code != "dot" && code != "yfi" && code != "bits" && 
               code != "sats"
    }
    
    // Helper function to normalize cryptocurrency IDs for CoinGecko API
    private fun normalizeId(id: String): String {
        // Map from simple ID to CoinGecko ID if needed
        return when(id.lowercase()) {
            "btc" -> "bitcoin"
            "eth" -> "ethereum" 
            "ltc" -> "litecoin"
            "bch" -> "bitcoin-cash"
            "bnb" -> "binancecoin"
            "xrp" -> "ripple"
            "xlm" -> "stellar"
            "link" -> "chainlink"
            "dot" -> "polkadot"
            "yfi" -> "yearn-finance"
            else -> id.lowercase()
        }
    }
    
    // Helper function to normalize currency codes for CoinGecko API
    private fun normalizeCurrencyCode(id: String): String {
        return id.lowercase()
    }
} 