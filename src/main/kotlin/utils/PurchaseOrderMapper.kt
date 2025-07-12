package utils

import data.api.PurchaseOrderDTO
import data.api.PurchaseOrderItemDTO
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for purchase order data mapping and formatting
 */
object PurchaseOrderMapper {

    // Status display names in Arabic
    fun getStatusDisplayName(status: String?): String {
        return when (status?.uppercase()) {
            "PENDING" -> "في انتظار الموافقة"
            "APPROVED" -> "تمت الموافقة"
            "SENT" -> "تم الإرسال للمورد"
            "DELIVERED" -> "تم التسليم"
            "CANCELLED" -> "ملغي"
            else -> status ?: "غير محدد"
        }
    }

    // Priority display names in Arabic
    fun getPriorityDisplayName(priority: String?): String {
        return when (priority?.uppercase()) {
            "LOW" -> "منخفضة"
            "NORMAL" -> "عادية"
            "HIGH" -> "عالية"
            "URGENT" -> "عاجلة"
            else -> priority ?: "عادية"
        }
    }

    // Status color mapping
    fun getStatusColor(status: String?): String {
        return when (status?.uppercase()) {
            "PENDING" -> "#FF9800" // Orange
            "APPROVED" -> "#2196F3" // Blue
            "SENT" -> "#9C27B0" // Purple
            "DELIVERED" -> "#4CAF50" // Green
            "CANCELLED" -> "#F44336" // Red
            else -> "#757575" // Gray
        }
    }

    // Priority color mapping
    fun getPriorityColor(priority: String?): String {
        return when (priority?.uppercase()) {
            "LOW" -> "#4CAF50" // Green
            "NORMAL" -> "#2196F3" // Blue
            "HIGH" -> "#FF9800" // Orange
            "URGENT" -> "#F44336" // Red
            else -> "#757575" // Gray
        }
    }

    // Format currency amounts
    fun formatAmount(amount: Double?): String {
        return if (amount != null) {
            String.format("%,.2f ر.س", amount)
        } else {
            "0.00 ر.س"
        }
    }

