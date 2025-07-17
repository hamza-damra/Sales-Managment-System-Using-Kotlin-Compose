package data.repository

import data.api.*
import data.api.services.ReportsApiService
import data.api.services.ProductApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonElement

/**
 * Enhanced repository for comprehensive enterprise reporting
 */
class ReportsRepository(
    private val reportsApiService: ReportsApiService,
    private val productApiService: ProductApiService
) {

    // State management
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Report data states
    private val _comprehensiveSalesReport = MutableStateFlow<ComprehensiveSalesReportDTO?>(null)
    val comprehensiveSalesReport: StateFlow<ComprehensiveSalesReportDTO?> = _comprehensiveSalesReport.asStateFlow()

    private val _customerReport = MutableStateFlow<CustomerReportDTO?>(null)
    val customerReport: StateFlow<CustomerReportDTO?> = _customerReport.asStateFlow()

    private val _productReport = MutableStateFlow<ProductReportDTO?>(null)
    val productReport: StateFlow<ProductReportDTO?> = _productReport.asStateFlow()

    private val _inventoryReport = MutableStateFlow<EnhancedInventoryReportDTO?>(null)
    val inventoryReport: StateFlow<EnhancedInventoryReportDTO?> = _inventoryReport.asStateFlow()

    private val _financialReport = MutableStateFlow<FinancialReportDTO?>(null)
    val financialReport: StateFlow<FinancialReportDTO?> = _financialReport.asStateFlow()

    private val _promotionReport = MutableStateFlow<PromotionReportDTO?>(null)
    val promotionReport: StateFlow<PromotionReportDTO?> = _promotionReport.asStateFlow()

    private val _realTimeKPIs = MutableStateFlow<JsonElement?>(null)
    val realTimeKPIs: StateFlow<JsonElement?> = _realTimeKPIs.asStateFlow()

    // Legacy state
    private val _dashboardSummary = MutableStateFlow<DashboardSummaryDTO?>(null)
    val dashboardSummary: StateFlow<DashboardSummaryDTO?> = _dashboardSummary.asStateFlow()

    // Recent Products state
    private val _recentProducts = MutableStateFlow<List<RecentProductDTO>>(emptyList())
    val recentProducts: StateFlow<List<RecentProductDTO>> = _recentProducts.asStateFlow()

    // Sales Reports
    suspend fun loadComprehensiveSalesReport(request: ReportRequestDTO): NetworkResult<ComprehensiveSalesReportDTO> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getComprehensiveSalesReport(request)

        when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    _comprehensiveSalesReport.value = result.data.data
                    _isLoading.value = false
                    return NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    _isLoading.value = false
                    return NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                _isLoading.value = false
                return result
            }
            is NetworkResult.Loading -> {
                return result
            }
        }
    }

    suspend fun getSalesSummary(
        startDate: String,
        endDate: String,
        useCache: Boolean = true
    ): NetworkResult<SalesSummary> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getSalesSummaryReport(startDate, endDate, useCache)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getSalesTrends(
        months: Int = 12,
        groupBy: String = "MONTH"
    ): NetworkResult<List<SalesTrend>> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getSalesTrends(months, groupBy)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    // Customer Reports
    suspend fun loadCustomerAnalytics(
        includeInactive: Boolean = false,
        months: Int = 12
    ): NetworkResult<CustomerReportDTO> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getCustomerAnalytics(includeInactive, months)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    _customerReport.value = result.data.data
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getCustomerLifetimeValue(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "totalValue"
    ): NetworkResult<List<CustomerLifetimeValue>> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getCustomerLifetimeValue(page, size, sortBy)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getCustomerRetention(months: Int = 12): NetworkResult<CustomerRetentionMetrics> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getCustomerRetention(months)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    // Product Reports
    suspend fun loadProductPerformance(request: ReportRequestDTO): NetworkResult<ProductReportDTO> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getProductPerformance(request)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    _productReport.value = result.data.data
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getInventoryTurnover(
        months: Int = 12,
        categoryIds: List<Long>? = null
    ): NetworkResult<List<ProductTurnover>> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getInventoryTurnover(months, categoryIds)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    // Inventory Reports
    suspend fun loadInventoryStatus(
        includeInactive: Boolean = false,
        warehouseIds: List<Long>? = null
    ): NetworkResult<EnhancedInventoryReportDTO> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getInventoryStatus(includeInactive, warehouseIds)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    _inventoryReport.value = result.data.data
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getInventoryValuation(
        valuationMethod: String = "FIFO",
        categoryIds: List<Long>? = null
    ): NetworkResult<InventoryValuation> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getInventoryValuation(valuationMethod, categoryIds)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    // Financial Reports
    suspend fun loadFinancialRevenue(
        startDate: String,
        endDate: String
    ): NetworkResult<FinancialReportDTO> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getFinancialRevenue(startDate, endDate)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    _financialReport.value = result.data.data
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    // Promotion Reports
    suspend fun loadPromotionEffectiveness(
        startDate: String,
        endDate: String
    ): NetworkResult<PromotionReportDTO> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getPromotionEffectiveness(startDate, endDate)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    _promotionReport.value = result.data.data
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getPromotionUsage(
        promotionIds: List<Long>? = null,
        days: Int = 30
    ): NetworkResult<List<PromotionUsage>> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getPromotionUsage(promotionIds, days)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    // Dashboard & KPI Reports
    suspend fun getExecutiveDashboard(days: Int = 30): NetworkResult<JsonElement> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getExecutiveDashboard(days)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getOperationalDashboard(): NetworkResult<JsonElement> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getOperationalDashboard()

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    suspend fun loadRealTimeKPIs(): NetworkResult<JsonElement> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.getRealTimeKPIs()

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                if (result.data.success && result.data.data != null) {
                    _realTimeKPIs.value = result.data.data
                    NetworkResult.Success(result.data.data)
                } else {
                    _error.value = result.data.message
                    NetworkResult.Error(ApiException.ServerError(result.data.message))
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
                result
            }
            is NetworkResult.Loading -> result
        }
    }

    // Export functionality
    suspend fun exportReport(request: ReportRequestDTO): NetworkResult<ByteArray> {
        _isLoading.value = true
        _error.value = null

        val result = reportsApiService.exportReport(request)

        result.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    // Legacy methods for backward compatibility
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

        val result = reportsApiService.getCustomerAnalyticsLegacy()

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

    fun clearAllReports() {
        _comprehensiveSalesReport.value = null
        _customerReport.value = null
        _productReport.value = null
        _inventoryReport.value = null
        _financialReport.value = null
        _promotionReport.value = null
        _realTimeKPIs.value = null
        _dashboardSummary.value = null
        _recentProducts.value = emptyList()
    }

    // Recent Products methods
    suspend fun loadRecentProducts(
        days: Int = 30,
        category: String? = null,
        categoryId: Long? = null,
        includeInventory: Boolean = false,
        page: Int = 0,
        size: Int = 50,
        sortBy: String = "lastSoldDate",
        sortDir: String = "desc"
    ): NetworkResult<List<RecentProductDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = productApiService.getRecentProducts(
            days = days,
            category = category,
            categoryId = categoryId,
            includeInventory = includeInventory,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir
        )

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                _recentProducts.value = result.data.content
                println("✅ ReportsRepository - Loaded ${result.data.content.size} recent products")
                NetworkResult.Success(result.data.content)
            }
            is NetworkResult.Error -> {
                println("❌ ReportsRepository - Error loading recent products: ${result.exception.message}")
                _error.value = result.exception.message
                NetworkResult.Error(result.exception)
            }
            is NetworkResult.Loading -> {
                // Keep loading state
                NetworkResult.Loading
            }
        }
    }

    suspend fun loadRecentProductsBasic(days: Int = 30): NetworkResult<List<RecentProductDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = productApiService.getRecentProductsBasic(days)

        _isLoading.value = false

        return when (result) {
            is NetworkResult.Success -> {
                // Extract products from the new response structure
                val products = result.data.products.content
                _recentProducts.value = products
                println("✅ ReportsRepository - Loaded ${products.size} recent products (basic)")
                NetworkResult.Success(products)
            }
            is NetworkResult.Error -> {
                println("❌ ReportsRepository - Error loading recent products (basic): ${result.exception.message}")
                _error.value = result.exception.message
                NetworkResult.Error(result.exception)
            }
            is NetworkResult.Loading -> {
                // Keep loading state
                NetworkResult.Loading
            }
        }
    }
}
