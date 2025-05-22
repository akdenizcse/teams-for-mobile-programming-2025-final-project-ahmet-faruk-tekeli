package com.aftekeli.currencytracker.ui.screen.markets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.aftekeli.currencytracker.data.model.Coin
import com.aftekeli.currencytracker.ui.components.CoinListItem
import com.aftekeli.currencytracker.ui.navigation.ScreenRoutes
import com.aftekeli.currencytracker.ui.viewmodel.MarketsViewModel
import com.aftekeli.currencytracker.ui.viewmodel.SortDirection
import com.aftekeli.currencytracker.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    viewModel: MarketsViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Pull to refresh state
    val pullRefreshState = rememberPullToRefreshState()
    
    // Handle refresh state
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshMarketTickers()
        }
    }
    
    // Update pull refresh state based on UI state
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing && pullRefreshState.isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar with Clear Button
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search coins...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearchQuery() }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Divider between search and sorting
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        
        // Sorting Options Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            SortingOptions(
                currentSortOption = uiState.sortOption,
                currentSortDirection = uiState.sortDirection,
                onSortOptionChanged = { viewModel.setSortOption(it) },
                onSortDirectionChanged = { viewModel.setSortDirection(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            )
        }
        
        // Results count
        if (!uiState.isLoading && uiState.errorMessage == null) {
            Text(
                text = "${uiState.tickers.size} ${if (uiState.tickers.size == 1) "coin" else "coins"} ${if (uiState.searchQuery.isNotBlank()) "matched" else "available"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.End
            )
        }
        
        // Content with pull-to-refresh
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.errorMessage != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Error loading data",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                if (uiState.tickers.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isBlank()) 
                                "No coins available" 
                            else 
                                "No results found for \"${uiState.searchQuery}\"",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        
                        if (uiState.searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try a different search term",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(uiState.tickers) { coin ->
                            CoinListItem(
                                coin = coin,
                                onItemClick = { selectedCoin ->
                                    navController?.navigate(
                                        "${ScreenRoutes.CoinDetailScreen.route}/${selectedCoin.symbol}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // Pull to refresh indicator - only show when actively refreshing
            if (pullRefreshState.isRefreshing) {
                PullToRefreshContainer(
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortingOptions(
    currentSortOption: SortOption,
    currentSortDirection: SortDirection,
    onSortOptionChanged: (SortOption) -> Unit,
    onSortDirectionChanged: (SortDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Sort option header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Sort Options",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Sort options row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sort Option Row with Chips
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SortOption.values().forEach { option ->
                    FilterChip(
                        selected = currentSortOption == option,
                        onClick = { onSortOptionChanged(option) },
                        label = { Text(option.displayName) },
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
            
            // Direction toggle with descriptive text
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentSortDirection == SortDirection.ASCENDING) {
                        when (currentSortOption) {
                            SortOption.NAME -> "A-Z"
                            SortOption.PRICE_CHANGE -> "Low to High"
                            SortOption.VOLUME -> "Low to High"
                        }
                    } else {
                        when (currentSortOption) {
                            SortOption.NAME -> "Z-A"
                            SortOption.PRICE_CHANGE -> "High to Low"
                            SortOption.VOLUME -> "High to Low"
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                IconButton(
                    onClick = {
                        val newDirection = if (currentSortDirection == SortDirection.ASCENDING) {
                            SortDirection.DESCENDING
                        } else {
                            SortDirection.ASCENDING
                        }
                        onSortDirectionChanged(newDirection)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (currentSortDirection == SortDirection.ASCENDING) {
                            Icons.Default.ArrowUpward
                        } else {
                            Icons.Default.ArrowDownward
                        },
                        contentDescription = "Toggle sort direction",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
} 