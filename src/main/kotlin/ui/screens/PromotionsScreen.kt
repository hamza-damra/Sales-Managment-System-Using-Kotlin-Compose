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
import ui.components.EnhancedFilterDropdown
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.utils.ResponsiveUtils
import ui.utils.ColorUtils
import UiUtils
import androidx.compose.runtime.collectAsState
import data.api.PromotionDTO
import data.api.NetworkResult
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerState
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate

// Promotions Tab Enum
enum class PromotionsTab(val title: String) {
    OVERVIEW("نظرة عامة"),
    ACTIVE("العروض النشطة"),
    EXPIRED("العروض المنتهية"),
    ANALYTICS("التحليلات")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionsScreen(promotionViewModel: ui.viewmodels.PromotionViewModel) {
    RTLProvider {
        // ViewModel state
        val promotions by promotionViewModel.filteredPromotions.collectAsState()
        val activePromotions by promotionViewModel.filteredActivePromotions.collectAsState()
        val expiredPromotions by promotionViewModel.filteredExpiredPromotions.collectAsState()
        val isLoading by promotionViewModel.isLoading.collectAsState()
        val error by promotionViewModel.error.collectAsState()
        val searchQuery by promotionViewModel.searchQuery.collectAsState()
        val selectedStatus by promotionViewModel.selectedStatus.collectAsState()
        val selectedType by promotionViewModel.selectedType.collectAsState()
        val sortBy by promotionViewModel.sortBy.collectAsState()
        val showActiveOnly by promotionViewModel.showActiveOnly.collectAsState()
        val showExpiringOnly by promotionViewModel.showExpiringOnly.collectAsState()
        val isProcessing by promotionViewModel.isProcessing.collectAsState()
        val lastOperationResult by promotionViewModel.lastOperationResult.collectAsState()

        // Enhanced state management
        var selectedTab by remember { mutableStateOf(PromotionsTab.OVERVIEW) }
        var isExporting by remember { mutableStateOf(false) }
        var exportMessage by remember { mutableStateOf<String?>(null) }

        // Dialog states
        var showNewPromotionDialog by remember { mutableStateOf(false) }
        var showNewCouponDialog by remember { mutableStateOf(false) }
        var editingPromotion by remember { mutableStateOf<data.api.PromotionDTO?>(null) }
        var selectedPromotion by remember { mutableStateOf<data.api.PromotionDTO?>(null) }
        var showPromotionDetails by remember { mutableStateOf(false) }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var promotionToDelete by remember { mutableStateOf<data.api.PromotionDTO?>(null) }

        // For desktop application, we'll use window size detection
        val isTablet = true // Assume tablet/desktop for now
        val isDesktop = true // Desktop application

        // Snackbar state
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // Handle operation results
        LaunchedEffect(lastOperationResult) {
            lastOperationResult?.let { result ->
                when (result) {
                    is data.api.NetworkResult.Success -> {
                        snackbarHostState.showSnackbar("تم تنفيذ العملية بنجاح")
                    }
                    is data.api.NetworkResult.Error -> {
                        snackbarHostState.showSnackbar("خطأ: ${result.exception.message}")
                    }
                    else -> {}
                }
                promotionViewModel.clearLastOperationResult()
            }
        }

        // Handle errors
        LaunchedEffect(error) {
            error?.let {
                snackbarHostState.showSnackbar("خطأ في تحميل البيانات: $it")
            }
        }

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
                                onValueChange = { promotionViewModel.updateSearchQuery(it) },
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
                                options = listOf("الكل", "نشط", "غير نشط", "منتهي الصلاحية", "مجدول"),
                                onValueChange = { promotionViewModel.updateSelectedStatus(it) },
                                modifier = Modifier.weight(0.7f)
                            )

                            // Type Filter
                            EnhancedFilterDropdown(
                                label = "النوع",
                                value = selectedType,
                                options = listOf("الكل", "نسبة مئوية", "مبلغ ثابت", "اشتري X احصل على Y", "شحن مجاني"),
                                onValueChange = { promotionViewModel.updateSelectedType(it) },
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
                                    val newSortBy = when(it) {
                                        "الاسم" -> "name"
                                        "قيمة الخصم" -> "discountValue"
                                        "الاستخدام" -> "usageCount"
                                        "تاريخ الانتهاء" -> "endDate"
                                        else -> "name"
                                    }
                                    promotionViewModel.updateSortBy(newSortBy)
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
                                    ) { promotionViewModel.updateShowActiveOnly(!showActiveOnly) },
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
                                    ) { promotionViewModel.updateShowExpiringOnly(!showExpiringOnly) },
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

