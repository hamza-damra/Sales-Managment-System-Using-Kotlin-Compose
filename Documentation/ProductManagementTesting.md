# Product Management System Testing Guide
## دليل اختبار نظام إدارة المنتجات

### Testing Overview - نظرة عامة على الاختبار

يتضمن هذا الدليل خطة شاملة لاختبار نظام إدارة المنتجات للتأكد من عمل جميع الوظائف بشكل صحيح.

### Test Environment Setup - إعداد بيئة الاختبار

#### Prerequisites - المتطلبات المسبقة

1. **Visual Studio 2022** أو أحدث
2. **SQL Server** (LocalDB أو SQL Server Express)
3. **DevExpress WinForms** مكونات مثبتة
4. **قاعدة بيانات SalesDB** منشأة ومهيأة

#### Database Setup - إعداد قاعدة البيانات

```sql
-- تشغيل سكريبت إنشاء قاعدة البيانات
-- Run database creation script
EXEC sp_executesql @sql = 'Database\CreateDatabase.sql'

-- تشغيل الإجراءات المخزنة للمنتجات
-- Run product stored procedures
EXEC sp_executesql @sql = 'Database\ProductStoredProcedures.sql'
```

### Test Categories - فئات الاختبار

#### 1. Unit Tests - اختبارات الوحدة

**ProductService Tests**

```csharp
[Test]
public async Task CreateProductAsync_ValidProduct_ReturnsTrue()
{
    // Arrange
    var product = new Product
    {
        ProductName = "منتج اختبار",
        CategoryId = 1,
        SalePrice = 100,
        PurchasePrice = 80
    };
    
    // Act
    var result = await _productService.CreateProductAsync(product);
    
    // Assert
    Assert.IsTrue(result);
}

[Test]
public async Task CreateProductAsync_DuplicateCode_ThrowsException()
{
    // Arrange
    var product1 = new Product { ProductCode = "TEST001", ProductName = "منتج 1", CategoryId = 1 };
    var product2 = new Product { ProductCode = "TEST001", ProductName = "منتج 2", CategoryId = 1 };
    
    // Act & Assert
    await _productService.CreateProductAsync(product1);
    Assert.ThrowsAsync<ArgumentException>(() => _productService.CreateProductAsync(product2));
}
```

**ProductValidationService Tests**

```csharp
[Test]
public async Task ValidateProductAsync_ValidProduct_ReturnsValid()
{
    // Arrange
    var product = new Product
    {
        ProductName = "منتج صحيح",
        CategoryId = 1,
        SalePrice = 100,
        PurchasePrice = 80
    };
    
    // Act
    var result = await _validationService.ValidateProductAsync(product);
    
    // Assert
    Assert.IsTrue(result.IsValid);
}

[Test]
public async Task ValidateProductAsync_EmptyName_ReturnsInvalid()
{
    // Arrange
    var product = new Product { ProductName = "", CategoryId = 1 };
    
    // Act
    var result = await _validationService.ValidateProductAsync(product);
    
    // Assert
    Assert.IsFalse(result.IsValid);
    Assert.IsTrue(result.Errors.Any(e => e.Field == "اسم المنتج"));
}
```

#### 2. Integration Tests - اختبارات التكامل

**Database Integration Tests**

```csharp
[Test]
public async Task ProductRepository_CRUD_Operations_WorkCorrectly()
{
    using var context = new SalesDbContext();
    var repository = new ProductRepository(context);
    
    // Create
    var product = new Product
    {
        ProductName = "منتج تكامل",
        CategoryId = 1,
        SalePrice = 150
    };
    
    await repository.AddAsync(product);
    await context.SaveChangesAsync();
    
    // Read
    var savedProduct = await repository.GetByIdAsync(product.Id);
    Assert.IsNotNull(savedProduct);
    Assert.AreEqual("منتج تكامل", savedProduct.ProductName);
    
    // Update
    savedProduct.ProductName = "منتج محدث";
    repository.Update(savedProduct);
    await context.SaveChangesAsync();
    
    var updatedProduct = await repository.GetByIdAsync(product.Id);
    Assert.AreEqual("منتج محدث", updatedProduct.ProductName);
    
    // Delete
    repository.Delete(savedProduct);
    await context.SaveChangesAsync();
    
    var deletedProduct = await repository.GetByIdAsync(product.Id);
    Assert.IsNull(deletedProduct);
}
```

