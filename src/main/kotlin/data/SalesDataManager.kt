package data

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

class SalesDataManager {
    // بيانات تجريبية للمنتجات
    val products = mutableListOf(
        Product(1, "iPhone 14", "123456789012", 4500.0, 4000.0, 15, "إلكترونيات"),
        Product(2, "Samsung Galaxy S23", "123456789013", 4200.0, 3700.0, 12, "إلكترونيات"),
        Product(3, "لابتوب HP", "123456789014", 3500.0, 3000.0, 8, "إلكترونيات"),
        Product(4, "سماعات AirPods", "123456789015", 750.0, 600.0, 25, "إكسسوارات"),
        Product(5, "شاحن سريع", "123456789016", 150.0, 100.0, 50, "إكسسوارات"),
        Product(6, "كيبورد لاسلكي", "123456789017", 200.0, 150.0, 30, "إكسسوارات"),
        Product(7, "ماوس جيمنج", "123456789018", 180.0, 130.0, 20, "إكسسوارات"),
        Product(8, "شاشة 27 بوصة", "123456789019", 1200.0, 1000.0, 10, "إلكترونيات")
    )

    // بيانات تجريبية للعملاء
    val customers = mutableListOf(
        Customer(1, "أحمد محمد", "0501234567", "ahmed@email.com", "الرياض", 5500.0),
        Customer(2, "فاطمة علي", "0507654321", "fatima@email.com", "جدة", 3200.0),
        Customer(3, "محمد السعيد", "0509876543", "mohammed@email.com", "الدمام", 2800.0),
        Customer(4, "نورا أحمد", "0502468135", "nora@email.com", "مكة", 4100.0),
        Customer(5, "خالد العتيبي", "0508642097", "khalid@email.com", "الرياض", 6700.0)
    )

    // المبيعات التجريبية
    val sales = mutableListOf<Sale>()

    init {
        generateSampleSales()
    }

    private fun generateSampleSales() {
        repeat(20) { index ->
            val customer = if (Random.nextBoolean()) customers.random() else null
            val itemCount = Random.nextInt(1, 4)
            val items = mutableListOf<SaleItem>()

            repeat(itemCount) {
                val product = products.random()
                val quantity = Random.nextInt(1, 5)
                items.add(SaleItem(product, quantity, product.price))
            }

            sales.add(
                Sale(
                    id = index + 1,
                    date = Clock.System.now().minus(kotlin.time.Duration.parse("${Random.nextInt(0, 30)}d")).toLocalDateTime(TimeZone.currentSystemDefault()),
                    customer = customer,
                    items = items,
                    tax = items.sumOf { it.subtotal } * 0.15,
                    paymentMethod = PaymentMethod.values().random()
                )
            )
        }
    }

    // حساب الإحصائيات اليومية
    fun getDailySalesStats(date: LocalDate): DailySalesStats {
        val todaySales = sales.filter {
            it.date.date == date
        }

        val totalSales = todaySales.sumOf { it.total }
        val totalTransactions = todaySales.size
        val allItems = todaySales.flatMap { it.items }
        val topProduct = allItems.groupBy { it.product }
            .maxByOrNull { it.value.sumOf { item -> item.quantity } }?.key
        val totalProfit = allItems.sumOf { (it.product.price - it.product.cost) * it.quantity }
        val totalItemsSold = allItems.sumOf { it.quantity }
        val averageOrderValue = if (totalTransactions > 0) totalSales / totalTransactions else 0.0

        return DailySalesStats(date, totalSales, totalTransactions, topProduct, totalProfit, averageOrderValue, totalItemsSold)
    }

    // حساب إحصائيات المنتجات
    fun getProductStats(): List<ProductStats> {
        return products.map { product ->
            val soldItems = sales.flatMap { it.items }.filter { it.product.id == product.id }
            val totalSold = soldItems.sumOf { it.quantity }
            val revenue = soldItems.sumOf { it.subtotal }
            val profit = soldItems.sumOf { (product.price - product.cost) * it.quantity }

            ProductStats(product, totalSold, revenue, profit)
        }.sortedByDescending { it.revenue }
    }

