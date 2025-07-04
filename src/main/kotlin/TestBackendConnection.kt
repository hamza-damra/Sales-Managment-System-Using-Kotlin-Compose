import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

/**
 * Simple test to check if the backend server is running
 */
fun main() = runBlocking {
    println("🔍 Testing backend connection...")
    
    val client = HttpClient(CIO)
    
    try {
        // Test 1: Check if server is running
        println("📡 Testing server connectivity...")
        val response = client.get("http://localhost:8081/api/auth/login") {
            // This should return 405 Method Not Allowed (since we're using GET instead of POST)
            // But it confirms the server is running and the endpoint exists
        }
        
        println("✅ Server is running!")
        println("   Status: ${response.status}")
        println("   Headers: ${response.headers}")
        
    } catch (e: Exception) {
        println("❌ Cannot connect to backend server")
        println("   Error: ${e.message}")
        println("   Type: ${e::class.simpleName}")
        
        when (e) {
            is java.net.ConnectException -> {
                println("\n💡 Solutions:")
                println("   1. Make sure your Spring Boot backend is running")
                println("   2. Check if it's running on localhost:8081")
                println("   3. Verify the /api/auth/login endpoint exists")
            }
            else -> {
                println("\n💡 Check your network connection and backend server")
            }
        }
    } finally {
        client.close()
    }
    
    // Test 2: Try the actual login endpoint
    println("\n🔐 Testing login endpoint...")
    
    val loginClient = HttpClient(CIO)
    try {
        val loginResponse = loginClient.post("http://localhost:8081/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "hamza", "password": "hamza123"}""")
        }
        
        println("✅ Login endpoint is working!")
        println("   Status: ${loginResponse.status}")
        println("   Response: ${loginResponse.bodyAsText()}")
        
    } catch (e: Exception) {
        println("❌ Login endpoint test failed")
        println("   Error: ${e.message}")
        println("   Type: ${e::class.simpleName}")
        
        if (e.message?.contains("401") == true) {
            println("\n💡 This might be normal if the credentials are wrong")
            println("   The endpoint is working, but credentials may be invalid")
        }
    } finally {
        loginClient.close()
    }
    
    println("\n📋 Summary:")
    println("   - Make sure your Spring Boot backend is running on localhost:8081")
    println("   - Verify the endpoint POST /api/auth/login exists")
    println("   - Check if the credentials 'hamza/hamza123' are valid in your database")
    println("   - Look at your backend console for any error messages")
}
