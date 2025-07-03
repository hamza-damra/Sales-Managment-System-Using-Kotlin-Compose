# Product Management System Deployment Guide
## دليل نشر نظام إدارة المنتجات

### Overview - نظرة عامة

هذا الدليل يوضح خطوات نشر نظام إدارة المنتجات في بيئة الإنتاج.

### System Requirements - متطلبات النظام

#### Server Requirements - متطلبات الخادم

**Hardware - الأجهزة:**
- معالج: Intel Core i5 أو أفضل
- ذاكرة: 8 GB RAM كحد أدنى (16 GB مُوصى)
- مساحة التخزين: 100 GB مساحة فارغة
- شبكة: اتصال إنترنت مستقر

**Software - البرمجيات:**
- Windows Server 2019 أو أحدث
- SQL Server 2019 أو أحدث
- .NET 6.0 Runtime أو أحدث
- IIS 10.0 أو أحدث (إذا كان مطلوباً)

#### Client Requirements - متطلبات العميل

**Hardware - الأجهزة:**
- معالج: Intel Core i3 أو أفضل
- ذاكرة: 4 GB RAM كحد أدنى
- مساحة التخزين: 500 MB مساحة فارغة
- دقة الشاشة: 1024x768 كحد أدنى

**Software - البرمجيات:**
- Windows 10 أو أحدث
- .NET 6.0 Runtime
- DevExpress WinForms Runtime (مُضمن)

### Pre-Deployment Checklist - قائمة ما قبل النشر

#### Code Preparation - إعداد الكود

- [ ] تم اختبار جميع الوظائف
- [ ] تم مراجعة الكود
- [ ] تم تحديث أرقام الإصدارات
- [ ] تم إنشاء ملفات التوثيق
- [ ] تم تحضير سكريبت قاعدة البيانات

#### Environment Setup - إعداد البيئة

- [ ] تم تجهيز خادم قاعدة البيانات
- [ ] تم إنشاء حسابات المستخدمين
- [ ] تم تكوين الشبكة والأمان
- [ ] تم تحضير نسخ احتياطية

### Database Deployment - نشر قاعدة البيانات

#### Step 1: Create Database - إنشاء قاعدة البيانات

```sql
-- إنشاء قاعدة البيانات
CREATE DATABASE [SalesDB]
ON 
( NAME = N'SalesDB', 
  FILENAME = N'C:\Database\SalesDB.mdf',
  SIZE = 100MB,
  MAXSIZE = 10GB,
  FILEGROWTH = 10MB )
LOG ON 
( NAME = N'SalesDB_Log',
  FILENAME = N'C:\Database\SalesDB_Log.ldf',
  SIZE = 10MB,
  MAXSIZE = 1GB,
  FILEGROWTH = 10% );
```

#### Step 2: Run Database Scripts - تشغيل سكريبت قاعدة البيانات

```bash
# تشغيل سكريبت إنشاء الجداول
sqlcmd -S ServerName -d SalesDB -i "Database\CreateDatabase.sql"

# تشغيل سكريبت الإجراءات المخزنة
sqlcmd -S ServerName -d SalesDB -i "Database\ProductStoredProcedures.sql"

# إدراج البيانات الأولية
sqlcmd -S ServerName -d SalesDB -i "Database\SeedData.sql"
```

#### Step 3: Configure Security - تكوين الأمان

```sql
-- إنشاء مستخدم التطبيق
CREATE LOGIN [SalesAppUser] WITH PASSWORD = 'StrongPassword123!';
USE [SalesDB];
CREATE USER [SalesAppUser] FOR LOGIN [SalesAppUser];

-- منح الصلاحيات
ALTER ROLE [db_datareader] ADD MEMBER [SalesAppUser];
ALTER ROLE [db_datawriter] ADD MEMBER [SalesAppUser];
ALTER ROLE [db_executor] ADD MEMBER [SalesAppUser];
```

### Application Deployment - نشر التطبيق

#### Step 1: Build Application - بناء التطبيق

```bash
# تنظيف المشروع
dotnet clean

# بناء التطبيق للإنتاج
dotnet build --configuration Release

# نشر التطبيق
dotnet publish --configuration Release --output "C:\Deploy\SalesApp"
```

#### Step 2: Configuration Files - ملفات التكوين

