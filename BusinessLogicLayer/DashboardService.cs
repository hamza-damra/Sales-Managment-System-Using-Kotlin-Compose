using Microsoft.EntityFrameworkCore;
using DXApplication1.DataAccessLayer;
using DXApplication1.Models;
using System.Globalization;

namespace DXApplication1.BusinessLogicLayer
{
    /// <summary>
    /// تنفيذ خدمة لوحة التحكم - Dashboard Service Implementation
    /// </summary>
    public class DashboardService : IDashboardService
    {
        private readonly IUnitOfWork? _unitOfWork;
        private readonly SalesDbContext? _context;

        // Constructor for dependency injection (existing)
        public DashboardService(IUnitOfWork unitOfWork, SalesDbContext context)
        {
            _unitOfWork = unitOfWork ?? throw new ArgumentNullException(nameof(unitOfWork));
            _context = context ?? throw new ArgumentNullException(nameof(context));
        }

        // Parameterless constructor for thread-safe operations
        public DashboardService()
        {
            // Services will create their own DbContext instances per operation
        }

        public async Task<DashboardSummary> GetDashboardSummaryAsync()
        {
            var summary = new DashboardSummary
            {
                TotalCustomers = await GetTotalCustomersAsync(),
                TotalProducts = await GetTotalProductsAsync(),
                TotalInvoices = await GetTotalInvoicesAsync(),
                TotalSales = await GetTotalSalesAsync(),
                MonthlyGrowthRate = await GetMonthlyGrowthRateAsync(),
                AverageSaleValue = await GetAverageSaleValueAsync(),
                ActiveCustomers = await GetActiveCustomersCountAsync(),
                InventoryValue = await GetInventoryValueAsync(),
                LowStockProducts = await GetLowStockProductsCountAsync(),
                OverdueInvoices = await GetOverdueInvoicesCountAsync()
            };

            return summary;
        }

