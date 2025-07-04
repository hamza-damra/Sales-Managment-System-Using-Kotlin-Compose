package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for product-related operations
 */
class ProductApiService(private val httpClient: HttpClient) {
    
    suspend fun getAllProducts(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR,
        category: String? = null
    ): NetworkResult<PageResponse<ProductDTO>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.PRODUCTS) {
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
                category?.let { parameter("category", it) }
            }
            response.body<PageResponse<ProductDTO>>()
        }
    }
    
    suspend fun getProductById(id: Long): NetworkResult<ProductDTO> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.productById(id))
            response.body<ProductDTO>()
        }
    }
    
    suspend fun createProduct(product: ProductDTO): NetworkResult<ProductDTO> {
        return safeApiCall {
            val response = httpClient.post(ApiConfig.Endpoints.PRODUCTS) {
                contentType(ContentType.Application.Json)
                setBody(product)
            }
            response.body<ProductDTO>()
        }
    }
    
    suspend fun updateProduct(id: Long, product: ProductDTO): NetworkResult<ProductDTO> {
        return safeApiCall {
            val response = httpClient.put(ApiConfig.Endpoints.productById(id)) {
                contentType(ContentType.Application.Json)
                setBody(product)
            }
            response.body<ProductDTO>()
        }
    }
    
    suspend fun deleteProduct(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            httpClient.delete(ApiConfig.Endpoints.productById(id))
        }
    }
    
    suspend fun searchProducts(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE
    ): NetworkResult<PageResponse<ProductDTO>> {
        return safeApiCall {
            val response = httpClient.get(ApiConfig.Endpoints.PRODUCTS_SEARCH) {
                parameter("query", query)
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
            }
            response.body<PageResponse<ProductDTO>>()
        }
    }
    
    suspend fun updateStock(id: Long, stockQuantity: Int): NetworkResult<ProductDTO> {
        return safeApiCall {
            val response = httpClient.put(ApiConfig.Endpoints.productStock(id)) {
                contentType(ContentType.Application.Json)
                setBody(StockUpdateRequest(stockQuantity))
            }
            response.body<ProductDTO>()
        }
    }
    
    suspend fun increaseStock(id: Long, quantity: Int): NetworkResult<ProductDTO> {
        return safeApiCall {
            val response = httpClient.post(ApiConfig.Endpoints.productStockIncrease(id)) {
                contentType(ContentType.Application.Json)
                setBody(StockAdjustmentRequest(quantity))
            }
            response.body<ProductDTO>()
        }
    }
    
    suspend fun decreaseStock(id: Long, quantity: Int): NetworkResult<ProductDTO> {
        return safeApiCall {
            val response = httpClient.post(ApiConfig.Endpoints.productStockDecrease(id)) {
                contentType(ContentType.Application.Json)
                setBody(StockAdjustmentRequest(quantity))
            }
            response.body<ProductDTO>()
        }
    }
}
