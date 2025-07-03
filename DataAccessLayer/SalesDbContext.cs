using Microsoft.EntityFrameworkCore;
using DXApplication1.Models;
using DXApplication1.Utilities;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// سياق قاعدة البيانات - Database Context
    /// </summary>
    public class SalesDbContext : DbContext
    {
        public SalesDbContext() : base()
        {
        }

        public SalesDbContext(DbContextOptions<SalesDbContext> options) : base(options)
        {
        }

        // DbSets - مجموعات البيانات
        public DbSet<User> Users { get; set; }
        public DbSet<Role> Roles { get; set; }
        public DbSet<Customer> Customers { get; set; }
        public DbSet<Category> Categories { get; set; }
        public DbSet<Product> Products { get; set; }
        public DbSet<Invoice> Invoices { get; set; }
        public DbSet<InvoiceItem> InvoiceItems { get; set; }
        public DbSet<Payment> Payments { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            if (!optionsBuilder.IsConfigured)
            {
                optionsBuilder.UseSqlServer(ConfigurationManager.GetConnectionString());
            }
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // تكوين الجداول - Configure Tables
            ConfigureUserEntities(modelBuilder);
            ConfigureCustomerEntities(modelBuilder);
            ConfigureProductEntities(modelBuilder);
            ConfigureInvoiceEntities(modelBuilder);

            // إضافة البيانات الأولية - Seed Data
            SeedData(modelBuilder);
        }

        private void ConfigureUserEntities(ModelBuilder modelBuilder)
        {
            // تكوين جدول المستخدمين - Configure Users Table
            modelBuilder.Entity<User>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Username).IsRequired().HasMaxLength(100);
                entity.Property(e => e.PasswordHash).IsRequired().HasMaxLength(255);
                entity.Property(e => e.FullName).IsRequired().HasMaxLength(200);
                entity.Property(e => e.Email).HasMaxLength(100);
                entity.Property(e => e.Phone).HasMaxLength(20);
                
                entity.HasOne(e => e.Role)
                      .WithMany(r => r.Users)
                      .HasForeignKey(e => e.RoleId)
                      .OnDelete(DeleteBehavior.Restrict);

                entity.HasIndex(e => e.Username).IsUnique();
            });

            // تكوين جدول الأدوار - Configure Roles Table
            modelBuilder.Entity<Role>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.RoleName).IsRequired().HasMaxLength(100);
                entity.Property(e => e.Description).HasMaxLength(500);
                
                entity.HasIndex(e => e.RoleName).IsUnique();
            });
        }

        private void ConfigureCustomerEntities(ModelBuilder modelBuilder)
        {
            // تكوين جدول العملاء - Configure Customers Table
            modelBuilder.Entity<Customer>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.CustomerName).IsRequired().HasMaxLength(200);
                entity.Property(e => e.CompanyName).HasMaxLength(100);
                entity.Property(e => e.Phone).HasMaxLength(20);
                entity.Property(e => e.Mobile).HasMaxLength(20);
                entity.Property(e => e.Email).HasMaxLength(100);
                entity.Property(e => e.Address).HasMaxLength(500);
                entity.Property(e => e.City).HasMaxLength(100);
                entity.Property(e => e.Country).HasMaxLength(100);
                entity.Property(e => e.PostalCode).HasMaxLength(20);
                entity.Property(e => e.TaxNumber).HasMaxLength(50);
                entity.Property(e => e.Notes).HasMaxLength(1000);
                
                entity.Property(e => e.CreditLimit).HasColumnType("decimal(18,2)");
                entity.Property(e => e.CurrentBalance).HasColumnType("decimal(18,2)");
            });
        }

        private void ConfigureProductEntities(ModelBuilder modelBuilder)
        {
            // تكوين جدول الفئات - Configure Categories Table
            modelBuilder.Entity<Category>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.CategoryName).IsRequired().HasMaxLength(200);
                entity.Property(e => e.Description).HasMaxLength(500);
                entity.Property(e => e.CategoryCode).HasMaxLength(100);
                
                entity.HasOne(e => e.ParentCategory)
                      .WithMany(c => c.SubCategories)
                      .HasForeignKey(e => e.ParentCategoryId)
                      .OnDelete(DeleteBehavior.Restrict);
            });

            // تكوين جدول المنتجات - Configure Products Table
            modelBuilder.Entity<Product>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.ProductName).IsRequired().HasMaxLength(200);
                entity.Property(e => e.ProductCode).HasMaxLength(100);
                entity.Property(e => e.Barcode).HasMaxLength(100);
                entity.Property(e => e.Description).HasMaxLength(500);
                entity.Property(e => e.Unit).HasMaxLength(50);
                entity.Property(e => e.ImagePath).HasMaxLength(500);
                entity.Property(e => e.Notes).HasMaxLength(1000);
                
                entity.Property(e => e.PurchasePrice).HasColumnType("decimal(18,2)");
                entity.Property(e => e.SalePrice).HasColumnType("decimal(18,2)");
                entity.Property(e => e.MinimumPrice).HasColumnType("decimal(18,2)");
                entity.Property(e => e.TaxRate).HasColumnType("decimal(5,2)");
                
                entity.HasOne(e => e.Category)
                      .WithMany(c => c.Products)
                      .HasForeignKey(e => e.CategoryId)
                      .OnDelete(DeleteBehavior.Restrict);

                entity.HasIndex(e => e.ProductCode).IsUnique();
                entity.HasIndex(e => e.Barcode).IsUnique();
            });
        }

        private void ConfigureInvoiceEntities(ModelBuilder modelBuilder)
        {
            // تكوين جدول الفواتير - Configure Invoices Table
            modelBuilder.Entity<Invoice>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.InvoiceNumber).IsRequired().HasMaxLength(50);
                entity.Property(e => e.Notes).HasMaxLength(1000);
                entity.Property(e => e.Terms).HasMaxLength(500);
                
                entity.Property(e => e.SubTotal).HasColumnType("decimal(18,2)");
                entity.Property(e => e.TaxAmount).HasColumnType("decimal(18,2)");
                entity.Property(e => e.DiscountAmount).HasColumnType("decimal(18,2)");
                entity.Property(e => e.DiscountPercentage).HasColumnType("decimal(5,2)");
                entity.Property(e => e.TotalAmount).HasColumnType("decimal(18,2)");
                entity.Property(e => e.PaidAmount).HasColumnType("decimal(18,2)");
                entity.Property(e => e.RemainingAmount).HasColumnType("decimal(18,2)");
                
                entity.HasOne(e => e.Customer)
                      .WithMany(c => c.Invoices)
                      .HasForeignKey(e => e.CustomerId)
                      .OnDelete(DeleteBehavior.Restrict);

                entity.HasOne(e => e.User)
                      .WithMany(u => u.Invoices)
                      .HasForeignKey(e => e.UserId)
                      .OnDelete(DeleteBehavior.Restrict);

                entity.HasIndex(e => e.InvoiceNumber).IsUnique();
            });

            // تكوين جدول عناصر الفاتورة - Configure Invoice Items Table
            modelBuilder.Entity<InvoiceItem>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.Notes).HasMaxLength(500);
                
                entity.Property(e => e.Quantity).HasColumnType("decimal(18,3)");
                entity.Property(e => e.UnitPrice).HasColumnType("decimal(18,2)");
                entity.Property(e => e.DiscountAmount).HasColumnType("decimal(18,2)");
                entity.Property(e => e.DiscountPercentage).HasColumnType("decimal(5,2)");
                entity.Property(e => e.TaxRate).HasColumnType("decimal(5,2)");
                entity.Property(e => e.TaxAmount).HasColumnType("decimal(18,2)");
                entity.Property(e => e.LineTotal).HasColumnType("decimal(18,2)");
                
                entity.HasOne(e => e.Invoice)
                      .WithMany(i => i.InvoiceItems)
                      .HasForeignKey(e => e.InvoiceId)
                      .OnDelete(DeleteBehavior.Cascade);

                entity.HasOne(e => e.Product)
                      .WithMany(p => p.InvoiceItems)
                      .HasForeignKey(e => e.ProductId)
                      .OnDelete(DeleteBehavior.Restrict);
            });

            // تكوين جدول المدفوعات - Configure Payments Table
            modelBuilder.Entity<Payment>(entity =>
            {
                entity.HasKey(e => e.Id);
                entity.Property(e => e.ReferenceNumber).HasMaxLength(100);
                entity.Property(e => e.Notes).HasMaxLength(500);
                
                entity.Property(e => e.Amount).HasColumnType("decimal(18,2)");
                
                entity.HasOne(e => e.Invoice)
                      .WithMany(i => i.Payments)
                      .HasForeignKey(e => e.InvoiceId)
                      .OnDelete(DeleteBehavior.Cascade);

                entity.HasOne(e => e.User)
                      .WithMany()
                      .HasForeignKey(e => e.UserId)
                      .OnDelete(DeleteBehavior.Restrict);
            });
        }

        private void SeedData(ModelBuilder modelBuilder)
        {
            // إضافة الأدوار الافتراضية - Seed Default Roles
            modelBuilder.Entity<Role>().HasData(
                new Role
                {
                    Id = 1,
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
                    Id = 2,
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
                    Id = 3,
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
            );

            // إضافة مستخدم افتراضي - Seed Default User
            modelBuilder.Entity<User>().HasData(
                new User
                {
                    Id = 1,
                    Username = "admin",
                    PasswordHash = SecurityHelper.HashPassword("admin123"),
                    FullName = "مدير النظام",
                    Email = "admin@salesmanagement.com",
                    RoleId = 1,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                }
            );

            // إضافة فئة افتراضية - Seed Default Category
            modelBuilder.Entity<Category>().HasData(
                new Category
                {
                    Id = 1,
                    CategoryName = "عام",
                    Description = "فئة عامة للمنتجات",
                    CategoryCode = "GEN",
                    SortOrder = 1,
                    CreatedDate = DateTime.Now,
                    CreatedBy = "System"
                }
            );
        }

        public override int SaveChanges()
        {
            UpdateAuditFields();
            return base.SaveChanges();
        }

        public override async Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
        {
            UpdateAuditFields();
            return await base.SaveChangesAsync(cancellationToken);
        }

        private void UpdateAuditFields()
        {
            var entries = ChangeTracker.Entries<BaseEntity>();

            foreach (var entry in entries)
            {
                switch (entry.State)
                {
                    case EntityState.Added:
                        entry.Entity.CreatedDate = DateTime.Now;
                        entry.Entity.CreatedBy = GetCurrentUser();
                        break;
                    case EntityState.Modified:
                        entry.Entity.ModifiedDate = DateTime.Now;
                        entry.Entity.ModifiedBy = GetCurrentUser();
                        break;
                }
            }
        }

        private string GetCurrentUser()
        {
            // TODO: Implement current user logic
            return "System";
        }
    }
}
