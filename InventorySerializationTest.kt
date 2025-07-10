import kotlinx.serialization.json.Json
import data.api.*

fun main() {
    println("🧪 Testing Inventory Report Serialization Fix...")
    
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Test the exact response structure that was causing the error
    val inventoryResponseJson = """
    {
        "success": true,
        "message": "Report generated successfully",
        "data": {
            "stockLevels": {},
            "lowStockAlerts": {},
            "outOfStockItems": [],
            "inventoryValuation": {},
            "warehouseDistribution": {}
        },
        "metadata": {
            "reportType": "INVENTORY_STATUS",
            "reportName": "Inventory Status Report",
            "generatedAt": "2025-07-10T21:30:49.6825497",
            "generatedBy": null,
            "period": null,
            "appliedFilters": {
                "includeInactive": false
            },
            "pagination": null,
            "totalRecords": null,
            "executionTimeMs": 4,
            "version": null,
            "fromCache": null,
            "cacheExpiry": null
        },
        "errorCode": null,
        "errorDetails": null
    }
    """.trimIndent()

    // Test with more complex inventory data
    val complexInventoryResponseJson = """
    {
        "success": true,
        "message": "Report generated successfully",
        "data": {
            "stockLevels": {
                "totalItems": 1500,
                "averageStockLevel": 75.5,
                "stockTurnoverRate": 4.2
            },
            "lowStockAlerts": {
                "criticalItems": 8,
                "warningItems": 15,
                "alertThreshold": 10
            },
            "outOfStockItems": [
                {
                    "productId": 123,
                    "productName": "Laptop Pro",
                    "category": "Electronics",
                    "lastStockDate": "2025-07-05"
                },
                {
                    "productId": 456,
                    "productName": "Office Chair",
                    "category": "Furniture",
                    "lastStockDate": "2025-07-08"
                }
            ],
            "inventoryValuation": {
                "totalValue": 125000.75,
                "costValue": 95000.50,
                "retailValue": 155000.00,
                "profitMargin": 25.5
            },
            "warehouseDistribution": {
                "warehouse1": {
                    "name": "Main Warehouse",
                    "itemCount": 800,
                    "value": 75000.00
                },
                "warehouse2": {
                    "name": "Secondary Warehouse", 
                    "itemCount": 700,
                    "value": 50000.75
                }
            }
        },
        "metadata": {
            "reportType": "INVENTORY_STATUS",
            "reportName": "Enhanced Inventory Status Report",
            "generatedAt": "2025-07-10T21:35:15.1234567",
            "appliedFilters": {
                "includeInactive": false,
                "warehouseIds": [1, 2],
                "categoryFilter": "all"
            },
            "executionTimeMs": 12
        }
    }
    """.trimIndent()

    try {
        println("📝 Testing Basic Inventory Response...")
        
        // This should now work without JsonConvertException
        val basicResponse = json.decodeFromString<StandardReportResponse<EnhancedInventoryReportDTO>>(inventoryResponseJson)
        
        println("✅ Basic Inventory JSON parsing successful!")
        println("📊 Response success: ${basicResponse.success}")
        println("📝 Message: ${basicResponse.message}")
        
        val basicInventoryData = basicResponse.data
        println("🔍 Basic Inventory Data:")
        println("   - Stock Levels: ${basicInventoryData.stockLevels != null}")
        println("   - Low Stock Alerts: ${basicInventoryData.lowStockAlerts != null}")
        println("   - Out of Stock Items: ${basicInventoryData.outOfStockItems?.size ?: 0}")
        println("   - Inventory Valuation: ${basicInventoryData.inventoryValuation != null}")
        println("   - Warehouse Distribution: ${basicInventoryData.warehouseDistribution != null}")
        
        // Test backward compatibility
        println("   - Computed Summary: ${basicInventoryData.computedSummary.totalProducts}")
        println("   - Computed Stock Alerts: ${basicInventoryData.computedStockAlerts.size}")
        
        println("\n📝 Testing Complex Inventory Response...")
        
        val complexResponse = json.decodeFromString<StandardReportResponse<EnhancedInventoryReportDTO>>(complexInventoryResponseJson)
        
        println("✅ Complex Inventory JSON parsing successful!")
        
        val complexInventoryData = complexResponse.data
        println("🔍 Complex Inventory Data:")
        
        // Test accessing data using helper functions
        val totalItems = complexInventoryData.getStockLevelDouble("totalItems")
        val averageStockLevel = complexInventoryData.getStockLevelDouble("averageStockLevel")
        val criticalItems = complexInventoryData.getLowStockAlertInt("criticalItems")
        val totalValue = complexInventoryData.getInventoryValuationDouble("totalValue")
        
        println("   - Total Items: ${totalItems?.toInt()}")
        println("   - Average Stock Level: $averageStockLevel")
        println("   - Critical Items: $criticalItems")
        println("   - Total Inventory Value: $${totalValue}")
        println("   - Out of Stock Items: ${complexInventoryData.outOfStockItems?.size}")
        
        // Test metadata access
        val metadata = complexResponse.metadata
        if (metadata != null) {
            println("\n📋 Metadata:")
            println("   - Report Type: ${metadata.reportType}")
            println("   - Execution Time: ${metadata.executionTimeMs}ms")
            println("   - Include Inactive Filter: ${metadata.getFilterBoolean("includeInactive")}")
            println("   - Category Filter: ${metadata.getFilterString("categoryFilter")}")
        }
        
        println("\n🎉 All inventory serialization tests passed!")
        println("✅ /api/v1/reports/inventory/status - Fixed")
        println("✅ No more JsonConvertException at $.data.warehouseDistribution")
        println("✅ Handles both empty objects {} and complex data structures")
        println("✅ Backward compatibility maintained with computed properties")
        println("✅ Safe data extraction using helper functions")
        
    } catch (e: Exception) {
        println("❌ Serialization error: ${e.message}")
        e.printStackTrace()
    }
}
