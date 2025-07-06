import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import data.SalesDataManager
import data.di.AppContainer
import data.di.AppDependencies
import ui.components.RTLProvider
import ui.screens.*
import ui.theme.AppTheme
import ui.theme.AppThemeProvider
import ui.theme.ThemeState
import java.text.NumberFormat
import java.util.*

fun main() = application {
    Window(
        onCloseRequest = {
            // Clean up resources before closing
            AppDependencies.container.cleanup()
            exitApplication()
        },
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
    CATEGORIES("الفئات", Icons.Default.Category),
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
    val appContainer = remember { AppDependencies.container }
    val authService = remember { appContainer.authService }
    val authState by authService.authState.collectAsState()

    // Show login screen if not authenticated
    if (!authState.isAuthenticated) {
        LoginScreen(
            authService = authService,
            onLoginSuccess = {
                // Authentication successful, main app will be shown
            }
        )
    } else {
        // Main application content
        MainAppContent(appContainer)
    }
}

@Composable
fun MainAppContent(appContainer: AppContainer) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

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
                    .padding(28.dp)
            ) {
                when (currentScreen) {
                    Screen.DASHBOARD -> DashboardScreen(
                        dashboardViewModel = appContainer.dashboardViewModel,
                        onNavigateToSales = { currentScreen = Screen.SALES },
                        onNavigateToProducts = { currentScreen = Screen.PRODUCTS },
                        onNavigateToCustomers = { currentScreen = Screen.CUSTOMERS },
                        onNavigateToInventory = { currentScreen = Screen.INVENTORY },
                        onNavigateToReports = { currentScreen = Screen.REPORTS }
                    )
                    Screen.SALES -> SalesScreen(
                        salesRepository = appContainer.salesRepository,
                        customerRepository = appContainer.customerRepository,
                        productRepository = appContainer.productRepository
                    )
                    Screen.PRODUCTS -> ProductsScreen(
                        productViewModel = appContainer.productViewModel
                    )
                    Screen.CATEGORIES -> CategoriesScreen(
                        categoryViewModel = appContainer.categoryViewModel
                    )
                    Screen.CUSTOMERS -> CustomersScreen()
                    Screen.INVENTORY -> InventoryScreen(SalesDataManager()) // TODO: Replace with ViewModel
                    Screen.SUPPLIERS -> SuppliersScreen(
                        supplierViewModel = appContainer.supplierViewModel
                    )
                    Screen.RETURNS -> ReturnsScreen() // TODO: Replace with ViewModel
                    Screen.PROMOTIONS -> PromotionsScreen() // TODO: Replace with ViewModel
                    Screen.REPORTS -> ReportsScreen() // TODO: Replace with ViewModel
                    Screen.SETTINGS -> SettingsScreen() // TODO: Replace with ViewModel
                }
            }

            // Navigation Sidebar - شريط التنقل على اليسار في RTL
            NavigationSidebar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it },
                authService = appContainer.authService
            )
        }
    }
}

@Composable
fun NavigationSidebar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    authService: data.auth.AuthService
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // App Title
        Text(
            text = "نظام إدارة المبيعات",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 20.dp),
            textAlign = TextAlign.Right
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant,
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(20.dp))

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

        // User info and logout
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${currentUser.firstName} ${currentUser.lastName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = currentUser.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val logoutInteractionSource = remember { MutableInteractionSource() }
                    val isLogoutHovered by logoutInteractionSource.collectIsHoveredAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                color = if (isLogoutHovered)
                                    MaterialTheme.colorScheme.error.copy(alpha = 1f)
                                else
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(
                                interactionSource = logoutInteractionSource,
                                indication = null
                            ) {
                                coroutineScope.launch {
                                    authService.logout()
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onError
                            )
                            Text(
                                "تسجيل الخروج",
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

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
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                color = when {
                    item.isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = if (item.isSelected) 1.5.dp else if (isHovered) 1.dp else 0.dp,
                color = if (item.isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { item.onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End // تغيير الترتيب للعربية
        ) {
            Text(
                text = item.screen.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    item.isSelected -> MaterialTheme.colorScheme.primary
                    isHovered -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                },
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                item.screen.icon,
                contentDescription = item.screen.title,
                tint = when {
                    item.isSelected -> MaterialTheme.colorScheme.primary
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(26.dp)
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
