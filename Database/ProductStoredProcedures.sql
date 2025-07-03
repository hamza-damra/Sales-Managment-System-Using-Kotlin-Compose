-- =============================================
-- Product Management Stored Procedures
-- إجراءات مخزنة لإدارة المنتجات
-- =============================================

USE [SalesDB]
GO

-- =============================================
-- إجراء إضافة منتج جديد - Create Product Procedure
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_CreateProduct]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_CreateProduct]
GO

CREATE PROCEDURE [dbo].[sp_CreateProduct]
    @ProductName NVARCHAR(200),
    @ProductCode NVARCHAR(100) = NULL,
    @Barcode NVARCHAR(100) = NULL,
    @Description NVARCHAR(500) = NULL,
    @CategoryId INT,
    @PurchasePrice DECIMAL(18,2) = 0,
    @SalePrice DECIMAL(18,2) = 0,
    @MinimumPrice DECIMAL(18,2) = 0,
    @StockQuantity INT = 0,
    @MinimumStock INT = 0,
    @MaximumStock INT = 0,
    @Unit NVARCHAR(50) = N'قطعة',
    @TaxRate DECIMAL(5,2) = 0,
    @TrackInventory BIT = 1,
    @AllowNegativeStock BIT = 0,
    @ImagePath NVARCHAR(500) = NULL,
    @Notes NVARCHAR(1000) = NULL,
    @CreatedBy NVARCHAR(100) = 'System',
    @ProductId INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- التحقق من صحة البيانات - Validate data
        IF @ProductName IS NULL OR LEN(TRIM(@ProductName)) = 0
        BEGIN
            RAISERROR(N'اسم المنتج مطلوب', 16, 1);
            RETURN;
        END
        
        -- التحقق من وجود الفئة - Check category exists
        IF NOT EXISTS (SELECT 1 FROM Categories WHERE Id = @CategoryId AND IsDeleted = 0)
        BEGIN
            RAISERROR(N'الفئة المحددة غير موجودة', 16, 1);
            RETURN;
        END
        
        -- التحقق من عدم تكرار كود المنتج - Check product code uniqueness
        IF @ProductCode IS NOT NULL AND EXISTS (SELECT 1 FROM Products WHERE ProductCode = @ProductCode AND IsDeleted = 0)
        BEGIN
            RAISERROR(N'كود المنتج مستخدم من قبل', 16, 1);
            RETURN;
        END
        
        -- التحقق من عدم تكرار الباركود - Check barcode uniqueness
        IF @Barcode IS NOT NULL AND EXISTS (SELECT 1 FROM Products WHERE Barcode = @Barcode AND IsDeleted = 0)
        BEGIN
            RAISERROR(N'الباركود مستخدم من قبل', 16, 1);
            RETURN;
        END
        
        -- إدراج المنتج الجديد - Insert new product
        INSERT INTO Products (
            ProductName, ProductCode, Barcode, Description, CategoryId,
            PurchasePrice, SalePrice, MinimumPrice, StockQuantity, MinimumStock, MaximumStock,
            Unit, TaxRate, TrackInventory, AllowNegativeStock, ImagePath, Notes,
            CreatedDate, CreatedBy, IsActive, IsDeleted
        )
        VALUES (
            @ProductName, @ProductCode, @Barcode, @Description, @CategoryId,
            @PurchasePrice, @SalePrice, @MinimumPrice, @StockQuantity, @MinimumStock, @MaximumStock,
            @Unit, @TaxRate, @TrackInventory, @AllowNegativeStock, @ImagePath, @Notes,
            GETDATE(), @CreatedBy, 1, 0
        );
        
        SET @ProductId = SCOPE_IDENTITY();
        
        COMMIT TRANSACTION;
        
        SELECT @ProductId as ProductId, N'تم إضافة المنتج بنجاح' as Message;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
            
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrorMessage, 16, 1);
    END CATCH
END
GO

-- =============================================
-- إجراء تحديث منتج - Update Product Procedure
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_UpdateProduct]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_UpdateProduct]
GO