#### 3. UI Tests - اختبارات واجهة المستخدم

**Manual Test Cases - حالات الاختبار اليدوي**

##### Test Case 1: Add New Product - إضافة منتج جديد

**الهدف:** التأكد من إمكانية إضافة منتج جديد بنجاح

**الخطوات:**
1. فتح نموذج إدارة المنتجات
2. النقر على زر "إضافة"
3. ملء البيانات التالية:
   - اسم المنتج: "لابتوب Dell"
   - كود المنتج: "DELL001"
   - الفئة: "إلكترونيات"
   - سعر الشراء: 2000
   - سعر البيع: 2500
4. النقر على "حفظ"

**النتيجة المتوقعة:**
- ظهور رسالة "تم إضافة المنتج بنجاح"
- ظهور المنتج في قائمة المنتجات
- إغلاق نموذج الإضافة

##### Test Case 2: Edit Existing Product - تعديل منتج موجود

**الهدف:** التأكد من إمكانية تعديل بيانات منتج موجود

**الخطوات:**
1. اختيار منتج من القائمة
2. النقر على زر "تعديل"
3. تغيير سعر البيع إلى 2700
4. النقر على "حفظ"

**النتيجة المتوقعة:**
- ظهور رسالة "تم تحديث المنتج بنجاح"
- تحديث السعر في قائمة المنتجات

##### Test Case 3: Delete Product - حذف منتج

**الهدف:** التأكد من إمكانية حذف منتج

**الخطوات:**
1. اختيار منتج من القائمة
2. النقر على زر "حذف"
3. تأكيد الحذف في الرسالة

**النتيجة المتوقعة:**
- ظهور رسالة تأكيد الحذف
- اختفاء المنتج من القائمة بعد التأكيد

##### Test Case 4: Search Products - البحث في المنتجات

**الهدف:** التأكد من عمل وظيفة البحث

**الخطوات:**
1. كتابة "Dell" في مربع البحث
2. الانتظار لتحديث النتائج

**النتيجة المتوقعة:**
- ظهور المنتجات التي تحتوي على "Dell" فقط

##### Test Case 5: Tile View - عرض البلاطات

**الهدف:** التأكد من عمل عرض البلاطات

**الخطوات:**
1. النقر على زر "عرض البلاطات"
2. تصفح المنتجات

**النتيجة المتوقعة:**
- فتح نموذج عرض البلاطات
- ظهور المنتجات كبطاقات مرئية

##### Test Case 6: Analytics Dashboard - لوحة التحليلات

**الهدف:** التأكد من عمل لوحة التحليلات

**الخطوات:**
1. النقر على زر "التحليلات"
2. مراجعة الرسوم البيانية

**النتيجة المتوقعة:**
- فتح نموذج التحليلات
- ظهور الرسوم البيانية والإحصائيات

#### 4. Validation Tests - اختبارات التحقق

##### Test Case 7: Required Field Validation - التحقق من الحقول المطلوبة

**الهدف:** التأكد من التحقق من الحقول المطلوبة

**الخطوات:**
1. فتح نموذج إضافة منتج
2. ترك اسم المنتج فارغاً
3. النقر على "حفظ"

**النتيجة المتوقعة:**
- ظهور رسالة خطأ "اسم المنتج مطلوب"
- عدم حفظ المنتج

##### Test Case 8: Duplicate Code Validation - التحقق من تكرار الكود

**الهدف:** التأكد من منع تكرار كود المنتج

**الخطوات:**
1. إضافة منتج بكود "TEST001"
2. محاولة إضافة منتج آخر بنفس الكود

