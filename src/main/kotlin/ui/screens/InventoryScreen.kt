package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import data.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import UiUtils
import ui.components.*
import ui.components.RTLProvider
import ui.components.RTLRow
import ui.theme.CardStyles
import ui.theme.AppTheme
import ui.utils.ColorUtils

// Inventory Tab Enum
enum class InventoryTab(val title: String) {
    OVERVIEW("نظرة عامة"),
    PRODUCTS("المنتجات"),
    MOVEMENTS("حركات المخزون"),
    WAREHOUSES("المستودعات")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    salesDataManager: SalesDataManager,
    inventoryExportService: InventoryExportService? = null
) {
    RTLProvider {
        // Enhanced state management
        var selectedTab by remember { mutableStateOf(InventoryTab.OVERVIEW) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("الكل") }
        var selectedWarehouse by remember { mutableStateOf("الكل") }
        var selectedStatus by remember { mutableStateOf("الكل") }
        var sortBy by remember { mutableStateOf("name") }
        var showLowStockOnly by remember { mutableStateOf(false) }
        var showExpiringOnly by remember { mutableStateOf(false) }
        var isExporting by remember { mutableStateOf(false) }
        var exportMessage by remember { mutableStateOf<String?>(null) }

        // Dialog states
        var showAddItemDialog by remember { mutableStateOf(false) }
        var editingItem by remember { mutableStateOf<InventoryItem?>(null) }
        var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
        var showItemDetails by remember { mutableStateOf(false) }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }

        // For desktop application, we'll use window size detection
        val isTablet = true // Assume tablet/desktop for now
        val isDesktop = true // Desktop application

        // Snackbar state
        val snackbarHostState = remember { SnackbarHostState() }

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

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Inventory Management
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
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إدارة المخزون",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = { showAddItemDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إضافة عنصر جديد")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Enhanced Tabs
                        EnhancedTabRow(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search and Filter Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Search Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("البحث في المخزون") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )

                            // Category Filter
                            EnhancedFilterDropdown(
                                label = "الفئة",
                                value = selectedCategory,
                                options = listOf("الكل", "إلكترونيات", "ملابس", "مواد غذائية", "أدوات"),
                                onValueChange = { selectedCategory = it },
                                modifier = Modifier.weight(0.7f)
                            )

                            // Warehouse Filter
                            EnhancedFilterDropdown(
                                label = "المستودع",
                                value = selectedWarehouse,
                                options = listOf("الكل", "المستودع الرئيسي", "مستودع فرعي 1", "مستودع فرعي 2"),
                                onValueChange = { selectedWarehouse = it },
                                modifier = Modifier.weight(0.7f)
                            )
                        }

                        // Sort and Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sort Dropdown
                            EnhancedFilterDropdown(
                                label = "ترتيب حسب",
                                value = when(sortBy) {
                                    "name" -> "الاسم"
                                    "stock" -> "المخزون"
                                    "category" -> "الفئة"
                                    "warehouse" -> "المستودع"
                                    else -> "الاسم"
                                },
                                options = listOf("الاسم", "المخزون", "الفئة", "المستودع"),
                                onValueChange = {
                                    sortBy = when(it) {
                                        "الاسم" -> "name"
                                        "المخزون" -> "stock"
                                        "الفئة" -> "category"
                                        "المستودع" -> "warehouse"
                                        else -> "name"
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Enhanced Quick Filters with complete hover coverage
                            val lowStockInteractionSource = remember { MutableInteractionSource() }
                            val isLowStockHovered by lowStockInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = when {
                                            showLowStockOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            isLowStockHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (showLowStockOnly) 1.5.dp else if (isLowStockHovered) 1.dp else 0.5.dp,
                                        color = when {
                                            showLowStockOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            isLowStockHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = lowStockInteractionSource,
                                        indication = null
                                    ) { showLowStockOnly = !showLowStockOnly },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    if (showLowStockOnly) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "مخزون منخفض",
                                        color = when {
                                            showLowStockOnly -> MaterialTheme.colorScheme.primary
                                            isLowStockHovered -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            val expiringInteractionSource = remember { MutableInteractionSource() }
                            val isExpiringHovered by expiringInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = when {
                                            showExpiringOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            isExpiringHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (showExpiringOnly) 1.5.dp else if (isExpiringHovered) 1.dp else 0.5.dp,
                                        color = when {
                                            showExpiringOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            isExpiringHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = expiringInteractionSource,
                                        indication = null
                                    ) { showExpiringOnly = !showExpiringOnly },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    if (showExpiringOnly) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "قارب على الانتهاء",
                                        color = when {
                                            showExpiringOnly -> MaterialTheme.colorScheme.primary
                                            isExpiringHovered -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            // Enhanced Export Button with complete hover coverage
                            val exportInteractionSource = remember { MutableInteractionSource() }
                            val isExportHovered by exportInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = if (isExportHovered && !isExporting)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else
                                            MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (isExportHovered && !isExporting) 1.5.dp else 1.dp,
                                        color = if (isExportHovered && !isExporting)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = exportInteractionSource,
                                        indication = null,
                                        enabled = !isExporting
                                    ) { if (!isExporting) handleExportExcel() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    if (isExporting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.FileDownload,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = if (isExportHovered)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        "تصدير",
                                        color = if (isExporting)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        else if (isExportHovered)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Content based on selected tab
                        when (selectedTab) {
                            InventoryTab.OVERVIEW -> EnhancedInventoryOverviewContent(
                                salesDataManager = salesDataManager,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                selectedWarehouse = selectedWarehouse,
                                showLowStockOnly = showLowStockOnly,
                                showExpiringOnly = showExpiringOnly,
                                sortBy = sortBy,
                                onItemClick = { item ->
                                    selectedItem = item
                                    showItemDetails = true
                                }
                            )
                            InventoryTab.PRODUCTS -> EnhancedInventoryProductsContent(
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                selectedWarehouse = selectedWarehouse,
                                showLowStockOnly = showLowStockOnly,
                                sortBy = sortBy,
                                onItemClick = { item ->
                                    selectedItem = item
                                    showItemDetails = true
                                },
                                onEditItem = { item ->
                                    editingItem = item
                                },
                                onDeleteItem = { item ->
                                    itemToDelete = item
                                    showDeleteConfirmation = true
                                }
                            )
                            InventoryTab.MOVEMENTS -> EnhancedStockMovementsContent(
                                searchQuery = searchQuery,
                                selectedWarehouse = selectedWarehouse
                            )
                            InventoryTab.WAREHOUSES -> EnhancedWarehousesContent(
                                searchQuery = searchQuery,
                                onWarehouseClick = { warehouse ->
                                    selectedWarehouse = warehouse
                                    selectedTab = InventoryTab.PRODUCTS
                                }
                            )
                        }
                    }
                }

                // Right Panel - Details and Statistics (when item selected)
                AnimatedVisibility(
                    visible = showItemDetails && selectedItem != null,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut()
                ) {
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
                        selectedItem?.let { item ->
                            EnhancedInventoryItemDetailsPanel(
                                item = item,
                                onEdit = {
                                    editingItem = item
                                    showItemDetails = false
                                },
                                onDelete = {
                                    itemToDelete = item
                                    showDeleteConfirmation = true
                                    showItemDetails = false
                                },
                                onClose = {
                                    showItemDetails = false
                                    selectedItem = null
                                }
                            )
                        }
                    }
                }
            }

            // Export status message
            exportMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("نجاح"))
                            AppTheme.colors.success.copy(alpha = 0.9f)
                        else
                            AppTheme.colors.error.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (message.contains("نجاح")) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { exportMessage = null }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Snackbar
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Dialogs
        if (showAddItemDialog) {
            InventoryItemDialog(
                item = null,
                onDismiss = { showAddItemDialog = false },
                onSave = { item ->
                    // Handle adding new inventory item
                    showAddItemDialog = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تم إضافة العنصر بنجاح")
                    }
                }
            )
        }

        if (editingItem != null) {
            InventoryItemDialog(
                item = editingItem,
                onDismiss = { editingItem = null },
                onSave = { item ->
                    // Handle updating inventory item
                    editingItem = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تم تحديث العنصر بنجاح")
                    }
                }
            )
        }

        if (showDeleteConfirmation && itemToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                    itemToDelete = null
                },
                title = { Text("تأكيد الحذف") },
                text = { Text("هل أنت متأكد من حذف هذا العنصر من المخزون؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            // Handle deleting inventory item
                            showDeleteConfirmation = false
                            itemToDelete = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("تم حذف العنصر بنجاح")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirmation = false
                            itemToDelete = null
                        }
                    ) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

// Enhanced Tab Row Component
@Composable
private fun EnhancedTabRow(
    selectedTab: InventoryTab,
    onTabSelected: (InventoryTab) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
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
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            when (tab) {
                                InventoryTab.OVERVIEW -> Icons.Default.Dashboard
                                InventoryTab.PRODUCTS -> Icons.Default.Inventory
                                InventoryTab.MOVEMENTS -> Icons.Default.SwapHoriz
                                InventoryTab.WAREHOUSES -> Icons.Default.Warehouse
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}

// Enhanced Filter Dropdown Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedFilterDropdown(
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
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
// Enhanced Inventory Overview Content
@Composable
private fun EnhancedInventoryOverviewContent(
    salesDataManager: SalesDataManager,
    searchQuery: String,
    selectedCategory: String,
    selectedWarehouse: String,
    showLowStockOnly: Boolean,
    showExpiringOnly: Boolean,
    sortBy: String,
    onItemClick: (InventoryItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedStatCard(
                    title = "إجمالي المنتجات",
                    value = "1,234",
                    subtitle = "منتج متاح",
                    icon = Icons.Default.Inventory,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                EnhancedStatCard(
                    title = "مخزون منخفض",
                    value = "23",
                    subtitle = "منتج يحتاج تجديد",
                    icon = Icons.Default.Warning,
                    iconColor = AppTheme.colors.warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedStatCard(
                    title = "قارب على الانتهاء",
                    value = "8",
                    subtitle = "منتج ينتهي قريباً",
                    icon = Icons.Default.Schedule,
                    iconColor = AppTheme.colors.error,
                    modifier = Modifier.weight(1f)
                )

                EnhancedStatCard(
                    title = "المستودعات",
                    value = "4",
                    subtitle = "مستودع نشط",
                    icon = Icons.Default.Warehouse,
                    iconColor = AppTheme.colors.info,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Inventory Items
        item {
            Text(
                text = "العناصر الحديثة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Sample inventory items
        items(10) { index ->
            EnhancedInventoryItemCard(
                item = InventoryItem(
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
                productName = "منتج $index",
                categoryName = if (index % 3 == 0) "إلكترونيات" else "ملابس",
                warehouseName = "المستودع الرئيسي",
                onClick = onItemClick
            )
        }
    }
}
// Enhanced Inventory Products Content
@Composable
private fun EnhancedInventoryProductsContent(
    searchQuery: String,
    selectedCategory: String,
    selectedWarehouse: String,
    showLowStockOnly: Boolean,
    sortBy: String,
    onItemClick: (InventoryItem) -> Unit,
    onEditItem: (InventoryItem) -> Unit,
    onDeleteItem: (InventoryItem) -> Unit
) {
    // Filter and sort inventory items
    val filteredItems = remember(searchQuery, selectedCategory, selectedWarehouse, showLowStockOnly, sortBy) {
        // Sample data - in real app, this would come from ViewModel
        val sampleItems = (0..20).map { index ->
            InventoryItem(
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
        }

        var filtered = sampleItems

        // Apply filters
        if (showLowStockOnly) {
            filtered = filtered.filter { it.currentStock <= it.minimumStock }
        }

        // Apply sorting
        when (sortBy) {
            "stock" -> filtered.sortedBy { it.currentStock }
            "warehouse" -> filtered.sortedBy { it.warehouseId }
            else -> filtered.sortedBy { it.productId }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filteredItems) { item ->
            EnhancedInventoryItemCard(
                item = item,
                productName = "منتج ${item.productId}",
                categoryName = if (item.productId % 3 == 0) "إلكترونيات" else "ملابس",
                warehouseName = "المستودع الرئيسي",
                onClick = onItemClick,
                onEdit = onEditItem,
                onDelete = onDeleteItem,
                showActions = true
            )
        }
    }
}
// Enhanced Stock Movements Content
@Composable
private fun EnhancedStockMovementsContent(
    searchQuery: String,
    selectedWarehouse: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sample stock movements
        items(15) { index ->
            EnhancedStockMovementCard(
                movement = StockMovement(
                    id = index,
                    productId = index,
                    warehouseId = 1,
                    movementType = when (index % 4) {
                        0 -> MovementType.PURCHASE
                        1 -> MovementType.SALE
                        2 -> MovementType.RETURN
                        else -> MovementType.ADJUSTMENT
                    },
                    quantity = (index + 1) * 5,
                    date = LocalDateTime(2024, 1, index + 1, 10, 0),
                    reference = "REF-${1000 + index}",
                    notes = "ملاحظة حول الحركة $index"
                ),
                productName = "منتج $index",
                warehouseName = "المستودع الرئيسي"
            )
        }
    }
}

// Enhanced Warehouses Content
@Composable
private fun EnhancedWarehousesContent(
    searchQuery: String,
    onWarehouseClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sample warehouses
        items(4) { index ->
            EnhancedWarehouseCard(
                warehouse = Warehouse(
                    id = index,
                    name = "المستودع ${if (index == 0) "الرئيسي" else "الفرعي $index"}",
                    location = "الموقع $index",
                    manager = "مدير المستودع $index"
                ),
                totalProducts = 100 + index * 50,
                lowStockItems = index * 2,
                onClick = { onWarehouseClick("المستودع ${if (index == 0) "الرئيسي" else "الفرعي $index"}") }
            )
        }
    }
}

// Enhanced Stat Card Component
@Composable
private fun EnhancedStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
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
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
// Enhanced Inventory Item Card Component
@Composable
private fun EnhancedInventoryItemCard(
    item: InventoryItem,
    productName: String,
    categoryName: String,
    warehouseName: String,
    onClick: (InventoryItem) -> Unit,
    onEdit: ((InventoryItem) -> Unit)? = null,
    onDelete: ((InventoryItem) -> Unit)? = null,
    showActions: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = if (isHovered)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick(item) }
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showActions && (onEdit != null || onDelete != null)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        onEdit?.let { editAction ->
                            IconButton(
                                onClick = { editAction(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "تعديل",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        onDelete?.let { deleteAction ->
                            IconButton(
                                onClick = { deleteAction(item) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Stock Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المخزون الحالي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.currentStock}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            item.currentStock <= item.minimumStock -> MaterialTheme.colorScheme.error
                            item.currentStock <= item.reorderPoint -> AppTheme.colors.warning
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }

                // Stock Status Indicator
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            item.currentStock <= item.minimumStock -> MaterialTheme.colorScheme.errorContainer
                            item.currentStock <= item.reorderPoint -> AppTheme.colors.warning.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            when {
                                item.currentStock <= item.minimumStock -> Icons.Default.Warning
                                item.currentStock <= item.reorderPoint -> Icons.Default.Info
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when {
                                item.currentStock <= item.minimumStock -> MaterialTheme.colorScheme.onErrorContainer
                                item.currentStock <= item.reorderPoint -> AppTheme.colors.warning
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                        Text(
                            text = when {
                                item.currentStock <= item.minimumStock -> "مخزون منخفض"
                                item.currentStock <= item.reorderPoint -> "يحتاج إعادة طلب"
                                else -> "متوفر"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                item.currentStock <= item.minimumStock -> MaterialTheme.colorScheme.onErrorContainer
                                item.currentStock <= item.reorderPoint -> AppTheme.colors.warning
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            }

            // Additional Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "المستودع",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = warehouseName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "الحد الأدنى",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.minimumStock}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
// Enhanced Stock Movement Card Component
@Composable
private fun EnhancedStockMovementCard(
    movement: StockMovement,
    productName: String,
    warehouseName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = movement.reference,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (movement.movementType) {
                            MovementType.PURCHASE -> MaterialTheme.colorScheme.primaryContainer
                            MovementType.SALE -> AppTheme.colors.success.copy(alpha = 0.2f)
                            MovementType.RETURN -> AppTheme.colors.warning.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = movement.movementType.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = when (movement.movementType) {
                            MovementType.PURCHASE -> MaterialTheme.colorScheme.onPrimaryContainer
                            MovementType.SALE -> AppTheme.colors.success
                            MovementType.RETURN -> AppTheme.colors.warning
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "الكمية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${movement.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "المستودع",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = warehouseName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "التاريخ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${movement.date.dayOfMonth}/${movement.date.monthNumber}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Enhanced Warehouse Card Component
@Composable
private fun EnhancedWarehouseCard(
    warehouse: Warehouse,
    totalProducts: Int,
    lowStockItems: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = if (isHovered)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = warehouse.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = warehouse.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    Icons.Default.Warehouse,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "إجمالي المنتجات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$totalProducts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = "مخزون منخفض",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$lowStockItems",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (lowStockItems > 0) MaterialTheme.colorScheme.error else AppTheme.colors.success
                    )
                }

                Column {
                    Text(
                        text = "المدير",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = warehouse.manager,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Enhanced Inventory Item Details Panel
@Composable
private fun EnhancedInventoryItemDetailsPanel(
    item: InventoryItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تفاصيل عنصر المخزون",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Stock Information Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "معلومات المخزون",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "المخزون الحالي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.currentStock}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text(
                            text = "المحجوز",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.reservedStock}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column {
                        Text(
                            text = "المتاح",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${item.currentStock - item.reservedStock}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.success
                        )
                    }
                }
            }
        }

        // Stock Levels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "الحد الأدنى",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.minimumStock}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = AppTheme.colors.success,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "الحد الأقصى",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${item.maximumStock}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.success
                    )
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تعديل")
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "حذف",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Inventory Item Dialog Component
@Composable
private fun InventoryItemDialog(
    item: InventoryItem?,
    onDismiss: () -> Unit,
    onSave: (InventoryItem) -> Unit
) {
    var currentStock by remember { mutableStateOf(item?.currentStock?.toString() ?: "") }
    var minimumStock by remember { mutableStateOf(item?.minimumStock?.toString() ?: "") }
    var maximumStock by remember { mutableStateOf(item?.maximumStock?.toString() ?: "") }
    var reorderPoint by remember { mutableStateOf(item?.reorderPoint?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (item == null) "إضافة عنصر جديد" else "تعديل عنصر المخزون",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = currentStock,
                    onValueChange = { currentStock = it },
                    label = { Text("المخزون الحالي") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = minimumStock,
                    onValueChange = { minimumStock = it },
                    label = { Text("الحد الأدنى") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maximumStock,
                    onValueChange = { maximumStock = it },
                    label = { Text("الحد الأقصى") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reorderPoint,
                    onValueChange = { reorderPoint = it },
                    label = { Text("نقطة إعادة الطلب") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val newItem = InventoryItem(
                                productId = item?.productId ?: 0,
                                warehouseId = item?.warehouseId ?: 1,
                                currentStock = currentStock.toIntOrNull() ?: 0,
                                reservedStock = item?.reservedStock ?: 0,
                                minimumStock = minimumStock.toIntOrNull() ?: 0,
                                maximumStock = maximumStock.toIntOrNull() ?: 100,
                                reorderPoint = reorderPoint.toIntOrNull() ?: 10,
                                lastUpdated = LocalDateTime(2024, 1, 1, 10, 0),
                                expiryDate = item?.expiryDate
                            )
                            onSave(newItem)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

