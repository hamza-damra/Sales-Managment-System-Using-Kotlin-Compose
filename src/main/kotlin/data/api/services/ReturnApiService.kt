package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for return management operations
 */
class ReturnApiService(private val httpClient: HttpClient) {
    
    suspend fun getAllReturns(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = "desc",
        status: String? = null
    ): NetworkResult<PageResponse<ReturnDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.RETURNS}") {
                parameter("page", page)
                parameter("size", size)
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
                status?.let { parameter("status", it) }
            }
            response.body<PageResponse<ReturnDTO>>()
        }
    }
    
    suspend fun getReturnById(id: Long): NetworkResult<ReturnDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.returnById(id)}")
            response.body<ReturnDTO>()
        }
    }
    
    suspend fun createReturn(returnRequest: ReturnDTO): NetworkResult<ReturnDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.RETURNS}") {
                contentType(ContentType.Application.Json)
                setBody(returnRequest)
            }
            response.body<ReturnDTO>()
        }
    }
    
    suspend fun updateReturn(id: Long, returnRequest: ReturnDTO): NetworkResult<ReturnDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.returnById(id)}") {
                contentType(ContentType.Application.Json)
                setBody(returnRequest)
            }
            response.body<ReturnDTO>()
        }
    }
    
    suspend fun deleteReturn(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.returnById(id)}")
        }
    }
    
    suspend fun approveReturn(id: Long, notes: String, processedBy: String): NetworkResult<ReturnDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.approveReturn(id)}") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("notes" to notes, "processedBy" to processedBy))
            }
            response.body<ReturnDTO>()
        }
    }
    
    suspend fun rejectReturn(id: Long, notes: String, processedBy: String): NetworkResult<ReturnDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.rejectReturn(id)}") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("notes" to notes, "processedBy" to processedBy))
            }
            response.body<ReturnDTO>()
        }
    }
    
    suspend fun processRefund(id: Long, refundMethod: String, notes: String, processedBy: String): NetworkResult<ReturnDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.processRefund(id)}") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "refundMethod" to refundMethod,
                    "notes" to notes,
                    "processedBy" to processedBy
                ))
            }
            response.body<ReturnDTO>()
        }
    }
}
