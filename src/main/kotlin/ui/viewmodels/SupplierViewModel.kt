package ui.viewmodels

import data.api.*
import data.repository.SupplierRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import data.*

/**
 * ViewModel for supplier management with comprehensive backend integration
 */
class SupplierViewModel(
    private val supplierRepository: SupplierRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Supplier data from repository
    val suppliers = supplierRepository.suppliers
    val isLoading = supplierRepository.isLoading
    val error = supplierRepository.error
    
    // UI State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedStatus = MutableStateFlow("الكل")
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()
    
    private val _selectedLocation = MutableStateFlow("الكل")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()
    
    private val _sortBy = MutableStateFlow("name")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()
    
    private val _showActiveOnly = MutableStateFlow(false)
    val showActiveOnly: StateFlow<Boolean> = _showActiveOnly.asStateFlow()
    
    private val _showWithOrdersOnly = MutableStateFlow(false)
    val showWithOrdersOnly: StateFlow<Boolean> = _showWithOrdersOnly.asStateFlow()
    
    // Analytics state
    private val _supplierAnalytics = MutableStateFlow<SupplierAnalyticsDTO?>(null)
    val supplierAnalytics: StateFlow<SupplierAnalyticsDTO?> = _supplierAnalytics.asStateFlow()

    private val _supplierAnalyticsOverview = MutableStateFlow<SupplierAnalyticsOverviewDTO?>(null)
    val supplierAnalyticsOverview: StateFlow<SupplierAnalyticsOverviewDTO?> = _supplierAnalyticsOverview.asStateFlow()

    private val _isLoadingAnalytics = MutableStateFlow(false)
    val isLoadingAnalytics: StateFlow<Boolean> = _isLoadingAnalytics.asStateFlow()

    // Top rated and high value suppliers
    private val _topRatedSuppliers = MutableStateFlow<List<SupplierDTO>>(emptyList())
    val topRatedSuppliers: StateFlow<List<SupplierDTO>> = _topRatedSuppliers.asStateFlow()

    private val _highValueSuppliers = MutableStateFlow<List<SupplierDTO>>(emptyList())
    val highValueSuppliers: StateFlow<List<SupplierDTO>> = _highValueSuppliers.asStateFlow()
    
    // Operation states
    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()
    
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()
    
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()
    
    // Success states
    private val _lastCreatedSupplier = MutableStateFlow<SupplierDTO?>(null)
    val lastCreatedSupplier: StateFlow<SupplierDTO?> = _lastCreatedSupplier.asStateFlow()
    
    private val _lastUpdatedSupplier = MutableStateFlow<SupplierDTO?>(null)
    val lastUpdatedSupplier: StateFlow<SupplierDTO?> = _lastUpdatedSupplier.asStateFlow()
    
    // Filtered suppliers based on search and filters
    val filteredSuppliers: StateFlow<List<SupplierDTO>> = combine(
        suppliers,
        searchQuery,
        selectedStatus,
        selectedLocation,
        showActiveOnly
    ) { suppliersList, query, status, location, activeOnly ->
        var filtered = suppliersList

        // Search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { supplier ->
                supplier.name.contains(query, ignoreCase = true) ||
                supplier.contactPerson?.contains(query, ignoreCase = true) == true ||
                supplier.email?.contains(query, ignoreCase = true) == true ||
                supplier.phone?.contains(query, ignoreCase = true) == true
            }
        }

        // Status filter
        if (status != "الكل") {
            val statusFilter = when(status) {
                "نشط" -> "ACTIVE"
                "غير نشط" -> "INACTIVE"
                "معلق" -> "SUSPENDED"
                else -> null
            }
            statusFilter?.let { filter ->
                filtered = filtered.filter { it.status == filter }
            }
        }

        // Location filter (based on city)
        if (location != "الكل") {
            filtered = filtered.filter { supplier ->
                supplier.city?.contains(location, ignoreCase = true) == true ||
                supplier.address?.contains(location, ignoreCase = true) == true
            }
        }

        // Active only filter
        if (activeOnly) {
            filtered = filtered.filter { it.status == "ACTIVE" }
        }

        filtered
    }.combine(showWithOrdersOnly) { filtered, withOrdersOnly ->
        if (withOrdersOnly) {
            filtered.filter { (it.totalOrders ?: 0) > 0 }
        } else {
            filtered
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        // Don't load data automatically - wait for explicit call
        // This prevents API calls when user is not authenticated
    }
    
    // Search functions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateSelectedStatus(status: String) {
        _selectedStatus.value = status
    }
    
    fun updateSelectedLocation(location: String) {
        _selectedLocation.value = location
    }
    
    fun updateSortBy(sortBy: String) {
        _sortBy.value = sortBy
    }
    
    fun toggleActiveOnly() {
        _showActiveOnly.value = !_showActiveOnly.value
    }
    
    fun toggleWithOrdersOnly() {
        _showWithOrdersOnly.value = !_showWithOrdersOnly.value
    }
    
    // Data loading functions
    suspend fun loadSuppliers(refresh: Boolean = false) {
        val page = if (refresh) 0 else (suppliers.value.size / 20)
        val statusFilter = if (selectedStatus.value != "الكل") {
            when(selectedStatus.value) {
                "نشط" -> "ACTIVE"
                "غير نشط" -> "INACTIVE"
                "معلق" -> "SUSPENDED"
                else -> null
            }
        } else null
        
        supplierRepository.loadSuppliers(
            page = page,
            size = 20,
            sortBy = sortBy.value,
            sortDir = "asc",
            status = statusFilter
        )
    }
    
    suspend fun searchSuppliers(query: String) {
        if (query.isBlank()) {
            loadSuppliers(refresh = true)
            return
        }
        
        supplierRepository.searchSuppliers(
            query = query,
            page = 0,
            size = 20,
            sortBy = sortBy.value,
            sortDir = "asc"
        )
    }
    
    // CRUD operations
    suspend fun createSupplier(supplierData: SupplierData): NetworkResult<SupplierDTO> {
        _isCreating.value = true

        val supplierDTO = SupplierDTO(
            name = supplierData.name,
            contactPerson = supplierData.contactPerson.ifBlank { null },
            phone = supplierData.phone.ifBlank { null },
            email = supplierData.email.ifBlank { null },
            address = supplierData.address.ifBlank { null },
            city = supplierData.city.ifBlank { null },
            country = supplierData.country.ifBlank { null },
            taxNumber = supplierData.taxNumber.ifBlank { null },
            paymentTerms = supplierData.paymentTerms,
            deliveryTerms = supplierData.deliveryTerms,
            rating = supplierData.rating,
            status = supplierData.status,
            notes = supplierData.notes.ifBlank { null }
        )

        val result = supplierRepository.createSupplier(supplierDTO)

        result.onSuccess { createdSupplier ->
            _lastCreatedSupplier.value = createdSupplier
        }

        _isCreating.value = false
        return result
    }

    suspend fun updateSupplier(id: Long, supplierData: SupplierData): NetworkResult<SupplierDTO> {
        _isUpdating.value = true

        val supplierDTO = SupplierDTO(
            id = id,
            name = supplierData.name,
            contactPerson = supplierData.contactPerson.ifBlank { null },
            phone = supplierData.phone.ifBlank { null },
            email = supplierData.email.ifBlank { null },
            address = supplierData.address.ifBlank { null },
            city = supplierData.city.ifBlank { null },
            country = supplierData.country.ifBlank { null },
            taxNumber = supplierData.taxNumber.ifBlank { null },
            paymentTerms = supplierData.paymentTerms,
            deliveryTerms = supplierData.deliveryTerms,
            rating = supplierData.rating,
            status = supplierData.status,
            notes = supplierData.notes.ifBlank { null }
        )

        val result = supplierRepository.updateSupplier(id, supplierDTO)

        result.onSuccess { updatedSupplier ->
            _lastUpdatedSupplier.value = updatedSupplier
        }

        _isUpdating.value = false
        return result
    }
    
    suspend fun deleteSupplier(id: Long): NetworkResult<Unit> {
        _isDeleting.value = true
        val result = supplierRepository.deleteSupplier(id)
        _isDeleting.value = false
        return result
    }
    
    // Analytics functions
    suspend fun loadSupplierAnalytics(supplierId: Long) {
        _isLoadingAnalytics.value = true

        viewModelScope.launch {
            try {
                val result = supplierRepository.getSupplierAnalyticsById(supplierId)
                result.onSuccess { analytics ->
                    _supplierAnalytics.value = analytics
                }.onError { exception ->
                    supplierRepository.clearError()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoadingAnalytics.value = false
            }
        }
    }

    suspend fun loadSupplierAnalyticsOverview() {
        _isLoadingAnalytics.value = true

        viewModelScope.launch {
            try {
                val result = supplierRepository.getSupplierAnalytics()
                result.onSuccess { overview ->
                    _supplierAnalyticsOverview.value = overview
                }.onError { exception ->
                    supplierRepository.clearError()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoadingAnalytics.value = false
            }
        }
    }

    suspend fun loadTopRatedSuppliers(minRating: Double = 4.0) {
        viewModelScope.launch {
            try {
                val result = supplierRepository.getTopRatedSuppliers(minRating)
                result.onSuccess { suppliers ->
                    _topRatedSuppliers.value = suppliers
                }.onError { exception ->
                    supplierRepository.clearError()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    suspend fun loadHighValueSuppliers(minAmount: Double = 10000.0) {
        viewModelScope.launch {
            try {
                val result = supplierRepository.getHighValueSuppliers(minAmount)
                result.onSuccess { suppliers ->
                    _highValueSuppliers.value = suppliers
                }.onError { exception ->
                    supplierRepository.clearError()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    suspend fun updateSupplierRating(id: Long, rating: Double): NetworkResult<SupplierDTO> {
        return supplierRepository.updateSupplierRating(id, rating)
    }
    
    // Utility functions
    fun clearError() {
        supplierRepository.clearError()
    }
    
    suspend fun refreshSuppliers() {
        supplierRepository.refreshSuppliers()
    }
    
    fun clearLastCreatedSupplier() {
        _lastCreatedSupplier.value = null
    }
    
    fun clearLastUpdatedSupplier() {
        _lastUpdatedSupplier.value = null
    }
}

// Enhanced data class for supplier form data
data class SupplierData(
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String,
    val city: String = "",
    val country: String = "",
    val taxNumber: String = "",
    val paymentTerms: String = "NET_30",
    val deliveryTerms: String = "FOB_DESTINATION",
    val rating: Double = 0.0,
    val status: String = "ACTIVE",
    val notes: String = ""
)

// UI State for supplier management
data class SupplierUiState(
    val isLoading: Boolean = false,
    val suppliers: List<SupplierDTO> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val selectedStatus: String = "الكل",
    val selectedLocation: String = "الكل",
    val sortBy: String = "name",
    val showActiveOnly: Boolean = false,
    val showWithOrdersOnly: Boolean = false
)
