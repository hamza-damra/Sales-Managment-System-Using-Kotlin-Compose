package ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.utils.ResponsiveUtils

@Composable
fun ReportsScreen() {
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsivePadding = ResponsiveUtils.getResponsivePadding()
    var selectedReportType by remember { mutableStateOf("sales") }

    RTLProvider {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(responsivePadding.screen),
                verticalArrangement = Arrangement.spacedBy(responsive.large)
            ) {
                // Enhanced Header Section
                item {
                    EnhancedReportsHeader()
                }

                // Report Type Selector with improved design
                item {
                    EnhancedReportTypeSelector(
                        selectedType = selectedReportType,
                        onTypeSelected = { selectedReportType = it }
                    )
                }

                // Main Analytics Dashboard
                item {
                    EnhancedAnalyticsDashboard()
                }

                // Detailed Reports Section
                item {
                    EnhancedDetailedReports(selectedReportType)
                }
            }
        }
    }
}

// Enhanced Header Component
@Composable
private fun EnhancedReportsHeader() {
    val responsive = ResponsiveUtils.getResponsiveSpacing()

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
                text = "تحليل شامل وتفصيلي لأداء المبيعات والمخزون",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // Action buttons
        RTLRow(
            horizontalArrangement = Arrangement.spacedBy(responsive.small)
        ) {
            IconButton(
                onClick = { /* Export functionality */ },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = AppTheme.colors.success.copy(alpha = 0.1f)
                )
            ) {
                Icon(
                    Icons.Default.FileDownload,
                    contentDescription = "تصدير",
                    tint = AppTheme.colors.success
                )
            }

            IconButton(
                onClick = { /* Refresh functionality */ },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Enhanced Report Type Selector (Simplified version without experimental APIs)
@Composable
private fun EnhancedReportTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsivePadding = ResponsiveUtils.getResponsivePadding()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.spacedBy(responsive.small)
    ) {
        // Section title with navigation controls
        RTLRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "أنواع التقارير",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Navigation controls
            RTLRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Right arrow (positioned on the left, scrolls LEFT through report types)
                EnhancedNavigationButton(
                    icon = Icons.Default.ChevronRight,
                    contentDescription = "السابق",
                    onClick = {
                        coroutineScope.launch {
                            val currentIndex = maxOf(0, listState.firstVisibleItemIndex - 1)
                            listState.animateScrollToItem(currentIndex)
                        }
                    }
                )

                // Left arrow (positioned on the right, scrolls RIGHT through report types)
                EnhancedNavigationButton(
                    icon = Icons.Default.ChevronLeft,
                    contentDescription = "التالي",
                    onClick = {
                        coroutineScope.launch {
                            val nextIndex = minOf(7, listState.firstVisibleItemIndex + 1) // 8 total items (0-7)
                            listState.animateScrollToItem(nextIndex)
                        }
                    }
                )
            }
        }

        // Scrollable cards row
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(responsive.medium),
            contentPadding = PaddingValues(
                horizontal = responsivePadding.item,
                vertical = 4.dp
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                EnhancedReportCard(
                    title = "تقرير المبيعات",
                    description = "تحليل المبيعات اليومية والشهرية",
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.primary,
                    isSelected = selectedType == "sales",
                    onClick = { onTypeSelected("sales") }
                )
            }
            item {
                EnhancedReportCard(
                    title = "تقرير المخزون",
                    description = "حالة المخزون والمنتجات",
                    icon = Icons.Default.Inventory,
                    color = AppTheme.colors.success,
                    isSelected = selectedType == "inventory",
                    onClick = { onTypeSelected("inventory") }
                )
            }
            item {
                EnhancedReportCard(
                    title = "تقرير العملاء",
                    description = "إحصائيات وتحليل العملاء",
                    icon = Icons.Default.People,
                    color = AppTheme.colors.info,
                    isSelected = selectedType == "customers",
                    onClick = { onTypeSelected("customers") }
                )
            }
            item {
                EnhancedReportCard(
                    title = "التقرير المالي",
                    description = "الأرباح والخسائر والتدفق النقدي",
                    icon = Icons.Default.AccountBalance,
                    color = AppTheme.colors.warning,
                    isSelected = selectedType == "financial",
                    onClick = { onTypeSelected("financial") }
                )
            }
            item {
                EnhancedReportCard(
                    title = "تقرير الموردين",
                    description = "إحصائيات الموردين والمشتريات",
                    icon = Icons.Default.Business,
                    color = AppTheme.colors.purple,
                    isSelected = selectedType == "suppliers",
                    onClick = { onTypeSelected("suppliers") }
                )
            }
            item {
                EnhancedReportCard(
                    title = "تقرير المرتجعات",
                    description = "تحليل المرتجعات والإلغاءات",
                    icon = Icons.Default.AssignmentReturn,
                    color = AppTheme.colors.error,
                    isSelected = selectedType == "returns",
                    onClick = { onTypeSelected("returns") }
                )
            }
            item {
                EnhancedReportCard(
                    title = "تقرير العروض",
                    description = "فعالية العروض والخصومات",
                    icon = Icons.Default.LocalOffer,
                    color = AppTheme.colors.pink,
                    isSelected = selectedType == "promotions",
                    onClick = { onTypeSelected("promotions") }
                )
            }
            item {
                EnhancedReportCard(
                    title = "التقرير الضريبي",
                    description = "الضرائب والرسوم المحصلة",
                    icon = Icons.Default.Receipt,
                    color = AppTheme.colors.indigo,
                    isSelected = selectedType == "tax",
                    onClick = { onTypeSelected("tax") }
                )
            }
        }
    }
}

