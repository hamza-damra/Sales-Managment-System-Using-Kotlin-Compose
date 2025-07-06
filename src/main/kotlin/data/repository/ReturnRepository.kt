package data.repository

import data.api.*
import data.api.services.ReturnApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for return data management
 */
class ReturnRepository(
    private val returnApiService: ReturnApiService
) {
    
    /**
     * Get all returns with pagination
     */
    fun getAllReturns(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = "desc",
        status: String? = null
    ): Flow<NetworkResult<PageResponse<ReturnDTO>>> = flow {
        emit(returnApiService.getAllReturns(page, size, sortBy, sortDir, status))
    }
    
    /**
     * Get return by ID
     */
    fun getReturnById(id: Long): Flow<NetworkResult<ReturnDTO>> = flow {
        emit(returnApiService.getReturnById(id))
    }
    
    /**
     * Create new return
     */
    fun createReturn(returnRequest: ReturnDTO): Flow<NetworkResult<ReturnDTO>> = flow {
        emit(returnApiService.createReturn(returnRequest))
    }
    
    /**
     * Update existing return
     */
    fun updateReturn(id: Long, returnRequest: ReturnDTO): Flow<NetworkResult<ReturnDTO>> = flow {
        emit(returnApiService.updateReturn(id, returnRequest))
    }
    
    /**
     * Delete return
     */
    fun deleteReturn(id: Long): Flow<NetworkResult<Unit>> = flow {
        emit(returnApiService.deleteReturn(id))
    }
    
    /**
     * Approve return
     */
    fun approveReturn(id: Long, notes: String, processedBy: String): Flow<NetworkResult<ReturnDTO>> = flow {
        emit(returnApiService.approveReturn(id, notes, processedBy))
    }
    
    /**
     * Reject return
     */
    fun rejectReturn(id: Long, notes: String, processedBy: String): Flow<NetworkResult<ReturnDTO>> = flow {
        emit(returnApiService.rejectReturn(id, notes, processedBy))
    }
    
    /**
     * Process refund
     */
    fun processRefund(id: Long, refundMethod: String, notes: String, processedBy: String): Flow<NetworkResult<ReturnDTO>> = flow {
        emit(returnApiService.processRefund(id, refundMethod, notes, processedBy))
    }
}
