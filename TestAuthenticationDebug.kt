// Authentication Debug Test Component
// Add this to your project to debug authentication issues

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.di.AppDependencies
import kotlinx.coroutines.launch

@Composable
fun AuthenticationDebugScreen() {
    val authService = remember { AppDependencies.container.authService }
    val tokenManager = remember { AppDependencies.container.tokenManager }
    val authState by authService.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var debugInfo by remember { mutableStateOf("Loading...") }
    var testResults by remember { mutableStateOf<List<String>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        val info = buildString {
            appendLine("=== AUTHENTICATION DEBUG INFO ===")
            appendLine()
            appendLine("🔍 Auth State:")
            appendLine("  - Is Authenticated: ${authState.isAuthenticated}")
            appendLine("  - User: ${authState.user?.username ?: "None"}")
            appendLine("  - User Role: ${authState.user?.role ?: "None"}")
            appendLine("  - Access Token: ${authState.accessToken?.take(30)}...")
            appendLine("  - Refresh Token: ${authState.refreshToken?.take(30)}...")
            appendLine()
            appendLine("🔍 Token Manager:")
            appendLine("  - Has Valid Tokens: ${tokenManager.hasValidTokens()}")
            appendLine("  - Is Authenticated: ${tokenManager.isAuthenticated()}")
            appendLine("  - Is Token Expired: ${tokenManager.isTokenExpired()}")
            appendLine("  - Access Token: ${tokenManager.getAccessToken()?.take(30)}...")
            appendLine("  - Refresh Token: ${tokenManager.getRefreshToken()?.take(30)}...")
            appendLine()
            appendLine("🔍 User Info:")
            val user = tokenManager.getUser()
            if (user != null) {
                appendLine("  - ID: ${user.id}")
                appendLine("  - Username: ${user.username}")
                appendLine("  - Email: ${user.email}")
                appendLine("  - Name: ${user.firstName} ${user.lastName}")
                appendLine("  - Role: ${user.role}")
                appendLine("  - Created: ${user.createdAt}")
            } else {
                appendLine("  - No user data available")
            }
        }
        debugInfo = info
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Authentication Debug Tool",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Debug Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current Authentication Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = debugInfo,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
        
        // Test Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        testResults = testResults + "🔄 Testing login..."
                        try {
                            val result = authService.login("admin", "admin") // Try default credentials
                            result.onSuccess {
                                testResults = testResults + "✅ Login test successful"
                            }.onError { error ->
                                testResults = testResults + "❌ Login test failed: ${error.message}"
                            }
                        } catch (e: Exception) {
                            testResults = testResults + "❌ Login test exception: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Test Login")
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        testResults = testResults + "🔄 Testing returns API..."
                        try {
                            val returnsViewModel = AppDependencies.container.returnsViewModel
                            val result = returnsViewModel.loadReturns()
                            result.onSuccess {
                                testResults = testResults + "✅ Returns API test successful"
                            }.onError { error ->
                                testResults = testResults + "❌ Returns API test failed: ${error.message}"
                            }
                        } catch (e: Exception) {
                            testResults = testResults + "❌ Returns API test exception: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Test Returns API")
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        testResults = testResults + "🔄 Clearing tokens..."
                        authService.logout()
                        testResults = testResults + "✅ Tokens cleared - should show login screen"
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear Tokens")
            }
            
            Button(
                onClick = {
                    testResults = emptyList()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear Results")
            }
        }
        
        // Test Results
        if (testResults.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Test Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    testResults.forEach { result ->
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = when {
                                result.contains("✅") -> Color(0xFF4CAF50)
                                result.contains("❌") -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
        
        // Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = """
                    1. Check the authentication status above
                    2. If not authenticated, use "Test Login" with your credentials
                    3. If login works, test the Returns API
                    4. If Returns API fails with 404, it's an authentication issue
                    5. Use "Clear Tokens" to force re-authentication
                    
                    Expected for working authentication:
                    - Is Authenticated: true
                    - Has Valid Tokens: true
                    - Is Token Expired: false
                    - Access Token: [30+ characters]
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Add this to your Main.kt Screen enum if you want to access it from navigation
// AUTHENTICATION_DEBUG("Authentication Debug", Icons.Default.BugReport)

// Add this to your MainAppContent when clause:
// Screen.AUTHENTICATION_DEBUG -> AuthenticationDebugScreen()
