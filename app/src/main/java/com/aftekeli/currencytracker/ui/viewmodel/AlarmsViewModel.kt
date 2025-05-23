package com.aftekeli.currencytracker.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aftekeli.currencytracker.data.model.PriceAlert
import com.aftekeli.currencytracker.data.repository.AlertRepository
import com.aftekeli.currencytracker.util.Result
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            alertRepository.getPriceAlerts(userId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                alerts = result.data,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                    }
                    is Result.Error -> {
                        Log.e("AlarmsViewModel", "Error fetching alerts", result.exception)
                        _uiState.update { 
                            it.copy(
                                errorMessage = result.exception.message ?: "Error fetching alerts",
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun deleteAlert(alertId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = alertRepository.deletePriceAlert(userId, alertId)) {
                is Result.Success -> {
                    // Alert will be removed automatically through the Firestore listener
                    _uiState.update { it.copy(isLoading = false) }
                }
                is Result.Error -> {
                    Log.e("AlarmsViewModel", "Error deleting alert", result.exception)
                    _uiState.update { 
                        it.copy(
                            errorMessage = "Could not delete alert: ${result.exception.message}",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class AlarmsUiState(
    val alerts: List<PriceAlert> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) 