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
import androidx.compose.material.icons.outlined.Lightbulb
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
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
    val realTimeKPIs: JsonElement? by reportsViewModel.realTimeKPIs.collectAsState()
    
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
    kpis: JsonElement?,
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
                            value = currencyFormatter.format(getKPIDouble(kpis, "todaysRevenue")),
                            subtitle = "${getKPILong(kpis, "todaysSales")} عملية بيع",
                            icon = Icons.Default.TrendingUp,
                            color = AppTheme.colors.success
                        )
                    }
                    item {
                        KPICard(
                            title = "العملاء النشطون",
                            value = "${getKPILong(kpis, "activeCustomers")}",
                            subtitle = "عميل نشط",
                            icon = Icons.Default.People,
                            color = AppTheme.colors.info
                        )
                    }
                    item {
                        KPICard(
                            title = "قيمة المخزون",
                            value = currencyFormatter.format(getKPIDouble(kpis, "inventoryValue")),
                            subtitle = "${getKPILong(kpis, "lowStockItems")} منتج ناقص",
                            icon = Icons.Default.Inventory,
                            color = AppTheme.colors.warning
                        )
                    }
                    item {
                        KPICard(
                            title = "المرتجعات المعلقة",
                            value = "${getKPILong(kpis, "pendingReturns")}",
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

// Customer Report Content - Comprehensive Analytics
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Customer Segmentation Overview
        customerReport.customerSegmentation?.let { segmentation ->
            CustomerSegmentationSection(segmentation, currencyFormatter)
        }

        // Acquisition Metrics
        customerReport.acquisitionMetrics?.let { acquisition ->
            CustomerAcquisitionSection(acquisition, currencyFormatter)
        }

        // Lifetime Value Analysis
        customerReport.lifetimeValueAnalysis?.let { ltv ->
            CustomerLifetimeValueSection(ltv, currencyFormatter)
        }

        // Churn Analysis
        customerReport.churnAnalysis?.let { churn ->
            CustomerChurnSection(churn, currencyFormatter)
        }

        // Behavior Analysis
        customerReport.behaviorAnalysis?.let { behavior ->
            CustomerBehaviorSection(behavior, currencyFormatter)
        }
    }
}

