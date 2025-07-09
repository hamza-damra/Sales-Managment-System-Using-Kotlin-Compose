/**
 * Test to verify dashboard data mapping from flat API response to nested structure
 */
fun main() {
    println("🧪 Testing Dashboard Data Mapping...")
    
    // Sample flat API response based on your description
    val sampleFlatResponse = mapOf(
        "totalRevenue" to 5025.39,
        "totalSales" to 8,
        "averageOrderValue" to 628.17,
        "totalCustomers" to 4,
        "totalProducts" to 27,
        "growthRate" to 12.5,
        "completedSales" to 7,
        "pendingSales" to 1,
        "cancelledSales" to 0,
        "newCustomers" to 2,
        "activeCustomers" to 3,
        "retentionRate" to 75.0,
        "lowStockAlerts" to 5,
        "outOfStockProducts" to 2,
        "totalStockValue" to 15000.0,
        "profitMargin" to 23.5,
        "topCategory" to "Electronics",
        "period" to "Last 30 days",
        "generatedAt" to "2025-01-09T10:30:00Z"
    )
    
    println("📋 Sample flat response:")
    sampleFlatResponse.forEach { (key, value) ->
        println("  $key: $value")
    }
    
    // Test the mapping logic
    try {
        val mappedData = mapFlatResponseToNestedStructure(sampleFlatResponse)
        
        println("\n✅ Mapping successful!")
        println("📊 Mapped Sales Data:")
        println("  - Total Sales: ${mappedData.sales?.totalSales}")
        println("  - Total Revenue: ${mappedData.sales?.totalRevenue}")
        println("  - Average Order Value: ${mappedData.sales?.averageOrderValue}")
        println("  - Growth Rate: ${mappedData.sales?.growthRate}")
        
        println("📊 Mapped Customer Data:")
        println("  - Total Customers: ${mappedData.customers?.totalCustomers}")
        println("  - New Customers: ${mappedData.customers?.newCustomers}")
        println("  - Active Customers: ${mappedData.customers?.activeCustomers}")
        println("  - Retention Rate: ${mappedData.customers?.retentionRate}")
        
        println("📊 Mapped Inventory Data:")
        println("  - Total Products: ${mappedData.inventory?.totalProducts}")
        println("  - Low Stock Alerts: ${mappedData.inventory?.lowStockAlerts}")
        println("  - Out of Stock: ${mappedData.inventory?.outOfStockProducts}")
        println("  - Total Stock Value: ${mappedData.inventory?.totalStockValue}")
        
        println("📊 Mapped Revenue Data:")
        println("  - Profit Margin: ${mappedData.revenue?.profitMargin}")
        println("  - Top Category: ${mappedData.revenue?.topCategory}")
        
        println("\n🎯 Expected vs Actual:")
        println("Expected totalRevenue: 5025.39, Got: ${mappedData.sales?.totalRevenue}")
        println("Expected totalSales: 8, Got: ${mappedData.sales?.totalSales}")
        println("Expected totalCustomers: 4, Got: ${mappedData.customers?.totalCustomers}")
        println("Expected totalProducts: 27, Got: ${mappedData.inventory?.totalProducts}")
        
        // Verify the mapping worked correctly
        val isCorrect = mappedData.sales?.totalRevenue == 5025.39 &&
                       mappedData.sales?.totalSales == 8 &&
                       mappedData.customers?.totalCustomers == 4 &&
                       mappedData.inventory?.totalProducts == 27
        
        if (isCorrect) {
            println("\n🎉 All mappings are correct!")
        } else {
            println("\n❌ Some mappings are incorrect!")
        }
        
    } catch (e: Exception) {
        println("❌ Mapping failed: ${e.message}")
        e.printStackTrace()
    }
}

// Copy of the mapping function for testing
private fun mapFlatResponseToNestedStructure(flatData: Map<String, Any>): data.api.DashboardSummaryDTO {
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
    
    // Map sales data
    val salesData = data.api.DashboardSalesDTO(
        totalSales = getIntValue("totalSales"),
        totalRevenue = getDoubleValue("totalRevenue"),
        averageOrderValue = getDoubleValue("averageOrderValue"),
        growthRate = getDoubleValue("growthRate"),
        completedSales = getIntValue("completedSales"),
        pendingSales = getIntValue("pendingSales"),
        cancelledSales = getIntValue("cancelledSales")
    )
    
    // Map customer data
    val customersData = data.api.DashboardCustomersDTO(
        totalCustomers = getIntValue("totalCustomers"),
        newCustomers = getIntValue("newCustomers"),
        activeCustomers = getIntValue("activeCustomers"),
        retentionRate = getDoubleValue("retentionRate")
    )
    
    // Map inventory data
    val inventoryData = data.api.DashboardInventoryDTO(
        totalProducts = getIntValue("totalProducts"),
        lowStockAlerts = getIntValue("lowStockAlerts"),
        outOfStockProducts = getIntValue("outOfStockProducts"),
        totalStockValue = getDoubleValue("totalStockValue"),
        outOfStockAlerts = getIntValue("outOfStockProducts"),
        totalValue = getDoubleValue("totalStockValue")
    )
    
    // Map revenue data
    val revenueData = data.api.DashboardRevenueDTO(
        monthlyRevenue = mapOf("يناير" to 0.0, "فبراير" to 0.0, "مارس" to 0.0),
        yearlyRevenue = getDoubleValue("yearlyRevenue"),
        profitMargin = getDoubleValue("profitMargin"),
        topCategory = getStringValue("topCategory"),
        thisMonth = getDoubleValue("thisMonth"),
        lastMonth = getDoubleValue("lastMonth"),
        growthRate = getDoubleValue("revenueGrowthRate")
    )
    
    return data.api.DashboardSummaryDTO(
        period = getStringValue("period") ?: "آخر 30 يوم",
        generatedAt = getStringValue("generatedAt") ?: kotlinx.datetime.Clock.System.now().toString(),
        sales = salesData,
        customers = customersData,
        inventory = inventoryData,
        revenue = revenueData
    )
}
