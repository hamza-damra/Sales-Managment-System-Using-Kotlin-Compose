package data.repository

import data.api.*
import data.api.services.SupplierApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for supplier data management
 */
class SupplierRepository(
    private val supplierApiService: SupplierApiService
) {
    
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
    fun createSupplier(supplier: SupplierDTO): Flow<NetworkResult<SupplierDTO>> = flow {
        emit(supplierApiService.createSupplier(supplier))
    }
    
    /**
     * Update existing supplier
     */
    fun updateSupplier(id: Long, supplier: SupplierDTO): Flow<NetworkResult<SupplierDTO>> = flow {
        emit(supplierApiService.updateSupplier(id, supplier))
    }
    
    /**
     * Delete supplier
     */
    fun deleteSupplier(id: Long): Flow<NetworkResult<Unit>> = flow {
        emit(supplierApiService.deleteSupplier(id))
    }
    
    /**
     * Search suppliers
     */
    fun searchSuppliers(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = "name",
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR
    ): Flow<NetworkResult<PageResponse<SupplierDTO>>> = flow {
        emit(supplierApiService.searchSuppliers(query, page, size, sortBy, sortDir))
    }
}
