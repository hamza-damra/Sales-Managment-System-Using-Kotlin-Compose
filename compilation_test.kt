// Test file to verify compilation fixes
import data.api.services.StockMovementApiService
import data.api.services.StockMovementDTO
import data.repository.StockMovementRepository
import data.*
import io.ktor.client.*

// Test that all types compile correctly
fun testCompilation() {
    // Test StockMovementDTO creation
    val movement = StockMovementDTO(
        id = 1L,
        productId = 1L,
        productName = "Test Product",
        warehouseId = 1L,
        warehouseName = "Test Warehouse",
        movementType = MovementType.SALE,
        quantity = 10,
        date = "2024-01-01T10:00:00",
        reference = "REF-001",
        notes = "Test movement",
        unitPrice = 100.0,
        totalValue = 1000.0
    )
    
    println("StockMovementDTO created successfully: ${movement.productName}")
    
    // Test that the API service can be instantiated
    val httpClient = HttpClient()
    val apiService = StockMovementApiService(httpClient)
    val repository = StockMovementRepository(apiService)
    
    println("All classes instantiated successfully")
}

// Summary of fixes applied:
// 1. Fixed nullable field handling in StockMovementApiService
// 2. Fixed type mismatches for Long vs Long? fields
// 3. Fixed SortInfo constructor parameters
// 4. Fixed list type assignments in filtering logic
// 5. Added StockMovementApiService and StockMovementRepository to AppContainer
// 6. Updated InventoryViewModel constructor to include StockMovementRepository
// 7. Updated button behavior to work on all tabs
// 8. Enhanced stock movements content with real API integration

println("All compilation issues should now be resolved!")
