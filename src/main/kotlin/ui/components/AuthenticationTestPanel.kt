package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.di.AppDependencies
import kotlinx.coroutines.launch
import utils.AuthenticationDebugger

/**
 * Test panel for verifying authentication fixes
 * Add this to any screen temporarily to test authentication
 */
@Composable
fun AuthenticationTestPanel(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var authStatus by remember { mutableStateOf<utils.AuthenticationStatus?>(null) }
    var testResult by remember { mutableStateOf<utils.AuthenticationTestResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Auto-check authentication status on load
    LaunchedEffect(Unit) {
        try {
            authStatus = AuthenticationDebugger.checkAuthenticationStatus()
            logs = logs + "✅ Authentication status loaded"
        } catch (e: Exception) {
            logs = logs + "❌ Failed to load auth status: ${e.message}"
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Authentication Test Panel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Divider()
            
            // Authentication Status
            authStatus?.let { status ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (status.isAuthenticated) 
                            Color(0xFF4CAF50).copy(alpha = 0.1f) 
                        else 
                            Color(0xFFF44336).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Authentication Status",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        StatusRow("Authenticated", status.isAuthenticated)
                        StatusRow("Has Access Token", status.hasAccessToken)
                        StatusRow("Has Refresh Token", status.hasRefreshToken)
                        StatusRow("Token Expired", status.isTokenExpired, isError = true)
                        StatusRow("Valid Tokens", status.hasValidTokens)
                        
                        if (status.user != null) {
                            Text(
                                text = "User: ${status.user.username} (${status.user.role})",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        if (status.accessTokenPreview != null) {
                            Text(
                                text = "Token: ${status.accessTokenPreview}...",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
            
            // Test Result
            testResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success) 
                            Color(0xFF4CAF50).copy(alpha = 0.1f) 
                        else 
                            Color(0xFFF44336).copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "API Test Result",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.success) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (result.success) "Success" else "Failed",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (result.success) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                        
                        if (result.error != null) {
                            Text(
                                text = "Error: ${result.error}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF44336)
                            )
                        }
                        
                        Text(
                            text = "💡 ${result.recommendation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                authStatus = AuthenticationDebugger.checkAuthenticationStatus()
                                logs = logs + "🔄 Authentication status refreshed"
                            } catch (e: Exception) {
                                logs = logs + "❌ Failed to refresh: ${e.message}"
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh")
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                testResult = AuthenticationDebugger.testAuthentication()
                                logs = logs + "🧪 API test completed"
                            } catch (e: Exception) {
                                logs = logs + "❌ Test failed: ${e.message}"
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Api, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Test API")
                }
            }
            
            // Debug Logs
            if (logs.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Debug Logs",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        logs.takeLast(5).forEach { log ->
                            Text(
                                text = log,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = when {
                                    log.contains("✅") -> Color(0xFF4CAF50)
                                    log.contains("❌") -> Color(0xFFF44336)
                                    log.contains("🔄") -> Color(0xFF2196F3)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        
                        if (logs.size > 5) {
                            Text(
                                text = "... and ${logs.size - 5} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Instructions
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = """
                        1. Check authentication status above
                        2. If not authenticated, login first
                        3. Use "Test API" to verify backend connectivity
                        4. Green = Working, Red = Needs attention
                        5. Remove this panel after testing
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: Boolean,
    isError: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (value xor isError) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (value xor isError) Color(0xFF4CAF50) else Color(0xFFF44336),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}
