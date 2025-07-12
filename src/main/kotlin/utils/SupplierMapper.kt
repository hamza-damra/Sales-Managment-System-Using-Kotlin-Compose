package utils

import data.api.SupplierDTO
import data.Supplier

/**
 * Utility functions for mapping between SupplierDTO and Supplier models
 */
object SupplierMapper {
    
    /**
     * Convert SupplierDTO to Supplier (UI model)
     */
    fun SupplierDTO.toSupplier(): Supplier {
        return Supplier(
            id = (this.id ?: 0).toInt(),
            name = this.name,
            contactPerson = this.contactPerson ?: "",
            phone = this.phone ?: "",
            email = this.email ?: "",
            address = this.address ?: "",
            paymentTerms = this.paymentTerms ?: "NET_30",
            deliveryTerms = this.deliveryTerms ?: "FOB_DESTINATION",
            rating = this.rating ?: 0.0
        )
    }

    /**
     * Convert Supplier (UI model) to SupplierDTO
     */
    fun Supplier.toSupplierDTO(): SupplierDTO {
        return SupplierDTO(
            id = this.id.toLong(),
            name = this.name,
            contactPerson = this.contactPerson.ifBlank { null },
            phone = this.phone.ifBlank { null },
            email = this.email.ifBlank { null },
            address = this.address.ifBlank { null },
            paymentTerms = this.paymentTerms,
            deliveryTerms = this.deliveryTerms,
            rating = this.rating
        )
    }
    
    /**
     * Convert list of SupplierDTO to list of Supplier
     */
    fun List<SupplierDTO>.toSuppliers(): List<Supplier> {
        return this.map { it.toSupplier() }
    }
    
    /**
     * Convert list of Supplier to list of SupplierDTO
     */
    fun List<Supplier>.toSupplierDTOs(): List<SupplierDTO> {
        return this.map { it.toSupplierDTO() }
    }
    
    /**
     * Get status display name in Arabic
     */
    fun getStatusDisplayName(status: String?): String {
        return when (status) {
            "ACTIVE" -> "نشط"
            "INACTIVE" -> "غير نشط"
            "SUSPENDED" -> "معلق"
            else -> "غير محدد"
        }
    }
    
    /**
     * Get status value from Arabic display name
     */
    fun getStatusValue(displayName: String): String {
        return when (displayName) {
            "نشط" -> "ACTIVE"
            "غير نشط" -> "INACTIVE"
            "معلق" -> "SUSPENDED"
            else -> "ACTIVE"
        }
    }
    
    /**
     * Get payment terms display name in Arabic
     */
    fun getPaymentTermsDisplayName(paymentTerms: String?): String {
        return when (paymentTerms) {
            "NET_30" -> "30 يوم"
            "NET_15" -> "15 يوم"
            "NET_7" -> "7 أيام"
            "COD" -> "الدفع عند الاستلام"
            "PREPAID" -> "دفع مقدم"
            else -> paymentTerms ?: "غير محدد"
        }
    }
    
    /**
     * Get delivery terms display name in Arabic
     */
    fun getDeliveryTermsDisplayName(deliveryTerms: String?): String {
        return when (deliveryTerms) {
            "FOB_DESTINATION" -> "تسليم في الوجهة"
            "FOB_ORIGIN" -> "تسليم من المنشأ"
            "CIF" -> "التكلفة والتأمين والشحن"
            "EXW" -> "تسليم في المصنع"
            else -> deliveryTerms ?: "غير محدد"
        }
    }
    
    /**
     * Format supplier rating for display
     */
    fun formatRating(rating: Double?): String {
        return if (rating != null) {
            String.format("%.1f", rating)
        } else {
            "غير مقيم"
        }
    }
    
    /**
     * Format total amount for display
     */
    fun formatTotalAmount(amount: Double?): String {
        return if (amount != null) {
            String.format("%,.2f ر.س", amount)
        } else {
            "0.00 ر.س"
        }
    }
    
    /**
     * Format total orders for display
     */
    fun formatTotalOrders(orders: Int?): String {
        return if (orders != null) {
            "$orders طلب"
        } else {
            "0 طلب"
        }
    }
    
