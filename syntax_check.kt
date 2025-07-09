import services.CanvasReturnReceiptService
import data.api.ReturnDTO
import data.api.ReturnItemDTO

fun main() {
    // Simple syntax check - create a sample ReturnDTO
    val sampleReturn = ReturnDTO(
        id = 1L,
        originalSaleId = 123L,
        customerId = 456L,
        customerName = "Test Customer",
        reason = "DEFECTIVE",
        status = "PENDING",
        totalRefundAmount = 100.0,
        items = listOf(
            ReturnItemDTO(
                id = 1L,
                returnId = 1L,
                originalSaleItemId = 1L,
                productId = 1L,
                productName = "Test Product",
                returnQuantity = 1,
                originalUnitPrice = 100.0,
                refundAmount = 100.0,
                itemCondition = "DEFECTIVE"
            )
        )
    )
    
    println("Syntax check passed - ReturnDTO created successfully")
    println("Service available: ${CanvasReturnReceiptService::class.simpleName}")
}
