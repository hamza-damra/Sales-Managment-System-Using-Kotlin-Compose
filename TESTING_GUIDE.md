# دليل اختبار نظام إدارة المبيعات - Sales Management System Testing Guide

## نظرة عامة - Overview

هذا الدليل يوضح كيفية اختبار نظام إدارة المبيعات DXApplication1 بشكل شامل للتأكد من عمل جميع المكونات بشكل صحيح.

This guide explains how to comprehensively test the DXApplication1 Sales Management System to ensure all components work correctly.

## متطلبات الاختبار - Testing Requirements

### 1. متطلبات النظام - System Requirements
- Windows 10/11
- .NET 8.0 Runtime
- SQL Server 2019+ أو SQL Server Express
- Visual Studio 2022 (للتطوير)

### 2. إعداد قاعدة البيانات - Database Setup
```sql
-- تشغيل سكريبت إنشاء قاعدة البيانات
-- Run the database creation script
-- File: Database/CreateDatabase.sql
```

## خطة الاختبار - Testing Plan

### المرحلة 1: اختبار قاعدة البيانات - Database Testing

#### 1.1 اختبار الاتصال - Connection Testing
- [ ] التحقق من اتصال قاعدة البيانات
- [ ] اختبار سلسلة الاتصال في App.config
- [ ] التأكد من إنشاء الجداول بشكل صحيح

#### 1.2 اختبار البيانات الأولية - Seed Data Testing
- [ ] التحقق من إدراج الأدوار الافتراضية
- [ ] التحقق من إنشاء المستخدم الافتراضي (admin/admin123)
- [ ] التحقق من إدراج الفئات الافتراضية
- [ ] التحقق من إدراج العملاء التجريبيين
- [ ] التحقق من إدراج المنتجات التجريبية

### المرحلة 2: اختبار طبقة الوصول للبيانات - Data Access Layer Testing

#### 2.1 اختبار المستودعات - Repository Testing
```csharp
// مثال على اختبار مستودع العملاء
[Test]
public async Task CustomerRepository_GetAll_ReturnsCustomers()
{
    // Arrange
    using var context = new SalesDbContext();
    var repository = new CustomerRepository(context);
    
    // Act
    var customers = await repository.GetAllAsync();
    
    // Assert
    Assert.IsNotNull(customers);
    Assert.IsTrue(customers.Any());
}
```

#### 2.2 اختبار وحدة العمل - Unit of Work Testing
- [ ] اختبار العمليات المتزامنة
- [ ] اختبار المعاملات (Transactions)
- [ ] اختبار الحفظ والتراجع

### المرحلة 3: اختبار طبقة منطق الأعمال - Business Logic Layer Testing

#### 3.1 اختبار خدمة العملاء - Customer Service Testing
- [ ] إضافة عميل جديد
- [ ] تحديث بيانات عميل
- [ ] حذف عميل (حذف منطقي)
- [ ] البحث عن العملاء
- [ ] التحقق من صحة البيانات

#### 3.2 اختبار خدمة المنتجات - Product Service Testing
- [ ] إضافة منتج جديد
- [ ] تحديث بيانات منتج
- [ ] حذف منتج
- [ ] إدارة المخزون
- [ ] البحث عن المنتجات

#### 3.3 اختبار خدمة لوحة التحكم - Dashboard Service Testing
- [ ] تحميل الإحصائيات
- [ ] عرض البيانات الحديثة
- [ ] إنشاء المخططات

### المرحلة 4: اختبار طبقة العرض - Presentation Layer Testing

#### 4.1 اختبار نموذج تسجيل الدخول - Login Form Testing
- [ ] تسجيل دخول صحيح (admin/admin123)
- [ ] تسجيل دخول خاطئ
- [ ] التحقق من صحة البيانات المدخلة
- [ ] اختبار الأمان

