package ui.utils

import androidx.compose.ui.graphics.Color

/**
 * Utility functions for color parsing and manipulation
 */
object ColorUtils {
    
    /**
     * Parse hex color string to Compose Color
     * Supports formats: #RGB, #RRGGBB, #AARRGGBB
     */
    fun parseHexColor(hexColor: String): Color? {
        return try {
            val cleanHex = hexColor.removePrefix("#")
            when (cleanHex.length) {
                3 -> {
                    // #RGB -> #RRGGBB
                    val r = cleanHex[0].toString().repeat(2)
                    val g = cleanHex[1].toString().repeat(2)
                    val b = cleanHex[2].toString().repeat(2)
                    Color(
                        red = r.toInt(16) / 255f,
                        green = g.toInt(16) / 255f,
                        blue = b.toInt(16) / 255f
                    )
                }
                6 -> {
                    // #RRGGBB
                    Color(
                        red = cleanHex.substring(0, 2).toInt(16) / 255f,
                        green = cleanHex.substring(2, 4).toInt(16) / 255f,
                        blue = cleanHex.substring(4, 6).toInt(16) / 255f
                    )
                }
                8 -> {
                    // #AARRGGBB
                    Color(
                        alpha = cleanHex.substring(0, 2).toInt(16) / 255f,
                        red = cleanHex.substring(2, 4).toInt(16) / 255f,
                        green = cleanHex.substring(4, 6).toInt(16) / 255f,
                        blue = cleanHex.substring(6, 8).toInt(16) / 255f
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Convert Color to hex string
     */
    fun colorToHex(color: Color): String {
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        val alpha = (color.alpha * 255).toInt()
        
        return if (alpha == 255) {
            "#%02X%02X%02X".format(red, green, blue)
        } else {
            "#%02X%02X%02X%02X".format(alpha, red, green, blue)
        }
    }
    
    /**
     * Get a contrasting text color (black or white) for a given background color
     */
    fun getContrastingTextColor(backgroundColor: Color): Color {
        val luminance = 0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue
        return if (luminance > 0.5) Color.Black else Color.White
    }

    /**
     * Validate if a hex color string is valid
     * Supports formats: #RGB, #RRGGBB, #AARRGGBB
     */
    fun isValidHexColor(hexColor: String): Boolean {
        if (hexColor.isBlank()) return false

        val cleanHex = hexColor.removePrefix("#")
        if (cleanHex.length !in listOf(3, 6, 8)) return false

        return cleanHex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
    }

    /**
     * Get validation error message for invalid hex color
     */
    fun getColorValidationError(hexColor: String): String? {
        if (hexColor.isBlank()) return "رمز اللون مطلوب"

        if (!hexColor.startsWith("#")) return "رمز اللون يجب أن يبدأ بـ #"

        val cleanHex = hexColor.removePrefix("#")
        when {
            cleanHex.length !in listOf(3, 6, 8) -> return "رمز اللون يجب أن يكون بصيغة #RGB أو #RRGGBB"
            !cleanHex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' } -> return "رمز اللون يحتوي على أحرف غير صالحة"
            else -> return null
        }
    }

    /**
     * Normalize hex color to standard format (#RRGGBB)
     */
    fun normalizeHexColor(hexColor: String): String {
        if (!isValidHexColor(hexColor)) return hexColor

        val cleanHex = hexColor.removePrefix("#")
        return when (cleanHex.length) {
            3 -> {
                // #RGB -> #RRGGBB
                val r = cleanHex[0].toString().repeat(2)
                val g = cleanHex[1].toString().repeat(2)
                val b = cleanHex[2].toString().repeat(2)
                "#$r$g$b"
            }
            6 -> "#$cleanHex"
            8 -> "#${cleanHex.substring(2)}" // Remove alpha for standard format
            else -> hexColor
        }
    }
}
