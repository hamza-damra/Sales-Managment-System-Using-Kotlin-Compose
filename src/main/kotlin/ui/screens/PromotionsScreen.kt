package ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles

@Composable
fun PromotionsScreen() {
    RTLProvider {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SectionHeader(
                title = "العروض والكوبونات",
                subtitle = "إدارة العروض الترويجية وكوبونات الخصم"
            )

            // Action buttons with RTL layout
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionButton(
                    text = "إضافة عرض جديد",
                    icon = Icons.Default.Add,
                    onClick = { /* Add new promotion */ }
                )
                QuickActionButton(
                    text = "إضافة كوبون",
                    icon = Icons.Default.LocalOffer,
                    onClick = { /* Add new coupon */ }
                )
            }

            // Active promotions section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardStyles.defaultCardColors(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardStyles.defaultCardElevation()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "العروض النشطة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Sample promotions with RTL layout
                    repeat(3) { index ->
                        PromotionCard(
                            title = "خصم ${(index + 1) * 10}%",
                            description = "خصم على جميع المنتجات في فئة الإلكترونيات",
                            validUntil = "صالح حتى 31/12/2024",
                            isActive = true
                        )
                        if (index < 2) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }

            // Expired/Inactive promotions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardStyles.elevatedCardColors(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardStyles.elevatedCardElevation()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "العروض المنتهية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    repeat(2) { index ->
                        PromotionCard(
                            title = "عرض الصيف ${index + 1}",
                            description = "عرض خاص لفصل الصيف",
                            validUntil = "انتهى في 30/09/2024",
                            isActive = false
                        )
                        if (index < 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromotionCard(
    title: String,
    description: String,
    validUntil: String,
    isActive: Boolean
) {
    RTLRow(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        RTLRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusChip(
                text = if (isActive) "نشط" else "منتهي",
                color = if (isActive) AppTheme.colors.success else AppTheme.colors.error
            )

            Column(
                horizontalAlignment = Alignment.Start
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
                Text(
                    text = validUntil,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) AppTheme.colors.success else AppTheme.colors.error
                )
            }
        }

        IconButton(
            onClick = { /* Edit promotion */ }
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "تعديل",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
