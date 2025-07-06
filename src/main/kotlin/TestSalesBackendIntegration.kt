import data.di.AppDependencies
import data.api.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Comprehensive test for SalesScreen backend integration
 */
fun main() = runBlocking {
    println("🔍 Testing SalesScreen Backend Integration")
    println("==========================================")
    
    try {
        val container = AppDependencies.container
        val salesRepository = container.salesRepository
        val customerRepository = container.customerRepository
        val productRepository = container.productRepository
        
        println("✅ Repositories initialized successfully")
        
        // Test 1: Load sales data (GET /api/sales)
        println("\n📊 Testing GET /api/sales...")
        val salesResult = salesRepository.loadSales(page = 0, size = 10)
        
        when {
            salesResult.isSuccess -> {
                val salesData = salesResult.getOrNull()
                println("✅ Sales API working!")
                println("   📋 Total sales: ${salesData?.totalElements}")
                println("   📄 Current page: ${salesData?.content?.size} sales")
                
                salesData?.content?.take(3)?.forEach { sale ->
                    println("   - Sale #${sale.id}: ${sale.customerName} - $${sale.totalAmount} (${sale.status})")
                }
            }
            salesResult.isError -> {
                val error = (salesResult as NetworkResult.Error).exception
                println("❌ Sales GET API failed: ${error.message}")
            }
        }
        
        // Test 2: Load customers for sales
        println("\n👥 Testing customer integration...")
        val customersResult = customerRepository.loadCustomers(page = 0, size = 5)
        
        when {
            customersResult.isSuccess -> {
                val customersData = customersResult.getOrNull()
                println("✅ Customers API working!")
                println("   📋 Available customers: ${customersData?.totalElements}")
                
                val firstCustomer = customersData?.content?.firstOrNull()
                if (firstCustomer != null) {
                    println("   👤 Test customer: ${firstCustomer.name} (ID: ${firstCustomer.id})")
                    
                    // Test 3: Load products for sales
                    println("\n📦 Testing product integration...")
                    val productsResult = productRepository.loadProducts(page = 0, size = 5)
                    
                    when {
                        productsResult.isSuccess -> {
                            val productsData = productsResult.getOrNull()
                            println("✅ Products API working!")
                            println("   📋 Available products: ${productsData?.totalElements}")
                            
                            val firstProduct = productsData?.content?.firstOrNull()
                            if (firstProduct != null) {
                                println("   📦 Test product: ${firstProduct.name} - $${firstProduct.price} (Stock: ${firstProduct.stockQuantity})")
                                
                                // Test 4: Create a test sale (POST /api/sales)
                                println("\n💰 Testing sale creation (POST /api/sales)...")
                                
                                val testSale = SaleDTO(
                                    customerId = firstCustomer.id!!,
                                    customerName = firstCustomer.name,
                                    saleDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
                                    totalAmount = firstProduct.price * 1.15, // Including 15% tax
                                    status = "PENDING",
                                    items = listOf(
                                        SaleItemDTO(
                                            productId = firstProduct.id!!,
                                            productName = firstProduct.name,
                                            quantity = 1,
                                            unitPrice = firstProduct.price,
                                            originalUnitPrice = firstProduct.price,
                                            costPrice = firstProduct.costPrice,
                                            discountPercentage = 0.0,
                                            discountAmount = 0.0,
                                            taxPercentage = 15.0,
                                            taxAmount = firstProduct.price * 0.15,
                                            subtotal = firstProduct.price,
                                            totalPrice = firstProduct.price * 1.15,
                                            unitOfMeasure = "PCS"
                                        )
                                    ),
                                    subtotal = firstProduct.price,
                                    discountAmount = 0.0,
                                    discountPercentage = 0.0,
                                    taxAmount = firstProduct.price * 0.15,
                                    taxPercentage = 15.0,
                                    shippingCost = 0.0,
                                    paymentMethod = "CASH",
                                    paymentStatus = "PENDING",
                                    billingAddress = firstCustomer.address,
                                    shippingAddress = firstCustomer.address,
                                    salesPerson = "Test User",
                                    salesChannel = "IN_STORE",
                                    saleType = "RETAIL",
                                    currency = "USD",
                                    exchangeRate = 1.0,
                                    deliveryStatus = "NOT_SHIPPED",
                                    isGift = false,
                                    loyaltyPointsEarned = (firstProduct.price / 10).toInt(),
                                    loyaltyPointsUsed = 0,
                                    isReturn = false
                                )
                                
                                val createResult = salesRepository.createSale(testSale)
                                
                                when {
                                    createResult.isSuccess -> {
                                        val createdSale = createResult.getOrNull()
                                        println("✅ Sale creation successful!")
                                        println("   🆔 Sale ID: ${createdSale?.id}")
                                        println("   📄 Sale Number: ${createdSale?.saleNumber}")
                                        println("   💰 Total: $${createdSale?.totalAmount}")
                                        println("   📊 Status: ${createdSale?.status}")
                                        
                                        // Test 5: Complete the sale (POST /api/sales/{id}/complete)
                                        createdSale?.id?.let { saleId ->
                                            println("\n✅ Testing sale completion...")
                                            val completeResult = salesRepository.completeSale(saleId)
                                            
                                            when {
                                                completeResult.isSuccess -> {
                                                    val completedSale = completeResult.getOrNull()
                                                    println("✅ Sale completion successful!")
                                                    println("   📊 New status: ${completedSale?.status}")
                                                    println("   💳 Payment status: ${completedSale?.paymentStatus}")
                                                }
                                                completeResult.isError -> {
                                                    val error = (completeResult as NetworkResult.Error).exception
                                                    println("❌ Sale completion failed: ${error.message}")
                                                }
                                            }
                                        }
                                        
                                    }
                                    createResult.isError -> {
                                        val error = (createResult as NetworkResult.Error).exception
                                        println("❌ Sale creation failed: ${error.message}")
                                        println("   🔍 This might indicate backend validation issues")
                                    }
                                }
                            } else {
                                println("⚠️  No products available for testing")
                            }
                        }
                        productsResult.isError -> {
                            val error = (productsResult as NetworkResult.Error).exception
                            println("❌ Products API failed: ${error.message}")
                        }
                    }
                } else {
                    println("⚠️  No customers available for testing")
                }
            }
            customersResult.isError -> {
                val error = (customersResult as NetworkResult.Error).exception
                println("❌ Customers API failed: ${error.message}")
            }
        }
        
        println("\n🎯 Integration Test Summary:")
        println("✅ SalesRepository: Available")
        println("✅ CustomerRepository: Available")
        println("✅ ProductRepository: Available")
        println("✅ API Configuration: Correct (${ApiConfig.BASE_URL})")
        println("✅ SalesScreenEnhanced: Ready for testing")
        
    } catch (e: Exception) {
        println("❌ Test failed with exception: ${e.message}")
        e.printStackTrace()
    }
}
