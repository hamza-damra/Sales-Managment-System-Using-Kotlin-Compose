package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import data.*
import data.api.SupplierDTO
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.utils.ResponsiveUtils
import ui.viewmodels.SupplierViewModel
import ui.viewmodels.SupplierData
import utils.SupplierMapper
import utils.SupplierMapper.toSupplier

// Supplier Tab Enum
enum class SupplierTab(val title: String) {
    SUPPLIERS("الموردين"),
    ORDERS("طلبات الشراء"),
    ANALYTICS("التحليلات والتقارير")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    supplierViewModel: SupplierViewModel,
    salesDataManager: SalesDataManager? = null // Keep for backward compatibility
) {
    RTLProvider {
        // ViewModel state
        val suppliers by supplierViewModel.filteredSuppliers.collectAsState()
        val isLoading by supplierViewModel.isLoading.collectAsState()
        val error by supplierViewModel.error.collectAsState()
        val searchQuery by supplierViewModel.searchQuery.collectAsState()
        val selectedStatus by supplierViewModel.selectedStatus.collectAsState()
        val selectedLocation by supplierViewModel.selectedLocation.collectAsState()
        val sortBy by supplierViewModel.sortBy.collectAsState()
        val showActiveOnly by supplierViewModel.showActiveOnly.collectAsState()
        val showWithOrdersOnly by supplierViewModel.showWithOrdersOnly.collectAsState()

        // Operation states
        val isCreating by supplierViewModel.isCreating.collectAsState()
        val isUpdating by supplierViewModel.isUpdating.collectAsState()
        val isDeleting by supplierViewModel.isDeleting.collectAsState()
        val lastCreatedSupplier by supplierViewModel.lastCreatedSupplier.collectAsState()
        val lastUpdatedSupplier by supplierViewModel.lastUpdatedSupplier.collectAsState()

        // UI state
        var selectedTab by remember { mutableStateOf(SupplierTab.SUPPLIERS) }
        var selectedRating by remember { mutableStateOf("الكل") }
        var isExporting by remember { mutableStateOf(false) }
        var exportMessage by remember { mutableStateOf<String?>(null) }

        // Dialog states
        var showAddSupplierDialog by remember { mutableStateOf(false) }
        var editingSupplier by remember { mutableStateOf<SupplierDTO?>(null) }
        var selectedSupplier by remember { mutableStateOf<SupplierDTO?>(null) }
        var showSupplierDetails by remember { mutableStateOf(false) }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var supplierToDelete by remember { mutableStateOf<SupplierDTO?>(null) }

        // Snackbar state
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // Load suppliers on first composition
        LaunchedEffect(Unit) {
            supplierViewModel.loadSuppliers(refresh = true)
        }

        // Handle search query changes
        LaunchedEffect(searchQuery) {
            if (searchQuery.isNotBlank()) {
                supplierViewModel.searchSuppliers(searchQuery)
            }
        }

        // Handle success states
        LaunchedEffect(lastCreatedSupplier) {
            lastCreatedSupplier?.let {
                snackbarHostState.showSnackbar("تم إضافة المورد بنجاح")
                supplierViewModel.clearLastCreatedSupplier()
            }
        }

        LaunchedEffect(lastUpdatedSupplier) {
            lastUpdatedSupplier?.let {
                snackbarHostState.showSnackbar("تم تحديث المورد بنجاح")
                supplierViewModel.clearLastUpdatedSupplier()
            }
        }

        // Handle error states
        LaunchedEffect(error) {
            error?.let {
                snackbarHostState.showSnackbar("خطأ: $it")
                supplierViewModel.clearError()
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Supplier Management
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
                                text = "إدارة الموردين",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = { showAddSupplierDialog = true },
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
                                Text("إضافة مورد جديد")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Enhanced Tabs
                        EnhancedSupplierTabRow(
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
                                onValueChange = { supplierViewModel.updateSearchQuery(it) },
                                label = { Text("البحث في الموردين") },
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

                            // Status Filter
                            EnhancedSupplierFilterDropdown(
                                label = "الحالة",
                                value = selectedStatus,
                                options = listOf("الكل", "نشط", "غير نشط", "معلق"),
                                onValueChange = { supplierViewModel.updateSelectedStatus(it) },
                                modifier = Modifier.weight(0.7f)
                            )

                            // Location Filter
                            EnhancedSupplierFilterDropdown(
                                label = "الموقع",
                                value = selectedLocation,
                                options = listOf("الكل", "الرياض", "جدة", "الدمام", "مكة"),
                                onValueChange = { supplierViewModel.updateSelectedLocation(it) },
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
                            EnhancedSupplierFilterDropdown(
                                label = "ترتيب حسب",
                                value = when(sortBy) {
                                    "name" -> "الاسم"
                                    "rating" -> "التقييم"
                                    "totalOrders" -> "عدد الطلبات"
                                    "totalAmount" -> "قيمة المشتريات"
                                    else -> "الاسم"
                                },
                                options = listOf("الاسم", "التقييم", "عدد الطلبات", "قيمة المشتريات"),
                                onValueChange = {
                                    val newSortBy = when(it) {
                                        "الاسم" -> "name"
                                        "التقييم" -> "rating"
                                        "عدد الطلبات" -> "totalOrders"
                                        "قيمة المشتريات" -> "totalAmount"
                                        else -> "name"
                                    }
                                    supplierViewModel.updateSortBy(newSortBy)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Enhanced Quick Filters with complete hover coverage
                            val activeOnlyInteractionSource = remember { MutableInteractionSource() }
                            val isActiveOnlyHovered by activeOnlyInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = when {
                                            showActiveOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            isActiveOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (showActiveOnly) 1.5.dp else if (isActiveOnlyHovered) 1.dp else 0.5.dp,
                                        color = when {
                                            showActiveOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            isActiveOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = activeOnlyInteractionSource,
                                        indication = null
                                    ) { supplierViewModel.toggleActiveOnly() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    if (showActiveOnly) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "نشط فقط",
                                        color = when {
                                            showActiveOnly -> MaterialTheme.colorScheme.primary
                                            isActiveOnlyHovered -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            val withOrdersInteractionSource = remember { MutableInteractionSource() }
                            val isWithOrdersHovered by withOrdersInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = when {
                                            showWithOrdersOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            isWithOrdersHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (showWithOrdersOnly) 1.5.dp else if (isWithOrdersHovered) 1.dp else 0.5.dp,
                                        color = when {
                                            showWithOrdersOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            isWithOrdersHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = withOrdersInteractionSource,
                                        indication = null
                                    ) { supplierViewModel.toggleWithOrdersOnly() },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    if (showWithOrdersOnly) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "لديهم طلبات",
                                        color = when {
                                            showWithOrdersOnly -> MaterialTheme.colorScheme.primary
                                            isWithOrdersHovered -> MaterialTheme.colorScheme.onSurface
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
                                    ) { if (!isExporting) { /* handleExportExcel() */ } },
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
                            SupplierTab.SUPPLIERS -> EnhancedSuppliersContent(
                                suppliers = suppliers,
                                isLoading = isLoading,
                                onSupplierClick = { supplier ->
                                    selectedSupplier = supplier
                                    showSupplierDetails = true
                                },
                                onEditSupplier = { supplier ->
                                    editingSupplier = supplier
                                },
                                onDeleteSupplier = { supplier ->
                                    supplierToDelete = supplier
                                    showDeleteConfirmation = true
                                },
                                onRefresh = {
                                    coroutineScope.launch {
                                        supplierViewModel.refreshSuppliers()
                                    }
                                }
                            )
                            SupplierTab.ORDERS -> EnhancedPurchaseOrdersContent(
                                searchQuery = searchQuery
                            )
                            SupplierTab.ANALYTICS -> EnhancedSupplierAnalyticsContent()
                        }
                    }
                }

                // Right Panel - Details and Statistics (when supplier selected)
                AnimatedVisibility(
                    visible = showSupplierDetails && selectedSupplier != null,
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
                        selectedSupplier?.let { supplier ->
                            EnhancedSupplierDetailsPanel(
                                supplier = supplier,
                                onEdit = {
                                    editingSupplier = supplier
                                    showSupplierDetails = false
                                },
                                onDelete = {
                                    supplierToDelete = supplier
                                    showDeleteConfirmation = true
                                    showSupplierDetails = false
                                },
                                onClose = {
                                    showSupplierDetails = false
                                    selectedSupplier = null
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
        if (showAddSupplierDialog) {
            EnhancedAddSupplierDialog(
                isLoading = isCreating,
                onDismiss = { showAddSupplierDialog = false },
                onSave = { supplierData ->
                    coroutineScope.launch {
                        val result = supplierViewModel.createSupplier(supplierData)
                        result.onSuccess {
                            showAddSupplierDialog = false
                        }.onError { exception ->
                            snackbarHostState.showSnackbar("خطأ في إضافة المورد: ${exception.message}")
                        }
                    }
                }
            )
        }

        if (editingSupplier != null) {
            EnhancedEditSupplierDialog(
                supplier = editingSupplier!!,
                isLoading = isUpdating,
                onDismiss = { editingSupplier = null },
                onSave = { supplierData ->
                    coroutineScope.launch {
                        val result = supplierViewModel.updateSupplier(editingSupplier!!.id!!, supplierData)
                        result.onSuccess {
                            editingSupplier = null
                        }.onError { exception ->
                            snackbarHostState.showSnackbar("خطأ في تحديث المورد: ${exception.message}")
                        }
                    }
                }
            )
        }

        if (showDeleteConfirmation && supplierToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                    supplierToDelete = null
                },
                title = { Text("تأكيد الحذف") },
                text = { Text("هل أنت متأكد من حذف هذا المورد؟ لا يمكن التراجع عن هذا الإجراء.") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = supplierViewModel.deleteSupplier(supplierToDelete!!.id!!)
                                result.onSuccess {
                                    showDeleteConfirmation = false
                                    supplierToDelete = null
                                    snackbarHostState.showSnackbar("تم حذف المورد بنجاح")
                                }.onError { exception ->
                                    snackbarHostState.showSnackbar("خطأ في حذف المورد: ${exception.message}")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !isDeleting
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("حذف")
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirmation = false
                            supplierToDelete = null
                        },
                        enabled = !isDeleting
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
private fun EnhancedSupplierTabRow(
    selectedTab: SupplierTab,
    onTabSelected: (SupplierTab) -> Unit
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
            SupplierTab.values().forEach { tab ->
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
                                SupplierTab.SUPPLIERS -> Icons.Default.Business
                                SupplierTab.ORDERS -> Icons.Default.ShoppingCart
                                SupplierTab.ANALYTICS -> Icons.Default.Analytics
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
private fun EnhancedSupplierFilterDropdown(
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

// Enhanced Suppliers Content
@Composable
private fun EnhancedSuppliersContent(
    suppliers: List<SupplierDTO>,
    isLoading: Boolean,
    onSupplierClick: (SupplierDTO) -> Unit,
    onEditSupplier: (SupplierDTO) -> Unit,
    onDeleteSupplier: (SupplierDTO) -> Unit,
    onRefresh: () -> Unit
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
                EnhancedSupplierStatCard(
                    title = "إجمالي الموردين",
                    value = suppliers.size.toString(),
                    subtitle = "مورد مسجل",
                    icon = Icons.Default.Business,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                EnhancedSupplierStatCard(
                    title = "موردين نشطين",
                    value = suppliers.count { it.status == "ACTIVE" }.toString(),
                    subtitle = "مورد نشط",
                    icon = Icons.Default.Verified,
                    iconColor = AppTheme.colors.success,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedSupplierStatCard(
                    title = "طلبات الشراء",
                    value = suppliers.sumOf { it.totalOrders ?: 0 }.toString(),
                    subtitle = "إجمالي الطلبات",
                    icon = Icons.Default.ShoppingCart,
                    iconColor = AppTheme.colors.warning,
                    modifier = Modifier.weight(1f)
                )

                EnhancedSupplierStatCard(
                    title = "قيمة المشتريات",
                    value = SupplierMapper.formatTotalAmount(suppliers.sumOf { it.totalAmount ?: 0.0 }),
                    subtitle = "إجمالي المبلغ",
                    icon = Icons.Default.AttachMoney,
                    iconColor = AppTheme.colors.info,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Header with refresh button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة الموردين",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
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
            }
        }

        // Loading state
        if (isLoading && suppliers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Empty state
        if (!isLoading && suppliers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "لا توجد موردين",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ابدأ بإضافة مورد جديد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Suppliers list
        items(suppliers) { supplier ->
            EnhancedSupplierCard(
                supplier = supplier,
                onClick = onSupplierClick,
                onEdit = onEditSupplier,
                onDelete = onDeleteSupplier
            )
        }
    }
}

// Enhanced Purchase Orders Content
@Composable
private fun EnhancedPurchaseOrdersContent(
    searchQuery: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sample purchase orders
        items(10) { index ->
            EnhancedPurchaseOrderCard(
                orderId = "PO-${1000 + index}",
                supplierName = "شركة المورد ${index + 1}",
                orderDate = "2024-01-${(index % 28) + 1}",
                totalAmount = (5000 + index * 1000).toDouble(),
                status = when (index % 3) {
                    0 -> "في الانتظار"
                    1 -> "تم التسليم"
                    else -> "ملغي"
                },
                itemsCount = 3 + index % 5
            )
        }
    }
}

// Enhanced Supplier Analytics Content
@Composable
private fun EnhancedSupplierAnalyticsContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedSupplierStatCard(
                    title = "متوسط قيمة الطلب",
                    value = "3,807 ر.س",
                    subtitle = "متوسط شهري",
                    icon = Icons.Default.Analytics,
                    iconColor = AppTheme.colors.purple,
                    modifier = Modifier.weight(1f)
                )

                EnhancedSupplierStatCard(
                    title = "أفضل مورد",
                    value = "شركة المورد 1",
                    subtitle = "حسب التقييم",
                    icon = Icons.Default.Star,
                    iconColor = AppTheme.colors.warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "تحليل الأداء",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Performance metrics would go here
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تقرير الأداء الشهري",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "سيتم إضافة الرسوم البيانية والتحليلات هنا",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Enhanced Supplier Stat Card Component
@Composable
private fun EnhancedSupplierStatCard(
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

// Enhanced Supplier Card Component
@Composable
private fun EnhancedSupplierCard(
    supplier: SupplierDTO,
    onClick: (SupplierDTO) -> Unit,
    onEdit: (SupplierDTO) -> Unit,
    onDelete: (SupplierDTO) -> Unit,
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
            ) { onClick(supplier) }
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
                        text = supplier.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "الشخص المسؤول: ${supplier.contactPerson ?: "غير محدد"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onEdit(supplier) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDelete(supplier) },
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

            // Status and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (supplier.status) {
                            "ACTIVE" -> AppTheme.colors.success.copy(alpha = 0.2f)
                            "SUSPENDED" -> AppTheme.colors.warning.copy(alpha = 0.2f)
                            else -> AppTheme.colors.error.copy(alpha = 0.2f)
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
                            when (supplier.status) {
                                "ACTIVE" -> Icons.Default.CheckCircle
                                "SUSPENDED" -> Icons.Default.Warning
                                else -> Icons.Default.Error
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (supplier.status) {
                                "ACTIVE" -> AppTheme.colors.success
                                "SUSPENDED" -> AppTheme.colors.warning
                                else -> AppTheme.colors.error
                            }
                        )
                        Text(
                            text = SupplierMapper.getStatusDisplayName(supplier.status),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = when (supplier.status) {
                                "ACTIVE" -> AppTheme.colors.success
                                "SUSPENDED" -> AppTheme.colors.warning
                                else -> AppTheme.colors.error
                            }
                        )
                    }
                }

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AppTheme.colors.warning,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = SupplierMapper.formatRating(supplier.rating),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Order Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "عدد الطلبات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SupplierMapper.formatTotalOrders(supplier.totalOrders),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = "إجمالي المشتريات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SupplierMapper.formatTotalAmount(supplier.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Contact Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "الهاتف",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = supplier.phone ?: "غير محدد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "البريد الإلكتروني",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = supplier.email ?: "غير محدد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Enhanced Purchase Order Card Component
@Composable
private fun EnhancedPurchaseOrderCard(
    orderId: String,
    supplierName: String,
    orderDate: String,
    totalAmount: Double,
    status: String,
    itemsCount: Int,
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
                        text = orderId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = supplierName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (status) {
                            "في الانتظار" -> AppTheme.colors.warning.copy(alpha = 0.2f)
                            "تم التسليم" -> AppTheme.colors.success.copy(alpha = 0.2f)
                            "ملغي" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = when (status) {
                            "في الانتظار" -> AppTheme.colors.warning
                            "تم التسليم" -> AppTheme.colors.success
                            "ملغي" -> MaterialTheme.colorScheme.onErrorContainer
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
                        text = "تاريخ الطلب",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = orderDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "عدد العناصر",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$itemsCount عنصر",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "المبلغ الإجمالي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${totalAmount.toInt()} ر.س",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Enhanced Supplier Details Panel Component
@Composable
private fun EnhancedSupplierDetailsPanel(
    supplier: SupplierDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تفاصيل المورد",
                style = MaterialTheme.typography.titleLarge,
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

        // Supplier Information
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = supplier.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AppTheme.colors.warning,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = SupplierMapper.formatRating(supplier.rating),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "تقييم",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Contact Information
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("الشخص المسؤول", supplier.contactPerson ?: "غير محدد")
                    DetailRow("الهاتف", supplier.phone ?: "غير محدد")
                    DetailRow("البريد الإلكتروني", supplier.email ?: "غير محدد")
                    DetailRow("العنوان", supplier.address ?: "غير محدد")
                    DetailRow("شروط الدفع", SupplierMapper.getPaymentTermsDisplayName(supplier.paymentTerms))
                    DetailRow("شروط التسليم", SupplierMapper.getDeliveryTermsDisplayName(supplier.deliveryTerms))
                    DetailRow("إجمالي الطلبات", SupplierMapper.formatTotalOrders(supplier.totalOrders))
                    DetailRow("إجمالي المبلغ", SupplierMapper.formatTotalAmount(supplier.totalAmount))
                }
            }
        }

        // Action Buttons
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تعديل")
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حذف")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

// Enhanced Edit Supplier Dialog
@Composable
private fun EnhancedEditSupplierDialog(
    supplier: SupplierDTO,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (SupplierData) -> Unit
) {
    var name by remember { mutableStateOf(supplier.name) }
    var contactPerson by remember { mutableStateOf(supplier.contactPerson ?: "") }
    var phone by remember { mutableStateOf(supplier.phone ?: "") }
    var email by remember { mutableStateOf(supplier.email ?: "") }
    var address by remember { mutableStateOf(supplier.address ?: "") }
    var paymentTerms by remember { mutableStateOf(supplier.paymentTerms ?: "NET_30") }
    var deliveryTerms by remember { mutableStateOf(supplier.deliveryTerms ?: "FOB_DESTINATION") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تعديل بيانات المورد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(400.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الشركة") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("الشخص المسؤول") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = paymentTerms,
                        onValueChange = { paymentTerms = it },
                        label = { Text("شروط الدفع") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = deliveryTerms,
                        onValueChange = { deliveryTerms = it },
                        label = { Text("شروط التسليم") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val supplierData = SupplierData(
                        name = name,
                        contactPerson = contactPerson,
                        phone = phone,
                        email = email,
                        address = address,
                        paymentTerms = paymentTerms,
                        deliveryTerms = deliveryTerms
                    )
                    onSave(supplierData)
                },
                enabled = !isLoading && name.isNotBlank() && contactPerson.isNotBlank() &&
                         phone.isNotBlank() && email.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "حفظ التغييرات",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "إلغاء",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun EnhancedAddSupplierDialog(
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (SupplierData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("NET_30") }
    var deliveryTerms by remember { mutableStateOf("FOB_DESTINATION") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مورد جديد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(400.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الشركة *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("الشخص المسؤول *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = paymentTerms,
                        onValueChange = { paymentTerms = it },
                        label = { Text("شروط الدفع") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = deliveryTerms,
                        onValueChange = { deliveryTerms = it },
                        label = { Text("شروط التسليم") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val supplier = SupplierData(
                        name = name,
                        contactPerson = contactPerson,
                        phone = phone,
                        email = email,
                        address = address,
                        paymentTerms = paymentTerms,
                        deliveryTerms = deliveryTerms
                    )
                    onSave(supplier)
                },
                enabled = !isLoading && name.isNotBlank() && contactPerson.isNotBlank() &&
                         phone.isNotBlank() && email.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "حفظ",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "إلغاء",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

data class SupplierData(
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String,
    val paymentTerms: String = "NET_30",
    val deliveryTerms: String = "FOB_DESTINATION"
)