    /**
     * Get rating color based on value
     */
    fun getRatingColor(rating: Double?): String {
        return when {
            rating == null -> "gray"
            rating >= 4.5 -> "green"
            rating >= 3.5 -> "orange"
            rating >= 2.5 -> "yellow"
            else -> "red"
        }
    }
    
    /**
     * Check if supplier has good rating
     */
    fun hasGoodRating(rating: Double?): Boolean {
        return (rating ?: 0.0) >= 4.0
    }
    
    /**
     * Check if supplier is active
     */
    fun isActive(status: String?): Boolean {
        return status == "ACTIVE"
    }
    
    /**
     * Check if supplier has orders
     */
    fun hasOrders(totalOrders: Int?): Boolean {
        return (totalOrders ?: 0) > 0
    }
    
    /**
     * Get supplier summary text
     */
    fun getSupplierSummary(supplier: SupplierDTO): String {
        val orders = formatTotalOrders(supplier.totalOrders)
        val amount = formatTotalAmount(supplier.totalAmount)
        val rating = formatRating(supplier.rating)
        
        return "الطلبات: $orders | المبلغ: $amount | التقييم: $rating"
    }
    
    /**
     * Validate supplier data
     */
    fun validateSupplierData(
        name: String,
        contactPerson: String,
        phone: String,
        email: String,
        address: String
    ): List<String> {
        val errors = mutableListOf<String>()
        
        if (name.isBlank()) {
            errors.add("اسم المورد مطلوب")
        }
        
        if (contactPerson.isBlank()) {
            errors.add("اسم الشخص المسؤول مطلوب")
        }
        
        if (phone.isBlank()) {
            errors.add("رقم الهاتف مطلوب")
        } else if (!phone.matches(Regex("^[+]?[0-9]{10,15}$"))) {
            errors.add("رقم الهاتف غير صحيح")
        }
        
        if (email.isBlank()) {
            errors.add("البريد الإلكتروني مطلوب")
        } else if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            errors.add("البريد الإلكتروني غير صحيح")
        }
        
        if (address.isBlank()) {
            errors.add("العنوان مطلوب")
        }
        
        return errors
    }
    
    /**
     * Create sample supplier for testing
     */
    fun createSampleSupplier(index: Int): SupplierDTO {
        val cities = listOf("الرياض", "جدة", "الدمام", "مكة", "المدينة المنورة")
        val companies = listOf("شركة التقنية المتقدمة", "مؤسسة الإلكترونيات", "شركة المواد الصناعية", "مجموعة التجارة", "شركة الخدمات")
        val contacts = listOf("أحمد محمد", "فاطمة علي", "محمد عبدالله", "نورا سعد", "خالد أحمد")

        return SupplierDTO(
            id = index.toLong(),
            name = "${companies[index % companies.size]} $index",
            contactPerson = contacts[index % contacts.size],
            phone = "+966-50-123-45${String.format("%02d", index + 10)}",
            email = "supplier$index@${companies[index % companies.size].replace(" ", "").lowercase()}.com",
            address = "شارع الملك فهد، حي النخيل، مبنى رقم ${100 + index}",
            city = cities[index % cities.size],
            country = "المملكة العربية السعودية",
            taxNumber = "TAX${String.format("%09d", 100000000 + index)}",
            paymentTerms = listOf("NET_30", "NET_15", "NET_7", "COD")[index % 4],
            deliveryTerms = listOf("FOB_DESTINATION", "FOB_ORIGIN", "CIF", "EXW")[index % 4],
            rating = 3.0 + (index % 6) * 0.3, // Rating between 3.0 and 4.5
            status = when (index % 4) {
                0 -> "ACTIVE"
                1 -> "ACTIVE"
                2 -> "INACTIVE"
                else -> "SUSPENDED"
            },
            totalOrders = 5 + index * 2,
            totalAmount = (10000 + index * 5000).toDouble(),
            lastOrderDate = "2024-01-${String.format("%02d", (index % 28) + 1)}T10:30:00",
            notes = "مورد ${if (index % 2 == 0) "موثوق ومتميز" else "جديد وواعد"}",
            createdAt = "2024-01-01T10:00:00",
            updatedAt = "2024-01-${String.format("%02d", (index % 28) + 1)}T15:30:00"
        )
    }
}
