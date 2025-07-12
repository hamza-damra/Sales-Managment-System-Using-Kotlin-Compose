@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import data.*
import ui.components.*
import ui.components.EnhancedFilterDropdown
import ui.components.RTLProvider
import ui.components.RTLRow
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.viewmodels.ExportResult
import ui.viewmodels.ImportResult
import ui.viewmodels.ParseResult
import data.api.ProductDTO
import ui.utils.ColorUtils
import java.text.NumberFormat
import java.util.*
import utils.CurrencyUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(productViewModel: ui.viewmodels.ProductViewModel) {
    RTLProvider {
        val uiState by productViewModel.uiState.collectAsState()
        val searchQuery by productViewModel.searchQuery.collectAsState()
        val selectedCategory by productViewModel.selectedCategory.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        // Enhanced state management
        var showAddProductDialog by remember { mutableStateOf(false) }
        var editingProduct by remember { mutableStateOf<Product?>(null) }
        var selectedStatus by remember { mutableStateOf("الكل") }
        var showProductDetails by remember { mutableStateOf(false) }
        var selectedProduct by remember { mutableStateOf<Product?>(null) }
        var sortBy by remember { mutableStateOf("name") }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var productToDelete by remember { mutableStateOf<Product?>(null) }

        // Export/Import UI state
        var showExportDialog by remember { mutableStateOf(false) }
        var showImportDialog by remember { mutableStateOf(false) }
        var showImportPreviewDialog by remember { mutableStateOf(false) }
        var isExporting by remember { mutableStateOf(false) }
        var isImporting by remember { mutableStateOf(false) }
        var isParsing by remember { mutableStateOf(false) }
        var parsedProducts by remember { mutableStateOf<List<ProductDTO>>(emptyList()) }
        var parseWarnings by remember { mutableStateOf<List<String>>(emptyList()) }

        // Snackbar state for success messages
        val snackbarHostState = remember { SnackbarHostState() }

        // Currency formatter using configurable currency system
        val currencyFormatter = remember {
            CurrencyUtils.getCurrencyFormatter()
        }

        // Load data when screen is first displayed
        LaunchedEffect(Unit) {
            if (!uiState.hasData && !uiState.isLoading) {
                productViewModel.loadProducts()
                productViewModel.loadActiveCategories()
            }
        }

        // Handle deletion success message
        LaunchedEffect(uiState.deletionSuccess) {
            if (uiState.deletionSuccess) {
                snackbarHostState.showSnackbar(
                    message = "تم حذف المنتج بنجاح",
                    duration = SnackbarDuration.Short
                )
                // Clear the deletion success state after showing the message
                productViewModel.clearDeletionSuccess()
            }
        }

        // Get categories from ViewModel
        val categories = remember(uiState.products) {
            productViewModel.getCategories()
        }
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Products List
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
                            .padding(24.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "إدارة المنتجات",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = { showAddProductDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("إضافة منتج جديد")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Search and Filter Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Search Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { productViewModel.searchProducts(it) },
                                label = { Text("البحث في المنتجات") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            )

                            // Category Filter
                            EnhancedFilterDropdown(
                                label = "الفئة",
                                value = selectedCategory,
                                options = listOf("الكل") + categories,
                                onValueChange = { productViewModel.filterByCategory(it) },
                                modifier = Modifier.weight(0.7f)
                            )

                            // Status Filter
                            EnhancedFilterDropdown(
                                label = "الحالة",
                                value = selectedStatus,
                                options = listOf("الكل", "نشط", "غير نشط", "مخزون منخفض"),
                                onValueChange = { selectedStatus = it },
                                modifier = Modifier.weight(0.7f)
                            )
                        }

                        // Sort and Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sort Dropdown
                            EnhancedFilterDropdown(
                                label = "ترتيب حسب",
                                value = when(sortBy) {
                                    "name" -> "الاسم"
                                    "price" -> "السعر"
                                    "stock" -> "المخزون"
                                    "category" -> "الفئة"
                                    else -> "الاسم"
                                },
                                options = listOf("الاسم", "السعر", "المخزون", "الفئة"),
                                onValueChange = {
                                    sortBy = when(it) {
                                        "الاسم" -> "name"
                                        "السعر" -> "price"
                                        "المخزون" -> "stock"
                                        "الفئة" -> "category"
                                        else -> "name"
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Enhanced Export Button with complete hover coverage
                            val exportInteractionSource = remember { MutableInteractionSource() }
                            val isExportHovered by exportInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = if (isExportHovered && !isExporting)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else
                                            MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (isExportHovered && !isExporting) 1.5.dp else 1.dp,
                                        color = if (isExportHovered && !isExporting)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = exportInteractionSource,
                                        indication = null,
                                        enabled = !isExporting
                                    ) { if (!isExporting) { showExportDialog = true } },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    if (isExporting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.FileDownload,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = if (isExportHovered)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        "تصدير",
                                        color = if (isExporting)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        else if (isExportHovered)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            // Enhanced Import Button with complete hover coverage
                            val importInteractionSource = remember { MutableInteractionSource() }
                            val isImportHovered by importInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = if (isImportHovered && !isImporting)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else
                                            MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (isImportHovered && !isImporting) 1.5.dp else 1.dp,
                                        color = if (isImportHovered && !isImporting)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = importInteractionSource,
                                        indication = null,
                                        enabled = !isImporting
                                    ) { if (!isImporting) { showImportDialog = true } },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    if (isImporting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.FileUpload,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = if (isImportHovered)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        "استيراد",
                                        color = if (isImporting)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        else if (isImportHovered)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Products List
                        // Filter products based on current filters
                        val filteredProducts = remember(uiState.products, searchQuery, selectedCategory, selectedStatus, sortBy) {
                            var filtered = uiState.products

                            // Apply search filter
                            if (searchQuery.isNotBlank()) {
                                filtered = filtered.filter {
                                    it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.barcode?.contains(searchQuery, ignoreCase = true) == true
                                }
                            }

                            // Apply category filter
                            if (selectedCategory != "الكل") {
                                filtered = filtered.filter { it.category == selectedCategory }
                            }

                            // Apply status filter
                            when (selectedStatus) {
                                "نشط" -> filtered = filtered.filter { it.productStatus == "ACTIVE" }
                                "غير نشط" -> filtered = filtered.filter { it.productStatus == "INACTIVE" }
                                "مخزون منخفض" -> filtered = filtered.filter { it.stock <= 10 }
                            }

                            // Apply sorting
                            when (sortBy) {
                                "name" -> filtered.sortedBy { it.name }
                                "price" -> filtered.sortedBy { it.price }
                                "stock" -> filtered.sortedBy { it.stock }
                                "category" -> filtered.sortedBy { it.category }
                                else -> filtered
                            }
                        }

                        // Products Content with State Management
                        when {
                            uiState.isLoading -> {
                                // Enhanced Loading state
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 3.dp
                                        )
                                        Text(
                                            text = "جاري تحميل المنتجات...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            uiState.error != null -> {
                                // Enhanced Error state
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "حدث خطأ في تحميل المنتجات",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = uiState.error ?: "حدث خطأ غير متوقع",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Button(
                                            onClick = { productViewModel.loadProducts() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("إعادة المحاولة")
                                        }
                                    }
                                }
                            }
                            filteredProducts.isEmpty() -> {
                                // Enhanced Empty state
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Inventory,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Text(
                                            text = if (searchQuery.isNotBlank() || selectedCategory != "الكل" || selectedStatus != "الكل")
                                                "لا توجد منتجات تطابق المرشحات المحددة"
                                            else "لا توجد منتجات متاحة",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        if (searchQuery.isBlank() && selectedCategory == "الكل" && selectedStatus == "الكل") {
                                            Button(
                                                onClick = { showAddProductDialog = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("إضافة أول منتج")
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                // Enhanced Products List
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredProducts) { product ->
                                        EnhancedProductCard(
                                            product = product,
                                            currencyFormatter = currencyFormatter,
                                            isSelected = selectedProduct?.id == product.id,
                                            onEdit = { editingProduct = product },
                                            onDelete = {
                                                productToDelete = product
                                                showDeleteConfirmation = true
                                            },
                                            onClick = {
                                                selectedProduct = product
                                                showProductDetails = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Panel - Product Details (when selected)
                AnimatedVisibility(
                    visible = showProductDetails && selectedProduct != null,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut()
                ) {
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
                        selectedProduct?.let { product ->
                            EnhancedProductDetailsPanel(
                                product = product,
                                currencyFormatter = currencyFormatter,
                                onEdit = {
                                    editingProduct = product
                                    showProductDetails = false
                                },
                                onDelete = {
                                    productToDelete = product
                                    showDeleteConfirmation = true
                                    showProductDetails = false
                                },
                                onClose = {
                                    showProductDetails = false
                                    selectedProduct = null
                                }
                            )
                        }
                    }
                }
            }

            // Snackbar
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Dialogs
        // Dialogs
        if (showAddProductDialog) {
            ComprehensiveProductDialog(
                product = null,
                productViewModel = productViewModel,
                onDismiss = { showAddProductDialog = false },
                onSave = { productDTO ->
                    productViewModel.createProduct(productDTO)
                    showAddProductDialog = false
                }
            )
        }

        if (editingProduct != null) {
            ComprehensiveProductDialog(
                product = editingProduct,
                productViewModel = productViewModel,
                onDismiss = { editingProduct = null },
                onSave = { productDTO ->
                    productViewModel.updateProduct(editingProduct!!.id.toLong(), productDTO)
                    editingProduct = null
                }
            )
        }

        if (showDeleteConfirmation && productToDelete != null) {
            AlertDialog(
                onDismissRequest = {}, // Disabled click-outside-to-dismiss
                title = { Text("تأكيد الحذف") },
                text = { Text("هل أنت متأكد من حذف المنتج \"${productToDelete!!.name}\"؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            productViewModel.deleteProduct(productToDelete!!.id.toLong(), productToDelete!!.name)
                            showDeleteConfirmation = false
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirmation = false
                            productToDelete = null
                        }
                    ) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // Export/Import Dialogs
        if (showExportDialog) {
            ExportDialog(
                onDismiss = { showExportDialog = false },
                onExportExcel = {
                    showExportDialog = false
                    isExporting = true
                    coroutineScope.launch {
                        productViewModel.exportProductsToExcel().collect { result ->
                            isExporting = false
                            when (result) {
                                is ExportResult.Success -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ExportResult.Error -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ExportResult.Loading -> {
                                    // Keep loading state
                                }
                            }
                        }
                    }
                },
                onExportCsv = {
                    showExportDialog = false
                    isExporting = true
                    coroutineScope.launch {
                        productViewModel.exportProductsToCsv().collect { result ->
                            isExporting = false
                            when (result) {
                                is ExportResult.Success -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ExportResult.Error -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ExportResult.Loading -> {
                                    // Keep loading state
                                }
                            }
                        }
                    }
                },
                onExportJson = {
                    showExportDialog = false
                    isExporting = true
                    coroutineScope.launch {
                        productViewModel.exportProductsToJson().collect { result ->
                            isExporting = false
                            when (result) {
                                is ExportResult.Success -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ExportResult.Error -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ExportResult.Loading -> {
                                    // Keep loading state
                                }
                            }
                        }
                    }
                }
            )
        }

        if (showImportDialog) {
            ImportDialog(
                onDismiss = { showImportDialog = false },
                onImport = {
                    showImportDialog = false
                    isParsing = true
                    coroutineScope.launch {
                        productViewModel.parseProductsFromFile().collect { result ->
                            isParsing = false
                            when (result) {
                                is ParseResult.Success -> {
                                    parsedProducts = result.products
                                    parseWarnings = result.warnings
                                    showImportPreviewDialog = true
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ParseResult.Error -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ParseResult.Cancelled -> {
                                    // User cancelled, no message needed
                                }
                                is ParseResult.Loading -> {
                                    // Keep loading state
                                }
                            }
                        }
                    }
                }
            )
        }

        if (showImportPreviewDialog) {
            ImportPreviewDialog(
                products = parsedProducts,
                warnings = parseWarnings,
                onDismiss = {
                    showImportPreviewDialog = false
                    parsedProducts = emptyList()
                    parseWarnings = emptyList()
                },
                onConfirmUpload = {
                    showImportPreviewDialog = false
                    isImporting = true
                    coroutineScope.launch {
                        productViewModel.uploadProductsToDatabase(parsedProducts).collect { result ->
                            isImporting = false
                            parsedProducts = emptyList()
                            parseWarnings = emptyList()
                            when (result) {
                                is ImportResult.Success -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ImportResult.Error -> {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                                is ImportResult.Cancelled -> {
                                    // Should not happen in upload step
                                }
                                is ImportResult.Loading -> {
                                    // Keep loading state
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}






@Composable
private fun ModernCategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    Color.White  // Force white text for selected state
                } else {
                    MaterialTheme.colorScheme.onSurface  // Dark text for unselected
                }
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White,  // Ensure white text on selected
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            labelColor = MaterialTheme.colorScheme.onSurface  // Dark text for unselected
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) MaterialTheme.colorScheme.primary
                         else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}




@Composable
private fun ModernProductCard(
    product: Product,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
            defaultElevation = 1.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with actions
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
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

            // Price and Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencyFormatter.format(product.price),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = if (product.stock < 10) AppTheme.colors.warning.copy(alpha = 0.1f)
                           else AppTheme.colors.success.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${product.stock} قطعة",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (product.stock < 10) AppTheme.colors.warning
                               else AppTheme.colors.success,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Barcode if available
            product.barcode?.takeIf { it.isNotBlank() }?.let { barcode ->
                Text(
                    text = "الباركود: $barcode",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ModernLowStockCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.warning.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = AppTheme.colors.warning.copy(alpha = 0.3f)
        )
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
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "متبقي ${product.stock} قطعة",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.warning
                )
            }

            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = AppTheme.colors.warning,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

// Dialog Components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComprehensiveProductDialog(
    product: Product?,
    productViewModel: ui.viewmodels.ProductViewModel,
    onDismiss: () -> Unit,
    onSave: (data.api.ProductDTO) -> Unit
) {
    // Get categories from ViewModel
    val activeCategories by productViewModel.activeCategoriesForProducts.collectAsState()

    // Focus manager for keyboard navigation
    val focusManager = LocalFocusManager.current

    // Focus requesters for explicit focus management
    val priceFocusRequester = remember { FocusRequester() }
    val stockQuantityFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val skuFocusRequester = remember { FocusRequester() }
    val costPriceFocusRequester = remember { FocusRequester() }
    val brandFocusRequester = remember { FocusRequester() }
    val modelNumberFocusRequester = remember { FocusRequester() }
    val barcodeFocusRequester = remember { FocusRequester() }
    val weightFocusRequester = remember { FocusRequester() }
    val lengthFocusRequester = remember { FocusRequester() }
    val widthFocusRequester = remember { FocusRequester() }
    val heightFocusRequester = remember { FocusRequester() }
    val minStockLevelFocusRequester = remember { FocusRequester() }
    val maxStockLevelFocusRequester = remember { FocusRequester() }
    val reorderPointFocusRequester = remember { FocusRequester() }
    val reorderQuantityFocusRequester = remember { FocusRequester() }

    // Required fields
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stockQuantity by remember { mutableStateOf(product?.stock?.toString() ?: "") }

    // Optional fields - Basic Info
    var description by remember { mutableStateOf(product?.description ?: "") }
    var selectedCategory by remember { mutableStateOf<data.Category?>(
        activeCategories.find { it.name == product?.category }
    ) }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var costPrice by remember { mutableStateOf(product?.cost?.toString() ?: "") }
    var brand by remember { mutableStateOf("") }
    var modelNumber by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }

    // Optional fields - Physical Properties
    var weight by remember { mutableStateOf("") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    // Optional fields - Stock Management
    var minStockLevel by remember { mutableStateOf("") }
    var maxStockLevel by remember { mutableStateOf("") }
    var reorderPoint by remember { mutableStateOf("") }
    var reorderQuantity by remember { mutableStateOf("") }

    // Optional fields - Supplier Info
    var supplierName by remember { mutableStateOf("") }
    var supplierCode by remember { mutableStateOf("") }

    // Optional fields - Additional Info
    var warrantyPeriod by remember { mutableStateOf("") }
    var unitOfMeasure by remember { mutableStateOf("PCS") }
    var taxRate by remember { mutableStateOf("") }
    var discountPercentage by remember { mutableStateOf("") }
    var locationInWarehouse by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // UI State
    var showOptionalFields by remember { mutableStateOf(false) }
    var isFormValid by remember { mutableStateOf(false) }

    // Validate required fields
    LaunchedEffect(name, price, stockQuantity) {
        isFormValid = name.isNotBlank() &&
                     price.toDoubleOrNull() != null &&
                     stockQuantity.toIntOrNull() != null
    }

    AlertDialog(
        onDismissRequest = {}, // Disabled click-outside-to-dismiss
        modifier = Modifier.onKeyEvent { keyEvent ->
            if (keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyDown) {
                onDismiss()
                true
            } else {
                false
            }
        },
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
                        text = if (product == null) "إضافة منتج جديد" else "تعديل المنتج",
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
                    // Required Fields Section
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
                                text = "الحقول المطلوبة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Product Name (Required)
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("اسم المنتج *") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = name.isBlank(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { priceFocusRequester.requestFocus() }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                supportingText = if (name.isBlank()) {
                                    { Text("اسم المنتج مطلوب", color = MaterialTheme.colorScheme.error) }
                                } else null
                            )

                            // Price and Stock Quantity (Required)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = price,
                                    onValueChange = { price = it },
                                    label = { Text("السعر *") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AttachMoney,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(priceFocusRequester),
                                    singleLine = true,
                                    isError = price.toDoubleOrNull() == null,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { stockQuantityFocusRequester.requestFocus() }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    supportingText = if (price.toDoubleOrNull() == null) {
                                        { Text("سعر صحيح مطلوب", color = MaterialTheme.colorScheme.error) }
                                    } else null
                                )

                                OutlinedTextField(
                                    value = stockQuantity,
                                    onValueChange = { stockQuantity = it },
                                    label = { Text("كمية المخزون *") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(stockQuantityFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = if (showOptionalFields) ImeAction.Next else ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            if (showOptionalFields) {
                                                descriptionFocusRequester.requestFocus()
                                            }
                                        },
                                        onDone = {
                                            if (!showOptionalFields && isFormValid) {
                                                focusManager.clearFocus()
                                                val productDTO = data.api.ProductDTO(
                                                    id = product?.id?.toLong(),
                                                    name = name,
                                                    description = description.takeIf { it.isNotBlank() },
                                                    price = price.toDouble(),
                                                    costPrice = costPrice.toDoubleOrNull(),
                                                    stockQuantity = stockQuantity.toInt(),
                                                    category = selectedCategory?.name,
                                                    categoryId = selectedCategory?.id,
                                                    categoryName = selectedCategory?.name,
                                                    sku = sku.takeIf { it.isNotBlank() },
                                                    brand = brand.takeIf { it.isNotBlank() },
                                                    modelNumber = modelNumber.takeIf { it.isNotBlank() },
                                                    barcode = barcode.takeIf { it.isNotBlank() },
                                                    weight = weight.toDoubleOrNull(),
                                                    length = length.toDoubleOrNull(),
                                                    width = width.toDoubleOrNull(),
                                                    height = height.toDoubleOrNull(),
                                                    minStockLevel = minStockLevel.toIntOrNull(),
                                                    maxStockLevel = maxStockLevel.toIntOrNull(),
                                                    reorderPoint = reorderPoint.toIntOrNull(),
                                                    reorderQuantity = reorderQuantity.toIntOrNull(),
                                                    supplierName = supplierName.takeIf { it.isNotBlank() },
                                                    supplierCode = supplierCode.takeIf { it.isNotBlank() },
                                                    warrantyPeriod = warrantyPeriod.toIntOrNull(),
                                                    unitOfMeasure = unitOfMeasure,
                                                    taxRate = taxRate.toDoubleOrNull(),
                                                    discountPercentage = discountPercentage.toDoubleOrNull(),
                                                    locationInWarehouse = locationInWarehouse.takeIf { it.isNotBlank() },
                                                    notes = notes.takeIf { it.isNotBlank() }
                                                )
                                                onSave(productDTO)
                                            }
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    isError = stockQuantity.toIntOrNull() == null,
                                    supportingText = if (stockQuantity.toIntOrNull() == null) {
                                        { Text("كمية صحيحة مطلوبة", color = MaterialTheme.colorScheme.error) }
                                    } else null
                                )
                            }
                        }
                    }

                    // Optional Fields Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showOptionalFields = !showOptionalFields }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الحقول الاختيارية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Icon(
                            imageVector = if (showOptionalFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Optional Fields (Expandable)
                    if (showOptionalFields) {
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

                                // Basic Optional Info
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("الوصف") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(descriptionFocusRequester),
                                    maxLines = 3,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = { skuFocusRequester.requestFocus() }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Category Dropdown
                                var categoryDropdownExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = categoryDropdownExpanded,
                                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = selectedCategory?.name ?: "اختر الفئة",
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("الفئة") },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                                        },
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                            .fillMaxWidth(),
                                        singleLine = true
                                    )
                                    ExposedDropdownMenu(
                                        expanded = categoryDropdownExpanded,
                                        onDismissRequest = { categoryDropdownExpanded = false }
                                    ) {
                                        // Option to clear selection
                                        DropdownMenuItem(
                                            text = { Text("بدون فئة") },
                                            onClick = {
                                                selectedCategory = null
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                        // Category options
                                        activeCategories.forEach { category ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Category color indicator
                                                        category.colorCode?.let { colorCode ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(12.dp)
                                                                    .background(
                                                                        ui.utils.ColorUtils.parseHexColor(colorCode)
                                                                            ?: MaterialTheme.colorScheme.primary,
                                                                        CircleShape
                                                                    )
                                                            )
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                        }
                                                        Text(category.name)
                                                    }
                                                },
                                                onClick = {
                                                    selectedCategory = category
                                                    categoryDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = sku,
                                    onValueChange = { sku = it },
                                    label = { Text("رمز المنتج") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(skuFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = { costPriceFocusRequester.requestFocus() }
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = costPrice,
                                    onValueChange = { costPrice = it },
                                    label = { Text("سعر التكلفة") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(costPriceFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { brandFocusRequester.requestFocus() }
                                    )
                                )

                                OutlinedTextField(
                                    value = brand,
                                    onValueChange = { brand = it },
                                    label = { Text("العلامة التجارية") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(brandFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = { modelNumberFocusRequester.requestFocus() }
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = modelNumber,
                                    onValueChange = { modelNumber = it },
                                    label = { Text("رقم الموديل") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(modelNumberFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = { barcodeFocusRequester.requestFocus() }
                                    )
                                )

                                OutlinedTextField(
                                    value = barcode,
                                    onValueChange = { barcode = it },
                                    label = { Text("الباركود") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(barcodeFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = { weightFocusRequester.requestFocus() }
                                    )
                                )
                            }

                            // Physical Properties
                            Text(
                                text = "الخصائص الفيزيائية",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = { weight = it },
                                    label = { Text("الوزن (كجم)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(weightFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { lengthFocusRequester.requestFocus() }
                                    )
                                )

                                OutlinedTextField(
                                    value = length,
                                    onValueChange = { length = it },
                                    label = { Text("الطول (سم)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(lengthFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { widthFocusRequester.requestFocus() }
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = width,
                                    onValueChange = { width = it },
                                    label = { Text("العرض (سم)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(widthFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { heightFocusRequester.requestFocus() }
                                    )
                                )

                                OutlinedTextField(
                                    value = height,
                                    onValueChange = { height = it },
                                    label = { Text("الارتفاع (سم)") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(heightFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { minStockLevelFocusRequester.requestFocus() }
                                    )
                                )
                            }

                            // Stock Management
                            Text(
                                text = "إدارة المخزون",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = minStockLevel,
                                    onValueChange = { minStockLevel = it },
                                    label = { Text("الحد الأدنى للمخزون") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(minStockLevelFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { maxStockLevelFocusRequester.requestFocus() }
                                    )
                                )

                                OutlinedTextField(
                                    value = maxStockLevel,
                                    onValueChange = { maxStockLevel = it },
                                    label = { Text("الحد الأقصى للمخزون") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(maxStockLevelFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { reorderPointFocusRequester.requestFocus() }
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = reorderPoint,
                                    onValueChange = { reorderPoint = it },
                                    label = { Text("نقطة إعادة الطلب") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(reorderPointFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { reorderQuantityFocusRequester.requestFocus() }
                                    )
                                )

                                OutlinedTextField(
                                    value = reorderQuantity,
                                    onValueChange = { reorderQuantity = it },
                                    label = { Text("كمية إعادة الطلب") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(reorderQuantityFocusRequester),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (isFormValid) {
                                                focusManager.clearFocus()
                                                val productDTO = data.api.ProductDTO(
                                                    id = product?.id?.toLong(),
                                                    name = name,
                                                    description = description.takeIf { it.isNotBlank() },
                                                    price = price.toDouble(),
                                                    costPrice = costPrice.toDoubleOrNull(),
                                                    stockQuantity = stockQuantity.toInt(),
                                                    category = selectedCategory?.name,
                                                    categoryId = selectedCategory?.id,
                                                    categoryName = selectedCategory?.name,
                                                    sku = sku.takeIf { it.isNotBlank() },
                                                    brand = brand.takeIf { it.isNotBlank() },
                                                    modelNumber = modelNumber.takeIf { it.isNotBlank() },
                                                    barcode = barcode.takeIf { it.isNotBlank() },
                                                    weight = weight.toDoubleOrNull(),
                                                    length = length.toDoubleOrNull(),
                                                    width = width.toDoubleOrNull(),
                                                    height = height.toDoubleOrNull(),
                                                    minStockLevel = minStockLevel.toIntOrNull(),
                                                    maxStockLevel = maxStockLevel.toIntOrNull(),
                                                    reorderPoint = reorderPoint.toIntOrNull(),
                                                    reorderQuantity = reorderQuantity.toIntOrNull(),
                                                    supplierName = supplierName.takeIf { it.isNotBlank() },
                                                    supplierCode = supplierCode.takeIf { it.isNotBlank() },
                                                    warrantyPeriod = warrantyPeriod.toIntOrNull(),
                                                    unitOfMeasure = unitOfMeasure,
                                                    taxRate = taxRate.toDoubleOrNull(),
                                                    discountPercentage = discountPercentage.toDoubleOrNull(),
                                                    locationInWarehouse = locationInWarehouse.takeIf { it.isNotBlank() },
                                                    notes = notes.takeIf { it.isNotBlank() }
                                                )
                                                onSave(productDTO)
                                            }
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            // Full-width Save button with enhanced hover effects
            val saveInteractionSource = remember { MutableInteractionSource() }
            val isSaveHovered by saveInteractionSource.collectIsHoveredAsState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = if (isSaveHovered && isFormValid)
                            MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                        else if (isFormValid)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = if (isSaveHovered && isFormValid) 2.dp else 1.dp,
                        color = if (isSaveHovered && isFormValid)
                            MaterialTheme.colorScheme.primary
                        else if (isFormValid)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = saveInteractionSource,
                        indication = null,
                        enabled = isFormValid
                    ) {
                        if (isFormValid) {
                            val productDTO = data.api.ProductDTO(
                                id = product?.id?.toLong(),
                                name = name,
                                description = description.takeIf { it.isNotBlank() },
                                price = price.toDouble(),
                                costPrice = costPrice.toDoubleOrNull(),
                                stockQuantity = stockQuantity.toInt(),
                                category = selectedCategory?.name,
                                categoryId = selectedCategory?.id,
                                categoryName = selectedCategory?.name,
                                sku = sku.takeIf { it.isNotBlank() },
                                brand = brand.takeIf { it.isNotBlank() },
                                modelNumber = modelNumber.takeIf { it.isNotBlank() },
                                barcode = barcode.takeIf { it.isNotBlank() },
                                weight = weight.toDoubleOrNull(),
                                length = length.toDoubleOrNull(),
                                width = width.toDoubleOrNull(),
                                height = height.toDoubleOrNull(),
                                minStockLevel = minStockLevel.toIntOrNull(),
                                maxStockLevel = maxStockLevel.toIntOrNull(),
                                reorderPoint = reorderPoint.toIntOrNull(),
                                reorderQuantity = reorderQuantity.toIntOrNull(),
                                supplierName = supplierName.takeIf { it.isNotBlank() },
                                supplierCode = supplierCode.takeIf { it.isNotBlank() },
                                warrantyPeriod = warrantyPeriod.toIntOrNull(),
                                unitOfMeasure = unitOfMeasure,
                                taxRate = taxRate.toDoubleOrNull(),
                                discountPercentage = discountPercentage.toDoubleOrNull(),
                                locationInWarehouse = locationInWarehouse.takeIf { it.isNotBlank() },
                                notes = notes.takeIf { it.isNotBlank() }
                            )
                            onSave(productDTO)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (product != null) "تحديث" else "حفظ",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        dismissButton = { },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun ProductDetailsDialog(
    product: Product,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تفاصيل المنتج",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Status Badge
                    product.productStatus?.let { status ->
                        Surface(
                            color = when (status) {
                                "ACTIVE" -> AppTheme.colors.success.copy(alpha = 0.1f)
                                "INACTIVE" -> AppTheme.colors.warning.copy(alpha = 0.1f)
                                "DISCONTINUED" -> AppTheme.colors.error.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (status) {
                                    "ACTIVE" -> "نشط"
                                    "INACTIVE" -> "غير نشط"
                                    "DISCONTINUED" -> "متوقف"
                                    else -> status
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = when (status) {
                                    "ACTIVE" -> AppTheme.colors.success
                                    "INACTIVE" -> AppTheme.colors.warning
                                    "DISCONTINUED" -> AppTheme.colors.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Basic Information Section
                    DetailSection(
                        title = "المعلومات الأساسية",
                        icon = Icons.Default.Info
                    ) {
                        DetailRow("الاسم", product.name)
                        product.description?.let { DetailRow("الوصف", it) }
                        product.sku?.let { DetailRow("رمز المنتج", it) }
                        product.barcode?.let { DetailRow("الباركود", it) }
                        DetailRow("الفئة", product.category)
                        product.brand?.let { DetailRow("العلامة التجارية", it) }
                        product.modelNumber?.let { DetailRow("رقم الموديل", it) }
                    }

                    // Pricing & Financial Information
                    DetailSection(
                        title = "المعلومات المالية",
                        icon = Icons.Default.AttachMoney
                    ) {
                        DetailRow("السعر", currencyFormatter.format(product.price))
                        DetailRow("سعر التكلفة", currencyFormatter.format(product.cost))
                        product.discountedPrice?.let {
                            DetailRow("السعر بعد الخصم", currencyFormatter.format(it))
                        }
                        product.discountPercentage?.let {
                            DetailRow("نسبة الخصم", "${it}%")
                        }
                        product.taxRate?.let {
                            DetailRow("معدل الضريبة", "${it}%")
                        }
                        product.profitMargin?.let {
                            DetailRow("هامش الربح", "${String.format("%.2f", it)}%")
                        }
                        product.totalRevenue?.let {
                            DetailRow("إجمالي الإيرادات", currencyFormatter.format(it))
                        }
                    }

                    // Stock & Inventory Information
                    DetailSection(
                        title = "معلومات المخزون",
                        icon = Icons.Default.Inventory
                    ) {
                        DetailRow("المخزون الحالي", "${product.stock} ${product.unitOfMeasure ?: "قطعة"}")
                        product.minStockLevel?.let {
                            DetailRow("الحد الأدنى للمخزون", "$it قطعة")
                        }
                        product.maxStockLevel?.let {
                            DetailRow("الحد الأقصى للمخزون", "$it قطعة")
                        }
                        product.reorderPoint?.let {
                            DetailRow("نقطة إعادة الطلب", "$it قطعة")
                        }
                        product.reorderQuantity?.let {
                            DetailRow("كمية إعادة الطلب", "$it قطعة")
                        }
                        product.totalSold?.let {
                            DetailRow("إجمالي المبيعات", "$it قطعة")
                        }
                        product.locationInWarehouse?.let {
                            DetailRow("موقع المستودع", it)
                        }

                        // Stock Status Indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (product.lowStock == true) {
                                StatusChip("مخزون منخفض", AppTheme.colors.warning)
                            }
                            if (product.outOfStock == true) {
                                StatusChip("نفد المخزون", AppTheme.colors.error)
                            }
                            if (product.expired == true) {
                                StatusChip("منتهي الصلاحية", AppTheme.colors.error)
                            }
                        }
                    }

                    // Physical Properties (if available)
                    if (product.weight != null || product.length != null ||
                        product.width != null || product.height != null) {
                        DetailSection(
                            title = "الخصائص الفيزيائية",
                            icon = Icons.Default.Straighten
                        ) {
                            product.weight?.let {
                                DetailRow("الوزن", "${it} كجم")
                            }
                            product.length?.let {
                                DetailRow("الطول", "${it} سم")
                            }
                            product.width?.let {
                                DetailRow("العرض", "${it} سم")
                            }
                            product.height?.let {
                                DetailRow("الارتفاع", "${it} سم")
                            }
                        }
                    }

                    // Supplier Information (if available)
                    if (product.supplierName != null || product.supplierCode != null) {
                        DetailSection(
                            title = "معلومات المورد",
                            icon = Icons.Default.Business
                        ) {
                            product.supplierName?.let {
                                DetailRow("اسم المورد", it)
                            }
                            product.supplierCode?.let {
                                DetailRow("رمز المورد", it)
                            }
                        }
                    }

                    // Additional Information
                    DetailSection(
                        title = "معلومات إضافية",
                        icon = Icons.Default.MoreHoriz
                    ) {
                        product.warrantyPeriod?.let {
                            DetailRow("فترة الضمان", "$it شهر")
                        }
                        product.unitOfMeasure?.let {
                            DetailRow("وحدة القياس", it)
                        }
                        product.manufacturingDate?.let {
                            DetailRow("تاريخ التصنيع", it)
                        }
                        product.expiryDate?.let {
                            DetailRow("تاريخ انتهاء الصلاحية", it)
                        }
                        product.lastSoldDate?.let {
                            DetailRow("آخر عملية بيع", it)
                        }
                        product.lastRestockedDate?.let {
                            DetailRow("آخر تجديد للمخزون", it)
                        }
                        product.createdAt?.let {
                            DetailRow("تاريخ الإنشاء", it)
                        }
                        product.updatedAt?.let {
                            DetailRow("آخر تحديث", it)
                        }
                        product.notes?.let {
                            DetailRow("ملاحظات", it)
                        }
                    }

                    // Product Features (if available)
                    if (product.isSerialized == true || product.isDigital == true ||
                        product.isTaxable == true) {
                        DetailSection(
                            title = "خصائص المنتج",
                            icon = Icons.Default.Settings
                        ) {
                            if (product.isSerialized == true) {
                                DetailRow("منتج مسلسل", "نعم")
                            }
                            if (product.isDigital == true) {
                                DetailRow("منتج رقمي", "نعم")
                            }
                            if (product.isTaxable == true) {
                                DetailRow("خاضع للضريبة", "نعم")
                            }
                        }
                    }

                    // Tags (if available)
                    product.tags?.takeIf { it.isNotEmpty() }?.let { tags ->
                        Text(
                            text = "العلامات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tags) { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إغلاق")
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppTheme.colors.error
                        ),
                        border = BorderStroke(1.dp, AppTheme.colors.error)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حذف")
                    }

                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تعديل")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatusChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

// Enhanced Delete Confirmation Dialog
@Composable
fun DeleteConfirmationDialog(
    productName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(300)),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 16.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header with animated warning icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Animated warning icon with pulsing effect
                        val infiniteTransition = rememberInfiniteTransition(label = "warning_pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale_animation"
                        )

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            AppTheme.colors.error.copy(alpha = 0.2f),
                                            AppTheme.colors.error.copy(alpha = 0.05f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = AppTheme.colors.error,
                                modifier = Modifier
                                    .size(40.dp)
                                    .graphicsLayer(scaleX = scale, scaleY = scale)
                            )
                        }

                        Text(
                            text = "تأكيد الحذف",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Content section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "هل أنت متأكد من حذف المنتج التالي؟",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Enhanced product name card with gradient border
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppTheme.colors.error.copy(alpha = 0.08f)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = AppTheme.colors.error.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = AppTheme.colors.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = productName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.error,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Warning message with enhanced styling
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AppTheme.colors.warning.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "⚠️",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "هذا الإجراء لا يمكن التراجع عنه",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppTheme.colors.warning,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Action buttons with enhanced styling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        OutlinedButton(
                            onClick = {
                                isVisible = false
                                // Delay dismiss to allow exit animation
                                coroutineScope.launch {
                                    delay(200)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إلغاء",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Delete button with gradient background
                        Button(
                            onClick = {
                                isVisible = false
                                // Delay confirm to allow exit animation
                                coroutineScope.launch {
                                    delay(200)
                                    onConfirm()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.error
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حذف",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Export Dialog Component
@Composable
private fun ExportDialog(
    onDismiss: () -> Unit,
    onExportExcel: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
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
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "تصدير قائمة المنتجات",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "اختر تنسيق الملف المطلوب للتصدير:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Export options
                ExportOptionButton(
                    text = "تصدير إلى Excel (.xlsx)",
                    description = "ملف Excel مع تنسيق متقدم",
                    icon = Icons.Default.TableChart,
                    onClick = onExportExcel
                )

                ExportOptionButton(
                    text = "تصدير إلى CSV (.csv)",
                    description = "ملف نصي مفصول بفواصل",
                    icon = Icons.Default.Description,
                    onClick = onExportCsv
                )

                ExportOptionButton(
                    text = "تصدير إلى JSON (.json)",
                    description = "ملف JSON مع جميع البيانات",
                    icon = Icons.Default.Code,
                    onClick = onExportJson
                )

                // Cancel button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

// Import Dialog Component
@Composable
private fun ImportDialog(
    onDismiss: () -> Unit,
    onImport: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
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
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "استيراد منتجات",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "يمكنك استيراد المنتجات من ملفات CSV أو JSON. تأكد من أن الملف يحتوي على الحقول المطلوبة: الاسم، السعر، وكمية المخزون.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Warning card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.warning.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = AppTheme.colors.warning,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "سيتم إضافة المنتجات الجديدة فقط. المنتجات الموجودة لن يتم تحديثها.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = onImport,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اختيار ملف")
                    }
                }
            }
        }
    }
}

// Export Option Button Component
@Composable
private fun ExportOptionButton(
    text: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Import Preview Dialog Component
@Composable
private fun ImportPreviewDialog(
    products: List<ProductDTO>,
    warnings: List<String>,
    onDismiss: () -> Unit,
    onConfirmUpload: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "معاينة المنتجات المستوردة",
                        style = MaterialTheme.typography.headlineSmall,
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

                // Summary
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ملخص الاستيراد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("عدد المنتجات:")
                            Text(
                                text = "${products.size}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (warnings.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("التحذيرات:")
                                Text(
                                    text = "${warnings.size}",
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.warning
                                )
                            }
                        }
                    }
                }

                // Warnings section
                if (warnings.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.warning.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AppTheme.colors.warning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "تحذيرات",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 100.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(warnings) { warning ->
                                    Text(
                                        text = "• $warning",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Products preview
                Text(
                    text = "المنتجات (${products.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductPreviewCard(product = product)
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = onConfirmUpload,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("رفع إلى قاعدة البيانات")
                    }
                }
            }
        }
    }
}

// Product Preview Card Component
@Composable
private fun ProductPreviewCard(product: ProductDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "السعر: ${product.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "المخزون: ${product.stockQuantity ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!product.category.isNullOrBlank()) {
                Text(
                    text = "الفئة: ${product.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}



// Enhanced Product Card Component
@Composable
private fun EnhancedProductCard(
    product: Product,
    currencyFormatter: NumberFormat,
    isSelected: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = when {
                    isSelected -> 2.dp
                    isHovered -> 1.5.dp
                    else -> 1.dp
                },
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with actions
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
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!product.barcode.isNullOrBlank()) {
                        Text(
                            text = product.barcode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Price and Stock Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currencyFormatter.format(product.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (product.cost > 0) {
                        Text(
                            text = "التكلفة: ${currencyFormatter.format(product.cost)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stock Status
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            product.stock <= 5 -> MaterialTheme.colorScheme.errorContainer
                            product.stock <= 20 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            when {
                                product.stock <= 5 -> Icons.Default.Warning
                                product.stock <= 20 -> Icons.Default.Info
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when {
                                product.stock <= 5 -> MaterialTheme.colorScheme.onErrorContainer
                                product.stock <= 20 -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                        Text(
                            text = "${product.stock}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                product.stock <= 5 -> MaterialTheme.colorScheme.onErrorContainer
                                product.stock <= 20 -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            }

            // Category
            if (!product.category.isNullOrBlank()) {
                Text(
                    text = "الفئة: ${product.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Enhanced Product Details Panel Component
@Composable
private fun EnhancedProductDetailsPanel(
    product: Product,
    currencyFormatter: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تفاصيل المنتج",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Product Info Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!product.barcode.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = product.barcode,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!product.category.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Price and Stock Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "السعر",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(product.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        product.stock <= 5 -> MaterialTheme.colorScheme.errorContainer
                        product.stock <= 20 -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        tint = when {
                            product.stock <= 5 -> MaterialTheme.colorScheme.onErrorContainer
                            product.stock <= 20 -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "المخزون",
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            product.stock <= 5 -> MaterialTheme.colorScheme.onErrorContainer
                            product.stock <= 20 -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                    Text(
                        text = "${product.stock}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            product.stock <= 5 -> MaterialTheme.colorScheme.onErrorContainer
                            product.stock <= 20 -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تعديل")
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حذف")
            }
        }
    }
}
