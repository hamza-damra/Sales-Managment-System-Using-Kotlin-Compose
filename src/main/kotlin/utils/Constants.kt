package utils

/**
 * Application-wide constants for centralized configuration
 * Following the established pattern for consistent configuration management
 */
object Constants {
    
    // API Configuration
    const val BASE_URL = "http://localhost:8080"
    
    // Currency Configuration
    object Currency {
        // Supported currencies with their display information
        val SUPPORTED_CURRENCIES = mapOf(
            "ILS" to CurrencyInfo(
                code = "ILS",
                symbol = "₪",
                displayName = "شيكل إسرائيلي",
                displayNameEnglish = "Israeli Shekel",
                locale = "he_IL",
                isDefault = true
            ),
            "SAR" to CurrencyInfo(
                code = "SAR",
                symbol = "ر.س",
                displayName = "ريال سعودي",
                displayNameEnglish = "Saudi Riyal",
                locale = "ar_SA",
                isDefault = false
            ),
            "USD" to CurrencyInfo(
                code = "USD",
                symbol = "$",
                displayName = "دولار أمريكي",
                displayNameEnglish = "US Dollar",
                locale = "en_US",
                isDefault = false
            ),
            "EUR" to CurrencyInfo(
                code = "EUR",
                symbol = "€",
                displayName = "يورو",
                displayNameEnglish = "Euro",
                locale = "en_EU",
                isDefault = false
            ),
            "GBP" to CurrencyInfo(
                code = "GBP",
                symbol = "£",
                displayName = "جنيه إسترليني",
                displayNameEnglish = "British Pound",
                locale = "en_GB",
                isDefault = false
            ),
            "AED" to CurrencyInfo(
                code = "AED",
                symbol = "د.إ",
                displayName = "درهم إماراتي",
                displayNameEnglish = "UAE Dirham",
                locale = "ar_AE",
                isDefault = false
            ),
            "JOD" to CurrencyInfo(
                code = "JOD",
                symbol = "د.أ",
                displayName = "دينار أردني",
                displayNameEnglish = "Jordanian Dinar",
                locale = "ar_JO",
                isDefault = false
            ),
            "EGP" to CurrencyInfo(
                code = "EGP",
                symbol = "ج.م",
                displayName = "جنيه مصري",
                displayNameEnglish = "Egyptian Pound",
                locale = "ar_EG",
                isDefault = false
            )
        )
        
        // Default currency (Israeli Shekel)
        const val DEFAULT_CURRENCY_CODE = "ILS"
        const val DEFAULT_CURRENCY_SYMBOL = "₪"
        const val DEFAULT_CURRENCY_DISPLAY_NAME = "شيكل إسرائيلي"
        const val DEFAULT_LOCALE = "he_IL"
        
        // Legacy currency (for migration)
        const val LEGACY_CURRENCY_CODE = "SAR"
        const val LEGACY_CURRENCY_SYMBOL = "ر.س"
    }
    
    // Application Configuration
    object App {
        const val NAME = "نظام إدارة المبيعات"
        const val VERSION = "2.0.0"
        const val COMPANY = "شركة التجارة المتقدمة"
    }
    
    // File and Export Configuration
    object Files {
        const val RECEIPTS_DIRECTORY = "receipts"
        const val EXPORTS_DIRECTORY = "exports"
        const val BACKUPS_DIRECTORY = "backups"
        const val TEMP_DIRECTORY = "temp"
    }
    
    // UI Configuration
    object UI {
        const val DEFAULT_ANIMATION_DURATION = 300L
        const val CARD_CORNER_RADIUS = 16
        const val BUTTON_CORNER_RADIUS = 12
        const val DIALOG_CORNER_RADIUS = 20
    }
}

/**
 * Data class representing currency information
 */
data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val displayName: String,
    val displayNameEnglish: String,
    val locale: String,
    val isDefault: Boolean = false
)
