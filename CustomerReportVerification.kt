import kotlinx.serialization.json.Json
import data.api.*

fun main() {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Test JSON that matches the backend response structure
    val testJson = """
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
                "conversionRate": 12.5,
                "growthRate": 8.2
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
                "churnReasons": {
                    "Price": 15,
                    "Service": 8,
                    "Competition": 12
                },
                "cohortAnalysis": [
                    {
                        "cohortMonth": "2024-01",
                        "customersCount": 100,
                        "retentionRates": {
                            "Month1": 95.0,
                            "Month2": 88.0,
                            "Month3": 82.0
                        }
                    }
                ]
            },
            "behaviorAnalysis": {
                "behaviorInsights": [
                    {
                        "insight": "العملاء يفضلون الشراء في نهاية الأسبوع",
                        "category": "Purchase Timing",
                        "impact": "High",
                        "recommendation": "زيادة العروض في نهاية الأسبوع"
                    }
                ],
                "purchasePatterns": {
                    "averageOrdersPerMonth": 2.3,
                    "preferredDayOfWeek": "Friday"
                },
                "engagementMetrics": {
                    "emailOpenRate": 25.5,
                    "clickThroughRate": 3.2
                }
            }
        },
        "metadata": {
            "reportType": "customer_analytics",
            "reportName": "Customer Analytics Report",
            "generatedAt": "2024-01-20T10:30:00Z",
            "generatedBy": "system",
            "executionTimeMs": 1250
        }
    }
    """.trimIndent()

    try {
        println("🔍 Testing CustomerReportDTO deserialization...")
        
        // Parse the JSON
        val response = json.decodeFromString<StandardReportResponse<CustomerReportDTO>>(testJson)
        
        println("✅ JSON parsing successful!")
        println("📊 Response success: ${response.success}")
        println("📝 Message: ${response.message}")
        
        val customerReport = response.data
        
        // Test new structure access
        println("\n🆕 New Structure Access:")
        println("   Customer Segmentation: ${customerReport.customerSegmentation != null}")
        println("   Acquisition Metrics: ${customerReport.acquisitionMetrics != null}")
        println("   Lifetime Value Analysis: ${customerReport.lifetimeValueAnalysis != null}")
        println("   Churn Analysis: ${customerReport.churnAnalysis != null}")
        println("   Behavior Analysis: ${customerReport.behaviorAnalysis != null}")
        
        // Test backward compatibility
        println("\n🔄 Backward Compatibility Access:")
        println("   Total Customers: ${customerReport.summary.totalCustomers}")
        println("   Active Customers: ${customerReport.summary.activeCustomers}")
        println("   New Customers This Month: ${customerReport.summary.newCustomersThisMonth}")
        println("   Average Customer Value: ${customerReport.summary.averageCustomerValue}")
        println("   Retention Rate: ${customerReport.summary.customerRetentionRate}%")
        println("   Churn Rate: ${customerReport.summary.churnRate}%")
        
        println("\n📊 Segments (${customerReport.segments.size} segments):")
        customerReport.segments.forEach { segment ->
            println("   - ${segment.segmentName}: ${segment.customerCount} customers (${segment.percentage}%)")
        }
        
        println("\n👥 Top Customers (${customerReport.topCustomers.size} customers):")
        customerReport.topCustomers.forEach { customer ->
            println("   - ${customer.customerName}: $${customer.totalValue}")
        }
        
        println("\n📈 Retention Metrics:")
        println("   - Retention Rate: ${customerReport.retention.retentionRate}%")
        println("   - Churn Rate: ${customerReport.retention.churnRate}%")
        println("   - Cohort Analysis: ${customerReport.retention.cohortAnalysis.size} cohorts")
        
        println("\n🎯 Acquisition Metrics:")
        println("   - New Customers: ${customerReport.acquisition.newCustomersThisMonth}")
        println("   - Acquisition Cost: $${customerReport.acquisition.acquisitionCost}")
        println("   - Conversion Rate: ${customerReport.acquisition.conversionRate}%")
        
        println("\n💡 Behavior Insights (${customerReport.behaviorInsights.size} insights):")
        customerReport.behaviorInsights.forEach { insight ->
            println("   - ${insight.category}: ${insight.insight}")
        }
        
        println("\n✅ All tests passed! The CustomerReportDTO successfully handles the backend response structure.")
        
    } catch (e: Exception) {
        println("❌ Error during deserialization: ${e.message}")
        e.printStackTrace()
    }
}
