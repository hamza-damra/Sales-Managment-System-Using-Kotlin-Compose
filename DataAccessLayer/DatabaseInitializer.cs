using Microsoft.EntityFrameworkCore;
using DXApplication1.Models;
using DXApplication1.Utilities;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// مُهيئ قاعدة البيانات - Database Initializer
    /// </summary>
    public static class DatabaseInitializer
    {
        /// <summary>
        /// تهيئة قاعدة البيانات وإنشاء البيانات الأولية
        /// Initialize database and create seed data
        /// </summary>
        public static async Task InitializeAsync(SalesDbContext context)
        {
            try
            {
                // إنشاء قاعدة البيانات إذا لم تكن موجودة - Create database if not exists
                await context.Database.EnsureCreatedAsync();

                // التحقق من وجود البيانات الأولية - Check if seed data exists
                if (await context.Roles.AnyAsync())
                {
                    return; // البيانات موجودة بالفعل - Data already exists
                }

                // إضافة البيانات الأولية - Add seed data
                await SeedRolesAsync(context);
                await SeedUsersAsync(context);
                await SeedCategoriesAsync(context);
                await SeedCustomersAsync(context);
                await SeedProductsAsync(context);

                await context.SaveChangesAsync();
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تهيئة قاعدة البيانات", ex);
            }
        }

        private static async Task SeedRolesAsync(SalesDbContext context)
        {
            var roles = new[]
            {
                new Role
                {
                    RoleName = DefaultRoles.Admin,
                    Description = "مدير النظام - صلاحيات كاملة",
                    CanManageUsers = true,
                    CanManageCustomers = true,
                    CanManageProducts = true,
                    CanCreateInvoices = true,
                    CanViewReports = true,
                    CanManageSettings = true,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Role
                {
                    RoleName = DefaultRoles.Salesperson,
                    Description = "موظف مبيعات - إدارة العملاء والفواتير",
                    CanManageUsers = false,
                    CanManageCustomers = true,
                    CanManageProducts = false,
                    CanCreateInvoices = true,
                    CanViewReports = true,
                    CanManageSettings = false,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Role
                {
                    RoleName = DefaultRoles.Viewer,
                    Description = "مستعرض - عرض البيانات فقط",
                    CanManageUsers = false,
                    CanManageCustomers = false,
                    CanManageProducts = false,
                    CanCreateInvoices = false,
                    CanViewReports = true,
                    CanManageSettings = false,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                }
            };

            await context.Roles.AddRangeAsync(roles);
        }

        private static async Task SeedUsersAsync(SalesDbContext context)
        {
            var adminRole = await context.Roles.FirstAsync(r => r.RoleName == DefaultRoles.Admin);

            var users = new[]
            {
                new User
                {
                    Username = "admin",
                    PasswordHash = SecurityHelper.HashPassword("admin123"),
                    FullName = "مدير النظام",
                    Email = "admin@salesmanagement.com",
                    RoleId = adminRole.Id,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                }
            };

            await context.Users.AddRangeAsync(users);
        }

        private static async Task SeedCategoriesAsync(SalesDbContext context)
        {
            var categories = new[]
            {
                new Category
                {
                    CategoryName = "عام",
                    Description = "فئة عامة للمنتجات",
                    CategoryCode = "GEN",
                    SortOrder = 1,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Category
                {
                    CategoryName = "إلكترونيات",
                    Description = "الأجهزة الإلكترونية والكهربائية",
                    CategoryCode = "ELEC",
                    SortOrder = 2,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Category
                {
                    CategoryName = "ملابس",
                    Description = "الملابس والأزياء",
                    CategoryCode = "CLOTH",
                    SortOrder = 3,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Category
                {
                    CategoryName = "أغذية",
                    Description = "المواد الغذائية والمشروبات",
                    CategoryCode = "FOOD",
                    SortOrder = 4,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Category
                {
                    CategoryName = "كتب وقرطاسية",
                    Description = "الكتب والأدوات المكتبية",
                    CategoryCode = "BOOKS",
                    SortOrder = 5,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                }
            };

            await context.Categories.AddRangeAsync(categories);
        }

        private static async Task SeedCustomersAsync(SalesDbContext context)
        {
            var customers = new[]
            {
                new Customer
                {
                    CustomerName = "أحمد محمد علي",
                    Phone = "011-1234567",
                    Mobile = "0501234567",
                    Email = "ahmed@email.com",
                    Address = "شارع الملك فهد، حي النزهة",
                    City = "الرياض",
                    Country = "السعودية",
                    CustomerType = CustomerType.Individual,
                    CreditLimit = 10000.00m,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Customer
                {
                    CustomerName = "شركة التقنية المتقدمة",
                    CompanyName = "شركة التقنية المتقدمة للحلول الرقمية",
                    Phone = "011-9876543",
                    Mobile = "0509876543",
                    Email = "info@techadvanced.com",
                    Address = "طريق الملك عبدالعزيز، حي العليا",
                    City = "الرياض",
                    Country = "السعودية",
                    CustomerType = CustomerType.Company,
                    CreditLimit = 50000.00m,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Customer
                {
                    CustomerName = "فاطمة عبدالله",
                    Phone = "012-5555555",
                    Mobile = "0555555555",
                    Email = "fatima@email.com",
                    Address = "شارع الأمير سلطان، حي الملز",
                    City = "الرياض",
                    Country = "السعودية",
                    CustomerType = CustomerType.Individual,
                    CreditLimit = 5000.00m,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                }
            };

            await context.Customers.AddRangeAsync(customers);
        }

        private static async Task SeedProductsAsync(SalesDbContext context)
        {
            var electronicsCategory = await context.Categories.FirstAsync(c => c.CategoryCode == "ELEC");
            var clothingCategory = await context.Categories.FirstAsync(c => c.CategoryCode == "CLOTH");
            var booksCategory = await context.Categories.FirstAsync(c => c.CategoryCode == "BOOKS");
            var foodCategory = await context.Categories.FirstAsync(c => c.CategoryCode == "FOOD");

            var products = new[]
            {
                new Product
                {
                    ProductName = "جهاز كمبيوتر محمول",
                    ProductCode = "ELEC-001",
                    Barcode = "1234567890123",
                    Description = "جهاز كمبيوتر محمول عالي الأداء",
                    CategoryId = electronicsCategory.Id,
                    PurchasePrice = 2500.00m,
                    SalePrice = 3500.00m,
                    MinimumPrice = 3000.00m,
                    StockQuantity = 10,
                    MinimumStock = 2,
                    Unit = "قطعة",
                    TaxRate = 15.00m,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Product
                {
                    ProductName = "قميص قطني رجالي",
                    ProductCode = "CLOTH-001",
                    Barcode = "2345678901234",
                    Description = "قميص قطني عالي الجودة للرجال",
                    CategoryId = clothingCategory.Id,
                    PurchasePrice = 50.00m,
                    SalePrice = 120.00m,
                    MinimumPrice = 80.00m,
                    StockQuantity = 50,
                    MinimumStock = 10,
                    Unit = "قطعة",
                    TaxRate = 15.00m,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Product
                {
                    ProductName = "كتاب البرمجة بلغة C#",
                    ProductCode = "BOOKS-001",
                    Barcode = "3456789012345",
                    Description = "كتاب تعليمي شامل للبرمجة بلغة C#",
                    CategoryId = booksCategory.Id,
                    PurchasePrice = 80.00m,
                    SalePrice = 150.00m,
                    MinimumPrice = 120.00m,
                    StockQuantity = 25,
                    MinimumStock = 5,
                    Unit = "قطعة",
                    TaxRate = 0.00m,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                },
                new Product
                {
                    ProductName = "عبوة أرز بسمتي",
                    ProductCode = "FOOD-001",
                    Barcode = "4567890123456",
                    Description = "أرز بسمتي فاخر - 5 كيلو",
                    CategoryId = foodCategory.Id,
                    PurchasePrice = 25.00m,
                    SalePrice = 45.00m,
                    MinimumPrice = 35.00m,
                    StockQuantity = 100,
                    MinimumStock = 20,
                    Unit = "عبوة",
                    TaxRate = 0.00m,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                }
            };

            await context.Products.AddRangeAsync(products);
        }
    }
}
