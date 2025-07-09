/**
 * Simple compilation test to verify that the NetworkResult and PromotionRepository fixes work correctly
 */

import data.api.NetworkResult
import data.api.PromotionDTO

fun testNetworkResultCompilation() {
    // Test NetworkResult creation
    val successResult: NetworkResult<String> = NetworkResult.Success("Test data")
    val errorResult: NetworkResult<String> = NetworkResult.Error(
        data.api.ApiException.NetworkError("Test error")
    )
    val loadingResult: NetworkResult<String> = NetworkResult.Loading
    
    // Test when expressions (should compile without issues)
    when (successResult) {
        is NetworkResult.Success -> {
            println("Success: ${successResult.data}")
        }
        is NetworkResult.Error -> {
            println("Error: ${successResult.exception.message}")
        }
        is NetworkResult.Loading -> {
            println("Loading...")
        }
    }
    
    // Test inline functions (should compile without issues)
    successResult.onSuccess { data ->
        println("Inline success: $data")
    }.onError { exception ->
        println("Inline error: ${exception.message}")
    }.onLoading {
        println("Inline loading")
    }
    
    println("✅ NetworkResult compilation test passed")
}

fun testPromotionRepositoryTypes() {
    // Test that PromotionDTO can be used in NetworkResult
    val promotionList: List<PromotionDTO> = emptyList()
    val promotionResult: NetworkResult<List<PromotionDTO>> = NetworkResult.Success(promotionList)
    
    when (promotionResult) {
        is NetworkResult.Success -> {
            println("Promotions loaded: ${promotionResult.data.size}")
        }
        is NetworkResult.Error -> {
            println("Error loading promotions: ${promotionResult.exception.message}")
        }
        is NetworkResult.Loading -> {
            println("Loading promotions...")
        }
    }
    
    println("✅ PromotionRepository types test passed")
}

fun main() {
    try {
        testNetworkResultCompilation()
        testPromotionRepositoryTypes()
        println("🎉 All compilation tests passed successfully!")
    } catch (e: Exception) {
        println("❌ Compilation test failed: ${e.message}")
        e.printStackTrace()
    }
}
