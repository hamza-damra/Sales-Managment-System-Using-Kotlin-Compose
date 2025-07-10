import kotlinx.serialization.json.Json
import data.api.*

fun main() {
    println("🧪 Testing Compilation and Extension Properties...")
    
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val testJson = """
    {
        "success": true,
        "message": "Test",
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
                "totalCustomers": 1000
            },
            "acquisitionMetrics": {
                "newCustomersThisMonth": 45,
                "acquisitionCost": 125.50,
                "acquisitionChannels": {
                    "Online": 30,
                    "Referral": 15
                },
                "conversionRate": 12.5
            },
            "lifetimeValueAnalysis": {
                "topCustomers": [
                    {
                        "customerId": 1,
                        "customerName": "أحمد محمد",
                        "email": "ahmed@example.com",
                        "totalValue": 15000.0,
                        "averageOrderValue": 500.0,
                        "orderFrequency": 2.5,
                        "lastOrderDate": "2024-01-15",
                        "predictedValue": 18000.0,
                        "segment": "Premium"
                    }
                ],
                "averageLifetimeValue": 2500.0
            },
            "churnAnalysis": {
                "churnRate": 5.2,
                "retentionRate": 94.8,
                "cohortAnalysis": [
                    {
                        "cohortMonth": "2024-01",
                        "customersCount": 100,
                        "retentionRates": {
                            "Month1": 95.0
                        }
                    }
                ]
            },
            "behaviorAnalysis": {
                "behaviorInsights": [
                    {
                        "insight": "Test insight",
                        "category": "Test Category",
                        "impact": "High",
                        "recommendation": "Test recommendation"
                    }
                ]
            }
        }
    }
    """.trimIndent()

    try {
        println("📝 Parsing JSON...")
        val response = json.decodeFromString<StandardReportResponse<CustomerReportDTO>>(testJson)
        
        println("✅ JSON parsing successful!")
        
        val customerReport = response.data
        
        // Test all extension properties (these should compile without conflicts)
        println("🔍 Testing Extension Properties:")
        println("   - summary.totalCustomers: ${customerReport.summary.totalCustomers}")
        println("   - segments.size: ${customerReport.segments.size}")
        println("   - topCustomers.size: ${customerReport.topCustomers.size}")
        println("   - retention.retentionRate: ${customerReport.retention.retentionRate}")
        println("   - acquisition.newCustomersThisMonth: ${customerReport.acquisition.newCustomersThisMonth}")
        println("   - behaviorInsights.size: ${customerReport.behaviorInsights.size}")
        
        // Test accessing data like the UI would
        println("\n📊 UI-style Access Test:")
        println("   Total Customers: ${customerReport.summary.totalCustomers}")
        println("   Active Customers: ${customerReport.summary.activeCustomers}")
        println("   Retention Rate: ${customerReport.summary.customerRetentionRate}%")
        
        if (customerReport.segments.isNotEmpty()) {
            val segment = customerReport.segments[0]
            println("   First Segment: ${segment.segmentName} (${segment.customerCount} customers)")
        }
        
        if (customerReport.topCustomers.isNotEmpty()) {
            val topCustomer = customerReport.topCustomers[0]
            println("   Top Customer: ${topCustomer.customerName} ($${topCustomer.totalValue})")
        }
        
        println("\n🎉 All compilation and extension property tests passed!")
        
    } catch (e: Exception) {
        println("❌ Error: ${e.message}")
        e.printStackTrace()
    }
}
