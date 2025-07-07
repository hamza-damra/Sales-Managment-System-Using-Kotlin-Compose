package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.*
import data.api.*
import data.di.AppDependencies
import kotlinx.datetime.LocalDateTime
import kotlinx.coroutines.launch
import UiUtils
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.utils.ResponsiveUtils.getResponsivePadding
import ui.utils.ResponsiveUtils.getScreenInfo
import ui.viewmodels.ReturnsViewModel
import androidx.compose.runtime.collectAsState
import services.ReturnReceiptService
import utils.FileDialogUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnsScreen() {
    RTLProvider {
        // ViewModel integration
        val viewModel = remember { AppDependencies.container.returnsViewModel }

        // Collect ViewModel state
        val returns by viewModel.filteredReturns.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val error by viewModel.error.collectAsState()
        val searchQuery by viewModel.searchQuery.collectAsState()
        val selectedStatusFromVM by viewModel.selectedStatus.collectAsState()
        val selectedReturn by viewModel.selectedReturn.collectAsState()
        val isCreatingReturn by viewModel.isCreatingReturn.collectAsState()
        val isUpdatingReturn by viewModel.isUpdatingReturn.collectAsState()
        val isDeletingReturn by viewModel.isDeletingReturn.collectAsState()
        val isProcessingReturn by viewModel.isProcessingReturn.collectAsState()
        val totalReturns by viewModel.totalReturns.collectAsState()
        val pendingReturns by viewModel.pendingReturns.collectAsState()
        val totalRefundAmount by viewModel.totalRefundAmount.collectAsState()
        val returnRate by viewModel.returnRate.collectAsState()
        val lastCreatedReturn by viewModel.lastCreatedReturn.collectAsState()
        val lastUpdatedReturn by viewModel.lastUpdatedReturn.collectAsState()
        val lastDeletedReturnId by viewModel.lastDeletedReturnId.collectAsState()

        // Enhanced state management
        var selectedTab by remember { mutableStateOf(ReturnTab.RETURNS) }
        var selectedReason by remember { mutableStateOf("الكل") }
        var selectedDateRange by remember { mutableStateOf("الكل") }
        var sortBy by remember { mutableStateOf("date") }
        var showPendingOnly by remember { mutableStateOf(false) }
        var showRefundableOnly by remember { mutableStateOf(false) }
        var isExporting by remember { mutableStateOf(false) }
        var exportMessage by remember { mutableStateOf<String?>(null) }

        // PDF generation states
        var generatedPdfFile by remember { mutableStateOf<File?>(null) }
        var showPdfViewer by remember { mutableStateOf(false) }
        var showFullScreenPdfViewer by remember { mutableStateOf(false) }
        var isGeneratingPdf by remember { mutableStateOf(false) }

        // Dialog states
        var showNewReturnDialog by remember { mutableStateOf(false) }
        var editingReturn by remember { mutableStateOf<ReturnDTO?>(null) }
        var showReturnDetails by remember { mutableStateOf(false) }
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var returnToDelete by remember { mutableStateOf<ReturnDTO?>(null) }

        // For desktop application, we'll use window size detection
        val isTablet = true // Assume tablet/desktop for now
        val isDesktop = true // Desktop application

        // Snackbar state
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Returns Management
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
                                text = "إدارة المرتجعات والإلغاءات",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = { showNewReturnDialog = true },
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
                                Text("إرجاع جديد")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Enhanced Tabs
                        EnhancedReturnsTabRow(
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
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                label = { Text("البحث في المرتجعات") },
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
                            EnhancedReturnsFilterDropdown(
                                label = "الحالة",
                                value = when (selectedStatusFromVM) {
                                    "PENDING" -> "في الانتظار"
                                    "APPROVED" -> "موافق عليه"
                                    "REJECTED" -> "مرفوض"
                                    "REFUNDED" -> "تم الاسترداد"
                                    "EXCHANGED" -> "تم الاستبدال"
                                    null -> "الكل"
                                    else -> "الكل"
                                },
                                options = listOf("الكل", "في الانتظار", "موافق عليه", "مرفوض", "تم الاسترداد", "تم الاستبدال"),
                                onValueChange = {
                                    val statusValue = when (it) {
                                        "في الانتظار" -> "PENDING"
                                        "موافق عليه" -> "APPROVED"
                                        "مرفوض" -> "REJECTED"
                                        "تم الاسترداد" -> "REFUNDED"
                                        "تم الاستبدال" -> "EXCHANGED"
                                        "الكل" -> null
                                        else -> null
                                    }
                                    viewModel.updateSelectedStatus(statusValue)
                                },
                                modifier = Modifier.weight(0.7f)
                            )

                            // Reason Filter
                            EnhancedReturnsFilterDropdown(
                                label = "السبب",
                                value = selectedReason,
                                options = listOf("الكل", "معيب", "منتج خاطئ", "تغيير رأي العميل", "منتهي الصلاحية", "تضرر أثناء الشحن", "أخرى"),
                                onValueChange = { selectedReason = it },
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
                            EnhancedReturnsFilterDropdown(
                                label = "ترتيب حسب",
                                value = when(sortBy) {
                                    "date" -> "التاريخ"
                                    "amount" -> "المبلغ"
                                    "status" -> "الحالة"
                                    "customer" -> "العميل"
                                    else -> "التاريخ"
                                },
                                options = listOf("التاريخ", "المبلغ", "الحالة", "العميل"),
                                onValueChange = {
                                    sortBy = when(it) {
                                        "التاريخ" -> "date"
                                        "المبلغ" -> "amount"
                                        "الحالة" -> "status"
                                        "العميل" -> "customer"
                                        else -> "date"
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Enhanced Quick Filters with complete hover coverage
                            val pendingOnlyInteractionSource = remember { MutableInteractionSource() }
                            val isPendingOnlyHovered by pendingOnlyInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = when {
                                            showPendingOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            isPendingOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (showPendingOnly) 1.5.dp else if (isPendingOnlyHovered) 1.dp else 0.5.dp,
                                        color = when {
                                            showPendingOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            isPendingOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = pendingOnlyInteractionSource,
                                        indication = null
                                    ) { showPendingOnly = !showPendingOnly },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    if (showPendingOnly) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "في الانتظار فقط",
                                        color = when {
                                            showPendingOnly -> MaterialTheme.colorScheme.primary
                                            isPendingOnlyHovered -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            val refundableOnlyInteractionSource = remember { MutableInteractionSource() }
                            val isRefundableOnlyHovered by refundableOnlyInteractionSource.collectIsHoveredAsState()

                            Box(
                                modifier = Modifier
                                    .height(56.dp) // Match dropdown height
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        color = when {
                                            showRefundableOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            isRefundableOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (showRefundableOnly) 1.5.dp else if (isRefundableOnlyHovered) 1.dp else 0.5.dp,
                                        color = when {
                                            showRefundableOnly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            isRefundableOnlyHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = refundableOnlyInteractionSource,
                                        indication = null
                                    ) { showRefundableOnly = !showRefundableOnly },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    if (showRefundableOnly) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "قابل للاسترداد",
                                        color = when {
                                            showRefundableOnly -> MaterialTheme.colorScheme.primary
                                            isRefundableOnlyHovered -> MaterialTheme.colorScheme.onSurface
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
                                    ) { if (!isExporting) { /* handleExportExcel() */ } },
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
                            ReturnTab.RETURNS -> EnhancedReturnsContent(
                                returns = returns,
                                isLoading = isLoading,
                                totalReturns = totalReturns,
                                pendingReturns = pendingReturns,
                                totalRefundAmount = totalRefundAmount,
                                returnRate = returnRate,
                                searchQuery = searchQuery,
                                selectedStatus = selectedStatusFromVM,
                                selectedReason = selectedReason,
                                showPendingOnly = showPendingOnly,
                                showRefundableOnly = showRefundableOnly,
                                sortBy = sortBy,
                                onReturnClick = { returnItem ->
                                    viewModel.selectReturn(returnItem)
                                    showReturnDetails = true
                                },
                                onEditReturn = { returnItem ->
                                    editingReturn = returnItem
                                },
                                onDeleteReturn = { returnItem ->
                                    returnToDelete = returnItem
                                    showDeleteConfirmation = true
                                },
                                onLoadMore = {
                                    coroutineScope.launch {
                                        viewModel.loadMoreReturns()
                                    }
                                },
                                onRefresh = {
                                    coroutineScope.launch {
                                        viewModel.refreshData()
                                    }
                                }
                            )
                            ReturnTab.ANALYTICS -> EnhancedReturnsAnalyticsContent()
                            ReturnTab.POLICIES -> EnhancedReturnsPoliciesContent()
                        }
                    }
                }

                // Right Panel - Details and Statistics (when return selected)
                AnimatedVisibility(
                    visible = showReturnDetails && selectedReturn != null,
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
                        selectedReturn?.let { returnItem ->
                            EnhancedReturnDetailsPanel(
                                returnItem = returnItem,
                                onEdit = {
                                    editingReturn = returnItem
                                    showReturnDetails = false
                                },
                                onDelete = {
                                    returnToDelete = returnItem
                                    showDeleteConfirmation = true
                                    showReturnDetails = false
                                },
                                onClose = {
                                    showReturnDetails = false
                                    viewModel.selectReturn(null)
                                },
                                onGeneratePdf = { returnItem ->
                                    coroutineScope.launch {
                                        isGeneratingPdf = true
                                        try {
                                            val receiptsDir = services.ReturnReceiptService.getReceiptsDirectory()
                                            val fileName = services.ReturnReceiptService.generateReturnReceiptFilename(returnItem.id?.toInt() ?: 0)
                                            val pdfFile = File(receiptsDir, fileName)

                                            val success = services.ReturnReceiptService.generateReturnReceipt(
                                                returnItem = returnItem,
                                                outputFile = pdfFile
                                            )

                                            if (success) {
                                                generatedPdfFile = pdfFile
                                                showPdfViewer = true
                                                exportMessage = "تم إنشاء إيصال الإرجاع بنجاح"
                                            } else {
                                                exportMessage = "خطأ في إنشاء إيصال الإرجاع"
                                            }
                                        } catch (e: Exception) {
                                            exportMessage = "خطأ في إنشاء إيصال الإرجاع: ${e.message}"
                                        } finally {
                                            isGeneratingPdf = false
                                        }
                                    }
                                },
                                isGeneratingPdf = isGeneratingPdf
                            )
                        }
                    }
                }
            }

            // Export status message
            exportMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("نجاح"))
                            AppTheme.colors.success.copy(alpha = 0.9f)
                        else
                            AppTheme.colors.error.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (message.contains("نجاح")) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { exportMessage = null }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "إغلاق",
                                tint = Color.White
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
        if (showNewReturnDialog) {
            EnhancedNewReturnDialog(
                isLoading = isCreatingReturn,
                onDismiss = { showNewReturnDialog = false },
                onConfirm = { returnData ->
                    coroutineScope.launch {
                        val result = viewModel.createReturn(returnData)
                        result.onSuccess {
                            showNewReturnDialog = false
                            snackbarHostState.showSnackbar("تم إضافة المرتجع بنجاح")
                        }.onError { exception ->
                            snackbarHostState.showSnackbar("خطأ في إضافة المرتجع: ${exception.message}")
                        }
                    }
                }
            )
        }

        if (editingReturn != null) {
            EnhancedEditReturnDialog(
                returnItem = editingReturn!!,
                isLoading = isUpdatingReturn,
                onDismiss = { editingReturn = null },
                onSave = { returnItem ->
                    coroutineScope.launch {
                        val result = viewModel.updateReturn(returnItem.id!!, returnItem)
                        result.onSuccess {
                            editingReturn = null
                            snackbarHostState.showSnackbar("تم تحديث المرتجع بنجاح")
                        }.onError { exception ->
                            snackbarHostState.showSnackbar("خطأ في تحديث المرتجع: ${exception.message}")
                        }
                    }
                }
            )
        }

        if (showDeleteConfirmation && returnToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                    returnToDelete = null
                },
                title = { Text("تأكيد الحذف") },
                text = { Text("هل أنت متأكد من حذف هذا المرتجع؟") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val result = viewModel.deleteReturn(returnToDelete!!.id!!)
                                result.onSuccess {
                                    showDeleteConfirmation = false
                                    returnToDelete = null
                                    snackbarHostState.showSnackbar("تم حذف المرتجع بنجاح")
                                }.onError { exception ->
                                    snackbarHostState.showSnackbar("خطأ في حذف المرتجع: ${exception.message}")
                                }
                            }
                        },
                        enabled = !isDeletingReturn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (isDeletingReturn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("حذف")
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteConfirmation = false
                            returnToDelete = null
                        },
                        enabled = !isDeletingReturn
                    ) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // Error handling
        error?.let { errorMessage ->
            LaunchedEffect(errorMessage) {
                snackbarHostState.showSnackbar(errorMessage)
                viewModel.clearError()
            }
        }

        // Success feedback
        lastCreatedReturn?.let {
            LaunchedEffect(it) {
                snackbarHostState.showSnackbar("تم إنشاء المرتجع بنجاح")
                viewModel.clearLastCreatedReturn()
            }
        }

        lastUpdatedReturn?.let {
            LaunchedEffect(it) {
                snackbarHostState.showSnackbar("تم تحديث المرتجع بنجاح")
                viewModel.clearLastUpdatedReturn()
            }
        }

        lastDeletedReturnId?.let {
            LaunchedEffect(it) {
                snackbarHostState.showSnackbar("تم حذف المرتجع بنجاح")
                viewModel.clearLastDeletedReturnId()
            }
        }

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
                            val printResult = utils.FileDialogUtils.printFile(pdfFile)
                            when (printResult) {
                                is utils.FileDialogUtils.PrintResult.Success -> {
                                    exportMessage = "تم إرسال الملف للطباعة بنجاح"
                                }
                                is utils.FileDialogUtils.PrintResult.NoAssociatedApp,
                                is utils.FileDialogUtils.PrintResult.NotSupported,
                                is utils.FileDialogUtils.PrintResult.Error -> {
                                    utils.FileDialogUtils.openWithSystemDefault(pdfFile)
                                    exportMessage = "تم فتح الملف للطباعة اليدوية"
                                }
                            }
                        }
                    },
                    onDownload = {
                        coroutineScope.launch {
                            try {
                                val defaultFileName = pdfFile.nameWithoutExtension + "_copy.pdf"
                                val selectedFile = utils.FileDialogUtils.selectPdfSaveFile(defaultFileName)

                                if (selectedFile != null) {
                                    pdfFile.copyTo(selectedFile, overwrite = true)
                                    exportMessage = "تم حفظ الملف بنجاح"
                                    try {
                                        utils.FileDialogUtils.openFolder(selectedFile.parentFile)
                                    } catch (e: Exception) {
                                        // Ignore if can't open folder
                                    }
                                }
                            } catch (e: Exception) {
                                exportMessage = "خطأ في حفظ الملف: ${e.message}"
                            }
                        }
                    }
                )
            }
        }

        // Full Screen PDF Viewer
        if (showFullScreenPdfViewer && generatedPdfFile != null) {
            ui.screens.PdfViewerFullScreen(
                pdfFile = generatedPdfFile!!,
                onBack = {
                    showFullScreenPdfViewer = false
                }
            )
        }
    }
}

