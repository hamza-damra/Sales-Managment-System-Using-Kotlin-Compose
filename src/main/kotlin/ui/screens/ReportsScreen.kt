package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ui.components.*
import ui.theme.AppColors
import ui.theme.CardStyles
import ui.theme.AppTheme

@Composable
fun ReportsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SectionHeader(
            title = "التقارير والإحصائيات",
            subtitle = "تحليل شامل لأداء المبيعات والمخزون"
        )

        // Report type selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                ReportCard(
                    title = "تقرير المبيعات",
                    description = "تحليل المبيعات اليومية والشهرية",
                    icon = Icons.Default.TrendingUp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                ReportCard(
                    title = "تقرير المخزون",
                    description = "حالة المخزون والمنتجات",
                    icon = Icons.Default.Inventory,
                    color = AppTheme.colors.success
                )
            }
            item {
                ReportCard(
                    title = "تقرير العملاء",
                    description = "إحصائيات وتحليل العملاء",
                    icon = Icons.Default.People,
                    color = AppTheme.colors.info
                )
            }
            item {
                ReportCard(
                    title = "التقرير المالي",
                    description = "الأرباح والخسائر والتدفق النقدي",
                    icon = Icons.Default.AccountBalance,
                    color = AppTheme.colors.warning
                )
            }
        }

        // Charts and analytics section with RTL layout
        RTLRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.weight(2f),
                colors = CardStyles.defaultCardColors(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardStyles.defaultCardElevation()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "مخطط المبيعات الشهرية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Placeholder for chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "مخطط المبيعات",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardStyles.elevatedCardColors(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardStyles.elevatedCardElevation()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "الإحصائيات السريعة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    QuickStatsItem("إجمالي المبيعات اليوم", "1,250 ر.س", AppTheme.colors.success)
                    QuickStatsItem("عدد الفواتير", "45", MaterialTheme.colorScheme.primary)
                    QuickStatsItem("متوسط قيمة الفاتورة", "278 ر.س", AppTheme.colors.info)
                    QuickStatsItem("أفضل منتج", "هاتف ذكي", AppTheme.colors.warning)
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { /* Navigate to detailed report */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Colored indicator at the top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )

            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
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

@Composable
private fun QuickStatsItem(
    label: String,
    value: String,
    color: Color
) {
    RTLRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
