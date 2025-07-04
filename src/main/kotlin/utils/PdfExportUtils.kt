package utils

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import data.*
import data.api.ProductDTO
import data.api.LowStockProductDTO
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Utility class for PDF export functionality
 */
object PdfExportUtils {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val arabicDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    
    /**
     * Export inventory overview to PDF
     */
    fun exportInventoryOverview(
        products: List<ProductDTO>,
        lowStockProducts: List<LowStockProductDTO>,
        fileName: String? = null
    ): Boolean {
        return try {
            val file = selectSaveFile(fileName ?: "inventory_overview_${getCurrentTimestamp()}.pdf")
            if (file != null) {
                val pdfWriter = PdfWriter(FileOutputStream(file))
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)
                
                // Add title
                document.add(
                    Paragraph("تقرير نظرة عامة على المخزون")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(20f)
                        .setBold()
                )
                
                document.add(
                    Paragraph("تاريخ التقرير: ${LocalDateTime.now().format(arabicDateFormatter)}")
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(12f)
                )
                
                // Add summary statistics
                addSummarySection(document, products)
                
                // Add products table
                addProductsTable(document, products)
                
                // Add low stock products if any
                if (lowStockProducts.isNotEmpty()) {
                    addLowStockTable(document, lowStockProducts)
                }
                
                document.close()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Export products list to PDF
     */
    fun exportProductsList(
        products: List<ProductDTO>,
        fileName: String? = null
    ): Boolean {
        return try {
            val file = selectSaveFile(fileName ?: "products_list_${getCurrentTimestamp()}.pdf")
            if (file != null) {
                val pdfWriter = PdfWriter(FileOutputStream(file))
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)
                
                // Add title
                document.add(
                    Paragraph("قائمة المنتجات")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(20f)
                        .setBold()
                )
                
                document.add(
                    Paragraph("تاريخ التقرير: ${LocalDateTime.now().format(arabicDateFormatter)}")
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(12f)
                )
                
                // Add products table
                addProductsTable(document, products)
                
                document.close()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Export stock movements to PDF
     */
    fun exportStockMovements(
        movements: List<StockMovement>,
        productNames: Map<Int, String>,
        fileName: String? = null
    ): Boolean {
        return try {
            val file = selectSaveFile(fileName ?: "stock_movements_${getCurrentTimestamp()}.pdf")
            if (file != null) {
                val pdfWriter = PdfWriter(FileOutputStream(file))
                val pdfDocument = PdfDocument(pdfWriter)
                val document = Document(pdfDocument)
                
                // Add title
                document.add(
                    Paragraph("تقرير حركات المخزون")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(20f)
                        .setBold()
                )
                
                document.add(
                    Paragraph("تاريخ التقرير: ${LocalDateTime.now().format(arabicDateFormatter)}")
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(12f)
                )
                
                // Add movements table
                addStockMovementsTable(document, movements, productNames)
                
                document.close()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun addSummarySection(document: Document, products: List<ProductDTO>) {
        document.add(
            Paragraph("ملخص المخزون")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(16f)
                .setBold()
                .setMarginTop(20f)
        )
        
        val totalProducts = products.size
        val totalValue = products.sumOf { (it.price * (it.stockQuantity ?: 0)) }
        val lowStockCount = products.count { (it.stockQuantity ?: 0) <= (it.minStockLevel ?: 10) }
        val outOfStockCount = products.count { (it.stockQuantity ?: 0) == 0 }
        
        val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        summaryTable.addCell(Cell().add(Paragraph("إجمالي المنتجات")).setTextAlignment(TextAlignment.RIGHT))
        summaryTable.addCell(Cell().add(Paragraph(totalProducts.toString())).setTextAlignment(TextAlignment.CENTER))
        
        summaryTable.addCell(Cell().add(Paragraph("إجمالي قيمة المخزون")).setTextAlignment(TextAlignment.RIGHT))
        summaryTable.addCell(Cell().add(Paragraph(String.format("%.2f", totalValue))).setTextAlignment(TextAlignment.CENTER))
        
        summaryTable.addCell(Cell().add(Paragraph("منتجات مخزون منخفض")).setTextAlignment(TextAlignment.RIGHT))
        summaryTable.addCell(Cell().add(Paragraph(lowStockCount.toString())).setTextAlignment(TextAlignment.CENTER))
        
        summaryTable.addCell(Cell().add(Paragraph("منتجات نفد مخزونها")).setTextAlignment(TextAlignment.RIGHT))
        summaryTable.addCell(Cell().add(Paragraph(outOfStockCount.toString())).setTextAlignment(TextAlignment.CENTER))
        
        document.add(summaryTable)
    }
    
    private fun addProductsTable(document: Document, products: List<ProductDTO>) {
        document.add(
            Paragraph("قائمة المنتجات")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(16f)
                .setBold()
                .setMarginTop(20f)
        )
        
        val table = Table(UnitValue.createPercentArray(floatArrayOf(10f, 25f, 15f, 12f, 12f, 12f, 14f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        // Add headers
        val headers = listOf("الرقم", "اسم المنتج", "الفئة", "السعر", "المخزون", "الحد الأدنى", "الحالة")
        headers.forEach { header ->
            table.addHeaderCell(
                Cell().add(Paragraph(header))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
            )
        }
        
        // Add data
        products.forEach { product ->
            val isLowStock = (product.stockQuantity ?: 0) <= (product.minStockLevel ?: 10)
            val status = when {
                (product.stockQuantity ?: 0) == 0 -> "نفد المخزون"
                isLowStock -> "مخزون منخفض"
                else -> "متوفر"
            }
            
            table.addCell(Cell().add(Paragraph(product.id?.toString() ?: "")).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(product.name)).setTextAlignment(TextAlignment.RIGHT))
            table.addCell(Cell().add(Paragraph(product.category ?: "")).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(String.format("%.2f", product.price))).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph((product.stockQuantity ?: 0).toString())).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph((product.minStockLevel ?: 0).toString())).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(status)).setTextAlignment(TextAlignment.CENTER))
        }
        
        document.add(table)
    }
    
    private fun addLowStockTable(document: Document, lowStockProducts: List<LowStockProductDTO>) {
        document.add(
            Paragraph("المنتجات ذات المخزون المنخفض")
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(16f)
                .setBold()
                .setMarginTop(20f)
        )
        
        val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 30f, 15f, 15f, 15f, 10f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        // Add headers
        val headers = listOf("رقم المنتج", "اسم المنتج", "المخزون الحالي", "الحد الأدنى", "نقطة إعادة الطلب", "مطلوب")
        headers.forEach { header ->
            table.addHeaderCell(
                Cell().add(Paragraph(header))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
            )
        }
        
        // Add data
        lowStockProducts.forEach { product ->
            val requiredQuantity = maxOf(0, product.reorderPoint - product.currentStock)
            
            table.addCell(Cell().add(Paragraph(product.productId.toString())).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(product.productName)).setTextAlignment(TextAlignment.RIGHT))
            table.addCell(Cell().add(Paragraph(product.currentStock.toString())).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(product.minStockLevel.toString())).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(product.reorderPoint.toString())).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(requiredQuantity.toString())).setTextAlignment(TextAlignment.CENTER))
        }
        
        document.add(table)
    }
    
    private fun addStockMovementsTable(
        document: Document, 
        movements: List<StockMovement>,
        productNames: Map<Int, String>
    ) {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(10f, 20f, 15f, 10f, 20f, 15f, 10f)))
            .setWidth(UnitValue.createPercentValue(100f))
        
        // Add headers
        val headers = listOf("الرقم", "المنتج", "نوع الحركة", "الكمية", "التاريخ", "المرجع", "ملاحظات")
        headers.forEach { header ->
            table.addHeaderCell(
                Cell().add(Paragraph(header))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
            )
        }
        
        // Add data
        movements.forEach { movement ->
            table.addCell(Cell().add(Paragraph(movement.id.toString())).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(productNames[movement.productId] ?: "غير معروف")).setTextAlignment(TextAlignment.RIGHT))
            table.addCell(Cell().add(Paragraph(movement.movementType.displayName)).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(movement.quantity.toString())).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(movement.date.toString())).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(movement.reference)).setTextAlignment(TextAlignment.CENTER))
            table.addCell(Cell().add(Paragraph(movement.notes)).setTextAlignment(TextAlignment.RIGHT))
        }
        
        document.add(table)
    }
    
    private fun selectSaveFile(defaultFileName: String): File? {
        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "حفظ ملف PDF"
        fileChooser.selectedFile = File(defaultFileName)
        fileChooser.fileFilter = FileNameExtensionFilter("PDF Files (*.pdf)", "pdf")
        
        return if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            if (!file.name.endsWith(".pdf")) {
                File(file.absolutePath + ".pdf")
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
