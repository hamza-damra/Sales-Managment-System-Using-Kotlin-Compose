package data.di

import data.api.HttpClientProvider
import data.api.services.*
import data.auth.AuthService
import data.auth.TokenManager
import data.repository.*
import io.ktor.client.*

/**
 * Dependency injection container for the application
 */
class AppContainer {
    
    // Core dependencies
    val tokenManager: TokenManager by lazy { TokenManager() }
    
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