// Enhanced Report Card Component with Box-based hover effects
@Composable
private fun EnhancedReportCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val responsivePadding = ResponsiveUtils.getResponsivePadding()

    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .width(300.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = when {
                    isSelected -> color.copy(alpha = 0.1f)
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = when {
                    isSelected -> 2.dp
                    isHovered -> 1.5.dp
                    else -> 1.dp
                },
                color = when {
                    isSelected -> color.copy(alpha = 0.5f)
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        // Subtle gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = if (isSelected) 0.05f else 0.02f),
                            color.copy(alpha = if (isSelected) 0.1f else 0.05f)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
        )

        Column(
            modifier = Modifier
                .padding(responsivePadding.card)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with enhanced styling
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.3f),
                                    color,
                                    color.copy(alpha = 0.3f)
                                )
                            ),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

// Enhanced Analytics Dashboard
@Composable
private fun EnhancedAnalyticsDashboard() {
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsivePadding = ResponsiveUtils.getResponsivePadding()

    RTLRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(responsive.large)
    ) {
        // Main Chart Section
        Card(
            modifier = Modifier.weight(2f),
            colors = CardStyles.elevatedCardColors(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardStyles.elevatedCardElevation(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Subtle gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.padding(responsivePadding.card),
                    verticalArrangement = Arrangement.spacedBy(responsive.medium)
                ) {
                    RTLRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مخطط المبيعات الشهرية",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Chart type selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ChartTypeButton("خطي", Icons.Default.ShowChart, true) { }
                            ChartTypeButton("أعمدة", Icons.Default.BarChart, false) { }
                        }
                    }

                    // Enhanced Chart Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "مخطط المبيعات التفاعلي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "سيتم عرض البيانات الفعلية هنا",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Quick Stats Section
        Card(
            modifier = Modifier.weight(1f),
            colors = CardStyles.elevatedCardColors(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardStyles.elevatedCardElevation(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Subtle gradient background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    AppTheme.colors.success.copy(alpha = 0.02f),
                                    AppTheme.colors.success.copy(alpha = 0.05f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.padding(responsivePadding.card),
                    verticalArrangement = Arrangement.spacedBy(responsive.medium)
                ) {
                    Text(
                        text = "الإحصائيات السريعة",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    EnhancedQuickStatsItem(
                        label = "إجمالي المبيعات اليوم",
                        value = "1,250 ر.س",
                        color = AppTheme.colors.success,
                        icon = Icons.Default.AttachMoney,
                        trend = "+12.5%"
                    )

                    EnhancedQuickStatsItem(
                        label = "عدد الفواتير",
                        value = "45",
                        color = MaterialTheme.colorScheme.primary,
                        icon = Icons.Default.Receipt,
                        trend = "+8.2%"
                    )

                    EnhancedQuickStatsItem(
                        label = "متوسط قيمة الفاتورة",
                        value = "278 ر.س",
                        color = AppTheme.colors.info,
                        icon = Icons.Default.Analytics,
                        trend = "+5.1%"
                    )

                    EnhancedQuickStatsItem(
                        label = "أفضل منتج",
                        value = "هاتف ذكي",
                        color = AppTheme.colors.warning,
                        icon = Icons.Default.Star,
                        trend = "15 مبيعة"
                    )
                }
            }
        }
    }
}

// Chart Type Button Component
@Composable
private fun ChartTypeButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Enhanced Quick Stats Item
@Composable
private fun EnhancedQuickStatsItem(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    trend: String
) {
    val responsive = ResponsiveUtils.getResponsiveSpacing()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(responsive.small)
        ) {
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = trend,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.success
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

// Enhanced Detailed Reports Section
@Composable
private fun EnhancedDetailedReports(selectedType: String) {
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsivePadding = ResponsiveUtils.getResponsivePadding()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardStyles.elevatedCardElevation(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Subtle gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.02f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(responsivePadding.card),
                verticalArrangement = Arrangement.spacedBy(responsive.large)
            ) {
                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getReportTitle(selectedType),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Enhanced export buttons
                    RTLRow(
                        horizontalArrangement = Arrangement.spacedBy(responsive.small)
                    ) {
                        EnhancedExportButton(
                            text = "Excel",
                            icon = Icons.Default.TableChart,
                            color = AppTheme.colors.success,
                            onClick = { /* Export to Excel */ }
                        )

                        EnhancedExportButton(
                            text = "PDF",
                            icon = Icons.Default.PictureAsPdf,
                            color = AppTheme.colors.error,
                            onClick = { /* Export to PDF */ }
                        )
                    }
                }

                // Report content based on selected type
                when (selectedType) {
                    "sales" -> ReportsSalesContent()
                    "inventory" -> ReportsInventoryContent()
                    "customers" -> ReportsCustomersContent()
                    "financial" -> ReportsFinancialContent()
                    "suppliers" -> ReportsSuppliersContent()
                    "returns" -> ReportsReturnsContent()
                    "promotions" -> ReportsPromotionsContent()
                    "tax" -> ReportsTaxContent()
                    else -> ReportsSalesContent()
                }
            }
        }
    }
}

