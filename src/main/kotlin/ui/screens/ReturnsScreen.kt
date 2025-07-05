package ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.*
import kotlinx.datetime.LocalDateTime
import UiUtils
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.utils.ResponsiveUtils.getResponsivePadding
import ui.utils.ResponsiveUtils.getScreenInfo

@Composable
fun ReturnsScreen() {
    RTLProvider {
        val responsivePadding = getResponsivePadding()
        val screenInfo = getScreenInfo()
        val isDesktop = screenInfo.isDesktop

        var showNewReturnDialog by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedStatus by remember { mutableStateOf("الكل") }
        var selectedTab by remember { mutableStateOf(ReturnTab.RETURNS) }
        var showQuickActions by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(responsivePadding.screen),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Enhanced Header Section with Tabs
                item {
                    EnhancedReturnsHeaderWithTabs(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        onNewReturn = { showNewReturnDialog = true },
                        isDesktop = isDesktop
                    )
                }

                // Tab Content
                when (selectedTab) {
                    ReturnTab.RETURNS -> {
                        // Search and Filters Section
                        item {
                            EnhancedSearchAndFilters(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                selectedStatus = selectedStatus,
                                onStatusChange = { selectedStatus = it }
                            )
                        }

                        // Enhanced Statistics Dashboard
                        item {
                            EnhancedReturnsStatistics(isDesktop = isDesktop)
                        }

                        // Enhanced Returns List
                        item {
                            EnhancedReturnsList(
                                searchQuery = searchQuery,
                                selectedStatus = selectedStatus
                            )
                        }
                    }
                    ReturnTab.ANALYTICS -> {
                        item {
                            ReturnsAnalyticsContent(isDesktop = isDesktop)
                        }
                    }
                    ReturnTab.POLICIES -> {
                        item {
                            ReturnsPoliciesContent(isDesktop = isDesktop)
                        }
                    }
                }
            }

            // Floating Action Button
            if (!isDesktop) {
                FloatingActionButton(
                    onClick = { showNewReturnDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "إرجاع جديد",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Dialogs
        if (showNewReturnDialog) {
            EnhancedNewReturnDialog(
                onDismiss = { showNewReturnDialog = false },
                onConfirm = { returnData ->
                    // Handle new return creation
                    showNewReturnDialog = false
                }
            )
        }
    }
}

// Old ReturnCard function removed - replaced with EnhancedReturnCard

@Composable
fun ReturnsStatsCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "إجمالي المرتجعات",
            value = "156",
            icon = Icons.AutoMirrored.Filled.AssignmentReturn,
            iconColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "في الانتظار",
            value = "23",
            icon = Icons.Default.Schedule,
            iconColor = AppTheme.colors.warning,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "تم الاسترداد",
            value = UiUtils.formatCurrency(12500.0),
            icon = Icons.Default.AccountBalance,
            iconColor = AppTheme.colors.success,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "معدل المرتجعات",
            value = "3.2%",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            iconColor = AppTheme.colors.info,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ReturnsListContent(
    searchQuery: String,
    selectedStatus: String,
    onReturnClick: (Return) -> Unit
) {
    val returns = remember {
        listOf(
            Return(1, 101, LocalDateTime(2024, 1, 15, 10, 0),
                listOf(ReturnItem(1, 2, 50.0, ItemCondition.DEFECTIVE)),
                ReturnReason.DEFECTIVE, ReturnStatus.APPROVED, 100.0, "منتج معيب"),
            Return(2, 102, LocalDateTime(2024, 1, 16, 14, 30),
                listOf(ReturnItem(2, 1, 75.0, ItemCondition.GOOD)),
                ReturnReason.CUSTOMER_CHANGE_MIND, ReturnStatus.PENDING, 75.0, "تغيير رأي العميل"),
            Return(3, 103, LocalDateTime(2024, 1, 17, 9, 15),
                listOf(ReturnItem(3, 3, 25.0, ItemCondition.DAMAGED)),
                ReturnReason.DAMAGED_SHIPPING, ReturnStatus.REFUNDED, 75.0, "تضرر أثناء الشحن"),
            Return(4, 104, LocalDateTime(2024, 1, 18, 16, 45),
                listOf(ReturnItem(4, 1, 120.0, ItemCondition.NEW)),
                ReturnReason.WRONG_ITEM, ReturnStatus.EXCHANGED, 120.0, "منتج خاطئ"),
            Return(5, 105, LocalDateTime(2024, 1, 19, 11, 20),
                listOf(ReturnItem(5, 2, 40.0, ItemCondition.DEFECTIVE)),
                ReturnReason.EXPIRED, ReturnStatus.REJECTED, 0.0, "منتج منتهي الصلاحية")
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(returns.filter { returnItem ->
            (selectedStatus == "الكل" || returnItem.status.displayName == selectedStatus) &&
            (searchQuery.isEmpty() || returnItem.id.toString().contains(searchQuery) ||
             returnItem.originalSaleId.toString().contains(searchQuery))
        }) { returnItem ->
            ReturnCard(
                returnItem = returnItem,
                onClick = { onReturnClick(returnItem) }
            )
        }
    }
}

@Composable
fun ReturnCard(
    returnItem: Return,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Status and Actions
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = getReturnStatusColor(returnItem.status).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = returnItem.status.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = getReturnStatusColor(returnItem.status),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { /* معالجة */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "معالجة",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { /* طباعة */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = "طباعة",
                                tint = AppTheme.colors.info,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Return Info
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "مرتجع #${returnItem.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "فاتورة أصلية #${returnItem.originalSaleId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = returnItem.date.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Return Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "عدد القطع: ${returnItem.items.sumOf { it.quantity }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "السبب: ${returnItem.reason.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = UiUtils.formatCurrency(returnItem.refundAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (returnItem.refundAmount > 0) AppTheme.colors.success else AppTheme.colors.error
                    )
                    Text(
                        text = "مبلغ الاسترداد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (returnItem.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ملاحظات: ${returnItem.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

// Old ReturnsAnalyticsContent function removed - replaced with enhanced version

// Old ReasonStatsItem function removed - replaced with enhanced version

// Removed duplicate MonthlyTrendItem function - using enhanced version below

// Old PerformanceMetricItem function removed - replaced with enhanced version

// Old ReturnPoliciesContent function removed - replaced with enhanced version

// Old PolicyItemCard function removed - replaced with enhanced version

// Enhanced New Return Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedNewReturnDialog(
    onDismiss: () -> Unit,
    onConfirm: (ReturnData) -> Unit
) {
    var originalSaleId by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf(ReturnReason.DEFECTIVE) }
    var notes by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var refundAmount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                        text = "إضافة مرتجع جديد",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = originalSaleId,
                    onValueChange = { originalSaleId = it },
                    label = { Text("رقم الفاتورة الأصلية") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("اسم العميل") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = refundAmount,
                    onValueChange = { refundAmount = it },
                    label = { Text("مبلغ الاسترداد") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedReason.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("سبب الإرجاع") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Help,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ReturnReason.entries.forEach { reason ->
                            DropdownMenuItem(
                                text = { Text(reason.displayName) },
                                onClick = {
                                    selectedReason = reason
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val returnData = ReturnData(
                        originalSaleId = originalSaleId.toIntOrNull() ?: 0,
                        reason = selectedReason,
                        notes = notes
                    )
                    onConfirm(returnData)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "إضافة",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "إلغاء",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnDetailsDialog(
    returnItem: Return,
    onDismiss: () -> Unit,
    onStatusUpdate: (ReturnStatus) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(returnItem.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تفاصيل المرتجع #${returnItem.id}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ReturnDetailItem("رقم الفاتورة الأصلية", returnItem.originalSaleId.toString())
                ReturnDetailItem("تاريخ الإرجاع", returnItem.date.date.toString())
                ReturnDetailItem("السبب", returnItem.reason.displayName)
                ReturnDetailItem("مبلغ الاسترداد", UiUtils.formatCurrency(returnItem.refundAmount))

                Text(
                    text = "القطع المرتجعة:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                returnItem.items.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            ReturnDetailItem("الكمية", item.quantity.toString())
                            ReturnDetailItem("السعر", UiUtils.formatCurrency(item.unitPrice))
                            ReturnDetailItem("الحالة", item.condition.displayName)
                        }
                    }
                }

                if (returnItem.notes.isNotEmpty()) {
                    ReturnDetailItem("ملاحظات", returnItem.notes)
                }

                // Status Update
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("تحديث الحالة") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ReturnStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.displayName) },
                                onClick = {
                                    selectedStatus = status
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStatusUpdate(selectedStatus) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "تحديث",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "إغلاق",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ReturnDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Text(
        text = "$label:",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

data class ReasonStats(
    val name: String,
    val percentage: Int,
    val color: Color
)

data class PolicyItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class ReturnData(
    val originalSaleId: Int,
    val reason: ReturnReason,
    val notes: String
)

enum class ReturnTab(val title: String) {
    RETURNS("قائمة المرتجعات"),
    ANALYTICS("تحليلات المرتجعات"),
    POLICIES("سياسات الإرجاع")
}

// Enhanced Header Component with Tabs
@Composable
private fun EnhancedReturnsHeaderWithTabs(
    selectedTab: ReturnTab,
    onTabSelected: (ReturnTab) -> Unit,
    onNewReturn: () -> Unit,
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Enhanced gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isDesktop) 220.dp else 200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isDesktop) 32.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Text with enhanced styling
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "إدارة المرتجعات والإلغاءات",
                        style = if (isDesktop) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "معالجة وتتبع طلبات الإرجاع والاستبدال بكفاءة عالية",
                        style = if (isDesktop) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tab Navigation
                ScrollableTabRow(
                    selectedTabIndex = ReturnTab.values().indexOf(selectedTab),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[ReturnTab.values().indexOf(selectedTab)]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    ReturnTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        )
                    }
                }

                // Action Buttons (only show on Returns tab)
                if (selectedTab == ReturnTab.RETURNS) {
                    RTLRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EnhancedActionButton(
                            text = "إرجاع جديد",
                            icon = Icons.Default.Add,
                            onClick = onNewReturn,
                            isPrimary = true
                        )
                        EnhancedActionButton(
                            text = "تصدير التقرير",
                            icon = Icons.Default.FileDownload,
                            onClick = { /* Export returns report */ },
                            isPrimary = false
                        )
                    }
                }
            }
        }
    }
}

// Returns Analytics Content
@Composable
private fun ReturnsAnalyticsContent(
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Analytics Overview Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isDesktop) 3 else 2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(if (isDesktop) 200.dp else 320.dp)
        ) {
            item {
                AnalyticsCard(
                    title = "معدل الإرجاع الشهري",
                    value = "2.8%",
                    change = "-0.5%",
                    isPositive = false,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = AppTheme.colors.success
                )
            }
            item {
                AnalyticsCard(
                    title = "متوسط وقت المعالجة",
                    value = "1.5 يوم",
                    change = "-12 ساعة",
                    isPositive = true,
                    icon = Icons.Default.Schedule,
                    color = AppTheme.colors.info
                )
            }
            item {
                AnalyticsCard(
                    title = "رضا العملاء",
                    value = "4.6/5",
                    change = "+0.2",
                    isPositive = true,
                    icon = Icons.Default.Star,
                    color = AppTheme.colors.warning
                )
            }
        }

        // Returns by Reason Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardStyles.elevatedCardColors(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardStyles.elevatedCardElevation()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "أسباب الإرجاع",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Reason breakdown
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReasonBreakdownItem("منتج معيب", 35, AppTheme.colors.error)
                    ReasonBreakdownItem("تغيير رأي العميل", 28, AppTheme.colors.warning)
                    ReasonBreakdownItem("منتج خاطئ", 20, AppTheme.colors.info)
                    ReasonBreakdownItem("تضرر أثناء الشحن", 12, AppTheme.colors.purple)
                    ReasonBreakdownItem("أخرى", 5, MaterialTheme.colorScheme.outline)
                }
            }
        }

        // Monthly Trend
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardStyles.elevatedCardColors(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardStyles.elevatedCardElevation()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "اتجاه المرتجعات الشهرية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MonthlyTrendItem("يناير", 45, MaterialTheme.colorScheme.primary)
                    MonthlyTrendItem("فبراير", 38, AppTheme.colors.success)
                    MonthlyTrendItem("مارس", 52, AppTheme.colors.warning)
                    MonthlyTrendItem("أبريل", 41, AppTheme.colors.info)
                }
            }
        }
    }
}

