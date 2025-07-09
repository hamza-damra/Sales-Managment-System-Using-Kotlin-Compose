package data.di

import data.api.HttpClientProvider
import data.api.services.*
import data.auth.AuthService
import data.auth.TokenManager
import data.preferences.ThemePreferencesManager
import data.repository.*
import ui.viewmodels.*
import services.NotificationService
import io.ktor.client.*

/**
 * Dependency injection container for the application
 */
class AppContainer {
    
    // Core dependencies
    val tokenManager: TokenManager by lazy { TokenManager() }

    val themePreferencesManager: ThemePreferencesManager by lazy {
        ThemePreferencesManager()
    }

    // Notification service
    val notificationService: NotificationService by lazy {
        NotificationService()
    }

    val httpClient: HttpClient by lazy {
        HttpClientProvider.getClient(tokenManager)
    }
    
    // Authentication
    val authService: AuthService by lazy { 
        AuthService(httpClient, tokenManager) 
    }
    
    // API Services
    val customerApiService: CustomerApiService by lazy { 
        CustomerApiService(httpClient) 
    }
    
    val productApiService: ProductApiService by lazy { 
        ProductApiService(httpClient) 
    }
    
    val salesApiService: SalesApiService by lazy { 
        SalesApiService(httpClient) 
    }
    
    val reportsApiService: ReportsApiService by lazy {
        ReportsApiService(httpClient)
    }

    val supplierApiService: SupplierApiService by lazy {
        SupplierApiService(httpClient)
    }

    val returnApiService: ReturnApiService by lazy {
        ReturnApiService(httpClient)
    }

    val promotionApiService: PromotionApiService by lazy {
        PromotionApiService(httpClient)
    }

    val dashboardApiService: DashboardApiService by lazy {
        DashboardApiService(httpClient)
    }

    val categoryApiService: CategoryApiService by lazy {
        CategoryApiService(httpClient)
    }

    val inventoryApiService: InventoryApiService by lazy {
        InventoryApiService(httpClient)
    }

    val stockMovementApiService: StockMovementApiService by lazy {
        StockMovementApiService(httpClient)
    }

    // Repositories
    val customerRepository: CustomerRepository by lazy { 
        CustomerRepository(customerApiService) 
    }
    
    val productRepository: ProductRepository by lazy { 
        ProductRepository(productApiService) 
    }
    
    val salesRepository: SalesRepository by lazy { 
        SalesRepository(salesApiService) 
    }
    
    val reportsRepository: ReportsRepository by lazy {
        ReportsRepository(reportsApiService)
    }

    val supplierRepository: SupplierRepository by lazy {
        SupplierRepository(supplierApiService)
    }

    val returnRepository: ReturnRepository by lazy {
        ReturnRepository(returnApiService)
    }

    val promotionRepository: PromotionRepository by lazy {
        PromotionRepository(promotionApiService)
    }

    val dashboardRepository: DashboardRepository by lazy {
        DashboardRepository(dashboardApiService)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(categoryApiService)
    }

    val inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(inventoryApiService)
    }

    val stockMovementRepository: StockMovementRepository by lazy {
        StockMovementRepository(stockMovementApiService)
    }

    // ViewModels
    val dashboardViewModel: DashboardViewModel by lazy {
        DashboardViewModel(dashboardRepository)
    }

    val salesViewModel: SalesViewModel by lazy {
        SalesViewModel(salesRepository, customerRepository, productRepository, promotionRepository)
    }

    val productViewModel: ProductViewModel by lazy {
        ProductViewModel(productRepository, categoryRepository)
    }

    val categoryViewModel: CategoryViewModel by lazy {
        CategoryViewModel(categoryRepository)
    }

    val supplierViewModel: SupplierViewModel by lazy {
        SupplierViewModel(supplierRepository)
    }

    val returnsViewModel: ReturnsViewModel by lazy {
        ReturnsViewModel(returnRepository, customerRepository, productRepository)
    }

    val inventoryViewModel: InventoryViewModel by lazy {
        InventoryViewModel(inventoryRepository, stockMovementRepository, productRepository, categoryRepository)
    }

    val promotionViewModel: ui.viewmodels.PromotionViewModel by lazy {
        ui.viewmodels.PromotionViewModel(promotionRepository)
    }

    val reportsViewModel: ui.viewmodels.ReportsViewModel by lazy {
        ui.viewmodels.ReportsViewModel(reportsRepository)
    }

    /**
     * Clean up resources when the application is closing
     */
    fun cleanup() {
        HttpClientProvider.closeClient()
    }
}

/**
 * Global application container instance
 */
object AppDependencies {
    val container = AppContainer()
}