        public async Task<int> GetTotalCustomersAsync()
        {
            if (_context != null)
            {
                return await _context.Customers
                    .Where(c => !c.IsDeleted)
                    .CountAsync();
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            return await context.Customers
                .Where(c => !c.IsDeleted)
                .CountAsync();
        }

        public async Task<int> GetTotalProductsAsync()
        {
            if (_context != null)
            {
                return await _context.Products
                    .Where(p => !p.IsDeleted)
                    .CountAsync();
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            return await context.Products
                .Where(p => !p.IsDeleted)
                .CountAsync();
        }

        public async Task<int> GetTotalInvoicesAsync()
        {
            if (_context != null)
            {
                return await _context.Invoices
                    .Where(i => !i.IsDeleted)
                    .CountAsync();
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            return await context.Invoices
                .Where(i => !i.IsDeleted)
                .CountAsync();
        }

        public async Task<decimal> GetTotalSalesAsync()
        {
            return await _context.Invoices
                .Where(i => !i.IsDeleted && i.Status != InvoiceStatus.Cancelled)
                .SumAsync(i => i.TotalAmount);
        }

        public async Task<decimal> GetTotalSalesAsync(DateTime fromDate, DateTime toDate)
        {
            return await _context.Invoices
                .Where(i => !i.IsDeleted && 
                           i.Status != InvoiceStatus.Cancelled &&
                           i.InvoiceDate >= fromDate && 
                           i.InvoiceDate <= toDate)
                .SumAsync(i => i.TotalAmount);
        }

        public async Task<IEnumerable<MonthlySalesData>> GetMonthlySalesDataAsync(int months = 12)
        {
            var startDate = DateTime.Now.AddMonths(-months);
            
            var salesData = await _context.Invoices
                .Where(i => !i.IsDeleted && 
                           i.Status != InvoiceStatus.Cancelled &&
                           i.InvoiceDate >= startDate)
                .GroupBy(i => new { i.InvoiceDate.Year, i.InvoiceDate.Month })
                .Select(g => new MonthlySalesData
                {
                    Date = new DateTime(g.Key.Year, g.Key.Month, 1),
                    Sales = g.Sum(i => i.TotalAmount),
                    InvoiceCount = g.Count()
                })
                .OrderBy(m => m.Date)
                .ToListAsync();

            // إضافة أسماء الأشهر بالعربية
            var arabicCulture = new CultureInfo("ar-SA");
            foreach (var data in salesData)
            {
                data.Month = data.Date.ToString("MMMM yyyy", arabicCulture);
            }

            return salesData;
        }

        public async Task<IEnumerable<TopProductData>> GetTopProductsDataAsync(int count = 10)
        {
            var topProducts = await _context.InvoiceItems
                .Include(ii => ii.Product)
                .Where(ii => !ii.IsDeleted && ii.Product != null && !ii.Product.IsDeleted)
                .GroupBy(ii => new { ii.ProductId, ii.Product.ProductName })
                .Select(g => new TopProductData
                {
                    ProductName = g.Key.ProductName,
                    TotalSales = g.Sum(ii => ii.LineTotal),
                    QuantitySold = (int)g.Sum(ii => ii.Quantity),
                    Profit = g.Sum(ii => ii.LineTotal - (ii.Product.PurchasePrice * ii.Quantity))
                })
                .OrderByDescending(p => p.TotalSales)
                .Take(count)
                .ToListAsync();

            return topProducts;
        }

        public async Task<IEnumerable<TopCustomerData>> GetTopCustomersDataAsync(int count = 10)
        {
            var topCustomers = await _context.Invoices
                .Include(i => i.Customer)
                .Where(i => !i.IsDeleted && 
                           i.Status != InvoiceStatus.Cancelled &&
                           i.Customer != null && 
                           !i.Customer.IsDeleted)
                .GroupBy(i => new { i.CustomerId, i.Customer.CustomerName, i.Customer.CurrentBalance })
                .Select(g => new TopCustomerData
                {
                    CustomerName = g.Key.CustomerName,
                    TotalPurchases = g.Sum(i => i.TotalAmount),
                    InvoiceCount = g.Count(),
                    CurrentBalance = g.Key.CurrentBalance
                })
                .OrderByDescending(c => c.TotalPurchases)
                .Take(count)
                .ToListAsync();

            return topCustomers;
        }

        public async Task<IEnumerable<SalesByPaymentMethodData>> GetSalesByPaymentMethodAsync()
        {
            var totalSales = await GetTotalSalesAsync();
            
            var paymentMethodData = await _context.Invoices
                .Where(i => !i.IsDeleted && i.Status != InvoiceStatus.Cancelled)
                .GroupBy(i => i.PaymentMethod)
                .Select(g => new SalesByPaymentMethodData
                {
                    PaymentMethod = g.Key,
                    TotalSales = g.Sum(i => i.TotalAmount),
                    InvoiceCount = g.Count()
                })
                .ToListAsync();

            // حساب النسب المئوية وإضافة أسماء طرق الدفع
            foreach (var data in paymentMethodData)
            {
                data.Percentage = totalSales > 0 ? (data.TotalSales / totalSales) * 100 : 0;
                data.PaymentMethodName = GetPaymentMethodName(data.PaymentMethod);
            }

            return paymentMethodData.OrderByDescending(p => p.TotalSales);
        }

        public async Task<IEnumerable<Invoice>> GetRecentInvoicesAsync(int count = 10)
        {
            return await _context.Invoices
                .Include(i => i.Customer)
                .Include(i => i.User)
                .Where(i => !i.IsDeleted)
                .OrderByDescending(i => i.InvoiceDate)
                .Take(count)
                .ToListAsync();
        }

        public async Task<IEnumerable<Customer>> GetRecentCustomersAsync(int count = 10)
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted)
                .OrderByDescending(c => c.CreatedDate)
                .Take(count)
                .ToListAsync();
        }

        public async Task<IEnumerable<Product>> GetLowStockProductsAsync(int count = 10)
        {
            return await _context.Products
                .Where(p => !p.IsDeleted && p.StockQuantity <= p.MinimumStock)
                .OrderBy(p => p.StockQuantity)
                .Take(count)
                .ToListAsync();
        }

        public async Task<decimal> GetAverageSaleValueAsync()
        {
            var invoices = await _context.Invoices
                .Where(i => !i.IsDeleted && i.Status != InvoiceStatus.Cancelled)
                .ToListAsync();

            return invoices.Any() ? invoices.Average(i => i.TotalAmount) : 0;
        }

        public async Task<decimal> GetMonthlyGrowthRateAsync()
        {
            var currentMonth = DateTime.Now;
            var lastMonth = currentMonth.AddMonths(-1);
            
            var currentMonthSales = await GetTotalSalesAsync(
                new DateTime(currentMonth.Year, currentMonth.Month, 1),
                new DateTime(currentMonth.Year, currentMonth.Month, DateTime.DaysInMonth(currentMonth.Year, currentMonth.Month)));
            
            var lastMonthSales = await GetTotalSalesAsync(
                new DateTime(lastMonth.Year, lastMonth.Month, 1),
                new DateTime(lastMonth.Year, lastMonth.Month, DateTime.DaysInMonth(lastMonth.Year, lastMonth.Month)));

            if (lastMonthSales == 0) return 0;
            
            return ((currentMonthSales - lastMonthSales) / lastMonthSales) * 100;
        }

        public async Task<int> GetActiveCustomersCountAsync()
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted && c.IsActive)
                .CountAsync();
        }

        public async Task<decimal> GetInventoryValueAsync()
        {
            return await _context.Products
                .Where(p => !p.IsDeleted)
                .SumAsync(p => p.StockQuantity * p.PurchasePrice);
        }

        private async Task<int> GetLowStockProductsCountAsync()
        {
            return await _context.Products
                .Where(p => !p.IsDeleted && p.StockQuantity <= p.MinimumStock)
                .CountAsync();
        }

        private async Task<int> GetOverdueInvoicesCountAsync()
        {
            var today = DateTime.Today;
            return await _context.Invoices
                .Where(i => !i.IsDeleted && 
                           i.Status == InvoiceStatus.Pending &&
                           i.DueDate.HasValue && 
                           i.DueDate.Value < today)
                .CountAsync();
        }

        private static string GetPaymentMethodName(PaymentMethod paymentMethod)
        {
            return paymentMethod switch
            {
                PaymentMethod.Cash => "نقدي",
                PaymentMethod.CreditCard => "بطاقة ائتمان",
                PaymentMethod.BankTransfer => "تحويل بنكي",
                PaymentMethod.Check => "شيك",
                PaymentMethod.Other => "أخرى",
                _ => "غير محدد"
            };
        }
    }
}
