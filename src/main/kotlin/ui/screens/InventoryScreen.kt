package ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import UiUtils
import ui.components.*
import ui.theme.CardStyles

@Composable
fun InventoryScreen(salesDataManager: SalesDataManager) {
    RTLProvider {
        var selectedTab by remember { mutableStateOf(InventoryTab.OVERVIEW) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("الكل") }
        var selectedWarehouse by remember { mutableStateOf("الكل") }
        var showLowStockOnly by remember { mutableStateOf(false) }
        var showExpiringOnly by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* إضافة منتج جديد */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    RTLSpacer(8.dp)
                    Text("إضافة منتج")
                }

                Text(
                    text = "إدارة المخزون",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Alerts Cards
            InventoryAlertsCards()

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                InventoryTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.title) }
                    )
                }
            }

            // Filters Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "البحث في المنتجات...",
                    modifier = Modifier.weight(1f)
                )

                FilterDropdown(
                    label = "الفئة",
                    value = selectedCategory,
                    options = listOf("الكل", "إلكترونيات", "ملابس", "مواد غذائية"),
                    onValueChange = { selectedCategory = it }
                )

                FilterDropdown(
                    label = "المستودع",
                    value = selectedWarehouse,
                    options = listOf("الكل", "المستودع الرئيسي", "المستودع الفرعي"),
                    onValueChange = { selectedWarehouse = it }
                )
            }

            // Quick Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = showLowStockOnly,
                    onClick = { showLowStockOnly = !showLowStockOnly },
                    label = { Text("مخزون منخفض") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                FilterChip(
                    selected = showExpiringOnly,
                    onClick = { showExpiringOnly = !showExpiringOnly },
                    label = { Text("قارب على الانتهاء") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                InventoryTab.OVERVIEW -> InventoryOverviewContent(salesDataManager)
                InventoryTab.PRODUCTS -> InventoryProductsContent(searchQuery, selectedCategory, selectedWarehouse, showLowStockOnly)
                InventoryTab.MOVEMENTS -> StockMovementsContent()
                InventoryTab.WAREHOUSES -> WarehousesContent()
            }
        }
    }
}

