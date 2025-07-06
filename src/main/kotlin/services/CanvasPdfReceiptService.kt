package services

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import data.Sale
import data.Customer
import data.SaleItem
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
 * Canvas-based PDF receipt service with enhanced Arabic text rendering
 * Uses Graphics2D canvas approach for proper Arabic text shaping and RTL support
 */
object CanvasPdfReceiptService {
    
    private val arabicLocale = Locale("ar", "SA")
    private val currencyFormatter = NumberFormat.getCurrencyInstance(arabicLocale).apply {
        currency = Currency.getInstance("SAR")
    }
    private val arabicDateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", arabicLocale)
    
    // Canvas dimensions
    private const val PAGE_WIDTH = 595f  // A4 width in points
    private const val PAGE_HEIGHT = 842f // A4 height in points
    private const val MARGIN = 40f
    
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
     * Create canvas image with Arabic text properly rendered
     */
    private fun createReceiptCanvas(sale: Sale): BufferedImage {
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
        
        // Draw receipt details
        yPosition = drawReceiptDetails(g2d, arabicFont, sale, yPosition)
        
        // Draw customer info
        yPosition = drawCustomerInfo(g2d, arabicFont, sale.customer, yPosition)
        
        // Draw items table
        yPosition = drawItemsTable(g2d, arabicFont, sale.items, yPosition)
        
        // Draw totals
        yPosition = drawTotals(g2d, arabicFont, sale, yPosition)
        
        // Draw footer
        drawFooter(g2d, arabicFont, sale, yPosition)
        
        g2d.dispose()
        return image
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
            TextAlignment.CENTER -> x - (textWidth / 2f)
            TextAlignment.LEFT -> x
            TextAlignment.RIGHT -> x - textWidth
        }
        
