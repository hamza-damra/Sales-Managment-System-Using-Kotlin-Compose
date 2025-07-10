import data.api.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Test to verify that the product analytics data models can properly deserialize
 * the API response structure, especially the List-to-Map conversion fixes.
 */
fun main() {
    val json = Json { ignoreUnknownKeys = true }
    
    // Sample API response structure based on the actual response
    val sampleApiResponse = """
    {
        "success": true,
        "message": "Report generated successfully",
        "data": {
            "productRankings": {
                "allProductMetrics": [
                    {
                        "totalQuantitySold": 2,
                        "salesCount": 2,
                        "productId": 12,
                        "totalProfit": 577.60,
                        "stockTurnover": 0.04,
                        "productName": "ساعة ذكية Amazfit GTS 4 #1",
                        "avgUnitPrice": 678.80,
                        "profitMargin": 42.5500,
                        "currentStock": 56,
                        "revenuePercentage": 81.9500,
                        "totalRevenue": 1357.60,
                        "sku": "AMAZ-GTS4-001",
                        "category": "Electronics",
                        "brand": "Amazfit",
                        "totalCost": 780.00
                    }
                ],
                "topProductsByRevenue": [
                    {
                        "productId": 12,
                        "productName": "ساعة ذكية Amazfit GTS 4 #1",
                        "category": "Electronics",
                        "quantitySold": 2,
                        "revenue": 1357.60,
                        "profit": 577.60,
                        "profitMargin": 42.5500,
                        "unitPrice": 678.80,
                        "rank": 1
                    }
                ],
                "summary": {
                    "totalProducts": 27,
                    "totalRevenue": 1656.60,
                    "totalQuantitySold": 3,
                    "avgProfitMargin": 39.6400,
                    "totalProfit": 656.60,
                    "avgUnitPrice": 552.20
                }
            },
            "profitabilityAnalysis": {
                "categoryProfitability": {
                    "Electronics": {
                        "revenue": 1656.60,
                        "profit": 656.60,
                        "itemCount": 3,
                        "cost": 1000.00,
                        "profitMargin": 39.6400
                    }
                }
            },
            "productTrends": {
                "weeklyTrends": {
                    "2025-W28": {
                        "uniqueProducts": 2,
                        "salesCount": 3,
                        "totalRevenue": 1656.60,
                        "totalQuantity": 3
                    }
                },
                "dailyTrends": {
                    "2025-07-08": {
                        "uniqueProducts": 1,
                        "salesCount": 2,
                        "totalRevenue": 1357.60,
                        "totalQuantity": 2
                    },
                    "2025-07-07": {
                        "uniqueProducts": 1,
                        "salesCount": 1,
                        "totalRevenue": 299.00,
                        "totalQuantity": 1
                    }
                }
            },
            "categoryPerformance": {
                "categoryComparison": {
                    "totalQuantitySold": 3,
                    "avgRevenuePerCategory": 1656.60,
                    "totalRevenue": 1656.60,
                    "totalCategories": 1
                }
            }
        }
    }
    """.trimIndent()
    
    try {
        println("🧪 Testing product analytics data model deserialization...")
        
        // Test the main response structure
        val response = json.decodeFromString<StandardReportResponse<ProductReportDTO>>(sampleApiResponse)
        
        println("✅ Successfully parsed StandardReportResponse")
        println("   Success: ${response.success}")
        println("   Message: ${response.message}")
        
        val productReport = response.data
        println("✅ Successfully parsed ProductReportDTO")
        
        // Test product rankings with the new List structure
        productReport.productRankings?.let { rankings ->
            println("✅ Successfully parsed ProductRankingsData")
            
            rankings.allProductMetrics?.let { metrics ->
                println("✅ Successfully parsed allProductMetrics as List<ProductAllMetricsItem>")
                println("   Items count: ${metrics.size}")
                metrics.firstOrNull()?.let { item ->
                    println("   First item: ${item.productName} - Revenue: ${item.totalRevenue}")
                }
                
                // Test the extension function
                val summary = metrics.asRankingSummary
                println("✅ Successfully converted List to ProductRankingSummary")
                println("   Total Revenue: ${summary.totalRevenue}")
                println("   Total Products: ${summary.totalProducts}")
            }
            
            rankings.topProductsByRevenue?.let { topProducts ->
                println("✅ Successfully parsed topProductsByRevenue")
                println("   Top products count: ${topProducts.size}")
            }
        }
        
        // Test profitability analysis with Map structure
        productReport.profitabilityAnalysis?.categoryProfitability?.let { categoryMap ->
            println("✅ Successfully parsed categoryProfitability as Map")
            println("   Categories: ${categoryMap.keys}")
            categoryMap["Electronics"]?.let { electronics ->
                println("   Electronics revenue: ${electronics.revenue}")
            }
        }
        
        // Test product trends with Map structures
        productReport.productTrends?.let { trends ->
            println("✅ Successfully parsed ProductTrendsData")
            
            trends.weeklyTrends?.let { weeklyMap ->
                println("✅ Successfully parsed weeklyTrends as Map")
                println("   Weeks: ${weeklyMap.keys}")
            }
            
            trends.dailyTrends?.let { dailyMap ->
                println("✅ Successfully parsed dailyTrends as Map")
                println("   Days: ${dailyMap.keys}")
            }
        }
        
        // Test category performance
        productReport.categoryPerformance?.categoryComparison?.let { comparison ->
            println("✅ Successfully parsed categoryComparison")
            println("   Total categories: ${comparison.totalCategories}")
        }
        
        println("\n🎉 All tests passed! Product analytics data models are working correctly.")
        
    } catch (e: Exception) {
        println("❌ Test failed with exception: ${e.javaClass.simpleName}")
        println("   Message: ${e.message}")
        e.printStackTrace()
    }
}
