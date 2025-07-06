package ui.viewmodels

import data.api.ProductDTO
import data.api.NetworkResult
import data.api.PageResponse
import data.repository.ProductRepository
import data.mappers.toDomainModel
import data.Product
import services.ProductImportExportService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for Products screen
 */
class ProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: data.repository.CategoryRepository
) {
    // Import/Export service
    private val importExportService = ProductImportExportService(productRepository)
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // UI State
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()
    
    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // Category filter state
    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    init {
        // Don't load data automatically - wait for explicit call
        // This prevents API calls when user is not authenticated
    }

    /**
     * Get active categories for product assignment
     */
    fun getActiveCategoriesAsFlow(): Flow<List<data.Category>> {
        return categoryRepository.activeCategories.map { dtoList ->
            dtoList.map { it.toDomainModel() }
        }
    }

    /**
     * Get active categories for product assignment (StateFlow)
     */
    private val _activeCategoriesForProducts = MutableStateFlow<List<data.Category>>(emptyList())
    val activeCategoriesForProducts: StateFlow<List<data.Category>> = _activeCategoriesForProducts.asStateFlow()

    /**
     * Update active categories for products when repository data changes
     */
    private fun observeActiveCategories() {
        viewModelScope.launch {
            categoryRepository.activeCategories.collect { dtoList ->
                _activeCategoriesForProducts.value = dtoList.map { it.toDomainModel() }
            }
        }
    }

    /**
     * Load active categories for dropdowns
     */
    fun loadActiveCategories() {
        println("📂 ProductViewModel - Loading active categories for product assignment...")
        viewModelScope.launch {
            categoryRepository.loadActiveCategories()
        }
        // Start observing category changes
        observeActiveCategories()
    }
    
    /**
     * Load all products data
     */
    fun loadProducts(
        page: Int = 0,
        size: Int = 50,
        sortBy: String = "name",
        sortDir: String = "asc",
        category: String? = null
    ) {
        println("📦 ProductViewModel - Starting to load products...")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            println("📡 ProductViewModel - Making API call to product repository...")
            val result = productRepository.loadProducts(page, size, sortBy, sortDir, category)
            
            when (result) {
                is NetworkResult.Success -> {
                    println("✅ ProductViewModel - Products loaded successfully: ${result.data.content.size} products")
                    val products = result.data.content.map { it.toDomainModel() }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = products,
                        totalProducts = result.data.totalElements.toInt(),
                        error = null,
                        lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                }
                is NetworkResult.Error -> {
                    println("❌ ProductViewModel - Error loading products: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to load products"
                    )
                }
                is NetworkResult.Loading -> {
                    // Loading state is already handled by setting isLoading = true above
                    println("📡 ProductViewModel - Loading state received")
                }
            }
        }
    }
    
    /**
     * Search products
     */
    fun searchProducts(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            loadProducts()
            return
        }
        
        println("🔍 ProductViewModel - Searching products with query: $query")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = productRepository.searchProducts(query, 0, 50)
            
            when (result) {
                is NetworkResult.Success -> {
                    println("✅ ProductViewModel - Search completed: ${result.data.content.size} products found")
                    val products = result.data.content.map { it.toDomainModel() }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        products = products,
                        totalProducts = result.data.totalElements.toInt(),
                        error = null,
                        lastUpdated = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    )
                }
                is NetworkResult.Error -> {
                    println("❌ ProductViewModel - Error searching products: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to search products"
                    )
                }
                is NetworkResult.Loading -> {
                    // Loading state is already handled by setting isLoading = true above
                    println("🔍 ProductViewModel - Search loading state received")
                }
            }
        }
    }
    
    /**
     * Filter products by category
     */
    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        
        val categoryFilter = if (category == "الكل") null else category
        loadProducts(category = categoryFilter)
    }
    
    /**
     * Refresh products data
     */
    fun refreshProducts() {
        val currentCategory = if (_selectedCategory.value == "الكل") null else _selectedCategory.value
        val currentQuery = _searchQuery.value
        
        if (currentQuery.isNotBlank()) {
            searchProducts(currentQuery)
        } else {
            loadProducts(category = currentCategory)
        }
    }
    
    /**
     * Get low stock products
     */
    fun getLowStockProducts(threshold: Int = 10): List<Product> {
        return _uiState.value.products.filter { it.stock <= threshold }
    }
    
    /**
     * Get product categories
     */
    fun getCategories(): List<String> {
        val categories = _uiState.value.products.map { it.category }.distinct().sorted()
        return listOf("الكل") + categories
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear deletion success state
     */
    fun clearDeletionSuccess() {
        _uiState.value = _uiState.value.copy(deletionSuccess = false)
    }
    
    /**
     * Create a new product
     */
    fun createProduct(productDTO: data.api.ProductDTO) {
        println("📦 ProductViewModel - Creating new product: ${productDTO.name}")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = productRepository.createProduct(productDTO)

            when (result) {
                is NetworkResult.Success -> {
                    println("✅ ProductViewModel - Product created successfully: ${result.data.name}")
                    // Refresh the products list to include the new product
                    refreshProducts()
                }
                is NetworkResult.Error -> {
                    println("❌ ProductViewModel - Error creating product: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to create product"
                    )
                }
                is NetworkResult.Loading -> {
                    println("📡 ProductViewModel - Create product loading state received")
                }
            }
        }
    }

    /**
     * Update an existing product
     */
    fun updateProduct(id: Long, productDTO: data.api.ProductDTO) {
        println("📦 ProductViewModel - Updating product: ${productDTO.name}")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = productRepository.updateProduct(id, productDTO)

            when (result) {
                is NetworkResult.Success -> {
                    println("✅ ProductViewModel - Product updated successfully: ${result.data.name}")
                    // Refresh the products list to show the updated product
                    refreshProducts()
                }
                is NetworkResult.Error -> {
                    println("❌ ProductViewModel - Error updating product: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to update product"
                    )
                }
                is NetworkResult.Loading -> {
                    println("📡 ProductViewModel - Update product loading state received")
                }
            }
        }
    }

    /**
     * Delete a product
     */
    fun deleteProduct(id: Long, productName: String) {
        println("📦 ProductViewModel - Deleting product: $productName (ID: $id)")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = productRepository.deleteProduct(id)

            when (result) {
                is NetworkResult.Success -> {
                    println("✅ ProductViewModel - Product deleted successfully: $productName")
                    // Set deletion success state first
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        deletionSuccess = true,
                        error = null
                    )
                    // Refresh the products list to remove the deleted product
                    refreshProducts()
                }
                is NetworkResult.Error -> {
                    println("❌ ProductViewModel - Error deleting product: ${result.exception.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Failed to delete product",
                        deletionSuccess = false
                    )
                }
                is NetworkResult.Loading -> {
                    println("📡 ProductViewModel - Delete product loading state received")
                }
            }
        }
    }

    /**
     * Export products to Excel format
     */
    fun exportProductsToExcel(): Flow<ExportResult> = flow {
        emit(ExportResult.Loading)
        try {
            val products = _uiState.value.products.map { it.toProductDTO() }
            val result = importExportService.exportProductsToExcel(products)

            if (result.isSuccess && result.getOrNull() == true) {
                emit(ExportResult.Success("تم تصدير المنتجات إلى Excel بنجاح"))
            } else {
                emit(ExportResult.Error("فشل في تصدير المنتجات إلى Excel"))
            }
        } catch (e: Exception) {
            emit(ExportResult.Error("خطأ في تصدير المنتجات: ${e.message}"))
        }
    }

    /**
     * Export products to CSV format
     */
    fun exportProductsToCsv(): Flow<ExportResult> = flow {
        emit(ExportResult.Loading)
        try {
            val products = _uiState.value.products.map { it.toProductDTO() }
            val result = importExportService.exportProductsToCsv(products)

            if (result.isSuccess && result.getOrNull() == true) {
                emit(ExportResult.Success("تم تصدير المنتجات إلى CSV بنجاح"))
            } else {
                emit(ExportResult.Error("فشل في تصدير المنتجات إلى CSV"))
            }
        } catch (e: Exception) {
            emit(ExportResult.Error("خطأ في تصدير المنتجات: ${e.message}"))
        }
    }

    /**
     * Export products to JSON format
     */
    fun exportProductsToJson(): Flow<ExportResult> = flow {
        emit(ExportResult.Loading)
        try {
            val products = _uiState.value.products.map { it.toProductDTO() }
            val result = importExportService.exportProductsToJson(products)

            if (result.isSuccess && result.getOrNull() == true) {
                emit(ExportResult.Success("تم تصدير المنتجات إلى JSON بنجاح"))
            } else {
                emit(ExportResult.Error("فشل في تصدير المنتجات إلى JSON"))
            }
        } catch (e: Exception) {
            emit(ExportResult.Error("خطأ في تصدير المنتجات: ${e.message}"))
        }
    }

    /**
     * Parse products from file (Step 1: Parse and validate)
     */
    fun parseProductsFromFile(): Flow<ParseResult> = flow {
        emit(ParseResult.Loading)
        try {
            val result = importExportService.parseProductsFromFile()

            if (result.isSuccess) {
                when (val parsedData = result.getOrNull()) {
                    is ProductImportExportService.ParsedImportData.Cancelled -> {
                        emit(ParseResult.Cancelled)
                    }
                    is ProductImportExportService.ParsedImportData.NoValidProducts -> {
                        emit(ParseResult.Error("لا توجد منتجات صالحة في الملف"))
                    }
                    is ProductImportExportService.ParsedImportData.Success -> {
                        val message = buildString {
                            append("تم تحليل ${parsedData.products.size} منتج من الملف: ${parsedData.fileName}")
                            if (parsedData.warnings.isNotEmpty()) {
                                append("\nتحذيرات: ${parsedData.warnings.size}")
                            }
                        }

                        emit(ParseResult.Success(message, parsedData.products, parsedData.warnings))
                    }
                    null -> {
                        emit(ParseResult.Error("خطأ غير متوقع في تحليل الملف"))
                    }
                }
            } else {
                emit(ParseResult.Error(result.exceptionOrNull()?.message ?: "فشل في تحليل الملف"))
            }
        } catch (e: Exception) {
            emit(ParseResult.Error("خطأ في تحليل الملف: ${e.message}"))
        }
    }

    /**
     * Upload products to database (Step 2: Database import)
     */
    fun uploadProductsToDatabase(products: List<ProductDTO>): Flow<ImportResult> = flow {
        emit(ImportResult.Loading)
        try {
            val result = importExportService.uploadProductsToDatabase(products)

            if (result.isSuccess) {
                when (val summary = result.getOrNull()) {
                    is ProductImportExportService.ImportSummary.Success -> {
                        // Refresh products list after successful import
                        refreshProducts()

                        val message = buildString {
                            append("تم رفع ${summary.successfulImports} منتج إلى قاعدة البيانات بنجاح")
                            if (summary.failedImports > 0) {
                                append("\nفشل في رفع ${summary.failedImports} منتج")
                            }
                        }

                        emit(ImportResult.Success(message, summary.errors))
                    }
                    else -> {
                        emit(ImportResult.Error("خطأ غير متوقع في رفع البيانات"))
                    }
                }
            } else {
                emit(ImportResult.Error(result.exceptionOrNull()?.message ?: "فشل في رفع المنتجات إلى قاعدة البيانات"))
            }
        } catch (e: Exception) {
            emit(ImportResult.Error("خطأ في رفع المنتجات: ${e.message}"))
        }
    }

    /**
     * Save sample CSV template
     */
    fun saveSampleCsvTemplate(): Flow<ExportResult> = flow {
        emit(ExportResult.Loading)
        try {
            val result = importExportService.saveSampleCsvTemplate()

            if (result.isSuccess && result.getOrNull() == true) {
                emit(ExportResult.Success("تم حفظ نموذج CSV بنجاح"))
            } else {
                emit(ExportResult.Error("فشل في حفظ نموذج CSV"))
            }
        } catch (e: Exception) {
            emit(ExportResult.Error("خطأ في حفظ نموذج CSV: ${e.message}"))
        }
    }

    /**
     * Clean up resources
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * UI State for Products screen
 */
