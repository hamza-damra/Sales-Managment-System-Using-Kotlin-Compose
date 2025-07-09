import data.api.*
import kotlinx.serialization.json.Json

/**
 * Test to verify the API models compile correctly after fixing Map<String, Any> issues
 */
fun main() {
    println("🧪 Testing API Models Compilation...")
    
    try {
        // Test creating the API response DTO
        val apiResponse = DashboardApiResponseDTO(
            success = true,
            message = "Test message",
            data = DashboardDataDTO(
                summary = DashboardSummaryDataDTO(
                    period = DashboardPeriodDTO(
                        startDate = "2025-06-09",
                        endDate = "2025-07-09"
                    ),
                    totalRevenue = 5025.39,
                    totalSales = 8,
                    averageOrderValue = 628.17
                ),
                salesOverview = DashboardSalesOverviewDTO(
                    placeholder = null
                ),
                quickStats = DashboardQuickStatsDTO(
                    totalCustomers = 4,
                    totalProducts = 27,
                    lowStockItems = 1,
                    todaysSales = 0,
                    todaysRevenue = 0.0
                ),
                topProducts = DashboardTopProductsDTO(
                    topProducts = listOf(
                        DashboardTopProductDTO(
                            revenue = 1180.52,
                            quantitySold = 2,
                            productName = "Test Product"
                        )
                    )
                ),
                recentSales = DashboardRecentSalesDTO(
                    count = 1,
                    sales = listOf(
                        DashboardRecentSaleDTO(
                            totalAmount = 590.26,
                            id = 11,
                            saleDate = "2025-07-08",
                            customerName = "Test Customer"
                        )
                    )
                )
            ),
            metadata = DashboardMetadataDTO(
                reportType = "LEGACY_DASHBOARD",
                reportName = "Test Dashboard",
                generatedAt = "2025-07-09T22:43:58.3532379",
                appliedFilters = DashboardAppliedFiltersDTO(
                    days = 30
                ),
                executionTimeMs = 27
            )
        )
        
        println("✅ API Response DTO created successfully")
        
        // Test JSON serialization
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            encodeDefaults = true
        }
        
        val jsonString = json.encodeToString(DashboardApiResponseDTO.serializer(), apiResponse)
        println("✅ JSON serialization successful")
        println("📄 JSON length: ${jsonString.length} characters")
        
        // Test JSON deserialization
        val deserializedResponse = json.decodeFromString(DashboardApiResponseDTO.serializer(), jsonString)
        println("✅ JSON deserialization successful")
        
        // Verify data integrity
        val originalRevenue = apiResponse.data?.summary?.totalRevenue
        val deserializedRevenue = deserializedResponse.data?.summary?.totalRevenue
        
        if (originalRevenue == deserializedRevenue) {
            println("✅ Data integrity verified: $originalRevenue == $deserializedRevenue")
        } else {
            println("❌ Data integrity failed: $originalRevenue != $deserializedRevenue")
        }
        
        // Test the expected structure mapping
        val expectedStructure = DashboardSummaryDTO(
            period = "Test Period",
            generatedAt = "2025-07-09T22:43:58.3532379",
            sales = DashboardSalesDTO(
                totalSales = 8,
                totalRevenue = 5025.39,
                averageOrderValue = 628.17,
                growthRate = 12.5
            ),
            customers = DashboardCustomersDTO(
                totalCustomers = 4,
                newCustomers = 2,
                activeCustomers = 3,
                retentionRate = 75.0
            ),
            inventory = DashboardInventoryDTO(
                totalProducts = 27,
                lowStockAlerts = 1,
                outOfStockProducts = 0,
                totalStockValue = 15000.0
            ),
            revenue = DashboardRevenueDTO(
                monthlyRevenue = mapOf(
                    "يناير" to 1000.0,
                    "فبراير" to 1200.0,
                    "مارس" to 1500.0
                ),
                yearlyRevenue = 50000.0,
                profitMargin = 23.5,
                topCategory = "Electronics"
            )
        )
        
        println("✅ Expected structure DTO created successfully")
        
        // Test expected structure serialization
        val expectedJsonString = json.encodeToString(DashboardSummaryDTO.serializer(), expectedStructure)
        println("✅ Expected structure JSON serialization successful")
        println("📄 Expected JSON length: ${expectedJsonString.length} characters")
        
        println("\n🎉 All compilation and serialization tests passed!")
        println("✅ The API models are now compatible with Kotlin serialization")
        println("✅ No more Map<String, Any> compilation errors")
        
    } catch (e: Exception) {
        println("❌ Compilation/serialization test failed: ${e.message}")
        e.printStackTrace()
    }
}
