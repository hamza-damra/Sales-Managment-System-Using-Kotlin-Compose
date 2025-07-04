package utils

import data.*
import data.api.ProductDTO
import data.api.LowStockProductDTO
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Utility class for Excel export functionality
 */
object ExcelExportUtils {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    /**
     * Export inventory overview to Excel
     */
    fun exportInventoryOverview(
        products: List<ProductDTO>,
        lowStockProducts: List<LowStockProductDTO>,
        fileName: String? = null
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            
            // Create styles
            val headerStyle = createHeaderStyle(workbook)
            val dataStyle = createDataStyle(workbook)
            val warningStyle = createWarningStyle(workbook)
            
            // Create Overview Sheet
            createOverviewSheet(workbook, products, headerStyle, dataStyle)
            
            // Create Products Sheet
            createProductsSheet(workbook, products, headerStyle, dataStyle, warningStyle)
            
            // Create Low Stock Sheet
            createLowStockSheet(workbook, lowStockProducts, headerStyle, warningStyle)
            
            // Save file
            val file = selectSaveFile(fileName ?: "inventory_overview_${getCurrentTimestamp()}.xlsx")
            if (file != null) {
                FileOutputStream(file).use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()
                true
            } else {
                workbook.close()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Export products list to Excel
     */
    fun exportProductsList(
        products: List<ProductDTO>,
        fileName: String? = null
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val headerStyle = createHeaderStyle(workbook)
            val dataStyle = createDataStyle(workbook)
            val warningStyle = createWarningStyle(workbook)
            
            createProductsSheet(workbook, products, headerStyle, dataStyle, warningStyle)
            
            val file = selectSaveFile(fileName ?: "products_list_${getCurrentTimestamp()}.xlsx")
            if (file != null) {
                FileOutputStream(file).use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()
                true
            } else {
                workbook.close()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Export stock movements to Excel
     */
    fun exportStockMovements(
        movements: List<StockMovement>,
        productNames: Map<Int, String>,
        fileName: String? = null
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val headerStyle = createHeaderStyle(workbook)
            val dataStyle = createDataStyle(workbook)
            
            val sheet = workbook.createSheet("حركات المخزون")
            
            // Create header
            val headerRow = sheet.createRow(0)
            val headers = listOf(
                "رقم الحركة", "اسم المنتج", "نوع الحركة", "الكمية", 
                "التاريخ", "المرجع", "الملاحظات"
            )
            
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.setCellStyle(headerStyle)
            }
            
            // Add data
            movements.forEachIndexed { index, movement ->
                val row = sheet.createRow(index + 1)

                row.createCell(0).apply {
                    setCellValue(movement.id.toDouble())
                    setCellStyle(dataStyle)
                }

                row.createCell(1).apply {
                    setCellValue(productNames[movement.productId] ?: "غير معروف")
                    setCellStyle(dataStyle)
                }

                row.createCell(2).apply {
                    setCellValue(movement.movementType.displayName)
                    setCellStyle(dataStyle)
                }

                row.createCell(3).apply {
                    setCellValue(movement.quantity.toDouble())
                    setCellStyle(dataStyle)
                }

                row.createCell(4).apply {
                    setCellValue(movement.date.toString())
                    setCellStyle(dataStyle)
                }

                row.createCell(5).apply {
                    setCellValue(movement.reference)
                    setCellStyle(dataStyle)
                }

                row.createCell(6).apply {
                    setCellValue(movement.notes)
                    setCellStyle(dataStyle)
                }
            }
            
            // Auto-size columns
            for (i in 0 until headers.size) {
                sheet.autoSizeColumn(i)
            }
            
            val file = selectSaveFile(fileName ?: "stock_movements_${getCurrentTimestamp()}.xlsx")
            if (file != null) {
                FileOutputStream(file).use { outputStream ->
                    workbook.write(outputStream)
                }
                workbook.close()
                true
            } else {
                workbook.close()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun createOverviewSheet(
        workbook: XSSFWorkbook,
        products: List<ProductDTO>,
        headerStyle: CellStyle,
        dataStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("نظرة عامة")
        
        // Summary statistics
        val totalProducts = products.size
        val totalValue = products.sumOf { (it.price * (it.stockQuantity ?: 0)) }
        val lowStockCount = products.count { (it.stockQuantity ?: 0) <= (it.minStockLevel ?: 10) }
        val outOfStockCount = products.count { (it.stockQuantity ?: 0) == 0 }
        
        val summaryData = listOf(
            "إجمالي المنتجات" to totalProducts.toString(),
            "إجمالي قيمة المخزون" to String.format("%.2f", totalValue),
            "منتجات مخزون منخفض" to lowStockCount.toString(),
            "منتجات نفد مخزونها" to outOfStockCount.toString()
        )
        
        summaryData.forEachIndexed { index, (label, value) ->
            val row = sheet.createRow(index)
            row.createCell(0).apply {
                setCellValue(label)
                setCellStyle(headerStyle)
            }

            row.createCell(1).apply {
                setCellValue(value)
                setCellStyle(dataStyle)
            }
        }
        
        // Auto-size columns
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
    }
    
    private fun createProductsSheet(
        workbook: XSSFWorkbook,
        products: List<ProductDTO>,
        headerStyle: CellStyle,
        dataStyle: CellStyle,
        warningStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("المنتجات")
        
        // Create header
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "الرقم", "اسم المنتج", "الفئة", "السعر", "التكلفة", 
            "المخزون الحالي", "الحد الأدنى", "الباركود", "الحالة"
        )
        
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.setCellStyle(headerStyle)
        }
        
        // Add data
        products.forEachIndexed { index, product ->
            val row = sheet.createRow(index + 1)
            val isLowStock = (product.stockQuantity ?: 0) <= (product.minStockLevel ?: 10)
            val style = if (isLowStock) warningStyle else dataStyle
            
            row.createCell(0).apply {
                setCellValue(product.id?.toDouble() ?: 0.0)
                setCellStyle(style)
            }

            row.createCell(1).apply {
                setCellValue(product.name)
                setCellStyle(style)
            }

            row.createCell(2).apply {
                setCellValue(product.category ?: "")
                setCellStyle(style)
            }

            row.createCell(3).apply {
                setCellValue(product.price)
                setCellStyle(style)
            }

            row.createCell(4).apply {
                setCellValue(product.costPrice ?: 0.0)
                setCellStyle(style)
            }

            row.createCell(5).apply {
                setCellValue((product.stockQuantity ?: 0).toDouble())
                setCellStyle(style)
            }

            row.createCell(6).apply {
                setCellValue((product.minStockLevel ?: 0).toDouble())
                setCellStyle(style)
            }

            row.createCell(7).apply {
                setCellValue(product.barcode ?: "")
                setCellStyle(style)
            }

            row.createCell(8).apply {
                setCellValue(
                    when {
                        (product.stockQuantity ?: 0) == 0 -> "نفد المخزون"
                        isLowStock -> "مخزون منخفض"
                        else -> "متوفر"
                    }
                )
                setCellStyle(style)
            }
        }
        
        // Auto-size columns
        for (i in 0 until headers.size) {
            sheet.autoSizeColumn(i)
        }
    }
    
    private fun createLowStockSheet(
        workbook: XSSFWorkbook,
        lowStockProducts: List<LowStockProductDTO>,
        headerStyle: CellStyle,
        warningStyle: CellStyle
    ) {
        val sheet = workbook.createSheet("مخزون منخفض")
        
        // Create header
        val headerRow = sheet.createRow(0)
        val headers = listOf(
            "رقم المنتج", "اسم المنتج", "الفئة", "المخزون الحالي", 
            "الحد الأدنى", "نقطة إعادة الطلب", "الكمية المطلوبة"
        )
        
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.setCellStyle(headerStyle)
        }
        
        // Add data
        lowStockProducts.forEachIndexed { index, product ->
            val row = sheet.createRow(index + 1)
            val requiredQuantity = product.reorderPoint - product.currentStock

            row.createCell(0).apply {
                setCellValue(product.productId.toDouble())
                setCellStyle(warningStyle)
            }

            row.createCell(1).apply {
                setCellValue(product.productName)
                setCellStyle(warningStyle)
            }

            row.createCell(2).apply {
                setCellValue(product.category ?: "")
                setCellStyle(warningStyle)
            }

            row.createCell(3).apply {
                setCellValue(product.currentStock.toDouble())
                setCellStyle(warningStyle)
            }

            row.createCell(4).apply {
                setCellValue(product.minStockLevel.toDouble())
                setCellStyle(warningStyle)
            }

            row.createCell(5).apply {
                setCellValue(product.reorderPoint.toDouble())
                setCellStyle(warningStyle)
            }

            row.createCell(6).apply {
                setCellValue(maxOf(0, requiredQuantity).toDouble())
                setCellStyle(warningStyle)
            }
        }
        
        // Auto-size columns
        for (i in 0 until headers.size) {
            sheet.autoSizeColumn(i)
        }
    }
    
    private fun createHeaderStyle(workbook: XSSFWorkbook): CellStyle {
        val style = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        font.color = IndexedColors.WHITE.index
        style.setFont(font)
        style.fillForegroundColor = IndexedColors.DARK_BLUE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        return style
    }
    
    private fun createDataStyle(workbook: XSSFWorkbook): CellStyle {
        val style = workbook.createCellStyle()
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        return style
    }
    
    private fun createWarningStyle(workbook: XSSFWorkbook): CellStyle {
        val style = workbook.createCellStyle()
        style.fillForegroundColor = IndexedColors.LIGHT_ORANGE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        return style
    }
    
    private fun selectSaveFile(defaultFileName: String): File? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "حفظ ملف Excel"
        fileChooser.selectedFile = File(defaultFileName)
        fileChooser.fileFilter = FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx")
        
        return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            if (!file.name.endsWith(".xlsx")) {
                File(file.absolutePath + ".xlsx")
            } else {
                file
            }
        } else {
            null
        }
    }
    
    private fun getCurrentTimestamp(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    }
}
