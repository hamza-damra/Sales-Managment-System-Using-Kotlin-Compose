@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

package ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.viewmodels.ReportsViewModel
import ui.viewmodels.DateRange
import data.api.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.text.NumberFormat
import java.util.*

/**
 * Comprehensive Reports Screen with enterprise-level analytics
 * Follows existing UI/UX patterns and integrates with backend reporting APIs
 */
@Composable
fun ReportsScreen(
    reportsViewModel: ReportsViewModel
) {
    // Collect state from ViewModel
    val selectedReportType by reportsViewModel.selectedReportType.collectAsState()
    val selectedDateRange by reportsViewModel.selectedDateRange.collectAsState()
    val isLoading by reportsViewModel.isLoading.collectAsState()
    val error by reportsViewModel.error.collectAsState()
    val isRefreshing by reportsViewModel.isRefreshing.collectAsState()
    
    // Report data
    val comprehensiveSalesReport by reportsViewModel.comprehensiveSalesReport.collectAsState()
    val customerReport by reportsViewModel.customerReport.collectAsState()
    val productReport by reportsViewModel.productReport.collectAsState()
    val inventoryReport by reportsViewModel.inventoryReport.collectAsState()
    val financialReport by reportsViewModel.financialReport.collectAsState()
    val promotionReport by reportsViewModel.promotionReport.collectAsState()
    val realTimeKPIs by reportsViewModel.realTimeKPIs.collectAsState()
    
    // UI state
    var showDatePicker by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showFiltersDialog by remember { mutableStateOf(false) }
    var selectedCustomStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedCustomEndDate by remember { mutableStateOf<LocalDate?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val currencyFormatter = remember { 
        NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
            currency = Currency.getInstance("SAR")
        }
    }

    RTLProvider {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(28.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Enhanced Header Section
                item {
                    EnhancedReportsHeader(
                        selectedDateRange = selectedDateRange,
                        onDateRangeSelected = { range ->
                            reportsViewModel.selectDateRange(range)
                        },
                        onCustomDateRangeClick = { showDatePicker = true },
                        onRefresh = { reportsViewModel.refreshAllReports() },
                        onExport = { showExportDialog = true },
                        onFilters = { showFiltersDialog = true },
                        isLoading = isLoading,
                        isRefreshing = isRefreshing
                    )
                }

                // Report Type Selector
                item {
                    EnhancedReportTypeSelector(
                        selectedType = selectedReportType,
                        onTypeSelected = { reportsViewModel.selectReportType(it) }
                    )
                }

                // Real-time KPIs Dashboard
                item {
                    RealTimeKPIsDashboard(
                        kpis = realTimeKPIs,
                        currencyFormatter = currencyFormatter,
                        isLoading = isLoading
                    )
                }

                // Main Report Content
                item {
                    ReportContentSection(
                        reportType = selectedReportType,
                        salesReport = comprehensiveSalesReport,
                        customerReport = customerReport,
                        productReport = productReport,
                        inventoryReport = inventoryReport,
                        financialReport = financialReport,
                        promotionReport = promotionReport,
                        currencyFormatter = currencyFormatter,
                        isLoading = isLoading,
                        error = error,
                        onRetry = { reportsViewModel.refreshCurrentReport() }
                    )
                }
            }
            
            // Error handling
            error?.let { errorMessage ->
                LaunchedEffect(errorMessage) {
                    // Show error notification
                    // You can integrate with your notification service here
                }
            }
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            CustomDateRangeDialog(
                startDate = selectedCustomStartDate,
                endDate = selectedCustomEndDate,
                onStartDateSelected = { selectedCustomStartDate = it },
                onEndDateSelected = { selectedCustomEndDate = it },
                onConfirm = { start, end ->
                    reportsViewModel.setCustomDateRange(start, end)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
        
        // Export Dialog
        if (showExportDialog) {
            ExportReportDialog(
                onExport = { format ->
                    coroutineScope.launch {
                        val result = reportsViewModel.exportReport(format)
                        // Handle export result
                        showExportDialog = false
                    }
                },
                onDismiss = { showExportDialog = false }
            )
        }
        
        // Filters Dialog
        if (showFiltersDialog) {
            ReportFiltersDialog(
                currentFilters = reportsViewModel.selectedFilters.collectAsState().value,
                onFiltersApplied = { filters ->
                    reportsViewModel.updateFilters(filters)
                    showFiltersDialog = false
                },
                onDismiss = { showFiltersDialog = false }
            )
        }
    }
}

// Enhanced Header Component
@Composable
private fun EnhancedReportsHeader(
    selectedDateRange: DateRange,
    onDateRangeSelected: (DateRange) -> Unit,
    onCustomDateRangeClick: () -> Unit,
    onRefresh: () -> Unit,
    onExport: () -> Unit,
    onFilters: () -> Unit,
    isLoading: Boolean,
    isRefreshing: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title and actions
        Row(
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
                    text = "تحليل شامل وتفصيلي لأداء الأعمال",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Refresh button
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Export button
                IconButton(onClick = onExport) {
                    Icon(
                        Icons.Default.FileDownload,
                        contentDescription = "تصدير",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Filters button
                IconButton(onClick = onFilters) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "فلاتر",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Date range selector
        DateRangeSelector(
            selectedRange = selectedDateRange,
            onRangeSelected = onDateRangeSelected,
            onCustomRangeClick = onCustomDateRangeClick
        )
    }
}

// Date Range Selector Component
@Composable
private fun DateRangeSelector(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit,
    onCustomRangeClick: () -> Unit
) {
    Card(
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.defaultCardElevation()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "فترة التقرير",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DateRange.values()) { range ->
                    DateRangeChip(
                        range = range,
                        isSelected = selectedRange == range,
                        onClick = {
                            if (range == DateRange.CUSTOM) {
                                onCustomRangeClick()
                            } else {
                                onRangeSelected(range)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateRangeChip(
    range: DateRange,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val text = when (range) {
        DateRange.TODAY -> "اليوم"
        DateRange.YESTERDAY -> "أمس"
        DateRange.LAST_7_DAYS -> "آخر 7 أيام"
        DateRange.LAST_30_DAYS -> "آخر 30 يوم"
        DateRange.LAST_90_DAYS -> "آخر 90 يوم"
        DateRange.THIS_MONTH -> "هذا الشهر"
        DateRange.LAST_MONTH -> "الشهر الماضي"
        DateRange.THIS_YEAR -> "هذا العام"
        DateRange.CUSTOM -> "فترة مخصصة"
    }

    FilterChip(
        onClick = onClick,
        label = { Text(text) },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
        )
    )
}

// Report Type Selector Component
@Composable
private fun EnhancedReportTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Card(
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.defaultCardElevation()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "نوع التقرير",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ReportTypeCard(
                        title = "تقرير المبيعات",
                        description = "تحليل شامل للمبيعات والإيرادات",
                        icon = Icons.Default.TrendingUp,
                        color = MaterialTheme.colorScheme.primary,
                        isSelected = selectedType == "sales",
                        onClick = { onTypeSelected("sales") }
                    )
                }
                item {
                    ReportTypeCard(
                        title = "تقرير العملاء",
                        description = "تحليل سلوك العملاء والقيمة الدائمة",
                        icon = Icons.Default.People,
                        color = AppTheme.colors.info,
                        isSelected = selectedType == "customers",
                        onClick = { onTypeSelected("customers") }
                    )
                }
                item {
                    ReportTypeCard(
                        title = "تقرير المنتجات",
                        description = "أداء المنتجات والربحية",
                        icon = Icons.Default.Inventory,
                        color = AppTheme.colors.success,
                        isSelected = selectedType == "products",
                        onClick = { onTypeSelected("products") }
                    )
                }
                item {
                    ReportTypeCard(
                        title = "تقرير المخزون",
                        description = "حالة المخزون والتقييم",
                        icon = Icons.Default.Warehouse,
                        color = AppTheme.colors.warning,
                        isSelected = selectedType == "inventory",
                        onClick = { onTypeSelected("inventory") }
                    )
                }
                item {
                    ReportTypeCard(
                        title = "التقرير المالي",
                        description = "التحليل المالي والربحية",
                        icon = Icons.Default.AccountBalance,
                        color = AppTheme.colors.purple,
                        isSelected = selectedType == "financial",
                        onClick = { onTypeSelected("financial") }
                    )
                }
                item {
                    ReportTypeCard(
                        title = "تقرير العروض",
                        description = "فعالية العروض والخصومات",
                        icon = Icons.Default.LocalOffer,
                        color = AppTheme.colors.pink,
                        isSelected = selectedType == "promotions",
                        onClick = { onTypeSelected("promotions") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportTypeCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(120.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, color) else null,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.02f),
                                color.copy(alpha = 0.08f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = color
                        )
                    }
                }
            }
        }
    }
}

// Real-time KPIs Dashboard
@Composable
private fun RealTimeKPIsDashboard(
    kpis: Map<String, Any>?,
    currencyFormatter: NumberFormat,
    isLoading: Boolean
) {
    Card(
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المؤشرات الرئيسية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            if (kpis != null) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        KPICard(
                            title = "مبيعات اليوم",
                            value = currencyFormatter.format(kpis["todaysRevenue"] as? Double ?: 0.0),
                            subtitle = "${kpis["todaysSales"] as? Int ?: 0} عملية بيع",
                            icon = Icons.Default.TrendingUp,
                            color = AppTheme.colors.success
                        )
                    }
                    item {
                        KPICard(
                            title = "العملاء النشطون",
                            value = "${kpis["activeCustomers"] as? Int ?: 0}",
                            subtitle = "عميل نشط",
                            icon = Icons.Default.People,
                            color = AppTheme.colors.info
                        )
                    }
                    item {
                        KPICard(
                            title = "قيمة المخزون",
                            value = currencyFormatter.format(kpis["inventoryValue"] as? Double ?: 0.0),
                            subtitle = "${kpis["lowStockItems"] as? Int ?: 0} منتج ناقص",
                            icon = Icons.Default.Inventory,
                            color = AppTheme.colors.warning
                        )
                    }
                    item {
                        KPICard(
                            title = "المرتجعات المعلقة",
                            value = "${kpis["pendingReturns"] as? Int ?: 0}",
                            subtitle = "مرتجع معلق",
                            icon = Icons.Default.AssignmentReturn,
                            color = AppTheme.colors.error
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(4) {
                        KPICardSkeleton(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun KPICard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.02f),
                                color.copy(alpha = 0.08f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = color
                        )
                    }
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun KPICardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
            }

            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
        }
    }
}

// Report Content Section
@Composable
private fun ReportContentSection(
    reportType: String,
    salesReport: ComprehensiveSalesReportDTO?,
    customerReport: CustomerReportDTO?,
    productReport: ProductReportDTO?,
    inventoryReport: EnhancedInventoryReportDTO?,
    financialReport: FinancialReportDTO?,
    promotionReport: PromotionReportDTO?,
    currencyFormatter: NumberFormat,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    Card(
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardStyles.defaultCardElevation()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = getReportTitle(reportType),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            when {
                isLoading -> {
                    ReportLoadingSkeleton()
                }
                error != null -> {
                    ReportErrorState(
                        error = error,
                        onRetry = onRetry
                    )
                }
                else -> {
                    when (reportType) {
                        "sales" -> SalesReportContent(salesReport, currencyFormatter)
                        "customers" -> CustomerReportContent(customerReport, currencyFormatter)
                        "products" -> ProductReportContent(productReport, currencyFormatter)
                        "inventory" -> InventoryReportContent(inventoryReport, currencyFormatter)
                        "financial" -> FinancialReportContent(financialReport, currencyFormatter)
                        "promotions" -> PromotionReportContent(promotionReport, currencyFormatter)
                        else -> EmptyReportState()
                    }
                }
            }
        }
    }
}

private fun getReportTitle(reportType: String): String {
    return when (reportType) {
        "sales" -> "تقرير المبيعات الشامل"
        "customers" -> "تقرير تحليل العملاء"
        "products" -> "تقرير أداء المنتجات"
        "inventory" -> "تقرير حالة المخزون"
        "financial" -> "التقرير المالي"
        "promotions" -> "تقرير فعالية العروض"
        else -> "التقرير"
    }
}

@Composable
private fun ReportLoadingSkeleton() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary cards skeleton
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(3) {
                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(20.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }

        // Chart skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun ReportErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = AppTheme.colors.error
        )

        Text(
            text = "حدث خطأ في تحميل التقرير",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("إعادة المحاولة")
        }
    }
}

