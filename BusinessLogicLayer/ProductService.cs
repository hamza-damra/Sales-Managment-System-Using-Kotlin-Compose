using DXApplication1.Models;
using DXApplication1.DataAccessLayer;
using Microsoft.EntityFrameworkCore;
using System.Text.RegularExpressions;

namespace DXApplication1.BusinessLogicLayer
{
    /// <summary>
    /// تنفيذ خدمة المنتجات - Product Service Implementation
    /// </summary>
    public class ProductService : IProductService, IDisposable
    {
        private readonly IUnitOfWork? _unitOfWork;
        private readonly IProductRepository? _productRepository;
        private readonly SalesDbContext? _context;

        // Constructor for dependency injection (existing)
        public ProductService(IUnitOfWork unitOfWork, SalesDbContext context)
        {
            _unitOfWork = unitOfWork ?? throw new ArgumentNullException(nameof(unitOfWork));
            _context = context ?? throw new ArgumentNullException(nameof(context));
            _productRepository = new ProductRepository(context);
        }

        // Parameterless constructor for thread-safe operations
        public ProductService()
        {
            // Services will create their own DbContext instances per operation
        }

        // إدارة المنتجات - Product Management
        public async Task<IEnumerable<Product>> GetAllProductsAsync()
        {
            if (_productRepository != null)
            {
                return await _productRepository.GetActiveProductsAsync();
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            var productRepository = new ProductRepository(context);
            return await productRepository.GetActiveProductsAsync();
        }

        public async Task<Product?> GetProductByIdAsync(int id)
        {
            if (_context != null)
            {
                return await _context.Products
                    .Include(p => p.Category)
                    .FirstOrDefaultAsync(p => p.Id == id && !p.IsDeleted);
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            return await context.Products
                .Include(p => p.Category)
                .FirstOrDefaultAsync(p => p.Id == id && !p.IsDeleted);
        }

        public async Task<Product?> GetProductByCodeAsync(string productCode)
        {
            if (_productRepository != null)
            {
                return await _productRepository.GetProductByCodeAsync(productCode);
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            var productRepository = new ProductRepository(context);
            return await productRepository.GetProductByCodeAsync(productCode);
        }

        public async Task<Product?> GetProductByBarcodeAsync(string barcode)
        {
            if (_productRepository != null)
            {
                return await _productRepository.GetProductByBarcodeAsync(barcode);
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            var productRepository = new ProductRepository(context);
            return await productRepository.GetProductByBarcodeAsync(barcode);
        }

        public async Task<IEnumerable<Product>> SearchProductsAsync(string searchTerm)
        {
            if (_productRepository != null)
            {
                return await _productRepository.SearchProductsAsync(searchTerm);
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            var productRepository = new ProductRepository(context);
            return await productRepository.SearchProductsAsync(searchTerm);
        }

        public async Task<IEnumerable<Product>> GetProductsByCategoryAsync(int categoryId)
        {
            if (_productRepository != null)
            {
                return await _productRepository.GetProductsByCategoryAsync(categoryId);
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            var productRepository = new ProductRepository(context);
            return await productRepository.GetProductsByCategoryAsync(categoryId);
        }

        public async Task<bool> CreateProductAsync(Product product)
        {
            try
            {
                if (product == null)
                    return false;

                // التحقق من صحة البيانات - Validate data
                if (!ValidateProductData(product, out List<string> errors))
                    throw new ArgumentException($"بيانات المنتج غير صحيحة: {string.Join(", ", errors)}");

                // التحقق من عدم تكرار كود المنتج - Check product code uniqueness
                if (!string.IsNullOrWhiteSpace(product.ProductCode) && 
                    !await IsProductCodeAvailableAsync(product.ProductCode))
                    throw new ArgumentException("كود المنتج مستخدم من قبل");

                // التحقق من عدم تكرار الباركود - Check barcode uniqueness
                if (!string.IsNullOrWhiteSpace(product.Barcode) && 
                    !await IsBarcodeAvailableAsync(product.Barcode))
                    throw new ArgumentException("الباركود مستخدم من قبل");

                product.CreatedDate = DateTime.Now;
                product.CreatedBy = "System"; // يجب تمرير المستخدم الحالي
                product.IsActive = true;
                product.IsDeleted = false;

                await _unitOfWork.Products.AddAsync(product);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إنشاء المنتج", ex);
            }
        }

        public async Task<bool> UpdateProductAsync(Product product)
        {
            try
            {
                if (product == null)
                    return false;

                var existingProduct = await GetProductByIdAsync(product.Id);
                if (existingProduct == null)
                    return false;

                // التحقق من صحة البيانات - Validate data
                if (!ValidateProductData(product, out List<string> errors))
                    throw new ArgumentException($"بيانات المنتج غير صحيحة: {string.Join(", ", errors)}");

                // التحقق من عدم تكرار كود المنتج - Check product code uniqueness
                if (!string.IsNullOrWhiteSpace(product.ProductCode) && 
                    !await IsProductCodeAvailableAsync(product.ProductCode, product.Id))
                    throw new ArgumentException("كود المنتج مستخدم من قبل");

                // التحقق من عدم تكرار الباركود - Check barcode uniqueness
                if (!string.IsNullOrWhiteSpace(product.Barcode) && 
                    !await IsBarcodeAvailableAsync(product.Barcode, product.Id))
                    throw new ArgumentException("الباركود مستخدم من قبل");

                // تحديث البيانات - Update data
                existingProduct.ProductName = product.ProductName;
                existingProduct.ProductCode = product.ProductCode;
                existingProduct.Barcode = product.Barcode;
                existingProduct.Description = product.Description;
                existingProduct.CategoryId = product.CategoryId;
                existingProduct.PurchasePrice = product.PurchasePrice;
                existingProduct.SalePrice = product.SalePrice;
                existingProduct.MinimumPrice = product.MinimumPrice;
                existingProduct.MinimumStock = product.MinimumStock;
                existingProduct.MaximumStock = product.MaximumStock;
                existingProduct.Unit = product.Unit;
                existingProduct.TaxRate = product.TaxRate;
                existingProduct.TrackInventory = product.TrackInventory;
                existingProduct.AllowNegativeStock = product.AllowNegativeStock;
                existingProduct.ImagePath = product.ImagePath;
                existingProduct.Notes = product.Notes;
                existingProduct.ModifiedDate = DateTime.Now;
                existingProduct.ModifiedBy = "System"; // يجب تمرير المستخدم الحالي

                _unitOfWork.Products.Update(existingProduct);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تحديث المنتج", ex);
            }
        }

        public async Task<bool> DeleteProductAsync(int id)
        {
            try
            {
                var product = await GetProductByIdAsync(id);
                if (product == null)
                    return false;

                if (_context != null && _unitOfWork != null)
                {
                    // Use existing context
                    var hasInvoiceItems = await _context.InvoiceItems
                        .AnyAsync(ii => ii.ProductId == id && !ii.IsDeleted);

                    if (hasInvoiceItems)
                        throw new InvalidOperationException("لا يمكن حذف المنتج لوجود فواتير مرتبطة به");

                    product.IsDeleted = true;
                    product.ModifiedDate = DateTime.Now;
                    product.ModifiedBy = "System";

                    _unitOfWork.Products.Update(product);
                    await _unitOfWork.SaveChangesAsync();
                }
                else
                {
                    // Create new context for thread-safe operation
                    using var context = new SalesDbContext();
                    using var unitOfWork = new UnitOfWork(context);

                    var hasInvoiceItems = await context.InvoiceItems
                        .AnyAsync(ii => ii.ProductId == id && !ii.IsDeleted);

                    if (hasInvoiceItems)
                        throw new InvalidOperationException("لا يمكن حذف المنتج لوجود فواتير مرتبطة به");

                    product.IsDeleted = true;
                    product.ModifiedDate = DateTime.Now;
                    product.ModifiedBy = "System";

                    unitOfWork.Products.Update(product);
                    await unitOfWork.SaveChangesAsync();
                }

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في حذف المنتج", ex);
            }
        }

        public async Task<bool> ActivateProductAsync(int id)
        {
            try
            {
                var product = await GetProductByIdAsync(id);
                if (product == null)
                    return false;

                product.IsActive = true;
                product.ModifiedDate = DateTime.Now;
                product.ModifiedBy = "System";

                _unitOfWork.Products.Update(product);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تفعيل المنتج", ex);
            }
        }

        public async Task<bool> DeactivateProductAsync(int id)
        {
            try
            {
                var product = await GetProductByIdAsync(id);
                if (product == null)
                    return false;

                product.IsActive = false;
                product.ModifiedDate = DateTime.Now;
                product.ModifiedBy = "System";

                _unitOfWork.Products.Update(product);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إلغاء تفعيل المنتج", ex);
            }
        }

        // إدارة المخزون - Inventory Management
        public async Task<bool> UpdateStockQuantityAsync(int productId, int newQuantity)
        {
            try
            {
                var result = await _productRepository.UpdateStockQuantityAsync(productId, newQuantity);
                if (result)
                    await _unitOfWork.SaveChangesAsync();
                return result;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تحديث كمية المخزون", ex);
            }
        }

        public async Task<bool> AddStockAsync(int productId, int quantity, string notes = "")
        {
            try
            {
                var result = await _productRepository.AddToStockAsync(productId, quantity);
                if (result)
                    await _unitOfWork.SaveChangesAsync();
                return result;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إضافة المخزون", ex);
            }
        }

        public async Task<bool> ReduceStockAsync(int productId, int quantity, string notes = "")
        {
            try
            {
                var result = await _productRepository.SubtractFromStockAsync(productId, quantity);
                if (result)
                    await _unitOfWork.SaveChangesAsync();
                return result;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تقليل المخزون", ex);
            }
        }

        public async Task<IEnumerable<Product>> GetLowStockProductsAsync(int? count = null)
        {
            var products = await _productRepository.GetLowStockProductsAsync();
            return count.HasValue ? products.Take(count.Value) : products;
        }

        public async Task<IEnumerable<Product>> GetOutOfStockProductsAsync()
        {
            return await _productRepository.GetOutOfStockProductsAsync();
        }

        public async Task<bool> IsStockAvailableAsync(int productId, int requiredQuantity)
        {
            var product = await GetProductByIdAsync(productId);
            if (product == null || !product.TrackInventory)
                return true;

            return product.AllowNegativeStock || product.StockQuantity >= requiredQuantity;
        }

        // إدارة الأسعار - Price Management
        public async Task<bool> UpdateProductPricesAsync(int productId, decimal purchasePrice, decimal salePrice, decimal? minimumPrice = null)
        {
            try
            {
                var result = await _productRepository.UpdateProductPriceAsync(productId, purchasePrice, salePrice, minimumPrice);
                if (result)
                    await _unitOfWork.SaveChangesAsync();
                return result;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تحديث أسعار المنتج", ex);
            }
        }

        public async Task<bool> ApplyDiscountAsync(int productId, decimal discountPercentage)
        {
            try
            {
                var product = await GetProductByIdAsync(productId);
                if (product == null)
                    return false;

                var newSalePrice = product.SalePrice * (1 - discountPercentage / 100);
                if (newSalePrice < product.MinimumPrice)
                    throw new InvalidOperationException("السعر الجديد أقل من الحد الأدنى المسموح");

                return await UpdateProductPricesAsync(productId, product.PurchasePrice, newSalePrice, product.MinimumPrice);
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تطبيق الخصم", ex);
            }
        }

        public async Task<bool> ApplyBulkPriceUpdateAsync(int categoryId, decimal priceChangePercentage)
        {
            try
            {
                var products = await GetProductsByCategoryAsync(categoryId);

                foreach (var product in products)
                {
                    var newSalePrice = product.SalePrice * (1 + priceChangePercentage / 100);
                    if (newSalePrice >= product.MinimumPrice)
                    {
                        await UpdateProductPricesAsync(product.Id, product.PurchasePrice, newSalePrice, product.MinimumPrice);
                    }
                }

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في التحديث الجماعي للأسعار", ex);
            }
        }

        // التحقق من صحة البيانات - Data Validation
        public async Task<bool> IsProductCodeAvailableAsync(string productCode, int? excludeProductId = null)
        {
            return !await _productRepository.IsProductCodeExistsAsync(productCode, excludeProductId);
        }

        public async Task<bool> IsBarcodeAvailableAsync(string barcode, int? excludeProductId = null)
        {
            return !await _productRepository.IsBarcodeExistsAsync(barcode, excludeProductId);
        }

        public bool ValidateProductData(Product product, out List<string> errors)
        {
            errors = new List<string>();

            if (product == null)
            {
                errors.Add("بيانات المنتج مطلوبة");
                return false;
            }

            if (string.IsNullOrWhiteSpace(product.ProductName))
                errors.Add("اسم المنتج مطلوب");

            if (product.ProductName?.Length > 200)
                errors.Add("اسم المنتج يجب أن يكون أقل من 200 حرف");

            if (product.CategoryId <= 0)
                errors.Add("فئة المنتج مطلوبة");

            if (product.PurchasePrice < 0)
                errors.Add("سعر الشراء لا يمكن أن يكون سالباً");

            if (product.SalePrice < 0)
                errors.Add("سعر البيع لا يمكن أن يكون سالباً");

            if (product.MinimumPrice < 0)
                errors.Add("الحد الأدنى للسعر لا يمكن أن يكون سالباً");

            if (product.SalePrice < product.MinimumPrice)
                errors.Add("سعر البيع لا يمكن أن يكون أقل من الحد الأدنى");

            if (product.MinimumStock < 0)
                errors.Add("الحد الأدنى للمخزون لا يمكن أن يكون سالباً");

            if (product.MaximumStock > 0 && product.MaximumStock < product.MinimumStock)
                errors.Add("الحد الأقصى للمخزون لا يمكن أن يكون أقل من الحد الأدنى");

            if (product.TaxRate < 0 || product.TaxRate > 100)
                errors.Add("معدل الضريبة يجب أن يكون بين 0 و 100");

            return errors.Count == 0;
        }

        // التقارير والإحصائيات - Reports and Statistics
        public async Task<int> GetTotalProductsCountAsync()
        {
            return await _productRepository.GetTotalProductCountAsync();
        }

        public async Task<int> GetActiveProductsCountAsync()
        {
            return await _context.Products
                .Where(p => !p.IsDeleted && p.IsActive)
                .CountAsync();
        }

        public async Task<int> GetProductsCountByCategoryAsync(int categoryId)
        {
            return await _productRepository.GetProductCountByCategoryAsync(categoryId);
        }

        public async Task<decimal> GetTotalInventoryValueAsync()
        {
            return await _productRepository.GetTotalInventoryValueAsync();
        }

        public async Task<IEnumerable<Product>> GetTopSellingProductsAsync(int count = 10)
        {
            return await _productRepository.GetTopSellingProductsAsync(count);
        }

        public async Task<IEnumerable<Product>> GetTopProfitableProductsAsync(int count = 10)
        {
            return await _productRepository.GetMostProfitableProductsAsync(count);
        }

        // الفئات - Categories
        public async Task<IEnumerable<Category>> GetAllCategoriesAsync()
        {
            if (_context != null)
            {
                return await _context.Categories
                    .Where(c => !c.IsDeleted)
                    .OrderBy(c => c.SortOrder)
                    .ThenBy(c => c.CategoryName)
                    .ToListAsync();
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            return await context.Categories
                .Where(c => !c.IsDeleted)
                .OrderBy(c => c.SortOrder)
                .ThenBy(c => c.CategoryName)
                .ToListAsync();
        }

        public async Task<Category?> GetCategoryByIdAsync(int id)
        {
            return await _context.Categories
                .FirstOrDefaultAsync(c => c.Id == id && !c.IsDeleted);
        }

        public async Task<bool> CreateCategoryAsync(Category category)
        {
            try
            {
                if (category == null || string.IsNullOrWhiteSpace(category.CategoryName))
                    return false;

                category.CreatedDate = DateTime.Now;
                category.CreatedBy = "System";
                category.IsActive = true;
                category.IsDeleted = false;

                await _unitOfWork.Categories.AddAsync(category);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إنشاء الفئة", ex);
            }
        }

        public async Task<bool> UpdateCategoryAsync(Category category)
        {
            try
            {
                if (category == null)
                    return false;

                var existingCategory = await GetCategoryByIdAsync(category.Id);
                if (existingCategory == null)
                    return false;

                existingCategory.CategoryName = category.CategoryName;
                existingCategory.Description = category.Description;
                existingCategory.CategoryCode = category.CategoryCode;
                existingCategory.ParentCategoryId = category.ParentCategoryId;
                existingCategory.SortOrder = category.SortOrder;
                existingCategory.ModifiedDate = DateTime.Now;
                existingCategory.ModifiedBy = "System";

                _unitOfWork.Categories.Update(existingCategory);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تحديث الفئة", ex);
            }
        }

        public async Task<bool> DeleteCategoryAsync(int id)
        {
            try
            {
                var category = await GetCategoryByIdAsync(id);
                if (category == null)
                    return false;

                // التحقق من وجود منتجات مرتبطة - Check for related products
                var hasProducts = await _context.Products
                    .AnyAsync(p => p.CategoryId == id && !p.IsDeleted);

                if (hasProducts)
                    throw new InvalidOperationException("لا يمكن حذف الفئة لوجود منتجات مرتبطة بها");

                // حذف منطقي - Soft delete
                category.IsDeleted = true;
                category.ModifiedDate = DateTime.Now;
                category.ModifiedBy = "System";

                _unitOfWork.Categories.Update(category);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في حذف الفئة", ex);
            }
        }

        // تنظيف الموارد - Dispose Resources
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (disposing)
            {
                _unitOfWork?.Dispose();
                if (_context != null)
                {
                    _context.Dispose();
                }
            }
        }
    }
}
