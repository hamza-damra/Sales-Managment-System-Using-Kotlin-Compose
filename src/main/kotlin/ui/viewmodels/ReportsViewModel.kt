package ui.viewmodels

import data.api.*
import data.repository.ReportsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*


/**
 * ViewModel for comprehensive enterprise reporting
 * Manages state and business logic for the Reports screen
 */
class ReportsViewModel(
    private val reportsRepository: ReportsRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // UI State
    private val _selectedReportType = MutableStateFlow("sales")
    val selectedReportType: StateFlow<String> = _selectedReportType.asStateFlow()
    
    private val _selectedDateRange = MutableStateFlow(DateRange.LAST_30_DAYS)
    val selectedDateRange: StateFlow<DateRange> = _selectedDateRange.asStateFlow()
    
    private val _customStartDate = MutableStateFlow<LocalDate?>(null)
    val customStartDate: StateFlow<LocalDate?> = _customStartDate.asStateFlow()
    
    private val _customEndDate = MutableStateFlow<LocalDate?>(null)
    val customEndDate: StateFlow<LocalDate?> = _customEndDate.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _selectedFilters = MutableStateFlow<ReportFilters>(ReportFilters())
    val selectedFilters: StateFlow<ReportFilters> = _selectedFilters.asStateFlow()
    
    // Repository state
    val isLoading = reportsRepository.isLoading
    val error = reportsRepository.error
    
    // Report data
    val comprehensiveSalesReport = reportsRepository.comprehensiveSalesReport
    val customerReport = reportsRepository.customerReport
    val productReport = reportsRepository.productReport
    val inventoryReport = reportsRepository.inventoryReport
    val financialReport = reportsRepository.financialReport
    val promotionReport = reportsRepository.promotionReport
    val realTimeKPIs = reportsRepository.realTimeKPIs
    
    // Computed properties
    val currentDateRange: StateFlow<Pair<String, String>> = combine(
        _selectedDateRange,
        _customStartDate,
        _customEndDate
    ) { range, customStart, customEnd ->
        when (range) {
            DateRange.CUSTOM -> {
                val start = customStart?.toString() ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(30, DateTimeUnit.DAY).toString()
                val end = customEnd?.toString() ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                Pair("${start}T00:00:00", "${end}T23:59:59")
            }
            else -> {
                val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val startDate = when (range) {
                    DateRange.TODAY -> endDate
                    DateRange.YESTERDAY -> endDate.minus(1, DateTimeUnit.DAY)
                    DateRange.LAST_7_DAYS -> endDate.minus(7, DateTimeUnit.DAY)
                    DateRange.LAST_30_DAYS -> endDate.minus(30, DateTimeUnit.DAY)
                    DateRange.LAST_90_DAYS -> endDate.minus(90, DateTimeUnit.DAY)
                    DateRange.THIS_MONTH -> endDate.minus(endDate.dayOfMonth - 1, DateTimeUnit.DAY)
                    DateRange.LAST_MONTH -> {
                        val firstOfThisMonth = endDate.minus(endDate.dayOfMonth - 1, DateTimeUnit.DAY)
                        firstOfThisMonth.minus(1, DateTimeUnit.MONTH)
                    }
                    DateRange.THIS_YEAR -> LocalDate(endDate.year, 1, 1)
                    else -> endDate.minus(30, DateTimeUnit.DAY)
                }
                Pair("${startDate}T00:00:00", "${endDate}T23:59:59")
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        // Provide proper default values instead of empty strings
        run {
            val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val startDate = endDate.minus(30, DateTimeUnit.DAY)
            Pair("${startDate}T00:00:00", "${endDate}T23:59:59")
        }
    )
    
    init {
        // Load initial reports when ViewModel is created
        // Wait for currentDateRange to be initialized before loading reports
        viewModelScope.launch {
            // Wait for the first emission of currentDateRange to ensure it's properly initialized
            currentDateRange.first { it.first.isNotEmpty() && it.second.isNotEmpty() }
            loadInitialReports()
        }

        // Auto-refresh every 5 minutes for real-time data
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000) // 5 minutes
                if (!_isRefreshing.value) {
                    reportsRepository.loadRealTimeKPIs()
                }
            }
        }
    }
    
    // Public methods
    fun selectReportType(type: String) {
        _selectedReportType.value = type
        loadReportForType(type)
    }
    
    fun selectDateRange(range: DateRange) {
        _selectedDateRange.value = range
        if (range != DateRange.CUSTOM) {
            _customStartDate.value = null
            _customEndDate.value = null
        }
        refreshCurrentReport()
    }
    
    fun setCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        _customStartDate.value = startDate
        _customEndDate.value = endDate
        _selectedDateRange.value = DateRange.CUSTOM
        refreshCurrentReport()
    }
    
    fun updateFilters(filters: ReportFilters) {
        _selectedFilters.value = filters
        refreshCurrentReport()
    }
    
    fun refreshCurrentReport() {
        loadReportForType(_selectedReportType.value)
    }
    
    fun refreshAllReports() {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                loadInitialReports()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    suspend fun exportReport(format: String): NetworkResult<ByteArray> {
        val (startDate, endDate) = currentDateRange.value
        val filters = _selectedFilters.value
        
        val request = ReportRequestDTO(
            startDate = startDate,
            endDate = endDate,
            exportFormat = format,
            customerIds = filters.customerIds,
            productIds = filters.productIds,
            categoryIds = filters.categoryIds,
            regions = filters.regions,
            paymentMethods = filters.paymentMethods,
            statuses = filters.statuses
        )
        
        return reportsRepository.exportReport(request)
    }
    
    fun clearError() {
        reportsRepository.clearError()
    }
    
    // Private methods
    private fun loadInitialReports() {
        viewModelScope.launch {
            // Load real-time KPIs first for dashboard
            reportsRepository.loadRealTimeKPIs()
            
            // Load the currently selected report
            loadReportForType(_selectedReportType.value)
        }
    }
    
    private fun loadReportForType(type: String) {
        viewModelScope.launch {
            val (startDate, endDate) = currentDateRange.value
            val filters = _selectedFilters.value

            println("🔍 ReportsViewModel - Loading report type: $type")
            println("🔍 ReportsViewModel - Date range: startDate='$startDate', endDate='$endDate'")

            when (type) {
                "sales" -> loadSalesReport(startDate, endDate, filters)
                "customers" -> loadCustomerReport(filters)
                "products" -> loadProductReport(startDate, endDate, filters)
                "inventory" -> loadInventoryReport(filters)
                "financial" -> loadFinancialReport(startDate, endDate)
                "promotions" -> loadPromotionReport(startDate, endDate)
            }
        }
    }
    
    private suspend fun loadSalesReport(startDate: String, endDate: String, filters: ReportFilters) {
        println("🔍 ReportsViewModel - Loading sales report with dates: startDate='$startDate', endDate='$endDate'")
        val request = ReportRequestDTO(
            startDate = startDate,
            endDate = endDate,
            customerIds = filters.customerIds,
            productIds = filters.productIds,
            regions = filters.regions,
            paymentMethods = filters.paymentMethods,
            statuses = filters.statuses
        )
        reportsRepository.loadComprehensiveSalesReport(request)
    }
    
    private suspend fun loadCustomerReport(filters: ReportFilters) {
        reportsRepository.loadCustomerAnalytics(
            includeInactive = filters.includeInactive,
            months = 12
        )
    }
    
    private suspend fun loadProductReport(startDate: String, endDate: String, filters: ReportFilters) {
        val request = ReportRequestDTO(
            startDate = startDate,
            endDate = endDate,
            categoryIds = filters.categoryIds,
            productIds = filters.productIds
        )
        reportsRepository.loadProductPerformance(request)
    }
    
    private suspend fun loadInventoryReport(filters: ReportFilters) {
        reportsRepository.loadInventoryStatus(
            includeInactive = filters.includeInactive,
            warehouseIds = filters.warehouseIds
        )
    }
    
    private suspend fun loadFinancialReport(startDate: String, endDate: String) {
        reportsRepository.loadFinancialRevenue(startDate, endDate)
    }
    
    private suspend fun loadPromotionReport(startDate: String, endDate: String) {
        reportsRepository.loadPromotionEffectiveness(startDate, endDate)
    }
    
    fun cleanup() {
        viewModelScope.cancel()
    }
}

// Data classes for UI state
enum class DateRange {
    TODAY, YESTERDAY, LAST_7_DAYS, LAST_30_DAYS, LAST_90_DAYS,
    THIS_MONTH, LAST_MONTH, THIS_YEAR, CUSTOM
}

data class ReportFilters(
    val customerIds: List<Long>? = null,
    val productIds: List<Long>? = null,
    val categoryIds: List<Long>? = null,
    val regions: List<String>? = null,
    val paymentMethods: List<String>? = null,
    val statuses: List<String>? = null,
    val warehouseIds: List<Long>? = null,
    val includeInactive: Boolean = false
)