@Composable
fun InventoryAlertsCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AlertCard(
            title = "مخزون منخفض",
            count = 12,
            icon = Icons.Default.Warning,
            color = AppColors.Warning,
            modifier = Modifier.weight(1f)
        )

        AlertCard(
            title = "نفاد المخزون",
            count = 3,
            icon = Icons.Default.Error,
            color = AppColors.Error,
            modifier = Modifier.weight(1f)
        )

        AlertCard(
            title = "قارب على الانتهاء",
            count = 8,
            icon = Icons.Default.Schedule,
            color = AppColors.Info,
            modifier = Modifier.weight(1f)
        )

        AlertCard(
            title = "منتهي الصلاحية",
            count = 2,
            icon = Icons.Default.Block,
            color = AppColors.Error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AlertCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun InventoryOverviewContent(salesDataManager: SalesDataManager) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Inventory Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    title = "إجمالي قيمة المخزون",
                    value = UiUtils.formatCurrency(125000.0),
                    icon = Icons.Default.AccountBalance,
                    color = AppColors.Primary,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "عدد المنتجات",
                    value = "1,234",
                    icon = Icons.Default.Inventory,
                    color = AppColors.Secondary,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "معدل دوران المخزون",
                    value = "4.2x",
                    icon = Icons.Default.TrendingUp,
                    color = AppColors.Success,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Low Stock Products
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { /* عرض الكل */ }) {
                            Text("عرض الكل")
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }

                        Text(
                            text = "المنتجات ذات المخزون المنخفض",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    repeat(5) { index ->
                        LowStockProductItem(
                            productName = "منتج ${index + 1}",
                            currentStock = (index + 1) * 2,
                            minimumStock = 10,
                            category = "إلكترونيات"
                        )
                        if (index < 4) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        item {
            // Expiring Products
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { /* عرض الكل */ }) {
                            Text("عرض الكل")
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }

                        Text(
                            text = "المنتجات القاربة على الانتهاء",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    repeat(3) { index ->
                        ExpiringProductItem(
                            productName = "منتج صالح ${index + 1}",
                            expiryDate = LocalDate(2024, 12, 15 + index),
                            daysRemaining = 15 - index * 5,
                            quantity = (index + 1) * 50
                        )
                        if (index < 2) {
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LowStockProductItem(
    productName: String,
    currentStock: Int,
    minimumStock: Int,
    category: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        RTLRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RTLRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { /* إعادة طلب */ },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("إعادة طلب", style = MaterialTheme.typography.bodySmall)
                }

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "$currentStock / $minimumStock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Error,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "المخزون المتبقي",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ExpiringProductItem(
    productName: String,
    expiryDate: LocalDate,
    daysRemaining: Int,
    quantity: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val urgencyColor = when {
                daysRemaining <= 5 -> AppColors.Error
                daysRemaining <= 15 -> AppColors.Warning
                else -> AppColors.Info
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = urgencyColor.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${daysRemaining} يوم",
                    style = MaterialTheme.typography.bodySmall,
                    color = urgencyColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Column {
                Text(
                    text = "$quantity قطعة",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = expiryDate.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnSurfaceVariant
                )
            }
        }

        Text(
            text = productName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InventoryProductsContent(
    searchQuery: String,
    selectedCategory: String,
    selectedWarehouse: String,
    showLowStockOnly: Boolean
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sample products data
        items(20) { index ->
            ProductInventoryCard(
                product = Product(
                    id = index,
                    name = "منتج $index",
                    barcode = "123456789$index",
                    price = 100.0 + index * 10,
                    cost = 80.0 + index * 8,
                    stock = if (index % 5 == 0) 5 else 50 + index,
                    category = if (index % 3 == 0) "إلكترونيات" else "ملابس"
                ),
                inventoryItem = InventoryItem(
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
            )
        }
    }
}

@Composable
fun ProductInventoryCard(
    product: Product,
    inventoryItem: InventoryItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { /* تعديل */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = AppColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { /* عرض التفاصيل */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "عرض",
                            tint = AppColors.Info,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Product Info
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.OnSurfaceVariant
                    )
                    Text(
                        text = "الباركود: ${product.barcode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StockInfoItem(
                    label = "الحد الأقصى",
                    value = inventoryItem.maximumStock.toString(),
                    color = AppColors.Info
                )

                StockInfoItem(
                    label = "نقطة الطلب",
                    value = inventoryItem.reorderPoint.toString(),
                    color = AppColors.Warning
                )

                StockInfoItem(
                    label = "الحد الأدنى",
                    value = inventoryItem.minimumStock.toString(),
                    color = AppColors.Error
                )

                StockInfoItem(
                    label = "المحجوز",
                    value = inventoryItem.reservedStock.toString(),
                    color = AppColors.Purple
                )

                StockInfoItem(
                    label = "المتوفر",
                    value = inventoryItem.currentStock.toString(),
                    color = UiUtils.getStockStatusColor(inventoryItem.currentStock)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "هامش الربح: ${UiUtils.formatPercentage(((product.price - product.cost) / product.cost) * 100)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Success
                )

                Text(
                    text = "السعر: ${UiUtils.formatCurrency(product.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Expiry Date if applicable
            inventoryItem.expiryDate?.let { expiryDate ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Warning.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "تاريخ الانتهاء: $expiryDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Warning
                        )
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AppColors.Warning,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StockInfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.OnSurfaceVariant
        )
    }
}

@Composable
fun StockMovementsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(15) { index ->
            StockMovementCard(
                movement = StockMovement(
                    id = index,
                    productId = index,
                    warehouseId = 1,
                    movementType = MovementType.values()[index % MovementType.values().size],
                    quantity = (index + 1) * 5,
                    date = LocalDateTime(2024, 1, 1 + index, 10, 0),
                    reference = "REF-${1000 + index}",
                    notes = "ملاحظة حول الحركة رقم $index"
                ),
                productName = "منتج $index"
            )
        }
    }
}

@Composable
fun StockMovementCard(
    movement: StockMovement,
    productName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = movement.reference,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.OnSurfaceVariant
                )
                Text(
                    text = movement.date.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnSurfaceVariant
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = getMovementTypeColor(movement.movementType).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${if (movement.movementType == MovementType.SALE) "-" else "+"}${movement.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = getMovementTypeColor(movement.movementType),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = movement.movementType.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = getMovementTypeColor(movement.movementType)
                )
            }
        }
    }
}

@Composable
fun getMovementTypeColor(movementType: MovementType): Color {
    return when (movementType) {
        MovementType.PURCHASE -> AppColors.Success
        MovementType.SALE -> AppColors.Info
        MovementType.RETURN -> AppColors.Warning
        MovementType.ADJUSTMENT -> AppColors.Purple
        MovementType.TRANSFER -> AppColors.Teal
        MovementType.DAMAGED -> AppColors.Error
        MovementType.EXPIRED -> AppColors.Error
    }
}

@Composable
fun WarehousesContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(3) { index ->
            WarehouseCard(
                warehouse = Warehouse(
                    id = index,
                    name = "المستودع ${if (index == 0) "الرئيسي" else "الفرعي $index"}",
                    location = "الرياض - حي النخيل",
                    manager = "أحمد محمد"
                ),
                totalProducts = 500 + index * 100,
                totalValue = 125000.0 + index * 50000
            )
        }
    }
}

@Composable
fun WarehouseCard(
    warehouse: Warehouse,
    totalProducts: Int,
    totalValue: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = { /* إدارة المستودع */ }
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "إدارة",
                        tint = AppColors.Primary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = warehouse.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = warehouse.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.OnSurfaceVariant
                    )
                    Text(
                        text = "المدير: ${warehouse.manager}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = UiUtils.formatCurrency(totalValue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                    Text(
                        text = "إجمالي القيمة",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalProducts.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Secondary
                    )
                    Text(
                        text = "عدد المنتجات",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor()
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

enum class InventoryTab(val title: String) {
    OVERVIEW("نظرة عامة"),
    PRODUCTS("المنتجات"),
    MOVEMENTS("حركة المخزون"),
    WAREHOUSES("المستودعات")
}
