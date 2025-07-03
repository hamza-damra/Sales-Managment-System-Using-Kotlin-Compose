-- إنشاء قاعدة بيانات نظام إدارة المبيعات
-- Sales Management System Database Creation Script

USE master;
GO

-- إنشاء قاعدة البيانات إذا لم تكن موجودة
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'FirstDB')
BEGIN
    CREATE DATABASE [FirstDB]
    COLLATE Arabic_CI_AS;
END
GO

USE [FirstDB];
GO

-- تفعيل دعم Unicode للنصوص العربية
-- Enable Unicode support for Arabic text
ALTER DATABASE [FirstDB] SET COMPATIBILITY_LEVEL = 150;
GO

-- إنشاء جدول الأدوار - Create Roles Table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Roles' AND xtype='U')
BEGIN
    CREATE TABLE [dbo].[Roles] (
        [Id] int IDENTITY(1,1) NOT NULL,
        [RoleName] nvarchar(100) NOT NULL,
        [Description] nvarchar(500) NULL,
        [CanManageUsers] bit NOT NULL DEFAULT 0,
        [CanManageCustomers] bit NOT NULL DEFAULT 0,
        [CanManageProducts] bit NOT NULL DEFAULT 0,
        [CanCreateInvoices] bit NOT NULL DEFAULT 0,
        [CanViewReports] bit NOT NULL DEFAULT 0,
        [CanManageSettings] bit NOT NULL DEFAULT 0,
        [CreatedDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [ModifiedDate] datetime2(7) NULL,
        [CreatedBy] nvarchar(100) NULL,
        [ModifiedBy] nvarchar(100) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [IsDeleted] bit NOT NULL DEFAULT 0,
        CONSTRAINT [PK_Roles] PRIMARY KEY CLUSTERED ([Id] ASC),
        CONSTRAINT [UK_Roles_RoleName] UNIQUE NONCLUSTERED ([RoleName] ASC)
    );
END
GO

-- إنشاء جدول المستخدمين - Create Users Table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Users' AND xtype='U')
BEGIN
    CREATE TABLE [dbo].[Users] (
        [Id] int IDENTITY(1,1) NOT NULL,
        [Username] nvarchar(100) NOT NULL,
        [PasswordHash] nvarchar(255) NOT NULL,
        [FullName] nvarchar(200) NOT NULL,
        [Email] nvarchar(100) NULL,
        [Phone] nvarchar(20) NULL,
        [RoleId] int NOT NULL,
        [LastLoginDate] datetime2(7) NULL,
        [FailedLoginAttempts] int NOT NULL DEFAULT 0,
        [LockoutEndDate] datetime2(7) NULL,
        [CreatedDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [ModifiedDate] datetime2(7) NULL,
        [CreatedBy] nvarchar(100) NULL,
        [ModifiedBy] nvarchar(100) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [IsDeleted] bit NOT NULL DEFAULT 0,
        CONSTRAINT [PK_Users] PRIMARY KEY CLUSTERED ([Id] ASC),
        CONSTRAINT [UK_Users_Username] UNIQUE NONCLUSTERED ([Username] ASC),
        CONSTRAINT [FK_Users_Roles] FOREIGN KEY([RoleId]) REFERENCES [dbo].[Roles] ([Id])
    );
END
GO

-- إنشاء جدول العملاء - Create Customers Table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Customers' AND xtype='U')
BEGIN
    CREATE TABLE [dbo].[Customers] (
        [Id] int IDENTITY(1,1) NOT NULL,
        [CustomerName] nvarchar(200) NOT NULL,
        [CompanyName] nvarchar(100) NULL,
        [Phone] nvarchar(20) NULL,
        [Mobile] nvarchar(20) NULL,
        [Email] nvarchar(100) NULL,
        [Address] nvarchar(500) NULL,
        [City] nvarchar(100) NULL,
        [Country] nvarchar(100) NULL,
        [PostalCode] nvarchar(20) NULL,
        [TaxNumber] nvarchar(50) NULL,
        [CreditLimit] decimal(18,2) NOT NULL DEFAULT 0,
        [CurrentBalance] decimal(18,2) NOT NULL DEFAULT 0,
        [CustomerType] int NOT NULL DEFAULT 1,
        [Notes] nvarchar(1000) NULL,
        [CreatedDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [ModifiedDate] datetime2(7) NULL,
        [CreatedBy] nvarchar(100) NULL,
        [ModifiedBy] nvarchar(100) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [IsDeleted] bit NOT NULL DEFAULT 0,
        CONSTRAINT [PK_Customers] PRIMARY KEY CLUSTERED ([Id] ASC)
    );
END
GO

-- إنشاء جدول الفئات - Create Categories Table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Categories' AND xtype='U')
BEGIN
    CREATE TABLE [dbo].[Categories] (
        [Id] int IDENTITY(1,1) NOT NULL,
        [CategoryName] nvarchar(200) NOT NULL,
        [Description] nvarchar(500) NULL,
        [CategoryCode] nvarchar(100) NULL,
        [ParentCategoryId] int NULL,
        [SortOrder] int NOT NULL DEFAULT 0,
        [CreatedDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [ModifiedDate] datetime2(7) NULL,
        [CreatedBy] nvarchar(100) NULL,
        [ModifiedBy] nvarchar(100) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [IsDeleted] bit NOT NULL DEFAULT 0,
        CONSTRAINT [PK_Categories] PRIMARY KEY CLUSTERED ([Id] ASC),
        CONSTRAINT [FK_Categories_ParentCategory] FOREIGN KEY([ParentCategoryId]) REFERENCES [dbo].[Categories] ([Id])
    );
END
GO

-- إنشاء جدول المنتجات - Create Products Table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Products' AND xtype='U')
BEGIN
    CREATE TABLE [dbo].[Products] (
        [Id] int IDENTITY(1,1) NOT NULL,
        [ProductName] nvarchar(200) NOT NULL,
        [ProductCode] nvarchar(100) NULL,
        [Barcode] nvarchar(100) NULL,
        [Description] nvarchar(500) NULL,
        [CategoryId] int NOT NULL,
        [PurchasePrice] decimal(18,2) NOT NULL DEFAULT 0,
        [SalePrice] decimal(18,2) NOT NULL DEFAULT 0,
        [MinimumPrice] decimal(18,2) NOT NULL DEFAULT 0,
        [StockQuantity] int NOT NULL DEFAULT 0,
        [MinimumStock] int NOT NULL DEFAULT 0,
        [MaximumStock] int NOT NULL DEFAULT 0,
        [Unit] nvarchar(50) NULL DEFAULT N'قطعة',
        [TaxRate] decimal(5,2) NOT NULL DEFAULT 0,
        [TrackInventory] bit NOT NULL DEFAULT 1,
        [AllowNegativeStock] bit NOT NULL DEFAULT 0,
        [ImagePath] nvarchar(500) NULL,
        [Notes] nvarchar(1000) NULL,
        [CreatedDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [ModifiedDate] datetime2(7) NULL,
        [CreatedBy] nvarchar(100) NULL,
        [ModifiedBy] nvarchar(100) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [IsDeleted] bit NOT NULL DEFAULT 0,
        CONSTRAINT [PK_Products] PRIMARY KEY CLUSTERED ([Id] ASC),
        CONSTRAINT [UK_Products_ProductCode] UNIQUE NONCLUSTERED ([ProductCode] ASC),
        CONSTRAINT [UK_Products_Barcode] UNIQUE NONCLUSTERED ([Barcode] ASC),
        CONSTRAINT [FK_Products_Categories] FOREIGN KEY([CategoryId]) REFERENCES [dbo].[Categories] ([Id])
    );
END
GO

-- إنشاء جدول الفواتير - Create Invoices Table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Invoices' AND xtype='U')
BEGIN
    CREATE TABLE [dbo].[Invoices] (
        [Id] int IDENTITY(1,1) NOT NULL,
        [InvoiceNumber] nvarchar(50) NOT NULL,
        [InvoiceDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [DueDate] datetime2(7) NULL,
        [CustomerId] int NOT NULL,
        [UserId] int NOT NULL,
        [SubTotal] decimal(18,2) NOT NULL DEFAULT 0,
        [TaxAmount] decimal(18,2) NOT NULL DEFAULT 0,
        [DiscountAmount] decimal(18,2) NOT NULL DEFAULT 0,
        [DiscountPercentage] decimal(5,2) NOT NULL DEFAULT 0,
        [TotalAmount] decimal(18,2) NOT NULL DEFAULT 0,
        [PaidAmount] decimal(18,2) NOT NULL DEFAULT 0,
        [RemainingAmount] decimal(18,2) NOT NULL DEFAULT 0,
        [Status] int NOT NULL DEFAULT 1,
        [PaymentMethod] int NOT NULL DEFAULT 1,
        [Notes] nvarchar(1000) NULL,
        [Terms] nvarchar(500) NULL,
        [IsPrinted] bit NOT NULL DEFAULT 0,
        [PrintedDate] datetime2(7) NULL,
        [CreatedDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [ModifiedDate] datetime2(7) NULL,
        [CreatedBy] nvarchar(100) NULL,
        [ModifiedBy] nvarchar(100) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [IsDeleted] bit NOT NULL DEFAULT 0,
        CONSTRAINT [PK_Invoices] PRIMARY KEY CLUSTERED ([Id] ASC),
        CONSTRAINT [UK_Invoices_InvoiceNumber] UNIQUE NONCLUSTERED ([InvoiceNumber] ASC),
        CONSTRAINT [FK_Invoices_Customers] FOREIGN KEY([CustomerId]) REFERENCES [dbo].[Customers] ([Id]),
        CONSTRAINT [FK_Invoices_Users] FOREIGN KEY([UserId]) REFERENCES [dbo].[Users] ([Id])
    );
END
GO

-- إنشاء جدول عناصر الفاتورة - Create Invoice Items Table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='InvoiceItems' AND xtype='U')
BEGIN
    CREATE TABLE [dbo].[InvoiceItems] (
        [Id] int IDENTITY(1,1) NOT NULL,
        [InvoiceId] int NOT NULL,
        [ProductId] int NOT NULL,
        [Quantity] decimal(18,3) NOT NULL DEFAULT 1,
        [UnitPrice] decimal(18,2) NOT NULL DEFAULT 0,
        [DiscountAmount] decimal(18,2) NOT NULL DEFAULT 0,
        [DiscountPercentage] decimal(5,2) NOT NULL DEFAULT 0,
        [TaxRate] decimal(5,2) NOT NULL DEFAULT 0,
        [TaxAmount] decimal(18,2) NOT NULL DEFAULT 0,
        [LineTotal] decimal(18,2) NOT NULL DEFAULT 0,
        [Notes] nvarchar(500) NULL,
        [LineNumber] int NOT NULL DEFAULT 1,
        [CreatedDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [ModifiedDate] datetime2(7) NULL,
        [CreatedBy] nvarchar(100) NULL,
        [ModifiedBy] nvarchar(100) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [IsDeleted] bit NOT NULL DEFAULT 0,
        CONSTRAINT [PK_InvoiceItems] PRIMARY KEY CLUSTERED ([Id] ASC),
        CONSTRAINT [FK_InvoiceItems_Invoices] FOREIGN KEY([InvoiceId]) REFERENCES [dbo].[Invoices] ([Id]) ON DELETE CASCADE,
        CONSTRAINT [FK_InvoiceItems_Products] FOREIGN KEY([ProductId]) REFERENCES [dbo].[Products] ([Id])
    );
END
GO

-- إنشاء جدول المدفوعات - Create Payments Table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Payments' AND xtype='U')
BEGIN
    CREATE TABLE [dbo].[Payments] (
        [Id] int IDENTITY(1,1) NOT NULL,
        [InvoiceId] int NOT NULL,
        [Amount] decimal(18,2) NOT NULL DEFAULT 0,
        [PaymentDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [PaymentMethod] int NOT NULL DEFAULT 1,
        [ReferenceNumber] nvarchar(100) NULL,
        [Notes] nvarchar(500) NULL,
        [UserId] int NOT NULL,
        [CreatedDate] datetime2(7) NOT NULL DEFAULT GETDATE(),
        [ModifiedDate] datetime2(7) NULL,
        [CreatedBy] nvarchar(100) NULL,
        [ModifiedBy] nvarchar(100) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [IsDeleted] bit NOT NULL DEFAULT 0,
        CONSTRAINT [PK_Payments] PRIMARY KEY CLUSTERED ([Id] ASC),
        CONSTRAINT [FK_Payments_Invoices] FOREIGN KEY([InvoiceId]) REFERENCES [dbo].[Invoices] ([Id]) ON DELETE CASCADE,
        CONSTRAINT [FK_Payments_Users] FOREIGN KEY([UserId]) REFERENCES [dbo].[Users] ([Id])
    );
END
GO

-- إنشاء الفهارس - Create Indexes
CREATE NONCLUSTERED INDEX [IX_Users_RoleId] ON [dbo].[Users] ([RoleId]);
CREATE NONCLUSTERED INDEX [IX_Products_CategoryId] ON [dbo].[Products] ([CategoryId]);
CREATE NONCLUSTERED INDEX [IX_Invoices_CustomerId] ON [dbo].[Invoices] ([CustomerId]);
CREATE NONCLUSTERED INDEX [IX_Invoices_UserId] ON [dbo].[Invoices] ([UserId]);
CREATE NONCLUSTERED INDEX [IX_Invoices_InvoiceDate] ON [dbo].[Invoices] ([InvoiceDate]);
CREATE NONCLUSTERED INDEX [IX_InvoiceItems_InvoiceId] ON [dbo].[InvoiceItems] ([InvoiceId]);
CREATE NONCLUSTERED INDEX [IX_InvoiceItems_ProductId] ON [dbo].[InvoiceItems] ([ProductId]);
CREATE NONCLUSTERED INDEX [IX_Payments_InvoiceId] ON [dbo].[Payments] ([InvoiceId]);
CREATE NONCLUSTERED INDEX [IX_Payments_UserId] ON [dbo].[Payments] ([UserId]);
GO

-- إدراج البيانات الأولية - Insert Initial Data

-- إدراج الأدوار الافتراضية - Insert Default Roles
IF NOT EXISTS (SELECT 1 FROM [dbo].[Roles] WHERE [RoleName] = N'مدير النظام')
BEGIN
    INSERT INTO [dbo].[Roles] ([RoleName], [Description], [CanManageUsers], [CanManageCustomers], [CanManageProducts], [CanCreateInvoices], [CanViewReports], [CanManageSettings], [CreatedBy])
    VALUES
    (N'مدير النظام', N'مدير النظام - صلاحيات كاملة', 1, 1, 1, 1, 1, 1, N'System'),
    (N'موظف مبيعات', N'موظف مبيعات - إدارة العملاء والفواتير', 0, 1, 0, 1, 1, 0, N'System'),
    (N'مستعرض', N'مستعرض - عرض البيانات فقط', 0, 0, 0, 0, 1, 0, N'System');
END
GO

-- إدراج المستخدم الافتراضي - Insert Default User
IF NOT EXISTS (SELECT 1 FROM [dbo].[Users] WHERE [Username] = N'admin')
BEGIN
    INSERT INTO [dbo].[Users] ([Username], [PasswordHash], [FullName], [Email], [RoleId], [CreatedBy])
    VALUES (N'admin', N'$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/VjPoyNdO6', N'مدير النظام', N'admin@salesmanagement.com', 1, N'System');
END
GO

-- إدراج الفئات الافتراضية - Insert Default Categories
IF NOT EXISTS (SELECT 1 FROM [dbo].[Categories] WHERE [CategoryName] = N'عام')
BEGIN
    INSERT INTO [dbo].[Categories] ([CategoryName], [Description], [CategoryCode], [SortOrder], [CreatedBy])
    VALUES
    (N'عام', N'فئة عامة للمنتجات', N'GEN', 1, N'System'),
    (N'إلكترونيات', N'الأجهزة الإلكترونية والكهربائية', N'ELEC', 2, N'System'),
    (N'ملابس', N'الملابس والأزياء', N'CLOTH', 3, N'System'),
    (N'أغذية', N'المواد الغذائية والمشروبات', N'FOOD', 4, N'System'),
    (N'كتب وقرطاسية', N'الكتب والأدوات المكتبية', N'BOOKS', 5, N'System');
END
GO

-- إدراج عملاء تجريبيين - Insert Sample Customers
IF NOT EXISTS (SELECT 1 FROM [dbo].[Customers] WHERE [CustomerName] = N'أحمد محمد علي')
BEGIN
    INSERT INTO [dbo].[Customers] ([CustomerName], [CompanyName], [Phone], [Mobile], [Email], [Address], [City], [Country], [CustomerType], [CreditLimit], [CreatedBy])
    VALUES
    (N'أحمد محمد علي', NULL, N'011-1234567', N'0501234567', N'ahmed@email.com', N'شارع الملك فهد، حي النزهة', N'الرياض', N'السعودية', 1, 10000.00, N'System'),
    (N'شركة التقنية المتقدمة', N'شركة التقنية المتقدمة للحلول الرقمية', N'011-9876543', N'0509876543', N'info@techadvanced.com', N'طريق الملك عبدالعزيز، حي العليا', N'الرياض', N'السعودية', 2, 50000.00, N'System'),
    (N'فاطمة عبدالله', NULL, N'012-5555555', N'0555555555', N'fatima@email.com', N'شارع الأمير سلطان، حي الملز', N'الرياض', N'السعودية', 1, 5000.00, N'System'),
    (N'مؤسسة النور التجارية', N'مؤسسة النور للتجارة العامة', N'013-7777777', N'0507777777', N'info@alnoor.com', N'شارع العروبة، حي الشفا', N'الرياض', N'السعودية', 2, 25000.00, N'System');
END
GO

-- إدراج منتجات تجريبية - Insert Sample Products
IF NOT EXISTS (SELECT 1 FROM [dbo].[Products] WHERE [ProductName] = N'جهاز كمبيوتر محمول')
BEGIN
    INSERT INTO [dbo].[Products] ([ProductName], [ProductCode], [Barcode], [Description], [CategoryId], [PurchasePrice], [SalePrice], [MinimumPrice], [StockQuantity], [MinimumStock], [Unit], [TaxRate], [CreatedBy])
    VALUES
    (N'جهاز كمبيوتر محمول', N'ELEC-20241230-001', N'1234567890123', N'جهاز كمبيوتر محمول عالي الأداء', 2, 2500.00, 3500.00, 3000.00, 10, 2, N'قطعة', 15.00, N'System'),
    (N'قميص قطني رجالي', N'CLOTH-20241230-001', N'2345678901234', N'قميص قطني عالي الجودة للرجال', 3, 50.00, 120.00, 80.00, 50, 10, N'قطعة', 15.00, N'System'),
    (N'كتاب البرمجة بلغة C#', N'BOOKS-20241230-001', N'3456789012345', N'كتاب تعليمي شامل للبرمجة بلغة C#', 5, 80.00, 150.00, 120.00, 25, 5, N'قطعة', 0.00, N'System'),
    (N'عبوة أرز بسمتي', N'FOOD-20241230-001', N'4567890123456', N'أرز بسمتي فاخر - 5 كيلو', 4, 25.00, 45.00, 35.00, 100, 20, N'عبوة', 0.00, N'System'),
    (N'هاتف ذكي', N'ELEC-20241230-002', N'5678901234567', N'هاتف ذكي بمواصفات عالية', 2, 1200.00, 1800.00, 1500.00, 15, 3, N'قطعة', 15.00, N'System');
END
GO

PRINT N'تم إنشاء قاعدة البيانات وإدراج البيانات الأولية بنجاح';
PRINT N'Database created and initial data inserted successfully';
PRINT N'';
PRINT N'بيانات تسجيل الدخول الافتراضية:';
PRINT N'Default login credentials:';
PRINT N'اسم المستخدم / Username: admin';
PRINT N'كلمة المرور / Password: admin123';
GO
