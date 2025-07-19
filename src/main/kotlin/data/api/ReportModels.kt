package data.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull

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

/**
 * Metadata for report responses containing execution details and applied filters.
 *
 * Note: appliedFilters and pagination are JsonElement to handle dynamic structures
 * from the backend (objects, arrays, or primitives). Use extension functions
 * like getFilterBoolean(), getFilterString(), etc. for safe access.
 */
@Serializable
data class ReportMetadata(
    val reportType: String,
    val reportName: String,
    val generatedAt: String,
    val generatedBy: String? = null,
    val period: ReportPeriodDTO? = null,
    val appliedFilters: JsonElement? = null, // Can be object, array, or primitive
    val pagination: JsonElement? = null,     // Can be object, array, or primitive
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

// Customer Reports - Updated to match new comprehensive backend response structure
@Serializable
data class CustomerReportDTO(
    val customerSegmentation: CustomerSegmentationData? = null,
    val acquisitionMetrics: CustomerAcquisitionData? = null,
    val lifetimeValueAnalysis: CustomerLifetimeValueData? = null,
    val churnAnalysis: CustomerChurnData? = null,
    val behaviorAnalysis: CustomerBehaviorData? = null
)

// Customer Segmentation Data - matches API response structure
@Serializable
data class CustomerSegmentationData(
    val segments: CustomerSegments? = null,
    val totalCustomers: Long? = null,
    val customerDetails: CustomerSegmentDetails? = null,
    val summary: CustomerSegmentSummary? = null,
    val thresholds: CustomerSegmentThresholds? = null
)

@Serializable
data class CustomerSegments(
    val lowValue: CustomerSegmentInfo? = null,
    val mediumValue: CustomerSegmentInfo? = null,
    val highValue: CustomerSegmentInfo? = null,
    val newCustomers: CustomerSegmentInfo? = null,
    val atRiskCustomers: CustomerSegmentInfo? = null
)

@Serializable
data class CustomerSegmentInfo(
    val count: Long? = null,
    val totalRevenue: Double? = null,
    val percentage: Double? = null,
    val avgOrderValue: Double? = null
)

@Serializable
data class CustomerSegmentDetails(
    val lowValue: List<CustomerDetail>? = null,
    val mediumValue: List<CustomerDetail>? = null,
    val highValue: List<CustomerDetail>? = null,
    val newCustomers: List<CustomerDetail>? = null,
    val atRiskCustomers: List<CustomerDetail>? = null
)

@Serializable
data class CustomerDetail(
    val customerId: Long? = null,
    val customerName: String? = null,
    val email: String? = null,
    val totalSpent: Double? = null,
    val totalOrders: Long? = null,
    val avgOrderValue: Double? = null,
    val firstPurchase: String? = null,
    val lastPurchase: String? = null
)

@Serializable
data class CustomerSegmentSummary(
    val totalRevenue: Double? = null,
    val avgCustomerValue: Double? = null
)

@Serializable
data class CustomerSegmentThresholds(
    val mediumValue: Double? = null,
    val highValue: Double? = null
)

// Customer Acquisition Data - matches API response structure
@Serializable
data class CustomerAcquisitionData(
    val channels: CustomerAcquisitionChannels? = null,
    val acquisitionTrends: CustomerAcquisitionTrends? = null,
    val metrics: CustomerAcquisitionMetricsData? = null,
    val recommendations: List<String>? = null
)

@Serializable
data class CustomerAcquisitionChannels(
    val topChannel: String? = null,
    val acquisitionChannels: Map<String, Long>? = null
)

@Serializable
data class CustomerAcquisitionTrends(
    val monthlyAcquisition: Map<String, Long>? = null,
    val growthRate: Double? = null,
    val totalNewCustomers: Long? = null
)

@Serializable
data class CustomerAcquisitionMetricsData(
    val customersWithPurchases: Long? = null,
    val avgDaysToFirstPurchase: Double? = null,
    val estimatedAcquisitionCost: Double? = null,
    val conversionRate: Double? = null
)

// Customer Lifetime Value Data - matches API response structure
@Serializable
data class CustomerLifetimeValueData(
    val topCustomers: List<CustomerLifetimeValueDetail>? = null,
    val recommendations: List<String>? = null,
    val metrics: CustomerLTVMetrics? = null,
    val analysis: CustomerLTVAnalysis? = null
)

@Serializable
data class CustomerLifetimeValueDetail(
    val customerId: Long? = null,
    val customerName: String? = null,
    val email: String? = null,
    val totalRevenue: Double? = null,
    val totalOrders: Long? = null,
    val avgOrderValue: Double? = null,
    val firstPurchase: String? = null,
    val lastPurchase: String? = null,
    val lifespanDays: Long? = null,
    val purchaseFrequency: Double? = null,
    val predictedLTV: Double? = null,
    val customerSegment: String? = null,
    val profitMargin: Double? = null
)

@Serializable
data class CustomerLTVMetrics(
    val lowValueCustomers: Long? = null,
    val mediumValueCustomers: Long? = null,
    val highValueCustomers: Long? = null,
    val revenueConcentration: CustomerRevenueConcentration? = null
)

@Serializable
data class CustomerRevenueConcentration(
    val top10Percent: Double? = null,
    val top20Percent: Double? = null
)

@Serializable
data class CustomerLTVAnalysis(
    val totalCustomers: Long? = null,
    val totalRevenue: Double? = null,
    val avgLifetimeValue: Double? = null,
    val avgOrderValue: Double? = null,
    val avgPurchaseFrequency: Double? = null,
    val ltvDistribution: Map<String, Long>? = null
)

// Customer Churn Analysis Data - matches API response structure
@Serializable
data class CustomerChurnData(
    val churnMetrics: CustomerChurnMetrics? = null,
    val churnedCustomers: List<CustomerDetail>? = null,
    val riskAnalysis: CustomerRiskAnalysis? = null,
    val recommendations: List<String>? = null
)

@Serializable
data class CustomerChurnMetrics(
    val churnRate: Double? = null,
    val totalCustomers: Long? = null,
    val churnedCustomers: Long? = null,
    val revenueAtRisk: Double? = null,
    val avgDaysSinceLastPurchase: Double? = null
)

@Serializable
data class CustomerRiskAnalysis(
    val riskDistribution: Map<String, Long>? = null,
    val retentionOpportunity: Double? = null,
    val highRiskCustomers: List<CustomerDetail>? = null
)

// Customer Behavior Analysis Data - matches API response structure
@Serializable
data class CustomerBehaviorData(
    val seasonality: CustomerSeasonality? = null,
    val preferences: CustomerPreferences? = null,
    val purchasePatterns: CustomerPurchasePatterns? = null,
    val customerJourney: CustomerJourney? = null,
    val summary: CustomerBehaviorSummary? = null
)

@Serializable
data class CustomerSeasonality(
    val monthlyTrends: Map<String, Long>? = null,
    val dayOfWeekTrends: Map<String, Long>? = null,
    val hourlyTrends: Map<String, Long>? = null,
    val peakSalesDay: String? = null,
    val peakSalesHour: String? = null
)

@Serializable
data class CustomerPreferences(
    val paymentMethodPreferences: Map<String, Long>? = null,
    val saleTypePreferences: Map<String, Long>? = null,
    val avgDiscountUsage: Double? = null
)

@Serializable
data class CustomerPurchasePatterns(
    val orderValueDistribution: Map<String, Long>? = null,
    val frequencyDistribution: Map<String, Long>? = null,
    val repeatCustomerRate: Double? = null,
    val avgDaysBetweenPurchases: Double? = null
)

@Serializable
data class CustomerJourney(
    val lifecycleStages: Map<String, Long>? = null,
    val avgCustomerAge: Double? = null,
    val customerRetentionInsights: List<String>? = null
)

@Serializable
data class CustomerBehaviorSummary(
    val analysisDate: String? = null,
    val totalCustomers: Long? = null,
    val totalSales: Long? = null
)

// Legacy Customer Report DTO for backward compatibility
@Serializable
data class LegacyCustomerReportDTO(
    val summary: CustomerSummary,
    val segments: List<CustomerSegment>,
    val topCustomers: List<CustomerLifetimeValue>,
    val retention: CustomerRetentionMetrics,
    val acquisition: CustomerAcquisitionMetrics,
    val behaviorInsights: List<CustomerBehaviorInsight>
)

// Helper functions for backward compatibility (internal use only)
private fun CustomerReportDTO.getAcquisitionMetrics(): CustomerAcquisitionMetrics? {
    val data = acquisitionMetrics ?: return null
    return CustomerAcquisitionMetrics(
        newCustomersThisMonth = data.acquisitionTrends?.totalNewCustomers ?: 0L,
        acquisitionCost = data.metrics?.estimatedAcquisitionCost ?: 0.0,
        acquisitionChannels = data.channels?.acquisitionChannels ?: emptyMap(),
        conversionRate = data.metrics?.conversionRate ?: 0.0
    )
}

private fun CustomerReportDTO.getRetentionMetrics(): CustomerRetentionMetrics? {
    val data = churnAnalysis ?: return null
    return CustomerRetentionMetrics(
        retentionRate = (100.0 - (data.churnMetrics?.churnRate ?: 0.0)), // Calculate retention from churn
        churnRate = data.churnMetrics?.churnRate ?: 0.0,
        averageLifespan = 0.0, // Not available in new structure
        cohortAnalysis = emptyList() // Not available in new structure
    )
}

// Computed summary property for backward compatibility
val CustomerReportDTO.summary: CustomerSummary
    get() {
        val segmentationData = customerSegmentation
        val acquisitionData = acquisitionMetrics
        val lifetimeData = lifetimeValueAnalysis
        val churnData = churnAnalysis

        return CustomerSummary(
            totalCustomers = segmentationData?.totalCustomers ?: 0L,
            activeCustomers = segmentationData?.totalCustomers ?: 0L, // Assuming all are active if not specified
            newCustomersThisMonth = acquisitionData?.acquisitionTrends?.totalNewCustomers ?: 0L,
            averageCustomerValue = lifetimeData?.analysis?.avgLifetimeValue ?: 0.0,
            customerRetentionRate = (100.0 - (churnData?.churnMetrics?.churnRate ?: 0.0)),
            churnRate = churnData?.churnMetrics?.churnRate ?: 0.0
        )
    }

// Computed segments property for backward compatibility
val CustomerReportDTO.segments: List<CustomerSegment>
    get() {
        val segments = customerSegmentation?.segments
        return listOfNotNull(
            segments?.newCustomers?.let {
                CustomerSegment(
                    segmentName = "عملاء جدد",
                    customerCount = it.count ?: 0L,
                    totalRevenue = it.totalRevenue ?: 0.0,
                    percentage = it.percentage ?: 0.0,
                    averageValue = it.avgOrderValue ?: 0.0
                )
            },
            segments?.highValue?.let {
                CustomerSegment(
                    segmentName = "عملاء عالي القيمة",
                    customerCount = it.count ?: 0L,
                    totalRevenue = it.totalRevenue ?: 0.0,
                    percentage = it.percentage ?: 0.0,
                    averageValue = it.avgOrderValue ?: 0.0
                )
            },
            segments?.mediumValue?.let {
                CustomerSegment(
                    segmentName = "عملاء متوسط القيمة",
                    customerCount = it.count ?: 0L,
                    totalRevenue = it.totalRevenue ?: 0.0,
                    percentage = it.percentage ?: 0.0,
                    averageValue = it.avgOrderValue ?: 0.0
                )
            },
            segments?.lowValue?.let {
                CustomerSegment(
                    segmentName = "عملاء منخفض القيمة",
                    customerCount = it.count ?: 0L,
                    totalRevenue = it.totalRevenue ?: 0.0,
                    percentage = it.percentage ?: 0.0,
                    averageValue = it.avgOrderValue ?: 0.0
                )
            }
        )
    }

// Computed topCustomers property for backward compatibility
val CustomerReportDTO.topCustomers: List<CustomerLifetimeValue>
    get() = lifetimeValueAnalysis?.topCustomers?.map { customer ->
        CustomerLifetimeValue(
            customerId = customer.customerId ?: 0L,
            customerName = customer.customerName ?: "",
            email = customer.email,
            totalValue = customer.totalRevenue ?: 0.0,
            averageOrderValue = customer.avgOrderValue ?: 0.0,
            orderFrequency = customer.purchaseFrequency ?: 0.0,
            lastOrderDate = customer.lastPurchase,
            predictedValue = customer.predictedLTV ?: 0.0,
            segment = customer.customerSegment ?: ""
        )
    } ?: emptyList()

// Computed retention property for backward compatibility
val CustomerReportDTO.retention: CustomerRetentionMetrics
    get() = getRetentionMetrics() ?: CustomerRetentionMetrics(
        retentionRate = 0.0,
        churnRate = 0.0,
        averageLifespan = 0.0,
        cohortAnalysis = emptyList()
    )

// Computed acquisition property for backward compatibility
val CustomerReportDTO.acquisition: CustomerAcquisitionMetrics
    get() = getAcquisitionMetrics() ?: CustomerAcquisitionMetrics(
        newCustomersThisMonth = 0L,
        acquisitionCost = 0.0,
        acquisitionChannels = emptyMap(),
        conversionRate = 0.0
    )

// Computed behaviorInsights property for backward compatibility
val CustomerReportDTO.behaviorInsights: List<CustomerBehaviorInsight>
    get() {
        val insights = mutableListOf<CustomerBehaviorInsight>()

        // Generate insights from behavior analysis data
        behaviorAnalysis?.let { behavior ->
            behavior.seasonality?.peakSalesDay?.let { peakDay ->
                insights.add(
                    CustomerBehaviorInsight(
                        insight = "أفضل يوم للمبيعات هو $peakDay",
                        category = "Seasonality",
                        impact = "High",
                        recommendation = "ركز الحملات التسويقية في يوم $peakDay"
                    )
                )
            }

            behavior.purchasePatterns?.repeatCustomerRate?.let { rate ->
                if (rate > 50.0) {
                    insights.add(
                        CustomerBehaviorInsight(
                            insight = "معدل العملاء المتكررين مرتفع (${String.format("%.1f", rate)}%)",
                            category = "Retention",
                            impact = "High",
                            recommendation = "استمر في برامج الولاء الحالية"
                        )
                    )
                } else {
                    insights.add(
                        CustomerBehaviorInsight(
                            insight = "معدل العملاء المتكررين منخفض (${String.format("%.1f", rate)}%)",
                            category = "Retention",
                            impact = "Medium",
                            recommendation = "طور برامج ولاء جديدة لزيادة الاحتفاظ بالعملاء"
                        )
                    )
                }
            }
        }

        return insights
    }

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

// Product Reports - Updated to match comprehensive backend response structure
@Serializable
data class ProductReportDTO(
    val productRankings: ProductRankingsData? = null,
    val crossSellAnalysis: CrossSellAnalysisData? = null,
    val profitabilityAnalysis: ProfitabilityAnalysisData? = null,
    val productTrends: ProductTrendsData? = null,
    val categoryPerformance: CategoryPerformanceData? = null,
    val reportSummary: ProductReportSummary? = null,
    val dataValidation: ProductDataValidation? = null,
    val metadata: ProductReportMetadata? = null,
    // Legacy fields for backward compatibility
    val summary: ProductSummary? = null,
    val topProducts: List<ProductPerformance>? = null,
    val categoryAnalysis: List<CategoryAnalysis>? = null,
    val turnoverAnalysis: List<ProductTurnover>? = null
)

// New comprehensive data models for product performance API
@Serializable
data class ProductReportSummary(
    val insights: List<String>? = null,
    val salesMetrics: ProductSalesMetrics? = null,
    val reportPeriod: ProductReportPeriod? = null,
    val financialMetrics: ProductFinancialMetrics? = null,
    val productCounts: ProductCounts? = null
)

@Serializable
data class ProductSalesMetrics(
    val totalSales: Long? = null,
    val totalRevenue: Double? = null,
    val totalQuantitySold: Long? = null,
    val avgOrderValue: Double? = null,
    val uniqueCustomers: Long? = null
)

@Serializable
data class ProductReportPeriod(
    val startDate: String? = null,
    val endDate: String? = null,
    val daysIncluded: Int? = null,
    val description: String? = null
)

@Serializable
data class ProductFinancialMetrics(
    val totalProfit: Double? = null,
    val avgProfitMargin: Double? = null,
    val totalCost: Double? = null,
    val roi: Double? = null
)

@Serializable
data class ProductCounts(
    val totalProducts: Long? = null,
    val activeProducts: Long? = null,
    val categoriesIncluded: Long? = null,
    val productsWithSales: Long? = null
)

@Serializable
data class ProductDataValidation(
    val validationCounts: ProductValidationCounts? = null,
    val productCoverage: ProductCoverage? = null,
    val dataQualityScore: Double? = null,
    val warnings: List<String>? = null,
    val errors: List<String>? = null
)

@Serializable
data class ProductValidationCounts(
    val totalRecords: Long? = null,
    val validRecords: Long? = null,
    val invalidRecords: Long? = null,
    val duplicateRecords: Long? = null
)

@Serializable
data class ProductCoverage(
    val productsWithData: Long? = null,
    val productsWithoutData: Long? = null,
    val coveragePercentage: Double? = null
)

@Serializable
data class ProductReportMetadata(
    val reportId: String? = null,
    val generatedAt: String? = null,
    val generatedBy: String? = null,
    val reportVersion: String? = null,
    val executionTimeMs: Long? = null,
    val dataSourceVersion: String? = null,
    val cacheStatus: String? = null
)

// Product Rankings Data - matches API response structure
@Serializable
data class ProductRankingsData(
    val allProductMetrics: List<ProductAllMetricsItem>? = null, // API returns array, not object
    val topProductsByQuantity: List<ProductRankingItem>? = null,
    val topProductsByRevenue: List<ProductRankingItem>? = null,
    val topProductsByMargin: List<ProductRankingItem>? = null,
    val topProductsByProfit: List<ProductRankingItem>? = null,
    val summary: ProductRankingSummary? = null,
    val recommendations: List<String>? = null
)

@Serializable
data class ProductAllMetrics(
    val totalProducts: Long? = null,
    val totalRevenue: Double? = null,
    val totalQuantitySold: Long? = null,
    val totalProfit: Double? = null,
    val avgProfitMargin: Double? = null
)

// New data class to match the actual API response structure for allProductMetrics array items
@Serializable
data class ProductAllMetricsItem(
    val totalQuantitySold: Long? = null,
    val salesCount: Long? = null,
    val productId: Long? = null,
    val totalProfit: Double? = null,
    val stockTurnover: Double? = null,
    val productName: String? = null,
    val avgUnitPrice: Double? = null,
    val profitMargin: Double? = null,
    val currentStock: Long? = null,
    val revenuePercentage: Double? = null,
    val totalRevenue: Double? = null,
    val sku: String? = null,
    val category: String? = null,
    val brand: String? = null,
    val totalCost: Double? = null
)

// Extension to make ProductAllMetrics compatible with ProductRankingSummary interface
val ProductAllMetrics.asRankingSummary: ProductRankingSummary
    get() = ProductRankingSummary(
        totalProducts = this.totalProducts,
        totalRevenue = this.totalRevenue,
        totalQuantitySold = this.totalQuantitySold,
        avgProfitMargin = this.avgProfitMargin,
        totalProfit = this.totalProfit,
        avgUnitPrice = null // Not available in ProductAllMetrics
    )

// Extension to convert List<ProductAllMetricsItem> to ProductRankingSummary for backward compatibility
val List<ProductAllMetricsItem>.asRankingSummary: ProductRankingSummary
    get() {
        val totalRevenue = this.sumOf { it.totalRevenue ?: 0.0 }
        val totalQuantity = this.sumOf { it.totalQuantitySold ?: 0L }
        val totalProfit = this.sumOf { it.totalProfit ?: 0.0 }
        val avgProfitMargin = if (this.isNotEmpty()) {
            this.mapNotNull { it.profitMargin }.average()
        } else 0.0
        val avgUnitPrice = if (this.isNotEmpty()) {
            this.mapNotNull { it.avgUnitPrice }.average()
        } else 0.0

        return ProductRankingSummary(
            totalProducts = this.size.toLong(),
            totalRevenue = totalRevenue,
            totalQuantitySold = totalQuantity,
            avgProfitMargin = avgProfitMargin,
            totalProfit = totalProfit,
            avgUnitPrice = avgUnitPrice
        )
    }

@Serializable
data class ProductRankingSummary(
    val totalProducts: Long? = null,
    val totalRevenue: Double? = null,
    val totalQuantitySold: Long? = null,
    val avgProfitMargin: Double? = null,
    val totalProfit: Double? = null,
    val avgUnitPrice: Double? = null
)

@Serializable
data class ProductRankingItem(
    val productId: Long? = null,
    val productName: String? = null,
    val category: String? = null,
    val quantitySold: Long? = null,
    val revenue: Double? = null,
    val profit: Double? = null,
    val profitMargin: Double? = null,
    val unitPrice: Double? = null,
    val rank: Int? = null
)

// Cross-Sell Analysis Data - matches API response structure
@Serializable
data class CrossSellAnalysisData(
    val basketAnalysis: BasketAnalysisData? = null,
    val productPairs: List<ProductPair>? = null,
    val crossSellOpportunities: List<CrossSellOpportunity>? = null,
    val productAssociations: List<ProductAssociation>? = null,
    val recommendations: List<String>? = null
)

@Serializable
data class ProductPair(
    val productA: String? = null,
    val productB: String? = null,
    val frequency: Long? = null,
    val confidence: Double? = null,
    val lift: Double? = null,
    val support: Double? = null
)

@Serializable
data class CrossSellOpportunity(
    val primaryProduct: String? = null,
    val suggestedProducts: List<String>? = null,
    val confidence: Double? = null,
    val potentialRevenue: Double? = null
)

@Serializable
data class BasketAnalysisData(
    val avgBasketSize: Double? = null,
    val avgBasketValue: Double? = null,
    val mostCommonCombinations: List<ProductCombination>? = null
)

@Serializable
data class ProductCombination(
    val products: List<String>? = null,
    val frequency: Long? = null,
    val totalValue: Double? = null
)

@Serializable
data class ProductAssociation(
    val productA: String? = null,
    val productB: String? = null,
    val support: Double? = null,
    val confidence: Double? = null,
    val lift: Double? = null
)

// Profitability Analysis Data - matches API response structure
@Serializable
data class ProfitabilityAnalysisData(
    val mostProfitableProducts: List<ProductRankingItem>? = null,
    val profitabilityMetrics: ProfitabilityMetrics? = null,
    val categoryProfitability: Map<String, CategoryProfitability>? = null,
    val costAnalysis: CostAnalysisData? = null,
    val leastProfitableProducts: List<ProductRankingItem>? = null,
    val profitMarginDistribution: Map<String, Long>? = null,
    val recommendations: List<String>? = null
)

@Serializable
data class ProfitabilityMetrics(
    val totalProfit: Double? = null,
    val avgProfitMargin: Double? = null,
    val profitGrowth: Double? = null,
    val marginTrend: String? = null
)

@Serializable
data class ProfitMarginsData(
    val avgProfitMargin: Double? = null,
    val marginDistribution: Map<String, Long>? = null,
    val topProfitableProducts: List<ProductRankingItem>? = null,
    val lowMarginProducts: List<ProductRankingItem>? = null
)

@Serializable
data class CostAnalysisData(
    val totalCosts: Double? = null,
    val avgCostPerUnit: Double? = null,
    val costBreakdown: Map<String, Double>? = null
)

@Serializable
data class CategoryProfitability(
    val categoryName: String? = null,
    val revenue: Double? = null,
    val totalRevenue: Double? = null,
    val cost: Double? = null,
    val profit: Double? = null,
    val totalProfit: Double? = null,
    val profitMargin: Double? = null,
    val itemCount: Long? = null,
    val productCount: Long? = null
)

@Serializable
data class ProductSummary(
    val totalProducts: Long,
    val activeProducts: Long,
    val totalRevenue: Double,
    val averagePrice: Double,
    val topCategory: String
)

// Computed summary property for backward compatibility
val ProductReportDTO.computedSummary: ProductSummary
    get() {
        val rankings = productRankings
        val categoryPerf = categoryPerformance

        return summary ?: ProductSummary(
            totalProducts = rankings?.summary?.totalProducts ?: 0L,
            activeProducts = rankings?.summary?.totalProducts ?: 0L, // Assuming all are active
            totalRevenue = rankings?.summary?.totalRevenue ?: 0.0,
            averagePrice = 0.0, // Not available in new structure
            topCategory = categoryPerf?.categoryMetricsData?.topPerformingCategory ?: ""
        )
    }

// Computed topProducts property for backward compatibility
val ProductReportDTO.computedTopProducts: List<ProductPerformance>
    get() = topProducts ?: productRankings?.topProductsByRevenue?.map { item ->
        ProductPerformance(
            productId = item.productId ?: 0L,
            productName = item.productName ?: "",
            category = item.category,
            quantitySold = item.quantitySold ?: 0L,
            revenue = item.revenue ?: 0.0,
            profitMargin = item.profitMargin ?: 0.0,
            growthRate = 0.0, // Not available in new structure
            ranking = item.rank ?: 0
        )
    } ?: emptyList()

// Computed categoryProfitabilityList property for backward compatibility
val ProfitabilityAnalysisData.categoryProfitabilityList: List<CategoryProfitability>
    get() = categoryProfitability?.map { (categoryName, profitData) ->
        profitData.copy(categoryName = categoryName)
    } ?: emptyList()

// Product Trends Data - matches API response structure
@Serializable
data class ProductTrendsData(
    val trendSummary: ProductTrendSummary? = null,
    val trendingProducts: List<TrendingProduct>? = null,
    val weeklyTrends: Map<String, WeeklyTrend>? = null,
    val dailyTrends: Map<String, DailyTrend>? = null,
    val seasonalPatterns: Map<String, Double>? = null,
    val recommendations: List<String>? = null
)

@Serializable
data class ProductTrendSummary(
    val totalTrendingProducts: Long? = null,
    val avgGrowthRate: Double? = null,
    val topTrendDirection: String? = null,
    val periodComparison: String? = null
)

@Serializable
data class DailyTrend(
    val date: String? = null,
    val totalQuantity: Long? = null,
    val totalSales: Long? = null,
    val totalRevenue: Double? = null,
    val salesCount: Long? = null,
    val uniqueProducts: Long? = null,
    val topProduct: String? = null
)

@Serializable
data class WeeklyTrend(
    val week: String? = null,
    val totalQuantity: Long? = null,
    val totalSales: Long? = null,
    val totalRevenue: Double? = null,
    val salesCount: Long? = null,
    val uniqueProducts: Long? = null,
    val growthRate: Double? = null
)

@Serializable
data class TrendingProduct(
    val productName: String? = null,
    val category: String? = null,
    val growthRate: Double? = null,
    val currentSales: Long? = null,
    val previousSales: Long? = null,
    val trendDirection: String? = null
)

// Computed trends list properties for backward compatibility
val ProductTrendsData.dailyTrendsList: List<DailyTrend>
    get() = dailyTrends?.map { (date, trendData) ->
        trendData.copy(date = date)
    } ?: emptyList()

val ProductTrendsData.weeklyTrendsList: List<WeeklyTrend>
    get() = weeklyTrends?.map { (week, trendData) ->
        trendData.copy(week = week)
    } ?: emptyList()

// Computed category performance properties for backward compatibility
val CategoryPerformanceData.categoryMetricsData: CategoryMetricsData?
    get() = categoryComparison?.let { comparison ->
        CategoryMetricsData(
            totalCategories = comparison.totalCategories,
            avgRevenuePerCategory = comparison.avgRevenuePerCategory,
            topPerformingCategory = categoryMetrics?.maxByOrNull { it.totalRevenue ?: 0.0 }?.categoryName,
            fastestGrowingCategory = null // Not available in new structure
        )
    }

val CategoryPerformanceData.categoryComparisonList: List<CategoryComparison>
    get() = categoryMetrics?.map { metric ->
        CategoryComparison(
            categoryName = metric.categoryName,
            totalRevenue = metric.totalRevenue,
            totalQuantity = metric.totalQuantitySold,
            avgPrice = metric.avgUnitPrice,
            profitMargin = metric.profitMargin,
            marketShare = metric.revenuePercentage
        )
    } ?: emptyList()

// Category Performance Data - matches API response structure
@Serializable
data class CategoryPerformanceData(
    val categoryComparison: CategoryComparisonSummary? = null,
    val categoryMetrics: List<CategoryMetric>? = null,
    val topCategoriesByRevenue: List<CategoryMetric>? = null,
    val topCategoriesByQuantity: List<CategoryMetric>? = null,
    val topCategoriesByProfitMargin: List<CategoryMetric>? = null,
    val topCategories: List<TopCategory>? = null,
    val recommendations: List<String>? = null
)

@Serializable
data class CategoryComparisonSummary(
    val totalCategories: Long? = null,
    val totalRevenue: Double? = null,
    val avgRevenuePerCategory: Double? = null,
    val totalQuantitySold: Long? = null
)

@Serializable
data class CategoryMetric(
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val totalRevenue: Double? = null,
    val totalQuantitySold: Long? = null,
    val salesCount: Long? = null,
    val totalProfit: Double? = null,
    val uniqueProducts: Long? = null,
    val quantityPercentage: Double? = null,
    val avgUnitPrice: Double? = null,
    val profitMargin: Double? = null,
    val revenuePercentage: Double? = null,
    val avgOrderValue: Double? = null,
    val uniqueCustomers: Long? = null
)

@Serializable
data class CategoryComparison(
    val categoryName: String? = null,
    val totalRevenue: Double? = null,
    val totalQuantity: Long? = null,
    val avgPrice: Double? = null,
    val profitMargin: Double? = null,
    val marketShare: Double? = null
)

@Serializable
data class CategoryMetricsData(
    val totalCategories: Long? = null,
    val avgRevenuePerCategory: Double? = null,
    val topPerformingCategory: String? = null,
    val fastestGrowingCategory: String? = null
)

@Serializable
data class TopCategory(
    val categoryName: String? = null,
    val rank: Int? = null,
    val revenue: Double? = null,
    val growth: Double? = null,
    val productCount: Long? = null
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

// Enhanced Inventory Report - Updated to match backend response structure
@Serializable
data class EnhancedInventoryReportDTO(
    val stockLevels: JsonElement? = null,
    val lowStockAlerts: JsonElement? = null,
    val outOfStockItems: List<JsonElement>? = null,
    val inventoryValuation: JsonElement? = null,
    val warehouseDistribution: JsonElement? = null,
    // Legacy fields for backward compatibility
    val summary: InventorySummary? = null,
    val stockAlerts: List<StockAlert>? = null,
    val turnoverAnalysis: List<ProductTurnover>? = null,
    val valuation: InventoryValuation? = null,
    val categoryBreakdown: List<CategoryInventoryAnalysis>? = null
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

// Financial Reports - Updated to match new comprehensive API response
@Serializable
data class FinancialReportDTO(
    val taxAnalysis: TaxAnalysisData? = null,
    val profitMarginAnalysis: ProfitMarginAnalysisData? = null,
    val revenueAnalysis: FinancialRevenueAnalysisData? = null,
    val executiveSummary: ExecutiveSummaryData? = null,
    val advancedMetrics: AdvancedMetricsData? = null,
    val paymentMethodAnalysis: PaymentMethodAnalysisData? = null,
    val costAnalysis: CostAnalysisFinancialData? = null,
    // Legacy fields for backward compatibility
    val summary: FinancialSummary? = null,
    val profitAnalysis: ProfitAnalysis? = null,
    val cashFlow: CashFlowAnalysis? = null,
    val trends: List<FinancialTrend>? = null
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

// New Comprehensive Financial Report Data Models

// Tax Analysis Data
@Serializable
data class TaxAnalysisData(
    val complianceMetrics: TaxComplianceMetrics? = null,
    val taxByCategory: List<TaxByCategoryItem>? = null,
    val taxSummary: TaxSummaryData? = null,
    val taxRateBreakdown: List<TaxRateBreakdownItem>? = null,
    val dailyTaxTrends: List<DailyTaxTrendItem>? = null
)

@Serializable
data class TaxComplianceMetrics(
    val totalTransactions: Long? = null,
    val taxComplianceRate: Double? = null,
    val taxableTransactions: Long? = null,
    val averageTaxRate: Double? = null
)

@Serializable
data class TaxByCategoryItem(
    val effectiveTaxRate: Double? = null,
    val taxCollected: Double? = null,
    val totalRevenue: Double? = null,
    val transactionCount: Long? = null,
    val categoryName: String? = null
)

@Serializable
data class TaxSummaryData(
    val taxAsPercentageOfRevenue: Double? = null,
    val effectiveTaxRate: Double? = null,
    val totalTaxCollected: Double? = null,
    val totalRevenue: Double? = null,
    val totalTaxableRevenue: Double? = null
)

@Serializable
data class TaxRateBreakdownItem(
    val taxRate: Double? = null,
    val taxCollected: Double? = null,
    val taxPercentage: Double? = null,
    val revenuePercentage: Double? = null,
    val transactionCount: Long? = null,
    val totalRevenue: Double? = null
)

@Serializable
data class DailyTaxTrendItem(
    val date: String? = null,
    val revenue: Double? = null,
    val avgTaxPerTransaction: Double? = null,
    val taxCollected: Double? = null,
    val transactionCount: Long? = null
)

// Profit Margin Analysis Data
@Serializable
data class ProfitMarginAnalysisData(
    val overallMargins: OverallMarginsData? = null,
    val marginVariance: MarginVarianceData? = null,
    val bottomPerformingProducts: List<ProductMarginItem>? = null,
    val topPerformingProducts: List<ProductMarginItem>? = null,
    val productMargins: List<ProductMarginItem>? = null,
    val categoryMargins: List<CategoryMarginItem>? = null
)

@Serializable
data class OverallMarginsData(
    val totalTax: Double? = null,
    val totalDiscounts: Double? = null,
    val netMarginPercentage: Double? = null,
    val totalRevenue: Double? = null,
    val totalShipping: Double? = null,
    val totalCost: Double? = null,
    val grossProfit: Double? = null,
    val netProfit: Double? = null,
    val grossMarginPercentage: Double? = null
)

@Serializable
data class MarginVarianceData(
    val maxMargin: Double? = null,
    val marginRange: Double? = null,
    val minMargin: Double? = null,
    val averageMargin: Double? = null
)

@Serializable
data class ProductMarginItem(
    val totalQuantitySold: Long? = null,
    val profitMarginPercentage: Double? = null,
    val productSku: String? = null,
    val salesCount: Long? = null,
    val profitPerUnit: Double? = null,
    val totalRevenue: Double? = null,
    val categoryName: String? = null,
    val productName: String? = null,
    val totalCost: Double? = null,
    val avgUnitPrice: Double? = null,
    val grossProfit: Double? = null
)

@Serializable
data class CategoryMarginItem(
    val profitMarginPercentage: Double? = null,
    val totalQuantitySold: Long? = null,
    val salesCount: Long? = null,
    val totalRevenue: Double? = null,
    val categoryName: String? = null,
    val totalCost: Double? = null,
    val grossProfit: Double? = null
)

// Financial Revenue Analysis Data (renamed to avoid conflict with existing RevenueAnalysis)
@Serializable
data class FinancialRevenueAnalysisData(
    val summary: FinancialRevenueSummary? = null,
    val revenueByCategory: List<RevenueByCategoryItem>? = null,
    val growthMetrics: RevenueGrowthMetrics? = null,
    val dailyTrends: List<FinancialDailyTrendItem>? = null
)

@Serializable
data class FinancialRevenueSummary(
    val totalTransactions: Long? = null,
    val netRevenue: Double? = null,
    val totalDiscounts: Double? = null,
    val averageOrderValue: Double? = null,
    val revenuePerCustomer: Double? = null,
    val grossRevenue: Double? = null,
    val netProfit: Double? = null,
    val totalTax: Double? = null,
    val totalRevenue: Double? = null,
    val totalShipping: Double? = null,
    val grossProfit: Double? = null,
    val totalCost: Double? = null,
    val uniqueCustomers: Long? = null
)

@Serializable
data class RevenueByCategoryItem(
    val totalQuantitySold: Long? = null,
    val salesCount: Long? = null,
    val profitMargin: Double? = null,
    val revenuePercentage: Double? = null,
    val totalRevenue: Double? = null,
    val categoryName: String? = null,
    val totalCost: Double? = null,
    val avgUnitPrice: Double? = null,
    val grossProfit: Double? = null
)

@Serializable
data class RevenueGrowthMetrics(
    val revenueGrowthPercentage: Double? = null,
    val currentPeriodRevenue: Double? = null,
    val previousPeriodRevenue: Double? = null
)

@Serializable
data class FinancialDailyTrendItem(
    val date: String? = null,
    val salesCount: Long? = null,
    val revenue: Double? = null,
    val cost: Double? = null,
    val profit: Double? = null,
    val avgOrderValue: Double? = null
)

// Executive Summary Data
@Serializable
data class ExecutiveSummaryData(
    val periodInformation: PeriodInformationData? = null,
    val insights: List<String>? = null,
    val keyPerformanceIndicators: KeyPerformanceIndicatorsData? = null,
    val recommendations: List<String>? = null
)

@Serializable
data class PeriodInformationData(
    val endDate: String? = null,
    val avgDailyRevenue: Double? = null,
    val periodDays: Int? = null,
    val startDate: String? = null
)

@Serializable
data class KeyPerformanceIndicatorsData(
    val totalTransactions: Long? = null,
    val totalRevenue: Double? = null,
    val grossMargin: Double? = null,
    val revenuePerCustomer: Double? = null,
    val grossProfit: Double? = null,
    val avgOrderValue: Double? = null,
    val uniqueCustomers: Long? = null
)

// Advanced Metrics Data
@Serializable
data class AdvancedMetricsData(
    val customerSegmentation: FinancialCustomerSegmentation? = null,
    val topCustomersByRevenue: List<TopCustomerByRevenueItem>? = null,
    val conversionMetrics: ConversionMetricsData? = null,
    val seasonalAnalysis: SeasonalAnalysisData? = null
)

@Serializable
data class FinancialCustomerSegmentation(
    val avgRevenuePerCustomer: Double? = null,
    val totalCustomers: Long? = null,
    val top20PercentCustomers: Long? = null,
    val totalRevenue: Double? = null,
    val paretoRatio: Double? = null,
    val top20PercentRevenue: Double? = null
)

@Serializable
data class TopCustomerByRevenueItem(
    val lastPurchaseDate: String? = null,
    val customerType: String? = null,
    val customerId: Long? = null,
    val totalOrders: Long? = null,
    val totalRevenue: Double? = null,
    val revenuePerOrder: Double? = null,
    val customerName: String? = null,
    val avgOrderValue: Double? = null
)

@Serializable
data class ConversionMetricsData(
    val repeatCustomerRate: Double? = null,
    val repeatCustomers: Long? = null,
    val totalSales: Long? = null,
    val uniqueCustomers: Long? = null,
    val salesPerCustomer: Double? = null
)

@Serializable
data class SeasonalAnalysisData(
    val dayOfWeekPatterns: List<DayOfWeekPatternItem>? = null,
    val monthlyPatterns: List<MonthlyPatternItem>? = null
)

@Serializable
data class DayOfWeekPatternItem(
    val salesCount: Long? = null,
    val revenue: Double? = null,
    val dayOfWeek: String? = null,
    val avgOrderValue: Double? = null
)

@Serializable
data class MonthlyPatternItem(
    val salesCount: Long? = null,
    val revenue: Double? = null,
    val month: String? = null,
    val avgOrderValue: Double? = null
)

// Payment Method Analysis Data
@Serializable
data class PaymentMethodAnalysisData(
    val summary: PaymentMethodSummaryData? = null,
    val preferences: PaymentMethodPreferencesData? = null,
    val paymentMethodBreakdown: List<PaymentMethodBreakdownItem>? = null,
    val trends: PaymentMethodTrendsData? = null
)

@Serializable
data class PaymentMethodSummaryData(
    val totalTransactions: Long? = null,
    val overallAvgTransactionValue: Double? = null,
    val uniquePaymentMethods: Long? = null,
    val totalRevenue: Double? = null
)

@Serializable
data class PaymentMethodPreferencesData(
    val mostPopularByTransactions: PaymentMethodPreferenceItem? = null,
    val highestAvgTransactionValue: PaymentMethodPreferenceItem? = null,
    val highestRevenueMethod: PaymentMethodPreferenceItem? = null
)

@Serializable
data class PaymentMethodPreferenceItem(
    val paymentMethod: String? = null,
    val revenuePercentage: Double? = null,
    val transactionCount: Long? = null,
    val totalRevenue: Double? = null,
    val avgTransactionValue: Double? = null,
    val transactionPercentage: Double? = null
)

@Serializable
data class PaymentMethodBreakdownItem(
    val paymentMethod: String? = null,
    val revenuePercentage: Double? = null,
    val transactionCount: Long? = null,
    val totalRevenue: Double? = null,
    val avgTransactionValue: Double? = null,
    val transactionPercentage: Double? = null
)

@Serializable
data class PaymentMethodTrendsData(
    val paymentMethodGrowth: List<PaymentMethodGrowthItem>? = null
)

@Serializable
data class PaymentMethodGrowthItem(
    val previousRevenue: Double? = null,
    val paymentMethod: String? = null,
    val currentRevenue: Double? = null,
    val growthPercentage: Double? = null
)

// Cost Analysis Financial Data (renamed to avoid conflict with existing CostAnalysisData)
@Serializable
data class CostAnalysisFinancialData(
    val efficiencyMetrics: CostEfficiencyMetrics? = null,
    val costByCategory: List<CostByCategoryItem>? = null,
    val costSummary: CostSummaryData? = null,
    val leastCostEfficientProducts: List<CostEfficientProductItem>? = null,
    val costPerSale: CostPerSaleData? = null,
    val mostCostEfficientProducts: List<CostEfficientProductItem>? = null
)

@Serializable
data class CostEfficiencyMetrics(
    val averageProfitMargin: Double? = null,
    val dailyCostEfficiency: List<DailyCostEfficiencyItem>? = null,
    val averageCostRatio: Double? = null
)

@Serializable
data class DailyCostEfficiencyItem(
    val date: String? = null,
    val revenue: Double? = null,
    val cost: Double? = null,
    val costRatio: Double? = null,
    val profitMargin: Double? = null,
    val profit: Double? = null
)

@Serializable
data class CostByCategoryItem(
    val totalQuantitySold: Long? = null,
    val salesCount: Long? = null,
    val costRatio: Double? = null,
    val profitMargin: Double? = null,
    val totalRevenue: Double? = null,
    val categoryName: String? = null,
    val totalCost: Double? = null,
    val grossProfit: Double? = null
)

@Serializable
data class CostSummaryData(
    val totalShippingCosts: Double? = null,
    val operationalCostPercentage: Double? = null,
    val totalCOGS: Double? = null,
    val totalCosts: Double? = null,
    val totalDiscounts: Double? = null,
    val cogsPercentage: Double? = null,
    val totalRevenue: Double? = null,
    val totalOperationalCosts: Double? = null,
    val grossProfit: Double? = null
)

@Serializable
data class CostEfficientProductItem(
    val productSku: String? = null,
    val costRatio: Double? = null,
    val profitMargin: Double? = null,
    val totalRevenue: Double? = null,
    val productName: String? = null,
    val totalCost: Double? = null
)

@Serializable
data class CostPerSaleData(
    val avgRevenuePerSale: Double? = null,
    val avgCOGSPerSale: Double? = null,
    val avgShippingPerSale: Double? = null,
    val totalSales: Long? = null,
    val avgTotalCostPerSale: Double? = null,
    val avgProfitPerSale: Double? = null
)

// Backward compatibility computed properties for FinancialReportDTO
val FinancialReportDTO.computedSummary: FinancialSummary
    get() = summary ?: FinancialSummary(
        totalRevenue = revenueAnalysis?.summary?.totalRevenue ?: 0.0,
        totalCosts = costAnalysis?.costSummary?.totalCosts ?: 0.0,
        grossProfit = revenueAnalysis?.summary?.grossProfit ?: 0.0,
        netProfit = revenueAnalysis?.summary?.netProfit ?: 0.0,
        profitMargin = profitMarginAnalysis?.overallMargins?.grossMarginPercentage ?: 0.0,
        roi = 0.0 // Not available in new structure
    )

val FinancialReportDTO.computedRevenueAnalysis: RevenueAnalysis
    get() = RevenueAnalysis(
        totalRevenue = revenueAnalysis?.summary?.totalRevenue ?: 0.0,
        revenueByChannel = paymentMethodAnalysis?.paymentMethodBreakdown?.associate { method ->
            (method.paymentMethod ?: "Unknown") to (method.totalRevenue ?: 0.0)
        } ?: emptyMap(),
        revenueByProduct = emptyMap(), // Not available in new structure
        revenueGrowth = revenueAnalysis?.growthMetrics?.revenueGrowthPercentage ?: 0.0,
        seasonalTrends = revenueAnalysis?.dailyTrends?.map { trend ->
            SeasonalTrend(
                period = trend.date ?: "",
                revenue = trend.revenue ?: 0.0,
                seasonalIndex = 1.0 // Default value
            )
        } ?: emptyList()
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

// Extension functions for ReportMetadata to safely access filter data
fun ReportMetadata.getFilterBoolean(key: String): Boolean? {
    return try {
        appliedFilters?.jsonObject?.get(key)?.jsonPrimitive?.content?.toBoolean()
    } catch (e: Exception) {
        null
    }
}

fun ReportMetadata.getFilterInt(key: String): Int? {
    return try {
        appliedFilters?.jsonObject?.get(key)?.jsonPrimitive?.content?.toInt()
    } catch (e: Exception) {
        null
    }
}

fun ReportMetadata.getFilterString(key: String): String? {
    return try {
        appliedFilters?.jsonObject?.get(key)?.jsonPrimitive?.content
    } catch (e: Exception) {
        null
    }
}

fun ReportMetadata.getFilterLong(key: String): Long? {
    return try {
        appliedFilters?.jsonObject?.get(key)?.jsonPrimitive?.content?.toLong()
    } catch (e: Exception) {
        null
    }
}

// Helper to get all filter keys for debugging
fun ReportMetadata.getFilterKeys(): Set<String> {
    return try {
        appliedFilters?.jsonObject?.keys ?: emptySet()
    } catch (e: Exception) {
        emptySet()
    }
}

// Helper to get pagination information
fun ReportMetadata.getPaginationInt(key: String): Int? {
    return try {
        pagination?.jsonObject?.get(key)?.jsonPrimitive?.content?.toInt()
    } catch (e: Exception) {
        null
    }
}

// Extension functions for EnhancedInventoryReportDTO backward compatibility
val EnhancedInventoryReportDTO.computedSummary: InventorySummary
    get() = summary ?: InventorySummary(
        totalProducts = 0L,
        totalInventoryValue = 0.0,
        lowStockItems = 0L,
        outOfStockItems = 0L,
        overStockItems = 0L,
        averageTurnoverRate = 0.0
    )

val EnhancedInventoryReportDTO.computedStockAlerts: List<StockAlert>
    get() = stockAlerts ?: emptyList()

val EnhancedInventoryReportDTO.computedTurnoverAnalysis: List<ProductTurnover>
    get() = turnoverAnalysis ?: emptyList()

val EnhancedInventoryReportDTO.computedValuation: InventoryValuation?
    get() = valuation

val EnhancedInventoryReportDTO.computedCategoryBreakdown: List<CategoryInventoryAnalysis>
    get() = categoryBreakdown ?: emptyList()

val EnhancedInventoryReportDTO.computedWarehouseAnalysis: List<WarehouseAnalysis>
    get() = emptyList() // Return empty list since backend returns object instead of array

// Helper functions to extract data from JsonElement fields
fun EnhancedInventoryReportDTO.getStockLevelDouble(key: String): Double? {
    return try {
        stockLevels?.jsonObject?.get(key)?.jsonPrimitive?.doubleOrNull
    } catch (e: Exception) {
        null
    }
}

fun EnhancedInventoryReportDTO.getLowStockAlertInt(key: String): Int? {
    return try {
        lowStockAlerts?.jsonObject?.get(key)?.jsonPrimitive?.content?.toInt()
    } catch (e: Exception) {
        null
    }
}

fun EnhancedInventoryReportDTO.getInventoryValuationDouble(key: String): Double? {
    return try {
        inventoryValuation?.jsonObject?.get(key)?.jsonPrimitive?.doubleOrNull
    } catch (e: Exception) {
        null
    }
}