// Returns Policies Content
@Composable
private fun ReturnsPoliciesContent(
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Policy Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardStyles.elevatedCardColors(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardStyles.elevatedCardElevation()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "سياسات الإرجاع والاستبدال",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "إدارة وتحديث سياسات الإرجاع لضمان تجربة عملاء متميزة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Policy Items
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isDesktop) 2 else 1),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(if (isDesktop) 400.dp else 600.dp)
        ) {
            item {
                PolicyCard(
                    title = "فترة الإرجاع",
                    description = "يمكن إرجاع المنتجات خلال 30 يوماً من تاريخ الشراء",
                    icon = Icons.Default.Schedule,
                    color = AppTheme.colors.info
                )
            }
            item {
                PolicyCard(
                    title = "حالة المنتج",
                    description = "يجب أن يكون المنتج في حالته الأصلية مع العبوة",
                    icon = Icons.Default.Inventory,
                    color = AppTheme.colors.success
                )
            }
            item {
                PolicyCard(
                    title = "طريقة الاسترداد",
                    description = "يتم الاسترداد بنفس طريقة الدفع الأصلية",
                    icon = Icons.Default.Payment,
                    color = AppTheme.colors.warning
                )
            }
            item {
                PolicyCard(
                    title = "المنتجات المستثناة",
                    description = "بعض المنتجات غير قابلة للإرجاع لأسباب صحية",
                    icon = Icons.Default.Block,
                    color = AppTheme.colors.error
                )
            }
        }
    }
}

