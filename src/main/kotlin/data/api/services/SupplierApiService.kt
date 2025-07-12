package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Enhanced API service for supplier management operations
 * Implements all endpoints from Supplier-API-Documentation.md
 */
class SupplierApiService(private val httpClient: HttpClient) {

    /**
     * Get all suppliers with pagination and filtering
     */
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

    /**
     * Get supplier by ID
     */
    suspend fun getSupplierById(id: Long): NetworkResult<SupplierDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierById(id)}")
            response.body<SupplierDTO>()
        }
    }

    /**
     * Create new supplier
     */
    suspend fun createSupplier(supplier: SupplierDTO): NetworkResult<SupplierDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SUPPLIERS}") {
                contentType(ContentType.Application.Json)
                setBody(supplier)
            }
            response.body<SupplierDTO>()
        }
    }

    /**
     * Update existing supplier
     */
    suspend fun updateSupplier(id: Long, supplier: SupplierDTO): NetworkResult<SupplierDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierById(id)}") {
                contentType(ContentType.Application.Json)
                setBody(supplier)
            }
            response.body<SupplierDTO>()
        }
    }

    /**
     * Delete supplier
     */
    suspend fun deleteSupplier(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierById(id)}")
        }
    }

    /**
     * Search suppliers across multiple fields
     */
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

    /**
     * Get supplier with associated purchase orders
     */
    suspend fun getSupplierWithOrders(id: Long): NetworkResult<SupplierDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierWithOrders(id)}")
            response.body<SupplierDTO>()
        }
    }

    /**
     * Get top rated suppliers
     */
    suspend fun getTopRatedSuppliers(minRating: Double = 4.0): NetworkResult<List<SupplierDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SUPPLIERS_TOP_RATED}") {
                parameter("minRating", minRating)
            }
            response.body<List<SupplierDTO>>()
        }
    }

    /**
     * Get high value suppliers
     */
    suspend fun getHighValueSuppliers(minAmount: Double = 10000.0): NetworkResult<List<SupplierDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SUPPLIERS_HIGH_VALUE}") {
                parameter("minAmount", minAmount)
            }
            response.body<List<SupplierDTO>>()
        }
    }

    /**
     * Update supplier rating
     */
    suspend fun updateSupplierRating(id: Long, rating: Double): NetworkResult<SupplierDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierRating(id)}") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("rating" to rating))
            }
            response.body<SupplierDTO>()
        }
    }

    /**
     * Get supplier analytics
     */
    suspend fun getSupplierAnalytics(): NetworkResult<SupplierAnalyticsOverviewDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SUPPLIERS_ANALYTICS}")
            response.body<SupplierAnalyticsOverviewDTO>()
        }
    }

    /**
     * Get individual supplier analytics
     */
    suspend fun getSupplierAnalyticsById(id: Long): NetworkResult<SupplierAnalyticsDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.supplierAnalytics(id)}")
            response.body<SupplierAnalyticsDTO>()
        }
    }
}
