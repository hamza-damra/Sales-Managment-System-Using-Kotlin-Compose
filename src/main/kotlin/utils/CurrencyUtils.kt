package utils

import data.preferences.CurrencyPreferencesManager
import data.preferences.CurrencySettings
import java.text.NumberFormat
import java.util.*

/**
 * Centralized currency utilities for consistent currency formatting across the application
 * Supports RTL text handling and configurable currency settings
 */
object CurrencyUtils {
    
    private val currencyPreferencesManager = CurrencyPreferencesManager()
    
    /**
     * Get a currency formatter based on current user preferences
     */
    fun getCurrencyFormatter(): NumberFormat {
        val settings = currencyPreferencesManager.loadCurrencySettings()
        return createCurrencyFormatter(settings)
    }
    
    /**
     * Create a currency formatter for specific settings
     */
    fun createCurrencyFormatter(settings: CurrencySettings): NumberFormat {
        val locale = parseLocale(settings.locale)
        val formatter = NumberFormat.getCurrencyInstance(locale)
        
        try {
            // Set currency if supported by Java
            val currency = Currency.getInstance(settings.currencyCode)
            formatter.currency = currency
        } catch (e: Exception) {
            println("⚠️ Currency ${settings.currencyCode} not supported by Java, using custom formatting")
        }
        
        // Configure decimal places
        formatter.minimumFractionDigits = settings.decimalPlaces
        formatter.maximumFractionDigits = settings.decimalPlaces
        
        // Configure grouping separator
        formatter.isGroupingUsed = settings.useGroupingSeparator
        
        return formatter
    }
    
    /**
     * Format amount with current currency settings
     */
    fun formatAmount(amount: Double): String {
        val settings = currencyPreferencesManager.loadCurrencySettings()
        return formatAmount(amount, settings)
    }
    
    /**
     * Format amount with specific currency settings
     */
    fun formatAmount(amount: Double, settings: CurrencySettings): String {
        return try {
            val formatter = createCurrencyFormatter(settings)
            val formattedAmount = formatter.format(amount)
            
            // For currencies not supported by Java, apply custom symbol
            if (!isJavaSupportedCurrency(settings.currencyCode)) {
                applyCustomCurrencySymbol(formattedAmount, settings)
            } else {
                formattedAmount
            }
        } catch (e: Exception) {
            println("❌ Error formatting amount: ${e.message}")
            // Fallback to simple formatting
            "${String.format("%.${settings.decimalPlaces}f", amount)} ${settings.currencySymbol}"
        }
    }
    
    /**
     * Format amount for Arabic RTL display
     */
    fun formatAmountRTL(amount: Double): String {
        val settings = currencyPreferencesManager.loadCurrencySettings()
        val formattedAmount = formatAmount(amount, settings)
        
        // For RTL languages, ensure proper text direction
        return if (isRTLCurrency(settings.currencyCode)) {
            "\u202D$formattedAmount\u202C" // LTR override for numbers with RTL context
        } else {
            formattedAmount
        }
    }
    
    /**
     * Get currency symbol for current settings
     */
    fun getCurrencySymbol(): String {
        return currencyPreferencesManager.loadCurrencySettings().currencySymbol
    }
    
    /**
     * Get currency code for current settings
     */
    fun getCurrencyCode(): String {
        return currencyPreferencesManager.loadCurrencySettings().currencyCode
    }
    
    /**
     * Get currency display name for current settings
     */
    fun getCurrencyDisplayName(): String {
        return currencyPreferencesManager.loadCurrencySettings().displayName
    }
    
    /**
     * Check if current currency is RTL
     */
    fun isCurrentCurrencyRTL(): Boolean {
        val settings = currencyPreferencesManager.loadCurrencySettings()
        return isRTLCurrency(settings.currencyCode)
    }
    
    /**
     * Format price for product display (with tax considerations)
     */
    fun formatProductPrice(price: Double, includeTax: Boolean = false, taxRate: Double = 0.0): String {
        val finalPrice = if (includeTax) price * (1 + taxRate) else price
        return formatAmount(finalPrice)
    }
    
    /**
     * Format price range (min - max)
     */
    fun formatPriceRange(minPrice: Double, maxPrice: Double): String {
        val minFormatted = formatAmount(minPrice)
        val maxFormatted = formatAmount(maxPrice)
        return "$minFormatted - $maxFormatted"
    }
    
    /**
     * Parse locale string to Locale object
     */
    private fun parseLocale(localeString: String): Locale {
        return try {
            val parts = localeString.split("_")
            when (parts.size) {
                1 -> Locale(parts[0])
                2 -> Locale(parts[0], parts[1])
                3 -> Locale(parts[0], parts[1], parts[2])
                else -> Locale.getDefault()
            }
        } catch (e: Exception) {
            println("⚠️ Invalid locale format: $localeString, using default")
            Locale.getDefault()
        }
    }
    
    /**
     * Check if currency is supported by Java Currency class
     */
    private fun isJavaSupportedCurrency(currencyCode: String): Boolean {
        return try {
            Currency.getInstance(currencyCode)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Apply custom currency symbol for unsupported currencies
     */
    private fun applyCustomCurrencySymbol(formattedAmount: String, settings: CurrencySettings): String {
        // Remove any existing currency symbols and apply our custom one
        val numberOnly = formattedAmount.replace(Regex("[^\\d.,\\s-]"), "").trim()
        
        return if (settings.showSymbolBeforeAmount) {
            "${settings.currencySymbol} $numberOnly"
        } else {
            "$numberOnly ${settings.currencySymbol}"
        }
    }
    
    /**
     * Check if currency uses RTL text direction
     */
    private fun isRTLCurrency(currencyCode: String): Boolean {
        return when (currencyCode) {
            "SAR", "AED", "JOD", "EGP", "ILS" -> true // Arabic and Hebrew currencies
            else -> false
        }
    }
    
    /**
     * Get sample formatted amount for preview
     */
    fun getSampleFormattedAmount(settings: CurrencySettings, amount: Double = 1234.56): String {
        return formatAmount(amount, settings)
    }
    
    /**
     * Migrate currency formatting from legacy format
     */
    fun migrateLegacyFormat(legacyFormattedAmount: String): String {
        // Convert old SAR format to new ILS format
        val numberOnly = legacyFormattedAmount
            .replace("ر.س", "")
            .replace("SAR", "")
            .replace("ريال", "")
            .trim()
        
        return try {
            val amount = numberOnly.toDouble()
            formatAmount(amount)
        } catch (e: Exception) {
            legacyFormattedAmount // Return original if parsing fails
        }
    }
    
    /**
     * Format amount for export (plain number with currency code)
     */
    fun formatAmountForExport(amount: Double): String {
        val settings = currencyPreferencesManager.loadCurrencySettings()
        return "${String.format("%.${settings.decimalPlaces}f", amount)} ${settings.currencyCode}"
    }
    
    /**
     * Parse amount from formatted string
     */
    fun parseAmount(formattedAmount: String): Double? {
        return try {
            val numberOnly = formattedAmount.replace(Regex("[^\\d.,-]"), "")
                .replace(",", "") // Remove grouping separators
            numberOnly.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
