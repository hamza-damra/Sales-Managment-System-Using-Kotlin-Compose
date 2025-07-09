package data.api

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for promotion-related data models
 */
class PromotionDataModelsTest {
    
    @Test
    fun `test SaleDTO with promotion fields`() {
        // Given
        val appliedPromotion = AppliedPromotionDTO(
            id = 1L,
            promotionId = 1L,
            promotionName = "Summer Sale 2024",
            promotionType = "PERCENTAGE",
            couponCode = "SUMMER20",
            discountAmount = 20.0,
            discountPercentage = 10.0,
            originalAmount = 200.0,
            finalAmount = 180.0,
            isAutoApplied = false,
            displayText = "Summer Sale 2024 (10.0% off)",
            typeDisplay = "Percentage Discount"
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
            couponCode = "SUMMER20",
            originalTotal = 200.0,
            finalTotal = 180.0,
            promotionDiscountAmount = 20.0,
            appliedPromotions = listOf(appliedPromotion),
            totalSavings = 20.0,
            hasPromotions = true,
            promotionCount = 1
        )
        
        // Then
        assertEquals(1L, saleDTO.id)
        assertEquals("SUMMER20", saleDTO.couponCode)
        assertEquals(200.0, saleDTO.originalTotal)
        assertEquals(180.0, saleDTO.finalTotal)
        assertEquals(20.0, saleDTO.promotionDiscountAmount)
        assertEquals(20.0, saleDTO.totalSavings)
        assertEquals(true, saleDTO.hasPromotions)
        assertEquals(1, saleDTO.promotionCount)
        assertNotNull(saleDTO.appliedPromotions)
        assertEquals(1, saleDTO.appliedPromotions?.size)
        assertEquals("Summer Sale 2024", saleDTO.appliedPromotions?.first()?.promotionName)
    }
    
    @Test
    fun `test AppliedPromotionDTO creation`() {
        // Given
        val appliedPromotion = AppliedPromotionDTO(
            id = 1L,
            saleId = 1L,
            promotionId = 1L,
            promotionName = "Flash Sale",
            promotionType = "FIXED_AMOUNT",
            couponCode = "FLASH10",
            discountAmount = 15.0,
            originalAmount = 100.0,
            finalAmount = 85.0,
            isAutoApplied = true,
            appliedAt = "2024-07-09T10:30:00",
            displayText = "Flash Sale (15.0 off)",
            typeDisplay = "Fixed Amount Discount",
            savingsAmount = 15.0,
            isPercentageDiscount = false,
            isFixedAmountDiscount = true
        )
        
        // Then
        assertEquals(1L, appliedPromotion.id)
        assertEquals(1L, appliedPromotion.saleId)
        assertEquals(1L, appliedPromotion.promotionId)
        assertEquals("Flash Sale", appliedPromotion.promotionName)
        assertEquals("FIXED_AMOUNT", appliedPromotion.promotionType)
        assertEquals("FLASH10", appliedPromotion.couponCode)
        assertEquals(15.0, appliedPromotion.discountAmount)
        assertEquals(100.0, appliedPromotion.originalAmount)
        assertEquals(85.0, appliedPromotion.finalAmount)
        assertEquals(true, appliedPromotion.isAutoApplied)
        assertEquals("Flash Sale (15.0 off)", appliedPromotion.displayText)
        assertEquals("Fixed Amount Discount", appliedPromotion.typeDisplay)
        assertEquals(15.0, appliedPromotion.savingsAmount)
        assertEquals(false, appliedPromotion.isPercentageDiscount)
        assertEquals(true, appliedPromotion.isFixedAmountDiscount)
    }
    
    @Test
    fun `test SaleDTO without promotions`() {
        // Given
        val saleDTO = SaleDTO(
            id = 1L,
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
            status = "PENDING",
            hasPromotions = false,
            promotionCount = 0,
            totalSavings = 0.0,
            appliedPromotions = emptyList()
        )
        
        // Then
        assertEquals(1L, saleDTO.id)
        assertNull(saleDTO.couponCode)
        assertNull(saleDTO.originalTotal)
        assertNull(saleDTO.finalTotal)
        assertNull(saleDTO.promotionDiscountAmount)
        assertEquals(0.0, saleDTO.totalSavings)
        assertEquals(false, saleDTO.hasPromotions)
        assertEquals(0, saleDTO.promotionCount)
        assertNotNull(saleDTO.appliedPromotions)
        assertTrue(saleDTO.appliedPromotions?.isEmpty() == true)
    }
    
    @Test
    fun `test PromotionDTO with all fields`() {
        // Given
        val promotionDTO = PromotionDTO(
            id = 1L,
            name = "Summer Sale 2024",
            description = "20% off all summer items",
            type = "PERCENTAGE",
            discountValue = 20.0,
            minimumOrderAmount = 50.0,
            maximumDiscountAmount = 100.0,
            startDate = "2024-06-01T00:00:00",
            endDate = "2024-08-31T23:59:59",
            isActive = true,
            applicableProducts = listOf(1L, 2L, 3L),
            applicableCategories = listOf("CLOTHING", "ACCESSORIES"),
            usageLimit = 1000,
            usageCount = 250,
            customerEligibility = "ALL",
            couponCode = "SUMMER20",
            autoApply = false,
            stackable = true,
            statusDisplay = "Active",
            typeDisplay = "Percentage Discount",
            eligibilityDisplay = "All Customers",
            isCurrentlyActive = true,
            isExpired = false,
            isNotYetStarted = false,
            isUsageLimitReached = false,
            daysUntilExpiry = 45,
            remainingUsage = 750,
            usagePercentage = 25.0
        )
        
        // Then
        assertEquals(1L, promotionDTO.id)
        assertEquals("Summer Sale 2024", promotionDTO.name)
        assertEquals("PERCENTAGE", promotionDTO.type)
        assertEquals(20.0, promotionDTO.discountValue)
        assertEquals(50.0, promotionDTO.minimumOrderAmount)
        assertEquals(100.0, promotionDTO.maximumDiscountAmount)
        assertEquals("SUMMER20", promotionDTO.couponCode)
        assertEquals(true, promotionDTO.isActive)
        assertEquals(false, promotionDTO.autoApply)
        assertEquals(true, promotionDTO.stackable)
        assertEquals(true, promotionDTO.isCurrentlyActive)
        assertEquals(false, promotionDTO.isExpired)
        assertEquals(1000, promotionDTO.usageLimit)
        assertEquals(250, promotionDTO.usageCount)
        assertEquals(750, promotionDTO.remainingUsage)
        assertEquals(25.0, promotionDTO.usagePercentage)
        assertNotNull(promotionDTO.applicableProducts)
        assertEquals(3, promotionDTO.applicableProducts?.size)
        assertNotNull(promotionDTO.applicableCategories)
        assertEquals(2, promotionDTO.applicableCategories?.size)
    }
}
