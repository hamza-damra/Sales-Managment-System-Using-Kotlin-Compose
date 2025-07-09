package ui.viewmodels

import data.api.*
import data.repository.PromotionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * ViewModel for promotion management with comprehensive backend integration
 */
class PromotionViewModel(
    private val promotionRepository: PromotionRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Repository state
    val promotions = promotionRepository.promotions
    val activePromotions = promotionRepository.activePromotions
    val expiredPromotions = promotionRepository.expiredPromotions
    val scheduledPromotions = promotionRepository.scheduledPromotions
    val isLoading = promotionRepository.isLoading
    val error = promotionRepository.error
    
    // UI State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedStatus = MutableStateFlow("الكل")
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()
    
    private val _selectedType = MutableStateFlow("الكل")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()
    
    private val _sortBy = MutableStateFlow("name")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()
    
    private val _showActiveOnly = MutableStateFlow(false)
    val showActiveOnly: StateFlow<Boolean> = _showActiveOnly.asStateFlow()
    
    private val _showExpiringOnly = MutableStateFlow(false)
    val showExpiringOnly: StateFlow<Boolean> = _showExpiringOnly.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _lastOperationResult = MutableStateFlow<NetworkResult<PromotionDTO>?>(null)
    val lastOperationResult: StateFlow<NetworkResult<PromotionDTO>?> = _lastOperationResult.asStateFlow()
    
    // Base filtered promotions (search, status, type)
    private val baseFilteredPromotions: StateFlow<List<PromotionDTO>> = combine(
        promotions,
        searchQuery,
        selectedStatus,
        selectedType
    ) { promotionsList, query, status, type ->
        var filtered = promotionsList

        // Search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter { promotion ->
                promotion.name.contains(query, ignoreCase = true) ||
                promotion.description?.contains(query, ignoreCase = true) == true ||
                promotion.couponCode?.contains(query, ignoreCase = true) == true
            }
        }

        // Status filter
        if (status != "الكل") {
            filtered = when (status) {
                "نشط" -> filtered.filter { it.isActive }
                "غير نشط" -> filtered.filter { !it.isActive }
                "منتهي الصلاحية" -> filtered.filter { isPromotionExpired(it) }
                "مجدول" -> filtered.filter { isPromotionNotYetStarted(it) }
                else -> filtered
            }
        }

        // Type filter
        if (type != "الكل") {
            filtered = when (type) {
                "نسبة مئوية" -> filtered.filter { it.type == "PERCENTAGE" }
                "مبلغ ثابت" -> filtered.filter { it.type == "FIXED_AMOUNT" }
                "اشتري X احصل على Y" -> filtered.filter { it.type == "BUY_X_GET_Y" }
                "شحن مجاني" -> filtered.filter { it.type == "FREE_SHIPPING" }
                else -> filtered
            }
        }

        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Final filtered promotions with additional filters
    val filteredPromotions: StateFlow<List<PromotionDTO>> = combine(
        baseFilteredPromotions,
        showActiveOnly,
        showExpiringOnly
    ) { baseFiltered, activeOnly, expiringOnly ->
        var filtered = baseFiltered

        // Active only filter
        if (activeOnly) {
            filtered = filtered.filter { isPromotionCurrentlyActive(it) }
        }

        // Expiring only filter
        if (expiringOnly) {
            filtered = filtered.filter {
                val daysUntilExpiry = calculateDaysUntilExpiry(it)
                daysUntilExpiry in 1..7 // Expiring within 7 days
            }
        }

        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Filtered active promotions
    val filteredActivePromotions: StateFlow<List<PromotionDTO>> = combine(
        activePromotions,
        searchQuery,
        selectedType
    ) { promotionsList, query, type ->
        var filtered = promotionsList
        
        if (query.isNotBlank()) {
            filtered = filtered.filter { promotion ->
                promotion.name.contains(query, ignoreCase = true) ||
                promotion.description?.contains(query, ignoreCase = true) == true
            }
        }
        
        if (type != "الكل") {
            filtered = when (type) {
                "نسبة مئوية" -> filtered.filter { it.type == "PERCENTAGE" }
                "مبلغ ثابت" -> filtered.filter { it.type == "FIXED_AMOUNT" }
                "اشتري X احصل على Y" -> filtered.filter { it.type == "BUY_X_GET_Y" }
                "شحن مجاني" -> filtered.filter { it.type == "FREE_SHIPPING" }
                else -> filtered
            }
        }
        
        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Filtered expired promotions
    val filteredExpiredPromotions: StateFlow<List<PromotionDTO>> = combine(
        expiredPromotions,
        searchQuery,
        selectedType
    ) { promotionsList, query, type ->
        var filtered = promotionsList
        
        if (query.isNotBlank()) {
            filtered = filtered.filter { promotion ->
                promotion.name.contains(query, ignoreCase = true) ||
                promotion.description?.contains(query, ignoreCase = true) == true
            }
        }
        
        if (type != "الكل") {
            filtered = when (type) {
                "نسبة مئوية" -> filtered.filter { it.type == "PERCENTAGE" }
                "مبلغ ثابت" -> filtered.filter { it.type == "FIXED_AMOUNT" }
                "اشتري X احصل على Y" -> filtered.filter { it.type == "BUY_X_GET_Y" }
                "شحن مجاني" -> filtered.filter { it.type == "FREE_SHIPPING" }
                else -> filtered
            }
        }
        
        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        // Load initial data
        loadInitialData()
    }
    
    /**
     * Load initial promotion data
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                promotionRepository.refreshAllData()
            } catch (e: Exception) {
                println("❌ PromotionViewModel - Error loading initial data: ${e.message}")
            }
        }
    }
    
    /**
     * Load all promotions
     */
    fun loadPromotions(
        page: Int = 0,
        size: Int = 50,
        sortBy: String = "name",
        sortDir: String = "desc",
        isActive: Boolean? = null
    ) {
        viewModelScope.launch {
            promotionRepository.loadPromotions(page, size, sortBy, sortDir, isActive)
        }
    }
    
    /**
     * Load active promotions
     */
    fun loadActivePromotions() {
        viewModelScope.launch {
            promotionRepository.loadActivePromotions()
        }
    }
    
    /**
     * Load expired promotions
     */
    fun loadExpiredPromotions() {
        viewModelScope.launch {
            promotionRepository.loadExpiredPromotions()
        }
    }
    
    /**
     * Load scheduled promotions
     */
    fun loadScheduledPromotions() {
        viewModelScope.launch {
            promotionRepository.loadScheduledPromotions()
        }
    }
    
    /**
     * Search promotions
     */
    fun searchPromotions(query: String) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                promotionRepository.searchPromotions(query)
            } else {
                promotionRepository.loadPromotions()
            }
        }
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            searchPromotions(query)
        }
    }
    
    /**
     * Update filters
     */
    fun updateSelectedStatus(status: String) {
        _selectedStatus.value = status
    }
    
    fun updateSelectedType(type: String) {
        _selectedType.value = type
    }
    
    fun updateSortBy(sortBy: String) {
        _sortBy.value = sortBy
        loadPromotions(sortBy = sortBy)
    }
    
    fun updateShowActiveOnly(showActiveOnly: Boolean) {
        _showActiveOnly.value = showActiveOnly
    }
    
    fun updateShowExpiringOnly(showExpiringOnly: Boolean) {
        _showExpiringOnly.value = showExpiringOnly
    }
    
    /**
     * Refresh all data
     */
    fun refreshData() {
        viewModelScope.launch {
            promotionRepository.refreshAllData()
        }
    }
    
    /**
     * Create new promotion
     */
    suspend fun createPromotion(promotion: PromotionDTO): NetworkResult<PromotionDTO> {
        _isProcessing.value = true

        // First check if coupon code is unique
        val uniquenessCheck = promotionRepository.checkCouponCodeUniqueness(promotion.couponCode)

        when (uniquenessCheck) {
            is NetworkResult.Success -> {
                if (!uniquenessCheck.data) {
                    // Coupon code already exists
                    val error = ApiException.ValidationError(
                        mapOf("couponCode" to listOf("كود الكوبون موجود بالفعل"))
                    )
                    val result = NetworkResult.Error(error)
                    _lastOperationResult.value = result
                    _isProcessing.value = false
                    return result
                }
            }
            is NetworkResult.Error -> {
                // If uniqueness check fails, proceed anyway (backend will handle it)
                println("⚠️ Could not check coupon code uniqueness: ${uniquenessCheck.exception.message}")
            }
            else -> {
                // Loading state - should not happen in this context
            }
        }

        val result = promotionRepository.createPromotion(promotion).first()

        result.onSuccess {
            // Refresh data after successful creation
            refreshData()
        }

        _lastOperationResult.value = result
        _isProcessing.value = false
        return result
    }
    
    /**
     * Update promotion
     */
    suspend fun updatePromotion(id: Long, promotion: PromotionDTO): NetworkResult<PromotionDTO> {
        _isProcessing.value = true
        
        val result = promotionRepository.updatePromotion(id, promotion).first()
        
        result.onSuccess {
            // Refresh data after successful update
            refreshData()
        }
        
        _lastOperationResult.value = result
        _isProcessing.value = false
        return result
    }
    
    /**
     * Delete promotion
     */
    suspend fun deletePromotion(id: Long): NetworkResult<Unit> {
        _isProcessing.value = true
        
        val result = promotionRepository.deletePromotion(id).first()
        
        result.onSuccess {
            // Refresh data after successful deletion
            refreshData()
        }
        
        _isProcessing.value = false
        return result
    }
    
    /**
     * Activate promotion
     */
    suspend fun activatePromotion(id: Long): NetworkResult<PromotionDTO> {
        _isProcessing.value = true
        
        val result = promotionRepository.activatePromotion(id).first()
        
        result.onSuccess {
            // Refresh data after successful activation
            refreshData()
        }
        
        _lastOperationResult.value = result
        _isProcessing.value = false
        return result
    }
    
    /**
     * Deactivate promotion
     */
    suspend fun deactivatePromotion(id: Long): NetworkResult<PromotionDTO> {
        _isProcessing.value = true
        
        val result = promotionRepository.deactivatePromotion(id).first()
        
        result.onSuccess {
            // Refresh data after successful deactivation
            refreshData()
        }
        
        _lastOperationResult.value = result
        _isProcessing.value = false
        return result
    }
    
    /**
     * Clear last operation result
     */
    fun clearLastOperationResult() {
        _lastOperationResult.value = null
    }
    
    /**
     * Validate coupon code uniqueness
     */
    suspend fun validateCouponCodeUniqueness(couponCode: String): Boolean {
        if (couponCode.isBlank()) return false

        return try {
            val result = promotionRepository.checkCouponCodeUniqueness(couponCode)
            when (result) {
                is NetworkResult.Success -> result.data
                else -> true // If check fails, assume it's unique (backend will validate)
            }
        } catch (e: Exception) {
            true // If check fails, assume it's unique
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        viewModelScope.cancel()
    }

    // Helper functions for promotion status calculations
    private fun isPromotionExpired(promotion: PromotionDTO): Boolean {
        return try {
            val endDate = LocalDateTime.parse(promotion.endDate.replace("Z", ""))
            endDate.isBefore(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    }

    private fun isPromotionNotYetStarted(promotion: PromotionDTO): Boolean {
        return try {
            val startDate = LocalDateTime.parse(promotion.startDate.replace("Z", ""))
            startDate.isAfter(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    }

    private fun isPromotionCurrentlyActive(promotion: PromotionDTO): Boolean {
        if (!promotion.isActive) return false

        return try {
            val now = LocalDateTime.now()
            val startDate = LocalDateTime.parse(promotion.startDate.replace("Z", ""))
            val endDate = LocalDateTime.parse(promotion.endDate.replace("Z", ""))

            now.isAfter(startDate) && now.isBefore(endDate)
        } catch (e: Exception) {
            false
        }
    }

    private fun calculateDaysUntilExpiry(promotion: PromotionDTO): Long {
        return try {
            val endDate = LocalDateTime.parse(promotion.endDate.replace("Z", ""))
            val now = LocalDateTime.now()

            if (endDate.isBefore(now)) {
                0L // Already expired
            } else {
                ChronoUnit.DAYS.between(now, endDate)
            }
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
    }
}
