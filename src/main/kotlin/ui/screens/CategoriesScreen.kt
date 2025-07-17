@file:OptIn(ExperimentalMaterial3Api::class)

package ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import data.Category
import data.CategoryStatus
import data.api.CategoryDTO
import data.api.InventoryDTO
import ui.components.*
import ui.theme.AppTheme
import ui.viewmodels.CategoryViewModel
import ui.viewmodels.InventoryViewModel
import ui.utils.ColorUtils
import kotlinx.coroutines.launch

@Composable
fun CategoriesScreen(
    categoryViewModel: CategoryViewModel,
    inventoryViewModel: InventoryViewModel
) {
    RTLProvider {
        var searchQuery by remember { mutableStateOf("") }
        var showAddCategoryDialog by remember { mutableStateOf(false) }
        var editingCategory by remember { mutableStateOf<Category?>(null) }
        var selectedStatus by remember { mutableStateOf("الكل") }
        var showCategoryDetails by remember { mutableStateOf(false) }
        var selectedCategory by remember { mutableStateOf<Category?>(null) }
        var sortBy by remember { mutableStateOf("displayOrder") }
        val coroutineScope = rememberCoroutineScope()

        val uiState by categoryViewModel.uiState.collectAsState()
        val searchQueryState by categoryViewModel.searchQuery.collectAsState()
        val selectedStatusState by categoryViewModel.selectedStatus.collectAsState()

        // Collect inventory state
        val inventoryUiState by inventoryViewModel.uiState.collectAsState()
        val inventories = inventoryUiState.inventories

        // Load categories and inventories when screen is first displayed
        LaunchedEffect(Unit) {
            categoryViewModel.loadCategories()
            categoryViewModel.loadActiveCategories()
            inventoryViewModel.loadInventories()
        }

        // Handle success states
        LaunchedEffect(uiState.creationSuccess) {
            if (uiState.creationSuccess) {
                showAddCategoryDialog = false
                categoryViewModel.clearSuccessStates()
            }
        }

        LaunchedEffect(uiState.updateSuccess) {
            if (uiState.updateSuccess) {
                editingCategory = null
                categoryViewModel.clearSuccessStates()
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Categories List
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
                                text = "إدارة الفئات",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val addCategoryInteractionSource = remember { MutableInteractionSource() }
                            val isAddCategoryHovered by addCategoryInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = if (isAddCategoryHovered)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                                        else
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = addCategoryInteractionSource,
                                        indication = null
                                    ) { showAddCategoryDialog = true }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        "إضافة فئة جديدة",
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
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
                                onValueChange = {
                                    searchQuery = it
                                    categoryViewModel.searchCategories(it)
                                },
                                label = { Text("البحث في الفئات") },
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

                            // Status Filter
                            var statusExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = statusExpanded,
                                onExpandedChange = { statusExpanded = !statusExpanded },
                                modifier = Modifier.width(200.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedStatus,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("الحالة") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = statusExpanded
                                        )
                                    },
                                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = statusExpanded,
                                    onDismissRequest = { statusExpanded = false }
                                ) {
                                    listOf("الكل", "نشط", "غير نشط", "مؤرشف").forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status) },
                                            onClick = {
                                                selectedStatus = status
                                                categoryViewModel.filterByStatus(status)
                                                statusExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Refresh Button with enhanced hover effects
                            val refreshInteractionSource = remember { MutableInteractionSource() }
                            val isRefreshHovered by refreshInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        color = if (isRefreshHovered)
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(
                                        interactionSource = refreshInteractionSource,
                                        indication = null
                                    ) { categoryViewModel.refreshCategories() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "تحديث",
                                    tint = if (isRefreshHovered)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Categories List
                        Text(
                            text = "قائمة الفئات (${uiState.categories.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (uiState.isLoading) {
                            // Professional shimmer loading state
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Header shimmer
                                CategoriesHeaderShimmer()

                                // Categories list shimmer
                                CategoriesListShimmer(
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else if (uiState.hasError) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = AppTheme.colors.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = uiState.error ?: "حدث خطأ غير معروف",
                                    color = AppTheme.colors.error,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        categoryViewModel.clearError()
                                        categoryViewModel.refreshCategories()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("إعادة المحاولة")
                                }
                            }
                        } else if (uiState.categories.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "لا توجد فئات",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "ابدأ بإضافة فئة جديدة لتنظيم منتجاتك",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(uiState.categories) { category ->
                                    ModernCategoryItem(
                                        category = category,
                                        inventories = inventories,
                                        onEdit = { editingCategory = it },
                                        onDelete = { categoryViewModel.deleteCategory(it.id) },
                                        onStatusChange = { cat, status ->
                                            categoryViewModel.updateCategoryStatus(cat.id, status)
                                        },
                                        onViewDetails = {
                                            selectedCategory = it
                                            showCategoryDetails = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Panel - Statistics and Overview
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
                        if (uiState.isLoading) {
                            // Statistics panel shimmer during loading
                            CategoriesStatsPanelShimmer()
                        } else {
                            // Statistics Header
                            Text(
                                text = "إحصائيات الفئات",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Summary Cards
                            ModernCategorySummaryCard(
                                title = "إجمالي الفئات",
                                value = uiState.totalCategories.toString(),
                                subtitle = "فئة مسجلة",
                                icon = Icons.Default.Category,
                                iconColor = MaterialTheme.colorScheme.primary
                            )

                            ModernCategorySummaryCard(
                                title = "الفئات النشطة",
                                value = uiState.activeCategories.size.toString(),
                                subtitle = "فئة نشطة",
                                icon = Icons.Default.CheckCircle,
                                iconColor = AppTheme.colors.success
                            )

                            ModernCategorySummaryCard(
                                title = "الفئات الفارغة",
                                value = uiState.categories.count { it.productCount == 0 }.toString(),
                                subtitle = "فئة بدون منتجات",
                                icon = Icons.Default.Warning,
                                iconColor = AppTheme.colors.warning
                            )

                        // Quick Actions
                        Text(
                            text = "إجراءات سريعة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        QuickActionButton(
                            text = "إضافة فئة جديدة",
                            icon = Icons.Default.Add,
                            onClick = { showAddCategoryDialog = true }
                        )

                        QuickActionButton(
                            text = "تحديث البيانات",
                            icon = Icons.Default.Refresh,
                            onClick = { categoryViewModel.refreshCategories() }
                        )
                        } // Close the else block for statistics loading state
                    }
                }
            }
        }

        // Add/Edit Category Dialog
        if (showAddCategoryDialog || editingCategory != null) {
            CategoryDialog(
                category = editingCategory,
                inventoryViewModel = inventoryViewModel,
                onDismiss = {
                    showAddCategoryDialog = false
                    editingCategory = null
                },
                onSave = { categoryDTO ->
                    if (editingCategory != null) {
                        categoryViewModel.updateCategory(editingCategory!!.id, categoryDTO)
                    } else {
                        categoryViewModel.createCategory(categoryDTO)
                    }
                }
            )
        }

        // Category Details Dialog
        if (showCategoryDetails && selectedCategory != null) {
            CategoryDetailsDialog(
                category = selectedCategory!!,
                onDismiss = { 
                    showCategoryDetails = false
                    selectedCategory = null
                }
            )
        }
    }
}

