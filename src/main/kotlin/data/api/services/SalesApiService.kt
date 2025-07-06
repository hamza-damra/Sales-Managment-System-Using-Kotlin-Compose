package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * API service for sales-related operations
 */
class SalesApiService(private val httpClient: HttpClient) {
    
    suspend fun getAllSales(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR,
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): NetworkResult<PageResponse<SaleDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SALES}") {
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
                status?.let { parameter("status", it) }
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
            }
            response.body<PageResponse<SaleDTO>>()
        }
    }
    
    suspend fun getSaleById(id: Long): NetworkResult<SaleDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.saleById(id)}")
            response.body<SaleDTO>()
        }
    }

    suspend fun getSalesByCustomer(
        customerId: Long,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE
    ): NetworkResult<PageResponse<SaleDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.salesByCustomer(customerId)}") {
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
            }
            response.body<PageResponse<SaleDTO>>()
        }
    }
    
    suspend fun createSale(sale: SaleDTO): NetworkResult<SaleDTO> {
        return safeApiCall {
            val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SALES}"
            println("🔍 Creating sale at URL: $url")
            println("🔍 Sale data: $sale")
            println("🔍 Sale items count: ${sale.items.size}")
            println("🔍 Customer ID: ${sale.customerId}")
            println("🔍 Total amount: ${sale.totalAmount}")

            // Validate required fields before sending
            if (sale.customerId <= 0) {
                throw IllegalArgumentException("Customer ID must be greater than 0")
            }
            if (sale.items.isEmpty()) {
                throw IllegalArgumentException("Sale must contain at least one item")
            }
            if (sale.totalAmount <= 0) {
                throw IllegalArgumentException("Total amount must be greater than 0")
            }

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(sale)
            }

            println("🔍 Create sale response status: ${response.status}")
            if (response.status.value >= 400) {
                val errorBody = response.bodyAsText()
                println("🔍 Error response body: $errorBody")
            }
            response.body<SaleDTO>()
        }
    }

    suspend fun updateSale(id: Long, sale: SaleDTO): NetworkResult<SaleDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.saleById(id)}") {
                contentType(ContentType.Application.Json)
                setBody(sale)
            }
            response.body<SaleDTO>()
        }
    }

    suspend fun deleteSale(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.saleById(id)}")
        }
    }
    
    suspend fun completeSale(id: Long): NetworkResult<SaleDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.completeSale(id)}")
            response.body<SaleDTO>()
        }
    }

    suspend fun cancelSale(id: Long): NetworkResult<SaleDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.cancelSale(id)}")
            response.body<SaleDTO>()
        }
    }
}
