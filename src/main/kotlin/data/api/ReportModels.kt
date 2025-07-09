package data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Note: DashboardSummaryDTO is now defined in ApiModels.kt

// Standard API Response Wrapper
@Serializable
data class StandardReportResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T,
    val metadata: ReportMetadata? = null,
    val errorCode: String? = null,
    val errorDetails: String? = null
)

@Serializable
data class ReportMetadata(
    val reportType: String,
    val reportName: String,
    val generatedAt: String,
    val generatedBy: String? = null,
    val period: ReportPeriodDTO? = null,
    val appliedFilters: String? = null,
    val pagination: String? = null,
    val totalRecords: Long? = null,
    val executionTimeMs: Long? = null,
    val version: String? = null,
    val fromCache: Boolean? = null,
    val cacheExpiry: String? = null
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

// Enhanced Report Request DTO
@Serializable
data class ReportRequestDTO(
    val startDate: String,
    val endDate: String,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC",
    val customerIds: List<Long>? = null,
    val productIds: List<Long>? = null,
    val categoryIds: List<Long>? = null,
    val regions: List<String>? = null,
    val paymentMethods: List<String>? = null,
    val statuses: List<String>? = null,
    val exportFormat: String? = null,
    val useCache: Boolean = true
)



// Enhanced Sales Report
@Serializable
data class ComprehensiveSalesReportDTO(
    val summary: SalesSummary,
    val dailyBreakdown: List<DailySalesData>,
    val topCustomers: List<TopCustomer>,
    val topProducts: List<TopProduct>,
    val salesByStatus: Map<String, Long>,
    val trends: List<SalesTrend>,
    val paymentAnalysis: PaymentMethodAnalysis,
    val regionalAnalysis: RegionalAnalysis? = null
)

@Serializable
data class SalesSummary(
    val totalSales: Long,
    val totalRevenue: Double,
    val averageOrderValue: Double,
    val totalDiscounts: Double? = null,
    val totalTax: Double? = null,
    val netRevenue: Double? = null,
    val uniqueCustomers: Long? = null,
    val conversionRate: Double? = null,
    val revenueGrowth: Double? = null,
    val salesGrowth: Double? = null
)

@Serializable
data class DailySalesData(
    val date: String,
    val salesCount: Long,
    val revenue: Double,
    val averageOrderValue: Double? = null,
    val uniqueCustomers: Long? = null,
    val discountAmount: Double? = null,
    val growthRate: Double? = null
)

@Serializable
data class TopCustomer(
    val customerId: Long,
    val customerName: String,
    val customerEmail: String? = null,
    val totalOrders: Long,
    val totalSpent: Double,
    val averageOrderValue: Double? = null,
    val lastPurchase: String? = null,
    val customerSegment: String? = null
)

@Serializable
data class TopProduct(
    val productId: Long,
    val productName: String,
    val category: String? = null,
    val quantitySold: Long,
    val revenue: Double,
    val averagePrice: Double? = null,
    val profitMargin: Double? = null,
    val uniqueCustomers: Long? = null
)

@Serializable
data class SalesTrend(
    val period: String,
    val revenue: Double,
    val salesCount: Long,
    val growthRate: Double? = null,
    val trendDirection: String
)

@Serializable
data class PaymentMethodAnalysis(
    val countByMethod: Map<String, Long>,
    val revenueByMethod: Map<String, Double>? = null,
    val mostPopularMethod: String,
    val highestRevenueMethod: String? = null
)

@Serializable
data class RegionalAnalysis(
    val revenueByRegion: Map<String, Double>? = null,
    val salesByRegion: Map<String, Long>? = null,
    val topPerformingRegion: String,
    val regionalGrowth: Double? = null,
    val regionGrowthRates: Map<String, Double>? = null
)

// Legacy Sales Report (for backward compatibility)
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
    val endDate: String,
    val description: String? = null,
    val daysIncluded: Int? = null
)

@Serializable
data class SalesReportSummaryDTO(
    val totalRevenue: Double,
    val totalSales: Int,
    val averageOrderValue: Double
)

