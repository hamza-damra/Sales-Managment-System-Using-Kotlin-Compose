package data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Authentication DTOs
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class SignupRequest(
    @SerialName("username") val username: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("firstName") val firstName: String,
    @SerialName("lastName") val lastName: String,
    @SerialName("role") val role: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val user: UserDTO? = null
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

@Serializable
data class UserDTO(
    val id: Long,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val createdAt: String
)

// Customer DTOs
@Serializable
data class CustomerDTO(
    val id: Long? = null,
    val name: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val dateOfBirth: String? = null, // ISO date
    val gender: String? = null, // MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    val customerType: String? = null, // REGULAR, PREMIUM, VIP
    val customerStatus: String? = null, // ACTIVE, INACTIVE, SUSPENDED
    val billingAddress: String? = null,
    val shippingAddress: String? = null,
    val preferredPaymentMethod: String? = null,
    val creditLimit: Double? = null,
    val currentBalance: Double? = null,
    val loyaltyPoints: Int? = null,
    val taxNumber: String? = null,
    val companyName: String? = null,
    val website: String? = null,
    val notes: String? = null,
    val lastPurchaseDate: String? = null, // ISO datetime
    val totalPurchases: Double? = null,
    val isEmailVerified: Boolean? = null,
    val isPhoneVerified: Boolean? = null,
    val createdAt: String? = null, // ISO datetime
    val updatedAt: String? = null // ISO datetime
)

// Category DTOs
@Serializable
data class CategoryDTO(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val displayOrder: Int? = null,
    val status: String? = null, // ACTIVE, INACTIVE, ARCHIVED
    val imageUrl: String? = null,
    val icon: String? = null,
    val colorCode: String? = null,
    val inventoryId: Long? = null, // Associated inventory/warehouse ID
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val productCount: Int? = null
)

@Serializable
data class CategoryStatusUpdateRequest(
    val status: String
)

// Product DTOs
@Serializable
data class ProductDTO(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val price: Double,
    val costPrice: Double? = null,
    val stockQuantity: Int? = null,
    val category: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val sku: String? = null,
    val brand: String? = null,
    val modelNumber: String? = null,
    val barcode: String? = null,
    val weight: Double? = null,
    val length: Double? = null,
    val width: Double? = null,
    val height: Double? = null,
    val productStatus: String? = null, // ACTIVE, INACTIVE, DISCONTINUED
    val minStockLevel: Int? = null,
    val maxStockLevel: Int? = null,
    val reorderPoint: Int? = null,
    val reorderQuantity: Int? = null,
    val supplierName: String? = null,
    val supplierCode: String? = null,
    val warrantyPeriod: Int? = null,
    val expiryDate: String? = null, // ISO date
    val manufacturingDate: String? = null, // ISO date
    val tags: List<String>? = null,
    val imageUrl: String? = null,
    val additionalImages: List<String>? = null,
    val isSerialized: Boolean? = null,
    val isDigital: Boolean? = null,
    val isTaxable: Boolean? = null,
    val taxRate: Double? = null,
    val unitOfMeasure: String? = null,
    val discountPercentage: Double? = null,
    val locationInWarehouse: String? = null,
    val totalSold: Int? = null,
    val totalRevenue: Double? = null,
    val lastSoldDate: String? = null, // ISO datetime
    val lastRestockedDate: String? = null, // ISO datetime
    val notes: String? = null,
    val createdAt: String? = null, // ISO datetime
    val updatedAt: String? = null // ISO datetime
)

// Sale DTOs
@Serializable
data class SaleDTO(
    val id: Long? = null,
    val customerId: Long,
    val customerName: String? = null,
    val saleDate: String? = null, // ISO datetime
    val totalAmount: Double,
    val status: String? = null, // PENDING, COMPLETED, CANCELLED, REFUNDED
    val items: List<SaleItemDTO> = emptyList(),
    val saleNumber: String? = null,
    val referenceNumber: String? = null,
    val subtotal: Double? = null,
    val discountAmount: Double? = null,
    val discountPercentage: Double? = null,

    // Enhanced promotion fields to match backend
    val promotionId: Long? = null, // Primary promotion ID
    val couponCode: String? = null, // Coupon code used
    val originalTotal: Double? = null, // Total before promotions
    val finalTotal: Double? = null, // Total after promotions
    val promotionDiscountAmount: Double? = null, // Total discount from promotions
    val appliedPromotions: List<AppliedPromotionDTO>? = null, // List of all applied promotions
    val promotionDetails: PromotionDTO? = null, // Details of primary promotion
    val totalSavings: Double? = null, // Total amount saved from all promotions
    val hasPromotions: Boolean? = null, // Boolean indicating if promotions are applied
    val promotionCount: Int? = null, // Number of promotions applied

    // Legacy promotion fields (for backward compatibility)
    val appliedPromotionId: Long? = null,
    val appliedPromotionCode: String? = null,
    val appliedPromotionName: String? = null,

    val taxAmount: Double? = null,
    val taxPercentage: Double? = null,
    val shippingCost: Double? = null,
    val paymentMethod: String? = null, // CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CHECK, DIGITAL_WALLET
    val paymentStatus: String? = null, // PENDING, PAID, PARTIAL, OVERDUE, REFUNDED
    val paymentDate: String? = null, // ISO datetime
    val dueDate: String? = null, // ISO date
    val billingAddress: String? = null,
    val shippingAddress: String? = null,
    val salesPerson: String? = null,
    val salesChannel: String? = null,
    val saleType: String? = null, // REGULAR, WHOLESALE, RETAIL, ONLINE, RETURN
    val currency: String? = null,
    val exchangeRate: Double? = null,
    val notes: String? = null,
    val internalNotes: String? = null,
    val termsAndConditions: String? = null,
    val warrantyInfo: String? = null,
    val deliveryDate: String? = null, // ISO datetime
    val expectedDeliveryDate: String? = null, // ISO date
    val deliveryStatus: String? = null, // PENDING, SHIPPED, DELIVERED, CANCELLED
    val trackingNumber: String? = null,
    val isGift: Boolean? = null,
    val giftMessage: String? = null,
    val loyaltyPointsEarned: Int? = null,
    val loyaltyPointsUsed: Int? = null,
    val isReturn: Boolean? = null,
    val originalSaleId: Long? = null,
    val returnReason: String? = null,
    val profitMargin: Double? = null,
    val costOfGoodsSold: Double? = null,
    val createdAt: String? = null, // ISO datetime
    val updatedAt: String? = null // ISO datetime
)

@Serializable
data class SaleItemDTO(
    val id: Long? = null,
    val productId: Long,
    val productName: String? = null,
    val quantity: Int,
    val unitPrice: Double,
    val originalUnitPrice: Double? = null,
    val costPrice: Double? = null,
    val discountPercentage: Double? = null,
    val discountAmount: Double? = null,
    val taxPercentage: Double? = null,
    val taxAmount: Double? = null,
    val subtotal: Double? = null,
    val totalPrice: Double? = null,
    val serialNumbers: String? = null,
    val warrantyInfo: String? = null,
    val notes: String? = null,
    val isReturned: Boolean? = null,
    val returnedQuantity: Int? = null,
    val unitOfMeasure: String? = null
)

// Pagination Response
@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val pageable: PageableInfo,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)

