package ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import services.InventoryExportService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import data.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import UiUtils
import ui.components.*
import ui.theme.CardStyles
import ui.theme.AppTheme

// Inventory Tab Enum
enum class InventoryTab(val title: String) {
    OVERVIEW("نظرة عامة"),
    PRODUCTS("المنتجات"),
    MOVEMENTS("حركات المخزون"),
    WAREHOUSES("المستودعات")
}

@Composable
fun InventoryScreen(
    salesDataManager: SalesDataManager,
    inventoryExportService: InventoryExportService? = null
) {
    // For desktop application, we'll use window size detection
    // In a real desktop app, you would get this from the window state
    val isTablet = true // Assume tablet/desktop for now
    val isDesktop = true // Desktop application

    RTLProvider {
        var selectedTab by remember { mutableStateOf(InventoryTab.OVERVIEW) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("الكل") }
        var selectedWarehouse by remember { mutableStateOf("الكل") }
        var showLowStockOnly by remember { mutableStateOf(false) }
        var showExpiringOnly by remember { mutableStateOf(false) }
        var isExporting by remember { mutableStateOf(false) }
        var exportMessage by remember { mutableStateOf<String?>(null) }

        val coroutineScope = rememberCoroutineScope()

        // Export functions
        val handleExportExcel = {
            if (inventoryExportService != null && !isExporting) {
                isExporting = true
                exportMessage = null
                coroutineScope.launch {
                    try {
                        val result = when (selectedTab) {
                            InventoryTab.OVERVIEW -> inventoryExportService.exportInventoryOverviewToExcel()
                            InventoryTab.PRODUCTS -> inventoryExportService.exportProductsListToExcel(
                                category = if (selectedCategory != "الكل") selectedCategory else null,
                                searchQuery = if (searchQuery.isNotBlank()) searchQuery else null
                            )
                            InventoryTab.MOVEMENTS -> inventoryExportService.exportStockMovementsToExcel()
                            InventoryTab.WAREHOUSES -> inventoryExportService.exportProductsListToExcel()
                        }

                        result.onSuccess { success ->
                            exportMessage = if (success) "تم تصدير الملف بنجاح!" else "تم إلغاء التصدير"
                        }.onFailure { exception ->
                            exportMessage = "خطأ في التصدير: ${exception.message}"
                        }
                    } catch (e: Exception) {
                        exportMessage = "خطأ في التصدير: ${e.message}"
                    } finally {
                        isExporting = false
                    }
                }
            }
        }

        val handleExportPdf = {
            if (inventoryExportService != null && !isExporting) {
                isExporting = true
                exportMessage = null
                coroutineScope.launch {
                    try {
                        val result = when (selectedTab) {
                            InventoryTab.OVERVIEW -> inventoryExportService.exportInventoryOverviewToPdf()
                            InventoryTab.PRODUCTS -> inventoryExportService.exportProductsListToPdf(
                                category = if (selectedCategory != "الكل") selectedCategory else null,
                                searchQuery = if (searchQuery.isNotBlank()) searchQuery else null
                            )
                            InventoryTab.MOVEMENTS -> inventoryExportService.exportStockMovementsToPdf()
                            InventoryTab.WAREHOUSES -> inventoryExportService.exportProductsListToPdf()
                        }

                        result.onSuccess { success ->
                            exportMessage = if (success) "تم تصدير الملف بنجاح!" else "تم إلغاء التصدير"
                        }.onFailure { exception ->
                            exportMessage = "خطأ في التصدير: ${exception.message}"
                        }
                    } catch (e: Exception) {
                        exportMessage = "خطأ في التصدير: ${e.message}"
                    } finally {
                        isExporting = false
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = when {
                        isDesktop -> 32.dp
                        isTablet -> 24.dp
                        else -> 16.dp
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(
                when {
                    isDesktop -> 24.dp
                    isTablet -> 20.dp
                    else -> 16.dp
                }
            )
        ) {
            // Header with responsive design
            ModernInventoryHeader(
                isTablet = isTablet,
                isDesktop = isDesktop,
                onExportExcel = handleExportExcel,
                onExportPdf = handleExportPdf
            )

            // Export status message
            exportMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("نجاح"))
                            AppTheme.colors.success.copy(alpha = 0.1f)
                        else
                            AppTheme.colors.error.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    RTLRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { exportMessage = null }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = if (message.contains("نجاح"))
                                    AppTheme.colors.success
                                else
                                    AppTheme.colors.error
                            )
                        }

                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (message.contains("نجاح"))
                                AppTheme.colors.success
                            else
                                AppTheme.colors.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Loading indicator during export
            if (isExporting) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.info.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    RTLRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AppTheme.colors.info,
                            strokeWidth = 2.dp
                        )
                        RTLSpacer(12.dp)
                        Text(
                            text = "جاري تصدير البيانات...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppTheme.colors.info,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Modern Alerts Cards with responsive grid
            ModernInventoryAlertsCards(
                isTablet = isTablet,
                isDesktop = isDesktop
            )

            // Modern Tabs with enhanced styling
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                ) {
                    InventoryTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }

            // Modern Responsive Filters
            ModernFiltersSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onCategoryChange = { selectedCategory = it },
                selectedWarehouse = selectedWarehouse,
                onWarehouseChange = { selectedWarehouse = it },
                isTablet = isTablet,
                isDesktop = isDesktop
            )

            // Modern Quick Filters with enhanced styling
            ModernQuickFilters(
                showLowStockOnly = showLowStockOnly,
                onLowStockToggle = { showLowStockOnly = !showLowStockOnly },
                showExpiringOnly = showExpiringOnly,
                onExpiringToggle = { showExpiringOnly = !showExpiringOnly },
                isTablet = isTablet
            )

            // Content based on selected tab with responsive design
            when (selectedTab) {
                InventoryTab.OVERVIEW -> ModernInventoryOverviewContent(
                    salesDataManager = salesDataManager,
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )
                InventoryTab.PRODUCTS -> ModernInventoryProductsContent(
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    selectedWarehouse = selectedWarehouse,
                    showLowStockOnly = showLowStockOnly,
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )
                InventoryTab.MOVEMENTS -> ModernStockMovementsContent(
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )
                InventoryTab.WAREHOUSES -> ModernWarehousesContent(
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )
            }
        }
    }
}

