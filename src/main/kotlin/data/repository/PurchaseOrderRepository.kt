package data.repository

import data.api.*
import data.api.services.PurchaseOrderApiService
import kotlinx.coroutines.flow.*

/**
 * Repository for purchase order data management
 * Provides centralized access to purchase order data with caching and state management
 */
class PurchaseOrderRepository(
    private val purchaseOrderApiService: PurchaseOrderApiService
) {
    
    // State management
    private val _purchaseOrders = MutableStateFlow<List<PurchaseOrderDTO>>(emptyList())
    val purchaseOrders: StateFlow<List<PurchaseOrderDTO>> = _purchaseOrders.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()
    
    private val _totalElements = MutableStateFlow(0L)
    val totalElements: StateFlow<Long> = _totalElements.asStateFlow()
    
    private val _hasNextPage = MutableStateFlow(false)
    val hasNextPage: StateFlow<Boolean> = _hasNextPage.asStateFlow()
    
    // Analytics state
    private val _analytics = MutableStateFlow<PurchaseOrderAnalyticsDTO?>(null)
    val analytics: StateFlow<PurchaseOrderAnalyticsDTO?> = _analytics.asStateFlow()
    
    /**
     * Load purchase orders with pagination and filtering
     */
    suspend fun loadPurchaseOrders(
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "orderDate",
        sortDir: String = "desc",
        status: String? = null,
        supplierId: Long? = null,
        priority: String? = null,
        fromDate: String? = null,
        toDate: String? = null,
        append: Boolean = false
    ) {
        _isLoading.value = true
        _error.value = null
        
        val result = purchaseOrderApiService.getAllPurchaseOrders(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir,
            status = status,
            supplierId = supplierId,
            priority = priority,
            fromDate = fromDate,
            toDate = toDate
        )
        
        result.onSuccess { pageResponse ->
            val newOrders = pageResponse.content
            _purchaseOrders.value = if (append && page > 0) {
                _purchaseOrders.value + newOrders
            } else {
                newOrders
            }
            
            _currentPage.value = pageResponse.pageable.pageNumber
            _totalPages.value = pageResponse.totalPages
            _totalElements.value = pageResponse.totalElements
            _hasNextPage.value = !pageResponse.last
            
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
    }
    
    /**
     * Get purchase order by ID
     */
    fun getPurchaseOrderById(id: Long): Flow<NetworkResult<PurchaseOrderDTO>> = flow {
        emit(purchaseOrderApiService.getPurchaseOrderById(id))
    }
    
    /**
     * Create new purchase order
     */
    suspend fun createPurchaseOrder(order: PurchaseOrderDTO): NetworkResult<PurchaseOrderDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = purchaseOrderApiService.createPurchaseOrder(order)
        
        result.onSuccess { createdOrder ->
            // Add to the beginning of the list
            _purchaseOrders.value = listOf(createdOrder) + _purchaseOrders.value
            _totalElements.value = _totalElements.value + 1
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Update existing purchase order
     */
    suspend fun updatePurchaseOrder(id: Long, order: PurchaseOrderDTO): NetworkResult<PurchaseOrderDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = purchaseOrderApiService.updatePurchaseOrder(id, order)
        
        result.onSuccess { updatedOrder ->
            _purchaseOrders.value = _purchaseOrders.value.map {
                if (it.id == id) updatedOrder else it
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Delete purchase order
     */
    suspend fun deletePurchaseOrder(id: Long): NetworkResult<Unit> {
        _isLoading.value = true
        _error.value = null
        
        val result = purchaseOrderApiService.deletePurchaseOrder(id)
        
        result.onSuccess {
            _purchaseOrders.value = _purchaseOrders.value.filter { it.id != id }
            _totalElements.value = maxOf(0, _totalElements.value - 1)
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Search purchase orders
     */
    suspend fun searchPurchaseOrders(
        query: String,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "orderDate",
        sortDir: String = "desc",
        append: Boolean = false
    ) {
        _isLoading.value = true
        _error.value = null
        
        val result = purchaseOrderApiService.searchPurchaseOrders(
            query = query,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir
        )
        
        result.onSuccess { pageResponse ->
            val newOrders = pageResponse.content
            _purchaseOrders.value = if (append && page > 0) {
                _purchaseOrders.value + newOrders
            } else {
                newOrders
            }
            
            _currentPage.value = pageResponse.pageable.pageNumber
            _totalPages.value = pageResponse.totalPages
            _totalElements.value = pageResponse.totalElements
            _hasNextPage.value = !pageResponse.last
            
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
    }
    
    /**
     * Update purchase order status
     */
    suspend fun updatePurchaseOrderStatus(
        id: Long, 
        status: String, 
        notes: String? = null,
        actualDeliveryDate: String? = null
    ): NetworkResult<PurchaseOrderDTO> {
        _isLoading.value = true
        _error.value = null
        
        val statusRequest = StatusUpdateRequestDTO(
            status = status,
            notes = notes,
            actualDeliveryDate = actualDeliveryDate
        )
        
        val result = purchaseOrderApiService.updatePurchaseOrderStatus(id, statusRequest)
        
        result.onSuccess { updatedOrder ->
            _purchaseOrders.value = _purchaseOrders.value.map {
                if (it.id == id) updatedOrder else it
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Approve purchase order
     */
    suspend fun approvePurchaseOrder(id: Long, approvalNotes: String? = null): NetworkResult<PurchaseOrderDTO> {
        _isLoading.value = true
        _error.value = null
        
        val approvalRequest = ApprovalRequestDTO(approvalNotes = approvalNotes)
        val result = purchaseOrderApiService.approvePurchaseOrder(id, approvalRequest)
        
        result.onSuccess { updatedOrder ->
            _purchaseOrders.value = _purchaseOrders.value.map {
                if (it.id == id) updatedOrder else it
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Receive purchase order items
     */
    suspend fun receivePurchaseOrderItems(
        id: Long, 
        receivedItems: List<ReceivedItemDTO>,
        actualDeliveryDate: String? = null,
        receivingNotes: String? = null
    ): NetworkResult<PurchaseOrderDTO> {
        _isLoading.value = true
        _error.value = null
        
        val receiveRequest = ReceiveItemsRequestDTO(
            receivedItems = receivedItems,
            actualDeliveryDate = actualDeliveryDate,
            receivingNotes = receivingNotes
        )
        
        val result = purchaseOrderApiService.receivePurchaseOrderItems(id, receiveRequest)
        
        result.onSuccess { updatedOrder ->
            _purchaseOrders.value = _purchaseOrders.value.map {
                if (it.id == id) updatedOrder else it
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    /**
     * Get purchase orders by supplier
     */
    suspend fun getPurchaseOrdersBySupplier(
        supplierId: Long,
        page: Int = 0,
        size: Int = 10,
        sortBy: String = "orderDate",
        sortDir: String = "desc"
    ): NetworkResult<PageResponse<PurchaseOrderDTO>> {
        return purchaseOrderApiService.getPurchaseOrdersBySupplier(
            supplierId = supplierId,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir
        )
    }
    
    /**
     * Load purchase order analytics
     */
    suspend fun loadPurchaseOrderAnalytics(
        fromDate: String? = null,
        toDate: String? = null,
        supplierId: Long? = null
    ) {
        _isLoading.value = true
        _error.value = null
        
        val result = purchaseOrderApiService.getPurchaseOrderAnalytics(
            fromDate = fromDate,
            toDate = toDate,
            supplierId = supplierId
        )
        
        result.onSuccess { analyticsData ->
            _analytics.value = analyticsData
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
    }
    
    /**
     * Generate purchase order PDF
     */
    suspend fun generatePurchaseOrderPdf(id: Long): NetworkResult<ByteArray> {
        return purchaseOrderApiService.generatePurchaseOrderPdf(id)
    }
    
    /**
     * Send purchase order to supplier
     */
    suspend fun sendPurchaseOrderToSupplier(
        id: Long, 
        sendRequest: SendOrderRequestDTO
    ): NetworkResult<SendOrderResponseDTO> {
        return purchaseOrderApiService.sendPurchaseOrderToSupplier(id, sendRequest)
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Refresh purchase orders data
     */
    suspend fun refreshPurchaseOrders() {
        loadPurchaseOrders(page = 0)
    }
}
