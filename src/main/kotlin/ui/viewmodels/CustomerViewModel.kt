package ui.viewmodels

import data.api.*
import data.repository.CustomerRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * ViewModel for customer management with comprehensive backend integration
 */
class CustomerViewModel(
    private val customerRepository: CustomerRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Customer data from repository
    val customers = customerRepository.customers
    val isLoading = customerRepository.isLoading
    val error = customerRepository.error
    
    // UI State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCustomer = MutableStateFlow<CustomerDTO?>(null)
    val selectedCustomer: StateFlow<CustomerDTO?> = _selectedCustomer.asStateFlow()
    
    private val _isCreatingCustomer = MutableStateFlow(false)
    val isCreatingCustomer: StateFlow<Boolean> = _isCreatingCustomer.asStateFlow()
    
    private val _isUpdatingCustomer = MutableStateFlow(false)
    val isUpdatingCustomer: StateFlow<Boolean> = _isUpdatingCustomer.asStateFlow()
    
    private val _isDeletingCustomer = MutableStateFlow(false)
    val isDeletingCustomer: StateFlow<Boolean> = _isDeletingCustomer.asStateFlow()
    
    private val _sortBy = MutableStateFlow("name")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()
    
    private val _sortDirection = MutableStateFlow("asc")
    val sortDirection: StateFlow<String> = _sortDirection.asStateFlow()
    
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()
    
    // Computed properties
    val filteredCustomers: StateFlow<List<CustomerDTO>> = combine(
        customers,
        searchQuery,
        sortBy,
        sortDirection
    ) { customerList, query, sort, direction ->
        var filtered = if (query.isNotEmpty()) {
            customerList.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                customer.email?.contains(query, ignoreCase = true) == true ||
                customer.phone?.contains(query, ignoreCase = true) == true ||
                customer.address?.contains(query, ignoreCase = true) == true
            }
        } else {
            customerList
        }
        
        // Apply sorting
        filtered = when (sort) {
            "name" -> if (direction == "asc") filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
            "email" -> if (direction == "asc") filtered.sortedBy { it.email ?: "" } else filtered.sortedByDescending { it.email ?: "" }
            "phone" -> if (direction == "asc") filtered.sortedBy { it.phone ?: "" } else filtered.sortedByDescending { it.phone ?: "" }
            "address" -> if (direction == "asc") filtered.sortedBy { it.address ?: "" } else filtered.sortedByDescending { it.address ?: "" }
            "customerType" -> if (direction == "asc") filtered.sortedBy { it.customerType ?: "" } else filtered.sortedByDescending { it.customerType ?: "" }
            "creditLimit" -> if (direction == "asc") filtered.sortedBy { it.creditLimit ?: 0.0 } else filtered.sortedByDescending { it.creditLimit ?: 0.0 }
            else -> filtered
        }
        
        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        // Load initial customers
        viewModelScope.launch {
            loadCustomers()
        }
    }
    
    // Customer operations
    suspend fun loadCustomers(
        page: Int = 0,
        size: Int = 20,
        refresh: Boolean = false
    ): NetworkResult<PageResponse<CustomerDTO>> {
        if (refresh) {
            _currentPage.value = 0
        }
        
        val result = customerRepository.loadCustomers(
            page = page,
            size = size,
            sortBy = _sortBy.value,
            sortDir = _sortDirection.value
        )
        
        result.onSuccess { pageResponse ->
            _hasMorePages.value = !pageResponse.last
            if (page == 0) {
                _currentPage.value = 0
            } else {
                _currentPage.value = page
            }
        }
        
        return result
    }
    
    suspend fun loadMoreCustomers(): NetworkResult<PageResponse<CustomerDTO>> {
        if (!_hasMorePages.value || isLoading.value) {
            return NetworkResult.Error(ApiException.ValidationError(mapOf("pagination" to listOf("No more pages or already loading"))))
        }

        val nextPage = _currentPage.value + 1
        return loadCustomers(page = nextPage)
    }
    
    suspend fun searchCustomers(query: String): NetworkResult<PageResponse<CustomerDTO>> {
        _searchQuery.value = query
        return if (query.isNotEmpty()) {
            customerRepository.searchCustomers(query, 0, 50)
        } else {
            loadCustomers(refresh = true)
        }
    }
    
    suspend fun getCustomerById(id: Long): NetworkResult<CustomerDTO> {
        return customerRepository.getCustomerById(id)
    }
    
    suspend fun createCustomer(customer: CustomerDTO): NetworkResult<CustomerDTO> {
        _isCreatingCustomer.value = true
        
        val result = customerRepository.createCustomer(customer)
        
        result.onSuccess {
            // Refresh the customer list to show the new customer
            loadCustomers(refresh = true)
        }
        
        _isCreatingCustomer.value = false
        return result
    }
    
    suspend fun updateCustomer(customer: CustomerDTO): NetworkResult<CustomerDTO> {
        _isUpdatingCustomer.value = true

        val result = if (customer.id != null) {
            customerRepository.updateCustomer(customer.id, customer)
        } else {
            NetworkResult.Error(ApiException.ValidationError(mapOf("id" to listOf("Customer ID is required for update"))))
        }

        result.onSuccess {
            // Refresh the customer list to show updated data
            loadCustomers(refresh = true)
        }

        _isUpdatingCustomer.value = false
        return result
    }
    
    suspend fun deleteCustomer(id: Long): NetworkResult<Unit> {
        _isDeletingCustomer.value = true
        
        val result = customerRepository.deleteCustomer(id)
        
        result.onSuccess {
            // Refresh the customer list to remove deleted customer
            loadCustomers(refresh = true)
        }
        
        _isDeletingCustomer.value = false
        return result
    }
    
    // UI State management
    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            searchCustomers(query)
        }
    }
    
    fun updateSorting(sortBy: String, sortDirection: String = "asc") {
        _sortBy.value = sortBy
        _sortDirection.value = sortDirection
        
        viewModelScope.launch {
            loadCustomers(refresh = true)
        }
    }
    
    fun selectCustomer(customer: CustomerDTO?) {
        _selectedCustomer.value = customer
    }
    
    fun clearError() {
        customerRepository.clearError()
    }
    
    suspend fun refreshCustomers() {
        loadCustomers(refresh = true)
    }
    
    // Analytics and statistics
    fun getCustomerStats(): Map<String, Any> {
        val customerList = customers.value
        val creditLimits = customerList.mapNotNull { it.creditLimit }
        val averageCreditLimit = if (creditLimits.isNotEmpty()) {
            creditLimits.average()
        } else {
            0.0
        }

        return mapOf(
            "totalCustomers" to customerList.size,
            "activeCustomers" to customerList.count { it.customerStatus == "ACTIVE" },
            "premiumCustomers" to customerList.count { it.customerType == "PREMIUM" || it.customerType == "VIP" },
            "averageCreditLimit" to averageCreditLimit,
            "totalCreditLimit" to creditLimits.sum(),
            "customersWithEmail" to customerList.count { !it.email.isNullOrBlank() },
            "customersWithPhone" to customerList.count { !it.phone.isNullOrBlank() }
        )
    }
    
    fun getTopCustomersByCredit(limit: Int = 5): List<CustomerDTO> {
        return customers.value
            .filter { it.creditLimit != null && it.creditLimit > 0 }
            .sortedByDescending { it.creditLimit }
            .take(limit)
    }
    
    fun getCustomersByType(): Map<String, Int> {
        return customers.value
            .groupBy { it.customerType ?: "REGULAR" }
            .mapValues { (_, customerList) -> customerList.size }
    }

    fun getCustomersByStatus(): Map<String, Int> {
        return customers.value
            .groupBy { it.customerStatus ?: "ACTIVE" }
            .mapValues { (_, customerList) -> customerList.size }
    }
    
    // Cleanup
    fun onCleared() {
        viewModelScope.cancel()
    }
}
