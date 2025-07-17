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
import ui.components.NotificationOverlay
import ui.screens.*
import ui.theme.AppTheme
import ui.theme.AppThemeProvider
import ui.theme.AppThemeProviderWithPersistence
import ui.theme.ThemeState
import java.text.NumberFormat
import java.util.*
import data.preferences.CurrencyPreferencesManager
import utils.CurrencyUtils
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = application {
    // Check for command line arguments
    val isFullscreen = args.contains("--fullscreen") ||
                      System.getProperty("app.fullscreen") == "true" ||
                      checkRegistryForFullscreen()

    // Perform currency migration on startup
    runBlocking {
        val currencyPreferencesManager = CurrencyPreferencesManager()
        currencyPreferencesManager.migrateFromLegacyCurrency()
    }

    // Configure window state based on fullscreen mode
    val windowState = if (isFullscreen) {
        rememberWindowState(placement = WindowPlacement.Fullscreen)
    } else {
        rememberWindowState(width = 1400.dp, height = 900.dp)
    }

    val closeApplication = {
        // Clean up resources before closing
        AppDependencies.container.cleanup()
        exitApplication()
    }

    Window(
        onCloseRequest = closeApplication,
        title = "Sales Management System - نظام إدارة المبيعات",
        state = windowState,
        undecorated = true, // Remove system title bar to use custom one
        resizable = !isFullscreen // Disable resizing in fullscreen mode
    ) {
        AppThemeProviderWithPersistence(
            preferencesManager = AppDependencies.container.themePreferencesManager
        ) {
            AppWithCustomTitleBar(windowState, closeApplication, isFullscreen)
        }
    }
}

/**
 * Check Windows registry for fullscreen mode setting
 */
