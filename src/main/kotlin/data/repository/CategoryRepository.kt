package data.repository

import data.api.*
import data.api.services.CategoryApiService
import kotlinx.coroutines.flow.*

/**
 * Repository for category data management
 */
class CategoryRepository(
    private val categoryApiService: CategoryApiService
) {
    
    // State management
    private val _categories = MutableStateFlow<List<CategoryDTO>>(emptyList())
    val categories: StateFlow<List<CategoryDTO>> = _categories.asStateFlow()
    
    private val _activeCategories = MutableStateFlow<List<CategoryDTO>>(emptyList())
    val activeCategories: StateFlow<List<CategoryDTO>> = _activeCategories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    suspend fun loadCategories(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "displayOrder",
        sortDir: String = "asc",
        status: String? = null
    ): NetworkResult<PageResponse<CategoryDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = categoryApiService.getAllCategories(page, size, sortBy, sortDir, status)
        
        result.onSuccess { pageResponse ->
            if (page == 0) {
                _categories.value = pageResponse.content
            } else {
                _categories.value = _categories.value + pageResponse.content
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun loadActiveCategories(): NetworkResult<List<CategoryDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = categoryApiService.getAllActiveCategories()
        
        result.onSuccess { categories ->
            _activeCategories.value = categories
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getCategoryById(id: Long): NetworkResult<CategoryDTO> {
        return categoryApiService.getCategoryById(id)
    }
    
    suspend fun getCategoryByName(name: String): NetworkResult<CategoryDTO> {
        return categoryApiService.getCategoryByName(name)
    }
    
    suspend fun createCategory(category: CategoryDTO): NetworkResult<CategoryDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = categoryApiService.createCategory(category)
        
        result.onSuccess { newCategory ->
            // Add to current categories list
            _categories.value = _categories.value + newCategory
            // If it's active, add to active categories too
            if (newCategory.status == "ACTIVE") {
                _activeCategories.value = _activeCategories.value + newCategory
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun updateCategory(id: Long, category: CategoryDTO): NetworkResult<CategoryDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = categoryApiService.updateCategory(id, category)
        
        result.onSuccess { updatedCategory ->
            // Update in categories list
            _categories.value = _categories.value.map { 
                if (it.id == id) updatedCategory else it 
            }
            // Update in active categories list
            _activeCategories.value = if (updatedCategory.status == "ACTIVE") {
                _activeCategories.value.map { 
                    if (it.id == id) updatedCategory else it 
                }.let { list ->
                    if (list.none { it.id == id }) list + updatedCategory else list
                }
            } else {
                _activeCategories.value.filter { it.id != id }
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun deleteCategory(id: Long): NetworkResult<Unit> {
        _isLoading.value = true
        _error.value = null
        
        val result = categoryApiService.deleteCategory(id)
        
        result.onSuccess {
            // Remove from both lists
            _categories.value = _categories.value.filter { it.id != id }
            _activeCategories.value = _activeCategories.value.filter { it.id != id }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun searchCategories(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<PageResponse<CategoryDTO>> {
        return categoryApiService.searchCategories(query, page, size)
    }
    
    suspend fun getCategoriesByStatus(status: String): NetworkResult<List<CategoryDTO>> {
        return categoryApiService.getCategoriesByStatus(status)
    }
    
    suspend fun getEmptyCategories(): NetworkResult<List<CategoryDTO>> {
        return categoryApiService.getEmptyCategories()
    }
    
    suspend fun updateCategoryStatus(id: Long, status: String): NetworkResult<CategoryDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = categoryApiService.updateCategoryStatus(id, status)
        
        result.onSuccess { updatedCategory ->
            // Update in categories list
            _categories.value = _categories.value.map { 
                if (it.id == id) updatedCategory else it 
            }
            // Update active categories list based on new status
            _activeCategories.value = if (status == "ACTIVE") {
                _activeCategories.value.map { 
                    if (it.id == id) updatedCategory else it 
                }.let { list ->
                    if (list.none { it.id == id }) list + updatedCategory else list
                }
            } else {
                _activeCategories.value.filter { it.id != id }
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Get categories for dropdown/selection
     */
    fun getCategoriesForSelection(): List<CategoryDTO> {
        return _activeCategories.value.sortedBy { it.displayOrder ?: 0 }
    }
}