    // البحث عن المنتجات
    fun searchProducts(query: String): List<Product> {
        return products.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.barcode.contains(query) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    // إضافة منتج جديد
    fun addProduct(product: Product): Boolean {
        return try {
            products.add(product)
            true
        } catch (e: Exception) {
            false
        }
    }

    // تحديث منتج
    fun updateProduct(product: Product): Boolean {
        val index = products.indexOfFirst { it.id == product.id }
        return if (index != -1) {
            products[index] = product
            true
        } else false
    }

    // حذف منتج
    fun deleteProduct(productId: Int): Boolean {
        return products.removeIf { it.id == productId }
    }

    // إضافة عميل جديد
    fun addCustomer(customer: Customer): Boolean {
        return try {
            customers.add(customer)
            true
        } catch (e: Exception) {
            false
        }
    }

    // البحث عن العملاء
    fun searchCustomers(query: String): List<Customer> {
        return customers.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.phone.contains(query) ||
            it.email.contains(query, ignoreCase = true)
        }
    }

    // إنشاء فاتورة جديدة
    fun createSale(items: List<SaleItem>, customer: Customer?, paymentMethod: PaymentMethod): Sale {
        val newId = (sales.maxOfOrNull { it.id } ?: 0) + 1
        val tax = items.sumOf { it.subtotal } * 0.15

        val sale = Sale(
            id = newId,
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            customer = customer,
            items = items,
            tax = tax,
            paymentMethod = paymentMethod
        )

        sales.add(sale)

        // تحديث المخزون
        items.forEach { saleItem ->
            val productIndex = products.indexOfFirst { it.id == saleItem.product.id }
            if (productIndex != -1) {
                val product = products[productIndex]
                products[productIndex] = product.copy(stock = product.stock - saleItem.quantity)
            }
        }

        return sale
    }

    // حساب أفضل المنتجات مبيعاً
    fun getTopSellingProducts(limit: Int = 5): List<ProductStats> {
        return getProductStats().take(limit)
    }

    // حساب إجمالي المبيعات للفترة
    fun getSalesForPeriod(startDate: LocalDate, endDate: LocalDate): List<Sale> {
        return sales.filter { sale ->
            val saleDate = sale.date.date
            saleDate >= startDate && saleDate <= endDate
        }
    }

    // حساب الربح للفترة
    fun getProfitForPeriod(startDate: LocalDate, endDate: LocalDate): Double {
        return getSalesForPeriod(startDate, endDate)
            .flatMap { it.items }
            .sumOf { (it.product.price - it.product.cost) * it.quantity }
    }

    // حساب إحصائيات شهرية
    fun getMonthlyStats(year: Int, month: Int): Map<String, Double> {
        val monthSales = sales.filter {
            it.date.year == year && it.date.monthNumber == month
        }

        return mapOf(
            "totalSales" to monthSales.sumOf { it.total },
            "totalProfit" to monthSales.flatMap { it.items }.sumOf { (it.product.price - it.product.cost) * it.quantity },
            "totalTransactions" to monthSales.size.toDouble(),
            "averageTransaction" to if (monthSales.isNotEmpty()) monthSales.sumOf { it.total } / monthSales.size else 0.0
        )
    }

    // المنتجات منخفضة المخزون
    fun getLowStockProducts(threshold: Int = 10): List<Product> {
        return products.filter { it.stock <= threshold }
    }

    // إجمالي المبيعات اليوم
    fun getTodayTotalSales(): Double {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return sales.filter { it.date.date == today }.sumOf { it.total }
    }

    // عدد المعاملات اليوم
    fun getTodayTransactionsCount(): Int {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return sales.count { it.date.date == today }
    }
}
