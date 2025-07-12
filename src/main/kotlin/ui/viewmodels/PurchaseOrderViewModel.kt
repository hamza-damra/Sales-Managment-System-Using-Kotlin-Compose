package ui.viewmodels

import data.api.*
import data.repository.PurchaseOrderRepository
import data.repository.SupplierRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * ViewModel for purchase order management
 * Handles state management, business logic, and UI interactions for purchase orders
 */
class PurchaseOrderViewModel(
    private val purchaseOrderRepository: PurchaseOrderRepository,
    private val supplierRepository: SupplierRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Purchase orders state
    val purchaseOrders = purchaseOrderRepository.purchaseOrders
    val isLoading = purchaseOrderRepository.isLoading
    val error = purchaseOrderRepository.error
    val currentPage = purchaseOrderRepository.currentPage
    val totalPages = purchaseOrderRepository.totalPages
    val totalElements = purchaseOrderRepository.totalElements
    val hasNextPage = purchaseOrderRepository.hasNextPage
    val analytics = purchaseOrderRepository.analytics

    // Suppliers state for dropdowns
    val suppliers = supplierRepository.suppliers

    // UI state
    private val _selectedOrder = MutableStateFlow<PurchaseOrderDTO?>(null)
    val selectedOrder: StateFlow<PurchaseOrderDTO?> = _selectedOrder.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showDetailsPanel = MutableStateFlow(false)
    val showDetailsPanel: StateFlow<Boolean> = _showDetailsPanel.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    // Filter state
    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    private val _supplierFilter = MutableStateFlow<Long?>(null)
    val supplierFilter: StateFlow<Long?> = _supplierFilter.asStateFlow()

    private val _priorityFilter = MutableStateFlow<String?>(null)
    val priorityFilter: StateFlow<String?> = _priorityFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow("orderDate")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortDirection = MutableStateFlow("desc")
    val sortDirection: StateFlow<String> = _sortDirection.asStateFlow()

    // Last operation results
    private val _lastCreatedOrder = MutableStateFlow<PurchaseOrderDTO?>(null)
    val lastCreatedOrder: StateFlow<PurchaseOrderDTO?> = _lastCreatedOrder.asStateFlow()

    private val _lastUpdatedOrder = MutableStateFlow<PurchaseOrderDTO?>(null)
    val lastUpdatedOrder: StateFlow<PurchaseOrderDTO?> = _lastUpdatedOrder.asStateFlow()

    init {
        // Load initial data
        viewModelScope.launch {
            loadPurchaseOrders()
            supplierRepository.loadSuppliers()
        }
    }

    // Data loading functions
    fun loadPurchaseOrders(page: Int = 0, append: Boolean = false) {
        viewModelScope.launch {
            purchaseOrderRepository.loadPurchaseOrders(
                page = page,
                size = 10,
                sortBy = _sortBy.value,
                sortDir = _sortDirection.value,
                status = _statusFilter.value,
                supplierId = _supplierFilter.value,
                priority = _priorityFilter.value,
                append = append
            )
        }
    }

    fun loadNextPage() {
        if (hasNextPage.value && !isLoading.value) {
            loadPurchaseOrders(page = currentPage.value + 1, append = true)
        }
    }

    fun refreshPurchaseOrders() {
        viewModelScope.launch {
            purchaseOrderRepository.refreshPurchaseOrders()
        }
    }

    fun searchPurchaseOrders(query: String, page: Int = 0, append: Boolean = false) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadPurchaseOrders(page, append)
            } else {
                purchaseOrderRepository.searchPurchaseOrders(
                    query = query,
                    page = page,
                    sortBy = _sortBy.value,
                    sortDir = _sortDirection.value,
                    append = append
                )
            }
        }
    }

    // CRUD operations
    suspend fun createPurchaseOrder(orderData: PurchaseOrderData): NetworkResult<PurchaseOrderDTO> {
        _isCreating.value = true

        val orderDTO = orderData.toPurchaseOrderDTO()
        val result = purchaseOrderRepository.createPurchaseOrder(orderDTO)

        result.onSuccess { createdOrder ->
            _lastCreatedOrder.value = createdOrder
            _showAddDialog.value = false
        }

        _isCreating.value = false
        return result
    }

    suspend fun updatePurchaseOrder(id: Long, orderData: PurchaseOrderData): NetworkResult<PurchaseOrderDTO> {
        _isUpdating.value = true

        val orderDTO = orderData.toPurchaseOrderDTO().copy(id = id)
        val result = purchaseOrderRepository.updatePurchaseOrder(id, orderDTO)

        result.onSuccess { updatedOrder ->
            _lastUpdatedOrder.value = updatedOrder
            _selectedOrder.value = updatedOrder
            _showEditDialog.value = false
        }

        _isUpdating.value = false
        return result
    }

    suspend fun deletePurchaseOrder(id: Long): NetworkResult<Unit> {
        _isDeleting.value = true

        val result = purchaseOrderRepository.deletePurchaseOrder(id)

        result.onSuccess {
            _selectedOrder.value = null
            _showDetailsPanel.value = false
        }

        _isDeleting.value = false
        return result
    }

    // Status management
    suspend fun updateOrderStatus(
        id: Long, 
        status: String, 
        notes: String? = null,
        actualDeliveryDate: String? = null
    ): NetworkResult<PurchaseOrderDTO> {
        return purchaseOrderRepository.updatePurchaseOrderStatus(id, status, notes, actualDeliveryDate)
    }

    suspend fun approveOrder(id: Long, approvalNotes: String? = null): NetworkResult<PurchaseOrderDTO> {
        return purchaseOrderRepository.approvePurchaseOrder(id, approvalNotes)
    }

    suspend fun receiveOrderItems(
        id: Long, 
        receivedItems: List<ReceivedItemDTO>,
        actualDeliveryDate: String? = null,
        receivingNotes: String? = null
    ): NetworkResult<PurchaseOrderDTO> {
        return purchaseOrderRepository.receivePurchaseOrderItems(id, receivedItems, actualDeliveryDate, receivingNotes)
    }

    // Analytics
    fun loadAnalytics(fromDate: String? = null, toDate: String? = null, supplierId: Long? = null) {
        viewModelScope.launch {
            purchaseOrderRepository.loadPurchaseOrderAnalytics(fromDate, toDate, supplierId)
        }
    }

    // PDF and communication
    suspend fun generatePdf(id: Long): NetworkResult<ByteArray> {
        return purchaseOrderRepository.generatePurchaseOrderPdf(id)
    }

    suspend fun sendOrderToSupplier(id: Long, sendRequest: SendOrderRequestDTO): NetworkResult<SendOrderResponseDTO> {
        return purchaseOrderRepository.sendPurchaseOrderToSupplier(id, sendRequest)
    }

    // UI state management
    fun selectOrder(order: PurchaseOrderDTO) {
        _selectedOrder.value = order
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    fun showEditDialog(order: PurchaseOrderDTO) {
        _selectedOrder.value = order
        _showEditDialog.value = true
    }

    fun hideEditDialog() {
        _showEditDialog.value = false
    }

    fun showDetailsPanel(order: PurchaseOrderDTO) {
        _selectedOrder.value = order
        _showDetailsPanel.value = true
    }

    fun hideDetailsPanel() {
        _showDetailsPanel.value = false
    }

    // Filter management
    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
        loadPurchaseOrders()
    }

    fun setSupplierFilter(supplierId: Long?) {
        _supplierFilter.value = supplierId
        loadPurchaseOrders()
    }

    fun setPriorityFilter(priority: String?) {
        _priorityFilter.value = priority
        loadPurchaseOrders()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        searchPurchaseOrders(query)
    }

    fun setSorting(sortBy: String, sortDirection: String) {
        _sortBy.value = sortBy
        _sortDirection.value = sortDirection
        loadPurchaseOrders()
    }

    fun clearFilters() {
        _statusFilter.value = null
        _supplierFilter.value = null
        _priorityFilter.value = null
        _searchQuery.value = ""
        loadPurchaseOrders()
    }

    // Utility functions
    fun clearError() {
        purchaseOrderRepository.clearError()
    }

    fun clearLastResults() {
        _lastCreatedOrder.value = null
        _lastUpdatedOrder.value = null
    }
}