@Composable
private fun EmptyReportState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Default.Analytics,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "لا توجد بيانات للعرض",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = "اختر فترة زمنية مختلفة أو تحقق من الفلاتر المطبقة",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Sales Report Content
@Composable
private fun SalesReportContent(
    salesReport: ComprehensiveSalesReportDTO?,
    currencyFormatter: NumberFormat
) {
    if (salesReport == null) {
        EmptyReportState()
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Summary cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MetricCard(
                    title = "إجمالي المبيعات",
                    value = "${salesReport.summary.totalSales}",
                    change = salesReport.summary.salesGrowth?.let { "+${String.format("%.1f", it)}%" } ?: "N/A",
                    isPositiveChange = salesReport.summary.salesGrowth?.let { it >= 0 } ?: true,
                    icon = Icons.Default.ShoppingCart
                )
            }
            item {
                MetricCard(
                    title = "إجمالي الإيرادات",
                    value = currencyFormatter.format(salesReport.summary.totalRevenue),
                    change = salesReport.summary.revenueGrowth?.let { "+${String.format("%.1f", it)}%" } ?: "N/A",
                    isPositiveChange = salesReport.summary.revenueGrowth?.let { it >= 0 } ?: true,
                    icon = Icons.Default.TrendingUp
                )
            }
            item {
                MetricCard(
                    title = "متوسط قيمة الطلب",
                    value = currencyFormatter.format(salesReport.summary.averageOrderValue),
                    change = null,
                    icon = Icons.Default.Receipt
                )
            }
            item {
                MetricCard(
                    title = "العملاء الفريدون",
                    value = "${salesReport.summary.uniqueCustomers ?: "N/A"}",
                    change = null,
                    icon = Icons.Default.People
                )
            }
        }

        // Top customers and products
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top customers
            Card(
                modifier = Modifier.weight(1f),
                colors = CardStyles.defaultCardColors()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "أفضل العملاء",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    salesReport.topCustomers.take(5).forEach { customer ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = customer.customerName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${customer.totalOrders} طلب",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = currencyFormatter.format(customer.totalSpent),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.success
                            )
                        }
                    }
                }
            }

            // Top products
            Card(
                modifier = Modifier.weight(1f),
                colors = CardStyles.defaultCardColors()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "أفضل المنتجات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    salesReport.topProducts.take(5).forEach { product ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = product.productName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${product.quantitySold} قطعة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = currencyFormatter.format(product.revenue),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.success
                            )
                        }
                    }
                }
            }
        }
    }
}

