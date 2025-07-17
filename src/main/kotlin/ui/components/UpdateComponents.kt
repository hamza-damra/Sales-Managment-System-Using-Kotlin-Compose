@file:OptIn(ExperimentalMaterial3Api::class)

package ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import data.api.*
import ui.theme.CardStyles
import utils.UpdateUtils
import utils.Constants
import java.io.File

/**
 * Reusable UI components for the update system
 * Following Material Design 3 and existing codebase patterns
 */

@Composable
fun UpdateStatusCard(
    updateState: UpdateState,
    latestVersionInfo: ApplicationVersionDTO?,
    onCheckForUpdates: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    modifier: Modifier = Modifier,
    isDownloading: Boolean = false,
    isInstalling: Boolean = false,
    downloadedFile: File? = null
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
                    text = "حالة التحديث",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Current version
            UpdateInfoRow(
                label = "الإصدار الحالي",
                value = updateState.currentVersion,
                icon = Icons.Outlined.Info
            )
            
            // Update status
            when {
                updateState.isChecking -> {
                    UpdateStatusIndicator(
                        text = "جاري فحص التحديثات...",
                        icon = Icons.Filled.Refresh,
                        color = MaterialTheme.colorScheme.primary,
                        isLoading = true
                    )
                }
                updateState.updateAvailable -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        UpdateStatusIndicator(
                            text = if (updateState.isMandatory) "تحديث إجباري متاح" else "تحديث جديد متاح",
                            icon = if (updateState.isMandatory) Icons.Filled.Warning else Icons.Filled.NewReleases,
                            color = if (updateState.isMandatory) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        
                        latestVersionInfo?.let { versionInfo ->
                            UpdateInfoRow(
                                label = "الإصدار الجديد",
                                value = versionInfo.versionNumber,
                                icon = Icons.Outlined.NewReleases
                            )
                            
                            UpdateInfoRow(
                                label = "حجم التحديث",
                                value = versionInfo.formattedFileSize,
                                icon = Icons.Outlined.Download
                            )
                        }
                        
                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when {
                                isInstalling -> {
                                    // Installing state - show progress
                                    Button(
                                        onClick = { },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("جاري التثبيت...")
                                    }
                                }
                                downloadedFile != null -> {
                                    // Download completed - show install button
                                    Button(
                                        onClick = onInstallUpdate,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.InstallMobile,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تثبيت وإعادة التشغيل")
                                    }
                                }
                                !isDownloading -> {
                                    // Ready to download
                                    Button(
                                        onClick = onDownloadUpdate,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تحميل")
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    UpdateStatusIndicator(
                        text = "لا توجد تحديثات متاحة",
                        icon = Icons.Filled.CheckCircle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Last check time
            updateState.lastCheckTime?.let { lastCheck ->
                Text(
                    text = "آخر فحص: ${UpdateUtils.formatTimestamp(lastCheck)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) TextAlign.Start else TextAlign.End
                )
            }
            
            // Check for updates button
            if (!updateState.isChecking && !isDownloading && !isInstalling) {
                OutlinedButton(
                    onClick = onCheckForUpdates,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فحص التحديثات")
                }
            }
        }
    }
}

@Composable
fun UpdateProgressCard(
    downloadProgress: DownloadProgress?,
    onCancelDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    downloadProgress?.let { progress ->
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
                        text = "تحميل التحديث",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(onClick = onCancelDownload) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "إلغاء التحميل",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Progress bar
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinearProgressIndicator(
                        progress = progress.percentage / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    // Progress text
                    Text(
                        text = UpdateUtils.generateProgressMessage(progress),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Progress details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${UpdateUtils.formatFileSize(progress.downloadedBytes)} / ${UpdateUtils.formatFileSize(progress.totalBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (progress.speedBytesPerSecond > 0) {
                        Text(
                            text = UpdateUtils.formatSpeed(progress.speedBytesPerSecond),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompatibilityCard(
    compatibilityInfo: CompatibilityCheckDTO,
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
                    text = "فحص التوافق",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Icon(
                    imageVector = if (compatibilityInfo.isCompatible) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = if (compatibilityInfo.isCompatible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Compatibility status
            UpdateStatusIndicator(
                text = if (compatibilityInfo.isCompatible) "متوافق مع النظام" else "غير متوافق مع النظام",
                icon = if (compatibilityInfo.isCompatible) Icons.Filled.CheckCircle else Icons.Filled.Error,
                color = if (compatibilityInfo.isCompatible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            
            // System information
            UpdateInfoRow(
                label = "نظام التشغيل",
                value = "${compatibilityInfo.operatingSystem.name ?: "غير محدد"} ${compatibilityInfo.operatingSystem.version ?: ""}".trim(),
                icon = Icons.Outlined.Computer
            )
            
            UpdateInfoRow(
                label = "إصدار Java",
                value = compatibilityInfo.javaVersion.detected ?: "غير محدد",
                icon = Icons.Outlined.Code
            )
            
            // Warning level
            if (compatibilityInfo.warningLevel != "NONE") {
                UpdateStatusIndicator(
                    text = "مستوى التحذير: ${UpdateUtils.getWarningLevelDisplayName(compatibilityInfo.warningLevel)}",
                    icon = when (compatibilityInfo.warningLevel) {
                        "INFO" -> Icons.Filled.Info
                        "WARNING" -> Icons.Filled.Warning
                        "CRITICAL" -> Icons.Filled.Error
                        else -> Icons.Filled.Info
                    },
                    color = when (compatibilityInfo.warningLevel) {
                        "INFO" -> MaterialTheme.colorScheme.primary
                        "WARNING" -> MaterialTheme.colorScheme.tertiary
                        "CRITICAL" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
            
            // Compatibility issues
            if (compatibilityInfo.compatibilityIssues.isNotEmpty()) {
                Text(
                    text = "مشاكل التوافق:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                compatibilityInfo.compatibilityIssues.forEach { issue ->
                    CompatibilityIssueItem(issue = issue)
                }
            }
        }
    }
}

@Composable
fun UpdateInfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun UpdateStatusIndicator(
    text: String,
    icon: ImageVector,
    color: Color,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = color,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun CompatibilityIssueItem(
    issue: CompatibilityIssueDTO,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (issue.severity) {
                "CRITICAL" -> MaterialTheme.colorScheme.errorContainer
                "WARNING" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (issue.severity) {
                        "CRITICAL" -> Icons.Filled.Error
                        "WARNING" -> Icons.Filled.Warning
                        else -> Icons.Filled.Info
                    },
                    contentDescription = null,
                    tint = when (issue.severity) {
                        "CRITICAL" -> MaterialTheme.colorScheme.error
                        "WARNING" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = UpdateUtils.getSeverityDisplayName(issue.severity),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = when (issue.severity) {
                        "CRITICAL" -> MaterialTheme.colorScheme.error
                        "WARNING" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
            
            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (issue.resolution.isNotBlank()) {
                Text(
                    text = "الحل: ${issue.resolution}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UpdatePreferencesDialog(
    preferences: UpdatePreferences,
    onDismiss: () -> Unit,
    onSave: (UpdatePreferences) -> Unit,
    modifier: Modifier = Modifier
) {
    var autoCheckEnabled by remember { mutableStateOf(preferences.autoCheckEnabled) }
    var checkIntervalMinutes by remember { mutableStateOf(preferences.checkIntervalMinutes.toString()) }
    var autoDownloadEnabled by remember { mutableStateOf(preferences.autoDownloadEnabled) }
    var notificationsEnabled by remember { mutableStateOf(preferences.notificationsEnabled) }
    var preferDifferentialUpdates by remember { mutableStateOf(preferences.preferDifferentialUpdates) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إعدادات التحديث",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Auto check enabled
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فحص تلقائي للتحديثات",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = autoCheckEnabled,
                        onCheckedChange = { autoCheckEnabled = it }
                    )
                }

                // Check interval
                if (autoCheckEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "فترة الفحص (بالدقائق)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = checkIntervalMinutes,
                            onValueChange = { checkIntervalMinutes = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("30") }
                        )
                    }
                }

                // Auto download
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تحميل تلقائي للتحديثات",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = autoDownloadEnabled,
                        onCheckedChange = { autoDownloadEnabled = it }
                    )
                }

                // Notifications
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إشعارات التحديث",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                }

                // Differential updates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تفضيل التحديثات التفاضلية",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = preferDifferentialUpdates,
                        onCheckedChange = { preferDifferentialUpdates = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val intervalMinutes = checkIntervalMinutes.toLongOrNull() ?: 30L
                    onSave(
                        preferences.copy(
                            autoCheckEnabled = autoCheckEnabled,
                            checkIntervalMinutes = intervalMinutes,
                            autoDownloadEnabled = autoDownloadEnabled,
                            notificationsEnabled = notificationsEnabled,
                            preferDifferentialUpdates = preferDifferentialUpdates
                        )
                    )
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun UpdateHistoryDialog(
    history: List<UpdateHistoryEntry>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "سجل التحديثات",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (history.isEmpty()) {
                Text(
                    text = "لا يوجد سجل للتحديثات",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history) { entry ->
                        UpdateHistoryItem(entry = entry)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
private fun UpdateHistoryItem(
    entry: UpdateHistoryEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.success) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإصدار ${entry.version}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (entry.success) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )

                Icon(
                    imageVector = if (entry.success) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = if (entry.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = UpdateUtils.getUpdateTypeDisplayName(entry.updateType),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.success) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )

                Text(
                    text = UpdateUtils.formatTimestamp(entry.updateDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.success) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )
            }

            if (entry.downloadSize > 0) {
                Text(
                    text = "الحجم: ${UpdateUtils.formatFileSize(entry.downloadSize)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (entry.success) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
                )
            }

            if (!entry.success && entry.errorMessage != null) {
                Text(
                    text = "خطأ: ${entry.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Installation Confirmation Dialog
 * Shows before proceeding with update installation
 */
@Composable
fun InstallConfirmationDialog(
    versionInfo: ApplicationVersionDTO?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "تأكيد التثبيت",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "هل أنت متأكد من أنك تريد تثبيت التحديث؟",
                    style = MaterialTheme.typography.bodyLarge
                )

                versionInfo?.let { version ->
                    Card(
                        colors = CardStyles.defaultCardColors(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UpdateInfoRow(
                                label = "الإصدار الجديد",
                                value = version.versionNumber,
                                icon = Icons.Outlined.NewReleases
                            )
                            UpdateInfoRow(
                                label = "حجم التحديث",
                                value = version.formattedFileSize,
                                icon = Icons.Outlined.Download
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "سيتم إعادة تشغيل التطبيق تلقائياً بعد التثبيت",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.InstallMobile,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("تثبيت الآن")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        modifier = modifier
    )
}

/**
 * Installation Progress Card
 * Shows progress during update installation
 */
@Composable
fun InstallationProgressCard(
    versionInfo: ApplicationVersionDTO?,
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
                    text = "جاري تثبيت التحديث",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = Icons.Filled.InstallMobile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Version info
            versionInfo?.let { version ->
                UpdateInfoRow(
                    label = "الإصدار",
                    value = version.versionNumber,
                    icon = Icons.Outlined.NewReleases
                )
            }

            // Progress indicator
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.secondaryContainer
                )

                Text(
                    text = "يرجى عدم إغلاق التطبيق أثناء التثبيت",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Installation steps
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InstallationStep(
                        text = "التحقق من سلامة الملف",
                        isActive = true
                    )
                    InstallationStep(
                        text = "إنشاء نسخة احتياطية",
                        isActive = true
                    )
                    InstallationStep(
                        text = "تثبيت الإصدار الجديد",
                        isActive = true
                    )
                    InstallationStep(
                        text = "إعادة تشغيل التطبيق",
                        isActive = false
                    )
                }
            }
        }
    }
}

/**
 * Installation Step Indicator
 */
@Composable
private fun InstallationStep(
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isActive) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            }
        )
    }
}

/**
 * Restart confirmation dialog for applying updates
 */
@Composable
fun RestartConfirmationDialog(
    versionInfo: ApplicationVersionDTO?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "إعادة تشغيل التطبيق",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "تم تثبيت التحديث بنجاح. يجب إعادة تشغيل التطبيق لتطبيق التحديث.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                versionInfo?.let { version ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "الإصدار الجديد: ${version.versionNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            if (version.releaseNotes.isNotBlank()) {
                                Text(
                                    text = version.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "⚠️ سيتم حفظ جميع البيانات تلقائياً قبل إعادة التشغيل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("إعادة التشغيل الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("لاحقاً")
            }
        },
        shape = RoundedCornerShape(Constants.UI.CARD_CORNER_RADIUS.dp)
    )
}

/**
 * Restart progress card showing restart status
 */
@Composable
fun RestartProgressCard(
    versionInfo: ApplicationVersionDTO?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(Constants.UI.CARD_CORNER_RADIUS.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = "جاري إعادة التشغيل...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Progress indicator
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )

            // Status text
            Text(
                text = "يتم الآن إعادة تشغيل التطبيق لتطبيق التحديث",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            versionInfo?.let { version ->
                Text(
                    text = "الإصدار الجديد: ${version.versionNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            // Warning text
            Text(
                text = "يرجى عدم إغلاق التطبيق أثناء عملية إعادة التشغيل",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
