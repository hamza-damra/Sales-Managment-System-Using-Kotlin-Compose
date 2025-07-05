import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ui.theme.AppTheme

@Composable
fun TestReturnsColors() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Testing Returns Screen Colors:")
        
        // Test card with surfaceVariant.copy(alpha = 0.3f) - should match dashboard
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Returns Card (surfaceVariant 0.3f)",
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Test primary color
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Primary Color Test",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Test AppTheme colors
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Success" to AppTheme.colors.success,
                "Warning" to AppTheme.colors.warning,
                "Error" to AppTheme.colors.error,
                "Info" to AppTheme.colors.info
            ).forEach { (name, color) ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = color.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        name,
                        modifier = Modifier.padding(8.dp),
                        color = color
                    )
                }
            }
        }
    }
}
