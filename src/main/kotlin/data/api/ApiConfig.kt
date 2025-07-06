package data.api

/**
 * API Configuration constants and settings
 */
object ApiConfig {
    // Base URL for the backend API
    const val BASE_URL = "http://localhost:8081/api"
    
    // API Endpoints
    object Endpoints {
        // Authentication
        const val AUTH_LOGIN = "/auth/login"
        const val AUTH_SIGNUP = "/auth/signup"
        const val AUTH_REFRESH = "/auth/refresh"
        
        // Customers
        const val CUSTOMERS = "/customers"
        const val CUSTOMERS_SEARCH = "/customers/search"
        fun customerById(id: Long) = "/customers/$id"
        
        // Categories
        const val CATEGORIES = "/categories"
        const val CATEGORIES_ACTIVE = "/categories/active"
        const val CATEGORIES_SEARCH = "/categories/search"
        const val CATEGORIES_EMPTY = "/categories/empty"
        fun categoryById(id: Long) = "/categories/$id"
        fun categoryByName(name: String) = "/categories/name/$name"
        fun categoryByStatus(status: String) = "/categories/status/$status"
        fun categoryStatus(id: Long) = "/categories/$id/status"

        // Products
        const val PRODUCTS = "/products"
        const val PRODUCTS_SEARCH = "/products/search"
        fun productById(id: Long) = "/products/$id"
        fun productStock(id: Long) = "/products/$id/stock"
        fun productStockIncrease(id: Long) = "/products/$id/stock/increase"
        fun productStockDecrease(id: Long) = "/products/$id/stock/decrease"
        
        // Sales
        const val SALES = "/sales"
        fun saleById(id: Long) = "/sales/$id"
        fun salesByCustomer(customerId: Long) = "/sales/customer/$customerId"
        fun completeSale(id: Long) = "/sales/$id/complete"
        fun cancelSale(id: Long) = "/sales/$id/cancel"
        
        // Suppliers
        const val SUPPLIERS = "/suppliers"
        const val SUPPLIERS_SEARCH = "/suppliers/search"
        fun supplierById(id: Long) = "/suppliers/$id"
        fun supplierAnalytics(id: Long) = "/suppliers/$id/analytics"

        // Returns
        const val RETURNS = "/returns"
        fun returnById(id: Long) = "/returns/$id"
        fun approveReturn(id: Long) = "/returns/$id/approve"
        fun rejectReturn(id: Long) = "/returns/$id/reject"
        fun processRefund(id: Long) = "/returns/$id/refund"

        // Promotions
        const val PROMOTIONS = "/promotions"
        const val PROMOTIONS_ACTIVE = "/promotions/active"
        fun promotionById(id: Long) = "/promotions/$id"
        fun activatePromotion(id: Long) = "/promotions/$id/activate"
        fun deactivatePromotion(id: Long) = "/promotions/$id/deactivate"
        fun promotionAnalytics(id: Long) = "/promotions/$id/analytics"

        // Reports
        const val REPORTS_SALES = "/reports/sales"
        const val REPORTS_REVENUE = "/reports/revenue"
        const val REPORTS_TOP_PRODUCTS = "/reports/top-products"
        const val REPORTS_CUSTOMER_ANALYTICS = "/reports/customer-analytics"
        const val REPORTS_INVENTORY = "/reports/inventory"
        const val REPORTS_DASHBOARD = "/reports/dashboard"
    }
    
    // HTTP Configuration
    object Http {
        const val CONNECT_TIMEOUT = 30_000L // 30 seconds
        const val REQUEST_TIMEOUT = 60_000L // 60 seconds
        const val SOCKET_TIMEOUT = 60_000L // 60 seconds
        
        // Headers
        const val CONTENT_TYPE = "application/json"
        const val ACCEPT = "application/json"
        const val AUTHORIZATION = "Authorization"
        const val BEARER_PREFIX = "Bearer "
    }
    
    // Pagination defaults
    object Pagination {
        const val DEFAULT_PAGE = 0
        const val DEFAULT_SIZE = 10
        const val MAX_SIZE = 100
        const val DEFAULT_SORT_BY = "id"
        const val DEFAULT_SORT_DIR = "asc"
    }
    
    // Error codes
    object ErrorCodes {
        const val UNAUTHORIZED = 401
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val VALIDATION_ERROR = 400
        const val SERVER_ERROR = 500
        const val NETWORK_ERROR = -1
    }
}