CREATE PROCEDURE [dbo].[sp_UpdateProduct]
    @ProductId INT,
    @ProductName NVARCHAR(200),
    @ProductCode NVARCHAR(100) = NULL,
    @Barcode NVARCHAR(100) = NULL,
    @Description NVARCHAR(500) = NULL,
    @CategoryId INT,
    @PurchasePrice DECIMAL(18,2) = 0,
    @SalePrice DECIMAL(18,2) = 0,
    @MinimumPrice DECIMAL(18,2) = 0,
    @StockQuantity INT = 0,
    @MinimumStock INT = 0,
    @MaximumStock INT = 0,
    @Unit NVARCHAR(50) = N'قطعة',
    @TaxRate DECIMAL(5,2) = 0,
    @TrackInventory BIT = 1,
    @AllowNegativeStock BIT = 0,
    @ImagePath NVARCHAR(500) = NULL,
    @Notes NVARCHAR(1000) = NULL,
    @IsActive BIT = 1,
    @ModifiedBy NVARCHAR(100) = 'System'
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- التحقق من وجود المنتج - Check product exists
        IF NOT EXISTS (SELECT 1 FROM Products WHERE Id = @ProductId AND IsDeleted = 0)
        BEGIN
            RAISERROR(N'المنتج غير موجود', 16, 1);
            RETURN;
        END
        
        -- التحقق من صحة البيانات - Validate data
        IF @ProductName IS NULL OR LEN(TRIM(@ProductName)) = 0
        BEGIN
            RAISERROR(N'اسم المنتج مطلوب', 16, 1);
            RETURN;
        END
        
        -- التحقق من وجود الفئة - Check category exists
        IF NOT EXISTS (SELECT 1 FROM Categories WHERE Id = @CategoryId AND IsDeleted = 0)
        BEGIN
            RAISERROR(N'الفئة المحددة غير موجودة', 16, 1);
            RETURN;
        END
        
        -- التحقق من عدم تكرار كود المنتج - Check product code uniqueness
        IF @ProductCode IS NOT NULL AND EXISTS (
            SELECT 1 FROM Products 
            WHERE ProductCode = @ProductCode AND Id != @ProductId AND IsDeleted = 0
        )
        BEGIN
            RAISERROR(N'كود المنتج مستخدم من قبل', 16, 1);
            RETURN;
        END
        
        -- التحقق من عدم تكرار الباركود - Check barcode uniqueness
        IF @Barcode IS NOT NULL AND EXISTS (
            SELECT 1 FROM Products 
            WHERE Barcode = @Barcode AND Id != @ProductId AND IsDeleted = 0
        )
        BEGIN
            RAISERROR(N'الباركود مستخدم من قبل', 16, 1);
            RETURN;
        END
        
        -- تحديث المنتج - Update product
        UPDATE Products SET
            ProductName = @ProductName,
            ProductCode = @ProductCode,
            Barcode = @Barcode,
            Description = @Description,
            CategoryId = @CategoryId,
            PurchasePrice = @PurchasePrice,
            SalePrice = @SalePrice,
            MinimumPrice = @MinimumPrice,
            StockQuantity = @StockQuantity,
            MinimumStock = @MinimumStock,
            MaximumStock = @MaximumStock,
            Unit = @Unit,
            TaxRate = @TaxRate,
            TrackInventory = @TrackInventory,
            AllowNegativeStock = @AllowNegativeStock,
            ImagePath = @ImagePath,
            Notes = @Notes,
            IsActive = @IsActive,
            ModifiedDate = GETDATE(),
            ModifiedBy = @ModifiedBy
        WHERE Id = @ProductId;
        
        COMMIT TRANSACTION;
        
        SELECT N'تم تحديث المنتج بنجاح' as Message;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
            
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrorMessage, 16, 1);
    END CATCH
END
GO

-- =============================================
-- إجراء حذف منتج - Delete Product Procedure
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_DeleteProduct]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_DeleteProduct]
GO

CREATE PROCEDURE [dbo].[sp_DeleteProduct]
    @ProductId INT,
    @ModifiedBy NVARCHAR(100) = 'System'
AS
BEGIN
    SET NOCOUNT ON;
    
    BEGIN TRY
        BEGIN TRANSACTION;
        
        -- التحقق من وجود المنتج - Check product exists
        IF NOT EXISTS (SELECT 1 FROM Products WHERE Id = @ProductId AND IsDeleted = 0)
        BEGIN
            RAISERROR(N'المنتج غير موجود', 16, 1);
            RETURN;
        END
        
        -- التحقق من وجود فواتير مرتبطة - Check for related invoices
        IF EXISTS (SELECT 1 FROM InvoiceItems WHERE ProductId = @ProductId)
        BEGIN
            RAISERROR(N'لا يمكن حذف المنتج لوجود فواتير مرتبطة به', 16, 1);
            RETURN;
        END
        
        -- حذف منطقي للمنتج - Soft delete product
        UPDATE Products SET
            IsDeleted = 1,
            ModifiedDate = GETDATE(),
            ModifiedBy = @ModifiedBy
        WHERE Id = @ProductId;
        
        COMMIT TRANSACTION;
        
        SELECT N'تم حذف المنتج بنجاح' as Message;
        
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
            
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrorMessage, 16, 1);
    END CATCH
END
GO

-- =============================================
-- إجراء البحث في المنتجات - Search Products Procedure
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_SearchProducts]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_SearchProducts]
GO

