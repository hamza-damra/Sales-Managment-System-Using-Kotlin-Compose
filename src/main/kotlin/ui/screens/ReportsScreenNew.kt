package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.repository.ReportsRepository
import kotlinx.coroutines.launch
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import java.text.NumberFormat
import java.util.*

@Composable
fun ReportsScreenNew(reportsRepository: ReportsRepository) {
    val dashboardSummary by reportsRepository.dashboardSummary.collectAsState()
    val isLoading by reportsRepository.isLoading.collectAsState()
    val error by reportsRepository.error.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    
    var selectedReportType by remember { mutableStateOf("dashboard") }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
            currency = Currency.getInstance("SAR")
        }
    }

    // Load dashboard data on first composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            reportsRepository.loadDashboardSummary()
        }
    }

    RTLProvider {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "التقارير والإحصائيات",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "تحليل شامل لأداء المبيعات",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            when (selectedReportType) {
                                "dashboard" -> reportsRepository.loadDashboardSummary()
                                // Add other report types as needed
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Report type selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    ReportTypeCard(
                        title = "لوحة التحكم",
                        description = "نظرة عامة سريعة",
                        icon = Icons.Default.Dashboard,
                        color = MaterialTheme.colorScheme.primary,
                        isSelected = selectedReportType == "dashboard",
                        onClick = { selectedReportType = "dashboard" }
                    )
                }
                item {
                    ReportTypeCard(
                        title = "تقرير المبيعات",
                        description = "تحليل المبيعات التفصيلي",
                        icon = Icons.Default.TrendingUp,
                        color = AppTheme.colors.success,
                        isSelected = selectedReportType == "sales",
                        onClick = { selectedReportType = "sales" }
                    )
                }
                item {
                    ReportTypeCard(
                        title = "تقرير المخزون",
                        description = "حالة المخزون والمنتجات",
                        icon = Icons.Default.Inventory,
                        color = AppTheme.colors.info,
                        isSelected = selectedReportType == "inventory",
                        onClick = { selectedReportType = "inventory" }
                    )
                }
                item {
                    ReportTypeCard(
                        title = "تقرير العملاء",
                        description = "إحصائيات وتحليل العملاء",
                        icon = Icons.Default.People,
                        color = AppTheme.colors.warning,
                        isSelected = selectedReportType == "customers",
                        onClick = { selectedReportType = "customers" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading State
            if (isLoading && dashboardSummary == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@RTLProvider
            }

            // Error State
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "خطأ في تحميل التقارير",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    reportsRepository.loadDashboardSummary()
                                }
                            }
                        ) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
                return@RTLProvider
            }

            // Report Content
            when (selectedReportType) {
                "dashboard" -> DashboardReportContent(dashboardSummary, currencyFormatter)
                "sales" -> SalesReportContent()
                "inventory" -> InventoryReportContent()
                "customers" -> CustomersReportContent()
            }
        }
    }
}

@Composable
fun ReportTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(200.dp),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
        } else {
            CardStyles.defaultCardColors()
        },
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.defaultCardElevation(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun DashboardReportContent(
    dashboardSummary: data.api.DashboardSummaryDTO?,
    currencyFormatter: NumberFormat
) {
    dashboardSummary?.let { summary ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Key Performance Indicators
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardStyles.defaultCardColors(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardStyles.defaultCardElevation()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "مؤشرات الأداء الرئيسية",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                KPICard(
                                    title = "إجمالي الإيرادات",
                                    value = currencyFormatter.format(summary.sales.totalRevenue),
                                    icon = Icons.Default.AttachMoney,
                                    color = AppTheme.colors.success
                                )
                            }
                            item {
                                KPICard(
                                    title = "عدد المبيعات",
                                    value = summary.sales.totalSales.toString(),
                                    icon = Icons.Default.ShoppingCart,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            item {
                                KPICard(
                                    title = "متوسط الفاتورة",
                                    value = currencyFormatter.format(
                                        if (summary.sales.totalSales > 0)
                                            summary.sales.totalRevenue / summary.sales.totalSales
                                        else 0.0
                                    ),
                                    icon = Icons.Default.Analytics,
                                    color = AppTheme.colors.info
                                )
                            }
                        }
                    }
                }
            }

            // Top Products
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardStyles.defaultCardColors(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardStyles.defaultCardElevation()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "أفضل المنتجات مبيعاً",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "لا توجد بيانات تفصيلية للمنتجات في هذا التقرير",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "استخدم تقرير المنتجات للحصول على تفاصيل أكثر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    } ?: run {
        EmptyState(
            icon = Icons.Default.Assessment,
            title = "لا توجد بيانات",
            description = "لا توجد بيانات تقارير متاحة حالياً"
        )
    }
}

@Composable
fun KPICard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TopProductItem(
    productName: String,
    salesCount: Int,
    revenue: Double,
    currencyFormatter: NumberFormat
) {
    RTLRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$salesCount مبيعة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Text(
            text = currencyFormatter.format(revenue),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.success
        )
    }
}

// Placeholder components for other report types
@Composable
fun SalesReportContent() {
    EmptyState(
        icon = Icons.Default.TrendingUp,
        title = "تقرير المبيعات",
        description = "سيتم تنفيذ تقرير المبيعات التفصيلي قريباً"
    )
}

@Composable
fun InventoryReportContent() {
    EmptyState(
        icon = Icons.Default.Inventory,
        title = "تقرير المخزون",
        description = "سيتم تنفيذ تقرير المخزون قريباً"
    )
}

@Composable
fun CustomersReportContent() {
    EmptyState(
        icon = Icons.Default.People,
        title = "تقرير العملاء",
        description = "سيتم تنفيذ تقرير العملاء قريباً"
    )
}
