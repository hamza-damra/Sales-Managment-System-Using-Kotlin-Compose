package ui.screens

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import data.*
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import java.text.NumberFormat
import java.util.*

@Composable
fun SalesScreen(salesDataManager: SalesDataManager) {
    RTLProvider {
        // State management
        var selectedProducts by remember { mutableStateOf(mutableListOf<SaleItem>()) }
        var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
        var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
        var showProductSelection by remember { mutableStateOf(false) }
        var showCustomerSelection by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var showSaleSuccess by remember { mutableStateOf(false) }
        var isProcessingSale by remember { mutableStateOf(false) }

        // Currency formatter for Arabic locale
        val currencyFormatter = remember {
            NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
                currency = Currency.getInstance("SAR")
            }
        }

        // Calculate totals
        val subtotal = selectedProducts.sumOf { it.unitPrice * it.quantity }
        val tax = subtotal * 0.15 // 15% VAT
        val total = subtotal + tax

        RTLRow(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left Panel - Product Selection
            Card(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                colors = CardStyles.defaultCardColors(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardStyles.defaultCardElevation()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = "إختيار المنتجات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Search Bar
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "البحث في المنتجات...",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Products Grid
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(minSize = 200.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        val filteredProducts = if (searchQuery.isNotEmpty()) {
                            salesDataManager.searchProducts(searchQuery)
                        } else {
                            salesDataManager.products
                        }

                        items(filteredProducts) { product ->
                            ProductSelectionCard(
                                product = product,
                                currencyFormatter = currencyFormatter,
                                onAddToSale = { quantity ->
                                    val existingItem = selectedProducts.find { it.product.id == product.id }
                                    if (existingItem != null) {
                                        val index = selectedProducts.indexOf(existingItem)
                                        selectedProducts[index] = existingItem.copy(quantity = existingItem.quantity + quantity)
                                    } else {
                                        selectedProducts.add(
                                            SaleItem(
                                                product = product,
                                                quantity = quantity,
                                                unitPrice = product.price
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            RTLSpacer(16.dp)

            // Right Panel - Sale Summary & Checkout
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = CardStyles.defaultCardColors(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardStyles.defaultCardElevation()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Text(
                        text = "تفاصيل البيع",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Customer Selection
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomerSelection = true }
                    ) {
                        RTLRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = selectedCustomer?.name ?: "إختيار العميل",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedCustomer != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (selectedCustomer != null) {
                                    Text(
                                        text = selectedCustomer!!.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Selected Products
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "المنتجات المحددة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (selectedProducts.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Default.ShoppingCart,
                                    title = "لا توجد منتجات",
                                    description = "إختر المنتجات من القائمة"
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(selectedProducts) { item ->
                                        SaleItemCard(
                                            item = item,
                                            currencyFormatter = currencyFormatter,
                                            onQuantityChange = { newQuantity ->
                                                if (newQuantity > 0) {
                                                    selectedProducts[selectedProducts.indexOf(item)] =
                                                        item.copy(quantity = newQuantity)
                                                } else {
                                                    selectedProducts.remove(item)
                                                }
                                            },
                                            onRemove = {
                                                selectedProducts.remove(item)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Payment Method Selection
                    PaymentMethodSelector(
                        selectedMethod = selectedPaymentMethod,
                        onMethodSelected = { selectedPaymentMethod = it }
                    )

                    // Totals
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TotalRow("المجموع الفرعي", currencyFormatter.format(subtotal))
                            TotalRow("الضريبة (15%)", currencyFormatter.format(tax))
                            Divider(color = MaterialTheme.colorScheme.primary)
                            TotalRow(
                                "الإجمالي",
                                currencyFormatter.format(total),
                                isTotal = true
                            )
                        }
                    }

                    // Checkout Button
                    Button(
                        onClick = {
                            isProcessingSale = true
                            // Process sale logic here
                            isProcessingSale = false
                            showSaleSuccess = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedProducts.isNotEmpty() && !isProcessingSale,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isProcessingSale) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            RTLSpacer(8.dp)
                            Text("إتمام البيع", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

        // Customer Selection Dialog
        if (showCustomerSelection) {
            CustomerSelectionDialog(
                customers = salesDataManager.customers,
                onCustomerSelected = { customer ->
                    selectedCustomer = customer
                    showCustomerSelection = false
                },
                onDismiss = { showCustomerSelection = false }
            )
        }

        // Sale Success Dialog
        if (showSaleSuccess) {
            SaleSuccessDialog(
                total = total,
                currencyFormatter = currencyFormatter,
                onDismiss = {
                    showSaleSuccess = false
                    selectedProducts.clear()
                    selectedCustomer = null
                    selectedPaymentMethod = PaymentMethod.CASH
                }
            )
        }
    }
}

@Composable
private fun SalesHeader(
    totalItems: Int,
    totalAmount: Double
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
            currency = Currency.getInstance("SAR")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.defaultCardElevation()
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
                    text = "نقطة البيع",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "إدارة المبيعات والفواتير",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatCard(
                    title = "إجمالي العناصر",
                    value = totalItems.toString(),
                    icon = Icons.Default.ShoppingCart,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(160.dp).height(80.dp)
                )

                StatCard(
                    title = "المبلغ الإجمالي",
                    value = currencyFormatter.format(totalAmount),
                    icon = Icons.Default.AttachMoney,
                    iconColor = AppTheme.colors.success,
                    modifier = Modifier.width(180.dp).height(80.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductsSection(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    products: List<Product>,
    onAddToCart: (Product, Int) -> Unit,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.defaultCardElevation()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المنتجات المتاحة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${products.size} منتج متاح",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick action to add new product
                IconButton(
                    onClick = { /* Navigate to add product */ },
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "إضافة منتج جديد",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enhanced Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange,
                placeholder = "البحث في المنتجات... (الاسم، الباركود، الفئة)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Products Grid - Responsive
            if (products.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Inventory,
                    title = "لا توجد منتجات",
                    description = if (searchQuery.isEmpty())
                        "لم يتم إضافة أي منتجات بعد"
                    else
                        "لا توجد منتجات تطابق البحث '$searchQuery'"
                )
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(minSize = 280.dp),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        EnhancedProductCard(
                            product = product,
                            onAddToCart = onAddToCart,
                            currencyFormatter = currencyFormatter
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedProductCard(
    product: Product,
    onAddToCart: (Product, Int) -> Unit,
    currencyFormatter: NumberFormat
) {
    var quantity by remember { mutableStateOf(1) }

    Card(
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardStyles.defaultCardElevation(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stock status indicator
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (product.stock > 10)
                            AppTheme.colors.success.copy(alpha = 0.1f)
                        else AppTheme.colors.warning.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${product.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (product.stock > 10) AppTheme.colors.success else AppTheme.colors.warning,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price and barcode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currencyFormatter.format(product.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = product.barcode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity selector and add to cart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "تقليل",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { if (quantity < product.stock) quantity++ },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "زيادة",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Add to cart button
                FilledTonalButton(
                    onClick = { onAddToCart(product, quantity) },
                    enabled = product.stock >= quantity,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CartAndCheckoutSection(
    modifier: Modifier = Modifier,
    selectedProducts: List<SaleItem>,
    selectedCustomer: Customer?,
    selectedPaymentMethod: PaymentMethod,
    customers: List<Customer>,
    onCustomerSelect: (Customer?) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onQuantityChange: (SaleItem, Int) -> Unit,
    onRemoveItem: (SaleItem) -> Unit,
    onClearAll: () -> Unit,
    onCompleteSale: () -> Unit,
    isProcessing: Boolean,
    currencyFormatter: NumberFormat
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Cart Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "سلة المشتريات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${selectedProducts.size} منتج",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedProducts.isNotEmpty()) {
                    TextButton(
                        onClick = onClearAll,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = AppTheme.colors.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مسح الكل")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Selection
            CustomerSelectionCard(
                selectedCustomer = selectedCustomer,
                customers = customers,
                onCustomerSelect = onCustomerSelect
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cart Items
            if (selectedProducts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ShoppingCart,
                    title = "السلة فارغة",
                    description = "اختر المنتجات من القائمة لإضافتها هنا",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedProducts) { saleItem ->
                        EnhancedCartItem(
                            saleItem = saleItem,
                            onQuantityChange = onQuantityChange,
                            onRemove = onRemoveItem,
                            currencyFormatter = currencyFormatter
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selection
                PaymentMethodSection(
                    selectedPaymentMethod = selectedPaymentMethod,
                    onPaymentMethodChange = onPaymentMethodChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Order Summary
                OrderSummaryCard(
                    selectedProducts = selectedProducts,
                    currencyFormatter = currencyFormatter
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Complete Sale Button
                Button(
                    onClick = onCompleteSale,
                    enabled = selectedProducts.isNotEmpty() && !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جاري المعالجة...")
                    } else {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "إتمام البيع",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerSelectionCard(
    selectedCustomer: Customer?,
    customers: List<Customer>,
    onCustomerSelect: (Customer?) -> Unit
) {
    var showCustomerDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showCustomerDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )

                Column {
                    Text(
                        text = "العميل",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedCustomer?.name ?: "عميل مباشر",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    selectedCustomer?.phone?.let { phone ->
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showCustomerDialog) {
        CustomerSelectionDialog(
            customers = customers,
            selectedCustomer = selectedCustomer,
            onCustomerSelect = { customer ->
                onCustomerSelect(customer)
                showCustomerDialog = false
            },
            onDismiss = { showCustomerDialog = false }
        )
    }
}

@Composable
private fun EnhancedCartItem(
    saleItem: SaleItem,
    onQuantityChange: (SaleItem, Int) -> Unit,
    onRemove: (SaleItem) -> Unit,
    currencyFormatter: NumberFormat
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = saleItem.product.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currencyFormatter.format(saleItem.unitPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { onRemove(saleItem) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إزالة",
                        tint = AppTheme.colors.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onQuantityChange(saleItem, saleItem.quantity - 1) },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "تقليل",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = saleItem.quantity.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { onQuantityChange(saleItem, saleItem.quantity + 1) },
                        enabled = saleItem.quantity < saleItem.product.stock,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "زيادة",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Subtotal
                Text(
                    text = currencyFormatter.format(saleItem.subtotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodSection(
    selectedPaymentMethod: PaymentMethod,
    onPaymentMethodChange: (PaymentMethod) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "طريقة الدفع",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            PaymentMethod.entries.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPaymentMethodChange(method) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == method,
                        onClick = { onPaymentMethodChange(method) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Icon(
                        imageVector = when (method) {
                            PaymentMethod.CASH -> Icons.Default.Money
                            PaymentMethod.CARD -> Icons.Default.CreditCard
                            PaymentMethod.BANK_TRANSFER -> Icons.Default.AccountBalance
                            PaymentMethod.DIGITAL_WALLET -> Icons.Default.Wallet
                        },
                        contentDescription = null,
                        tint = if (selectedPaymentMethod == method) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = method.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selectedPaymentMethod == method) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (selectedPaymentMethod == method) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    selectedProducts: List<SaleItem>,
    currencyFormatter: NumberFormat
) {
    val subtotal = selectedProducts.sumOf { it.subtotal }
    val tax = subtotal * 0.15
    val total = subtotal + tax

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ملخص الطلب",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "المجموع الفرعي:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    currencyFormatter.format(subtotal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "الضريبة (15%):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    currencyFormatter.format(tax),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "الإجمالي:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    currencyFormatter.format(total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CustomerSelectionDialog(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    onCustomerSelect: (Customer?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "اختيار العميل",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "البحث في العملاء...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Direct customer option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCustomerSelect(null) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedCustomer == null,
                        onClick = { onCustomerSelect(null) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("عميل مباشر", fontWeight = FontWeight.Medium)
                }

                Divider()

                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    val filteredCustomers = customers.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phone.contains(searchQuery) ||
                        it.email.contains(searchQuery, ignoreCase = true)
                    }

                    items(filteredCustomers) { customer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCustomerSelect(customer) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCustomer?.id == customer.id,
                                onClick = { onCustomerSelect(customer) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    customer.name,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    customer.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("تم")
            }
        }
    )
}

@Composable
private fun SaleSuccessDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppTheme.colors.success,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "تم إنجاز البيع بنجاح",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "تم حفظ الفاتورة وتحديث المخزون بنجاح",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.success
                )
            ) {
                Text("موافق")
            }
        }
    )
}
