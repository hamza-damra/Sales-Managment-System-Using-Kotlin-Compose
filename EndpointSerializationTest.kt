import kotlinx.serialization.json.Json
import data.api.*

fun main() {
    println("🧪 Testing Affected Endpoints Serialization...")
    
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Test the exact response structure that was causing issues
    println("📝 Testing /api/v1/reports/customers/analytics endpoint...")
    
    val customerAnalyticsJson = """
    {
        "success": true,
        "message": "Report generated successfully",
        "data": {
            "customerSegmentation": {
                "segments": [
                    {
                        "segmentName": "Premium",
                        "customerCount": 150,
                        "averageValue": 2500.0,
                        "totalRevenue": 375000.0,
                        "percentage": 15.0
                    }
                ],
                "totalCustomers": 1000,
                "segmentDistribution": {
                    "Premium": 15.0,
                    "Regular": 85.0
                }
            },
            "acquisitionMetrics": {
                "newCustomersThisMonth": 45,
                "acquisitionCost": 125.50,
                "acquisitionChannels": {
                    "Online": 30,
                    "Referral": 15
                },
                "conversionRate": 12.5
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
            "executionTimeMs": 2
        }
    }
    """.trimIndent()

    try {
        val customerResponse = json.decodeFromString<StandardReportResponse<CustomerReportDTO>>(customerAnalyticsJson)
        println("✅ Customer Analytics endpoint deserialization successful!")
        
        // Test accessing the data
        val customerData = customerResponse.data
        println("   - Total Customers: ${customerData.summary.totalCustomers}")
        println("   - Segments: ${customerData.segments.size}")
        
        // Test accessing metadata
        val metadata = customerResponse.metadata
        if (metadata != null) {
            println("   - Report Type: ${metadata.reportType}")
            println("   - Include Inactive Filter: ${metadata.getFilterBoolean("includeInactive")}")
            println("   - Months Filter: ${metadata.getFilterInt("months")}")
        }
        
    } catch (e: Exception) {
        println("❌ Customer Analytics failed: ${e.message}")
        return
    }

    println("\n📝 Testing /api/v1/reports/products/performance endpoint...")
    
    val productPerformanceJson = """
    {
        "success": true,
        "message": "Report generated successfully",
        "data": {
            "summary": {
                "totalProducts": 500,
                "activeProducts": 450,
                "totalRevenue": 125000.75,
                "averagePrice": 250.0,
                "topCategory": "Electronics"
            },
            "productRankings": {
                "topProducts": [
                    {
                        "productId": 1,
                        "productName": "Laptop Pro",
                        "category": "Electronics",
                        "quantitySold": 150,
                        "revenue": 75000.0,
                        "profitMargin": 25.5,
                        "growthRate": 12.3,
                        "ranking": 1
                    }
                ]
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
                "includeInactive": true,
                "sortBy": "revenue",
                "sortDirection": "DESC"
            },
            "pagination": {
                "page": 0,
                "size": 50,
                "totalElements": 500,
                "totalPages": 10
            },
            "totalRecords": 500,
            "executionTimeMs": 15,
            "fromCache": false
        }
    }
    """.trimIndent()

    try {
        val productResponse = json.decodeFromString<StandardReportResponse<ProductReportDTO>>(productPerformanceJson)
        println("✅ Product Performance endpoint deserialization successful!")
        
        // Test accessing the data
        val productData = productResponse.data
        println("   - Total Products: ${productData.summary?.totalProducts}")
        println("   - Active Products: ${productData.summary?.activeProducts}")
        
        // Test accessing metadata with complex filters
        val metadata = productResponse.metadata
        if (metadata != null) {
            println("   - Report Type: ${metadata.reportType}")
            println("   - Total Records: ${metadata.totalRecords}")
            println("   - Start Date Filter: ${metadata.getFilterString("startDate")}")
            println("   - End Date Filter: ${metadata.getFilterString("endDate")}")
            println("   - Include Inactive Filter: ${metadata.getFilterBoolean("includeInactive")}")
            println("   - Sort By Filter: ${metadata.getFilterString("sortBy")}")
            println("   - Page: ${metadata.getPaginationInt("page")}")
            println("   - Size: ${metadata.getPaginationInt("size")}")
            println("   - Total Elements: ${metadata.getPaginationInt("totalElements")}")
        }
        
    } catch (e: Exception) {
        println("❌ Product Performance failed: ${e.message}")
        return
    }

    println("\n🎉 All endpoint serialization tests passed!")
    println("✅ /api/v1/reports/customers/analytics - Fixed")
    println("✅ /api/v1/reports/products/performance - Fixed")
    println("✅ No more JsonConvertException at metadata.appliedFilters")
    println("✅ No more JsonConvertException at metadata.pagination")
    println("✅ Safe access to filter and pagination data via extension functions")
    
    println("\n📋 Summary of Changes:")
    println("   - appliedFilters: String? → JsonElement?")
    println("   - pagination: String? → JsonElement?")
    println("   - Added extension functions for safe data access")
    println("   - Backward compatible (metadata fields are optional)")
}
