package ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.theme.AppTheme
import ui.theme.CardStyles

/**
 * Professional shimmer loading components following Material Design 3 guidelines
 * with Arabic RTL support and configurable animations
 */

/**
 * Base shimmer animation configuration
 */
@Composable
private fun rememberShimmerAnimation(): InfiniteTransition {
    return rememberInfiniteTransition(label = "shimmer")
}

/**
 * Creates a shimmer brush with smooth animation
 */
@Composable
private fun createShimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    val transition = rememberShimmerAnimation()
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
}

/**
 * Base shimmer box component
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val shimmerBrush = createShimmerBrush()
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush)
    )
}

/**
 * Shimmer text placeholder
 */
@Composable
fun ShimmerText(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 16.dp,
    width: androidx.compose.ui.unit.Dp? = null
) {
    ShimmerBox(
        modifier = modifier
            .height(height)
            .then(
                if (width != null) Modifier.width(width)
                else Modifier.fillMaxWidth()
            ),
        shape = RoundedCornerShape(4.dp)
    )
}

/**
 * Shimmer icon placeholder
 */
@Composable
fun ShimmerIcon(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp,
    shape: androidx.compose.ui.graphics.Shape = CircleShape
) {
    ShimmerBox(
        modifier = modifier.size(size),
        shape = shape
    )
}

/**
 * Shimmer card component matching the app's card styling
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.elevatedCardElevation(),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        content()
    }
}

/**
 * Dashboard stat card shimmer placeholder
 */
@Composable
fun DashboardStatCardShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Title shimmer
                    ShimmerText(
                        height = 14.dp,
                        width = 80.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Value shimmer
                    ShimmerText(
                        height = 24.dp,
                        width = 120.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Subtitle shimmer
                    ShimmerText(
                        height = 12.dp,
                        width = 60.dp
                    )
                }

                // Icon shimmer
                ShimmerIcon(
                    size = 48.dp,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Trend indicator shimmer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ShimmerIcon(size = 16.dp)
                ShimmerText(
                    height = 12.dp,
                    width = 40.dp
                )
            }
        }
    }
}

/**
 * Dashboard stats grid shimmer (4 cards in 2x2 layout)
 */
@Composable
fun DashboardStatsGridShimmer(
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.height(400.dp)
    ) {
        items(4) {
            DashboardStatCardShimmer()
        }
    }
}

/**
 * Dashboard revenue section shimmer
 */
@Composable
fun DashboardRevenueSectionShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title shimmer
        ShimmerText(
            height = 20.dp,
            width = 120.dp
        )

        // Revenue card shimmer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ShimmerText(
                            height = 16.dp,
                            width = 100.dp
                        )
                        ShimmerText(
                            height = 16.dp,
                            width = 80.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dashboard quick action button shimmer
 */
@Composable
fun DashboardQuickActionShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * Dashboard quick actions panel shimmer
 */
@Composable
fun DashboardQuickActionsPanelShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section title shimmer
        ShimmerText(
            height = 20.dp,
            width = 100.dp
        )

        // Quick action buttons shimmer
        repeat(5) {
            DashboardQuickActionShimmer()
        }

        // Inventory alerts section shimmer
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerText(
                height = 18.dp,
                width = 90.dp,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Alert card shimmer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShimmerIcon(size = 20.dp)
                        ShimmerText(
                            height = 14.dp,
                            width = 80.dp
                        )
                    }
                    ShimmerText(
                        height = 12.dp,
                        width = 140.dp
                    )
                }
            }
        }
    }
}

/**
 * Complete dashboard shimmer layout matching the actual dashboard structure
 * with RTL support and Material Design 3 styling
 */
@Composable
fun DashboardShimmerLayout(
    modifier: Modifier = Modifier
) {
    RTLProvider {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Statistics and Overview Shimmer
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
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header shimmer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                ShimmerText(
                                    height = 24.dp,
                                    width = 120.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ShimmerText(
                                    height = 16.dp,
                                    width = 200.dp
                                )
                            }

                            // Refresh button shimmer
                            ShimmerIcon(
                                size = 48.dp,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }

                        // Stats grid shimmer
                        DashboardStatsGridShimmer()

                        // Revenue section shimmer
                        DashboardRevenueSectionShimmer()
                    }
                }

                // Right Panel - Quick Actions and Alerts Shimmer
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
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        DashboardQuickActionsPanelShimmer()
                    }
                }
            }
        }
    }
}

