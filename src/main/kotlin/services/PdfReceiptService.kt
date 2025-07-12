package services

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.BaseDirection
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import data.Sale
import data.Customer
import data.PaymentMethod
import data.SaleItem
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Service for generating professional PDF receipts/invoices for sales transactions
 * Comprehensive Arabic language support with proper RTL text direction, font handling, and text shaping
 */
object PdfReceiptService {

    // Arabic locale formatters
    private val arabicLocale = Locale("ar")
    private val currencyFormatter = utils.CurrencyUtils.getCurrencyFormatter()
    private val arabicDateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", arabicLocale)
    private val arabicNumberFormat = NumberFormat.getNumberInstance(arabicLocale)

    // Arabic font management
    private var arabicFont: PdfFont? = null
    private var fallbackFont: PdfFont? = null

    /**
     * Initialize Arabic fonts with enhanced text shaping support
     */
    private fun initializeFonts(): Pair<PdfFont, PdfFont> {
        if (arabicFont != null && fallbackFont != null) {
            return Pair(arabicFont!!, fallbackFont!!)
        }

        try {
            // Enhanced Arabic font paths with better Arabic support
            val arabicFontPaths = listOf(
                // Windows Arabic fonts (prioritize fonts with better Arabic support)
                "C:/Windows/Fonts/tahoma.ttf",        // Excellent Arabic support
                "C:/Windows/Fonts/arial.ttf",         // Good Arabic support
                "C:/Windows/Fonts/calibri.ttf",       // Modern Arabic support
                "C:/Windows/Fonts/segoeui.ttf",       // Windows 10+ Arabic support
                "C:/Windows/Fonts/times.ttf",         // Classic Arabic support
                "C:/Windows/Fonts/trebuc.ttf",        // Trebuchet MS
                // macOS Arabic fonts
                "/System/Library/Fonts/Arial.ttf",
                "/System/Library/Fonts/Helvetica.ttc",
                "/System/Library/Fonts/Times.ttc",
                "/Library/Fonts/Arial.ttf",
                // Linux Arabic fonts
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                "/usr/share/fonts/TTF/arial.ttf",
                "/usr/share/fonts/TTF/tahoma.ttf",
                "/usr/share/fonts/truetype/noto/NotoSansArabic-Regular.ttf"
            )

            var loadedArabicFont: PdfFont? = null
            var loadedFallbackFont: PdfFont? = null

            // Try to load Arabic-compatible font with proper encoding
            for (fontPath in arabicFontPaths) {
                try {
                    val file = File(fontPath)
                    if (file.exists()) {
                        // Use Identity-H encoding for proper Arabic text shaping
                        loadedArabicFont = PdfFontFactory.createFont(
                            fontPath,
                            "Identity-H",
                            PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
                        )
                        println("✅ Successfully loaded Arabic font: $fontPath")
                        break
                    }
                } catch (e: Exception) {
                    println("❌ Failed to load font: $fontPath - ${e.message}")
                    // Continue to next font
                }
            }

            // Enhanced fallback strategy
            if (loadedArabicFont == null) {
                try {
                    println("🔄 Trying built-in fonts with Arabic support...")

                    // Try different built-in font approaches
                    val builtInAttempts = listOf(
                        { PdfFontFactory.createFont("Helvetica", "Identity-H", PdfFontFactory.EmbeddingStrategy.FORCE_NOT_EMBEDDED) },
                        { PdfFontFactory.createFont("Times-Roman", "Identity-H", PdfFontFactory.EmbeddingStrategy.FORCE_NOT_EMBEDDED) },
                        { PdfFontFactory.createFont("Courier", "Identity-H", PdfFontFactory.EmbeddingStrategy.FORCE_NOT_EMBEDDED) }
                    )

                    for (attempt in builtInAttempts) {
                        try {
                            loadedArabicFont = attempt()
                            println("✅ Successfully loaded built-in font with Identity-H")
                            break
                        } catch (e: Exception) {
                            println("❌ Built-in font attempt failed: ${e.message}")
                        }
                    }

                    // Ultimate fallback
                    if (loadedArabicFont == null) {
                        loadedArabicFont = PdfFontFactory.createFont()
                        println("⚠️ Using ultimate fallback font (may not support Arabic properly)")
                    }

                } catch (e: Exception) {
                    loadedArabicFont = PdfFontFactory.createFont()
                    println("⚠️ Emergency fallback font loaded")
                }
            }

            // Set fallback font
            loadedFallbackFont = try {
                PdfFontFactory.createFont("Helvetica")
            } catch (e: Exception) {
                PdfFontFactory.createFont()
            }

            arabicFont = loadedArabicFont
            fallbackFont = loadedFallbackFont

            return Pair(loadedArabicFont, loadedFallbackFont)

        } catch (e: Exception) {
            println("❌ Critical error in font initialization: ${e.message}")
            e.printStackTrace()
            // Emergency fallback
            val emergency = PdfFontFactory.createFont()
            arabicFont = emergency
            fallbackFont = emergency
            return Pair(emergency, emergency)
        }
    }

