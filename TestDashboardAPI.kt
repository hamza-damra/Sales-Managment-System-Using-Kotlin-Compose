package test

import data.api.*
import data.api.services.DashboardApiService
import data.auth.TokenManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * Test to debug dashboard API response
 */
fun main() {
    println("🧪 Testing Dashboard API Response...")
    
    runBlocking {
        // Create a simple HTTP client for testing
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                    prettyPrint = true
                })
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000L
                connectTimeoutMillis = 30_000L
                socketTimeoutMillis = 30_000L
            }
        }
        
        try {
            // First, let's try to get the raw response
            println("📡 Making request to: ${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_DASHBOARD}")
            
            val response = client.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_DASHBOARD}") {
                header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczNjA3NzE5NCwiZXhwIjoxNzM2MDgwNzk0fQ.Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8") // Use a test token
            }
            
            println("📊 Response Status: ${response.status}")
            println("📊 Response Headers: ${response.headers}")
            
            val responseText = response.body<String>()
            println("📄 Raw Response Body:")
            println(responseText)
            
            // Try to parse it as JSON
            try {
                val json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                    prettyPrint = true
                }
                
                val dashboardData = json.decodeFromString<DashboardSummaryDTO>(responseText)
                println("✅ Successfully parsed dashboard data:")
                println("   Period: ${dashboardData.period}")
                println("   Generated At: ${dashboardData.generatedAt}")
                println("   Sales: ${dashboardData.sales}")
                println("   Customers: ${dashboardData.customers}")
                println("   Inventory: ${dashboardData.inventory}")
                println("   Revenue: ${dashboardData.revenue}")
                
            } catch (e: Exception) {
                println("❌ Failed to parse JSON: ${e.message}")
                println("Exception type: ${e::class.simpleName}")
                e.printStackTrace()
            }
            
        } catch (e: Exception) {
            println("❌ Request failed: ${e.message}")
            println("Exception type: ${e::class.simpleName}")
            e.printStackTrace()
        } finally {
            client.close()
        }
    }
}