// Customer Report Content
@Composable
private fun CustomerReportContent(
    customerReport: CustomerReportDTO?,
    currencyFormatter: NumberFormat
) {
    if (customerReport == null) {
        EmptyReportState()
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Customer summary
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MetricCard(
                    title = "إجمالي العملاء",
                    value = "${customerReport.summary.totalCustomers}",
                    icon = Icons.Default.People
                )
            }
            item {
                MetricCard(
                    title = "العملاء النشطون",
                    value = "${customerReport.summary.activeCustomers}",
                    icon = Icons.Default.PersonAdd
                )
            }
            item {
                MetricCard(
                    title = "متوسط قيمة العميل",
                    value = currencyFormatter.format(customerReport.summary.averageCustomerValue),
                    icon = Icons.Default.AttachMoney
                )
            }
            item {
                MetricCard(
                    title = "معدل الاحتفاظ",
                    value = "${String.format("%.1f", customerReport.summary.customerRetentionRate)}%",
                    icon = Icons.Default.TrendingUp
                )
            }
        }

        // Customer segments
        Card(
            colors = CardStyles.defaultCardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "شرائح العملاء",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                customerReport.segments.forEach { segment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = segment.segmentName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${segment.customerCount} عميل (${String.format("%.1f", segment.percentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = currencyFormatter.format(segment.totalRevenue),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.success
                        )
                    }
                }
            }
        }
    }
}