/**
 * Configurable shimmer component for future use in other screens
 */
@Composable
fun ConfigurableShimmer(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    animationDuration: Int = 1200,
    content: @Composable () -> Unit = {}
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "configurable_shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "configurable_shimmer_translate"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush)
    ) {
        content()
    }
}

// ==================== SALES SCREEN SHIMMER COMPONENTS ====================

/**
 * Sales screen header shimmer with tabs and search
 */
@Composable
fun SalesHeaderShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(2) {
                ShimmerBox(
                    modifier = Modifier
                        .width(120.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Search and filters row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search bar shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Filter buttons shimmer
            repeat(2) {
                ShimmerBox(
                    modifier = Modifier
                        .size(56.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

/**
 * Add products button shimmer
 */
@Composable
fun AddProductsButtonShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon shimmer
            ShimmerIcon(
                size = 32.dp,
                shape = RoundedCornerShape(8.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                ShimmerText(
                    height = 18.dp,
                    width = 120.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerText(
                    height = 14.dp,
                    width = 80.dp
                )
            }

            // Arrow icon shimmer
            ShimmerIcon(size = 24.dp)
        }
    }
}

/**
 * Shopping cart item shimmer
 */
@Composable
fun CartItemShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerText(
                        height = 16.dp,
                        width = 140.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerText(
                        height = 14.dp,
                        width = 80.dp
                    )
                }

                // Remove button shimmer
                ShimmerIcon(size = 24.dp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity controls shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerIcon(size = 28.dp, shape = RoundedCornerShape(6.dp))
                    ShimmerText(height = 16.dp, width = 24.dp)
                    ShimmerIcon(size = 28.dp, shape = RoundedCornerShape(6.dp))
                }

                // Price shimmer
                ShimmerText(
                    height = 16.dp,
                    width = 60.dp
                )
            }
        }
    }
}

/**
 * Shopping cart section shimmer
 */
@Composable
fun ShoppingCartSectionShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerText(
                    height = 20.dp,
                    width = 80.dp
                )
                ShimmerText(
                    height = 16.dp,
                    width = 60.dp
                )
            }

            // Cart items shimmer
            repeat(3) {
                CartItemShimmer()
            }
        }
    }
}

/**
 * Customer selection section shimmer
 */
@Composable
fun CustomerSelectionShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title shimmer
            ShimmerText(
                height = 18.dp,
                width = 100.dp
            )

            // Customer selection button shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerIcon(size = 24.dp)
                    Column {
                        ShimmerText(
                            height = 16.dp,
                            width = 120.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerText(
                            height = 12.dp,
                            width = 80.dp
                        )
                    }
                }
                ShimmerIcon(size = 20.dp)
            }
        }
    }
}

/**
 * Payment method section shimmer
 */
@Composable
fun PaymentMethodSectionShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title shimmer
            ShimmerText(
                height = 18.dp,
                width = 100.dp
            )

            // Payment method options shimmer
            repeat(4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerIcon(size = 20.dp)
                    ShimmerIcon(size = 20.dp)
                    ShimmerText(
                        height = 16.dp,
                        width = 80.dp
                    )
                }
            }
        }
    }
}

/**
 * Checkout section shimmer
 */
@Composable
fun CheckoutSectionShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title shimmer
            ShimmerText(
                height = 20.dp,
                width = 120.dp
            )

            // Promotion code section shimmer
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerText(
                    height = 14.dp,
                    width = 80.dp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Order summary shimmer
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ShimmerText(
                            height = 16.dp,
                            width = 80.dp
                        )
                        ShimmerText(
                            height = 16.dp,
                            width = 60.dp
                        )
                    }
                }
            }

            // Checkout button shimmer
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

