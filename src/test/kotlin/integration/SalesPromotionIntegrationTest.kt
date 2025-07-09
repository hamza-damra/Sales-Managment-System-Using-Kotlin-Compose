package integration

import data.api.*
import data.api.services.SalesApiService
import data.repository.SalesRepository
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*

/**
 * Integration test for sales-promotion functionality
 */
class SalesPromotionIntegrationTest {
    
    private lateinit var mockHttpClient: HttpClient
    private lateinit var salesApiService: SalesApiService
    private lateinit var salesRepository: SalesRepository
    
    @BeforeEach
    fun setup() {
        mockHttpClient = HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            
            engine {
                addHandler { request ->
                    when {
                        request.url.encodedPath.contains("/api/sales") && request.method == HttpMethod.Post -> {
                            val couponCode = request.url.parameters["couponCode"]
                            
                            // Mock response with promotion applied
                            val saleWithPromotion = createMockSaleWithPromotion(couponCode)
                            respond(
                                content = Json.encodeToString(SaleDTO.serializer(), saleWithPromotion),
                                status = HttpStatusCode.Created,
                                headers = headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                        
                        request.url.encodedPath.contains("/apply-promotion") -> {
                            val saleWithPromotion = createMockSaleWithPromotion("SUMMER20")
                            respond(
                                content = Json.encodeToString(SaleDTO.serializer(), saleWithPromotion),
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                        
                        request.url.encodedPath.contains("/eligible-promotions") -> {
                            val eligiblePromotions = listOf(createMockPromotion())
                            respond(
                                content = Json.encodeToString(kotlinx.serialization.builtins.ListSerializer(PromotionDTO.serializer()), eligiblePromotions),
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                        
                        else -> {
                            respond(
                                content = "{}",
                                status = HttpStatusCode.NotFound,
                                headers = headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }
                    }
                }
            }
        }
        
        salesApiService = SalesApiService(mockHttpClient)
        salesRepository = SalesRepository(salesApiService)
    }
    
    @Test
    fun `test create sale with coupon code applies promotion`() = runTest {
        // Given
        val saleDTO = createMockSaleDTO()
        val couponCode = "SUMMER20"
        
        // When
        val result = salesRepository.createSale(saleDTO, couponCode)
        
        // Then
        assertTrue(result.isSuccess)
        val createdSale = result.getOrNull()
        assertNotNull(createdSale)
        assertEquals(couponCode, createdSale?.couponCode)
        assertTrue(createdSale?.hasPromotions == true)
        assertTrue((createdSale?.totalSavings ?: 0.0) > 0.0)
        assertNotNull(createdSale?.appliedPromotions)
        assertTrue(createdSale?.appliedPromotions?.isNotEmpty() == true)
    }
    
    @Test
    fun `test apply promotion to existing sale`() = runTest {
        // Given
        val saleId = 1L
        val couponCode = "SUMMER20"
        
        // When
        val result = salesRepository.applyPromotionToSale(saleId, couponCode)
        
        // Then
        assertTrue(result.isSuccess)
        val updatedSale = result.getOrNull()
        assertNotNull(updatedSale)
        assertEquals(couponCode, updatedSale?.couponCode)
        assertTrue(updatedSale?.hasPromotions == true)
    }
    
    @Test
    fun `test get eligible promotions for sale`() = runTest {
        // Given
        val saleId = 1L
        
        // When
        val result = salesRepository.getEligiblePromotionsForSale(saleId)
        
        // Then
        assertTrue(result.isSuccess)
        val promotions = result.getOrNull()
        assertNotNull(promotions)
        assertTrue(promotions?.isNotEmpty() == true)
    }
    
    private fun createMockSaleDTO(): SaleDTO {
        return SaleDTO(
            customerId = 1L,
            customerName = "Test Customer",
            totalAmount = 200.0,
            items = listOf(
                SaleItemDTO(
                    productId = 1L,
                    productName = "Test Product",
                    quantity = 2,
                    unitPrice = 100.0,
                    totalPrice = 200.0
                )
            ),
            subtotal = 200.0,
            status = "PENDING"
        )
    }
    
    private fun createMockSaleWithPromotion(couponCode: String?): SaleDTO {
        val appliedPromotion = if (couponCode != null) {
            AppliedPromotionDTO(
                id = 1L,
                promotionId = 1L,
                promotionName = "Summer Sale 2024",
                promotionType = "PERCENTAGE",
                couponCode = couponCode,
                discountAmount = 20.0,
                discountPercentage = 10.0,
                originalAmount = 200.0,
                finalAmount = 180.0,
                isAutoApplied = false,
                displayText = "Summer Sale 2024 (10.0% off)",
                typeDisplay = "Percentage Discount"
            )
        } else null
        
        return SaleDTO(
            id = 1L,
            customerId = 1L,
            customerName = "Test Customer",
            totalAmount = if (appliedPromotion != null) 180.0 else 200.0,
            items = listOf(
                SaleItemDTO(
                    productId = 1L,
                    productName = "Test Product",
                    quantity = 2,
                    unitPrice = 100.0,
                    totalPrice = 200.0
                )
            ),
            subtotal = 200.0,
            status = "PENDING",
            couponCode = couponCode,
            originalTotal = 200.0,
            finalTotal = if (appliedPromotion != null) 180.0 else 200.0,
            promotionDiscountAmount = appliedPromotion?.discountAmount ?: 0.0,
            appliedPromotions = if (appliedPromotion != null) listOf(appliedPromotion) else emptyList(),
            totalSavings = appliedPromotion?.discountAmount ?: 0.0,
            hasPromotions = appliedPromotion != null,
            promotionCount = if (appliedPromotion != null) 1 else 0
        )
    }
    
    private fun createMockPromotion(): PromotionDTO {
        return PromotionDTO(
            id = 1L,
            name = "Summer Sale 2024",
            description = "20% off all summer items",
            type = "PERCENTAGE",
            discountValue = 20.0,
            startDate = "2024-06-01T00:00:00",
            endDate = "2024-08-31T23:59:59",
            isActive = true,
            couponCode = "SUMMER20",
            isCurrentlyActive = true
        )
    }
}