// Placeholder implementations for other report types
@Composable
private fun ProductReportContent(
    productReport: ProductReportDTO?,
    currencyFormatter: NumberFormat
) {
    if (productReport == null) {
        EmptyReportState()
        return
    }

    Text(
        text = "تقرير المنتجات قيد التطوير",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun InventoryReportContent(
    inventoryReport: EnhancedInventoryReportDTO?,
    currencyFormatter: NumberFormat
) {
    if (inventoryReport == null) {
        EmptyReportState()
        return
    }

    Text(
        text = "تقرير المخزون قيد التطوير",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FinancialReportContent(
    financialReport: FinancialReportDTO?,
    currencyFormatter: NumberFormat
) {
    if (financialReport == null) {
        EmptyReportState()
        return
    }

    Text(
        text = "التقرير المالي قيد التطوير",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PromotionReportContent(
    promotionReport: PromotionReportDTO?,
    currencyFormatter: NumberFormat
) {
    if (promotionReport == null) {
        EmptyReportState()
        return
    }

    Text(
        text = "تقرير العروض قيد التطوير",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// Dialog Components
@Composable
private fun CustomDateRangeDialog(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختيار فترة مخصصة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "اختر تاريخ البداية والنهاية للتقرير",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Date selection UI would go here
                // For now, showing placeholder
                Text(
                    text = "منتقي التاريخ قيد التطوير",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val start = startDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(30, DateTimeUnit.DAY)
                    val end = endDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    onConfirm(start, end)
                }
            ) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun ExportReportDialog(
    onExport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تصدير التقرير",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "اختر تنسيق التصدير:",
                    style = MaterialTheme.typography.bodyMedium
                )

                val exportFormats = listOf(
                    "PDF" to Icons.Default.PictureAsPdf,
                    "Excel" to Icons.Default.TableChart,
                    "CSV" to Icons.Default.Description
                )

                exportFormats.forEach { (format, icon) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExport(format) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = format,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun ReportFiltersDialog(
    currentFilters: ui.viewmodels.ReportFilters,
    onFiltersApplied: (ui.viewmodels.ReportFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var filters by remember { mutableStateOf(currentFilters) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "فلاتر التقرير",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "تخصيص فلاتر التقرير:",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Include inactive items toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تضمين العناصر غير النشطة",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = filters.includeInactive,
                        onCheckedChange = { filters = filters.copy(includeInactive = it) }
                    )
                }

                // Placeholder for other filter options
                Text(
                    text = "فلاتر إضافية قيد التطوير",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onFiltersApplied(filters) }
            ) {
                Text("تطبيق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

// Metric Card Component
@Composable
private fun MetricCard(
    title: String,
    value: String,
    change: String? = null,
    isPositiveChange: Boolean = true,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(100.dp),
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.defaultCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            change?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPositiveChange) AppTheme.colors.success else AppTheme.colors.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