**appsettings.json**
```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=ProductionServer;Database=SalesDB;User Id=SalesAppUser;Password=StrongPassword123!;TrustServerCertificate=true;"
  },
  "Logging": {
    "LogLevel": {
      "Default": "Information",
      "Microsoft": "Warning",
      "Microsoft.Hosting.Lifetime": "Information"
    }
  },
  "ApplicationSettings": {
    "Environment": "Production",
    "EnableDetailedErrors": false,
    "MaxRetryAttempts": 3,
    "CommandTimeout": 30
  }
}
```

**App.config**
```xml
<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <connectionStrings>
    <add name="DefaultConnection" 
         connectionString="Server=ProductionServer;Database=SalesDB;User Id=SalesAppUser;Password=StrongPassword123!;TrustServerCertificate=true;" 
         providerName="System.Data.SqlClient" />
  </connectionStrings>
  
  <appSettings>
    <add key="Environment" value="Production" />
    <add key="LogLevel" value="Information" />
    <add key="EnableDetailedErrors" value="false" />
  </appSettings>
</configuration>
```

#### Step 3: Copy Files - نسخ الملفات

```bash
# نسخ ملفات التطبيق
xcopy "C:\Deploy\SalesApp\*" "C:\Program Files\SalesApplication\" /E /Y

# نسخ ملفات التكوين
copy "Config\Production\appsettings.json" "C:\Program Files\SalesApplication\"
copy "Config\Production\App.config" "C:\Program Files\SalesApplication\"
```

### Client Installation - تثبيت العميل

#### Step 1: Create Installer - إنشاء برنامج التثبيت

```xml
<!-- Setup.wixproj -->
<Project ToolsVersion="4.0" DefaultTargets="Build" xmlns="http://schemas.microsoft.com/developer/msbuild/2003">
  <PropertyGroup>
    <Configuration Condition=" '$(Configuration)' == '' ">Debug</Configuration>
    <Platform Condition=" '$(Platform)' == '' ">x86</Platform>
    <ProductVersion>3.10</ProductVersion>
    <ProjectGuid>{12345678-1234-1234-1234-123456789012}</ProjectGuid>
    <SchemaVersion>2.0</SchemaVersion>
    <OutputName>SalesApplicationSetup</OutputName>
    <OutputType>Package</OutputType>
  </PropertyGroup>
</Project>
```

#### Step 2: Distribution - التوزيع

**Network Installation - التثبيت الشبكي:**
```bash
# نسخ ملفات التثبيت إلى مجلد مشترك
copy "Setup\SalesApplicationSetup.msi" "\\FileServer\Software\SalesApp\"

# إنشاء سكريبت تثبيت صامت
msiexec /i "\\FileServer\Software\SalesApp\SalesApplicationSetup.msi" /quiet /norestart
```

**USB Installation - التثبيت عبر USB:**
```bash
# نسخ ملفات التثبيت إلى USB
copy "Setup\*" "E:\SalesApp\"

# تشغيل التثبيت
E:\SalesApp\SalesApplicationSetup.exe
```

### Configuration Management - إدارة التكوين

#### Environment Variables - متغيرات البيئة

```bash
# تعيين متغيرات البيئة
setx SALES_DB_SERVER "ProductionServer" /M
setx SALES_DB_NAME "SalesDB" /M
setx SALES_LOG_LEVEL "Information" /M
```

#### Registry Settings - إعدادات السجل

```reg
Windows Registry Editor Version 5.00

[HKEY_LOCAL_MACHINE\SOFTWARE\SalesApplication]
"InstallPath"="C:\\Program Files\\SalesApplication"
"Version"="1.0.0"
"DatabaseServer"="ProductionServer"
"LogLevel"="Information"
```

### Security Configuration - تكوين الأمان

#### Firewall Rules - قواعد جدار الحماية

```bash
# فتح منفذ SQL Server
netsh advfirewall firewall add rule name="SQL Server" dir=in action=allow protocol=TCP localport=1433

# فتح منافذ التطبيق (إذا كان مطلوباً)
netsh advfirewall firewall add rule name="Sales Application" dir=in action=allow protocol=TCP localport=8080
```

#### SSL/TLS Configuration - تكوين SSL/TLS

```sql
-- تمكين التشفير في SQL Server
EXEC sp_configure 'show advanced options', 1;
RECONFIGURE;
EXEC sp_configure 'force encryption', 1;
RECONFIGURE;
```

### Monitoring and Logging - المراقبة والتسجيل

#### Log Configuration - تكوين السجلات

