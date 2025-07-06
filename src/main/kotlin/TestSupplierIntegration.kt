import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import data.di.AppDependencies
import ui.screens.SuppliersScreen
import ui.theme.AppTheme
import ui.theme.AppThemeProvider

/**
 * Test application for Supplier integration
 */
fun main() = application {
    Window(
        onCloseRequest = {
            AppDependencies.container.cleanup()
            exitApplication()
        },
        title = "Supplier Integration Test",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        AppThemeProvider {
            TestSupplierApp()
        }
    }
}

@Composable
fun TestSupplierApp() {
    val appContainer = remember { AppDependencies.container }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Supplier Management Integration Test",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            // Test the SuppliersScreen with ViewModel
            SuppliersScreen(
                supplierViewModel = appContainer.supplierViewModel
            )
        }
    }
}
