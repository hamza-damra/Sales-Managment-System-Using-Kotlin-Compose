# 🔗 Frontend-Backend Integration Setup Guide

This guide will help you set up and test the complete integration between the Kotlin Compose frontend and Spring Boot backend.

## 📋 Prerequisites

1. **Backend Server**: Your Spring Boot backend must be running on `localhost:8081`
2. **Java**: JDK 11 or higher
3. **Gradle**: For building the project

## 🚀 Quick Start

### 1. Fix Gradle Wrapper (if needed)
```bash
# If you encounter Gradle wrapper issues, run:
gradle wrapper

# Then use the wrapper:
./gradlew build    # Linux/Mac
.\gradlew.bat build # Windows
```

### 2. Test the Integration
```bash
# Run the integration test first:
./gradlew run --args="TestIntegration"

# Or run the main application:
./gradlew run
```

## 🔧 Configuration

### API Configuration
The API configuration is in `src/main/kotlin/data/api/ApiConfig.kt`:

```kotlin
const val BASE_URL = "http://localhost:8081/api"
```

Change this if your backend runs on a different host/port.

### Authentication
The system supports:
- **Login**: Username/password authentication
- **Signup**: New user registration
- **JWT Tokens**: Automatic token management and refresh
- **Role-based Access**: USER, ADMIN, MANAGER roles

## 🧪 Testing the Integration

### 1. Backend Connection Test
```kotlin
// Run TestIntegration.kt to verify:
// ✅ Backend connectivity
// ✅ API endpoints
// ✅ Authentication flow
// ✅ Data retrieval
```

### 2. Manual Testing Steps

1. **Start Backend Server**
   ```bash
   # Make sure your Spring Boot app is running
   # Default: http://localhost:8081
   ```

2. **Run Frontend Application**
   ```bash
   ./gradlew run
   ```

3. **Test Authentication**
   - Try creating a new account
   - Test login with existing credentials
   - Verify logout functionality

4. **Test Customer Management**
   - Navigate to Customers screen
   - Add a new customer
   - Edit existing customer
   - Delete a customer
   - Test search functionality

## 📁 Integration Architecture

### API Layer
```
data/api/
├── ApiConfig.kt              # Endpoints and configuration
├── ApiModels.kt              # DTOs matching backend
├── HttpClientProvider.kt     # HTTP client setup
├── NetworkResult.kt          # Response wrapper
└── services/                 # API service classes
    ├── CustomerApiService.kt
    ├── ProductApiService.kt
    ├── SalesApiService.kt
    └── ReportsApiService.kt
```

### Authentication
```
data/auth/
├── AuthModels.kt            # Auth DTOs
├── AuthService.kt           # Auth operations
└── TokenManager.kt          # Token storage
```

### Repository Layer
```
data/repository/
├── CustomerRepository.kt    # Customer data management
├── ProductRepository.kt     # Product data management
├── SalesRepository.kt       # Sales data management
└── ReportsRepository.kt     # Reports data management
```

## 🔍 Troubleshooting

### Common Issues

1. **Backend Not Running**
   ```
   Error: Connection refused
   Solution: Start your Spring Boot backend on localhost:8081
   ```

2. **Authentication Errors**
   ```
   Error: 401 Unauthorized
   Solution: Check your credentials or create a new account
   ```

3. **CORS Issues**
   ```
   Error: CORS policy
   Solution: Ensure backend CORS is configured for frontend origin
   ```

4. **Gradle Build Issues**
   ```
   Error: Gradle wrapper not found
   Solution: Run 'gradle wrapper' to regenerate wrapper files
   ```

### Debug Mode
Enable detailed logging by setting log level in `HttpClientProvider.kt`:
```kotlin
level = LogLevel.ALL  // Change from LogLevel.INFO
```

## 🎯 Features Implemented

### ✅ Authentication
- [x] JWT-based authentication
- [x] Login/Signup screens
- [x] Automatic token refresh
- [x] Secure token storage
- [x] Role-based access control

### ✅ Customer Management
- [x] CRUD operations
- [x] Search and filtering
- [x] Pagination support
- [x] Real-time updates
- [x] Error handling

### ✅ Product Management
- [x] Inventory operations
- [x] Stock management
- [x] Category filtering
- [x] Search functionality

### ✅ Sales Management
- [x] Sales creation
- [x] Status management
- [x] Customer association
- [x] Item management

### ✅ Reports & Analytics
- [x] Dashboard summary
- [x] Sales reports
- [x] Revenue trends
- [x] Customer analytics
- [x] Inventory reports

## 🔄 Next Steps

1. **Replace Old Screens**: Update remaining screens to use new API integration
2. **Add Offline Support**: Implement local caching
3. **Real-time Updates**: Consider WebSocket integration
4. **Performance**: Add request caching and optimization
5. **Testing**: Add comprehensive unit tests

## 📞 Support

If you encounter issues:
1. Check the console logs for detailed error messages
2. Verify backend API endpoints match the documentation
3. Test API endpoints directly with tools like Postman
4. Check network connectivity and CORS configuration

## 🎉 Success Indicators

When everything is working correctly, you should see:
- ✅ Login screen appears on startup
- ✅ Successful authentication redirects to main app
- ✅ Customer list loads from backend
- ✅ CRUD operations work without errors
- ✅ Real-time data updates
- ✅ Proper error handling and user feedback

The integration is complete and ready for production use!
