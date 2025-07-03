using DXApplication1.Models;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// واجهة مستودع المنتجات - Product Repository Interface
    /// </summary>
    public interface IProductRepository : IRepository<Product>
    {
        // البحث والتصفية - Search and Filter
        Task<IEnumerable<Product>> SearchProductsAsync(string searchTerm);
        Task<IEnumerable<Product>> GetProductsByCategoryAsync(int categoryId);
        Task<IEnumerable<Product>> GetActiveProductsAsync();
        Task<Product?> GetProductByCodeAsync(string productCode);
        Task<Product?> GetProductByBarcodeAsync(string barcode);

        // إدارة المخزون - Inventory Management
        Task<bool> UpdateStockQuantityAsync(int productId, int quantity);
        Task<bool> AddToStockAsync(int productId, int quantity);
        Task<bool> SubtractFromStockAsync(int productId, int quantity);
        Task<IEnumerable<Product>> GetLowStockProductsAsync();
        Task<IEnumerable<Product>> GetOutOfStockProductsAsync();
        Task<IEnumerable<Product>> GetOverStockProductsAsync();

        // إدارة الأسعار - Price Management
        Task<bool> UpdateProductPriceAsync(int productId, decimal purchasePrice, decimal salePrice, decimal? minimumPrice = null);
        Task<IEnumerable<Product>> GetProductsByPriceRangeAsync(decimal minPrice, decimal maxPrice);

        // التحقق من صحة البيانات - Data Validation
        Task<bool> IsProductCodeExistsAsync(string productCode, int? excludeProductId = null);
        Task<bool> IsBarcodeExistsAsync(string barcode, int? excludeProductId = null);

        // التقارير والإحصائيات - Reports and Statistics
        Task<int> GetTotalProductCountAsync();
        Task<int> GetProductCountByCategoryAsync(int categoryId);
        Task<decimal> GetTotalInventoryValueAsync();
        Task<decimal> GetCategoryInventoryValueAsync(int categoryId);
        Task<IEnumerable<Product>> GetTopSellingProductsAsync(int count = 10);
        Task<IEnumerable<Product>> GetMostProfitableProductsAsync(int count = 10);

        // تاريخ المبيعات - Sales History
        Task<IEnumerable<InvoiceItem>> GetProductSalesHistoryAsync(int productId);
        Task<IEnumerable<InvoiceItem>> GetProductSalesHistoryAsync(int productId, DateTime fromDate, DateTime toDate);
        Task<decimal> GetProductTotalSalesAsync(int productId);
        Task<decimal> GetProductTotalSalesAsync(int productId, DateTime fromDate, DateTime toDate);
        Task<int> GetProductTotalQuantitySoldAsync(int productId);
        Task<int> GetProductTotalQuantitySoldAsync(int productId, DateTime fromDate, DateTime toDate);
    }
}
