package data.repository

import data.api.services.StockMovementApiService
import data.api.services.StockMovementDTO
import data.api.PageResponse
import data.api.NetworkResult
import data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing stock movement data
 */
class StockMovementRepository(
    private val stockMovementApiService: StockMovementApiService
) {
    
    private val _stockMovements = MutableStateFlow<List<StockMovementDTO>>(emptyList())
    val stockMovements: StateFlow<List<StockMovementDTO>> = _stockMovements.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load stock movements with pagination and filtering
     */
    suspend fun loadStockMovements(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "date",
        sortDir: String = "desc",
        warehouseId: Long? = null,
        productId: Long? = null,
        movementType: MovementType? = null,
        startDate: String? = null,
        endDate: String? = null,
        refresh: Boolean = false
    ): NetworkResult<PageResponse<StockMovementDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = stockMovementApiService.getStockMovements(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir,
            warehouseId = warehouseId,
            productId = productId,
            movementType = movementType,
            startDate = startDate,
            endDate = endDate
        )
        
        result.onSuccess { pageResponse ->
            if (page == 0 || refresh) {
                _stockMovements.value = pageResponse.content
            } else {
                _stockMovements.value = _stockMovements.value + pageResponse.content
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Search stock movements
     */
    suspend fun searchStockMovements(
        query: String,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "date",
        sortDir: String = "desc"
    ): NetworkResult<PageResponse<StockMovementDTO>> {
        _isLoading.value = true
        _error.value = null
        
        // For now, we'll filter locally since there's no search endpoint
        val result = stockMovementApiService.getStockMovements(
            page = 0,
            size = 100, // Get more data to search through
            sortBy = sortBy,
            sortDir = sortDir
        )
        
        result.onSuccess { pageResponse ->
            // Filter movements based on query
            val filteredMovements = pageResponse.content.filter { movement ->
                movement.productName.contains(query, ignoreCase = true) ||
                movement.reference.contains(query, ignoreCase = true) ||
                movement.notes.contains(query, ignoreCase = true) ||
                movement.warehouseName.contains(query, ignoreCase = true)
            }
            
            // Apply pagination to filtered results
            val totalElements = filteredMovements.size
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, totalElements)
            val pageContent = if (startIndex < totalElements) {
                filteredMovements.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
            
            _stockMovements.value = pageContent
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Get stock movements by warehouse
     */
    suspend fun getMovementsByWarehouse(
        warehouseId: Long,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<PageResponse<StockMovementDTO>> {
        return loadStockMovements(
            page = page,
            size = size,
            warehouseId = warehouseId
        )
    }
    
    /**
     * Get stock movements by product
     */
    suspend fun getMovementsByProduct(
        productId: Long,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<PageResponse<StockMovementDTO>> {
        return loadStockMovements(
            page = page,
            size = size,
            productId = productId
        )
    }
    
    /**
     * Get stock movements by type
     */
    suspend fun getMovementsByType(
        movementType: MovementType,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<PageResponse<StockMovementDTO>> {
        return loadStockMovements(
            page = page,
            size = size,
            movementType = movementType
        )
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Refresh stock movements
     */
    suspend fun refresh(): NetworkResult<PageResponse<StockMovementDTO>> {
        return loadStockMovements(refresh = true)
    }
}
