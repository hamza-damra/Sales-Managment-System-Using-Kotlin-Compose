@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)

package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import data.*
import data.api.*
import data.repository.*
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.viewmodels.SalesViewModel
import services.PdfReceiptService
import services.CanvasPdfReceiptService
import utils.FileDialogUtils
import java.text.NumberFormat
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import kotlinx.datetime.*
import java.io.File

/**
 * Comprehensive Sales Screen with full backend integration, PDF generation, and advanced features
 * Combines the best features from all sales screen implementations
 */
@Composable
fun SalesScreen(
    salesRepository: SalesRepository,
    customerRepository: CustomerRepository,
    productRepository: ProductRepository,
    promotionRepository: PromotionRepository,
    notificationService: services.NotificationService
) {
    val salesViewModel = remember {
        SalesViewModel(salesRepository, customerRepository, productRepository, promotionRepository)
    }
    
    // Collect state from ViewModel
    val sales by salesViewModel.sales.collectAsState()
    val customers by salesViewModel.customers.collectAsState()
    val products by salesViewModel.products.collectAsState()
    val selectedProducts by salesViewModel.selectedProducts.collectAsState()
    val selectedCustomer by salesViewModel.selectedCustomer.collectAsState()
    val selectedPaymentMethod by salesViewModel.selectedPaymentMethod.collectAsState()
    val isLoading by salesViewModel.isLoading.collectAsState()
    val error by salesViewModel.error.collectAsState()
    val isProcessingSale by salesViewModel.isProcessingSale.collectAsState()
    val lastCompletedSale by salesViewModel.lastCompletedSale.collectAsState()
    val cartTotal by salesViewModel.cartTotal.collectAsState()
    val cartSubtotal by salesViewModel.cartSubtotal.collectAsState()
    val cartTax by salesViewModel.cartTax.collectAsState()
    val filteredSales by salesViewModel.filteredSales.collectAsState()
    val searchQuery by salesViewModel.searchQuery.collectAsState()

    // Promotion state
    val appliedPromotion by salesViewModel.appliedPromotion.collectAsState()
    val promotionCode by salesViewModel.promotionCode.collectAsState()
    val promotionDiscount by salesViewModel.promotionDiscount.collectAsState()
    val isValidatingPromotion by salesViewModel.isValidatingPromotion.collectAsState()
    val promotionError by salesViewModel.promotionError.collectAsState()
    
    // Enhanced UI State
    var currentTab by remember { mutableStateOf(SalesTab.NEW_SALE) }
    var showProductSelection by remember { mutableStateOf(false) }
    var showCustomerSelection by remember { mutableStateOf(false) }
    var showSaleSuccess by remember { mutableStateOf(false) }
    var showSaleDetails by remember { mutableStateOf<SaleDTO?>(null) }
    var statusFilter by remember { mutableStateOf<String?>(null) }
    var autoRefreshEnabled by remember { mutableStateOf(true) }
    var showAdvancedFilters by remember { mutableStateOf(false) }
    var showAddToCartAnimation by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

    // Currency formatter for Arabic locale
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
            currency = Currency.getInstance("SAR")
        }
    }
    
    // Auto-refresh sales data every 30 seconds
    LaunchedEffect(autoRefreshEnabled) {
        if (autoRefreshEnabled && currentTab == SalesTab.SALES_HISTORY) {
            while (autoRefreshEnabled) {
                delay(30000) // 30 seconds
                salesViewModel.refreshSales()
            }
        }
    }

    RTLProvider {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Enhanced Header with real-time stats
            EnhancedSalesHeader(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                searchQuery = searchQuery,
                onSearchQueryChange = { salesViewModel.updateSearchQuery(it) },
                statusFilter = statusFilter,
                onStatusFilterChange = { 
                    statusFilter = it
                    salesViewModel.updateStatusFilter(it)
                },
                autoRefreshEnabled = autoRefreshEnabled,
                onAutoRefreshToggle = { autoRefreshEnabled = it },
                showAdvancedFilters = showAdvancedFilters,
                onToggleAdvancedFilters = { showAdvancedFilters = it },
                salesStats = SalesStats(
                    totalSales = sales.size,
                    pendingSales = sales.count { it.status == "PENDING" },
                    completedSales = sales.count { it.status == "COMPLETED" },
                    canceledSales = sales.count { it.status == "CANCELLED" },
                    totalRevenue = sales.filter { it.status == "COMPLETED" }.sumOf { it.totalAmount }
                ),
                onRefresh = {
                    coroutineScope.launch {
                        salesViewModel.refreshSales()
                    }
                }
            )
            
            // Enhanced Error handling with retry functionality
            error?.let { errorMessage ->
                EnhancedErrorBanner(
                    message = errorMessage,
                    onDismiss = { salesViewModel.clearError() },
                    onRetry = {
                        coroutineScope.launch {
                            when (currentTab) {
                                SalesTab.NEW_SALE -> {
                                    // Retry loading customers and products
                                    customerRepository.loadCustomers()
                                    productRepository.loadProducts()
                                }
                                SalesTab.SALES_HISTORY -> {
                                    salesViewModel.refreshSales()
                                }
                            }
                        }
                    }
                )
            }
            
            // Loading indicator with progress details
            if (isLoading) {
                EnhancedLoadingIndicator(
                    message = when (currentTab) {
                        SalesTab.NEW_SALE -> "جاري تحميل البيانات..."
                        SalesTab.SALES_HISTORY -> "جاري تحميل المبيعات..."
                    }
                )
            }
            
            // Content based on selected tab
            when (currentTab) {
                SalesTab.NEW_SALE -> {
                    EnhancedNewSaleContent(
                        selectedProducts = selectedProducts,
                        selectedCustomer = selectedCustomer,
                        selectedPaymentMethod = selectedPaymentMethod,
                        cartTotal = cartTotal,
                        cartSubtotal = cartSubtotal,
                        cartTax = cartTax,
                        promotionDiscount = promotionDiscount,
                        appliedPromotion = appliedPromotion,
                        promotionCode = promotionCode,
                        isValidatingPromotion = isValidatingPromotion,
                        promotionError = promotionError,
                        isProcessingSale = isProcessingSale,
                        currencyFormatter = currencyFormatter,
                        availableProducts = products,
                        availableCustomers = customers,
                        onShowProductSelection = { showProductSelection = true },
                        onShowCustomerSelection = { showCustomerSelection = true },
                        onPaymentMethodChange = { salesViewModel.selectPaymentMethod(it) },
                        onQuantityChange = { productId, quantity ->
                            salesViewModel.updateCartItemQuantity(productId, quantity)
                        },
                        onRemoveFromCart = { productId ->
                            salesViewModel.removeFromCart(productId)
                        },
                        onPromotionCodeChange = { code ->
                            salesViewModel.updatePromotionCode(code)
                        },
                        onApplyPromotion = { code ->
                            coroutineScope.launch {
                                salesViewModel.validateAndApplyPromotion(code)
                            }
                        },
                        onClearPromotion = {
                            salesViewModel.clearPromotion()
                        },
                        onCreateSale = {
                            coroutineScope.launch {
                                println("🔍 SalesScreen - Create Sale button clicked!")
                                println("🔍 Selected Customer: ${selectedCustomer?.name}")
                                println("🔍 Selected Products: ${selectedProducts.size}")
                                println("🔍 Cart Total: $cartTotal")
                                println("🔍 Applied Promotion: ${appliedPromotion?.name}")
                                println("🔍 Promotion Code: $promotionCode")

                                // Pass the coupon code if a promotion is applied
                                val couponCode = if (appliedPromotion != null) promotionCode.takeIf { it.isNotBlank() } else null
                                val result = salesViewModel.createSale(couponCode)
                                println("🔍 Create Sale Result: ${if (result.isSuccess) "SUCCESS" else "ERROR"}")

                                if (result.isSuccess) {
                                    println("🔍 Sale created successfully!")
                                    println("🔍 lastCompletedSale: ${lastCompletedSale?.id}")
                                    notificationService.showSuccess(
                                        message = "تم إنشاء البيع بنجاح",
                                        title = "نجح العملية"
                                    )
                                    showSaleSuccess = true
                                    // Auto-switch to sales history to show the new sale
                                    delay(2000)
                                    currentTab = SalesTab.SALES_HISTORY
                                } else if (result.isError) {
                                    val error = (result as NetworkResult.Error).exception
                                    println("🔍 Sale creation failed: ${error.message}")

                                    // Handle specific validation errors
                                    when {
                                        error.message?.contains("Customer must be selected") == true -> {
                                            notificationService.showValidationError(
                                                message = "يرجى اختيار عميل لإتمام البيع",
                                                title = "عميل مطلوب"
                                            )
                                        }
                                        error.message?.contains("At least one product must be added") == true -> {
                                            notificationService.showValidationError(
                                                message = "يرجى إضافة منتج واحد على الأقل إلى السلة",
                                                title = "منتجات مطلوبة"
                                            )
                                        }
                                        else -> {
                                            notificationService.showError(
                                                message = error.message ?: "حدث خطأ غير متوقع أثناء إنشاء البيع",
                                                title = "خطأ في إنشاء البيع"
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        onAddToCartAnimation = {
                            coroutineScope.launch {
                                showAddToCartAnimation = true
                                delay(2000)
                                showAddToCartAnimation = false
                            }
                        }
                    )
                }
                
                SalesTab.SALES_HISTORY -> {
                    EnhancedSalesHistoryContent(
                        sales = filteredSales,
                        currencyFormatter = currencyFormatter,
                        statusFilter = statusFilter,
                        showAdvancedFilters = showAdvancedFilters,
                        onSaleClick = { sale -> showSaleDetails = sale },
                        onCompleteSale = { saleId ->
                            coroutineScope.launch {
                                val result = salesViewModel.completeSale(saleId)
                                if (result.isSuccess) {
                                    // Auto-refresh to show updated status
                                    salesViewModel.refreshSales()
                                }
                            }
                        },
                        onCancelSale = { saleId ->
                            coroutineScope.launch {
                                val result = salesViewModel.cancelSale(saleId)
                                if (result.isSuccess) {
                                    // Auto-refresh to show updated status
                                    salesViewModel.refreshSales()
                                }
                            }
                        },
                        onLoadMore = {
                            coroutineScope.launch {
                                salesViewModel.loadMoreSales()
                            }
                        }
                    )
                }
            }
        }
        
        // Enhanced Dialogs with better UX
        if (showProductSelection) {
            EnhancedProductSelectionDialog(
                products = products,
                onProductSelected = { product, quantity ->
                    salesViewModel.addProductToCart(product, quantity)
                    showProductSelection = false
                    // Show add to cart animation
                    coroutineScope.launch {
                        showAddToCartAnimation = true
                        delay(2000)
                        showAddToCartAnimation = false
                    }
                },
                onDismiss = { showProductSelection = false }
            )
        }
        
        if (showCustomerSelection) {
            EnhancedCustomerSelectionDialog(
                customers = customers,
                onCustomerSelected = { customer ->
                    salesViewModel.selectCustomer(customer)
                    showCustomerSelection = false
                },
                onDismiss = { showCustomerSelection = false }
            )
        }
        
        if (showSaleSuccess) {
            // Use the actual sale total amount instead of cartTotal (which gets cleared)
            val actualTotal = lastCompletedSale?.totalAmount ?: cartTotal

            println("🔍 SalesScreen - Success Dialog Debug:")
            println("🔍 showSaleSuccess: $showSaleSuccess")
            println("🔍 cartTotal: $cartTotal")
            println("🔍 lastCompletedSale: ${lastCompletedSale?.id}")
            println("🔍 lastCompletedSale.totalAmount: ${lastCompletedSale?.totalAmount}")
            println("🔍 actualTotal: $actualTotal")

            SaleSuccessDialogImproved(
                total = actualTotal,
                currencyFormatter = currencyFormatter,
                saleData = lastCompletedSale,
                selectedCustomer = selectedCustomer,
                selectedPaymentMethod = selectedPaymentMethod,
                selectedProducts = selectedProducts,
                onDismiss = {
                    showSaleSuccess = false
                    salesViewModel.clearCart()
                    salesViewModel.clearLastCompletedSale()
                },
                onViewSale = {
                    showSaleSuccess = false
                    lastCompletedSale?.let { sale ->
                        showSaleDetails = sale
                    }
                },
                onCreateAnother = {
                    showSaleSuccess = false
                    salesViewModel.clearCart()
                    salesViewModel.clearLastCompletedSale()
                    currentTab = SalesTab.NEW_SALE
                }
            )
        }
        
        showSaleDetails?.let { sale ->
            EnhancedSaleDetailsDialog(
                sale = sale,
                currencyFormatter = currencyFormatter,
                onDismiss = { showSaleDetails = null },
                onCompleteSale = { saleId ->
                    coroutineScope.launch {
                        val result = salesViewModel.completeSale(saleId)
                        if (result.isSuccess) {
                            showSaleDetails = null
                            salesViewModel.refreshSales()
                        }
                    }
                },
                onCancelSale = { saleId ->
                    coroutineScope.launch {
                        val result = salesViewModel.cancelSale(saleId)
                        if (result.isSuccess) {
                            showSaleDetails = null
                            salesViewModel.refreshSales()
                        }
                    }
                }
            )
        }
        
        // Add to cart animation overlay
        Box(modifier = Modifier.fillMaxSize()) {
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
    }
}

// Enums and Data Classes
enum class SalesTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    NEW_SALE("بيع جديد", Icons.Filled.Add),
    SALES_HISTORY("سجل المبيعات", Icons.Filled.History)
}

data class SalesStats(
    val totalSales: Int,
    val pendingSales: Int,
    val completedSales: Int,
    val canceledSales: Int,
    val totalRevenue: Double
)

// Enhanced Components
@Composable
private fun EnhancedSalesHeader(
    currentTab: SalesTab,
    onTabSelected: (SalesTab) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: String?,
    onStatusFilterChange: (String?) -> Unit,
    autoRefreshEnabled: Boolean,
    onAutoRefreshToggle: (Boolean) -> Unit,
    showAdvancedFilters: Boolean,
    onToggleAdvancedFilters: (Boolean) -> Unit,
    salesStats: SalesStats,
    onRefresh: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "إدارة المبيعات",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${salesStats.totalSales} عملية بيع • ${salesStats.completedSales} مكتملة",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto-refresh toggle
                if (currentTab == SalesTab.SALES_HISTORY) {
                    IconButton(
                        onClick = { onAutoRefreshToggle(!autoRefreshEnabled) }
                    ) {
                        Icon(
                            if (autoRefreshEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (autoRefreshEnabled) "إيقاف التحديث التلقائي" else "تشغيل التحديث التلقائي",
                            tint = if (autoRefreshEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Refresh button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "تحديث",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Sales Statistics Cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                StatCard(
                    title = "إجمالي المبيعات",
                    value = salesStats.totalSales.toString(),
                    icon = Icons.Default.ShoppingCart,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                StatCard(
                    title = "قيد الانتظار",
                    value = salesStats.pendingSales.toString(),
                    icon = Icons.Default.Schedule,
                    color = AppTheme.colors.warning
                )
            }
            item {
                StatCard(
                    title = "مكتملة",
                    value = salesStats.completedSales.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = AppTheme.colors.success
                )
            }
            item {
                StatCard(
                    title = "إجمالي الإيرادات",
                    value = NumberFormat.getCurrencyInstance(Locale("ar", "SA")).format(salesStats.totalRevenue),
                    icon = Icons.Default.AttachMoney,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            item {
                StatCard(
                    title = "مدفوعات ملغية",
                    value = salesStats.canceledSales.toString(),
                    icon = Icons.Default.Cancel,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SalesTab.values().forEach { tab ->
                SalesTabButton(
                    tab = tab,
                    isSelected = currentTab == tab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }

        // Search bar and filters (only for sales history)
        if (currentTab == SalesTab.SALES_HISTORY) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("البحث في المبيعات...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Status filter dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = statusFilter ?: "جميع الحالات",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(150.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf(null, "PENDING", "COMPLETED", "CANCELLED").forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (status) {
                                            null -> "جميع الحالات"
                                            "PENDING" -> "قيد الانتظار"
                                            "COMPLETED" -> "مكتملة"
                                            "CANCELLED" -> "ملغية"
                                            else -> status
                                        }
                                    )
                                },
                                onClick = {
                                    onStatusFilterChange(status)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Advanced filters toggle
                IconButton(
                    onClick = { onToggleAdvancedFilters(!showAdvancedFilters) }
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "فلاتر متقدمة",
                        tint = if (showAdvancedFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isHovered)
                    color.copy(alpha = 0.15f)
                else
                    color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* No action for stats cards */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
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

@Composable
private fun SalesTabButton(
    tab: SalesTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                tab.icon,
                contentDescription = null,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = tab.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EnhancedErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إعادة المحاولة")
                }
            }
        }
    }
}

@Composable
private fun EnhancedLoadingIndicator(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EnhancedNewSaleContent(
    selectedProducts: List<SaleItemDTO>,
    selectedCustomer: CustomerDTO?,
    selectedPaymentMethod: String,
    cartTotal: Double,
    cartSubtotal: Double,
    cartTax: Double,
    promotionDiscount: Double,
    appliedPromotion: PromotionDTO?,
    promotionCode: String,
    isValidatingPromotion: Boolean,
    promotionError: String?,
    isProcessingSale: Boolean,
    currencyFormatter: NumberFormat,
    availableProducts: List<ProductDTO>,
    availableCustomers: List<CustomerDTO>,
    onShowProductSelection: () -> Unit,
    onShowCustomerSelection: () -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onQuantityChange: (Long, Int) -> Unit,
    onRemoveFromCart: (Long) -> Unit,
    onPromotionCodeChange: (String) -> Unit,
    onApplyPromotion: (String) -> Unit,
    onClearPromotion: () -> Unit,
    onCreateSale: () -> Unit,
    onAddToCartAnimation: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left side - Product selection and cart
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add products button
            AddProductsButton(
                onClick = onShowProductSelection,
                productCount = availableProducts.size
            )

            // Shopping cart
            ShoppingCartSection(
                selectedProducts = selectedProducts,
                currencyFormatter = currencyFormatter,
                onQuantityChange = onQuantityChange,
                onRemoveFromCart = onRemoveFromCart
            )
        }

        // Right side - Customer, payment, and checkout
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer selection
            CustomerSelectionSection(
                selectedCustomer = selectedCustomer,
                onShowCustomerSelection = onShowCustomerSelection,
                customerCount = availableCustomers.size
            )

            // Payment method selection
            PaymentMethodSection(
                selectedPaymentMethod = selectedPaymentMethod,
                onPaymentMethodChange = onPaymentMethodChange
            )

            // Order summary and checkout
            CheckoutSection(
                cartSubtotal = cartSubtotal,
                cartTax = cartTax,
                cartTotal = cartTotal,
                promotionDiscount = promotionDiscount,
                appliedPromotion = appliedPromotion,
                promotionCode = promotionCode,
                isValidatingPromotion = isValidatingPromotion,
                promotionError = promotionError,
                isProcessingSale = isProcessingSale,
                canCheckout = selectedProducts.isNotEmpty() && selectedCustomer != null,
                currencyFormatter = currencyFormatter,
                selectedCustomer = selectedCustomer,
                onPromotionCodeChange = onPromotionCodeChange,
                onApplyPromotion = onApplyPromotion,
                onClearPromotion = onClearPromotion,
                onCreateSale = onCreateSale
            )
        }
    }
}

@Composable
private fun AddProductsButton(
    onClick: () -> Unit,
    productCount: Int
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = 2.dp,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "إضافة منتجات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "اختر من $productCount منتج متاح",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ShoppingCartSection(
    selectedProducts: List<SaleItemDTO>,
    currencyFormatter: NumberFormat,
    onQuantityChange: (Long, Int) -> Unit,
    onRemoveFromCart: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "سلة التسوق",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "(${selectedProducts.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectedProducts.isEmpty()) {
                EmptyCartMessage()
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(selectedProducts) { item ->
                        CartItemCard(
                            item = item,
                            currencyFormatter = currencyFormatter,
                            onQuantityChange = { newQuantity ->
                                onQuantityChange(item.productId, newQuantity)
                            },
                            onRemove = {
                                onRemoveFromCart(item.productId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCartMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.ShoppingCartCheckout,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = "السلة فارغة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "اختر المنتجات لإضافتها إلى السلة",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CartItemCard(
    item: SaleItemDTO,
    currencyFormatter: NumberFormat,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    var isRemoving by remember { mutableStateOf(false) }

    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = if (isHovered)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName ?: "منتج غير معروف",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currencyFormatter.format(item.unitPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        isRemoving = true
                        onRemove()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إزالة",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Quantity controls and total
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
                        onClick = { if (item.quantity > 1) onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "تقليل",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.widthIn(min = 40.dp)
                    ) {
                        Text(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "زيادة",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Total price
                Text(
                    text = currencyFormatter.format(item.totalPrice ?: (item.unitPrice * item.quantity)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CustomerSelectionSection(
    selectedCustomer: CustomerDTO?,
    onShowCustomerSelection: () -> Unit,
    customerCount: Int
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = when {
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                },
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
            ) { onShowCustomerSelection() }
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
                    } ?: run {
                        Text(
                            text = "اختر من $customerCount عميل",
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
private fun PaymentMethodSection(
    selectedPaymentMethod: String,
    onPaymentMethodChange: (String) -> Unit
) {
    val paymentMethods = listOf("CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER")
    val lazyRowState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Calculate if arrows should be shown
    val canScrollLeft by remember {
        derivedStateOf {
            lazyRowState.firstVisibleItemIndex > 0 || lazyRowState.firstVisibleItemScrollOffset > 0
        }
    }

    val canScrollRight by remember {
        derivedStateOf {
            lazyRowState.layoutInfo.visibleItemsInfo.lastOrNull()?.let { lastItem ->
                lastItem.index < paymentMethods.size - 1 ||
                lastItem.offset + lastItem.size > lazyRowState.layoutInfo.viewportEndOffset
            } ?: true
        }
    }

    Card(
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
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with title and navigation arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طريقة الدفع",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Navigation arrows
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Right arrow (positioned on the left, scrolls RIGHT)
                    NavigationArrowButton(
                        icon = Icons.Filled.KeyboardArrowRight,
                        enabled = canScrollRight,
                        onClick = {
                            coroutineScope.launch {
                                val currentIndex = lazyRowState.firstVisibleItemIndex
                                if (currentIndex < paymentMethods.size - 1) {
                                    lazyRowState.animateScrollToItem(minOf(paymentMethods.size - 1, currentIndex + 1))
                                }
                            }
                        }
                    )

                    // Left arrow (positioned on the right, scrolls LEFT)
                    NavigationArrowButton(
                        icon = Icons.Filled.KeyboardArrowLeft,
                        enabled = canScrollLeft,
                        onClick = {
                            coroutineScope.launch {
                                val currentIndex = lazyRowState.firstVisibleItemIndex
                                if (currentIndex > 0) {
                                    lazyRowState.animateScrollToItem(maxOf(0, currentIndex - 1))
                                }
                            }
                        }
                    )
                }
            }

            LazyRow(
                state = lazyRowState,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(paymentMethods) { method ->
                    PaymentMethodCard(
                        method = method,
                        isSelected = selectedPaymentMethod == method,
                        onClick = { onPaymentMethodChange(method) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationArrowButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = when {
                    !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                isHovered -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PaymentMethodCard(
    method: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (method) {
        "CASH" -> Icons.Outlined.Payments
        "CREDIT_CARD" -> Icons.Outlined.CreditCard
        "DEBIT_CARD" -> Icons.Outlined.CreditCard
        "BANK_TRANSFER" -> Icons.Outlined.AccountBalance
        else -> Icons.Outlined.Payment
    }

    val displayName = when (method) {
        "CASH" -> "نقدي"
        "CREDIT_CARD" -> "بطاقة ائتمان"
        "DEBIT_CARD" -> "بطاقة خصم"
        "BANK_TRANSFER" -> "تحويل بنكي"
        else -> method
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
                text = displayName,
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

@Composable
private fun CheckoutSection(
    cartSubtotal: Double,
    cartTax: Double,
    cartTotal: Double,
    promotionDiscount: Double,
    appliedPromotion: PromotionDTO?,
    promotionCode: String,
    isValidatingPromotion: Boolean,
    promotionError: String?,
    isProcessingSale: Boolean,
    canCheckout: Boolean,
    currencyFormatter: NumberFormat,
    selectedCustomer: CustomerDTO?,
    onPromotionCodeChange: (String) -> Unit,
    onApplyPromotion: (String) -> Unit,
    onClearPromotion: () -> Unit,
    onCreateSale: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Promotion Code Input
        PromotionCodeSection(
            promotionCode = promotionCode,
            appliedPromotion = appliedPromotion,
            isValidatingPromotion = isValidatingPromotion,
            promotionError = promotionError,
            onPromotionCodeChange = onPromotionCodeChange,
            onApplyPromotion = onApplyPromotion,
            onClearPromotion = onClearPromotion
        )

        // Totals card
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
                TotalRow("المجموع الفرعي", currencyFormatter.format(cartSubtotal))

                // Show promotion discount if applied
                if (promotionDiscount > 0) {
                    TotalRow(
                        "خصم العرض (${appliedPromotion?.name ?: ""})",
                        "-${currencyFormatter.format(promotionDiscount)}",
                        textColor = MaterialTheme.colorScheme.error
                    )
                }

                TotalRow("الضريبة (15%)", currencyFormatter.format(cartTax))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
                TotalRow(
                    "الإجمالي",
                    currencyFormatter.format(cartTotal),
                    isTotal = true
                )
            }
        }

        // Checkout Button with enhanced styling
        Button(
            onClick = {
                println("🔍 CheckoutSection - Button clicked!")
                println("🔍 Can checkout: $canCheckout")
                println("🔍 Is processing: $isProcessingSale")
                onCreateSale()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = canCheckout && !isProcessingSale,
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

        // Enhanced validation messages
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (selectedCustomer == null) {
                Text(
                    text = "⚠️ يرجى اختيار عميل لإتمام البيع",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.warning,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!canCheckout && selectedCustomer != null) {
                Text(
                    text = "يرجى إضافة منتجات إلى السلة لإتمام البيع",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PromotionCodeSection(
    promotionCode: String,
    appliedPromotion: PromotionDTO?,
    isValidatingPromotion: Boolean,
    promotionError: String?,
    onPromotionCodeChange: (String) -> Unit,
    onApplyPromotion: (String) -> Unit,
    onClearPromotion: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "كود العرض",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (appliedPromotion != null) {
                // Show applied promotion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appliedPromotion.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "كود: ${appliedPromotion.couponCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onClearPromotion,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إزالة العرض",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // Show promotion code input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = promotionCode,
                        onValueChange = onPromotionCodeChange,
                        label = { Text("أدخل كود العرض") },
                        placeholder = { Text("مثال: SUMMER2024") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        singleLine = true,
                        isError = promotionError != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Button(
                        onClick = { onApplyPromotion(promotionCode) },
                        enabled = promotionCode.isNotBlank() && !isValidatingPromotion,
                        modifier = Modifier
                            .height(64.dp)
                            .defaultMinSize(minHeight = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (isValidatingPromotion) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("تطبيق")
                        }
                    }
                }

                // Show error message if any
                promotionError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalRow(
    label: String,
    value: String,
    isTotal: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = if (isTotal) MaterialTheme.colorScheme.onSurface else textColor
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isTotal) MaterialTheme.colorScheme.primary else textColor
        )
    }
}

@Composable
private fun EnhancedSalesHistoryContent(
    sales: List<SaleDTO>,
    currencyFormatter: NumberFormat,
    statusFilter: String?,
    showAdvancedFilters: Boolean,
    onSaleClick: (SaleDTO) -> Unit,
    onCompleteSale: (Long) -> Unit,
    onCancelSale: (Long) -> Unit,
    onLoadMore: () -> Unit
) {
    if (sales.isEmpty()) {
        EmptySalesMessage()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(sales) { sale ->
                SaleHistoryCard(
                    sale = sale,
                    currencyFormatter = currencyFormatter,
                    onClick = { onSaleClick(sale) },
                    onComplete = { onCompleteSale(sale.id!!) },
                    onCancel = { onCancelSale(sale.id!!) }
                )
            }

            // Load more button
            if (sales.size >= 20) {
                item {
                    Button(
                        onClick = onLoadMore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("تحميل المزيد")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySalesMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
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
                    Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Text(
            text = "لا توجد مبيعات",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "ابدأ ببيع جديد لرؤية المبيعات هنا",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SaleHistoryCard(
    sale: SaleDTO,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with sale info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "فاتورة #${sale.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    sale.customerName?.let { customerName ->
                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    sale.saleDate?.let { date ->
                        Text(
                            text = date.substring(0, 10), // Show only date part
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status badge
                StatusBadge(status = sale.status ?: "PENDING")
            }

            // Sale details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "المبلغ الإجمالي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(sale.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${sale.items.size} منتج",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons (only for pending sales)
            if (sale.status == "PENDING") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.success
                        )
                    ) {
                        Text("إكمال", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (backgroundColor, textColor, text) = when (status) {
        "COMPLETED" -> Triple(
            AppTheme.colors.success.copy(alpha = 0.15f),
            AppTheme.colors.success,
            "مكتملة"
        )
        "PENDING" -> Triple(
            AppTheme.colors.warning.copy(alpha = 0.15f),
            AppTheme.colors.warning,
            "قيد الانتظار"
        )
        "CANCELLED" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "ملغية"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            status
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// Enhanced Dialog Components
@Composable
private fun EnhancedProductSelectionDialog(
    products: List<ProductDTO>,
    onProductSelected: (ProductDTO, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = products.filter { product ->
        product.name.contains(searchQuery, ignoreCase = true) ||
        product.category?.contains(searchQuery, ignoreCase = true) == true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f),
        title = {
            Column {
                Text(
                    text = "اختيار المنتجات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("البحث في المنتجات...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        text = {
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "لا توجد منتجات",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductSelectionItem(
                            product = product,
                            onSelect = { quantity ->
                                onProductSelected(product, quantity)
                            }
                        )
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
private fun ProductSelectionItem(
    product: ProductDTO,
    onSelect: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    product.category?.let { category ->
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("ar", "SA")).format(product.price),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Stock indicator
                product.stockQuantity?.let { stock ->
                    Text(
                        text = "متوفر: $stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (stock > 0) AppTheme.colors.success else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Quantity and add controls
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
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "تقليل",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.widthIn(min = 40.dp)
                    ) {
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    IconButton(
                        onClick = {
                            val maxStock = product.stockQuantity ?: Int.MAX_VALUE
                            if (quantity < maxStock) quantity++
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "زيادة",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Add button
                Button(
                    onClick = { onSelect(quantity) },
                    modifier = Modifier.height(36.dp),
                    enabled = (product.stockQuantity ?: 0) >= quantity
                ) {
                    Text("إضافة")
                }
            }
        }
    }
}

@Composable
private fun EnhancedCustomerSelectionDialog(
    customers: List<CustomerDTO>,
    onCustomerSelected: (CustomerDTO?) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = customers.filter { customer ->
        customer.name.contains(searchQuery, ignoreCase = true) ||
        customer.email?.contains(searchQuery, ignoreCase = true) == true ||
        customer.phone?.contains(searchQuery, ignoreCase = true) == true
    }

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
                    singleLine = true
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
    customer: CustomerDTO,
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
                    customer.phone?.let { phone ->
                        Icon(
                            Icons.Outlined.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    customer.email?.let { email ->
                        if (customer.phone != null) {
                            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            Icons.Outlined.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = email,
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

// Enhanced Sale Success Dialog with PDF functionality
@Composable
private fun SaleSuccessDialogImproved(
    total: Double,
    currencyFormatter: NumberFormat,
    saleData: SaleDTO?,
    selectedCustomer: CustomerDTO?,
    selectedPaymentMethod: String,
    selectedProducts: List<SaleItemDTO>,
    onDismiss: () -> Unit,
    onViewSale: (() -> Unit)? = null,
    onCreateAnother: (() -> Unit)? = null
) {
    var showPdfViewer by remember { mutableStateOf(false) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Debug logging for button state
    LaunchedEffect(saleData, isGeneratingPdf, total) {
        println("🔍 SaleSuccessDialog - State Debug:")
        println("🔍 total parameter: $total")
        println("🔍 saleData: ${saleData?.id}")
        println("🔍 saleData.totalAmount: ${saleData?.totalAmount}")
        println("🔍 isGeneratingPdf: $isGeneratingPdf")
        println("🔍 Button enabled: ${!isGeneratingPdf && saleData != null}")
        println("🔍 selectedProducts count: ${selectedProducts.size}")
        println("🔍 selectedCustomer: ${selectedCustomer?.name}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            // Done button
            val doneInteractionSource = remember { MutableInteractionSource() }
            val isDoneHovered by doneInteractionSource.collectIsHoveredAsState()

            Box(
                modifier = Modifier
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDoneHovered)
                            AppTheme.colors.success.copy(alpha = 0.9f)
                        else
                            AppTheme.colors.success
                    ),
                    shape = RoundedCornerShape(12.dp),
                    interactionSource = doneInteractionSource
                ) {
                    Text(
                        "تم",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        dismissButton = {
            // Action buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Generate Invoice Button
                val generateInteractionSource = remember { MutableInteractionSource() }
                val isGenerateHovered by generateInteractionSource.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Button(
                        onClick = {
                            println("🔍 Generate Invoice Button Clicked!")
                            println("🔍 saleData: ${saleData?.id}")
                            println("🔍 saleData != null: ${saleData != null}")
                            println("🔍 isGeneratingPdf: $isGeneratingPdf")

                            if (saleData != null) {
                                coroutineScope.launch {
                                    isGeneratingPdf = true
                                    showError = null
                                    try {
                                        println("🔍 Starting PDF generation...")
                                        val receiptsDir = CanvasPdfReceiptService.getReceiptsDirectory()
                                        val fileName = CanvasPdfReceiptService.generateReceiptFilename((saleData.id ?: 0L).toInt())
                                        val pdfFile = File(receiptsDir, fileName)

                                        // Convert SaleDTO to Sale for PDF generation
                                        val sale = convertSaleDTOToSale(saleData, selectedCustomer, selectedProducts, selectedPaymentMethod)
                                        println("🔍 Converted sale data, generating PDF...")
                                        val success = CanvasPdfReceiptService.generateReceipt(sale, pdfFile, useArabicIndic = false)
                                        if (success) {
                                            println("🔍 PDF generated successfully!")
                                            generatedPdfFile = pdfFile
                                            showPdfViewer = true
                                        } else {
                                            println("🔍 PDF generation failed!")
                                            showError = "فشل في إنشاء الفاتورة"
                                        }
                                    } catch (e: Exception) {
                                        println("🔍 PDF generation exception: ${e.message}")
                                        showError = "خطأ في إنشاء الفاتورة: ${e.message}"
                                        e.printStackTrace()
                                    } finally {
                                        isGeneratingPdf = false
                                    }
                                }
                            } else {
                                println("🔍 saleData is null!")
                                showError = "بيانات البيع غير متوفرة"
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGenerateHovered)
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                            else
                                MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        interactionSource = generateInteractionSource,
                        enabled = !isGeneratingPdf && saleData != null
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                                Text(
                                    "إنشاء فاتورة",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Print Invoice Button
                val printInteractionSource = remember { MutableInteractionSource() }
                val isPrintHovered by printInteractionSource.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Button(
                        onClick = {
                            println("🔍 Print Invoice Button Clicked!")
                            println("🔍 saleData: ${saleData?.id}")

                            if (saleData != null) {
                                coroutineScope.launch {
                                    isGeneratingPdf = true
                                    showError = null
                                    try {
                                        println("🔍 Starting PDF generation for printing...")
                                        val receiptsDir = CanvasPdfReceiptService.getReceiptsDirectory()
                                        val fileName = CanvasPdfReceiptService.generateReceiptFilename((saleData.id ?: 0L).toInt())
                                        val pdfFile = File(receiptsDir, fileName)

                                        // Convert SaleDTO to Sale for PDF generation
                                        val sale = convertSaleDTOToSale(saleData, selectedCustomer, selectedProducts, selectedPaymentMethod)
                                        val success = CanvasPdfReceiptService.generateReceipt(sale, pdfFile, useArabicIndic = false)
                                        if (success) {
                                            println("🔍 PDF generated, attempting to print...")
                                            val printResult = FileDialogUtils.printFile(pdfFile)
                                            when (printResult) {
                                                is FileDialogUtils.PrintResult.Success -> {
                                                    println("🔍 Print successful!")
                                                }
                                                is FileDialogUtils.PrintResult.NoAssociatedApp,
                                                is FileDialogUtils.PrintResult.NotSupported,
                                                is FileDialogUtils.PrintResult.Error -> {
                                                    println("🔍 Print failed, opening file manually...")
                                                    FileDialogUtils.openWithSystemDefault(pdfFile)
                                                }
                                            }
                                        } else {
                                            println("🔍 PDF generation failed!")
                                            showError = "فشل في إنشاء الفاتورة"
                                        }
                                    } catch (e: Exception) {
                                        println("🔍 Print exception: ${e.message}")
                                        showError = "خطأ في طباعة الفاتورة: ${e.message}"
                                        e.printStackTrace()
                                    } finally {
                                        isGeneratingPdf = false
                                    }
                                }
                            } else {
                                println("🔍 saleData is null for print!")
                                showError = "بيانات البيع غير متوفرة"
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPrintHovered)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        interactionSource = printInteractionSource,
                        enabled = !isGeneratingPdf && saleData != null
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Print,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.White
                                )
                                Text(
                                    "طباعة فاتورة",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // View Sale Button (if callback provided)
                onViewSale?.let { viewCallback ->
                    val viewInteractionSource = remember { MutableInteractionSource() }
                    val isViewHovered by viewInteractionSource.collectIsHoveredAsState()

                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        OutlinedButton(
                            onClick = viewCallback,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp),
                            interactionSource = viewInteractionSource,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isViewHovered)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                else
                                    MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isViewHovered)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                "عرض التفاصيل",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Create Another Button (if callback provided)
                onCreateAnother?.let { createCallback ->
                    val createInteractionSource = remember { MutableInteractionSource() }
                    val isCreateHovered by createInteractionSource.collectIsHoveredAsState()

                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        OutlinedButton(
                            onClick = createCallback,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp),
                            interactionSource = createInteractionSource,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isCreateHovered)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                else
                                    MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isCreateHovered)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                "بيع آخر",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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

                // Show sale details if available
                saleData?.let { sale ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "رقم الفاتورة:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "#${sale.id}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "طريقة الدفع:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = when (selectedPaymentMethod) {
                                        "CASH" -> "نقدي"
                                        "CREDIT_CARD" -> "بطاقة ائتمان"
                                        "DEBIT_CARD" -> "بطاقة خصم"
                                        "BANK_TRANSFER" -> "تحويل بنكي"
                                        else -> selectedPaymentMethod
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            selectedCustomer?.let { customer ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "العميل:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Error message if any
                showError?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "تنبيه",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )

    // PDF Viewer Dialog
    generatedPdfFile?.let { pdfFile ->
        if (showPdfViewer) {
            ui.screens.PdfViewerDialog(
                pdfFile = pdfFile,
                onDismiss = {
                    showPdfViewer = false
                    generatedPdfFile = null
                },
                onPrint = {
                    coroutineScope.launch {
                        val printResult = FileDialogUtils.printFile(pdfFile)
                        when (printResult) {
                            is FileDialogUtils.PrintResult.Success -> {
                                // Print successful
                            }
                            is FileDialogUtils.PrintResult.NoAssociatedApp,
                            is FileDialogUtils.PrintResult.NotSupported,
                            is FileDialogUtils.PrintResult.Error -> {
                                // Fallback: open file for manual printing
                                FileDialogUtils.openWithSystemDefault(pdfFile)
                            }
                        }
                    }
                },
                onDownload = {
                    // Use the PdfViewerDialog's internal save functionality instead
                    // This callback is not used since PdfViewerDialog handles its own save button
                }
            )
        }
    }
}

// Helper function to convert SaleDTO to Sale for PDF generation
private fun convertSaleDTOToSale(
    saleDTO: SaleDTO,
    customerDTO: CustomerDTO?,
    selectedProducts: List<SaleItemDTO>,
    paymentMethod: String
): Sale {
    val customer = customerDTO?.let { dto ->
        Customer(
            id = dto.id?.toInt() ?: 0,
            name = dto.name,
            phone = dto.phone ?: "",
            email = dto.email ?: "",
            address = dto.address ?: "",
            totalPurchases = 0.0
        )
    }

    val saleItems = selectedProducts.map { itemDTO ->
        SaleItem(
            product = Product(
                id = itemDTO.productId.toInt(),
                name = itemDTO.productName ?: "منتج غير معروف",
                price = itemDTO.unitPrice,
                cost = itemDTO.unitPrice * 0.7, // Assume 30% profit margin
                category = "",
                stock = 0,
                barcode = null,
                description = null,
                discountedPrice = null
            ),
            quantity = itemDTO.quantity,
            unitPrice = itemDTO.unitPrice
        )
    }

    val paymentMethodEnum = when (paymentMethod) {
        "CASH" -> PaymentMethod.CASH
        "CREDIT_CARD" -> PaymentMethod.CARD
        "DEBIT_CARD" -> PaymentMethod.CARD
        "BANK_TRANSFER" -> PaymentMethod.BANK_TRANSFER
        else -> PaymentMethod.CASH
    }

    return Sale(
        id = saleDTO.id?.toInt() ?: 0,
        date = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
        customer = customer,
        items = saleItems,
        tax = saleDTO.taxAmount ?: 0.0,
        paymentMethod = paymentMethodEnum
    )
}

@Composable
private fun EnhancedSaleDetailsDialog(
    sale: SaleDTO,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onCompleteSale: ((Long) -> Unit)? = null,
    onCancelSale: ((Long) -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.9f),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تفاصيل الفاتورة #${sale.id}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = sale.status ?: "PENDING")
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sale info
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sale.customerName?.let { customerName ->
                                DetailRow("العميل", customerName)
                            }
                            sale.saleDate?.let { date ->
                                DetailRow("التاريخ", date.substring(0, 10))
                            }
                            sale.paymentMethod?.let { method ->
                                DetailRow("طريقة الدفع", when (method) {
                                    "CASH" -> "نقدي"
                                    "CREDIT_CARD" -> "بطاقة ائتمان"
                                    "DEBIT_CARD" -> "بطاقة خصم"
                                    "BANK_TRANSFER" -> "تحويل بنكي"
                                    else -> method
                                })
                            }
                        }
                    }
                }

                // Items
                item {
                    Text(
                        text = "المنتجات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(sale.items) { item ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.productName ?: "منتج غير معروف",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "الكمية: ${item.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = currencyFormatter.format(item.totalPrice ?: (item.unitPrice * item.quantity)),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Totals
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sale.subtotal?.let { subtotal ->
                                DetailRow("المجموع الفرعي", currencyFormatter.format(subtotal))
                            }
                            sale.taxAmount?.let { tax ->
                                DetailRow("الضريبة", currencyFormatter.format(tax))
                            }
                            HorizontalDivider()
                            DetailRow(
                                "الإجمالي",
                                currencyFormatter.format(sale.totalAmount),
                                isTotal = true
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
                Text("إغلاق", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            if (sale.status == "PENDING" && (onCompleteSale != null || onCancelSale != null)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    onCompleteSale?.let { completeCallback ->
                        Button(
                            onClick = { sale.id?.let { completeCallback(it) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.success
                            )
                        ) {
                            Text("إكمال", color = Color.White)
                        }
                    }

                    onCancelSale?.let { cancelCallback ->
                        OutlinedButton(
                            onClick = { sale.id?.let { cancelCallback(it) } },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Text("إلغاء")
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun DetailRow(
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
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Medium,
            color = if (isTotal) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
