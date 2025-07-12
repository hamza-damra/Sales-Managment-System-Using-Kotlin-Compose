package data.preferences

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import utils.Constants
import utils.CurrencyInfo
import java.util.prefs.Preferences

/**
 * Data class representing currency configuration settings
 */
data class CurrencySettings(
    val currencyCode: String = Constants.Currency.DEFAULT_CURRENCY_CODE,
    val currencySymbol: String = Constants.Currency.DEFAULT_CURRENCY_SYMBOL,
    val displayName: String = Constants.Currency.DEFAULT_CURRENCY_DISPLAY_NAME,
    val locale: String = Constants.Currency.DEFAULT_LOCALE,
    val showSymbolBeforeAmount: Boolean = false, // For RTL languages, symbol usually comes after
    val decimalPlaces: Int = 2,
    val useGroupingSeparator: Boolean = true
)

/**
 * Manages currency preferences with persistent storage using Java Preferences API
 * Follows the same pattern as TaxPreferencesManager for consistency
 */
class CurrencyPreferencesManager {
    private val prefs = Preferences.userNodeForPackage(CurrencyPreferencesManager::class.java)
    private val mutex = Mutex()
    
    companion object {
        private const val CURRENCY_CODE_KEY = "currency_code"
        private const val CURRENCY_SYMBOL_KEY = "currency_symbol"
        private const val DISPLAY_NAME_KEY = "display_name"
        private const val LOCALE_KEY = "locale"
        private const val SHOW_SYMBOL_BEFORE_KEY = "show_symbol_before_amount"
        private const val DECIMAL_PLACES_KEY = "decimal_places"
        private const val USE_GROUPING_SEPARATOR_KEY = "use_grouping_separator"
        
        // Default values (Israeli Shekel)
        private const val DEFAULT_CURRENCY_CODE = Constants.Currency.DEFAULT_CURRENCY_CODE
        private const val DEFAULT_CURRENCY_SYMBOL = Constants.Currency.DEFAULT_CURRENCY_SYMBOL
        private const val DEFAULT_DISPLAY_NAME = Constants.Currency.DEFAULT_CURRENCY_DISPLAY_NAME
        private const val DEFAULT_LOCALE = Constants.Currency.DEFAULT_LOCALE
        private const val DEFAULT_SHOW_SYMBOL_BEFORE = false
        private const val DEFAULT_DECIMAL_PLACES = 2
        private const val DEFAULT_USE_GROUPING_SEPARATOR = true
    }
    
    private var _currentSettings: CurrencySettings? = null
    
    init {
        loadSettingsFromStorage()
    }
    
    /**
     * Save currency settings to persistent storage
     */
    suspend fun saveCurrencySettings(settings: CurrencySettings) = mutex.withLock {
        try {
            _currentSettings = settings
            prefs.put(CURRENCY_CODE_KEY, settings.currencyCode)
            prefs.put(CURRENCY_SYMBOL_KEY, settings.currencySymbol)
            prefs.put(DISPLAY_NAME_KEY, settings.displayName)
            prefs.put(LOCALE_KEY, settings.locale)
            prefs.putBoolean(SHOW_SYMBOL_BEFORE_KEY, settings.showSymbolBeforeAmount)
            prefs.putInt(DECIMAL_PLACES_KEY, settings.decimalPlaces)
            prefs.putBoolean(USE_GROUPING_SEPARATOR_KEY, settings.useGroupingSeparator)
            println("✅ CurrencyPreferencesManager - Currency settings saved: $settings")
        } catch (e: Exception) {
            println("❌ CurrencyPreferencesManager - Failed to save currency settings: ${e.message}")
        }
    }
    
    /**
     * Load currency settings from persistent storage
     */
    fun loadCurrencySettings(): CurrencySettings {
        return _currentSettings ?: run {
            loadSettingsFromStorage()
            _currentSettings ?: getDefaultSettings()
        }
    }
    
    /**
     * Get current currency settings without loading from storage
     */
    fun getCurrentCurrencySettings(): CurrencySettings {
        return _currentSettings ?: getDefaultSettings()
    }
    
    /**
     * Get default currency settings (Israeli Shekel)
     */
    fun getDefaultSettings(): CurrencySettings {
        return CurrencySettings(
            currencyCode = DEFAULT_CURRENCY_CODE,
            currencySymbol = DEFAULT_CURRENCY_SYMBOL,
            displayName = DEFAULT_DISPLAY_NAME,
            locale = DEFAULT_LOCALE,
            showSymbolBeforeAmount = DEFAULT_SHOW_SYMBOL_BEFORE,
            decimalPlaces = DEFAULT_DECIMAL_PLACES,
            useGroupingSeparator = DEFAULT_USE_GROUPING_SEPARATOR
        )
    }
    
    /**
     * Set currency from predefined currency info
     */
    suspend fun setCurrency(currencyInfo: CurrencyInfo) {
        val settings = CurrencySettings(
            currencyCode = currencyInfo.code,
            currencySymbol = currencyInfo.symbol,
            displayName = currencyInfo.displayName,
            locale = currencyInfo.locale,
            showSymbolBeforeAmount = when (currencyInfo.code) {
                "USD", "EUR", "GBP" -> true // Western currencies show symbol before
                else -> false // Arabic/Hebrew currencies show symbol after
            },
            decimalPlaces = when (currencyInfo.code) {
                "JOD" -> 3 // Jordanian Dinar has 3 decimal places
                else -> 2
            },
            useGroupingSeparator = true
        )
        saveCurrencySettings(settings)
    }
    
