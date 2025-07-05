package ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import data.*
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.utils.ResponsiveUtils

@Composable
fun SuppliersScreen(salesDataManager: SalesDataManager) {
    RTLProvider {
        var selectedTab by remember { mutableStateOf(SupplierTab.SUPPLIERS) }
        var searchQuery by remember { mutableStateOf("") }
        var showAddSupplierDialog by remember { mutableStateOf(false) }
        var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }

        val responsive = ResponsiveUtils.getResponsiveSpacing()
        val responsivePadding = ResponsiveUtils.getResponsivePadding()
        val responsiveCorners = ResponsiveUtils.getResponsiveCornerRadius()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(responsivePadding.screen),
                verticalArrangement = Arrangement.spacedBy(responsive.large)
            ) {
                // Enhanced Header Section
                item {
                    EnhancedSuppliersHeader(
                        onAddSupplier = { showAddSupplierDialog = true }
                    )
                }

                // Search and Filters Section
                item {
                    EnhancedSearchAndFilters(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it }
                    )
                }

                // Enhanced Statistics Dashboard
                item {
                    EnhancedSupplierStatistics()
                }

                // Enhanced Suppliers List
                item {
                    EnhancedSuppliersList()
                }
            }
        }

        // Add supplier dialog
        if (showAddSupplierDialog) {
            EnhancedAddSupplierDialog(
                onDismiss = { showAddSupplierDialog = false },
                onSave = { supplier ->
                    // Add supplier logic
                    showAddSupplierDialog = false
                }
            )
        }
    }
}

// Enhanced Component Functions
@Composable
private fun EnhancedSuppliersHeader() {
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsiveCorners = ResponsiveUtils.getResponsiveCornerRadius()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(responsiveCorners.large),
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
                    .height(120.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

            RTLRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(responsive.large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(responsive.small)
                ) {
                    Text(
                        text = "إدارة الموردين",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "تنظيم معلومات الموردين وطلبات الشراء",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedSuppliersHeader(
    onAddSupplier: () -> Unit
) {
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsiveCorners = ResponsiveUtils.getResponsiveCornerRadius()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(responsiveCorners.large),
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
                    .height(120.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

            RTLRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(responsive.large),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(responsive.small)
                ) {
                    Text(
                        text = "إدارة الموردين",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "تنظيم معلومات الموردين وطلبات الشراء",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAddSupplier,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إضافة مورد",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedSearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsiveCorners = ResponsiveUtils.getResponsiveCornerRadius()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(responsiveCorners.large),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        RTLRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(responsive.medium),
            horizontalArrangement = Arrangement.spacedBy(responsive.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "البحث في الموردين...",
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = true,
                onClick = { /* Filter active suppliers */ },
                label = { Text("موردين نشطين") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppTheme.colors.success.copy(alpha = 0.2f),
                    selectedLabelColor = AppTheme.colors.success
                )
            )

            FilterChip(
                selected = false,
                onClick = { /* Filter by orders */ },
                label = { Text("لديهم طلبات") }
            )
        }
    }
}

@Composable
private fun EnhancedSupplierStatistics() {
    val responsive = ResponsiveUtils.getResponsiveSpacing()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(responsive.medium),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            ModernStatCard(
                title = "إجمالي الموردين",
                value = "25",
                subtitle = "مورد مسجل",
                icon = Icons.Default.Business,
                iconColor = MaterialTheme.colorScheme.primary,
                trend = "+3 هذا الشهر"
            )
        }
        item {
            ModernStatCard(
                title = "موردين نشطين",
                value = "23",
                subtitle = "مورد نشط",
                icon = Icons.Default.Verified,
                iconColor = AppTheme.colors.success,
                trend = "+92%"
            )
        }
        item {
            ModernStatCard(
                title = "طلبات الشراء",
                value = "12",
                subtitle = "طلب هذا الشهر",
                icon = Icons.Default.ShoppingCart,
                iconColor = AppTheme.colors.warning,
                trend = "+25%"
            )
        }
        item {
            ModernStatCard(
                title = "قيمة المشتريات",
                value = "45,680 ر.س",
                subtitle = "إجمالي الشهر",
                icon = Icons.Default.AttachMoney,
                iconColor = AppTheme.colors.info,
                trend = "+18.5%"
            )
        }
        item {
            ModernStatCard(
                title = "متوسط قيمة الطلب",
                value = "3,807 ر.س",
                subtitle = "متوسط شهري",
                icon = Icons.Default.Analytics,
                iconColor = AppTheme.colors.purple,
                trend = "+12.3%"
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
                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Trend indicator
                Surface(
                    color = AppTheme.colors.success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = trend,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.success,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedSuppliersList() {
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsiveCorners = ResponsiveUtils.getResponsiveCornerRadius()

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(responsiveCorners.large),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(responsive.large),
            verticalArrangement = Arrangement.spacedBy(responsive.medium)
        ) {
            RTLRow(
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

                TextButton(
                    onClick = { /* View all suppliers */ }
                ) {
                    Text("عرض الكل")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(responsive.small)
            ) {
                repeat(10) { index ->
                    EnhancedSupplierCard(
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

@Composable
private fun EnhancedSupplierCard(
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
            .clickable { /* Handle click */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
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
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.01f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

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
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status indicator
                        Surface(
                            color = if (status == "نشط") AppTheme.colors.success.copy(alpha = 0.1f)
                                   else AppTheme.colors.warning.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (status == "نشط") AppTheme.colors.success else AppTheme.colors.warning,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

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
}

@Composable
private fun EnhancedAddSupplierDialog(
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
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "حفظ",
                    fontWeight = FontWeight.Medium
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
    val address: String
)

enum class SupplierTab(val title: String) {
    SUPPLIERS("الموردين"),
    ORDERS("طلبات الشراء"),
    ANALYTICS("التحليلات والتقارير")
}
