package utils

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility to generate test data for import testing
 */
object TestDataGenerator {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    /**
     * Generate a simple test CSV file with valid products
     */
    fun generateSimpleTestCsv(fileName: String = "test_products.csv"): File {
        val file = File(fileName)
        
        val csvContent = """
name,price,stockQuantity,description,category,sku
"منتج اختبار 1",100.0,50,"وصف المنتج الأول","إلكترونيات","TEST001"
"منتج اختبار 2",200.0,30,"وصف المنتج الثاني","أجهزة","TEST002"
"منتج اختبار 3",150.0,25,"وصف المنتج الثالث","إكسسوارات","TEST003"
        """.trimIndent()
        
        file.writeText(csvContent)
        println("✅ Generated simple test CSV: ${file.absolutePath}")
        return file
    }
    
    /**
     * Generate a comprehensive test CSV file with all fields
     */
    fun generateComprehensiveTestCsv(fileName: String = "comprehensive_test_products.csv"): File {
        val file = File(fileName)
        
        // Get all headers
        val headers = listOf(
            "name", "price", "stockQuantity", "description", "category", "sku", 
            "costPrice", "brand", "modelNumber", "barcode", "weight", "length", 
            "width", "height", "productStatus", "minStockLevel", "maxStockLevel", 
            "reorderPoint", "reorderQuantity", "supplierName", "supplierCode", 
            "warrantyPeriod", "expiryDate", "manufacturingDate", "tags", "imageUrl", 
            "additionalImages", "isSerialized", "isDigital", "isTaxable", "taxRate", 
            "unitOfMeasure", "discountPercentage", "locationInWarehouse", 
            "totalSold", "totalRevenue", "lastSoldDate", "lastRestockedDate", 
            "notes", "createdAt", "updatedAt"
        )
        
        val products = listOf(
            listOf(
                "لابتوب اختبار", "1500.0", "10", "لابتوب للاختبار", "حاسوب", "LAPTOP001",
                "1200.0", "Dell", "XPS13", "123456789", "1.5", "30.0", "20.0", "2.0",
                "ACTIVE", "5", "50", "10", "20", "مورد الحاسوب", "COMP001",
                "24", "2026-12-31", "2024-01-15", "حاسوب;لابتوب;اختبار", 
                "https://example.com/laptop.jpg", "https://example.com/laptop1.jpg;https://example.com/laptop2.jpg",
                "true", "false", "true", "15.0", "PCS", "5.0", "A1-B1-C1",
                "5", "7500.0", "2024-01-10T14:30:00", "2024-01-05T09:00:00",
                "لابتوب عالي الجودة", "2024-01-01T10:00:00", "2024-01-10T15:30:00"
            ),
            listOf(
                "ماوس اختبار", "50.0", "100", "ماوس لاسلكي", "إكسسوارات", "MOUSE001",
                "30.0", "Logitech", "MX3", "987654321", "0.1", "10.0", "6.0", "3.0",
                "ACTIVE", "20", "200", "30", "50", "مورد الإكسسوارات", "ACC001",
                "12", "2025-12-31", "2024-02-01", "ماوس;لاسلكي;اختبار",
                "https://example.com/mouse.jpg", "",
                "false", "false", "true", "15.0", "PCS", "10.0", "A2-B2-C2",
                "15", "750.0", "2024-01-08T10:15:00", "2024-01-03T14:20:00",
                "ماوس مريح للاستخدام", "2024-01-01T11:00:00", "2024-01-08T16:45:00"
            ),
            listOf(
                "سماعات اختبار", "200.0", "25", "سماعات عالية الجودة", "صوتيات", "HEADPHONE001",
                "150.0", "Sony", "WH1000XM4", "456789123", "0.3", "20.0", "15.0", "8.0",
                "ACTIVE", "5", "100", "10", "25", "مورد الصوتيات", "AUDIO001",
                "18", "2025-06-30", "2024-03-01", "سماعات;صوت;اختبار",
                "https://example.com/headphones.jpg", "https://example.com/headphones1.jpg",
                "true", "false", "true", "15.0", "PCS", "0.0", "A3-B3-C3",
                "8", "1600.0", "2024-01-12T16:45:00", "2024-01-07T11:30:00",
                "سماعات بتقنية إلغاء الضوضاء", "2024-01-01T12:00:00", "2024-01-12T17:00:00"
            )
        )
        
        val csvContent = buildString {
            // Add headers
            appendLine(headers.joinToString(","))
            
            // Add products
            products.forEach { product ->
                val escapedValues = product.map { value ->
                    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                        "\"${value.replace("\"", "\"\"")}\"" 
                    } else {
                        value
                    }
                }
                appendLine(escapedValues.joinToString(","))
            }
        }
        
        file.writeText(csvContent)
        println("✅ Generated comprehensive test CSV: ${file.absolutePath}")
        return file
    }
    
    /**
     * Generate a test JSON file
     */
    fun generateTestJson(fileName: String = "test_products.json"): File {
        val file = File(fileName)
        
        val jsonContent = """
[
  {
    "name": "منتج JSON 1",
    "price": 300.0,
    "stockQuantity": 40,
    "description": "منتج من ملف JSON",
    "category": "اختبار",
    "sku": "JSON001",
    "costPrice": 250.0,
    "brand": "علامة تجارية",
    "unitOfMeasure": "PCS",
    "isTaxable": true,
    "taxRate": 15.0
  },
  {
    "name": "منتج JSON 2",
    "price": 450.0,
    "stockQuantity": 20,
    "description": "منتج آخر من ملف JSON",
    "category": "اختبار",
    "sku": "JSON002",
    "costPrice": 350.0,
    "brand": "علامة أخرى",
    "unitOfMeasure": "PCS",
    "isTaxable": true,
    "taxRate": 15.0
  }
]
        """.trimIndent()
        
        file.writeText(jsonContent)
        println("✅ Generated test JSON: ${file.absolutePath}")
        return file
    }
    
    /**
     * Generate test files in the current directory
     */
    fun generateAllTestFiles() {
        try {
            generateSimpleTestCsv()
            generateComprehensiveTestCsv()
            generateTestJson()
            println("🎉 All test files generated successfully!")
        } catch (e: Exception) {
            println("❌ Error generating test files: ${e.message}")
            e.printStackTrace()
        }
    }
}
