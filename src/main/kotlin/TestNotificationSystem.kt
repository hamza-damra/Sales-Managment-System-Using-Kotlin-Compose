import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import data.di.AppDependencies
import ui.components.NotificationOverlay
import ui.theme.AppThemeProvider

/**
 * Test application for the notification system
 */
fun main() = application {
    Window(
        onCloseRequest = {
            AppDependencies.container.cleanup()
            exitApplication()
        },
        title = "Notification System Test",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        AppThemeProvider {
            TestNotificationApp()
        }
    }
}

@Composable
fun TestNotificationApp() {
    val appContainer = remember { AppDependencies.container }
    val notificationService = appContainer.notificationService
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = "Notification System Test",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        notificationService.showSuccess(
                            message = "تم إنجاز العملية بنجاح",
                            title = "نجح"
                        )
                    }
                ) {
                    Text("Success")
                }
                
                Button(
                    onClick = {
                        notificationService.showError(
                            message = "حدث خطأ أثناء العملية",
                            title = "خطأ"
                        )
                    }
                ) {
                    Text("Error")
                }
                
                Button(
                    onClick = {
                        notificationService.showWarning(
                            message = "تحذير: يرجى التحقق من البيانات",
                            title = "تحذير"
                        )
                    }
                ) {
                    Text("Warning")
                }
                
                Button(
                    onClick = {
                        notificationService.showValidationError(
                            message = "يرجى اختيار عميل لإتمام البيع"
                        )
                    }
                ) {
                    Text("Validation Error")
                }
            }
        }
        
        // Notification overlay
        NotificationOverlay(
            notificationService = notificationService
        )
    }
}
