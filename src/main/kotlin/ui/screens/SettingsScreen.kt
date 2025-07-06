package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import ui.components.*
import ui.theme.AppTheme
import ui.theme.LocalThemeState
import ui.theme.ThemeMode

// Settings Tab Enum
enum class SettingsTab(val title: String) {
    GENERAL("الإعدادات العامة"),
    ACCOUNT("الحساب والأمان"),
    SYSTEM("النظام والبيانات"),
    ABOUT("حول التطبيق")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val themeState = LocalThemeState.current

    // Enhanced state management
    var selectedTab by remember { mutableStateOf(SettingsTab.GENERAL) }
    var searchQuery by remember { mutableStateOf("") }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }

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
            // Left Panel - Settings Management
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
                        Column {
                            Text(
                                text = "إعدادات النظام",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "إدارة إعدادات التطبيق والنظام",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Enhanced Tabs
                    EnhancedSettingsTabRow(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("البحث في الإعدادات") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content based on selected tab
                    when (selectedTab) {
                        SettingsTab.GENERAL -> EnhancedGeneralSettingsContent(
                            searchQuery = searchQuery,
                            onThemeClick = { showThemeDialog = true },
                            onLanguageClick = { showLanguageDialog = true },
                            onNotificationClick = { showNotificationDialog = true }
                        )
                        SettingsTab.ACCOUNT -> EnhancedAccountSettingsContent(
                            searchQuery = searchQuery,
                            onAccountClick = { showAccountDialog = true },
                            onPasswordClick = { showPasswordDialog = true },
                            onSecurityClick = { showSecurityDialog = true }
                        )
                        SettingsTab.SYSTEM -> EnhancedSystemSettingsContent(
                            searchQuery = searchQuery,
                            onBackupClick = { showBackupDialog = true },
                            onExportClick = { showExportDialog = true }
                        )
                        SettingsTab.ABOUT -> EnhancedAboutContent(
                            onAboutClick = { showAboutDialog = true }
                        )
                    }
                }
            }

            // Right Panel - Quick Actions and Info (when needed)
            AnimatedVisibility(
                visible = selectedTab == SettingsTab.ABOUT,
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
                    EnhancedAboutPanel()
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
    if (showThemeDialog) {
        EnhancedThemeSelectionDialog(
            currentTheme = themeState.themeMode,
            onThemeSelected = { newTheme ->
                themeState.setThemeMode(newTheme)
                showThemeDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تغيير السمة بنجاح")
                }
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showLanguageDialog) {
        EnhancedLanguageSelectionDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { language ->
                showLanguageDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تغيير اللغة بنجاح")
                }
            }
        )
    }

    if (showNotificationDialog) {
        EnhancedNotificationSettingsDialog(
            onDismiss = { showNotificationDialog = false },
            onSave = {
                showNotificationDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم حفظ إعدادات الإشعارات")
                }
            }
        )
    }

    if (showBackupDialog) {
        EnhancedBackupDialog(
            onDismiss = { showBackupDialog = false },
            onBackup = {
                showBackupDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم إنشاء النسخة الاحتياطية بنجاح")
                }
            }
        )
    }

    if (showExportDialog) {
        EnhancedExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = {
                showExportDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تصدير البيانات بنجاح")
                }
            }
        )
    }

    if (showAccountDialog) {
        EnhancedAccountDialog(
            onDismiss = { showAccountDialog = false },
            onSave = {
                showAccountDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تحديث معلومات الحساب")
                }
            }
        )
    }

    if (showPasswordDialog) {
        EnhancedPasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onSave = {
                showPasswordDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تغيير كلمة المرور بنجاح")
                }
            }
        )
    }

    if (showSecurityDialog) {
        EnhancedSecurityDialog(
            onDismiss = { showSecurityDialog = false },
            onSave = {
                showSecurityDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("تم تحديث إعدادات الأمان")
                }
            }
        )
    }
}

