# How to Revert to Full API Integration

When your backend server is ready, revert these changes in `Main.kt`:

## 1. Re-enable Authentication

Change this:
```kotlin
// Temporary: Skip authentication for testing
// Show login screen if not authenticated
// if (!authState.isAuthenticated) {
//     LoginScreen(
//         authService = authService,
//         onLoginSuccess = {
//             // Authentication successful, main app will be shown
//         }
//     )
// } else {
    // Main application content
    MainAppContent(appContainer)
// }
```

Back to:
```kotlin
// Show login screen if not authenticated
if (!authState.isAuthenticated) {
    LoginScreen(
        authService = authService,
        onLoginSuccess = {
            // Authentication successful, main app will be shown
        }
    )
} else {
    // Main application content
    MainAppContent(appContainer)
}
```

## 2. Re-enable API Integration

Change this:
```kotlin
when (currentScreen) {
    Screen.DASHBOARD -> DashboardScreen(SalesDataManager()) // Temporary: Use mock data
    Screen.SALES -> SalesScreen(SalesDataManager()) // Temporary: Use mock data
    Screen.PRODUCTS -> ProductsScreen(SalesDataManager()) // Temporary: Use mock data
    Screen.CUSTOMERS -> CustomersScreen(SalesDataManager()) // Temporary: Use mock data
    Screen.INVENTORY -> InventoryScreen(SalesDataManager())
    Screen.SUPPLIERS -> SuppliersScreen(SalesDataManager())
    Screen.RETURNS -> ReturnsScreen()
    Screen.PROMOTIONS -> PromotionsScreen()
    Screen.REPORTS -> ReportsScreen() // Temporary: Use mock data
    Screen.SETTINGS -> SettingsScreen()
}
```

Back to:
```kotlin
when (currentScreen) {
    Screen.DASHBOARD -> DashboardScreenNew(appContainer.reportsRepository)
    Screen.SALES -> SalesScreenNew(
        salesRepository = appContainer.salesRepository,
        customerRepository = appContainer.customerRepository,
        productRepository = appContainer.productRepository
    )
    Screen.PRODUCTS -> ProductsScreenNew(appContainer.productRepository)
    Screen.CUSTOMERS -> CustomersScreenNew()
    Screen.INVENTORY -> InventoryScreen(SalesDataManager()) // TODO: Create InventoryRepository
    Screen.SUPPLIERS -> SuppliersScreen(SalesDataManager()) // TODO: Create SuppliersRepository
    Screen.RETURNS -> ReturnsScreen()
    Screen.PROMOTIONS -> PromotionsScreen()
    Screen.REPORTS -> ReportsScreenNew(appContainer.reportsRepository)
    Screen.SETTINGS -> SettingsScreen()
}
```

## 3. Test with Backend

1. Start your Spring Boot backend on localhost:8081
2. Run the frontend application
3. Test login and all API integrations
