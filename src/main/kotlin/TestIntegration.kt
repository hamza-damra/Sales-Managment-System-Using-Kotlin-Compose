import data.api.*
import data.auth.*
import data.di.AppDependencies
import kotlinx.coroutines.runBlocking

/**
 * Simple test to verify the API integration works
 * Run this to test the connection to your backend
 */
fun main() = runBlocking {
    println("🚀 Testing Sales Management System API Integration...")
    
    val appContainer = AppDependencies.container
    val authService = appContainer.authService
    
    try {
        // Test 1: Check if backend is reachable
        println("\n📡 Testing backend connection...")
        val dashboardResult = appContainer.reportsRepository.loadDashboardSummary()
        
        when {
            dashboardResult.isSuccess -> {
                println("✅ Backend connection successful!")
                val dashboard = dashboardResult.getOrNull()
                println("   📊 Dashboard data: ${dashboard?.sales?.totalRevenue} revenue")
            }
            dashboardResult.isError -> {
                val error = (dashboardResult as NetworkResult.Error).exception
                println("❌ Backend connection failed: ${error.message}")
                println("   💡 Make sure your Spring Boot backend is running on localhost:8081")
            }
        }
        
        // Test 2: Test authentication (if backend is available)
        if (dashboardResult.isSuccess) {
            println("\n🔐 Testing authentication...")
            
            // Try to login with test credentials
            val loginResult = authService.login("testuser", "testpass")
            
            when {
                loginResult.isSuccess -> {
                    println("✅ Authentication successful!")
                    val authResponse = loginResult.getOrNull()
                    println("   👤 User: ${authResponse?.user?.firstName} ${authResponse?.user?.lastName}")
                    println("   🎭 Role: ${authResponse?.user?.role}")
                }
                loginResult.isError -> {
                    val error = (loginResult as NetworkResult.Error).exception
                    println("❌ Authentication failed: ${error.message}")
                    println("   💡 This is expected if you don't have test credentials")
                }
            }
        }
        
        // Test 3: Test customer API
        println("\n👥 Testing customer API...")
        val customersResult = appContainer.customerRepository.loadCustomers(page = 0, size = 5)
        
        when {
            customersResult.isSuccess -> {
                println("✅ Customer API working!")
                val customers = customersResult.getOrNull()
                println("   📋 Found ${customers?.totalElements} customers")
                customers?.content?.take(3)?.forEach { customer ->
                    println("   - ${customer.name} (${customer.email})")
                }
            }
            customersResult.isError -> {
                val error = (customersResult as NetworkResult.Error).exception
                println("❌ Customer API failed: ${error.message}")
            }
        }
        
        // Test 4: Test products API
        println("\n📦 Testing products API...")
        val productsResult = appContainer.productRepository.loadProducts(page = 0, size = 5)
        
        when {
            productsResult.isSuccess -> {
                println("✅ Products API working!")
                val products = productsResult.getOrNull()
                println("   📋 Found ${products?.totalElements} products")
                products?.content?.take(3)?.forEach { product ->
                    println("   - ${product.name} (${product.price} ${product.category})")
                }
            }
            productsResult.isError -> {
                val error = (productsResult as NetworkResult.Error).exception
                println("❌ Products API failed: ${error.message}")
            }
        }
        
        println("\n🎉 Integration test completed!")
        println("📝 Summary:")
        println("   - Backend connection: ${if (dashboardResult.isSuccess) "✅ Working" else "❌ Failed"}")
        println("   - Customer API: ${if (customersResult.isSuccess) "✅ Working" else "❌ Failed"}")
        println("   - Products API: ${if (productsResult.isSuccess) "✅ Working" else "❌ Failed"}")
        
        if (dashboardResult.isSuccess && customersResult.isSuccess && productsResult.isSuccess) {
            println("\n🚀 All systems ready! You can now run the main application.")
        } else {
            println("\n⚠️  Some issues detected. Check your backend server and API endpoints.")
        }
        
    } catch (e: Exception) {
        println("❌ Test failed with exception: ${e.message}")
        e.printStackTrace()
    } finally {
        // Clean up
        appContainer.cleanup()
    }
}
