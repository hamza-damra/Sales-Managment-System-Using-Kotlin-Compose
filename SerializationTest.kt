import kotlinx.serialization.json.Json
import data.api.*

fun main() {
    println("🧪 Testing Kotlin Serialization Compatibility...")
    
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Test minimal JSON to ensure no serialization errors
    val minimalJson = """
    {
        "success": true,
        "message": "Test",
        "data": {
            "customerSegmentation": {
                "totalCustomers": 100
            },
            "behaviorAnalysis": {
                "purchasePatterns": {
                    "averageOrdersPerMonth": 2.3,
                    "preferredDayOfWeek": "Friday",
                    "complexData": {
                        "nested": true,
                        "values": [1, 2, 3]
                    }
                },
                "engagementMetrics": {
                    "emailOpenRate": 25.5,
                    "clickThroughRate": 3.2
                }
            }
        }
    }
    """.trimIndent()

    try {
        println("📝 Parsing JSON...")
        val response = json.decodeFromString<StandardReportResponse<CustomerReportDTO>>(minimalJson)
        
        println("✅ JSON parsing successful!")
        println("📊 Success: ${response.success}")
        println("📝 Message: ${response.message}")
        
        val customerReport = response.data
        println("🔍 Customer Segmentation: ${customerReport.customerSegmentation != null}")
        println("🔍 Behavior Analysis: ${customerReport.behaviorAnalysis != null}")
        
        // Test that JsonElement can handle complex data
        val behaviorData = customerReport.behaviorAnalysis
        if (behaviorData?.purchasePatterns != null) {
            println("✅ Purchase patterns JsonElement handled successfully")
        }
        
        // Test backward compatibility
        println("🔄 Total Customers (backward compatibility): ${customerReport.summary.totalCustomers}")
        
        println("\n🎉 All serialization tests passed!")
        
    } catch (e: Exception) {
        println("❌ Serialization error: ${e.message}")
        e.printStackTrace()
    }
}
