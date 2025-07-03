using DXApplication1.Models;
using DXApplication1.DataAccessLayer;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Text.RegularExpressions;

namespace DXApplication1.BusinessLogicLayer
{
    /// <summary>
    /// خدمة التحقق من صحة بيانات المنتجات - Product Validation Service
    /// </summary>
    public class ProductValidationService : IProductValidationService
    {
        private readonly SalesDbContext _context;

        public ProductValidationService(SalesDbContext context)
        {
            _context = context ?? throw new ArgumentNullException(nameof(context));
        }

        public ProductValidationService()
        {
            // For thread-safe operations
        }

        /// <summary>
        /// التحقق الشامل من صحة بيانات المنتج - Comprehensive product data validation
        /// </summary>
        public async Task<ValidationResult> ValidateProductAsync(Product product, bool isUpdate = false)
        {
            var result = new ValidationResult();

            try
            {
                // التحقق من البيانات الأساسية - Basic data validation
                ValidateBasicData(product, result);

                // التحقق من بيانات الأسعار - Price validation
                ValidatePricing(product, result);

                // التحقق من بيانات المخزون - Inventory validation
                ValidateInventory(product, result);

                // التحقق من القيود الفريدة - Unique constraints validation
                await ValidateUniqueConstraintsAsync(product, result, isUpdate);

                // التحقق من الفئة - Category validation
                await ValidateCategoryAsync(product, result);

                // التحقق من قواعد العمل - Business rules validation
                ValidateBusinessRules(product, result);

            }
            catch (Exception ex)
            {
                result.AddError("خطأ في التحقق من صحة البيانات", ex.Message);
            }

            return result;
        }

        /// <summary>
        /// التحقق من البيانات الأساسية - Basic data validation
        /// </summary>
        private void ValidateBasicData(Product product, ValidationResult result)
        {
            // اسم المنتج - Product name
            if (string.IsNullOrWhiteSpace(product.ProductName))
            {
                result.AddError("اسم المنتج", "اسم المنتج مطلوب");
            }
            else if (product.ProductName.Length > 200)
            {
                result.AddError("اسم المنتج", "اسم المنتج لا يمكن أن يزيد عن 200 حرف");
            }

            // كود المنتج - Product code
            if (!string.IsNullOrWhiteSpace(product.ProductCode))
            {
                if (product.ProductCode.Length > 100)
                {
                    result.AddError("كود المنتج", "كود المنتج لا يمكن أن يزيد عن 100 حرف");
                }

                if (!IsValidProductCode(product.ProductCode))
                {
                    result.AddError("كود المنتج", "كود المنتج يجب أن يحتوي على أحرف وأرقام فقط");
                }
            }

            // الباركود - Barcode
            if (!string.IsNullOrWhiteSpace(product.Barcode))
            {
                if (product.Barcode.Length > 100)
                {
                    result.AddError("الباركود", "الباركود لا يمكن أن يزيد عن 100 حرف");
                }

                if (!IsValidBarcode(product.Barcode))
                {
                    result.AddError("الباركود", "تنسيق الباركود غير صحيح");
                }
            }

            // الوصف - Description
            if (!string.IsNullOrWhiteSpace(product.Description) && product.Description.Length > 500)
            {
                result.AddError("الوصف", "الوصف لا يمكن أن يزيد عن 500 حرف");
            }

            // الوحدة - Unit
            if (!string.IsNullOrWhiteSpace(product.Unit) && product.Unit.Length > 50)
            {
                result.AddError("الوحدة", "الوحدة لا يمكن أن تزيد عن 50 حرف");
            }

            // الملاحظات - Notes
            if (!string.IsNullOrWhiteSpace(product.Notes) && product.Notes.Length > 1000)
            {
                result.AddError("الملاحظات", "الملاحظات لا يمكن أن تزيد عن 1000 حرف");
            }
        }

