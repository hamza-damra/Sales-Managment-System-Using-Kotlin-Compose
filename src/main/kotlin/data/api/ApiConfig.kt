package data.api

/**
 * API Configuration constants and settings
 */
object ApiConfig {
    // Base URL for the backend API
    const val BASE_URL = "http://localhost:8081"
    
    // API Endpoints
    object Endpoints {
        // Authentication
        const val AUTH_LOGIN = "/api/auth/login"
        const val AUTH_SIGNUP = "/api/auth/signup"
        const val AUTH_REFRESH = "/api/auth/refresh"
        
        // Customers
        const val CUSTOMERS = "/api/customers"
        const val CUSTOMERS_SEARCH = "/api/customers/search"
        fun customerById(id: Long) = "/api/customers/$id"

        // Categories
        const val CATEGORIES = "/api/categories"
        const val CATEGORIES_ACTIVE = "/api/categories/active"
        const val CATEGORIES_SEARCH = "/api/categories/search"
        const val CATEGORIES_EMPTY = "/api/categories/empty"
        fun categoryById(id: Long) = "/api/categories/$id"
        fun categoryByName(name: String) = "/api/categories/name/$name"
        fun categoryByStatus(status: String) = "/api/categories/status/$status"
        fun categoryStatus(id: Long) = "/api/categories/$id/status"

        // Products
        const val PRODUCTS = "/api/products"
        const val PRODUCTS_SEARCH = "/api/products/search"
        fun productById(id: Long) = "/api/products/$id"
        fun productStock(id: Long) = "/api/products/$id/stock"
        fun productStockIncrease(id: Long) = "/api/products/$id/stock/increase"
        fun productStockDecrease(id: Long) = "/api/products/$id/stock/decrease"

        // Sales
        const val SALES = "/api/sales"
        fun saleById(id: Long) = "/api/sales/$id"
        fun salesByCustomer(customerId: Long) = "/api/sales/customer/$customerId"
        fun completeSale(id: Long) = "/api/sales/$id/complete"
        fun cancelSale(id: Long) = "/api/sales/$id/cancel"

        // Suppliers
        const val SUPPLIERS = "/api/suppliers"
        const val SUPPLIERS_SEARCH = "/api/suppliers/search"
        fun supplierById(id: Long) = "/api/suppliers/$id"
        fun supplierAnalytics(id: Long) = "/api/suppliers/$id/analytics"

        // Returns
        const val RETURNS = "/api/returns"
        fun returnById(id: Long) = "/api/returns/$id"
        fun approveReturn(id: Long) = "/api/returns/$id/approve"
        fun rejectReturn(id: Long) = "/api/returns/$id/reject"
        fun processRefund(id: Long) = "/api/returns/$id/refund"

        // Promotions
        const val PROMOTIONS = "/api/promotions"
        const val PROMOTIONS_ACTIVE = "/api/promotions/active"
        const val PROMOTIONS_AVAILABLE = "/api/promotions/available"
        const val PROMOTIONS_EXPIRED = "/api/promotions/expired"
        const val PROMOTIONS_SCHEDULED = "/api/promotions/scheduled"
        const val PROMOTIONS_SEARCH = "/api/promotions/search"
        fun promotionById(id: Long) = "/api/promotions/$id"
        fun activatePromotion(id: Long) = "/api/promotions/$id/activate"
        fun deactivatePromotion(id: Long) = "/api/promotions/$id/deactivate"
        fun promotionAnalytics(id: Long) = "/api/promotions/$id/analytics"
        fun promotionsByType(type: String) = "/api/promotions/type/$type"
        fun promotionsByEligibility(eligibility: String) = "/api/promotions/eligibility/$eligibility"
        fun promotionsForProduct(productId: Long) = "/api/promotions/product/$productId"
        fun promotionsForCategory(category: String) = "/api/promotions/category/$category"
        fun validateCoupon(couponCode: String) = "/api/promotions/coupon/$couponCode"
        fun applyPromotion(id: Long) = "/api/promotions/$id/apply"

