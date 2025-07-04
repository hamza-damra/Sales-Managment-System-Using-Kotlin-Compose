package ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive design utilities for consistent sizing across different screen sizes
 */
object ResponsiveUtils {
    
    // Breakpoints
    const val TABLET_BREAKPOINT = 600
    const val DESKTOP_BREAKPOINT = 1024
    const val LARGE_DESKTOP_BREAKPOINT = 1440
    
    @Composable
    fun getScreenInfo(): ScreenInfo {
        // For Compose Desktop, we'll assume desktop by default
        // This can be enhanced later with proper window size detection
        return ScreenInfo(
            widthDp = 1400, // Increased default desktop width for better layout
            heightDp = 900,  // Increased default desktop height
            isTablet = false,
            isDesktop = true,
            isLargeDesktop = true // Assume large desktop for better experience
        )
    }
    
    @Composable
    fun getResponsiveSpacing(): ResponsiveSpacing {
        val screenInfo = getScreenInfo()
        
        return when {
            screenInfo.isLargeDesktop -> ResponsiveSpacing(
                tiny = 4.dp,
                small = 8.dp,
                medium = 16.dp,
                large = 24.dp,
                extraLarge = 32.dp,
                huge = 48.dp
            )
            screenInfo.isDesktop -> ResponsiveSpacing(
                tiny = 4.dp,
                small = 8.dp,
                medium = 12.dp,
                large = 20.dp,
                extraLarge = 28.dp,
                huge = 40.dp
            )
            screenInfo.isTablet -> ResponsiveSpacing(
                tiny = 3.dp,
                small = 6.dp,
                medium = 10.dp,
                large = 16.dp,
                extraLarge = 24.dp,
                huge = 32.dp
            )
            else -> ResponsiveSpacing(
                tiny = 2.dp,
                small = 4.dp,
                medium = 8.dp,
                large = 12.dp,
                extraLarge = 16.dp,
                huge = 24.dp
            )
        }
    }
    
    @Composable
    fun getResponsivePadding(): ResponsivePadding {
        val screenInfo = getScreenInfo()

        return when {
            screenInfo.isLargeDesktop -> ResponsivePadding(
                card = 32.dp,
                screen = 36.dp,
                section = 28.dp,
                item = 24.dp
            )
            screenInfo.isDesktop -> ResponsivePadding(
                card = 28.dp,
                screen = 32.dp,
                section = 24.dp,
                item = 20.dp
            )
            screenInfo.isTablet -> ResponsivePadding(
                card = 24.dp,
                screen = 24.dp,
                section = 20.dp,
                item = 16.dp
            )
            else -> ResponsivePadding(
                card = 20.dp,
                screen = 20.dp,
                section = 16.dp,
                item = 14.dp
            )
        }
    }
    
    @Composable
    fun getResponsiveCornerRadius(): ResponsiveCornerRadius {
        val screenInfo = getScreenInfo()
        
        return when {
            screenInfo.isDesktop -> ResponsiveCornerRadius(
                small = 8.dp,
                medium = 12.dp,
                large = 20.dp,
                extraLarge = 24.dp
            )
            screenInfo.isTablet -> ResponsiveCornerRadius(
                small = 6.dp,
                medium = 10.dp,
                large = 16.dp,
                extraLarge = 20.dp
            )
            else -> ResponsiveCornerRadius(
                small = 4.dp,
                medium = 8.dp,
                large = 12.dp,
                extraLarge = 16.dp
            )
        }
    }
    
    @Composable
    fun getGridColumns(
        mobile: Int = 1,
        tablet: Int = 2,
        desktop: Int = 2, // Changed to 2 as requested
        largeDesktop: Int = 3 // Can be 3 for very large screens
    ): Int {
        val screenInfo = getScreenInfo()

        return when {
            screenInfo.isLargeDesktop -> largeDesktop
            screenInfo.isDesktop -> desktop
            screenInfo.isTablet -> tablet
            else -> mobile
        }
    }

    @Composable
    fun getProductGridColumns(): Int {
        val screenInfo = getScreenInfo()

        return when {
            screenInfo.isLargeDesktop -> 3 // 3 cards per row on very large screens
            screenInfo.isDesktop -> 2 // 2 cards per row on desktop (as requested)
            screenInfo.isTablet -> 2 // 2 cards per row on tablet
            else -> 1 // 1 card per row on mobile
        }
    }
    
    @Composable
    fun getCardHeight(
        mobile: Dp = 120.dp,
        tablet: Dp = 140.dp,
        desktop: Dp = 160.dp
    ): Dp {
        val screenInfo = getScreenInfo()
        
        return when {
            screenInfo.isDesktop -> desktop
            screenInfo.isTablet -> tablet
            else -> mobile
        }
    }
}

data class ScreenInfo(
    val widthDp: Int,
    val heightDp: Int,
    val isTablet: Boolean,
    val isDesktop: Boolean,
    val isLargeDesktop: Boolean
)

data class ResponsiveSpacing(
    val tiny: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val huge: Dp
)

data class ResponsivePadding(
    val card: Dp,
    val screen: Dp,
    val section: Dp,
    val item: Dp
)

data class ResponsiveCornerRadius(
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp
)

/**
 * Extension functions for easier usage
 */
@Composable
fun Int.responsiveColumns(
    tablet: Int = this * 2,
    desktop: Int = this * 3,
    largeDesktop: Int = this * 4
): Int = ResponsiveUtils.getGridColumns(this, tablet, desktop, largeDesktop)

@Composable
fun Dp.responsiveSize(
    tablet: Dp = this * 1.2f,
    desktop: Dp = this * 1.4f
): Dp {
    val screenInfo = ResponsiveUtils.getScreenInfo()
    return when {
        screenInfo.isDesktop -> desktop
        screenInfo.isTablet -> tablet
        else -> this
    }
}