        /// <summary>
        /// التحقق من بيانات الأسعار - Price validation
        /// </summary>
        private void ValidatePricing(Product product, ValidationResult result)
        {
            // سعر الشراء - Purchase price
            if (product.PurchasePrice < 0)
            {
                result.AddError("سعر الشراء", "سعر الشراء لا يمكن أن يكون سالباً");
            }

            // سعر البيع - Sale price
            if (product.SalePrice < 0)
            {
                result.AddError("سعر البيع", "سعر البيع لا يمكن أن يكون سالباً");
            }

            if (product.SalePrice == 0)
            {
                result.AddWarning("سعر البيع", "سعر البيع لم يتم تحديده");
            }

            // الحد الأدنى للسعر - Minimum price
            if (product.MinimumPrice < 0)
            {
                result.AddError("الحد الأدنى للسعر", "الحد الأدنى للسعر لا يمكن أن يكون سالباً");
            }

            if (product.MinimumPrice > product.SalePrice && product.SalePrice > 0)
            {
                result.AddError("الحد الأدنى للسعر", "الحد الأدنى للسعر لا يمكن أن يكون أكبر من سعر البيع");
            }

            // معدل الضريبة - Tax rate
            if (product.TaxRate < 0 || product.TaxRate > 100)
            {
                result.AddError("معدل الضريبة", "معدل الضريبة يجب أن يكون بين 0 و 100");
            }

            // التحقق من هامش الربح - Profit margin validation
            if (product.PurchasePrice > 0 && product.SalePrice > 0)
            {
                var profitMargin = ((product.SalePrice - product.PurchasePrice) / product.PurchasePrice) * 100;
                if (profitMargin < 0)
                {
                    result.AddWarning("هامش الربح", "سعر البيع أقل من سعر الشراء (خسارة)");
                }
                else if (profitMargin < 10)
                {
                    result.AddWarning("هامش الربح", "هامش الربح منخفض (أقل من 10%)");
                }
            }
        }

        /// <summary>
        /// التحقق من بيانات المخزون - Inventory validation
        /// </summary>
        private void ValidateInventory(Product product, ValidationResult result)
        {
            // الحد الأدنى للمخزون - Minimum stock
            if (product.MinimumStock < 0)
            {
                result.AddError("الحد الأدنى للمخزون", "الحد الأدنى للمخزون لا يمكن أن يكون سالباً");
            }

            // الحد الأقصى للمخزون - Maximum stock
            if (product.MaximumStock < 0)
            {
                result.AddError("الحد الأقصى للمخزون", "الحد الأقصى للمخزون لا يمكن أن يكون سالباً");
            }

            if (product.MaximumStock > 0 && product.MaximumStock < product.MinimumStock)
            {
                result.AddError("الحد الأقصى للمخزون", "الحد الأقصى للمخزون لا يمكن أن يكون أقل من الحد الأدنى");
            }

            // الكمية الحالية - Current stock
            if (product.TrackInventory && !product.AllowNegativeStock && product.StockQuantity < 0)
            {
                result.AddError("الكمية الحالية", "الكمية الحالية لا يمكن أن تكون سالبة");
            }

            // تحذير المخزون المنخفض - Low stock warning
            if (product.TrackInventory && product.StockQuantity <= product.MinimumStock && product.MinimumStock > 0)
            {
                result.AddWarning("المخزون", "كمية المخزون أقل من أو تساوي الحد الأدنى");
            }
        }

        /// <summary>
        /// التحقق من القيود الفريدة - Unique constraints validation
        /// </summary>
        private async Task ValidateUniqueConstraintsAsync(Product product, ValidationResult result, bool isUpdate)
        {
            using var context = _context ?? new SalesDbContext();

            // التحقق من كود المنتج - Product code uniqueness
            if (!string.IsNullOrWhiteSpace(product.ProductCode))
            {
                var query = context.Products.Where(p => p.ProductCode == product.ProductCode && !p.IsDeleted);
                if (isUpdate)
                {
                    query = query.Where(p => p.Id != product.Id);
                }

                if (await query.AnyAsync())
                {
                    result.AddError("كود المنتج", "كود المنتج مستخدم من قبل");
                }
            }

            // التحقق من الباركود - Barcode uniqueness
            if (!string.IsNullOrWhiteSpace(product.Barcode))
            {
                var query = context.Products.Where(p => p.Barcode == product.Barcode && !p.IsDeleted);
                if (isUpdate)
                {
                    query = query.Where(p => p.Id != product.Id);
                }

                if (await query.AnyAsync())
                {
                    result.AddError("الباركود", "الباركود مستخدم من قبل");
                }
            }
        }

