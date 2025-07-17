package ui.viewmodels

import androidx.compose.runtime.*
import data.api.*
import data.api.services.StockMovementDTO
import data.*
import data.repository.InventoryRepository
import data.repository.StockMovementRepository
import data.repository.ProductRepository
import data.repository.CategoryRepository
import data.repository.ReportsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing inventory screen state and operations
 */
class InventoryViewModel(
    private val inventoryRepository: InventoryRepository,
    private val stockMovementRepository: StockMovementRepository,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val reportsRepository: ReportsRepository,
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    // UI State
    data class InventoryUiState(
        val inventories: List<InventoryDTO> = emptyList(),
        val stockMovements: List<StockMovementDTO> = emptyList(),
        val products: List<ProductDTO> = emptyList(),
        val recentProducts: List<RecentProductDTO> = emptyList(),
        val categories: List<CategoryDTO> = emptyList(),
        val dashboardInventory: DashboardInventoryDTO? = null,
        val inventorySummary: RecentProductsInventorySummaryDTO? = null, // New field for inventory summary from recent products API
        val isLoading: Boolean = false,
        val isLoadingMovements: Boolean = false,
        val isLoadingProducts: Boolean = false,
        val isLoadingRecentProducts: Boolean = false,
        val isLoadingCategories: Boolean = false,
        val isLoadingDashboard: Boolean = false,
        val error: String? = null,
        val movementsError: String? = null,
        val productsError: String? = null,
        val recentProductsError: String? = null,
        val categoriesError: String? = null,
        val dashboardError: String? = null,
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

        // Collect stock movement repository state
        viewModelScope.launch {
            stockMovementRepository.stockMovements.collect { movements ->
                _uiState.value = _uiState.value.copy(stockMovements = movements)
            }
        }

        viewModelScope.launch {
            stockMovementRepository.isLoading.collect { isLoading ->
                _uiState.value = _uiState.value.copy(isLoadingMovements = isLoading)
            }
        }

        viewModelScope.launch {
            stockMovementRepository.error.collect { error ->
                _uiState.value = _uiState.value.copy(movementsError = error)
            }
        }

        // Collect recent products repository state
        viewModelScope.launch {
            productRepository.recentProducts.collect { recentProducts ->
                _uiState.value = _uiState.value.copy(recentProducts = recentProducts)
            }
        }

        viewModelScope.launch {
            productRepository.isLoadingRecent.collect { isLoading ->
                _uiState.value = _uiState.value.copy(isLoadingRecentProducts = isLoading)
            }
        }

        viewModelScope.launch {
            productRepository.recentError.collect { error ->
                _uiState.value = _uiState.value.copy(recentProductsError = error)
            }
        }

        // Collect dashboard summary repository state
        viewModelScope.launch {
            reportsRepository.dashboardSummary.collect { dashboardSummary ->
                _uiState.value = _uiState.value.copy(dashboardInventory = dashboardSummary?.inventory)
            }
        }

        viewModelScope.launch {
            reportsRepository.isLoading.collect { isLoading ->
                _uiState.value = _uiState.value.copy(isLoadingDashboard = isLoading)
            }
        }

        viewModelScope.launch {
            reportsRepository.error.collect { error ->
                _uiState.value = _uiState.value.copy(dashboardError = error)
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
     * Load stock movements
     */
    fun loadStockMovements(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "date",
        sortDir: String = "desc",
        warehouseId: Long? = null,
        productId: Long? = null,
        movementType: MovementType? = null,
        refresh: Boolean = false
    ) {
        viewModelScope.launch {
            stockMovementRepository.loadStockMovements(
                page = page,
                size = size,
                sortBy = sortBy,
                sortDir = sortDir,
                warehouseId = warehouseId,
                productId = productId,
                movementType = movementType,
                refresh = refresh
            )
        }
    }

    /**
     * Load products for inventory display
     */
    fun loadProducts(
        page: Int = 0,
        size: Int = 50,
        sortBy: String = "name",
        sortDir: String = "asc",
        category: String? = null,
        refresh: Boolean = false
    ) {
        if (refresh) {
            _uiState.value = _uiState.value.copy(currentPage = 0)
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProducts = true, productsError = null)

            val result = productRepository.loadProducts(
                page = page,
                size = size,
                sortBy = sortBy,
                sortDir = sortDir,
                category = category
            )

            result.onSuccess { pageResponse ->
                _uiState.value = _uiState.value.copy(
                    products = if (page == 0) pageResponse.content else _uiState.value.products + pageResponse.content,
                    hasMorePages = !pageResponse.last,
                    currentPage = if (page == 0) 0 else page,
                    isLoadingProducts = false
                )
            }.onError { exception ->
                _uiState.value = _uiState.value.copy(
                    productsError = exception.message,
                    isLoadingProducts = false
                )
            }
        }
    }

    /**
     * Search products
     */
    fun searchProducts(query: String) {
        searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isBlank()) {
            loadProducts(refresh = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProducts = true, productsError = null)

            val result = productRepository.searchProducts(
                query = query,
                page = 0,
                size = 50
            )

            result.onSuccess { pageResponse ->
                _uiState.value = _uiState.value.copy(
                    products = pageResponse.content,
                    isLoadingProducts = false
                )
            }.onError { exception ->
                _uiState.value = _uiState.value.copy(
                    productsError = exception.message,
                    isLoadingProducts = false
                )
            }
        }
    }

    /**
     * Get low stock products
     */
    fun getLowStockProducts(threshold: Int = 10): List<ProductDTO> {
        return _uiState.value.products.filter {
            (it.stockQuantity ?: 0) <= threshold
        }
    }

    /**
     * Clear products error
     */
    fun clearProductsError() {
        _uiState.value = _uiState.value.copy(productsError = null)
    }

    /**
     * Load recent products for overview section
     */
    fun loadRecentProducts(
        days: Int = 30,
        category: String? = null,
        categoryId: Long? = null,
        includeInventory: Boolean = true,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "lastSoldDate",
        sortDir: String = "desc",
        refresh: Boolean = false
    ) {
        if (refresh) {
            _uiState.value = _uiState.value.copy(currentPage = 0)
        }

        viewModelScope.launch {
            val result = productRepository.loadRecentProducts(
                days = days,
                category = if (category != "الكل") category else null,
                categoryId = categoryId,
                includeInventory = includeInventory,
                page = page,
                size = size,
                sortBy = sortBy,
                sortDir = sortDir
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
     * Load recent products basic (for simple overview)
     */
    fun loadRecentProductsBasic(days: Int = 30) {
        viewModelScope.launch {
            val result = productRepository.loadRecentProductsBasic(days)

            result.onSuccess { recentProductsResponse ->
                // Update UI state with inventory summary from the API response
                _uiState.value = _uiState.value.copy(
                    inventorySummary = recentProductsResponse.inventorySummary
                )
            }.onError { exception ->
                // Error handling is already done in the repository
                // Just clear the inventory summary on error
                _uiState.value = _uiState.value.copy(
                    inventorySummary = null
                )
            }
        }
    }

    /**
     * Clear recent products error
     */
    fun clearRecentProductsError() {
        _uiState.value = _uiState.value.copy(recentProductsError = null)
        productRepository.clearRecentError()
    }

    /**
     * Load dashboard inventory statistics
     */
    fun loadDashboardInventoryStats() {
        viewModelScope.launch {
            val result = reportsRepository.loadDashboardSummary()

            result.onError { exception ->
                _uiState.value = _uiState.value.copy(dashboardError = exception.message)
            }
        }
    }

    /**
     * Clear dashboard error
     */
    fun clearDashboardError() {
        _uiState.value = _uiState.value.copy(dashboardError = null)
        reportsRepository.clearError()
    }

    /**
     * Load categories for dropdown
     */
    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCategories = true, categoriesError = null)

            val result = categoryRepository.loadActiveCategories()

            result.onSuccess { categories ->
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    isLoadingCategories = false
                )
            }.onError { exception ->
                _uiState.value = _uiState.value.copy(
                    categoriesError = exception.message,
                    isLoadingCategories = false
                )
            }
        }
    }

    /**
     * Get category options for dropdown
     */
    fun getCategoryOptions(): List<String> {
        val categories = _uiState.value.categories
        return listOf("الكل") + categories.map { it.name }
    }

    /**
     * Get warehouse options for dropdown
     */
    fun getWarehouseOptions(): List<String> {
        val inventories = _uiState.value.inventories
        return listOf("الكل") + inventories.map { it.name }
    }

    /**
     * Clear categories error
     */
    fun clearCategoriesError() {
        _uiState.value = _uiState.value.copy(categoriesError = null)
    }

    /**
     * Search stock movements
     */
    fun searchStockMovements(query: String) {
        viewModelScope.launch {
            stockMovementRepository.searchStockMovements(query)
        }
    }

    /**
     * Refresh stock movements
     */
    fun refreshStockMovements() {
        viewModelScope.launch {
            stockMovementRepository.refresh()
        }
    }
    
    /**
     * UI Actions
     */
    fun openCreateDialog() {
        showCreateDialog.value = true
    }

    fun closeCreateDialog() {
        showCreateDialog.value = false
    }
    
    fun openEditDialog(inventory: InventoryDTO) {
        selectedInventory.value = inventory
        showEditDialog.value = true
    }

    fun closeEditDialog() {
        showEditDialog.value = false
        selectedInventory.value = null
    }

    fun openDeleteDialog(inventory: InventoryDTO) {
        selectedInventory.value = inventory
        showDeleteDialog.value = true
    }

    fun closeDeleteDialog() {
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
