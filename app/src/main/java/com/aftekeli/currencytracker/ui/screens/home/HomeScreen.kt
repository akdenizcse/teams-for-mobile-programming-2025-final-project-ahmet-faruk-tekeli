package com.aftekeli.currencytracker.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.aftekeli.currencytracker.ui.screens.market.CurrencyListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val sortOption by viewModel.sortOption.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val showFavorites by viewModel.showFavorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showFilterPanel by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(searchQuery) }
    
    LaunchedEffect(searchText) {
        viewModel.setSearchQuery(searchText)
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Currency Tracker") },
                navigationIcon = {
                    // Favoriler Butonu - sol tarafa taşındı
                    IconButton(
                        onClick = { viewModel.setShowFavorites(!showFavorites) }
                    ) {
                        Icon(
                            imageVector = if (showFavorites) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                            contentDescription = "Toggle Favorites",
                            tint = if (showFavorites) Color.Red else LocalContentColor.current,
                            modifier = Modifier.size(if (showFavorites) 28.dp else 24.dp)
                        )
                    }
                },
                actions = {                    
                    // Filtreleme Butonu
                    IconButton(onClick = { showFilterPanel = !showFilterPanel }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    
                    // Yenileme Butonu
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Arama çubuğu
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Para birimi ara...") },
                leadingIcon = { 
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(32.dp)
            )
            
            // Filtreleme Paneli
            AnimatedVisibility(visible = showFilterPanel) {
                FilterPanel(
                    sortOption = sortOption,
                    onSortOptionChanged = { viewModel.setSortOption(it) },
                    filterType = filterType,
                    onFilterTypeChanged = { viewModel.setFilterType(it) }
                )
            }
            
            // Para birimleri listesi
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CurrencyListScreen(navController, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPanel(
    sortOption: SortOption,
    onSortOptionChanged: (SortOption) -> Unit,
    filterType: FilterType,
    onFilterTypeChanged: (FilterType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filtreler ve Sıralama",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Para Birimi Tipi Filtresi
            Text(
                text = "Para Birimi Tipi",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == FilterType.ALL,
                    onClick = { onFilterTypeChanged(FilterType.ALL) },
                    label = { Text("Tümü") }
                )
                
                FilterChip(
                    selected = filterType == FilterType.CRYPTO_ONLY,
                    onClick = { onFilterTypeChanged(FilterType.CRYPTO_ONLY) },
                    label = { Text("Kripto") }
                )
                
                FilterChip(
                    selected = filterType == FilterType.FIAT_ONLY,
                    onClick = { onFilterTypeChanged(FilterType.FIAT_ONLY) },
                    label = { Text("Fiat") }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Sıralama Seçenekleri
            Text(
                text = "Sıralama",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FilterChip(
                        selected = sortOption == SortOption.NAME_ASC,
                        onClick = { onSortOptionChanged(SortOption.NAME_ASC) },
                        label = { Text("İsim (A-Z)") }
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    FilterChip(
                        selected = sortOption == SortOption.PRICE_ASC,
                        onClick = { onSortOptionChanged(SortOption.PRICE_ASC) },
                        label = { Text("Fiyat (Düşük-Yüksek)") }
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    FilterChip(
                        selected = sortOption == SortOption.CHANGE_ASC,
                        onClick = { onSortOptionChanged(SortOption.CHANGE_ASC) },
                        label = { Text("Değişim (Düşük-Yüksek)") }
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    FilterChip(
                        selected = sortOption == SortOption.NAME_DESC,
                        onClick = { onSortOptionChanged(SortOption.NAME_DESC) },
                        label = { Text("İsim (Z-A)") }
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    FilterChip(
                        selected = sortOption == SortOption.PRICE_DESC,
                        onClick = { onSortOptionChanged(SortOption.PRICE_DESC) },
                        label = { Text("Fiyat (Yüksek-Düşük)") }
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    FilterChip(
                        selected = sortOption == SortOption.CHANGE_DESC,
                        onClick = { onSortOptionChanged(SortOption.CHANGE_DESC) },
                        label = { Text("Değişim (Yüksek-Düşük)") }
                    )
                }
            }
        }
    }
}
