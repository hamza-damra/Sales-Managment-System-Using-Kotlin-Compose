package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.repository.UpdateRepository
import services.UpdateService
import services.NotificationService
import ui.viewmodels.UpdateViewModel

/**
 * Simple test version of UpdateScreen to check compilation
 */
@Composable
fun UpdateScreenTest(
    updateRepository: UpdateRepository,
    updateService: UpdateService,
    notificationService: NotificationService
) {
    val updateViewModel = remember {
        UpdateViewModel(updateRepository, updateService)
    }

    // Test basic state collection
    val updateState by updateViewModel.updateState.collectAsState()
    val uiState by updateViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Update System Test",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Current Version: ${updateState.currentVersion}",
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (updateState.updateAvailable) {
            Text(
                text = "Update Available: ${updateState.latestVersion}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Button(
            onClick = {
                // Test button click
            }
        ) {
            Text("Test Button")
        }
    }
}
