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
            if (page == 0) {
                _customers.value = pageResponse.content
            } else {
                _customers.value = _customers.value + pageResponse.content
            }
        }.onError { exception ->
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
    
    suspend fun deleteCustomer(id: Long): NetworkResult<Unit> {
        _isLoading.value = true
        _error.value = null

        val result = customerApiService.deleteCustomer(id)

        result.onSuccess {
            _customers.value = _customers.value.filter { it.id != id }
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

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
