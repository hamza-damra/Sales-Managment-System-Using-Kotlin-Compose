import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.di.AppDependencies
import ui.theme.AppTheme
import ui.screens.CategoriesScreen

/**
 * Test application to verify category implementation
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Category Management Test"
    ) {
        AppTheme {
            TestCategoryApp()
        }
    }
}

@Composable
fun TestCategoryApp() {
    val appContainer = remember { AppDependencies.container }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Category Management Implementation Test",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            // Test the CategoriesScreen
            CategoriesScreen(
                categoryViewModel = appContainer.categoryViewModel
            )
        }
    }
}

@Preview
@Composable
fun TestCategoryAppPreview() {
    AppTheme {
        TestCategoryApp()
    }
}
