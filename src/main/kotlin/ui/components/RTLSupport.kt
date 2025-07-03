package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * مكون لدعم اتجاه النص من اليمين لليسار (RTL) للغة العربية
 */
@Composable
fun RTLProvider(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        content()
    }
}

/**
 * Row مع دعم RTL للعربية
 */
@Composable
fun RTLRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = when (horizontalArrangement) {
            Arrangement.Start -> Arrangement.End
            Arrangement.End -> Arrangement.Start
            else -> horizontalArrangement
        },
        verticalAlignment = verticalAlignment,
        content = content
    )
}

/**
 * مساعد لتحديد اتجاه التخطيط
 */
@Composable
fun isRTL(): Boolean {
    return LocalLayoutDirection.current == LayoutDirection.Rtl
}

/**
 * Spacer للحصول على المسافة الصحيحة في RTL
 */
@Composable
fun RTLSpacer(width: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.width(width))
}