CREATE PROCEDURE [dbo].[sp_SearchProducts]
    @SearchTerm NVARCHAR(200),
    @CategoryId INT = NULL,
    @IsActive BIT = NULL,
    @PageNumber INT = 1,
    @PageSize INT = 50
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @Offset INT = (@PageNumber - 1) * @PageSize;
    
    SELECT 
        p.Id,
        p.ProductName,
        p.ProductCode,
        p.Barcode,
        p.Description,
        p.CategoryId,
        c.CategoryName,
        p.PurchasePrice,
        p.SalePrice,
        p.MinimumPrice,
        p.StockQuantity,
        p.MinimumStock,
        p.MaximumStock,
        p.Unit,
        p.TaxRate,
        p.TrackInventory,
        p.AllowNegativeStock,
        p.ImagePath,
        p.Notes,
        p.IsActive,
        p.CreatedDate,
        p.ModifiedDate
    FROM Products p
    INNER JOIN Categories c ON p.CategoryId = c.Id
    WHERE p.IsDeleted = 0
        AND (@SearchTerm IS NULL OR @SearchTerm = '' OR 
             p.ProductName LIKE '%' + @SearchTerm + '%' OR
             p.ProductCode LIKE '%' + @SearchTerm + '%' OR
             p.Barcode LIKE '%' + @SearchTerm + '%' OR
             p.Description LIKE '%' + @SearchTerm + '%')
        AND (@CategoryId IS NULL OR p.CategoryId = @CategoryId)
        AND (@IsActive IS NULL OR p.IsActive = @IsActive)
    ORDER BY p.ProductName
    OFFSET @Offset ROWS
    FETCH NEXT @PageSize ROWS ONLY;
    
    -- إرجاع العدد الإجمالي - Return total count
    SELECT COUNT(*) as TotalCount
    FROM Products p
    WHERE p.IsDeleted = 0
        AND (@SearchTerm IS NULL OR @SearchTerm = '' OR 
             p.ProductName LIKE '%' + @SearchTerm + '%' OR
             p.ProductCode LIKE '%' + @SearchTerm + '%' OR
             p.Barcode LIKE '%' + @SearchTerm + '%' OR
             p.Description LIKE '%' + @SearchTerm + '%')
        AND (@CategoryId IS NULL OR p.CategoryId = @CategoryId)
        AND (@IsActive IS NULL OR p.IsActive = @IsActive);
END
GO

-- =============================================
-- إجراء تحديث المخزون - Update Stock Procedure
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_UpdateProductStock]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_UpdateProductStock]
GO

CREATE PROCEDURE [dbo].[sp_UpdateProductStock]
    @ProductId INT,
    @NewQuantity INT,
    @ModifiedBy NVARCHAR(100) = 'System'
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- التحقق من وجود المنتج - Check product exists
        IF NOT EXISTS (SELECT 1 FROM Products WHERE Id = @ProductId AND IsDeleted = 0)
        BEGIN
            RAISERROR(N'المنتج غير موجود', 16, 1);
            RETURN;
        END

        -- تحديث كمية المخزون - Update stock quantity
        UPDATE Products SET
            StockQuantity = @NewQuantity,
            ModifiedDate = GETDATE(),
            ModifiedBy = @ModifiedBy
        WHERE Id = @ProductId;

        COMMIT TRANSACTION;

        SELECT N'تم تحديث المخزون بنجاح' as Message;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        RAISERROR(@ErrorMessage, 16, 1);
    END CATCH
END
GO

-- =============================================
-- إجراء الحصول على المنتجات قليلة المخزون - Get Low Stock Products
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_GetLowStockProducts]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_GetLowStockProducts]
GO

CREATE PROCEDURE [dbo].[sp_GetLowStockProducts]
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        p.Id,
        p.ProductName,
        p.ProductCode,
        p.Barcode,
        p.CategoryId,
        c.CategoryName,
        p.StockQuantity,
        p.MinimumStock,
        p.Unit,
        p.SalePrice,
        (p.MinimumStock - p.StockQuantity) as ShortageQuantity
    FROM Products p
    INNER JOIN Categories c ON p.CategoryId = c.Id
    WHERE p.IsDeleted = 0
        AND p.IsActive = 1
        AND p.TrackInventory = 1
        AND p.StockQuantity <= p.MinimumStock
    ORDER BY (p.MinimumStock - p.StockQuantity) DESC, p.ProductName;
END
GO

-- =============================================
-- إجراء الحصول على المنتجات نافدة المخزون - Get Out of Stock Products
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_GetOutOfStockProducts]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_GetOutOfStockProducts]
GO

