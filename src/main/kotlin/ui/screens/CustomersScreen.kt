@file:OptIn(ExperimentalAnimationApi::class)

package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import data.*
import data.api.*
import ui.components.*
import data.di.AppDependencies
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.viewmodels.CustomerViewModel
import ui.viewmodels.ViewModelFactory
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen() {
    // Initialize ViewModel
    val customerViewModel = remember { ViewModelFactory.createCustomerViewModel() }

    // Collect state from ViewModel
    val customers by customerViewModel.customers.collectAsState()
    val filteredCustomers by customerViewModel.filteredCustomers.collectAsState()
    val isLoading by customerViewModel.isLoading.collectAsState()
    val error by customerViewModel.error.collectAsState()
    val searchQuery by customerViewModel.searchQuery.collectAsState()
    val sortBy by customerViewModel.sortBy.collectAsState()
    val isCreatingCustomer by customerViewModel.isCreatingCustomer.collectAsState()
    val isUpdatingCustomer by customerViewModel.isUpdatingCustomer.collectAsState()
    val isDeletingCustomer by customerViewModel.isDeletingCustomer.collectAsState()

    RTLProvider {
        var showAddCustomerDialog by remember { mutableStateOf(false) }
        var editingCustomer by remember { mutableStateOf<CustomerDTO?>(null) }
        var selectedCity by remember { mutableStateOf("الكل") }
        var showCustomerDetails by remember { mutableStateOf(false) }
        var selectedCustomer by remember { mutableStateOf<CustomerDTO?>(null) }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var showForeignKeyWarning by remember { mutableStateOf(false) }
        var showCascadeDeleteConfirmation by remember { mutableStateOf(false) }
        var customerToDelete by remember { mutableStateOf<CustomerDTO?>(null) }
        var foreignKeyError by remember { mutableStateOf<ApiException.ForeignKeyConstraintError?>(null) }
        val coroutineScope = rememberCoroutineScope()

        // Currency formatter for Arabic locale
        val currencyFormatter = remember {
            NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
                currency = Currency.getInstance("SAR")
            }
        }

        // Extract cities from customers for filtering
        val cities = remember(customers) {
            listOf("الكل") + customers.mapNotNull { it.address }.distinct()
        }

        // Apply city filter to already filtered customers from ViewModel
        val displayCustomers = remember(filteredCustomers, selectedCity) {
            if (selectedCity != "الكل") {
                filteredCustomers.filter { it.address == selectedCity }
            } else {
                filteredCustomers
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // Enhanced Error handling with retry functionality
            error?.let { errorMessage ->
                EnhancedErrorBanner(
                    message = errorMessage,
                    onDismiss = { customerViewModel.clearError() },
                    onRetry = {
                        coroutineScope.launch {
                            customerViewModel.refreshCustomers()
                        }
                    }
                )
            }

            // Loading indicator with progress details
            if (isLoading) {
                EnhancedLoadingIndicator(
                    message = when {
                        isCreatingCustomer -> "جاري إضافة العميل..."
                        isUpdatingCustomer -> "جاري تحديث العميل..."
                        isDeletingCustomer -> "جاري حذف العميل..."
                        else -> "جاري تحميل العملاء..."
                    }
                )
            }

            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Customers List
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
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header with improved styling
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "إدارة العملاء",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${customers.size} عميل مسجل",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Add Customer Button with enhanced hover effects
                            val addCustomerInteractionSource = remember { MutableInteractionSource() }
                            val isAddCustomerHovered by addCustomerInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = if (isAddCustomerHovered)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                        else
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = addCustomerInteractionSource,
                                        indication = null
                                    ) { showAddCustomerDialog = true }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        "عميل جديد",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }

                        // Enhanced Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { customerViewModel.updateSearchQuery(it) },
                            placeholder = {
                                Text(
                                    "البحث في العملاء...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
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
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )

                        // Filters Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // City Filter
                            Text(
                                text = "المدينة:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            LazyRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(cities) { city ->
                                    ModernCityChip(
                                        city = city,
                                        isSelected = selectedCity == city,
                                        onClick = { selectedCity = city }
                                    )
                                }
                            }

                            // Sort Dropdown
                            ModernSortDropdown(
                                sortBy = sortBy,
                                onSortChange = { customerViewModel.updateSorting(it) }
                            )
                        }

                        // Customers Grid
                        if (displayCustomers.isEmpty()) {
                            EmptyCustomersState(
                                hasSearch = searchQuery.isNotEmpty() || selectedCity != "الكل",
                                isLoading = isLoading
                            )
                        } else {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalItemSpacing = 16.dp,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                items(displayCustomers) { customer ->
                                    ModernCustomerCard(
                                        customer = customer,
                                        currencyFormatter = currencyFormatter,
                                        onEdit = { editingCustomer = customer },
                                        onDelete = {
                                            customerToDelete = customer
                                            showDeleteConfirmation = true
                                        },
                                        onClick = {
                                            selectedCustomer = customer
                                            showCustomerDetails = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Panel - Customer Details and Actions
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Customer Statistics
                        Text(
                            text = "إحصائيات العملاء",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val customerStats = remember(customers) { customerViewModel.getCustomerStats() }

                        ModernStatCard(
                            title = "إجمالي العملاء",
                            value = (customerStats["totalCustomers"] as? Int)?.toString() ?: "0",
                            subtitle = "عميل مسجل",
                            icon = Icons.Default.People,
                            iconColor = MaterialTheme.colorScheme.primary
                        )

                        ModernStatCard(
                            title = "العملاء النشطين",
                            value = (customerStats["activeCustomers"] as? Int)?.toString() ?: "0",
                            subtitle = "عميل نشط",
                            icon = Icons.Default.CheckCircle,
                            iconColor = AppTheme.colors.success
                        )

                        val avgCreditLimit = customerStats["averageCreditLimit"] as? Double ?: 0.0
                        ModernStatCard(
                            title = "متوسط الحد الائتماني",
                            value = currencyFormatter.format(avgCreditLimit),
                            subtitle = "لكل عميل",
                            icon = Icons.Default.CreditCard,
                            iconColor = AppTheme.colors.info
                        )

                        ModernStatCard(
                            title = "العملاء المميزين",
                            value = (customerStats["premiumCustomers"] as? Int)?.toString() ?: "0",
                            subtitle = "عميل مميز",
                            icon = Icons.Default.Star,
                            iconColor = AppTheme.colors.warning
                        )

                        // Quick Actions
                        Text(
                            text = "إجراءات سريعة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        ModernQuickActionButton(
                            text = "إضافة عميل جديد",
                            icon = Icons.Default.PersonAdd,
                            onClick = { showAddCustomerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ModernQuickActionButton(
                            text = "تصدير قائمة العملاء",
                            icon = Icons.Default.FileDownload,
                            onClick = { /* Export functionality */ },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ModernQuickActionButton(
                            text = "استيراد عملاء",
                            icon = Icons.Default.FileUpload,
                            onClick = { /* Import functionality */ },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Top Customers by Credit Limit
                        val topCustomers = remember(customers) {
                            customerViewModel.getTopCustomersByCredit(5)
                        }

                        if (topCustomers.isNotEmpty()) {
                            Text(
                                text = "أفضل العملاء",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            LazyColumn(
                                modifier = Modifier.height(200.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(topCustomers) { customer ->
                                    ModernTopCustomerCard(
                                        customer = customer,
                                        currencyFormatter = currencyFormatter,
                                        onClick = {
                                            selectedCustomer = customer
                                            showCustomerDetails = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showAddCustomerDialog) {
            EnhancedCustomerDialog(
                customer = null,
                isLoading = isCreatingCustomer,
                onDismiss = { showAddCustomerDialog = false },
                onSave = { customer: CustomerDTO ->
                    coroutineScope.launch {
                        val result = customerViewModel.createCustomer(customer)
                        if (result.isSuccess) {
                            showAddCustomerDialog = false
                        }
                    }
                }
            )
        }

        if (editingCustomer != null) {
            EnhancedCustomerDialog(
                customer = editingCustomer!!,
                isLoading = isUpdatingCustomer,
                onDismiss = { editingCustomer = null },
                onSave = { updatedCustomer: CustomerDTO ->
                    coroutineScope.launch {
                        val result = customerViewModel.updateCustomer(updatedCustomer)
                        if (result.isSuccess) {
                            editingCustomer = null
                        }
                    }
                }
            )
        }

        if (showCustomerDetails && selectedCustomer != null) {
            CustomerDetailsDialog(
                customer = selectedCustomer!!,
                currencyFormatter = currencyFormatter,
                onDismiss = {
                    showCustomerDetails = false
                    selectedCustomer = null
                },
                onEdit = {
                    editingCustomer = selectedCustomer
                    showCustomerDetails = false
                    selectedCustomer = null
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmation && customerToDelete != null) {
            DeleteConfirmationDialog(
                customerName = customerToDelete!!.name,
                isLoading = isDeletingCustomer,
                onConfirm = {
                    coroutineScope.launch {
                        val result = customerViewModel.deleteCustomer(customerToDelete!!.id!!)
                        if (result.isSuccess) {
                            showDeleteConfirmation = false
                            customerToDelete = null
                            AppDependencies.container.notificationService.showSuccess(
                                message = "تم حذف العميل بنجاح",
                                title = "تم الحذف"
                            )
                        } else if (result.isError) {
                            val exception = (result as NetworkResult.Error).exception
                            println("🔍 Delete error type: ${exception::class.simpleName}")
                            println("🔍 Delete error message: ${exception.message}")

                            if (exception is ApiException.ForeignKeyConstraintError) {
                                println("✅ Detected foreign key constraint error")
                                println("🔍 Referenced table: ${exception.referencedTable}")
                                println("🔍 Original message: ${exception.originalMessage}")

                                foreignKeyError = exception
                                showDeleteConfirmation = false
                                showForeignKeyWarning = true
                            } else {
                                println("❌ Not a foreign key constraint error, showing generic error")
                                AppDependencies.container.notificationService.showError(
                                    message = exception.message ?: "حدث خطأ أثناء حذف العميل",
                                    title = "خطأ في الحذف"
                                )
                            }
                        }
                    }
                },
                onDismiss = {
                    showDeleteConfirmation = false
                    customerToDelete = null
                }
            )
        }

        // Foreign Key Constraint Warning Dialog
        if (showForeignKeyWarning && customerToDelete != null && foreignKeyError != null) {
            ForeignKeyWarningDialog(
                customerName = customerToDelete!!.name,
                referencedTable = foreignKeyError!!.referencedTable,
                foreignKeyError = foreignKeyError,
                onCascadeDelete = {
                    showForeignKeyWarning = false
                    showCascadeDeleteConfirmation = true
                },
                onDismiss = {
                    showForeignKeyWarning = false
                    customerToDelete = null
                    foreignKeyError = null
                }
            )
        }

        // Cascade Delete Confirmation Dialog
        if (showCascadeDeleteConfirmation && customerToDelete != null) {
            CascadeDeleteConfirmationDialog(
                customerName = customerToDelete!!.name,
                foreignKeyError = foreignKeyError,
                isLoading = isDeletingCustomer,
                onConfirm = {
                    coroutineScope.launch {
                        val result = customerViewModel.deleteCustomerWithCascade(customerToDelete!!.id!!)
                        if (result.isSuccess) {
                            showCascadeDeleteConfirmation = false
                            val deletedCustomerName = customerToDelete!!.name
                            customerToDelete = null

                            // Create specific success message based on what was deleted
                            val errorMessage = foreignKeyError?.originalMessage ?: ""
                            val countMatch = "they have (\\d+) associated".toRegex().find(errorMessage)
                            val count = countMatch?.groupValues?.get(1) ?: ""
                            val referencedTable = foreignKeyError?.referencedTable ?: ""

                            val specificMessage = when {
                                count.isNotEmpty() && referencedTable == "sales" ->
                                    "تم حذف العميل \"$deletedCustomerName\" مع $count من المبيعات المرتبطة بنجاح"
                                count.isNotEmpty() && referencedTable == "returns" ->
                                    "تم حذف العميل \"$deletedCustomerName\" مع $count من المرتجعات المرتبطة بنجاح"
                                else ->
                                    "تم حذف العميل \"$deletedCustomerName\" وجميع البيانات المرتبطة بنجاح"
                            }

                            foreignKeyError = null
                            AppDependencies.container.notificationService.showSuccess(
                                message = specificMessage,
                                title = "تم الحذف الكامل"
                            )
                        } else if (result.isError) {
                            val exception = (result as NetworkResult.Error).exception
                            AppDependencies.container.notificationService.showError(
                                message = exception.message ?: "حدث خطأ أثناء حذف العميل",
                                title = "خطأ في الحذف"
                            )
                        }
                    }
                },
                onDismiss = {
                    showCascadeDeleteConfirmation = false
                    customerToDelete = null
                    foreignKeyError = null
                }
            )
        }
    }
}

// Modern Component Functions
@Composable
private fun ModernCityChip(
    city: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 1.5.dp else if (isHovered) 1.dp else 0.5.dp,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = city,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isHovered -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernSortDropdown(
    sortBy: String,
    onSortChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val sortOptions = mapOf(
        "name" to "الاسم",
        "email" to "البريد الإلكتروني",
        "phone" to "رقم الهاتف",
        "address" to "العنوان",
        "customerType" to "نوع العميل",
        "creditLimit" to "الحد الائتماني"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .menuAnchor()
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = sortOptions[sortBy] ?: "الاسم",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                sortOptions.forEach { (key, value) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onSortChange(key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernCustomerCard(
    customer: CustomerDTO,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chartColors = AppTheme.colors.chartColors
    val avatarColor = remember(customer.id) {
        chartColors[(customer.id?.toInt() ?: 0) % chartColors.size]
    }

    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isHovered)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Avatar and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(avatarColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.firstOrNull()?.toString() ?: "C",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = avatarColor
                        )
                    }
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = customer.email ?: "لا يوجد بريد إلكتروني",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
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
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = AppTheme.colors.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Customer Details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ModernInfoRow(
                    icon = Icons.Default.CreditCard,
                    label = "الحد الائتماني",
                    value = customer.creditLimit?.let { currencyFormatter.format(it) } ?: "غير محدد",
                    valueColor = MaterialTheme.colorScheme.primary
                )
                ModernInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "العنوان",
                    value = customer.address ?: "غير محدد"
                )
                ModernInfoRow(
                    icon = Icons.Default.Phone,
                    label = "رقم الهاتف",
                    value = customer.phone ?: "غير محدد"
                )
                ModernInfoRow(
                    icon = Icons.Default.Category,
                    label = "نوع العميل",
                    value = when (customer.customerType) {
                        "PREMIUM" -> "مميز"
                        "VIP" -> "كبار الشخصيات"
                        "REGULAR" -> "عادي"
                        else -> customer.customerType ?: "عادي"
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModernStatCard(
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModernQuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isHovered)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                else
                    MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ModernTopCustomerCard(
    customer: CustomerDTO,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = when {
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = when {
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = customer.creditLimit?.let { currencyFormatter.format(it) } ?: "غير محدد",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyCustomersState(
    hasSearch: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when {
                isLoading -> "جاري تحميل العملاء..."
                hasSearch -> "لا يوجد عملاء"
                else -> "لا توجد عملاء"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when {
                isLoading -> "يرجى الانتظار..."
                hasSearch -> "لم يتم العثور على عملاء يطابقون بحثك. حاول تغيير الفلاتر."
                else -> "ابدأ بإضافة عملاء جدد لإدارة قاعدة عملائك"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

// Enhanced Dialog Components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedCustomerDialog(
    customer: CustomerDTO?,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (CustomerDTO) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var firstName by remember { mutableStateOf(customer?.firstName ?: "") }
    var lastName by remember { mutableStateOf(customer?.lastName ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var billingAddress by remember { mutableStateOf(customer?.billingAddress ?: "") }
    var shippingAddress by remember { mutableStateOf(customer?.shippingAddress ?: "") }
    var customerType by remember { mutableStateOf(customer?.customerType ?: "REGULAR") }
    var customerStatus by remember { mutableStateOf(customer?.customerStatus ?: "ACTIVE") }
    var creditLimit by remember { mutableStateOf(customer?.creditLimit?.toString() ?: "") }
    var taxNumber by remember { mutableStateOf(customer?.taxNumber ?: "") }
    var companyName by remember { mutableStateOf(customer?.companyName ?: "") }
    var website by remember { mutableStateOf(customer?.website ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }

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
                        text = if (customer == null) "إضافة عميل جديد" else "تعديل العميل",
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
                            label = { Text("اسم العميل *") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
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
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("الاسم الأول") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("اسم العائلة") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الهاتف") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("البريد الإلكتروني") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // Address Information Section
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
                            text = "معلومات العنوان",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("العنوان") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = billingAddress,
                            onValueChange = { billingAddress = it },
                            label = { Text("عنوان الفواتير") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = shippingAddress,
                            onValueChange = { shippingAddress = it },
                            label = { Text("عنوان الشحن") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // Additional Information Section
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
                            text = "معلومات إضافية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Customer Type Dropdown
                            var customerTypeExpanded by remember { mutableStateOf(false) }
                            val customerTypes = mapOf(
                                "REGULAR" to "عادي",
                                "PREMIUM" to "مميز",
                                "VIP" to "كبار الشخصيات"
                            )

                            ExposedDropdownMenuBox(
                                expanded = customerTypeExpanded,
                                onExpandedChange = { customerTypeExpanded = !customerTypeExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = customerTypes[customerType] ?: "عادي",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("نوع العميل") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerTypeExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isLoading,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = customerTypeExpanded,
                                    onDismissRequest = { customerTypeExpanded = false }
                                ) {
                                    customerTypes.forEach { (key, value) ->
                                        DropdownMenuItem(
                                            text = { Text(value) },
                                            onClick = {
                                                customerType = key
                                                customerTypeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Customer Status Dropdown
                            var customerStatusExpanded by remember { mutableStateOf(false) }
                            val customerStatuses = mapOf(
                                "ACTIVE" to "نشط",
                                "INACTIVE" to "غير نشط",
                                "SUSPENDED" to "معلق"
                            )

                            ExposedDropdownMenuBox(
                                expanded = customerStatusExpanded,
                                onExpandedChange = { customerStatusExpanded = !customerStatusExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = customerStatuses[customerStatus] ?: "نشط",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("حالة العميل") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerStatusExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isLoading,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = customerStatusExpanded,
                                    onDismissRequest = { customerStatusExpanded = false }
                                ) {
                                    customerStatuses.forEach { (key, value) ->
                                        DropdownMenuItem(
                                            text = { Text(value) },
                                            onClick = {
                                                customerStatus = key
                                                customerStatusExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = creditLimit,
                            onValueChange = { creditLimit = it },
                            label = { Text("الحد الائتماني") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("اسم الشركة") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
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
                val isValid = name.isNotBlank()

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
                                val newCustomer = CustomerDTO(
                                    id = customer?.id,
                                    name = name,
                                    firstName = firstName.takeIf { it.isNotBlank() },
                                    lastName = lastName.takeIf { it.isNotBlank() },
                                    phone = phone.takeIf { it.isNotBlank() },
                                    email = email.takeIf { it.isNotBlank() },
                                    address = address.takeIf { it.isNotBlank() },
                                    billingAddress = billingAddress.takeIf { it.isNotBlank() },
                                    shippingAddress = shippingAddress.takeIf { it.isNotBlank() },
                                    customerType = customerType,
                                    customerStatus = customerStatus,
                                    creditLimit = creditLimit.toDoubleOrNull(),
                                    taxNumber = taxNumber.takeIf { it.isNotBlank() },
                                    companyName = companyName.takeIf { it.isNotBlank() },
                                    website = website.takeIf { it.isNotBlank() },
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                                onSave(newCustomer)
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
                            text = if (customer != null) "تحديث" else "إضافة",
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

@Composable
private fun CustomerDetailsDialog(
    customer: CustomerDTO,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val chartColors = AppTheme.colors.chartColors
    val avatarColor = remember(customer.id) {
        chartColors[(customer.id?.toInt() ?: 0) % chartColors.size]
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تفاصيل العميل",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                // Customer Avatar and Basic Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(avatarColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.firstOrNull()?.toString() ?: "C",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = avatarColor
                        )
                    }
                    Column {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = customer.email ?: "لا يوجد بريد إلكتروني",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Details
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomerDetailRow("الاسم الكامل", customer.name, Icons.Default.Person)
                    customer.email?.let {
                        CustomerDetailRow("البريد الإلكتروني", it, Icons.Default.Email)
                    }
                    customer.phone?.let {
                        CustomerDetailRow("رقم الهاتف", it, Icons.Default.Phone)
                    }
                    customer.address?.let {
                        CustomerDetailRow("العنوان", it, Icons.Default.LocationOn)
                    }
                    CustomerDetailRow(
                        "نوع العميل",
                        when (customer.customerType) {
                            "PREMIUM" -> "مميز"
                            "VIP" -> "كبار الشخصيات"
                            "REGULAR" -> "عادي"
                            else -> customer.customerType ?: "عادي"
                        },
                        Icons.Default.Category
                    )
                    CustomerDetailRow(
                        "حالة العميل",
                        when (customer.customerStatus) {
                            "ACTIVE" -> "نشط"
                            "INACTIVE" -> "غير نشط"
                            "SUSPENDED" -> "معلق"
                            else -> customer.customerStatus ?: "نشط"
                        },
                        Icons.Default.CheckCircle
                    )
                    customer.creditLimit?.let {
                        CustomerDetailRow(
                            "الحد الائتماني",
                            currencyFormatter.format(it),
                            Icons.Default.CreditCard,
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    customer.companyName?.let {
                        CustomerDetailRow("اسم الشركة", it, Icons.Default.Business)
                    }
                    customer.notes?.let {
                        CustomerDetailRow("ملاحظات", it, Icons.Default.Notes)
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إغلاق")
                    }

                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تعديل")
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerDetailRow(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
}

// Enhanced Error Banner Component
@Composable
private fun EnhancedErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.error.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, AppTheme.colors.error.copy(alpha = 0.3f)),
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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = AppTheme.colors.error,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "حدث خطأ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.error
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppTheme.colors.error
                    )
                ) {
                    Text("إعادة المحاولة")
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Enhanced Loading Indicator Component
@Composable
private fun EnhancedLoadingIndicator(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Foreign Key Warning Dialog Component
@Composable
private fun ForeignKeyWarningDialog(
    customerName: String,
    referencedTable: String,
    foreignKeyError: ApiException.ForeignKeyConstraintError? = null,
    onCascadeDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppTheme.colors.warning,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "تحذير: لا يمكن حذف العميل",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Extract count from error message first
                val errorMessage = foreignKeyError?.originalMessage ?: ""
                println("🔍 Parsing error message: $errorMessage")

                // Try multiple patterns to extract the count
                val countPatterns = listOf(
                    "they have (\\d+) associated".toRegex(),
                    "because they have (\\d+)".toRegex(),
                    "(\\d+) associated".toRegex()
                )

                var count = "عدة"
                for (pattern in countPatterns) {
                    val match = pattern.find(errorMessage)
                    if (match != null) {
                        count = match.groupValues[1]
                        println("✅ Extracted count: $count")
                        break
                    }
                }

                if (count == "عدة") {
                    println("⚠️ Could not extract specific count, using default")
                }

                val tableDisplayName = when (referencedTable) {
                    "returns" -> "المرتجعات"
                    "sales" -> "المبيعات"
                    else -> "البيانات المرتبطة"
                }

                // Create a more specific and professional error message
                val specificMessage = if (count != "عدة" && referencedTable == "sales") {
                    "لا يمكن حذف العميل \"$customerName\" لأنه مرتبط بـ $count من المبيعات في النظام."
                } else if (count != "عدة" && referencedTable == "returns") {
                    "لا يمكن حذف العميل \"$customerName\" لأنه مرتبط بـ $count من المرتجعات في النظام."
                } else {
                    "لا يمكن حذف العميل \"$customerName\" لأنه مرتبط ببيانات أخرى في النظام."
                }

                Text(
                    text = specificMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.warning.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "البيانات المرتبطة:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.warning
                        )
                        Text(
                            text = "• $count من $tableDisplayName",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        // Show backend suggestion if available
                        if (referencedTable == "sales") {
                            Text(
                                text = "💡 اقتراح النظام:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.primary
                            )
                            Text(
                                text = "يجب إكمال أو إلغاء أو إعادة تعيين جميع مبيعات العميل قبل حذفه.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = "الخيارات المتاحة:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "• إلغاء العملية والاحتفاظ بالعميل",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• حذف العميل مع جميع البيانات المرتبطة (غير قابل للتراجع)",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.error
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val interactionSource1 = remember { MutableInteractionSource() }
                val isHovered1 by interactionSource1.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            color = if (isHovered1) AppTheme.colors.error.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            BorderStroke(1.dp, AppTheme.colors.error.copy(alpha = 0.2f)),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource1,
                            indication = null
                        ) { onCascadeDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "حذف مع البيانات المرتبطة",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.error
                    )
                }

                val interactionSource2 = remember { MutableInteractionSource() }
                val isHovered2 by interactionSource2.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            color = if (isHovered2) MaterialTheme.colorScheme.outline.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource2,
                            indication = null
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// Cascade Delete Confirmation Dialog Component
@Composable
private fun CascadeDeleteConfirmationDialog(
    customerName: String,
    foreignKeyError: ApiException.ForeignKeyConstraintError? = null,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (!isLoading) onDismiss else {{}},
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = AppTheme.colors.error,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "تأكيد الحذف الكامل",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.error
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "هل أنت متأكد من حذف العميل \"$customerName\" مع جميع البيانات المرتبطة؟",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.error.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⚠️ تحذير: سيتم حذف:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.error
                        )
                        Text(
                            text = "• بيانات العميل الأساسية",
                            style = MaterialTheme.typography.bodySmall
                        )

                        // Show specific information based on the error
                        val errorMessage = foreignKeyError?.originalMessage ?: ""
                        val countMatch = "they have (\\d+) associated".toRegex().find(errorMessage)
                        val count = countMatch?.groupValues?.get(1) ?: "جميع"

                        when (foreignKeyError?.referencedTable) {
                            "sales" -> {
                                Text(
                                    text = "• $count من المبيعات المرتبطة",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = AppTheme.colors.error
                                )
                            }
                            "returns" -> {
                                Text(
                                    text = "• $count من المرتجعات المرتبطة",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = AppTheme.colors.error
                                )
                            }
                            else -> {
                                Text(
                                    text = "• جميع المبيعات المرتبطة",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "• جميع المرتجعات المرتبطة",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "هذا الإجراء لا يمكن التراجع عنه!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.error
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val interactionSource1 = remember { MutableInteractionSource() }
                val isHovered1 by interactionSource1.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            color = if (isHovered1) AppTheme.colors.error.copy(alpha = 0.1f) else AppTheme.colors.error,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource1,
                            indication = null,
                            enabled = !isLoading
                        ) { onConfirm() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "حذف نهائي",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                val interactionSource2 = remember { MutableInteractionSource() }
                val isHovered2 by interactionSource2.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            color = if (isHovered2) MaterialTheme.colorScheme.outline.copy(alpha = 0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource2,
                            indication = null,
                            enabled = !isLoading
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إلغاء",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

// Delete Confirmation Dialog Component
@Composable
private fun DeleteConfirmationDialog(
    customerName: String,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (!isLoading) onDismiss else {{}},
        title = {
            Text(
                text = "تأكيد الحذف",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "هل أنت متأكد من حذف العميل \"$customerName\"؟ لا يمكن التراجع عن هذا الإجراء.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.error
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("حذف", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}