/**
 * Product selection grid shimmer
 */
@Composable
fun ProductSelectionGridShimmer(
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(6) { index ->
            ProductCardShimmer()
        }
    }
}

/**
 * Product card shimmer for selection dialog
 */
@Composable
fun ProductCardShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerText(
                        height = 16.dp,
                        width = 120.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerText(
                        height = 12.dp,
                        width = 80.dp
                    )
                }

                // Stock badge shimmer
                ShimmerBox(
                    modifier = Modifier
                        .width(40.dp)
                        .height(20.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Price shimmer
            ShimmerText(
                height = 18.dp,
                width = 80.dp
            )

            // Quantity and add button row shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity controls shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerIcon(size = 32.dp, shape = RoundedCornerShape(6.dp))
                    ShimmerText(height = 16.dp, width = 32.dp)
                    ShimmerIcon(size = 32.dp, shape = RoundedCornerShape(6.dp))
                }

                // Add button shimmer
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

/**
 * Sales history list shimmer
 */
@Composable
fun SalesHistoryListShimmer(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) { index ->
            SalesHistoryItemShimmer()
        }
    }
}

/**
 * Sales history item shimmer
 */
@Composable
fun SalesHistoryItemShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerText(
                        height = 16.dp,
                        width = 100.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerText(
                        height = 14.dp,
                        width = 120.dp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    ShimmerText(
                        height = 16.dp,
                        width = 80.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerText(
                        height = 12.dp,
                        width = 60.dp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerText(
                    height = 12.dp,
                    width = 80.dp
                )

                // Status badge shimmer
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * Complete sales screen shimmer layout matching the actual sales screen structure
 * with RTL support and Material Design 3 styling
 */
@Composable
fun SalesScreenShimmerLayout(
    currentTab: String = "NEW_SALE",
    modifier: Modifier = Modifier
) {
    RTLProvider {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header shimmer
            SalesHeaderShimmer()

            // Content based on tab
            when (currentTab) {
                "NEW_SALE" -> {
                    // New sale content shimmer
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left side - Product selection and cart
                        Column(
                            modifier = Modifier.weight(2f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Add products button shimmer
                            AddProductsButtonShimmer()

                            // Shopping cart shimmer
                            ShoppingCartSectionShimmer(
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Right side - Customer, payment, and checkout
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Customer selection shimmer
                            CustomerSelectionShimmer()

                            // Payment method shimmer
                            PaymentMethodSectionShimmer()

                            // Checkout section shimmer
                            CheckoutSectionShimmer()
                        }
                    }
                }
                "SALES_HISTORY" -> {
                    // Sales history content shimmer
                    SalesHistoryListShimmer(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Enhanced loading indicator with shimmer for sales screen
 */
@Composable
fun SalesLoadingIndicator(
    message: String = "جاري تحميل البيانات...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated shimmer circle
                ShimmerIcon(
                    size = 32.dp,
                    shape = CircleShape
                )

                Column {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Progress bar shimmer
                    ShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(4.dp),
                        shape = RoundedCornerShape(2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Reusable shimmer components for other management screens
 */

/**
 * Generic list item shimmer for management screens
 */
@Composable
fun ManagementListItemShimmer(
    modifier: Modifier = Modifier,
    showActions: Boolean = true
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                ShimmerText(
                    height = 16.dp,
                    width = 140.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerText(
                    height = 12.dp,
                    width = 100.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerText(
                    height = 12.dp,
                    width = 80.dp
                )
            }

            if (showActions) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(2) {
                        ShimmerIcon(
                            size = 32.dp,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Generic management screen shimmer layout
 */
@Composable
fun ManagementScreenShimmerLayout(
    modifier: Modifier = Modifier,
    showAddButton: Boolean = true,
    itemCount: Int = 5
) {
    RTLProvider {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with search and add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search bar shimmer
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                )

                if (showAddButton) {
                    // Add button shimmer
                    ShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // List items shimmer
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(itemCount) { index ->
                    ManagementListItemShimmer()
                }
            }
        }
    }
}

// ==================== PRODUCTS SCREEN SHIMMER COMPONENTS ====================

/**
 * Products screen header shimmer with search and filters
 */
@Composable
fun ProductsHeaderShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title and Add button row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerText(
                height = 28.dp,
                width = 150.dp
            )

            ShimmerBox(
                modifier = Modifier
                    .width(140.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Search and filters row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Category filter shimmer
            ShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Status filter shimmer
            ShimmerBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Action buttons row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * Enhanced product card shimmer for products list
 */
@Composable
fun EnhancedProductCardShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Product name shimmer
                ShimmerText(
                    height = 18.dp,
                    width = 160.dp
                )

                // Category shimmer
                ShimmerText(
                    height = 14.dp,
                    width = 100.dp
                )

                // Price shimmer
                ShimmerText(
                    height = 16.dp,
                    width = 80.dp
                )

                // Stock info shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerText(
                        height = 12.dp,
                        width = 60.dp
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .width(50.dp)
                            .height(20.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Action buttons column
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Status badge shimmer
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Action buttons shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        ShimmerIcon(
                            size = 32.dp,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Products list shimmer
 */
@Composable
fun ProductsListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(itemCount) { index ->
            EnhancedProductCardShimmer()
        }
    }
}

/**
 * Product details panel shimmer
 */
@Composable
fun ProductDetailsPanelShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerText(
                    height = 24.dp,
                    width = 120.dp
                )

                ShimmerIcon(
                    size = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Product image placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Product details sections
            repeat(4) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerText(
                        height = 16.dp,
                        width = 100.dp
                    )
                    ShimmerText(
                        height = 14.dp,
                        width = 180.dp
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Complete products screen shimmer layout matching the actual products screen structure
 * with RTL support and Material Design 3 styling
 */
@Composable
fun ProductsScreenShimmerLayout(
    modifier: Modifier = Modifier,
    showDetailsPanel: Boolean = false
) {
    RTLProvider {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Products List Shimmer
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
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header shimmer
                        ProductsHeaderShimmer()

                        // Products list shimmer
                        ProductsListShimmer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Right Panel - Product Details Shimmer (conditional)
                if (showDetailsPanel) {
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
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {
                        ProductDetailsPanelShimmer()
                    }
                }
            }
        }
    }
}

/**
 * Product form dialog shimmer for add/edit dialogs
 */
@Composable
fun ProductFormDialogShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.9f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dialog header shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerIcon(
                    size = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                )

                ShimmerText(
                    height = 24.dp,
                    width = 150.dp
                )
            }

            // Form fields shimmer
            repeat(6) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerText(
                        height = 14.dp,
                        width = 80.dp
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

// ==================== CUSTOMERS SCREEN SHIMMER COMPONENTS ====================

/**
 * Customers screen header shimmer with title, count, and add button
 */
@Composable
fun CustomersHeaderShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            ShimmerText(
                height = 28.dp,
                width = 150.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerText(
                height = 16.dp,
                width = 120.dp
            )
        }

        ShimmerBox(
            modifier = Modifier
                .width(140.dp)
                .height(40.dp),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Customers search and filters shimmer
 */
@Composable
fun CustomersSearchAndFiltersShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Search bar shimmer
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        )

        // Filters row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // City filter label shimmer
            ShimmerText(
                height = 16.dp,
                width = 60.dp
            )

            // City filter chips shimmer
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Sort dropdown shimmer
            ShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

/**
 * Individual customer card shimmer for staggered grid
 */
@Composable
fun CustomerCardShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Customer name shimmer
            ShimmerText(
                height = 18.dp,
                width = 140.dp
            )

            // Contact info shimmer
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ShimmerText(
                    height = 14.dp,
                    width = 120.dp
                )
                ShimmerText(
                    height = 14.dp,
                    width = 100.dp
                )
                ShimmerText(
                    height = 14.dp,
                    width = 110.dp
                )
            }

            // Status and actions row shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge shimmer
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Action buttons shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(2) {
                        ShimmerIcon(
                            size = 28.dp,
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Customers list shimmer with staggered grid layout
 */
@Composable
fun CustomersListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(itemCount) { index ->
            CustomerCardShimmer()
        }
    }
}

/**
 * Customer statistics card shimmer
 */
@Composable
fun CustomerStatCardShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon shimmer
            ShimmerIcon(
                size = 40.dp,
                shape = RoundedCornerShape(12.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                ShimmerText(
                    height = 14.dp,
                    width = 100.dp
                )
                Spacer(modifier = Modifier.height(4.dp))
                ShimmerText(
                    height = 20.dp,
                    width = 80.dp
                )
                Spacer(modifier = Modifier.height(2.dp))
                ShimmerText(
                    height = 12.dp,
                    width = 60.dp
                )
            }
        }
    }
}

/**
 * Customer statistics section shimmer
 */
@Composable
fun CustomerStatsShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section title shimmer
        ShimmerText(
            height = 24.dp,
            width = 150.dp
        )

        // Statistics cards shimmer
        repeat(4) {
            CustomerStatCardShimmer()
        }
    }
}

/**
 * Customer quick actions shimmer
 */
@Composable
fun CustomerQuickActionsShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title shimmer
        ShimmerText(
            height = 24.dp,
            width = 120.dp,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Quick action buttons shimmer
        repeat(3) {
            ShimmerCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerIcon(
                        size = 24.dp,
                        shape = RoundedCornerShape(8.dp)
                    )
                    ShimmerText(
                        height = 16.dp,
                        width = 120.dp
                    )
                }
            }
        }
    }
}

/**
 * Complete customers screen shimmer layout matching the actual customers screen structure
 * with RTL support and Material Design 3 styling
 */
@Composable
fun CustomersScreenShimmerLayout(
    modifier: Modifier = Modifier
) {
    RTLProvider {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Customers List Shimmer
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
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header shimmer
                        CustomersHeaderShimmer()

                        // Search and filters shimmer
                        CustomersSearchAndFiltersShimmer()

                        // Customers list shimmer
                        CustomersListShimmer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Right Panel - Customer Statistics and Actions Shimmer
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
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Customer statistics shimmer
                        CustomerStatsShimmer()

                        // Quick actions shimmer
                        CustomerQuickActionsShimmer()
                    }
                }
            }
        }
    }
}

/**
 * Customer form dialog shimmer for add/edit dialogs
 */
@Composable
fun CustomerFormDialogShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.9f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dialog header shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerIcon(
                    size = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                )

                ShimmerText(
                    height = 24.dp,
                    width = 150.dp
                )
            }

            // Form fields shimmer (13 fields for customer form)
            repeat(13) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerText(
                        height = 14.dp,
                        width = 80.dp
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

// ==================== INVENTORY SCREEN SHIMMER COMPONENTS ====================

/**
 * Inventory screen header shimmer with title and add button
 */
@Composable
fun InventoryHeaderShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerText(
            height = 28.dp,
            width = 150.dp
        )

        ShimmerBox(
            modifier = Modifier
                .width(180.dp)
                .height(40.dp),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Inventory tabs shimmer
 */
@Composable
fun InventoryTabsShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) { index ->
            ShimmerBox(
                modifier = Modifier
                    .width(if (index == 0) 100.dp else 80.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

/**
 * Inventory search and filters shimmer
 */
@Composable
fun InventorySearchAndFiltersShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search and filters row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Category filter shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Warehouse filter shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Sort dropdown shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Quick filters shimmer
            repeat(2) {
                ShimmerBox(
                    modifier = Modifier
                        .width(120.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Export button shimmer
            ShimmerBox(
                modifier = Modifier
                    .width(80.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

/**
 * Individual inventory item card shimmer
 */
@Composable
fun InventoryItemCardShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Product name shimmer
                ShimmerText(
                    height = 18.dp,
                    width = 160.dp
                )

                // SKU shimmer
                ShimmerText(
                    height = 14.dp,
                    width = 100.dp
                )

                // Category shimmer
                ShimmerText(
                    height = 14.dp,
                    width = 120.dp
                )

                // Location shimmer
                ShimmerText(
                    height = 14.dp,
                    width = 140.dp
                )
            }

            // Stock info column
            Column(
                modifier = Modifier.weight(0.8f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Stock level shimmer
                ShimmerText(
                    height = 16.dp,
                    width = 80.dp
                )

                // Stock status badge shimmer
                ShimmerBox(
                    modifier = Modifier
                        .width(70.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Last updated shimmer
                ShimmerText(
                    height = 12.dp,
                    width = 90.dp
                )
            }

            // Action buttons column
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Action buttons shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        ShimmerIcon(
                            size = 32.dp,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Inventory list shimmer with LazyColumn layout
 */
@Composable
fun InventoryListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(itemCount) { index ->
            InventoryItemCardShimmer()
        }
    }
}

/**
 * Inventory item details panel shimmer for right panel
 */
@Composable
fun InventoryDetailsShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerText(
                    height = 24.dp,
                    width = 120.dp
                )

                ShimmerIcon(
                    size = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Product image placeholder
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Product details sections
            repeat(6) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerText(
                        height = 16.dp,
                        width = 100.dp
                    )
                    ShimmerText(
                        height = 14.dp,
                        width = 180.dp
                    )
                }
            }

            // Stock alerts section
            StockAlertsShimmer()

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(2) {
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Stock alerts shimmer section
 */
@Composable
fun StockAlertsShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section title shimmer
        ShimmerText(
            height = 18.dp,
            width = 120.dp
        )

        // Alert cards shimmer
        repeat(2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerIcon(
                        size = 20.dp,
                        shape = CircleShape
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        ShimmerText(
                            height = 14.dp,
                            width = 100.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerText(
                            height = 12.dp,
                            width = 140.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Complete inventory screen shimmer layout matching the actual inventory screen structure
 * with RTL support and Material Design 3 styling
 */
@Composable
fun InventoryScreenShimmerLayout(
    modifier: Modifier = Modifier,
    showDetailsPanel: Boolean = false
) {
    RTLProvider {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Inventory Management Shimmer
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
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header shimmer
                        InventoryHeaderShimmer()

                        // Tabs shimmer
                        InventoryTabsShimmer()

                        // Search and filters shimmer
                        InventorySearchAndFiltersShimmer()

                        // Inventory list shimmer
                        InventoryListShimmer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Right Panel - Item Details Shimmer (conditional)
                if (showDetailsPanel) {
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
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {
                        InventoryDetailsShimmer()
                    }
                }
            }
        }
    }
}

/**
 * Inventory form dialog shimmer for warehouse add/edit dialogs
 */
@Composable
fun InventoryFormDialogShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.9f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dialog header shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerText(
                    height = 24.dp,
                    width = 180.dp
                )

                ShimmerIcon(
                    size = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Form fields shimmer (warehouse form has many fields)
            repeat(12) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerText(
                        height = 14.dp,
                        width = 100.dp
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Checkbox section shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier.size(20.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                ShimmerText(
                    height = 16.dp,
                    width = 120.dp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

// ==================== SUPPLIERS SCREEN SHIMMER COMPONENTS ====================

/**
 * Suppliers screen header shimmer with search, filters, and add button
 */
@Composable
fun SuppliersHeaderShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title and Add button row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerText(
                height = 28.dp,
                width = 150.dp
            )

            ShimmerBox(
                modifier = Modifier
                    .width(140.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Enhanced tabs shimmer
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        )

        // Search and filters row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Status filter shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Location filter shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(0.7f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Sort and action buttons row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sort dropdown shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Quick filter buttons shimmer
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .width(100.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

/**
 * Individual supplier card shimmer for suppliers list
 */
@Composable
fun SupplierCardShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Supplier info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Company name shimmer
                ShimmerText(
                    height = 18.dp,
                    width = 180.dp
                )

                // Contact person shimmer
                ShimmerText(
                    height = 14.dp,
                    width = 140.dp
                )

                // Contact info row shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Phone shimmer
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerIcon(size = 16.dp)
                        ShimmerText(
                            height = 12.dp,
                            width = 80.dp
                        )
                    }

                    // Email shimmer
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerIcon(size = 16.dp)
                        ShimmerText(
                            height = 12.dp,
                            width = 100.dp
                        )
                    }
                }

                // Location and rating row shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location shimmer
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerIcon(size = 16.dp)
                        ShimmerText(
                            height = 12.dp,
                            width = 60.dp
                        )
                    }

                    // Rating shimmer
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) {
                            ShimmerIcon(size = 12.dp)
                        }
                    }
                }
            }

            // Stats and actions column
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Status badge shimmer
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Stats shimmer
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ShimmerText(
                        height = 14.dp,
                        width = 80.dp
                    )
                    ShimmerText(
                        height = 12.dp,
                        width = 60.dp
                    )
                }

                // Action buttons shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        ShimmerIcon(
                            size = 32.dp,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Suppliers statistics cards shimmer
 */
@Composable
fun SuppliersStatsCardsShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // First row of stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(2) {
                ShimmerCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerIcon(size = 24.dp)
                            ShimmerText(
                                height = 24.dp,
                                width = 60.dp
                            )
                        }
                        Column {
                            ShimmerText(
                                height = 16.dp,
                                width = 100.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ShimmerText(
                                height = 14.dp,
                                width = 80.dp
                            )
                        }
                    }
                }
            }
        }

        // Second row of stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(2) {
                ShimmerCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerIcon(size = 24.dp)
                            ShimmerText(
                                height = 24.dp,
                                width = 80.dp
                            )
                        }
                        Column {
                            ShimmerText(
                                height = 16.dp,
                                width = 120.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ShimmerText(
                                height = 14.dp,
                                width = 90.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Suppliers list shimmer with LazyColumn of supplier cards
 */
@Composable
fun SuppliersListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 6
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics cards shimmer
        item {
            SuppliersStatsCardsShimmer()
        }

        // List header shimmer
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerText(
                    height = 20.dp,
                    width = 120.dp
                )
                ShimmerIcon(
                    size = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Supplier cards shimmer
        items(itemCount) { index ->
            SupplierCardShimmer()
        }
    }
}

/**
 * Supplier details panel shimmer for right panel
 */
@Composable
fun SupplierDetailsPanelShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header with close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerIcon(
                size = 32.dp,
                shape = RoundedCornerShape(8.dp)
            )
            ShimmerText(
                height = 24.dp,
                width = 120.dp
            )
        }

        // Supplier basic info section
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerText(
                height = 18.dp,
                width = 100.dp
            )
            repeat(4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ShimmerText(
                        height = 14.dp,
                        width = 80.dp
                    )
                    ShimmerText(
                        height = 14.dp,
                        width = 120.dp
                    )
                }
            }
        }

        // Contact information section
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerText(
                height = 18.dp,
                width = 120.dp
            )
            repeat(3) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerIcon(size = 16.dp)
                    ShimmerText(
                        height = 14.dp,
                        width = 140.dp
                    )
                }
            }
        }

        // Performance metrics section
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerText(
                height = 18.dp,
                width = 140.dp
            )
            repeat(3) {
                ShimmerCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            ShimmerText(
                                height = 14.dp,
                                width = 80.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ShimmerText(
                                height = 12.dp,
                                width = 60.dp
                            )
                        }
                        ShimmerText(
                            height = 16.dp,
                            width = 50.dp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * Supplier form dialog shimmer for add/edit dialogs
 */
@Composable
fun SupplierFormDialogShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .fillMaxHeight(0.9f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dialog header shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerIcon(
                    size = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                )

                ShimmerText(
                    height = 24.dp,
                    width = 150.dp
                )
            }

            // Form fields shimmer
            repeat(8) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerText(
                        height = 14.dp,
                        width = 100.dp
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * Complete suppliers screen shimmer layout matching the actual suppliers screen structure
 * with RTL support and Material Design 3 styling
 */
@Composable
fun SuppliersScreenShimmerLayout(
    modifier: Modifier = Modifier,
    showDetailsPanel: Boolean = false
) {
    RTLProvider {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Suppliers Management Shimmer
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
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header shimmer
                        SuppliersHeaderShimmer()

                        // Suppliers list shimmer
                        SuppliersListShimmer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Right Panel - Supplier Details Shimmer (conditional)
                if (showDetailsPanel) {
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
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {
                        SupplierDetailsPanelShimmer()
                    }
                }
            }
        }
    }
}

// ==================== CATEGORIES SCREEN SHIMMER COMPONENTS ====================

/**
 * Categories screen header shimmer with search and filters
 */
@Composable
fun CategoriesHeaderShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title and Add button row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerText(
                height = 28.dp,
                width = 120.dp
            )

            ShimmerBox(
                modifier = Modifier
                    .width(140.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Search and filters row shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search field shimmer
            ShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Status filter shimmer
            ShimmerBox(
                modifier = Modifier
                    .width(120.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )

            // Sort dropdown shimmer
            ShimmerBox(
                modifier = Modifier
                    .width(100.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

/**
 * Modern category item shimmer for categories list
 */
@Composable
fun ModernCategoryItemShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator shimmer
            ShimmerBox(
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            )

            // Category info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category name shimmer
                ShimmerText(
                    height = 18.dp,
                    width = 140.dp
                )

                // Description shimmer
                ShimmerText(
                    height = 14.dp,
                    width = 200.dp
                )

                // Stats row shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ShimmerText(
                        height = 12.dp,
                        width = 80.dp
                    )
                    ShimmerText(
                        height = 12.dp,
                        width = 60.dp
                    )
                }
            }

            // Status and actions column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status badge shimmer
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Action buttons shimmer
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) {
                        ShimmerIcon(
                            size = 32.dp,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Categories list shimmer
 */
@Composable
fun CategoriesListShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(itemCount) { index ->
            ModernCategoryItemShimmer()
        }
    }
}

/**
 * Categories statistics panel shimmer
 */
@Composable
fun CategoriesStatsPanelShimmer(
    modifier: Modifier = Modifier
) {
    ShimmerCard(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Panel title shimmer
            ShimmerText(
                height = 24.dp,
                width = 140.dp
            )

            // Stats cards shimmer
            repeat(3) {
                ShimmerCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerIcon(size = 32.dp)
                            ShimmerText(
                                height = 20.dp,
                                width = 60.dp
                            )
                        }

                        ShimmerText(
                            height = 14.dp,
                            width = 100.dp
                        )
                    }
                }
            }

            // Chart section shimmer
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerText(
                    height = 18.dp,
                    width = 120.dp
                )

                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Recent activity shimmer
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerText(
                    height = 18.dp,
                    width = 100.dp
                )

                repeat(3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShimmerIcon(size = 24.dp)
                            ShimmerText(
                                height = 14.dp,
                                width = 120.dp
                            )
                        }
                        ShimmerText(
                            height = 12.dp,
                            width = 60.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Complete categories screen shimmer layout matching the actual categories screen structure
 * with RTL support and Material Design 3 styling
 */
@Composable
fun CategoriesScreenShimmerLayout(
    modifier: Modifier = Modifier
) {
    RTLProvider {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            RTLRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Panel - Categories List Shimmer
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
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Header shimmer
                        CategoriesHeaderShimmer()

                        // Categories list shimmer
                        CategoriesListShimmer(
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Right Panel - Statistics Shimmer
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
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                ) {
                    CategoriesStatsPanelShimmer()
                }
            }
        }
    }
}

/**
 * Category form dialog shimmer for add/edit dialogs
 */
@Composable
fun CategoryFormDialogShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.6f)
            .fillMaxHeight(0.7f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dialog header shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerIcon(
                    size = 32.dp,
                    shape = RoundedCornerShape(8.dp)
                )

                ShimmerText(
                    height = 24.dp,
                    width = 120.dp
                )
            }

            // Form fields shimmer
            repeat(4) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerText(
                        height = 14.dp,
                        width = 80.dp
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Color picker shimmer
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerText(
                    height = 14.dp,
                    width = 60.dp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(6) {
                        ShimmerBox(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

