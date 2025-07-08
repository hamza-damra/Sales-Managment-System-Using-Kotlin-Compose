package ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import services.NotificationItem
import services.NotificationService
import services.NotificationType
import ui.theme.AppTheme

/**
 * Global notification overlay that displays toast messages in the top-right corner
 */
@Composable
fun NotificationOverlay(
    notificationService: NotificationService,
    modifier: Modifier = Modifier
) {
    val notifications by notificationService.notifications.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1000f), // Ensure notifications appear on top
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            notifications.forEach { notification ->
                key(notification.id) {
                    NotificationCard(
                        notification = notification,
                        onDismiss = { notificationService.dismissNotification(notification.id) }
                    )
                }
            }
        }
    }
}

/**
 * Individual notification card with animations
 */
@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(notification.id) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(250)
        ) + fadeOut(
            animationSpec = tween(250)
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = getNotificationColor(notification.type).copy(alpha = 0.1f),
                    spotColor = getNotificationColor(notification.type).copy(alpha = 0.2f)
                )
                .border(
                    width = 1.dp,
                    color = getNotificationColor(notification.type).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onDismiss() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Icon(
                    imageVector = getNotificationIcon(notification.type),
                    contentDescription = null,
                    tint = getNotificationColor(notification.type),
                    modifier = Modifier.size(24.dp)
                )
                
                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title (if provided)
                    notification.title?.let { title ->
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Message
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                    
                    // Action button (if provided)
                    notification.actionLabel?.let { actionLabel ->
                        notification.onAction?.let { action ->
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = {
                                    action()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = getNotificationColor(notification.type)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = actionLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Get the appropriate icon for each notification type
 */
private fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.SUCCESS -> Icons.Default.CheckCircle
        NotificationType.ERROR -> Icons.Default.Error
        NotificationType.WARNING -> Icons.Default.Warning
        NotificationType.INFO -> Icons.Default.Info
    }
}

/**
 * Get the appropriate color for each notification type
 */
@Composable
private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.SUCCESS -> AppTheme.colors.success
        NotificationType.ERROR -> AppTheme.colors.error
        NotificationType.WARNING -> AppTheme.colors.warning
        NotificationType.INFO -> AppTheme.colors.info
    }
}
