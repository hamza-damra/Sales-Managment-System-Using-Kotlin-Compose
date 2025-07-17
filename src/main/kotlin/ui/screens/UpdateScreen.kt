@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)

package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import data.api.*
import data.repository.UpdateRepository
import services.UpdateService
import ui.components.*
import ui.theme.CardStyles
import ui.viewmodels.UpdateViewModel
import utils.Constants
import utils.UpdateUtils
import kotlinx.coroutines.launch

/**
 * Comprehensive Update Screen with Material Design 3 and Arabic RTL support
 * Follows the established UI/UX patterns from SalesScreen.kt and other screens
 */
@Composable
fun UpdateScreen(
    updateRepository: UpdateRepository,
    updateService: UpdateService,
    notificationService: services.NotificationService
) {
    val updateViewModel = remember {
        UpdateViewModel(updateRepository, updateService)
    }

    // Collect state from ViewModel
    val updateState by updateViewModel.updateState.collectAsState()
    val updatePreferences by updateViewModel.updatePreferences.collectAsState()
    val updateHistory by updateViewModel.updateHistory.collectAsState()
    val uiState by updateViewModel.uiState.collectAsState()
    val latestVersionInfo by updateViewModel.latestVersionInfo.collectAsState()
    val compatibilityInfo by updateViewModel.compatibilityInfo.collectAsState()
    val downloadProgress by updateViewModel.downloadProgress.collectAsState()
    val isPollingActive by updateViewModel.isPollingActive.collectAsState()

    val scope = rememberCoroutineScope()
    var showPreferencesDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showInstallConfirmDialog by remember { mutableStateOf(false) }
    var showRestartConfirmDialog by remember { mutableStateOf(false) }

    // Auto-check for updates on screen load
    LaunchedEffect(Unit) {
        if (updatePreferences.autoCheckEnabled) {
            updateViewModel.checkForUpdates()
        }
    }

    // Handle errors with snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            notificationService.showError(
                title = "خطأ في النظام",
                message = error
            )
            updateViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section
        UpdateScreenHeader(
            isPollingActive = isPollingActive,
            onShowPreferences = { showPreferencesDialog = true },
            onShowHistory = { showHistoryDialog = true }
        )

        // Development Mode Warning
        if (utils.UpdateUtils.isDevelopmentMode()) {
            DevelopmentModeWarningCard()
        }

        // Main Update Status Card
        UpdateStatusCard(
            updateState = updateState,
            latestVersionInfo = latestVersionInfo,
            isDownloading = uiState.isDownloading,
            isInstalling = uiState.isInstalling,
            downloadedFile = uiState.downloadedFile,
            onCheckForUpdates = {
                scope.launch {
                    updateViewModel.checkForUpdates()
                }
            },
            onDownloadUpdate = {
                latestVersionInfo?.let { versionInfo ->
                    scope.launch {
                        updateViewModel.downloadUpdate(versionInfo.versionNumber)
                    }
                }
            },
            onInstallUpdate = {
                showInstallConfirmDialog = true
            }
        )

        // Download Progress Card
        AnimatedVisibility(
            visible = uiState.isDownloading || downloadProgress != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            UpdateProgressCard(
                downloadProgress = downloadProgress,
                onCancelDownload = {
                    updateViewModel.cancelDownload()
                }
            )
        }

        // Installation Progress Card
        AnimatedVisibility(
            visible = uiState.isInstalling,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            InstallationProgressCard(
                versionInfo = latestVersionInfo
            )
        }

        // Restart Progress Card
        AnimatedVisibility(
            visible = uiState.isRestarting,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            RestartProgressCard(
                versionInfo = latestVersionInfo
            )
        }

        // Compatibility Information Card
        AnimatedVisibility(
            visible = compatibilityInfo != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            compatibilityInfo?.let { compatibility ->
                CompatibilityCard(compatibilityInfo = compatibility)
            }
        }

        // Version Information Card
        AnimatedVisibility(
            visible = latestVersionInfo != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            latestVersionInfo?.let { versionInfo ->
                VersionInfoCard(versionInfo = versionInfo)
            }
        }

        // Installation Result Card
        AnimatedVisibility(
            visible = uiState.installationResult != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            uiState.installationResult?.let { result ->
                InstallationResultCard(
                    result = result,
                    onDismiss = {
                        updateViewModel.clearUpdateState()
                    },
                    onRestart = {
                        if (result.success && result.requiresRestart) {
                            showRestartConfirmDialog = true
                        }
                    }
                )
            }
        }

        // System Information Card
        SystemInfoCard(
            systemInfo = updateViewModel.getSystemInfo()
        )
    }

    // Preferences Dialog
    if (showPreferencesDialog) {
        UpdatePreferencesDialog(
            preferences = updatePreferences,
            onDismiss = { showPreferencesDialog = false },
            onSave = { newPreferences ->
                updateViewModel.updatePreferences(newPreferences)
                showPreferencesDialog = false
            }
        )
    }

    // History Dialog
    if (showHistoryDialog) {
        UpdateHistoryDialog(
            history = updateHistory,
            onDismiss = { showHistoryDialog = false }
        )
    }

    // Installation Confirmation Dialog
    if (showInstallConfirmDialog) {
        InstallConfirmationDialog(
            versionInfo = latestVersionInfo,
            onConfirm = {
                showInstallConfirmDialog = false
                uiState.downloadedFile?.let { file ->
                    latestVersionInfo?.let { versionInfo ->
                        scope.launch {
                            updateViewModel.installUpdate(file, versionInfo.versionNumber)
                        }
                    }
                }
            },
            onDismiss = { showInstallConfirmDialog = false }
        )
    }

    // Restart Confirmation Dialog
    if (showRestartConfirmDialog) {
        RestartConfirmationDialog(
            versionInfo = latestVersionInfo,
            onConfirm = {
                showRestartConfirmDialog = false
                scope.launch {
                    updateViewModel.restartApplication()
                }
            },
            onDismiss = { showRestartConfirmDialog = false }
        )
    }
}

