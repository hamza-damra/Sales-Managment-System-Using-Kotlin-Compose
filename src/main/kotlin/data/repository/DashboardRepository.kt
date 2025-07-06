package data.repository

import data.api.*
import data.api.services.DashboardApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for dashboard data management
 */
class DashboardRepository(
    private val dashboardApiService: DashboardApiService
) {
    
    /**
     * Get dashboard summary data
     */
    fun getDashboardSummary(): Flow<NetworkResult<DashboardSummaryDTO>> = flow {
        emit(dashboardApiService.getDashboardSummary())
    }
    
    /**
     * Get sales report data
     */
    fun getSalesReport(startDate: String, endDate: String): Flow<NetworkResult<Map<String, Any>>> = flow {
        emit(dashboardApiService.getSalesReport(startDate, endDate))
    }
    
    /**
     * Get revenue report data
     */
    fun getRevenueReport(months: Int = 6): Flow<NetworkResult<Map<String, Any>>> = flow {
        emit(dashboardApiService.getRevenueReport(months))
    }
    
    /**
     * Get top products report
     */
    fun getTopProductsReport(startDate: String, endDate: String): Flow<NetworkResult<Map<String, Any>>> = flow {
        emit(dashboardApiService.getTopProductsReport(startDate, endDate))
    }
    
    /**
     * Get customer analytics
     */
    fun getCustomerAnalytics(): Flow<NetworkResult<Map<String, Any>>> = flow {
        emit(dashboardApiService.getCustomerAnalytics())
    }
    
    /**
     * Get inventory report
     */
    fun getInventoryReport(): Flow<NetworkResult<Map<String, Any>>> = flow {
        emit(dashboardApiService.getInventoryReport())
    }
}
