package ui.viewmodels

import data.api.CategoryDTO
import data.api.NetworkResult
import data.api.PageResponse
import data.repository.CategoryRepository
import data.mappers.toDomainModel
import data.Category
import data.CategoryStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for Categories screen
 */
class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // UI State
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()
    
    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Status filter state
    private val _selectedStatus = MutableStateFlow("الكل")
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()
    
    init {
        // Don't load data automatically - wait for explicit call
        // This prevents API calls when user is not authenticated
    }
    
    /**
     * Load all categories data
     */
    fun loadCategories(
        page: Int = 0,
        size: Int = 50,
        sortBy: String = "displayOrder",
        sortDir: String = "asc",
        status: String? = null
    ) {
        println("📂 CategoryViewModel - Starting to load categories...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            println("📡 CategoryViewModel - Making API call to category repository...")
            val result = categoryRepository.loadCategories(page, size, sortBy, sortDir, status)
            
            when (result) {
                is NetworkResult.Success -> {
                    println("✅ CategoryViewModel - Categories loaded successfully: ${result.data.content.size} categories")
                    val categories = result.data.content.map { it.toDomainModel() }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categories = categories,
                        totalCategories = result.data.totalElements.toInt(),
                        error = null,
                        lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                }
                is NetworkResult.Error -> {
                    println("❌ CategoryViewModel - Error loading categories: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to load categories"
                    )
                }
                is NetworkResult.Loading -> {
                    // Loading state is already handled by setting isLoading = true above
                    println("📡 CategoryViewModel - Loading state received")
                }
            }
        }
    }
    
    /**
     * Load active categories only
     */
    fun loadActiveCategories() {
        println("📂 CategoryViewModel - Loading active categories...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = categoryRepository.loadActiveCategories()
            
            when (result) {
                is NetworkResult.Success -> {
                    println("✅ CategoryViewModel - Active categories loaded: ${result.data.size} categories")
                    val categories = result.data.map { it.toDomainModel() }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        activeCategories = categories,
                        error = null,
                        lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                }
                is NetworkResult.Error -> {
                    println("❌ CategoryViewModel - Error loading active categories: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to load active categories"
                    )
                }
                is NetworkResult.Loading -> {
                    println("📡 CategoryViewModel - Loading active categories state received")
                }
            }
        }
    }
    
    /**
     * Search categories
     */
    fun searchCategories(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            loadCategories()
            return
        }
        
        println("🔍 CategoryViewModel - Searching categories with query: $query")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = categoryRepository.searchCategories(query, 0, 50)
            
            when (result) {
                is NetworkResult.Success -> {
                    println("✅ CategoryViewModel - Search completed: ${result.data.content.size} categories found")
                    val categories = result.data.content.map { it.toDomainModel() }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categories = categories,
                        totalCategories = result.data.totalElements.toInt(),
                        error = null,
                        lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                }
                is NetworkResult.Error -> {
                    println("❌ CategoryViewModel - Error searching categories: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to search categories"
                    )
                }
                is NetworkResult.Loading -> {
                    println("🔍 CategoryViewModel - Search loading state received")
                }
            }
        }
    }
    
    /**
     * Filter categories by status
     */
    fun filterByStatus(status: String) {
        _selectedStatus.value = status
        
        val statusFilter = when (status) {
            "الكل" -> null
            "نشط" -> "ACTIVE"
            "غير نشط" -> "INACTIVE"
            "مؤرشف" -> "ARCHIVED"
            else -> null
        }
        loadCategories(status = statusFilter)
    }
    
    /**
     * Create a new category
     */
    fun createCategory(categoryDTO: CategoryDTO) {
        println("📂 CategoryViewModel - Creating new category: ${categoryDTO.name}")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = categoryRepository.createCategory(categoryDTO)
            
            when (result) {
                is NetworkResult.Success -> {
                    println("✅ CategoryViewModel - Category created successfully: ${result.data.name}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        creationSuccess = true,
                        error = null,
                        lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                    // Refresh the categories list
                    refreshCategories()
                }
                is NetworkResult.Error -> {
                    println("❌ CategoryViewModel - Error creating category: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to create category"
                    )
                }
                is NetworkResult.Loading -> {
                    println("📡 CategoryViewModel - Creating category loading state received")
                }
            }
        }
    }
    
    /**
     * Update an existing category
     */
    fun updateCategory(id: Long, categoryDTO: CategoryDTO) {
        println("📂 CategoryViewModel - Updating category: $id")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = categoryRepository.updateCategory(id, categoryDTO)
            
            when (result) {
                is NetworkResult.Success -> {
                    println("✅ CategoryViewModel - Category updated successfully: ${result.data.name}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        updateSuccess = true,
                        error = null,
                        lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                    // Refresh the categories list
                    refreshCategories()
                }
                is NetworkResult.Error -> {
                    println("❌ CategoryViewModel - Error updating category: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to update category"
                    )
                }
                is NetworkResult.Loading -> {
                    println("📡 CategoryViewModel - Updating category loading state received")
                }
            }
        }
    }
    
    /**
     * Delete a category
     */
    fun deleteCategory(id: Long) {
        println("📂 CategoryViewModel - Deleting category: $id")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = categoryRepository.deleteCategory(id)
            
            when (result) {
                is NetworkResult.Success -> {
                    println("✅ CategoryViewModel - Category deleted successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        deletionSuccess = true,
                        error = null,
                        lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                    // Refresh the categories list
                    refreshCategories()
                }
                is NetworkResult.Error -> {
                    println("❌ CategoryViewModel - Error deleting category: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to delete category"
                    )
                }
                is NetworkResult.Loading -> {
                    println("📡 CategoryViewModel - Deleting category loading state received")
                }
            }
        }
    }
    
    /**
     * Update category status
     */
    fun updateCategoryStatus(id: Long, status: CategoryStatus) {
        println("📂 CategoryViewModel - Updating category status: $id to ${status.name}")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = categoryRepository.updateCategoryStatus(id, status.name)
            
            when (result) {
                is NetworkResult.Success -> {
                    println("✅ CategoryViewModel - Category status updated successfully")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        updateSuccess = true,
                        error = null,
                        lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                    // Refresh the categories list
                    refreshCategories()
                }
                is NetworkResult.Error -> {
                    println("❌ CategoryViewModel - Error updating category status: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to update category status"
                    )
                }
                is NetworkResult.Loading -> {
                    println("📡 CategoryViewModel - Updating category status loading state received")
                }
            }
        }
    }
    
    /**
     * Refresh categories data
     */
    fun refreshCategories() {
        val currentStatus = when (_selectedStatus.value) {
            "الكل" -> null
            "نشط" -> "ACTIVE"
            "غير نشط" -> "INACTIVE"
            "مؤرشف" -> "ARCHIVED"
            else -> null
        }
        val currentQuery = _searchQuery.value
        
        if (currentQuery.isNotBlank()) {
            searchCategories(currentQuery)
        } else {
            loadCategories(status = currentStatus)
        }
    }
    
    /**
     * Get categories for dropdown/selection
     */
    fun getCategoriesForSelection(): List<Category> {
        return _uiState.value.activeCategories.sortedBy { it.displayOrder }
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear success states
     */
    fun clearSuccessStates() {
        _uiState.value = _uiState.value.copy(
            creationSuccess = false,
            updateSuccess = false,
            deletionSuccess = false
        )
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        viewModelScope.cancel()
    }
}

/**
 * UI State for Categories screen
 */
data class CategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val activeCategories: List<Category> = emptyList(),
    val totalCategories: Int = 0,
    val error: String? = null,
    val lastUpdated: kotlinx.datetime.LocalDateTime? = null,
    val creationSuccess: Boolean = false,
    val updateSuccess: Boolean = false,
    val deletionSuccess: Boolean = false
) {
    val hasData: Boolean get() = categories.isNotEmpty()
    val hasError: Boolean get() = error != null
    val hasActiveCategories: Boolean get() = activeCategories.isNotEmpty()
}
