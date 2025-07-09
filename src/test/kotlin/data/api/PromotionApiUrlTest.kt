package data.api

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Test class to verify that promotion API URLs are constructed correctly
 * This helps ensure the frontend is not the source of the routing issues
 */
class PromotionApiUrlTest {

    @Test
    fun `test promotion endpoints URL construction`() {
        // Test base URL
        val expectedBaseUrl = "http://localhost:8081/api"
        assertEquals(expectedBaseUrl, ApiConfig.BASE_URL)
        
        // Test promotion endpoints
        assertEquals("/promotions", ApiConfig.Endpoints.PROMOTIONS)
        assertEquals("/promotions/active", ApiConfig.Endpoints.PROMOTIONS_ACTIVE)
        assertEquals("/promotions/available", ApiConfig.Endpoints.PROMOTIONS_AVAILABLE)
        assertEquals("/promotions/expired", ApiConfig.Endpoints.PROMOTIONS_EXPIRED)
        assertEquals("/promotions/scheduled", ApiConfig.Endpoints.PROMOTIONS_SCHEDULED)
        assertEquals("/promotions/search", ApiConfig.Endpoints.PROMOTIONS_SEARCH)
    }
    
    @Test
    fun `test full URL construction for expired promotions`() {
        val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS_EXPIRED}"
        val expectedUrl = "http://localhost:8081/api/promotions/expired"
        
        assertEquals(expectedUrl, fullUrl)
        println("✅ Expired promotions URL: $fullUrl")
    }
    
    @Test
    fun `test full URL construction for scheduled promotions`() {
        val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PROMOTIONS_SCHEDULED}"
        val expectedUrl = "http://localhost:8081/api/promotions/scheduled"
        
        assertEquals(expectedUrl, fullUrl)
        println("✅ Scheduled promotions URL: $fullUrl")
    }
    
    @Test
    fun `test promotion by ID URL construction`() {
        val promotionId = 123L
        val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.promotionById(promotionId)}"
        val expectedUrl = "http://localhost:8081/api/promotions/123"
        
        assertEquals(expectedUrl, fullUrl)
        println("✅ Promotion by ID URL: $fullUrl")
    }
    
    @Test
    fun `verify URL paths do not conflict`() {
        // These should be distinct paths that don't conflict with each other
        val expiredPath = ApiConfig.Endpoints.PROMOTIONS_EXPIRED
        val scheduledPath = ApiConfig.Endpoints.PROMOTIONS_SCHEDULED
        val activePath = ApiConfig.Endpoints.PROMOTIONS_ACTIVE
        val byIdPath = ApiConfig.Endpoints.promotionById(1L)
        
        // Verify they are all different
        val paths = setOf(expiredPath, scheduledPath, activePath, byIdPath)
        assertEquals(4, paths.size, "All promotion endpoint paths should be unique")
        
        // Verify specific paths don't contain numeric IDs that could be confused
        assert(!expiredPath.contains(Regex("\\d+"))) { "Expired path should not contain numbers" }
        assert(!scheduledPath.contains(Regex("\\d+"))) { "Scheduled path should not contain numbers" }
        assert(!activePath.contains(Regex("\\d+"))) { "Active path should not contain numbers" }
        assert(byIdPath.contains(Regex("\\d+"))) { "By ID path should contain numbers" }
        
        println("✅ All promotion endpoint paths are unique and properly formatted")
        println("   - Expired: $expiredPath")
        println("   - Scheduled: $scheduledPath") 
        println("   - Active: $activePath")
        println("   - By ID: $byIdPath")
    }
    
    @Test
    fun `test backend routing expectations`() {
        // Document what the backend should support
        val expectedEndpoints = mapOf(
            "GET /api/promotions" to "Get all promotions with pagination",
            "GET /api/promotions/{id}" to "Get specific promotion by ID (Long)",
            "GET /api/promotions/active" to "Get active promotions",
            "GET /api/promotions/expired" to "Get expired promotions", 
            "GET /api/promotions/scheduled" to "Get scheduled promotions",
            "GET /api/promotions/available" to "Get available promotions",
            "GET /api/promotions/search" to "Search promotions"
        )
        
        println("📋 Expected backend endpoints:")
        expectedEndpoints.forEach { (endpoint, description) ->
            println("   $endpoint - $description")
        }
        
        // The key issue: backend should prioritize specific paths over {id} parameter
        println("\n⚠️  Backend Routing Priority:")
        println("   1. Specific paths like /expired, /scheduled should be matched first")
        println("   2. Generic /{id} parameter should be matched last")
        println("   3. Current issue: backend treats 'expired' as an ID parameter")
    }
}
