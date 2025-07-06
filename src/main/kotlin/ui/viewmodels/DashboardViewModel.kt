package ui.viewmodels

import data.api.DashboardSummaryDTO
import data.api.NetworkResult
import data.repository.DashboardRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for Dashboard screen
 */
class DashboardViewModel(
    private val dashboardRepository: DashboardRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // UI State
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        // Don't load data automatically - wait for explicit call
        // This prevents API calls when user is not authenticated
    }
    
    /**
     * Load all dashboard data
     */
    fun loadDashboardData() {
        println("📊 DashboardViewModel - Starting to load dashboard data...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            println("📡 DashboardViewModel - Making API call to dashboard repository...")
            dashboardRepository.getDashboardSummary()
                .catch { exception ->
                    println("❌ DashboardViewModel - Exception caught: ${exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
                .collect { result ->
                    println("📥 DashboardViewModel - Received result: ${result::class.simpleName}")
                    when (result) {
                        is NetworkResult.Success -> {
                            println("✅ DashboardViewModel - Dashboard data loaded successfully")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                dashboardSummary = result.data,
                                error = null,
                                lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            )
                        }
                        is NetworkResult.Error -> {
                            println("❌ DashboardViewModel - Error loading dashboard: ${result.exception.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Failed to load dashboard data"
                            )
                        }
                        is NetworkResult.Loading -> {
                            println("⏳ DashboardViewModel - Loading state")
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                    }
                }
        }
    }
    
    /**
     * Refresh dashboard data
     */
    fun refreshData() {
        loadDashboardData()
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clean up resources
     */
    fun onCleared() {
        // Cancel any ongoing coroutines
        viewModelScope.cancel()
    }
}

/**
 * UI State for Dashboard screen
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val dashboardSummary: DashboardSummaryDTO? = null,
    val error: String? = null,
    val lastUpdated: kotlinx.datetime.LocalDateTime? = null
) {
    val hasData: Boolean get() = dashboardSummary != null
    val hasError: Boolean get() = error != null
}