private fun checkRegistryForFullscreen(): Boolean {
    return try {
        // On Windows, check registry for fullscreen setting
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            val process = ProcessBuilder(
                "reg", "query",
                "HKLM\\SOFTWARE\\HamzaDamra\\SalesManagementSystem",
                "/v", "FullscreenMode"
            ).start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            output.contains("0x1") || output.contains("1")
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}

// إضافة بعض الوظائف المساعدة للتحسين
object UiUtils {
    // تنسيق العملة
    fun formatCurrency(amount: Double): String {
        return CurrencyUtils.formatAmount(amount)
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
    SETTINGS("الإعدادات", Icons.Default.Settings),
    UPDATE("التحديثات", Icons.Default.SystemUpdate)
}

data class NavigationItem(
    val screen: Screen,
    val isSelected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun FrameWindowScope.AppWithCustomTitleBar(
    windowState: WindowState,
    onClose: () -> Unit,
    isFullscreen: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom title bar
        CustomTitleBar(
            onClose = onClose,
            onMinimize = { windowState.isMinimized = true },
            onMaximize = {
                windowState.placement = if (windowState.placement == WindowPlacement.Maximized) {
                    WindowPlacement.Floating
                } else {
                    WindowPlacement.Maximized
                }
            }
        )

        // Main app content
        Box(modifier = Modifier.weight(1f)) {
            App()
        }
    }
}

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // Navigation Sidebar - شريط التنقل على اليمين في RTL
                NavigationSidebar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                    authService = appContainer.authService
                )

                // Main Content - العرض الرئيسي على اليسار في RTL
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
                            productRepository = appContainer.productRepository,
                            promotionRepository = appContainer.promotionRepository,
                            notificationService = appContainer.notificationService
                        )
                        Screen.PRODUCTS -> ProductsScreen(
                            productViewModel = appContainer.productViewModel
                        )
                        Screen.CATEGORIES -> CategoriesScreen(
                            categoryViewModel = appContainer.categoryViewModel,
                            inventoryViewModel = appContainer.inventoryViewModel
                        )
                        Screen.CUSTOMERS -> CustomersScreen()
                        Screen.INVENTORY -> InventoryScreen(
                            inventoryViewModel = appContainer.inventoryViewModel
                        )
                        Screen.SUPPLIERS -> SuppliersScreen(
                            supplierViewModel = appContainer.supplierViewModel
                        )
                        Screen.RETURNS -> ReturnsScreen() // TODO: Replace with ViewModel
                        Screen.PROMOTIONS -> PromotionsScreen(promotionViewModel = appContainer.promotionViewModel)
                        Screen.REPORTS -> ReportsScreen(
                            reportsViewModel = appContainer.reportsViewModel
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            onNavigateToUpdates = { currentScreen = Screen.UPDATE }
                        )
                        Screen.UPDATE -> UpdateScreen(
                            updateRepository = appContainer.updateRepository,
                            updateService = appContainer.updateService,
                            notificationService = appContainer.notificationService
                        )
                    }
                }
            }

            // Global notification overlay
            NotificationOverlay(
                notificationService = appContainer.notificationService
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

        // User info and logout - Always show logout when authenticated
        val currentUser = authService.getCurrentUser()
        val authState by authService.authState.collectAsState()

        // Enhanced debug logging
        val displayName = getDisplayName(currentUser ?: authState.user)
        println("🔍 NavigationSidebar - Auth State: isAuthenticated=${authState.isAuthenticated}")
        println("🔍 NavigationSidebar - AuthState User: ${authState.user}")
        println("🔍 NavigationSidebar - AuthState User Username: ${authState.user?.username}")
        println("🔍 NavigationSidebar - AuthState User FirstName: ${authState.user?.firstName}")
        println("🔍 NavigationSidebar - AuthState User LastName: ${authState.user?.lastName}")
        println("🔍 NavigationSidebar - Current User: ${currentUser}")
        println("🔍 NavigationSidebar - Current User Username: ${currentUser?.username}")
        println("🔍 NavigationSidebar - Current User FirstName: ${currentUser?.firstName}")
        println("🔍 NavigationSidebar - Current User LastName: ${currentUser?.lastName}")
        println("🔍 NavigationSidebar - Display Name: $displayName")

        // Get the best available user data (prioritize currentUser, fallback to authState.user)
        val bestUser = currentUser ?: authState.user

        // Show user card if we have user info, otherwise show minimal logout section
        if (bestUser != null) {
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

                    // Dynamic username display with fallback
                    Text(
                        text = getDisplayName(bestUser),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    // Show role if available
                    if (!bestUser.role.isNullOrBlank()) {
                        Text(
                            text = bestUser.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Logout button
                    LogoutButton(authService)
                }
            }
        } else {
            // Fallback: Always show logout section when in main app (user must be authenticated to reach here)
            // Try to get user from authState as additional fallback
            val fallbackUser = authState.user

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

                    // Dynamic username display with fallback (try authState.user as well)
                    Text(
                        text = getDisplayName(fallbackUser),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    // Show role if available from fallback user
                    if (!fallbackUser?.role.isNullOrBlank()) {
                        Text(
                            text = fallbackUser!!.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Logout button - Always visible as fallback
                    LogoutButton(authService)
                }
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

        // أزرار التحكم على اليمين (البداية في RTL)
        Row(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // زر الإغلاق
            TitleBarButton(
                onClick = onClose,
                icon = Icons.Default.Close,
                contentDescription = "إغلاق",
                isCloseButton = true
            )
            // زر التكبير
            TitleBarButton(
                onClick = onMaximize,
                icon = Icons.Default.CropSquare,
                contentDescription = "تكبير",
                isCloseButton = false
            )
            // زر التصغير
            TitleBarButton(
                onClick = onMinimize,
                icon = Icons.Default.Minimize,
                contentDescription = "تصغير",
                isCloseButton = false
            )
        }
    }
}

@Composable
fun TitleBarButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    isCloseButton: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = when {
                    isCloseButton && isHovered -> Color(0xFFE81123) // Windows-style red for close button
                    isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                    else -> Color.Transparent
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = when {
                isCloseButton && isHovered -> Color.White
                isCloseButton -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Helper function to get display name with fallback hierarchy
 * Priority: username -> firstName lastName -> firstName -> lastName -> static text
 */
private fun getDisplayName(user: data.auth.UserDTO?): String {
    println("🔍 getDisplayName called with user: $user")

    return when {
        // First priority: username (raw value without prefix)
        !user?.username.isNullOrBlank() -> {
            val displayName = user!!.username
            println("🔍 Using username: $displayName")
            displayName
        }
        // Second priority: first and last name
        !user?.firstName.isNullOrBlank() && !user?.lastName.isNullOrBlank() -> {
            val displayName = "${user!!.firstName} ${user.lastName}"
            println("🔍 Using full name: $displayName")
            displayName
        }
        // Third priority: first name only
        !user?.firstName.isNullOrBlank() -> {
            val displayName = user!!.firstName
            println("🔍 Using first name: $displayName")
            displayName
        }
        // Fourth priority: last name only
        !user?.lastName.isNullOrBlank() -> {
            val displayName = user!!.lastName
            println("🔍 Using last name: $displayName")
            displayName
        }
        // Last resort: static Arabic text
        else -> {
            println("🔍 Using fallback text: مستخدم مسجل")
            "مستخدم مسجل"
        }
    }
}

@Composable
private fun LogoutButton(authService: data.auth.AuthService) {
    val coroutineScope = rememberCoroutineScope()
    val logoutInteractionSource = remember { MutableInteractionSource() }
    val isLogoutHovered by logoutInteractionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isLogoutHovered) 2.dp else 1.dp,
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isLogoutHovered)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = logoutInteractionSource,
                indication = null
            ) {
                coroutineScope.launch {
                    println("🚪 Logout button clicked")
                    authService.logout()
                }
            }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = "تسجيل الخروج",
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Text(
                "تسجيل الخروج",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
