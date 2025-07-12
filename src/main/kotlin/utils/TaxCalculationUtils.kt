package utils

import data.preferences.TaxSettings
import kotlin.math.round

/**
 * Utility class for tax calculations with comprehensive support for different tax scenarios
 * Provides consistent tax calculation logic across the application
 */
object TaxCalculationUtils {
    
    private const val CURRENCY_DECIMAL_PLACES = 2
    
    /**
     * Round currency values to 2 decimal places for consistency
     */
    fun roundToCurrency(value: Double): Double {
        return round(value * 100.0) / 100.0
    }
    
    /**
     * Calculate tax amount based on tax settings and base amount
     */
    fun calculateTaxAmount(
        baseAmount: Double,
        taxSettings: TaxSettings,
        discountAmount: Double = 0.0
    ): Double {
        val taxableAmount = if (taxSettings.calculateTaxOnDiscountedAmount) {
            maxOf(0.0, baseAmount - discountAmount)
        } else {
            baseAmount
        }
        return roundToCurrency(taxableAmount * taxSettings.taxRate)
    }
    
    /**
     * Calculate total amount including tax
     */
    fun calculateTotalWithTax(
        baseAmount: Double,
        taxSettings: TaxSettings,
        discountAmount: Double = 0.0
    ): Double {
        val discountedAmount = maxOf(0.0, baseAmount - discountAmount)
        val taxAmount = calculateTaxAmount(baseAmount, taxSettings, discountAmount)
        return roundToCurrency(discountedAmount + taxAmount)
    }
    
    /**
     * Calculate tax-inclusive price from base price
     */
    fun calculateTaxInclusivePrice(basePrice: Double, taxRate: Double): Double {
        return roundToCurrency(basePrice * (1 + taxRate))
    }
    
    /**
     * Calculate base price from tax-inclusive price
     */
    fun calculateBasePriceFromInclusive(inclusivePrice: Double, taxRate: Double): Double {
        return roundToCurrency(inclusivePrice / (1 + taxRate))
    }
    
    /**
     * Get display price based on tax settings
     */
    fun getDisplayPrice(basePrice: Double, taxSettings: TaxSettings): Double {
        return if (taxSettings.displayTaxInclusivePricing) {
            calculateTaxInclusivePrice(basePrice, taxSettings.taxRate)
        } else {
            basePrice
        }
    }
    
    /**
     * Get price label based on tax settings
     */
    fun getPriceLabel(taxSettings: TaxSettings): String {
        return if (taxSettings.displayTaxInclusivePricing) {
            "شامل الضريبة"
        } else {
            "قبل الضريبة"
        }
    }
    
    /**
     * Format tax percentage for display
     */
    fun formatTaxPercentage(taxRate: Double): String {
        return String.format("%.1f%%", taxRate * 100)
    }
    
    /**
     * Validate tax rate
     */
    fun isValidTaxRate(taxRate: Double): Boolean {
        return taxRate >= 0.0 && taxRate <= 1.0
    }
    
    /**
     * Calculate tax breakdown for detailed display
     */
    data class TaxBreakdown(
        val baseAmount: Double,
        val discountAmount: Double,
        val taxableAmount: Double,
        val taxAmount: Double,
        val totalAmount: Double,
        val taxRate: Double,
        val calculationMethod: String
    )
    
    /**
     * Get detailed tax breakdown for display purposes
     */
    fun getTaxBreakdown(
        baseAmount: Double,
        taxSettings: TaxSettings,
        discountAmount: Double = 0.0
    ): TaxBreakdown {
        val taxableAmount = if (taxSettings.calculateTaxOnDiscountedAmount) {
            maxOf(0.0, baseAmount - discountAmount)
        } else {
            baseAmount
        }
        
        val taxAmount = roundToCurrency(taxableAmount * taxSettings.taxRate)
        val totalAmount = roundToCurrency(maxOf(0.0, baseAmount - discountAmount) + taxAmount)
        
        val calculationMethod = if (taxSettings.calculateTaxOnDiscountedAmount) {
            "على المبلغ بعد الخصم"
        } else {
            "على المبلغ الأصلي"
        }
        
        return TaxBreakdown(
            baseAmount = roundToCurrency(baseAmount),
            discountAmount = roundToCurrency(discountAmount),
            taxableAmount = roundToCurrency(taxableAmount),
            taxAmount = taxAmount,
            totalAmount = totalAmount,
            taxRate = taxSettings.taxRate,
            calculationMethod = calculationMethod
        )
    }
    
    /**
     * Calculate tax for multiple items
     */
    fun calculateItemsTax(
        items: List<Pair<Double, Int>>, // (price, quantity) pairs
        taxSettings: TaxSettings,
        totalDiscount: Double = 0.0
    ): Double {
        val totalAmount = items.sumOf { (price, quantity) -> price * quantity }
        return calculateTaxAmount(totalAmount, taxSettings, totalDiscount)
    }
    
    /**
     * Get tax summary for reporting
     */
    data class TaxSummary(
        val totalTaxableAmount: Double,
        val totalTaxAmount: Double,
        val effectiveTaxRate: Double,
        val taxSettings: TaxSettings
    )
    
    /**
     * Generate tax summary for reporting purposes
     */
    fun generateTaxSummary(
        transactions: List<Triple<Double, Double, Double>>, // (base, discount, tax) triples
        taxSettings: TaxSettings
    ): TaxSummary {
        val totalTaxableAmount = transactions.sumOf { (base, discount, _) ->
            if (taxSettings.calculateTaxOnDiscountedAmount) {
                maxOf(0.0, base - discount)
            } else {
                base
            }
        }
        
        val totalTaxAmount = transactions.sumOf { (_, _, tax) -> tax }
        
        val effectiveTaxRate = if (totalTaxableAmount > 0) {
            totalTaxAmount / totalTaxableAmount
        } else {
            0.0
        }
        
        return TaxSummary(
            totalTaxableAmount = roundToCurrency(totalTaxableAmount),
            totalTaxAmount = roundToCurrency(totalTaxAmount),
            effectiveTaxRate = roundToCurrency(effectiveTaxRate),
            taxSettings = taxSettings
        )
    }
    
    /**
     * Convert between different tax rate formats
     */
    object TaxRateConverter {
        fun percentageToDecimal(percentage: Double): Double = percentage / 100.0
        fun decimalToPercentage(decimal: Double): Double = decimal * 100.0
        fun basisPointsToDecimal(basisPoints: Int): Double = basisPoints / 10000.0
        fun decimalToBasisPoints(decimal: Double): Int = (decimal * 10000).toInt()
    }
    
    /**
     * Tax calculation validation
     */
    fun validateTaxCalculation(
        baseAmount: Double,
        discountAmount: Double,
        taxAmount: Double,
        totalAmount: Double,
        taxSettings: TaxSettings
    ): Boolean {
        val expectedTax = calculateTaxAmount(baseAmount, taxSettings, discountAmount)
        val expectedTotal = calculateTotalWithTax(baseAmount, taxSettings, discountAmount)
        
        return kotlin.math.abs(taxAmount - expectedTax) < 0.01 &&
               kotlin.math.abs(totalAmount - expectedTotal) < 0.01
    }
}