// Customer Reports
@Serializable
data class CustomerReportDTO(
    val summary: CustomerSummary,
    val segments: List<CustomerSegment>,
    val topCustomers: List<CustomerLifetimeValue>,
    val retention: CustomerRetentionMetrics,
    val acquisition: CustomerAcquisitionMetrics,
    val behaviorInsights: List<CustomerBehaviorInsight>
)

@Serializable
data class CustomerSummary(
    val totalCustomers: Long,
    val activeCustomers: Long,
    val newCustomersThisMonth: Long,
    val averageCustomerValue: Double,
    val customerRetentionRate: Double,
    val churnRate: Double
)

@Serializable
data class CustomerSegment(
    val segmentName: String,
    val customerCount: Long,
    val averageValue: Double,
    val totalRevenue: Double,
    val percentage: Double
)

@Serializable
data class CustomerLifetimeValue(
    val customerId: Long,
    val customerName: String,
    val email: String? = null,
    val totalValue: Double,
    val averageOrderValue: Double,
    val orderFrequency: Double,
    val lastOrderDate: String? = null,
    val predictedValue: Double,
    val segment: String
)

@Serializable
data class CustomerRetentionMetrics(
    val retentionRate: Double,
    val churnRate: Double,
    val averageLifespan: Double,
    val cohortAnalysis: List<CohortData>
)

@Serializable
data class CustomerAcquisitionMetrics(
    val newCustomersThisMonth: Long,
    val acquisitionCost: Double,
    val acquisitionChannels: Map<String, Long>,
    val conversionRate: Double
)

@Serializable
data class CustomerBehaviorInsight(
    val insight: String,
    val category: String,
    val impact: String,
    val recommendation: String
)

@Serializable
data class CohortData(
    val cohortMonth: String,
    val customersCount: Long,
    val retentionRates: Map<String, Double>
)

// Product Reports
@Serializable
data class ProductReportDTO(
    val summary: ProductSummary? = null,
    val productRankings: JsonElement? = null,
    val crossSellAnalysis: JsonElement? = null,
    val profitabilityAnalysis: JsonElement? = null,
    val productTrends: JsonElement? = null,
    val categoryPerformance: JsonElement? = null,
    // Legacy fields for backward compatibility
    val topProducts: List<ProductPerformance>? = null,
    val categoryAnalysis: List<CategoryAnalysis>? = null,
    val turnoverAnalysis: List<ProductTurnover>? = null
)

@Serializable
data class ProductSummary(
    val totalProducts: Long,
    val activeProducts: Long,
    val totalRevenue: Double,
    val averagePrice: Double,
    val topCategory: String
)

@Serializable
data class ProductPerformance(
    val productId: Long,
    val productName: String,
    val category: String? = null,
    val quantitySold: Long,
    val revenue: Double,
    val profitMargin: Double,
    val growthRate: Double,
    val ranking: Int
)

@Serializable
data class CategoryAnalysis(
    val categoryName: String,
    val productCount: Long,
    val totalRevenue: Double,
    val averagePrice: Double,
    val marketShare: Double
)

@Serializable
data class ProductTurnover(
    val productId: Long,
    val productName: String,
    val turnoverRate: Double,
    val averageInventory: Double,
    val costOfGoodsSold: Double,
    val recommendation: String
)