        /// <summary>
        /// التحقق من الفئة - Category validation
        /// </summary>
        private async Task ValidateCategoryAsync(Product product, ValidationResult result)
        {
            using var context = _context ?? new SalesDbContext();

            if (product.CategoryId <= 0)
            {
                result.AddError("الفئة", "يجب اختيار فئة للمنتج");
                return;
            }

            var category = await context.Categories
                .FirstOrDefaultAsync(c => c.Id == product.CategoryId && !c.IsDeleted);

            if (category == null)
            {
                result.AddError("الفئة", "الفئة المحددة غير موجودة");
            }
            else if (!category.IsActive)
            {
                result.AddError("الفئة", "الفئة المحددة غير نشطة");
            }
        }

        /// <summary>
        /// التحقق من قواعد العمل - Business rules validation
        /// </summary>
        private void ValidateBusinessRules(Product product, ValidationResult result)
        {
            // قاعدة: المنتجات التي تتبع المخزون يجب أن تحدد الحد الأدنى
            if (product.TrackInventory && product.MinimumStock == 0)
            {
                result.AddWarning("إدارة المخزون", "يُنصح بتحديد الحد الأدنى للمخزون للمنتجات التي تتبع المخزون");
            }

            // قاعدة: المنتجات ذات السعر العالي يجب أن تحدد الحد الأدنى للسعر
            if (product.SalePrice > 1000 && product.MinimumPrice == 0)
            {
                result.AddWarning("الأسعار", "يُنصح بتحديد الحد الأدنى للسعر للمنتجات ذات السعر العالي");
            }

            // قاعدة: التحقق من مسار الصورة
            if (!string.IsNullOrWhiteSpace(product.ImagePath) && !IsValidImagePath(product.ImagePath))
            {
                result.AddWarning("مسار الصورة", "مسار الصورة قد لا يكون صحيحاً");
            }
        }

        /// <summary>
        /// التحقق من صحة كود المنتج - Validate product code format
        /// </summary>
        private bool IsValidProductCode(string productCode)
        {
            // يجب أن يحتوي على أحرف وأرقام فقط
            return Regex.IsMatch(productCode, @"^[a-zA-Z0-9\-_]+$");
        }

        /// <summary>
        /// التحقق من صحة الباركود - Validate barcode format
        /// </summary>
        private bool IsValidBarcode(string barcode)
        {
            // التحقق من أطوال الباركود الشائعة
            var validLengths = new[] { 8, 12, 13, 14 };
            if (!validLengths.Contains(barcode.Length))
                return false;

            // يجب أن يحتوي على أرقام فقط
            return Regex.IsMatch(barcode, @"^\d+$");
        }

        /// <summary>
        /// التحقق من صحة مسار الصورة - Validate image path
        /// </summary>
        private bool IsValidImagePath(string imagePath)
        {
            try
            {
                var validExtensions = new[] { ".jpg", ".jpeg", ".png", ".bmp", ".gif" };
                var extension = Path.GetExtension(imagePath).ToLower();
                return validExtensions.Contains(extension);
            }
            catch
            {
                return false;
            }
        }
    }

    /// <summary>
    /// واجهة خدمة التحقق من صحة المنتجات - Product Validation Service Interface
    /// </summary>
    public interface IProductValidationService
    {
        Task<ValidationResult> ValidateProductAsync(Product product, bool isUpdate = false);
    }

    /// <summary>
    /// نتيجة التحقق من صحة البيانات - Validation Result
    /// </summary>
    public class ValidationResult
    {
        public bool IsValid => !Errors.Any();
        public List<ValidationError> Errors { get; } = new List<ValidationError>();
        public List<ValidationError> Warnings { get; } = new List<ValidationError>();

        public void AddError(string field, string message)
        {
            Errors.Add(new ValidationError { Field = field, Message = message, Type = ValidationErrorType.Error });
        }

        public void AddWarning(string field, string message)
        {
            Warnings.Add(new ValidationError { Field = field, Message = message, Type = ValidationErrorType.Warning });
        }

        public string GetErrorsAsString()
        {
            return string.Join("\n", Errors.Select(e => $"{e.Field}: {e.Message}"));
        }

        public string GetWarningsAsString()
        {
            return string.Join("\n", Warnings.Select(w => $"{w.Field}: {w.Message}"));
        }
    }

    /// <summary>
    /// خطأ التحقق من صحة البيانات - Validation Error
    /// </summary>
    public class ValidationError
    {
        public string Field { get; set; } = string.Empty;
        public string Message { get; set; } = string.Empty;
        public ValidationErrorType Type { get; set; }
    }

    /// <summary>
    /// نوع خطأ التحقق - Validation Error Type
    /// </summary>
    public enum ValidationErrorType
    {
        Error,
        Warning
    }
}
