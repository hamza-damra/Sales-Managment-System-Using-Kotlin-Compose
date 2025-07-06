package utils

import data.api.ProductDTO
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.BufferedReader
import java.io.FileReader

/**
 * Utility class for importing products from various file formats
 */
object ProductImportUtils {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }
    
    /**
     * Import products from a file (CSV or JSON)
     */
    fun importProductsFromFile(file: File): ImportResult {
        return try {
            when (FileDialogUtils.getFileExtension(file)) {
                "csv" -> importFromCsv(file)
                "json" -> importFromJson(file)
                else -> ImportResult.Error("نوع الملف غير مدعوم. يرجى اختيار ملف CSV أو JSON")
            }
        } catch (e: Exception) {
            ImportResult.Error("خطأ في قراءة الملف: ${e.message}")
        }
    }
    
    /**
     * Import products from CSV file
     */
    private fun importFromCsv(file: File): ImportResult {
        val products = mutableListOf<ProductDTO>()
        val errors = mutableListOf<String>()
        var lineNumber = 0
        
        try {
            BufferedReader(FileReader(file)).use { reader ->
                val headerLine = reader.readLine()
                if (headerLine == null) {
                    return ImportResult.Error("الملف فارغ")
                }
                
                lineNumber++
                val headers = headerLine.split(",").map { it.trim().replace("\"", "") }
                
                // Validate required headers
                val requiredHeaders = listOf("name", "price", "stockQuantity")
                val missingHeaders = requiredHeaders.filter { it !in headers }
                if (missingHeaders.isNotEmpty()) {
                    return ImportResult.Error("الحقول المطلوبة مفقودة: ${missingHeaders.joinToString(", ")}")
                }
                
                reader.forEachLine { line ->
                    lineNumber++
                    try {
                        val values = parseCsvLine(line)
                        if (values.size != headers.size) {
                            errors.add("السطر $lineNumber: عدد القيم لا يطابق عدد الحقول")
                            return@forEachLine
                        }
                        
                        val productData = headers.zip(values).toMap()
                        val product = createProductFromMap(productData, lineNumber)
                        
                        if (product != null) {
                            products.add(product)
                        } else {
                            errors.add("السطر $lineNumber: فشل في إنشاء المنتج")
                        }
                    } catch (e: Exception) {
                        errors.add("السطر $lineNumber: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            return ImportResult.Error("خطأ في قراءة ملف CSV: ${e.message}")
        }
        
        return ImportResult.Success(products, errors)
    }
    
    /**
     * Import products from JSON file
     */
    private fun importFromJson(file: File): ImportResult {
        return try {
            val jsonContent = file.readText()
            val products = json.decodeFromString<List<ProductDTO>>(jsonContent)
            
            // Validate required fields
            val errors = mutableListOf<String>()
            val validProducts = mutableListOf<ProductDTO>()
            
            products.forEachIndexed { index, product ->
                if (product.name.isBlank()) {
                    errors.add("المنتج ${index + 1}: اسم المنتج مطلوب")
                } else if (product.price <= 0) {
                    errors.add("المنتج ${index + 1}: السعر يجب أن يكون أكبر من صفر")
                } else if ((product.stockQuantity ?: 0) < 0) {
                    errors.add("المنتج ${index + 1}: كمية المخزون لا يمكن أن تكون سالبة")
                } else {
                    validProducts.add(product)
                }
            }
            
            ImportResult.Success(validProducts, errors)
        } catch (e: Exception) {
            ImportResult.Error("خطأ في قراءة ملف JSON: ${e.message}")
        }
    }
    
    /**
     * Parse CSV line handling quoted values
     */
    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' && !inQuotes -> inQuotes = true
                char == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                char == ',' && !inQuotes -> {
                    values.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
            i++
        }
        values.add(current.toString().trim())
        
        return values
    }
    
    /**
     * Create ProductDTO from map of field values
     */
    private fun createProductFromMap(data: Map<String, String>, lineNumber: Int): ProductDTO? {
        return try {
            // Validate required fields first
            val name = data["name"]?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("اسم المنتج مطلوب")
            val price = data["price"]?.toDoubleOrNull()
                ?: throw IllegalArgumentException("السعر يجب أن يكون رقم صحيح")
            val stockQuantity = data["stockQuantity"]?.toIntOrNull()
                ?: throw IllegalArgumentException("كمية المخزون يجب أن تكون رقم صحيح")

            // Validate price and stock are positive
            if (price <= 0) {
                throw IllegalArgumentException("السعر يجب أن يكون أكبر من صفر")
            }
            if (stockQuantity < 0) {
                throw IllegalArgumentException("كمية المخزون لا يمكن أن تكون سالبة")
            }

            ProductDTO(
                // Required fields
                name = name,
                price = price,
                stockQuantity = stockQuantity,

                // Basic product information
                description = data["description"]?.takeIf { it.isNotBlank() },
                category = data["category"]?.takeIf { it.isNotBlank() },
                sku = data["sku"]?.takeIf { it.isNotBlank() },
                costPrice = data["costPrice"]?.toDoubleOrNull(),
                brand = data["brand"]?.takeIf { it.isNotBlank() },
                modelNumber = data["modelNumber"]?.takeIf { it.isNotBlank() },
                barcode = data["barcode"]?.takeIf { it.isNotBlank() },

                // Physical properties
                weight = data["weight"]?.toDoubleOrNull(),
                length = data["length"]?.toDoubleOrNull(),
                width = data["width"]?.toDoubleOrNull(),
                height = data["height"]?.toDoubleOrNull(),

                // Product status and classification
                productStatus = data["productStatus"]?.takeIf { it.isNotBlank() },

                // Stock management
                minStockLevel = data["minStockLevel"]?.toIntOrNull(),
                maxStockLevel = data["maxStockLevel"]?.toIntOrNull(),
                reorderPoint = data["reorderPoint"]?.toIntOrNull(),
                reorderQuantity = data["reorderQuantity"]?.toIntOrNull(),

                // Supplier information
                supplierName = data["supplierName"]?.takeIf { it.isNotBlank() },
                supplierCode = data["supplierCode"]?.takeIf { it.isNotBlank() },

                // Product lifecycle
                warrantyPeriod = data["warrantyPeriod"]?.toIntOrNull(),
                expiryDate = data["expiryDate"]?.takeIf { it.isNotBlank() },
                manufacturingDate = data["manufacturingDate"]?.takeIf { it.isNotBlank() },

                // Tags and images
                tags = data["tags"]?.takeIf { it.isNotBlank() }?.split(";")?.map { it.trim() },
                imageUrl = data["imageUrl"]?.takeIf { it.isNotBlank() },
                additionalImages = data["additionalImages"]?.takeIf { it.isNotBlank() }?.split(";")?.map { it.trim() },

                // Product characteristics
                isSerialized = data["isSerialized"]?.toBooleanStrictOrNull(),
                isDigital = data["isDigital"]?.toBooleanStrictOrNull(),
                isTaxable = data["isTaxable"]?.toBooleanStrictOrNull(),

                // Pricing and measurement
                taxRate = data["taxRate"]?.toDoubleOrNull(),
                unitOfMeasure = data["unitOfMeasure"]?.takeIf { it.isNotBlank() } ?: "PCS",
                discountPercentage = data["discountPercentage"]?.toDoubleOrNull(),

                // Warehouse and location
                locationInWarehouse = data["locationInWarehouse"]?.takeIf { it.isNotBlank() },

                // Sales tracking (read-only fields for import)
                totalSold = data["totalSold"]?.toIntOrNull(),
                totalRevenue = data["totalRevenue"]?.toDoubleOrNull(),
                lastSoldDate = data["lastSoldDate"]?.takeIf { it.isNotBlank() },
                lastRestockedDate = data["lastRestockedDate"]?.takeIf { it.isNotBlank() },

                // Additional information
                notes = data["notes"]?.takeIf { it.isNotBlank() },
                createdAt = data["createdAt"]?.takeIf { it.isNotBlank() },
                updatedAt = data["updatedAt"]?.takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Export products to CSV format
     */
    fun exportProductsToCsv(products: List<ProductDTO>, file: File): Boolean {
        return try {
            file.bufferedWriter().use { writer ->
                // Write header with all available fields
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
                writer.write(headers.joinToString(","))
                writer.newLine()

                // Write data
                products.forEach { product ->
                    val values = listOf(
                        // Required fields
                        escapeCsvValue(product.name),
                        product.price.toString(),
                        product.stockQuantity?.toString() ?: "",

                        // Basic product information
                        escapeCsvValue(product.description ?: ""),
                        escapeCsvValue(product.category ?: ""),
                        escapeCsvValue(product.sku ?: ""),
                        product.costPrice?.toString() ?: "",
                        escapeCsvValue(product.brand ?: ""),
                        escapeCsvValue(product.modelNumber ?: ""),
                        escapeCsvValue(product.barcode ?: ""),

                        // Physical properties
                        product.weight?.toString() ?: "",
                        product.length?.toString() ?: "",
                        product.width?.toString() ?: "",
                        product.height?.toString() ?: "",

                        // Product status and classification
                        escapeCsvValue(product.productStatus ?: ""),

                        // Stock management
                        product.minStockLevel?.toString() ?: "",
                        product.maxStockLevel?.toString() ?: "",
                        product.reorderPoint?.toString() ?: "",
                        product.reorderQuantity?.toString() ?: "",

                        // Supplier information
                        escapeCsvValue(product.supplierName ?: ""),
                        escapeCsvValue(product.supplierCode ?: ""),

                        // Product lifecycle
                        product.warrantyPeriod?.toString() ?: "",
                        escapeCsvValue(product.expiryDate ?: ""),
                        escapeCsvValue(product.manufacturingDate ?: ""),

                        // Tags and images (join lists with semicolons)
                        escapeCsvValue(product.tags?.joinToString(";") ?: ""),
                        escapeCsvValue(product.imageUrl ?: ""),
                        escapeCsvValue(product.additionalImages?.joinToString(";") ?: ""),

                        // Product characteristics
                        product.isSerialized?.toString() ?: "",
                        product.isDigital?.toString() ?: "",
                        product.isTaxable?.toString() ?: "",

                        // Pricing and measurement
                        product.taxRate?.toString() ?: "",
                        escapeCsvValue(product.unitOfMeasure ?: ""),
                        product.discountPercentage?.toString() ?: "",

                        // Warehouse and location
                        escapeCsvValue(product.locationInWarehouse ?: ""),

                        // Sales tracking
                        product.totalSold?.toString() ?: "",
                        product.totalRevenue?.toString() ?: "",
                        escapeCsvValue(product.lastSoldDate ?: ""),
                        escapeCsvValue(product.lastRestockedDate ?: ""),

                        // Additional information
                        escapeCsvValue(product.notes ?: ""),
                        escapeCsvValue(product.createdAt ?: ""),
                        escapeCsvValue(product.updatedAt ?: "")
                    )
                    writer.write(values.joinToString(","))
                    writer.newLine()
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Export products to JSON format
     */
    fun exportProductsToJson(products: List<ProductDTO>, file: File): Boolean {
        return try {
            val jsonString = json.encodeToString(products)
            file.writeText(jsonString)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Escape CSV value by adding quotes if necessary
     */
    private fun escapeCsvValue(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\"" 
        } else {
            value
        }
    }
    
    /**
     * Result of import operation
     */
    sealed class ImportResult {
        data class Success(val products: List<ProductDTO>, val warnings: List<String> = emptyList()) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
