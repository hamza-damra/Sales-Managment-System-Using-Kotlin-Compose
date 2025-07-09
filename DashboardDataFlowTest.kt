import data.api.services.DashboardApiService
import data.api.HttpClientProvider
import data.repository.DashboardRepository
import ui.viewmodels.DashboardViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

/**
 * Test to verify dashboard data flow from API to ViewModel
 */
fun main() {
    println("🧪 Starting Dashboard Data Flow Test...")
    
    runBlocking {
        try {
            // Initialize components
            val httpClient = HttpClientProvider.create()
            val dashboardApiService = DashboardApiService(httpClient)
            val dashboardRepository = DashboardRepository(dashboardApiService)
            val dashboardViewModel = DashboardViewModel(dashboardRepository)
            
            println("✅ Components initialized successfully")
            
            // Test 1: Direct API call
            println("\n📡 Test 1: Direct API Service Call")
            val apiResult = dashboardApiService.getDashboardSummary()
            println("API Result type: ${apiResult::class.simpleName}")
            
            when (apiResult) {
                is data.api.NetworkResult.Success -> {
                    val data = apiResult.data
                    println("✅ API call successful")
                    println("📊 Sales total: ${data?.sales?.totalSales}")
                    println("📊 Revenue total: ${data?.sales?.totalRevenue}")
                    println("📊 Customers total: ${data?.customers?.totalCustomers}")
                    println("📊 Period: ${data?.period}")
                }
                is data.api.NetworkResult.Error -> {
                    println("❌ API call failed: ${apiResult.exception.message}")
                }
                else -> {
                    println("⚠️ Unexpected result type")
                }
            }
            
            // Test 2: Repository call
            println("\n📦 Test 2: Repository Call")
            val repositoryResult = dashboardRepository.getDashboardSummary().first()
            println("Repository Result type: ${repositoryResult::class.simpleName}")
            
            when (repositoryResult) {
                is data.api.NetworkResult.Success -> {
                    val data = repositoryResult.data
                    println("✅ Repository call successful")
                    println("📊 Sales total: ${data?.sales?.totalSales}")
                    println("📊 Revenue total: ${data?.sales?.totalRevenue}")
                    println("📊 Customers total: ${data?.customers?.totalCustomers}")
                }
                is data.api.NetworkResult.Error -> {
                    println("❌ Repository call failed: ${repositoryResult.exception.message}")
                }
                else -> {
                    println("⚠️ Unexpected result type")
                }
            }
            
            // Test 3: ViewModel state
            println("\n🎯 Test 3: ViewModel State")
            dashboardViewModel.loadDashboardData()
            
            // Wait a bit for the data to load
            kotlinx.coroutines.delay(2000)
            
            val uiState = dashboardViewModel.uiState.value
            println("UI State - hasData: ${uiState.hasData}")
            println("UI State - isLoading: ${uiState.isLoading}")
            println("UI State - hasError: ${uiState.hasError}")
            println("UI State - error: ${uiState.error}")
            println("UI State - isUsingMockData: ${uiState.isUsingMockData}")
            
            if (uiState.hasData) {
                val data = uiState.dashboardSummary
                println("✅ ViewModel has data")
                println("📊 Sales total: ${data?.sales?.totalSales}")
                println("📊 Revenue total: ${data?.sales?.totalRevenue}")
                println("📊 Customers total: ${data?.customers?.totalCustomers}")
                println("📊 Period: ${data?.period}")
            } else {
                println("❌ ViewModel has no data")
            }
            
            println("\n🏁 Dashboard Data Flow Test Complete")
            
        } catch (e: Exception) {
            println("❌ Test failed with exception: ${e.message}")
            e.printStackTrace()
        }
    }
}
