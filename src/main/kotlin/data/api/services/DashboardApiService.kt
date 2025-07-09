package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json

/**
 * API service for dashboard data operations
 */
class DashboardApiService(private val httpClient: HttpClient) {
    
    suspend fun getDashboardSummary(): NetworkResult<DashboardSummaryDTO> {
        println("📊 DashboardApiService - Starting getDashboardSummary API call...")

        return try {
            println("📡 Making API call to: ${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_DASHBOARD}")

            val result = safeApiCall {
                val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_DASHBOARD}")
                println("✅ API call successful, status: ${response.status}")

                // Debug: Log the raw response
                try {
                    val responseText = response.body<String>()
                    println("🔍 Raw dashboard response length: ${responseText.length} characters")
                    println("🔍 Raw dashboard response FULL: $responseText")

                    // Parse the response with lenient JSON settings
                    val json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                        encodeDefaults = true
                    }

                    // Parse the actual API response structure
                    try {
                        val apiResponse = json.decodeFromString<DashboardApiResponseDTO>(responseText)
                        println("✅ Successfully parsed API response wrapper")

                        if (apiResponse.success == true && apiResponse.data != null) {
                            println("✅ API response indicates success")

                            // Map the actual API structure to our expected structure
                            val mappedData = mapApiResponseToExpectedStructure(apiResponse)
                            println("✅ Successfully mapped API response to expected structure")
                            println("📊 Sales total: ${mappedData.sales?.totalSales}")
                            println("📊 Revenue total: ${mappedData.sales?.totalRevenue}")
                            println("📊 Customers total: ${mappedData.customers?.totalCustomers}")

                            mappedData
                        } else {
                            println("❌ API response indicates failure: ${apiResponse.message}")
                            throw Exception("API response indicates failure: ${apiResponse.message}")
                        }
                    } catch (apiParseException: Exception) {
                        println("⚠️ Failed to parse as API response structure: ${apiParseException.message}")
                        println("🔄 Attempting fallback parsing methods...")

                        // Fallback: try to parse as the old expected nested structure
                        try {
                            val parsedData = json.decodeFromString<DashboardSummaryDTO>(responseText)
                            println("✅ Successfully parsed dashboard data with old nested structure")
                            parsedData
                        } catch (nestedParseException: Exception) {
                            println("⚠️ Failed to parse as old nested structure: ${nestedParseException.message}")
                            println("🔄 Attempting to parse as flat structure and map to nested...")

                            // Try to parse as a flat structure and map it
                            try {
                                val flatData = json.decodeFromString<Map<String, Any>>(responseText)
                                println("📋 Flat data keys: ${flatData.keys}")

                                // Map flat structure to nested structure
                                mapFlatResponseToNestedStructure(flatData)
                            } catch (flatParseException: Exception) {
                                println("❌ Failed to parse as flat structure too: ${flatParseException.message}")
                                println("🔄 Falling back to mock data due to parsing failure")
                                throw Exception("Unable to parse API response in any expected format")
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Failed to parse dashboard response: ${e.message}")
                    println("❌ Exception type: ${e::class.simpleName}")
                    e.printStackTrace()
                    throw e
                }
            }

            when (result) {
                is NetworkResult.Success -> {
                    println("✅ API call returned success with real data")
                    result
                }
                is NetworkResult.Error -> {
                    println("❌ API call returned error: ${result.exception.message}")
                    println("🔄 Falling back to mock data...")
                    NetworkResult.Success(getMockDashboardSummary())
                }
                else -> {
                    println("⚠️ Unexpected result type: ${result::class.simpleName}")
                    NetworkResult.Success(getMockDashboardSummary())
                }
            }
        } catch (e: Exception) {
            println("⚠️ Exception in getDashboardSummary: ${e.message}")
            println("⚠️ Exception type: ${e::class.simpleName}")

            // Check if it's an authentication error
            val isAuthError = e.message?.contains("Authentication", ignoreCase = true) == true ||
                             e.message?.contains("401", ignoreCase = true) == true ||
                             e.message?.contains("Unauthorized", ignoreCase = true) == true

            if (isAuthError) {
                println("🔐 Authentication error detected - user needs to login")
                // Return error for authentication issues so UI can handle appropriately
                return NetworkResult.Error(e.toApiException())
            } else {
                println("🔄 Non-auth error - returning mock data as fallback...")
                e.printStackTrace()
                // Return mock data for other errors (network, server, etc.)
                NetworkResult.Success(getMockDashboardSummary())
            }
        }
    }

    /**
     * Provides mock dashboard data for development/testing when API is unavailable
     */
    private fun getMockDashboardSummary(): DashboardSummaryDTO {
        println("🎭 Generating mock dashboard data...")

        val mockData = DashboardSummaryDTO(
            period = "آخر 30 يوم (بيانات تجريبية)",
            generatedAt = kotlinx.datetime.Clock.System.now().toString(),
            sales = DashboardSalesDTO(
                totalSales = 156,
                totalRevenue = 45750.0,
                averageOrderValue = 293.27,
                growthRate = 12.5,
                completedSales = 142,
                pendingSales = 8,
                cancelledSales = 6
            ),
            customers = DashboardCustomersDTO(
                totalCustomers = 89,
                newCustomers = 12,
                activeCustomers = 67,
                retentionRate = 85.2
            ),
            inventory = DashboardInventoryDTO(
                totalProducts = 234,
                lowStockAlerts = 8,
                outOfStockProducts = 3,
                totalStockValue = 125000.0,
                outOfStockAlerts = 3,
                totalValue = 125000.0
            ),
            revenue = DashboardRevenueDTO(
                monthlyRevenue = mapOf(
                    "يناير" to 38500.0,
                    "فبراير" to 42300.0,
                    "مارس" to 45750.0,
                    "أبريل" to 41200.0,
                    "مايو" to 47800.0,
                    "يونيو" to 52100.0
                ),
                yearlyRevenue = 267650.0,
                profitMargin = 23.5,
                topCategory = "الإلكترونيات",
                thisMonth = 45750.0,
                lastMonth = 42300.0,
                growthRate = 8.2
            )
        )

        println("✅ Mock data generated successfully")
        println("📊 Mock Sales total: ${mockData.sales?.totalSales}")
        println("📊 Mock Revenue total: ${mockData.sales?.totalRevenue}")
        println("📊 Mock Customers total: ${mockData.customers?.totalCustomers}")

        return mockData
    }

    /**
     * Maps the actual API response structure to our expected DashboardSummaryDTO structure
     */
    private fun mapApiResponseToExpectedStructure(apiResponse: DashboardApiResponseDTO): DashboardSummaryDTO {
        println("🔄 Mapping API response to expected structure...")

        val data = apiResponse.data
        val summary = data?.summary
        val quickStats = data?.quickStats
        val metadata = apiResponse.metadata

        // Log the data we're working with
        println("📊 Summary data - totalRevenue: ${summary?.totalRevenue}, totalSales: ${summary?.totalSales}")
        println("📊 QuickStats data - totalCustomers: ${quickStats?.totalCustomers}, totalProducts: ${quickStats?.totalProducts}")

        // Map sales data from summary
        val salesData = DashboardSalesDTO(
            totalSales = summary?.totalSales,
            totalRevenue = summary?.totalRevenue,
            averageOrderValue = summary?.averageOrderValue,
            growthRate = null, // Not provided in current API
            completedSales = null, // Not provided in current API
            pendingSales = null, // Not provided in current API
            cancelledSales = null // Not provided in current API
        )

        // Map customer data from quickStats
        val customersData = DashboardCustomersDTO(
            totalCustomers = quickStats?.totalCustomers,
            newCustomers = null, // Not provided in current API
            activeCustomers = null, // Not provided in current API
            retentionRate = null // Not provided in current API
        )

        // Map inventory data from quickStats
        val inventoryData = DashboardInventoryDTO(
            totalProducts = quickStats?.totalProducts,
            lowStockAlerts = quickStats?.lowStockItems,
            outOfStockProducts = null, // Not provided in current API
            totalStockValue = null, // Not provided in current API
            outOfStockAlerts = quickStats?.lowStockItems, // Duplicate field
            totalValue = null // Not provided in current API
        )

        // Map revenue data (limited data available)
        val revenueData = DashboardRevenueDTO(
            monthlyRevenue = null, // Not provided in current API
            yearlyRevenue = null, // Not provided in current API
            profitMargin = null, // Not provided in current API
            topCategory = null, // Not provided in current API
            thisMonth = summary?.totalRevenue, // Use total revenue as approximation
            lastMonth = null, // Not provided in current API
            growthRate = null // Not provided in current API
        )

        // Create period string from the period object
        val periodString = if (summary?.period != null) {
            "من ${summary.period.startDate} إلى ${summary.period.endDate}"
        } else {
            "آخر 30 يوم"
        }

        val mappedData = DashboardSummaryDTO(
            period = periodString,
            generatedAt = metadata?.generatedAt,
            sales = salesData,
            customers = customersData,
            inventory = inventoryData,
            revenue = revenueData
        )

        // Log the mapped data
        println("✅ API mapping completed successfully:")
        println("📊 Mapped Sales total: ${mappedData.sales?.totalSales}")
        println("📊 Mapped Revenue total: ${mappedData.sales?.totalRevenue}")
        println("📊 Mapped Customers total: ${mappedData.customers?.totalCustomers}")
        println("📊 Mapped Products total: ${mappedData.inventory?.totalProducts}")
        println("📊 Mapped Period: ${mappedData.period}")

        return mappedData
    }

    /**
     * Maps flat API response structure to nested DTO structure
     */
    private fun mapFlatResponseToNestedStructure(flatData: Map<String, Any>): DashboardSummaryDTO {
        println("🔄 Mapping flat response to nested structure...")

        // Helper function to safely get numeric values
        fun getDoubleValue(key: String): Double? {
            return when (val value = flatData[key]) {
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull()
                else -> null
            }
        }

        fun getIntValue(key: String): Int? {
            return when (val value = flatData[key]) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            }
        }

        fun getStringValue(key: String): String? {
            return flatData[key]?.toString()
        }

        // Log all available keys for debugging
        println("📋 Available keys in response: ${flatData.keys.sorted()}")

        // Map sales data
        val salesData = DashboardSalesDTO(
            totalSales = getIntValue("totalSales"),
            totalRevenue = getDoubleValue("totalRevenue"),
            averageOrderValue = getDoubleValue("averageOrderValue"),
            growthRate = getDoubleValue("growthRate") ?: getDoubleValue("salesGrowthRate"),
            completedSales = getIntValue("completedSales"),
            pendingSales = getIntValue("pendingSales"),
            cancelledSales = getIntValue("cancelledSales")
        )

        // Map customer data
        val customersData = DashboardCustomersDTO(
            totalCustomers = getIntValue("totalCustomers"),
            newCustomers = getIntValue("newCustomers"),
            activeCustomers = getIntValue("activeCustomers"),
            retentionRate = getDoubleValue("retentionRate") ?: getDoubleValue("customerRetentionRate")
        )

        // Map inventory data
        val inventoryData = DashboardInventoryDTO(
            totalProducts = getIntValue("totalProducts"),
            lowStockAlerts = getIntValue("lowStockAlerts"),
            outOfStockProducts = getIntValue("outOfStockProducts"),
            totalStockValue = getDoubleValue("totalStockValue"),
            outOfStockAlerts = getIntValue("outOfStockProducts"), // Duplicate field
            totalValue = getDoubleValue("totalStockValue") // Duplicate field
        )

        // Map revenue data
        val revenueData = DashboardRevenueDTO(
            monthlyRevenue = extractMonthlyRevenue(flatData),
            yearlyRevenue = getDoubleValue("yearlyRevenue"),
            profitMargin = getDoubleValue("profitMargin"),
            topCategory = getStringValue("topCategory"),
            thisMonth = getDoubleValue("thisMonth") ?: getDoubleValue("currentMonthRevenue"),
            lastMonth = getDoubleValue("lastMonth") ?: getDoubleValue("previousMonthRevenue"),
            growthRate = getDoubleValue("revenueGrowthRate") ?: getDoubleValue("growthRate")
        )

        val mappedData = DashboardSummaryDTO(
            period = getStringValue("period") ?: "آخر 30 يوم",
            generatedAt = getStringValue("generatedAt") ?: kotlinx.datetime.Clock.System.now().toString(),
            sales = salesData,
            customers = customersData,
            inventory = inventoryData,
            revenue = revenueData
        )

        // Log the mapped data
        println("✅ Mapped data successfully:")
        println("📊 Sales total: ${mappedData.sales?.totalSales}")
        println("📊 Revenue total: ${mappedData.sales?.totalRevenue}")
        println("📊 Customers total: ${mappedData.customers?.totalCustomers}")
        println("📊 Products total: ${mappedData.inventory?.totalProducts}")

        return mappedData
    }