```xml
<!-- NLog.config -->
<?xml version="1.0" encoding="utf-8" ?>
<nlog xmlns="http://www.nlog-project.org/schemas/NLog.xsd"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <targets>
    <target xsi:type="File" name="fileTarget"
            fileName="C:\Logs\SalesApp\${shortdate}.log"
            layout="${longdate} ${level:uppercase=true} ${logger} ${message} ${exception:format=tostring}" />
  </targets>
  
  <rules>
    <logger name="*" minlevel="Info" writeTo="fileTarget" />
  </rules>
</nlog>
```

#### Performance Monitoring - مراقبة الأداء

```bash
# إنشاء عدادات الأداء
typeperf "\Processor(_Total)\% Processor Time" "\Memory\Available MBytes" -o "C:\Logs\Performance.csv" -f CSV -si 60
```

### Backup and Recovery - النسخ الاحتياطي والاستعادة

#### Database Backup - نسخ احتياطي لقاعدة البيانات

```sql
-- نسخة احتياطية كاملة يومية
BACKUP DATABASE [SalesDB] 
TO DISK = N'C:\Backup\SalesDB_Full_' + CONVERT(VARCHAR, GETDATE(), 112) + '.bak'
WITH FORMAT, INIT, COMPRESSION;

-- نسخة احتياطية تفاضلية كل 6 ساعات
BACKUP DATABASE [SalesDB] 
TO DISK = N'C:\Backup\SalesDB_Diff_' + CONVERT(VARCHAR, GETDATE(), 112) + '_' + REPLACE(CONVERT(VARCHAR, GETDATE(), 108), ':', '') + '.bak'
WITH DIFFERENTIAL, COMPRESSION;
```

#### Application Backup - نسخ احتياطي للتطبيق

```bash
# نسخ ملفات التطبيق
robocopy "C:\Program Files\SalesApplication" "C:\Backup\Application" /MIR /Z /W:5 /R:3

# ضغط النسخة الاحتياطية
7z a "C:\Backup\SalesApp_Backup_%date%.7z" "C:\Backup\Application\*"
```

### Post-Deployment Testing - اختبار ما بعد النشر

#### Smoke Tests - اختبارات أساسية

1. **Database Connectivity**
   ```sql
   SELECT @@VERSION;
   SELECT COUNT(*) FROM Products;
   ```

2. **Application Launch**
   - تشغيل التطبيق
   - تسجيل الدخول
   - فتح نموذج المنتجات

3. **Basic Operations**
   - إضافة منتج جديد
   - تعديل منتج موجود
   - البحث في المنتجات

#### Performance Tests - اختبارات الأداء

```bash
# اختبار استجابة قاعدة البيانات
sqlcmd -S ProductionServer -d SalesDB -Q "SELECT COUNT(*) FROM Products" -o response_time.txt
```

### Rollback Plan - خطة التراجع

#### Emergency Rollback - التراجع الطارئ

1. **إيقاف التطبيق**
   ```bash
   taskkill /F /IM SalesApplication.exe
   ```

2. **استعادة قاعدة البيانات**
   ```sql
   RESTORE DATABASE [SalesDB] FROM DISK = N'C:\Backup\SalesDB_Full_LastKnownGood.bak'
   WITH REPLACE;
   ```

3. **استعادة التطبيق**
   ```bash
   robocopy "C:\Backup\Application_LastKnownGood" "C:\Program Files\SalesApplication" /MIR
   ```

### Maintenance Schedule - جدول الصيانة

#### Daily - يومياً
- [ ] مراجعة ملفات السجل
- [ ] فحص مساحة القرص الصلب
- [ ] نسخة احتياطية كاملة

#### Weekly - أسبوعياً
- [ ] تحديث إحصائيات قاعدة البيانات
- [ ] فحص أداء النظام
- [ ] مراجعة تقارير الأخطاء

#### Monthly - شهرياً
- [ ] تحديث النظام والتطبيقات
- [ ] مراجعة أمان النظام
- [ ] اختبار خطة الاستعادة

### Support Information - معلومات الدعم

**Technical Support - الدعم الفني:**
- البريد الإلكتروني: support@company.com
- الهاتف: +966-11-1234567
- ساعات العمل: 8:00 ص - 5:00 م

**Emergency Contact - الاتصال الطارئ:**
- الهاتف: +966-50-1234567 (24/7)

---

**ملاحظة:** يجب مراجعة هذا الدليل وتحديثه بانتظام ليعكس أي تغييرات في النظام أو البيئة.
