package ui.viewmodels

import data.api.*
import data.repository.ReturnRepository
import data.repository.CustomerRepository
import data.repository.ProductRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * ViewModel for returns management with comprehensive backend integration
 */
class ReturnsViewModel(
    private val returnRepository: ReturnRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Returns data
    private val _returns = MutableStateFlow<List<ReturnDTO>>(emptyList())
    val returns: StateFlow<List<ReturnDTO>> = _returns.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // UI State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedStatus = MutableStateFlow<String?>(null)
    val selectedStatus: StateFlow<String?> = _selectedStatus.asStateFlow()
    
    private val _selectedReturn = MutableStateFlow<ReturnDTO?>(null)
    val selectedReturn: StateFlow<ReturnDTO?> = _selectedReturn.asStateFlow()
    
    private val _isCreatingReturn = MutableStateFlow(false)
    val isCreatingReturn: StateFlow<Boolean> = _isCreatingReturn.asStateFlow()
    
    private val _isUpdatingReturn = MutableStateFlow(false)
    val isUpdatingReturn: StateFlow<Boolean> = _isUpdatingReturn.asStateFlow()
    
    private val _isDeletingReturn = MutableStateFlow(false)
    val isDeletingReturn: StateFlow<Boolean> = _isDeletingReturn.asStateFlow()
    
    private val _isProcessingReturn = MutableStateFlow(false)
    val isProcessingReturn: StateFlow<Boolean> = _isProcessingReturn.asStateFlow()
    
    private val _sortBy = MutableStateFlow("returnDate")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()
    
    private val _sortDirection = MutableStateFlow("desc")
    val sortDirection: StateFlow<String> = _sortDirection.asStateFlow()
    
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()
    
    // Success states for UI feedback
    private val _lastCreatedReturn = MutableStateFlow<ReturnDTO?>(null)
    val lastCreatedReturn: StateFlow<ReturnDTO?> = _lastCreatedReturn.asStateFlow()
    
    private val _lastUpdatedReturn = MutableStateFlow<ReturnDTO?>(null)
    val lastUpdatedReturn: StateFlow<ReturnDTO?> = _lastUpdatedReturn.asStateFlow()
    
    private val _lastDeletedReturnId = MutableStateFlow<Long?>(null)
    val lastDeletedReturnId: StateFlow<Long?> = _lastDeletedReturnId.asStateFlow()
    
    // Analytics data
    private val _totalReturns = MutableStateFlow(0)
    val totalReturns: StateFlow<Int> = _totalReturns.asStateFlow()
    
    private val _pendingReturns = MutableStateFlow(0)
    val pendingReturns: StateFlow<Int> = _pendingReturns.asStateFlow()
    
    private val _totalRefundAmount = MutableStateFlow(0.0)
    val totalRefundAmount: StateFlow<Double> = _totalRefundAmount.asStateFlow()
    
    private val _returnRate = MutableStateFlow(0.0)
    val returnRate: StateFlow<Double> = _returnRate.asStateFlow()
    
    // Computed properties
    val filteredReturns: StateFlow<List<ReturnDTO>> = combine(
        returns,
        searchQuery,
        selectedStatus,
        sortBy,
        sortDirection
    ) { returnsList, query, status, sort, direction ->
        var filtered = returnsList
        
        // Apply search filter
        if (query.isNotEmpty()) {
            filtered = filtered.filter { returnItem ->
                returnItem.returnNumber?.contains(query, ignoreCase = true) == true ||
                returnItem.originalSaleNumber?.contains(query, ignoreCase = true) == true ||
                returnItem.customerName?.contains(query, ignoreCase = true) == true ||
                returnItem.notes?.contains(query, ignoreCase = true) == true ||
                returnItem.id.toString().contains(query, ignoreCase = true)
            }
        }
        
        // Apply status filter
        if (!status.isNullOrEmpty() && status != "الكل") {
            val statusFilter = when (status) {
                "في الانتظار" -> "PENDING"
                "موافق عليه" -> "APPROVED"
                "مرفوض" -> "REJECTED"
                "تم الاسترداد" -> "REFUNDED"
                "تم الاستبدال" -> "EXCHANGED"
                else -> status
            }
            filtered = filtered.filter { it.status == statusFilter }
        }
        
        // Apply sorting
        filtered = when (sort) {
            "returnDate" -> if (direction == "asc") filtered.sortedBy { it.returnDate } else filtered.sortedByDescending { it.returnDate }
            "totalRefundAmount" -> if (direction == "asc") filtered.sortedBy { it.totalRefundAmount } else filtered.sortedByDescending { it.totalRefundAmount }
            "status" -> if (direction == "asc") filtered.sortedBy { it.status } else filtered.sortedByDescending { it.status }
            "customerName" -> if (direction == "asc") filtered.sortedBy { it.customerName ?: "" } else filtered.sortedByDescending { it.customerName ?: "" }
            "reason" -> if (direction == "asc") filtered.sortedBy { it.reason } else filtered.sortedByDescending { it.reason }
            else -> filtered
        }
        
        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        // Load initial returns
        viewModelScope.launch {
            loadReturns()
        }
    }
    
    // Returns operations
    suspend fun loadReturns(
        page: Int = 0,
        size: Int = 20,
        refresh: Boolean = false
    ): NetworkResult<PageResponse<ReturnDTO>> {
        if (refresh) {
            _currentPage.value = 0
        }
        
        _isLoading.value = true
        _error.value = null
        
        val result = returnRepository.getAllReturns(
            page = page,
            size = size,
            sortBy = _sortBy.value,
            sortDir = _sortDirection.value,
            status = _selectedStatus.value
        ).first()
        
        result.onSuccess { pageResponse ->
            if (page == 0 || refresh) {
                _returns.value = pageResponse.content
            } else {
                _returns.value = _returns.value + pageResponse.content
            }
            _currentPage.value = page
            _hasMorePages.value = page < pageResponse.totalPages - 1
            
            // Update analytics
            _totalReturns.value = pageResponse.totalElements.toInt()
            updateAnalytics()
        }.onError { exception ->
            _error.value = exception.message ?: "خطأ في تحميل المرتجعات"
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getReturnById(id: Long): NetworkResult<ReturnDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = returnRepository.getReturnById(id).first()
        
        result.onSuccess { returnItem ->
            _selectedReturn.value = returnItem
        }.onError { exception ->
            _error.value = exception.message ?: "خطأ في تحميل تفاصيل المرتجع"
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun createReturn(returnData: ReturnDTO): NetworkResult<ReturnDTO> {
        _isCreatingReturn.value = true
        _error.value = null
        
        val result = returnRepository.createReturn(returnData).first()
        
        result.onSuccess { createdReturn ->
            _lastCreatedReturn.value = createdReturn
            // Refresh the list to include the new return
            loadReturns(refresh = true)
        }.onError { exception ->
            _error.value = exception.message ?: "خطأ في إنشاء المرتجع"
        }
        
        _isCreatingReturn.value = false
        return result
    }
    
    suspend fun updateReturn(id: Long, returnData: ReturnDTO): NetworkResult<ReturnDTO> {
        _isUpdatingReturn.value = true
        _error.value = null
        
        val result = returnRepository.updateReturn(id, returnData).first()
        
        result.onSuccess { updatedReturn ->
            _lastUpdatedReturn.value = updatedReturn
            // Update the return in the list
            _returns.value = _returns.value.map { 
                if (it.id == id) updatedReturn else it 
            }
        }.onError { exception ->
            _error.value = exception.message ?: "خطأ في تحديث المرتجع"
        }
        
        _isUpdatingReturn.value = false
        return result
    }
    
    suspend fun deleteReturn(id: Long): NetworkResult<Unit> {
        _isDeletingReturn.value = true
        _error.value = null
        
        val result = returnRepository.deleteReturn(id).first()
        
        result.onSuccess {
            _lastDeletedReturnId.value = id
            // Remove the return from the list
            _returns.value = _returns.value.filter { it.id != id }
            updateAnalytics()
        }.onError { exception ->
            _error.value = exception.message ?: "خطأ في حذف المرتجع"
        }
        
        _isDeletingReturn.value = false
        return result
    }
    
    suspend fun approveReturn(id: Long, notes: String, processedBy: String): NetworkResult<ReturnDTO> {
        _isProcessingReturn.value = true
        _error.value = null
        
        val result = returnRepository.approveReturn(id, notes, processedBy).first()
        
        result.onSuccess { approvedReturn ->
            _lastUpdatedReturn.value = approvedReturn
            // Update the return in the list
            _returns.value = _returns.value.map { 
                if (it.id == id) approvedReturn else it 
            }
            updateAnalytics()
        }.onError { exception ->
            _error.value = exception.message ?: "خطأ في الموافقة على المرتجع"
        }
        
        _isProcessingReturn.value = false
        return result
    }
    
    suspend fun rejectReturn(id: Long, notes: String, processedBy: String): NetworkResult<ReturnDTO> {
        _isProcessingReturn.value = true
        _error.value = null
        
        val result = returnRepository.rejectReturn(id, notes, processedBy).first()
        
        result.onSuccess { rejectedReturn ->
            _lastUpdatedReturn.value = rejectedReturn
            // Update the return in the list
            _returns.value = _returns.value.map { 
                if (it.id == id) rejectedReturn else it 
            }
            updateAnalytics()
        }.onError { exception ->
            _error.value = exception.message ?: "خطأ في رفض المرتجع"
        }
        
        _isProcessingReturn.value = false
        return result
    }
    
    suspend fun processRefund(id: Long, refundMethod: String, notes: String, processedBy: String): NetworkResult<ReturnDTO> {
        _isProcessingReturn.value = true
        _error.value = null
        
        val result = returnRepository.processRefund(id, refundMethod, notes, processedBy).first()
        
        result.onSuccess { refundedReturn ->
            _lastUpdatedReturn.value = refundedReturn
            // Update the return in the list
            _returns.value = _returns.value.map { 
                if (it.id == id) refundedReturn else it 
            }
            updateAnalytics()
        }.onError { exception ->
            _error.value = exception.message ?: "خطأ في معالجة الاسترداد"
        }
        
        _isProcessingReturn.value = false
        return result
    }
    
    // UI State management
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateSelectedStatus(status: String?) {
        _selectedStatus.value = status
        // Reload data with new filter
        viewModelScope.launch {
            loadReturns(refresh = true)
        }
    }
    
    fun updateSorting(sortBy: String, direction: String) {
        _sortBy.value = sortBy
        _sortDirection.value = direction
        // Reload data with new sorting
        viewModelScope.launch {
            loadReturns(refresh = true)
        }
    }
    
    fun selectReturn(returnItem: ReturnDTO?) {
        _selectedReturn.value = returnItem
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearLastCreatedReturn() {
        _lastCreatedReturn.value = null
    }
    
    fun clearLastUpdatedReturn() {
        _lastUpdatedReturn.value = null
    }
    
    fun clearLastDeletedReturnId() {
        _lastDeletedReturnId.value = null
    }
    
    // Analytics calculation
    private fun updateAnalytics() {
        val currentReturns = _returns.value
        _pendingReturns.value = currentReturns.count { it.status == "PENDING" }
        _totalRefundAmount.value = currentReturns.sumOf { it.totalRefundAmount }
        // Return rate calculation would need sales data - placeholder for now
        _returnRate.value = 3.2 // This should be calculated based on sales vs returns
    }
    
    // Load more returns for pagination
    suspend fun loadMoreReturns() {
        if (_hasMorePages.value && !_isLoading.value) {
            loadReturns(page = _currentPage.value + 1)
        }
    }
    
    // Refresh all data
    suspend fun refreshData() {
        loadReturns(refresh = true)
    }
    
    // Cleanup
    fun onCleared() {
        viewModelScope.cancel()
    }
}

// Data class for return form data
data class ReturnFormData(
    val originalSaleId: Long,
    val customerId: Long,
    val reason: String,
    val totalRefundAmount: Double,
    val notes: String,
    val refundMethod: String,
    val items: List<ReturnItemFormData>
)

data class ReturnItemFormData(
    val originalSaleItemId: Long,
    val productId: Long,
    val returnQuantity: Int,
    val originalUnitPrice: Double,
    val refundAmount: Double,
    val itemCondition: String
)