    /**
     * Get available currencies
     */
    fun getAvailableCurrencies(): Map<String, CurrencyInfo> {
        return Constants.Currency.SUPPORTED_CURRENCIES
    }
    
    /**
     * Check if currency settings exist in storage
     */
    fun hasCurrencySettings(): Boolean {
        return prefs.get(CURRENCY_CODE_KEY, null) != null
    }
    
    /**
     * Clear currency settings from storage (reset to default)
     */
    suspend fun clearCurrencySettings() = mutex.withLock {
        try {
            _currentSettings = null
            prefs.remove(CURRENCY_CODE_KEY)
            prefs.remove(CURRENCY_SYMBOL_KEY)
            prefs.remove(DISPLAY_NAME_KEY)
            prefs.remove(LOCALE_KEY)
            prefs.remove(SHOW_SYMBOL_BEFORE_KEY)
            prefs.remove(DECIMAL_PLACES_KEY)
            prefs.remove(USE_GROUPING_SEPARATOR_KEY)
            println("✅ CurrencyPreferencesManager - Currency settings cleared")
        } catch (e: Exception) {
            println("❌ CurrencyPreferencesManager - Failed to clear currency settings: ${e.message}")
        }
    }
    
    /**
     * Migrate from legacy SAR currency to ILS
     */
    suspend fun migrateFromLegacyCurrency() {
        val currentSettings = loadCurrencySettings()
        if (currentSettings.currencyCode == Constants.Currency.LEGACY_CURRENCY_CODE) {
            println("🔄 Migrating from legacy SAR currency to ILS...")
            val ilsCurrency = Constants.Currency.SUPPORTED_CURRENCIES[Constants.Currency.DEFAULT_CURRENCY_CODE]
            if (ilsCurrency != null) {
                setCurrency(ilsCurrency)
                println("✅ Migration completed: SAR → ILS")
            }
        }
    }
    
    /**
     * Validate currency settings before saving
     */
    fun validateCurrencySettings(settings: CurrencySettings): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate currency code
        if (settings.currencyCode.isBlank()) {
            errors.add("رمز العملة مطلوب")
        } else if (!Constants.Currency.SUPPORTED_CURRENCIES.containsKey(settings.currencyCode)) {
            errors.add("رمز العملة غير مدعوم: ${settings.currencyCode}")
        }

        // Validate currency symbol
        if (settings.currencySymbol.isBlank()) {
            errors.add("رمز العملة مطلوب")
        }

        // Validate decimal places
        if (settings.decimalPlaces < 0 || settings.decimalPlaces > 4) {
            errors.add("عدد الخانات العشرية يجب أن يكون بين 0 و 4")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }

    /**
     * Save currency settings with validation
     */
    suspend fun saveCurrencySettingsWithValidation(settings: CurrencySettings): ValidationResult {
        val validationResult = validateCurrencySettings(settings)

        return when (validationResult) {
            is ValidationResult.Success -> {
                saveCurrencySettings(settings)
                ValidationResult.Success
            }
            is ValidationResult.Error -> validationResult
        }
    }

    /**
     * Load settings from persistent storage
     */
    private fun loadSettingsFromStorage() {
        try {
            val currencyCode = prefs.get(CURRENCY_CODE_KEY, DEFAULT_CURRENCY_CODE)
            val currencySymbol = prefs.get(CURRENCY_SYMBOL_KEY, DEFAULT_CURRENCY_SYMBOL)
            val displayName = prefs.get(DISPLAY_NAME_KEY, DEFAULT_DISPLAY_NAME)
            val locale = prefs.get(LOCALE_KEY, DEFAULT_LOCALE)
            val showSymbolBefore = prefs.getBoolean(SHOW_SYMBOL_BEFORE_KEY, DEFAULT_SHOW_SYMBOL_BEFORE)
            val decimalPlaces = prefs.getInt(DECIMAL_PLACES_KEY, DEFAULT_DECIMAL_PLACES)
            val useGroupingSeparator = prefs.getBoolean(USE_GROUPING_SEPARATOR_KEY, DEFAULT_USE_GROUPING_SEPARATOR)

            val loadedSettings = CurrencySettings(
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                displayName = displayName,
                locale = locale,
                showSymbolBeforeAmount = showSymbolBefore,
                decimalPlaces = decimalPlaces,
                useGroupingSeparator = useGroupingSeparator
            )

            val validationResult = validateCurrencySettings(loadedSettings)
            _currentSettings = if (validationResult is ValidationResult.Success) {
                loadedSettings
            } else {
                println("⚠️ CurrencyPreferencesManager - Loaded settings invalid, using defaults")
                getDefaultSettings()
            }

            println("✅ CurrencyPreferencesManager - Currency settings loaded: $_currentSettings")
        } catch (e: Exception) {
            println("❌ CurrencyPreferencesManager - Failed to load currency settings: ${e.message}")
            _currentSettings = getDefaultSettings()
        }
    }
}
