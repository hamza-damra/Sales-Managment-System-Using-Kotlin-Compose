@file:OptIn(ExperimentalMaterial3Api::class)

package ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import data.api.PurchaseOrderDTO
import data.di.AppContainer
import kotlinx.coroutines.launch
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.viewmodels.PurchaseOrderViewModel
import ui.viewmodels.PurchaseOrderData
import utils.PurchaseOrderMapper

/**
 * Purchase Orders Management Screen
 * Provides comprehensive purchase order management with CRUD operations,
 * filtering, search, and integration with supplier management
 */
@Composable
fun PurchaseOrdersScreen(
    appContainer: AppContainer,
    onNavigateToSuppliers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: PurchaseOrderViewModel = remember {
        PurchaseOrderViewModel(
            purchaseOrderRepository = appContainer.purchaseOrderRepository,
            supplierRepository = appContainer.supplierRepository
        )
    }

    // Collect state
    val purchaseOrders by viewModel.purchaseOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showDetailsPanel by viewModel.showDetailsPanel.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val supplierFilter by viewModel.supplierFilter.collectAsState()
    val priorityFilter by viewModel.priorityFilter.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val hasNextPage by viewModel.hasNextPage.collectAsState()
    val totalElements by viewModel.totalElements.collectAsState()

    // UI state
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var orderToDelete by remember { mutableStateOf<PurchaseOrderDTO?>(null) }

    // Handle infinite scrolling
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= purchaseOrders.size - 3 && 
                    hasNextPage && 
                    !isLoading) {
                    viewModel.loadNextPage()
                }
            }
    }

    // Error handling
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error snackbar or handle error
            viewModel.clearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Main content
            Column(
                modifier = Modifier
                    .weight(if (showDetailsPanel) 0.6f else 1f)
                    .fillMaxHeight()
            ) {
                // Header with title and actions
                PurchaseOrdersHeader(
                    totalOrders = totalElements.toInt(),
                    onAddClick = { viewModel.showAddDialog() },
                    onRefreshClick = { viewModel.refreshPurchaseOrders() },
                    onNavigateToSuppliers = onNavigateToSuppliers
                )

                // Search and filters
                PurchaseOrdersFilters(
                    searchQuery = searchQuery,
                    statusFilter = statusFilter,
                    supplierFilter = supplierFilter,
                    priorityFilter = priorityFilter,
                    suppliers = suppliers,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onStatusFilterChange = { viewModel.setStatusFilter(it) },
                    onSupplierFilterChange = { viewModel.setSupplierFilter(it) },
                    onPriorityFilterChange = { viewModel.setPriorityFilter(it) },
                    onClearFilters = { viewModel.clearFilters() }
                )

                // Purchase orders list
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading && purchaseOrders.isEmpty()) {
                        // Initial loading state
                        LoadingState()
                    } else if (purchaseOrders.isEmpty() && !isLoading) {
                        // Empty state
                        EmptyPurchaseOrdersState(
                            hasFilters = searchQuery.isNotBlank() || statusFilter != null || 
                                        supplierFilter != null || priorityFilter != null,
                            onAddClick = { viewModel.showAddDialog() },
                            onClearFilters = { viewModel.clearFilters() }
                        )
                    } else {
                        // Purchase orders list
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = purchaseOrders,
                                key = { it.id ?: 0 }
                            ) { order ->
                                EnhancedPurchaseOrderCard(
                                    order = order,
                                    onClick = { viewModel.showDetailsPanel(order) },
                                    onEdit = { viewModel.showEditDialog(order) },
                                    onDelete = { 
                                        orderToDelete = order
                                        showDeleteDialog = true
                                    },
                                    onStatusUpdate = { newStatus ->
                                        scope.launch {
                                            viewModel.updateOrderStatus(order.id!!, newStatus)
                                        }
                                    },
                                    onApprove = {
                                        scope.launch {
                                            viewModel.approveOrder(order.id!!)
                                        }
                                    }
                                )
                            }

                            // Loading indicator for pagination
                            if (isLoading && purchaseOrders.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Details panel
            AnimatedVisibility(
                visible = showDetailsPanel,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                selectedOrder?.let { order ->
                    Card(
                        modifier = Modifier
                            .width(400.dp)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        EnhancedPurchaseOrderDetailsPanel(
                            order = order,
                            onEdit = { viewModel.showEditDialog(order) },
                            onDelete = { 
                                orderToDelete = order
                                showDeleteDialog = true
                            },
                            onClose = { viewModel.hideDetailsPanel() },
                            onStatusUpdate = { newStatus ->
                                scope.launch {
                                    viewModel.updateOrderStatus(order.id!!, newStatus)
                                }
                            },
                            onApprove = {
                                scope.launch {
                                    viewModel.approveOrder(order.id!!)
                                }
                            },
                            onReceiveItems = { receivedItems ->
                                scope.launch {
                                    viewModel.receiveOrderItems(order.id!!, receivedItems)
                                }
                            },
                            onGeneratePdf = {
                                scope.launch {
                                    viewModel.generatePdf(order.id!!)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Dialogs
        if (showAddDialog) {
            EnhancedAddPurchaseOrderDialog(
                suppliers = suppliers,
                isLoading = viewModel.isCreating.collectAsState().value,
                onDismiss = { viewModel.hideAddDialog() },
                onSave = { orderData: PurchaseOrderData ->
                    scope.launch {
                        viewModel.createPurchaseOrder(orderData)
                    }
                }
            )
        }

        if (showEditDialog) {
            selectedOrder?.let { order ->
                EnhancedEditPurchaseOrderDialog(
                    order = order,
                    suppliers = suppliers,
                    isLoading = viewModel.isUpdating.collectAsState().value,
                    onDismiss = { viewModel.hideEditDialog() },
                    onSave = { orderData: PurchaseOrderData ->
                        scope.launch {
                            viewModel.updatePurchaseOrder(order.id!!, orderData)
                        }
                    }
                )
            }
        }

        if (showDeleteDialog) {
            orderToDelete?.let { order ->
                AlertDialog(
                    onDismissRequest = {}, // Disabled click-outside-to-dismiss
                    title = { Text("تأكيد الحذف") },
                    text = { 
                        Text("هل أنت متأكد من حذف طلب الشراء ${order.orderNumber}؟ لا يمكن التراجع عن هذا الإجراء.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.deletePurchaseOrder(order.id!!)
                                    showDeleteDialog = false
                                    orderToDelete = null
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
                                showDeleteDialog = false
                                orderToDelete = null
                            }
                        ) {
                            Text("إلغاء")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Header component for purchase orders screen
 */
@Composable
private fun PurchaseOrdersHeader(
    totalOrders: Int,
    onAddClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onNavigateToSuppliers: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "إدارة طلبات الشراء",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "إجمالي الطلبات: $totalOrders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onNavigateToSuppliers,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = "إدارة الموردين",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                IconButton(
                    onClick = onRefreshClick,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("طلب شراء جديد")
                }
            }
        }
    }
}

/**
 * Loading state component
 */
@Composable
private fun LoadingState() {
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
                strokeWidth = 4.dp
            )
            Text(
                text = "جاري تحميل طلبات الشراء...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Empty state component
 */
@Composable
private fun EmptyPurchaseOrdersState(
    hasFilters: Boolean,
    onAddClick: () -> Unit,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Text(
                text = if (hasFilters) "لا توجد طلبات شراء تطابق المرشحات" else "لا توجد طلبات شراء",
                style = MaterialTheme.typography.headlineSmall.copy(
                    textDirection = TextDirection.Rtl
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (hasFilters)
                    "جرب تعديل المرشحات أو إزالتها لعرض المزيد من النتائج"
                else
                    "ابدأ بإضافة طلب شراء جديد لإدارة مشترياتك",
                style = MaterialTheme.typography.bodyMedium.copy(
                    textDirection = TextDirection.Rtl
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasFilters) {
                    OutlinedButton(
                        onClick = onClearFilters,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مسح المرشحات",
                            style = MaterialTheme.typography.labelLarge.copy(
                                textDirection = TextDirection.Rtl
                            )
                        )
                    }
                }

                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "طلب شراء جديد",
                        style = MaterialTheme.typography.labelLarge.copy(
                            textDirection = TextDirection.Rtl
                        )
                    )
                }
            }
        }
    }
}
