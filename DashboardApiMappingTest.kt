/**
 * Test to verify dashboard API response mapping from actual backend structure
 */
fun main() {
    println("🧪 Testing Dashboard API Response Mapping...")
    
    // Actual API response structure from the logs
    val actualApiResponse = """
    {
      "success": true,
      "message": "Report generated successfully",
      "data": {
        "summary": {
          "period": {
            "endDate": "2025-07-09",
            "startDate": "2025-06-09"
          },
          "averageOrderValue": 628.17,
          "totalRevenue": 5025.39,
          "totalSales": 8
        },
        "salesOverview": {},
        "topProducts": {
          "topProducts": [
            {
              "revenue": 1180.52,
              "quantitySold": 2,
              "productName": "ساعة ذكية Amazfit GTS 4 #1"
            },
            {
              "revenue": 299.00,
              "quantitySold": 1,
              "productName": "سماعات بلوتوث Jabra Elite 75t"
            }
          ]
        },
        "quickStats": {
          "totalCustomers": 4,
          "lowStockItems": 1,
          "totalProducts": 27,
          "todaysSales": 0,
          "todaysRevenue": 0
        },
        "recentSales": {
          "count": 8,
          "sales": [
            {
              "totalAmount": 590.26,
              "id": 11,
              "saleDate": "2025-07-08",
              "customerName": "Bob Johnson"
            }
          ]
        }
      },
      "metadata": {
        "reportType": "LEGACY_DASHBOARD",
        "reportName": "Legacy Dashboard (Default)",
        "generatedAt": "2025-07-09T22:43:58.3532379",
        "generatedBy": null,
        "period": null,
        "appliedFilters": {
          "days": 30
        },
        "pagination": null,
        "totalRecords": null,
        "executionTimeMs": 27,
        "version": null,
        "fromCache": null,
        "cacheExpiry": null
      },
      "errorCode": null,
      "errorDetails": null
    }
    """.trimIndent()
    
    println("📋 Testing with actual API response structure")
    println("📊 Expected values:")
    println("  - totalRevenue: 5025.39")
    println("  - totalSales: 8")
    println("  - averageOrderValue: 628.17")
    println("  - totalCustomers: 4")
    println("  - totalProducts: 27")
    println("  - lowStockItems: 1")
    
    // Test parsing with kotlinx.serialization
    try {
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            encodeDefaults = true
        }
        
        // Parse the API response
        val apiResponse = json.decodeFromString<data.api.DashboardApiResponseDTO>(actualApiResponse)
        println("✅ Successfully parsed API response")
        
        // Check if parsing worked
        println("📊 Parsed API Response:")
        println("  - success: ${apiResponse.success}")
        println("  - message: ${apiResponse.message}")
        println("  - data.summary.totalRevenue: ${apiResponse.data?.summary?.totalRevenue}")
        println("  - data.summary.totalSales: ${apiResponse.data?.summary?.totalSales}")
        println("  - data.quickStats.totalCustomers: ${apiResponse.data?.quickStats?.totalCustomers}")
        println("  - data.quickStats.totalProducts: ${apiResponse.data?.quickStats?.totalProducts}")
        
        // Test the mapping function
        val mappedData = mapApiResponseToExpectedStructure(apiResponse)
        
        println("\n✅ Mapping completed!")
        println("📊 Mapped Dashboard Data:")
        println("  - sales.totalRevenue: ${mappedData.sales?.totalRevenue}")
        println("  - sales.totalSales: ${mappedData.sales?.totalSales}")
        println("  - sales.averageOrderValue: ${mappedData.sales?.averageOrderValue}")
        println("  - customers.totalCustomers: ${mappedData.customers?.totalCustomers}")
        println("  - inventory.totalProducts: ${mappedData.inventory?.totalProducts}")
        println("  - inventory.lowStockAlerts: ${mappedData.inventory?.lowStockAlerts}")
        println("  - period: ${mappedData.period}")
        println("  - generatedAt: ${mappedData.generatedAt}")
        
        // Verify the mapping worked correctly
        val isCorrect = mappedData.sales?.totalRevenue == 5025.39 &&
                       mappedData.sales?.totalSales == 8 &&
                       mappedData.sales?.averageOrderValue == 628.17 &&
                       mappedData.customers?.totalCustomers == 4 &&
                       mappedData.inventory?.totalProducts == 27 &&
                       mappedData.inventory?.lowStockAlerts == 1
        
        if (isCorrect) {
            println("\n🎉 All API response mappings are correct!")
            println("✅ The dashboard should now display real data values")
        } else {
            println("\n❌ Some API response mappings are incorrect!")
            println("🔍 Check the mapping logic in mapApiResponseToExpectedStructure()")
        }
        
    } catch (e: Exception) {
        println("❌ API response parsing/mapping failed: ${e.message}")
        e.printStackTrace()
    }
}

// Copy of the mapping function for testing
private fun mapApiResponseToExpectedStructure(apiResponse: data.api.DashboardApiResponseDTO): data.api.DashboardSummaryDTO {
    println("🔄 Mapping API response to expected structure...")
    
    val data = apiResponse.data
    val summary = data?.summary
    val quickStats = data?.quickStats
    val metadata = apiResponse.metadata
    
    // Map sales data from summary
    val salesData = data.api.DashboardSalesDTO(
        totalSales = summary?.totalSales,
        totalRevenue = summary?.totalRevenue,
        averageOrderValue = summary?.averageOrderValue,
        growthRate = null,
        completedSales = null,
        pendingSales = null,
        cancelledSales = null
    )
    
    // Map customer data from quickStats
    val customersData = data.api.DashboardCustomersDTO(
        totalCustomers = quickStats?.totalCustomers,
        newCustomers = null,
        activeCustomers = null,
        retentionRate = null
    )
    
    // Map inventory data from quickStats
    val inventoryData = data.api.DashboardInventoryDTO(
        totalProducts = quickStats?.totalProducts,
        lowStockAlerts = quickStats?.lowStockItems,
        outOfStockProducts = null,
        totalStockValue = null,
        outOfStockAlerts = quickStats?.lowStockItems,
        totalValue = null
    )
    
    // Map revenue data
    val revenueData = data.api.DashboardRevenueDTO(
        monthlyRevenue = null,
        yearlyRevenue = null,
        profitMargin = null,
        topCategory = null,
        thisMonth = summary?.totalRevenue,
        lastMonth = null,
        growthRate = null
    )
    
    // Create period string
    val periodString = if (summary?.period != null) {
        "من ${summary.period.startDate} إلى ${summary.period.endDate}"
    } else {
        "آخر 30 يوم"
    }
    
    return data.api.DashboardSummaryDTO(
        period = periodString,
        generatedAt = metadata?.generatedAt,
        sales = salesData,
        customers = customersData,
        inventory = inventoryData,
        revenue = revenueData
    )
}