@Serializable
data class ProductProfitability(
    val productId: Long,
    val productName: String,
    val revenue: Double,
    val cost: Double,
    val profit: Double,
    val profitMargin: Double,
    val roi: Double
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

// Top Products Report (Legacy)
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

// Customer Analytics (Legacy)
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

// Enhanced Inventory Report
@Serializable
data class EnhancedInventoryReportDTO(
    val summary: InventorySummary,
    val stockAlerts: List<StockAlert>,
    val turnoverAnalysis: List<ProductTurnover>,
    val valuation: InventoryValuation,
    val categoryBreakdown: List<CategoryInventoryAnalysis>,
    val warehouseDistribution: List<WarehouseAnalysis>
)

@Serializable
data class InventorySummary(
    val totalProducts: Long,
    val totalInventoryValue: Double,
    val lowStockItems: Long,
    val outOfStockItems: Long,
    val overStockItems: Long,
    val averageTurnoverRate: Double
)

@Serializable
data class StockAlert(
    val productId: Long,
    val productName: String,
    val currentStock: Int,
    val minStockLevel: Int,
    val reorderPoint: Int,
    val alertType: String, // LOW_STOCK, OUT_OF_STOCK, OVERSTOCK
    val urgency: String, // HIGH, MEDIUM, LOW
    val category: String? = null
)

@Serializable
data class InventoryValuation(
    val totalValue: Double,
    val costValue: Double,
    val marketValue: Double,
    val valuationMethod: String,
    val lastUpdated: String
)

@Serializable
data class CategoryInventoryAnalysis(
    val categoryName: String,
    val productCount: Long,
    val totalValue: Double,
    val averageTurnover: Double,
    val stockHealth: String
)

@Serializable
data class WarehouseAnalysis(
    val warehouseId: Long,
    val warehouseName: String,
    val totalProducts: Long,
    val totalValue: Double,
    val utilizationRate: Double,
    val capacity: Double
)

// Financial Reports
@Serializable
data class FinancialReportDTO(
    val summary: FinancialSummary,
    val revenueAnalysis: RevenueAnalysis,
    val profitAnalysis: ProfitAnalysis,
    val cashFlow: CashFlowAnalysis,
    val trends: List<FinancialTrend>
)

@Serializable
data class FinancialSummary(
    val totalRevenue: Double,
    val totalCosts: Double,
    val grossProfit: Double,
    val netProfit: Double,
    val profitMargin: Double,
    val roi: Double
)

@Serializable
data class RevenueAnalysis(
    val totalRevenue: Double,
    val revenueByChannel: Map<String, Double>,
    val revenueByProduct: Map<String, Double>,
    val revenueGrowth: Double,
    val seasonalTrends: List<SeasonalTrend>
)

@Serializable
data class ProfitAnalysis(
    val grossProfit: Double,
    val netProfit: Double,
    val profitByProduct: Map<String, Double>,
    val profitByCategory: Map<String, Double>,
    val profitMarginTrend: List<ProfitTrend>
)

@Serializable
data class CashFlowAnalysis(
    val operatingCashFlow: Double,
    val investingCashFlow: Double,
    val financingCashFlow: Double,
    val netCashFlow: Double,
    val cashFlowTrend: List<CashFlowTrend>
)

@Serializable
data class FinancialTrend(
    val period: String,
    val revenue: Double,
    val profit: Double,
    val margin: Double,
    val growth: Double
)

@Serializable
data class SeasonalTrend(
    val period: String,
    val revenue: Double,
    val seasonalIndex: Double
)

@Serializable
data class ProfitTrend(
    val period: String,
    val grossMargin: Double,
    val netMargin: Double
)

@Serializable
data class CashFlowTrend(
    val period: String,
    val inflow: Double,
    val outflow: Double,
    val netFlow: Double
)

// Promotion Reports
@Serializable
data class PromotionReportDTO(
    val summary: PromotionSummary,
    val effectiveness: List<PromotionEffectiveness>,
    val usage: List<PromotionUsage>,
    val roi: List<PromotionROI>,
    val customerResponse: PromotionCustomerResponse
)

@Serializable
data class PromotionSummary(
    val totalPromotions: Long,
    val activePromotions: Long,
    val totalDiscountGiven: Double,
    val averageDiscountPercentage: Double,
    val promotionRevenue: Double,
    val promotionROI: Double
)

@Serializable
data class PromotionEffectiveness(
    val promotionId: Long,
    val promotionName: String,
    val discountType: String,
    val usageCount: Long,
    val totalDiscount: Double,
    val revenueGenerated: Double,
    val roi: Double,
    val effectiveness: String
)

@Serializable
data class PromotionUsage(
    val promotionId: Long,
    val promotionName: String,
    val totalUsage: Long,
    val uniqueCustomers: Long,
    val averageOrderValue: Double,
    val usagePattern: Map<String, Long>
)

@Serializable
data class PromotionROI(
    val promotionId: Long,
    val promotionName: String,
    val investmentCost: Double,
    val revenueGenerated: Double,
    val roi: Double,
    val paybackPeriod: Int
)

@Serializable
data class PromotionCustomerResponse(
    val newCustomersAcquired: Long,
    val customerRetentionRate: Double,
    val repeatPurchaseRate: Double,
    val customerSegmentResponse: Map<String, Double>
)


// Legacy Inventory Report (for backward compatibility)
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


