package com.aftekeli.currencytracker.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for Binance ticker/price API response
 */
data class PriceDto(
    val symbol: String,
    val price: String
)

/**
 * Data Transfer Object for Binance kline/candlestick API response
 * Each item in the array represents:
 * [
 *   1499040000000,      // Open time
 *   "8591.26",          // Open
 *   "8607.69",          // High
 *   "8580.56",          // Low
 *   "8604.77",          // Close
 *   "1234.56",          // Volume
 *   1499644799999,      // Close time
 *   "123456.7",         // Quote asset volume
 *   308,                // Number of trades
 *   "12.3",             // Taker buy base asset volume
 *   "1234.5",           // Taker buy quote asset volume
 *   "0"                 // Ignore
 * ]
 */
data class CandlestickDto(
    val openTime: Long,
    val open: String,
    val high: String,
    val low: String,
    val close: String,
    val volume: String,
    val closeTime: Long,
    val quoteAssetVolume: String,
    val numberOfTrades: Int,
    val takerBuyBaseAssetVolume: String,
    val takerBuyQuoteAssetVolume: String,
    val ignore: String
) 