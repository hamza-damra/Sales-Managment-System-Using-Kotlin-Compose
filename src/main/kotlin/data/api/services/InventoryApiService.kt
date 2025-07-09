package data.api.services

import data.api.*
import data.api.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for inventory management operations
 */
class InventoryApiService(private val httpClient: HttpClient) {
    
    suspend fun getAllInventories(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR,
        status: String? = null
    ): NetworkResult<PageResponse<InventoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.INVENTORIES}") {
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
                status?.let { parameter("status", it) }
            }
            response.body<PageResponse<InventoryDTO>>()
        }
    }
    
    suspend fun getInventoryById(id: Long): NetworkResult<InventoryDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.inventoryById(id)}")
            response.body<InventoryDTO>()
        }
    }
    
    suspend fun createInventory(request: InventoryCreateRequest): NetworkResult<InventoryDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.INVENTORIES}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<InventoryDTO>()
        }
    }
    
    suspend fun updateInventory(id: Long, request: InventoryUpdateRequest): NetworkResult<InventoryDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.inventoryById(id)}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.body<InventoryDTO>()
        }
    }
    
    suspend fun deleteInventory(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.inventoryById(id)}")
        }
    }
    
    suspend fun searchInventories(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = ApiConfig.Pagination.DEFAULT_SORT_BY,
        sortDir: String = ApiConfig.Pagination.DEFAULT_SORT_DIR
    ): NetworkResult<PageResponse<InventoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.INVENTORIES_SEARCH}") {
                parameter("query", query)
                parameter("page", page)
                parameter("size", size.coerceAtMost(ApiConfig.Pagination.MAX_SIZE))
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
            }
            response.body<PageResponse<InventoryDTO>>()
        }
    }
    
    suspend fun getActiveInventories(): NetworkResult<List<InventoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.INVENTORIES_ACTIVE}")
            response.body<List<InventoryDTO>>()
        }
    }
    
    suspend fun getMainWarehouses(): NetworkResult<List<InventoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.INVENTORIES_MAIN_WAREHOUSES}")
            response.body<List<InventoryDTO>>()
        }
    }
    
    suspend fun getInventoryByName(name: String): NetworkResult<InventoryDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.inventoryByName(name)}")
            response.body<InventoryDTO>()
        }
    }
    
    suspend fun getInventoryByWarehouseCode(warehouseCode: String): NetworkResult<InventoryDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.inventoryByWarehouseCode(warehouseCode)}")
            response.body<InventoryDTO>()
        }
    }
    
    suspend fun getInventoriesByStatus(status: String): NetworkResult<List<InventoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.inventoryByStatus(status)}")
            response.body<List<InventoryDTO>>()
        }
    }
    
    suspend fun getEmptyInventories(): NetworkResult<List<InventoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.INVENTORIES_EMPTY}")
            response.body<List<InventoryDTO>>()
        }
    }
    
    suspend fun getInventoriesNearCapacity(threshold: Double = 80.0): NetworkResult<List<InventoryDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.INVENTORIES_NEAR_CAPACITY}") {
                parameter("threshold", threshold)
            }
            response.body<List<InventoryDTO>>()
        }
    }
    
    suspend fun updateInventoryStatus(id: Long, status: InventoryStatus): NetworkResult<InventoryDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.inventoryStatus(id)}") {
                contentType(ContentType.Application.Json)
                setBody(InventoryStatusUpdateRequest(status))
            }
            response.body<InventoryDTO>()
        }
    }
}
