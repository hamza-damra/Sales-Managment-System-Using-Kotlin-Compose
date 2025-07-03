package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import data.*
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(salesDataManager: SalesDataManager) {
    RTLProvider {
        var searchQuery by remember { mutableStateOf("") }
        var showAddCustomerDialog by remember { mutableStateOf(false) }
        var editingCustomer by remember { mutableStateOf<Customer?>(null) }
        var sortBy by remember { mutableStateOf("name") }

        val currencyFormatter = remember {
            NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
                currency = Currency.getInstance("SAR")
            }
        }

        val filteredCustomers = remember(searchQuery, sortBy, salesDataManager.customers) {
            var customers = if (searchQuery.isNotEmpty()) {
                salesDataManager.searchCustomers(searchQuery)
            } else {
                salesDataManager.customers
            }

            when (sortBy) {
                "name" -> customers.sortedBy { it.name }
                "purchases" -> customers.sortedByDescending { it.totalPurchases }
                "city" -> customers.sortedBy { it.address }
                else -> customers
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with actions
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                SectionHeader(
                    title = "إدارة العملاء",
                    subtitle = "${salesDataManager.customers.size} عميل إجمالي"
                )

                QuickActionButton(
                    text = "إضافة عميل",
                    icon = Icons.Default.PersonAdd,
                    onClick = { showAddCustomerDialog = true },
                    modifier = Modifier.width(160.dp)
                )
            }

            // Search and Sort
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "البحث في العملاء...",
                    modifier = Modifier.weight(1f)
                )

                // Sort Filter
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        var expanded by remember { mutableStateOf(false) }
                        val sortOptions = mapOf(
                            "name" to "الاسم",
                            "purchases" to "المشتريات",
                            "city" to "المدينة"
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            TextField(
                                value = sortOptions[sortBy] ?: "الاسم",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .wrapContentWidth()
                                    .widthIn(min = 100.dp, max = 180.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )

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
                                            sortBy = key
                                            expanded = false
                                        },
                                        modifier = Modifier.wrapContentWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Customer Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "إجمالي العملاء",
                    value = salesDataManager.customers.size.toString(),
                    subtitle = "عميل مسجل",
                    icon = Icons.Default.People,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                val totalPurchases = salesDataManager.customers.sumOf { it.totalPurchases }
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "إجمالي المشتريات",
                    value = currencyFormatter.format(totalPurchases),
                    subtitle = "من جميع العملاء",
                    icon = Icons.Default.AttachMoney,
                    iconColor = AppTheme.colors.success
                )

                val avgPurchases = if (salesDataManager.customers.isNotEmpty()) {
                    salesDataManager.customers.sumOf { it.totalPurchases } / salesDataManager.customers.size
                } else 0.0
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "متوسط المشتريات",
                    value = currencyFormatter.format(avgPurchases),
                    subtitle = "لكل عميل",
                    icon = Icons.Default.TrendingUp,
                    iconColor = AppTheme.colors.info
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customers List
            if (filteredCustomers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.People,
                    title = "لا يوجد عملاء",
                    description = "لم يتم العثور على عملاء يطابقون بحثك. حاول تغيير الفلاتر أو إضافة عميل جديد.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 320.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalItemSpacing = 16.dp
                ) {
                    items(filteredCustomers) { customer ->
                        CustomerCard(
                            customer = customer,
                            currencyFormatter = currencyFormatter,
                            onEdit = { editingCustomer = customer },
                            onDelete = { /* TODO: Implement delete logic */ }
                        )
                    }
                }
            }
        }

        // Add/Edit Customer Dialog
        if (showAddCustomerDialog || editingCustomer != null) {
            CustomerDialog(
                customer = editingCustomer,
                onDismiss = {
                    showAddCustomerDialog = false
                    editingCustomer = null
                },
                onSave = { customer ->
                    if (editingCustomer != null) {
                        // Update existing customer
                        val index = salesDataManager.customers.indexOfFirst { it.id == customer.id }
                        if (index != -1) {
                            salesDataManager.customers[index] = customer
                        }
                    } else {
                        salesDataManager.addCustomer(customer)
                    }
                    showAddCustomerDialog = false
                    editingCustomer = null
                }
            )
        }
    }
}

@Composable
fun CustomerCard(
    customer: Customer,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chartColors = AppTheme.colors.chartColors
    val avatarColor = remember(customer.id) {
        chartColors[customer.id % chartColors.size]
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(avatarColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.firstOrNull()?.toString() ?: "C",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = avatarColor
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = customer.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Customer Details
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(
                    icon = Icons.Default.ShoppingCart,
                    label = "إجمالي المشتريات",
                    value = currencyFormatter.format(customer.totalPurchases),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                InfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "العنوان",
                    value = customer.address
                )
                InfoRow(
                    icon = Icons.Default.Phone,
                    label = "رقم الهاتف",
                    value = customer.phone
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تعديل")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.error.copy(alpha = 0.1f),
                        contentColor = AppTheme.colors.error
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp,
                        hoveredElevation = 1.dp,
                        focusedElevation = 1.dp
                    ),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun CustomerDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(500.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (customer != null) "تعديل عميل" else "إضافة عميل جديد",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null)
                    }
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("المدينة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    val isValid = name.isNotBlank() && phone.isNotBlank() &&
                            email.isNotBlank() && address.isNotBlank()

                    Button(
                        onClick = {
                            if (isValid) {
                                val newCustomer = Customer(
                                    id = customer?.id ?: (System.currentTimeMillis().toInt()),
                                    name = name,
                                    phone = phone,
                                    email = email,
                                    address = address,
                                    totalPurchases = customer?.totalPurchases ?: 0.0
                                )
                                onSave(newCustomer)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isValid
                    ) {
                        Text(if (customer != null) "تحديث" else "إضافة")
                    }
                }
            }
        }
    }
}
