package com.aftekeli.currencytracker.data.local.db.mapper

import com.aftekeli.currencytracker.data.local.db.entity.CoinTickerEntity
import com.aftekeli.currencytracker.data.local.db.entity.FavoriteCoinEntity
import com.aftekeli.currencytracker.data.model.Coin
import com.aftekeli.currencytracker.data.remote.dto.TickerDto

/**
 * Extension function to convert TickerDto (API response) to CoinTickerEntity (Room entity)
 */
fun TickerDto.toEntity(): CoinTickerEntity {
    // Extract base and quote assets from symbol (e.g., "BTCUSDT" -> "BTC" and "USDT")
    // Handle special cases like "/BTCUSDT" with proper cleaning
    val cleanedSymbol = this.symbol.replace(Regex("^[/\\\\]"), "") // Remove leading / or \ if present
    
    // Common quote assets to identify the base asset correctly
    val quoteAssets = listOf("USDT", "BTC", "ETH", "BNB")
    
    // Find which quote asset is used in this symbol
    val quoteAsset = quoteAssets.find { cleanedSymbol.endsWith(it) } ?: "USDT"
    val baseAsset = cleanedSymbol.removeSuffix(quoteAsset)
    
    return CoinTickerEntity(
        symbol = this.symbol,
        baseAsset = baseAsset,
        quoteAsset = quoteAsset,
        lastPrice = this.lastPrice,
        priceChangePercent = this.priceChangePercent,
        volume = this.volume,
        quoteVolume = this.quoteVolume,
        high24h = this.highPrice,
        low24h = this.lowPrice,
        openPrice = this.openPrice,
        weightedAvgPrice = this.weightedAvgPrice,
        priceChange = this.priceChange,
        count = this.count,
        bidPrice = this.bidPrice,
        askPrice = this.askPrice,
        timestamp = System.currentTimeMillis()
    )
}

/**
 * Extension function to convert CoinTickerEntity (Room entity) to Coin (Domain model)
 */
fun CoinTickerEntity.toDomain(): Coin {
    return Coin(
        symbol = this.symbol,
        baseAsset = this.baseAsset,
        quoteAsset = this.quoteAsset,
        lastPrice = this.lastPrice.toDoubleOrNull() ?: 0.0,
        priceChangePercent = this.priceChangePercent.toDoubleOrNull() ?: 0.0,
        volume = this.volume.toDoubleOrNull() ?: 0.0,
        quoteVolume = this.quoteVolume.toDoubleOrNull() ?: 0.0,
        high24h = this.high24h.toDoubleOrNull() ?: 0.0,
        low24h = this.low24h.toDoubleOrNull() ?: 0.0,
        openPrice = this.openPrice.toDoubleOrNull() ?: 0.0,
        weightedAvgPrice = this.weightedAvgPrice.toDoubleOrNull() ?: 0.0,
        priceChange = this.priceChange.toDoubleOrNull() ?: 0.0,
        count = this.count,
        bidPrice = this.bidPrice.toDoubleOrNull() ?: 0.0,
        askPrice = this.askPrice.toDoubleOrNull() ?: 0.0
    )
}

/**
 * Extension function to convert a list of CoinTickerEntity to a list of Coin
 */
fun List<CoinTickerEntity>.toDomainList(): List<Coin> {
    return this.map { it.toDomain() }
}

/**
 * Extension function to convert a list of TickerDto to a list of CoinTickerEntity
 */
fun List<TickerDto>.toEntityList(): List<CoinTickerEntity> {
    return this.map { it.toEntity() }
} 