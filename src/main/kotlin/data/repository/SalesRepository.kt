package data.repository

import data.api.*
import data.api.services.SalesApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for sales data management
 */
class SalesRepository(private val salesApiService: SalesApiService) {
    
    private val _sales = MutableStateFlow<List<SaleDTO>>(emptyList())
    val sales: StateFlow<List<SaleDTO>> = _sales.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    suspend fun loadSales(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "saleDate",
        sortDir: String = "desc",
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): NetworkResult<PageResponse<SaleDTO>> {
        _isLoading.value = true
        _error.value = null
        
        val result = salesApiService.getAllSales(page, size, sortBy, sortDir, status, startDate, endDate)
        
        result.onSuccess { pageResponse ->
            if (page == 0) {
                _sales.value = pageResponse.content
            } else {
                _sales.value = _sales.value + pageResponse.content
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun getSaleById(id: Long): NetworkResult<SaleDTO> {
        return salesApiService.getSaleById(id)
    }
    
    suspend fun getSalesByCustomer(
        customerId: Long,
        page: Int = 0,
        size: Int = 20
    ): NetworkResult<PageResponse<SaleDTO>> {
        return salesApiService.getSalesByCustomer(customerId, page, size)
    }
    
    suspend fun createSale(sale: SaleDTO, couponCode: String? = null): NetworkResult<SaleDTO> {
        _isLoading.value = true
        _error.value = null

        val result = salesApiService.createSale(sale, couponCode)

        result.onSuccess { newSale ->
            _sales.value = listOf(newSale) + _sales.value
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }
    
    suspend fun updateSale(id: Long, sale: SaleDTO): NetworkResult<SaleDTO> {
        _isLoading.value = true
        _error.value = null
        
        val result = salesApiService.updateSale(id, sale)
        
        result.onSuccess { updatedSale ->
            _sales.value = _sales.value.map { 
                if (it.id == id) updatedSale else it 
            }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun deleteSale(id: Long): NetworkResult<Unit> {
        _isLoading.value = true
        _error.value = null
        
        val result = salesApiService.deleteSale(id)
        
        result.onSuccess {
            _sales.value = _sales.value.filter { it.id != id }
        }.onError { exception ->
            _error.value = exception.message
        }
        
        _isLoading.value = false
        return result
    }
    
    suspend fun completeSale(id: Long): NetworkResult<SaleDTO> {
        val result = salesApiService.completeSale(id)
        
        result.onSuccess { updatedSale ->
            _sales.value = _sales.value.map { 
                if (it.id == id) updatedSale else it 
            }
        }
        
        return result
    }
    
    suspend fun cancelSale(id: Long): NetworkResult<SaleDTO> {
        val result = salesApiService.cancelSale(id)
        
        result.onSuccess { updatedSale ->
            _sales.value = _sales.value.map { 
                if (it.id == id) updatedSale else it 
            }
        }
        
        return result
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun getTodaysSales(): List<SaleDTO> {
        val today = kotlinx.datetime.Clock.System.now().toString().substring(0, 10)
        return _sales.value.filter { sale ->
            sale.saleDate?.startsWith(today) == true
        }
    }
    
    fun getSalesByStatus(status: String): List<SaleDTO> {
        return _sales.value.filter { it.status == status }
    }
    
    fun getTotalRevenue(): Double {
        return _sales.value.sumOf { it.totalAmount }
    }

    // Promotion-related methods
    suspend fun applyPromotionToSale(saleId: Long, couponCode: String): NetworkResult<SaleDTO> {
        _isLoading.value = true
        _error.value = null

        val result = salesApiService.applyPromotionToSale(saleId, couponCode)

        result.onSuccess { updatedSale ->
            _sales.value = _sales.value.map {
                if (it.id == saleId) updatedSale else it
            }
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    suspend fun removePromotionFromSale(saleId: Long, promotionId: Long): NetworkResult<SaleDTO> {
        _isLoading.value = true
        _error.value = null

        val result = salesApiService.removePromotionFromSale(saleId, promotionId)

        result.onSuccess { updatedSale ->
            _sales.value = _sales.value.map {
                if (it.id == saleId) updatedSale else it
            }
        }.onError { exception ->
            _error.value = exception.message
        }

        _isLoading.value = false
        return result
    }

    suspend fun getEligiblePromotionsForSale(saleId: Long): NetworkResult<List<PromotionDTO>> {
        return salesApiService.getEligiblePromotionsForSale(saleId)
    }
}
