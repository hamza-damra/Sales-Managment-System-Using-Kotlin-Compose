package ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.utils.ResponsiveUtils
import UiUtils

@Composable
fun PromotionsScreen() {
    // State management
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("الكل") }
    var showNewPromotionDialog by remember { mutableStateOf(false) }
    var showNewCouponDialog by remember { mutableStateOf(false) }

    // Responsive design
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsivePadding = ResponsiveUtils.getResponsivePadding()
    val isDesktop = ResponsiveUtils.isDesktop()

    RTLProvider {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(responsivePadding.screen),
                verticalArrangement = Arrangement.spacedBy(responsive.large)
            ) {
                // Enhanced Header Section
                item {
                    EnhancedPromotionsHeader(
                        onAddPromotion = { showNewPromotionDialog = true },
                        onAddCoupon = { showNewCouponDialog = true },
                        isDesktop = isDesktop
                    )
                }

                // Search and Filters Section
                item {
                    EnhancedSearchAndFilters(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        selectedStatus = selectedStatus,
                        onStatusChange = { selectedStatus = it }
                    )
                }

                // Enhanced Statistics Dashboard
                item {
                    EnhancedPromotionsStatistics(isDesktop = isDesktop)
                }

                // Enhanced Active Promotions Section
                item {
                    EnhancedActivePromotions(
                        searchQuery = searchQuery,
                        selectedStatus = selectedStatus
                    )
                }

                // Enhanced Expired Promotions Section
                item {
                    EnhancedExpiredPromotions()
                }

                // Analytics Section
                item {
                    EnhancedPromotionAnalytics(isDesktop = isDesktop)
                }
            }
        }

        // Dialogs
        if (showNewPromotionDialog) {
            NewPromotionDialog(
                onDismiss = { showNewPromotionDialog = false },
                onSave = {
                    // Handle save promotion
                    showNewPromotionDialog = false
                }
            )
        }

        if (showNewCouponDialog) {
            NewCouponDialog(
                onDismiss = { showNewCouponDialog = false },
                onSave = {
                    // Handle save coupon
                    showNewCouponDialog = false
                }
            )
        }
    }
}

// Enhanced Header Component
@Composable
private fun EnhancedPromotionsHeader(
    onAddPromotion: () -> Unit,
    onAddCoupon: () -> Unit,
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
                    .height(if (isDesktop) 200.dp else 180.dp)
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header content
                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "العروض والخصومات",
                            style = if (isDesktop) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "إدارة العروض الترويجية وكوبونات الخصم بكفاءة عالية",
                            style = if (isDesktop) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Header icon
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.LocalOffer,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Action buttons
                RTLRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onAddPromotion,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إضافة عرض جديد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    OutlinedButton(
                        onClick = onAddCoupon,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إضافة كوبون",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Enhanced Search and Filters Component
@Composable
private fun EnhancedSearchAndFilters(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit
) {
    val responsive = ResponsiveUtils.getResponsiveSpacing()
    val responsiveCorners = ResponsiveUtils.getResponsiveCornerRadius()

    Card(
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(responsiveCorners.large),
        elevation = CardStyles.defaultCardElevation(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "البحث والتصفية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("البحث في العروض والكوبونات") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "مسح",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            // Status filter chips
            Text(
                text = "تصفية حسب الحالة",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                val statuses = listOf("الكل", "نشط", "منتهي", "مجدول", "متوقف")
                items(statuses) { status ->
                    FilterChip(
                        onClick = { onStatusChange(status) },
                        label = { Text(status) },
                        selected = selectedStatus == status,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

// Enhanced Statistics Dashboard
@Composable
private fun EnhancedPromotionsStatistics(
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "إحصائيات العروض والخصومات",
            style = MaterialTheme.typography.titleLarge,
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
                    ModernPromotionStatCard(
                        title = "إجمالي العروض",
                        value = "24",
                        subtitle = "عرض مسجل",
                        icon = Icons.Default.LocalOffer,
                        iconColor = MaterialTheme.colorScheme.primary,
                        trend = "+3 هذا الشهر"
                    )
                }
                item {
                    ModernPromotionStatCard(
                        title = "العروض النشطة",
                        value = "12",
                        subtitle = "عرض نشط",
                        icon = Icons.Default.TrendingUp,
                        iconColor = AppTheme.colors.success,
                        trend = "+50%"
                    )
                }
                item {
                    ModernPromotionStatCard(
                        title = "إجمالي الخصومات",
                        value = UiUtils.formatCurrency(45600.0),
                        subtitle = "قيمة الخصومات",
                        icon = Icons.Default.Discount,
                        iconColor = AppTheme.colors.warning,
                        trend = "+15.2%"
                    )
                }
                item {
                    ModernPromotionStatCard(
                        title = "معدل الاستخدام",
                        value = "78%",
                        subtitle = "من العروض",
                        icon = Icons.Default.Analytics,
                        iconColor = AppTheme.colors.info,
                        trend = "+5.8%"
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
                    ModernPromotionStatCard(
                        title = "إجمالي العروض",
                        value = "24",
                        subtitle = "عرض مسجل",
                        icon = Icons.Default.LocalOffer,
                        iconColor = MaterialTheme.colorScheme.primary,
                        trend = "+3"
                    )
                }
                item {
                    ModernPromotionStatCard(
                        title = "العروض النشطة",
                        value = "12",
                        subtitle = "عرض نشط",
                        icon = Icons.Default.TrendingUp,
                        iconColor = AppTheme.colors.success,
                        trend = "+50%"
                    )
                }
                item {
                    ModernPromotionStatCard(
                        title = "إجمالي الخصومات",
                        value = "45.6K ر.س",
                        subtitle = "قيمة الخصومات",
                        icon = Icons.Default.Discount,
                        iconColor = AppTheme.colors.warning,
                        trend = "+15.2%"
                    )
                }
                item {
                    ModernPromotionStatCard(
                        title = "معدل الاستخدام",
                        value = "78%",
                        subtitle = "من العروض",
                        icon = Icons.Default.Analytics,
                        iconColor = AppTheme.colors.info,
                        trend = "+5.8%"
                    )
                }
            }
        }
    }
}

// Modern Promotion Stat Card Component
@Composable
private fun ModernPromotionStatCard(
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
                // Header with icon and trend
                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = iconColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = iconColor
                            )
                        }
                    }

                    // Trend indicator
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (trend.startsWith("+")) AppTheme.colors.success.copy(alpha = 0.1f)
                               else AppTheme.colors.error.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = trend,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (trend.startsWith("+")) AppTheme.colors.success
                                   else AppTheme.colors.error
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
                        style = MaterialTheme.typography.titleMedium,
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

// Enhanced Active Promotions Section
@Composable
private fun EnhancedActivePromotions(
    searchQuery: String,
    selectedStatus: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AppTheme.colors.success.copy(alpha = 0.01f),
                                AppTheme.colors.success.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section header
                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "العروض النشطة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "العروض والخصومات المتاحة حالياً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppTheme.colors.success.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = AppTheme.colors.success
                            )
                        }
                    }
                }

                // Sample active promotions
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(3) { index ->
                        EnhancedPromotionCard(
                            title = "خصم ${(index + 1) * 15}%",
                            description = "خصم على جميع المنتجات في فئة ${if (index == 0) "الإلكترونيات" else if (index == 1) "الملابس" else "المنزل والحديقة"}",
                            validUntil = "صالح حتى 31/12/2024",
                            discountValue = "${(index + 1) * 15}%",
                            usageCount = "${(index + 1) * 45}",
                            isActive = true,
                            promotionType = if (index % 2 == 0) "نسبة مئوية" else "مبلغ ثابت"
                        )
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) AppTheme.colors.success.copy(alpha = 0.3f)
                   else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            hoveredElevation = 4.dp
        )
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

