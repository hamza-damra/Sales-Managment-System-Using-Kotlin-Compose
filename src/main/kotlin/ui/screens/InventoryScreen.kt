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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import data.*
import data.api.*
import data.api.services.StockMovementDTO
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import UiUtils
import ui.components.*
import ui.components.RTLProvider
import ui.components.RTLRow
import ui.components.EnhancedFilterDropdown
import ui.theme.CardStyles
import ui.theme.AppTheme
import ui.utils.ColorUtils
import ui.viewmodels.InventoryViewModel

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
    inventoryViewModel: InventoryViewModel,
    inventoryExportService: InventoryExportService? = null
) {
    RTLProvider {
        // Collect ViewModel state
        val uiState by inventoryViewModel.uiState.collectAsState()
        val inventories = uiState.inventories
        val isLoading = uiState.isLoading
        val error = uiState.error

        // Enhanced state management
        var selectedTab by remember { mutableStateOf(InventoryTab.OVERVIEW) }
        var searchQuery by inventoryViewModel.searchQuery
        var selectedCategory by remember { mutableStateOf("الكل") }
        var selectedWarehouse by remember { mutableStateOf("الكل") }
        var selectedStatus by inventoryViewModel.selectedStatus
        var sortBy by inventoryViewModel.sortBy
        var showLowStockOnly by remember { mutableStateOf(false) }
        var showExpiringOnly by remember { mutableStateOf(false) }
        var isExporting by remember { mutableStateOf(false) }
        var exportMessage by remember { mutableStateOf<String?>(null) }

        // Load data on first composition
        LaunchedEffect(Unit) {
            inventoryViewModel.loadInventories()
            inventoryViewModel.loadCategories()
        }

        // Dialog states
        var showAddWarehouseDialog by inventoryViewModel.showCreateDialog
        var editingWarehouse by inventoryViewModel.selectedInventory
        var showEditWarehouseDialog by inventoryViewModel.showEditDialog
        var showDeleteWarehouseDialog by inventoryViewModel.showDeleteDialog
        var selectedItem by remember { mutableStateOf<InventoryItem?>(null) }
        var showItemDetails by remember { mutableStateOf(false) }



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
                                onClick = {
                                    inventoryViewModel.openCreateDialog()
                                },
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
                                Text("اضافة مستودع جديد")
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
                            EnhancedFilterDropdownWithData(
                                label = "الفئة",
                                value = selectedCategory,
                                options = inventoryViewModel.getCategoryOptions(),
                                isLoading = uiState.isLoadingCategories,
                                error = uiState.categoriesError,
                                onValueChange = { selectedCategory = it },
                                onRetry = {
                                    inventoryViewModel.clearCategoriesError()
                                    inventoryViewModel.loadCategories()
                                },
                                modifier = Modifier.weight(0.7f)
                            )

                            // Warehouse Filter
                            EnhancedFilterDropdownWithData(
                                label = "المستودع",
                                value = selectedWarehouse,
                                options = inventoryViewModel.getWarehouseOptions(),
                                isLoading = uiState.isLoading,
                                error = uiState.error,
                                onValueChange = { selectedWarehouse = it },
                                onRetry = {
                                    inventoryViewModel.clearError()
                                    inventoryViewModel.loadInventories()
                                },
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
                                inventoryViewModel = inventoryViewModel,
                                inventories = inventories,
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
                                inventoryViewModel = inventoryViewModel,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                selectedWarehouse = selectedWarehouse,
                                showLowStockOnly = showLowStockOnly,
                                sortBy = sortBy,
                                onItemClick = { item ->
                                    selectedItem = item
                                    showItemDetails = true
                                }
                            )
                            InventoryTab.MOVEMENTS -> EnhancedStockMovementsContent(
                                inventoryViewModel = inventoryViewModel,
                                searchQuery = searchQuery,
                                selectedWarehouse = selectedWarehouse
                            )
                            InventoryTab.WAREHOUSES -> EnhancedWarehousesContent(
                                inventoryViewModel = inventoryViewModel,
                                inventories = inventories,
                                searchQuery = searchQuery,
                                onWarehouseClick = { warehouse ->
                                    selectedWarehouse = warehouse
                                    selectedTab = InventoryTab.PRODUCTS
                                },
                                onAddWarehouse = {
                                    inventoryViewModel.openCreateDialog()
                                },
                                onEditWarehouse = { inventory ->
                                    inventoryViewModel.openEditDialog(inventory)
                                },
                                onDeleteWarehouse = { inventory ->
                                    inventoryViewModel.openDeleteDialog(inventory)
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
                                    // Handle item edit - placeholder for future implementation
                                    showItemDetails = false
                                },
                                onDelete = {
                                    // Handle item delete - placeholder for future implementation
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

        // Warehouse Dialogs
        if (showAddWarehouseDialog) {
            WarehouseDialog(
                inventory = null,
                isLoading = isLoading,
                onDismiss = {
                    inventoryViewModel.closeCreateDialog()
                },
                onSave = { request ->
                    coroutineScope.launch {
                        val result = inventoryViewModel.createInventory(request)
                        result.onSuccess {
                            snackbarHostState.showSnackbar("تم إضافة المستودع بنجاح")
                            inventoryViewModel.closeCreateDialog()
                            inventoryViewModel.loadInventories() // Refresh the list
                        }.onError { exception ->
                            snackbarHostState.showSnackbar("خطأ: ${exception.message}")
                        }
                    }
                }
            )
        }

        if (showEditWarehouseDialog && editingWarehouse != null) {
            WarehouseDialog(
                inventory = editingWarehouse,
                isLoading = isLoading,
                onDismiss = { inventoryViewModel.closeEditDialog() },
                onSave = { request ->
                    coroutineScope.launch {
                        // Convert InventoryCreateRequest to InventoryUpdateRequest
                        val updateRequest = InventoryUpdateRequest(
                            name = request.name,
                            description = request.description,
                            location = request.location,
                            address = request.address,
                            managerName = request.managerName,
                            managerPhone = request.managerPhone,
                            managerEmail = request.managerEmail,
                            length = request.length,
                            width = request.width,
                            height = request.height,
                            currentStockCount = request.currentStockCount,
                            warehouseCode = request.warehouseCode,
                            isMainWarehouse = request.isMainWarehouse,
                            startWorkTime = request.startWorkTime,
                            endWorkTime = request.endWorkTime,
                            contactPhone = request.contactPhone,
                            contactEmail = request.contactEmail,
                            notes = request.notes
                        )
                        val result = inventoryViewModel.updateInventory(editingWarehouse!!.id, updateRequest)
                        result.onSuccess {
                            snackbarHostState.showSnackbar("تم تحديث المستودع بنجاح")
                            inventoryViewModel.closeEditDialog()
                        }.onError { exception ->
                            snackbarHostState.showSnackbar("خطأ: ${exception.message}")
                        }
                    }
                }
            )
        }

        if (showDeleteWarehouseDialog && editingWarehouse != null) {
            AlertDialog(
                onDismissRequest = {}, // Disabled click-outside-to-dismiss
                title = { Text("تأكيد الحذف") },
                text = { Text("هل أنت متأكد من حذف هذا المستودع؟ لا يمكن التراجع عن هذا الإجراء.") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = inventoryViewModel.deleteInventory(editingWarehouse!!.id)
                                result.onSuccess {
                                    snackbarHostState.showSnackbar("تم حذف المستودع بنجاح")
                                    inventoryViewModel.closeDeleteDialog()
                                }.onError { exception ->
                                    snackbarHostState.showSnackbar("خطأ: ${exception.message}")
                                }
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
                        onClick = { inventoryViewModel.closeDeleteDialog() }
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



// Enhanced Filter Dropdown Component with Data Loading Support
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedFilterDropdownWithData(
    label: String,
    value: String,
    options: List<String>,
    isLoading: Boolean = false,
    error: String? = null,
    onValueChange: (String) -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && !isLoading && error == null,
        onExpandedChange = {
            if (!isLoading && error == null) {
                expanded = !expanded
            }
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (isLoading) "جاري التحميل..." else if (error != null) "خطأ في التحميل" else value,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    error != null -> IconButton(
                        onClick = onRetry,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "إعادة المحاولة",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            isError = error != null
        )

        if (!isLoading && error == null) {
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
}
// Enhanced Inventory Overview Content
@Composable
private fun EnhancedInventoryOverviewContent(
    inventoryViewModel: InventoryViewModel,
    inventories: List<InventoryDTO>,
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
                    title = "إجمالي المستودعات",
                    value = inventoryViewModel.totalInventories.value.toString(),
                    subtitle = "مستودع مسجل",
                    icon = Icons.Default.Inventory,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                EnhancedStatCard(
                    title = "قريب من السعة القصوى",
                    value = inventoryViewModel.nearCapacityInventories.value.toString(),
                    subtitle = "مستودع يحتاج انتباه",
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
                    title = "المستودعات النشطة",
                    value = inventoryViewModel.activeInventories.value.toString(),
                    subtitle = "مستودع نشط",
                    icon = Icons.Default.Schedule,
                    iconColor = AppTheme.colors.success,
                    modifier = Modifier.weight(1f)
                )

                EnhancedStatCard(
                    title = "المستودعات الرئيسية",
                    value = inventoryViewModel.mainWarehouses.value.toString(),
                    subtitle = "مستودع رئيسي",
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
    inventoryViewModel: InventoryViewModel,
    searchQuery: String,
    selectedCategory: String,
    selectedWarehouse: String,
    showLowStockOnly: Boolean,
    sortBy: String,
    onItemClick: (InventoryItem) -> Unit
) {
    // Collect ViewModel state
    val uiState by inventoryViewModel.uiState.collectAsState()
    val products = uiState.products
    val isLoadingProducts = uiState.isLoadingProducts
    val productsError = uiState.productsError

    // Load products when the composable is first created
    LaunchedEffect(Unit) {
        inventoryViewModel.loadProducts(refresh = true)
    }

    // Filter and sort products based on current filters
    val filteredItems = remember(products, searchQuery, selectedCategory, selectedWarehouse, showLowStockOnly, sortBy) {
        var filtered = products

        // Apply search filter
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { product ->
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.description?.contains(searchQuery, ignoreCase = true) == true ||
                product.sku?.contains(searchQuery, ignoreCase = true) == true ||
                product.barcode?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        // Apply category filter
        if (selectedCategory.isNotBlank() && selectedCategory != "الكل") {
            filtered = filtered.filter { product ->
                product.categoryName?.equals(selectedCategory, ignoreCase = true) == true ||
                product.category?.equals(selectedCategory, ignoreCase = true) == true
            }
        }

        // Apply warehouse filter
        // Note: ProductDTO doesn't contain warehouse information directly
        // In a real implementation, you would need to either:
        // 1. Add warehouse info to ProductDTO, or
        // 2. Create a separate API endpoint that returns products with warehouse data
        // For now, warehouse filter is available but doesn't affect product filtering
        if (selectedWarehouse.isNotBlank() && selectedWarehouse != "الكل") {
            // TODO: Implement warehouse filtering when warehouse data is available in ProductDTO
            // This would require either modifying the ProductDTO or creating a new endpoint
        }

        // Apply low stock filter
        if (showLowStockOnly) {
            filtered = filtered.filter { product ->
                val currentStock = product.stockQuantity ?: 0
                val minStock = product.minStockLevel ?: 10
                currentStock <= minStock
            }
        }

        // Apply sorting
        when (sortBy) {
            "stock" -> filtered.sortedBy { it.stockQuantity ?: 0 }
            "name" -> filtered.sortedBy { it.name }
            "category" -> filtered.sortedBy { it.categoryName ?: it.category ?: "" }
            "price" -> filtered.sortedBy { it.price }
            else -> filtered.sortedBy { it.name }
        }
    }

    // Convert ProductDTO to InventoryItem for display
    val inventoryItems = remember(filteredItems) {
        filteredItems.mapIndexed { index, product ->
            InventoryItem(
                productId = product.id?.toInt() ?: index,
                warehouseId = 1, // Default warehouse ID
                currentStock = product.stockQuantity ?: 0,
                reservedStock = 0, // Not available in ProductDTO
                minimumStock = product.minStockLevel ?: 10,
                maximumStock = product.maxStockLevel ?: 100,
                reorderPoint = product.reorderPoint ?: 15,
                lastUpdated = LocalDateTime(2024, 1, 1, 10, 0), // Default value
                expiryDate = product.expiryDate?.let {
                    try { LocalDate.parse(it.substring(0, 10)) } catch (e: Exception) { null }
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Show loading state
        if (isLoadingProducts && products.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Show error state
        else if (productsError != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "خطأ في تحميل المنتجات",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = productsError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                inventoryViewModel.clearProductsError()
                                inventoryViewModel.loadProducts(refresh = true)
                            }
                        ) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
            }
        }

        // Show empty state
        else if (inventoryItems.isEmpty() && !isLoadingProducts) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "لا توجد منتجات",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Show products
        else {
            items(inventoryItems.zip(filteredItems)) { (inventoryItem, product) ->
                // Get warehouse name from selected warehouse or use main warehouse
                val warehouseName = if (selectedWarehouse != "الكل") {
                    selectedWarehouse
                } else {
                    // Use main warehouse from inventories or default
                    val mainWarehouse = uiState.inventories.find { it.isMainWarehouse }
                    mainWarehouse?.name ?: "المستودع الرئيسي"
                }

                EnhancedInventoryItemCard(
                    item = inventoryItem,
                    productName = product.name,
                    categoryName = product.categoryName ?: product.category ?: "غير محدد",
                    warehouseName = warehouseName,
                    onClick = onItemClick,
                    showActions = false
                )
            }
        }
    }
}
// Enhanced Stock Movements Content
@Composable
private fun EnhancedStockMovementsContent(
    inventoryViewModel: InventoryViewModel,
    searchQuery: String,
    selectedWarehouse: String
) {
    val uiState by inventoryViewModel.uiState.collectAsState()
    val stockMovements = uiState.stockMovements
    val isLoading = uiState.isLoadingMovements
    val error = uiState.movementsError

    // Load stock movements on first composition
    LaunchedEffect(Unit) {
        inventoryViewModel.loadStockMovements()
    }

    // Reload when search query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            inventoryViewModel.searchStockMovements(searchQuery)
        } else {
            inventoryViewModel.loadStockMovements(refresh = true)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "حركات المخزون",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { inventoryViewModel.refreshStockMovements() }
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "تحديث",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "خطأ في تحميل حركات المخزون",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { inventoryViewModel.refreshStockMovements() }
                        ) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
            }

            stockMovements.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد حركات مخزون",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "لم يتم العثور على أي حركات مخزون",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stockMovements) { movement ->
                        EnhancedStockMovementCard(
                            movement = movement,
                            onClick = { /* Handle movement click if needed */ }
                        )
                    }
                }
            }
        }
    }
}

// Enhanced Warehouses Content
@Composable
private fun EnhancedWarehousesContent(
    inventoryViewModel: InventoryViewModel,
    inventories: List<InventoryDTO>,
    searchQuery: String,
    onWarehouseClick: (String) -> Unit,
    onAddWarehouse: () -> Unit,
    onEditWarehouse: (InventoryDTO) -> Unit,
    onDeleteWarehouse: (InventoryDTO) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Add Warehouse Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddWarehouse() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة مستودع",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إضافة مستودع جديد",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Filter inventories based on search query
        val filteredInventories = inventories.filter { inventory ->
            searchQuery.isBlank() ||
            inventory.name.contains(searchQuery, ignoreCase = true) ||
            inventory.location.contains(searchQuery, ignoreCase = true) ||
            inventory.managerName?.contains(searchQuery, ignoreCase = true) == true
        }

        // Real warehouses from API
        items(filteredInventories) { inventory ->
            EnhancedWarehouseCard(
                inventory = inventory,
                onClick = { onWarehouseClick(inventory.name) },
                onEdit = { onEditWarehouse(inventory) },
                onDelete = { onDeleteWarehouse(inventory) }
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
    movement: StockMovementDTO,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                        text = movement.productName,
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
                        text = movement.warehouseName,
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
                        text = movement.date.substring(0, 10), // Extract date part from ISO string
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Add notes if available
            if (movement.notes.isNotBlank()) {
                Text(
                    text = movement.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Add value information if available
            if (movement.totalValue != 0.0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "القيمة الإجمالية:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.2f", movement.totalValue)} ر.س",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Enhanced Warehouse Card Component (New API Version)
@Composable
private fun EnhancedWarehouseCard(
    inventory: InventoryDTO,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = inventory.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (inventory.isMainWarehouse) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "رئيسي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = inventory.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    inventory.warehouseCode?.let { code ->
                        Text(
                            text = "كود: $code",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status indicator
                    val statusColor = when (inventory.status) {
                        InventoryStatus.ACTIVE -> AppTheme.colors.success
                        InventoryStatus.INACTIVE -> AppTheme.colors.warning
                        InventoryStatus.MAINTENANCE -> AppTheme.colors.info
                        InventoryStatus.ARCHIVED -> AppTheme.colors.error
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = when (inventory.status) {
                                InventoryStatus.ACTIVE -> "نشط"
                                InventoryStatus.INACTIVE -> "غير نشط"
                                InventoryStatus.MAINTENANCE -> "صيانة"
                                InventoryStatus.ARCHIVED -> "مؤرشف"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Icon(
                        Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

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
                        text = "${inventory.currentStockCount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Display dimensions if available
                if (inventory.length != null || inventory.width != null || inventory.height != null) {
                    Column {
                        Text(
                            text = "الأبعاد (م)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = buildString {
                                inventory.length?.let { append("${String.format("%.1f", it)}") }
                                if (inventory.width != null) {
                                    if (isNotEmpty()) append(" × ")
                                    append("${String.format("%.1f", inventory.width)}")
                                }
                                if (inventory.height != null) {
                                    if (isNotEmpty()) append(" × ")
                                    append("${String.format("%.1f", inventory.height)}")
                                }
                                if (isEmpty()) append("غير محدد")
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column {
                    Text(
                        text = "نسبة الاستخدام",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", inventory.capacityUtilization)}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (inventory.isNearCapacity) MaterialTheme.colorScheme.error else AppTheme.colors.success
                    )
                }
            }

            // Manager information
            inventory.managerName?.let { manager ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "المدير",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = manager,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    inventory.managerPhone?.let { phone ->
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل")
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف")
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

    AlertDialog(
        onDismissRequest = {}, // Disabled click-outside-to-dismiss
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item == null) "إضافة عنصر جديد" else "تعديل عنصر المخزون",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Inventory Information Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "معلومات المخزون",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = currentStock,
                            onValueChange = { currentStock = it },
                            label = { Text("المخزون الحالي") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = minimumStock,
                                onValueChange = { minimumStock = it },
                                label = { Text("الحد الأدنى") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            OutlinedTextField(
                                value = maximumStock,
                                onValueChange = { maximumStock = it },
                                label = { Text("الحد الأقصى") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        OutlinedTextField(
                            value = reorderPoint,
                            onValueChange = { reorderPoint = it },
                            label = { Text("نقطة إعادة الطلب") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            // Full-width button row with enhanced hover effects
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel Button with Box-based hover effects
                val cancelInteractionSource = remember { MutableInteractionSource() }
                val isCancelHovered by cancelInteractionSource.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = if (isCancelHovered)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isCancelHovered) 1.5.dp else 1.dp,
                            color = if (isCancelHovered)
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = cancelInteractionSource,
                            indication = null
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إلغاء",
                        color = if (isCancelHovered)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Save Button with Box-based hover effects
                val saveInteractionSource = remember { MutableInteractionSource() }
                val isSaveHovered by saveInteractionSource.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = if (isSaveHovered)
                                MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isSaveHovered) 2.dp else 1.dp,
                            color = if (isSaveHovered)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = saveInteractionSource,
                            indication = null
                        ) {
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
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (item != null) "تحديث" else "حفظ",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        dismissButton = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// Time Picker Dialog Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    title: String = "اختر الوقت"
) {
    var selectedHour by remember { mutableStateOf(selectedTime?.hour ?: 8) }
    var selectedMinute by remember { mutableStateOf(selectedTime?.minute ?: 0) }

    val hourListState = rememberLazyListState()
    val minuteListState = rememberLazyListState()

    // Scroll to selected values when dialog opens
    LaunchedEffect(selectedTime) {
        if (selectedTime != null) {
            hourListState.scrollToItem(selectedTime.hour)
            minuteListState.scrollToItem(selectedTime.minute)
        }
    }

    AlertDialog(
        onDismissRequest = {}, // Disabled click-outside-to-dismiss
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selected time display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = String.format("%02d:%02d", selectedHour, selectedMinute),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hour selector
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الساعة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            LazyColumn(
                                state = hourListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items((0..23).toList()) { hour ->
                                    val isSelected = hour == selectedHour

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .clickable { selectedHour = hour },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else
                                                Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = String.format("%02d", hour),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Minute selector
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الدقيقة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            LazyColumn(
                                state = minuteListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items((0..59).toList()) { minute ->
                                    val isSelected = minute == selectedMinute

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .clickable { selectedMinute = minute },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else
                                                Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = String.format("%02d", minute),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
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
                        onTimeSelected(LocalTime(selectedHour, selectedMinute))
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("موافق")
                }
            }
        },
        dismissButton = null
    )
}

// Time Picker Button Component
@Composable
private fun TimePickerButton(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime?) -> Unit,
    label: String,
    placeholder: String = "اختر الوقت",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    onNext: (() -> Unit)? = null
) {
    var showTimePicker by remember { mutableStateOf(false) }

    // Create a focusable button that can participate in keyboard navigation
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .then(
                    if (focusRequester != null) {
                        Modifier.focusRequester(focusRequester)
                    } else Modifier
                )
                .focusable()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled
                ) {
                    if (enabled) {
                        showTimePicker = true
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = if (isHovered && enabled)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = if (isHovered && enabled) 1.5.dp else 1.dp,
                color = if (isHovered && enabled)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (enabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = selectedTime?.let {
                            String.format("%02d:%02d", it.hour, it.minute)
                        } ?: placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedTime != null && enabled)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedTime != null && enabled) {
                        IconButton(
                            onClick = {
                                onTimeSelected(null)
                                onNext?.invoke()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "مسح الوقت",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "اختر الوقت",
                        tint = if (enabled)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    // Handle keyboard navigation
    LaunchedEffect(focusRequester) {
        focusRequester?.let { requester ->
            // This allows the button to receive focus and handle Enter key
            // The actual focus handling is done by the clickable modifier
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            selectedTime = selectedTime,
            onTimeSelected = { time ->
                onTimeSelected(time)
                onNext?.invoke()
            },
            onDismiss = { showTimePicker = false },
            title = label
        )
    }
}

// Enhanced Warehouse Dialog Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WarehouseDialog(
    inventory: InventoryDTO?,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (InventoryCreateRequest) -> Unit
) {
    // Helper function to parse time string to LocalTime
    fun parseTimeString(timeStr: String): LocalTime? {
        return try {
            if (timeStr.isBlank()) return null
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull()
                val minute = parts[1].toIntOrNull()
                if (hour != null && minute != null && hour in 0..23 && minute in 0..59) {
                    LocalTime(hour, minute)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
    var name by remember { mutableStateOf(inventory?.name ?: "") }
    var description by remember { mutableStateOf(inventory?.description ?: "") }
    var location by remember { mutableStateOf(inventory?.location ?: "") }
    var address by remember { mutableStateOf(inventory?.address ?: "") }
    var managerName by remember { mutableStateOf(inventory?.managerName ?: "") }
    var managerPhone by remember { mutableStateOf(inventory?.managerPhone ?: "") }
    var managerEmail by remember { mutableStateOf(inventory?.managerEmail ?: "") }
    var length by remember { mutableStateOf(inventory?.length?.toString() ?: "") }
    var width by remember { mutableStateOf(inventory?.width?.toString() ?: "") }
    var height by remember { mutableStateOf(inventory?.height?.toString() ?: "") }
    var warehouseCode by remember { mutableStateOf(inventory?.warehouseCode ?: "") }
    var isMainWarehouse by remember { mutableStateOf(inventory?.isMainWarehouse ?: false) }
    var startWorkTime by remember { mutableStateOf(inventory?.startWorkTime) }
    var endWorkTime by remember { mutableStateOf(inventory?.endWorkTime) }
    var contactPhone by remember { mutableStateOf(inventory?.contactPhone ?: "") }
    var contactEmail by remember { mutableStateOf(inventory?.contactEmail ?: "") }
    var notes by remember { mutableStateOf(inventory?.notes ?: "") }

    // Focus manager for keyboard navigation
    val focusManager = LocalFocusManager.current

    // Focus requesters for explicit focus management
    val locationFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val addressFocusRequester = remember { FocusRequester() }
    val lengthFocusRequester = remember { FocusRequester() }
    val widthFocusRequester = remember { FocusRequester() }
    val heightFocusRequester = remember { FocusRequester() }
    val warehouseCodeFocusRequester = remember { FocusRequester() }
    val managerNameFocusRequester = remember { FocusRequester() }
    val managerPhoneFocusRequester = remember { FocusRequester() }
    val managerEmailFocusRequester = remember { FocusRequester() }
    val contactPhoneFocusRequester = remember { FocusRequester() }
    val contactEmailFocusRequester = remember { FocusRequester() }
    val startWorkTimeFocusRequester = remember { FocusRequester() }
    val endWorkTimeFocusRequester = remember { FocusRequester() }
    val notesFocusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = {}, // Disabled click-outside-to-dismiss
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (inventory != null) "تعديل المستودع" else "إضافة مستودع جديد",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Basic Information Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "المعلومات الأساسية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("اسم المستودع *") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Warehouse,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = name.isBlank(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { locationFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("الموقع *") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(locationFocusRequester),
                            singleLine = true,
                            isError = location.isBlank(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { descriptionFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("الوصف") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(descriptionFocusRequester),
                            maxLines = 2,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { addressFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("العنوان") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(addressFocusRequester),
                            maxLines = 2,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { lengthFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // Warehouse Dimensions Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "أبعاد المستودع (بالمتر)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Dimensions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = length,
                                onValueChange = { length = it },
                                label = { Text("الطول") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Straighten,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(lengthFocusRequester),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { widthFocusRequester.requestFocus() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                placeholder = { Text("0.0") }
                            )

                            OutlinedTextField(
                                value = width,
                                onValueChange = { width = it },
                                label = { Text("العرض") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Straighten,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(widthFocusRequester),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { heightFocusRequester.requestFocus() }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                placeholder = { Text("0.0") }
                            )

                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = { Text("الارتفاع") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Height,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(heightFocusRequester),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { warehouseCodeFocusRequester.requestFocus() }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                placeholder = { Text("0.0") }
                            )
                        }

                        // Warehouse Code and Main Warehouse Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = warehouseCode,
                                onValueChange = { warehouseCode = it },
                                label = { Text("كود المستودع") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(warehouseCodeFocusRequester),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { managerNameFocusRequester.requestFocus() }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            // Main Warehouse Checkbox
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isMainWarehouse,
                                    onCheckedChange = { isMainWarehouse = it },
                                    enabled = !isLoading,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مستودع رئيسي",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Manager Information Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "معلومات المدير",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = managerName,
                            onValueChange = { managerName = it },
                            label = { Text("اسم المدير") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(managerNameFocusRequester),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { managerPhoneFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = managerPhone,
                                onValueChange = { managerPhone = it },
                                label = { Text("هاتف المدير") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(managerPhoneFocusRequester),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { managerEmailFocusRequester.requestFocus() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            OutlinedTextField(
                                value = managerEmail,
                                onValueChange = { managerEmail = it },
                                label = { Text("بريد المدير") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(managerEmailFocusRequester),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { contactPhoneFocusRequester.requestFocus() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }

                // Contact Information Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "معلومات الاتصال",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                label = { Text("هاتف المستودع") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(contactPhoneFocusRequester),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { contactEmailFocusRequester.requestFocus() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            OutlinedTextField(
                                value = contactEmail,
                                onValueChange = { contactEmail = it },
                                label = { Text("بريد المستودع") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(contactEmailFocusRequester),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { startWorkTimeFocusRequester.requestFocus() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // Work Time Fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start Work Time
                            TimePickerButton(
                                selectedTime = startWorkTime,
                                onTimeSelected = { time -> startWorkTime = time },
                                label = "بداية العمل",
                                placeholder = "اختر وقت البداية",
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                focusRequester = startWorkTimeFocusRequester,
                                onNext = { endWorkTimeFocusRequester.requestFocus() }
                            )

                            // End Work Time
                            TimePickerButton(
                                selectedTime = endWorkTime,
                                onTimeSelected = { time -> endWorkTime = time },
                                label = "نهاية العمل",
                                placeholder = "اختر وقت النهاية",
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                focusRequester = endWorkTimeFocusRequester,
                                onNext = { notesFocusRequester.requestFocus() }
                            )
                        }
                    }
                }

                // Notes Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "ملاحظات إضافية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(notesFocusRequester),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val isValid = name.isNotBlank() && location.isNotBlank()
                                    if (isValid && !isLoading) {
                                        focusManager.clearFocus()
                                        val request = InventoryCreateRequest(
                                            name = name.trim(),
                                            description = description.trim().takeIf { it.isNotBlank() },
                                            location = location.trim(),
                                            address = address.trim().takeIf { it.isNotBlank() },
                                            managerName = managerName.trim().takeIf { it.isNotBlank() },
                                            managerPhone = managerPhone.trim().takeIf { it.isNotBlank() },
                                            managerEmail = managerEmail.trim().takeIf { it.isNotBlank() },
                                            length = length.toDoubleOrNull(),
                                            width = width.toDoubleOrNull(),
                                            height = height.toDoubleOrNull(),
                                            warehouseCode = warehouseCode.trim().takeIf { it.isNotBlank() },
                                            isMainWarehouse = isMainWarehouse,
                                            startWorkTime = startWorkTime,
                                            endWorkTime = endWorkTime,
                                            contactPhone = contactPhone.trim().takeIf { it.isNotBlank() },
                                            contactEmail = contactEmail.trim().takeIf { it.isNotBlank() },
                                            notes = notes.trim().takeIf { it.isNotBlank() }
                                        )
                                        onSave(request)
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }
        },

        confirmButton = {
            // Full-width button row with enhanced hover effects
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val isValid = name.isNotBlank() && location.isNotBlank()

                // Cancel Button with Box-based hover effects
                val cancelInteractionSource = remember { MutableInteractionSource() }
                val isCancelHovered by cancelInteractionSource.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = if (isCancelHovered)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isCancelHovered) 1.5.dp else 1.dp,
                            color = if (isCancelHovered)
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = cancelInteractionSource,
                            indication = null
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إلغاء",
                        color = if (isCancelHovered)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Save Button with Box-based hover effects
                val saveInteractionSource = remember { MutableInteractionSource() }
                val isSaveHovered by saveInteractionSource.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = if (isSaveHovered && isValid && !isLoading)
                                MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                            else if (isValid && !isLoading)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isSaveHovered && isValid && !isLoading) 2.dp else 1.dp,
                            color = if (isSaveHovered && isValid && !isLoading)
                                MaterialTheme.colorScheme.primary
                            else if (isValid && !isLoading)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = saveInteractionSource,
                            indication = null,
                            enabled = isValid && !isLoading
                        ) {
                            if (isValid && !isLoading) {
                                val request = InventoryCreateRequest(
                                    name = name.trim(),
                                    description = description.trim().takeIf { it.isNotBlank() },
                                    location = location.trim(),
                                    address = address.trim().takeIf { it.isNotBlank() },
                                    managerName = managerName.trim().takeIf { it.isNotBlank() },
                                    managerPhone = managerPhone.trim().takeIf { it.isNotBlank() },
                                    managerEmail = managerEmail.trim().takeIf { it.isNotBlank() },
                                    length = length.toDoubleOrNull(),
                                    width = width.toDoubleOrNull(),
                                    height = height.toDoubleOrNull(),
                                    warehouseCode = warehouseCode.trim().takeIf { it.isNotBlank() },
                                    isMainWarehouse = isMainWarehouse,
                                    startWorkTime = startWorkTime,
                                    endWorkTime = endWorkTime,
                                    contactPhone = contactPhone.trim().takeIf { it.isNotBlank() },
                                    contactEmail = contactEmail.trim().takeIf { it.isNotBlank() },
                                    notes = notes.trim().takeIf { it.isNotBlank() }
                                )
                                onSave(request)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = if (inventory != null) "تحديث" else "إضافة",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        dismissButton = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

