// Test file to verify the scrolling arrows implementation compiles correctly
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Mock PaymentMethod enum for testing
enum class PaymentMethod(val displayName: String) {
    CASH("نقد"),
    CARD("بطاقة ائتمان"),
    BANK_TRANSFER("تحويل بنكي"),
    DIGITAL_WALLET("محفظة رقمية")
}

@Composable
fun TestPaymentMethodHeaderWithArrows(
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
                            Icons.Default.ChevronLeft,
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
                            Icons.Default.ChevronRight,
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
                TestPaymentMethodCard(
                    method = method,
                    isSelected = selectedMethod == method,
                    onClick = { onMethodSelected(method) }
                )
            }
        }
    }
}

@Composable
private fun TestPaymentMethodCard(
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

    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
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
