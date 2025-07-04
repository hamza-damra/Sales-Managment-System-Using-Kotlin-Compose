package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

/**
 * API service for reports and analytics
 */
class ReportsApiService(private val httpClient: HttpClient) {
    
    suspend fun getDashboardSummary(): NetworkResult<DashboardSummaryDTO> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_DASHBOARD)
            response.body<DashboardSummaryDTO>()
        }
    }
    
    suspend fun getSalesReport(
        startDate: String,
        endDate: String
    ): NetworkResult<SalesReportDTO> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_SALES) {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }
            response.body<SalesReportDTO>()
        }
    }
    
    suspend fun getRevenueTrends(
        months: Int = 6
    ): NetworkResult<RevenueTrendsDTO> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_REVENUE) {
                parameter("months", months)
            }
            response.body<RevenueTrendsDTO>()
        }
    }
    
    suspend fun getTopProducts(
        startDate: String,
        endDate: String
    ): NetworkResult<TopProductsReportDTO> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_TOP_PRODUCTS) {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }
            response.body<TopProductsReportDTO>()
        }
    }
    
    suspend fun getCustomerAnalytics(): NetworkResult<CustomerAnalyticsDTO> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_CUSTOMER_ANALYTICS)
            response.body<CustomerAnalyticsDTO>()
        }
    }
    
    suspend fun getInventoryReport(): NetworkResult<InventoryReportDTO> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_INVENTORY)
            response.body<InventoryReportDTO>()
        }
    }
}