@Serializable
data class PageableInfo(
    val sort: SortInfo,
    val pageNumber: Int,
    val pageSize: Int
)

@Serializable
data class SortInfo(
    val sorted: Boolean,
    val unsorted: Boolean
)

// Stock Update DTOs
@Serializable
data class StockUpdateRequest(
    val stockQuantity: Int
)

@Serializable
data class StockAdjustmentRequest(
    val quantity: Int
)

// Supplier DTOs
@Serializable
data class SupplierDTO(
    val id: Long? = null,
    val name: String,
    val contactPerson: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val taxNumber: String? = null,
    val paymentTerms: String? = null, // NET_30, NET_15, etc.
    val deliveryTerms: String? = null, // FOB_DESTINATION, FOB_ORIGIN, etc.
    val rating: Double? = null,
    val status: String? = null, // ACTIVE, INACTIVE, SUSPENDED
    val totalOrders: Int? = null,
    val totalAmount: Double? = null,
    val lastOrderDate: String? = null, // ISO datetime
    val notes: String? = null,
    val createdAt: String? = null, // ISO datetime
    val updatedAt: String? = null // ISO datetime
)

@Serializable
data class SupplierAnalyticsDTO(
    val supplierId: Long,
    val supplierName: String,
    val totalOrders: Int,
    val totalAmount: Double,
    val averageOrderValue: Double,
    val lastOrderDate: String? = null, // ISO datetime
    val rating: Double,
    val onTimeDeliveryRate: Double,
    val qualityRating: Double,
    val monthlyOrderTrends: List<MonthlyOrderTrendDTO>
)

