package ui.viewmodels

import data.api.*
import data.repository.SalesRepository
import data.repository.CustomerRepository
import data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for sales management with comprehensive backend integration
 */
class SalesViewModel(
    private val salesRepository: SalesRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Sales data
    val sales = salesRepository.sales
    val isLoading = salesRepository.isLoading
    val error = salesRepository.error
    
    // Customer and product data
    val customers = customerRepository.customers
    val products = productRepository.products
    
    // UI State
    private val _selectedProducts = MutableStateFlow<List<SaleItemDTO>>(emptyList())
    val selectedProducts: StateFlow<List<SaleItemDTO>> = _selectedProducts.asStateFlow()
    
    private val _selectedCustomer = MutableStateFlow<CustomerDTO?>(null)
    val selectedCustomer: StateFlow<CustomerDTO?> = _selectedCustomer.asStateFlow()
    
    private val _selectedPaymentMethod = MutableStateFlow("CASH")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()
    
    private val _isProcessingSale = MutableStateFlow(false)
    val isProcessingSale: StateFlow<Boolean> = _isProcessingSale.asStateFlow()
    
    private val _lastCompletedSale = MutableStateFlow<SaleDTO?>(null)
    val lastCompletedSale: StateFlow<SaleDTO?> = _lastCompletedSale.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()
    
    // Computed properties
    val cartTotal: StateFlow<Double> = _selectedProducts.map { items ->
        items.sumOf { it.totalPrice ?: (it.unitPrice * it.quantity) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    
    val cartSubtotal: StateFlow<Double> = _selectedProducts.map { items ->
        items.sumOf { it.subtotal ?: (it.unitPrice * it.quantity) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    
    val cartTax: StateFlow<Double> = cartSubtotal.map { subtotal ->
        subtotal * 0.15 // 15% tax rate
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    
    // Filtered sales based on search and status
    val filteredSales: StateFlow<List<SaleDTO>> = combine(
        sales,
        searchQuery,
        statusFilter
    ) { salesList, query, status ->
        var filtered = salesList
        
        if (query.isNotBlank()) {
            filtered = filtered.filter { sale ->
                sale.customerName?.contains(query, ignoreCase = true) == true ||
                sale.saleNumber?.contains(query, ignoreCase = true) == true ||
                sale.referenceNumber?.contains(query, ignoreCase = true) == true
            }
        }
        
        if (status != null) {
            filtered = filtered.filter { it.status == status }
        }
        
        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        // Load initial data
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            // Load sales
            salesRepository.loadSales()
            
            // Load customers and products if not already loaded
            if (customers.value.isEmpty()) {
                customerRepository.loadCustomers()
            }
            if (products.value.isEmpty()) {
                productRepository.loadProducts()
            }
        }
    }
    
    // Cart management
    fun addProductToCart(product: ProductDTO, quantity: Int = 1) {
        val currentItems = _selectedProducts.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.productId == product.id }
        
        if (existingItemIndex >= 0) {
            // Update existing item
            val existingItem = currentItems[existingItemIndex]
            val newQuantity = existingItem.quantity + quantity
            val updatedItem = existingItem.copy(
                quantity = newQuantity,
                subtotal = product.price * newQuantity,
                totalPrice = product.price * newQuantity * 1.15 // Including tax
            )
            currentItems[existingItemIndex] = updatedItem
        } else {
            // Add new item
            val newItem = SaleItemDTO(
                productId = product.id!!,
                productName = product.name,
                quantity = quantity,
                unitPrice = product.price,
                originalUnitPrice = product.price,
                costPrice = product.costPrice,
                discountPercentage = 0.0,
                discountAmount = 0.0,
                taxPercentage = 15.0,
                taxAmount = product.price * quantity * 0.15,
                subtotal = product.price * quantity,
                totalPrice = product.price * quantity * 1.15,
                unitOfMeasure = "PCS"
            )
            currentItems.add(newItem)
        }
        
        _selectedProducts.value = currentItems
    }
    
    fun updateCartItemQuantity(productId: Long, newQuantity: Int) {
        val currentItems = _selectedProducts.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.productId == productId }
        
        if (itemIndex >= 0) {
            if (newQuantity > 0) {
                val item = currentItems[itemIndex]
                val updatedItem = item.copy(
                    quantity = newQuantity,
                    subtotal = item.unitPrice * newQuantity,
                    totalPrice = item.unitPrice * newQuantity * 1.15,
                    taxAmount = item.unitPrice * newQuantity * 0.15
                )
                currentItems[itemIndex] = updatedItem
            } else {
                currentItems.removeAt(itemIndex)
            }
            _selectedProducts.value = currentItems
        }
    }
    
    fun removeFromCart(productId: Long) {
        val currentItems = _selectedProducts.value.toMutableList()
        currentItems.removeAll { it.productId == productId }
        _selectedProducts.value = currentItems
    }
    
    fun clearCart() {
        _selectedProducts.value = emptyList()
        _selectedCustomer.value = null
        _selectedPaymentMethod.value = "CASH"
        // Don't clear lastCompletedSale here - it's needed for the success dialog
    }

    fun clearLastCompletedSale() {
        _lastCompletedSale.value = null
    }
    
    // Customer selection
    fun selectCustomer(customer: CustomerDTO?) {
        _selectedCustomer.value = customer
    }
    
    // Payment method selection
    fun selectPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }
    
    // Search and filtering
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateStatusFilter(status: String?) {
        _statusFilter.value = status
    }
    
    // Sale operations
    suspend fun createSale(): NetworkResult<SaleDTO> {
        _isProcessingSale.value = true

        // Validation before creating sale
        if (_selectedCustomer.value == null) {
            _isProcessingSale.value = false
            return NetworkResult.Error(ApiException.ValidationError(mapOf("customer" to listOf("Customer must be selected"))))
        }

        if (_selectedProducts.value.isEmpty()) {
            _isProcessingSale.value = false
            return NetworkResult.Error(ApiException.ValidationError(mapOf("items" to listOf("At least one product must be added to cart"))))
        }

        val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val saleDTO = SaleDTO(
            customerId = _selectedCustomer.value?.id ?: 0L,
            customerName = _selectedCustomer.value?.name,
            saleDate = currentTime.toString(),
            totalAmount = cartTotal.value,
            status = "PENDING",
            items = _selectedProducts.value,
            subtotal = cartSubtotal.value,
            discountAmount = 0.0,
            discountPercentage = 0.0,
            taxAmount = cartTax.value,
            taxPercentage = 15.0,
            shippingCost = 0.0,
            paymentMethod = _selectedPaymentMethod.value,
            paymentStatus = "PENDING",
            billingAddress = _selectedCustomer.value?.address,
            shippingAddress = _selectedCustomer.value?.address,
            salesPerson = "Current User", // TODO: Get from auth service
            salesChannel = "IN_STORE",
            saleType = "RETAIL",
            currency = "USD",
            exchangeRate = 1.0,
            deliveryStatus = "NOT_SHIPPED",
            isGift = false,
            loyaltyPointsEarned = (cartTotal.value / 10).toInt(),
            loyaltyPointsUsed = 0,
            isReturn = false
        )

        println("🔍 SalesViewModel - Creating sale with data:")
        println("🔍 Customer ID: ${saleDTO.customerId}")
        println("🔍 Customer Name: ${saleDTO.customerName}")
        println("🔍 Total Amount: ${saleDTO.totalAmount}")
        println("🔍 Items count: ${saleDTO.items.size}")
        println("🔍 Payment Method: ${saleDTO.paymentMethod}")
        saleDTO.items.forEachIndexed { index, item ->
            println("🔍 Item $index: Product ID=${item.productId}, Quantity=${item.quantity}, Unit Price=${item.unitPrice}")
        }
        
        val result = salesRepository.createSale(saleDTO)
        
        result.onSuccess { createdSale ->
            println("🔍 SalesViewModel - Sale created successfully:")
            println("🔍 Created Sale ID: ${createdSale.id}")
            println("🔍 Created Sale Total: ${createdSale.totalAmount}")
            _lastCompletedSale.value = createdSale
            // Don't clear cart immediately - let the success dialog handle it
        }
        
        _isProcessingSale.value = false
        return result
    }
    
    suspend fun completeSale(saleId: Long): NetworkResult<SaleDTO> {
        return salesRepository.completeSale(saleId)
    }
    
    suspend fun cancelSale(saleId: Long): NetworkResult<SaleDTO> {
        return salesRepository.cancelSale(saleId)
    }
    
    suspend fun refreshSales() {
        salesRepository.loadSales()
    }
    
    suspend fun loadMoreSales() {
        val currentSales = sales.value
        val nextPage = (currentSales.size / 20) // Assuming page size of 20
        salesRepository.loadSales(page = nextPage)
    }
    
    fun clearError() {
        salesRepository.clearError()
    }
    
    // Analytics helpers
    fun getTodaysSales(): List<SaleDTO> {
        return salesRepository.getTodaysSales()
    }
    
    fun getSalesByStatus(status: String): List<SaleDTO> {
        return salesRepository.getSalesByStatus(status)
    }
    
    fun getTotalRevenue(): Double {
        return salesRepository.getTotalRevenue()
    }
}
