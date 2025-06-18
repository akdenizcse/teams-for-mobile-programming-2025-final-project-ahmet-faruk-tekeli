package com.aftekeli.currencytracker.util

import com.aftekeli.currencytracker.R

/**
 * Kripto para logoları için utility sınıfı.
 * Kripto para sembollerine göre doğru logo kaynaklarını döndürür.
 */
object CoinLogoUtil {

    /**
     * Verilen kripto para sembolü için logo kaynağını döndürür.
     * Eğer sembol için logo yoksa null döner.
     *
     * @param symbol Kripto para sembolü (örn. "BTC", "ETH")
     * @return Logo kaynağı ID'si veya null
     */
    fun getCoinLogoResource(symbol: String?): Int? {
        if (symbol == null) return null
        
        return when (symbol.uppercase()) {
            // Yeni eklenen logolar
            "XRP" -> R.drawable.ic_logo_xrp
            "SPK" -> R.drawable.ic_logo_spk
            "BONK" -> R.drawable.ic_logo_bonk
            "NXPC" -> R.drawable.ic_logo_nxpc
            "BNB" -> R.drawable.ic_logo_bnb
            "COOKIE" -> R.drawable.ic_logo_cookie
            "AAVE" -> R.drawable.ic_logo_aave
            "FLOKI" -> R.drawable.ic_logo_floki
            "TAO" -> R.drawable.ic_logo_tao
            
            // Mevcut logolar
            "BTC", "BITCOIN" -> R.drawable.ic_logo_btc
            "ETH", "ETHEREUM" -> R.drawable.ic_logo_eth
            "SOL", "SOLANA" -> R.drawable.ic_logo_sol
            "DOGE", "DOGECOIN" -> R.drawable.ic_logo_doge
            "ADA", "CARDANO" -> R.drawable.ic_logo_ada
            "AVAX", "AVALANCHE" -> R.drawable.ic_logo_avax
            "LINK", "CHAINLINK" -> R.drawable.ic_logo_link
            "UNI", "UNISWAP" -> R.drawable.ic_logo_uni
            
            // Diğer mevcut logolar
            "ALPACA" -> R.drawable.ic_logo_alpaca
            "BAB" -> R.drawable.ic_logo_bab
            "CETUS" -> R.drawable.ic_logo_cetus
            "ENA" -> R.drawable.ic_logo_ena
            "FDUSD" -> R.drawable.ic_logo_fdusd
            "LTC", "LITECOIN" -> R.drawable.ic_logo_ltc
            "PEPE" -> R.drawable.ic_logo_pepe
            "RUNE", "THORCHAIN" -> R.drawable.ic_logo_rune
            "SUI" -> R.drawable.ic_logo_sui
            "TRX", "TRON" -> R.drawable.ic_logo_trx
            "USDC" -> R.drawable.ic_logo_usdc
            "WIF" -> R.drawable.ic_logo_wif
            "WLD", "WORLDCOIN" -> R.drawable.ic_logo_wld
            
            // Eğer sembol için logo yoksa null döndür
            else -> null
        }
    }
    
    /**
     * Verilen kripto para sembolü için logo kaynağını döndürür.
     * Eğer sembol için logo yoksa varsayılan logo döner.
     *
     * @param symbol Kripto para sembolü (örn. "BTC", "ETH")
     * @param defaultLogo Varsayılan logo kaynağı ID'si
     * @return Logo kaynağı ID'si
     */
    fun getCoinLogoResourceOrDefault(symbol: String?, defaultLogo: Int): Int {
        return getCoinLogoResource(symbol) ?: defaultLogo
    }
} 