        // Inventories
        const val INVENTORIES = "/api/inventories"
        const val INVENTORIES_SEARCH = "/api/inventories/search"
        const val INVENTORIES_ACTIVE = "/api/inventories/active"
        const val INVENTORIES_MAIN_WAREHOUSES = "/api/inventories/main-warehouses"
        const val INVENTORIES_EMPTY = "/api/inventories/empty"
        const val INVENTORIES_NEAR_CAPACITY = "/api/inventories/near-capacity"
        fun inventoryById(id: Long) = "/api/inventories/$id"
        fun inventoryByName(name: String) = "/api/inventories/name/$name"
        fun inventoryByWarehouseCode(code: String) = "/api/inventories/warehouse-code/$code"
        fun inventoryByStatus(status: String) = "/api/inventories/status/$status"
        fun inventoryStatus(id: Long) = "/api/inventories/$id/status"

        // Reports - Enterprise Reporting API v1
        const val REPORTS_BASE = "/api/v1/reports"

        // Sales Reports
        const val REPORTS_SALES_COMPREHENSIVE = "$REPORTS_BASE/sales/comprehensive"
        const val REPORTS_SALES_SUMMARY = "$REPORTS_BASE/sales/summary"
        const val REPORTS_SALES_TRENDS = "$REPORTS_BASE/sales/trends"
        const val REPORTS_SALES_EXPORT = "$REPORTS_BASE/sales/export"

        // Customer Reports
        const val REPORTS_CUSTOMERS_ANALYTICS = "$REPORTS_BASE/customers/analytics"
        const val REPORTS_CUSTOMERS_LIFETIME_VALUE = "$REPORTS_BASE/customers/lifetime-value"
        const val REPORTS_CUSTOMERS_RETENTION = "$REPORTS_BASE/customers/retention"

        // Product Reports
        const val REPORTS_PRODUCTS_PERFORMANCE = "$REPORTS_BASE/products/performance"
        const val REPORTS_PRODUCTS_INVENTORY_TURNOVER = "$REPORTS_BASE/products/inventory-turnover"

        // Inventory Reports
        const val REPORTS_INVENTORY_STATUS = "$REPORTS_BASE/inventory/status"
        const val REPORTS_INVENTORY_VALUATION = "$REPORTS_BASE/inventory/valuation"

        // Promotion Reports
        const val REPORTS_PROMOTIONS_EFFECTIVENESS = "$REPORTS_BASE/promotions/effectiveness"
        const val REPORTS_PROMOTIONS_USAGE = "$REPORTS_BASE/promotions/usage"

        // Financial Reports
        const val REPORTS_FINANCIAL_REVENUE = "$REPORTS_BASE/financial/revenue"

        // Dashboard & KPI Reports
        const val REPORTS_DASHBOARD_EXECUTIVE = "$REPORTS_BASE/dashboard/executive"
        const val REPORTS_DASHBOARD_OPERATIONAL = "$REPORTS_BASE/dashboard/operational"
        const val REPORTS_KPI_REAL_TIME = "$REPORTS_BASE/kpi/real-time"

        // Export Functionality
        const val REPORTS_EXPORT = "$REPORTS_BASE/export"
        const val REPORTS_EXPORT_ASYNC = "$REPORTS_BASE/export/async"

        // Legacy Reports (for backward compatibility)
        const val REPORTS_SALES = "/api/reports/sales"
        const val REPORTS_REVENUE = "/api/reports/revenue"
        const val REPORTS_TOP_PRODUCTS = "/api/reports/top-products"
        const val REPORTS_CUSTOMER_ANALYTICS = "/api/reports/customer-analytics"
        const val REPORTS_INVENTORY = "/api/reports/inventory"
        const val REPORTS_DASHBOARD = "/api/reports/dashboard"
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
