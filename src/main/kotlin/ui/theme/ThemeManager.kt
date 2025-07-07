package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Enhanced AppColors with additional properties for compatibility
@Immutable
data class AppColors(
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val purple: Color,
    val pink: Color,
    val indigo: Color,
    val teal: Color,
    val cardBackground: Color,
    val cardBackgroundElevated: Color,
    val cardStroke: Color,
    val cardStrokeVariant: Color,
    val shadowColor: Color,
    val elevatedShadowColor: Color,
    val chartColors: List<Color>,
    // Add compatibility properties
    val primary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val surface: Color,
    val cardBorder: Color
)

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

// Theme preference enum
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

// Theme state holder with persistent storage
@Stable
class ThemeState(
    private val preferencesManager: data.preferences.ThemePreferencesManager? = null
) {
    private var _themeMode by mutableStateOf(
        preferencesManager?.loadTheme() ?: ThemeMode.SYSTEM
    )
    val themeMode: ThemeMode get() = _themeMode

    fun setThemeMode(mode: ThemeMode) {
        _themeMode = mode
        // Save to persistent storage if available
        preferencesManager?.let { manager ->
            CoroutineScope(Dispatchers.IO).launch {
                manager.saveTheme(mode)
            }
        }
    }

    /**
     * Initialize theme from persistent storage
     */
    fun initializeFromStorage() {
        preferencesManager?.let { manager ->
            _themeMode = manager.loadTheme()
        }
    }
}

// Create a compositionLocal for theme state
val LocalThemeState = compositionLocalOf { ThemeState() }

// Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE7FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = Color(0xFF10B981),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F7ED),
    onSecondaryContainer = Color(0xFF002114),
    tertiary = Color(0xFF8B5CF6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8DDFF),
    onTertiaryContainer = Color(0xFF2E004E),
    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1E293B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E293B),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2F3349),
    inverseOnSurface = Color(0xFFF1F3F9),
    inversePrimary = Color(0xFFB0C6FF),
    surfaceDim = Color(0xFFDDE3EA),
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF7F9FC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFEBF0F7),
    surfaceContainerHighest = Color(0xFFE5EAF1)
)

// Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF001A41),
    primaryContainer = Color(0xFF00297A),
    onPrimaryContainer = Color(0xFFDBE7FF),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF002114),
    secondaryContainer = Color(0xFF00351F),
    onSecondaryContainer = Color(0xFFD4F7ED),
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF2E004E),
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFE8DDFF),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF410002),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF475569),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE2E8F0),
    inverseOnSurface = Color(0xFF2F3349),
    inversePrimary = Color(0xFF2563EB),
    surfaceDim = Color(0xFF1E293B),
    surfaceBright = Color(0xFF3B4A5C),
    surfaceContainerLowest = Color(0xFF0A1628),
    surfaceContainerLow = Color(0xFF1E293B),
    surfaceContainer = Color(0xFF222D3F),
    surfaceContainerHigh = Color(0xFF2D3748),
    surfaceContainerHighest = Color(0xFF374151)
)

private val LightAppColors = AppColors(
    success = Color(0xFF059669),
    warning = Color(0xFFD97706),
    error = Color(0xFFDC2626),
    info = Color(0xFF2563EB),
    purple = Color(0xFF7C3AED),
    pink = Color(0xFFDB2777),
    indigo = Color(0xFF4F46E5),
    teal = Color(0xFF0D9488),
    cardBackground = Color(0xFFF1F5F9),
    cardBackgroundElevated = Color(0xFFF8FAFC),
    cardStroke = Color(0xFFE2E8F0).copy(alpha = 0.8f),
    cardStrokeVariant = Color(0xFFCBD5E1).copy(alpha = 0.9f),
    shadowColor = Color.Black.copy(alpha = 0.08f),
    elevatedShadowColor = Color.Black.copy(alpha = 0.12f),
    chartColors = listOf(
        LightColorScheme.primary,
        Color(0xFF059669), // success
        Color(0xFFD97706), // warning
        Color(0xFFDC2626), // error
        Color(0xFF2563EB), // info
        Color(0xFF7C3AED), // purple
        Color(0xFFDB2777), // pink
        Color(0xFF4F46E5), // indigo
        Color(0xFF0D9488)  // teal
    ),
    // Compatibility properties
    primary = Color(0xFF2563EB),
    onSurface = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF64748B),
    surface = Color(0xFFFFFFFF),
    cardBorder = Color(0xFFE2E8F0)
)

