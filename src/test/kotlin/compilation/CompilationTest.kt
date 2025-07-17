package compilation

import data.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Simple compilation test to verify all classes compile correctly
 */
class CompilationTest {
    
    @Test
    fun `test SaleDTO with promotion fields compiles`() {
        // Test that SaleDTO with new promotion fields compiles correctly
        val appliedPromotion = AppliedPromotionDTO(
            id = 1L,
            promotionId = 1L,
            promotionName = "Test Promotion",
            promotionType = "PERCENTAGE",
            couponCode = "TEST20",
            discountAmount = 20.0,
            discountPercentage = 10.0,
            originalAmount = 200.0,
            finalAmount = 180.0,
            isAutoApplied = false
        )
        
        val saleDTO = SaleDTO(
            id = 1L,
            customerId = 1L,
            customerName = "Test Customer",
            totalAmount = 180.0,
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
            // New promotion fields
            promotionId = 1L,
            couponCode = "TEST20",
            originalTotal = 200.0,
            finalTotal = 180.0,
            promotionDiscountAmount = 20.0,
            appliedPromotions = listOf(appliedPromotion),
            totalSavings = 20.0,
            hasPromotions = true,
            promotionCount = 1
        )
        
        // Verify the object was created successfully
        assertNotNull(saleDTO)
        assertEquals("TEST20", saleDTO.couponCode)
        assertEquals(200.0, saleDTO.originalTotal)
        assertEquals(180.0, saleDTO.finalTotal)
        assertEquals(20.0, saleDTO.promotionDiscountAmount)
        assertEquals(true, saleDTO.hasPromotions)
        assertEquals(1, saleDTO.promotionCount)
        assertNotNull(saleDTO.appliedPromotions)
        assertEquals(1, saleDTO.appliedPromotions?.size)
    }
    
    @Test
    fun `test AppliedPromotionDTO compiles`() {
        // Test that AppliedPromotionDTO compiles correctly
        val appliedPromotion = AppliedPromotionDTO(
            id = 1L,
            saleId = 1L,
            promotionId = 1L,
            promotionName = "Summer Sale",
            promotionType = "PERCENTAGE",
            couponCode = "SUMMER20",
            discountAmount = 25.0,
            discountPercentage = 12.5,
            originalAmount = 200.0,
            finalAmount = 175.0,
            isAutoApplied = false,
            appliedAt = "2024-07-09T10:30:00",
            displayText = "Summer Sale (12.5% off)",
            typeDisplay = "Percentage Discount",
            savingsAmount = 25.0,
            isPercentageDiscount = true,
            isFixedAmountDiscount = false
        )
        
        // Verify the object was created successfully
        assertNotNull(appliedPromotion)
        assertEquals("Summer Sale", appliedPromotion.promotionName)
        assertEquals("PERCENTAGE", appliedPromotion.promotionType)
        assertEquals("SUMMER20", appliedPromotion.couponCode)
        assertEquals(25.0, appliedPromotion.discountAmount)
        assertEquals(12.5, appliedPromotion.discountPercentage)
        assertEquals(200.0, appliedPromotion.originalAmount)
        assertEquals(175.0, appliedPromotion.finalAmount)
        assertEquals(false, appliedPromotion.isAutoApplied)
        assertEquals(true, appliedPromotion.isPercentageDiscount)
        assertEquals(false, appliedPromotion.isFixedAmountDiscount)
    }
    
    @Test
    fun `test PromotionDTO compiles`() {
        // Test that PromotionDTO compiles correctly
        val promotionDTO = PromotionDTO(
            id = 1L,
            name = "Black Friday Sale",
            description = "Huge discounts on all items",
            type = "PERCENTAGE",
            discountValue = 25.0,
            minimumOrderAmount = 100.0,
            startDate = "2024-11-24T00:00:00",
            endDate = "2024-11-30T23:59:59",
            isActive = true,
            applicableProducts = listOf(1L, 2L, 3L),
            applicableCategories = listOf("ELECTRONICS", "CLOTHING"),
            usageLimit = 1000,
            usageCount = 150,
            customerEligibility = "ALL",
            couponCode = "BLACKFRIDAY25",
            autoApply = false
        )
        
        // Verify the object was created successfully
        assertNotNull(promotionDTO)
        assertEquals("Black Friday Sale", promotionDTO.name)
        assertEquals("PERCENTAGE", promotionDTO.type)
        assertEquals(25.0, promotionDTO.discountValue)
        assertEquals(100.0, promotionDTO.minimumOrderAmount)
        assertEquals("BLACKFRIDAY25", promotionDTO.couponCode)
        assertEquals(true, promotionDTO.isActive)
        assertEquals(false, promotionDTO.autoApply)
        assertNotNull(promotionDTO.applicableProducts)
        assertEquals(3, promotionDTO.applicableProducts?.size)
        assertNotNull(promotionDTO.applicableCategories)
        assertEquals(2, promotionDTO.applicableCategories?.size)
    }
}