@Serializable
data class MonthlyOrderTrendDTO(
    val month: String, // YYYY-MM format
    val orderCount: Int,
    val totalAmount: Double
)

// Return DTOs
@Serializable
data class ReturnDTO(
    val id: Long? = null,
    val returnNumber: String? = null,
    val originalSaleId: Long,
    val originalSaleNumber: String? = null,
    val customerId: Long,
    val customerName: String? = null,
    val returnDate: String? = null, // ISO datetime
    val reason: String, // DEFECTIVE, WRONG_ITEM, CUSTOMER_CHANGE_MIND, etc.
    val status: String? = null, // PENDING, APPROVED, REJECTED, REFUNDED, EXCHANGED
    val totalRefundAmount: Double,
    val notes: String? = null,
    val processedBy: String? = null,
    val processedDate: String? = null, // ISO datetime
    val refundMethod: String? = null, // ORIGINAL_PAYMENT, STORE_CREDIT, CASH
    val items: List<ReturnItemDTO>? = null,
    val createdAt: String? = null, // ISO datetime
    val updatedAt: String? = null // ISO datetime
)

@Serializable
data class ReturnItemDTO(
    val id: Long? = null,
    val returnId: Long? = null,
    val originalSaleItemId: Long,
    val productId: Long,
    val productName: String? = null,
    val productSku: String? = null,
    val returnQuantity: Int,
    val originalUnitPrice: Double,
    val refundAmount: Double,
    val restockingFee: Double? = null,
    val conditionNotes: String? = null,
    val itemCondition: String, // NEW, LIKE_NEW, GOOD, FAIR, POOR, DAMAGED, DEFECTIVE
    val serialNumbers: String? = null,
    val isRestockable: Boolean? = null,
    val disposalReason: String? = null
)

// Promotion DTOs
@Serializable
data class PromotionDTO(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val type: String, // PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y
    val discountValue: Double,
    val minimumOrderAmount: Double? = null,
    val maximumDiscountAmount: Double? = null,
    val startDate: String, // ISO datetime
    val endDate: String, // ISO datetime
    val isActive: Boolean,
    val applicableProducts: List<Long>? = null, // Product IDs
    val applicableCategories: List<String>? = null,
    val usageLimit: Int? = null,
    val usageCount: Int? = null,
    val customerEligibility: String? = null, // ALL, VIP_ONLY, NEW_CUSTOMERS
    val couponCode: String,
    val autoApply: Boolean? = null,
    val stackable: Boolean? = null,
    val statusDisplay: String? = null,
    val typeDisplay: String? = null,
    val eligibilityDisplay: String? = null,
    val isCurrentlyActive: Boolean? = null,
    val isExpired: Boolean? = null,
    val isNotYetStarted: Boolean? = null,
    val isUsageLimitReached: Boolean? = null,
    val daysUntilExpiry: Int? = null,
    val remainingUsage: Int? = null,
    val usagePercentage: Double? = null,
    val createdAt: String? = null, // ISO datetime
    val updatedAt: String? = null // ISO datetime
)

