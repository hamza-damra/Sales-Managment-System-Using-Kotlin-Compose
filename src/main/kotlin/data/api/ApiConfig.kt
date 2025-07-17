package data.api

import utils.Constants

/**
 * API Configuration constants and settings
 */
object ApiConfig {
    // Base URL for the backend API - using centralized configuration
    const val BASE_URL = Constants.BASE_URL
    
    // API Endpoints
    object Endpoints {
        // Authentication
        const val AUTH_LOGIN = "/api/v1/auth/login"
        const val AUTH_SIGNUP = "/api/v1/auth/signup"
        const val AUTH_REFRESH = "/api/v1/auth/refresh"

        // Customers
        const val CUSTOMERS = "/api/v1/customers"
        const val CUSTOMERS_SEARCH = "/api/v1/customers/search"
        fun customerById(id: Long) = "/api/v1/customers/$id"

        // Categories
        const val CATEGORIES = "/api/v1/categories"
        const val CATEGORIES_ACTIVE = "/api/v1/categories/active"
        const val CATEGORIES_SEARCH = "/api/v1/categories/search"
        const val CATEGORIES_EMPTY = "/api/v1/categories/empty"
        fun categoryById(id: Long) = "/api/v1/categories/$id"
        fun categoryByName(name: String) = "/api/v1/categories/name/$name"
        fun categoryByStatus(status: String) = "/api/v1/categories/status/$status"
        fun categoryStatus(id: Long) = "/api/v1/categories/$id/status"

        // Products
        const val PRODUCTS = "/api/v1/products"
        const val PRODUCTS_SEARCH = "/api/v1/products/search"
        const val PRODUCTS_RECENT = "/api/v1/products/recent"
        fun productById(id: Long) = "/api/v1/products/$id"
        fun productStock(id: Long) = "/api/v1/products/$id/stock"
        fun productStockIncrease(id: Long) = "/api/v1/products/$id/stock/increase"
        fun productStockDecrease(id: Long) = "/api/v1/products/$id/stock/decrease"

        // Sales
        const val SALES = "/api/v1/sales"
        fun saleById(id: Long) = "/api/v1/sales/$id"
        fun salesByCustomer(customerId: Long) = "/api/v1/sales/customer/$customerId"
        fun completeSale(id: Long) = "/api/v1/sales/$id/complete"
        fun cancelSale(id: Long) = "/api/v1/sales/$id/cancel"

        // Suppliers
        const val SUPPLIERS = "/api/v1/suppliers"
        const val SUPPLIERS_SEARCH = "/api/v1/suppliers/search"
        const val SUPPLIERS_TOP_RATED = "/api/v1/suppliers/top-rated"
        const val SUPPLIERS_HIGH_VALUE = "/api/v1/suppliers/high-value"
        const val SUPPLIERS_ANALYTICS = "/api/v1/suppliers/analytics"
        fun supplierById(id: Long) = "/api/v1/suppliers/$id"
        fun supplierWithOrders(id: Long) = "/api/v1/suppliers/$id/orders"
        fun supplierRating(id: Long) = "/api/v1/suppliers/$id/rating"
        fun supplierAnalytics(id: Long) = "/api/v1/suppliers/$id/analytics"

        // Purchase Orders
        const val PURCHASE_ORDERS = "/api/v1/purchase-orders"
        const val PURCHASE_ORDERS_SEARCH = "/api/v1/purchase-orders/search"
        const val PURCHASE_ORDERS_ANALYTICS = "/api/v1/purchase-orders/analytics"
        fun purchaseOrderById(id: Long) = "/api/v1/purchase-orders/$id"
        fun purchaseOrderStatus(id: Long) = "/api/v1/purchase-orders/$id/status"
        fun purchaseOrderApprove(id: Long) = "/api/v1/purchase-orders/$id/approve"
        fun purchaseOrderReceive(id: Long) = "/api/v1/purchase-orders/$id/receive"
        fun purchaseOrdersBySupplier(supplierId: Long) = "/api/v1/purchase-orders/supplier/$supplierId"
        fun purchaseOrderPdf(id: Long) = "/api/v1/purchase-orders/$id/pdf"
        fun purchaseOrderSend(id: Long) = "/api/v1/purchase-orders/$id/send"

