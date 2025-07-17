package data.repository

import data.api.*
import data.api.services.PromotionApiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Repository for promotion data management with state management
 */
class PromotionRepository(
    private val promotionApiService: PromotionApiService
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // State management
    private val _promotions = MutableStateFlow<List<PromotionDTO>>(emptyList())
    val promotions: StateFlow<List<PromotionDTO>> = _promotions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _activePromotions = MutableStateFlow<List<PromotionDTO>>(emptyList())
    val activePromotions: StateFlow<List<PromotionDTO>> = _activePromotions.asStateFlow()

    private val _expiredPromotions = MutableStateFlow<List<PromotionDTO>>(emptyList())
    val expiredPromotions: StateFlow<List<PromotionDTO>> = _expiredPromotions.asStateFlow()

    private val _scheduledPromotions = MutableStateFlow<List<PromotionDTO>>(emptyList())
    val scheduledPromotions: StateFlow<List<PromotionDTO>> = _scheduledPromotions.asStateFlow()

    /**
     * Load all promotions with state management
     */
    suspend fun loadPromotions(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = "desc",
        isActive: Boolean? = null
    ): NetworkResult<PageResponse<PromotionDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = promotionApiService.getAllPromotions(page, size, sortBy, sortDir, isActive)

        when (result) {
            is NetworkResult.Success -> {
                if (page == 0) {
                    _promotions.value = result.data.content
                } else {
                    _promotions.value = _promotions.value + result.data.content
                }
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
            }
            is NetworkResult.Loading -> {
                // Handle loading state if needed
            }
        }

        _isLoading.value = false
        return result
    }

    /**
     * Get all promotions with pagination (Flow version for compatibility)
     */
    fun getAllPromotions(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = "desc",
        isActive: Boolean? = null
    ): Flow<NetworkResult<PageResponse<PromotionDTO>>> = flow {
        emit(promotionApiService.getAllPromotions(page, size, sortBy, sortDir, isActive))
    }
    
    /**
     * Get promotion by ID
     */
    fun getPromotionById(id: Long): Flow<NetworkResult<PromotionDTO>> = flow {
        emit(promotionApiService.getPromotionById(id))
    }
    
    /**
     * Create new promotion
     */
    fun createPromotion(promotion: PromotionDTO): Flow<NetworkResult<PromotionDTO>> = flow {
        emit(promotionApiService.createPromotion(promotion))
    }
    
    /**
     * Update existing promotion
     */
    fun updatePromotion(id: Long, promotion: PromotionDTO): Flow<NetworkResult<PromotionDTO>> = flow {
        emit(promotionApiService.updatePromotion(id, promotion))
    }
    
    /**
     * Delete promotion
     */
    fun deletePromotion(id: Long): Flow<NetworkResult<Unit>> = flow {
        emit(promotionApiService.deletePromotion(id))
    }
    
    /**
     * Activate promotion
     */
    fun activatePromotion(id: Long): Flow<NetworkResult<PromotionDTO>> = flow {
        emit(promotionApiService.activatePromotion(id))
    }
    
    /**
     * Deactivate promotion
     */
    fun deactivatePromotion(id: Long): Flow<NetworkResult<PromotionDTO>> = flow {
        emit(promotionApiService.deactivatePromotion(id))
    }
    
    /**
     * Load active promotions with state management
     */
    suspend fun loadActivePromotions(): NetworkResult<List<PromotionDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = promotionApiService.getActivePromotions()

        when (result) {
            is NetworkResult.Success -> {
                _activePromotions.value = result.data
            }
            is NetworkResult.Error -> {
                _error.value = result.exception.message
            }
            is NetworkResult.Loading -> {
                // Handle loading state if needed
            }
        }

        _isLoading.value = false
        return result
    }

    /**
     * Load expired promotions with state management
     */
    suspend fun loadExpiredPromotions(): NetworkResult<List<PromotionDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = promotionApiService.getExpiredPromotions()

        when (result) {
            is NetworkResult.Success -> {
                _expiredPromotions.value = result.data
                println("✅ PromotionRepository - Loaded ${result.data.size} expired promotions")
                _isLoading.value = false
                return result
            }
            is NetworkResult.Error -> {
                println("❌ PromotionRepository - Error loading expired promotions: ${result.exception.message}")
                _error.value = result.exception.message
                _isLoading.value = false
                return result
            }
            is NetworkResult.Loading -> {
                _isLoading.value = false
                return result
            }
        }
    }

    /**
     * Load scheduled promotions with state management
     */
    suspend fun loadScheduledPromotions(): NetworkResult<List<PromotionDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = promotionApiService.getScheduledPromotions()

        when (result) {
            is NetworkResult.Success -> {
                _scheduledPromotions.value = result.data
                println("✅ PromotionRepository - Loaded ${result.data.size} scheduled promotions")
                _isLoading.value = false
                return result
            }
            is NetworkResult.Error -> {
                println("❌ PromotionRepository - Error loading scheduled promotions: ${result.exception.message}")
                _error.value = result.exception.message
                _isLoading.value = false
                return result
            }
            is NetworkResult.Loading -> {
                _isLoading.value = false
                return result
            }
        }
    }

    /**
     * Search promotions
     */
    suspend fun searchPromotions(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = "desc"
    ): NetworkResult<PageResponse<PromotionDTO>> {
        _isLoading.value = true
        _error.value = null

        val result = promotionApiService.searchPromotions(query, page, size, sortBy, sortDir)

        result.onSuccess { pageResponse ->
            if (page == 0) {
                _promotions.value = pageResponse.content
            } else {
                _promotions.value = _promotions.value + pageResponse.content
            }
        }

        result.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Get active promotions (Flow version for compatibility)
     */
    fun getActivePromotions(): Flow<NetworkResult<List<PromotionDTO>>> = flow {
        emit(promotionApiService.getActivePromotions())
    }

    /**
     * Validate coupon code and return promotion details
     */
    suspend fun validateCouponCode(couponCode: String): NetworkResult<PromotionDTO> {
        _isLoading.value = true
        _error.value = null

        val result = promotionApiService.validateCouponCode(couponCode)

        result.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    /**
     * Check if promotion is currently valid for use
     */
    fun isPromotionValid(promotion: PromotionDTO, orderAmount: Double, customerId: Long? = null): Boolean {
        // Check if promotion is active
        if (!promotion.isActive) return false

        // Check date range
        val now = System.currentTimeMillis()
        try {
            val startDate = java.time.Instant.parse(promotion.startDate).toEpochMilli()
            val endDate = java.time.Instant.parse(promotion.endDate).toEpochMilli()
            if (now < startDate || now > endDate) return false
        } catch (e: Exception) {
            return false
        }

        // Check minimum order amount
        promotion.minimumOrderAmount?.let { minAmount ->
            if (orderAmount < minAmount) return false
        }

        // Check usage limit
        promotion.usageLimit?.let { limit ->
            val currentUsage = promotion.usageCount ?: 0
            if (currentUsage >= limit) return false
        }

        // TODO: Add customer eligibility check when customer service is available
        // For now, assume all customers are eligible

        return true
    }

    /**
     * Calculate discount amount for a promotion
     */
    fun calculateDiscount(promotion: PromotionDTO, orderAmount: Double): Double {
        if (!isPromotionValid(promotion, orderAmount)) return 0.0

        val discount = when (promotion.type) {
            "PERCENTAGE" -> {
                val percentageDiscount = orderAmount * (promotion.discountValue / 100.0)
                percentageDiscount
            }
            "FIXED_AMOUNT" -> {
                // Don't exceed the order amount
                minOf(promotion.discountValue, orderAmount)
            }
            "FREE_SHIPPING" -> {
                // For free shipping, return a fixed amount (could be configurable)
                10.0 // Assuming $10 shipping cost
            }
            else -> 0.0
        }

        return maxOf(0.0, discount)
    }

    /**
     * Refresh all promotion data
     */
    suspend fun refreshAllData() {
        repositoryScope.launch {
            loadPromotions()
            loadActivePromotions()
            loadExpiredPromotions()
            loadScheduledPromotions()
        }
    }



    /**
     * Check if coupon code is unique
     */
    suspend fun checkCouponCodeUniqueness(couponCode: String): NetworkResult<Boolean> {
        return promotionApiService.checkCouponCodeUniqueness(couponCode)
    }
}
