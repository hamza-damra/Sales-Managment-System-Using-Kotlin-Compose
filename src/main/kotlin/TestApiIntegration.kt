import data.di.AppDependencies
import data.api.NetworkResult
import kotlinx.coroutines.runBlocking

/**
 * Test API integration and connectivity
 */
fun main() = runBlocking {
    println("=== API Integration Test ===")
    println("Testing connection to backend API...")
    
    try {
        // Initialize services using dependency injection
        val container = AppDependencies.container
        val productRepository = container.productRepository
        val reportsApiService = container.reportsApiService

        println("✓ Services initialized successfully using dependency injection")
        
        // Test 1: Load Products
        println("\n1. Testing Product Loading...")
        try {
            val productsResult = productRepository.loadProducts(page = 0, size = 10)
            when (productsResult) {
                is NetworkResult.Success -> {
                    val products = productsResult.data.content
                    println("✓ Successfully loaded ${products.size} products")
                    if (products.isNotEmpty()) {
                        val firstProduct = products.first()
                        println("   Sample product: ${firstProduct.name} (ID: ${firstProduct.id})")
                        println("   Price: ${firstProduct.price}, Stock: ${firstProduct.stockQuantity}")
                    }
                }
                is NetworkResult.Error -> {
                    println("✗ Failed to load products: ${productsResult.exception.message}")
                    println("   This might be expected if the backend server is not running")
                }
                is NetworkResult.Loading -> {
                    println("⏳ Loading products...")
                }
            }
        } catch (e: Exception) {
            println("✗ Exception during product loading: ${e.message}")
        }
        
        // Test 2: Search Products
        println("\n2. Testing Product Search...")
        try {
            val searchResult = productRepository.searchProducts("laptop", page = 0, size = 5)
            when (searchResult) {
                is NetworkResult.Success -> {
                    val products = searchResult.data.content
                    println("✓ Successfully searched products, found ${products.size} results")
                }
                is NetworkResult.Error -> {
                    println("✗ Failed to search products: ${searchResult.exception.message}")
                }
                is NetworkResult.Loading -> {
                    println("⏳ Searching products...")
                }
            }
        } catch (e: Exception) {
            println("✗ Exception during product search: ${e.message}")
        }
        
        // Test 3: Get Inventory Report
        println("\n3. Testing Inventory Report...")
        try {
            val reportResult = reportsApiService.getInventoryReport()
            when (reportResult) {
                is NetworkResult.Success -> {
                    val report = reportResult.data
                    println("✓ Successfully retrieved inventory report")
                    println("   Total products: ${report.totalProducts}")
                    println("   Low stock products: ${report.lowStockProducts.size}")
                    println("   Out of stock products: ${report.outOfStockProducts.size}")
                }
                is NetworkResult.Error -> {
                    println("✗ Failed to get inventory report: ${reportResult.exception.message}")
                }
                is NetworkResult.Loading -> {
                    println("⏳ Loading inventory report...")
                }
            }
        } catch (e: Exception) {
            println("✗ Exception during inventory report: ${e.message}")
        }
        
        // Test 4: Get Dashboard Summary
        println("\n4. Testing Dashboard Summary...")
        try {
            val dashboardResult = reportsApiService.getDashboardSummary()
            when (dashboardResult) {
                is NetworkResult.Success -> {
                    val dashboard = dashboardResult.data
                    println("✓ Successfully retrieved dashboard summary")
                    println("   Total sales count: ${dashboard.sales?.totalSales ?: 0}")
                    println("   Total revenue: ${dashboard.sales?.totalRevenue ?: 0.0}")
                    println("   Total customers: ${dashboard.customers?.totalCustomers ?: 0}")
                    println("   Total products: ${dashboard.inventory?.totalProducts ?: 0}")
                }
                is NetworkResult.Error -> {
                    println("✗ Failed to get dashboard summary: ${dashboardResult.exception.message}")
                }
                is NetworkResult.Loading -> {
                    println("⏳ Loading dashboard summary...")
                }
            }
        } catch (e: Exception) {
            println("✗ Exception during dashboard summary: ${e.message}")
        }
        
        // Test 5: Low Stock Products
        println("\n5. Testing Low Stock Detection...")
        try {
            val lowStockProducts = productRepository.getLowStockProducts(threshold = 10)
            println("✓ Successfully identified ${lowStockProducts.size} low stock products")
            lowStockProducts.take(3).forEach { product ->
                println("   - ${product.name}: ${product.stockQuantity} units (min: ${product.minStockLevel})")
            }
        } catch (e: Exception) {
            println("✗ Exception during low stock detection: ${e.message}")
        }
        
        // Clean up resources
        container.cleanup()
        println("\n✓ Resources cleaned up successfully")
        
    } catch (e: Exception) {
        println("✗ Failed to initialize services: ${e.message}")
        e.printStackTrace()
    }
    
    println("\n=== Test Summary ===")
    println("API Integration test completed.")
    println("\nNotes:")
    println("- If tests fail, ensure the backend server is running on http://localhost:8081")
    println("- Some failures are expected if the server is not available")
    println("- The export functionality will work with sample data even if API is unavailable")
    
    println("\n=== Next Steps ===")
    println("1. Start the backend server if not already running")
    println("2. Run the main application: ./gradlew run")
    println("3. Navigate to the Inventory screen")
    println("4. Test the export functionality with real data")
    println("5. Verify exported files contain correct information")
}