// Applied Promotion DTO - tracks individual promotion applications to sales
@Serializable
data class AppliedPromotionDTO(
    val id: Long? = null,
    val saleId: Long? = null,
    val promotionId: Long,
    val promotionName: String,
    val promotionType: String, // PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y, FREE_SHIPPING
    val couponCode: String? = null,
    val discountAmount: Double,
    val discountPercentage: Double? = null,
    val originalAmount: Double? = null,
    val finalAmount: Double? = null,
    val isAutoApplied: Boolean? = false,
    val appliedAt: String? = null, // ISO datetime
    val displayText: String? = null, // e.g., "Summer Sale 2024 (10.0% off)"
    val typeDisplay: String? = null, // e.g., "Percentage Discount"
    val savingsAmount: Double? = null, // Same as discountAmount for convenience
    val isPercentageDiscount: Boolean? = null,
    val isFixedAmountDiscount: Boolean? = null
)

// Dashboard and Report DTOs
@Serializable
data class DashboardSummaryDTO(
    val period: String? = null,
    val generatedAt: String? = null,
    val sales: DashboardSalesDTO? = null,
    val customers: DashboardCustomersDTO? = null,
    val inventory: DashboardInventoryDTO? = null,
    val revenue: DashboardRevenueDTO? = null
)

// API Response wrapper for the actual backend structure
@Serializable
data class DashboardApiResponseDTO(
    val success: Boolean? = null,
    val message: String? = null,
    val data: DashboardDataDTO? = null,
    val metadata: DashboardMetadataDTO? = null,
    val errorCode: String? = null,
    val errorDetails: String? = null
)

@Serializable
data class DashboardDataDTO(
    val summary: DashboardSummaryDataDTO? = null,
    val salesOverview: DashboardSalesOverviewDTO? = null,
    val topProducts: DashboardTopProductsDTO? = null,
    val quickStats: DashboardQuickStatsDTO? = null,
    val recentSales: DashboardRecentSalesDTO? = null
)

@Serializable
data class DashboardSalesOverviewDTO(
    // Add specific fields as needed, for now empty since the API returns {}
    val placeholder: String? = null
)

@Serializable
data class DashboardSummaryDataDTO(
    val period: DashboardPeriodDTO? = null,
    val averageOrderValue: Double? = null,
    val totalRevenue: Double? = null,
    val totalSales: Int? = null
)

@Serializable
data class DashboardPeriodDTO(
    val endDate: String? = null,
    val startDate: String? = null
)

@Serializable
data class DashboardQuickStatsDTO(
    val totalCustomers: Int? = null,
    val lowStockItems: Int? = null,
    val totalProducts: Int? = null,
    val todaysSales: Int? = null,
    val todaysRevenue: Double? = null
)

@Serializable
data class DashboardTopProductsDTO(
    val topProducts: List<DashboardTopProductDTO>? = null
)

@Serializable
data class DashboardTopProductDTO(
    val revenue: Double? = null,
    val quantitySold: Int? = null,
    val productName: String? = null
)

@Serializable
data class DashboardRecentSalesDTO(
    val count: Int? = null,
    val sales: List<DashboardRecentSaleDTO>? = null
)

@Serializable
data class DashboardRecentSaleDTO(
    val totalAmount: Double? = null,
    val id: Int? = null,
    val saleDate: String? = null,
    val customerName: String? = null
)

