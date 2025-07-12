@file:OptIn(ExperimentalMaterial3Api::class)

package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import data.api.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.viewmodels.PurchaseOrderData
import ui.viewmodels.PurchaseOrderItemData
import utils.PurchaseOrderMapper

/**
 * Header component for purchase orders screen
 */
@Composable
fun PurchaseOrdersHeader(
    totalOrders: Int,
    onAddClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onNavigateToSuppliers: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
            // Title and stats
            Column {
                Text(
                    text = "إدارة طلبات الشراء",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalOrders طلب شراء",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Navigate to suppliers button
                OutlinedButton(
                    onClick = onNavigateToSuppliers,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("الموردين")
                }

                // Refresh button
                IconButton(onClick = onRefreshClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Add button
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("طلب جديد")
                }
            }
        }
    }
}

/**
 * Filters component for purchase orders
 */
@Composable
fun PurchaseOrdersFilters(
    searchQuery: String,
    statusFilter: String?,
    supplierFilter: Long?,
    priorityFilter: String?,
    suppliers: List<SupplierDTO>,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (String?) -> Unit,
    onSupplierFilterChange: (Long?) -> Unit,
    onPriorityFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("البحث في طلبات الشراء") },
                placeholder = { Text("رقم الطلب، اسم المورد، الملاحظات...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                // Status filter
                item {
                    var statusExpanded by remember { mutableStateOf(false) }
                    val statusOptions = listOf(
                        null to "جميع الحالات",
                        "PENDING" to "في انتظار الموافقة",
                        "APPROVED" to "تمت الموافقة",
                        "SENT" to "تم الإرسال",
                        "DELIVERED" to "تم التسليم",
                        "CANCELLED" to "ملغي"
                    )

                    FilterChip(
                        selected = statusFilter != null,
                        onClick = { statusExpanded = true },
                        label = { 
                            Text(statusOptions.find { it.first == statusFilter }?.second ?: "الحالة")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.FilterList, contentDescription = null)
                        }
                    )

                    DropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statusOptions.forEach { (value, display) ->
                            DropdownMenuItem(
                                text = { Text(display) },
                                onClick = {
                                    onStatusFilterChange(value)
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Supplier filter
                item {
                    var supplierExpanded by remember { mutableStateOf(false) }

                    FilterChip(
                        selected = supplierFilter != null,
                        onClick = { supplierExpanded = true },
                        label = { 
                            Text(
                                suppliers.find { it.id == supplierFilter }?.name ?: "المورد"
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Business, contentDescription = null)
                        }
                    )

                    DropdownMenu(
                        expanded = supplierExpanded,
                        onDismissRequest = { supplierExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("جميع الموردين") },
                            onClick = {
                                onSupplierFilterChange(null)
                                supplierExpanded = false
                            }
                        )
                        suppliers.forEach { supplier ->
                            DropdownMenuItem(
                                text = { Text(supplier.name) },
                                onClick = {
                                    onSupplierFilterChange(supplier.id)
                                    supplierExpanded = false
                                }
                            )
                        }
                    }
                }

                // Priority filter
                item {
                    var priorityExpanded by remember { mutableStateOf(false) }
                    val priorityOptions = listOf(
                        null to "جميع الأولويات",
                        "LOW" to "منخفضة",
                        "NORMAL" to "عادية",
                        "HIGH" to "عالية",
                        "URGENT" to "عاجلة"
                    )

                    FilterChip(
                        selected = priorityFilter != null,
                        onClick = { priorityExpanded = true },
                        label = { 
                            Text(priorityOptions.find { it.first == priorityFilter }?.second ?: "الأولوية")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null)
                        }
                    )

                    DropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        priorityOptions.forEach { (value, display) ->
                            DropdownMenuItem(
                                text = { Text(display) },
                                onClick = {
                                    onPriorityFilterChange(value)
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }

                // Clear filters
                if (statusFilter != null || supplierFilter != null || priorityFilter != null || searchQuery.isNotEmpty()) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = onClearFilters,
                            label = { Text("إزالة المرشحات") },
                            leadingIcon = {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Purchase Order Card Component
 */
@Composable
fun EnhancedPurchaseOrderCard(
    order: PurchaseOrderDTO,
    onClick: (PurchaseOrderDTO) -> Unit,
    onEdit: (PurchaseOrderDTO) -> Unit,
    onDelete: (PurchaseOrderDTO) -> Unit,
    onStatusUpdate: (String) -> Unit,
    onApprove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick(order) },
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 4.dp else 1.dp
        ),
        border = BorderStroke(
            width = if (isHovered) 1.5.dp else 1.dp,
            color = if (isHovered)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = PurchaseOrderMapper.formatOrderNumber(order.orderNumber),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    order.supplierName?.let { supplierName ->
                        Text(
                            text = supplierName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Status and Priority Badges
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusBadge(status = order.status)
                    PriorityBadge(priority = order.priority)
                }
            }

            // Order Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "تاريخ الطلب: ${PurchaseOrderMapper.formatDate(order.orderDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                order.expectedDeliveryDate?.let { deliveryDate ->
                    Text(
                        text = "التسليم المتوقع: ${PurchaseOrderMapper.formatDate(deliveryDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (PurchaseOrderMapper.isOrderOverdue(deliveryDate))
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Statistics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(
                    icon = Icons.Default.ShoppingCart,
                    label = "العناصر",
                    value = "${order.itemsCount ?: 0}",
                    color = AppTheme.colors.info
                )

                StatItem(
                    icon = Icons.Default.AttachMoney,
                    label = "المبلغ",
                    value = PurchaseOrderMapper.formatAmount(order.totalAmount),
                    color = AppTheme.colors.success
                )

                StatItem(
                    icon = Icons.Default.Inventory,
                    label = "الاستلام",
                    value = PurchaseOrderMapper.formatReceivingProgress(order.receivingProgress),
                    color = AppTheme.colors.warning
                )
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                // Quick action buttons based on status
                when (order.status?.uppercase()) {
                    "PENDING" -> {
                        IconButton(
                            onClick = onApprove,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "موافقة",
                                modifier = Modifier.size(16.dp),
                                tint = AppTheme.colors.success
                            )
                        }
                    }
                    "APPROVED" -> {
                        IconButton(
                            onClick = { onStatusUpdate("SENT") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "إرسال",
                                modifier = Modifier.size(16.dp),
                                tint = AppTheme.colors.info
                            )
                        }
                    }
                    "SENT" -> {
                        IconButton(
                            onClick = { onStatusUpdate("DELIVERED") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = "تسليم",
                                modifier = Modifier.size(16.dp),
                                tint = AppTheme.colors.warning
                            )
                        }
                    }
                }

                if (PurchaseOrderMapper.canModifyOrder(order.status)) {
                    IconButton(
                        onClick = { onEdit(order) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (PurchaseOrderMapper.canDeleteOrder(order.status)) {
                    IconButton(
                        onClick = { onDelete(order) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Status Badge Component
 */
@Composable
private fun StatusBadge(status: String?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(PurchaseOrderMapper.getStatusColor(status).removePrefix("#").toLong(16) or 0xFF000000).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = PurchaseOrderMapper.getStatusDisplayName(status),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(PurchaseOrderMapper.getStatusColor(status).removePrefix("#").toLong(16) or 0xFF000000),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Priority Badge Component
 */
@Composable
private fun PriorityBadge(priority: String?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(PurchaseOrderMapper.getPriorityColor(priority).removePrefix("#").toLong(16) or 0xFF000000).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = PurchaseOrderMapper.getPriorityIcon(priority),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = PurchaseOrderMapper.getPriorityDisplayName(priority),
                style = MaterialTheme.typography.labelSmall,
                color = Color(PurchaseOrderMapper.getPriorityColor(priority).removePrefix("#").toLong(16) or 0xFF000000),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Stat Item Component for Purchase Order Card
 */
@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Enhanced Add Purchase Order Dialog
 */
@Composable
fun EnhancedAddPurchaseOrderDialog(
    suppliers: List<SupplierDTO>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (PurchaseOrderData) -> Unit
) {
    // Form state
    var selectedSupplierId by remember { mutableStateOf(0L) }
    var expectedDeliveryDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") }
    var shippingAddress by remember { mutableStateOf("") }
    var taxRate by remember { mutableStateOf(15.0) }
    var shippingCost by remember { mutableStateOf(0.0) }
    var discountAmount by remember { mutableStateOf(0.0) }
    var notes by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<PurchaseOrderItemData>>(emptyList()) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Purchase Order",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form content (simplified for now)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Supplier selection
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = suppliers.find { it.id == selectedSupplierId }?.name ?: "Select Supplier",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Supplier") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                suppliers.forEach { supplier ->
                                    DropdownMenuItem(
                                        text = { Text(supplier.name) },
                                        onClick = {
                                            selectedSupplierId = supplier.id!!
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        // Shipping Address
                        OutlinedTextField(
                            value = shippingAddress,
                            onValueChange = { shippingAddress = it },
                            label = { Text("Shipping Address") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    item {
                        // Notes
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (selectedSupplierId > 0 && shippingAddress.isNotBlank()) {
                                val orderData = PurchaseOrderData(
                                    supplierId = selectedSupplierId,
                                    expectedDeliveryDate = expectedDeliveryDate.takeIf { it.isNotBlank() },
                                    priority = priority,
                                    shippingAddress = shippingAddress,
                                    taxRate = taxRate,
                                    shippingCost = shippingCost,
                                    discountAmount = discountAmount,
                                    notes = notes.takeIf { it.isNotBlank() },
                                    items = items
                                )
                                onSave(orderData)
                            }
                        },
                        enabled = !isLoading && selectedSupplierId > 0 && shippingAddress.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create Order")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Edit Purchase Order Dialog
 */
@Composable
fun EnhancedEditPurchaseOrderDialog(
    order: PurchaseOrderDTO,
    suppliers: List<SupplierDTO>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (PurchaseOrderData) -> Unit
) {
    // Pre-fill with existing order data
    var selectedSupplierId by remember { mutableStateOf(order.supplierId) }
    var expectedDeliveryDate by remember { mutableStateOf(order.expectedDeliveryDate ?: "") }
    var priority by remember { mutableStateOf(order.priority ?: "NORMAL") }
    var shippingAddress by remember { mutableStateOf(order.shippingAddress) }
    var taxRate by remember { mutableStateOf(order.taxRate ?: 15.0) }
    var shippingCost by remember { mutableStateOf(order.shippingCost ?: 0.0) }
    var discountAmount by remember { mutableStateOf(order.discountAmount ?: 0.0) }
    var notes by remember { mutableStateOf(order.notes ?: "") }
    var items by remember { mutableStateOf(order.items.map {
        PurchaseOrderItemData(
            productId = it.productId,
            quantity = it.quantity,
            unitPrice = it.unitPrice,
            notes = it.notes
        )
    }) }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعديل طلب الشراء ${order.orderNumber}",
                        style = MaterialTheme.typography.headlineSmall,
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

                Spacer(modifier = Modifier.height(16.dp))

                // Simplified form for editing (similar structure to add dialog)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = shippingAddress,
                            onValueChange = { shippingAddress = it },
                            label = { Text("عنوان الشحن") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            onSave(
                                PurchaseOrderData(
                                    supplierId = selectedSupplierId,
                                    expectedDeliveryDate = expectedDeliveryDate.ifBlank { null },
                                    priority = priority,
                                    shippingAddress = shippingAddress,
                                    taxRate = taxRate,
                                    shippingCost = shippingCost,
                                    discountAmount = discountAmount,
                                    notes = notes.ifBlank { null },
                                    items = items
                                )
                            )
                        },
                        enabled = !isLoading,
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
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("حفظ التغييرات")
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Purchase Order Details Panel
 */
@Composable
fun EnhancedPurchaseOrderDetailsPanel(
    order: PurchaseOrderDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    onStatusUpdate: (String) -> Unit,
    onApprove: () -> Unit,
    onReceiveItems: (List<ReceivedItemDTO>) -> Unit,
    onGeneratePdf: () -> Unit
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
                text = "تفاصيل الطلب",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onGeneratePdf) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "تصدير PDF",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (PurchaseOrderMapper.canModifyOrder(order.status)) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (PurchaseOrderMapper.canDeleteOrder(order.status)) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Order Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = PurchaseOrderMapper.formatOrderNumber(order.orderNumber),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        order.supplierName?.let { supplierName ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = supplierName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "تاريخ الطلب: ${PurchaseOrderMapper.formatDate(order.orderDate)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Status and Priority
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
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
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "الحالة",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            StatusBadge(status = order.status)
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
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "الأولوية",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PriorityBadge(priority = order.priority)
                        }
                    }
                }
            }

            // Financial Summary
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
                            text = "الملخص المالي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المجموع الفرعي:")
                            Text(PurchaseOrderMapper.formatAmount(order.subtotal))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الضريبة:")
                            Text(PurchaseOrderMapper.formatAmount(order.taxAmount))
                        }

                        order.shippingCost?.let { shipping ->
                            if (shipping > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الشحن:")
                                    Text(PurchaseOrderMapper.formatAmount(shipping))
                                }
                            }
                        }

                        order.discountAmount?.let { discount ->
                            if (discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الخصم:")
                                    Text("-${PurchaseOrderMapper.formatAmount(discount)}")
                                }
                            }
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "المجموع الكلي:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = PurchaseOrderMapper.formatAmount(order.totalAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Action buttons based on status
            item {
                when (order.status?.uppercase()) {
                    "PENDING" -> {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.success
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("موافقة على الطلب")
                        }
                    }
                    "APPROVED" -> {
                        Button(
                            onClick = { onStatusUpdate("SENT") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.info
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرسال للمورد")
                        }
                    }
                    "SENT" -> {
                        Button(
                            onClick = { onStatusUpdate("DELIVERED") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.warning
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تأكيد التسليم")
                        }
                    }
                }
            }
        }
    }
}
