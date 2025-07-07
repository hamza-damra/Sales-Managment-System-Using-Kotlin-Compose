package services

import com.itextpdf.io.font.FontProgram
import com.itextpdf.io.font.FontProgramFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.BaseDirection
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import data.api.ReturnDTO
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Service for generating professional PDF receipts for return transactions
 * Comprehensive Arabic language support with proper RTL text direction and font handling
 */
object ReturnReceiptService {

    // Arabic locale formatters
    private val arabicLocale = Locale("ar", "SA")
    private val currencyFormatter = NumberFormat.getCurrencyInstance(arabicLocale).apply {
        currency = Currency.getInstance("SAR")
    }
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", arabicLocale)

    /**
     * Initialize Arabic fonts with fallback support
     */
    private fun initializeFonts(): Pair<PdfFont, PdfFont> {
        return try {
            // Try to load Arabic font
            val arabicFontProgram: FontProgram = FontProgramFactory.createFont("fonts/NotoSansArabic-Regular.ttf")
            val arabicFont = PdfFontFactory.createFont(arabicFontProgram, "Identity-H")
            
            // Fallback font
            val fallbackFont = PdfFontFactory.createFont("Helvetica", "Identity-H")
            
            Pair(arabicFont, fallbackFont)
        } catch (e: Exception) {
            println("⚠️ Arabic font not found, using fallback fonts")
            val fallbackFont = PdfFontFactory.createFont("Helvetica", "Identity-H")
            Pair(fallbackFont, fallbackFont)
        }
    }

    /**
     * Create Arabic paragraph with proper RTL support
     */
    private fun createArabicParagraph(
        text: String,
        font: PdfFont,
        fontSize: Float,
        isBold: Boolean = false,
        alignment: TextAlignment = TextAlignment.RIGHT
    ): Paragraph {
        val paragraph = Paragraph(text)
            .setFont(font)
            .setFontSize(fontSize)
            .setTextAlignment(alignment)
            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
        
        if (isBold) {
            paragraph.setBold()
        }
        
        return paragraph
    }

