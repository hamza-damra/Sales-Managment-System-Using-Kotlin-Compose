package data.preferences

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.prefs.Preferences

/**
 * Data class representing tax configuration settings
 */
data class TaxSettings(
    val taxRate: Double = 0.15, // Default 15%
    val showTaxBreakdownInCart: Boolean = true,
    val showTaxOnReceipts: Boolean = true,
    val displayTaxInclusivePricing: Boolean = false,
    val calculateTaxOnDiscountedAmount: Boolean = true // true = on discounted, false = on original
)

/**
 * Manages tax preferences with persistent storage using Java Preferences API
 * Follows the same pattern as ThemePreferencesManager for consistency
 */
class TaxPreferencesManager {
    private val prefs = Preferences.userNodeForPackage(TaxPreferencesManager::class.java)
    private val mutex = Mutex()
    
    companion object {
        private const val TAX_RATE_KEY = "tax_rate"
        private const val SHOW_TAX_BREAKDOWN_KEY = "show_tax_breakdown_in_cart"
        private const val SHOW_TAX_ON_RECEIPTS_KEY = "show_tax_on_receipts"
        private const val DISPLAY_TAX_INCLUSIVE_KEY = "display_tax_inclusive_pricing"
        private const val CALCULATE_TAX_ON_DISCOUNTED_KEY = "calculate_tax_on_discounted_amount"
        
        // Default values
        private const val DEFAULT_TAX_RATE = 0.15
        private const val DEFAULT_SHOW_TAX_BREAKDOWN = true
        private const val DEFAULT_SHOW_TAX_ON_RECEIPTS = true
        private const val DEFAULT_DISPLAY_TAX_INCLUSIVE = false
        private const val DEFAULT_CALCULATE_TAX_ON_DISCOUNTED = true
    }
    
    private var _currentSettings: TaxSettings? = null
    
    init {
        loadSettingsFromStorage()
    }
    
    /**
     * Save tax settings to persistent storage
     */
    suspend fun saveTaxSettings(settings: TaxSettings) = mutex.withLock {
        try {
            _currentSettings = settings
            prefs.putDouble(TAX_RATE_KEY, settings.taxRate)
            prefs.putBoolean(SHOW_TAX_BREAKDOWN_KEY, settings.showTaxBreakdownInCart)
            prefs.putBoolean(SHOW_TAX_ON_RECEIPTS_KEY, settings.showTaxOnReceipts)
            prefs.putBoolean(DISPLAY_TAX_INCLUSIVE_KEY, settings.displayTaxInclusivePricing)
            prefs.putBoolean(CALCULATE_TAX_ON_DISCOUNTED_KEY, settings.calculateTaxOnDiscountedAmount)
            println("✅ TaxPreferencesManager - Tax settings saved: $settings")
        } catch (e: Exception) {
            println("❌ TaxPreferencesManager - Failed to save tax settings: ${e.message}")
        }
    }
    
    /**
     * Load tax settings from persistent storage
     */
    fun loadTaxSettings(): TaxSettings {
        return _currentSettings ?: run {
            loadSettingsFromStorage()
            _currentSettings ?: getDefaultSettings()
        }
    }
    
    /**
     * Get current tax settings without loading from storage
     */
    fun getCurrentTaxSettings(): TaxSettings {
        return _currentSettings ?: getDefaultSettings()
    }
    
    /**
     * Get default tax settings
     */
    fun getDefaultSettings(): TaxSettings {
        return TaxSettings(
            taxRate = DEFAULT_TAX_RATE,
            showTaxBreakdownInCart = DEFAULT_SHOW_TAX_BREAKDOWN,
            showTaxOnReceipts = DEFAULT_SHOW_TAX_ON_RECEIPTS,
            displayTaxInclusivePricing = DEFAULT_DISPLAY_TAX_INCLUSIVE,
            calculateTaxOnDiscountedAmount = DEFAULT_CALCULATE_TAX_ON_DISCOUNTED
        )
    }
    
    /**
     * Check if tax settings exist in storage
     */
    fun hasTaxSettings(): Boolean {
        return prefs.get(TAX_RATE_KEY, null) != null
    }
    
    /**
     * Clear tax settings from storage (reset to default)
     */
    suspend fun clearTaxSettings() = mutex.withLock {
        try {
            _currentSettings = null
            prefs.remove(TAX_RATE_KEY)
            prefs.remove(SHOW_TAX_BREAKDOWN_KEY)
            prefs.remove(SHOW_TAX_ON_RECEIPTS_KEY)
            prefs.remove(DISPLAY_TAX_INCLUSIVE_KEY)
            prefs.remove(CALCULATE_TAX_ON_DISCOUNTED_KEY)
            println("✅ TaxPreferencesManager - Tax settings cleared")
        } catch (e: Exception) {
            println("❌ TaxPreferencesManager - Failed to clear tax settings: ${e.message}")
        }
    }
    
