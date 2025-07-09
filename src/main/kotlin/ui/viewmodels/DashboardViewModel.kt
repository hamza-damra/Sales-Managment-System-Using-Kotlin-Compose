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

                            // Log the received data for debugging
                            val data = result.data
                            println("📊 Received data - Period: ${data?.period}")
                            println("📊 Received data - Sales: ${data?.sales?.totalSales}")
                            println("📊 Received data - Revenue: ${data?.sales?.totalRevenue}")
                            println("📊 Received data - Customers: ${data?.customers?.totalCustomers}")

                            // Improved mock data detection
                            val isUsingMockData = data?.period?.contains("بيانات تجريبية") == true ||
                                                 (data?.period?.contains("آخر 30 يوم") == true &&
                                                  data.sales?.totalSales == 156) // Mock data indicator

                            // Additional validation - check if we have real data
                            val hasRealData = data?.sales?.totalRevenue != null &&
                                            data.sales.totalRevenue > 0 &&
                                            data.customers?.totalCustomers != null &&
                                            data.customers.totalCustomers > 0

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                dashboardSummary = data,
                                error = null,
                                lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                                isUsingMockData = isUsingMockData
                            )

                            if (isUsingMockData) {
                                println("ℹ️ DashboardViewModel - Using mock data (API unavailable)")
                            } else if (hasRealData) {
                                println("✅ DashboardViewModel - Using real data from API")
                                println("📊 Real data values - Revenue: ${data.sales?.totalRevenue}, Sales: ${data.sales?.totalSales}, Customers: ${data.customers?.totalCustomers}")
                            } else {
                                println("⚠️ DashboardViewModel - Data received but appears to be empty/null")
                                println("📊 Data check - Revenue: ${data?.sales?.totalRevenue}, Sales: ${data?.sales?.totalSales}, Customers: ${data?.customers?.totalCustomers}")
                            }

                            // Log final UI state
                            println("📊 Final UI State - hasData: ${_uiState.value.hasData}")
                            println("📊 Final UI State - isLoading: ${_uiState.value.isLoading}")
                            println("📊 Final UI State - error: ${_uiState.value.error}")
                        }
                        is NetworkResult.Error -> {
                            println("❌ DashboardViewModel - Error loading dashboard: ${result.exception.message}")

                            // Check if it's an authentication error
                            val isAuthError = result.exception.message?.contains("Authentication", ignoreCase = true) == true ||
                                             result.exception.message?.contains("401", ignoreCase = true) == true ||
                                             result.exception.message?.contains("Unauthorized", ignoreCase = true) == true ||
                                             result.exception is data.api.ApiException.AuthenticationError

                            val errorMessage = if (isAuthError) {
                                "يرجى تسجيل الدخول لعرض بيانات لوحة التحكم"
                            } else {
                                result.exception.message ?: "فشل في تحميل بيانات لوحة التحكم"
                            }

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = errorMessage
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
    val lastUpdated: kotlinx.datetime.LocalDateTime? = null,
    val isUsingMockData: Boolean = false
) {
    val hasData: Boolean get() = dashboardSummary != null
    val hasError: Boolean get() = error != null
}