**النتيجة المتوقعة:**
- ظهور رسالة خطأ "كود المنتج مستخدم من قبل"

##### Test Case 9: Price Validation - التحقق من الأسعار

**الهدف:** التأكد من صحة الأسعار

**الخطوات:**
1. إدخال سعر بيع أقل من سعر الشراء
2. النقر على "حفظ"

**النتيجة المتوقعة:**
- ظهور تحذير حول هامش الربح السالب

#### 5. Performance Tests - اختبارات الأداء

##### Test Case 10: Large Dataset Performance - أداء مع بيانات كبيرة

**الهدف:** التأكد من الأداء مع عدد كبير من المنتجات

**الخطوات:**
1. إضافة 1000 منتج إلى قاعدة البيانات
2. فتح نموذج إدارة المنتجات
3. قياس وقت التحميل

**النتيجة المتوقعة:**
- تحميل البيانات في أقل من 3 ثوانٍ
- استجابة سريعة للبحث والتصفية

#### 6. Error Handling Tests - اختبارات معالجة الأخطاء

##### Test Case 11: Database Connection Error - خطأ اتصال قاعدة البيانات

**الهدف:** التأكد من معالجة أخطاء الاتصال

**الخطوات:**
1. قطع الاتصال بقاعدة البيانات
2. محاولة فتح نموذج المنتجات

**النتيجة المتوقعة:**
- ظهور رسالة خطأ واضحة
- عدم تعطل التطبيق

### Test Data - بيانات الاختبار

#### Sample Products - منتجات عينة

```sql
-- منتجات للاختبار
INSERT INTO Products (ProductName, ProductCode, CategoryId, PurchasePrice, SalePrice, StockQuantity)
VALUES 
('لابتوب Dell Inspiron', 'DELL001', 1, 2000, 2500, 10),
('ماوس لاسلكي', 'MOUSE001', 1, 50, 80, 50),
('قميص قطني', 'SHIRT001', 2, 30, 60, 25),
('كتاب البرمجة', 'BOOK001', 3, 40, 70, 15);
```

### Test Execution Schedule - جدول تنفيذ الاختبارات

1. **Unit Tests** - يومياً مع كل build
2. **Integration Tests** - أسبوعياً
3. **UI Tests** - قبل كل إصدار
4. **Performance Tests** - شهرياً
5. **Security Tests** - ربع سنوياً

### Test Reporting - تقارير الاختبار

#### Test Results Template - قالب نتائج الاختبار

```
تاريخ الاختبار: [التاريخ]
المختبر: [اسم المختبر]
الإصدار: [رقم الإصدار]

نتائج الاختبار:
- اختبارات ناجحة: [العدد]
- اختبارات فاشلة: [العدد]
- اختبارات متخطاة: [العدد]

الأخطاء المكتشفة:
1. [وصف الخطأ]
2. [وصف الخطأ]

التوصيات:
- [التوصية الأولى]
- [التوصية الثانية]
```

### Automated Testing - الاختبار الآلي

#### Continuous Integration - التكامل المستمر

```yaml
# Azure DevOps Pipeline
trigger:
- main

pool:
  vmImage: 'windows-latest'

steps:
- task: NuGetRestore@1
- task: VSBuild@1
  inputs:
    solution: '**/*.sln'
- task: VSTest@2
  inputs:
    testSelector: 'testAssemblies'
    testAssemblyVer2: |
      **\*Tests.dll
      !**\*TestAdapter.dll
      !**\obj\**
```

### Best Practices - أفضل الممارسات

1. **اختبر مبكراً واختبر كثيراً**
2. **استخدم بيانات اختبار واقعية**
3. **اختبر السيناريوهات الحدية**
4. **وثق جميع الأخطاء المكتشفة**
5. **راجع نتائج الاختبار بانتظام**

---

**ملاحظة:** يجب تشغيل جميع الاختبارات في بيئة منفصلة عن الإنتاج لتجنب تأثير البيانات الحقيقية.
