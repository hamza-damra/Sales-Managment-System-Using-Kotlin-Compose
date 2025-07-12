package utils

import data.preferences.TaxSettings
import data.api.SaleDTO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.*
import utils.CurrencyUtils

/**
 * Utility for generating tax reports and analytics
 * Provides comprehensive tax reporting functionality
 */
object TaxReportingUtils {
    
    private val arabicLocale = Locale("ar")
    private val currencyFormatter = CurrencyUtils.getCurrencyFormatter()
    private val percentFormatter = NumberFormat.getPercentInstance(arabicLocale)
    
    /**
     * Tax report data structure
     */
    data class TaxReport(
        val reportPeriod: String,
        val totalSales: Double,
        val totalTaxableAmount: Double,
        val totalTaxCollected: Double,
        val effectiveTaxRate: Double,
        val taxByRate: Map<Double, TaxRateBreakdown>,
        val dailyTaxSummary: List<DailyTaxSummary>,
        val taxSettings: TaxSettings,
        val generatedAt: LocalDateTime = LocalDateTime.now()
    )
    
    data class TaxRateBreakdown(
        val taxRate: Double,
        val salesCount: Int,
        val totalSales: Double,
        val totalTax: Double,
        val percentage: Double
    )
    
    data class DailyTaxSummary(
        val date: String,
        val salesCount: Int,
        val totalSales: Double,
        val totalTax: Double,
        val averageOrderValue: Double
    )
    
    /**
     * Generate comprehensive tax report from sales data
     */
    fun generateTaxReport(
        sales: List<SaleDTO>,
        taxSettings: TaxSettings,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): TaxReport {
        val filteredSales = sales.filter { sale ->
            // Filter sales by date range if needed
            true // Placeholder - implement date filtering based on your date structure
        }
        
        val totalSales = filteredSales.sumOf { it.totalAmount ?: 0.0 }
        val totalTaxCollected = filteredSales.sumOf { it.taxAmount ?: 0.0 }
        val totalTaxableAmount = if (taxSettings.calculateTaxOnDiscountedAmount) {
            filteredSales.sumOf { (it.subtotal ?: 0.0) - (it.promotionDiscountAmount ?: 0.0) }
        } else {
            filteredSales.sumOf { it.subtotal ?: 0.0 }
        }
        
        val effectiveTaxRate = if (totalTaxableAmount > 0) {
            totalTaxCollected / totalTaxableAmount
        } else {
            0.0
        }
        
        // Group by tax rate (assuming all sales use the same rate for now)
        val taxByRate = mapOf(
            taxSettings.taxRate to TaxRateBreakdown(
                taxRate = taxSettings.taxRate,
                salesCount = filteredSales.size,
                totalSales = totalSales,
                totalTax = totalTaxCollected,
                percentage = 100.0
            )
        )
        
        // Generate daily summary (placeholder implementation)
        val dailyTaxSummary = generateDailySummary(filteredSales)
        
        return TaxReport(
            reportPeriod = "${startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))} إلى ${endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}",
            totalSales = totalSales,
            totalTaxableAmount = totalTaxableAmount,
            totalTaxCollected = totalTaxCollected,
            effectiveTaxRate = effectiveTaxRate,
            taxByRate = taxByRate,
            dailyTaxSummary = dailyTaxSummary,
            taxSettings = taxSettings
        )
    }
    
    /**
     * Generate daily tax summary
     */
    private fun generateDailySummary(sales: List<SaleDTO>): List<DailyTaxSummary> {
        // Group sales by date and calculate daily summaries
        // This is a placeholder implementation
        return listOf(
            DailyTaxSummary(
                date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                salesCount = sales.size,
                totalSales = sales.sumOf { it.totalAmount ?: 0.0 },
                totalTax = sales.sumOf { it.taxAmount ?: 0.0 },
                averageOrderValue = if (sales.isNotEmpty()) {
                    sales.sumOf { it.totalAmount ?: 0.0 } / sales.size
                } else {
                    0.0
                }
            )
        )
    }
    
