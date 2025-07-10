import kotlinx.serialization.json.Json
import data.api.*

fun main() {
    println("🧪 Testing ReportMetadata Serialization Fix...")
    
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Test JSON that matches the problematic backend response structure
    val customerAnalyticsResponse = """
    {
        "success": true,
        "message": "Report generated successfully",
        "data": {
            "customerSegmentation": {
                "totalCustomers": 1000
            },
            "acquisitionMetrics": {
                "newCustomersThisMonth": 45
            }
        },
        "metadata": {
            "reportType": "CUSTOMER_ANALYTICS",
            "reportName": "Customer Analytics Report",
            "generatedAt": "2025-07-10T21:24:39.2531153",
            "appliedFilters": {
                "includeInactive": false,
                "months": 12
            },
            "pagination": {
                "page": 0,
                "size": 20,
                "totalElements": 1000
            },
            "executionTimeMs": 2,
            "fromCache": false
        }
    }
    """.trimIndent()

    val productPerformanceResponse = """
    {
        "success": true,
        "message": "Report generated successfully",
        "data": {
            "summary": {
                "totalProducts": 500,
                "activeProducts": 450
            }
        },
        "metadata": {
            "reportType": "PRODUCT_PERFORMANCE",
            "reportName": "Product Performance Report",
            "generatedAt": "2025-07-10T21:25:15.1234567",
            "appliedFilters": {
                "startDate": "2024-01-01",
                "endDate": "2024-12-31",
                "categoryIds": [1, 2, 3],
                "includeInactive": true
            },
            "pagination": {
                "page": 0,
                "size": 50
            },
            "totalRecords": 500,
            "executionTimeMs": 15
        }
    }
    """.trimIndent()

    try {
        println("📝 Testing Customer Analytics Response...")
        
        // This should now work without JsonConvertException
        val customerResponse = json.decodeFromString<StandardReportResponse<CustomerReportDTO>>(customerAnalyticsResponse)
        
        println("✅ Customer Analytics JSON parsing successful!")
        println("📊 Response success: ${customerResponse.success}")
        println("📝 Message: ${customerResponse.message}")
        
        val customerMetadata = customerResponse.metadata
        if (customerMetadata != null) {
            println("\n🔍 Testing Customer Analytics Metadata:")
            println("   - Report Type: ${customerMetadata.reportType}")
            println("   - Generated At: ${customerMetadata.generatedAt}")
            println("   - Execution Time: ${customerMetadata.executionTimeMs}ms")
            
            // Test filter access using extension functions
            println("   - Include Inactive: ${customerMetadata.getFilterBoolean("includeInactive")}")
            println("   - Months: ${customerMetadata.getFilterInt("months")}")
            println("   - All Filter Keys: ${customerMetadata.getFilterKeys()}")
            
            // Test pagination access
            println("   - Page: ${customerMetadata.getPaginationInt("page")}")
            println("   - Size: ${customerMetadata.getPaginationInt("size")}")
            println("   - Total Elements: ${customerMetadata.getPaginationInt("totalElements")}")
        }
        
        println("\n📝 Testing Product Performance Response...")
        
        // This should also work without JsonConvertException
        val productResponse = json.decodeFromString<StandardReportResponse<ProductReportDTO>>(productPerformanceResponse)
        
        println("✅ Product Performance JSON parsing successful!")
        println("📊 Response success: ${productResponse.success}")
        
        val productMetadata = productResponse.metadata
        if (productMetadata != null) {
            println("\n🔍 Testing Product Performance Metadata:")
            println("   - Report Type: ${productMetadata.reportType}")
            println("   - Total Records: ${productMetadata.totalRecords}")
            
            // Test complex filter access
            println("   - Start Date: ${productMetadata.getFilterString("startDate")}")
            println("   - End Date: ${productMetadata.getFilterString("endDate")}")
            println("   - Include Inactive: ${productMetadata.getFilterBoolean("includeInactive")}")
            println("   - All Filter Keys: ${productMetadata.getFilterKeys()}")
            
            // Test pagination
            println("   - Page: ${productMetadata.getPaginationInt("page")}")
            println("   - Size: ${productMetadata.getPaginationInt("size")}")
        }
        
        println("\n🎉 All metadata serialization tests passed!")
        println("✅ appliedFilters as JsonElement works correctly")
        println("✅ pagination as JsonElement works correctly")
        println("✅ No more JsonConvertException errors")
        println("✅ Safe filter data extraction using extension functions")
        
    } catch (e: Exception) {
        println("❌ Serialization error: ${e.message}")
        e.printStackTrace()
    }
}
