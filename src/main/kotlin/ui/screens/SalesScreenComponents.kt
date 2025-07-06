@file:OptIn(ExperimentalAnimationApi::class)

package ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.*
import ui.theme.AppTheme
import java.text.NumberFormat

@Composable
fun CartItemImproved(
    item: SaleItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    currencyFormatter: NumberFormat
) {
    var isRemoving by remember { mutableStateOf(false) }

    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    AnimatedVisibility(
        visible = !isRemoving,
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    color = if (isHovered)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isHovered) 1.5.dp else 1.dp,
                    color = if (isHovered)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
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
                        Text(
                            text = item.product.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currencyFormatter.format(item.unitPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            isRemoving = true
                            onRemove()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إزالة",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Improved quantity controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = { onQuantityChange(item.quantity - 1) },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            enabled = item.quantity > 1
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "تقليل",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.quantity.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        FilledTonalIconButton(
                            onClick = { onQuantityChange(item.quantity + 1) },
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            enabled = item.quantity < item.product.stock
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "زيادة",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Subtotal with animation
                    AnimatedContent(
                        targetState = item.subtotal,
                        transitionSpec = {
                            slideInVertically { it } + fadeIn() with
                                    slideOutVertically { -it } + fadeOut()
                        }
                    ) { subtotal ->
                        Text(
                            text = currencyFormatter.format(subtotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodSelectorImproved(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PaymentMethodHeaderWithArrows(
                selectedMethod = selectedMethod,
                onMethodSelected = onMethodSelected
            )
        }
    }
}

@Composable
private fun PaymentMethodHeaderWithArrows(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Check if we can scroll
    val canScrollLeft by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0
        }
    }

    val canScrollRight by remember {
        derivedStateOf {
            scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.let { lastItem ->
                lastItem.index < scrollState.layoutInfo.totalItemsCount - 1 ||
                lastItem.offset + lastItem.size > scrollState.layoutInfo.viewportEndOffset
            } ?: false
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with title and arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "طريقة الدفع",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Arrow buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left scroll arrow
                AnimatedVisibility(
                    visible = canScrollLeft,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            coroutineScope.launch {
                                val currentIndex = scrollState.firstVisibleItemIndex
                                scrollState.animateScrollToItem(maxOf(0, currentIndex - 1))
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "التمرير لليسار",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Right scroll arrow
                AnimatedVisibility(
                    visible = canScrollRight,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            coroutineScope.launch {
                                val currentIndex = scrollState.firstVisibleItemIndex
                                val maxIndex = scrollState.layoutInfo.totalItemsCount - 1
                                scrollState.animateScrollToItem(minOf(maxIndex, currentIndex + 1))
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                           Icons.Default.ChevronLeft,
                            contentDescription = "التمرير لليمين",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Payment method cards row
        LazyRow(
            state = scrollState,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PaymentMethod.entries) { method ->
                PaymentMethodCard(
                    method = method,
                    isSelected = selectedMethod == method,
                    onClick = { onMethodSelected(method) }
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (method) {
        PaymentMethod.CASH -> Icons.Outlined.Payments
        PaymentMethod.CARD -> Icons.Outlined.CreditCard
        PaymentMethod.BANK_TRANSFER -> Icons.Outlined.AccountBalance
        PaymentMethod.DIGITAL_WALLET -> Icons.Outlined.Wallet
    }

    // Enhanced hover effect with complete coverage
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isHovered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
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
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
