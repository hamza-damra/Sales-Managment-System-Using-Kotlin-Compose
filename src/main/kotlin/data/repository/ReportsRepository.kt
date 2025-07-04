package data.repository

import data.api.*
import data.api.services.ReportsApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for reports and analytics data
 */
class ReportsRepository(private val reportsApiService: ReportsApiService) {
    
    private val _dashboardSummary = MutableStateFlow<DashboardSummaryDTO?>(null)
    val dashboardSummary: StateFlow<DashboardSummaryDTO?> = _dashboardSummary.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    suspend fun loadDashboardSummary(): NetworkResult<DashboardSummaryDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = reportsApiService.getDashboardSummary()
        
        result.onSuccess { summary ->
            _dashboardSummary.value = summary
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getSalesReport(
        startDate: String,
        endDate: String
    ): NetworkResult<SalesReportDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = reportsApiService.getSalesReport(startDate, endDate)
        
        result.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getRevenueTrends(months: Int = 6): NetworkResult<RevenueTrendsDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = reportsApiService.getRevenueTrends(months)
        
        result.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getTopProducts(
        startDate: String,
        endDate: String
    ): NetworkResult<TopProductsReportDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = reportsApiService.getTopProducts(startDate, endDate)
        
        result.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getCustomerAnalytics(): NetworkResult<CustomerAnalyticsDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = reportsApiService.getCustomerAnalytics()
        
        result.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getInventoryReport(): NetworkResult<InventoryReportDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = reportsApiService.getInventoryReport()
        
        result.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    fun clearError() {
        _error.value = null
    }
}
