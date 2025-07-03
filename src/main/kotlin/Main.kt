import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import data.SalesDataManager
import ui.components.RTLProvider
import ui.screens.CustomersScreen
import ui.screens.DashboardScreen
import ui.screens.InventoryScreen
import ui.screens.ProductsScreen
import ui.screens.PromotionsScreen
import ui.screens.ReportsScreen
import ui.screens.ReturnsScreen
import ui.screens.SalesScreen
import ui.screens.SettingsScreen
import ui.screens.SuppliersScreen
import ui.theme.AppTheme
import ui.theme.AppThemeProvider
import ui.theme.ThemeState
import java.text.NumberFormat
import java.util.*

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "نظام إدارة المبيعات - Sales Management System",
        state = rememberWindowState(width = 1400.dp, height = 900.dp)
    ) {
        AppThemeProvider {
            App()
        }
    }
}

// إضافة بعض الوظائف المساعدة للتحسين
object UiUtils {
    // تنسيق العملة
    fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("ar", "SA"))
        formatter.currency = Currency.getInstance("SAR")
        return formatter.format(amount)
    }

    // تنسيق النسبة المئوية
    fun formatPercentage(value: Double): String {
        return String.format("%.1f%%", value)
    }

    // تحديد لون حالة المخزون - now requires @Composable context
    @Composable
    fun getStockStatusColor(stock: Int): androidx.compose.ui.graphics.Color {
        return when {
            stock > 20 -> AppTheme.colors.success
            stock > 10 -> AppTheme.colors.warning
            stock > 0 -> AppTheme.colors.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    // تحديد نص حالة المخزون
    fun getStockStatusText(stock: Int): String {
        return when {
            stock > 20 -> "متوفر"
            stock > 10 -> "قليل"
            stock > 0 -> "ناقص"
            else -> "نفد"
        }
    }
}

enum class Screen(val title: String, val icon: ImageVector) {
    DASHBOARD("لوحة التحكم", Icons.Default.Dashboard),
    SALES("المبيعات", Icons.Default.ShoppingCart),
    PRODUCTS("المنتجات", Icons.Default.Inventory),
    CUSTOMERS("العملاء", Icons.Default.People),
    INVENTORY("إدارة المخزون", Icons.Default.Warehouse),
    SUPPLIERS("إدارة الموردين", Icons.Default.Business),
    RETURNS("المرتجعات والإلغاءات", Icons.Default.AssignmentReturn),
    PROMOTIONS("العروض والخصومات", Icons.Default.LocalOffer),
    REPORTS("التقارير والتحليلات", Icons.Default.Analytics),
    SETTINGS("الإعدادات", Icons.Default.Settings)
}

data class NavigationItem(
    val screen: Screen,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    val salesDataManager = remember { SalesDataManager() }

    RTLProvider {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Content - العرض الرئيسي على اليمين في RTL
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(24.dp)
            ) {
                when (currentScreen) {
                    Screen.DASHBOARD -> DashboardScreen(salesDataManager)
                    Screen.SALES -> SalesScreen(salesDataManager)
                    Screen.PRODUCTS -> ProductsScreen(salesDataManager)
                    Screen.CUSTOMERS -> CustomersScreen(salesDataManager)
                    Screen.INVENTORY -> InventoryScreen(salesDataManager)
                    Screen.SUPPLIERS -> SuppliersScreen(salesDataManager)
                    Screen.RETURNS -> ReturnsScreen()
                    Screen.PROMOTIONS -> PromotionsScreen()
                    Screen.REPORTS -> ReportsScreen()
                    Screen.SETTINGS -> SettingsScreen()
                }
            }

            // Navigation Sidebar - شريط التنقل على اليسار في RTL
            NavigationSidebar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    }
}

@Composable
fun NavigationSidebar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // App Title
        Text(
            text = "نظام إدارة المبيعات",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = TextAlign.Right
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Items
        Screen.values().forEach { screen ->
            NavigationItem(
                item = NavigationItem(
                    screen = screen,
                    isSelected = currentScreen == screen,
                    onClick = { onScreenSelected(screen) }
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.info.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = AppTheme.colors.info,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "إصدار 1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun NavigationItem(item: NavigationItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        onClick = item.onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End // تغيير الترتيب للعربية
        ) {
            Text(
                text = item.screen.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                item.screen.icon,
                contentDescription = item.screen.title,
                tint = if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FrameWindowScope.CustomTitleBar(
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // منطقة قابلة للسحب في الخلفية
        WindowDraggableArea(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "نظام إدارة المبيعات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // أزرار التحكم على الشمال (النهاية في RTL)
        Row(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // زر التصغير
            IconButton(onClick = onMinimize, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Minimize, "تصغير", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // زر التكبير
            IconButton(onClick = onMaximize, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.CropSquare, "تكبير", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // زر الإغلاق
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, "إغلاق", tint = AppTheme.colors.error)
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
