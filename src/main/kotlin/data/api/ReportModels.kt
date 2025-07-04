package data.api

import kotlinx.serialization.Serializable

// Dashboard Summary
@Serializable
data class DashboardSummaryDTO(
    val period: String,
    val sales: SalesSummaryDTO,
    val customers: CustomersSummaryDTO,
    val inventory: InventorySummaryDTO,
    val generatedAt: String
)

@Serializable
data class SalesSummaryDTO(
    val totalRevenue: Double,
    val totalSales: Int
)

@Serializable
data class CustomersSummaryDTO(
    val totalCustomers: Int,
    val activeCustomers: Int
)

@Serializable
data class InventorySummaryDTO(
    val totalProducts: Int,
    val lowStockItems: Int
)

// Sales Report
@Serializable
data class SalesReportDTO(
    val period: ReportPeriodDTO,
    val summary: SalesReportSummaryDTO,
    val salesByStatus: Map<String, Int>,
    val dailyRevenue: Map<String, Double>
)

@Serializable
data class ReportPeriodDTO(
    val startDate: String,
    val endDate: String
)

@Serializable
data class SalesReportSummaryDTO(
    val totalRevenue: Double,
    val totalSales: Int,
    val averageOrderValue: Double
)

// Revenue Trends
@Serializable
data class RevenueTrendsDTO(
    val months: List<MonthlyRevenueDTO>,
    val totalRevenue: Double,
    val averageMonthlyRevenue: Double,
    val growthRate: Double
)

@Serializable
data class MonthlyRevenueDTO(
    val month: String,
    val year: Int,
    val revenue: Double,
    val salesCount: Int
)

// Top Products Report
@Serializable
data class TopProductsReportDTO(
    val period: ReportPeriodDTO,
    val products: List<TopProductDTO>
)

@Serializable
data class TopProductDTO(
    val productId: Long,
    val productName: String,
    val category: String?,
    val totalSold: Int,
    val totalRevenue: Double,
    val averagePrice: Double
)

// Customer Analytics
@Serializable
data class CustomerAnalyticsDTO(
    val totalCustomers: Int,
    val newCustomersThisMonth: Int,
    val activeCustomers: Int,
    val customerRetentionRate: Double,
    val averageCustomerValue: Double,
    val topCustomers: List<TopCustomerDTO>,
    val customerSegmentation: Map<String, Int>
)

@Serializable
data class TopCustomerDTO(
    val customerId: Long,
    val customerName: String,
    val totalOrders: Int,
    val totalSpent: Double,
    val lastOrderDate: String?
)

// Inventory Report
@Serializable
data class InventoryReportDTO(
    val totalProducts: Int,
    val totalInventoryValue: Double,
    val lowStockProducts: List<LowStockProductDTO>,
    val outOfStockProducts: List<ProductDTO>,
    val topSellingProducts: List<TopProductDTO>,
    val slowMovingProducts: List<SlowMovingProductDTO>
)

@Serializable
data class LowStockProductDTO(
    val productId: Long,
    val productName: String,
    val currentStock: Int,
    val minStockLevel: Int,
    val reorderPoint: Int,
    val category: String?
)

@Serializable
data class SlowMovingProductDTO(
    val productId: Long,
    val productName: String,
    val currentStock: Int,
    val lastSoldDate: String?,
    val daysSinceLastSale: Int,
    val category: String?
)
