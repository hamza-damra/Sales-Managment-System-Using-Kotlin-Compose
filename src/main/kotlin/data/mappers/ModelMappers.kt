package data.mappers

import data.*
import data.api.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime

/**
 * Extension functions to convert between domain models and API DTOs
 */

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
        barcode = this.barcode ?: "",
        price = this.price,
        cost = this.costPrice ?: 0.0,
        stock = this.stockQuantity ?: 0,
        category = this.category ?: ""
    )
}

fun Product.toApiModel(): ProductDTO {
    return ProductDTO(
        id = this.id.toLong(),
        name = this.name,
        barcode = this.barcode,
        price = this.price,
        costPrice = this.cost,
        stockQuantity = this.stock,
        category = this.category
    )
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
        totalSales = this.sales.totalRevenue,
        totalTransactions = this.sales.totalSales,
        topProduct = null, // This would need to be fetched separately
        totalProfit = 0.0, // Not available in dashboard summary
        averageOrderValue = if (this.sales.totalSales > 0) this.sales.totalRevenue / this.sales.totalSales else 0.0,
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