CREATE PROCEDURE [dbo].[sp_GetOutOfStockProducts]
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        p.Id,
        p.ProductName,
        p.ProductCode,
        p.Barcode,
        p.CategoryId,
        c.CategoryName,
        p.StockQuantity,
        p.MinimumStock,
        p.Unit,
        p.SalePrice
    FROM Products p
    INNER JOIN Categories c ON p.CategoryId = c.Id
    WHERE p.IsDeleted = 0
        AND p.IsActive = 1
        AND p.TrackInventory = 1
        AND p.StockQuantity <= 0
    ORDER BY p.ProductName;
END
GO

-- =============================================
-- إجراء إحصائيات المنتجات - Product Statistics
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_GetProductStatistics]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_GetProductStatistics]
GO

CREATE PROCEDURE [dbo].[sp_GetProductStatistics]
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        -- إجمالي المنتجات - Total Products
        (SELECT COUNT(*) FROM Products WHERE IsDeleted = 0) as TotalProducts,

        -- المنتجات النشطة - Active Products
        (SELECT COUNT(*) FROM Products WHERE IsDeleted = 0 AND IsActive = 1) as ActiveProducts,

        -- المنتجات غير النشطة - Inactive Products
        (SELECT COUNT(*) FROM Products WHERE IsDeleted = 0 AND IsActive = 0) as InactiveProducts,

        -- المنتجات قليلة المخزون - Low Stock Products
        (SELECT COUNT(*) FROM Products
         WHERE IsDeleted = 0 AND IsActive = 1 AND TrackInventory = 1
         AND StockQuantity <= MinimumStock) as LowStockProducts,

        -- المنتجات نافدة المخزون - Out of Stock Products
        (SELECT COUNT(*) FROM Products
         WHERE IsDeleted = 0 AND IsActive = 1 AND TrackInventory = 1
         AND StockQuantity <= 0) as OutOfStockProducts,

        -- إجمالي قيمة المخزون - Total Inventory Value
        (SELECT ISNULL(SUM(StockQuantity * PurchasePrice), 0)
         FROM Products WHERE IsDeleted = 0 AND IsActive = 1) as TotalInventoryValue,

        -- متوسط سعر البيع - Average Sale Price
        (SELECT ISNULL(AVG(SalePrice), 0)
         FROM Products WHERE IsDeleted = 0 AND IsActive = 1) as AverageSalePrice,

        -- أعلى سعر بيع - Highest Sale Price
        (SELECT ISNULL(MAX(SalePrice), 0)
         FROM Products WHERE IsDeleted = 0 AND IsActive = 1) as HighestSalePrice,

        -- أقل سعر بيع - Lowest Sale Price
        (SELECT ISNULL(MIN(SalePrice), 0)
         FROM Products WHERE IsDeleted = 0 AND IsActive = 1 AND SalePrice > 0) as LowestSalePrice;
END
GO

-- =============================================
-- إجراء توزيع المنتجات حسب الفئة - Products Distribution by Category
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_GetProductsDistributionByCategory]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_GetProductsDistributionByCategory]
GO

CREATE PROCEDURE [dbo].[sp_GetProductsDistributionByCategory]
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        c.Id as CategoryId,
        c.CategoryName,
        COUNT(p.Id) as ProductCount,
        ISNULL(SUM(p.StockQuantity), 0) as TotalStock,
        ISNULL(SUM(p.StockQuantity * p.PurchasePrice), 0) as TotalValue,
        ISNULL(AVG(p.SalePrice), 0) as AverageSalePrice
    FROM Categories c
    LEFT JOIN Products p ON c.Id = p.CategoryId AND p.IsDeleted = 0 AND p.IsActive = 1
    WHERE c.IsDeleted = 0 AND c.IsActive = 1
    GROUP BY c.Id, c.CategoryName
    ORDER BY ProductCount DESC, c.CategoryName;
END
GO

-- =============================================
-- إجراء الحصول على أفضل المنتجات مبيعاً - Get Top Selling Products
-- =============================================
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_GetTopSellingProducts]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_GetTopSellingProducts]
GO

CREATE PROCEDURE [dbo].[sp_GetTopSellingProducts]
    @TopCount INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@TopCount)
        p.Id,
        p.ProductName,
        p.ProductCode,
        p.SalePrice,
        c.CategoryName,
        ISNULL(SUM(ii.Quantity), 0) as TotalSold,
        ISNULL(SUM(ii.Quantity * ii.UnitPrice), 0) as TotalSalesAmount
    FROM Products p
    INNER JOIN Categories c ON p.CategoryId = c.Id
    LEFT JOIN InvoiceItems ii ON p.Id = ii.ProductId
    LEFT JOIN Invoices i ON ii.InvoiceId = i.Id AND i.IsDeleted = 0
    WHERE p.IsDeleted = 0 AND p.IsActive = 1
    GROUP BY p.Id, p.ProductName, p.ProductCode, p.SalePrice, c.CategoryName
    ORDER BY TotalSold DESC, TotalSalesAmount DESC;
END
GO