// Helper function to get report title
private fun getReportTitle(type: String): String {
    return when (type) {
        "sales" -> "تقرير المبيعات التفصيلي"
        "inventory" -> "تقرير المخزون التفصيلي"
        "customers" -> "تقرير العملاء التفصيلي"
        "financial" -> "التقرير المالي التفصيلي"
        "suppliers" -> "تقرير الموردين التفصيلي"
        "returns" -> "تقرير المرتجعات التفصيلي"
        "promotions" -> "تقرير العروض التفصيلي"
        "tax" -> "التقرير الضريبي التفصيلي"
        else -> "التقرير التفصيلي"
    }
}

// Report Content Components
@Composable
private fun ReportsSalesContent() {
    EnhancedEmptyState(
        icon = Icons.Default.TrendingUp,
        title = "تقرير المبيعات",
        description = "سيتم عرض تحليل مفصل للمبيعات اليومية والشهرية والسنوية هنا"
    )
}

@Composable
private fun ReportsInventoryContent() {
    EnhancedEmptyState(
        icon = Icons.Default.Inventory,
        title = "تقرير المخزون",
        description = "سيتم عرض حالة المخزون والمنتجات والتنبيهات هنا"
    )
}

@Composable
private fun ReportsCustomersContent() {
    EnhancedEmptyState(
        icon = Icons.Default.People,
        title = "تقرير العملاء",
        description = "سيتم عرض إحصائيات وتحليل العملاء والمبيعات لكل عميل هنا"
    )
}

@Composable
private fun ReportsFinancialContent() {
    EnhancedEmptyState(
        icon = Icons.Default.AccountBalance,
        title = "التقرير المالي",
        description = "سيتم عرض الأرباح والخسائر والتدفق النقدي هنا"
    )
}

@Composable
private fun ReportsSuppliersContent() {
    EnhancedEmptyState(
        icon = Icons.Default.Business,
        title = "تقرير الموردين",
        description = "سيتم عرض إحصائيات الموردين والمشتريات والأداء هنا"
    )
}

@Composable
private fun ReportsReturnsContent() {
    EnhancedEmptyState(
        icon = Icons.Default.AssignmentReturn,
        title = "تقرير المرتجعات",
        description = "سيتم عرض تحليل المرتجعات والإلغاءات والأسباب هنا"
    )
}

@Composable
private fun ReportsPromotionsContent() {
    EnhancedEmptyState(
        icon = Icons.Default.LocalOffer,
        title = "تقرير العروض",
        description = "سيتم عرض فعالية العروض والخصومات والنتائج هنا"
    )
}

@Composable
private fun ReportsTaxContent() {
    EnhancedEmptyState(
        icon = Icons.Default.Receipt,
        title = "التقرير الضريبي",
        description = "سيتم عرض الضرائب والرسوم المحصلة والتفاصيل الضريبية هنا"
    )
}

// Enhanced Navigation Button Component with complete hover coverage
@Composable
private fun EnhancedNavigationButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = when {
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = when {
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = when {
                isHovered -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

// Enhanced Export Button Component with complete hover coverage
@Composable
private fun EnhancedExportButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .height(56.dp) // Match dropdown height for consistency
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = when {
                    isHovered -> color.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = when {
                    isHovered -> color.copy(alpha = 0.6f)
                    else -> color.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = when {
                    isHovered -> color
                    else -> color.copy(alpha = 0.8f)
                }
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = when {
                    isHovered -> color
                    else -> color.copy(alpha = 0.8f)
                }
            )
        }
    }
}