#### 4.2 اختبار لوحة التحكم الرئيسية - Main Dashboard Testing
- [ ] عرض الإحصائيات
- [ ] عمل البلاطات (Tiles)
- [ ] عرض المخططات
- [ ] التنقل بين النماذج
- [ ] دعم اللغة العربية والـ RTL

#### 4.3 اختبار نموذج إدارة العملاء - Customers Form Testing
- [ ] عرض قائمة العملاء
- [ ] البحث والتصفية
- [ ] إضافة عميل جديد
- [ ] تعديل عميل موجود
- [ ] حذف عميل
- [ ] التحقق من صحة البيانات

#### 4.4 اختبار نموذج إدارة المنتجات - Products Form Testing
- [ ] عرض قائمة المنتجات
- [ ] البحث والتصفية حسب الفئة
- [ ] إضافة منتج جديد (قيد التطوير)
- [ ] تعديل منتج موجود (قيد التطوير)
- [ ] حذف منتج

### المرحلة 5: اختبار التكامل - Integration Testing

#### 5.1 اختبار تدفق العمل الكامل - End-to-End Workflow Testing
1. تسجيل الدخول
2. عرض لوحة التحكم
3. إدارة العملاء
4. إدارة المنتجات
5. تسجيل الخروج

#### 5.2 اختبار الأداء - Performance Testing
- [ ] اختبار سرعة تحميل البيانات
- [ ] اختبار الاستجابة مع كميات كبيرة من البيانات
- [ ] اختبار استهلاك الذاكرة

### المرحلة 6: اختبار واجهة المستخدم - UI/UX Testing

#### 6.1 اختبار دعم اللغة العربية - Arabic Language Support Testing
- [ ] عرض النصوص العربية بشكل صحيح
- [ ] دعم الـ RTL (Right-to-Left)
- [ ] تنسيق التواريخ والأرقام
- [ ] عرض العملة بالريال السعودي

#### 6.2 اختبار DevExpress Controls
- [ ] RibbonControl
- [ ] GridControl وعرض البيانات
- [ ] TileControl والبلاطات
- [ ] ChartControl والمخططات
- [ ] النماذج والحقول

## سيناريوهات الاختبار - Test Scenarios

### سيناريو 1: إضافة عميل جديد
1. فتح نموذج إدارة العملاء
2. النقر على زر "إضافة"
3. ملء البيانات المطلوبة
4. حفظ العميل
5. التحقق من ظهور العميل في القائمة

### سيناريو 2: البحث عن منتج
1. فتح نموذج إدارة المنتجات
2. كتابة اسم المنتج في حقل البحث
3. التحقق من ظهور النتائج المطابقة
4. تصفية حسب الفئة
5. التحقق من دقة النتائج

### سيناريو 3: عرض الإحصائيات
1. تسجيل الدخول
2. عرض لوحة التحكم
3. التحقق من عرض الإحصائيات
4. التحقق من عمل البلاطات
5. التحقق من عرض المخططات

## أدوات الاختبار المقترحة - Recommended Testing Tools

### 1. اختبار الوحدة - Unit Testing
- NUnit أو MSTest
- Moq للـ Mocking
- FluentAssertions

### 2. اختبار التكامل - Integration Testing
- Entity Framework In-Memory Database
- TestContainers لقاعدة البيانات

### 3. اختبار واجهة المستخدم - UI Testing
- White Framework
- FlaUI
- Appium for Windows

## تقرير الأخطاء - Bug Reporting

عند العثور على خطأ، يرجى تسجيل:
- وصف الخطأ
- خطوات إعادة الإنتاج
- النتيجة المتوقعة
- النتيجة الفعلية
- لقطة شاشة إن أمكن
- معلومات البيئة (نظام التشغيل، إصدار .NET)

## الخلاصة - Conclusion

هذا الدليل يوفر إطار عمل شامل لاختبار نظام إدارة المبيعات. يُنصح بتنفيذ الاختبارات بشكل منتظم أثناء التطوير وقبل النشر.

This guide provides a comprehensive framework for testing the Sales Management System. It's recommended to run tests regularly during development and before deployment.
