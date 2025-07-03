using DXApplication1.Models;

namespace DXApplication1.BusinessLogicLayer
{
    /// <summary>
    /// واجهة خدمة لوحة التحكم - Dashboard Service Interface
    /// </summary>
    public interface IDashboardService
    {
        // الإحصائيات الأساسية - Basic Statistics
        Task<DashboardSummary> GetDashboardSummaryAsync();
        Task<int> GetTotalCustomersAsync();
        Task<int> GetTotalProductsAsync();
        Task<int> GetTotalInvoicesAsync();
        Task<decimal> GetTotalSalesAsync();
        Task<decimal> GetTotalSalesAsync(DateTime fromDate, DateTime toDate);

        // بيانات المخططات - Chart Data
        Task<IEnumerable<MonthlySalesData>> GetMonthlySalesDataAsync(int months = 12);
        Task<IEnumerable<TopProductData>> GetTopProductsDataAsync(int count = 10);
        Task<IEnumerable<TopCustomerData>> GetTopCustomersDataAsync(int count = 10);
        Task<IEnumerable<SalesByPaymentMethodData>> GetSalesByPaymentMethodAsync();

        // الأنشطة الحديثة - Recent Activities
        Task<IEnumerable<Invoice>> GetRecentInvoicesAsync(int count = 10);
        Task<IEnumerable<Customer>> GetRecentCustomersAsync(int count = 10);
        Task<IEnumerable<Product>> GetLowStockProductsAsync(int count = 10);

        // تحليلات متقدمة - Advanced Analytics
        Task<decimal> GetAverageSaleValueAsync();
        Task<decimal> GetMonthlyGrowthRateAsync();
        Task<int> GetActiveCustomersCountAsync();
        Task<decimal> GetInventoryValueAsync();
    }

    /// <summary>
    /// ملخص لوحة التحكم - Dashboard Summary
    /// </summary>
    public class DashboardSummary
    {
        public int TotalCustomers { get; set; }
        public int TotalProducts { get; set; }
        public int TotalInvoices { get; set; }
        public decimal TotalSales { get; set; }
        public decimal MonthlyGrowthRate { get; set; }
        public decimal AverageSaleValue { get; set; }
        public int ActiveCustomers { get; set; }
        public decimal InventoryValue { get; set; }
        public int LowStockProducts { get; set; }
        public int OverdueInvoices { get; set; }
    }

    /// <summary>
    /// بيانات المبيعات الشهرية - Monthly Sales Data
    /// </summary>
    public class MonthlySalesData
    {
        public string Month { get; set; } = string.Empty;
        public decimal Sales { get; set; }
        public int InvoiceCount { get; set; }
        public DateTime Date { get; set; }
    }

    /// <summary>
    /// بيانات أفضل المنتجات - Top Products Data
    /// </summary>
    public class TopProductData
    {
        public string ProductName { get; set; } = string.Empty;
        public decimal TotalSales { get; set; }
        public int QuantitySold { get; set; }
        public decimal Profit { get; set; }
    }

    /// <summary>
    /// بيانات أفضل العملاء - Top Customers Data
    /// </summary>
    public class TopCustomerData
    {
        public string CustomerName { get; set; } = string.Empty;
        public decimal TotalPurchases { get; set; }
        public int InvoiceCount { get; set; }
        public decimal CurrentBalance { get; set; }
    }

    /// <summary>
    /// بيانات المبيعات حسب طريقة الدفع - Sales by Payment Method Data
    /// </summary>
    public class SalesByPaymentMethodData
    {
        public PaymentMethod PaymentMethod { get; set; }
        public string PaymentMethodName { get; set; } = string.Empty;
        public decimal TotalSales { get; set; }
        public int InvoiceCount { get; set; }
        public decimal Percentage { get; set; }
    }
}
