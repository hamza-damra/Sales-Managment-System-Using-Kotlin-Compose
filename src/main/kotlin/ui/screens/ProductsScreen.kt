package ui.screens

import AppColors  // use compatibility alias for static color references
import androidx.compose.foundation.background
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
import androidx.compose.ui.window.Dialog
import data.*
import ui.components.*
import ui.theme.CardStyles
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(salesDataManager: SalesDataManager) {
    RTLProvider {
        var searchQuery by remember { mutableStateOf("") }
        var showAddProductDialog by remember { mutableStateOf(false) }
        var editingProduct by remember { mutableStateOf<Product?>(null) }
        var selectedCategory by remember { mutableStateOf("الكل") }

        val currencyFormatter = remember {
            NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
                currency = Currency.getInstance("SAR")
            }
        }

        val categories = remember(salesDataManager.products) {
            listOf("الكل") + salesDataManager.products.map { it.category }.distinct()
        }

        val filteredProducts = remember(searchQuery, selectedCategory, salesDataManager.products) {
            var products = if (searchQuery.isNotEmpty()) {
                salesDataManager.searchProducts(searchQuery)
            } else {
                salesDataManager.products
            }

            if (selectedCategory != "الكل") {
                products = products.filter { it.category == selectedCategory }
            }

            products.sortedBy { it.name }
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
                    title = "إدارة المنتجات",
                    subtitle = "${salesDataManager.products.size} منتج إجمالي"
                )

                QuickActionButton(
                    text = "إضافة منتج",
                    icon = Icons.Default.Add,
                    onClick = { showAddProductDialog = true },
                    modifier = Modifier.width(160.dp)
                )
            }

            // Search and Filter
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "البحث في المنتجات...",
                    modifier = Modifier.weight(1f)
                )

                // Category Filter
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    RTLRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        RTLSpacer(8.dp)

                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            TextField(
                                value = selectedCategory,
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
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        onClick = {
                                            selectedCategory = category
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

            // Product Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "إجمالي المنتجات",
                    value = salesDataManager.products.size.toString(),
                    subtitle = "منتج مسجل",
                    icon = Icons.Default.Inventory,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                val totalValue = salesDataManager.products.sumOf { it.price * it.stock }
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "قيمة المخزون",
                    value = currencyFormatter.format(totalValue),
                    subtitle = "بسعر التجزئة",
                    icon = Icons.Default.AccountBalance,
                    iconColor = AppColors.Success
                )

                val lowStockCount = salesDataManager.products.count { it.stock < 10 }
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "مخزون منخفض",
                    value = lowStockCount.toString(),
                    subtitle = "منتج يحتاج تجديد",
                    icon = Icons.Default.Warning,
                    iconColor = AppColors.Warning
                )

                val categoriesCount = categories.size - 1 // Exclude "الكل"
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "الفئات",
                    value = categoriesCount.toString(),
                    subtitle = "فئة منتجات",
                    icon = Icons.Default.Category,
                    iconColor = AppColors.Info
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Products Grid
            if (filteredProducts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Inventory,
                    title = "لا يوجد منتجات",
                    description = "لم يتم العثور على منتجات تطابق بحثك. حاول تغيير الفلاتر أو إضافة منتج جديد.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalItemSpacing = 16.dp
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            currencyFormatter = currencyFormatter,
                            onEdit = { editingProduct = product },
                            onDelete = { /* TODO: Implement delete logic */ }
                        )
                    }
                }
            }
        }

        // Add/Edit Product Dialog
        if (showAddProductDialog || editingProduct != null) {
            ProductDialog(
                product = editingProduct,
                categories = categories.filter { it != "الكل" },
                onDismiss = {
                    showAddProductDialog = false
                    editingProduct = null
                },
                onSave = { product ->
                    if (editingProduct != null) {
                        // Update existing product
                        val index = salesDataManager.products.indexOfFirst { it.id == product.id }
                        if (index != -1) {
                            salesDataManager.products[index] = product
                        }
                    } else {
                        salesDataManager.addProduct(product)
                    }
                    showAddProductDialog = false
                    editingProduct = null
                }
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    @Composable
    fun getCategoryColor(category: String): Color {
        return when (category) {
            "إلكترونيات" -> AppColors.Primary
            "إكسسوارات" -> AppColors.Secondary
            "ملابس" -> AppColors.Purple
            "كتب" -> AppColors.Indigo
            else -> AppColors.Info
        }
    }

    @Composable
    fun getStockStatus(stock: Int): Pair<String, Color> {
        return when {
            stock > 20 -> "متوفر" to AppColors.Success
            stock > 10 -> "قليل" to AppColors.Warning
            stock > 0 -> "ناقص" to AppColors.Error
            else -> "نفد" to AppColors.Error
        }
    }

    val categoryColor = getCategoryColor(product.category)
    val stockStatus = getStockStatus(product.stock)

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardStyles.defaultCardColors(),
        elevation = CardStyles.defaultCardElevation()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Card(
                        modifier = Modifier.padding(top = 4.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = categoryColor.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = stockStatus.second.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = stockStatus.first,
                            style = MaterialTheme.typography.bodySmall,
                            color = stockStatus.second,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "المخزون: ${product.stock}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = currencyFormatter.format(product.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (product.cost > 0) {
                        val profit = product.price - product.cost
                        val profitMargin = (profit / product.price) * 100
                        Text(
                            text = "هامش الربح: ${String.format("%.1f", profitMargin)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.Success
                        )
                    }
                }
            }

            if (!product.barcode.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    RTLSpacer(4.dp)
                    Text(
                        text = product.barcode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    product: Product?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var category by remember { mutableStateOf(product?.category ?: (if (categories.isEmpty()) "" else categories.first())) }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var cost by remember { mutableStateOf(product?.cost?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardStyles.defaultCardColors(),
            elevation = CardStyles.defaultCardElevation(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (product == null) "إضافة منتج جديد" else "تعديل المنتج",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المنتج") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("الباركود") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("الفئة") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        readOnly = categories.isNotEmpty()
                    )

                    if (categories.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { categoryOption ->
                                DropdownMenuItem(
                                    text = { Text(categoryOption) },
                                    onClick = {
                                        category = categoryOption
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("السعر") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = { Text("التكلفة") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("المخزون") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val newProduct = Product(
                                id = product?.id ?: (System.currentTimeMillis() % 10000).toInt(),
                                name = name,
                                barcode = barcode,
                                category = category,
                                price = price.toDoubleOrNull() ?: 0.0,
                                cost = cost.toDoubleOrNull() ?: 0.0,
                                stock = stock.toIntOrNull() ?: 0
                            )
                            onSave(newProduct)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}
