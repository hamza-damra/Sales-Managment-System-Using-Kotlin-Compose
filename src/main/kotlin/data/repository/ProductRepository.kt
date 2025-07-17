package data.repository

import data.api.*
import data.api.services.ProductApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for product data management
 */
class ProductRepository(private val productApiService: ProductApiService) {
    
    private val _products = MutableStateFlow<List<ProductDTO>>(emptyList())
    val products: StateFlow<List<ProductDTO>> = _products.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    suspend fun loadProducts(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDir: String = "asc",
        category: String? = null
    ): NetworkResult<PageResponse<ProductDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = productApiService.getAllProducts(page, size, sortBy, sortDir, category)
        
        result.onSuccess { pageResponse ->
            if (page == 0) {
                _products.value = pageResponse.content
            } else {
                _products.value = _products.value + pageResponse.content
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getProductById(id: Long): NetworkResult<ProductDTO> {
        return productApiService.getProductById(id)
    }
    
    suspend fun createProduct(product: ProductDTO): NetworkResult<ProductDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = productApiService.createProduct(product)
        
        result.onSuccess { newProduct ->
            _products.value = _products.value + newProduct
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun updateProduct(id: Long, product: ProductDTO): NetworkResult<ProductDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = productApiService.updateProduct(id, product)
        
        result.onSuccess { updatedProduct ->
            _products.value = _products.value.map { 
                if (it.id == id) updatedProduct else it 
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun deleteProduct(id: Long): NetworkResult<Unit> {
        _isLoading.value = true
        _error.value = null
        
        val result = productApiService.deleteProduct(id)
        
        result.onSuccess {
            _products.value = _products.value.filter { it.id != id }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun searchProducts(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<PageResponse<ProductDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = productApiService.searchProducts(query, page, size)
        
        result.onSuccess { pageResponse ->
            _products.value = pageResponse.content
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun updateStock(id: Long, stockQuantity: Int): NetworkResult<ProductDTO> {
        val result = productApiService.updateStock(id, stockQuantity)
        
        result.onSuccess { updatedProduct ->
            _products.value = _products.value.map { 
                if (it.id == id) updatedProduct else it 
            }
        }
        
        return result
    }
    
    suspend fun increaseStock(id: Long, quantity: Int): NetworkResult<ProductDTO> {
        val result = productApiService.increaseStock(id, quantity)
        
        result.onSuccess { updatedProduct ->
            _products.value = _products.value.map { 
                if (it.id == id) updatedProduct else it 
            }
        }
        
        return result
    }
    
    suspend fun decreaseStock(id: Long, quantity: Int): NetworkResult<ProductDTO> {
        val result = productApiService.decreaseStock(id, quantity)
        
        result.onSuccess { updatedProduct ->
            _products.value = _products.value.map { 
                if (it.id == id) updatedProduct else it 
            }
        }
        
        return result
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun getLowStockProducts(threshold: Int = 10): List<ProductDTO> {
        return _products.value.filter { 
            (it.stockQuantity ?: 0) <= threshold 
        }
    }
    
    fun getProductsByCategory(category: String): List<ProductDTO> {
        return _products.value.filter {
            it.category?.equals(category, ignoreCase = true) == true
        }
    }

    // Recent Products API methods
    private val _recentProducts = MutableStateFlow<List<RecentProductDTO>>(emptyList())
    val recentProducts: StateFlow<List<RecentProductDTO>> = _recentProducts.asStateFlow()

    private val _isLoadingRecent = MutableStateFlow(false)
    val isLoadingRecent: StateFlow<Boolean> = _isLoadingRecent.asStateFlow()

    private val _recentError = MutableStateFlow<String?>(null)
    val recentError: StateFlow<String?> = _recentError.asStateFlow()

    suspend fun loadRecentProducts(
        days: Int = 30,
        category: String? = null,
        categoryId: Long? = null,
        includeInventory: Boolean = false,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "lastSoldDate",
        sortDir: String = "desc"
    ): NetworkResult<PageResponse<RecentProductDTO>> {
        _isLoadingRecent.value = true
        _recentError.value = null

        val result = productApiService.getRecentProducts(
            days = days,
            category = category,
            categoryId = categoryId,
            includeInventory = includeInventory,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir
        )

        result.onSuccess { pageResponse ->
            if (page == 0) {
                _recentProducts.value = pageResponse.content
            } else {
                _recentProducts.value = _recentProducts.value + pageResponse.content
            }
        }.onError { exception ->
            _recentError.value = exception.message
        }

        _isLoadingRecent.value = false
        return result
    }

    suspend fun loadRecentProductsBasic(days: Int = 30): NetworkResult<RecentProductsResponseDTO> {
        _isLoadingRecent.value = true
        _recentError.value = null

        val result = productApiService.getRecentProductsBasic(days)

        result.onSuccess { recentProductsResponse ->
            // Extract products from the new response structure
            _recentProducts.value = recentProductsResponse.products.content
        }.onError { exception ->
            _recentError.value = exception.message
        }

        _isLoadingRecent.value = false
        return result
    }

    fun clearRecentError() {
        _recentError.value = null
    }
}
