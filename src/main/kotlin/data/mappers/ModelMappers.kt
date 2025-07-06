package data.mappers

import data.*
import data.api.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime

/**
 * Extension functions to convert between domain models and API DTOs
 */

// Category mappings
fun CategoryDTO.toDomainModel(): Category {
    return Category(
        id = this.id ?: 0L,
        name = this.name,
        description = this.description,
        displayOrder = this.displayOrder ?: 0,
        status = when (this.status) {
            "ACTIVE" -> CategoryStatus.ACTIVE
            "INACTIVE" -> CategoryStatus.INACTIVE
            "ARCHIVED" -> CategoryStatus.ARCHIVED
            else -> CategoryStatus.ACTIVE
        },
        imageUrl = this.imageUrl,
        icon = this.icon,
        colorCode = this.colorCode,
        createdAt = this.createdAt?.let {
            try { it.toLocalDateTime() } catch (e: Exception) { null }
        },
        updatedAt = this.updatedAt?.let {
            try { it.toLocalDateTime() } catch (e: Exception) { null }
        },
        productCount = this.productCount ?: 0
    )
}

fun Category.toApiModel(): CategoryDTO {
    return CategoryDTO(
        id = this.id,
        name = this.name,
        description = this.description,
        displayOrder = this.displayOrder,
        status = this.status.name,
        imageUrl = this.imageUrl,
        icon = this.icon,
        colorCode = this.colorCode,
        createdAt = this.createdAt?.toString(),
        updatedAt = this.updatedAt?.toString(),
        productCount = this.productCount
    )
}

// Customer mappings
fun CustomerDTO.toDomainModel(): Customer {
    return Customer(
        id = this.id?.toInt() ?: 0,
        name = this.name,
        phone = this.phone ?: "",
        email = this.email ?: "",
        address = this.address ?: "",
        totalPurchases = this.totalPurchases ?: 0.0
    )
}

fun Customer.toApiModel(): CustomerDTO {
    return CustomerDTO(
        id = this.id.toLong(),
        name = this.name,
        phone = this.phone,
        email = this.email,
        address = this.address,
        totalPurchases = this.totalPurchases
    )
}

// Product mappings
fun ProductDTO.toDomainModel(): Product {
    return Product(
        id = this.id?.toInt() ?: 0,
        name = this.name,
        description = this.description,
        price = this.price,
        cost = this.costPrice ?: 0.0,
        stock = this.stockQuantity ?: 0,
        category = this.category ?: this.categoryName ?: "",
        categoryId = this.categoryId,
        categoryName = this.categoryName,
        sku = this.sku,
        brand = this.brand,
        modelNumber = this.modelNumber,
        barcode = this.barcode,
        weight = this.weight,
        length = this.length,
        width = this.width,
        height = this.height,
        productStatus = this.productStatus,
        minStockLevel = this.minStockLevel,
        maxStockLevel = this.maxStockLevel,
        reorderPoint = this.reorderPoint,
        reorderQuantity = this.reorderQuantity,
        supplierName = this.supplierName,
        supplierCode = this.supplierCode,
        warrantyPeriod = this.warrantyPeriod,
        expiryDate = this.expiryDate,
        manufacturingDate = this.manufacturingDate,
        tags = this.tags,
        imageUrl = this.imageUrl,
        additionalImages = this.additionalImages,
        isSerialized = this.isSerialized,
        isDigital = this.isDigital,
        isTaxable = this.isTaxable,
        taxRate = this.taxRate,
        unitOfMeasure = this.unitOfMeasure,
        discountPercentage = this.discountPercentage,
        locationInWarehouse = this.locationInWarehouse,
        totalSold = this.totalSold,
        totalRevenue = this.totalRevenue,
        lastSoldDate = this.lastSoldDate,
        lastRestockedDate = this.lastRestockedDate,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lowStock = calculateLowStock(),
        expired = calculateExpired(),
        profitMargin = calculateProfitMargin(),
        outOfStock = calculateOutOfStock(),
        discountedPrice = calculateDiscountedPrice()
    )
}

fun Product.toApiModel(): ProductDTO {
    return ProductDTO(
        id = this.id.toLong(),
        name = this.name,
        description = this.description,
        price = this.price,
        costPrice = this.cost,
        stockQuantity = this.stock,
        category = this.category,
        categoryId = this.categoryId,
        categoryName = this.categoryName,
        sku = this.sku,
        brand = this.brand,
        modelNumber = this.modelNumber,
        barcode = this.barcode,
        weight = this.weight,
        length = this.length,
        width = this.width,
        height = this.height,
        productStatus = this.productStatus,
        minStockLevel = this.minStockLevel,
        maxStockLevel = this.maxStockLevel,
        reorderPoint = this.reorderPoint,
        reorderQuantity = this.reorderQuantity,
        supplierName = this.supplierName,
        supplierCode = this.supplierCode,
        warrantyPeriod = this.warrantyPeriod,
        expiryDate = this.expiryDate,
        manufacturingDate = this.manufacturingDate,
        tags = this.tags,
        imageUrl = this.imageUrl,
        additionalImages = this.additionalImages,
        isSerialized = this.isSerialized,
        isDigital = this.isDigital,
        isTaxable = this.isTaxable,
        taxRate = this.taxRate,
        unitOfMeasure = this.unitOfMeasure,
        discountPercentage = this.discountPercentage,
        locationInWarehouse = this.locationInWarehouse,
        totalSold = this.totalSold,
        totalRevenue = this.totalRevenue,
        lastSoldDate = this.lastSoldDate,
        lastRestockedDate = this.lastRestockedDate,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
        // Note: lowStock, expired, profitMargin, outOfStock, discountedPrice are calculated fields
        // and not sent to the API
    )
}

