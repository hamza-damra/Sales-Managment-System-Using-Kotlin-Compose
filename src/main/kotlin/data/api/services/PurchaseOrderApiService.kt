package data.api.services

import data.api.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * API service for purchase order management operations
 * Implements all endpoints from Purchase-Orders-API-Documentation.md
 */
class PurchaseOrderApiService(private val httpClient: HttpClient) {
    
    /**
     * Get all purchase orders with pagination and filtering
     */
    suspend fun getAllPurchaseOrders(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = "orderDate",
        sortDir: String = "desc",
        status: String? = null,
        supplierId: Long? = null,
        priority: String? = null,
        fromDate: String? = null,
        toDate: String? = null
    ): NetworkResult<PageResponse<PurchaseOrderDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PURCHASE_ORDERS}") {
                parameter("page", page)
                parameter("size", size)
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
                status?.let { parameter("status", it) }
                supplierId?.let { parameter("supplierId", it) }
                priority?.let { parameter("priority", it) }
                fromDate?.let { parameter("fromDate", it) }
                toDate?.let { parameter("toDate", it) }
            }
            response.body<PageResponse<PurchaseOrderDTO>>()
        }
    }
    
    /**
     * Get purchase order by ID
     */
    suspend fun getPurchaseOrderById(id: Long): NetworkResult<PurchaseOrderDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.purchaseOrderById(id)}")
            response.body<PurchaseOrderDTO>()
        }
    }
    
    /**
     * Create new purchase order
     */
    suspend fun createPurchaseOrder(order: PurchaseOrderDTO): NetworkResult<PurchaseOrderDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PURCHASE_ORDERS}") {
                contentType(ContentType.Application.Json)
                setBody(order)
            }
            response.body<PurchaseOrderDTO>()
        }
    }
    
    /**
     * Update existing purchase order
     */
    suspend fun updatePurchaseOrder(id: Long, order: PurchaseOrderDTO): NetworkResult<PurchaseOrderDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.purchaseOrderById(id)}") {
                contentType(ContentType.Application.Json)
                setBody(order)
            }
            response.body<PurchaseOrderDTO>()
        }
    }
    
    /**
     * Delete purchase order
     */
    suspend fun deletePurchaseOrder(id: Long): NetworkResult<Unit> {
        return safeApiCall {
            httpClient.delete("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.purchaseOrderById(id)}")
        }
    }
    
    /**
     * Search purchase orders across multiple fields
     */
    suspend fun searchPurchaseOrders(
        query: String,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = "orderDate",
        sortDir: String = "desc"
    ): NetworkResult<PageResponse<PurchaseOrderDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PURCHASE_ORDERS_SEARCH}") {
                parameter("query", query)
                parameter("page", page)
                parameter("size", size)
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
            }
            response.body<PageResponse<PurchaseOrderDTO>>()
        }
    }

    /**
     * Update purchase order status
     */
    suspend fun updatePurchaseOrderStatus(
        id: Long, 
        statusRequest: StatusUpdateRequestDTO
    ): NetworkResult<PurchaseOrderDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.purchaseOrderStatus(id)}") {
                contentType(ContentType.Application.Json)
                setBody(statusRequest)
            }
            response.body<PurchaseOrderDTO>()
        }
    }

    /**
     * Approve purchase order
     */
    suspend fun approvePurchaseOrder(
        id: Long, 
        approvalRequest: ApprovalRequestDTO
    ): NetworkResult<PurchaseOrderDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.purchaseOrderApprove(id)}") {
                contentType(ContentType.Application.Json)
                setBody(approvalRequest)
            }
            response.body<PurchaseOrderDTO>()
        }
    }

    /**
     * Receive purchase order items
     */
    suspend fun receivePurchaseOrderItems(
        id: Long, 
        receiveRequest: ReceiveItemsRequestDTO
    ): NetworkResult<PurchaseOrderDTO> {
        return safeApiCall {
            val response = httpClient.put("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.purchaseOrderReceive(id)}") {
                contentType(ContentType.Application.Json)
                setBody(receiveRequest)
            }
            response.body<PurchaseOrderDTO>()
        }
    }

    /**
     * Get purchase orders by supplier
     */
    suspend fun getPurchaseOrdersBySupplier(
        supplierId: Long,
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = "orderDate",
        sortDir: String = "desc"
    ): NetworkResult<PageResponse<PurchaseOrderDTO>> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.purchaseOrdersBySupplier(supplierId)}") {
                parameter("page", page)
                parameter("size", size)
                parameter("sortBy", sortBy)
                parameter("sortDir", sortDir)
            }
            response.body<PageResponse<PurchaseOrderDTO>>()
        }
    }

    /**
     * Get purchase order analytics
     */
    suspend fun getPurchaseOrderAnalytics(
        fromDate: String? = null,
        toDate: String? = null,
        supplierId: Long? = null
    ): NetworkResult<PurchaseOrderAnalyticsDTO> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PURCHASE_ORDERS_ANALYTICS}") {
                fromDate?.let { parameter("fromDate", it) }
                toDate?.let { parameter("toDate", it) }
                supplierId?.let { parameter("supplierId", it) }
            }
            response.body<PurchaseOrderAnalyticsDTO>()
        }
    }

    /**
     * Generate purchase order PDF
     */
    suspend fun generatePurchaseOrderPdf(id: Long): NetworkResult<ByteArray> {
        return safeApiCall {
            val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.purchaseOrderPdf(id)}")
            response.body<ByteArray>()
        }
    }

    /**
     * Send purchase order to supplier
     */
    suspend fun sendPurchaseOrderToSupplier(
        id: Long, 
        sendRequest: SendOrderRequestDTO
    ): NetworkResult<SendOrderResponseDTO> {
        return safeApiCall {
            val response = httpClient.post("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.purchaseOrderSend(id)}") {
                contentType(ContentType.Application.Json)
                setBody(sendRequest)
            }
            response.body<SendOrderResponseDTO>()
        }
    }
}
