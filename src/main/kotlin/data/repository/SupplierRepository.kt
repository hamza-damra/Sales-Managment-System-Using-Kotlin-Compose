package data.repository

import data.api.*
import data.api.services.SupplierApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Repository for supplier data management with state management
 */
class SupplierRepository(
    private val supplierApiService: SupplierApiService
) {
    // State management
    private val _suppliers = MutableStateFlow<List<SupplierDTO>>(emptyList())
    val suppliers: StateFlow<List<SupplierDTO>> = _suppliers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load suppliers with state management
     */
    suspend fun loadSuppliers(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDir: String = "asc",
        status: String? = null
    ): NetworkResult<PageResponse<SupplierDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = supplierApiService.getAllSuppliers(page, size, sortBy, sortDir, status)

        result.onSuccess { pageResponse ->
            if (page == 0) {
                _suppliers.value = pageResponse.content
            } else {
                _suppliers.value = _suppliers.value + pageResponse.content
            }
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Get all suppliers with pagination
     */
    fun getAllSuppliers(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR,
        status: String? = null
    ): Flow<NetworkResult<PageResponse<SupplierDTO>>> = flow {
        emit(supplierApiService.getAllSuppliers(page, size, sortBy, sortDir, status))
    }

    /**
     * Get supplier by ID
     */
    fun getSupplierById(id: Long): Flow<NetworkResult<SupplierDTO>> = flow {
        emit(supplierApiService.getSupplierById(id))
    }

    /**
     * Create new supplier
     */
    suspend fun createSupplier(supplier: SupplierDTO): NetworkResult<SupplierDTO> {
        _isLoading.value = true
        _error.value = null

        val result = supplierApiService.createSupplier(supplier)

        result.onSuccess { createdSupplier ->
            _suppliers.value = _suppliers.value + createdSupplier
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Update existing supplier
     */
    suspend fun updateSupplier(id: Long, supplier: SupplierDTO): NetworkResult<SupplierDTO> {
        _isLoading.value = true
        _error.value = null

        val result = supplierApiService.updateSupplier(id, supplier)

        result.onSuccess { updatedSupplier ->
            _suppliers.value = _suppliers.value.map {
                if (it.id == id) updatedSupplier else it
            }
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Delete supplier
     */
    suspend fun deleteSupplier(id: Long): NetworkResult<Unit> {
        _isLoading.value = true
        _error.value = null

        val result = supplierApiService.deleteSupplier(id)

        result.onSuccess {
            _suppliers.value = _suppliers.value.filter { it.id != id }
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Search suppliers
     */
    suspend fun searchSuppliers(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = "name",
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR
    ): NetworkResult<PageResponse<SupplierDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = supplierApiService.searchSuppliers(query, page, size, sortBy, sortDir)

        result.onSuccess { pageResponse ->
            if (page == 0) {
                _suppliers.value = pageResponse.content
            } else {
                _suppliers.value = _suppliers.value + pageResponse.content
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
     * Refresh suppliers data
     */
    suspend fun refreshSuppliers() {
        loadSuppliers(page = 0)
    }
}
