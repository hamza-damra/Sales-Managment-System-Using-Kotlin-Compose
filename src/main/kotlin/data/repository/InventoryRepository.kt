package data.repository

import data.api.*
import data.api.services.InventoryApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing inventory data and state
 */
class InventoryRepository(private val inventoryApiService: InventoryApiService) {
    
    private val _inventories = MutableStateFlow<List<InventoryDTO>>(emptyList())
    val inventories: StateFlow<List<InventoryDTO>> = _inventories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    suspend fun loadInventories(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDir: String = "asc",
        status: String? = null
    ): NetworkResult<PageResponse<InventoryDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = inventoryApiService.getAllInventories(page, size, sortBy, sortDir, status)
        
        result.onSuccess { pageResponse ->
            if (page == 0) {
                _inventories.value = pageResponse.content
            } else {
                _inventories.value = _inventories.value + pageResponse.content
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getInventoryById(id: Long): NetworkResult<InventoryDTO> {
        return inventoryApiService.getInventoryById(id)
    }
    
    suspend fun createInventory(request: InventoryCreateRequest): NetworkResult<InventoryDTO> {
        val result = inventoryApiService.createInventory(request)
        
        result.onSuccess { newInventory ->
            _inventories.value = _inventories.value + newInventory
        }
        
        return result
    }
    
    suspend fun updateInventory(id: Long, request: InventoryUpdateRequest): NetworkResult<InventoryDTO> {
        val result = inventoryApiService.updateInventory(id, request)
        
        result.onSuccess { updatedInventory ->
            _inventories.value = _inventories.value.map { 
                if (it.id == id) updatedInventory else it 
            }
        }
        
        return result
    }
    
    suspend fun deleteInventory(id: Long): NetworkResult<Unit> {
        val result = inventoryApiService.deleteInventory(id)
        
        result.onSuccess {
            _inventories.value = _inventories.value.filter { it.id != id }
        }
        
        return result
    }
    
    suspend fun searchInventories(
        query: String,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDir: String = "asc"
    ): NetworkResult<PageResponse<InventoryDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = inventoryApiService.searchInventories(query, page, size, sortBy, sortDir)
        
        result.onSuccess { pageResponse ->
            _inventories.value = pageResponse.content
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun loadActiveInventories(): NetworkResult<List<InventoryDTO>> {
        val result = inventoryApiService.getActiveInventories()
        
        result.onSuccess { activeInventories ->
            _inventories.value = activeInventories
        }
        
        return result
    }
    
    suspend fun loadMainWarehouses(): NetworkResult<List<InventoryDTO>> {
        return inventoryApiService.getMainWarehouses()
    }
    
    suspend fun getInventoryByName(name: String): NetworkResult<InventoryDTO> {
        return inventoryApiService.getInventoryByName(name)
    }
    
    suspend fun getInventoryByWarehouseCode(warehouseCode: String): NetworkResult<InventoryDTO> {
        return inventoryApiService.getInventoryByWarehouseCode(warehouseCode)
    }
    
    suspend fun getInventoriesByStatus(status: String): NetworkResult<List<InventoryDTO>> {
        return inventoryApiService.getInventoriesByStatus(status)
    }
    
    suspend fun loadEmptyInventories(): NetworkResult<List<InventoryDTO>> {
        return inventoryApiService.getEmptyInventories()
    }
    
    suspend fun loadInventoriesNearCapacity(threshold: Double = 80.0): NetworkResult<List<InventoryDTO>> {
        return inventoryApiService.getInventoriesNearCapacity(threshold)
    }
    
    suspend fun updateInventoryStatus(id: Long, status: InventoryStatus): NetworkResult<InventoryDTO> {
        val result = inventoryApiService.updateInventoryStatus(id, status)
        
        result.onSuccess { updatedInventory ->
            _inventories.value = _inventories.value.map { 
                if (it.id == id) updatedInventory else it 
            }
        }
        
        return result
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun getLowCapacityInventories(threshold: Double = 80.0): List<InventoryDTO> {
        return _inventories.value.filter { 
            it.capacityUtilization >= threshold 
        }
    }
    
    fun getMainWarehouse(): InventoryDTO? {
        return _inventories.value.find { it.isMainWarehouse }
    }
}
