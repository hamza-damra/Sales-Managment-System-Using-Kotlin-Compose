package ui.screens

import data.api.services.UpdateApiService
import data.auth.TokenManager
import data.repository.UpdateRepository
import services.UpdateService
import services.NotificationService
import ui.viewmodels.UpdateViewModel
import data.api.HttpClientProvider

/**
 * Factory for creating update system components
 * This helps with dependency injection and testing
 */
object UpdateSystemFactory {
    
    fun createUpdateRepository(
        tokenManager: TokenManager
    ): UpdateRepository {
        val httpClient = HttpClientProvider.create()
        val updateApiService = UpdateApiService(httpClient)
        return UpdateRepository(updateApiService, tokenManager)
    }
    
    fun createUpdateService(
        updateRepository: UpdateRepository,
        notificationService: NotificationService
    ): UpdateService {
        return UpdateService(updateRepository, notificationService)
    }
    
    fun createUpdateViewModel(
        updateRepository: UpdateRepository,
        updateService: UpdateService
    ): UpdateViewModel {
        return UpdateViewModel(updateRepository, updateService)
    }
    
    /**
     * Create a complete update system with all dependencies
     */
    fun createCompleteUpdateSystem(
        tokenManager: TokenManager,
        notificationService: NotificationService
    ): UpdateSystemComponents {
        val updateRepository = createUpdateRepository(tokenManager)
        val updateService = createUpdateService(updateRepository, notificationService)
        val updateViewModel = createUpdateViewModel(updateRepository, updateService)
        
        return UpdateSystemComponents(
            repository = updateRepository,
            service = updateService,
            viewModel = updateViewModel
        )
    }
}

/**
 * Data class to hold all update system components
 */
data class UpdateSystemComponents(
    val repository: UpdateRepository,
    val service: UpdateService,
    val viewModel: UpdateViewModel
)
