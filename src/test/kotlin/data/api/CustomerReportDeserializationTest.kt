package data.api

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CustomerReportDeserializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `test CustomerReportDTO deserialization with backend response structure`() {
        // This is the actual response structure from the backend
        val backendResponse = """
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
                    "averageLifetimeValue": 2500.0,
                    "lifetimeValueDistribution": {
                        "High": 20.0,
                        "Medium": 60.0,
                        "Low": 20.0
                    }
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

        // Test deserialization of the full response
        val response = json.decodeFromString<StandardReportResponse<CustomerReportDTO>>(backendResponse)
        
        assertNotNull(response)
        assertTrue(response.success)
        assertEquals("Report generated successfully", response.message)
        assertNotNull(response.data)
        assertNotNull(response.metadata)

        val customerReport = response.data
        
        // Test new structure access
        assertNotNull(customerReport.customerSegmentation)
        assertNotNull(customerReport.acquisitionMetrics)
        assertNotNull(customerReport.lifetimeValueAnalysis)
        assertNotNull(customerReport.churnAnalysis)
        assertNotNull(customerReport.behaviorAnalysis)

        // Test backward compatibility properties
        assertEquals(1000L, customerReport.summary.totalCustomers)
        assertEquals(45L, customerReport.summary.newCustomersThisMonth)
        assertEquals(2500.0, customerReport.summary.averageCustomerValue, 0.01)
        assertEquals(94.8, customerReport.summary.customerRetentionRate, 0.01)
        assertEquals(5.2, customerReport.summary.churnRate, 0.01)

        // Test segments access via extension property
        assertEquals(1, customerReport.segments.size)
        assertEquals("Premium", customerReport.segments[0].segmentName)
        assertEquals(150L, customerReport.segments[0].customerCount)

        // Test top customers access via extension property
        assertEquals(1, customerReport.topCustomers.size)
        assertEquals("أحمد محمد", customerReport.topCustomers[0].customerName)
        assertEquals(15000.0, customerReport.topCustomers[0].totalValue, 0.01)

        // Test retention metrics
        assertEquals(94.8, customerReport.retention.retentionRate, 0.01)
        assertEquals(5.2, customerReport.retention.churnRate, 0.01)
        assertEquals(1, customerReport.retention.cohortAnalysis.size)

        // Test acquisition metrics
        assertEquals(45L, customerReport.acquisition.newCustomersThisMonth)
        assertEquals(125.50, customerReport.acquisition.acquisitionCost, 0.01)
        assertEquals(12.5, customerReport.acquisition.conversionRate, 0.01)

        // Test behavior insights
        assertEquals(1, customerReport.behaviorInsights.size)
        assertEquals("العملاء يفضلون الشراء في نهاية الأسبوع", customerReport.behaviorInsights[0].insight)
        assertEquals("Purchase Timing", customerReport.behaviorInsights[0].category)
    }

    @Test
    fun `test CustomerReportDTO deserialization with partial data`() {
        // Test with minimal data to ensure optional fields work
        val minimalResponse = """
        {
            "success": true,
            "message": "Report generated successfully",
            "data": {
                "customerSegmentation": {
                    "totalCustomers": 500
                }
            }
        }
        """.trimIndent()

        val response = json.decodeFromString<StandardReportResponse<CustomerReportDTO>>(minimalResponse)
        
        assertNotNull(response)
        assertTrue(response.success)
        assertNotNull(response.data)

        val customerReport = response.data
        
        // Test that missing fields don't cause errors
        assertEquals(500L, customerReport.summary.totalCustomers)
        assertEquals(0L, customerReport.summary.newCustomersThisMonth)
        assertEquals(0.0, customerReport.summary.averageCustomerValue, 0.01)
        assertTrue(customerReport.segments.isEmpty())
        assertTrue(customerReport.topCustomers.isEmpty())
        assertTrue(customerReport.behaviorInsights.isEmpty())
    }

    @Test
    fun `test CustomerReportDTO deserialization with empty data object`() {
        // Test with completely empty data object
        val emptyDataResponse = """
        {
            "success": true,
            "message": "No data available",
            "data": {}
        }
        """.trimIndent()

        val response = json.decodeFromString<StandardReportResponse<CustomerReportDTO>>(emptyDataResponse)
        
        assertNotNull(response)
        assertTrue(response.success)
        assertNotNull(response.data)

        val customerReport = response.data
        
        // Test that all computed properties work with null data
        assertEquals(0L, customerReport.summary.totalCustomers)
        assertTrue(customerReport.segments.isEmpty())
        assertTrue(customerReport.topCustomers.isEmpty())
        assertTrue(customerReport.behaviorInsights.isEmpty())
        assertEquals(0.0, customerReport.retention.retentionRate, 0.01)
        assertEquals(0L, customerReport.acquisition.newCustomersThisMonth)
    }
}
