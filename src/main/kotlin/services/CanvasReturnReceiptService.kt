package services

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.geom.PageSize
import data.api.ReturnDTO
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.font.TextAttribute
import java.text.AttributedString
import java.text.Bidi
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream

/**
 * Canvas-based PDF return receipt service with enhanced Arabic text rendering
 * Uses Graphics2D canvas approach for proper Arabic text shaping and RTL support
 */
object CanvasReturnReceiptService {

    private val currencyFormatter = utils.CurrencyUtils.getCurrencyFormatter()
    private val arabicDateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar"))
    
    // Canvas dimensions
    private const val PAGE_WIDTH = 595f  // A4 width in points
    private const val PAGE_HEIGHT = 842f // A4 height in points
    private const val MARGIN = 40f
    
    // Text alignment enum
    enum class TextAlignment {
        LEFT, CENTER, RIGHT
    }
    
    /**
     * Load Arabic-compatible system font
     */
    private fun loadArabicFont(): Font {
        val fontPaths = listOf(
            "C:/Windows/Fonts/tahoma.ttf",
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/calibri.ttf",
            "C:/Windows/Fonts/segoeui.ttf"
        )
        
        for (fontPath in fontPaths) {
            try {
                val fontFile = File(fontPath)
                if (fontFile.exists()) {
                    val font = Font.createFont(Font.TRUETYPE_FONT, fontFile)
                    println("✅ Loaded Arabic font: $fontPath")
                    return font.deriveFont(Font.PLAIN, 12f)
                }
            } catch (e: Exception) {
                println("❌ Failed to load font: $fontPath - ${e.message}")
            }
        }
        
        // Fallback to system fonts
        val systemFonts = listOf("Tahoma", "Arial", "Segoe UI", "Dialog")
        for (fontName in systemFonts) {
            try {
                val font = Font(fontName, Font.PLAIN, 12)
                if (font.canDisplayUpTo("نظام إدارة المبيعات") == -1) {
                    println("✅ Using system font: $fontName")
                    return font
                }
            } catch (e: Exception) {
                println("❌ System font failed: $fontName")
            }
        }
        
        println("⚠️ Using fallback font")
        return Font(Font.SANS_SERIF, Font.PLAIN, 12)
    }
    
    /**
     * Draw Arabic text with proper RTL support using AttributedString
     */
    private fun drawArabicText(
        g2d: Graphics2D, 
        text: String, 
        x: Float, 
        y: Float, 
        font: Font, 
        color: Color = Color.BLACK,
        alignment: TextAlignment = TextAlignment.RIGHT
    ) {
        g2d.color = color
        g2d.font = font
        
        // Create AttributedString for proper text shaping
        val attributedString = AttributedString(text)
        attributedString.addAttribute(TextAttribute.FONT, font)
        attributedString.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL)
        
        // Check if text contains Arabic characters
        val bidi = Bidi(text, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT)
        
        val fontMetrics = g2d.fontMetrics
        val textWidth = fontMetrics.stringWidth(text)
        
        val drawX = when (alignment) {
            TextAlignment.RIGHT -> x - textWidth
            TextAlignment.CENTER -> x - (textWidth / 2)
            TextAlignment.LEFT -> x
        }
        