// Data class for purchase order form data
data class PurchaseOrderData(
    val supplierId: Long,
    val expectedDeliveryDate: String? = null,
    val priority: String = "NORMAL",
    val shippingAddress: String,
    val taxRate: Double = 15.0,
    val shippingCost: Double = 0.0,
    val discountAmount: Double = 0.0,
    val paymentTerms: String? = null,
    val deliveryTerms: String? = null,
    val notes: String? = null,
    val items: List<PurchaseOrderItemData> = emptyList()
) {
    fun toPurchaseOrderDTO(): PurchaseOrderDTO {
        val itemDTOs = items.map { it.toPurchaseOrderItemDTO() }
        val subtotal = itemDTOs.sumOf { it.totalPrice }
        val taxAmount = subtotal * (taxRate / 100)
        val totalAmount = subtotal + taxAmount + shippingCost - discountAmount

        return PurchaseOrderDTO(
            supplierId = supplierId,
            expectedDeliveryDate = expectedDeliveryDate,
            priority = priority,
            shippingAddress = shippingAddress,
            taxRate = taxRate,
            shippingCost = shippingCost,
            discountAmount = discountAmount,
            paymentTerms = paymentTerms,
            deliveryTerms = deliveryTerms,
            notes = notes,
            items = itemDTOs,
            subtotal = subtotal,
            taxAmount = taxAmount,
            totalAmount = totalAmount
        )
    }
}

// Data class for purchase order item form data
data class PurchaseOrderItemData(
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val taxPercentage: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val notes: String? = null
) {
    fun toPurchaseOrderItemDTO(): PurchaseOrderItemDTO {
        val subtotal = quantity * unitPrice
        val discountAmount = subtotal * (discountPercentage / 100)
        val afterDiscount = subtotal - discountAmount
        val taxAmount = afterDiscount * (taxPercentage / 100)
        val totalPrice = afterDiscount + taxAmount

        return PurchaseOrderItemDTO(
            productId = productId,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice,
            taxPercentage = taxPercentage,
            taxAmount = taxAmount,
            discountPercentage = discountPercentage,
            discountAmount = discountAmount,
            subtotal = subtotal,
            notes = notes
        )
    }
}