@Composable
fun ModernCategorySummaryCard(
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
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            iconColor.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ModernCategoryItem(
    category: Category,
    inventories: List<InventoryDTO>,
    onEdit: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onStatusChange: (Category, CategoryStatus) -> Unit,
    onViewDetails: (Category) -> Unit
) {
    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
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
            ) { onViewDetails(category) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon or Color with enhanced design
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            category.colorCode?.let {
                                ColorUtils.parseHexColor(it) ?: MaterialTheme.colorScheme.primary
                            } ?: MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (category.icon != null) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Text(
                            text = category.name.take(2),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (category.description != null) {
                        Text(
                            text = category.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Show inventory information if available
                    category.inventoryId?.let { inventoryId ->
                        val inventory = inventories.find { it.id == inventoryId }
                        inventory?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warehouse,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (it.isMainWarehouse) {
                                    Text(
                                        text = "• رئيسي",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Product count with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${category.productCount} منتج",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Display order
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ترتيب ${category.displayOrder}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status Badge
                ModernStatusBadge(status = category.status)

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Edit Button
                    IconButton(
                        onClick = { onEdit(category) },
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Status Toggle Button
                    var statusMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { statusMenuExpanded = true },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "تغيير الحالة",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false }
                        ) {
                            CategoryStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.displayName) },
                                    onClick = {
                                        onStatusChange(category, status)
                                        statusMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Delete Button (only if no products)
                    if (category.productCount == 0) {
                        IconButton(
                            onClick = { onDelete(category) },
                            modifier = Modifier
                                .background(
                                    AppTheme.colors.error.copy(alpha = 0.1f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = AppTheme.colors.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernStatusBadge(status: CategoryStatus) {
    val (backgroundColor, textColor) = when (status) {
        CategoryStatus.ACTIVE -> AppTheme.colors.success to Color.White
        CategoryStatus.INACTIVE -> AppTheme.colors.warning to Color.White
        CategoryStatus.ARCHIVED -> MaterialTheme.colorScheme.onSurfaceVariant to Color.White
    }

    Box(
        modifier = Modifier
            .background(
                backgroundColor,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isHovered)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered)
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isHovered)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isHovered)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun CategoryDialog(
    category: Category? = null,
    inventoryViewModel: InventoryViewModel,
    onDismiss: () -> Unit,
    onSave: (CategoryDTO) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }
    var displayOrder by remember { mutableStateOf(category?.displayOrder?.toString() ?: "0") }
    var colorCode by remember { mutableStateOf(category?.colorCode ?: "#007bff") }
    var icon by remember { mutableStateOf(category?.icon ?: "") }
    var imageUrl by remember { mutableStateOf(category?.imageUrl ?: "") }
    var selectedInventoryId by remember { mutableStateOf(category?.inventoryId) }
    var showInventoryDropdown by remember { mutableStateOf(false) }

    // Focus manager for keyboard navigation
    val focusManager = LocalFocusManager.current

    // Focus requesters for explicit focus management
    val descriptionFocusRequester = remember { FocusRequester() }
    val displayOrderFocusRequester = remember { FocusRequester() }
    val colorCodeFocusRequester = remember { FocusRequester() }
    val iconFocusRequester = remember { FocusRequester() }
    val imageUrlFocusRequester = remember { FocusRequester() }

    // Collect inventory data
    val inventoryUiState by inventoryViewModel.uiState.collectAsState()
    val inventories = inventoryUiState.inventories

    // Load inventories when dialog opens
    LaunchedEffect(Unit) {
        inventoryViewModel.loadInventories()
    }

    val isEditing = category != null
    val title = if (isEditing) "تعديل الفئة" else "إضافة فئة جديدة"

    AlertDialog(
        onDismissRequest = {}, // Disabled click-outside-to-dismiss
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
                        text = title,
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

                        // Name Field (Required)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("اسم الفئة *") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = name.isBlank(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { descriptionFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        if (name.isBlank()) {
                            Text(
                                text = "اسم الفئة مطلوب",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        // Description Field
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("الوصف") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(descriptionFocusRequester),
                            minLines = 3,
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { displayOrderFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Inventory Selection Dropdown (Required)
                        ExposedDropdownMenuBox(
                            expanded = showInventoryDropdown,
                            onExpandedChange = { showInventoryDropdown = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = inventories.find { it.id == selectedInventoryId }?.name ?: "اختر المستودع",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("المستودع المرتبط *") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Warehouse,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showInventoryDropdown)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                isError = selectedInventoryId == null,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = showInventoryDropdown,
                                onDismissRequest = { showInventoryDropdown = false }
                            ) {
                                inventories.forEach { inventory ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = inventory.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = inventory.location,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (inventory.isMainWarehouse) {
                                                    Text(
                                                        text = "مستودع رئيسي",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedInventoryId = inventory.id
                                            showInventoryDropdown = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Warehouse,
                                                contentDescription = null,
                                                tint = if (inventory.isMainWarehouse)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    )
                                }

                                if (inventories.isEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "لا توجد مستودعات متاحة",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        onClick = { },
                                        enabled = false
                                    )
                                }
                            }
                        }

                        if (selectedInventoryId == null) {
                            Text(
                                text = "يجب اختيار مستودع للفئة",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        // Display Order Field
                        OutlinedTextField(
                            value = displayOrder,
                            onValueChange = { displayOrder = it },
                            label = { Text("ترتيب العرض") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(displayOrderFocusRequester),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { colorCodeFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // Styling & Media Section
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
                            text = "التصميم والوسائط",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Color Picker Field
                        ColorPickerField(
                            value = colorCode,
                            onValueChange = { newColor ->
                                colorCode = ColorUtils.normalizeHexColor(newColor)
                            },
                            label = "لون الفئة",
                            modifier = Modifier.fillMaxWidth(),
                            isError = !ColorUtils.isValidHexColor(colorCode) && colorCode.isNotBlank(),
                            errorMessage = ColorUtils.getColorValidationError(colorCode),
                            focusRequester = colorCodeFocusRequester,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { iconFocusRequester.requestFocus() }
                            )
                        )

                        // Icon Field
                        OutlinedTextField(
                            value = icon,
                            onValueChange = { icon = it },
                            label = { Text("الأيقونة") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(iconFocusRequester),
                            singleLine = true,
                            placeholder = { Text("category-icon") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { imageUrlFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        // Image URL Field
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it },
                            label = { Text("رابط الصورة") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(imageUrlFocusRequester),
                            singleLine = true,
                            placeholder = { Text("https://example.com/image.jpg") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val isColorValid = colorCode.isBlank() || ColorUtils.isValidHexColor(colorCode)
                                    val isValid = name.isNotBlank() && selectedInventoryId != null && isColorValid
                                    if (isValid) {
                                        focusManager.clearFocus()
                                        val categoryDTO = CategoryDTO(
                                            id = category?.id,
                                            name = name,
                                            description = description.ifBlank { null },
                                            displayOrder = displayOrder.toIntOrNull() ?: 0,
                                            colorCode = colorCode.ifBlank { null },
                                            icon = icon.ifBlank { null },
                                            imageUrl = imageUrl.ifBlank { null },
                                            inventoryId = selectedInventoryId
                                        )
                                        onSave(categoryDTO)
                                    }
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
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
                val isColorValid = colorCode.isBlank() || ColorUtils.isValidHexColor(colorCode)
                val isValid = name.isNotBlank() && selectedInventoryId != null && isColorValid

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = if (isSaveHovered && isValid)
                                MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                            else if (isValid)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isSaveHovered && isValid) 2.dp else 1.dp,
                            color = if (isSaveHovered && isValid)
                                MaterialTheme.colorScheme.primary
                            else if (isValid)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = saveInteractionSource,
                            indication = null,
                            enabled = isValid
                        ) {
                            if (isValid) {
                                val categoryDTO = CategoryDTO(
                                    id = category?.id,
                                    name = name.trim(),
                                    description = description.trim().takeIf { it.isNotEmpty() },
                                    displayOrder = displayOrder.toIntOrNull() ?: 0,
                                    status = category?.status?.name ?: "ACTIVE",
                                    imageUrl = imageUrl.trim().takeIf { it.isNotEmpty() },
                                    icon = icon.trim().takeIf { it.isNotEmpty() },
                                    colorCode = colorCode.trim().takeIf { it.isNotEmpty() },
                                    inventoryId = selectedInventoryId
                                )
                                onSave(categoryDTO)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEditing) "تحديث" else "إضافة",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        dismissButton = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun CategoryDetailsDialog(
    category: Category,
    onDismiss: () -> Unit
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
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تفاصيل الفئة",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ModernStatusBadge(status = category.status)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Category Icon/Color and Name
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                category.colorCode?.let {
                                    ColorUtils.parseHexColor(it) ?: MaterialTheme.colorScheme.primary
                                } ?: MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (category.icon != null) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Text(
                                text = category.name.take(2),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (category.description != null) {
                            Text(
                                text = category.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Details Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryDetailRow("المعرف", category.id.toString())
                    CategoryDetailRow("عدد المنتجات", category.productCount.toString())
                    CategoryDetailRow("ترتيب العرض", category.displayOrder.toString())

                    if (category.colorCode != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "رمز اللون:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.colorCode,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            ColorUtils.parseHexColor(category.colorCode) ?: MaterialTheme.colorScheme.primary
                                        )
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }

                    if (category.icon != null) {
                        CategoryDetailRow("الأيقونة", category.icon)
                    }

                    if (category.imageUrl != null) {
                        CategoryDetailRow("رابط الصورة", category.imageUrl)
                    }

                    if (category.createdAt != null) {
                        CategoryDetailRow("تاريخ الإنشاء", category.createdAt.toString().split("T")[0])
                    }

                    if (category.updatedAt != null) {
                        CategoryDetailRow("آخر تحديث", category.updatedAt.toString().split("T")[0])
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
