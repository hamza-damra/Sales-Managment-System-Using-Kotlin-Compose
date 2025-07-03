using Microsoft.EntityFrameworkCore;
using DXApplication1.Models;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// تنفيذ مستودع المنتجات - Product Repository Implementation
    /// </summary>
    public class ProductRepository : Repository<Product>, IProductRepository
    {
        public ProductRepository(SalesDbContext context) : base(context)
        {
        }

        // البحث والتصفية - Search and Filter
        public async Task<IEnumerable<Product>> SearchProductsAsync(string searchTerm)
        {
            if (string.IsNullOrWhiteSpace(searchTerm))
                return await GetActiveProductsAsync();

            searchTerm = searchTerm.Trim().ToLower();

            return await _dbSet
                .Include(p => p.Category)
                .Where(p => !p.IsDeleted &&
                    (p.ProductName.ToLower().Contains(searchTerm) ||
                     (p.ProductCode != null && p.ProductCode.ToLower().Contains(searchTerm)) ||
                     (p.Barcode != null && p.Barcode.Contains(searchTerm)) ||
                     (p.Description != null && p.Description.ToLower().Contains(searchTerm))))
                .OrderBy(p => p.ProductName)
                .ToListAsync();
        }

        public async Task<IEnumerable<Product>> GetProductsByCategoryAsync(int categoryId)
        {
            return await _dbSet
                .Include(p => p.Category)
                .Where(p => p.CategoryId == categoryId && !p.IsDeleted)
                .OrderBy(p => p.ProductName)
                .ToListAsync();
        }

        public async Task<IEnumerable<Product>> GetActiveProductsAsync()
        {
            return await _dbSet
                .Include(p => p.Category)
                .Where(p => p.IsActive && !p.IsDeleted)
                .OrderBy(p => p.ProductName)
                .ToListAsync();
        }

        public async Task<Product?> GetProductByCodeAsync(string productCode)
        {
            if (string.IsNullOrWhiteSpace(productCode))
                return null;

            return await _dbSet
                .Include(p => p.Category)
                .FirstOrDefaultAsync(p => p.ProductCode == productCode && !p.IsDeleted);
        }

        public async Task<Product?> GetProductByBarcodeAsync(string barcode)
        {
            if (string.IsNullOrWhiteSpace(barcode))
                return null;

            return await _dbSet
                .Include(p => p.Category)
                .FirstOrDefaultAsync(p => p.Barcode == barcode && !p.IsDeleted);
        }

        // إدارة المخزون - Inventory Management
        public async Task<bool> UpdateStockQuantityAsync(int productId, int quantity)
        {
            try
            {
                var product = await GetByIdAsync(productId);
                if (product == null || product.IsDeleted)
                    return false;

                product.StockQuantity = quantity;
                product.ModifiedDate = DateTime.Now;
                product.ModifiedBy = "System";

                Update(product);
                return true;
            }
            catch
            {
                return false;
            }
        }

        public async Task<bool> AddToStockAsync(int productId, int quantity)
        {
            try
            {
                var product = await GetByIdAsync(productId);
                if (product == null || product.IsDeleted)
                    return false;

                product.StockQuantity += quantity;
                product.ModifiedDate = DateTime.Now;
                product.ModifiedBy = "System";

                Update(product);
                return true;
            }
            catch
            {
                return false;
            }
        }

        public async Task<bool> SubtractFromStockAsync(int productId, int quantity)
        {
            try
            {
                var product = await GetByIdAsync(productId);
                if (product == null || product.IsDeleted)
                    return false;

                if (!product.AllowNegativeStock && product.StockQuantity < quantity)
                    return false;

                product.StockQuantity -= quantity;
                product.ModifiedDate = DateTime.Now;
                product.ModifiedBy = "System";

                Update(product);
                return true;
            }
            catch
            {
                return false;
            }
        }

        public async Task<IEnumerable<Product>> GetLowStockProductsAsync()
        {
            return await _dbSet
                .Include(p => p.Category)
                .Where(p => !p.IsDeleted && p.TrackInventory && p.StockQuantity <= p.MinimumStock)
                .OrderBy(p => p.StockQuantity)
                .ToListAsync();
        }

        public async Task<IEnumerable<Product>> GetOutOfStockProductsAsync()
        {
            return await _dbSet
                .Include(p => p.Category)
                .Where(p => !p.IsDeleted && p.TrackInventory && p.StockQuantity <= 0)
                .OrderBy(p => p.ProductName)
                .ToListAsync();
        }

        public async Task<IEnumerable<Product>> GetOverStockProductsAsync()
        {
            return await _dbSet
                .Include(p => p.Category)
                .Where(p => !p.IsDeleted && p.TrackInventory && p.MaximumStock > 0 && p.StockQuantity >= p.MaximumStock)
                .OrderByDescending(p => p.StockQuantity)
                .ToListAsync();
        }

        // إدارة الأسعار - Price Management
        public async Task<bool> UpdateProductPriceAsync(int productId, decimal purchasePrice, decimal salePrice, decimal? minimumPrice = null)
        {
            try
            {
                var product = await GetByIdAsync(productId);
                if (product == null || product.IsDeleted)
                    return false;

                product.PurchasePrice = purchasePrice;
                product.SalePrice = salePrice;
                if (minimumPrice.HasValue)
                    product.MinimumPrice = minimumPrice.Value;
                product.ModifiedDate = DateTime.Now;
                product.ModifiedBy = "System";

                Update(product);
                return true;
            }
            catch
            {
                return false;
            }
        }

        public async Task<IEnumerable<Product>> GetProductsByPriceRangeAsync(decimal minPrice, decimal maxPrice)
        {
            return await _dbSet
                .Include(p => p.Category)
                .Where(p => !p.IsDeleted && p.SalePrice >= minPrice && p.SalePrice <= maxPrice)
                .OrderBy(p => p.SalePrice)
                .ToListAsync();
        }

        // التحقق من صحة البيانات - Data Validation
        public async Task<bool> IsProductCodeExistsAsync(string productCode, int? excludeProductId = null)
        {
            if (string.IsNullOrWhiteSpace(productCode))
                return false;

            var query = _dbSet.Where(p => !p.IsDeleted && p.ProductCode == productCode);
            
            if (excludeProductId.HasValue)
                query = query.Where(p => p.Id != excludeProductId.Value);

            return await query.AnyAsync();
        }

        public async Task<bool> IsBarcodeExistsAsync(string barcode, int? excludeProductId = null)
        {
            if (string.IsNullOrWhiteSpace(barcode))
                return false;

            var query = _dbSet.Where(p => !p.IsDeleted && p.Barcode == barcode);
            
            if (excludeProductId.HasValue)
                query = query.Where(p => p.Id != excludeProductId.Value);

            return await query.AnyAsync();
        }

        // التقارير والإحصائيات - Reports and Statistics
        public async Task<int> GetTotalProductCountAsync()
        {
            return await _dbSet
                .Where(p => !p.IsDeleted)
                .CountAsync();
        }

        public async Task<int> GetProductCountByCategoryAsync(int categoryId)
        {
            return await _dbSet
                .Where(p => p.CategoryId == categoryId && !p.IsDeleted)
                .CountAsync();
        }

        public async Task<decimal> GetTotalInventoryValueAsync()
        {
            return await _dbSet
                .Where(p => !p.IsDeleted && p.TrackInventory)
                .SumAsync(p => p.StockQuantity * p.PurchasePrice);
        }

        public async Task<decimal> GetCategoryInventoryValueAsync(int categoryId)
        {
            return await _dbSet
                .Where(p => p.CategoryId == categoryId && !p.IsDeleted && p.TrackInventory)
                .SumAsync(p => p.StockQuantity * p.PurchasePrice);
        }

        public async Task<IEnumerable<Product>> GetTopSellingProductsAsync(int count = 10)
        {
            return await _context.InvoiceItems
                .Include(ii => ii.Product)
                .ThenInclude(p => p.Category)
                .Where(ii => !ii.IsDeleted && !ii.Product.IsDeleted)
                .GroupBy(ii => ii.ProductId)
                .OrderByDescending(g => g.Sum(ii => ii.Quantity))
                .Take(count)
                .Select(g => g.First().Product)
                .ToListAsync();
        }

        public async Task<IEnumerable<Product>> GetMostProfitableProductsAsync(int count = 10)
        {
            return await _context.InvoiceItems
                .Include(ii => ii.Product)
                .ThenInclude(p => p.Category)
                .Where(ii => !ii.IsDeleted && !ii.Product.IsDeleted)
                .GroupBy(ii => ii.ProductId)
                .OrderByDescending(g => g.Sum(ii => (ii.UnitPrice - ii.Product.PurchasePrice) * ii.Quantity))
                .Take(count)
                .Select(g => g.First().Product)
                .ToListAsync();
        }

        // تاريخ المبيعات - Sales History
        public async Task<IEnumerable<InvoiceItem>> GetProductSalesHistoryAsync(int productId)
        {
            return await _context.InvoiceItems
                .Include(ii => ii.Invoice)
                .ThenInclude(i => i.Customer)
                .Include(ii => ii.Product)
                .Where(ii => ii.ProductId == productId && !ii.IsDeleted && !ii.Invoice.IsDeleted)
                .OrderByDescending(ii => ii.Invoice.InvoiceDate)
                .ToListAsync();
        }

        public async Task<IEnumerable<InvoiceItem>> GetProductSalesHistoryAsync(int productId, DateTime fromDate, DateTime toDate)
        {
            return await _context.InvoiceItems
                .Include(ii => ii.Invoice)
                .ThenInclude(i => i.Customer)
                .Include(ii => ii.Product)
                .Where(ii => ii.ProductId == productId && !ii.IsDeleted && !ii.Invoice.IsDeleted &&
                           ii.Invoice.InvoiceDate >= fromDate && ii.Invoice.InvoiceDate <= toDate)
                .OrderByDescending(ii => ii.Invoice.InvoiceDate)
                .ToListAsync();
        }

        public async Task<decimal> GetProductTotalSalesAsync(int productId)
        {
            return await _context.InvoiceItems
                .Where(ii => ii.ProductId == productId && !ii.IsDeleted && !ii.Invoice.IsDeleted)
                .SumAsync(ii => ii.LineTotal);
        }

        public async Task<decimal> GetProductTotalSalesAsync(int productId, DateTime fromDate, DateTime toDate)
        {
            return await _context.InvoiceItems
                .Where(ii => ii.ProductId == productId && !ii.IsDeleted && !ii.Invoice.IsDeleted &&
                           ii.Invoice.InvoiceDate >= fromDate && ii.Invoice.InvoiceDate <= toDate)
                .SumAsync(ii => ii.LineTotal);
        }

        public async Task<int> GetProductTotalQuantitySoldAsync(int productId)
        {
            return await _context.InvoiceItems
                .Where(ii => ii.ProductId == productId && !ii.IsDeleted && !ii.Invoice.IsDeleted)
                .SumAsync(ii => (int)ii.Quantity);
        }

        public async Task<int> GetProductTotalQuantitySoldAsync(int productId, DateTime fromDate, DateTime toDate)
        {
            return await _context.InvoiceItems
                .Where(ii => ii.ProductId == productId && !ii.IsDeleted && !ii.Invoice.IsDeleted &&
                           ii.Invoice.InvoiceDate >= fromDate && ii.Invoice.InvoiceDate <= toDate)
                .SumAsync(ii => (int)ii.Quantity);
        }
    }
}