// Analytics Card Component
@Composable
private fun AnalyticsCard(
    title: String,
    value: String,
    change: String,
    isPositive: Boolean,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = color
                    )

                    Surface(
                        color = if (isPositive) AppTheme.colors.success.copy(alpha = 0.1f)
                               else AppTheme.colors.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = change,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) AppTheme.colors.success else AppTheme.colors.error,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Reason Breakdown Item
@Composable
private fun ReasonBreakdownItem(
    reason: String,
    percentage: Int,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

// Monthly Trend Item
@Composable
fun MonthlyTrendItem(
    month: String,
    value: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height((value * 2).dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = month,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Policy Card Component
@Composable
private fun PolicyCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = color
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

// Enhanced Search and Filters Component
@Composable
private fun EnhancedSearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusOptions = listOf("الكل", "معلق", "مكتمل", "مرفوض", "تم الاسترداد")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("البحث في المرتجعات") },
                placeholder = { Text("رقم المرتجع، اسم العميل، أو المنتج...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Status Filter
            Text(
                text = "تصفية حسب الحالة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(statusOptions) { status ->
                    FilterChip(
                        onClick = { onStatusChange(status) },
                        label = { Text(status) },
                        selected = selectedStatus == status,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

// Enhanced Statistics Component
@Composable
private fun EnhancedReturnsStatistics(
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "إحصائيات المرتجعات",
            style = if (isDesktop) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isDesktop) {
            // Desktop: 4 cards in a row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    ModernStatCard(
                        title = "إجمالي المرتجعات",
                        value = "156",
                        subtitle = "طلب إرجاع",
                        icon = Icons.AutoMirrored.Filled.AssignmentReturn,
                        iconColor = MaterialTheme.colorScheme.primary,
                        trend = "+12 هذا الشهر"
                    )
                }
                item {
                    ModernStatCard(
                        title = "في الانتظار",
                        value = "23",
                        subtitle = "طلب معلق",
                        icon = Icons.Default.Schedule,
                        iconColor = AppTheme.colors.warning,
                        trend = "+5 اليوم"
                    )
                }
                item {
                    ModernStatCard(
                        title = "تم الاسترداد",
                        value = UiUtils.formatCurrency(12500.0),
                        subtitle = "قيمة مستردة",
                        icon = Icons.Default.AccountBalance,
                        iconColor = AppTheme.colors.success,
                        trend = "+8.5%"
                    )
                }
                item {
                    ModernStatCard(
                        title = "معدل المرتجعات",
                        value = "3.2%",
                        subtitle = "من إجمالي المبيعات",
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        iconColor = AppTheme.colors.info,
                        trend = "-0.3%"
                    )
                }
            }
        } else {
            // Mobile/Tablet: 2 cards per row
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(320.dp)
            ) {
                item {
                    ModernStatCard(
                        title = "إجمالي المرتجعات",
                        value = "156",
                        subtitle = "طلب إرجاع",
                        icon = Icons.AutoMirrored.Filled.AssignmentReturn,
                        iconColor = MaterialTheme.colorScheme.primary,
                        trend = "+12"
                    )
                }
                item {
                    ModernStatCard(
                        title = "في الانتظار",
                        value = "23",
                        subtitle = "طلب معلق",
                        icon = Icons.Default.Schedule,
                        iconColor = AppTheme.colors.warning,
                        trend = "+5"
                    )
                }
                item {
                    ModernStatCard(
                        title = "تم الاسترداد",
                        value = "12.5K ر.س",
                        subtitle = "قيمة مستردة",
                        icon = Icons.Default.AccountBalance,
                        iconColor = AppTheme.colors.success,
                        trend = "+8.5%"
                    )
                }
                item {
                    ModernStatCard(
                        title = "معدل المرتجعات",
                        value = "3.2%",
                        subtitle = "من المبيعات",
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        iconColor = AppTheme.colors.info,
                        trend = "-0.3%"
                    )
                }
            }
        }
    }
}