private val DarkAppColors = AppColors(
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    error = Color(0xFFFF6B6B),
    info = Color(0xFF60A5FA),
    purple = Color(0xFFA78BFA),
    pink = Color(0xFFF472B6),
    indigo = Color(0xFF818CF8),
    teal = Color(0xFF2DD4BF),
    cardBackground = Color(0xFF2D3748),
    cardBackgroundElevated = Color(0xFF374151),
    cardStroke = Color(0xFF4B5563).copy(alpha = 0.4f),
    cardStrokeVariant = Color(0xFF6B7280).copy(alpha = 0.5f),
    shadowColor = Color.Black.copy(alpha = 0.4f),
    elevatedShadowColor = Color.Black.copy(alpha = 0.6f),
    chartColors = listOf(
        DarkColorScheme.primary,
        Color(0xFF34D399), // success
        Color(0xFFFBBF24), // warning
        Color(0xFFFF6B6B), // error
        Color(0xFF60A5FA), // info
        Color(0xFFA78BFA), // purple
        Color(0xFFF472B6), // pink
        Color(0xFF818CF8), // indigo
        Color(0xFF2DD4BF)  // teal
    ),
    // Compatibility properties
    primary = Color(0xFF60A5FA),
    onSurface = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF94A3B8),
    surface = Color(0xFF1E293B),
    cardBorder = Color(0xFF4B5563)
)

// Enhanced AppColors object that adapts to theme
object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    // Material 3 color scheme access
    val colorScheme: androidx.compose.material3.ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme
}

// Enhanced card styling utilities
object CardStyles {
    @Composable
    fun defaultCardColors() = CardDefaults.cardColors(
        containerColor = AppTheme.colors.cardBackground
    )

    @Composable
    fun elevatedCardColors() = CardDefaults.cardColors(
        containerColor = AppTheme.colors.cardBackgroundElevated
    )

    @Composable
    fun defaultCardElevation() = CardDefaults.cardElevation(
        defaultElevation = if (isSystemInDarkTheme()) 3.dp else 4.dp, // Adjusted elevation
        hoveredElevation = if (isSystemInDarkTheme()) 5.dp else 8.dp,
        pressedElevation = if (isSystemInDarkTheme()) 1.dp else 2.dp
    )

    @Composable
    fun elevatedCardElevation() = CardDefaults.cardElevation(
        defaultElevation = if (isSystemInDarkTheme()) 5.dp else 8.dp,
        hoveredElevation = if (isSystemInDarkTheme()) 8.dp else 12.dp,
        pressedElevation = if (isSystemInDarkTheme()) 2.dp else 4.dp
    )
}

// Fallback AppColors for non-Composable contexts (backwards compatibility)
// These use light theme colors as default
object AppColorsCompat {
    val Primary = Color(0xFF2563EB)
    val PrimaryVariant = Color(0xFF1D4ED8)
    val Secondary = Color(0xFF10B981)
    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F5F9)
    val OnPrimary = Color.White
    val OnSurface = Color(0xFF1E293B)
    val OnSurfaceVariant = Color(0xFF64748B)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF3B82F6)
    val Purple = Color(0xFF8B5CF6)
    val Pink = Color(0xFFEC4899)
    val Indigo = Color(0xFF6366F1)
    val Teal = Color(0xFF14B8A6)
}

// Main theme composable
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (isDarkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}

// Theme provider with state management
@Composable
fun AppThemeProvider(
    themeState: ThemeState = remember { ThemeState() },
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalThemeState provides themeState) {
        AppTheme(
            themeMode = themeState.themeMode,
            content = content
        )
    }
}

// Enhanced theme provider with persistent storage
@Composable
fun AppThemeProviderWithPersistence(
    preferencesManager: data.preferences.ThemePreferencesManager,
    content: @Composable () -> Unit
) {
    val themeState = remember { ThemeState(preferencesManager) }

    CompositionLocalProvider(LocalThemeState provides themeState) {
        AppTheme(
            themeMode = themeState.themeMode,
            content = content
        )
    }
}
