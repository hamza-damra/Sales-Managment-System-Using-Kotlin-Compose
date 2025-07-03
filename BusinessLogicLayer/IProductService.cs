using DXApplication1.Models;

namespace DXApplication1.BusinessLogicLayer
{
    /// <summary>
    /// واجهة خدمة المنتجات - Product Service Interface
    /// </summary>
    public interface IProductService
    {
        // إدارة المنتجات - Product Management
        Task<IEnumerable<Product>> GetAllProductsAsync();
        Task<Product?> GetProductByIdAsync(int id);
        Task<Product?> GetProductByCodeAsync(string productCode);
        Task<Product?> GetProductByBarcodeAsync(string barcode);
        Task<IEnumerable<Product>> SearchProductsAsync(string searchTerm);
        Task<IEnumerable<Product>> GetProductsByCategoryAsync(int categoryId);
        Task<bool> CreateProductAsync(Product product);
        Task<bool> UpdateProductAsync(Product product);
        Task<bool> DeleteProductAsync(int id);
        Task<bool> ActivateProductAsync(int id);
        Task<bool> DeactivateProductAsync(int id);

        // إدارة المخزون - Inventory Management
        Task<bool> UpdateStockQuantityAsync(int productId, int newQuantity);
        Task<bool> AddStockAsync(int productId, int quantity, string notes = "");
        Task<bool> ReduceStockAsync(int productId, int quantity, string notes = "");
        Task<IEnumerable<Product>> GetLowStockProductsAsync(int? count = null);
        Task<IEnumerable<Product>> GetOutOfStockProductsAsync();
        Task<bool> IsStockAvailableAsync(int productId, int requiredQuantity);

        // إدارة الأسعار - Price Management
        Task<bool> UpdateProductPricesAsync(int productId, decimal purchasePrice, decimal salePrice, decimal? minimumPrice = null);
        Task<bool> ApplyDiscountAsync(int productId, decimal discountPercentage);
        Task<bool> ApplyBulkPriceUpdateAsync(int categoryId, decimal priceChangePercentage);

        // التحقق من صحة البيانات - Data Validation
        Task<bool> IsProductCodeAvailableAsync(string productCode, int? excludeProductId = null);
        Task<bool> IsBarcodeAvailableAsync(string barcode, int? excludeProductId = null);
        bool ValidateProductData(Product product, out List<string> errors);

        // التقارير والإحصائيات - Reports and Statistics
        Task<int> GetTotalProductsCountAsync();
        Task<int> GetActiveProductsCountAsync();
        Task<int> GetProductsCountByCategoryAsync(int categoryId);
        Task<decimal> GetTotalInventoryValueAsync();
        Task<IEnumerable<Product>> GetTopSellingProductsAsync(int count = 10);
        Task<IEnumerable<Product>> GetTopProfitableProductsAsync(int count = 10);

        // الفئات - Categories
        Task<IEnumerable<Category>> GetAllCategoriesAsync();
        Task<Category?> GetCategoryByIdAsync(int id);
        Task<bool> CreateCategoryAsync(Category category);
        Task<bool> UpdateCategoryAsync(Category category);
        Task<bool> DeleteCategoryAsync(int id);
    }
}
