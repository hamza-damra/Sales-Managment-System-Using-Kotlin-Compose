import kotlinx.coroutines.runBlocking
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Test script to debug the POST /api/sales endpoint
 */
fun main() = runBlocking {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                prettyPrint = true
            })
        }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }
    
    try {
        // Test 1: Check if GET /api/sales works (should work based on logs)
        println("=== Testing GET /api/sales ===")
        val getResponse = client.get("http://localhost:8081/api/sales") {
            header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYW16YSIsImlhdCI6MTcyMDI4NzI5MSwiZXhwIjoxNzIwMzczNjkxfQ.YOUR_TOKEN_HERE")
        }
        println("GET Response Status: ${getResponse.status}")
        println("GET Response Body: ${getResponse.bodyAsText()}")
        
        // Test 2: Test POST with minimal valid data
        println("\n=== Testing POST /api/sales with minimal data ===")
        val minimalSaleJson = """
        {
            "customerId": 1,
            "totalAmount": 999.99,
            "items": [
                {
                    "productId": 1,
                    "quantity": 1,
                    "unitPrice": 999.99
                }
            ]
        }
        """.trimIndent()
        
        val postResponse = client.post("http://localhost:8081/api/sales") {
            header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYW16YSIsImlhdCI6MTcyMDI4NzI5MSwiZXhwIjoxNzIwMzczNjkxfQ.YOUR_TOKEN_HERE")
            contentType(ContentType.Application.Json)
            setBody(minimalSaleJson)
        }
        println("POST Response Status: ${postResponse.status}")
        println("POST Response Body: ${postResponse.bodyAsText()}")
        
        // Test 3: Check what endpoints are available
        println("\n=== Testing available endpoints ===")
        val endpoints = listOf(
            "http://localhost:8081/api/sales",
            "http://localhost:8081/api/sales/",
            "http://localhost:8081/sales",
            "http://localhost:8081/api/sale"
        )
        
        for (endpoint in endpoints) {
            try {
                val response = client.get(endpoint) {
                    header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoYW16YSIsImlhdCI6MTcyMDI4NzI5MSwiZXhwIjoxNzIwMzczNjkxfQ.YOUR_TOKEN_HERE")
                }
                println("$endpoint -> ${response.status}")
            } catch (e: Exception) {
                println("$endpoint -> ERROR: ${e.message}")
            }
        }
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    } finally {
        client.close()
    }
}
