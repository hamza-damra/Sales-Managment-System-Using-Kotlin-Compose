@file:OptIn(ExperimentalAnimationApi::class)

package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import data.*
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode

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
        var showAddToCartAnimation by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

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

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Product Selection
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
                                    text = "إختيار المنتجات",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${salesDataManager.products.size} منتج متاح",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Add Product Button with better styling
                            FilledTonalButton(
                                onClick = { /* Navigate to add product */ },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "إضافة منتج",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("منتج جديد")
                            }
                        }

                        // Enhanced Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "البحث في المنتجات...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "بحث",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "مسح البحث",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        // Products Grid with better spacing
                        val filteredProducts = if (searchQuery.isNotEmpty()) {
                            salesDataManager.searchProducts(searchQuery)
                        } else {
                            salesDataManager.products
                        }

                        if (filteredProducts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyStateImproved(
                                    icon = if (searchQuery.isEmpty()) Icons.Outlined.Inventory2 else Icons.Outlined.SearchOff,
                                    title = if (searchQuery.isEmpty()) "لا توجد منتجات" else "لا توجد نتائج",
                                    description = if (searchQuery.isEmpty())
                                        "ابدأ بإضافة منتجات جديدة للمخزون"
                                    else
                                        "جرب البحث بكلمات مختلفة"
                                )
                            }
                        } else {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Adaptive(minSize = 280.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalItemSpacing = 16.dp
                            ) {
                                items(filteredProducts) { product ->
                                    ProductCardImproved(
                                        product = product,
                                        currencyFormatter = currencyFormatter,
                                        onAddToSale = { quantity ->
                                            val existingItem = selectedProducts.find { it.product.id == product.id }
                                            if (existingItem != null) {
                                                val index = selectedProducts.indexOf(existingItem)
                                                if (index >= 0) {
                                                    selectedProducts[index] = existingItem.copy(
                                                        quantity = existingItem.quantity + quantity
                                                    )
                                                }
                                            } else {
                                                selectedProducts.add(
                                                    SaleItem(
                                                        product = product,
                                                        quantity = quantity,
                                                        unitPrice = product.price
                                                    )
                                                )
                                            }

                                            // Show add to cart animation
                                            coroutineScope.launch {
                                                showAddToCartAnimation = true
                                                delay(2000)
                                                showAddToCartAnimation = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }













                // Right Panel - Sale Summary & Checkout
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
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header with cart count badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    if (selectedProducts.isNotEmpty()) {
                                        Badge(
                                            modifier = Modifier.align(Alignment.TopEnd),
                                            containerColor = MaterialTheme.colorScheme.error
                                        ) {
                                            Text(
                                                selectedProducts.size.toString(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "سلة المشتريات",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (selectedProducts.isNotEmpty()) {
                                TextButton(
                                    onClick = { selectedProducts.clear() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مسح الكل")
                                }
                            }
                        }

                        // Customer Selection with better styling
                        CustomerSelectionCardImproved(
                            selectedCustomer = selectedCustomer,
                            onClick = { showCustomerSelection = true }
                        )

                        // Cart Items with improved layout
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        ) {
                            if (selectedProducts.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyStateImproved(
                                        icon = Icons.Outlined.ShoppingCartCheckout,
                                        title = "السلة فارغة",
                                        description = "اختر المنتجات من القائمة لإضافتها"
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(selectedProducts) { item ->
                                        CartItemImproved(
                                            item = item,
                                            onQuantityChange = { newQuantity ->
                                                if (newQuantity > 0) {
                                                    val index = selectedProducts.indexOf(item)
                                                    if (index >= 0) {
                                                        selectedProducts[index] = item.copy(quantity = newQuantity)
                                                    }
                                                } else {
                                                    selectedProducts.remove(item)
                                                }
                                            },
                                            onRemove = {
                                                selectedProducts.remove(item)
                                            },
                                            currencyFormatter = currencyFormatter
                                        )
                                    }
                                }
                            }
                        }

                        // Payment Method Selection
                        PaymentMethodSelectorImproved(
                            selectedMethod = selectedPaymentMethod,
                            onMethodSelected = { selectedPaymentMethod = it }
                        )

                        // Totals with better styling
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TotalRowImproved("المجموع الفرعي", currencyFormatter.format(subtotal))
                                TotalRowImproved("الضريبة (15%)", currencyFormatter.format(tax))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    thickness = 1.dp
                                )
                                TotalRowImproved(
                                    "الإجمالي",
                                    currencyFormatter.format(total),
                                    isTotal = true
                                )
                            }
                        }

                        // Checkout Button with enhanced styling
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isProcessingSale = true
                                    delay(2000) // Simulate processing
                                    isProcessingSale = false
                                    showSaleSuccess = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = selectedProducts.isNotEmpty() && !isProcessingSale,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 8.dp,
                                disabledElevation = 0.dp
                            )
                        ) {
                            AnimatedContent(
                                targetState = isProcessingSale,
                                transitionSpec = {
                                    fadeIn() with fadeOut()
                                }
                            ) { processing ->
                                if (processing) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            "جاري المعالجة...",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Payment,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            "إتمام البيع",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Add to cart animation overlay
            AnimatedVisibility(
                visible = showAddToCartAnimation,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "تمت الإضافة للسلة",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
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
            SaleSuccessDialogImproved(
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
    }


}

// Component functions
@Composable
private fun ProductCardImproved(
    product: Product,
    onAddToSale: (Int) -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    var quantity by remember { mutableStateOf(1) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 4.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Product Header
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Category,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Stock indicator with improved styling
                    StockIndicator(stock = product.stock)
                }

                // Barcode if available
                if (product.barcode.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = product.barcode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Price with enhanced styling
                Text(
                    text = currencyFormatter.format(product.price),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Quantity and Add to Cart Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quantity selector with better touch targets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrease button
                    FilledIconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "تقليل",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Quantity display
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .widthIn(min = 60.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Increase button
                    FilledIconButton(
                        onClick = { if (quantity < product.stock) quantity++ },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        enabled = quantity < product.stock
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "زيادة",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Add to cart button with animation
                Button(
                    onClick = {
                        onAddToSale(quantity)
                        quantity = 1 // Reset quantity after adding
                    },
                    enabled = product.stock >= quantity,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 8.dp,
                        disabledElevation = 0.dp
                    )
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "إضافة للسلة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StockIndicator(stock: Int) {
    val (backgroundColor, textColor, text) = when {
        stock > 20 -> Triple(
            AppTheme.colors.success.copy(alpha = 0.15f),
            AppTheme.colors.success,
            "$stock متوفر"
        )
        stock > 5 -> Triple(
            AppTheme.colors.warning.copy(alpha = 0.15f),
            AppTheme.colors.warning,
            "$stock متبقي"
        )
        stock > 0 -> Triple(
            AppTheme.colors.error.copy(alpha = 0.15f),
            AppTheme.colors.error,
            "$stock فقط!"
        )
        else -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "نفذ المخزون"
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (stock > 0) Icons.Outlined.Inventory else Icons.Outlined.RemoveShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = textColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun CustomerSelectionCardImproved(
    selectedCustomer: Customer?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer avatar
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "العميل",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedCustomer?.name ?: "عميل مباشر",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
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
}

@Composable
private fun TotalRowImproved(
    label: String,
    value: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = if (isTotal)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isTotal)
                MaterialTheme.typography.titleLarge
            else
                MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isTotal)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyStateImproved(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SaleSuccessDialogImproved(
    total: Double,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.success
                )
            ) {
                Text("تم", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { /* Print receipt */ }) {
                Icon(
                    Icons.Default.Print,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("طباعة الفاتورة")
            }
        },
        icon = {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = AppTheme.colors.success.copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppTheme.colors.success,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        },
        title = {
            Text(
                "تم إتمام البيع بنجاح!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "تم حفظ الفاتورة وتحديث المخزون",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "المبلغ الإجمالي: ${currencyFormatter.format(total)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CustomerSelectionDialog(
    customers: List<Customer>,
    onCustomerSelected: (Customer?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.9f),
        title = {
            Column {
                Text(
                    "اختيار العميل",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("البحث بالاسم أو رقم الهاتف...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }
        },
        text = {
            Column {
                // Direct customer option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCustomerSelected(null) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (searchQuery.isEmpty())
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "عميل مباشر (بدون حساب)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Customer list
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredCustomers = customers.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.phone.contains(searchQuery) ||
                                it.email.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredCustomers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.PersonSearch,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        "لا توجد نتائج",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredCustomers) { customer ->
                            CustomerListItem(
                                customer = customer,
                                onClick = { onCustomerSelected(customer) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء", fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CustomerListItem(
    customer: Customer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Customer avatar
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = customer.name.take(1),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (customer.email.isNotEmpty()) {
                        Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = customer.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}






@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (method) {
        PaymentMethod.CASH -> Icons.Outlined.Payments
        PaymentMethod.CARD -> Icons.Outlined.CreditCard
        PaymentMethod.BANK_TRANSFER -> Icons.Outlined.AccountBalance
        PaymentMethod.DIGITAL_WALLET -> Icons.Outlined.Wallet
    }

    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}