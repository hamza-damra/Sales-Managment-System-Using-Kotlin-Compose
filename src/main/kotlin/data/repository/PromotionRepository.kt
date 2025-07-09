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
                _isLoading.value = false
                return result
            }
            is NetworkResult.Error -> {
                println("❌ PromotionRepository - Error loading expired promotions: ${result.exception.message}")

                // Check if this is the specific routing error
                if (result.exception.message?.contains("promotion endpoint routing", ignoreCase = true) == true ||
                    result.exception.message?.contains("Invalid Parameter Type", ignoreCase = true) == true) {

                    println("🔄 PromotionRepository - Attempting fallback for expired promotions")
                    val fallbackResult = loadExpiredPromotionsFallback()
                    _isLoading.value = false
                    return fallbackResult
                }

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
                _isLoading.value = false
                return result
            }
            is NetworkResult.Error -> {
                println("❌ PromotionRepository - Error loading scheduled promotions: ${result.exception.message}")

                // Check if this is the specific routing error
                if (result.exception.message?.contains("promotion endpoint routing", ignoreCase = true) == true ||
                    result.exception.message?.contains("Invalid Parameter Type", ignoreCase = true) == true) {

                    println("🔄 PromotionRepository - Attempting fallback for scheduled promotions")
                    val fallbackResult = loadScheduledPromotionsFallback()
                    _isLoading.value = false
                    return fallbackResult
                }

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
                // Apply maximum discount limit if specified
                promotion.maximumDiscountAmount?.let { maxDiscount ->
                    minOf(percentageDiscount, maxDiscount)
                } ?: percentageDiscount
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
     * Fallback method to load expired promotions by filtering all promotions
     * Used when the dedicated expired endpoint fails due to backend routing issues
     */
    private suspend fun loadExpiredPromotionsFallback(): NetworkResult<List<PromotionDTO>> {
        println("🔄 PromotionRepository - Using fallback method for expired promotions")

        return try {
            // Get all promotions and filter for expired ones
            val allPromotionsResult = promotionApiService.getAllPromotions(page = 0, size = 1000)

            if (allPromotionsResult is NetworkResult.Success) {
                val currentTime = System.currentTimeMillis()
                val expiredPromotions = allPromotionsResult.data.content.filter { promotion ->
                    try {
                        // Parse end date and check if it's in the past
                        val endDate = java.time.Instant.parse(promotion.endDate)
                        endDate.toEpochMilli() < currentTime
                    } catch (e: Exception) {
                        println("⚠️ Error parsing date for promotion ${promotion.id}: ${e.message}")
                        false
                    }
                }

                _expiredPromotions.value = expiredPromotions
                println("✅ PromotionRepository - Fallback loaded ${expiredPromotions.size} expired promotions")
                NetworkResult.Success(expiredPromotions)
            } else if (allPromotionsResult is NetworkResult.Error) {
                println("❌ PromotionRepository - Fallback also failed: ${allPromotionsResult.exception.message}")
                allPromotionsResult
            } else {
                // Loading state - return empty list as fallback
                NetworkResult.Success(emptyList())
            }
        } catch (e: Exception) {
            println("❌ PromotionRepository - Exception in fallback: ${e.message}")
            NetworkResult.Error(e.toApiException())
        }
    }

    /**
     * Fallback method to load scheduled promotions by filtering all promotions
     * Used when the dedicated scheduled endpoint fails due to backend routing issues
     */
    private suspend fun loadScheduledPromotionsFallback(): NetworkResult<List<PromotionDTO>> {
        println("🔄 PromotionRepository - Using fallback method for scheduled promotions")

        return try {
            // Get all promotions and filter for scheduled ones
            val allPromotionsResult = promotionApiService.getAllPromotions(page = 0, size = 1000)

            if (allPromotionsResult is NetworkResult.Success) {
                val currentTime = System.currentTimeMillis()
                val scheduledPromotions = allPromotionsResult.data.content.filter { promotion ->
                    try {
                        // Parse start date and check if it's in the future
                        val startDate = java.time.Instant.parse(promotion.startDate)
                        startDate.toEpochMilli() > currentTime
                    } catch (e: Exception) {
                        println("⚠️ Error parsing date for promotion ${promotion.id}: ${e.message}")
                        false
                    }
                }

                _scheduledPromotions.value = scheduledPromotions
                println("✅ PromotionRepository - Fallback loaded ${scheduledPromotions.size} scheduled promotions")
                NetworkResult.Success(scheduledPromotions)
            } else if (allPromotionsResult is NetworkResult.Error) {
                println("❌ PromotionRepository - Fallback also failed: ${allPromotionsResult.exception.message}")
                allPromotionsResult
            } else {
                // Loading state - return empty list as fallback
                NetworkResult.Success(emptyList())
            }
        } catch (e: Exception) {
            println("❌ PromotionRepository - Exception in fallback: ${e.message}")
            NetworkResult.Error(e.toApiException())
        }
    }

    /**
     * Check if coupon code is unique
     */
    suspend fun checkCouponCodeUniqueness(couponCode: String): NetworkResult<Boolean> {
        return promotionApiService.checkCouponCodeUniqueness(couponCode)
    }
}