// Enhanced Tab Row Component
@Composable
private fun EnhancedSettingsTabRow(
    selectedTab: SettingsTab,
    onTabSelected: (SettingsTab) -> Unit
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
            SettingsTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    icon = {
                        Icon(
                            when (tab) {
                                SettingsTab.GENERAL -> Icons.Default.Tune
                                SettingsTab.ACCOUNT -> Icons.Default.AccountCircle
                                SettingsTab.SYSTEM -> Icons.Default.Storage
                                SettingsTab.ABOUT -> Icons.Default.Info
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

// Enhanced General Settings Content
@Composable
private fun EnhancedGeneralSettingsContent(
    searchQuery: String,
    onThemeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "الإعدادات العامة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            EnhancedSettingsCard(
                title = "المظهر والسمة",
                description = "تخصيص مظهر التطبيق",
                icon = Icons.Default.Palette,
                iconColor = AppTheme.colors.purple,
                onClick = onThemeClick
            )
        }

        item {
            EnhancedSettingsCard(
                title = "اللغة والمنطقة",
                description = "تغيير لغة التطبيق والإعدادات الإقليمية",
                icon = Icons.Default.Language,
                iconColor = AppTheme.colors.info,
                onClick = onLanguageClick
            )
        }

        item {
            EnhancedSettingsCard(
                title = "الإشعارات",
                description = "إدارة التنبيهات والإشعارات",
                icon = Icons.Default.Notifications,
                iconColor = AppTheme.colors.warning,
                onClick = onNotificationClick
            )
        }

        item {
            EnhancedSettingsCard(
                title = "إعدادات العرض",
                description = "حجم الخط وكثافة العرض",
                icon = Icons.Default.DisplaySettings,
                iconColor = AppTheme.colors.success,
                onClick = { /* Handle display settings */ }
            )
        }

        item {
            EnhancedSettingsCard(
                title = "الاختصارات",
                description = "تخصيص اختصارات لوحة المفاتيح",
                icon = Icons.Default.Keyboard,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = { /* Handle keyboard shortcuts */ }
            )
        }
    }
}

// Enhanced Account Settings Content
@Composable
private fun EnhancedAccountSettingsContent(
    searchQuery: String,
    onAccountClick: () -> Unit,
    onPasswordClick: () -> Unit,
    onSecurityClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "الحساب والأمان",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            EnhancedSettingsCard(
                title = "معلومات الحساب",
                description = "تحديث البيانات الشخصية والمعلومات",
                icon = Icons.Default.AccountCircle,
                iconColor = AppTheme.colors.info,
                onClick = onAccountClick
            )
        }

        item {
            EnhancedSettingsCard(
                title = "كلمة المرور",
                description = "تغيير كلمة المرور الحالية",
                icon = Icons.Default.Lock,
                iconColor = AppTheme.colors.warning,
                onClick = onPasswordClick
            )
        }

        item {
            EnhancedSettingsCard(
                title = "إعدادات الأمان",
                description = "المصادقة الثنائية وإعدادات الحماية",
                icon = Icons.Default.Security,
                iconColor = AppTheme.colors.error,
                onClick = onSecurityClick
            )
        }

        item {
            EnhancedSettingsCard(
                title = "جلسات النشاط",
                description = "عرض وإدارة جلسات تسجيل الدخول",
                icon = Icons.Default.DeviceHub,
                iconColor = AppTheme.colors.purple,
                onClick = { /* Handle active sessions */ }
            )
        }

        item {
            EnhancedSettingsCard(
                title = "سجل النشاط",
                description = "عرض سجل العمليات والأنشطة",
                icon = Icons.Default.History,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = { /* Handle activity log */ }
            )
        }
    }
}