@Composable
private fun UpdateScreenHeader(
    isPollingActive: Boolean,
    onShowPreferences: () -> Unit,
    onShowHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.defaultCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(Constants.UI.CARD_CORNER_RADIUS.dp)
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
                    text = "إدارة التحديثات",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPollingActive) Icons.Filled.RadioButtonChecked else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isPollingActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = if (isPollingActive) "الفحص التلقائي نشط" else "الفحص التلقائي متوقف",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onShowHistory) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "سجل التحديثات",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onShowPreferences) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "إعدادات التحديث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionInfoCard(
    versionInfo: ApplicationVersionDTO,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.defaultCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(Constants.UI.CARD_CORNER_RADIUS.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "معلومات الإصدار",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = UpdateUtils.getReleaseChannelDisplayName(versionInfo.releaseChannel),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            // Version details
            UpdateInfoRow(
                label = "رقم الإصدار",
                value = versionInfo.versionNumber,
                icon = Icons.Outlined.Tag
            )
            
            UpdateInfoRow(
                label = "تاريخ الإصدار",
                value = UpdateUtils.formatTimestamp(versionInfo.releaseDate),
                icon = Icons.Outlined.CalendarToday
            )
            
            UpdateInfoRow(
                label = "حجم الملف",
                value = versionInfo.formattedFileSize,
                icon = Icons.Outlined.Storage
            )
            
            if (versionInfo.isMandatory) {
                UpdateStatusIndicator(
                    text = "تحديث إجباري",
                    icon = Icons.Filled.Warning,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            // Release notes
            if (versionInfo.releaseNotes.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ملاحظات الإصدار:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = versionInfo.releaseNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemInfoCard(
    systemInfo: SystemInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.defaultCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(Constants.UI.CARD_CORNER_RADIUS.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "معلومات النظام",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Icon(
                    imageVector = Icons.Outlined.Computer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // System details
            UpdateInfoRow(
                label = "نظام التشغيل",
                value = "${systemInfo.osName} ${systemInfo.osVersion}",
                icon = Icons.Outlined.Computer
            )
            
            UpdateInfoRow(
                label = "معمارية النظام",
                value = systemInfo.osArch,
                icon = Icons.Outlined.Architecture
            )
            
            UpdateInfoRow(
                label = "إصدار Java",
                value = systemInfo.javaVersion,
                icon = Icons.Outlined.Code
            )
            
            UpdateInfoRow(
                label = "مورد Java",
                value = systemInfo.javaVendor,
                icon = Icons.Outlined.Business
            )
            
            UpdateInfoRow(
                label = "الذاكرة المتاحة",
                value = "${UpdateUtils.formatFileSize(systemInfo.availableMemoryMB * 1024 * 1024)} / ${UpdateUtils.formatFileSize(systemInfo.totalMemoryMB * 1024 * 1024)}",
                icon = Icons.Outlined.Memory
            )
        }
    }
}

@Composable
private fun InstallationResultCard(
    result: UpdateInstallationResult,
    onDismiss: () -> Unit,
    onRestart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.success) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(Constants.UI.CARD_CORNER_RADIUS.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (result.success) "تم التثبيت بنجاح" else "فشل التثبيت",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (result.success) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = if (result.success) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Result details
            if (result.success) {
                Text(
                    text = "تم تثبيت الإصدار ${result.version} بنجاح في ${UpdateUtils.formatDuration(result.installationTimeMs / 1000)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (result.requiresRestart) {
                    Text(
                        text = "⚠️ يرجى إعادة تشغيل التطبيق لتطبيق التحديث",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRestart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعادة التشغيل الآن")
                    }
                }
            } else {
                Text(
                    text = result.errorMessage ?: "حدث خطأ غير معروف أثناء التثبيت",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun DevelopmentModeWarningCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(Constants.UI.CARD_CORNER_RADIUS.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Code,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(24.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "وضع التطوير",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                Text(
                    text = "يتم تشغيل التطبيق في وضع التطوير. سيتم محاكاة عمليات التحديث وإعادة التشغيل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}