// Enhanced Tab Row Component
@Composable
private fun EnhancedReturnsTabRow(
    selectedTab: ReturnTab,
    onTabSelected: (ReturnTab) -> Unit
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
            ReturnTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            when (tab) {
                                ReturnTab.RETURNS -> Icons.AutoMirrored.Filled.AssignmentReturn
                                ReturnTab.ANALYTICS -> Icons.Default.Analytics
                                ReturnTab.POLICIES -> Icons.Default.Policy
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }
}

// Enhanced Filter Dropdown Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedReturnsFilterDropdown(
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
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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

// Enhanced Returns Content
@Composable
private fun EnhancedReturnsContent(
    returns: List<ReturnDTO>,
    isLoading: Boolean,
    totalReturns: Int,
    pendingReturns: Int,
    totalRefundAmount: Double,
    returnRate: Double,
    searchQuery: String,
    selectedStatus: String?,
    selectedReason: String,
    showPendingOnly: Boolean,
    showRefundableOnly: Boolean,
    sortBy: String,
    onReturnClick: (ReturnDTO) -> Unit,
    onEditReturn: (ReturnDTO) -> Unit,
    onDeleteReturn: (ReturnDTO) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit
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
                EnhancedReturnsStatCard(
                    title = "إجمالي المرتجعات",
                    value = totalReturns.toString(),
                    subtitle = "مرتجع مسجل",
                    icon = Icons.AutoMirrored.Filled.AssignmentReturn,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                EnhancedReturnsStatCard(
                    title = "في الانتظار",
                    value = pendingReturns.toString(),
                    subtitle = "يحتاج معالجة",
                    icon = Icons.Default.Schedule,
                    iconColor = AppTheme.colors.warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedReturnsStatCard(
                    title = "تم الاسترداد",
                    value = "${String.format("%.2f", totalRefundAmount)} ر.س",
                    subtitle = "إجمالي المبلغ",
                    icon = Icons.Default.AccountBalance,
                    iconColor = AppTheme.colors.success,
                    modifier = Modifier.weight(1f)
                )

                EnhancedReturnsStatCard(
                    title = "معدل المرتجعات",
                    value = "${String.format("%.1f", returnRate)}%",
                    subtitle = "من إجمالي المبيعات",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    iconColor = AppTheme.colors.info,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Returns
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "قائمة المرتجعات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Loading state
        if (isLoading && returns.isEmpty()) {
            items(5) {
                EnhancedReturnCardSkeleton()
            }
        } else if (returns.isEmpty()) {
            // Empty state
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.AssignmentReturn,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "لا توجد مرتجعات",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "لم يتم العثور على أي مرتجعات مطابقة للبحث",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Real returns data
            items(returns) { returnItem ->
                EnhancedReturnCardFromDTO(
                    returnItem = returnItem,
                    onClick = onReturnClick,
                    onEdit = onEditReturn,
                    onDelete = onDeleteReturn
                )
            }

            // Load more button
            item {
                Button(
                    onClick = onLoadMore,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تحميل المزيد")
                }
            }
        }
    }
}

// Enhanced Returns Analytics Content
@Composable
private fun EnhancedReturnsAnalyticsContent() {
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
                EnhancedReturnsStatCard(
                    title = "معدل الإرجاع الشهري",
                    value = "2.8%",
                    subtitle = "انخفاض 0.5%",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    iconColor = AppTheme.colors.success,
                    modifier = Modifier.weight(1f)
                )

                EnhancedReturnsStatCard(
                    title = "متوسط وقت المعالجة",
                    value = "1.5 يوم",
                    subtitle = "تحسن 12 ساعة",
                    icon = Icons.Default.Schedule,
                    iconColor = AppTheme.colors.info,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedReturnsStatCard(
                    title = "رضا العملاء",
                    value = "4.6/5",
                    subtitle = "زيادة 0.2",
                    icon = Icons.Default.Star,
                    iconColor = AppTheme.colors.warning,
                    modifier = Modifier.weight(1f)
                )

                EnhancedReturnsStatCard(
                    title = "أكثر الأسباب",
                    value = "معيب",
                    subtitle = "45% من المرتجعات",
                    icon = Icons.Default.BugReport,
                    iconColor = AppTheme.colors.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "تحليل الأداء",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Performance metrics would go here
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "تقرير الأداء الشهري",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "سيتم إضافة الرسوم البيانية والتحليلات هنا",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Enhanced Returns Policies Content
@Composable
private fun EnhancedReturnsPoliciesContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "سياسات الإرجاع والاستبدال",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Policy Cards
        item {
            EnhancedPolicyCard(
                title = "فترة الإرجاع",
                description = "يمكن إرجاع المنتجات خلال 30 يوماً من تاريخ الشراء",
                icon = Icons.Default.Schedule,
                iconColor = AppTheme.colors.info
            )
        }

        item {
            EnhancedPolicyCard(
                title = "شروط الإرجاع",
                description = "يجب أن يكون المنتج في حالته الأصلية مع العبوة والفاتورة",
                icon = Icons.Default.CheckCircle,
                iconColor = AppTheme.colors.success
            )
        }

        item {
            EnhancedPolicyCard(
                title = "رسوم الإرجاع",
                description = "لا توجد رسوم إضافية للإرجاع في حالة عيب المنتج",
                icon = Icons.Default.AttachMoney,
                iconColor = AppTheme.colors.warning
            )
        }

        item {
            EnhancedPolicyCard(
                title = "طريقة الاسترداد",
                description = "يتم الاسترداد بنفس طريقة الدفع الأصلية خلال 3-5 أيام عمل",
                icon = Icons.Default.AccountBalance,
                iconColor = AppTheme.colors.purple
            )
        }
    }
}

// Enhanced Returns Stat Card Component
@Composable
private fun EnhancedReturnsStatCard(
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
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Legacy functions removed to fix compilation errors

// Enhanced Policy Card Component
@Composable
private fun EnhancedPolicyCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = iconColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(12.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Enhanced Return Details Panel Component
@Composable
private fun EnhancedReturnDetailsPanel(
    returnItem: ReturnDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    onGeneratePdf: (ReturnDTO) -> Unit = {},
    isGeneratingPdf: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تفاصيل المرتجع",
                style = MaterialTheme.typography.titleLarge,
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

        // Return Information
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "مرتجع #${returnItem.returnNumber ?: returnItem.id}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (returnItem.status) {
                            "PENDING" -> Icons.Default.Schedule
                            "APPROVED" -> Icons.Default.CheckCircle
                            "REJECTED" -> Icons.Default.Cancel
                            "REFUNDED" -> Icons.Default.AccountBalance
                            "EXCHANGED" -> Icons.Default.SwapHoriz
                            else -> Icons.Default.Schedule
                        },
                        contentDescription = null,
                        tint = when (returnItem.status) {
                            "PENDING" -> AppTheme.colors.warning
                            "APPROVED" -> AppTheme.colors.success
                            "REJECTED" -> AppTheme.colors.error
                            "REFUNDED" -> AppTheme.colors.info
                            "EXCHANGED" -> AppTheme.colors.purple
                            else -> AppTheme.colors.warning
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = when (returnItem.status) {
                            "PENDING" -> "في الانتظار"
                            "APPROVED" -> "موافق عليه"
                            "REJECTED" -> "مرفوض"
                            "REFUNDED" -> "تم الاسترداد"
                            "EXCHANGED" -> "تم الاستبدال"
                            else -> returnItem.status ?: "غير محدد"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Return Details
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow("رقم البيع الأصلي", returnItem.originalSaleNumber ?: "#${returnItem.originalSaleId}")
                    DetailRow("العميل", returnItem.customerName ?: "غير محدد")
                    DetailRow("تاريخ الإرجاع", returnItem.returnDate?.take(10) ?: "غير محدد")
                    DetailRow("سبب الإرجاع", getReasonDisplayName(returnItem.reason))
                    DetailRow("المبلغ الإجمالي", "${String.format("%.2f", returnItem.totalRefundAmount)} ر.س")
                    returnItem.refundMethod?.let { method ->
                        DetailRow("طريقة الاسترداد", when (method) {
                            "ORIGINAL_PAYMENT" -> "الطريقة الأصلية"
                            "STORE_CREDIT" -> "رصيد المتجر"
                            "CASH" -> "نقداً"
                            else -> method
                        })
                    }
                    if (!returnItem.notes.isNullOrBlank()) {
                        DetailRow("ملاحظات", returnItem.notes)
                    }
                    returnItem.processedBy?.let { processor ->
                        DetailRow("تمت المعالجة بواسطة", processor)
                    }
                    returnItem.processedDate?.let { date ->
                        DetailRow("تاريخ المعالجة", date.take(10))
                    }
                }
            }
        }

        // Items List
        Text(
            text = "العناصر المرتجعة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(returnItem.items ?: emptyList()) { item ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.productName ?: "منتج #${item.productId}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${item.returnQuantity} × ${String.format("%.2f", item.originalUnitPrice)} ر.س",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "الحالة: ${when (item.itemCondition) {
                                "NEW" -> "جديد"
                                "LIKE_NEW" -> "شبه جديد"
                                "GOOD" -> "جيد"
                                "FAIR" -> "مقبول"
                                "POOR" -> "ضعيف"
                                "DAMAGED" -> "تالف"
                                "DEFECTIVE" -> "معيب"
                                else -> item.itemCondition
                            }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.conditionNotes?.isNotEmpty() == true) {
                            Text(
                                text = "ملاحظات الحالة: ${item.conditionNotes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "إجمالي الاسترداد: ${String.format("%.2f", item.refundAmount)} ر.س",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First row - Edit and Delete
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
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تعديل")
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف")
                }
            }

            // Second row - PDF Generation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Generate PDF Receipt Button
                val pdfInteractionSource = remember { MutableInteractionSource() }
                val isPdfHovered by pdfInteractionSource.collectIsHoveredAsState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = when {
                                isGeneratingPdf -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                isPdfHovered && !isGeneratingPdf -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else -> MaterialTheme.colorScheme.surface
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = if (isPdfHovered && !isGeneratingPdf) 1.5.dp else 1.dp,
                            color = if (isPdfHovered && !isGeneratingPdf)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = pdfInteractionSource,
                            indication = null,
                            enabled = !isGeneratingPdf
                        ) {
                            if (!isGeneratingPdf) {
                                onGeneratePdf(returnItem)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isPdfHovered)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = if (isGeneratingPdf) "جاري الإنشاء..." else "إنشاء إيصال PDF",
                            color = when {
                                isGeneratingPdf -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                isPdfHovered -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Print Receipt Button (placeholder for future enhancement)
                OutlinedButton(
                    onClick = {
                        // Future: Direct print functionality
                        onGeneratePdf(returnItem)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isGeneratingPdf,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isGeneratingPdf)
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        else
                            Color.Transparent
                    )
                ) {
                    Icon(
                        Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("طباعة إيصال")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

// Legacy ReturnsListContent function removed

// Legacy ReturnCard function removed

// Old ReturnsAnalyticsContent function removed - replaced with enhanced version

// Old ReasonStatsItem function removed - replaced with enhanced version

// Removed duplicate MonthlyTrendItem function - using enhanced version below

// Old PerformanceMetricItem function removed - replaced with enhanced version

// Old ReturnPoliciesContent function removed - replaced with enhanced version

// Old PolicyItemCard function removed - replaced with enhanced version

// Enhanced New Return Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedNewReturnDialog(
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (ReturnDTO) -> Unit
) {
    // State for form fields
    var selectedCustomer by remember { mutableStateOf<CustomerDTO?>(null) }
    var selectedSale by remember { mutableStateOf<SaleDTO?>(null) }
    var selectedSaleItems by remember { mutableStateOf<List<SaleItemDTO>>(emptyList()) }
    var selectedReason by remember { mutableStateOf("DEFECTIVE") }
    var notes by remember { mutableStateOf("") }
    var refundMethod by remember { mutableStateOf("ORIGINAL_PAYMENT") }

    // State for dropdowns
    var showCustomerDropdown by remember { mutableStateOf(false) }
    var showSaleDropdown by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }
    var saleSearchQuery by remember { mutableStateOf("") }

    // Data loading states
    var customers by remember { mutableStateOf<List<CustomerDTO>>(emptyList()) }
    var sales by remember { mutableStateOf<List<SaleDTO>>(emptyList()) }
    var isLoadingCustomers by remember { mutableStateOf(false) }
    var isLoadingSales by remember { mutableStateOf(false) }

    // Load customers when dialog opens
    LaunchedEffect(Unit) {
        isLoadingCustomers = true
        try {
            val customerService = AppDependencies.container.customerApiService
            val result = customerService.getAllCustomers(page = 0, size = 100)
            result.onSuccess { pageResponse ->
                customers = pageResponse.content
            }.onError { exception ->
                println("Error loading customers: ${exception.message}")
            }
        } catch (e: Exception) {
            println("Error loading customers: ${e.message}")
        } finally {
            isLoadingCustomers = false
        }
    }

    // Load sales when customer is selected
    LaunchedEffect(selectedCustomer) {
        selectedCustomer?.let { customer ->
            isLoadingSales = true
            try {
                val salesService = AppDependencies.container.salesApiService
                val result = salesService.getSalesByCustomer(customer.id!!, page = 0, size = 50)
                result.onSuccess { pageResponse ->
                    // Filter for completed sales only
                    sales = pageResponse.content.filter { it.status == "COMPLETED" }
                }.onError { exception ->
                    println("Error loading sales: ${exception.message}")
                }
            } catch (e: Exception) {
                println("Error loading sales: ${e.message}")
            } finally {
                isLoadingSales = false
            }
        }
    }

    // Update sale items when sale is selected
    LaunchedEffect(selectedSale) {
        selectedSale?.let { sale ->
            selectedSaleItems = sale.items
        }
    }

    val refundMethods = listOf(
        "ORIGINAL_PAYMENT" to "طريقة الدفع الأصلية",
        "CASH" to "نقداً",
        "CREDIT_CARD" to "بطاقة ائتمان",
        "BANK_TRANSFER" to "تحويل بنكي",
        "STORE_CREDIT" to "رصيد المتجر"
    )

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
                        text = "إضافة مرتجع جديد",
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
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Selection
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
                        Text(
                            text = "اختيار العميل",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        ExposedDropdownMenuBox(
                            expanded = showCustomerDropdown,
                            onExpandedChange = { showCustomerDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = selectedCustomer?.name ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("العميل") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCustomerDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = showCustomerDropdown,
                                onDismissRequest = { showCustomerDropdown = false }
                            ) {
                                if (isLoadingCustomers) {
                                    DropdownMenuItem(
                                        text = { Text("جاري التحميل...") },
                                        onClick = { }
                                    )
                                } else {
                                    customers.forEach { customer ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(customer.name)
                                                    Text(
                                                        text = customer.phone ?: "",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCustomer = customer
                                                selectedSale = null // Reset sale selection
                                                showCustomerDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sale Selection (only show if customer is selected)
                if (selectedCustomer != null) {
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
                            Text(
                                text = "اختيار الفاتورة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            ExposedDropdownMenuBox(
                                expanded = showSaleDropdown,
                                onExpandedChange = { showSaleDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedSale?.let { "فاتورة #${it.id} - ${it.totalAmount} ريال" } ?: "",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("الفاتورة") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSaleDropdown) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = showSaleDropdown,
                                    onDismissRequest = { showSaleDropdown = false }
                                ) {
                                    if (isLoadingSales) {
                                        DropdownMenuItem(
                                            text = { Text("جاري التحميل...") },
                                            onClick = { }
                                        )
                                    } else if (sales.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("لا توجد فواتير مكتملة لهذا العميل") },
                                            onClick = { }
                                        )
                                    } else {
                                        sales.forEach { sale ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text("فاتورة #${sale.id}")
                                                        Text(
                                                            text = "${sale.totalAmount} ريال - ${sale.items.size} منتج",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedSale = sale
                                                    showSaleDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Return Details (only show if sale is selected)
                if (selectedSale != null) {
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
                            Text(
                                text = "تفاصيل الإرجاع",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Return Reason
                            var reasonExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = reasonExpanded,
                                onExpandedChange = { reasonExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = getReasonDisplayName(selectedReason),
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("سبب الإرجاع") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Help,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = reasonExpanded,
                                    onDismissRequest = { reasonExpanded = false }
                                ) {
                                    val reasons = listOf(
                                        "DEFECTIVE" to "معيب",
                                        "WRONG_ITEM" to "منتج خاطئ",
                                        "NOT_AS_DESCRIBED" to "لا يطابق الوصف",
                                        "CUSTOMER_CHANGE_MIND" to "تغيير رأي العميل",
                                        "DAMAGED_IN_SHIPPING" to "تضرر أثناء الشحن",
                                        "OTHER" to "أخرى"
                                    )
                                    reasons.forEach { (reason, displayName) ->
                                        DropdownMenuItem(
                                            text = { Text(displayName) },
                                            onClick = {
                                                selectedReason = reason
                                                reasonExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Refund Method
                            var refundExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = refundExpanded,
                                onExpandedChange = { refundExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = refundMethods.find { it.first == refundMethod }?.second ?: "طريقة الدفع الأصلية",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("طريقة الاسترداد") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AttachMoney,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = refundExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = refundExpanded,
                                    onDismissRequest = { refundExpanded = false }
                                ) {
                                    refundMethods.forEach { (method, displayName) ->
                                        DropdownMenuItem(
                                            text = { Text(displayName) },
                                            onClick = {
                                                refundMethod = method
                                                refundExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Notes
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("ملاحظات إضافية") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Notes,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Sale Items Selection
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
                            Text(
                                text = "المنتجات المراد إرجاعها",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            selectedSaleItems.forEach { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.productName ?: "منتج #${item.productId}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "الكمية: ${item.quantity} - السعر: ${item.unitPrice} ريال",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Checkbox(
                                            checked = true, // For now, all items are selected
                                            onCheckedChange = { /* TODO: Implement item selection */ }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedCustomer != null && selectedSale != null) {
                        // Create return items from selected sale items
                        val returnItems = selectedSaleItems.map { saleItem ->
                            ReturnItemDTO(
                                originalSaleItemId = saleItem.id ?: 0L,
                                productId = saleItem.productId,
                                productName = saleItem.productName,
                                returnQuantity = saleItem.quantity, // For now, return full quantity
                                originalUnitPrice = saleItem.unitPrice,
                                refundAmount = saleItem.totalPrice ?: (saleItem.unitPrice * saleItem.quantity),
                                itemCondition = "GOOD" // Default condition
                            )
                        }

                        val returnData = ReturnDTO(
                            originalSaleId = selectedSale!!.id!!,
                            customerId = selectedCustomer!!.id!!,
                            reason = selectedReason,
                            totalRefundAmount = returnItems.sumOf { it.refundAmount },
                            notes = notes,
                            refundMethod = refundMethod,
                            items = returnItems
                        )
                        onConfirm(returnData)
                    }
                },
                enabled = !isLoading && selectedCustomer != null && selectedSale != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "إضافة",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "إلغاء",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

// Enhanced Edit Return Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedEditReturnDialog(
    returnItem: ReturnDTO,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (ReturnDTO) -> Unit
) {
    var selectedReason by remember { mutableStateOf(returnItem.reason) }
    var selectedStatus by remember { mutableStateOf(returnItem.status ?: "PENDING") }
    var notes by remember { mutableStateOf(returnItem.notes ?: "") }
    var refundAmount by remember { mutableStateOf(returnItem.totalRefundAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تعديل المرتجع #${returnItem.id}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(400.dp)
            ) {
                item {
                    Text(
                        text = "معلومات المرتجع",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
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
                            Text(
                                text = "رقم البيع الأصلي: #${returnItem.originalSaleId}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "تاريخ الإرجاع: ${returnItem.returnDate?.take(10) ?: "غير محدد"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    var reasonExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = reasonExpanded,
                        onExpandedChange = { reasonExpanded = !reasonExpanded }
                    ) {
                        OutlinedTextField(
                            value = getReasonDisplayName(selectedReason),
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("سبب الإرجاع") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = reasonExpanded,
                            onDismissRequest = { reasonExpanded = false }
                        ) {
                            val reasons = listOf("DEFECTIVE", "WRONG_ITEM", "CUSTOMER_CHANGE_MIND", "EXPIRED", "DAMAGED_SHIPPING", "OTHER")
                            reasons.forEach { reason ->
                                DropdownMenuItem(
                                    text = { Text(getReasonDisplayName(reason)) },
                                    onClick = {
                                        selectedReason = reason
                                        reasonExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    var statusExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = when (selectedStatus) {
                                "PENDING" -> "في الانتظار"
                                "APPROVED" -> "موافق عليه"
                                "REJECTED" -> "مرفوض"
                                "REFUNDED" -> "تم الاسترداد"
                                "EXCHANGED" -> "تم الاستبدال"
                                else -> selectedStatus
                            },
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("حالة المرتجع") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            val statuses = listOf("PENDING", "APPROVED", "REJECTED", "REFUNDED", "EXCHANGED")
                            statuses.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(when (status) {
                                        "PENDING" -> "في الانتظار"
                                        "APPROVED" -> "موافق عليه"
                                        "REJECTED" -> "مرفوض"
                                        "REFUNDED" -> "تم الاسترداد"
                                        "EXCHANGED" -> "تم الاستبدال"
                                        else -> status
                                    }) },
                                    onClick = {
                                        selectedStatus = status
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = refundAmount,
                        onValueChange = { refundAmount = it },
                        label = { Text("مبلغ الاسترداد") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedReturn = returnItem.copy(
                        reason = selectedReason,
                        status = selectedStatus,
                        notes = notes,
                        totalRefundAmount = refundAmount.toDoubleOrNull() ?: returnItem.totalRefundAmount
                    )
                    onSave(updatedReturn)
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        "حفظ التغييرات",
                        fontWeight = FontWeight.Medium
                    )
                }
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

// Legacy ReturnDetailsDialog function removed

@Composable
fun ReturnDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Text(
        text = "$label:",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

data class ReasonStats(
    val name: String,
    val percentage: Int,
    val color: Color
)

data class PolicyItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class ReturnData(
    val originalSaleId: Int,
    val reason: ReturnReason,
    val notes: String
)

enum class ReturnTab(val title: String) {
    RETURNS("قائمة المرتجعات"),
    ANALYTICS("تحليلات المرتجعات"),
    POLICIES("سياسات الإرجاع")
}

// Enhanced Header Component with Tabs
@Composable
private fun EnhancedReturnsHeaderWithTabs(
    selectedTab: ReturnTab,
    onTabSelected: (ReturnTab) -> Unit,
    onNewReturn: () -> Unit,
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Enhanced gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isDesktop) 220.dp else 200.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isDesktop) 32.dp else 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Text with enhanced styling
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "إدارة المرتجعات والإلغاءات",
                        style = if (isDesktop) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "معالجة وتتبع طلبات الإرجاع والاستبدال بكفاءة عالية",
                        style = if (isDesktop) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tab Navigation
                ScrollableTabRow(
                    selectedTabIndex = ReturnTab.values().indexOf(selectedTab),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[ReturnTab.values().indexOf(selectedTab)]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    ReturnTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        )
                    }
                }

                // Action Buttons (only show on Returns tab)
                if (selectedTab == ReturnTab.RETURNS) {
                    RTLRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EnhancedActionButton(
                            text = "إرجاع جديد",
                            icon = Icons.Default.Add,
                            onClick = onNewReturn,
                            isPrimary = true
                        )
                        EnhancedActionButton(
                            text = "تصدير التقرير",
                            icon = Icons.Default.FileDownload,
                            onClick = { /* Export returns report */ },
                            isPrimary = false
                        )
                    }
                }
            }
        }
    }
}

// Returns Analytics Content
@Composable
private fun ReturnsAnalyticsContent(
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Analytics Overview Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isDesktop) 3 else 2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(if (isDesktop) 200.dp else 320.dp)
        ) {
            item {
                AnalyticsCard(
                    title = "معدل الإرجاع الشهري",
                    value = "2.8%",
                    change = "-0.5%",
                    isPositive = false,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = AppTheme.colors.success
                )
            }
            item {
                AnalyticsCard(
                    title = "متوسط وقت المعالجة",
                    value = "1.5 يوم",
                    change = "-12 ساعة",
                    isPositive = true,
                    icon = Icons.Default.Schedule,
                    color = AppTheme.colors.info
                )
            }
            item {
                AnalyticsCard(
                    title = "رضا العملاء",
                    value = "4.6/5",
                    change = "+0.2",
                    isPositive = true,
                    icon = Icons.Default.Star,
                    color = AppTheme.colors.warning
                )
            }
        }

        // Returns by Reason Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardStyles.elevatedCardColors(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardStyles.elevatedCardElevation()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "أسباب الإرجاع",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Reason breakdown
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReasonBreakdownItem("منتج معيب", 35, AppTheme.colors.error)
                    ReasonBreakdownItem("تغيير رأي العميل", 28, AppTheme.colors.warning)
                    ReasonBreakdownItem("منتج خاطئ", 20, AppTheme.colors.info)
                    ReasonBreakdownItem("تضرر أثناء الشحن", 12, AppTheme.colors.purple)
                    ReasonBreakdownItem("أخرى", 5, MaterialTheme.colorScheme.outline)
                }
            }
        }

        // Monthly Trend
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardStyles.elevatedCardColors(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardStyles.elevatedCardElevation()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "اتجاه المرتجعات الشهرية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MonthlyTrendItem("يناير", 45, MaterialTheme.colorScheme.primary)
                    MonthlyTrendItem("فبراير", 38, AppTheme.colors.success)
                    MonthlyTrendItem("مارس", 52, AppTheme.colors.warning)
                    MonthlyTrendItem("أبريل", 41, AppTheme.colors.info)
                }
            }
        }
    }
}

// Returns Policies Content
@Composable
private fun ReturnsPoliciesContent(
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Policy Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardStyles.elevatedCardColors(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardStyles.elevatedCardElevation()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "سياسات الإرجاع والاستبدال",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "إدارة وتحديث سياسات الإرجاع لضمان تجربة عملاء متميزة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Policy Items
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isDesktop) 2 else 1),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(if (isDesktop) 400.dp else 600.dp)
        ) {
            item {
                PolicyCard(
                    title = "فترة الإرجاع",
                    description = "يمكن إرجاع المنتجات خلال 30 يوماً من تاريخ الشراء",
                    icon = Icons.Default.Schedule,
                    color = AppTheme.colors.info
                )
            }
            item {
                PolicyCard(
                    title = "حالة المنتج",
                    description = "يجب أن يكون المنتج في حالته الأصلية مع العبوة",
                    icon = Icons.Default.Inventory,
                    color = AppTheme.colors.success
                )
            }
            item {
                PolicyCard(
                    title = "طريقة الاسترداد",
                    description = "يتم الاسترداد بنفس طريقة الدفع الأصلية",
                    icon = Icons.Default.Payment,
                    color = AppTheme.colors.warning
                )
            }
            item {
                PolicyCard(
                    title = "المنتجات المستثناة",
                    description = "بعض المنتجات غير قابلة للإرجاع لأسباب صحية",
                    icon = Icons.Default.Block,
                    color = AppTheme.colors.error
                )
            }
        }
    }
}

// Analytics Card Component
@Composable
private fun AnalyticsCard(
    title: String,
    value: String,
    change: String,
    isPositive: Boolean,
    icon: ImageVector,
    color: Color,
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
            // Gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.02f),
                                color.copy(alpha = 0.08f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = color
                    )

                    Surface(
                        color = if (isPositive) AppTheme.colors.success.copy(alpha = 0.1f)
                               else AppTheme.colors.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = change,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) AppTheme.colors.success else AppTheme.colors.error,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
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

// Reason Breakdown Item
@Composable
private fun ReasonBreakdownItem(
    reason: String,
    percentage: Int,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

// Monthly Trend Item
@Composable
fun MonthlyTrendItem(
    month: String,
    value: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height((value * 2).dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                )
        )

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = month,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Policy Card Component
@Composable
private fun PolicyCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = color
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

// Enhanced Search and Filters Component
@Composable
private fun EnhancedSearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statusOptions = listOf("الكل", "معلق", "مكتمل", "مرفوض", "تم الاسترداد")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("البحث في المرتجعات") },
                placeholder = { Text("رقم المرتجع، اسم العميل، أو المنتج...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Status Filter
            Text(
                text = "تصفية حسب الحالة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(statusOptions) { status ->
                    FilterChip(
                        onClick = { onStatusChange(status) },
                        label = { Text(status) },
                        selected = selectedStatus == status,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

// Enhanced Statistics Component
@Composable
private fun EnhancedReturnsStatistics(
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "إحصائيات المرتجعات",
            style = if (isDesktop) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (isDesktop) {
            // Desktop: 4 cards in a row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    ModernStatCard(
                        title = "إجمالي المرتجعات",
                        value = "156",
                        subtitle = "طلب إرجاع",
                        icon = Icons.AutoMirrored.Filled.AssignmentReturn,
                        iconColor = MaterialTheme.colorScheme.primary,
                        trend = "+12 هذا الشهر"
                    )
                }
                item {
                    ModernStatCard(
                        title = "في الانتظار",
                        value = "23",
                        subtitle = "طلب معلق",
                        icon = Icons.Default.Schedule,
                        iconColor = AppTheme.colors.warning,
                        trend = "+5 اليوم"
                    )
                }
                item {
                    ModernStatCard(
                        title = "تم الاسترداد",
                        value = UiUtils.formatCurrency(12500.0),
                        subtitle = "قيمة مستردة",
                        icon = Icons.Default.AccountBalance,
                        iconColor = AppTheme.colors.success,
                        trend = "+8.5%"
                    )
                }
                item {
                    ModernStatCard(
                        title = "معدل المرتجعات",
                        value = "3.2%",
                        subtitle = "من إجمالي المبيعات",
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        iconColor = AppTheme.colors.info,
                        trend = "-0.3%"
                    )
                }
            }
        } else {
            // Mobile/Tablet: 2 cards per row
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(320.dp)
            ) {
                item {
                    ModernStatCard(
                        title = "إجمالي المرتجعات",
                        value = "156",
                        subtitle = "طلب إرجاع",
                        icon = Icons.AutoMirrored.Filled.AssignmentReturn,
                        iconColor = MaterialTheme.colorScheme.primary,
                        trend = "+12"
                    )
                }
                item {
                    ModernStatCard(
                        title = "في الانتظار",
                        value = "23",
                        subtitle = "طلب معلق",
                        icon = Icons.Default.Schedule,
                        iconColor = AppTheme.colors.warning,
                        trend = "+5"
                    )
                }
                item {
                    ModernStatCard(
                        title = "تم الاسترداد",
                        value = "12.5K ر.س",
                        subtitle = "قيمة مستردة",
                        icon = Icons.Default.AccountBalance,
                        iconColor = AppTheme.colors.success,
                        trend = "+8.5%"
                    )
                }
                item {
                    ModernStatCard(
                        title = "معدل المرتجعات",
                        value = "3.2%",
                        subtitle = "من المبيعات",
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        iconColor = AppTheme.colors.info,
                        trend = "-0.3%"
                    )
                }
            }
        }
    }
}

// Modern Stat Card Component
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
                // Icon and trend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
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
                            icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = iconColor
                        )
                    }

                    Surface(
                        color = if (trend.startsWith("+")) AppTheme.colors.success.copy(alpha = 0.1f)
                               else AppTheme.colors.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = trend,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (trend.startsWith("+")) AppTheme.colors.success else AppTheme.colors.error,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Value and title
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// Legacy EnhancedReturnsList function removed

// Legacy EnhancedReturnCard function removed

// Enhanced Action Button Component with complete hover coverage
@Composable
private fun EnhancedActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = modifier
            .height(56.dp) // Match dropdown height for consistency
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = when {
                    isPrimary && isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    isPrimary -> MaterialTheme.colorScheme.primary
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = when {
                    isPrimary && isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    isPrimary -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        RTLRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = when {
                    isPrimary -> MaterialTheme.colorScheme.onPrimary
                    isHovered -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isPrimary -> MaterialTheme.colorScheme.onPrimary
                    isHovered -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// Enhanced Empty State Component
@Composable
private fun EnhancedEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompact) 64.dp else 80.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(if (isCompact) 16.dp else 20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(if (isCompact) 32.dp else 40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun getReturnStatusColor(status: ReturnStatus): Color {
    return when (status) {
        ReturnStatus.PENDING -> AppTheme.colors.warning
        ReturnStatus.APPROVED -> AppTheme.colors.info
        ReturnStatus.REJECTED -> AppTheme.colors.error
        ReturnStatus.REFUNDED -> AppTheme.colors.success
        ReturnStatus.EXCHANGED -> MaterialTheme.colorScheme.primary
    }
}

// Enhanced Return Card for DTO
@Composable
private fun EnhancedReturnCardFromDTO(
    returnItem: ReturnDTO,
    onClick: (ReturnDTO) -> Unit,
    onEdit: (ReturnDTO) -> Unit,
    onDelete: (ReturnDTO) -> Unit,
    onGeneratePdf: (ReturnDTO) -> Unit = {},
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
                color = if (isHovered)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = if (isHovered) 1.5.dp else 1.dp,
                color = if (isHovered)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick(returnItem) }
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
                        text = "مرتجع #${returnItem.returnNumber ?: returnItem.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "العميل: ${returnItem.customerName ?: "غير محدد"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onEdit(returnItem) },
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
                        onClick = { onGeneratePdf(returnItem) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "إنشاء PDF",
                            tint = AppTheme.colors.info,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDelete(returnItem) },
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

            // Status and Reason
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (returnItem.status) {
                            "PENDING" -> AppTheme.colors.warning.copy(alpha = 0.2f)
                            "APPROVED" -> AppTheme.colors.success.copy(alpha = 0.2f)
                            "REJECTED" -> AppTheme.colors.error.copy(alpha = 0.2f)
                            "REFUNDED" -> AppTheme.colors.info.copy(alpha = 0.2f)
                            "EXCHANGED" -> AppTheme.colors.purple.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (returnItem.status) {
                            "PENDING" -> "في الانتظار"
                            "APPROVED" -> "موافق عليه"
                            "REJECTED" -> "مرفوض"
                            "REFUNDED" -> "تم الاسترداد"
                            "EXCHANGED" -> "تم الاستبدال"
                            else -> returnItem.status ?: "غير محدد"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when (returnItem.status) {
                            "PENDING" -> AppTheme.colors.warning
                            "APPROVED" -> AppTheme.colors.success
                            "REJECTED" -> AppTheme.colors.error
                            "REFUNDED" -> AppTheme.colors.info
                            "EXCHANGED" -> AppTheme.colors.purple
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "${String.format("%.2f", returnItem.totalRefundAmount)} ر.س",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Additional info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "السبب: ${getReasonDisplayName(returnItem.reason)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = returnItem.returnDate?.take(10) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Notes if available
            returnItem.notes?.let { notes ->
                if (notes.isNotEmpty()) {
                    Text(
                        text = "ملاحظات: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Skeleton loading card
@Composable
private fun EnhancedReturnCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(20.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            // Status and amount skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}

// Helper function to get reason display name
private fun getReasonDisplayName(reason: String): String {
    return when (reason) {
        "DEFECTIVE" -> "معيب"
        "WRONG_ITEM" -> "منتج خاطئ"
        "CUSTOMER_CHANGE_MIND" -> "تغيير رأي العميل"
        "EXPIRED" -> "منتهي الصلاحية"
        "DAMAGED_SHIPPING" -> "تضرر أثناء الشحن"
        "OTHER" -> "أخرى"
        else -> reason
    }
}
