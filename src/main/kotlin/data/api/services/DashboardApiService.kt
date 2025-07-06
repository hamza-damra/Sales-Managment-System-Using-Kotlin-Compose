package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json

/**
 * API service for dashboard data operations
 */
class DashboardApiService(private val httpClient: HttpClient) {
    
    suspend fun getDashboardSummary(): NetworkResult<DashboardSummaryDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_DASHBOARD}")

            // Debug: Log the raw response
            try {
                val responseText = response.body<String>()
                println("🔍 Raw dashboard response: $responseText")

                // Parse the response with lenient JSON settings
                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                    encodeDefaults = true
                }
                json.decodeFromString<DashboardSummaryDTO>(responseText)
            } catch (e: Exception) {
                println("❌ Failed to parse dashboard response: ${e.message}")
                throw e
            }
        }
    }
    
    suspend fun getSalesReport(
        startDate: String,
        endDate: String
    ): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_SALES}") {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }
            response.body<Map<String, Any>>()
        }
    }
    
    suspend fun getRevenueReport(months: Int = 6): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_REVENUE}") {
                parameter("months", months)
            }
            response.body<Map<String, Any>>()
        }
    }
    
    suspend fun getTopProductsReport(
        startDate: String,
        endDate: String
    ): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_TOP_PRODUCTS}") {
                parameter("startDate", startDate)
                parameter("endDate", endDate)
            }
            response.body<Map<String, Any>>()
        }
    }
    
    suspend fun getCustomerAnalytics(): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_CUSTOMER_ANALYTICS}")
            response.body<Map<String, Any>>()
        }
    }
    
    suspend fun getInventoryReport(): NetworkResult<Map<String, Any>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_INVENTORY}")
            response.body<Map<String, Any>>()
        }
    }
}