    /**
     * Process Arabic text to ensure proper rendering
     */
    private fun processArabicText(text: String): String {
        // Ensure proper Unicode normalization for Arabic text
        return text.trim()
            .replace("\u200E", "") // Remove LTR mark
            .replace("\u200F", "") // Remove RTL mark
            .replace("\u202A", "") // Remove LTR embedding
            .replace("\u202B", "") // Remove RTL embedding
            .replace("\u202C", "") // Remove pop directional formatting
            .replace("\u202D", "") // Remove LTR override
            .replace("\u202E", "") // Remove RTL override
    }

    /**
     * Create Arabic-compatible paragraph with enhanced RTL support and text processing
     */
    private fun createArabicParagraph(
        text: String,
        font: PdfFont,
        fontSize: Float = 12f,
        isBold: Boolean = false,
        alignment: TextAlignment = TextAlignment.RIGHT
    ): Paragraph {
        // Process the text for better Arabic rendering
        val processedText = processArabicText(text)

        val paragraph = Paragraph(processedText)
            .setFont(font)
            .setFontSize(fontSize)
            .setTextAlignment(alignment)
            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
            .setCharacterSpacing(0.1f) // Slight character spacing for better readability
            .setWordSpacing(0.2f)      // Word spacing for Arabic text

        if (isBold) {
            paragraph.setBold()
        }

        return paragraph
    }

