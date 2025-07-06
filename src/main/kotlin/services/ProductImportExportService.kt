package services

import data.api.ProductDTO
import data.api.NetworkResult
import data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.ExcelExportUtils
import utils.SimpleExcelExportUtils
import utils.FileDialogUtils
import utils.ProductImportUtils
import java.io.File

/**
 * Service for handling product import and export operations
 */
class ProductImportExportService(
    private val productRepository: ProductRepository
) {
    
    /**
     * Export products to Excel format with fallback
     */
    suspend fun exportProductsToExcel(
        products: List<ProductDTO>,
        fileName: String? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                // Try the original Excel export first
                val success = ExcelExportUtils.exportProductsList(products, fileName)
                Result.success(success)
            } catch (e: Exception) {
                println("⚠️ Original Excel export failed, trying simplified export: ${e.message}")
                try {
                    // Fallback to simplified Excel export (CSV with Excel extension)
                    val success = SimpleExcelExportUtils.exportProductsList(products, fileName)
                    if (success) {
                        println("✅ Excel export completed using CSV format (Excel-compatible)")
                        Result.success(success)
                    } else {
                        Result.failure(Exception("فشل في تصدير Excel. جرب تصدير CSV كبديل."))
                    }
                } catch (fallbackException: Exception) {
                    println("❌ Both Excel export methods failed")
                    fallbackException.printStackTrace()
                    Result.failure(Exception("فشل في تصدير Excel. جرب تصدير CSV كبديل. الخطأ: ${fallbackException.message}"))
                }
            }
        }
    }
    
    /**
     * Export products to CSV format
     */
    suspend fun exportProductsToCsv(
        products: List<ProductDTO>,
        fileName: String? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val file = FileDialogUtils.selectCsvSaveFile(fileName)
                if (file != null) {
                    val success = ProductImportUtils.exportProductsToCsv(products, file)
                    Result.success(success)
                } else {
                    Result.success(false) // User cancelled
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Export products to JSON format
     */
    suspend fun exportProductsToJson(
        products: List<ProductDTO>,
        fileName: String? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val file = FileDialogUtils.selectJsonSaveFile(fileName)
                if (file != null) {
                    val success = ProductImportUtils.exportProductsToJson(products, file)
                    Result.success(success)
                } else {
                    Result.success(false) // User cancelled
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Import products from file (Step 1: Parse and validate only)
     */
    suspend fun parseProductsFromFile(): Result<ParsedImportData> {
        return withContext(Dispatchers.IO) {
            try {
                val file = FileDialogUtils.selectImportFile()
                if (file == null) {
                    return@withContext Result.success(ParsedImportData.Cancelled)
                }

                if (!FileDialogUtils.isValidImportFile(file)) {
                    return@withContext Result.failure(Exception("ملف غير صالح أو غير مدعوم"))
                }

                val importResult = ProductImportUtils.importProductsFromFile(file)

                when (importResult) {
                    is ProductImportUtils.ImportResult.Error -> {
                        Result.failure(Exception(importResult.message))
                    }
                    is ProductImportUtils.ImportResult.Success -> {
                        if (importResult.products.isEmpty()) {
                            Result.success(ParsedImportData.NoValidProducts(importResult.warnings))
                        } else {
                            Result.success(
                                ParsedImportData.Success(
                                    products = importResult.products,
                                    warnings = importResult.warnings,
                                    fileName = file.name
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Upload parsed products to database (Step 2: Actual database import)
     */
    suspend fun uploadProductsToDatabase(products: List<ProductDTO>): Result<ImportSummary> {
        return withContext(Dispatchers.IO) {
            try {
                val results = importProductsToBackend(products)
                Result.success(
                    ImportSummary.Success(
                        totalProducts = products.size,
                        successfulImports = results.successCount,
                        failedImports = results.failureCount,
                        warnings = emptyList(),
                        errors = results.errors
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Import products to backend API with enhanced error handling
     */
    private suspend fun importProductsToBackend(products: List<ProductDTO>): ImportBackendResult {
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()

        products.forEachIndexed { index, product ->
            try {
                // Log the product being sent for debugging
                println("🔄 Uploading product ${index + 1}: ${product.name}")

                val result = productRepository.createProduct(product)
                when (result) {
                    is NetworkResult.Success -> {
                        successCount++
                        println("✅ Product ${index + 1} uploaded successfully: ${product.name}")
                    }
                    is NetworkResult.Error -> {
                        failureCount++
                        val errorMessage = result.exception.message ?: "خطأ غير معروف"
                        val detailedError = "المنتج ${index + 1} (${product.name}): $errorMessage"
                        errors.add(detailedError)
                        println("❌ Product ${index + 1} failed: $detailedError")

                        // Log additional details for debugging
                        println("   Product details: name=${product.name}, price=${product.price}, stock=${product.stockQuantity}")
                    }
                    is NetworkResult.Loading -> {
                        // Should not happen in this context
                    }
                }
            } catch (e: Exception) {
                failureCount++
                val detailedError = "المنتج ${index + 1} (${product.name}): ${e.message}"
                errors.add(detailedError)
                println("❌ Exception for product ${index + 1}: $detailedError")
                e.printStackTrace()
            }
        }

        println("📊 Import summary: $successCount successful, $failureCount failed")
        return ImportBackendResult(successCount, failureCount, errors)
    }
    
    /**
     * Get sample CSV template with all available fields
     */
    fun generateSampleCsvTemplate(): String {
        val headers = listOf(
            // Required fields
            "name", "price", "stockQuantity",

            // Basic product information
            "description", "category", "sku", "costPrice", "brand", "modelNumber", "barcode",

            // Physical properties
            "weight", "length", "width", "height",

            // Product status and classification
            "productStatus",

            // Stock management
            "minStockLevel", "maxStockLevel", "reorderPoint", "reorderQuantity",

            // Supplier information
            "supplierName", "supplierCode",

            // Product lifecycle
            "warrantyPeriod", "expiryDate", "manufacturingDate",

            // Tags and images
            "tags", "imageUrl", "additionalImages",

            // Product characteristics
            "isSerialized", "isDigital", "isTaxable",

            // Pricing and measurement
            "taxRate", "unitOfMeasure", "discountPercentage",

            // Warehouse and location
            "locationInWarehouse",

            // Sales tracking
            "totalSold", "totalRevenue", "lastSoldDate", "lastRestockedDate",

            // Additional information
            "notes", "createdAt", "updatedAt"
        )

        val sampleData = listOf(
            // Required fields
            "منتج تجريبي", "100.0", "50",

            // Basic product information
            "وصف المنتج التجريبي", "إلكترونيات", "SKU001", "80.0", "علامة تجارية", "MODEL001", "1234567890",

            // Physical properties
            "1.5", "10.0", "5.0", "3.0",

            // Product status and classification
            "ACTIVE",

            // Stock management
            "10", "100", "20", "50",

            // Supplier information
            "مورد تجريبي", "SUP001",

            // Product lifecycle
            "12", "2025-12-31", "2024-01-15",

            // Tags and images
            "إلكترونيات;جديد;مميز", "https://example.com/image.jpg", "https://example.com/img1.jpg;https://example.com/img2.jpg",

            // Product characteristics
            "false", "false", "true",

            // Pricing and measurement
            "15.0", "PCS", "5.0",

            // Warehouse and location
            "A1-B2-C3",

            // Sales tracking
            "25", "2500.0", "2024-01-10T14:30:00", "2024-01-05T09:00:00",

            // Additional information
            "ملاحظات إضافية حول المنتج", "2024-01-01T10:00:00", "2024-01-10T15:30:00"
        )

        return headers.joinToString(",") + "\n" + sampleData.joinToString(",")
    }
    
    /**
     * Save sample CSV template to file
     */
    suspend fun saveSampleCsvTemplate(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val file = FileDialogUtils.selectCsvSaveFile("sample_products_template.csv")
                if (file != null) {
                    file.writeText(generateSampleCsvTemplate())
                    Result.success(true)
                } else {
                    Result.success(false) // User cancelled
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Result of backend import operation
     */
    private data class ImportBackendResult(
        val successCount: Int,
        val failureCount: Int,
        val errors: List<String>
    )
    
    /**
     * Result of parsing import file (before database upload)
     */
    sealed class ParsedImportData {
        object Cancelled : ParsedImportData()

        data class NoValidProducts(val warnings: List<String>) : ParsedImportData()

        data class Success(
            val products: List<ProductDTO>,
            val warnings: List<String>,
            val fileName: String
        ) : ParsedImportData()
    }

    /**
     * Summary of import operation
     */
    sealed class ImportSummary {
        object Cancelled : ImportSummary()

        data class NoValidProducts(val warnings: List<String>) : ImportSummary()

        data class Success(
            val totalProducts: Int,
            val successfulImports: Int,
            val failedImports: Int,
            val warnings: List<String>,
            val errors: List<String>
        ) : ImportSummary()
    }
}
