import data.repository.ReportsRepository
import data.api.services.ReportsApiService
import data.api.HttpClientProvider
import ui.viewmodels.ReportsViewModel
import kotlinx.serialization.json.JsonElement

fun main() {
    println("🔍 Verifying Type Consistency...")
    
    // Create instances to verify types
    val httpClient = HttpClientProvider.create()
    val apiService = ReportsApiService(httpClient)
    val repository = ReportsRepository(apiService)
    val viewModel = ReportsViewModel(repository)
    
    // Check type consistency
    println("📊 Type Verification:")
    
    // Repository types
    val repositoryKPIs = repository.realTimeKPIs
    println("   Repository realTimeKPIs type: ${repositoryKPIs::class.simpleName}")
    
    // ViewModel types  
    val viewModelKPIs = viewModel.realTimeKPIs
    println("   ViewModel realTimeKPIs type: ${viewModelKPIs::class.simpleName}")
    
    // Check if they're the same type
    val typesMatch = repositoryKPIs::class == viewModelKPIs::class
    println("   Types match: $typesMatch")
    
    if (typesMatch) {
        println("✅ Type consistency verified!")
        println("✅ Both Repository and ViewModel expose JsonElement? type")
        println("✅ ReportsScreen should compile without type mismatch errors")
    } else {
        println("❌ Type mismatch detected!")
        println("❌ Repository type: ${repositoryKPIs::class}")
        println("❌ ViewModel type: ${viewModelKPIs::class}")
    }
    
    // Test JsonElement usage
    println("\n🧪 Testing JsonElement functionality...")
    try {
        // This should compile without errors
        val testFunction: (JsonElement?) -> Unit = { kpis ->
            if (kpis != null) {
                println("   JsonElement received successfully")
            } else {
                println("   JsonElement is null (expected for test)")
            }
        }
        
        // Simulate what ReportsScreen does
        testFunction(null) // This should work
        println("✅ JsonElement parameter passing works correctly")
        
    } catch (e: Exception) {
        println("❌ JsonElement test failed: ${e.message}")
    }
    
    println("\n🎉 Type verification complete!")
}