    /**
     * Generate a professional PDF receipt for a return transaction
     */
    fun generateReturnReceipt(
        returnItem: ReturnDTO,
        outputFile: File,
        useArabicIndic: Boolean = false
    ): Boolean {
        return try {
            println("🚀 Starting Return Receipt PDF generation...")

            val pdfWriter = PdfWriter(FileOutputStream(outputFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            // Initialize fonts
            val (arabicFont, fallbackFont) = initializeFonts()
            println("📝 Font initialization completed")

            // Set document properties for better Arabic rendering
            document.setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
            document.setMargins(20f, 20f, 20f, 20f)

            println("📄 Adding document sections...")

            // Company header
            addCompanyHeader(document, arabicFont)
            println("✅ Company header added")

            // Return receipt details
            addReturnReceiptDetails(document, returnItem, arabicFont, useArabicIndic)
            println("✅ Return receipt details added")

            // Customer information
            addCustomerInfo(document, returnItem, arabicFont)
            println("✅ Customer info added")

            // Return items details
            addReturnItemsDetails(document, returnItem, arabicFont, useArabicIndic)
            println("✅ Return items details added")

            // Return summary
            addReturnSummary(document, returnItem, arabicFont, useArabicIndic)
            println("✅ Return summary added")

            // Footer
            addFooter(document, returnItem, arabicFont)
            println("✅ Footer added")

            document.close()
            println("🎉 Return Receipt PDF generation completed successfully!")
            println("📁 File saved to: ${outputFile.absolutePath}")

            true
        } catch (e: Exception) {
            println("❌ Error in Return Receipt PDF generation: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Add company header with Arabic typography
     */
    private fun addCompanyHeader(document: Document, font: PdfFont) {
        document.add(
            createArabicParagraph(
                "نظام إدارة المبيعات",
                font,
                24f,
                true,
                TextAlignment.CENTER
            ).setMarginBottom(5f)
        )

        document.add(
            createArabicParagraph(
                "إيصال إرجاع / استبدال",
                font,
                18f,
                true,
                TextAlignment.CENTER
            ).setMarginBottom(20f)
        )

        // Add separator line
        document.add(
            Paragraph("_".repeat(50))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
        )
    }

    /**
     * Add return receipt details
     */
    private fun addReturnReceiptDetails(
        document: Document,
        returnItem: ReturnDTO,
        font: PdfFont,
        useArabicIndic: Boolean
    ) {
        val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
            .setWidth(UnitValue.createPercentValue(100f))

        // Return ID
        table.addCell(
            Cell().add(createArabicParagraph("رقم الإرجاع:", font, 12f, true))
                .setBorder(Border.NO_BORDER)
        )
        table.addCell(
            Cell().add(createArabicParagraph("${returnItem.id ?: "غير محدد"}", font, 12f))
                .setBorder(Border.NO_BORDER)
        )

        // Return date
        table.addCell(
            Cell().add(createArabicParagraph("تاريخ الإرجاع:", font, 12f, true))
                .setBorder(Border.NO_BORDER)
        )
        table.addCell(
            Cell().add(createArabicParagraph(dateFormatter.format(Date()), font, 12f))
                .setBorder(Border.NO_BORDER)
        )

        // Return status
        table.addCell(
            Cell().add(createArabicParagraph("حالة الإرجاع:", font, 12f, true))
                .setBorder(Border.NO_BORDER)
        )
        val statusText = when (returnItem.status) {
            "PENDING" -> "في الانتظار"
            "APPROVED" -> "موافق عليه"
            "REJECTED" -> "مرفوض"
            "REFUNDED" -> "تم الاسترداد"
            "EXCHANGED" -> "تم الاستبدال"
            else -> returnItem.status ?: "غير محدد"
        }
        table.addCell(
            Cell().add(createArabicParagraph(statusText, font, 12f))
                .setBorder(Border.NO_BORDER)
        )

        // Return reason
        table.addCell(
            Cell().add(createArabicParagraph("سبب الإرجاع:", font, 12f, true))
                .setBorder(Border.NO_BORDER)
        )
        table.addCell(
            Cell().add(createArabicParagraph(returnItem.reason ?: "غير محدد", font, 12f))
                .setBorder(Border.NO_BORDER)
        )

        document.add(table.setMarginBottom(20f))
    }

    /**
     * Add customer information
     */
    private fun addCustomerInfo(document: Document, returnItem: ReturnDTO, font: PdfFont) {
        document.add(
            createArabicParagraph(
                "معلومات العميل",
                font,
                16f,
                true
            ).setMarginBottom(10f)
        )

        val table = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
            .setWidth(UnitValue.createPercentValue(100f))

        // Customer name
        table.addCell(
            Cell().add(createArabicParagraph("اسم العميل:", font, 12f, true))
                .setBorder(Border.NO_BORDER)
        )
        table.addCell(
            Cell().add(createArabicParagraph(returnItem.customerName ?: "غير محدد", font, 12f))
                .setBorder(Border.NO_BORDER)
        )

        // Original sale ID
        table.addCell(
            Cell().add(createArabicParagraph("رقم البيع الأصلي:", font, 12f, true))
                .setBorder(Border.NO_BORDER)
        )
        table.addCell(
            Cell().add(createArabicParagraph("${returnItem.originalSaleId ?: "غير محدد"}", font, 12f))
                .setBorder(Border.NO_BORDER)
        )

        document.add(table.setMarginBottom(20f))
    }

    /**
     * Add return items details
     */
    private fun addReturnItemsDetails(
        document: Document,
        returnItem: ReturnDTO,
        font: PdfFont,
        useArabicIndic: Boolean
    ) {
        document.add(
            createArabicParagraph(
                "تفاصيل المنتجات المرتجعة",
                font,
                16f,
                true
            ).setMarginBottom(10f)
        )

        val table = Table(UnitValue.createPercentArray(floatArrayOf(40f, 20f, 20f, 20f)))
            .setWidth(UnitValue.createPercentValue(100f))

        // Table headers
        table.addHeaderCell(
            Cell().add(createArabicParagraph("المنتج", font, 12f, true, TextAlignment.CENTER))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
        )
        table.addHeaderCell(
            Cell().add(createArabicParagraph("الكمية", font, 12f, true, TextAlignment.CENTER))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
        )
        table.addHeaderCell(
            Cell().add(createArabicParagraph("السعر", font, 12f, true, TextAlignment.CENTER))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
        )
        table.addHeaderCell(
            Cell().add(createArabicParagraph("المجموع", font, 12f, true, TextAlignment.CENTER))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
        )

        // Add return item details
        val items = returnItem.items ?: emptyList()
        if (items.isNotEmpty()) {
            for (item in items) {
                table.addCell(Cell().add(createArabicParagraph(item.productName ?: "منتج مرتجع", font, 11f)))
                table.addCell(Cell().add(createArabicParagraph("${item.returnQuantity}", font, 11f, false, TextAlignment.CENTER)))
                table.addCell(Cell().add(createArabicParagraph(currencyFormatter.format(item.originalUnitPrice), font, 11f, false, TextAlignment.CENTER)))
                table.addCell(Cell().add(createArabicParagraph(currencyFormatter.format(item.refundAmount), font, 11f, false, TextAlignment.CENTER)))
            }
        } else {
            // Fallback if no items data
            table.addCell(Cell().add(createArabicParagraph("منتج مرتجع", font, 11f)))
            table.addCell(Cell().add(createArabicParagraph("1", font, 11f, false, TextAlignment.CENTER)))
            table.addCell(Cell().add(createArabicParagraph(currencyFormatter.format(returnItem.totalRefundAmount), font, 11f, false, TextAlignment.CENTER)))
            table.addCell(Cell().add(createArabicParagraph(currencyFormatter.format(returnItem.totalRefundAmount), font, 11f, false, TextAlignment.CENTER)))
        }

        document.add(table.setMarginBottom(20f))
    }

    /**
     * Add return summary
     */
    private fun addReturnSummary(
        document: Document,
        returnItem: ReturnDTO,
        font: PdfFont,
        useArabicIndic: Boolean
    ) {
        document.add(
            createArabicParagraph(
                "ملخص الإرجاع",
                font,
                16f,
                true
            ).setMarginBottom(10f)
        )

        val table = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .setWidth(UnitValue.createPercentValue(100f))

        // Total refund amount
        table.addCell(
            Cell().add(createArabicParagraph("إجمالي مبلغ الاسترداد:", font, 14f, true))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
        )
        table.addCell(
            Cell().add(createArabicParagraph(currencyFormatter.format(returnItem.totalRefundAmount), font, 14f, true, TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
        )

        document.add(table.setMarginBottom(20f))
    }

    /**
     * Add footer with Arabic typography
     */
    private fun addFooter(document: Document, returnItem: ReturnDTO, font: PdfFont) {
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
                "تم إنشاء هذا الإيصال إلكترونياً",
                font,
                10f,
                false,
                TextAlignment.CENTER
            ).setMarginTop(10f)
        )

        document.add(
            createArabicParagraph(
                "رقم المرجع: ${returnItem.id ?: "غير محدد"}-${System.currentTimeMillis()}",
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
