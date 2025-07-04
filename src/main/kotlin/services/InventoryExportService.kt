package services

import data.*
import data.api.*
import data.repository.ProductRepository
import data.api.services.ReportsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.ExcelExportUtils
import utils.PdfExportUtils

/**
 * Service for handling inventory export operations
 */
class InventoryExportService(
    private val productRepository: ProductRepository,
    private val reportsApiService: ReportsApiService
) {
    
    /**
     * Export inventory overview to Excel
     */
    suspend fun exportInventoryOverviewToExcel(fileName: String? = null): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch products data
                val productsResult = productRepository.loadProducts(page = 0, size = 1000)
                if (productsResult is NetworkResult.Error) {
                    return@withContext Result.failure(Exception("Failed to load products: ${productsResult.exception.message}"))
                }
                
                val products = (productsResult as NetworkResult.Success).data.content
                
                // Fetch inventory report for low stock products
                val inventoryReportResult = reportsApiService.getInventoryReport()
                val lowStockProducts = if (inventoryReportResult is NetworkResult.Success) {
                    inventoryReportResult.data.lowStockProducts
                } else {
                    // Fallback: calculate low stock products from products list
                    products.filter { (it.stockQuantity ?: 0) <= (it.minStockLevel ?: 10) }
                        .map { product ->
                            LowStockProductDTO(
                                productId = product.id ?: 0,
                                productName = product.name,
                                currentStock = product.stockQuantity ?: 0,
                                minStockLevel = product.minStockLevel ?: 10,
                                reorderPoint = product.reorderPoint ?: 15,
                                category = product.category
                            )
                        }
                }
                
                // Export to Excel
                val success = ExcelExportUtils.exportInventoryOverview(
                    products = products,
                    lowStockProducts = lowStockProducts,
                    fileName = fileName
                )
                
                Result.success(success)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Export inventory overview to PDF
     */
    suspend fun exportInventoryOverviewToPdf(fileName: String? = null): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Fetch products data
                val productsResult = productRepository.loadProducts(page = 0, size = 1000)
                if (productsResult is NetworkResult.Error) {
                    return@withContext Result.failure(Exception("Failed to load products: ${productsResult.exception.message}"))
                }
                
                val products = (productsResult as NetworkResult.Success).data.content
                
                // Fetch inventory report for low stock products
                val inventoryReportResult = reportsApiService.getInventoryReport()
                val lowStockProducts = if (inventoryReportResult is NetworkResult.Success) {
                    inventoryReportResult.data.lowStockProducts
                } else {
                    // Fallback: calculate low stock products from products list
                    products.filter { (it.stockQuantity ?: 0) <= (it.minStockLevel ?: 10) }
                        .map { product ->
                            LowStockProductDTO(
                                productId = product.id ?: 0,
                                productName = product.name,
                                currentStock = product.stockQuantity ?: 0,
                                minStockLevel = product.minStockLevel ?: 10,
                                reorderPoint = product.reorderPoint ?: 15,
                                category = product.category
                            )
                        }
                }
                
                // Export to PDF
                val success = PdfExportUtils.exportInventoryOverview(
                    products = products,
                    lowStockProducts = lowStockProducts,
                    fileName = fileName
                )
                
                Result.success(success)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Export products list to Excel
     */
    suspend fun exportProductsListToExcel(
        category: String? = null,
        searchQuery: String? = null,
        fileName: String? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val products = if (!searchQuery.isNullOrBlank()) {
                    // Search products
                    val searchResult = productRepository.searchProducts(searchQuery, page = 0, size = 1000)
                    if (searchResult is NetworkResult.Error) {
                        return@withContext Result.failure(Exception("Failed to search products: ${searchResult.exception.message}"))
                    }
                    (searchResult as NetworkResult.Success).data.content
                } else {
                    // Load all products with optional category filter
                    val productsResult = productRepository.loadProducts(
                        page = 0, 
                        size = 1000, 
                        category = category
                    )
                    if (productsResult is NetworkResult.Error) {
                        return@withContext Result.failure(Exception("Failed to load products: ${productsResult.exception.message}"))
                    }
                    (productsResult as NetworkResult.Success).data.content
                }
                
                // Export to Excel
                val success = ExcelExportUtils.exportProductsList(
                    products = products,
                    fileName = fileName
                )
                
                Result.success(success)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Export products list to PDF
     */
    suspend fun exportProductsListToPdf(
        category: String? = null,
        searchQuery: String? = null,
        fileName: String? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val products = if (!searchQuery.isNullOrBlank()) {
                    // Search products
                    val searchResult = productRepository.searchProducts(searchQuery, page = 0, size = 1000)
                    if (searchResult is NetworkResult.Error) {
                        return@withContext Result.failure(Exception("Failed to search products: ${searchResult.exception.message}"))
                    }
                    (searchResult as NetworkResult.Success).data.content
                } else {
                    // Load all products with optional category filter
                    val productsResult = productRepository.loadProducts(
                        page = 0, 
                        size = 1000, 
                        category = category
                    )
                    if (productsResult is NetworkResult.Error) {
                        return@withContext Result.failure(Exception("Failed to load products: ${productsResult.exception.message}"))
                    }
                    (productsResult as NetworkResult.Success).data.content
                }
                
                // Export to PDF
                val success = PdfExportUtils.exportProductsList(
                    products = products,
                    fileName = fileName
                )
                
                Result.success(success)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Export stock movements to Excel
     */
    suspend fun exportStockMovementsToExcel(fileName: String? = null): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // For now, we'll use sample data since there's no stock movements API endpoint
                // In a real implementation, you would fetch this from an API
                val sampleMovements = generateSampleStockMovements()
                val productNames = generateSampleProductNames()
                
                // Export to Excel
                val success = ExcelExportUtils.exportStockMovements(
                    movements = sampleMovements,
                    productNames = productNames,
                    fileName = fileName
                )
                
                Result.success(success)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Export stock movements to PDF
     */
    suspend fun exportStockMovementsToPdf(fileName: String? = null): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // For now, we'll use sample data since there's no stock movements API endpoint
                // In a real implementation, you would fetch this from an API
                val sampleMovements = generateSampleStockMovements()
                val productNames = generateSampleProductNames()
                
                // Export to PDF
                val success = PdfExportUtils.exportStockMovements(
                    movements = sampleMovements,
                    productNames = productNames,
                    fileName = fileName
                )
                
                Result.success(success)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // Sample data generators (replace with actual API calls when available)
    private fun generateSampleStockMovements(): List<StockMovement> {
        return listOf(
            StockMovement(
                id = 1,
                productId = 1,
                warehouseId = 1,
                movementType = MovementType.PURCHASE,
                quantity = 100,
                date = kotlinx.datetime.LocalDateTime(2024, 1, 15, 10, 30),
                reference = "PO-001",
                notes = "شراء جديد من المورد"
            ),
            StockMovement(
                id = 2,
                productId = 2,
                warehouseId = 1,
                movementType = MovementType.SALE,
                quantity = -25,
                date = kotlinx.datetime.LocalDateTime(2024, 1, 16, 14, 15),
                reference = "INV-001",
                notes = "بيع للعميل"
            ),
            StockMovement(
                id = 3,
                productId = 1,
                warehouseId = 1,
                movementType = MovementType.ADJUSTMENT,
                quantity = -5,
                date = kotlinx.datetime.LocalDateTime(2024, 1, 17, 9, 0),
                reference = "ADJ-001",
                notes = "تعديل جرد"
            )
        )
    }
    
    private fun generateSampleProductNames(): Map<Int, String> {
        return mapOf(
            1 to "لابتوب Dell XPS 13",
            2 to "ماوس لاسلكي Logitech",
            3 to "كيبورد ميكانيكي",
            4 to "شاشة Samsung 24 بوصة",
            5 to "سماعات Sony WH-1000XM4"
        )
    }
}