// Enhanced System Settings Content
@Composable
private fun EnhancedSystemSettingsContent(
    searchQuery: String,
    onBackupClick: () -> Unit,
    onExportClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "النظام والبيانات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            EnhancedSettingsCard(
                title = "النسخ الاحتياطي",
                description = "إنشاء واستعادة النسخ الاحتياطية",
                icon = Icons.Default.Backup,
                iconColor = AppTheme.colors.success,
                onClick = onBackupClick
            )
        }

        item {
            EnhancedSettingsCard(
                title = "تصدير البيانات",
                description = "تصدير البيانات بصيغ مختلفة",
                icon = Icons.Default.Download,
                iconColor = AppTheme.colors.info,
                onClick = onExportClick
            )
        }

        item {
            EnhancedSettingsCard(
                title = "إدارة قاعدة البيانات",
                description = "صيانة وتحسين قاعدة البيانات",
                icon = Icons.Default.Storage,
                iconColor = AppTheme.colors.warning,
                onClick = { /* Handle database management */ }
            )
        }

        item {
            EnhancedSettingsCard(
                title = "التحديثات",
                description = "فحص وتثبيت التحديثات",
                icon = Icons.Default.SystemUpdate,
                iconColor = AppTheme.colors.purple,
                onClick = { /* Handle updates */ }
            )
        }

        item {
            EnhancedSettingsCard(
                title = "إعدادات الأداء",
                description = "تحسين أداء التطبيق",
                icon = Icons.Default.Speed,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = { /* Handle performance settings */ }
            )
        }
    }
}

