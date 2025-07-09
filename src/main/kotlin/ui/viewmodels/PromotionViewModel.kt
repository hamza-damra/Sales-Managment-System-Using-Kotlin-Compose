package ui.viewmodels

import data.api.*
import data.repository.PromotionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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
    
    // Filtered promotions based on search and filters
    val filteredPromotions: StateFlow<List<PromotionDTO>> = combine(
        promotions,
        searchQuery,
        selectedStatus,
        selectedType,
        showActiveOnly,
        showExpiringOnly
    ) { promotionsList, query, status, type, activeOnly, expiringOnly ->
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
                "منتهي الصلاحية" -> filtered.filter { it.isExpired == true }
                "مجدول" -> filtered.filter { it.isNotYetStarted == true }
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
        
        // Active only filter
        if (activeOnly) {
            filtered = filtered.filter { it.isCurrentlyActive == true }
        }
        
        // Expiring only filter
        if (expiringOnly) {
            filtered = filtered.filter { 
                val daysUntilExpiry = it.daysUntilExpiry ?: Long.MAX_VALUE
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
     * Clean up resources
     */
    fun cleanup() {
        viewModelScope.cancel()
    }
}
