package ui.viewmodels

import androidx.compose.runtime.*
import data.api.*
import data.repository.InventoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing inventory screen state and operations
 */
class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    
    // UI State
    data class InventoryUiState(
        val inventories: List<InventoryDTO> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchQuery: String = "",
        val selectedStatus: String = "الكل",
        val sortBy: String = "name",
        val sortDirection: String = "asc",
        val currentPage: Int = 0,
        val hasMorePages: Boolean = true,
        val selectedInventory: InventoryDTO? = null,
        val showCreateDialog: Boolean = false,
        val showEditDialog: Boolean = false,
        val showDeleteDialog: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()
    
    // Observe repository state
    init {
        viewModelScope.launch {
            inventoryRepository.inventories.collect { inventories ->
                _uiState.value = _uiState.value.copy(inventories = inventories)
            }
        }
        
        viewModelScope.launch {
            inventoryRepository.isLoading.collect { isLoading ->
                _uiState.value = _uiState.value.copy(isLoading = isLoading)
            }
        }
        
        viewModelScope.launch {
            inventoryRepository.error.collect { error ->
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }
    
    // Search and filter state
    val searchQuery = mutableStateOf("")
    val selectedStatus = mutableStateOf("الكل")
    val sortBy = mutableStateOf("name")
    val sortDirection = mutableStateOf("asc")
    
    // Dialog states
    val showCreateDialog = mutableStateOf(false)
    val showEditDialog = mutableStateOf(false)
    val showDeleteDialog = mutableStateOf(false)
    val selectedInventory = mutableStateOf<InventoryDTO?>(null)
    
    // Statistics
    val totalInventories = derivedStateOf { _uiState.value.inventories.size }
    val activeInventories = derivedStateOf { 
        _uiState.value.inventories.count { it.status == InventoryStatus.ACTIVE } 
    }
    val nearCapacityInventories = derivedStateOf { 
        _uiState.value.inventories.count { it.isNearCapacity } 
    }
    val mainWarehouses = derivedStateOf { 
        _uiState.value.inventories.count { it.isMainWarehouse } 
    }
    
    /**
     * Load inventories with pagination and filtering
     */
    fun loadInventories(
        page: Int = 0,
        size: Int = 20,
        refresh: Boolean = false
    ) {
        if (refresh) {
            _uiState.value = _uiState.value.copy(currentPage = 0)
        }
        
        val statusFilter = if (selectedStatus.value != "الكل") {
            when(selectedStatus.value) {
                "نشط" -> "ACTIVE"
                "غير نشط" -> "INACTIVE"
                "مؤرشف" -> "ARCHIVED"
                "صيانة" -> "MAINTENANCE"
                else -> null
            }
        } else null
        
        viewModelScope.launch {
            val result = inventoryRepository.loadInventories(
                page = page,
                size = size,
                sortBy = sortBy.value,
                sortDir = sortDirection.value,
                status = statusFilter
            )
            
            result.onSuccess { pageResponse ->
                _uiState.value = _uiState.value.copy(
                    hasMorePages = !pageResponse.last,
                    currentPage = if (page == 0) 0 else page
                )
            }
        }
    }
    
    /**
     * Search inventories
     */
    fun searchInventories(query: String) {
        searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.isBlank()) {
            loadInventories(refresh = true)
            return
        }
        
        viewModelScope.launch {
            inventoryRepository.searchInventories(
                query = query,
                page = 0,
                size = 20,
                sortBy = sortBy.value,
                sortDir = sortDirection.value
            )
        }
    }
    
    /**
     * Create new inventory
     */
    suspend fun createInventory(request: InventoryCreateRequest): NetworkResult<InventoryDTO> {
        return inventoryRepository.createInventory(request)
    }
    
    /**
     * Update existing inventory
     */
    suspend fun updateInventory(id: Long, request: InventoryUpdateRequest): NetworkResult<InventoryDTO> {
        return inventoryRepository.updateInventory(id, request)
    }
    
    /**
     * Delete inventory
     */
    suspend fun deleteInventory(id: Long): NetworkResult<Unit> {
        return inventoryRepository.deleteInventory(id)
    }
    
    /**
     * Update inventory status
     */
    suspend fun updateInventoryStatus(id: Long, status: InventoryStatus): NetworkResult<InventoryDTO> {
        return inventoryRepository.updateInventoryStatus(id, status)
    }
    
    /**
     * Load active inventories only
     */
    fun loadActiveInventories() {
        viewModelScope.launch {
            inventoryRepository.loadActiveInventories()
        }
    }
    
    /**
     * Load main warehouses
     */
    suspend fun loadMainWarehouses(): NetworkResult<List<InventoryDTO>> {
        return inventoryRepository.loadMainWarehouses()
    }
    
    /**
     * Load inventories near capacity
     */
    fun loadInventoriesNearCapacity(threshold: Double = 80.0) {
        viewModelScope.launch {
            inventoryRepository.loadInventoriesNearCapacity(threshold)
        }
    }
    
    /**
     * UI Actions
     */
    fun showCreateDialog() {
        showCreateDialog.value = true
    }
    
    fun hideCreateDialog() {
        showCreateDialog.value = false
    }
    
    fun showEditDialog(inventory: InventoryDTO) {
        selectedInventory.value = inventory
        showEditDialog.value = true
    }
    
    fun hideEditDialog() {
        showEditDialog.value = false
        selectedInventory.value = null
    }
    
    fun showDeleteDialog(inventory: InventoryDTO) {
        selectedInventory.value = inventory
        showDeleteDialog.value = true
    }
    
    fun hideDeleteDialog() {
        showDeleteDialog.value = false
        selectedInventory.value = null
    }
    
    fun clearError() {
        inventoryRepository.clearError()
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun updateSortBy(newSortBy: String) {
        sortBy.value = newSortBy
        loadInventories(refresh = true)
    }
    
    fun updateSortDirection(newDirection: String) {
        sortDirection.value = newDirection
        loadInventories(refresh = true)
    }
    
    fun updateStatusFilter(newStatus: String) {
        selectedStatus.value = newStatus
        loadInventories(refresh = true)
    }
}
