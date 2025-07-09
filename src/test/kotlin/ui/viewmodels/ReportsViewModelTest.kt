package ui.viewmodels

import data.api.*
import data.repository.ReportsRepository
import data.api.services.ReportsApiService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private lateinit var mockReportsRepository: ReportsRepository
    private lateinit var reportsViewModel: ReportsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        mockReportsRepository = mockk(relaxed = true)
        
        // Mock repository state flows
        every { mockReportsRepository.isLoading } returns MutableStateFlow(false)
        every { mockReportsRepository.error } returns MutableStateFlow(null)
        every { mockReportsRepository.comprehensiveSalesReport } returns MutableStateFlow(null)
        every { mockReportsRepository.customerReport } returns MutableStateFlow(null)
        every { mockReportsRepository.productReport } returns MutableStateFlow(null)
        every { mockReportsRepository.inventoryReport } returns MutableStateFlow(null)
        every { mockReportsRepository.financialReport } returns MutableStateFlow(null)
        every { mockReportsRepository.promotionReport } returns MutableStateFlow(null)
        every { mockReportsRepository.realTimeKPIs } returns MutableStateFlow(null)
        
        reportsViewModel = ReportsViewModel(mockReportsRepository)
    }

    @Test
    fun `selectReportType should update selected report type`() = runTest(testDispatcher) {
        // When
        reportsViewModel.selectReportType("customers")
        
        // Then
        assertEquals("customers", reportsViewModel.selectedReportType.value)
    }

    @Test
    fun `selectDateRange should update selected date range`() = runTest(testDispatcher) {
        // When
        reportsViewModel.selectDateRange(DateRange.LAST_7_DAYS)
        
        // Then
        assertEquals(DateRange.LAST_7_DAYS, reportsViewModel.selectedDateRange.value)
    }

    @Test
    fun `setCustomDateRange should update custom dates and set range to CUSTOM`() = runTest(testDispatcher) {
        // Given
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 31)
        
        // When
        reportsViewModel.setCustomDateRange(startDate, endDate)
        
        // Then
        assertEquals(DateRange.CUSTOM, reportsViewModel.selectedDateRange.value)
        assertEquals(startDate, reportsViewModel.customStartDate.value)
        assertEquals(endDate, reportsViewModel.customEndDate.value)
    }

    @Test
    fun `updateFilters should update selected filters`() = runTest(testDispatcher) {
        // Given
        val filters = ReportFilters(
            customerIds = listOf(1L, 2L),
            includeInactive = true
        )
        
        // When
        reportsViewModel.updateFilters(filters)
        
        // Then
        assertEquals(filters, reportsViewModel.selectedFilters.value)
    }

    @Test
    fun `refreshCurrentReport should call repository method for sales report`() = runTest(testDispatcher) {
        // Given
        reportsViewModel.selectReportType("sales")
        coEvery { mockReportsRepository.loadComprehensiveSalesReport(any()) } returns NetworkResult.Success(mockk())
        
        // When
        reportsViewModel.refreshCurrentReport()
        
        // Then
        coVerify { mockReportsRepository.loadComprehensiveSalesReport(any()) }
    }

    @Test
    fun `refreshCurrentReport should call repository method for customer report`() = runTest(testDispatcher) {
        // Given
        reportsViewModel.selectReportType("customers")
        coEvery { mockReportsRepository.loadCustomerAnalytics(any(), any()) } returns NetworkResult.Success(mockk())
        
        // When
        reportsViewModel.refreshCurrentReport()
        
        // Then
        coVerify { mockReportsRepository.loadCustomerAnalytics(any(), any()) }
    }

    @Test
    fun `exportReport should call repository export method with correct parameters`() = runTest(testDispatcher) {
        // Given
        val format = "PDF"
        val mockByteArray = byteArrayOf(1, 2, 3)
        coEvery { mockReportsRepository.exportReport(any()) } returns NetworkResult.Success(mockByteArray)
        
        // When
        val result = reportsViewModel.exportReport(format)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockByteArray, result.getOrNull())
        coVerify { mockReportsRepository.exportReport(any()) }
    }

    @Test
    fun `clearError should call repository clearError`() {
        // When
        reportsViewModel.clearError()
        
        // Then
        verify { mockReportsRepository.clearError() }
    }

    @Test
    fun `currentDateRange should return correct dates for LAST_30_DAYS`() = runTest(testDispatcher) {
        // Given
        reportsViewModel.selectDateRange(DateRange.LAST_30_DAYS)
        
        // When
        val dateRange = reportsViewModel.currentDateRange.value
        
        // Then
        assertNotNull(dateRange.first)
        assertNotNull(dateRange.second)
        assertTrue(dateRange.first.contains("T00:00:00"))
        assertTrue(dateRange.second.contains("T23:59:59"))
    }

    @Test
    fun `currentDateRange should return custom dates when CUSTOM range is selected`() = runTest(testDispatcher) {
        // Given
        val startDate = LocalDate(2024, 1, 1)
        val endDate = LocalDate(2024, 1, 31)
        reportsViewModel.setCustomDateRange(startDate, endDate)
        
        // When
        val dateRange = reportsViewModel.currentDateRange.value
        
        // Then
        assertEquals("2024-01-01T00:00:00", dateRange.first)
        assertEquals("2024-01-31T23:59:59", dateRange.second)
    }
}
