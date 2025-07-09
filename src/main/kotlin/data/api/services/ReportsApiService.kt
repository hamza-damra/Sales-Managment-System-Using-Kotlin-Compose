package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Enhanced API service for comprehensive enterprise reporting
 */
class ReportsApiService(private val httpClient: HttpClient) {

    // Sales Reports
    suspend fun getComprehensiveSalesReport(
        request: ReportRequestDTO
    ): NetworkResult<StandardReportResponse<ComprehensiveSalesReportDTO>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_SALES_COMPREHENSIVE) {
                parameter("startDate", request.startDate)
                parameter("endDate", request.endDate)
                parameter("page", request.page)
                parameter("size", request.size)
                parameter("sortBy", request.sortBy)
                parameter("sortDirection", request.sortDirection)
                parameter("useCache", request.useCache)
                request.customerIds?.let { parameter("customerIds", it.joinToString(",")) }
                request.productIds?.let { parameter("productIds", it.joinToString(",")) }
                request.regions?.let { parameter("regions", it.joinToString(",")) }
                request.paymentMethods?.let { parameter("paymentMethods", it.joinToString(",")) }
            }
            response.body<StandardReportResponse<ComprehensiveSalesReportDTO>>()
        }
    }

    suspend fun getSalesSummaryReport(
        startDate: String,
        endDate: String,
        useCache: Boolean = true
    ): NetworkResult<StandardReportResponse<SalesSummary>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_SALES_SUMMARY) {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
                parameter("useCache", useCache)
            }
            response.body<StandardReportResponse<SalesSummary>>()
        }
    }

    suspend fun getSalesTrends(
        months: Int = 12,
        groupBy: String = "MONTH"
    ): NetworkResult<StandardReportResponse<List<SalesTrend>>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_SALES_TRENDS) {
                parameter("months", months)
                parameter("groupBy", groupBy)
            }
            response.body<StandardReportResponse<List<SalesTrend>>>()
        }
    }

    // Customer Reports
    suspend fun getCustomerAnalytics(
        includeInactive: Boolean = false,
        months: Int = 12
    ): NetworkResult<StandardReportResponse<CustomerReportDTO>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_CUSTOMERS_ANALYTICS) {
                parameter("includeInactive", includeInactive)
                parameter("months", months)
            }
            response.body<StandardReportResponse<CustomerReportDTO>>()
        }
    }

    suspend fun getCustomerLifetimeValue(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "totalValue"
    ): NetworkResult<StandardReportResponse<List<CustomerLifetimeValue>>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_CUSTOMERS_LIFETIME_VALUE) {
                parameter("page", page)
                parameter("size", size)
                parameter("sortBy", sortBy)
            }
            response.body<StandardReportResponse<List<CustomerLifetimeValue>>>()
        }
    }

    suspend fun getCustomerRetention(
        months: Int = 12
    ): NetworkResult<StandardReportResponse<CustomerRetentionMetrics>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_CUSTOMERS_RETENTION) {
                parameter("months", months)
            }
            response.body<StandardReportResponse<CustomerRetentionMetrics>>()
        }
    }

    // Product Reports
    suspend fun getProductPerformance(
        request: ReportRequestDTO
    ): NetworkResult<StandardReportResponse<ProductReportDTO>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_PRODUCTS_PERFORMANCE) {
                parameter("startDate", request.startDate)
                parameter("endDate", request.endDate)
                request.categoryIds?.let { parameter("categoryIds", it.joinToString(",")) }
                request.productIds?.let { parameter("productIds", it.joinToString(",")) }
            }
            response.body<StandardReportResponse<ProductReportDTO>>()
        }
    }

    suspend fun getInventoryTurnover(
        months: Int = 12,
        categoryIds: List<Long>? = null
    ): NetworkResult<StandardReportResponse<List<ProductTurnover>>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_PRODUCTS_INVENTORY_TURNOVER) {
                parameter("months", months)
                categoryIds?.let { parameter("categoryIds", it.joinToString(",")) }
            }
            response.body<StandardReportResponse<List<ProductTurnover>>>()
        }
    }

    // Inventory Reports
    suspend fun getInventoryStatus(
        includeInactive: Boolean = false,
        warehouseIds: List<Long>? = null
    ): NetworkResult<StandardReportResponse<EnhancedInventoryReportDTO>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_INVENTORY_STATUS) {
                parameter("includeInactive", includeInactive)
                warehouseIds?.let { parameter("warehouseIds", it.joinToString(",")) }
            }
            response.body<StandardReportResponse<EnhancedInventoryReportDTO>>()
        }
    }

    suspend fun getInventoryValuation(
        valuationMethod: String = "FIFO",
        categoryIds: List<Long>? = null
    ): NetworkResult<StandardReportResponse<InventoryValuation>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_INVENTORY_VALUATION) {
                parameter("valuationMethod", valuationMethod)
                categoryIds?.let { parameter("categoryIds", it.joinToString(",")) }
            }
            response.body<StandardReportResponse<InventoryValuation>>()
        }
    }

    // Promotion Reports
    suspend fun getPromotionEffectiveness(
        startDate: String,
        endDate: String
    ): NetworkResult<StandardReportResponse<PromotionReportDTO>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_PROMOTIONS_EFFECTIVENESS) {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }
            response.body<StandardReportResponse<PromotionReportDTO>>()
        }
    }

    suspend fun getPromotionUsage(
        promotionIds: List<Long>? = null,
        days: Int = 30
    ): NetworkResult<StandardReportResponse<List<PromotionUsage>>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_PROMOTIONS_USAGE) {
                promotionIds?.let { parameter("promotionIds", it.joinToString(",")) }
                parameter("days", days)
            }
            response.body<StandardReportResponse<List<PromotionUsage>>>()
        }
    }

    // Financial Reports
    suspend fun getFinancialRevenue(
        startDate: String,
        endDate: String
    ): NetworkResult<StandardReportResponse<FinancialReportDTO>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_FINANCIAL_REVENUE) {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }
            response.body<StandardReportResponse<FinancialReportDTO>>()
        }
    }

    // Dashboard & KPI Reports
    suspend fun getExecutiveDashboard(
        days: Int = 30
    ): NetworkResult<StandardReportResponse<Map<String, Any>>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_DASHBOARD_EXECUTIVE) {
                parameter("days", days)
            }
            response.body<StandardReportResponse<Map<String, Any>>>()
        }
    }

    suspend fun getOperationalDashboard(): NetworkResult<StandardReportResponse<Map<String, Any>>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_DASHBOARD_OPERATIONAL)
            response.body<StandardReportResponse<Map<String, Any>>>()
        }
    }

    suspend fun getRealTimeKPIs(): NetworkResult<StandardReportResponse<Map<String, Any>>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.REPORTS_KPI_REAL_TIME)
            response.body<StandardReportResponse<Map<String, Any>>>()
        }
    }

    // Export Functionality
    suspend fun exportReport(
        request: ReportRequestDTO
    ): NetworkResult<ByteArray> {
        return safeApiCall {
            val response = httpClient.post(ApiConfig.Endpoints.REPORTS_EXPORT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<ByteArray>()
        }
    }

    // Legacy methods for backward compatibility
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

    suspend fun getCustomerAnalyticsLegacy(): NetworkResult<CustomerAnalyticsDTO> {
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
