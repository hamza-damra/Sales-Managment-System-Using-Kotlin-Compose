package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.utils.ResponsiveUtils
import ui.utils.ColorUtils
import UiUtils

// Promotions Tab Enum
enum class PromotionsTab(val title: String) {
    OVERVIEW("نظرة عامة"),
    ACTIVE("العروض النشطة"),
    EXPIRED("العروض المنتهية"),
    ANALYTICS("التحليلات")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionsScreen() {
    RTLProvider {
        // Enhanced state management
        var selectedTab by remember { mutableStateOf(PromotionsTab.OVERVIEW) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedStatus by remember { mutableStateOf("الكل") }
        var selectedType by remember { mutableStateOf("الكل") }
        var sortBy by remember { mutableStateOf("name") }
        var showActiveOnly by remember { mutableStateOf(false) }
        var showExpiringOnly by remember { mutableStateOf(false) }
        var isExporting by remember { mutableStateOf(false) }
        var exportMessage by remember { mutableStateOf<String?>(null) }

        // Dialog states
        var showNewPromotionDialog by remember { mutableStateOf(false) }
        var showNewCouponDialog by remember { mutableStateOf(false) }
        var editingPromotion by remember { mutableStateOf<String?>(null) }
        var selectedPromotion by remember { mutableStateOf<String?>(null) }
        var showPromotionDetails by remember { mutableStateOf(false) }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var promotionToDelete by remember { mutableStateOf<String?>(null) }

        // For desktop application, we'll use window size detection
        val isTablet = true // Assume tablet/desktop for now
        val isDesktop = true // Desktop application

        // Snackbar state
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // Export functions
        val handleExportExcel = {
            if (!isExporting) {
                isExporting = true
                exportMessage = null
                coroutineScope.launch {
                    try {
                        // Simulate export process
                        kotlinx.coroutines.delay(2000)
                        exportMessage = "تم تصدير الملف بنجاح!"
                    } catch (e: Exception) {
                        exportMessage = "خطأ في التصدير: ${e.message}"
                    } finally {
                        isExporting = false
                    }
                }
            }
        }

        val handleExportPdf = {
            if (!isExporting) {
                isExporting = true
                exportMessage = null
                coroutineScope.launch {
                    try {
                        // Simulate export process
                        kotlinx.coroutines.delay(2000)
                        exportMessage = "تم تصدير الملف بنجاح!"
                    } catch (e: Exception) {
                        exportMessage = "خطأ في التصدير: ${e.message}"
                    } finally {
                        isExporting = false
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Promotions Management
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
                                text = "إدارة العروض والخصومات",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = { showNewPromotionDialog = true },
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
                                Text("إضافة عرض جديد")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Enhanced Tabs
                        EnhancedPromotionsTabRow(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search and Filter Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Search Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("البحث في العروض") },
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
                            EnhancedFilterDropdown(
                                label = "الحالة",
                                value = selectedStatus,
                                options = listOf("الكل", "نشط", "منتهي", "مجدول", "متوقف"),
                                onValueChange = { selectedStatus = it },
                                modifier = Modifier.weight(0.7f)
                            )

                            // Type Filter
                            EnhancedFilterDropdown(
                                label = "النوع",
                                value = selectedType,
                                options = listOf("الكل", "نسبة مئوية", "مبلغ ثابت", "كوبون", "عرض خاص"),
                                onValueChange = { selectedType = it },
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
                                    "discount" -> "قيمة الخصم"
                                    "usage" -> "الاستخدام"
                                    "expiry" -> "تاريخ الانتهاء"
                                    else -> "الاسم"
                                },
                                options = listOf("الاسم", "قيمة الخصم", "الاستخدام", "تاريخ الانتهاء"),
                                onValueChange = {
                                    sortBy = when(it) {
                                        "الاسم" -> "name"
                                        "قيمة الخصم" -> "discount"
                                        "الاستخدام" -> "usage"
                                        "تاريخ الانتهاء" -> "expiry"
                                        else -> "name"
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Enhanced Quick Filters with complete hover coverage
                            val activeOnlyInteractionSource = remember { MutableInteractionSource() }
                            val isActiveOnlyHovered by activeOnlyInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = when {
                                            showActiveOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            isActiveOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (showActiveOnly) 1.5.dp else if (isActiveOnlyHovered) 1.dp else 0.5.dp,
                                        color = when {
                                            showActiveOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            isActiveOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = activeOnlyInteractionSource,
                                        indication = null
                                    ) { showActiveOnly = !showActiveOnly },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    if (showActiveOnly) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "نشط فقط",
                                        color = when {
                                            showActiveOnly -> MaterialTheme.colorScheme.primary
                                            isActiveOnlyHovered -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            val expiringOnlyInteractionSource = remember { MutableInteractionSource() }
                            val isExpiringOnlyHovered by expiringOnlyInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = when {
                                            showExpiringOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            isExpiringOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (showExpiringOnly) 1.5.dp else if (isExpiringOnlyHovered) 1.dp else 0.5.dp,
                                        color = when {
                                            showExpiringOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            isExpiringOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = expiringOnlyInteractionSource,
                                        indication = null
                                    ) { showExpiringOnly = !showExpiringOnly },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    if (showExpiringOnly) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "قارب على الانتهاء",
                                        color = when {
                                            showExpiringOnly -> MaterialTheme.colorScheme.primary
                                            isExpiringOnlyHovered -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

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
                                    ) { if (!isExporting) handleExportExcel() },
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
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Content based on selected tab
                        when (selectedTab) {
                            PromotionsTab.OVERVIEW -> EnhancedPromotionsOverviewContent(
                                searchQuery = searchQuery,
                                selectedStatus = selectedStatus,
                                selectedType = selectedType,
                                showActiveOnly = showActiveOnly,
                                showExpiringOnly = showExpiringOnly,
                                sortBy = sortBy,
                                onPromotionClick = { promotion ->
                                    selectedPromotion = promotion
                                    showPromotionDetails = true
                                }
                            )
                            PromotionsTab.ACTIVE -> EnhancedActivePromotionsContent(
                                searchQuery = searchQuery,
                                selectedType = selectedType,
                                sortBy = sortBy,
                                onPromotionClick = { promotion ->
                                    selectedPromotion = promotion
                                    showPromotionDetails = true
                                },
                                onEditPromotion = { promotion ->
                                    editingPromotion = promotion
                                },
                                onDeletePromotion = { promotion ->
                                    promotionToDelete = promotion
                                    showDeleteConfirmation = true
                                }
                            )
                            PromotionsTab.EXPIRED -> EnhancedExpiredPromotionsContent(
                                searchQuery = searchQuery,
                                selectedType = selectedType,
                                sortBy = sortBy
                            )
                            PromotionsTab.ANALYTICS -> EnhancedPromotionAnalyticsContent(
                                searchQuery = searchQuery
                            )
                        }
                    }
                }

                // Right Panel - Details and Statistics (when promotion selected)
                AnimatedVisibility(
                    visible = showPromotionDetails && selectedPromotion != null,
                    enter = slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeIn(),
                    exit = slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    ) + fadeOut()
                ) {
                    EnhancedPromotionDetailsPanel(
                        promotionId = selectedPromotion ?: "",
                        onClose = {
                            showPromotionDetails = false
                            selectedPromotion = null
                        },
                        onEdit = { promotion ->
                            editingPromotion = promotion
                            showPromotionDetails = false
                        }
                    )
                }
            }

            // Snackbar for export messages
            exportMessage?.let { message ->
                LaunchedEffect(message) {
                    snackbarHostState.showSnackbar(message)
                    exportMessage = null
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Dialogs
        if (showNewPromotionDialog) {
            EnhancedNewPromotionDialog(
                onDismiss = { showNewPromotionDialog = false },
                onSave = {
                    // Handle save promotion
                    showNewPromotionDialog = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تم إضافة العرض بنجاح!")
                    }
                }
            )
        }

        if (showNewCouponDialog) {
            EnhancedNewCouponDialog(
                onDismiss = { showNewCouponDialog = false },
                onSave = {
                    // Handle save coupon
                    showNewCouponDialog = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تم إضافة الكوبون بنجاح!")
                    }
                }
            )
        }

        if (showDeleteConfirmation && promotionToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("تأكيد الحذف") },
                text = { Text("هل أنت متأكد من حذف هذا العرض؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            // Handle delete
                            showDeleteConfirmation = false
                            promotionToDelete = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("تم حذف العرض بنجاح!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("حذف")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

// Enhanced Tab Row Component
@Composable
private fun EnhancedPromotionsTabRow(
    selectedTab: PromotionsTab,
    onTabSelected: (PromotionsTab) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        ) {
            PromotionsTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

// Enhanced Filter Dropdown Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedFilterDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
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

// Enhanced Promotions Overview Content
@Composable
private fun EnhancedPromotionsOverviewContent(
    searchQuery: String,
    selectedStatus: String,
    selectedType: String,
    showActiveOnly: Boolean,
    showExpiringOnly: Boolean,
    sortBy: String,
    onPromotionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedPromotionStatCard(
                    title = "إجمالي العروض",
                    value = "24",
                    subtitle = "عرض مسجل",
                    icon = Icons.Default.LocalOffer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    trend = "+3 هذا الشهر",
                    modifier = Modifier.weight(1f)
                )
                EnhancedPromotionStatCard(
                    title = "العروض النشطة",
                    value = "12",
                    subtitle = "عرض نشط",
                    icon = Icons.Default.TrendingUp,
                    iconColor = AppTheme.colors.success,
                    trend = "+50%",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedPromotionStatCard(
                    title = "إجمالي الخصومات",
                    value = UiUtils.formatCurrency(45600.0),
                    subtitle = "قيمة الخصومات",
                    icon = Icons.Default.Discount,
                    iconColor = AppTheme.colors.warning,
                    trend = "+15.2%",
                    modifier = Modifier.weight(1f)
                )
                EnhancedPromotionStatCard(
                    title = "معدل الاستخدام",
                    value = "78%",
                    subtitle = "من العروض",
                    icon = Icons.Default.Analytics,
                    iconColor = AppTheme.colors.info,
                    trend = "+5.8%",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Promotions
        item {
            Text(
                text = "العروض الحديثة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Sample promotion items
        items(8) { index ->
            EnhancedPromotionCard(
                title = "خصم ${(index + 1) * 15}%",
                description = "خصم على جميع المنتجات في فئة ${if (index == 0) "الإلكترونيات" else if (index == 1) "الملابس" else "المنزل والحديقة"}",
                validUntil = "صالح حتى 31/12/2024",
                discountValue = "${(index + 1) * 15}%",
                usageCount = "${(index + 1) * 45}",
                isActive = index % 3 != 0,
                promotionType = if (index % 2 == 0) "نسبة مئوية" else "مبلغ ثابت",
                onClick = { onPromotionClick("promotion_$index") }
            )
        }
    }
}

// Enhanced Promotion Stat Card Component
@Composable
private fun EnhancedPromotionStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    trend: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                                iconColor.copy(alpha = 0.02f),
                                iconColor.copy(alpha = 0.06f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with icon and trend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = iconColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = iconColor
                            )
                        }
                    }

                    // Trend indicator
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (trend.startsWith("+")) AppTheme.colors.success.copy(alpha = 0.1f)
                               else AppTheme.colors.error.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = trend,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (trend.startsWith("+")) AppTheme.colors.success
                                   else AppTheme.colors.error
                        )
                    }
                }

                // Value and title
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
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
    }
}

// Enhanced Promotion Card Component
@Composable
private fun EnhancedPromotionCard(
    title: String,
    description: String,
    validUntil: String,
    discountValue: String,
    usageCount: String,
    isActive: Boolean,
    promotionType: String,
    onClick: () -> Unit = {},
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    showActions: Boolean = false,
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
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = when {
                    isHovered && isActive -> AppTheme.colors.success.copy(alpha = 0.5f)
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    isActive -> AppTheme.colors.success.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                if (isActive) AppTheme.colors.success.copy(alpha = 0.02f)
                                else AppTheme.colors.error.copy(alpha = 0.02f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with status and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isActive) AppTheme.colors.success.copy(alpha = 0.1f)
                                   else AppTheme.colors.error.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = if (isActive) "نشط" else "منتهي",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isActive) AppTheme.colors.success else AppTheme.colors.error
                            )
                        }

                        // Promotion type chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = promotionType,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Action buttons
                    if (showActions && (onEdit != null || onDelete != null)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            onEdit?.let { editAction ->
                                IconButton(
                                    onClick = editAction,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "تعديل",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            onDelete?.let { deleteAction ->
                                IconButton(
                                    onClick = deleteAction,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Main content
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title and discount value
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

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = discountValue,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Description
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Footer with validity and usage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isActive) AppTheme.colors.success else AppTheme.colors.error
                                )
                                Text(
                                    text = validUntil,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isActive) AppTheme.colors.success else AppTheme.colors.error
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$usageCount استخدام",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // View details button
                        TextButton(
                            onClick = onClick,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "عرض التفاصيل",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Enhanced Promotion Card Component
@Composable
private fun EnhancedPromotionCard(
    title: String,
    description: String,
    validUntil: String,
    discountValue: String,
    usageCount: String,
    isActive: Boolean,
    promotionType: String,
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
                color = when {
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = when {
                    isHovered && isActive -> AppTheme.colors.success.copy(alpha = 0.5f)
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    isActive -> AppTheme.colors.success.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Handle click */ }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                if (isActive) AppTheme.colors.success.copy(alpha = 0.02f)
                                else AppTheme.colors.error.copy(alpha = 0.02f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with status and actions
                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    RTLRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isActive) AppTheme.colors.success.copy(alpha = 0.1f)
                                   else AppTheme.colors.error.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = if (isActive) "نشط" else "منتهي",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isActive) AppTheme.colors.success else AppTheme.colors.error
                            )
                        }

                        // Promotion type chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = promotionType,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Action buttons
                    RTLRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { /* Edit promotion */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "تعديل",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { /* More options */ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "المزيد",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Main content
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title and discount value
                    RTLRow(
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

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = discountValue,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Description
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Footer with validity and usage
                    RTLRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RTLRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RTLRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isActive) AppTheme.colors.success else AppTheme.colors.error
                                )
                                Text(
                                    text = validUntil,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isActive) AppTheme.colors.success else AppTheme.colors.error
                                )
                            }

                            RTLRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$usageCount استخدام",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // View details button
                        TextButton(
                            onClick = { /* View details */ },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "عرض التفاصيل",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Enhanced Active Promotions Content
@Composable
private fun EnhancedActivePromotionsContent(
    searchQuery: String,
    selectedType: String,
    sortBy: String,
    onPromotionClick: (String) -> Unit,
    onEditPromotion: (String) -> Unit,
    onDeletePromotion: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sample active promotions
        items(6) { index ->
            EnhancedPromotionCard(
                title = "خصم ${(index + 1) * 15}%",
                description = "خصم على جميع المنتجات في فئة ${if (index == 0) "الإلكترونيات" else if (index == 1) "الملابس" else "المنزل والحديقة"}",
                validUntil = "صالح حتى 31/12/2024",
                discountValue = "${(index + 1) * 15}%",
                usageCount = "${(index + 1) * 45}",
                isActive = true,
                promotionType = if (index % 2 == 0) "نسبة مئوية" else "مبلغ ثابت",
                onClick = { onPromotionClick("promotion_$index") },
                onEdit = { onEditPromotion("promotion_$index") },
                onDelete = { onDeletePromotion("promotion_$index") },
                showActions = true
            )
        }
    }
}

// Enhanced Expired Promotions Content
@Composable
private fun EnhancedExpiredPromotionsContent(
    searchQuery: String,
    selectedType: String,
    sortBy: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sample expired promotions
        items(4) { index ->
            EnhancedPromotionCard(
                title = "عرض الصيف ${index + 1}",
                description = "عرض خاص لفصل الصيف على منتجات مختارة",
                validUntil = "انتهى في 30/09/2024",
                discountValue = "${(index + 1) * 20}%",
                usageCount = "${(index + 1) * 120}",
                isActive = false,
                promotionType = "عرض موسمي",
                onClick = { /* View expired promotion details */ }
            )
        }
    }
}

// Enhanced Promotion Analytics Content
@Composable
private fun EnhancedPromotionAnalyticsContent(
    searchQuery: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedPromotionStatCard(
                    title = "أفضل عرض",
                    value = "خصم 30%",
                    subtitle = "على الإلكترونيات",
                    icon = Icons.Default.TrendingUp,
                    iconColor = AppTheme.colors.success,
                    trend = "450 استخدام",
                    modifier = Modifier.weight(1f)
                )
                EnhancedPromotionStatCard(
                    title = "متوسط الخصم",
                    value = "18.5%",
                    subtitle = "لجميع العروض",
                    icon = Icons.Default.Analytics,
                    iconColor = AppTheme.colors.info,
                    trend = "+2.3%",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Top Performing Promotions
        item {
            Text(
                text = "أفضل العروض أداءً",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(5) { index ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "خصم ${(index + 1) * 15}% على الإلكترونيات",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(index + 1) * 150} استخدام",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = UiUtils.formatCurrency((index + 1) * 5600.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.success
                    )
                }
            }
        }
    }
}

// Enhanced Promotion Details Panel
@Composable
private fun EnhancedPromotionDetailsPanel(
    promotionId: String,
    onClose: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(400.dp)
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
                    text = "تفاصيل العرض",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onEdit(promotionId) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "تعديل",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Promotion details content
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "خصم 25% على الإلكترونيات",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "عرض خاص على جميع المنتجات الإلكترونية",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "معلومات العرض",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Promotion details
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailRow("نوع الخصم", "نسبة مئوية")
                        DetailRow("قيمة الخصم", "25%")
                        DetailRow("تاريخ البداية", "01/11/2024")
                        DetailRow("تاريخ النهاية", "31/12/2024")
                        DetailRow("عدد الاستخدامات", "156 استخدام")
                        DetailRow("الحد الأقصى للاستخدام", "500 استخدام")
                        DetailRow("الحالة", "نشط")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
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

// Enhanced New Promotion Dialog
@Composable
private fun EnhancedNewPromotionDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var promotionName by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("نسبة مئوية") }
    var discountValue by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إضافة عرض جديد",
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

                // Form fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = promotionName,
                        onValueChange = { promotionName = it },
                        label = { Text("اسم العرض") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    EnhancedFilterDropdown(
                        label = "نوع الخصم",
                        value = discountType,
                        options = listOf("نسبة مئوية", "مبلغ ثابت"),
                        onValueChange = { discountType = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = discountValue,
                        onValueChange = { discountValue = it },
                        label = { Text("قيمة الخصم") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

// Enhanced New Coupon Dialog
@Composable
private fun EnhancedNewCouponDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var couponCode by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("نسبة مئوية") }
    var discountValue by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إضافة كوبون جديد",
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

                // Form fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = { couponCode = it },
                        label = { Text("كود الكوبون") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    EnhancedFilterDropdown(
                        label = "نوع الخصم",
                        value = discountType,
                        options = listOf("نسبة مئوية", "مبلغ ثابت"),
                        onValueChange = { discountType = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = discountValue,
                        onValueChange = { discountValue = it },
                        label = { Text("قيمة الخصم") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}