@Composable
private fun CustomerSegmentationSection(
    segmentation: CustomerSegmentationData,
    currencyFormatter: NumberFormat
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
                text = "تقسيم العملاء",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Summary metrics
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MetricCard(
                        title = "إجمالي العملاء",
                        value = "${segmentation.totalCustomers ?: 0}",
                        icon = Icons.Default.People
                    )
                }
                segmentation.summary?.let { summary ->
                    item {
                        MetricCard(
                            title = "إجمالي الإيرادات",
                            value = currencyFormatter.format(summary.totalRevenue ?: 0.0),
                            icon = Icons.Default.TrendingUp
                        )
                    }
                    item {
                        MetricCard(
                            title = "متوسط قيمة العميل",
                            value = currencyFormatter.format(summary.avgCustomerValue ?: 0.0),
                            icon = Icons.Default.AttachMoney
                        )
                    }
                }
            }

            // Customer segments breakdown
            segmentation.segments?.let { segments ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "توزيع الشرائح",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    listOf(
                        "عملاء جدد" to segments.newCustomers,
                        "عملاء عالي القيمة" to segments.highValue,
                        "عملاء متوسط القيمة" to segments.mediumValue,
                        "عملاء منخفض القيمة" to segments.lowValue,
                        "عملاء معرضون للخطر" to segments.atRiskCustomers
                    ).forEach { (title, segment) ->
                        segment?.let {
                            CustomerSegmentRow(
                                title = title,
                                segment = it,
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerSegmentRow(
    title: String,
    segment: CustomerSegmentInfo,
    currencyFormatter: NumberFormat
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${segment.count ?: 0} عميل (${String.format("%.1f", segment.percentage ?: 0.0)}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = currencyFormatter.format(segment.totalRevenue ?: 0.0),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.success
            )
            Text(
                text = "متوسط: ${currencyFormatter.format(segment.avgOrderValue ?: 0.0)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CustomerAcquisitionSection(
    acquisition: CustomerAcquisitionData,
    currencyFormatter: NumberFormat
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
                text = "مقاييس اكتساب العملاء",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Acquisition metrics
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                acquisition.metrics?.let { metrics ->
                    item {
                        MetricCard(
                            title = "معدل التحويل",
                            value = "${String.format("%.2f", metrics.conversionRate ?: 0.0)}%",
                            icon = Icons.Default.TrendingUp
                        )
                    }
                    item {
                        MetricCard(
                            title = "تكلفة الاكتساب",
                            value = currencyFormatter.format(metrics.estimatedAcquisitionCost ?: 0.0),
                            icon = Icons.Default.AttachMoney
                        )
                    }
                    item {
                        MetricCard(
                            title = "العملاء مع المشتريات",
                            value = "${metrics.customersWithPurchases ?: 0}",
                            icon = Icons.Default.People
                        )
                    }
                }
            }

            // Acquisition channels
            acquisition.channels?.let { channels ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "قنوات الاكتساب",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    channels.topChannel?.let { topChannel ->
                        Text(
                            text = "القناة الأفضل: $topChannel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.success,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    channels.acquisitionChannels?.forEach { (channel, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = channel,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "$count عميل",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Recommendations
            acquisition.recommendations?.let { recommendations ->
                if (recommendations.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "التوصيات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        recommendations.forEach { recommendation ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AppTheme.colors.warning
                                )
                                Text(
                                    text = recommendation,
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
}

@Composable
private fun CustomerLifetimeValueSection(
    ltv: CustomerLifetimeValueData,
    currencyFormatter: NumberFormat
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
                text = "تحليل القيمة الدائمة للعملاء",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // LTV metrics
            ltv.analysis?.let { analysis ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MetricCard(
                            title = "متوسط القيمة الدائمة",
                            value = currencyFormatter.format(analysis.avgLifetimeValue ?: 0.0),
                            icon = Icons.Default.TrendingUp
                        )
                    }
                    item {
                        MetricCard(
                            title = "متوسط قيمة الطلب",
                            value = currencyFormatter.format(analysis.avgOrderValue ?: 0.0),
                            icon = Icons.Default.Receipt
                        )
                    }
                    item {
                        MetricCard(
                            title = "تكرار الشراء",
                            value = "${String.format("%.1f", analysis.avgPurchaseFrequency ?: 0.0)}",
                            icon = Icons.Default.Repeat
                        )
                    }
                }
            }

            // Top customers
            ltv.topCustomers?.let { topCustomers ->
                if (topCustomers.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "أفضل العملاء",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        topCustomers.take(5).forEach { customer ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = customer.customerName ?: "غير محدد",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = customer.email ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = currencyFormatter.format(customer.totalRevenue ?: 0.0),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = AppTheme.colors.success
                                            )
                                            Text(
                                                text = "${customer.totalOrders ?: 0} طلب",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "القيمة المتوقعة: ${currencyFormatter.format(customer.predictedLTV ?: 0.0)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppTheme.colors.info
                                        )
                                        Text(
                                            text = customer.customerSegment ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerChurnSection(
    churn: CustomerChurnData,
    currencyFormatter: NumberFormat
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
                text = "تحليل فقدان العملاء",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Churn metrics
            churn.churnMetrics?.let { metrics ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MetricCard(
                            title = "معدل فقدان العملاء",
                            value = "${String.format("%.2f", metrics.churnRate ?: 0.0)}%",
                            icon = Icons.Default.TrendingDown,
                            isNegative = true
                        )
                    }
                    item {
                        MetricCard(
                            title = "الإيرادات المعرضة للخطر",
                            value = currencyFormatter.format(metrics.revenueAtRisk ?: 0.0),
                            icon = Icons.Default.Warning,
                            isNegative = true
                        )
                    }
                    item {
                        MetricCard(
                            title = "متوسط أيام عدم الشراء",
                            value = "${String.format("%.1f", metrics.avgDaysSinceLastPurchase ?: 0.0)} يوم",
                            icon = Icons.Default.Schedule
                        )
                    }
                }
            }

            // Risk analysis
            churn.riskAnalysis?.let { risk ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "توزيع المخاطر",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    risk.riskDistribution?.forEach { (riskLevel, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = riskLevel,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "$count عميل",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerBehaviorSection(
    behavior: CustomerBehaviorData,
    currencyFormatter: NumberFormat
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
                text = "تحليل سلوك العملاء",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Purchase patterns
            behavior.purchasePatterns?.let { patterns ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "أنماط الشراء",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            MetricCard(
                                title = "معدل العملاء المتكررين",
                                value = "${String.format("%.1f", patterns.repeatCustomerRate ?: 0.0)}%",
                                icon = Icons.Default.Repeat
                            )
                        }
                        item {
                            MetricCard(
                                title = "متوسط أيام بين الطلبات",
                                value = "${String.format("%.1f", patterns.avgDaysBetweenPurchases ?: 0.0)} يوم",
                                icon = Icons.Default.Schedule
                            )
                        }
                    }

                    // Order value distribution
                    patterns.orderValueDistribution?.let { distribution ->
                        Text(
                            text = "توزيع قيم الطلبات",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        distribution.forEach { (range, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = range,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "$count طلب",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Seasonality insights
            behavior.seasonality?.let { seasonality ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "الاتجاهات الموسمية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    seasonality.peakSalesDay?.let { peakDay ->
                        Text(
                            text = "أفضل يوم للمبيعات: $peakDay",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.success
                        )
                    }

                    seasonality.peakSalesHour?.let { peakHour ->
                        Text(
                            text = "أفضل ساعة للمبيعات: $peakHour",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.success
                        )
                    }
                }
            }

            // Preferences
            behavior.preferences?.let { preferences ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تفضيلات العملاء",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    preferences.paymentMethodPreferences?.let { paymentMethods ->
                        Text(
                            text = "طرق الدفع المفضلة",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        paymentMethods.forEach { (method, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "$count استخدام",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Product Report Content - Comprehensive Analytics
@Composable
private fun ProductReportContent(
    productReport: ProductReportDTO?,
    currencyFormatter: NumberFormat
) {
    if (productReport == null) {
        EmptyReportState()
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Report Summary with insights and metrics
        productReport.reportSummary?.let { summary ->
            ProductReportSummarySection(summary, currencyFormatter)
        }

        // Product Rankings Overview
        productReport.productRankings?.let { rankings ->
            ProductRankingsSection(rankings, currencyFormatter)
        }

        // Profitability Analysis
        productReport.profitabilityAnalysis?.let { profitability ->
            ProfitabilityAnalysisSection(profitability, currencyFormatter)
        }

        // Product Trends
        productReport.productTrends?.let { trends ->
            ProductTrendsSection(trends, currencyFormatter)
        }

        // Category Performance
        productReport.categoryPerformance?.let { categoryPerf ->
            CategoryPerformanceSection(categoryPerf, currencyFormatter)
        }

        // Cross-Sell Analysis
        productReport.crossSellAnalysis?.let { crossSell ->
            CrossSellAnalysisSection(crossSell, currencyFormatter)
        }

        // Data Validation section
        productReport.dataValidation?.let { validation ->
            ProductDataValidationSection(validation)
        }

        // Report Metadata section
        productReport.metadata?.let { metadata ->
            ProductReportMetadataSection(metadata)
        }
    }
}

@Composable
private fun ProductReportSummarySection(
    summary: ProductReportSummary,
    currencyFormatter: NumberFormat
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
                text = "ملخص التقرير",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Key insights
            summary.insights?.let { insights ->
                if (insights.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "الرؤى الرئيسية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        insights.forEach { insight ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AppTheme.colors.warning
                                )
                                Text(
                                    text = insight,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Sales and financial metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sales metrics
                summary.salesMetrics?.let { salesMetrics ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "مقاييس المبيعات",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            salesMetrics.totalSales?.let {
                                Text(
                                    text = "إجمالي المبيعات: $it",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            salesMetrics.totalRevenue?.let {
                                Text(
                                    text = "الإيرادات: ${currencyFormatter.format(it)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            salesMetrics.totalQuantitySold?.let {
                                Text(
                                    text = "الكمية المباعة: $it",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                // Financial metrics
                summary.financialMetrics?.let { financialMetrics ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "المقاييس المالية",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            financialMetrics.totalProfit?.let {
                                Text(
                                    text = "إجمالي الربح: ${currencyFormatter.format(it)}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            financialMetrics.avgProfitMargin?.let {
                                Text(
                                    text = "متوسط هامش الربح: ${String.format("%.1f", it)}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            financialMetrics.roi?.let {
                                Text(
                                    text = "العائد على الاستثمار: ${String.format("%.1f", it)}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Report period info
            summary.reportPeriod?.let { period ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "فترة التقرير: ${period.description ?: "غير محدد"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        period.daysIncluded?.let { days ->
                            Text(
                                text = "$days يوم",
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

@Composable
private fun ProductRankingsSection(
    rankings: ProductRankingsData,
    currencyFormatter: NumberFormat
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
                text = "ترتيب المنتجات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Summary metrics - use allProductMetrics if available, fallback to summary
            val metricsData = rankings.allProductMetrics?.asRankingSummary ?: rankings.summary
            metricsData?.let { metrics ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MetricCard(
                            title = "إجمالي المنتجات",
                            value = "${metrics.totalProducts ?: 0}",
                            icon = Icons.Default.Inventory
                        )
                    }
                    item {
                        MetricCard(
                            title = "إجمالي الإيرادات",
                            value = currencyFormatter.format(metrics.totalRevenue ?: 0.0),
                            icon = Icons.Default.TrendingUp
                        )
                    }
                    item {
                        MetricCard(
                            title = "الكمية المباعة",
                            value = "${metrics.totalQuantitySold ?: 0}",
                            icon = Icons.Default.ShoppingCart
                        )
                    }
                    item {
                        MetricCard(
                            title = "إجمالي الربح",
                            value = currencyFormatter.format(metrics.totalProfit ?: 0.0),
                            icon = Icons.Default.Analytics,
                            color = AppTheme.colors.success
                        )
                    }
                    item {
                        MetricCard(
                            title = "متوسط هامش الربح",
                            value = "${String.format("%.1f", metrics.avgProfitMargin ?: 0.0)}%",
                            icon = Icons.Default.TrendingUp
                        )
                    }
                }
            }

            // Multiple ranking sections
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top products by revenue
                rankings.topProductsByRevenue?.let { topProducts ->
                    if (topProducts.isNotEmpty()) {
                        ProductRankingSubsection(
                            title = "أفضل المنتجات حسب الإيرادات",
                            products = topProducts.take(5),
                            currencyFormatter = currencyFormatter,
                            rankingType = "revenue"
                        )
                    }
                }

                // Top products by profit
                rankings.topProductsByProfit?.let { topProducts ->
                    if (topProducts.isNotEmpty()) {
                        ProductRankingSubsection(
                            title = "أفضل المنتجات حسب الربح",
                            products = topProducts.take(5),
                            currencyFormatter = currencyFormatter,
                            rankingType = "profit"
                        )
                    }
                }

                // Top products by margin
                rankings.topProductsByMargin?.let { topProducts ->
                    if (topProducts.isNotEmpty()) {
                        ProductRankingSubsection(
                            title = "أفضل المنتجات حسب هامش الربح",
                            products = topProducts.take(5),
                            currencyFormatter = currencyFormatter,
                            rankingType = "margin"
                        )
                    }
                }

                // Top products by quantity
                rankings.topProductsByQuantity?.let { topProducts ->
                    if (topProducts.isNotEmpty()) {
                        ProductRankingSubsection(
                            title = "أفضل المنتجات حسب الكمية",
                            products = topProducts.take(5),
                            currencyFormatter = currencyFormatter,
                            rankingType = "quantity"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRankingSubsection(
    title: String,
    products: List<ProductRankingItem>,
    currencyFormatter: NumberFormat,
    rankingType: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        products.forEach { product ->
            ProductRankingCard(
                product = product,
                currencyFormatter = currencyFormatter,
                rankingType = rankingType
            )
        }
    }
}

@Composable
private fun ProductRankingCard(
    product: ProductRankingItem,
    currencyFormatter: NumberFormat,
    rankingType: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${product.rank ?: 0}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Text(
                        text = product.productName ?: "غير محدد",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = product.category ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                when (rankingType) {
                    "revenue" -> {
                        Text(
                            text = currencyFormatter.format(product.revenue ?: 0.0),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.success
                        )
                        Text(
                            text = "${product.quantitySold ?: 0} قطعة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    "quantity" -> {
                        Text(
                            text = "${product.quantitySold ?: 0} قطعة",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.info
                        )
                        Text(
                            text = currencyFormatter.format(product.revenue ?: 0.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    "profit" -> {
                        Text(
                            text = currencyFormatter.format(product.profit ?: 0.0),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.success
                        )
                        Text(
                            text = "${String.format("%.1f", product.profitMargin ?: 0.0)}% هامش",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    "margin" -> {
                        Text(
                            text = "${String.format("%.1f", product.profitMargin ?: 0.0)}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.success
                        )
                        Text(
                            text = currencyFormatter.format(product.profit ?: 0.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfitabilityAnalysisSection(
    profitability: ProfitabilityAnalysisData,
    currencyFormatter: NumberFormat
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
                text = "تحليل الربحية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Profitability metrics overview
            profitability.profitabilityMetrics?.let { metrics ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MetricCard(
                            title = "إجمالي الربح",
                            value = currencyFormatter.format(metrics.totalProfit ?: 0.0),
                            icon = Icons.Default.TrendingUp,
                            color = AppTheme.colors.success
                        )
                    }
                    item {
                        MetricCard(
                            title = "متوسط هامش الربح",
                            value = "${String.format("%.1f", metrics.avgProfitMargin ?: 0.0)}%",
                            icon = Icons.Default.Analytics
                        )
                    }
                    metrics.profitGrowth?.let { growth ->
                        item {
                            MetricCard(
                                title = "نمو الربح",
                                value = "${String.format("%.1f", growth)}%",
                                icon = Icons.Default.TrendingUp,
                                color = if (growth >= 0) AppTheme.colors.success else AppTheme.colors.error
                            )
                        }
                    }
                }
            }

            // Most profitable products
            profitability.mostProfitableProducts?.let { products ->
                if (products.isNotEmpty()) {
                    ProductRankingSubsection(
                        title = "أكثر المنتجات ربحية",
                        products = products.take(5),
                        currencyFormatter = currencyFormatter,
                        rankingType = "profit"
                    )
                }
            }

            // Least profitable products
            profitability.leastProfitableProducts?.let { products ->
                if (products.isNotEmpty()) {
                    ProductRankingSubsection(
                        title = "أقل المنتجات ربحية",
                        products = products.take(5),
                        currencyFormatter = currencyFormatter,
                        rankingType = "profit"
                    )
                }
            }

            // Profit margin distribution
            profitability.profitMarginDistribution?.let { distribution ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "توزيع هوامش الربح",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    distribution.forEach { (range, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = range,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "$count منتج",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Cost analysis
            profitability.costAnalysis?.let { costAnalysis ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MetricCard(
                            title = "إجمالي التكاليف",
                            value = currencyFormatter.format(costAnalysis.totalCosts ?: 0.0),
                            icon = Icons.Default.AttachMoney,
                            color = AppTheme.colors.warning
                        )
                    }
                    item {
                        MetricCard(
                            title = "متوسط التكلفة للوحدة",
                            value = currencyFormatter.format(costAnalysis.avgCostPerUnit ?: 0.0),
                            icon = Icons.Default.Analytics
                        )
                    }
                }
            }

            // Category profitability
            profitability.categoryProfitability?.let { categoryMap ->
                if (categoryMap.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ربحية الفئات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        categoryMap.entries.take(5).forEach { (categoryName, category) ->
                            CategoryProfitabilityCard(
                                category = category.copy(categoryName = categoryName),
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryProfitabilityCard(
    category: CategoryProfitability,
    currencyFormatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
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
                    text = category.categoryName ?: "غير محدد",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${category.productCount ?: 0} منتج",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(category.totalRevenue ?: 0.0),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.success
                )
                Text(
                    text = "${String.format("%.1f", category.profitMargin ?: 0.0)}% هامش",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProductTrendsSection(
    trends: ProductTrendsData,
    currencyFormatter: NumberFormat
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
                text = "اتجاهات المنتجات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Trend summary metrics
            trends.trendSummary?.let { summary ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MetricCard(
                            title = "المنتجات الرائجة",
                            value = "${summary.totalTrendingProducts ?: 0}",
                            icon = Icons.Default.TrendingUp
                        )
                    }
                    summary.avgGrowthRate?.let { growthRate ->
                        item {
                            MetricCard(
                                title = "متوسط معدل النمو",
                                value = "${String.format("%.1f", growthRate)}%",
                                icon = Icons.Default.Analytics,
                                color = if (growthRate >= 0) AppTheme.colors.success else AppTheme.colors.error
                            )
                        }
                    }
                    summary.topTrendDirection?.let { direction ->
                        item {
                            MetricCard(
                                title = "الاتجاه السائد",
                                value = direction,
                                icon = Icons.Default.TrendingUp
                            )
                        }
                    }
                }
            }

            // Trending products
            trends.trendingProducts?.let { trendingProducts ->
                if (trendingProducts.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "المنتجات الرائجة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        trendingProducts.take(5).forEach { product ->
                            TrendingProductCard(
                                product = product,
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }
                }
            }

            // Weekly trends summary
            val weeklyTrends = trends.weeklyTrendsList
            if (weeklyTrends.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الاتجاهات الأسبوعية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val latestWeek = weeklyTrends.lastOrNull()
                    latestWeek?.let { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "الأسبوع الحالي",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = currencyFormatter.format(week.totalRevenue ?: 0.0),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    week.growthRate?.let { growth ->
                                        Text(
                                            text = "${if (growth >= 0) "+" else ""}${String.format("%.1f", growth)}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (growth >= 0) AppTheme.colors.success else AppTheme.colors.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun TrendingProductCard(
    product: TrendingProduct,
    currencyFormatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Trend direction indicator
                Icon(
                    imageVector = when (product.trendDirection) {
                        "UP" -> Icons.Default.TrendingUp
                        "DOWN" -> Icons.Default.TrendingDown
                        else -> Icons.Default.Remove
                    },
                    contentDescription = null,
                    tint = when (product.trendDirection) {
                        "UP" -> AppTheme.colors.success
                        "DOWN" -> AppTheme.colors.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = product.productName ?: "غير محدد",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = product.category ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${product.currentSales ?: 0} مبيعات",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                product.growthRate?.let { growth ->
                    Text(
                        text = "${if (growth >= 0) "+" else ""}${String.format("%.1f", growth)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (growth >= 0) AppTheme.colors.success else AppTheme.colors.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPerformanceSection(
    categoryPerf: CategoryPerformanceData,
    currencyFormatter: NumberFormat
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
                text = "أداء الفئات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Category metrics
            categoryPerf.categoryMetricsData?.let { metrics ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MetricCard(
                            title = "إجمالي الفئات",
                            value = "${metrics.totalCategories ?: 0}",
                            icon = Icons.Default.Folder
                        )
                    }
                    item {
                        MetricCard(
                            title = "متوسط الإيرادات",
                            value = currencyFormatter.format(metrics.avgRevenuePerCategory ?: 0.0),
                            icon = Icons.Default.TrendingUp
                        )
                    }
                }

                metrics.topPerformingCategory?.let { topCategory ->
                    Text(
                        text = "أفضل فئة: $topCategory",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Category comparison
            val comparisons = categoryPerf.categoryComparisonList
            if (comparisons.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "مقارنة الفئات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    comparisons.take(5).forEach { category ->
                        CategoryComparisonCard(
                            category = category,
                            currencyFormatter = currencyFormatter
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CrossSellAnalysisSection(
    crossSell: CrossSellAnalysisData,
    currencyFormatter: NumberFormat
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
                text = "تحليل البيع المتقاطع",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Basket analysis
            crossSell.basketAnalysis?.let { basket ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تحليل السلة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            MetricCard(
                                title = "متوسط حجم السلة",
                                value = "${String.format("%.1f", basket.avgBasketSize ?: 0.0)} منتج",
                                icon = Icons.Default.ShoppingCart
                            )
                        }
                        item {
                            MetricCard(
                                title = "متوسط قيمة السلة",
                                value = currencyFormatter.format(basket.avgBasketValue ?: 0.0),
                                icon = Icons.Default.AttachMoney
                            )
                        }
                    }
                }
            }

            // Product pairs analysis
            crossSell.productPairs?.let { pairs ->
                if (pairs.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "أزواج المنتجات الأكثر شراءً معاً",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        pairs.take(5).forEach { pair ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
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
                                            text = "${pair.productA} + ${pair.productB}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${pair.frequency} مرة",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        pair.confidence?.let { confidence ->
                                            Text(
                                                text = "الثقة: ${String.format("%.1f", confidence)}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        pair.lift?.let { lift ->
                                            Text(
                                                text = "الرفع: ${String.format("%.2f", lift)}",
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
            }

            // Cross-sell opportunities
            crossSell.crossSellOpportunities?.let { opportunities ->
                if (opportunities.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "فرص البيع المتقاطع",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        opportunities.take(3).forEach { opportunity ->
                            CrossSellOpportunityCard(
                                opportunity = opportunity,
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryComparisonCard(
    category: CategoryComparison,
    currencyFormatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
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
                    text = category.categoryName ?: "غير محدد",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${category.totalQuantity ?: 0} قطعة مباعة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormatter.format(category.totalRevenue ?: 0.0),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.success
                )
                Text(
                    text = "${String.format("%.1f", category.marketShare ?: 0.0)}% حصة السوق",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CrossSellOpportunityCard(
    opportunity: CrossSellOpportunity,
    currencyFormatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "المنتج الأساسي: ${opportunity.primaryProduct ?: "غير محدد"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    opportunity.suggestedProducts?.let { suggested ->
                        Text(
                            text = "المنتجات المقترحة:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = suggested.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.info
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormatter.format(opportunity.potentialRevenue ?: 0.0),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.success
                    )
                    Text(
                        text = "${String.format("%.1f", (opportunity.confidence ?: 0.0) * 100)}% ثقة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
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
    isNegative: Boolean = false,
    icon: ImageVector,
    color: Color? = null
) {
    val valueColor = when {
        isNegative -> AppTheme.colors.error
        color != null -> color
        else -> MaterialTheme.colorScheme.onSurface
    }

    val iconColor = when {
        isNegative -> AppTheme.colors.error
        color != null -> color
        else -> MaterialTheme.colorScheme.primary
    }

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
                        color = valueColor
                    )
                }

                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
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

// Helper functions for safely extracting values from JsonElement
private fun getKPIDouble(kpis: JsonElement, key: String): Double {
    return try {
        kpis.jsonObject[key]?.jsonPrimitive?.doubleOrNull ?: 0.0
    } catch (e: Exception) {
        0.0
    }
}

private fun getKPILong(kpis: JsonElement, key: String): Long {
    return try {
        kpis.jsonObject[key]?.jsonPrimitive?.longOrNull ?: 0L
    } catch (e: Exception) {
        0L
    }
}

private fun getKPIString(kpis: JsonElement, key: String): String {
    return try {
        kpis.jsonObject[key]?.jsonPrimitive?.content ?: ""
    } catch (e: Exception) {
        ""
    }
}

// Product Data Validation Section
@Composable
private fun ProductDataValidationSection(
    validation: ProductDataValidation
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
                text = "جودة البيانات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Data quality score
            validation.dataQualityScore?.let { score ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "نقاط جودة البيانات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val scoreColor = when {
                        score >= 90 -> AppTheme.colors.success
                        score >= 70 -> AppTheme.colors.warning
                        else -> AppTheme.colors.error
                    }

                    Text(
                        text = "${String.format("%.1f", score)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            // Validation counts
            validation.validationCounts?.let { counts ->
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MetricCard(
                            title = "إجمالي السجلات",
                            value = "${counts.totalRecords ?: 0}",
                            icon = Icons.Default.Storage
                        )
                    }
                    item {
                        MetricCard(
                            title = "السجلات الصحيحة",
                            value = "${counts.validRecords ?: 0}",
                            icon = Icons.Default.CheckCircle,
                            color = AppTheme.colors.success
                        )
                    }
                    item {
                        MetricCard(
                            title = "السجلات الخاطئة",
                            value = "${counts.invalidRecords ?: 0}",
                            icon = Icons.Default.Error,
                            color = AppTheme.colors.error
                        )
                    }
                }
            }

            // Product coverage
            validation.productCoverage?.let { coverage ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "تغطية المنتجات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "المنتجات مع البيانات: ${coverage.productsWithData ?: 0}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${String.format("%.1f", coverage.coveragePercentage ?: 0.0)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.info
                        )
                    }
                }
            }

            // Warnings and errors
            validation.warnings?.let { warnings ->
                if (warnings.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "تحذيرات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.warning
                        )

                        warnings.forEach { warning ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AppTheme.colors.warning
                                )
                                Text(
                                    text = warning,
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
}

// Product Report Metadata Section
@Composable
private fun ProductReportMetadataSection(
    metadata: ProductReportMetadata
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
                text = "معلومات التقرير",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                metadata.reportId?.let { id ->
                    MetadataRow("معرف التقرير", id)
                }

                metadata.generatedAt?.let { generatedAt ->
                    MetadataRow("تاريخ الإنشاء", generatedAt)
                }

                metadata.generatedBy?.let { generatedBy ->
                    MetadataRow("أنشئ بواسطة", generatedBy)
                }

                metadata.reportVersion?.let { version ->
                    MetadataRow("إصدار التقرير", version)
                }

                metadata.executionTimeMs?.let { executionTime ->
                    MetadataRow("وقت التنفيذ", "${executionTime}ms")
                }

                metadata.cacheStatus?.let { cacheStatus ->
                    MetadataRow("حالة التخزين المؤقت", cacheStatus)
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
