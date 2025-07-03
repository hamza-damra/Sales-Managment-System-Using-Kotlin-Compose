package ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.*
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles

@Composable
fun SuppliersScreen(salesDataManager: SalesDataManager) {
    RTLProvider {
        var selectedTab by remember { mutableStateOf(SupplierTab.SUPPLIERS) }
        var searchQuery by remember { mutableStateOf("") }
        var showAddSupplierDialog by remember { mutableStateOf(false) }
        var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with RTL support
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "إدارة الموردين",
                    subtitle = "تنظيم معلومات الموردين وطلبات الشراء"
                )

                QuickActionButton(
                    text = "إضافة مورد",
                    icon = Icons.Default.Add,
                    onClick = { showAddSupplierDialog = true },
                    modifier = Modifier.width(160.dp)
                )
            }

            // Search and filters with RTL layout
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "البحث في الموردين...",
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = true,
                    onClick = { /* Filter active suppliers */ },
                    label = { Text("موردين نشطين") }
                )
            }

            // Suppliers statistics with RTL layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "إجمالي الموردين",
                    value = "25",
                    subtitle = "مورد مسجل",
                    icon = Icons.Default.Business,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "موردين نشطين",
                    value = "23",
                    subtitle = "مورد نشط",
                    icon = Icons.Default.Verified,
                    iconColor = AppTheme.colors.success,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "طلبات الشراء",
                    value = "12",
                    subtitle = "طلب هذا الشهر",
                    icon = Icons.Default.ShoppingCart,
                    iconColor = AppTheme.colors.warning,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "قيمة المشتريات",
                    value = "45,680 ر.س",
                    subtitle = "إجمالي الشهر",
                    icon = Icons.Default.AttachMoney,
                    iconColor = AppTheme.colors.info,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "متوسط قيمة الطلب",
                    value = "3,807 ر.س",
                    subtitle = "متوسط شهري",
                    icon = Icons.Default.Analytics,
                    iconColor = AppTheme.colors.purple,
                    modifier = Modifier.weight(1f)
                )
            }

            // Suppliers list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardStyles.defaultCardColors(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardStyles.defaultCardElevation()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "قائمة الموردين",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(10) { index ->
                            SupplierCard(
                                supplierName = "شركة المورد ${index + 1}",
                                contactPerson = "أحمد محمد",
                                phone = "+966-50-123-45${index + 10}",
                                email = "supplier${index + 1}@company.com",
                                totalOrders = (5 + index * 2),
                                totalAmount = (10000 + index * 5000).toDouble(),
                                status = if (index % 3 == 0) "معلق" else "نشط"
                            )
                        }
                    }
                }
            }
        }

        // Add supplier dialog
        if (showAddSupplierDialog) {
            AddSupplierDialog(
                onDismiss = { showAddSupplierDialog = false },
                onSave = { supplier ->
                    // Add supplier logic
                    showAddSupplierDialog = false
                }
            )
        }
    }
}

@Composable
private fun SupplierCard(
    supplierName: String,
    contactPerson: String,
    phone: String,
    email: String,
    totalOrders: Int,
    totalAmount: Double,
    status: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(vertical = 4.dp),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RTLRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(
                        text = status,
                        color = if (status == "نشط") AppTheme.colors.success else AppTheme.colors.warning
                    )

                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = supplierName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "الشخص المسؤول: $contactPerson",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { /* View details */ }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "عرض التفاصيل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RTLRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${totalAmount.toInt()} ر.س",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$totalOrders طلب",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { /* Edit supplier */ }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onSave: (SupplierData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مورد جديد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الشركة") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("الشخص المسؤول") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("العنوان") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
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
                        address = address
                    )
                    onSave(supplier)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

data class SupplierData(
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String
)

enum class SupplierTab(val title: String) {
    SUPPLIERS("الموردين"),
    ORDERS("طلبات الشراء"),
    ANALYTICS("التحليلات والتقارير")
}