    /**
     * Export tax report to formatted text
     */
    fun exportTaxReportToText(report: TaxReport): String {
        val sb = StringBuilder()
        
        sb.appendLine("=".repeat(60))
        sb.appendLine("تقرير الضريبة المفصل")
        sb.appendLine("=".repeat(60))
        sb.appendLine()
        
        sb.appendLine("فترة التقرير: ${report.reportPeriod}")
        sb.appendLine("تاريخ الإنشاء: ${report.generatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        sb.appendLine()
        
        sb.appendLine("ملخص الضريبة:")
        sb.appendLine("-".repeat(40))
        sb.appendLine("إجمالي المبيعات: ${currencyFormatter.format(report.totalSales)}")
        sb.appendLine("إجمالي المبلغ الخاضع للضريبة: ${currencyFormatter.format(report.totalTaxableAmount)}")
        sb.appendLine("إجمالي الضريبة المحصلة: ${currencyFormatter.format(report.totalTaxCollected)}")
        sb.appendLine("معدل الضريبة الفعلي: ${percentFormatter.format(report.effectiveTaxRate)}")
        sb.appendLine()
        
        sb.appendLine("تفصيل الضريبة حسب المعدل:")
        sb.appendLine("-".repeat(40))
        report.taxByRate.forEach { (rate, breakdown) ->
            sb.appendLine("معدل الضريبة: ${percentFormatter.format(rate)}")
            sb.appendLine("  عدد المبيعات: ${breakdown.salesCount}")
            sb.appendLine("  إجمالي المبيعات: ${currencyFormatter.format(breakdown.totalSales)}")
            sb.appendLine("  إجمالي الضريبة: ${currencyFormatter.format(breakdown.totalTax)}")
            sb.appendLine("  النسبة: ${String.format("%.1f%%", breakdown.percentage)}")
            sb.appendLine()
        }
        
        sb.appendLine("الملخص اليومي:")
        sb.appendLine("-".repeat(40))
        report.dailyTaxSummary.forEach { daily ->
            sb.appendLine("التاريخ: ${daily.date}")
            sb.appendLine("  عدد المبيعات: ${daily.salesCount}")
            sb.appendLine("  إجمالي المبيعات: ${currencyFormatter.format(daily.totalSales)}")
            sb.appendLine("  إجمالي الضريبة: ${currencyFormatter.format(daily.totalTax)}")
            sb.appendLine("  متوسط قيمة الطلب: ${currencyFormatter.format(daily.averageOrderValue)}")
            sb.appendLine()
        }
        
        sb.appendLine("إعدادات الضريبة المستخدمة:")
        sb.appendLine("-".repeat(40))
        sb.appendLine("معدل الضريبة: ${percentFormatter.format(report.taxSettings.taxRate)}")
        sb.appendLine("إظهار تفصيل الضريبة في السلة: ${if (report.taxSettings.showTaxBreakdownInCart) "نعم" else "لا"}")
        sb.appendLine("إظهار الضريبة في الفواتير: ${if (report.taxSettings.showTaxOnReceipts) "نعم" else "لا"}")
        sb.appendLine("عرض الأسعار شاملة الضريبة: ${if (report.taxSettings.displayTaxInclusivePricing) "نعم" else "لا"}")
        sb.appendLine("حساب الضريبة على المبلغ بعد الخصم: ${if (report.taxSettings.calculateTaxOnDiscountedAmount) "نعم" else "لا"}")
        
        sb.appendLine()
        sb.appendLine("=".repeat(60))
        sb.appendLine("نهاية التقرير")
        sb.appendLine("=".repeat(60))
        
        return sb.toString()
    }
    
    /**
     * Generate tax compliance summary
     */
    fun generateTaxComplianceSummary(
        sales: List<SaleDTO>,
        taxSettings: TaxSettings
    ): TaxComplianceSummary {
        val totalTaxCollected = sales.sumOf { it.taxAmount ?: 0.0 }
        val totalSales = sales.sumOf { it.totalAmount ?: 0.0 }
        val taxableTransactions = sales.filter { (it.taxAmount ?: 0.0) > 0 }
        val nonTaxableTransactions = sales.filter { (it.taxAmount ?: 0.0) == 0.0 }
        
        return TaxComplianceSummary(
            totalTransactions = sales.size,
            taxableTransactions = taxableTransactions.size,
            nonTaxableTransactions = nonTaxableTransactions.size,
            totalTaxCollected = totalTaxCollected,
            totalSalesValue = totalSales,
            averageTaxPerTransaction = if (taxableTransactions.isNotEmpty()) {
                totalTaxCollected / taxableTransactions.size
            } else {
                0.0
            },
            complianceRate = if (sales.isNotEmpty()) {
                taxableTransactions.size.toDouble() / sales.size * 100
            } else {
                0.0
            },
            taxSettings = taxSettings
        )
    }
    
    data class TaxComplianceSummary(
        val totalTransactions: Int,
        val taxableTransactions: Int,
        val nonTaxableTransactions: Int,
        val totalTaxCollected: Double,
        val totalSalesValue: Double,
        val averageTaxPerTransaction: Double,
        val complianceRate: Double,
        val taxSettings: TaxSettings
    )
    
    /**
     * Validate tax calculations in sales data
     */
    fun validateTaxCalculations(
        sales: List<SaleDTO>,
        taxSettings: TaxSettings
    ): List<TaxValidationError> {
        val errors = mutableListOf<TaxValidationError>()
        
        sales.forEach { sale ->
            val expectedTax = TaxCalculationUtils.calculateTaxAmount(
                baseAmount = sale.subtotal ?: 0.0,
                taxSettings = taxSettings,
                discountAmount = sale.promotionDiscountAmount ?: 0.0
            )
            
            val actualTax = sale.taxAmount ?: 0.0
            val tolerance = 0.01 // 1 cent tolerance
            
            if (kotlin.math.abs(expectedTax - actualTax) > tolerance) {
                errors.add(
                    TaxValidationError(
                        saleId = sale.id?.toString() ?: "Unknown",
                        expectedTax = expectedTax,
                        actualTax = actualTax,
                        difference = actualTax - expectedTax,
                        description = "عدم تطابق في حساب الضريبة"
                    )
                )
            }
        }
        
        return errors
    }
    
    data class TaxValidationError(
        val saleId: String,
        val expectedTax: Double,
        val actualTax: Double,
        val difference: Double,
        val description: String
    )
}
