# Professional Notification Service Guide

## Overview

The `NotificationService` provides a centralized, professional toast notification system for the Sales Management System. It replaces scattered snackbar implementations with a consistent, visually appealing notification system positioned in the top-right corner of the screen.

## Features

- **Professional UI**: Modern card-based design with animations and shadows
- **Multiple Types**: Success, Error, Warning, Info, and Validation Error notifications
- **Auto-dismiss**: Configurable duration with automatic dismissal
- **Action Support**: Optional action buttons with callbacks
- **RTL Support**: Proper Arabic text support and right-to-left layout
- **Global Positioning**: Top-right corner overlay that doesn't interfere with content
- **Animation**: Smooth slide-in/slide-out animations with spring physics

## Usage

### Basic Usage

```kotlin
// Inject the notification service (already available in AppContainer)
val notificationService = appContainer.notificationService

// Show different types of notifications
notificationService.showSuccess("تم حفظ البيانات بنجاح")
notificationService.showError("حدث خطأ أثناء العملية")
notificationService.showWarning("تحذير: يرجى التحقق من البيانات")
notificationService.showInfo("معلومة مفيدة للمستخدم")
```

### Advanced Usage with Titles and Actions

```kotlin
// Success with title
notificationService.showSuccess(
    message = "تم إنشاء المنتج بنجاح",
    title = "نجح الحفظ"
)

// Error with action button
notificationService.showError(
    message = "فشل في الاتصال بالخادم",
    title = "خطأ في الشبكة",
    actionLabel = "إعادة المحاولة",
    onAction = { 
        // Retry logic here
        retryOperation()
    }
)

// Validation error (specific for form validation)
notificationService.showValidationError(
    message = "يرجى اختيار عميل لإتمام البيع",
    title = "عميل مطلوب"
)
```

### Custom Duration

```kotlin
// Short notification (2 seconds)
notificationService.showInfo(
    message = "تم نسخ الرابط",
    duration = 2000L
)

// Long notification (8 seconds)
notificationService.showError(
    message = "خطأ خطير يتطلب انتباه المستخدم",
    duration = 8000L
)
```

## Integration in Screens

### 1. Add NotificationService Parameter

```kotlin
@Composable
fun YourScreen(
    // ... other parameters
    notificationService: NotificationService
) {
    // Screen content
}
```

### 2. Replace Snackbar Usage

**Before (using Snackbar):**
```kotlin
LaunchedEffect(uiState.deletionSuccess) {
    if (uiState.deletionSuccess) {
        snackbarHostState.showSnackbar(
            message = "تم حذف المنتج بنجاح",
            duration = SnackbarDuration.Short
        )
        viewModel.clearDeletionSuccess()
    }
}
```

**After (using NotificationService):**
```kotlin
LaunchedEffect(uiState.deletionSuccess) {
    if (uiState.deletionSuccess) {
        notificationService.showSuccess(
            message = "تم حذف المنتج بنجاح",
            title = "تم الحذف"
        )
        viewModel.clearDeletionSuccess()
    }
}
```

### 3. Handle Different Result Types

```kotlin
// Handle API results with appropriate notifications
when (result) {
    is NetworkResult.Success -> {
        notificationService.showSuccess(
            message = "تم تحديث البيانات بنجاح",
            title = "نجح التحديث"
        )
    }
    is NetworkResult.Error -> {
        val error = result.exception
        when {
            error.message?.contains("validation") == true -> {
                notificationService.showValidationError(
                    message = error.message ?: "خطأ في التحقق من البيانات"
                )
            }
            error.message?.contains("network") == true -> {
                notificationService.showError(
                    message = "خطأ في الاتصال بالشبكة",
                    title = "خطأ في الشبكة",
                    actionLabel = "إعادة المحاولة",
                    onAction = { retryOperation() }
                )
            }
            else -> {
                notificationService.showError(
                    message = error.message ?: "حدث خطأ غير متوقع",
                    title = "خطأ"
                )
            }
        }
    }
}
```

## Best Practices

### When to Use Each Type

- **Success**: Successful operations (save, update, delete, create)
- **Error**: Failed operations, network errors, server errors
- **Warning**: Non-critical issues, deprecation notices, capacity warnings
- **Info**: General information, tips, status updates
- **Validation Error**: Form validation failures, required field errors

### Message Guidelines

1. **Keep messages concise** but informative
2. **Use Arabic** for user-facing messages
3. **Include context** when helpful
4. **Provide actions** for recoverable errors
5. **Use appropriate titles** to categorize the notification

### Duration Guidelines

- **Success**: 3-4 seconds (default)
- **Info**: 3-4 seconds (default)
- **Warning**: 5-6 seconds
- **Error**: 6-8 seconds
- **Critical Error**: 8-10 seconds

## Migration from Snackbars

To migrate existing screens from snackbars to the notification service:

1. Add `notificationService: NotificationService` parameter to screen composable
2. Remove `SnackbarHost` and `snackbarHostState` usage
3. Replace `snackbarHostState.showSnackbar()` calls with appropriate notification methods
4. Update screen calls in Main.kt to pass the notification service
5. Remove snackbar-related imports

## Examples in Current Implementation

The notification system is already integrated in:

- **SalesScreen**: Handles validation errors for customer selection and cart validation
- **Main.kt**: Global NotificationOverlay positioned in top-right corner
- **AppContainer**: NotificationService available as singleton

## Technical Details

- **Position**: Fixed top-right corner with 24dp padding
- **Max Width**: 400dp to prevent overly wide notifications
- **Animation**: Spring-based slide-in from right, fade-out on dismiss
- **Stacking**: Multiple notifications stack vertically with 12dp spacing
- **Z-Index**: 1000 to ensure notifications appear above all content
- **Auto-dismiss**: Configurable duration with automatic cleanup
- **Manual Dismiss**: Click anywhere on notification or close button to dismiss