// Extension functions for calculated fields
private fun ProductDTO.calculateLowStock(): Boolean {
    val currentStock = this.stockQuantity ?: 0
    val minStock = this.minStockLevel ?: 5
    return currentStock <= minStock
}

private fun ProductDTO.calculateOutOfStock(): Boolean {
    return (this.stockQuantity ?: 0) <= 0
}

private fun ProductDTO.calculateExpired(): Boolean {
    // Check if product has expiry date and if it's past current date
    return this.expiryDate?.let { expiryDateStr ->
        try {
            // Assuming ISO date format (YYYY-MM-DD)
            val expiryDate = kotlinx.datetime.LocalDate.parse(expiryDateStr)
            val currentInstant = kotlinx.datetime.Clock.System.now()
            val currentDate = currentInstant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
            expiryDate < currentDate
        } catch (e: Exception) {
            false // If date parsing fails, assume not expired
        }
    } ?: false
}

private fun ProductDTO.calculateProfitMargin(): Double? {
    val price = this.price
    val cost = this.costPrice ?: return null

    return if (price > 0) {
        ((price - cost) / price) * 100
    } else null
}

private fun ProductDTO.calculateDiscountedPrice(): Double? {
    val discountPercentage = this.discountPercentage ?: return null
    return if (discountPercentage > 0) {
        this.price * (1 - discountPercentage / 100)
    } else null
}

// Sale mappings
fun SaleDTO.toDomainModel(customers: List<Customer>, products: List<Product>): Sale {
    val customer = customers.find { it.id.toLong() == this.customerId }
    val saleItems = this.items.map { itemDto ->
        val product = products.find { it.id.toLong() == itemDto.productId }
        SaleItem(
            product = product ?: Product(0, "Unknown", "", 0.0, 0.0, 0, ""),
            quantity = itemDto.quantity,
            unitPrice = itemDto.unitPrice
        )
    }
    
    return Sale(
        id = this.id?.toInt() ?: 0,
        date = this.saleDate?.let { 
            try {
                it.toLocalDateTime()
            } catch (e: Exception) {
                kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            }
        } ?: kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()),
        customer = customer,
        items = saleItems,
        tax = this.taxAmount ?: 0.0,
        paymentMethod = when (this.paymentMethod) {
            "CASH" -> PaymentMethod.CASH
            "CREDIT_CARD" -> PaymentMethod.CARD
            "BANK_TRANSFER" -> PaymentMethod.BANK_TRANSFER
            "DIGITAL_WALLET" -> PaymentMethod.DIGITAL_WALLET
            else -> PaymentMethod.CASH
        }
    )
}

fun Sale.toApiModel(): SaleDTO {
    val saleItems = this.items.map { item ->
        SaleItemDTO(
            productId = item.product.id.toLong(),
            productName = item.product.name,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            totalPrice = item.subtotal
        )
    }
    
    return SaleDTO(
        id = this.id.toLong(),
        customerId = this.customer?.id?.toLong() ?: 0,
        customerName = this.customer?.name,
        saleDate = this.date.toString(),
        totalAmount = this.total,
        subtotal = this.subtotal,
        taxAmount = this.tax,
        items = saleItems,
        paymentMethod = when (this.paymentMethod) {
            PaymentMethod.CASH -> "CASH"
            PaymentMethod.CARD -> "CREDIT_CARD"
            PaymentMethod.BANK_TRANSFER -> "BANK_TRANSFER"
            PaymentMethod.DIGITAL_WALLET -> "DIGITAL_WALLET"
        },
        status = "COMPLETED"
    )
}

// Dashboard summary mappings
fun DashboardSummaryDTO.toDailySalesStats(): DailySalesStats {
    val today = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date

    return DailySalesStats(
        date = today,
        totalSales = this.sales?.totalRevenue ?: 0.0,
        totalTransactions = this.sales?.totalSales ?: 0,
        topProduct = null, // This would need to be fetched separately
        totalProfit = 0.0, // Not available in dashboard summary
        averageOrderValue = if ((this.sales?.totalSales ?: 0) > 0)
            (this.sales?.totalRevenue ?: 0.0) / (this.sales?.totalSales ?: 1) else 0.0,
        totalItemsSold = 0 // Not available in dashboard summary
    )
}

// Helper functions for date conversion
fun String.toLocalDateTimeOrNow(): LocalDateTime {
    return try {
        this.toLocalDateTime()
    } catch (e: Exception) {
        kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    }
}

fun LocalDateTime.toIsoString(): String {
    return this.toString()
}

// Payment method conversion
fun String?.toPaymentMethod(): PaymentMethod {
    return when (this) {
        "CASH" -> PaymentMethod.CASH
        "CREDIT_CARD", "DEBIT_CARD" -> PaymentMethod.CARD
        "BANK_TRANSFER" -> PaymentMethod.BANK_TRANSFER
        "DIGITAL_WALLET" -> PaymentMethod.DIGITAL_WALLET
        else -> PaymentMethod.CASH
    }
}

fun PaymentMethod.toApiString(): String {
    return when (this) {
        PaymentMethod.CASH -> "CASH"
        PaymentMethod.CARD -> "CREDIT_CARD"
        PaymentMethod.BANK_TRANSFER -> "BANK_TRANSFER"
        PaymentMethod.DIGITAL_WALLET -> "DIGITAL_WALLET"
    }
}
