package data.api.services

import data.api.*
import data.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * API service for stock movement operations
 * Currently uses aggregated data from sales and products until dedicated stock movement endpoints are available
 */
class StockMovementApiService(private val httpClient: HttpClient) {
    
    /**
     * Get stock movements with pagination and filtering
     * Currently aggregates data from sales API and creates movement records
     */
    suspend fun getStockMovements(
        page: Int = ApiConfig.Pagination.DEFAULT_PAGE,
        size: Int = ApiConfig.Pagination.DEFAULT_SIZE,
        sortBy: String = "date",
        sortDir: String = "desc",
        warehouseId: Long? = null,
        productId: Long? = null,
        movementType: MovementType? = null,
        startDate: String? = null,
        endDate: String? = null
    ): NetworkResult<PageResponse<StockMovementDTO>> {
        return safeApiCall {
            // For now, we'll aggregate data from sales to create stock movements
            // In the future, this should call a dedicated stock movements endpoint
            
            // Get sales data to derive stock movements
            val salesResponse = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.SALES}") {
                parameter("page", 0)
                parameter("size", 100) // Get more sales to create movements
                parameter("sortBy", "saleDate")
                parameter("sortDir", "desc")
                startDate?.let { parameter("startDate", it) }
                endDate?.let { parameter("endDate", it) }
            }
            
            val salesPage = salesResponse.body<PageResponse<SaleDTO>>()
            val movements = mutableListOf<StockMovementDTO>()
            
            // Convert sales to stock movements
            salesPage.content.forEachIndexed { index, sale ->
                sale.items.forEach { item ->
                    movements.add(
                        StockMovementDTO(
                            id = (index * 1000 + (item.id ?: 0L)).toLong(),
                            productId = item.productId,
                            productName = item.productName ?: "منتج غير محدد",
                            warehouseId = 1L, // Default warehouse for now
                            warehouseName = "المستودع الرئيسي",
                            movementType = MovementType.SALE,
                            quantity = -item.quantity, // Negative for sales
                            date = sale.saleDate ?: "",
                            reference = sale.saleNumber ?: "SALE-${sale.id ?: 0L}",
                            notes = "بيع للعميل ${sale.customerName ?: "عميل غير محدد"}",
                            unitPrice = item.unitPrice,
                            totalValue = item.totalPrice ?: 0.0
                        )
                    )
                }
            }
            
            // Add some sample purchase movements for demonstration
            val samplePurchases = generateSamplePurchaseMovements()
            movements.addAll(samplePurchases)
            
            // Apply filters
            var filteredMovements: List<StockMovementDTO> = movements

            warehouseId?.let { id ->
                filteredMovements = filteredMovements.filter { it.warehouseId == id }
            }

            productId?.let { id ->
                filteredMovements = filteredMovements.filter { it.productId == id }
            }

            movementType?.let { type ->
                filteredMovements = filteredMovements.filter { it.movementType == type }
            }
            
            // Sort movements
            filteredMovements = when (sortBy) {
                "date" -> if (sortDir == "desc") filteredMovements.sortedByDescending { it.date } else filteredMovements.sortedBy { it.date }
                "quantity" -> if (sortDir == "desc") filteredMovements.sortedByDescending { it.quantity } else filteredMovements.sortedBy { it.quantity }
                "productName" -> if (sortDir == "desc") filteredMovements.sortedByDescending { it.productName } else filteredMovements.sortedBy { it.productName }
                else -> filteredMovements.sortedByDescending { it.date }
            }
            
            // Apply pagination
            val totalElements = filteredMovements.size
            val startIndex = page * size
            val endIndex = minOf(startIndex + size, totalElements)
            val pageContent = if (startIndex < totalElements) {
                filteredMovements.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
            
            PageResponse(
                content = pageContent,
                pageable = PageableInfo(
                    pageNumber = page,
                    pageSize = size,
                    sort = SortInfo(sorted = true, unsorted = false)
                ),
                totalElements = totalElements.toLong(),
                totalPages = (totalElements + size - 1) / size,
                first = page == 0,
                last = page >= (totalElements + size - 1) / size - 1,
                numberOfElements = pageContent.size,
                empty = pageContent.isEmpty()
            )
        }
    }
    
    /**
     * Generate sample purchase movements for demonstration
     */
    private fun generateSamplePurchaseMovements(): List<StockMovementDTO> {
        return listOf(
            StockMovementDTO(
                id = 10001L,
                productId = 1L,
                productName = "Smartphone",
                warehouseId = 1L,
                warehouseName = "المستودع الرئيسي",
                movementType = MovementType.PURCHASE,
                quantity = 50,
                date = "2024-01-15T10:30:00",
                reference = "PO-001",
                notes = "شراء جديد من المورد الرئيسي",
                unitPrice = 800.0,
                totalValue = 40000.0
            ),
            StockMovementDTO(
                id = 10002L,
                productId = 2L,
                productName = "Laptop",
                warehouseId = 1L,
                warehouseName = "المستودع الرئيسي",
                movementType = MovementType.PURCHASE,
                quantity = 25,
                date = "2024-01-14T14:15:00",
                reference = "PO-002",
                notes = "شراء أجهزة لابتوب جديدة",
                unitPrice = 1200.0,
                totalValue = 30000.0
            ),
            StockMovementDTO(
                id = 10003L,
                productId = 3L,
                productName = "Headphones",
                warehouseId = 1L,
                warehouseName = "المستودع الرئيسي",
                movementType = MovementType.ADJUSTMENT,
                quantity = -5,
                date = "2024-01-13T16:45:00",
                reference = "ADJ-001",
                notes = "تعديل المخزون - منتجات تالفة",
                unitPrice = 150.0,
                totalValue = -750.0
            ),
            StockMovementDTO(
                id = 10004L,
                productId = 1L,
                productName = "Smartphone",
                warehouseId = 1L,
                warehouseName = "المستودع الرئيسي",
                movementType = MovementType.RETURN,
                quantity = 3,
                date = "2024-01-12T11:20:00",
                reference = "RET-001",
                notes = "إرجاع من العميل - عيب في التصنيع",
                unitPrice = 999.99,
                totalValue = 2999.97
            )
        )
    }
}

/**
 * Stock Movement DTO for API responses
 */
@Serializable
data class StockMovementDTO(
    val id: Long,
    val productId: Long,
    val productName: String,
    val warehouseId: Long,
    val warehouseName: String,
    val movementType: MovementType,
    val quantity: Int,
    val date: String, // ISO datetime string
    val reference: String,
    val notes: String,
    val unitPrice: Double = 0.0,
    val totalValue: Double = 0.0
)
