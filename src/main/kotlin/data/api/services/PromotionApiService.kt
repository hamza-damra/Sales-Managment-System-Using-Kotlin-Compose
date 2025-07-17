package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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

    suspend fun getAvailablePromotions(): NetworkResult<List<PromotionDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS_AVAILABLE}")
            response.body<List<PromotionDTO>>()
        }
    }

    suspend fun getExpiredPromotions(): NetworkResult<List<PromotionDTO>> {
        return safeApiCall {
            val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS}"
            println("🔍 PromotionApiService - Calling getExpiredPromotions URL: $url with status=expired")

            val response = httpClient.get(url) {
                parameter("status", "expired")
                parameter("page", 0)
                parameter("size", 1000)
                parameter("sortBy", "id")
                parameter("sortDir", "desc")
            }

            println("🔍 PromotionApiService - getExpiredPromotions response status: ${response.status}")
            if (response.status.value >= 400) {
                val errorBody = response.bodyAsText()
                println("🔍 PromotionApiService - getExpiredPromotions error response: $errorBody")
            }

            // Handle both direct list response and paginated response
            val responseText = response.bodyAsText()
            if (responseText.contains("\"content\"")) {
                // Paginated response
                val pageResponse = response.body<PageResponse<PromotionDTO>>()
                pageResponse.content
            } else {
                // Direct list response
                response.body<List<PromotionDTO>>()
            }
        }
    }

    suspend fun getScheduledPromotions(): NetworkResult<List<PromotionDTO>> {
        return safeApiCall {
            val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS}"
            println("🔍 PromotionApiService - Calling getScheduledPromotions URL: $url with status=scheduled")

            val response = httpClient.get(url) {
                parameter("status", "scheduled")
                parameter("page", 0)
                parameter("size", 1000)
                parameter("sortBy", "id")
                parameter("sortDir", "desc")
            }

            println("🔍 PromotionApiService - getScheduledPromotions response status: ${response.status}")
            if (response.status.value >= 400) {
                val errorBody = response.bodyAsText()
                println("🔍 PromotionApiService - getScheduledPromotions error response: $errorBody")
            }

            // Handle both direct list response and paginated response
            val responseText = response.bodyAsText()
            if (responseText.contains("\"content\"")) {
                // Paginated response
                val pageResponse = response.body<PageResponse<PromotionDTO>>()
                pageResponse.content
            } else {
                // Direct list response
                response.body<List<PromotionDTO>>()
            }
        }
    }

    suspend fun searchPromotions(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = "desc"
    ): NetworkResult<PageResponse<PromotionDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS_SEARCH}") {
                parameter("query", query)
                parameter("page", page)
                parameter("size", size)
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
            }
            response.body<PageResponse<PromotionDTO>>()
        }
    }

    suspend fun getPromotionsByType(type: String): NetworkResult<List<PromotionDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.promotionsByType(type)}")
            response.body<List<PromotionDTO>>()
        }
    }

    suspend fun getPromotionsByEligibility(eligibility: String): NetworkResult<List<PromotionDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.promotionsByEligibility(eligibility)}")
            response.body<List<PromotionDTO>>()
        }
    }

    suspend fun validateCouponCode(couponCode: String): NetworkResult<PromotionDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.validateCoupon(couponCode)}")
            response.body<PromotionDTO>()
        }
    }

    suspend fun checkCouponCodeUniqueness(couponCode: String): NetworkResult<Boolean> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS}/check-coupon/$couponCode")
            response.body<Boolean>()
        }
    }
}
