import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import data.api.*

fun main() {
    println("🧪 Testing KPI Serialization Fix...")
    
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Test JSON that matches the KPI endpoint response structure
    val kpiResponseJson = """
    {
        "success": true,
        "message": "KPI data retrieved successfully",
        "data": {
            "todaysRevenue": 15750.50,
            "todaysSales": 42,
            "activeCustomers": 1250,
            "inventoryValue": 125000.75,
            "lowStockItems": 8,
            "pendingReturns": 3,
            "monthlyGrowth": 12.5,
            "customerSatisfaction": 4.7,
            "averageOrderValue": 375.25,
            "conversionRate": 8.3,
            "topSellingCategory": "Electronics",
            "lastUpdated": "2024-01-20T15:30:00Z"
        },
        "metadata": {
            "reportType": "real_time_kpis",
            "reportName": "Real-time KPI Dashboard",
            "generatedAt": "2024-01-20T15:30:00Z",
            "generatedBy": "system",
            "executionTimeMs": 125,
            "fromCache": false
        }
    }
    """.trimIndent()

    try {
        println("📝 Testing StandardReportResponse<JsonElement> deserialization...")
        
        // This should now work without serialization errors
        val response = json.decodeFromString<StandardReportResponse<JsonElement>>(kpiResponseJson)
        
        println("✅ JSON parsing successful!")
        println("📊 Response success: ${response.success}")
        println("📝 Message: ${response.message}")
        
        val kpiData = response.data
        println("🔍 KPI Data type: ${kpiData::class.simpleName}")
        
        // Test accessing KPI values using JsonElement API
        println("\n📈 Testing KPI Value Access:")
        
        // Test helper functions (simulating what the UI would do)
        val todaysRevenue = getKPIDouble(kpiData, "todaysRevenue")
        val todaysSales = getKPILong(kpiData, "todaysSales")
        val activeCustomers = getKPILong(kpiData, "activeCustomers")
        val inventoryValue = getKPIDouble(kpiData, "inventoryValue")
        val lowStockItems = getKPILong(kpiData, "lowStockItems")
        val pendingReturns = getKPILong(kpiData, "pendingReturns")
        
        println("   - Today's Revenue: $${String.format("%.2f", todaysRevenue)}")
        println("   - Today's Sales: $todaysSales")
        println("   - Active Customers: $activeCustomers")
        println("   - Inventory Value: $${String.format("%.2f", inventoryValue)}")
        println("   - Low Stock Items: $lowStockItems")
        println("   - Pending Returns: $pendingReturns")
        
        // Test metadata access
        val metadata = response.metadata
        if (metadata != null) {
            println("\n📋 Metadata:")
            println("   - Report Type: ${metadata.reportType}")
            println("   - Generated At: ${metadata.generatedAt}")
            println("   - Execution Time: ${metadata.executionTimeMs}ms")
            println("   - From Cache: ${metadata.fromCache}")
        }
        
        println("\n🎉 All KPI serialization tests passed!")
        println("✅ StandardReportResponse<JsonElement> works correctly")
        println("✅ KPI data can be safely extracted using JsonElement API")
        println("✅ No more 'Serializer for class StandardReportResponse is not found' errors")
        
    } catch (e: Exception) {
        println("❌ Serialization error: ${e.message}")
        e.printStackTrace()
    }
}

// Helper functions for safely extracting values from JsonElement (same as in ReportsScreen)
private fun getKPIDouble(kpis: JsonElement, key: String): Double {
    return try {
        kpis.jsonObject[key]?.jsonPrimitive?.doubleOrNull ?: 0.0
    } catch (e: Exception) {
        0.0
    }
}

private fun getKPILong(kpis: JsonElement, key: String): Long {
    return try {
        kpis.jsonObject[key]?.jsonPrimitive?.longOrNull ?: 0L
    } catch (e: Exception) {
        0L
    }
}

private fun getKPIString(kpis: JsonElement, key: String): String {
    return try {
        kpis.jsonObject[key]?.jsonPrimitive?.content ?: ""
    } catch (e: Exception) {
        ""
    }
}
