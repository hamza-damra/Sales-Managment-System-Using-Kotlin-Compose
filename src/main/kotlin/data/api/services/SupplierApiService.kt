package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for supplier management operations
 */
class SupplierApiService(private val httpClient: HttpClient) {
    
    suspend fun getAllSuppliers(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR,
        status: String? = null
    ): NetworkResult<PageResponse<SupplierDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SUPPLIERS}") {
                parameter("page", page)
                parameter("size", size)
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
                status?.let { parameter("status", it) }
            }
            response.body<PageResponse<SupplierDTO>>()
        }
    }
    
    suspend fun getSupplierById(id: Long): NetworkResult<SupplierDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierById(id)}")
            response.body<SupplierDTO>()
        }
    }
    
    suspend fun createSupplier(supplier: SupplierDTO): NetworkResult<SupplierDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SUPPLIERS}") {
                contentType(ContentType.Application.Json)
                setBody(supplier)
            }
            response.body<SupplierDTO>()
        }
    }
    
    suspend fun updateSupplier(id: Long, supplier: SupplierDTO): NetworkResult<SupplierDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierById(id)}") {
                contentType(ContentType.Application.Json)
                setBody(supplier)
            }
            response.body<SupplierDTO>()
        }
    }
    
    suspend fun deleteSupplier(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierById(id)}")
        }
    }
    
    suspend fun searchSuppliers(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = "name",
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR
    ): NetworkResult<PageResponse<SupplierDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SUPPLIERS_SEARCH}") {
                parameter("query", query)
                parameter("page", page)
                parameter("size", size)
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
            }
            response.body<PageResponse<SupplierDTO>>()
        }
    }

    suspend fun getSupplierAnalytics(id: Long): NetworkResult<SupplierAnalyticsDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierAnalytics(id)}")
            response.body<SupplierAnalyticsDTO>()
        }
    }
}
