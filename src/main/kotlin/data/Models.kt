package data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

// نموذج الفئة
data class Category(
    val id: Long,
    val name: String,
    val description: String? = null,
    val displayOrder: Int = 0,
    val status: CategoryStatus = CategoryStatus.ACTIVE,
    val imageUrl: String? = null,
    val icon: String? = null,
    val colorCode: String? = null,
    val inventoryId: Long? = null, // Associated inventory/warehouse ID
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val productCount: Int = 0
)

// حالة الفئة
enum class CategoryStatus(val displayName: String) {
    ACTIVE("نشط"),
    INACTIVE("غير نشط"),
    ARCHIVED("مؤرشف")
}

// نموذج المنتج
data class Product(
    val id: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val cost: Double,
    val stock: Int,
    val category: String,
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
    val productStatus: String? = null,
    val minStockLevel: Int? = null,
    val maxStockLevel: Int? = null,
    val reorderPoint: Int? = null,
    val reorderQuantity: Int? = null,
    val supplierName: String? = null,
    val supplierCode: String? = null,
    val warrantyPeriod: Int? = null,
    val expiryDate: String? = null,
    val manufacturingDate: String? = null,
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
    val lastSoldDate: String? = null,
    val lastRestockedDate: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lowStock: Boolean? = null,
    val expired: Boolean? = null,
    val profitMargin: Double? = null,
    val outOfStock: Boolean? = null,
    val discountedPrice: Double? = null
)

// نموذج العميل
data class Customer(
    val id: Int,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val totalPurchases: Double
)

// عنصر في الفاتورة
data class SaleItem(
    val product: Product,
    val quantity: Int,
    val unitPrice: Double
) {
    val subtotal: Double get() = quantity * unitPrice
}

// نموذج الفاتورة
data class Sale(
    val id: Int,
    val date: LocalDateTime,
    val customer: Customer?,
    val items: List<SaleItem>,
    val tax: Double,
    val paymentMethod: PaymentMethod
) {
    val subtotal: Double get() = items.sumOf { it.subtotal }
    val total: Double get() = subtotal + tax
}

// طرق الدفع
enum class PaymentMethod(val displayName: String) {
    CASH("نقد"),
    CARD("بطاقة ائتمان"),
    BANK_TRANSFER("تحويل بنكي"),
    DIGITAL_WALLET("محفظة رقمية")
}

// إحصائيات يومية
data class DailySalesStats(
    val date: LocalDate,
    val totalSales: Double,
    val totalTransactions: Int,
    val topProduct: Product?,
    val totalProfit: Double,
    val averageOrderValue: Double,
    val totalItemsSold: Int
)

// إحصائيات المنتجات
data class ProductStats(
    val product: Product,
    val totalSold: Int,
    val revenue: Double,
    val profit: Double
)

// نموذج المورد
data class Supplier(
    val id: Int,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String,
    val paymentTerms: String, // شروط الدفع
    val deliveryTerms: String, // شروط التسليم
    val rating: Double // تقييم الأداء من 1-5
)

// سجل المشتريات من المورد
data class Purchase(
    val id: Int,
    val supplierId: Int,
    val date: LocalDateTime,
    val items: List<PurchaseItem>,
    val totalAmount: Double,
    val status: PurchaseStatus
)

data class PurchaseItem(
    val productId: Int,
    val quantity: Int,
    val unitCost: Double
)

enum class PurchaseStatus(val displayName: String) {
    PENDING("في الانتظار"),
    DELIVERED("تم التسليم"),
    CANCELLED("ملغي")
}

// إدارة المخزون المتقدمة
data class InventoryItem(
    val productId: Int,
    val warehouseId: Int,
    val currentStock: Int,
    val reservedStock: Int, // الكمية المحجوزة
    val minimumStock: Int, // الحد الأدنى للطلب
    val maximumStock: Int,
    val reorderPoint: Int, // نقطة إعادة الطلب
    val lastUpdated: LocalDateTime,
    val expiryDate: LocalDate? // تاريخ الصلاحية
)

data class Warehouse(
    val id: Int,
    val name: String,
    val location: String,
    val manager: String
)

// حركة المخزون
data class StockMovement(
    val id: Int,
    val productId: Int,
    val warehouseId: Int,
    val movementType: MovementType,
    val quantity: Int,
    val date: LocalDateTime,
    val reference: String, // رقم مرجعي (فاتورة، إرجاع، تعديل)
    val notes: String
)

enum class MovementType(val displayName: String) {
    PURCHASE("شراء"),
    SALE("بيع"),
    RETURN("إرجاع"),
    ADJUSTMENT("تعديل"),
    TRANSFER("نقل بين المستودعات"),
    DAMAGED("تالف"),
    EXPIRED("منتهي الصلاحية")
}

// إدارة المرتجعات
data class Return(
    val id: Int,
    val originalSaleId: Int,
    val date: LocalDateTime,
    val items: List<ReturnItem>,
    val reason: ReturnReason,
    val status: ReturnStatus,
    val refundAmount: Double,
    val notes: String
)

data class ReturnItem(
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double,
    val condition: ItemCondition
)