data class ProductUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val totalProducts: Int = 0,
    val error: String? = null,
    val lastUpdated: kotlinx.datetime.LocalDateTime? = null,
    val deletionSuccess: Boolean = false
) {
    val hasData: Boolean get() = products.isNotEmpty()
    val hasError: Boolean get() = error != null
}

/**
 * Export operation result
 */
sealed class ExportResult {
    object Loading : ExportResult()
    data class Success(val message: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

/**
 * Parse operation result (Step 1)
 */
sealed class ParseResult {
    object Loading : ParseResult()
    object Cancelled : ParseResult()
    data class Success(val message: String, val products: List<ProductDTO>, val warnings: List<String> = emptyList()) : ParseResult()
    data class Error(val message: String) : ParseResult()
}

/**
 * Import operation result (Step 2)
 */
sealed class ImportResult {
    object Loading : ImportResult()
    object Cancelled : ImportResult()
    data class Success(val message: String, val details: List<String> = emptyList()) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

/**
 * Extension function to convert Product to ProductDTO with all available fields
 */
private fun Product.toProductDTO(): ProductDTO {
    return ProductDTO(
        id = this.id.toLong(),

        // Required fields
        name = this.name,
        price = this.price,
        stockQuantity = this.stock,

        // Basic product information
        description = this.description,
        category = this.category,
        sku = this.sku,
        costPrice = this.cost,
        brand = this.brand,
        modelNumber = this.modelNumber,
        barcode = this.barcode,

        // Physical properties
        weight = this.weight,
        length = this.length,
        width = this.width,
        height = this.height,

        // Product status and classification
        productStatus = this.productStatus,

        // Stock management
        minStockLevel = this.minStockLevel,
        maxStockLevel = this.maxStockLevel,
        reorderPoint = this.reorderPoint,
        reorderQuantity = this.reorderQuantity,

        // Supplier information
        supplierName = this.supplierName,
        supplierCode = this.supplierCode,

        // Product lifecycle
        warrantyPeriod = this.warrantyPeriod,
        expiryDate = this.expiryDate,
        manufacturingDate = this.manufacturingDate,

        // Tags and images
        tags = this.tags,
        imageUrl = this.imageUrl,
        additionalImages = this.additionalImages,

        // Product characteristics
        isSerialized = this.isSerialized,
        isDigital = this.isDigital,
        isTaxable = this.isTaxable,

        // Pricing and measurement
        taxRate = this.taxRate,
        unitOfMeasure = this.unitOfMeasure,
        discountPercentage = this.discountPercentage,

        // Warehouse and location
        locationInWarehouse = this.locationInWarehouse,

        // Sales tracking
        totalSold = this.totalSold,
        totalRevenue = this.totalRevenue,
        lastSoldDate = this.lastSoldDate,
        lastRestockedDate = this.lastRestockedDate,

        // Additional information
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
