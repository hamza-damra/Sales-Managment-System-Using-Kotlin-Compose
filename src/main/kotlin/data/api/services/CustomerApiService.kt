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
            val response = httpClient.get(fullUrl) {
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
            }
            response.body<PageResponse<CustomerDTO>>()
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
    
    suspend fun deleteCustomer(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.customerById(id)}"
            httpClient.delete(fullUrl)
        }
    }

    suspend fun deleteCustomerWithCascade(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.customerById(id)}"
            httpClient.delete(fullUrl) {
                parameter("cascade", "true")
            }
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
