package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for customer-related operations
 */
class CustomerApiService(private val httpClient: HttpClient) {
    
    suspend fun getAllCustomers(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR
    ): NetworkResult<PageResponse<CustomerDTO>> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CUSTOMERS}"
            println("🔍 CustomerApiService - Loading customers from: $fullUrl")
            println("🔍 CustomerApiService - Parameters: page=$page, size=$size, sortBy=$sortBy, sortDir=$sortDir")

            val response = httpClient.get(fullUrl) {
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
            }

            println("🔍 CustomerApiService - Response status: ${response.status}")
            val result = response.body<PageResponse<CustomerDTO>>()
            println("🔍 CustomerApiService - Received ${result.content.size} customers out of ${result.totalElements} total")
            result
        }
    }
    
    suspend fun getCustomerById(id: Long): NetworkResult<CustomerDTO> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.customerById(id)}"
            val response = httpClient.get(fullUrl)
            response.body<CustomerDTO>()
        }
    }

    suspend fun createCustomer(customer: CustomerDTO): NetworkResult<CustomerDTO> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CUSTOMERS}"
            val response = httpClient.post(fullUrl) {
                contentType(ContentType.Application.Json)
                setBody(customer)
            }
            response.body<CustomerDTO>()
        }
    }
    
    suspend fun updateCustomer(id: Long, customer: CustomerDTO): NetworkResult<CustomerDTO> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.customerById(id)}"
            val response = httpClient.put(fullUrl) {
                contentType(ContentType.Application.Json)
                setBody(customer)
            }
            response.body<CustomerDTO>()
        }
    }
    
    /**
     * Soft delete customer (recommended approach)
     * Marks customer as deleted without removing from database
     */
    suspend fun deleteCustomer(
        id: Long,
        deletedBy: String? = null,
        reason: String? = null
    ): NetworkResult<Unit> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.customerById(id)}"
            httpClient.delete(fullUrl) {
                parameter("deleteType", "soft")
                deletedBy?.let { parameter("deletedBy", it) }
                reason?.let { parameter("reason", it) }
            }
        }
    }

    /**
     * Hard delete customer with cascade (removes all associated data)
     * ⚠️ Warning: This operation is irreversible
     */
    suspend fun deleteCustomerWithCascade(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.customerById(id)}"
            httpClient.delete(fullUrl) {
                parameter("deleteType", "hard")
            }
        }
    }

    /**
     * Force delete customer (legacy compatibility)
     */
    suspend fun forceDeleteCustomer(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.customerById(id)}"
            httpClient.delete(fullUrl) {
                parameter("deleteType", "force")
            }
        }
    }

    /**
     * Restore a soft-deleted customer
     */
    suspend fun restoreCustomer(id: Long): NetworkResult<CustomerDTO> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.customerById(id)}/restore"
            val response = httpClient.post(fullUrl)
            response.body<CustomerDTO>()
        }
    }

    /**
     * Get paginated list of soft-deleted customers
     */
    suspend fun getDeletedCustomers(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = "deletedAt",
        sortDir: String = "desc"
    ): NetworkResult<PageResponse<CustomerDTO>> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CUSTOMERS}/deleted"
            println("🔍 CustomerApiService - Loading deleted customers from: $fullUrl")

            val response = httpClient.get(fullUrl) {
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
            }

            val result = response.body<PageResponse<CustomerDTO>>()
            println("🔍 CustomerApiService - Found ${result.content.size} deleted customers")
            result
        }
    }

    /**
     * Debug method: Get all customers including soft-deleted ones
     * This is for debugging purposes to see if customers were accidentally soft-deleted
     */
    suspend fun getAllCustomersIncludingDeleted(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR
    ): NetworkResult<PageResponse<CustomerDTO>> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CUSTOMERS}"
            println("🔍 CustomerApiService - Loading ALL customers (including deleted) from: $fullUrl")

            val response = httpClient.get(fullUrl) {
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
                parameter("includeDeleted", "true") // Try this parameter
            }

            val result = response.body<PageResponse<CustomerDTO>>()
            println("🔍 CustomerApiService - Found ${result.content.size} customers (including deleted)")
            result
        }
    }
    
    suspend fun searchCustomers(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE
    ): NetworkResult<PageResponse<CustomerDTO>> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CUSTOMERS_SEARCH}"
            val response = httpClient.get(fullUrl) {
                parameter("query", query)
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
            }
            response.body<PageResponse<CustomerDTO>>()
        }
    }
}
