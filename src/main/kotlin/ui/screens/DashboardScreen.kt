@file:OptIn(ExperimentalAnimationApi::class)

package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.utils.ResponsiveUtils
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    dashboardViewModel: ui.viewmodels.DashboardViewModel,
    onNavigateToSales: () -> Unit = {},
    onNavigateToProducts: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Currency formatter for Arabic locale
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
            currency = Currency.getInstance("SAR")
        }
    }

    // Load data when screen is first displayed
    LaunchedEffect(Unit) {
        if (!uiState.hasData && !uiState.isLoading) {
            dashboardViewModel.loadDashboardData()
        }
    }

    RTLProvider {

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when {
                uiState.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "جاري تحميل بيانات لوحة التحكم...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                uiState.hasError -> {
                    // Error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "خطأ في تحميل البيانات",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = uiState.error ?: "حدث خطأ غير معروف",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            val retryInteractionSource = remember { MutableInteractionSource() }
                            val isRetryHovered by retryInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        color = if (isRetryHovered)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                        else
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(
                                        interactionSource = retryInteractionSource,
                                        indication = null
                                    ) { dashboardViewModel.refreshData() }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        "إعادة المحاولة",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
                uiState.hasData -> {
                    // Success state with data
                    DashboardContent(
                        dashboardSummary = uiState.dashboardSummary!!,
                        currencyFormatter = currencyFormatter,
                        onRefresh = { dashboardViewModel.refreshData() },
                        onNavigateToSales = onNavigateToSales,
                        onNavigateToProducts = onNavigateToProducts,
                        onNavigateToCustomers = onNavigateToCustomers,
                        onNavigateToInventory = onNavigateToInventory,
                        onNavigateToReports = onNavigateToReports
                    )
                }
                else -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد بيانات متاحة",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    dashboardSummary: data.api.DashboardSummaryDTO,
    currencyFormatter: NumberFormat,
    onRefresh: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    RTLRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Panel - Statistics and Overview
        Card(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header with improved styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "لوحة التحكم",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "نظرة عامة على أداء المبيعات - ${dashboardSummary.period ?: "آخر 30 يوم"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Quick refresh button with enhanced hover effects
                    val refreshInteractionSource = remember { MutableInteractionSource() }
                    val isRefreshHovered by refreshInteractionSource.collectIsHoveredAsState()

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                color = if (isRefreshHovered)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                                else
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(
                                interactionSource = refreshInteractionSource,
                                indication = null
                            ) { onRefresh() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "تحديث البيانات",
                            modifier = Modifier.size(24.dp),
                            tint = if (isRefreshHovered)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }

                // Sales Stats Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.height(400.dp)
                ) {
                    item {
                        ModernStatCard(
                            title = "إجمالي المبيعات",
                            value = currencyFormatter.format(dashboardSummary.sales?.totalRevenue ?: 0.0),
                            subtitle = "${dashboardSummary.sales?.totalSales ?: 0} معاملة",
                            icon = Icons.Default.AttachMoney,
                            iconColor = AppTheme.colors.success,
                            trend = "+${String.format("%.1f", dashboardSummary.sales?.growthRate ?: 0.0)}%"
                        )
                    }
                    item {
                        ModernStatCard(
                            title = "متوسط قيمة الطلب",
                            value = currencyFormatter.format(dashboardSummary.sales?.averageOrderValue ?: 0.0),
                            subtitle = "لكل معاملة",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconColor = AppTheme.colors.info,
                            trend = "+${String.format("%.1f", dashboardSummary.sales?.growthRate ?: 0.0)}%"
                        )
                    }
                    item {
                        ModernStatCard(
                            title = "إجمالي العملاء",
                            value = (dashboardSummary.customers?.totalCustomers ?: 0).toString(),
                            subtitle = "${dashboardSummary.customers?.newCustomers ?: 0} عميل جديد",
                            icon = Icons.Default.People,
                            iconColor = MaterialTheme.colorScheme.primary,
                            trend = "+${String.format("%.1f", dashboardSummary.customers?.retentionRate ?: 0.0)}%"
                        )
                    }
                    item {
                        val lowStockAlerts = dashboardSummary.inventory?.lowStockAlerts ?: 0
                        val outOfStockProducts = dashboardSummary.inventory?.outOfStockProducts ?: 0

                        ModernStatCard(
                            title = "المخزون",
                            value = (dashboardSummary.inventory?.totalProducts ?: 0).toString(),
                            subtitle = "$lowStockAlerts تنبيه مخزون",
                            icon = Icons.Default.Inventory,
                            iconColor = if (lowStockAlerts > 0)
                                AppTheme.colors.warning else AppTheme.colors.success,
                            trend = if (outOfStockProducts > 0)
                                "-$outOfStockProducts" else "✓"
                        )
                    }
                }

                // Revenue and Performance Section
                Text(
                    text = "الأداء المالي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الإيرادات الشهرية",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )

                            // Calculate current month revenue or total from the map
                            val monthlyRevenueValue = dashboardSummary.revenue?.monthlyRevenue?.values?.sum() ?: 0.0

                            Text(
                                text = currencyFormatter.format(monthlyRevenueValue),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "هامش الربح",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${String.format("%.1f", dashboardSummary.revenue?.profitMargin ?: 0.0)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppTheme.colors.success
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "أفضل فئة",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = dashboardSummary.revenue?.topCategory ?: "غير محدد",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Right Panel - Quick Actions and Alerts
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Quick Actions Section
                Text(
                    text = "إجراءات سريعة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ModernQuickActionButton(
                    text = "بيع جديد",
                    icon = Icons.Default.Add,
                    onClick = onNavigateToSales,
                    modifier = Modifier.fillMaxWidth()
                )

                ModernQuickActionButton(
                    text = "إضافة منتج",
                    icon = Icons.Default.Inventory,
                    onClick = onNavigateToProducts,
                    modifier = Modifier.fillMaxWidth()
                )

                ModernQuickActionButton(
                    text = "إضافة عميل",
                    icon = Icons.Default.PersonAdd,
                    onClick = onNavigateToCustomers,
                    modifier = Modifier.fillMaxWidth()
                )

                ModernQuickActionButton(
                    text = "إدارة المخزون",
                    icon = Icons.Default.Warehouse,
                    onClick = onNavigateToInventory,
                    modifier = Modifier.fillMaxWidth()
                )

                ModernQuickActionButton(
                    text = "التقارير والتحليلات",
                    icon = Icons.Default.Analytics,
                    onClick = onNavigateToReports,
                    modifier = Modifier.fillMaxWidth()
                )

                // Inventory Alerts Section
                val lowStockAlerts = dashboardSummary.inventory?.lowStockAlerts ?: 0
                if (lowStockAlerts > 0) {
                    Text(
                        text = "تنبيهات المخزون",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.warning.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AppTheme.colors.warning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "مخزون منخفض",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "$lowStockAlerts منتج يحتاج إعادة تخزين",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Out of Stock Alert
                val outOfStockProducts = dashboardSummary.inventory?.outOfStockProducts ?: 0
                if (outOfStockProducts > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "نفاد المخزون",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "$outOfStockProducts منتج غير متوفر",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Modern Component Functions
@Composable
private fun ModernStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    trend: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* No action, just for hover effect */ },
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isHovered) 1.5.dp else 1.dp,
            color = if (isHovered)
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )

                Surface(
                    color = AppTheme.colors.success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.colors.success,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModernQuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isHovered)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                else
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isHovered)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ModernProductCard(
    product: ProductStats,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${product.totalSold} قطعة مباعة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = currencyFormatter.format(product.revenue),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ModernSaleCard(
    sale: Sale,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sale.customer?.name ?: "عميل غير محدد",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = currencyFormatter.format(sale.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${sale.items.size} منتج",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = sale.paymentMethod.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModernLowStockCard(
    product: Product,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.warning.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = AppTheme.colors.warning.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "متبقي ${product.stock} قطعة",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.warning
                )
            }

            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = AppTheme.colors.warning,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
