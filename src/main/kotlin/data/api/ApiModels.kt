package data.api

import kotlinx.serialization.Serializable

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
    val items: List<SaleItemDTO>,
    val saleNumber: String? = null,
    val referenceNumber: String? = null,
    val subtotal: Double? = null,
    val discountAmount: Double? = null,
    val discountPercentage: Double? = null,
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