// Enhanced Expired Promotions Section
@Composable
private fun EnhancedExpiredPromotions(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardStyles.defaultCardElevation()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AppTheme.colors.error.copy(alpha = 0.01f),
                                AppTheme.colors.error.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section header
                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "العروض المنتهية",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "العروض والخصومات المنتهية الصلاحية",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppTheme.colors.error.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = AppTheme.colors.error
                            )
                        }
                    }
                }

                // Sample expired promotions
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(2) { index ->
                        EnhancedPromotionCard(
                            title = "عرض الصيف ${index + 1}",
                            description = "عرض خاص لفصل الصيف على منتجات مختارة",
                            validUntil = "انتهى في 30/09/2024",
                            discountValue = "${(index + 1) * 20}%",
                            usageCount = "${(index + 1) * 120}",
                            isActive = false,
                            promotionType = "عرض موسمي"
                        )
                    }
                }
            }
        }
    }
}

// Enhanced Promotion Analytics Section
@Composable
private fun EnhancedPromotionAnalytics(
    isDesktop: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardStyles.elevatedCardElevation()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Subtle gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AppTheme.colors.info.copy(alpha = 0.01f),
                                AppTheme.colors.info.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section header
                RTLRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "تحليلات العروض",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تحليل أداء العروض والخصومات",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { /* View detailed analytics */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.info
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تقرير مفصل",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Analytics content placeholder
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "أفضل العروض أداءً",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    repeat(3) { index ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
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
        }
    }
}

// New Promotion Dialog
@Composable
private fun NewPromotionDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة عرض جديد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "سيتم تنفيذ نموذج إضافة العرض الجديد قريباً",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Placeholder for form fields
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
                            text = "الحقول المطلوبة:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text("• اسم العرض", style = MaterialTheme.typography.bodySmall)
                        Text("• نوع الخصم", style = MaterialTheme.typography.bodySmall)
                        Text("• قيمة الخصم", style = MaterialTheme.typography.bodySmall)
                        Text("• تاريخ البداية والنهاية", style = MaterialTheme.typography.bodySmall)
                        Text("• المنتجات المشمولة", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

// New Coupon Dialog
@Composable
private fun NewCouponDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة كوبون جديد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "سيتم تنفيذ نموذج إضافة الكوبون الجديد قريباً",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Placeholder for form fields
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
                            text = "الحقول المطلوبة:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text("• كود الكوبون", style = MaterialTheme.typography.bodySmall)
                        Text("• نوع الخصم", style = MaterialTheme.typography.bodySmall)
                        Text("• قيمة الخصم", style = MaterialTheme.typography.bodySmall)
                        Text("• عدد مرات الاستخدام", style = MaterialTheme.typography.bodySmall)
                        Text("• الحد الأدنى للطلب", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
