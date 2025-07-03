package ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ui.components.*
import ui.theme.AppTheme
import ui.theme.LocalThemeState
import ui.theme.ThemeMode

@Composable
fun SettingsScreen() {
    val themeState = LocalThemeState.current
    var showThemeDialog by remember { mutableStateOf(false) }

    RTLProvider {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SectionHeader(
                title = "الإعدادات",
                subtitle = "إدارة إعدادات النظام والتطبيق"
            )

            // Settings sections with RTL support
            SettingsSection(
                title = "الحساب والأمان",
                items = listOf(
                    SettingsItem("معلومات الحساب", "تحديث البيانات الشخصية", Icons.Default.AccountCircle),
                    SettingsItem("كلمة المرور", "تغيير كلمة المرور", Icons.Default.Lock),
                    SettingsItem("إعدادات الأمان", "تفعيل المصادقة الثنائية", Icons.Default.Security)
                )
            )

            SettingsSection(
                title = "التطبيق",
                items = listOf(
                    SettingsItem("اللغة", "العربية", Icons.Default.Language),
                    SettingsItem("السمة", getThemeDisplayName(themeState.themeMode), Icons.Default.Palette) {
                        showThemeDialog = true
                    },
                    SettingsItem("الإشعارات", "إدارة التنبيهات", Icons.Default.Notifications)
                )
            )

            SettingsSection(
                title = "النظام",
                items = listOf(
                    SettingsItem("النسخ الاحتياطي", "حفظ واستعادة البيانات", Icons.Default.Backup),
                    SettingsItem("التصدير", "تصدير البيانات", Icons.Default.Download),
                    SettingsItem("حول التطبيق", "معلومات الإصدار", Icons.Default.Info)
                )
            )
        }
    }

    // Theme selection dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeState.themeMode,
            onThemeSelected = { newTheme ->
                themeState.setThemeMode(newTheme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "اختيار السمة",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.values().forEach { themeMode ->
                    ThemeOptionRow(
                        themeMode = themeMode,
                        isSelected = currentTheme == themeMode,
                        onSelected = { onThemeSelected(themeMode) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun ThemeOptionRow(
    themeMode: ThemeMode,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
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

@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            items.forEach { item ->
                SettingsItemRow(item = item)
                if (item != items.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }
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
