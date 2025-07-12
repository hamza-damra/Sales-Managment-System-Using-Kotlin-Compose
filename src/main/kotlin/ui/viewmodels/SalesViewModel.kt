package ui.viewmodels

import data.api.*
import data.repository.SalesRepository
import data.repository.CustomerRepository
import data.repository.ProductRepository
import data.repository.PromotionRepository
import data.preferences.TaxPreferencesManager
import data.preferences.TaxSettings
import utils.TaxCalculationUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round

/**
 * ViewModel for sales management with comprehensive backend integration
 */

// Constants for calculations
private const val CURRENCY_DECIMAL_PLACES = 2

/**
 * Utility function to round currency values to 2 decimal places
 */
private fun roundToCurrency(amount: Double): Double {
    return TaxCalculationUtils.roundToCurrency(amount)
}

/**
 * Utility function to calculate item subtotal
 */
private fun calculateItemSubtotal(unitPrice: Double, quantity: Int): Double {
    return roundToCurrency(unitPrice * quantity)
}

/**
 * Utility function to calculate item tax amount
 */
private fun calculateItemTax(subtotal: Double, taxRate: Double): Double {
    return roundToCurrency(subtotal * taxRate)
}

/**
 * Utility function to calculate item total (subtotal + tax)
 */
private fun calculateItemTotal(subtotal: Double, taxRate: Double): Double {
    val tax = calculateItemTax(subtotal, taxRate)
    return roundToCurrency(subtotal + tax)
}
class SalesViewModel(
    private val salesRepository: SalesRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val promotionRepository: PromotionRepository,
    private val taxPreferencesManager: TaxPreferencesManager = TaxPreferencesManager()
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

    // Tax settings state
    private val _taxSettings = MutableStateFlow(taxPreferencesManager.loadTaxSettings())
    val taxSettings: StateFlow<TaxSettings> = _taxSettings.asStateFlow()

    // Promotion-related state
    private val _appliedPromotion = MutableStateFlow<PromotionDTO?>(null)
    val appliedPromotion: StateFlow<PromotionDTO?> = _appliedPromotion.asStateFlow()

    private val _promotionCode = MutableStateFlow("")
    val promotionCode: StateFlow<String> = _promotionCode.asStateFlow()

    private val _promotionDiscount = MutableStateFlow(0.0)
    val promotionDiscount: StateFlow<Double> = _promotionDiscount.asStateFlow()

    private val _isValidatingPromotion = MutableStateFlow(false)
    val isValidatingPromotion: StateFlow<Boolean> = _isValidatingPromotion.asStateFlow()

    private val _promotionError = MutableStateFlow<String?>(null)
    val promotionError: StateFlow<String?> = _promotionError.asStateFlow()

    // Computed properties with proper calculation logic
    val cartSubtotal: StateFlow<Double> = _selectedProducts.map { items ->
        val subtotal = items.sumOf { item ->
            // Use consistent calculation: unitPrice * quantity
            roundToCurrency(item.unitPrice * item.quantity)
        }
        roundToCurrency(subtotal)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val cartTax: StateFlow<Double> = combine(
        cartSubtotal,
        _promotionDiscount,
        _taxSettings
    ) { subtotal, discount, taxSettings ->
        // Calculate tax based on settings
        val baseAmount = if (taxSettings.calculateTaxOnDiscountedAmount) {
            maxOf(0.0, subtotal - discount)
        } else {
            subtotal
        }
        roundToCurrency(baseAmount * taxSettings.taxRate)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val cartTotal: StateFlow<Double> = combine(
        cartSubtotal,
        _promotionDiscount,
        cartTax
    ) { subtotal, discount, tax ->
        // Proper order: subtotal - discount + tax_on_discounted_amount
        val discountedSubtotal = maxOf(0.0, subtotal - discount)
        roundToCurrency(discountedSubtotal + tax)
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
        // Validate inputs
        if (quantity <= 0) {
            println("⚠️ Invalid quantity: $quantity. Must be greater than 0.")
            return
        }
        if (product.price < 0) {
            println("⚠️ Invalid price: ${product.price}. Must be non-negative.")
            return
        }

        val currentItems = _selectedProducts.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.productId == product.id }

        val currentTaxSettings = _taxSettings.value

        if (existingItemIndex >= 0) {
            // Update existing item with proper calculations
            val existingItem = currentItems[existingItemIndex]
            val newQuantity = existingItem.quantity + quantity
            val subtotal = calculateItemSubtotal(product.price, newQuantity)
            val taxAmount = calculateItemTax(subtotal, currentTaxSettings.taxRate)
            val totalPrice = calculateItemTotal(subtotal, currentTaxSettings.taxRate)

            val updatedItem = existingItem.copy(
                quantity = newQuantity,
                subtotal = subtotal,
                taxAmount = taxAmount,
                totalPrice = totalPrice
            )
            currentItems[existingItemIndex] = updatedItem
        } else {
            // Add new item with proper calculations
            val subtotal = calculateItemSubtotal(product.price, quantity)
            val taxAmount = calculateItemTax(subtotal, currentTaxSettings.taxRate)
            val totalPrice = calculateItemTotal(subtotal, currentTaxSettings.taxRate)

            val newItem = SaleItemDTO(
                productId = product.id!!,
                productName = product.name,
                quantity = quantity,
                unitPrice = product.price,
                originalUnitPrice = product.price,
                costPrice = product.costPrice,
                discountPercentage = 0.0,
                discountAmount = 0.0,
                taxPercentage = currentTaxSettings.taxRate * 100, // Store as percentage
                taxAmount = taxAmount,
                subtotal = subtotal,
                totalPrice = totalPrice,
                unitOfMeasure = "PCS"
            )
            currentItems.add(newItem)
        }

        _selectedProducts.value = currentItems
    }
    
    fun updateCartItemQuantity(productId: Long, newQuantity: Int) {
        // Validate quantity
        if (newQuantity < 0) {
            println("⚠️ Invalid quantity: $newQuantity. Must be non-negative.")
            return
        }

        val currentItems = _selectedProducts.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.productId == productId }

        if (itemIndex >= 0) {
            if (newQuantity > 0) {
                val item = currentItems[itemIndex]
                val currentTaxSettings = _taxSettings.value

                // Recalculate with proper order and rounding
                val subtotal = calculateItemSubtotal(item.unitPrice, newQuantity)
                val taxAmount = calculateItemTax(subtotal, currentTaxSettings.taxRate)
                val totalPrice = calculateItemTotal(subtotal, currentTaxSettings.taxRate)

                val updatedItem = item.copy(
                    quantity = newQuantity,
                    subtotal = subtotal,
                    taxAmount = taxAmount,
                    totalPrice = totalPrice
                )
                currentItems[itemIndex] = updatedItem
            } else {
                // Remove item if quantity is 0
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
        clearPromotion()
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
    suspend fun createSale(couponCode: String? = null): NetworkResult<SaleDTO> {
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
            discountAmount = _promotionDiscount.value,
            discountPercentage = 0.0,
            appliedPromotionId = _appliedPromotion.value?.id,
            appliedPromotionCode = _appliedPromotion.value?.couponCode,
            appliedPromotionName = _appliedPromotion.value?.name,
            promotionDiscountAmount = _promotionDiscount.value,
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
        
        val result = salesRepository.createSale(saleDTO, couponCode)

        result.onSuccess { createdSale ->
            println("🔍 SalesViewModel - Sale created successfully:")
            println("🔍 Created Sale ID: ${createdSale.id}")
            println("🔍 Created Sale Total: ${createdSale.totalAmount}")
            println("🔍 Applied Promotions: ${createdSale.appliedPromotions?.size ?: 0}")
            println("🔍 Total Savings: ${createdSale.totalSavings ?: 0.0}")
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

    // Enhanced promotion methods
    suspend fun applyPromotionToSale(saleId: Long, couponCode: String): NetworkResult<SaleDTO> {
        _isValidatingPromotion.value = true
        _promotionError.value = null

        val result = salesRepository.applyPromotionToSale(saleId, couponCode)

        result.onSuccess { updatedSale ->
            println("🔍 SalesViewModel - Promotion applied successfully:")
            println("🔍 Sale ID: ${updatedSale.id}")
            println("🔍 Applied Promotions: ${updatedSale.appliedPromotions?.size ?: 0}")
            println("🔍 Total Savings: ${updatedSale.totalSavings ?: 0.0}")
        }.onError { exception ->
            _promotionError.value = exception.message
        }

        _isValidatingPromotion.value = false
        return result
    }

    suspend fun removePromotionFromSale(saleId: Long, promotionId: Long): NetworkResult<SaleDTO> {
        val result = salesRepository.removePromotionFromSale(saleId, promotionId)

        result.onSuccess { updatedSale ->
            println("🔍 SalesViewModel - Promotion removed successfully:")
            println("🔍 Sale ID: ${updatedSale.id}")
            println("🔍 Remaining Promotions: ${updatedSale.appliedPromotions?.size ?: 0}")
        }

        return result
    }

    suspend fun getEligiblePromotionsForSale(saleId: Long): NetworkResult<List<PromotionDTO>> {
        return salesRepository.getEligiblePromotionsForSale(saleId)
    }

    // Enhanced promotion validation
    suspend fun validateAndApplyPromotion(code: String) {
        if (code.isBlank()) {
            _promotionError.value = "يرجى إدخال رمز الكوبون"
            return
        }

        _isValidatingPromotion.value = true
        _promotionError.value = null

        try {
            // For now, we'll validate by trying to find the promotion
            // In a real implementation, you might want a separate validation endpoint
            val promotions = promotionRepository.promotions.value
            val promotion = promotions.find { it.couponCode == code && it.isActive }

            if (promotion != null) {
                // Calculate discount based on promotion type with proper validation
                val currentSubtotal = cartSubtotal.value

                // Validate minimum order amount first
                if (promotion.minimumOrderAmount != null && currentSubtotal < promotion.minimumOrderAmount) {
                    _promotionError.value = "الحد الأدنى للطلب هو ${roundToCurrency(promotion.minimumOrderAmount)}"
                    _isValidatingPromotion.value = false
                    return
                }

                val discount = when (promotion.type) {
                    "PERCENTAGE" -> {
                        val discountAmount = currentSubtotal * (promotion.discountValue / 100)
                        val finalDiscount = promotion.maximumDiscountAmount?.let { maxDiscount ->
                            minOf(discountAmount, maxDiscount)
                        } ?: discountAmount
                        roundToCurrency(finalDiscount)
                    }
                    "FIXED_AMOUNT" -> {
                        // Ensure discount doesn't exceed subtotal
                        val maxDiscount = minOf(promotion.discountValue, currentSubtotal)
                        roundToCurrency(maxDiscount)
                    }
                    else -> 0.0
                }

                // Validate discount amount
                if (discount > currentSubtotal) {
                    _promotionError.value = "قيمة الخصم تتجاوز إجمالي الطلب"
                    _isValidatingPromotion.value = false
                    return
                }

                _appliedPromotion.value = promotion
                _promotionDiscount.value = discount
                _promotionCode.value = code
                println("🔍 SalesViewModel - Promotion applied: ${promotion.name}, Discount: $discount, Subtotal: $currentSubtotal")
            } else {
                _promotionError.value = "رمز الكوبون غير صحيح أو منتهي الصلاحية"
            }
        } catch (e: Exception) {
            _promotionError.value = "خطأ في التحقق من الكوبون: ${e.message}"
        }

        _isValidatingPromotion.value = false
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

    // Tax settings methods
    fun updateTaxSettings(newSettings: TaxSettings) {
        viewModelScope.launch {
            try {
                taxPreferencesManager.saveTaxSettings(newSettings)
                _taxSettings.value = newSettings

                // Recalculate all cart items with new tax rate
                recalculateCartItems()

                println("✅ Tax settings updated successfully")
            } catch (e: Exception) {
                println("❌ Failed to update tax settings: ${e.message}")
            }
        }
    }

    fun refreshTaxSettings() {
        _taxSettings.value = taxPreferencesManager.loadTaxSettings()
        recalculateCartItems()
    }

    private fun recalculateCartItems() {
        val currentItems = _selectedProducts.value.toMutableList()
        val currentTaxSettings = _taxSettings.value

        val updatedItems = currentItems.map { item ->
            val subtotal = calculateItemSubtotal(item.unitPrice, item.quantity)
            val taxAmount = calculateItemTax(subtotal, currentTaxSettings.taxRate)
            val totalPrice = calculateItemTotal(subtotal, currentTaxSettings.taxRate)

            item.copy(
                taxPercentage = currentTaxSettings.taxRate * 100,
                taxAmount = taxAmount,
                totalPrice = totalPrice
            )
        }

        _selectedProducts.value = updatedItems
    }

    // Promotion-related methods
    fun updatePromotionCode(code: String) {
        _promotionCode.value = code.uppercase()
        _promotionError.value = null
    }

    fun clearPromotion() {
        _appliedPromotion.value = null
        _promotionCode.value = ""
        _promotionDiscount.value = 0.0
        _promotionError.value = null
    }

    fun clearPromotionError() {
        _promotionError.value = null
    }

    /**
     * Test function to verify calculation logic works correctly
     * This can be called during development to validate calculations
     */
    fun testCalculations() {
        println("🧮 Testing Sales Calculation Logic:")
        val currentTaxRate = _taxSettings.value.taxRate

        // Test 1: Basic calculations
        val price1 = 100.0
        val qty1 = 2
        val subtotal1 = calculateItemSubtotal(price1, qty1)
        val tax1 = calculateItemTax(subtotal1, currentTaxRate)
        val total1 = calculateItemTotal(subtotal1, currentTaxRate)

        println("Test 1 - Basic: Price=$price1, Qty=$qty1, Tax Rate=${currentTaxRate * 100}%")
        println("  Subtotal: $subtotal1 (expected: 200.0)")
        println("  Tax: $tax1 (expected: ${200.0 * currentTaxRate})")
        println("  Total: $total1 (expected: ${200.0 + (200.0 * currentTaxRate)})")

        // Test 2: Rounding precision
        val price2 = 33.33
        val qty2 = 3
        val subtotal2 = calculateItemSubtotal(price2, qty2)
        val tax2 = calculateItemTax(subtotal2, currentTaxRate)
        val total2 = calculateItemTotal(subtotal2, currentTaxRate)

        println("Test 2 - Rounding: Price=$price2, Qty=$qty2")
        println("  Subtotal: $subtotal2 (expected: 99.99)")
        println("  Tax: $tax2 (expected: ${99.99 * currentTaxRate})")
        println("  Total: $total2 (expected: ${99.99 + (99.99 * currentTaxRate)})")

        // Test 3: Discount calculation
        val subtotal3 = 200.0
        val discountPercent = 10.0
        val discountAmount = roundToCurrency(subtotal3 * (discountPercent / 100))
        val discountedSubtotal = subtotal3 - discountAmount
        val taxOnDiscounted = calculateItemTax(discountedSubtotal, currentTaxRate)
        val finalTotal = roundToCurrency(discountedSubtotal + taxOnDiscounted)

        println("Test 3 - Discount: Subtotal=$subtotal3, Discount=$discountPercent%")
        println("  Discount Amount: $discountAmount (expected: 20.0)")
        println("  Discounted Subtotal: $discountedSubtotal (expected: 180.0)")
        println("  Tax on Discounted: $taxOnDiscounted (expected: ${180.0 * currentTaxRate})")
        println("  Final Total: $finalTotal (expected: ${180.0 + (180.0 * currentTaxRate)})")

        // Test 4: Edge cases
        val zeroPrice = calculateItemSubtotal(0.0, 5)
        val zeroQty = calculateItemSubtotal(100.0, 0)
        val smallPrice = calculateItemSubtotal(0.01, 1)

        println("Test 4 - Edge Cases:")
        println("  Zero price: $zeroPrice (expected: 0.0)")
        println("  Zero quantity: $zeroQty (expected: 0.0)")
        println("  Small price: $smallPrice (expected: 0.01)")

        println("🧮 Calculation tests completed!")
    }
}
