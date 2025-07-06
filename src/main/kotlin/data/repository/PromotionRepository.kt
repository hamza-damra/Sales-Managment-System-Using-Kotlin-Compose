package data.repository

import data.api.*
import data.api.services.PromotionApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for promotion data management
 */
class PromotionRepository(
    private val promotionApiService: PromotionApiService
) {
    
    /**
     * Get all promotions with pagination
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
     * Get active promotions
     */
    fun getActivePromotions(): Flow<NetworkResult<List<PromotionDTO>>> = flow {
        emit(promotionApiService.getActivePromotions())
    }
}
