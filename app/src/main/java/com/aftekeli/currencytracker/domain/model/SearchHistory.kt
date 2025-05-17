package com.aftekeli.currencytracker.domain.model

import java.util.Date

data class SearchHistory(
    val id: String = "",
    val userId: String,
    val query: String,
    val category: String = "all",
    val timestamp: Date = Date()
) 