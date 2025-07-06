package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * API service for category management operations
 */
class CategoryApiService(private val httpClient: HttpClient) {
    
    suspend fun getAllCategories(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = "displayOrder",
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR,
        status: String? = null
    ): NetworkResult<PageResponse<CategoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CATEGORIES}") {
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
                status?.let { parameter("status", it) }
            }
            response.body<PageResponse<CategoryDTO>>()
        }
    }
    
    suspend fun getAllActiveCategories(): NetworkResult<List<CategoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CATEGORIES_ACTIVE}")
            response.body<List<CategoryDTO>>()
        }
    }
    
    suspend fun getCategoryById(id: Long): NetworkResult<CategoryDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.categoryById(id)}")
            response.body<CategoryDTO>()
        }
    }
    
    suspend fun getCategoryByName(name: String): NetworkResult<CategoryDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.categoryByName(name)}")
            response.body<CategoryDTO>()
        }
    }
    
    suspend fun createCategory(category: CategoryDTO): NetworkResult<CategoryDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CATEGORIES}") {
                contentType(ContentType.Application.Json)
                setBody(category)
            }
            response.body<CategoryDTO>()
        }
    }
    
    suspend fun updateCategory(id: Long, category: CategoryDTO): NetworkResult<CategoryDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.categoryById(id)}") {
                contentType(ContentType.Application.Json)
                setBody(category)
            }
            response.body<CategoryDTO>()
        }
    }
    
    suspend fun deleteCategory(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.categoryById(id)}")
        }
    }
    
    suspend fun searchCategories(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE
    ): NetworkResult<PageResponse<CategoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CATEGORIES_SEARCH}") {
                parameter("q", query)
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
            }
            response.body<PageResponse<CategoryDTO>>()
        }
    }
    
    suspend fun getCategoriesByStatus(status: String): NetworkResult<List<CategoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.categoryByStatus(status)}")
            response.body<List<CategoryDTO>>()
        }
    }
    
    suspend fun getEmptyCategories(): NetworkResult<List<CategoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.CATEGORIES_EMPTY}")
            response.body<List<CategoryDTO>>()
        }
    }
    
    suspend fun updateCategoryStatus(id: Long, status: String): NetworkResult<CategoryDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.categoryStatus(id)}") {
                contentType(ContentType.Application.Json)
                setBody(CategoryStatusUpdateRequest(status))
            }
            response.body<CategoryDTO>()
        }
    }
}