enum class ReturnReason(val displayName: String) {
    DEFECTIVE("معيب"),
    WRONG_ITEM("منتج خاطئ"),
    CUSTOMER_CHANGE_MIND("تغيير رأي العميل"),
    EXPIRED("منتهي الصلاحية"),
    DAMAGED_SHIPPING("تضرر أثناء الشحن"),
    OTHER("أخرى")
}

enum class ReturnStatus(val displayName: String) {
    PENDING("في الانتظار"),
    APPROVED("موافق عليه"),
    REJECTED("مرفوض"),
    REFUNDED("تم الاسترداد"),
    EXCHANGED("تم الاستبدال")
}

enum class ItemCondition(val displayName: String) {
    NEW("جديد"),
    GOOD("حالة جيدة"),
    DAMAGED("تالف"),
    DEFECTIVE("معيب"),
    EXPIRED("منتهي الصلاحية")
}

// إدارة العروض والخصومات
data class Promotion(
    val id: Int,
    val name: String,
    val description: String,
    val type: PromotionType,
    val value: Double, // قيمة الخصم أو النسبة
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val isActive: Boolean,
    val applicableProducts: List<Int>, // معرفات المنتجات المشمولة
    val applicableCategories: List<String>,
    val minimumPurchase: Double = 0.0,
    val maxUsageCount: Int? = null,
    val currentUsageCount: Int = 0
)

enum class PromotionType(val displayName: String) {
    PERCENTAGE_DISCOUNT("خصم نسبة مئوية"),
    FIXED_AMOUNT("خصم مبلغ ثابت"),
    BUY_ONE_GET_ONE("اشترِ واحدًا واحصل على الثاني مجانًا"),
    BUY_X_GET_Y("اشترِ X واحصل على Y"),
    FREE_SHIPPING("شحن مجاني"),
    BUNDLE_DEAL("عرض حزمة")
}

data class Coupon(
    val id: Int,
    val code: String,
    val promotionId: Int,
    val isUsed: Boolean,
    val usedDate: LocalDateTime?,
    val customerId: Int?
)

// تقارير وتحليلات متقدمة
data class SalesReport(
    val period: ReportPeriod,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalSales: Double,
    val totalTransactions: Int,
    val averageOrderValue: Double,
    val salesByPaymentMethod: Map<PaymentMethod, Double>,
    val salesByCategory: Map<String, Double>,
    val salesGrowth: Double, // نسبة النمو مقارنة بالفترة السابقة
    val topProducts: List<ProductStats>,
    val topCustomers: List<CustomerStats>
)

data class InventoryReport(
    val totalInventoryValue: Double,
    val lowStockItems: List<InventoryAlert>,
    val expiringSoonItems: List<InventoryAlert>,
    val slowMovingItems: List<ProductStats>,
    val inventoryTurnoverRate: Double,
    val deadStock: List<Product>
)

data class CustomerReport(
    val totalCustomers: Int,
    val newCustomers: Int,
    val retentionRate: Double,
    val topCustomers: List<CustomerStats>,
    val customerSegmentation: Map<CustomerSegment, Int>,
    val averageCustomerValue: Double
)

data class ProfitLossReport(
    val revenue: Double,
    val costOfGoodsSold: Double,
    val grossProfit: Double,
    val grossProfitMargin: Double,
    val operatingExpenses: Double,
    val netProfit: Double,
    val netProfitMargin: Double,
    val profitByProduct: List<ProductProfitability>,
    val profitByCategory: Map<String, Double>
)

data class CustomerStats(
    val customer: Customer,
    val totalOrders: Int,
    val totalSpent: Double,
    val averageOrderValue: Double,
    val lastOrderDate: LocalDateTime?,
    val loyaltyPoints: Int = 0
)

data class ProductProfitability(
    val product: Product,
    val revenue: Double,
    val cost: Double,
    val profit: Double,
    val profitMargin: Double,
    val unitsSold: Int
)

data class InventoryAlert(
    val productId: Int,
    val productName: String,
    val currentStock: Int,
    val minimumStock: Int,
    val alertType: AlertType,
    val urgency: AlertUrgency,
    val expiryDate: LocalDate? = null
)

enum class AlertType(val displayName: String) {
    LOW_STOCK("مخزون منخفض"),
    OUT_OF_STOCK("نفاد المخزون"),
    EXPIRING_SOON("قارب على الانتهاء"),
    EXPIRED("منتهي الصلاحية"),
    OVERSTOCK("مخزون زائد")
}

enum class AlertUrgency(val displayName: String, val color: androidx.compose.ui.graphics.Color) {
    LOW("منخفض", androidx.compose.ui.graphics.Color.Green),
    MEDIUM("متوسط", androidx.compose.ui.graphics.Color.Yellow),
    HIGH("عالي", androidx.compose.ui.graphics.Color(0xFFF59E0B)), // Warning color
    CRITICAL("حرج", androidx.compose.ui.graphics.Color.Red)
}

enum class ReportPeriod(val displayName: String) {
    DAILY("يومي"),
    WEEKLY("أسبوعي"),
    MONTHLY("شهري"),
    QUARTERLY("ربعي"),
    YEARLY("سنوي"),
    CUSTOM("مخصص")
}

enum class CustomerSegment(val displayName: String) {
    NEW("عملاء جدد"),
    REGULAR("عملاء منتظمين"),
    VIP("عملاء مميزين"),
    INACTIVE("عملاء غير نشطين")
}