        try {
            if (bidi.isRightToLeft) {
                // Use AttributedString for proper Arabic text shaping
                g2d.drawString(attributedString.iterator, drawX, y)
            } else {
                // Regular text drawing for non-Arabic text
                g2d.drawString(text, drawX, y)
            }
        } catch (e: Exception) {
            // Fallback to simple text drawing
            g2d.drawString(text, drawX, y)
        }
    }
    
    /**
     * Create canvas image with return receipt content
     */
    private fun createReturnReceiptCanvas(returnItem: ReturnDTO): BufferedImage {
        val image = BufferedImage(
            PAGE_WIDTH.toInt(), 
            PAGE_HEIGHT.toInt(), 
            BufferedImage.TYPE_INT_RGB
        )
        val g2d = image.createGraphics()
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        
        // Set background
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt())
        
        // Load Arabic font
        val arabicFont = loadArabicFont()
        
        var yPosition = MARGIN + 30f
        
        // Draw company header
        yPosition = drawCompanyHeader(g2d, arabicFont, yPosition)
        
        // Draw return receipt details
        yPosition = drawReturnDetails(g2d, arabicFont, returnItem, yPosition)
        
        // Draw customer info
        yPosition = drawCustomerInfo(g2d, arabicFont, returnItem, yPosition)
        
        // Draw return items
        yPosition = drawReturnItems(g2d, arabicFont, returnItem, yPosition)
        
        // Draw totals
        yPosition = drawReturnTotals(g2d, arabicFont, returnItem, yPosition)
        
        // Draw footer
        drawReturnFooter(g2d, arabicFont, returnItem, yPosition)
        
        g2d.dispose()
        return image
    }
    
    /**
     * Draw company header
     */
    private fun drawCompanyHeader(g2d: Graphics2D, baseFont: Font, startY: Float): Float {
        var y = startY
        val titleFont = baseFont.deriveFont(Font.BOLD, 18f)
        val subtitleFont = baseFont.deriveFont(Font.PLAIN, 14f)
        
        // Company name
        drawArabicText(g2d, "نظام إدارة المبيعات", PAGE_WIDTH - MARGIN, y, titleFont, Color.BLUE, TextAlignment.RIGHT)
        y += 30f
        
        // Return receipt title
        drawArabicText(g2d, "إيصال إرجاع", PAGE_WIDTH - MARGIN, y, titleFont, Color.RED, TextAlignment.RIGHT)
        y += 25f
        
        // Separator line
        g2d.color = Color.GRAY
        g2d.drawLine(MARGIN.toInt(), y.toInt(), (PAGE_WIDTH - MARGIN).toInt(), y.toInt())
        y += 20f
        
        return y
    }
    
    /**
     * Draw return details
     */
    private fun drawReturnDetails(g2d: Graphics2D, baseFont: Font, returnItem: ReturnDTO, startY: Float): Float {
        var y = startY
        val labelFont = baseFont.deriveFont(Font.BOLD, 12f)
        val valueFont = baseFont.deriveFont(Font.PLAIN, 12f)
        
        // Return ID
        drawArabicText(g2d, "رقم الإرجاع:", PAGE_WIDTH - MARGIN, y, labelFont)
        drawArabicText(g2d, "#${returnItem.id}", PAGE_WIDTH - 200f, y, valueFont, Color.BLUE)
        y += 25f
        
        // Return date
        drawArabicText(g2d, "تاريخ الإرجاع:", PAGE_WIDTH - MARGIN, y, labelFont)
        val dateStr = returnItem.returnDate?.let { arabicDateFormatter.format(Date()) } ?: "غير محدد"
        drawArabicText(g2d, dateStr, PAGE_WIDTH - 200f, y, valueFont)
        y += 25f
        
        // Status
        drawArabicText(g2d, "الحالة:", PAGE_WIDTH - MARGIN, y, labelFont)
        val statusText = when (returnItem.status) {
            "PENDING" -> "في الانتظار"
            "APPROVED" -> "موافق عليه"
            "REJECTED" -> "مرفوض"
            "PROCESSED" -> "تم المعالجة"
            else -> returnItem.status ?: "غير محدد"
        }
        val statusColor = when (returnItem.status) {
            "APPROVED", "PROCESSED" -> Color.GREEN
            "REJECTED" -> Color.RED
            else -> Color.ORANGE
        }
        drawArabicText(g2d, statusText, PAGE_WIDTH - 200f, y, valueFont, statusColor)
        y += 30f
        
        return y
    }
    
    /**
     * Draw customer information
     */
    private fun drawCustomerInfo(g2d: Graphics2D, baseFont: Font, returnItem: ReturnDTO, startY: Float): Float {
        var y = startY
        val labelFont = baseFont.deriveFont(Font.BOLD, 12f)
        val valueFont = baseFont.deriveFont(Font.PLAIN, 12f)
        
        // Section title
        drawArabicText(g2d, "معلومات العميل", PAGE_WIDTH - MARGIN, y, labelFont, Color.DARK_GRAY)
        y += 25f
        
        // Customer name
        drawArabicText(g2d, "اسم العميل:", PAGE_WIDTH - MARGIN, y, labelFont)
        drawArabicText(g2d, returnItem.customerName ?: "غير محدد", PAGE_WIDTH - 200f, y, valueFont)
        y += 20f
        
        // Original sale ID
        drawArabicText(g2d, "رقم البيع الأصلي:", PAGE_WIDTH - MARGIN, y, labelFont)
        drawArabicText(g2d, "#${returnItem.originalSaleId}", PAGE_WIDTH - 200f, y, valueFont, Color.BLUE)
        y += 30f
        
        return y
    }
    
    /**
     * Draw return items
     */
    private fun drawReturnItems(g2d: Graphics2D, baseFont: Font, returnItem: ReturnDTO, startY: Float): Float {
        var y = startY
        val headerFont = baseFont.deriveFont(Font.BOLD, 12f)
        val itemFont = baseFont.deriveFont(Font.PLAIN, 11f)

        // Section title
        drawArabicText(g2d, "تفاصيل المنتجات المرتجعة", PAGE_WIDTH - MARGIN, y, headerFont, Color.DARK_GRAY)
        y += 25f

        // Draw return items if available
        val items = returnItem.items
        if (!items.isNullOrEmpty()) {
            for ((index, item) in items.withIndex()) {
                if (index > 0) {
                    // Add separator between items
                    g2d.color = Color.LIGHT_GRAY
                    g2d.drawLine((MARGIN + 20f).toInt(), y.toInt(), (PAGE_WIDTH - MARGIN - 20f).toInt(), y.toInt())
                    y += 15f
                }

                // Product name
                drawArabicText(g2d, "المنتج:", PAGE_WIDTH - MARGIN, y, headerFont)
                drawArabicText(g2d, item.productName ?: "غير محدد", PAGE_WIDTH - 200f, y, itemFont)
                y += 20f

                // Quantity
                drawArabicText(g2d, "الكمية:", PAGE_WIDTH - MARGIN, y, headerFont)
                drawArabicText(g2d, "${item.returnQuantity}", PAGE_WIDTH - 200f, y, itemFont)
                y += 20f

                // Unit price
                drawArabicText(g2d, "السعر الأصلي:", PAGE_WIDTH - MARGIN, y, headerFont)
                val unitPriceFormatted = currencyFormatter.format(item.originalUnitPrice)
                drawArabicText(g2d, unitPriceFormatted, PAGE_WIDTH - 200f, y, itemFont)
                y += 20f

                // Item refund amount
                drawArabicText(g2d, "مبلغ الاسترداد:", PAGE_WIDTH - MARGIN, y, headerFont)
                val itemRefundFormatted = currencyFormatter.format(item.refundAmount)
                drawArabicText(g2d, itemRefundFormatted, PAGE_WIDTH - 200f, y, itemFont, Color.GREEN)
                y += 20f

                // Item condition
                drawArabicText(g2d, "حالة المنتج:", PAGE_WIDTH - MARGIN, y, headerFont)
                val conditionText = when (item.itemCondition) {
                    "NEW" -> "جديد"
                    "LIKE_NEW" -> "كالجديد"
                    "GOOD" -> "جيد"
                    "FAIR" -> "مقبول"
                    "POOR" -> "ضعيف"
                    "DAMAGED" -> "تالف"
                    "DEFECTIVE" -> "معيب"
                    else -> item.itemCondition
                }
                drawArabicText(g2d, conditionText, PAGE_WIDTH - 200f, y, itemFont)
                y += 25f
            }
        } else {
            // No items available
            drawArabicText(g2d, "لا توجد تفاصيل منتجات متاحة", PAGE_WIDTH - MARGIN, y, itemFont, Color.GRAY)
            y += 20f
        }

        // Reason
        drawArabicText(g2d, "سبب الإرجاع:", PAGE_WIDTH - MARGIN, y, headerFont)
        drawArabicText(g2d, returnItem.reason ?: "غير محدد", PAGE_WIDTH - 200f, y, itemFont)
        y += 30f

        return y
    }
    
    /**
     * Draw return totals
     */
    private fun drawReturnTotals(g2d: Graphics2D, baseFont: Font, returnItem: ReturnDTO, startY: Float): Float {
        var y = startY
        val labelFont = baseFont.deriveFont(Font.BOLD, 14f)
        val valueFont = baseFont.deriveFont(Font.BOLD, 14f)

        // Separator line
        g2d.color = Color.GRAY
        g2d.drawLine(MARGIN.toInt(), y.toInt(), (PAGE_WIDTH - MARGIN).toInt(), y.toInt())
        y += 20f

        // Total refund amount
        drawArabicText(g2d, "إجمالي مبلغ الاسترداد:", PAGE_WIDTH - MARGIN, y, labelFont)
        val totalRefundAmount = currencyFormatter.format(returnItem.totalRefundAmount)
        drawArabicText(g2d, totalRefundAmount, PAGE_WIDTH - 200f, y, valueFont, Color.GREEN)
        y += 30f

        return y
    }
    
    /**
     * Draw footer
     */
    private fun drawReturnFooter(g2d: Graphics2D, baseFont: Font, returnItem: ReturnDTO, startY: Float) {
        val footerFont = baseFont.deriveFont(Font.PLAIN, 10f)
        val y = PAGE_HEIGHT - MARGIN - 40f
        
        // Footer text
        drawArabicText(g2d, "شكراً لتعاملكم معنا", PAGE_WIDTH / 2, y, footerFont, Color.GRAY, TextAlignment.CENTER)
        
        // Generation timestamp
        val timestamp = "تم الإنشاء: ${arabicDateFormatter.format(Date())}"
        drawArabicText(g2d, timestamp, PAGE_WIDTH - MARGIN, y + 15f, footerFont, Color.LIGHT_GRAY, TextAlignment.RIGHT)
    }
    
    /**
     * Generate return receipt PDF using canvas approach
     */
    fun generateReturnReceipt(
        returnItem: ReturnDTO,
        outputFile: File,
        useArabicIndic: Boolean = false
    ): Boolean {
        return try {
            println("🎨 Starting Canvas-based Return Receipt PDF generation...")

            // Create canvas image with proper Arabic text
            val canvasImage = createReturnReceiptCanvas(returnItem)
            println("✅ Canvas image created successfully")

            // Create PDF document
            val pdfWriter = PdfWriter(FileOutputStream(outputFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val page = pdfDocument.addNewPage(PageSize.A4)

            // Convert BufferedImage to PDF
            val baos = ByteArrayOutputStream()
            ImageIO.write(canvasImage, "PNG", baos)
            val imageData = baos.toByteArray()

            // Create iText image from canvas
            val pdfImage = com.itextpdf.io.image.ImageDataFactory.create(imageData)
            val image = com.itextpdf.layout.element.Image(pdfImage)

            // Scale image to fit page
            image.scaleToFit(PageSize.A4.width, PageSize.A4.height)
            image.setFixedPosition(0f, 0f)

            // Add image to PDF
            val document = com.itextpdf.layout.Document(pdfDocument)
            document.add(image)

            document.close()
            println("🎉 Canvas-based Return Receipt PDF generation completed successfully!")
            println("📁 File saved to: ${outputFile.absolutePath}")

            true
        } catch (e: Exception) {
            println("❌ Error in canvas-based return receipt PDF generation: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Generate return receipt filename with timestamp
     */
    fun generateReturnReceiptFilename(returnId: Int): String {
        val timestamp = System.currentTimeMillis()
        return "return_receipt_${returnId}_${timestamp}.pdf"
    }

    /**
     * Get default receipts directory
     */
    fun getReceiptsDirectory(): File {
        val userHome = System.getProperty("user.home")
        val receiptsDir = File(userHome, "Documents/Return_Receipts")
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs()
        }
        return receiptsDir
    }
}
