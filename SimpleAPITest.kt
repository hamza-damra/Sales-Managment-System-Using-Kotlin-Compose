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

fun main() {
    println("🧪 Simple API Test - Dashboard Endpoint")
    
    runBlocking {
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
            println("📡 Testing connection to: http://localhost:8081/api/reports/dashboard")
            
            val response = client.get("http://localhost:8081/api/reports/dashboard") {
                // Add a test token - you might need to replace this with a valid one
                header("Authorization", "Bearer test-token")
            }
            
            println("📊 Response Status: ${response.status}")
            println("📊 Response Headers:")
            response.headers.forEach { name, values ->
                println("  $name: ${values.joinToString(", ")}")
            }
            
            val responseText = response.body<String>()
            println("\n📄 Raw Response Body:")
            println(responseText)
            
        } catch (e: Exception) {
            println("❌ Request failed: ${e.message}")
            println("Exception type: ${e::class.simpleName}")
            
            when (e) {
                is java.net.ConnectException -> {
                    println("\n🔧 TROUBLESHOOTING:")
                    println("  - Backend server is not running")
                    println("  - Check if backend is running on localhost:8081")
                    println("  - Verify the backend API is accessible")
                }
                is ClientRequestException -> {
                    println("\n🔧 TROUBLESHOOTING:")
                    println("  - HTTP Error: ${e.response.status}")
                    println("  - Check authentication token")
                    println("  - Verify API endpoint exists")
                }
                else -> {
                    println("\n🔧 TROUBLESHOOTING:")
                    println("  - Unknown error occurred")
                    println("  - Check network connectivity")
                }
            }
        } finally {
            client.close()
        }
    }
}
