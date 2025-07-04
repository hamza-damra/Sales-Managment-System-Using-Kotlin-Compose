import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.SalesDataManager
import data.di.AppDependencies
import services.InventoryExportService
import ui.screens.InventoryScreen
import ui.theme.AppTheme
import kotlinx.coroutines.runBlocking

/**
 * Test application for inventory export functionality
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sales Management System - Inventory Export Test"
    ) {
        AppTheme {
            TestInventoryApp()
        }
    }
}

@Composable
fun TestInventoryApp() {
    var isInitialized by remember { mutableStateOf(false) }
    var initializationError by remember { mutableStateOf<String?>(null) }
    var salesDataManager by remember { mutableStateOf<SalesDataManager?>(null) }
    var inventoryExportService by remember { mutableStateOf<InventoryExportService?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            // Initialize services using dependency injection
            val container = AppDependencies.container
            val productRepository = container.productRepository
            val reportsApiService = container.reportsApiService

            // Initialize services
            salesDataManager = SalesDataManager()
            inventoryExportService = InventoryExportService(productRepository, reportsApiService)
            
            isInitialized = true
        } catch (e: Exception) {
            initializationError = "Failed to initialize services: ${e.message}"
            e.printStackTrace()
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            initializationError != null -> {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "خطأ في التهيئة",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = initializationError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                initializationError = null
                                isInitialized = false
                            }
                        ) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
            }
            !isInitialized -> {
                Card(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري تهيئة التطبيق...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            else -> {
                // Show the inventory screen with export functionality
                InventoryScreen(
                    salesDataManager = salesDataManager!!,
                    inventoryExportService = inventoryExportService
                )
            }
        }
    }
}

@Preview
@Composable
fun TestInventoryAppPreview() {
    AppTheme {
        TestInventoryApp()
    }
}