                            // Refresh Button
                            val refreshInteractionSource = remember { MutableInteractionSource() }
                            val isRefreshHovered by refreshInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = if (isRefreshHovered && !isLoading)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                        else
                                            MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (isRefreshHovered && !isLoading) 1.5.dp else 1.dp,
                                        color = if (isRefreshHovered && !isLoading)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = refreshInteractionSource,
                                        indication = null,
                                        enabled = !isLoading
                                    ) {
                                        if (!isLoading) {
                                            promotionViewModel.refreshData()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = if (isRefreshHovered)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                    Text(
                                        "تحديث",
                                        color = if (isLoading)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        else if (isRefreshHovered)
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
                                promotions = promotions,
                                isLoading = isLoading,
                                error = error,
                                onPromotionClick = { promotion ->
                                    selectedPromotion = promotion
                                    showPromotionDetails = true
                                },
                                onRefresh = { promotionViewModel.refreshData() }
                            )
                            PromotionsTab.ACTIVE -> EnhancedActivePromotionsContent(
                                promotions = activePromotions,
                                isLoading = isLoading,
                                error = error,
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
                                },
                                onActivatePromotion = { promotion ->
                                    coroutineScope.launch {
                                        promotion.id?.let { promotionViewModel.activatePromotion(it) }
                                    }
                                },
                                onDeactivatePromotion = { promotion ->
                                    coroutineScope.launch {
                                        promotion.id?.let { promotionViewModel.deactivatePromotion(it) }
                                    }
                                },
                                onRefresh = { promotionViewModel.loadActivePromotions() }
                            )
                            PromotionsTab.EXPIRED -> EnhancedExpiredPromotionsContent(
                                promotions = expiredPromotions,
                                isLoading = isLoading,
                                error = error,
                                onRefresh = { promotionViewModel.loadExpiredPromotions() }
                            )
                            PromotionsTab.ANALYTICS -> EnhancedPromotionAnalyticsContent(
                                promotions = promotions,
                                activePromotions = activePromotions,
                                expiredPromotions = expiredPromotions
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
                        promotion = selectedPromotion,
                        onClose = {
                            showPromotionDetails = false
                            selectedPromotion = null
                        },
                        onEdit = { promotion: data.api.PromotionDTO ->
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
                promotionViewModel = promotionViewModel,
                onDismiss = { showNewPromotionDialog = false },
                onSave = { promotionDTO ->
                    coroutineScope.launch {
                        val result = promotionViewModel.createPromotion(promotionDTO)
                        if (result.isSuccess) {
                            showNewPromotionDialog = false
                            snackbarHostState.showSnackbar("تم إضافة العرض بنجاح!")
                        } else {
                            val errorMessage = when (result) {
                                is NetworkResult.Error -> result.exception.message ?: "خطأ غير معروف"
                                else -> "خطأ في إضافة العرض"
                            }
                            snackbarHostState.showSnackbar("خطأ في إضافة العرض: $errorMessage")
                        }
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
                            coroutineScope.launch {
                                promotionToDelete?.id?.let { id ->
                                    val result = promotionViewModel.deletePromotion(id)
                                    if (result.isSuccess) {
                                        snackbarHostState.showSnackbar("تم حذف العرض بنجاح!")
                                    } else {
                                        snackbarHostState.showSnackbar("خطأ في حذف العرض")
                                    }
                                }
                                showDeleteConfirmation = false
                                promotionToDelete = null
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
fun EnhancedPromotionsTabRow(
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

// Helper function for detail rows in promotions
@Composable
private fun PromotionDetailRow(
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

// Forward declarations for enhanced components
@Composable
fun EnhancedExpiredPromotionsContent(
    promotions: List<data.api.PromotionDTO>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    // Temporary implementation - will be replaced
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("خطأ في تحميل البيانات: $error", color = MaterialTheme.colorScheme.error)
            Button(onClick = onRefresh) { Text("إعادة المحاولة") }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (promotions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد عروض منتهية الصلاحية")
                    }
                }
            } else {
                items(promotions) { promotion ->
                    EnhancedPromotionCard(promotion = promotion, onClick = { })
                }
            }
        }
    }
}

@Composable
fun EnhancedPromotionAnalyticsContent(
    promotions: List<data.api.PromotionDTO>,
    activePromotions: List<data.api.PromotionDTO>,
    expiredPromotions: List<data.api.PromotionDTO>
) {
    // Temporary implementation - will be replaced
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                EnhancedPromotionStatCard(
                    title = "إجمالي العروض",
                    value = promotions.size.toString(),
                    subtitle = "عرض",
                    icon = Icons.Default.LocalOffer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    trend = "+0%",
                    modifier = Modifier.weight(1f)
                )
                EnhancedPromotionStatCard(
                    title = "العروض النشطة",
                    value = activePromotions.size.toString(),
                    subtitle = "عرض نشط",
                    icon = Icons.Default.TrendingUp,
                    iconColor = AppTheme.colors.success,
                    trend = "+0%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EnhancedPromotionDetailsPanel(
    promotion: data.api.PromotionDTO?,
    onClose: () -> Unit,
    onEdit: (data.api.PromotionDTO) -> Unit,
    modifier: Modifier = Modifier
) {
    // Temporary implementation - will be replaced
    Card(
        modifier = modifier.width(400.dp).fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("تفاصيل العرض", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            promotion?.let {
                Text("الاسم: ${it.name}")
                Text("النوع: ${it.type}")
                Text("القيمة: ${it.discountValue}")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onEdit(it) }) {
                    Text("تعديل")
                }
            } ?: Text("لا توجد تفاصيل متاحة")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedNewPromotionDialog(
    promotionViewModel: ui.viewmodels.PromotionViewModel,
    onDismiss: () -> Unit,
    onSave: (PromotionDTO) -> Unit
) {
    // Form state variables
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PERCENTAGE") }
    var discountValue by remember { mutableStateOf("") }
    var minimumOrderAmount by remember { mutableStateOf("") }
    var maximumDiscountAmount by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var usageLimit by remember { mutableStateOf("") }
    var customerEligibility by remember { mutableStateOf("ALL") }
    var couponCode by remember { mutableStateOf("") }
    var autoApply by remember { mutableStateOf(false) }
    var stackable by remember { mutableStateOf(false) }

    // UI state
    var showOptionalFields by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var isFormValid by remember { mutableStateOf(false) }
    var isCouponCodeUnique by remember { mutableStateOf(true) }
    var isCheckingCouponCode by remember { mutableStateOf(false) }

    // Loading state
    val isProcessing by promotionViewModel.isProcessing.collectAsState()

    // Real-time coupon code uniqueness validation
    LaunchedEffect(couponCode) {
        if (couponCode.isNotBlank() && couponCode.length >= 3 && validateCouponCodeFormat(couponCode)) {
            isCheckingCouponCode = true
            delay(500) // Debounce
            isCouponCodeUnique = promotionViewModel.validateCouponCodeUniqueness(couponCode)
            isCheckingCouponCode = false
        } else {
            isCouponCodeUnique = true
            isCheckingCouponCode = false
        }
    }

    // Form validation
    LaunchedEffect(name, type, discountValue, startDate, endDate, couponCode, isCouponCodeUnique, minimumOrderAmount, maximumDiscountAmount, usageLimit) {
        val isNameValid = name.isNotBlank() && name.length >= 3
        val isDiscountValid = discountValue.toDoubleOrNull() != null &&
                             discountValue.toDoubleOrNull()!! > 0 &&
                             validateDiscountValue(type, discountValue)
        val areDatesValid = startDate.isNotBlank() &&
                           endDate.isNotBlank() &&
                           validateDates(startDate, endDate)
        val isCouponCodeValid = couponCode.isNotBlank() &&
                               couponCode.length >= 3 &&
                               validateCouponCodeFormat(couponCode) &&
                               isCouponCodeUnique
        val isMinOrderValid = minimumOrderAmount.isBlank() ||
                             (minimumOrderAmount.toDoubleOrNull() != null && minimumOrderAmount.toDoubleOrNull()!! >= 0)
        val isMaxDiscountValid = maximumDiscountAmount.isBlank() ||
                                (maximumDiscountAmount.toDoubleOrNull() != null && maximumDiscountAmount.toDoubleOrNull()!! > 0)
        val isUsageLimitValid = usageLimit.isBlank() ||
                               (usageLimit.toIntOrNull() != null && usageLimit.toIntOrNull()!! > 0)

        isFormValid = isNameValid && isDiscountValid && areDatesValid && isCouponCodeValid &&
                     isMinOrderValid && isMaxDiscountValid && isUsageLimitValid
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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

                // Required Fields Section
                Text(
                    text = "المعلومات الأساسية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Promotion Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العرض *") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isBlank() || name.length < 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                if (name.isBlank()) {
                    Text(
                        text = "اسم العرض مطلوب",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                } else if (name.length < 3) {
                    Text(
                        text = "اسم العرض يجب أن يكون 3 أحرف على الأقل",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف العرض") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Promotion Type and Discount Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Promotion Type
                    EnhancedFilterDropdown(
                        label = "نوع العرض *",
                        value = when(type) {
                            "PERCENTAGE" -> "نسبة مئوية"
                            "FIXED_AMOUNT" -> "مبلغ ثابت"
                            "BUY_X_GET_Y" -> "اشتري X احصل على Y"
                            "FREE_SHIPPING" -> "شحن مجاني"
                            else -> "نسبة مئوية"
                        },
                        options = listOf("نسبة مئوية", "مبلغ ثابت", "اشتري X احصل على Y", "شحن مجاني"),
                        onValueChange = { selectedType ->
                            type = when(selectedType) {
                                "نسبة مئوية" -> "PERCENTAGE"
                                "مبلغ ثابت" -> "FIXED_AMOUNT"
                                "اشتري X احصل على Y" -> "BUY_X_GET_Y"
                                "شحن مجاني" -> "FREE_SHIPPING"
                                else -> "PERCENTAGE"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Discount Value
                    OutlinedTextField(
                        value = discountValue,
                        onValueChange = { discountValue = it },
                        label = { Text(if (type == "PERCENTAGE") "النسبة المئوية *" else "المبلغ *") },
                        leadingIcon = {
                            Icon(
                                if (type == "PERCENTAGE") Icons.Default.Percent else Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.weight(1f),
                        isError = discountValue.toDoubleOrNull() == null || !validateDiscountValue(type, discountValue),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
                if (discountValue.toDoubleOrNull() == null || !validateDiscountValue(type, discountValue)) {
                    Text(
                        text = if (type == "PERCENTAGE") "يجب أن تكون النسبة بين 1 و 100" else "يجب أن يكون المبلغ أكبر من 0",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                // Coupon Code (Required)
                OutlinedTextField(
                    value = couponCode,
                    onValueChange = { couponCode = it.uppercase() },
                    label = { Text("كود الكوبون *") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        when {
                            isCheckingCouponCode -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            couponCode.isNotBlank() && couponCode.length >= 3 && validateCouponCodeFormat(couponCode) -> {
                                Icon(
                                    if (isCouponCodeUnique) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = if (isCouponCodeUnique) "متاح" else "غير متاح",
                                    tint = if (isCouponCodeUnique) Color.Green else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = couponCode.isBlank() || couponCode.length < 3 || !validateCouponCodeFormat(couponCode) || !isCouponCodeUnique,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    supportingText = {
                        Text(
                            text = when {
                                isCouponCodeUnique && couponCode.isNotBlank() && validateCouponCodeFormat(couponCode) -> "كود متاح ✓"
                                !isCouponCodeUnique && couponCode.isNotBlank() -> "كود موجود بالفعل"
                                else -> "مثال: SUMMER2024-20OFF"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                isCouponCodeUnique && couponCode.isNotBlank() && validateCouponCodeFormat(couponCode) -> Color.Green
                                !isCouponCodeUnique && couponCode.isNotBlank() -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                )
                if (couponCode.isBlank()) {
                    Text(
                        text = "كود الكوبون مطلوب",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                } else if (couponCode.length < 3) {
                    Text(
                        text = "كود الكوبون يجب أن يكون 3 أحرف على الأقل",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                } else if (!validateCouponCodeFormat(couponCode)) {
                    Text(
                        text = "كود الكوبون يجب أن يحتوي على أحرف وأرقام فقط",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                } else if (!isCouponCodeUnique) {
                    Text(
                        text = "كود الكوبون موجود بالفعل، يرجى اختيار كود آخر",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                // Date Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Start Date
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { },
                        label = { Text("تاريخ البداية *") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "اختر التاريخ",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        isError = startDate.isBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // End Date
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { },
                        label = { Text("تاريخ النهاية *") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "اختر التاريخ",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        isError = endDate.isBlank() || !validateDates(startDate, endDate),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
                if (startDate.isBlank() || endDate.isBlank()) {
                    Text(
                        text = "تاريخ البداية والنهاية مطلوبان",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                } else if (!validateDates(startDate, endDate)) {
                    Text(
                        text = "تاريخ النهاية يجب أن يكون بعد تاريخ البداية",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                // Optional Fields Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showOptionalFields = !showOptionalFields }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (showOptionalFields) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "إعدادات إضافية",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Optional Fields
                AnimatedVisibility(
                    visible = showOptionalFields,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Minimum Order Amount and Maximum Discount
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = minimumOrderAmount,
                                onValueChange = { minimumOrderAmount = it },
                                label = { Text("الحد الأدنى للطلب") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            OutlinedTextField(
                                value = maximumDiscountAmount,
                                onValueChange = { maximumDiscountAmount = it },
                                label = { Text("الحد الأقصى للخصم") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // Usage Limit and Customer Eligibility
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = usageLimit,
                                onValueChange = { usageLimit = it },
                                label = { Text("حد الاستخدام") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Numbers,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            EnhancedFilterDropdown(
                                label = "أهلية العملاء",
                                value = when(customerEligibility) {
                                    "ALL" -> "جميع العملاء"
                                    "VIP_ONLY" -> "عملاء VIP فقط"
                                    "NEW_CUSTOMERS" -> "عملاء جدد فقط"
                                    else -> "جميع العملاء"
                                },
                                options = listOf("جميع العملاء", "عملاء VIP فقط", "عملاء جدد فقط"),
                                onValueChange = { selectedEligibility ->
                                    customerEligibility = when(selectedEligibility) {
                                        "جميع العملاء" -> "ALL"
                                        "عملاء VIP فقط" -> "VIP_ONLY"
                                        "عملاء جدد فقط" -> "NEW_CUSTOMERS"
                                        else -> "ALL"
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }



                        // Checkboxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = autoApply,
                                    onCheckedChange = { autoApply = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "تطبيق تلقائي",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = stackable,
                                    onCheckedChange = { stackable = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "قابل للتراكم",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isProcessing
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (isFormValid) {
                                val promotionDTO = PromotionDTO(
                                    name = name,
                                    description = description.ifBlank { null },
                                    type = type,
                                    discountValue = discountValue.toDouble(),
                                    minimumOrderAmount = minimumOrderAmount.toDoubleOrNull(),
                                    maximumDiscountAmount = maximumDiscountAmount.toDoubleOrNull(),
                                    startDate = formatDateForApi(startDate),
                                    endDate = formatDateForApi(endDate),
                                    isActive = true,
                                    usageLimit = usageLimit.toIntOrNull(),
                                    usageCount = 0,
                                    customerEligibility = customerEligibility,
                                    couponCode = couponCode, // Now required, not nullable
                                    autoApply = autoApply,
                                    stackable = stackable
                                )
                                onSave(promotionDTO)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isFormValid && !isProcessing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("حفظ العرض")
                        }
                    }
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                startDate = selectedDate
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                endDate = selectedDate
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

// Helper functions for validation and formatting
private fun validateDiscountValue(type: String, value: String): Boolean {
    val doubleValue = value.toDoubleOrNull() ?: return false
    return when (type) {
        "PERCENTAGE" -> doubleValue in 1.0..100.0
        "FIXED_AMOUNT" -> doubleValue > 0
        else -> doubleValue > 0
    }
}

private fun validateDates(startDate: String, endDate: String): Boolean {
    if (startDate.isBlank() || endDate.isBlank()) return false
    return try {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        end.isAfter(start)
    } catch (e: Exception) {
        false
    }
}

private fun validateCouponCodeFormat(couponCode: String): Boolean {
    // Coupon code should contain only letters, numbers, and hyphens
    // Minimum 3 characters, maximum 50 characters
    if (couponCode.length < 3 || couponCode.length > 50) return false
    return couponCode.matches(Regex("^[A-Z0-9-]+$"))
}

private fun formatDateForApi(dateString: String): String {
    return try {
        val localDate = LocalDate.parse(dateString)
        localDate.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
    } catch (e: Exception) {
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z"
    }
}

// Custom Date Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(localDate.toString())
                    }
                }
            ) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun EnhancedNewCouponDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    // Temporary implementation - will be replaced
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة كوبون جديد") },
        text = { Text("نموذج إضافة كوبون جديد") },
        confirmButton = {
            Button(onClick = onSave) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

// Enhanced Promotions Overview Content
@Composable
fun EnhancedPromotionsOverviewContent(
    promotions: List<data.api.PromotionDTO>,
    isLoading: Boolean,
    error: String?,
    onPromotionClick: (data.api.PromotionDTO) -> Unit,
    onRefresh: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "خطأ في تحميل البيانات: $error",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRefresh) {
                Text("إعادة المحاولة")
            }
        }
    } else {
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
                        value = promotions.size.toString(),
                        subtitle = "عرض مسجل",
                        icon = Icons.Default.LocalOffer,
                        iconColor = MaterialTheme.colorScheme.primary,
                    trend = "+3 هذا الشهر",
                    modifier = Modifier.weight(1f)
                )
                EnhancedPromotionStatCard(
                    title = "العروض النشطة",
                    value = promotions.count { it.isCurrentlyActive == true }.toString(),
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
                    value = UiUtils.formatCurrency(
                        promotions.sumOf { it.discountValue }
                    ),
                    subtitle = "قيمة الخصومات",
                    icon = Icons.Default.Discount,
                    iconColor = AppTheme.colors.warning,
                    trend = "+15.2%",
                    modifier = Modifier.weight(1f)
                )
                EnhancedPromotionStatCard(
                    title = "معدل الاستخدام",
                    value = "${
                        if (promotions.isNotEmpty()) {
                            val totalUsage = promotions.sumOf { it.usageCount ?: 0 }
                            val totalLimit = promotions.sumOf { it.usageLimit ?: 0 }
                            if (totalLimit > 0) (totalUsage * 100 / totalLimit) else 0
                        } else 0
                    }%",
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

            // Real promotion items
            if (promotions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardStyles.elevatedCardColors()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد عروض متاحة",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(promotions) { promotion ->
                    EnhancedPromotionCard(
                        promotion = promotion,
                        onClick = { onPromotionClick(promotion) }
                    )
                }
            }
        }
    }
}

// Enhanced Promotion Stat Card Component
@Composable
fun EnhancedPromotionStatCard(
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

// Enhanced Promotion Card Component with PromotionDTO
@Composable
fun EnhancedPromotionCard(
    promotion: data.api.PromotionDTO,
    onClick: () -> Unit = {},
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    showActions: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Helper functions to format data
    val typeDisplay = when (promotion.type) {
        "PERCENTAGE" -> "نسبة مئوية"
        "FIXED_AMOUNT" -> "مبلغ ثابت"
        "BUY_X_GET_Y" -> "اشتري X احصل على Y"
        "FREE_SHIPPING" -> "شحن مجاني"
        else -> promotion.typeDisplay ?: "غير محدد"
    }

    val discountDisplay = when (promotion.type) {
        "PERCENTAGE" -> "${promotion.discountValue}%"
        "FIXED_AMOUNT" -> UiUtils.formatCurrency(promotion.discountValue)
        else -> "${promotion.discountValue}"
    }

    val validUntilDisplay = promotion.endDate?.let { endDate ->
        try {
            // Format the date for display
            "صالح حتى ${endDate.substring(0, 10)}"
        } catch (e: Exception) {
            "صالح حتى ${endDate}"
        }
    } ?: "غير محدد"

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
                    isHovered && promotion.isActive -> AppTheme.colors.success.copy(alpha = 0.5f)
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    promotion.isActive -> AppTheme.colors.success.copy(alpha = 0.3f)
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
                                if (promotion.isActive) AppTheme.colors.success.copy(alpha = 0.02f)
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
                            color = if (promotion.isActive) AppTheme.colors.success.copy(alpha = 0.1f)
                                   else AppTheme.colors.error.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = promotion.statusDisplay ?: if (promotion.isActive) "نشط" else "منتهي",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (promotion.isActive) AppTheme.colors.success else AppTheme.colors.error
                            )
                        }

                        // Promotion type chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = typeDisplay,
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
                            text = promotion.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = discountDisplay,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Description
                    Text(
                        text = promotion.description ?: "لا يوجد وصف",
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
                                    tint = if (promotion.isActive) AppTheme.colors.success else AppTheme.colors.error
                                )
                                Text(
                                    text = validUntilDisplay,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (promotion.isActive) AppTheme.colors.success else AppTheme.colors.error
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
                                    text = "${promotion.usageCount ?: 0} استخدام",
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

// Enhanced Active Promotions Content
@Composable
fun EnhancedActivePromotionsContent(
    promotions: List<data.api.PromotionDTO>,
    isLoading: Boolean,
    error: String?,
    onPromotionClick: (data.api.PromotionDTO) -> Unit,
    onEditPromotion: (data.api.PromotionDTO) -> Unit,
    onDeletePromotion: (data.api.PromotionDTO) -> Unit,
    onActivatePromotion: (data.api.PromotionDTO) -> Unit,
    onDeactivatePromotion: (data.api.PromotionDTO) -> Unit,
    onRefresh: () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "خطأ في تحميل البيانات: $error",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRefresh) {
                Text("إعادة المحاولة")
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (promotions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardStyles.elevatedCardColors()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد عروض نشطة",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(promotions) { promotion ->
                    EnhancedPromotionCard(
                        promotion = promotion,
                        onClick = { onPromotionClick(promotion) },
                        onEdit = { onEditPromotion(promotion) },
                        onDelete = { onDeletePromotion(promotion) },
                        showActions = true
                    )
                }
            }
        }
    }




// Data class for promotion details
data class PromotionDetails(
    val id: String,
    val title: String,
    val description: String,
    val discountType: String,
    val discountValue: String,
    val startDate: String,
    val endDate: String,
    val usageCount: Int,
    val maxUsage: Int,
    val isActive: Boolean,
    val category: String
)

// Function to get promotion details by ID
fun getPromotionDetails(promotionId: String): PromotionDetails {
    // Extract index from promotionId (e.g., "promotion_0" -> 0)
    val index = promotionId.substringAfter("_").toIntOrNull() ?: 0

    return when (index) {
        0 -> PromotionDetails(
            id = promotionId,
            title = "خصم 15% على الإلكترونيات",
            description = "خصم على جميع المنتجات في فئة الإلكترونيات",
            discountType = "نسبة مئوية",
            discountValue = "15%",
            startDate = "01/11/2024",
            endDate = "31/12/2024",
            usageCount = 45,
            maxUsage = 500,
            isActive = true,
            category = "الإلكترونيات"
        )
        1 -> PromotionDetails(
            id = promotionId,
            title = "خصم 30% على الملابس",
            description = "خصم على جميع المنتجات في فئة الملابس",
            discountType = "نسبة مئوية",
            discountValue = "30%",
            startDate = "15/10/2024",
            endDate = "15/01/2025",
            usageCount = 90,
            maxUsage = 300,
            isActive = true,
            category = "الملابس"
        )
        2 -> PromotionDetails(
            id = promotionId,
            title = "خصم 45% على المنزل والحديقة",
            description = "خصم على جميع المنتجات في فئة المنزل والحديقة",
            discountType = "نسبة مئوية",
            discountValue = "45%",
            startDate = "01/09/2024",
            endDate = "30/11/2024",
            usageCount = 135,
            maxUsage = 200,
            isActive = false,
            category = "المنزل والحديقة"
        )
        3 -> PromotionDetails(
            id = promotionId,
            title = "خصم 60% على الرياضة",
            description = "خصم على جميع المنتجات في فئة الرياضة",
            discountType = "مبلغ ثابت",
            discountValue = "60 ريال",
            startDate = "01/12/2024",
            endDate = "31/01/2025",
            usageCount = 180,
            maxUsage = 400,
            isActive = true,
            category = "الرياضة"
        )
        4 -> PromotionDetails(
            id = promotionId,
            title = "خصم 75% على الكتب",
            description = "خصم على جميع المنتجات في فئة الكتب",
            discountType = "نسبة مئوية",
            discountValue = "75%",
            startDate = "01/08/2024",
            endDate = "31/10/2024",
            usageCount = 225,
            maxUsage = 150,
            isActive = false,
            category = "الكتب"
        )
        5 -> PromotionDetails(
            id = promotionId,
            title = "خصم 90% على الألعاب",
            description = "خصم على جميع المنتجات في فئة الألعاب",
            discountType = "مبلغ ثابت",
            discountValue = "90 ريال",
            startDate = "15/11/2024",
            endDate = "15/02/2025",
            usageCount = 270,
            maxUsage = 600,
            isActive = true,
            category = "الألعاب"
        )
        6 -> PromotionDetails(
            id = promotionId,
            title = "خصم 105% على الجمال",
            description = "خصم على جميع المنتجات في فئة الجمال",
            discountType = "نسبة مئوية",
            discountValue = "105%",
            startDate = "01/07/2024",
            endDate = "30/09/2024",
            usageCount = 315,
            maxUsage = 250,
            isActive = false,
            category = "الجمال"
        )
        7 -> PromotionDetails(
            id = promotionId,
            title = "خصم 120% على السيارات",
            description = "خصم على جميع المنتجات في فئة السيارات",
            discountType = "مبلغ ثابت",
            discountValue = "120 ريال",
            startDate = "01/01/2025",
            endDate = "31/03/2025",
            usageCount = 360,
            maxUsage = 800,
            isActive = true,
            category = "السيارات"
        )
        else -> PromotionDetails(
            id = promotionId,
            title = "عرض افتراضي",
            description = "وصف العرض الافتراضي",
            discountType = "نسبة مئوية",
            discountValue = "10%",
            startDate = "01/01/2024",
            endDate = "31/12/2024",
            usageCount = 0,
            maxUsage = 100,
            isActive = true,
            category = "عام"
        )
    }
}

// Enhanced Promotion Details Panel (Duplicate - will be removed)
@Composable
fun EnhancedPromotionDetailsPanelDuplicate(
    promotion: data.api.PromotionDTO?,
    onClose: () -> Unit,
    onEdit: (data.api.PromotionDTO) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use the promotion directly
    val promotionDetails = promotion

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
                        onClick = { promotionDetails?.let { onEdit(it) } },
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
            if (promotionDetails != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (promotionDetails.isActive)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = promotionDetails.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = promotionDetails.description ?: "لا يوجد وصف",
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
                        PromotionDetailRow("نوع الخصم", promotionDetails.typeDisplay ?: promotionDetails.type)
                        PromotionDetailRow("قيمة الخصم", "${promotionDetails.discountValue}")
                        PromotionDetailRow("تاريخ البداية", promotionDetails.startDate)
                        PromotionDetailRow("تاريخ النهاية", promotionDetails.endDate)
                        PromotionDetailRow("عدد الاستخدامات", "${promotionDetails.usageCount ?: 0} استخدام")
                        PromotionDetailRow("الحد الأقصى للاستخدام", "${promotionDetails.usageLimit ?: "غير محدود"}")
                        PromotionDetailRow("كود الكوبون", promotionDetails.couponCode ?: "لا يوجد")
                        PromotionDetailRow("الحالة", if (promotionDetails.isActive) "نشط" else "غير نشط")
                    }
                }
                } // Close LazyColumn
            } else {
                // Show message when no promotion is selected
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لم يتم العثور على تفاصيل العرض",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Enhanced New Promotion Dialog (Duplicate - will be removed)
@Composable
fun EnhancedNewPromotionDialogDuplicate(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var promotionName by remember { mutableStateOf("") }
    var discountType by remember { mutableStateOf("نسبة مئوية") }
    var discountValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                        text = "إضافة عرض جديد",
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

                // Promotion Information Section
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
                            text = "معلومات العرض",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = promotionName,
                            onValueChange = { promotionName = it },
                            label = { Text("اسم العرض") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
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
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Percent,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = if (isSaveHovered)
                                MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isSaveHovered) 2.dp else 1.dp,
                            color = if (isSaveHovered)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = saveInteractionSource,
                            indication = null
                        ) { onSave() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "حفظ",
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

// Enhanced New Coupon Dialog (Duplicate - will be removed)
@Composable
fun EnhancedNewCouponDialogDuplicate(
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
}
