package data.repository

import data.api.*
import data.api.services.CustomerApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for customer data management
 */
class CustomerRepository(private val customerApiService: CustomerApiService) {
    
    private val _customers = MutableStateFlow<List<CustomerDTO>>(emptyList())
    val customers: StateFlow<List<CustomerDTO>> = _customers.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    suspend fun loadCustomers(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDir: String = "asc"
    ): NetworkResult<PageResponse<CustomerDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = customerApiService.getAllCustomers(page, size, sortBy, sortDir)
        
        result.onSuccess { pageResponse ->
            println("🔍 CustomerRepository - Successfully loaded ${pageResponse.content.size} customers")
            println("🔍 CustomerRepository - Total elements: ${pageResponse.totalElements}")
            println("🔍 CustomerRepository - Page info: ${pageResponse.pageable.pageNumber}/${pageResponse.totalPages}")

            if (page == 0) {
                _customers.value = pageResponse.content
                println("🔍 CustomerRepository - Set customers list with ${pageResponse.content.size} items")
            } else {
                _customers.value = _customers.value + pageResponse.content
                println("🔍 CustomerRepository - Added ${pageResponse.content.size} customers to existing list")
            }
        }.onError { exception ->
            println("❌ CustomerRepository - Error loading customers: ${exception.message}")
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getCustomerById(id: Long): NetworkResult<CustomerDTO> {
        return customerApiService.getCustomerById(id)
    }
    
    suspend fun createCustomer(customer: CustomerDTO): NetworkResult<CustomerDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = customerApiService.createCustomer(customer)
        
        result.onSuccess { newCustomer ->
            _customers.value = _customers.value + newCustomer
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun updateCustomer(id: Long, customer: CustomerDTO): NetworkResult<CustomerDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = customerApiService.updateCustomer(id, customer)
        
        result.onSuccess { updatedCustomer ->
            _customers.value = _customers.value.map { 
                if (it.id == id) updatedCustomer else it 
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Soft delete customer (recommended approach)
     */
    suspend fun deleteCustomer(
        id: Long,
        deletedBy: String? = null,
        reason: String? = null
    ): NetworkResult<Unit> {
        _isLoading.value = true
        _error.value = null

        val result = customerApiService.deleteCustomer(id, deletedBy, reason)

        result.onSuccess {
            // For soft delete, we remove from the active customers list
            _customers.value = _customers.value.filter { it.id != id }
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Hard delete customer with cascade (removes all associated data)
     */
    suspend fun deleteCustomerWithCascade(id: Long): NetworkResult<Unit> {
        _isLoading.value = true
        _error.value = null

        val result = customerApiService.deleteCustomerWithCascade(id)

        result.onSuccess {
            _customers.value = _customers.value.filter { it.id != id }
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Force delete customer (legacy compatibility)
     */
    suspend fun forceDeleteCustomer(id: Long): NetworkResult<Unit> {
        _isLoading.value = true
        _error.value = null

        val result = customerApiService.forceDeleteCustomer(id)

        result.onSuccess {
            _customers.value = _customers.value.filter { it.id != id }
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Restore a soft-deleted customer
     */
    suspend fun restoreCustomer(id: Long): NetworkResult<CustomerDTO> {
        _isLoading.value = true
        _error.value = null

        val result = customerApiService.restoreCustomer(id)

        result.onSuccess { restoredCustomer ->
            // Add the restored customer back to the active list
            _customers.value = _customers.value + restoredCustomer
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Get paginated list of soft-deleted customers
     */
    suspend fun getDeletedCustomers(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "deletedAt",
        sortDir: String = "desc"
    ): NetworkResult<PageResponse<CustomerDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = customerApiService.getDeletedCustomers(page, size, sortBy, sortDir)

        result.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    suspend fun searchCustomers(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<PageResponse<CustomerDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = customerApiService.searchCustomers(query, page, size)
        
        result.onSuccess { pageResponse ->
            _customers.value = pageResponse.content
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun refreshCustomers() {
        // Trigger a refresh by loading the first page
        // This should be called from a coroutine scope
    }
}