    // Format dates
    fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "غير محدد"
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    // Format date and time
    fun formatDateTime(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "غير محدد"
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    // Format receiving progress
    fun formatReceivingProgress(progress: Double?): String {
        return if (progress != null) {
            "${String.format("%.1f", progress)}%"
        } else {
            "0.0%"
        }
    }

    // Get receipt status display
    fun getReceiptStatusDisplay(order: PurchaseOrderDTO): String {
        return when {
            order.isFullyReceived == true -> "مكتمل"
            (order.receivingProgress ?: 0.0) > 0.0 -> "جزئي"
            else -> "لم يتم الاستلام"
        }
    }

    // Get receipt status color
    fun getReceiptStatusColor(order: PurchaseOrderDTO): String {
        return when {
            order.isFullyReceived == true -> "#4CAF50" // Green
            (order.receivingProgress ?: 0.0) > 0.0 -> "#FF9800" // Orange
            else -> "#757575" // Gray
        }
    }

    // Calculate order summary
    fun getOrderSummary(order: PurchaseOrderDTO): String {
        val itemsCount = order.itemsCount ?: 0
        val totalAmount = formatAmount(order.totalAmount)
        return "$itemsCount عنصر - $totalAmount"
    }

    // Get available status transitions
    fun getAvailableStatusTransitions(currentStatus: String?): List<String> {
        return when (currentStatus?.uppercase()) {
            "PENDING" -> listOf("APPROVED", "CANCELLED")
            "APPROVED" -> listOf("SENT", "CANCELLED")
            "SENT" -> listOf("DELIVERED", "CANCELLED")
            "DELIVERED" -> listOf("CANCELLED")
            "CANCELLED" -> emptyList()
            else -> emptyList()
        }
    }

    // Check if order can be modified
    fun canModifyOrder(status: String?): Boolean {
        return status?.uppercase() in listOf("PENDING", "APPROVED")
    }

    // Check if order can be deleted
    fun canDeleteOrder(status: String?): Boolean {
        return status?.uppercase() == "PENDING"
    }

    // Check if order can be approved
    fun canApproveOrder(status: String?): Boolean {
        return status?.uppercase() == "PENDING"
    }

    // Check if order can be sent
    fun canSendOrder(status: String?): Boolean {
        return status?.uppercase() == "APPROVED"
    }

    // Check if order can receive items
    fun canReceiveItems(status: String?): Boolean {
        return status?.uppercase() == "SENT"
    }

    // Validate purchase order data
    fun validatePurchaseOrderData(
        supplierId: Long?,
        shippingAddress: String,
        items: List<PurchaseOrderItemDTO>
    ): List<String> {
        val errors = mutableListOf<String>()

        if (supplierId == null || supplierId <= 0) {
            errors.add("يجب اختيار مورد صحيح")
        }

        if (shippingAddress.isBlank()) {
            errors.add("عنوان الشحن مطلوب")
        }

        if (items.isEmpty()) {
            errors.add("يجب إضافة عنصر واحد على الأقل")
        }

        items.forEachIndexed { index, item ->
            if (item.productId <= 0) {
                errors.add("المنتج في العنصر ${index + 1} غير صحيح")
            }
            if (item.quantity <= 0) {
                errors.add("الكمية في العنصر ${index + 1} يجب أن تكون أكبر من صفر")
            }
            if (item.unitPrice <= 0) {
                errors.add("سعر الوحدة في العنصر ${index + 1} يجب أن يكون أكبر من صفر")
            }
        }

        return errors
    }

    // Calculate order totals
    fun calculateOrderTotals(
        items: List<PurchaseOrderItemDTO>,
        taxRate: Double = 15.0,
        shippingCost: Double = 0.0,
        discountAmount: Double = 0.0
    ): Triple<Double, Double, Double> { // subtotal, taxAmount, totalAmount
        val subtotal = items.sumOf { it.totalPrice }
        val taxAmount = subtotal * (taxRate / 100)
        val totalAmount = subtotal + taxAmount + shippingCost - discountAmount
        
        return Triple(
            String.format("%.2f", subtotal).toDouble(),
            String.format("%.2f", taxAmount).toDouble(),
            String.format("%.2f", totalAmount).toDouble()
        )
    }

    // Calculate item total
    fun calculateItemTotal(
        quantity: Int,
        unitPrice: Double,
        discountPercentage: Double = 0.0,
        taxPercentage: Double = 0.0
    ): Double {
        val subtotal = quantity * unitPrice
        val discountAmount = subtotal * (discountPercentage / 100)
        val afterDiscount = subtotal - discountAmount
        val taxAmount = afterDiscount * (taxPercentage / 100)
        return afterDiscount + taxAmount
    }

    // Format order number for display
    fun formatOrderNumber(orderNumber: String?): String {
        return orderNumber ?: "غير محدد"
    }

    // Get order age in days
    fun getOrderAge(orderDate: String?): Int {
        if (orderDate.isNullOrBlank()) return 0
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(orderDate)
            val now = Date()
            val diffInMillis = now.time - (date?.time ?: 0)
            (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }

    // Check if order is overdue
    fun isOrderOverdue(expectedDeliveryDate: String?): Boolean {
        if (expectedDeliveryDate.isNullOrBlank()) return false
        
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(expectedDeliveryDate)
            val now = Date()
            now.after(date)
        } catch (e: Exception) {
            false
        }
    }

    // Get order priority icon
    fun getPriorityIcon(priority: String?): String {
        return when (priority?.uppercase()) {
            "LOW" -> "⬇️"
            "NORMAL" -> "➡️"
            "HIGH" -> "⬆️"
            "URGENT" -> "🔥"
            else -> "➡️"
        }
    }

    // Create sample purchase order for testing
    fun createSamplePurchaseOrder(index: Int): PurchaseOrderDTO {
        val statuses = listOf("PENDING", "APPROVED", "SENT", "DELIVERED", "CANCELLED")
        val priorities = listOf("LOW", "NORMAL", "HIGH", "URGENT")
        
        return PurchaseOrderDTO(
            id = index.toLong(),
            orderNumber = "PO-2024-${String.format("%03d", index)}",
            supplierId = (index % 5) + 1L,
            supplierName = "مورد رقم ${(index % 5) + 1}",
            orderDate = "2024-01-${String.format("%02d", (index % 28) + 1)}T10:30:00",
            expectedDeliveryDate = "2024-02-${String.format("%02d", (index % 28) + 1)}T10:30:00",
            status = statuses[index % statuses.size],
            priority = priorities[index % priorities.size],
            totalAmount = (1000 + index * 500).toDouble(),
            subtotal = (800 + index * 400).toDouble(),
            taxAmount = (120 + index * 60).toDouble(),
            taxRate = 15.0,
            shippingCost = 100.0,
            discountAmount = 0.0,
            shippingAddress = "الرياض، المملكة العربية السعودية",
            notes = "طلب شراء تجريبي رقم $index",
            items = listOf(
                PurchaseOrderItemDTO(
                    id = (index * 10 + 1).toLong(),
                    productId = (index % 10) + 1L,
                    productName = "منتج رقم ${(index % 10) + 1}",
                    productSku = "SKU-${String.format("%03d", (index % 10) + 1)}",
                    quantity = (index % 5) + 1,
                    unitPrice = (50 + index * 10).toDouble(),
                    totalPrice = ((index % 5) + 1) * (50 + index * 10).toDouble(),
                    receivedQuantity = if (index % 3 == 0) (index % 5) + 1 else 0
                )
            ),
            itemsCount = 1,
            isFullyReceived = index % 3 == 0,
            receivingProgress = if (index % 3 == 0) 100.0 else if (index % 2 == 0) 50.0 else 0.0,
            createdBy = "admin@company.com",
            createdAt = "2024-01-${String.format("%02d", (index % 28) + 1)}T10:30:00",
            updatedAt = "2024-01-${String.format("%02d", (index % 28) + 1)}T10:30:00"
        )
    }
}