    /**
     * Update only the tax rate
     */
    suspend fun updateTaxRate(newRate: Double) {
        val currentSettings = loadTaxSettings()
        saveTaxSettings(currentSettings.copy(taxRate = newRate))
    }
    
    /**
     * Validate tax rate (must be between 0 and 1, representing 0% to 100%)
     */
    fun isValidTaxRate(rate: Double): Boolean {
        return rate >= 0.0 && rate <= 1.0
    }
    
    /**
     * Convert percentage to decimal (e.g., 15.0 -> 0.15)
     */
    fun percentageToDecimal(percentage: Double): Double {
        return percentage / 100.0
    }
    
    /**
     * Convert decimal to percentage (e.g., 0.15 -> 15.0)
     */
    fun decimalToPercentage(decimal: Double): Double {
        return decimal * 100.0
    }
    
    /**
     * Validate tax settings before saving
     */
    fun validateTaxSettings(settings: TaxSettings): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate tax rate
        if (!isValidTaxRate(settings.taxRate)) {
            errors.add("معدل الضريبة يجب أن يكون بين 0% و 100%")
        }

        // Additional business logic validations can be added here
        if (settings.taxRate > 0.5) { // 50%
            errors.add("تحذير: معدل الضريبة مرتفع جداً (أكثر من 50%)")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }

    /**
     * Save tax settings with validation
     */
    suspend fun saveTaxSettingsWithValidation(settings: TaxSettings): ValidationResult {
        val validationResult = validateTaxSettings(settings)

        return when (validationResult) {
            is ValidationResult.Success -> {
                saveTaxSettings(settings)
                ValidationResult.Success
            }
            is ValidationResult.Error -> validationResult
        }
    }

    /**
     * Get tax settings history (for audit purposes)
     */
    fun getTaxSettingsHistory(): List<TaxSettingsHistoryEntry> {
        // This could be expanded to track changes over time
        val currentSettings = loadTaxSettings()
        return listOf(
            TaxSettingsHistoryEntry(
                timestamp = System.currentTimeMillis(),
                settings = currentSettings,
                changeReason = "Current settings"
            )
        )
    }

    /**
     * Load settings from persistent storage
     */
    private fun loadSettingsFromStorage() {
        try {
            val taxRate = prefs.getDouble(TAX_RATE_KEY, DEFAULT_TAX_RATE)
            val showTaxBreakdown = prefs.getBoolean(SHOW_TAX_BREAKDOWN_KEY, DEFAULT_SHOW_TAX_BREAKDOWN)
            val showTaxOnReceipts = prefs.getBoolean(SHOW_TAX_ON_RECEIPTS_KEY, DEFAULT_SHOW_TAX_ON_RECEIPTS)
            val displayTaxInclusive = prefs.getBoolean(DISPLAY_TAX_INCLUSIVE_KEY, DEFAULT_DISPLAY_TAX_INCLUSIVE)
            val calculateTaxOnDiscounted = prefs.getBoolean(CALCULATE_TAX_ON_DISCOUNTED_KEY, DEFAULT_CALCULATE_TAX_ON_DISCOUNTED)

            // Validate loaded settings
            val loadedSettings = TaxSettings(
                taxRate = taxRate,
                showTaxBreakdownInCart = showTaxBreakdown,
                showTaxOnReceipts = showTaxOnReceipts,
                displayTaxInclusivePricing = displayTaxInclusive,
                calculateTaxOnDiscountedAmount = calculateTaxOnDiscounted
            )

            val validationResult = validateTaxSettings(loadedSettings)
            _currentSettings = if (validationResult is ValidationResult.Success) {
                loadedSettings
            } else {
                println("⚠️ TaxPreferencesManager - Loaded settings invalid, using defaults")
                getDefaultSettings()
            }

            println("✅ TaxPreferencesManager - Tax settings loaded: $_currentSettings")
        } catch (e: Exception) {
            println("❌ TaxPreferencesManager - Failed to load tax settings: ${e.message}")
            _currentSettings = getDefaultSettings()
        }
    }
}

/**
 * Validation result for tax settings
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: List<String>) : ValidationResult()
}

/**
 * Tax settings history entry for audit trail
 */
data class TaxSettingsHistoryEntry(
    val timestamp: Long,
    val settings: TaxSettings,
    val changeReason: String
)