    /**
     * Extract monthly revenue data from flat response
     */
    private fun extractMonthlyRevenue(flatData: Map<String, Any>): Map<String, Double>? {
        // Look for monthly revenue data in various possible formats
        val monthlyRevenueKey = flatData.keys.find { it.contains("monthly", ignoreCase = true) && it.contains("revenue", ignoreCase = true) }

        return when (val monthlyData = flatData[monthlyRevenueKey]) {
            is Map<*, *> -> {
                monthlyData.mapNotNull { (key, value) ->
                    val keyStr = key?.toString()
                    val valueDouble = when (value) {
                        is Number -> value.toDouble()
                        is String -> value.toDoubleOrNull()
                        else -> null
                    }
                    if (keyStr != null && valueDouble != null) keyStr to valueDouble else null
                }.toMap()
            }
            else -> {
                // Create default monthly data if not available
                mapOf(
                    "يناير" to 0.0,
                    "فبراير" to 0.0,
                    "مارس" to 0.0,
                    "أبريل" to 0.0,
                    "مايو" to 0.0,
                    "يونيو" to 0.0
                )
            }
        }
    }

    suspend fun getSalesReport(
        startDate: String,
        endDate: String
    ): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_SALES}") {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }
            response.body<Map<String, Any>>()
        }
    }
    
    suspend fun getRevenueReport(months: Int = 6): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_REVENUE}") {
                parameter("months", months)
            }
            response.body<Map<String, Any>>()
        }
    }
    
    suspend fun getTopProductsReport(
        startDate: String,
        endDate: String
    ): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_TOP_PRODUCTS}") {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }
            response.body<Map<String, Any>>()
        }
    }
    
    suspend fun getCustomerAnalytics(): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_CUSTOMER_ANALYTICS}")
            response.body<Map<String, Any>>()
        }
    }
    
    suspend fun getInventoryReport(): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_INVENTORY}")
            response.body<Map<String, Any>>()
        }
    }
}
