package com.aftekeli.currencytracker.util

import com.aftekeli.currencytracker.R

/**
 * Utility function to get the drawable resource ID for a coin logo based on its base asset symbol.
 * Returns null if no matching logo is found, allowing for fallback to a placeholder.
 */
fun getCoinLogoResource(baseAsset: String): Int? {
    val normalizedAsset = baseAsset.lowercase().trim()
    
    return when (normalizedAsset) {
        "btc" -> R.drawable.ic_logo_btc
        "eth" -> R.drawable.ic_logo_eth
        "sol" -> R.drawable.ic_logo_sol
        "ada" -> R.drawable.ic_logo_ada
        "avax" -> R.drawable.ic_logo_avax
        "bab" -> R.drawable.ic_logo_bab
        "cetus" -> R.drawable.ic_logo_cetus
        "doge" -> R.drawable.ic_logo_doge
        "ena" -> R.drawable.ic_logo_ena
        "fdusd" -> R.drawable.ic_logo_fdusd
        "link" -> R.drawable.ic_logo_link
        "ltc" -> R.drawable.ic_logo_ltc
        "pepe" -> R.drawable.ic_logo_pepe
        "rune" -> R.drawable.ic_logo_rune
        "sui" -> R.drawable.ic_logo_sui
        "trx" -> R.drawable.ic_logo_trx
        "uni" -> R.drawable.ic_logo_uni
        "usdc" -> R.drawable.ic_logo_usdc
        "wif" -> R.drawable.ic_logo_wif
        "wld" -> R.drawable.ic_logo_wld
        "alpaca" -> R.drawable.ic_logo_alpaca
        else -> null
    }
} 