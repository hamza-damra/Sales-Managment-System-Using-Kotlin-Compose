package services

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Professional notification service for managing toast messages across the application
 */
class NotificationService {
    
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    /**
     * Show a success notification
     */
    fun showSuccess(
        message: String,
        title: String? = null,
        duration: Long = 4000L,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        showNotification(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                type = NotificationType.SUCCESS,
                title = title,
                message = message,
                duration = duration,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }
    
    /**
     * Show an error notification
     */
    fun showError(
        message: String,
        title: String? = null,
        duration: Long = 6000L,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        showNotification(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                type = NotificationType.ERROR,
                title = title,
                message = message,
                duration = duration,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }
    
    /**
     * Show a warning notification
     */
    fun showWarning(
        message: String,
        title: String? = null,
        duration: Long = 5000L,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        showNotification(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                type = NotificationType.WARNING,
                title = title,
                message = message,
                duration = duration,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }
    
    /**
     * Show an info notification
     */
    fun showInfo(
        message: String,
        title: String? = null,
        duration: Long = 4000L,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        showNotification(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                type = NotificationType.INFO,
                title = title,
                message = message,
                duration = duration,
                actionLabel = actionLabel,
                onAction = onAction
            )
        )
    }
    
    /**
     * Show a validation error notification (specific for form validation)
     */
    fun showValidationError(
        message: String,
        title: String = "خطأ في التحقق",
        duration: Long = 5000L
    ) {
        showError(
            message = message,
            title = title,
            duration = duration
        )
    }
    
    private fun showNotification(notification: NotificationItem) {
        val currentNotifications = _notifications.value.toMutableList()
        currentNotifications.add(notification)
        _notifications.value = currentNotifications
        
        // Auto-dismiss after duration
        coroutineScope.launch {
            delay(notification.duration)
            dismissNotification(notification.id)
        }
    }
    
    /**
     * Manually dismiss a notification
     */
    fun dismissNotification(id: String) {
        val currentNotifications = _notifications.value.toMutableList()
        currentNotifications.removeAll { it.id == id }
        _notifications.value = currentNotifications
    }
    
    /**
     * Clear all notifications
     */
    fun clearAll() {
        _notifications.value = emptyList()
    }
}

/**
 * Data class representing a notification item
 */
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String?,
    val message: String,
    val duration: Long,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Enum representing different types of notifications
 */
enum class NotificationType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}