@Serializable
data class DashboardMetadataDTO(
    val reportType: String? = null,
    val reportName: String? = null,
    val generatedAt: String? = null,
    val generatedBy: String? = null,
    val period: String? = null,
    val appliedFilters: DashboardAppliedFiltersDTO? = null,
    val pagination: String? = null,
    val totalRecords: Int? = null,
    val executionTimeMs: Int? = null,
    val version: String? = null,
    val fromCache: Boolean? = null,
    val cacheExpiry: String? = null
)

@Serializable
data class DashboardAppliedFiltersDTO(
    val days: Int? = null,
    // Add other filter fields as needed
    val startDate: String? = null,
    val endDate: String? = null,
    val category: String? = null,
    val customer: String? = null
)

@Serializable
data class DashboardSalesDTO(
    val totalSales: Int? = null,
    val totalRevenue: Double? = null,
    val averageOrderValue: Double? = null,
    val growthRate: Double? = null,
    val completedSales: Int? = null,
    val pendingSales: Int? = null,
    val cancelledSales: Int? = null
)

@Serializable
data class DashboardCustomersDTO(
    val totalCustomers: Int? = null,
    val newCustomers: Int? = null,
    val activeCustomers: Int? = null,
    val retentionRate: Double? = null
)

@Serializable
data class DashboardInventoryDTO(
    val totalProducts: Int? = null,
    val lowStockAlerts: Int? = null,
    val outOfStockProducts: Int? = null,
    val totalStockValue: Double? = null,
    val outOfStockAlerts: Int? = null,
    val totalValue: Double? = null
)

@Serializable
data class DashboardRevenueDTO(
    val monthlyRevenue: Map<String, Double>? = null, // Changed to Map for monthly data
    val yearlyRevenue: Double? = null,
    val profitMargin: Double? = null,
    val topCategory: String? = null,
    val thisMonth: Double? = null,
    val lastMonth: Double? = null,
    val growthRate: Double? = null
)

// Error Response DTO
@Serializable
data class ErrorResponseDTO(
    val message: String,
    val timestamp: String,
    val status: Int? = null,
    val error: String? = null,
    val path: String? = null
)

// Inventory Management DTOs
@Serializable
data class InventoryDTO(
    val id: Long,
    val name: String,
    val description: String? = null,
    val location: String,
    val address: String? = null,
    val managerName: String? = null,
    val managerPhone: String? = null,
    val managerEmail: String? = null,
    val capacity: Int? = null,
    val currentStockCount: Int = 0,
    val status: InventoryStatus = InventoryStatus.ACTIVE,
    val warehouseCode: String? = null,
    val isMainWarehouse: Boolean = false,
    val operatingHours: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val notes: String? = null,
    val createdAt: String? = null, // ISO datetime
    val updatedAt: String? = null, // ISO datetime
    val categoryCount: Int = 0,
    val capacityUtilization: Double = 0.0,
    val isNearCapacity: Boolean = false
)

@Serializable
data class InventoryCreateRequest(
    val name: String,
    val description: String? = null,
    val location: String,
    val address: String? = null,
    val managerName: String? = null,
    val managerPhone: String? = null,
    val managerEmail: String? = null,
    val capacity: Int? = null,
    val currentStockCount: Int = 0,
    val warehouseCode: String? = null,
    val isMainWarehouse: Boolean = false,
    val operatingHours: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val notes: String? = null
)

@Serializable
data class InventoryUpdateRequest(
    val name: String,
    val description: String? = null,
    val location: String,
    val address: String? = null,
    val managerName: String? = null,
    val managerPhone: String? = null,
    val managerEmail: String? = null,
    val capacity: Int? = null,
    val currentStockCount: Int = 0,
    val warehouseCode: String? = null,
    val isMainWarehouse: Boolean = false,
    val operatingHours: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val notes: String? = null
)

@Serializable
data class InventoryStatusUpdateRequest(
    val status: InventoryStatus
)

@Serializable
enum class InventoryStatus {
    ACTIVE,
    INACTIVE,
    ARCHIVED,
    MAINTENANCE
}