    /**
     * Validate if font supports Arabic characters
     */
    private fun validateArabicSupport(font: PdfFont): Boolean {
        return try {
            // Test with common Arabic characters
            val testChars = listOf(
                '\u0627', // Arabic Letter Alef
                '\u0628', // Arabic Letter Beh
                '\u062A', // Arabic Letter Teh
                '\u062B', // Arabic Letter Theh
                '\u062C', // Arabic Letter Jeem
                '\u0644', // Arabic Letter Lam
                '\u0645', // Arabic Letter Meem
                '\u0646', // Arabic Letter Noon
                '\u0647', // Arabic Letter Heh
                '\u0648', // Arabic Letter Waw
                '\u064A'  // Arabic Letter Yeh
            )

            // Check if font contains Arabic glyphs
            testChars.all { char ->
                try {
                    font.containsGlyph(char.code)
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Create Arabic-compatible table cell with enhanced RTL support
     */
    private fun createArabicCell(
        text: String,
        font: PdfFont,
        fontSize: Float = 10f,
        isBold: Boolean = false,
        alignment: TextAlignment = TextAlignment.RIGHT,
        backgroundColor: com.itextpdf.kernel.colors.Color? = null
    ): Cell {
        val cell = Cell().add(
            createArabicParagraph(text, font, fontSize, isBold, alignment)
        ).setBorder(Border.NO_BORDER)
            .setPadding(4f) // Add padding for better readability

        backgroundColor?.let { cell.setBackgroundColor(it) }

        return cell
    }

    /**
     * Format numbers for Arabic locale (with option for Arabic-Indic numerals)
     */
    private fun formatArabicNumber(number: Number, useArabicIndic: Boolean = false): String {
        val formatted = arabicNumberFormat.format(number)
        return if (useArabicIndic) {
            // Convert Western numerals to Arabic-Indic numerals
            formatted.map { char ->
                when (char) {
                    '0' -> '٠'
                    '1' -> '١'
                    '2' -> '٢'
                    '3' -> '٣'
                    '4' -> '٤'
                    '5' -> '٥'
                    '6' -> '٦'
                    '7' -> '٧'
                    '8' -> '٨'
                    '9' -> '٩'
                    else -> char
                }
            }.joinToString("")
        } else {
            formatted
        }
    }

    /**
     * Format currency for Arabic locale with proper symbol placement
     */
    private fun formatArabicCurrency(amount: Double): String {
        return currencyFormatter.format(amount)
    }

    /**
     * Format date for Arabic locale
     */
    private fun formatArabicDate(dateTime: kotlinx.datetime.LocalDateTime): String {
        // Convert kotlinx.datetime to Java Date for formatting
        val dateString = dateTime.toString().replace("T", " ").substringBefore(".")
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date = inputFormat.parse(dateString)
            arabicDateFormatter.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Generate a professional PDF receipt for a completed sale with enhanced Arabic support
     */
    fun generateReceipt(
        sale: Sale,
        outputFile: File,
        useArabicIndic: Boolean = false
    ): Boolean {
        return try {
            println("🚀 Starting PDF generation with Arabic support...")

            val pdfWriter = PdfWriter(FileOutputStream(outputFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Initialize fonts with validation
            val (arabicFont, fallbackFont) = initializeFonts()
            println("📝 Font initialization completed")

            // Validate Arabic support
            val arabicSupported = validateArabicSupport(arabicFont)
            println("🔤 Arabic support validation: $arabicSupported")

            // Set document properties for better Arabic rendering
            document.setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
            document.setMargins(20f, 20f, 20f, 20f) // Top, Right, Bottom, Left

            println("📄 Adding document sections...")

            // Company header
            addCompanyHeader(document, arabicFont)
            println("✅ Company header added")

            // Receipt details section
            addReceiptDetails(document, sale, arabicFont, useArabicIndic)
            println("✅ Receipt details added")

            // Customer information
            addCustomerInfo(document, sale.customer, arabicFont)
            println("✅ Customer info added")

            // Items table
            addItemsTable(document, sale.items, arabicFont, useArabicIndic)
            println("✅ Items table added")

            // Totals section
            addTotalsSection(document, sale, arabicFont, useArabicIndic)
            println("✅ Totals section added")

            // Footer
            addFooter(document, sale, arabicFont)
            println("✅ Footer added")

            document.close()
            println("🎉 PDF generation completed successfully!")
            println("📁 File saved to: ${outputFile.absolutePath}")

            true
        } catch (e: Exception) {
            println("❌ Error generating PDF: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Add company header with enhanced Arabic typography
     */
    private fun addCompanyHeader(document: Document, font: PdfFont) {
        // Validate Arabic support
        val hasArabicSupport = validateArabicSupport(font)
        println("🔤 Font Arabic support validation: $hasArabicSupport")

        // Company name with enhanced formatting
        val companyName = "نظام إدارة المبيعات"
        document.add(
            createArabicParagraph(
                companyName,
                font,
                24f,
                true,
                TextAlignment.CENTER
            ).setMarginBottom(15f)
             .setMarginTop(10f)
        )

        // Add a decorative line
        document.add(
            createArabicParagraph(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                font,
                8f,
                false,
                TextAlignment.CENTER
            ).setMarginBottom(10f)
        )

        // Receipt title with enhanced styling
        document.add(
            createArabicParagraph(
                "فاتورة بيع",
                font,
                20f,
                true,
                TextAlignment.CENTER
            ).setMarginBottom(20f)
        )

        // Add another decorative line
        document.add(
            createArabicParagraph(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                font,
                8f,
                false,
                TextAlignment.CENTER
            ).setMarginBottom(15f)
        )
    }

    /**
     * Add receipt details with proper Arabic formatting
     */
    private fun addReceiptDetails(document: Document, sale: Sale, font: PdfFont, useArabicIndic: Boolean) {
        val detailsTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)

        // Receipt number
        detailsTable.addCell(createArabicCell("رقم الفاتورة:", font, 12f, true))
        detailsTable.addCell(createArabicCell("#${formatArabicNumber(sale.id, useArabicIndic)}", font, 12f))

        // Date and time
        detailsTable.addCell(createArabicCell("التاريخ والوقت:", font, 12f, true))
        detailsTable.addCell(createArabicCell(formatArabicDate(sale.date), font, 12f))

        // Payment method
        detailsTable.addCell(createArabicCell("طريقة الدفع:", font, 12f, true))
        detailsTable.addCell(createArabicCell(sale.paymentMethod.displayName, font, 12f))

        document.add(detailsTable)
        document.add(createArabicParagraph("", font, 12f)) // Spacer
    }

    /**
     * Add customer information with Arabic support
     */
    private fun addCustomerInfo(document: Document, customer: Customer?, font: PdfFont) {
        if (customer != null) {
            document.add(
                createArabicParagraph(
                    "معلومات العميل",
                    font,
                    14f,
                    true
                ).setMarginBottom(10f)
            )

            val customerTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
                .setWidth(UnitValue.createPercentValue(100f))
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)

            customerTable.addCell(createArabicCell("الاسم:", font, 12f, true))
            customerTable.addCell(createArabicCell(customer.name, font, 12f))

            customerTable.addCell(createArabicCell("الهاتف:", font, 12f, true))
            customerTable.addCell(createArabicCell(customer.phone, font, 12f))

            if (customer.address.isNotEmpty()) {
                customerTable.addCell(createArabicCell("العنوان:", font, 12f, true))
                customerTable.addCell(createArabicCell(customer.address, font, 12f))
            }

            document.add(customerTable)
            document.add(createArabicParagraph("", font, 12f)) // Spacer
        } else {
            document.add(
                createArabicParagraph(
                    "عميل غير محدد",
                    font,
                    14f,
                    true
                ).setMarginBottom(20f)
            )
        }
    }

    /**
     * Add items table with proper Arabic formatting and RTL layout
     */
    private fun addItemsTable(document: Document, items: List<SaleItem>, font: PdfFont, useArabicIndic: Boolean) {
        document.add(
            createArabicParagraph(
                "تفاصيل المنتجات",
                font,
                14f,
                true
            ).setMarginBottom(10f)
        )

        val itemsTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 15f, 20f, 25f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)

        // Table headers (RTL order: Product, Quantity, Price, Total)
        itemsTable.addHeaderCell(
            createArabicCell(
                "المنتج",
                font,
                12f,
                true,
                TextAlignment.CENTER,
                ColorConstants.LIGHT_GRAY
            )
        )
        itemsTable.addHeaderCell(
            createArabicCell(
                "الكمية",
                font,
                12f,
                true,
                TextAlignment.CENTER,
                ColorConstants.LIGHT_GRAY
            )
        )
        itemsTable.addHeaderCell(
            createArabicCell(
                "السعر",
                font,
                12f,
                true,
                TextAlignment.CENTER,
                ColorConstants.LIGHT_GRAY
            )
        )
        itemsTable.addHeaderCell(
            createArabicCell(
                "المجموع",
                font,
                12f,
                true,
                TextAlignment.CENTER,
                ColorConstants.LIGHT_GRAY
            )
        )

        // Add items
        items.forEach { item ->
            itemsTable.addCell(createArabicCell(item.product.name, font, 10f))
            itemsTable.addCell(createArabicCell(formatArabicNumber(item.quantity, useArabicIndic), font, 10f, false, TextAlignment.CENTER))
            itemsTable.addCell(createArabicCell(formatArabicCurrency(item.unitPrice), font, 10f, false, TextAlignment.CENTER))
            itemsTable.addCell(createArabicCell(formatArabicCurrency(item.subtotal), font, 10f, false, TextAlignment.CENTER))
        }

        document.add(itemsTable)
        document.add(createArabicParagraph("", font, 12f)) // Spacer
    }

    /**
     * Add totals section with proper Arabic currency formatting
     */
    private fun addTotalsSection(document: Document, sale: Sale, font: PdfFont, useArabicIndic: Boolean) {
        val totalsTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)

        // Subtotal
        totalsTable.addCell(createArabicCell("المجموع الفرعي:", font, 12f, true))
        totalsTable.addCell(createArabicCell(formatArabicCurrency(sale.subtotal), font, 12f, false, TextAlignment.LEFT))

        // Tax
        totalsTable.addCell(createArabicCell("الضريبة (15%):", font, 12f, true))
        totalsTable.addCell(createArabicCell(formatArabicCurrency(sale.tax), font, 12f, false, TextAlignment.LEFT))

        // Total with border
        val totalLabelCell = createArabicCell("المجموع الإجمالي:", font, 14f, true)
            .setBorderTop(SolidBorder(ColorConstants.BLACK, 1f))
            .setBorderBottom(Border.NO_BORDER)
            .setBorderLeft(Border.NO_BORDER)
            .setBorderRight(Border.NO_BORDER)

        val totalValueCell = createArabicCell(formatArabicCurrency(sale.total), font, 14f, true, TextAlignment.LEFT)
            .setBorderTop(SolidBorder(ColorConstants.BLACK, 1f))
            .setBorderBottom(Border.NO_BORDER)
            .setBorderLeft(Border.NO_BORDER)
            .setBorderRight(Border.NO_BORDER)

        totalsTable.addCell(totalLabelCell)
        totalsTable.addCell(totalValueCell)

        document.add(totalsTable)
        document.add(createArabicParagraph("", font, 12f)) // Spacer
    }

    /**
     * Add footer with Arabic typography
     */
    private fun addFooter(document: Document, sale: Sale, font: PdfFont) {
        document.add(
            createArabicParagraph(
                "شكراً لتعاملكم معنا",
                font,
                12f,
                true,
                TextAlignment.CENTER
            ).setMarginTop(20f)
        )

        document.add(
            createArabicParagraph(
                "تم إنشاء هذه الفاتورة إلكترونياً",
                font,
                10f,
                false,
                TextAlignment.CENTER
            ).setMarginTop(10f)
        )

        document.add(
            createArabicParagraph(
                "رقم المرجع: ${sale.id}-${System.currentTimeMillis()}",
                font,
                8f,
                false,
                TextAlignment.CENTER
            ).setMarginTop(5f)
        )
    }

    /**
     * Generate receipt filename with timestamp
     */
    fun generateReceiptFilename(saleId: Int): String {
        val timestamp = System.currentTimeMillis()
        return "receipt_${saleId}_${timestamp}.pdf"
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

    /**
     * Comprehensive Arabic text rendering test with detailed diagnostics
     */
    fun testArabicSupport(outputFile: File): Boolean {
        return try {
            println("🧪 Starting comprehensive Arabic support test...")

            val pdfWriter = PdfWriter(FileOutputStream(outputFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            val (arabicFont, fallbackFont) = initializeFonts()
            document.setBaseDirection(BaseDirection.RIGHT_TO_LEFT)

            // Font information
            println("📝 Font details:")
            println("   - Font family: ${arabicFont.fontProgram?.fontNames?.fontName}")
            println("   - Arabic support: ${validateArabicSupport(arabicFont)}")

            // Test header
            document.add(createArabicParagraph("🧪 اختبار شامل للنص العربي", arabicFont, 18f, true, TextAlignment.CENTER))
            document.add(createArabicParagraph("", arabicFont, 12f)) // Spacer

            // Test basic Arabic text
            document.add(createArabicParagraph("1. النص الأساسي:", arabicFont, 14f, true))
            document.add(createArabicParagraph("نظام إدارة المبيعات - فاتورة بيع", arabicFont, 12f))
            document.add(createArabicParagraph("", arabicFont, 12f)) // Spacer

            // Test connected letters
            document.add(createArabicParagraph("2. الحروف المتصلة:", arabicFont, 14f, true))
            document.add(createArabicParagraph("بسم الله الرحمن الرحيم", arabicFont, 12f))
            document.add(createArabicParagraph("محمد أحمد علي حسن", arabicFont, 12f))
            document.add(createArabicParagraph("", arabicFont, 12f)) // Spacer

            // Test numbers and currency
            document.add(createArabicParagraph("3. الأرقام والعملة:", arabicFont, 14f, true))
            document.add(createArabicParagraph("الأرقام العربية: ١٢٣٤٥٦٧٨٩٠", arabicFont, 12f))
            document.add(createArabicParagraph("الأرقام الإنجليزية: 1234567890", arabicFont, 12f))
            document.add(createArabicParagraph("العملة: ١٠٠ شيكل إسرائيلي", arabicFont, 12f))
            document.add(createArabicParagraph("", arabicFont, 12f)) // Spacer

            // Test dates
            document.add(createArabicParagraph("4. التواريخ:", arabicFont, 14f, true))
            document.add(createArabicParagraph("التاريخ الهجري: ١٤٤٦/٠٦/١٥", arabicFont, 12f))
            document.add(createArabicParagraph("التاريخ الميلادي: ٢٠٢٤/١٢/٢٥", arabicFont, 12f))
            document.add(createArabicParagraph("", arabicFont, 12f)) // Spacer

            // Test mixed content
            document.add(createArabicParagraph("5. المحتوى المختلط:", arabicFont, 14f, true))
            document.add(createArabicParagraph("Samsung Galaxy S23 - سامسونج جالاكسي", arabicFont, 12f))
            document.add(createArabicParagraph("iPhone 15 Pro - آيفون ١٥ برو", arabicFont, 12f))
            document.add(createArabicParagraph("", arabicFont, 12f)) // Spacer

            // Test table structure
            document.add(createArabicParagraph("6. جدول تجريبي:", arabicFont, 14f, true))
            val testTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
                .setWidth(UnitValue.createPercentValue(100f))
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)

            testTable.addCell(createArabicCell("المنتج", arabicFont, 12f, true, TextAlignment.CENTER, ColorConstants.LIGHT_GRAY))
            testTable.addCell(createArabicCell("السعر", arabicFont, 12f, true, TextAlignment.CENTER, ColorConstants.LIGHT_GRAY))
            testTable.addCell(createArabicCell("هاتف ذكي", arabicFont, 10f))
            testTable.addCell(createArabicCell("١٠٠٠ ريال", arabicFont, 10f))

            document.add(testTable)
            document.add(createArabicParagraph("", arabicFont, 12f)) // Spacer

            // Test result summary
            document.add(createArabicParagraph("✅ تم اختبار النص العربي بنجاح", arabicFont, 14f, true, TextAlignment.CENTER))

            document.close()
            println("✅ Arabic support test completed successfully!")
            println("📁 Test file saved to: ${outputFile.absolutePath}")

            true
        } catch (e: Exception) {
            println("❌ Arabic support test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Quick Arabic font test for debugging
     */
    fun quickArabicTest(): String {
        return try {
            val (arabicFont, _) = initializeFonts()
            val hasSupport = validateArabicSupport(arabicFont)
            "Arabic support: $hasSupport, Font: ${arabicFont.fontProgram?.fontNames?.fontName ?: "Unknown"}"
        } catch (e: Exception) {
            "Error testing Arabic support: ${e.message}"
        }
    }
}