// Modern Stat Card Component
@Composable
private fun ModernStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    trend: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                iconColor.copy(alpha = 0.02f),
                                iconColor.copy(alpha = 0.08f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon and trend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = iconColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = iconColor
                        )
                    }

                    Surface(
                        color = if (trend.startsWith("+")) AppTheme.colors.success.copy(alpha = 0.1f)
                               else AppTheme.colors.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = trend,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (trend.startsWith("+")) AppTheme.colors.success else AppTheme.colors.error,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Value and title
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// Enhanced Returns List Component
@Composable
private fun EnhancedReturnsList(
    searchQuery: String,
    selectedStatus: String,
    modifier: Modifier = Modifier
) {
    // Sample returns data
    val returns = remember {
        listOf(
            Return(1, 101, LocalDateTime(2024, 1, 15, 10, 0),
                listOf(ReturnItem(1, 2, 50.0, ItemCondition.DEFECTIVE)),
                ReturnReason.DEFECTIVE, ReturnStatus.APPROVED, 100.0, "منتج معيب"),
            Return(2, 102, LocalDateTime(2024, 1, 16, 14, 30),
                listOf(ReturnItem(2, 1, 75.0, ItemCondition.GOOD)),
                ReturnReason.CUSTOMER_CHANGE_MIND, ReturnStatus.PENDING, 75.0, "تغيير رأي العميل"),
            Return(3, 103, LocalDateTime(2024, 1, 17, 9, 15),
                listOf(ReturnItem(3, 3, 25.0, ItemCondition.DAMAGED)),
                ReturnReason.DAMAGED_SHIPPING, ReturnStatus.REFUNDED, 75.0, "تضرر أثناء الشحن"),
            Return(4, 104, LocalDateTime(2024, 1, 18, 16, 45),
                listOf(ReturnItem(4, 1, 120.0, ItemCondition.NEW)),
                ReturnReason.WRONG_ITEM, ReturnStatus.EXCHANGED, 120.0, "منتج خاطئ"),
            Return(5, 105, LocalDateTime(2024, 1, 19, 11, 20),
                listOf(ReturnItem(5, 2, 40.0, ItemCondition.DEFECTIVE)),
                ReturnReason.EXPIRED, ReturnStatus.REJECTED, 0.0, "منتج منتهي الصلاحية")
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    text = "طلبات الإرجاع الحديثة",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = { /* View all returns */ }
                ) {
                    Text(
                        text = "عرض الكل",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Returns List
            if (returns.isEmpty()) {
                EnhancedEmptyState(
                    icon = Icons.AutoMirrored.Filled.AssignmentReturn,
                    title = "لا توجد مرتجعات",
                    description = "لم يتم تسجيل أي طلبات إرجاع حتى الآن",
                    isCompact = true
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(returns.filter { returnItem ->
                        (selectedStatus == "الكل" || returnItem.status.displayName == selectedStatus) &&
                        (searchQuery.isEmpty() || returnItem.id.toString().contains(searchQuery) ||
                         returnItem.originalSaleId.toString().contains(searchQuery))
                    }) { returnItem ->
                        EnhancedReturnCard(
                            returnItem = returnItem,
                            onClick = { /* Handle return click */ }
                        )
                    }
                }
            }
        }
    }
}

// Enhanced Return Card Component
@Composable
private fun EnhancedReturnCard(
    returnItem: Return,
    onClick: () -> Unit,
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
            defaultElevation = 1.dp,
            hoveredElevation = 4.dp,
            pressedElevation = 0.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RTLRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Chip
                    Surface(
                        color = when (returnItem.status) {
                            ReturnStatus.PENDING -> AppTheme.colors.warning.copy(alpha = 0.1f)
                            ReturnStatus.APPROVED -> AppTheme.colors.info.copy(alpha = 0.1f)
                            ReturnStatus.REFUNDED -> AppTheme.colors.success.copy(alpha = 0.1f)
                            ReturnStatus.EXCHANGED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ReturnStatus.REJECTED -> AppTheme.colors.error.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = returnItem.status.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (returnItem.status) {
                                ReturnStatus.PENDING -> AppTheme.colors.warning
                                ReturnStatus.APPROVED -> AppTheme.colors.info
                                ReturnStatus.REFUNDED -> AppTheme.colors.success
                                ReturnStatus.EXCHANGED -> MaterialTheme.colorScheme.primary
                                ReturnStatus.REJECTED -> AppTheme.colors.error
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Return ID and Sale ID
                    Column {
                        Text(
                            text = "مرتجع #${returnItem.id}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "من فاتورة #${returnItem.originalSaleId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Arrow Icon
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "عرض التفاصيل",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Divider
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            // Details Row
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "السبب: ${returnItem.reason.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "التاريخ: ${returnItem.date.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = UiUtils.formatCurrency(returnItem.refundAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (returnItem.refundAmount > 0) AppTheme.colors.success else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Enhanced Action Button Component
@Composable
private fun EnhancedActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPrimary) 3.dp else 1.dp,
            hoveredElevation = if (isPrimary) 6.dp else 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        RTLRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Enhanced Empty State Component
@Composable
private fun EnhancedEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompact) 64.dp else 80.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(if (isCompact) 16.dp else 20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(if (isCompact) 32.dp else 40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun getReturnStatusColor(status: ReturnStatus): Color {
    return when (status) {
        ReturnStatus.PENDING -> AppTheme.colors.warning
        ReturnStatus.APPROVED -> AppTheme.colors.info
        ReturnStatus.REJECTED -> AppTheme.colors.error
        ReturnStatus.REFUNDED -> AppTheme.colors.success
        ReturnStatus.EXCHANGED -> MaterialTheme.colorScheme.primary
    }
}