@Composable
private fun EnhancedThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختيار السمة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeMode.values().forEach { themeMode ->
                    EnhancedThemeOptionRow(
                        themeMode = themeMode,
                        isSelected = currentTheme == themeMode,
                        onSelected = { onThemeSelected(themeMode) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إغلاق")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ThemeOptionRow(
    themeMode: ThemeMode,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = when {
                    isSelected -> 2.dp
                    isHovered -> 1.5.dp
                    else -> 1.dp
                },
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onSelected() }
    ) {
        RTLRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RTLRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getThemeIcon(themeMode),
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = getThemeDisplayName(themeMode),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = getThemeDescription(themeMode),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "محدد",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Enhanced Settings Card Component
@Composable
private fun EnhancedSettingsCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
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
            ) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(14.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Enhanced Theme Option Row
@Composable
private fun EnhancedThemeOptionRow(
    themeMode: ThemeMode,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = when {
                    isSelected -> 2.dp
                    isHovered -> 1.5.dp
                    else -> 1.dp
                },
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onSelected() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = getThemeIcon(themeMode),
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(12.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = getThemeDisplayName(themeMode),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getThemeDescription(themeMode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "محدد",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

// Enhanced About Content
@Composable
private fun EnhancedAboutContent(
    onAboutClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "حول التطبيق",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            EnhancedSettingsCard(
                title = "معلومات التطبيق",
                description = "الإصدار والتفاصيل التقنية",
                icon = Icons.Default.Info,
                iconColor = AppTheme.colors.info,
                onClick = onAboutClick
            )
        }

        item {
            EnhancedSettingsCard(
                title = "الترخيص والشروط",
                description = "شروط الاستخدام وسياسة الخصوصية",
                icon = Icons.Default.Gavel,
                iconColor = AppTheme.colors.warning,
                onClick = { /* Handle license */ }
            )
        }

        item {
            EnhancedSettingsCard(
                title = "الدعم الفني",
                description = "التواصل مع فريق الدعم",
                icon = Icons.Default.Support,
                iconColor = AppTheme.colors.success,
                onClick = { /* Handle support */ }
            )
        }

        item {
            EnhancedSettingsCard(
                title = "التقييم والمراجعة",
                description = "قيم التطبيق واترك مراجعة",
                icon = Icons.Default.Star,
                iconColor = AppTheme.colors.warning,
                onClick = { /* Handle rating */ }
            )
        }
    }
}

// Enhanced About Panel
@Composable
private fun EnhancedAboutPanel() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Logo and Name
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(20.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "نظام إدارة المبيعات",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "الإصدار 1.0.0",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // App Info
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "معلومات التطبيق",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            InfoRow("تاريخ الإصدار", "يناير 2024")
            InfoRow("المطور", "فريق التطوير")
            InfoRow("الترخيص", "MIT License")
            InfoRow("التقنيات", "Kotlin Compose")
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Quick Stats
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "إحصائيات سريعة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            InfoRow("حجم التطبيق", "25.6 MB")
            InfoRow("آخر تحديث", "منذ 3 أيام")
            InfoRow("عدد المستخدمين", "1,250+")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Copyright
        Text(
            text = "© 2024 جميع الحقوق محفوظة",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InfoRow(
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

@Composable
private fun SettingsItemRow(item: SettingsItem) {
    RTLRow(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick?.invoke() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RTLRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

private data class SettingsItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: (() -> Unit)? = null
)

private fun getThemeDisplayName(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.LIGHT -> "فاتح"
        ThemeMode.DARK -> "داكن"
        ThemeMode.SYSTEM -> "حسب النظام"
    }
}

private fun getThemeDescription(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.LIGHT -> "سمة فاتحة دائماً"
        ThemeMode.DARK -> "سمة داكنة دائماً"
        ThemeMode.SYSTEM -> "يتبع إعدادات النظام"
    }
}

private fun getThemeIcon(themeMode: ThemeMode): ImageVector {
    return when (themeMode) {
        ThemeMode.LIGHT -> Icons.Default.LightMode
        ThemeMode.DARK -> Icons.Default.DarkMode
        ThemeMode.SYSTEM -> Icons.Default.AutoMode
    }
}

// Enhanced Language Selection Dialog
@Composable
private fun EnhancedLanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "العربية" to "ar",
        "English" to "en",
        "Français" to "fr",
        "Español" to "es"
    )
    var selectedLanguage by remember { mutableStateOf("العربية") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختيار اللغة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(languages) { (name, code) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLanguage = name },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedLanguage == name)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedLanguage == name) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedLanguage == name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )

                            if (selectedLanguage == name) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "محدد",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onLanguageSelected(selectedLanguage) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("تطبيق")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

// Enhanced Notification Settings Dialog
@Composable
private fun EnhancedNotificationSettingsDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var enableNotifications by remember { mutableStateOf(true) }
    var enableSounds by remember { mutableStateOf(true) }
    var enableVibration by remember { mutableStateOf(false) }
    var enableSalesAlerts by remember { mutableStateOf(true) }
    var enableInventoryAlerts by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إعدادات الإشعارات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(300.dp)
            ) {
                item {
                    NotificationToggleRow(
                        title = "تفعيل الإشعارات",
                        description = "تلقي جميع الإشعارات",
                        checked = enableNotifications,
                        onCheckedChange = { enableNotifications = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "الأصوات",
                        description = "تشغيل أصوات الإشعارات",
                        checked = enableSounds,
                        onCheckedChange = { enableSounds = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "الاهتزاز",
                        description = "اهتزاز عند الإشعارات",
                        checked = enableVibration,
                        onCheckedChange = { enableVibration = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "تنبيهات المبيعات",
                        description = "إشعارات العمليات التجارية",
                        checked = enableSalesAlerts,
                        onCheckedChange = { enableSalesAlerts = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "تنبيهات المخزون",
                        description = "إشعارات نفاد المخزون",
                        checked = enableInventoryAlerts,
                        onCheckedChange = { enableInventoryAlerts = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun NotificationToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// Enhanced Backup Dialog
@Composable
private fun EnhancedBackupDialog(
    onDismiss: () -> Unit,
    onBackup: () -> Unit
) {
    var includeProducts by remember { mutableStateOf(true) }
    var includeSales by remember { mutableStateOf(true) }
    var includeCustomers by remember { mutableStateOf(true) }
    var includeSettings by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إنشاء نسخة احتياطية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(250.dp)
            ) {
                item {
                    Text(
                        text = "اختر البيانات المراد نسخها:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "المنتجات",
                        description = "جميع بيانات المنتجات والمخزون",
                        checked = includeProducts,
                        onCheckedChange = { includeProducts = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "المبيعات",
                        description = "سجل المبيعات والفواتير",
                        checked = includeSales,
                        onCheckedChange = { includeSales = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "العملاء",
                        description = "قاعدة بيانات العملاء",
                        checked = includeCustomers,
                        onCheckedChange = { includeCustomers = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "الإعدادات",
                        description = "إعدادات التطبيق والتخصيصات",
                        checked = includeSettings,
                        onCheckedChange = { includeSettings = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onBackup,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("إنشاء النسخة")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

// Enhanced Export Dialog
@Composable
private fun EnhancedExportDialog(
    onDismiss: () -> Unit,
    onExport: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf("Excel") }
    var includeProducts by remember { mutableStateOf(true) }
    var includeSales by remember { mutableStateOf(true) }
    var includeCustomers by remember { mutableStateOf(false) }

    val formats = listOf("Excel", "CSV", "JSON", "PDF")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تصدير البيانات",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(300.dp)
            ) {
                item {
                    Text(
                        text = "تنسيق التصدير:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        formats.forEach { format ->
                            FilterChip(
                                selected = selectedFormat == format,
                                onClick = { selectedFormat = format },
                                label = { Text(format) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "البيانات المراد تصديرها:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "المنتجات",
                        description = "قائمة المنتجات والأسعار",
                        checked = includeProducts,
                        onCheckedChange = { includeProducts = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "المبيعات",
                        description = "تقارير المبيعات",
                        checked = includeSales,
                        onCheckedChange = { includeSales = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "العملاء",
                        description = "معلومات العملاء",
                        checked = includeCustomers,
                        onCheckedChange = { includeCustomers = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onExport,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تصدير")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

// Enhanced Account Dialog
@Composable
private fun EnhancedAccountDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var fullName by remember { mutableStateOf("أحمد محمد") }
    var email by remember { mutableStateOf("ahmed@example.com") }
    var phone by remember { mutableStateOf("+966501234567") }
    var company by remember { mutableStateOf("شركة التجارة المتقدمة") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "معلومات الحساب",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(300.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("الاسم الكامل") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("اسم الشركة") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ التغييرات")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

// Enhanced Password Dialog
@Composable
private fun EnhancedPasswordDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تغيير كلمة المرور",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(250.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("كلمة المرور الحالية") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                Icon(
                                    if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }

                item {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("كلمة المرور الجديدة") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }

                item {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("تأكيد كلمة المرور") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        isError = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword
                    )
                }

                if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    item {
                        Text(
                            text = "كلمات المرور غير متطابقة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmPassword,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("تغيير كلمة المرور")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

// Enhanced Security Dialog
@Composable
private fun EnhancedSecurityDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var enableTwoFactor by remember { mutableStateOf(false) }
    var enableLoginAlerts by remember { mutableStateOf(true) }
    var enableSessionTimeout by remember { mutableStateOf(true) }
    var sessionTimeoutMinutes by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إعدادات الأمان",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(300.dp)
            ) {
                item {
                    NotificationToggleRow(
                        title = "المصادقة الثنائية",
                        description = "تفعيل المصادقة بخطوتين",
                        checked = enableTwoFactor,
                        onCheckedChange = { enableTwoFactor = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "تنبيهات تسجيل الدخول",
                        description = "إشعار عند تسجيل دخول جديد",
                        checked = enableLoginAlerts,
                        onCheckedChange = { enableLoginAlerts = it }
                    )
                }

                item {
                    NotificationToggleRow(
                        title = "انتهاء الجلسة التلقائي",
                        description = "إنهاء الجلسة بعد فترة عدم نشاط",
                        checked = enableSessionTimeout,
                        onCheckedChange = { enableSessionTimeout = it }
                    )
                }

                if (enableSessionTimeout) {
                    item {
                        OutlinedTextField(
                            value = sessionTimeoutMinutes,
                            onValueChange = { sessionTimeoutMinutes = it },
                            label = { Text("مدة انتهاء الجلسة (بالدقائق)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حفظ الإعدادات")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("إلغاء")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