        if (bidi.isRightToLeft) {
            // Use AttributedString for proper Arabic rendering
            val iterator = attributedString.iterator
            g2d.drawString(iterator, drawX, y)
        } else {
            // Regular text rendering
            g2d.drawString(text, drawX, y)
        }
    }
    
    enum class TextAlignment {
        LEFT, CENTER, RIGHT
    }
    
    /**
     * Draw company header
     */
    private fun drawCompanyHeader(g2d: Graphics2D, baseFont: Font, startY: Float): Float {
        var y = startY
        
        // Company name
        val titleFont = baseFont.deriveFont(Font.BOLD, 24f)
        drawArabicText(
            g2d, 
            "نظام إدارة المبيعات", 
            PAGE_WIDTH / 2f, 
            y, 
            titleFont, 
            Color.BLACK, 
            TextAlignment.CENTER
        )
        y += 40f
        
        // Decorative line
        g2d.color = Color.GRAY
        g2d.drawLine(MARGIN.toInt(), y.toInt(), (PAGE_WIDTH - MARGIN).toInt(), y.toInt())
        y += 20f
        
        // Receipt title
        val subtitleFont = baseFont.deriveFont(Font.BOLD, 18f)
        drawArabicText(
            g2d, 
            "فاتورة بيع", 
            PAGE_WIDTH / 2f, 
            y, 
            subtitleFont, 
            Color.BLACK, 
            TextAlignment.CENTER
        )
        y += 30f
        
        // Another decorative line
        g2d.color = Color.GRAY
        g2d.drawLine(MARGIN.toInt(), y.toInt(), (PAGE_WIDTH - MARGIN).toInt(), y.toInt())
        y += 30f
        
        return y
    }
    
    /**
     * Draw receipt details
     */
    private fun drawReceiptDetails(g2d: Graphics2D, baseFont: Font, sale: Sale, startY: Float): Float {
        var y = startY
        val labelFont = baseFont.deriveFont(Font.BOLD, 12f)
        val valueFont = baseFont.deriveFont(Font.PLAIN, 12f)
        
        // Receipt number
        drawArabicText(g2d, "رقم الفاتورة:", PAGE_WIDTH - MARGIN, y, labelFont)
        drawArabicText(g2d, "#${sale.id}", PAGE_WIDTH - 200f, y, valueFont, Color.BLUE)
        y += 25f
        
        // Date
        drawArabicText(g2d, "التاريخ والوقت:", PAGE_WIDTH - MARGIN, y, labelFont)
        val dateStr = formatArabicDate(sale.date)
        drawArabicText(g2d, dateStr, PAGE_WIDTH - 200f, y, valueFont)
        y += 25f
        
        // Payment method
        drawArabicText(g2d, "طريقة الدفع:", PAGE_WIDTH - MARGIN, y, labelFont)
        drawArabicText(g2d, sale.paymentMethod.displayName, PAGE_WIDTH - 200f, y, valueFont)
        y += 35f
        
        return y
    }
    
    /**
     * Format date for Arabic locale
     */
    private fun formatArabicDate(dateTime: kotlinx.datetime.LocalDateTime): String {
        return try {
            val dateString = dateTime.toString().replace("T", " ").substringBefore(".")
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = inputFormat.parse(dateString)
            arabicDateFormatter.format(date)
        } catch (e: Exception) {
            dateTime.toString().replace("T", " ").substringBefore(".")
        }
    }
    
    /**
     * Draw customer information
     */
    private fun drawCustomerInfo(g2d: Graphics2D, baseFont: Font, customer: Customer?, startY: Float): Float {
        var y = startY
        
        if (customer != null) {
            val headerFont = baseFont.deriveFont(Font.BOLD, 14f)
            val labelFont = baseFont.deriveFont(Font.BOLD, 12f)
            val valueFont = baseFont.deriveFont(Font.PLAIN, 12f)
            
            drawArabicText(g2d, "معلومات العميل", PAGE_WIDTH - MARGIN, y, headerFont)
            y += 25f
            
            drawArabicText(g2d, "الاسم:", PAGE_WIDTH - MARGIN, y, labelFont)
            drawArabicText(g2d, customer.name, PAGE_WIDTH - 200f, y, valueFont)
            y += 20f
            
            drawArabicText(g2d, "الهاتف:", PAGE_WIDTH - MARGIN, y, labelFont)
            drawArabicText(g2d, customer.phone, PAGE_WIDTH - 200f, y, valueFont)
            y += 20f
            
            if (customer.address.isNotEmpty()) {
                drawArabicText(g2d, "العنوان:", PAGE_WIDTH - MARGIN, y, labelFont)
                drawArabicText(g2d, customer.address, PAGE_WIDTH - 200f, y, valueFont)
                y += 20f
            }
        } else {
            val font = baseFont.deriveFont(Font.BOLD, 14f)
            drawArabicText(g2d, "عميل غير محدد", PAGE_WIDTH - MARGIN, y, font)
            y += 25f
        }
        
        y += 20f
        return y
    }

    /**
     * Draw items table
     */
    private fun drawItemsTable(g2d: Graphics2D, baseFont: Font, items: List<SaleItem>, startY: Float): Float {
        var y = startY
        val headerFont = baseFont.deriveFont(Font.BOLD, 14f)
        val tableHeaderFont = baseFont.deriveFont(Font.BOLD, 12f)
        val tableFont = baseFont.deriveFont(Font.PLAIN, 10f)

        // Table title
        drawArabicText(g2d, "تفاصيل المنتجات", PAGE_WIDTH - MARGIN, y, headerFont)
        y += 30f

        // Table header background
        g2d.color = Color.LIGHT_GRAY
        g2d.fillRect(MARGIN.toInt(), (y - 15f).toInt(), (PAGE_WIDTH - 2 * MARGIN).toInt(), 25)

        // Table headers
        val col1X = PAGE_WIDTH - MARGIN
        val col2X = PAGE_WIDTH - 200f
        val col3X = PAGE_WIDTH - 300f
        val col4X = PAGE_WIDTH - 400f

        drawArabicText(g2d, "المنتج", col1X, y, tableHeaderFont, Color.BLACK)
        drawArabicText(g2d, "الكمية", col2X, y, tableHeaderFont, Color.BLACK, TextAlignment.CENTER)
        drawArabicText(g2d, "السعر", col3X, y, tableHeaderFont, Color.BLACK, TextAlignment.CENTER)
        drawArabicText(g2d, "المجموع", col4X, y, tableHeaderFont, Color.BLACK, TextAlignment.CENTER)
        y += 30f

        // Table rows
        items.forEach { item ->
            drawArabicText(g2d, item.product.name, col1X, y, tableFont)
            drawArabicText(g2d, item.quantity.toString(), col2X, y, tableFont, Color.BLACK, TextAlignment.CENTER)
            drawArabicText(g2d, currencyFormatter.format(item.unitPrice), col3X, y, tableFont, Color.BLACK, TextAlignment.CENTER)
            drawArabicText(g2d, currencyFormatter.format(item.subtotal), col4X, y, tableFont, Color.BLACK, TextAlignment.CENTER)
            y += 25f
        }

        y += 20f
        return y
    }

    /**
     * Draw totals section
     */
    private fun drawTotals(g2d: Graphics2D, baseFont: Font, sale: Sale, startY: Float): Float {
        var y = startY
        val labelFont = baseFont.deriveFont(Font.BOLD, 12f)
        val valueFont = baseFont.deriveFont(Font.PLAIN, 12f)
        val totalFont = baseFont.deriveFont(Font.BOLD, 14f)

        // Subtotal
        drawArabicText(g2d, "المجموع الفرعي:", PAGE_WIDTH - MARGIN, y, labelFont)
        drawArabicText(g2d, currencyFormatter.format(sale.subtotal), PAGE_WIDTH - 200f, y, valueFont)
        y += 25f

        // Tax
        drawArabicText(g2d, "الضريبة (15%):", PAGE_WIDTH - MARGIN, y, labelFont)
        drawArabicText(g2d, currencyFormatter.format(sale.tax), PAGE_WIDTH - 200f, y, valueFont)
        y += 25f

        // Line above total
        g2d.color = Color.BLACK
        g2d.drawLine((PAGE_WIDTH - 300f).toInt(), y.toInt(), (PAGE_WIDTH - MARGIN).toInt(), y.toInt())
        y += 15f

        // Total
        drawArabicText(g2d, "المجموع الإجمالي:", PAGE_WIDTH - MARGIN, y, totalFont)
        drawArabicText(g2d, currencyFormatter.format(sale.total), PAGE_WIDTH - 200f, y, totalFont, Color.BLUE)
        y += 40f

        return y
    }

    /**
     * Draw footer
     */
    private fun drawFooter(g2d: Graphics2D, baseFont: Font, sale: Sale, startY: Float) {
        var y = startY
        val footerFont = baseFont.deriveFont(Font.BOLD, 12f)
        val smallFont = baseFont.deriveFont(Font.PLAIN, 10f)
        val tinyFont = baseFont.deriveFont(Font.PLAIN, 8f)

        // Thank you message
        drawArabicText(
            g2d,
            "شكراً لتعاملكم معنا",
            PAGE_WIDTH / 2f,
            y,
            footerFont,
            Color.BLACK,
            TextAlignment.CENTER
        )
        y += 30f

        // Electronic receipt note
        drawArabicText(
            g2d,
            "تم إنشاء هذه الفاتورة إلكترونياً",
            PAGE_WIDTH / 2f,
            y,
            smallFont,
            Color.GRAY,
            TextAlignment.CENTER
        )
        y += 20f

        // Reference number
        drawArabicText(
            g2d,
            "رقم المرجع: ${sale.id}-${System.currentTimeMillis()}",
            PAGE_WIDTH / 2f,
            y,
            tinyFont,
            Color.GRAY,
            TextAlignment.CENTER
        )
    }

    /**
     * Generate PDF receipt using canvas approach
     */
    fun generateReceipt(
        sale: Sale,
        outputFile: File,
        useArabicIndic: Boolean = false
    ): Boolean {
        return try {
            println("🎨 Starting Canvas-based PDF generation with enhanced Arabic support...")

            // Create canvas image with proper Arabic text
            val canvasImage = createReceiptCanvas(sale)
            println("✅ Canvas image created successfully")

            // Create PDF document
            val pdfWriter = PdfWriter(FileOutputStream(outputFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val page = pdfDocument.addNewPage(PageSize.A4)
            val pdfCanvas = PdfCanvas(page)

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
            println("🎉 Canvas-based PDF generation completed successfully!")
            println("📁 File saved to: ${outputFile.absolutePath}")

            true
        } catch (e: Exception) {
            println("❌ Error in canvas-based PDF generation: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Test Arabic rendering with canvas approach
     */
    fun testArabicCanvas(outputFile: File): Boolean {
        return try {
            println("🧪 Testing Canvas-based Arabic rendering...")

            val image = BufferedImage(600, 800, BufferedImage.TYPE_INT_RGB)
            val g2d = image.createGraphics()

            // Enable high-quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

            // Set background
            g2d.color = Color.WHITE
            g2d.fillRect(0, 0, 600, 800)

            val arabicFont = loadArabicFont()
            var y = 50f

            // Test various Arabic texts
            val titleFont = arabicFont.deriveFont(Font.BOLD, 18f)
            drawArabicText(g2d, "🧪 اختبار النص العربي بالكانفاس", 300f, y, titleFont, Color.BLACK, TextAlignment.CENTER)
            y += 50f

            val testFont = arabicFont.deriveFont(Font.PLAIN, 14f)
            drawArabicText(g2d, "نظام إدارة المبيعات", 550f, y, testFont)
            y += 30f

            drawArabicText(g2d, "فاتورة بيع", 550f, y, testFont)
            y += 30f

            drawArabicText(g2d, "بسم الله الرحمن الرحيم", 550f, y, testFont)
            y += 30f

            drawArabicText(g2d, "محمد أحمد علي حسن", 550f, y, testFont)
            y += 30f

            drawArabicText(g2d, "Samsung Galaxy S23 - سامسونج", 550f, y, testFont)
            y += 30f

            g2d.dispose()

            // Save as PDF
            val pdfWriter = PdfWriter(FileOutputStream(outputFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val page = pdfDocument.addNewPage(PageSize.A4)

            val baos = ByteArrayOutputStream()
            ImageIO.write(image, "PNG", baos)
            val imageData = baos.toByteArray()

            val pdfImage = com.itextpdf.io.image.ImageDataFactory.create(imageData)
            val pdfImageElement = com.itextpdf.layout.element.Image(pdfImage)
            pdfImageElement.scaleToFit(PageSize.A4.width, PageSize.A4.height)

            val document = com.itextpdf.layout.Document(pdfDocument)
            document.add(pdfImageElement)
            document.close()

            println("✅ Canvas Arabic test completed!")
            true
        } catch (e: Exception) {
            println("❌ Canvas Arabic test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Generate receipt filename with timestamp
     */
    fun generateReceiptFilename(saleId: Int): String {
        val timestamp = System.currentTimeMillis()
        return "canvas_receipt_${saleId}_${timestamp}.pdf"
    }

    /**
     * Get default receipts directory
     */
    fun getReceiptsDirectory(): File {
        val userHome = System.getProperty("user.home")
        val receiptsDir = File(userHome, "Documents/Sales_Receipts")
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs()
        }
        return receiptsDir
    }
}
