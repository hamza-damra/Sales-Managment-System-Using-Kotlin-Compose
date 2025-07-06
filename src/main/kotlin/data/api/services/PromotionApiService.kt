package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for promotion management operations
 */
class PromotionApiService(private val httpClient: HttpClient) {
    
    suspend fun getAllPromotions(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = "desc",
        isActive: Boolean? = null
    ): NetworkResult<PageResponse<PromotionDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS}") {
                parameter("page", page)
                parameter("size", size)
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
                isActive?.let { parameter("isActive", it) }
            }
            response.body<PageResponse<PromotionDTO>>()
        }
    }
    
    suspend fun getPromotionById(id: Long): NetworkResult<PromotionDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.promotionById(id)}")
            response.body<PromotionDTO>()
        }
    }
    
    suspend fun createPromotion(promotion: PromotionDTO): NetworkResult<PromotionDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS}") {
                contentType(ContentType.Application.Json)
                setBody(promotion)
            }
            response.body<PromotionDTO>()
        }
    }
    
    suspend fun updatePromotion(id: Long, promotion: PromotionDTO): NetworkResult<PromotionDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.promotionById(id)}") {
                contentType(ContentType.Application.Json)
                setBody(promotion)
            }
            response.body<PromotionDTO>()
        }
    }
    
    suspend fun deletePromotion(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.promotionById(id)}")
        }
    }
    
    suspend fun activatePromotion(id: Long): NetworkResult<PromotionDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.activatePromotion(id)}")
            response.body<PromotionDTO>()
        }
    }
    
    suspend fun deactivatePromotion(id: Long): NetworkResult<PromotionDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.deactivatePromotion(id)}")
            response.body<PromotionDTO>()
        }
    }
    
    suspend fun getActivePromotions(): NetworkResult<List<PromotionDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS_ACTIVE}")
            response.body<List<PromotionDTO>>()
        }
    }
}