// Modern Header Component
@Composable
fun ModernInventoryHeader(
    isTablet: Boolean,
    isDesktop: Boolean,
    onExportExcel: () -> Unit = {},
    onExportPdf: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        RTLRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = when {
                        isDesktop -> 32.dp
                        isTablet -> 24.dp
                        else -> 20.dp
                    },
                    vertical = when {
                        isDesktop -> 24.dp
                        isTablet -> 20.dp
                        else -> 16.dp
                    }
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Action buttons
            RTLRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Product Button
                Button(
                    onClick = { /* إضافة منتج جديد */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    RTLSpacer(8.dp)
                    Text(
                        "إضافة منتج",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Export Buttons
                if (isTablet || isDesktop) {
                    // Excel Export Button
                    OutlinedButton(
                        onClick = onExportExcel,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppTheme.colors.success
                        ),
                        border = BorderStroke(1.dp, AppTheme.colors.success),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            Icons.Default.TableChart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        RTLSpacer(6.dp)
                        Text(
                            "تصدير Excel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // PDF Export Button
                    OutlinedButton(
                        onClick = onExportPdf,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppTheme.colors.error
                        ),
                        border = BorderStroke(1.dp, AppTheme.colors.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        RTLSpacer(6.dp)
                        Text(
                            "تصدير PDF",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Mobile - Export Menu Button
                    var showExportMenu by remember { mutableStateOf(false) }

                    Box {
                        OutlinedButton(
                            onClick = { showExportMenu = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            RTLSpacer(6.dp)
                            Text(
                                "تصدير",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    RTLRow(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("تصدير Excel")
                                        Icon(
                                            Icons.Default.TableChart,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = AppTheme.colors.success
                                        )
                                    }
                                },
                                onClick = {
                                    showExportMenu = false
                                    onExportExcel()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    RTLRow(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("تصدير PDF")
                                        Icon(
                                            Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = AppTheme.colors.error
                                        )
                                    }
                                },
                                onClick = {
                                    showExportMenu = false
                                    onExportPdf()
                                }
                            )
                        }
                    }
                }
            }

            // Right side - Title
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "إدارة المخزون",
                    style = when {
                        isDesktop -> MaterialTheme.typography.headlineLarge
                        isTablet -> MaterialTheme.typography.headlineMedium
                        else -> MaterialTheme.typography.headlineSmall
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "نظام إدارة شامل للمخزون والمنتجات",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Modern Alerts Cards with responsive grid
@Composable
fun ModernInventoryAlertsCards(
    isTablet: Boolean,
    isDesktop: Boolean
) {
    val columns = when {
        isDesktop -> 4
        isTablet -> 2
        else -> 1
    }

    // Access theme colors in composable context first
    val warningColor = AppTheme.colors.warning
    val errorColor = AppTheme.colors.error
    val infoColor = AppTheme.colors.info

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.height(
            when {
                isDesktop -> 140.dp
                isTablet -> 280.dp
                else -> 560.dp
            }
        )
    ) {
        items(4) { index ->
            val alertData = when (index) {
                0 -> AlertData("مخزون منخفض", 12, Icons.Default.Warning, warningColor)
                1 -> AlertData("نفاد المخزون", 3, Icons.Default.Error, errorColor)
                2 -> AlertData("قارب على الانتهاء", 8, Icons.Default.Schedule, infoColor)
                else -> AlertData("منتهي الصلاحية", 2, Icons.Default.Block, errorColor)
            }

            ModernAlertCard(
                title = alertData.title,
                count = alertData.count,
                icon = alertData.icon,
                color = alertData.color,
                isTablet = isTablet,
                isDesktop = isDesktop
            )
        }
    }
}

data class AlertData(
    val title: String,
    val count: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

data class SummaryData(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val trend: String? = null,
    val trendPositive: Boolean = true
)

// Modern Alert Card with enhanced design
@Composable
fun ModernAlertCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isTablet: Boolean,
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(
                when {
                    isDesktop -> 140.dp
                    isTablet -> 130.dp
                    else -> 120.dp
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    when {
                        isDesktop -> 24.dp
                        isTablet -> 20.dp
                        else -> 16.dp
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Icon with modern background
            Box(
                modifier = Modifier
                    .size(
                        when {
                            isDesktop -> 56.dp
                            isTablet -> 48.dp
                            else -> 40.dp
                        }
                    )
                    .background(
                        color.copy(alpha = 0.15f),
                        RoundedCornerShape(
                            when {
                                isDesktop -> 28.dp
                                isTablet -> 24.dp
                                else -> 20.dp
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(
                        when {
                            isDesktop -> 28.dp
                            isTablet -> 24.dp
                            else -> 20.dp
                        }
                    )
                )
            }

            // Title with responsive typography
            Text(
                text = title,
                style = when {
                    isDesktop -> MaterialTheme.typography.titleMedium
                    isTablet -> MaterialTheme.typography.titleSmall
                    else -> MaterialTheme.typography.bodyLarge
                },
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Count with enhanced styling
            Text(
                text = count.toString(),
                style = when {
                    isDesktop -> MaterialTheme.typography.headlineMedium
                    isTablet -> MaterialTheme.typography.headlineSmall
                    else -> MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// Modern Filters Section
@Composable
fun ModernFiltersSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    selectedWarehouse: String,
    onWarehouseChange: (String) -> Unit,
    isTablet: Boolean,
    isDesktop: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (isTablet || isDesktop) {
            // Horizontal layout for larger screens
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = "البحث في المنتجات...",
                    modifier = Modifier.weight(2f)
                )

                ModernFilterDropdown(
                    label = "الفئة",
                    value = selectedCategory,
                    options = listOf("الكل", "إلكترونيات", "ملابس", "مواد غذائية"),
                    onValueChange = onCategoryChange,
                    modifier = Modifier.weight(1f)
                )

                ModernFilterDropdown(
                    label = "المستودع",
                    value = selectedWarehouse,
                    options = listOf("الكل", "المستودع الرئيسي", "المستودع الفرعي"),
                    onValueChange = onWarehouseChange,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Vertical layout for mobile
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = "البحث في المنتجات...",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernFilterDropdown(
                        label = "الفئة",
                        value = selectedCategory,
                        options = listOf("الكل", "إلكترونيات", "ملابس", "مواد غذائية"),
                        onValueChange = onCategoryChange,
                        modifier = Modifier.weight(1f)
                    )

                    ModernFilterDropdown(
                        label = "المستودع",
                        value = selectedWarehouse,
                        options = listOf("الكل", "المستودع الرئيسي", "المستودع الفرعي"),
                        onValueChange = onWarehouseChange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Modern Quick Filters
@Composable
fun ModernQuickFilters(
    showLowStockOnly: Boolean,
    onLowStockToggle: () -> Unit,
    showExpiringOnly: Boolean,
    onExpiringToggle: () -> Unit,
    isTablet: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            if (isTablet) 16.dp else 12.dp
        )
    ) {
        ModernFilterChip(
            selected = showLowStockOnly,
            onClick = onLowStockToggle,
            label = "مخزون منخفض",
            icon = Icons.Default.Warning,
            selectedColor = AppTheme.colors.warning,
            isTablet = isTablet
        )

        ModernFilterChip(
            selected = showExpiringOnly,
            onClick = onExpiringToggle,
            label = "قارب على الانتهاء",
            icon = Icons.Default.Schedule,
            selectedColor = AppTheme.colors.info,
            isTablet = isTablet
        )
    }
}

// Modern Filter Chip
@Composable
fun ModernFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedColor: Color,
    isTablet: Boolean
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = true,
        label = {
            Text(
                text = label,
                style = if (isTablet) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(if (isTablet) 20.dp else 16.dp),
                tint = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (selected) selectedColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (selected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = selectedColor.copy(alpha = 0.15f),
            selectedLabelColor = selectedColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) selectedColor else MaterialTheme.colorScheme.outline,
            selectedBorderColor = selectedColor
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(if (isTablet) 48.dp else 40.dp)
    )
}

// Modern Filter Dropdown Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernFilterDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            label = {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (option == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
fun ModernInventoryOverviewContent(
    salesDataManager: SalesDataManager,
    isTablet: Boolean,
    isDesktop: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(
            when {
                isDesktop -> 24.dp
                isTablet -> 20.dp
                else -> 16.dp
            }
        )
    ) {
        item {
            // Modern Inventory Summary Cards with responsive grid
            val summaryColumns = when {
                isDesktop -> 3
                isTablet -> 2
                else -> 1
            }

            // Access theme colors in composable context first
            val primaryColor = AppTheme.colors.primary
            val infoColor = AppTheme.colors.info
            val successColor = AppTheme.colors.success

            LazyVerticalGrid(
                columns = GridCells.Fixed(summaryColumns),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(
                    when {
                        isDesktop -> 180.dp
                        isTablet -> 360.dp
                        else -> 540.dp
                    }
                )
            ) {
                // Create the summary data list using the pre-accessed colors
                val summaryDataList = listOf(
                    SummaryData(
                        title = "إجمالي قيمة المخزون",
                        value = UiUtils.formatCurrency(125000.0),
                        icon = Icons.Default.AccountBalance,
                        color = primaryColor,
                        trend = "+12.5%",
                        trendPositive = true
                    ),
                    SummaryData(
                        title = "عدد المنتجات",
                        value = "1,234",
                        icon = Icons.Default.Inventory,
                        color = infoColor,
                        trend = "+45",
                        trendPositive = true
                    ),
                    SummaryData(
                        title = "معدل دوران المخزون",
                        value = "4.2x",
                        icon = Icons.Default.TrendingUp,
                        color = successColor,
                        trend = "+0.3x",
                        trendPositive = true
                    )
                )

                items(summaryDataList) { summaryData ->
                    ModernSummaryCard(
                        title = summaryData.title,
                        value = summaryData.value,
                        icon = summaryData.icon,
                        color = summaryData.color,
                        trend = summaryData.trend,
                        trendPositive = summaryData.trendPositive,
                        isTablet = isTablet,
                        isDesktop = isDesktop
                    )
                }
            }
        }

        item {
            // Modern Low Stock Products Section
            ModernLowStockSection(
                isTablet = isTablet,
                isDesktop = isDesktop
            )
        }

        item {
            // Modern Expiring Products Section
            ModernExpiringProductsSection(
                isTablet = isTablet,
                isDesktop = isDesktop
            )
        }
    }
}

// Modern Low Stock Section
@Composable
fun ModernLowStockSection(
    isTablet: Boolean,
    isDesktop: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                when {
                    isDesktop -> 24.dp
                    isTablet -> 20.dp
                    else -> 16.dp
                }
            )
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { /* عرض الكل */ },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppTheme.colors.primary
                    )
                ) {
                    Text(
                        "عرض الكل",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "المنتجات ذات المخزون المنخفض",
                    style = when {
                        isDesktop -> MaterialTheme.typography.titleLarge
                        isTablet -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(
                when {
                    isDesktop -> 20.dp
                    isTablet -> 16.dp
                    else -> 12.dp
                }
            ))

            // Low Stock Items
            repeat(5) { index ->
                ModernLowStockProductItem(
                    productName = "منتج ${index + 1}",
                    currentStock = (index + 1) * 2,
                    minimumStock = 10,
                    category = "إلكترونيات",
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )
                if (index < 4) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = when {
                                isDesktop -> 12.dp
                                isTablet -> 10.dp
                                else -> 8.dp
                            }
                        ),
                        color = AppTheme.colors.cardBorder
                    )
                }
            }
        }
    }
}

// Modern Expiring Products Section
@Composable
fun ModernExpiringProductsSection(
    isTablet: Boolean,
    isDesktop: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                when {
                    isDesktop -> 24.dp
                    isTablet -> 20.dp
                    else -> 16.dp
                }
            )
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { /* عرض الكل */ },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppTheme.colors.primary
                    )
                ) {
                    Text(
                        "عرض الكل",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "المنتجات القاربة على الانتهاء",
                    style = when {
                        isDesktop -> MaterialTheme.typography.titleLarge
                        isTablet -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(
                when {
                    isDesktop -> 20.dp
                    isTablet -> 16.dp
                    else -> 12.dp
                }
            ))

            // Expiring Items
            repeat(3) { index ->
                ModernExpiringProductItem(
                    productName = "منتج صالح ${index + 1}",
                    expiryDate = LocalDate(2024, 12, 15 + index),
                    daysRemaining = 15 - index * 5,
                    quantity = (index + 1) * 50,
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )
                if (index < 2) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = when {
                                isDesktop -> 12.dp
                                isTablet -> 10.dp
                                else -> 8.dp
                            }
                        ),
                        color = AppTheme.colors.cardBorder
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Modern Low Stock Product Item
@Composable
fun ModernLowStockProductItem(
    productName: String,
    currentStock: Int,
    minimumStock: Int,
    category: String,
    isTablet: Boolean,
    isDesktop: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ },
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.warning.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppTheme.colors.warning.copy(alpha = 0.2f))
    ) {
        RTLRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    when {
                        isDesktop -> 20.dp
                        isTablet -> 16.dp
                        else -> 12.dp
                    }
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Action Button
            Button(
                onClick = { /* إعادة طلب */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.primary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier.height(
                    when {
                        isDesktop -> 44.dp
                        isTablet -> 40.dp
                        else -> 36.dp
                    }
                )
            ) {
                Text(
                    "إعادة طلب",
                    style = when {
                        isDesktop -> MaterialTheme.typography.labelLarge
                        isTablet -> MaterialTheme.typography.labelMedium
                        else -> MaterialTheme.typography.labelSmall
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Stock Info
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "$currentStock / $minimumStock",
                    style = when {
                        isDesktop -> MaterialTheme.typography.titleMedium
                        isTablet -> MaterialTheme.typography.titleSmall
                        else -> MaterialTheme.typography.bodyLarge
                    },
                    color = AppTheme.colors.warning,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category,
                    style = when {
                        isDesktop -> MaterialTheme.typography.bodyMedium
                        isTablet -> MaterialTheme.typography.bodySmall
                        else -> MaterialTheme.typography.bodySmall
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Product Info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = productName,
                    style = when {
                        isDesktop -> MaterialTheme.typography.titleMedium
                        isTablet -> MaterialTheme.typography.titleSmall
                        else -> MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "المخزون المتبقي",
                    style = when {
                        isDesktop -> MaterialTheme.typography.bodyMedium
                        isTablet -> MaterialTheme.typography.bodySmall
                        else -> MaterialTheme.typography.bodySmall
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Modern Expiring Product Item
@Composable
fun ModernExpiringProductItem(
    productName: String,
    expiryDate: LocalDate,
    daysRemaining: Int,
    quantity: Int,
    isTablet: Boolean,
    isDesktop: Boolean
) {
    val urgencyColor = when {
        daysRemaining <= 5 -> AppTheme.colors.error
        daysRemaining <= 15 -> AppTheme.colors.warning
        else -> AppTheme.colors.info
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Days remaining badge
        Card(
            colors = CardDefaults.cardColors(
                containerColor = urgencyColor.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.3f))
        ) {
            Text(
                text = "${daysRemaining} يوم",
                style = when {
                    isDesktop -> MaterialTheme.typography.labelLarge
                    isTablet -> MaterialTheme.typography.labelMedium
                    else -> MaterialTheme.typography.labelSmall
                },
                color = urgencyColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = when {
                        isDesktop -> 12.dp
                        isTablet -> 10.dp
                        else -> 8.dp
                    },
                    vertical = when {
                        isDesktop -> 8.dp
                        isTablet -> 6.dp
                        else -> 4.dp
                    }
                )
            )
        }

        // Quantity and date info
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "$quantity قطعة",
                style = when {
                    isDesktop -> MaterialTheme.typography.titleSmall
                    isTablet -> MaterialTheme.typography.bodyLarge
                    else -> MaterialTheme.typography.bodyMedium
                },
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.onSurface
            )
            Text(
                text = expiryDate.toString(),
                style = when {
                    isDesktop -> MaterialTheme.typography.bodyMedium
                    isTablet -> MaterialTheme.typography.bodySmall
                    else -> MaterialTheme.typography.bodySmall
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Product name
        Text(
            text = productName,
            style = when {
                isDesktop -> MaterialTheme.typography.titleMedium
                isTablet -> MaterialTheme.typography.titleSmall
                else -> MaterialTheme.typography.bodyLarge
            },
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LowStockProductItem(
    productName: String,
    currentStock: Int,
    minimumStock: Int,
    category: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(vertical = 4.dp),
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(12.dp)
    ) {
        RTLRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RTLRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* إعادة طلب */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("إعادة طلب", style = MaterialTheme.typography.bodySmall)
                }

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "$currentStock / $minimumStock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.error,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "المخزون المتبقي",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ExpiringProductItem(
    productName: String,
    expiryDate: LocalDate,
    daysRemaining: Int,
    quantity: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val urgencyColor = when {
                daysRemaining <= 5 -> AppTheme.colors.error
                daysRemaining <= 15 -> AppTheme.colors.warning
                else -> AppTheme.colors.info
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = urgencyColor.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${daysRemaining} يوم",
                    style = MaterialTheme.typography.bodySmall,
                    color = urgencyColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Column {
                Text(
                    text = "$quantity قطعة",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = expiryDate.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = productName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

// Modern Products Content with responsive grid
@Composable
fun ModernInventoryProductsContent(
    searchQuery: String,
    selectedCategory: String,
    selectedWarehouse: String,
    showLowStockOnly: Boolean,
    isTablet: Boolean,
    isDesktop: Boolean
) {
    val columns = when {
        isDesktop -> 2
        isTablet -> 1
        else -> 1
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sample products data
        items(20) { index ->
            ModernProductInventoryCard(
                product = Product(
                    id = index,
                    name = "منتج $index",
                    barcode = "123456789$index",
                    price = 100.0 + index * 10,
                    cost = 80.0 + index * 8,
                    stock = if (index % 5 == 0) 5 else 50 + index,
                    category = if (index % 3 == 0) "إلكترونيات" else "ملابس"
                ),
                inventoryItem = InventoryItem(
                    productId = index,
                    warehouseId = 1,
                    currentStock = if (index % 5 == 0) 5 else 50 + index,
                    reservedStock = index % 3,
                    minimumStock = 10,
                    maximumStock = 100,
                    reorderPoint = 15,
                    lastUpdated = LocalDateTime(2024, 1, 1, 10, 0),
                    expiryDate = if (index % 7 == 0) LocalDate(2024, 12, 15) else null
                ),
                isTablet = isTablet,
                isDesktop = isDesktop
            )
        }
    }
}

// Modern Stock Movements Content
@Composable
fun ModernStockMovementsContent(
    isTablet: Boolean,
    isDesktop: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(
            when {
                isDesktop -> 16.dp
                isTablet -> 12.dp
                else -> 8.dp
            }
        )
    ) {
        items(15) { index ->
            ModernStockMovementCard(
                movement = StockMovement(
                    id = index,
                    productId = index,
                    warehouseId = 1,
                    movementType = MovementType.values()[index % MovementType.values().size],
                    quantity = (index + 1) * 5,
                    date = LocalDateTime(2024, 1, 1 + index, 10, 0),
                    reference = "REF-${1000 + index}",
                    notes = "ملاحظة حول الحركة رقم $index"
                ),
                productName = "منتج $index",
                isTablet = isTablet,
                isDesktop = isDesktop
            )
        }
    }
}

// Modern Warehouses Content
@Composable
fun ModernWarehousesContent(
    isTablet: Boolean,
    isDesktop: Boolean
) {
    val columns = when {
        isDesktop -> 2
        isTablet -> 1
        else -> 1
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(3) { index ->
            ModernWarehouseCard(
                warehouse = Warehouse(
                    id = index,
                    name = "المستودع ${if (index == 0) "الرئيسي" else "الفرعي $index"}",
                    location = "الرياض - حي النخيل",
                    manager = "أحمد محمد"
                ),
                totalProducts = 500 + index * 100,
                totalValue = 125000.0 + index * 50000,
                isTablet = isTablet,
                isDesktop = isDesktop
            )
        }
    }
}

@Composable
fun InventoryProductsContent(
    searchQuery: String,
    selectedCategory: String,
    selectedWarehouse: String,
    showLowStockOnly: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sample products data
        items(20) { index ->
            ProductInventoryCard(
                product = Product(
                    id = index,
                    name = "منتج $index",
                    barcode = "123456789$index",
                    price = 100.0 + index * 10,
                    cost = 80.0 + index * 8,
                    stock = if (index % 5 == 0) 5 else 50 + index,
                    category = if (index % 3 == 0) "إلكترونيات" else "ملابس"
                ),
                inventoryItem = InventoryItem(
                    productId = index,
                    warehouseId = 1,
                    currentStock = if (index % 5 == 0) 5 else 50 + index,
                    reservedStock = index % 3,
                    minimumStock = 10,
                    maximumStock = 100,
                    reorderPoint = 15,
                    lastUpdated = LocalDateTime(2024, 1, 1, 10, 0),
                    expiryDate = if (index % 7 == 0) LocalDate(2024, 12, 15) else null
                )
            )
        }
    }
}

// Modern Product Inventory Card
@Composable
fun ModernProductInventoryCard(
    product: Product,
    inventoryItem: InventoryItem,
    isTablet: Boolean,
    isDesktop: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(
                when {
                    isDesktop -> 24.dp
                    isTablet -> 20.dp
                    else -> 16.dp
                }
            )
        ) {
            // Header with actions and product info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { /* تعديل */ },
                        modifier = Modifier.size(
                            when {
                                isDesktop -> 40.dp
                                isTablet -> 36.dp
                                else -> 32.dp
                            }
                        ),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = AppTheme.colors.primary.copy(alpha = 0.1f),
                            contentColor = AppTheme.colors.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            modifier = Modifier.size(
                                when {
                                    isDesktop -> 20.dp
                                    isTablet -> 18.dp
                                    else -> 16.dp
                                }
                            )
                        )
                    }

                    IconButton(
                        onClick = { /* عرض التفاصيل */ },
                        modifier = Modifier.size(
                            when {
                                isDesktop -> 40.dp
                                isTablet -> 36.dp
                                else -> 32.dp
                            }
                        ),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = AppTheme.colors.info.copy(alpha = 0.1f),
                            contentColor = AppTheme.colors.info
                        )
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "عرض",
                            modifier = Modifier.size(
                                when {
                                    isDesktop -> 20.dp
                                    isTablet -> 18.dp
                                    else -> 16.dp
                                }
                            )
                        )
                    }
                }

                // Product info
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = product.name,
                        style = when {
                            isDesktop -> MaterialTheme.typography.titleLarge
                            isTablet -> MaterialTheme.typography.titleMedium
                            else -> MaterialTheme.typography.titleSmall
                        },
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.category,
                        style = when {
                            isDesktop -> MaterialTheme.typography.bodyLarge
                            isTablet -> MaterialTheme.typography.bodyMedium
                            else -> MaterialTheme.typography.bodySmall
                        },
                        color = AppTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = "الباركود: ${product.barcode}",
                        style = when {
                            isDesktop -> MaterialTheme.typography.bodyMedium
                            isTablet -> MaterialTheme.typography.bodySmall
                            else -> MaterialTheme.typography.bodySmall
                        },
                        color = AppTheme.colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(
                when {
                    isDesktop -> 20.dp
                    isTablet -> 16.dp
                    else -> 12.dp
                }
            ))

            // Stock information grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ModernStockInfoItem(
                    label = "المتوفر",
                    value = inventoryItem.currentStock.toString(),
                    color = UiUtils.getStockStatusColor(inventoryItem.currentStock),
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )

                ModernStockInfoItem(
                    label = "المحجوز",
                    value = inventoryItem.reservedStock.toString(),
                    color = AppTheme.colors.warning,
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )

                ModernStockInfoItem(
                    label = "الحد الأدنى",
                    value = inventoryItem.minimumStock.toString(),
                    color = AppTheme.colors.error,
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )

                ModernStockInfoItem(
                    label = "نقطة الطلب",
                    value = inventoryItem.reorderPoint.toString(),
                    color = AppTheme.colors.warning,
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )

                ModernStockInfoItem(
                    label = "الحد الأقصى",
                    value = inventoryItem.maximumStock.toString(),
                    color = AppTheme.colors.info,
                    isTablet = isTablet,
                    isDesktop = isDesktop
                )
            }

            Spacer(modifier = Modifier.height(
                when {
                    isDesktop -> 16.dp
                    isTablet -> 12.dp
                    else -> 8.dp
                }
            ))

            // Price information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "هامش الربح: ${UiUtils.formatPercentage(((product.price - product.cost) / product.cost) * 100)}",
                    style = when {
                        isDesktop -> MaterialTheme.typography.bodyMedium
                        isTablet -> MaterialTheme.typography.bodySmall
                        else -> MaterialTheme.typography.bodySmall
                    },
                    color = AppTheme.colors.success,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "السعر: ${UiUtils.formatCurrency(product.price)}",
                    style = when {
                        isDesktop -> MaterialTheme.typography.titleMedium
                        isTablet -> MaterialTheme.typography.titleSmall
                        else -> MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.onSurface
                )
            }

            // Expiry date if applicable
            inventoryItem.expiryDate?.let { expiryDate ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.warning.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AppTheme.colors.warning.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AppTheme.colors.warning,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "تاريخ الانتهاء: $expiryDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.warning,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductInventoryCard(
    product: Product,
    inventoryItem: InventoryItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardStyles.defaultCardColors(),
        elevation = CardStyles.defaultCardElevation()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { /* تعديل */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { /* عرض التفاصيل */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "عرض",
                            tint = AppTheme.colors.info,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Product Info
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "الباركود: ${product.barcode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StockInfoItem(
                    label = "الحد الأقصى",
                    value = inventoryItem.maximumStock.toString(),
                    color = AppTheme.colors.info
                )

                StockInfoItem(
                    label = "نقطة الطلب",
                    value = inventoryItem.reorderPoint.toString(),
                    color = AppTheme.colors.warning
                )

                StockInfoItem(
                    label = "الحد الأدنى",
                    value = inventoryItem.minimumStock.toString(),
                    color = AppTheme.colors.error
                )

                StockInfoItem(
                    label = "المحجوز",
                    value = inventoryItem.reservedStock.toString(),
                    color = AppTheme.colors.purple
                )

                StockInfoItem(
                    label = "المتوفر",
                    value = inventoryItem.currentStock.toString(),
                    color = UiUtils.getStockStatusColor(inventoryItem.currentStock)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "هامش الربح: ${UiUtils.formatPercentage(((product.price - product.cost) / product.cost) * 100)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.success
                )

                Text(
                    text = "السعر: ${UiUtils.formatCurrency(product.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Expiry Date if applicable
            inventoryItem.expiryDate?.let { expiryDate ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.warning.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "تاريخ الانتهاء: $expiryDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.warning
                        )
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AppTheme.colors.warning,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Modern Stock Info Item
@Composable
fun ModernStockInfoItem(
    label: String,
    value: String,
    color: Color,
    isTablet: Boolean,
    isDesktop: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = when {
                isDesktop -> MaterialTheme.typography.titleMedium
                isTablet -> MaterialTheme.typography.titleSmall
                else -> MaterialTheme.typography.bodyLarge
            },
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = when {
                isDesktop -> MaterialTheme.typography.bodyMedium
                isTablet -> MaterialTheme.typography.bodySmall
                else -> MaterialTheme.typography.bodySmall
            },
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Modern Stock Movement Card
@Composable
fun ModernStockMovementCard(
    movement: StockMovement,
    productName: String,
    isTablet: Boolean,
    isDesktop: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    when {
                        isDesktop -> 20.dp
                        isTablet -> 16.dp
                        else -> 12.dp
                    }
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reference and date
            Column {
                Text(
                    text = movement.reference,
                    style = when {
                        isDesktop -> MaterialTheme.typography.bodyLarge
                        isTablet -> MaterialTheme.typography.bodyMedium
                        else -> MaterialTheme.typography.bodySmall
                    },
                    color = AppTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = movement.date.toString(),
                    style = when {
                        isDesktop -> MaterialTheme.typography.bodyMedium
                        isTablet -> MaterialTheme.typography.bodySmall
                        else -> MaterialTheme.typography.bodySmall
                    },
                    color = AppTheme.colors.onSurfaceVariant
                )
            }

            // Quantity badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = getMovementTypeColor(movement.movementType).copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, getMovementTypeColor(movement.movementType).copy(alpha = 0.3f))
            ) {
                Text(
                    text = "${if (movement.movementType == MovementType.SALE) "-" else "+"}${movement.quantity}",
                    style = when {
                        isDesktop -> MaterialTheme.typography.titleMedium
                        isTablet -> MaterialTheme.typography.titleSmall
                        else -> MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.Bold,
                    color = getMovementTypeColor(movement.movementType),
                    modifier = Modifier.padding(
                        horizontal = when {
                            isDesktop -> 16.dp
                            isTablet -> 12.dp
                            else -> 8.dp
                        },
                        vertical = when {
                            isDesktop -> 8.dp
                            isTablet -> 6.dp
                            else -> 4.dp
                        }
                    )
                )
            }

            // Product name and movement type
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = productName,
                    style = when {
                        isDesktop -> MaterialTheme.typography.titleMedium
                        isTablet -> MaterialTheme.typography.titleSmall
                        else -> MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = movement.movementType.displayName,
                    style = when {
                        isDesktop -> MaterialTheme.typography.bodyLarge
                        isTablet -> MaterialTheme.typography.bodyMedium
                        else -> MaterialTheme.typography.bodySmall
                    },
                    color = getMovementTypeColor(movement.movementType),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Modern Warehouse Card
@Composable
fun ModernWarehouseCard(
    warehouse: Warehouse,
    totalProducts: Int,
    totalValue: Double,
    isTablet: Boolean,
    isDesktop: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, AppTheme.colors.cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(
                when {
                    isDesktop -> 24.dp
                    isTablet -> 20.dp
                    else -> 16.dp
                }
            )
        ) {
            // Header with settings and warehouse info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = { /* إدارة المستودع */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = AppTheme.colors.primary.copy(alpha = 0.1f),
                        contentColor = AppTheme.colors.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "إدارة",
                        modifier = Modifier.size(
                            when {
                                isDesktop -> 24.dp
                                isTablet -> 20.dp
                                else -> 18.dp
                            }
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = warehouse.name,
                        style = when {
                            isDesktop -> MaterialTheme.typography.headlineSmall
                            isTablet -> MaterialTheme.typography.titleLarge
                            else -> MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.onSurface
                    )
                    Text(
                        text = warehouse.location,
                        style = when {
                            isDesktop -> MaterialTheme.typography.bodyLarge
                            isTablet -> MaterialTheme.typography.bodyMedium
                            else -> MaterialTheme.typography.bodySmall
                        },
                        color = AppTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = "المدير: ${warehouse.manager}",
                        style = when {
                            isDesktop -> MaterialTheme.typography.bodyMedium
                            isTablet -> MaterialTheme.typography.bodySmall
                            else -> MaterialTheme.typography.bodySmall
                        },
                        color = AppTheme.colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(
                when {
                    isDesktop -> 24.dp
                    isTablet -> 20.dp
                    else -> 16.dp
                }
            ))

            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalProducts.toString(),
                        style = when {
                            isDesktop -> MaterialTheme.typography.headlineMedium
                            isTablet -> MaterialTheme.typography.headlineSmall
                            else -> MaterialTheme.typography.titleLarge
                        },
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.info
                    )
                    Text(
                        text = "عدد المنتجات",
                        style = when {
                            isDesktop -> MaterialTheme.typography.bodyLarge
                            isTablet -> MaterialTheme.typography.bodyMedium
                            else -> MaterialTheme.typography.bodySmall
                        },
                        color = AppTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = UiUtils.formatCurrency(totalValue),
                        style = when {
                            isDesktop -> MaterialTheme.typography.headlineMedium
                            isTablet -> MaterialTheme.typography.headlineSmall
                            else -> MaterialTheme.typography.titleLarge
                        },
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "إجمالي القيمة",
                        style = when {
                            isDesktop -> MaterialTheme.typography.bodyLarge
                            isTablet -> MaterialTheme.typography.bodyMedium
                            else -> MaterialTheme.typography.bodySmall
                        },
                        color = AppTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


// Modern Summary Card with enhanced design
@Composable
fun ModernSummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    trend: String? = null,
    trendPositive: Boolean = true,
    isTablet: Boolean,
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(
                when {
                    isDesktop -> 180.dp
                    isTablet -> 160.dp
                    else -> 140.dp
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    when {
                        isDesktop -> 24.dp
                        isTablet -> 20.dp
                        else -> 16.dp
                    }
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Trend indicator
                trend?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = (if (trendPositive) AppTheme.colors.success else AppTheme.colors.error).copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = trend,
                            style = when {
                                isDesktop -> MaterialTheme.typography.labelMedium
                                isTablet -> MaterialTheme.typography.labelSmall
                                else -> MaterialTheme.typography.labelSmall
                            },
                            color = if (trendPositive) AppTheme.colors.success else AppTheme.colors.error,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Icon with background
                Box(
                    modifier = Modifier
                        .size(
                            when {
                                isDesktop -> 56.dp
                                isTablet -> 48.dp
                                else -> 40.dp
                            }
                        )
                        .background(
                            color.copy(alpha = 0.15f),
                            RoundedCornerShape(
                                when {
                                    isDesktop -> 28.dp
                                    isTablet -> 24.dp
                                    else -> 20.dp
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(
                            when {
                                isDesktop -> 28.dp
                                isTablet -> 24.dp
                                else -> 20.dp
                            }
                        )
                    )
                }
            }

            // Title and Value
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = title,
                    style = when {
                        isDesktop -> MaterialTheme.typography.bodyLarge
                        isTablet -> MaterialTheme.typography.bodyMedium
                        else -> MaterialTheme.typography.bodySmall
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    style = when {
                        isDesktop -> MaterialTheme.typography.headlineMedium
                        isTablet -> MaterialTheme.typography.headlineSmall
                        else -> MaterialTheme.typography.titleLarge
                    },
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Missing StockInfoItem Component
@Composable
fun StockInfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Movement Type Color Function
@Composable
fun getMovementTypeColor(movementType: MovementType): Color {
    return when (movementType) {
        MovementType.PURCHASE -> AppTheme.colors.success
        MovementType.SALE -> AppTheme.colors.info
        MovementType.RETURN -> AppTheme.colors.warning
        MovementType.ADJUSTMENT -> AppTheme.colors.purple
        MovementType.TRANSFER -> AppTheme.colors.teal
        MovementType.DAMAGED -> AppTheme.colors.error
        MovementType.EXPIRED -> AppTheme.colors.error
    }
}