        // Returns
        const val RETURNS = "/api/v1/returns"
        fun returnById(id: Long) = "/api/v1/returns/$id"
        fun approveReturn(id: Long) = "/api/v1/returns/$id/approve"
        fun rejectReturn(id: Long) = "/api/v1/returns/$id/reject"
        fun processRefund(id: Long) = "/api/v1/returns/$id/refund"

        // Promotions
        const val PROMOTIONS = "/api/v1/promotions"
        const val PROMOTIONS_ACTIVE = "/api/v1/promotions/active"
        const val PROMOTIONS_AVAILABLE = "/api/v1/promotions/available"
        const val PROMOTIONS_SEARCH = "/api/v1/promotions/search"
        fun promotionById(id: Long) = "/api/v1/promotions/$id"
        fun activatePromotion(id: Long) = "/api/v1/promotions/$id/activate"
        fun deactivatePromotion(id: Long) = "/api/v1/promotions/$id/deactivate"
        fun promotionAnalytics(id: Long) = "/api/v1/promotions/$id/analytics"
        fun promotionsByType(type: String) = "/api/v1/promotions/type/$type"
        fun promotionsByEligibility(eligibility: String) = "/api/v1/promotions/eligibility/$eligibility"
        fun promotionsForProduct(productId: Long) = "/api/v1/promotions/product/$productId"
        fun promotionsForCategory(category: String) = "/api/v1/promotions/category/$category"
        fun validateCoupon(couponCode: String) = "/api/v1/promotions/coupon/$couponCode"
        fun applyPromotion(id: Long) = "/api/v1/promotions/$id/apply"

        // Inventories
        const val INVENTORIES = "/api/v1/inventories"
        const val INVENTORIES_SEARCH = "/api/v1/inventories/search"
        const val INVENTORIES_ACTIVE = "/api/v1/inventories/active"
        const val INVENTORIES_MAIN_WAREHOUSES = "/api/v1/inventories/main-warehouses"
        const val INVENTORIES_EMPTY = "/api/v1/inventories/empty"
        const val INVENTORIES_NEAR_CAPACITY = "/api/v1/inventories/near-capacity"
        fun inventoryById(id: Long) = "/api/v1/inventories/$id"
        fun inventoryByName(name: String) = "/api/v1/inventories/name/$name"
        fun inventoryByWarehouseCode(code: String) = "/api/v1/inventories/warehouse-code/$code"
        fun inventoryByStatus(status: String) = "/api/v1/inventories/status/$status"
        fun inventoryStatus(id: Long) = "/api/v1/inventories/$id/status"

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
        const val REPORTS_SALES = "/api/v1/reports/sales"
        const val REPORTS_REVENUE = "/api/v1/reports/revenue"
        const val REPORTS_TOP_PRODUCTS = "/api/v1/reports/top-products"
        const val REPORTS_CUSTOMER_ANALYTICS = "/api/v1/reports/customer-analytics"
        const val REPORTS_INVENTORY = "/api/v1/reports/inventory"
        const val REPORTS_DASHBOARD = "/api/v1/reports/dashboard"

        // Tax Configuration (for future backend integration)
        const val TAX_SETTINGS = "/api/v1/settings/tax"
        const val TAX_RATES = "/api/v1/settings/tax/rates"
        const val TAX_REGIONS = "/api/v1/settings/tax/regions"
        fun taxRateByRegion(region: String) = "/api/v1/settings/tax/rates/$region"
        fun taxSettingsValidate() = "/api/v1/settings/tax/validate"

        // Update System Endpoints
        const val UPDATES_BASE = "/api/v1/updates"
        const val UPDATES_CHECK = "$UPDATES_BASE/check"
        const val UPDATES_LATEST = "$UPDATES_BASE/latest"
        const val UPDATES_COMPATIBILITY = "$UPDATES_BASE/compatibility"
        const val UPDATES_DELTA = "$UPDATES_BASE/delta"
        fun updateDownload(version: String) = "$UPDATES_BASE/download/$version"
        fun updateVersion(version: String) = "$UPDATES_BASE/version/$version"
        fun updateCompatibility(version: String) = "$UPDATES_BASE/compatibility/$version"
        fun updateDelta(fromVersion: String, toVersion: String) = "$UPDATES_BASE/delta/$fromVersion/$toVersion"
        fun updateDeltaDownload(fromVersion: String, toVersion: String) = "$UPDATES_BASE/delta/download/$fromVersion/$toVersion"
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
