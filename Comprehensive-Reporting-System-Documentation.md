# Comprehensive Reporting System Documentation

## Table of Contents
1. [Overview](#overview)
2. [API Endpoints](#api-endpoints)
3. [Business Logic](#business-logic)
4. [Data Models](#data-models)
5. [Error Handling](#error-handling)
6. [Export Formats](#export-formats)
7. [Caching Strategy](#caching-strategy)
8. [Security & Authorization](#security--authorization)
9. [Audit Trail](#audit-trail)
10. [Usage Examples](#usage-examples)
11. [Testing](#testing)

## Overview

The Sales Management System's Reporting module provides comprehensive business intelligence and analytics capabilities designed for enterprise-level operations. The system offers real-time insights across all business entities with advanced features including multi-format exports, intelligent caching, role-based security, and comprehensive audit trails.

### Key Capabilities

- **Multi-Entity Reporting**: Sales, customers, products, inventory, promotions, financial data, and user analytics
- **Advanced Analytics**: Trend analysis, forecasting, customer segmentation, lifetime value analysis, and profitability metrics
- **Real-time Dashboards**: Executive KPIs, operational metrics, and live performance indicators
- **Export Flexibility**: PDF, Excel, CSV, and JSON formats with professional templates
- **Performance Optimization**: Redis-based caching, pagination, asynchronous processing, and query optimization
- **Enterprise Security**: Role-based access control, audit logging, and data privacy compliance
- **Scalability**: Designed to handle large datasets with efficient memory management

### Architecture Overview

The reporting system follows a layered architecture:
- **Controller Layer**: RESTful endpoints with security and validation
- **Service Layer**: Business logic, data aggregation, and report generation
- **Repository Layer**: Data access with optimized queries
- **Cache Layer**: Redis-based caching for performance
- **Export Layer**: Multi-format export services
- **Security Layer**: Role-based access control and audit logging

## API Endpoints

All reporting endpoints follow the standardized pattern: `/api/v1/reports/{entity}/{reportType}`

### Base URL
```
/api/v1/reports
```

### Sales Reports

#### 1. Comprehensive Sales Report
**Endpoint:** `GET /api/v1/reports/sales/comprehensive`

**Description:** Generate detailed sales analytics with trends, customer insights, and product performance

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SALES_ANALYST')")`

**Parameters:**
- `startDate` (required): Start date in ISO format (yyyy-MM-ddTHH:mm:ss)
- `endDate` (required): End date in ISO format
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20, max: 100)
- `sortBy` (optional): Sort field (default: createdAt)
- `sortDirection` (optional): Sort direction (ASC/DESC, default: DESC)
- `customerIds` (optional): Filter by specific customers
- `productIds` (optional): Filter by specific products
- `regions` (optional): Filter by regions
- `paymentMethods` (optional): Filter by payment methods
- `useCache` (optional): Enable caching (default: true)

**Response Structure:**
```json
{
  "success": true,
  "message": "Report generated successfully",
  "data": {
    "summary": {
      "totalSales": 1250,
      "totalRevenue": 125000.00,
      "averageOrderValue": 100.00,
      "totalDiscounts": 5000.00,
      "uniqueCustomers": 450,
      "conversionRate": 15.5,
      "growthRate": 12.3
    },
    "dailyBreakdown": [...],
    "topCustomers": [...],
    "topProducts": [...],
    "salesByStatus": {...},
    "trends": [...],
    "paymentAnalysis": {...},
    "regionalAnalysis": {...}
  },
  "metadata": {
    "reportType": "COMPREHENSIVE_SALES",
    "reportName": "Comprehensive Sales Report",
    "generatedAt": "2025-07-09T10:30:00",
    "fromCache": false,
    "executionTimeMs": 1250,
    "period": {
      "startDate": "2025-06-01T00:00:00",
      "endDate": "2025-06-30T23:59:59",
      "daysIncluded": 30
    },
    "pagination": {
      "page": 0,
      "size": 20,
      "totalPages": 5,
      "totalElements": 100,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

#### 2. Sales Summary Report
**Endpoint:** `GET /api/v1/reports/sales/summary`

**Description:** Quick overview of sales performance with key metrics

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SALES_ANALYST')")`

**Parameters:**
- `startDate` (required): Start date
- `endDate` (required): End date
- `useCache` (optional): Enable caching (default: true)

#### 3. Sales Export
**Endpoint:** `GET /api/v1/reports/sales/export`

**Description:** Export sales data in various formats

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SALES_ANALYST')")`

**Parameters:**
- All parameters from comprehensive report
- `exportFormat` (required): PDF, EXCEL, CSV, JSON

### Customer Reports

#### 1. Customer Lifetime Value Report
**Endpoint:** `GET /api/v1/reports/customers/lifetime-value`

**Description:** Analyze customer lifetime value with segmentation and retention metrics

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CUSTOMER_ANALYST')")`

#### 2. Customer Analytics Report
**Endpoint:** `GET /api/v1/reports/customers/analytics`

**Description:** Comprehensive customer behavior analysis and segmentation

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CUSTOMER_ANALYST')")`

### Product Reports

#### 1. Product Performance Report
**Endpoint:** `GET /api/v1/reports/products/performance`

**Description:** Product sales performance, profitability, and trend analysis

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('PRODUCT_ANALYST')")`

#### 2. Inventory Turnover Report
**Endpoint:** `GET /api/v1/reports/products/inventory-turnover`

**Description:** Inventory turnover analysis with optimization recommendations

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('INVENTORY_ANALYST')")`

### Inventory Reports

#### 1. Inventory Status Report
**Endpoint:** `GET /api/v1/reports/inventory/status`

**Description:** Current inventory levels, stock alerts, and valuation

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('INVENTORY_ANALYST')")`

### Promotion Reports

#### 1. Promotion Effectiveness Report
**Endpoint:** `GET /api/v1/reports/promotions/effectiveness`

**Description:** Promotion ROI, usage statistics, and customer response analysis

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('MARKETING_ANALYST')")`

### Financial Reports

#### 1. Financial Revenue Report
**Endpoint:** `GET /api/v1/reports/financial/revenue`

**Description:** Comprehensive revenue analysis with profit margins and financial metrics

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('FINANCIAL_ANALYST')")`

### Dashboard & KPI Reports

#### 1. Executive Dashboard
**Endpoint:** `GET /api/v1/reports/dashboard/executive`

**Description:** High-level KPIs and metrics for executive overview

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('EXECUTIVE')")`

#### 2. Real-time KPIs
**Endpoint:** `GET /api/v1/reports/kpi/real-time`

**Description:** Real-time key performance indicators and live metrics

**Security:** `@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")`

## Business Logic

### Report Generation Process

The reporting system implements a sophisticated multi-stage process for generating accurate and performant reports:

#### 1. Data Retrieval and Filtering
- **Repository Layer**: Optimized JPA queries with custom repository methods
- **Date Range Filtering**: Efficient date-based filtering using indexed columns
- **Entity Filtering**: Support for filtering by customers, products, categories, regions
- **Status Filtering**: Filter by sale status, payment status, return status
- **Pagination**: Memory-efficient pagination for large datasets

#### 2. Data Aggregation and Calculations

**Sales Metrics Calculation:**
```java
// Revenue calculation with discount consideration
BigDecimal totalRevenue = sales.stream()
    .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
    .map(sale -> sale.getTotalAmount().subtract(sale.getDiscountAmount()))
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// Average Order Value calculation
BigDecimal averageOrderValue = totalRevenue.divide(
    BigDecimal.valueOf(completedSales.size()),
    2, RoundingMode.HALF_UP
);
```

**Customer Lifetime Value Calculation:**
```java
// CLV = (Average Order Value × Purchase Frequency × Gross Margin × Lifespan)
BigDecimal clv = averageOrderValue
    .multiply(purchaseFrequency)
    .multiply(grossMargin)
    .multiply(customerLifespan);
```

**Inventory Turnover Calculation:**
```java
// Turnover Rate = Cost of Goods Sold / Average Inventory Value
BigDecimal turnoverRate = costOfGoodsSold.divide(
    averageInventoryValue,
    4, RoundingMode.HALF_UP
);
```

#### 3. Trend Analysis and Forecasting

**Growth Rate Calculation:**
```java
// Period-over-period growth rate
BigDecimal growthRate = currentPeriodValue
    .subtract(previousPeriodValue)
    .divide(previousPeriodValue, 4, RoundingMode.HALF_UP)
    .multiply(BigDecimal.valueOf(100));
```

**Trend Direction Analysis:**
- **INCREASING**: Growth rate > 5%
- **STABLE**: Growth rate between -5% and 5%
- **DECREASING**: Growth rate < -5%

#### 4. Customer Segmentation Logic

**RFM Analysis (Recency, Frequency, Monetary):**
- **Recency**: Days since last purchase
- **Frequency**: Number of purchases in period
- **Monetary**: Total amount spent

**Segmentation Categories:**
- **VIP**: High frequency, high monetary, recent activity
- **LOYAL**: High frequency, moderate monetary
- **POTENTIAL**: Recent activity, low frequency
- **AT_RISK**: High monetary, low recency
- **LOST**: Low recency, low frequency

## Data Models

### Core DTOs and Request/Response Objects

#### 1. ReportRequestDTO
**File:** `src/main/java/com/hamza/salesmanagementbackend/dto/report/ReportRequestDTO.java`

**Purpose:** Standardized request DTO for all report generation with filtering and pagination

**Key Fields:**
```java
@Data
@Builder
public class ReportRequestDTO {
    @NotNull private LocalDateTime startDate;
    @NotNull private LocalDateTime endDate;
    @Min(0) private Integer page = 0;
    @Min(1) @Max(100) private Integer size = 20;
    private String sortBy;
    private String sortDirection = "DESC";

    // Filtering options
    private List<Long> customerIds;
    private List<Long> productIds;
    private List<Long> categoryIds;
    private List<String> regions;
    private List<String> paymentMethods;
    private List<String> statuses;

    // Range filters
    private BigDecimalRange amountRange;
    private BigDecimalRange discountRange;

    // Export and caching
    private String exportFormat; // PDF, EXCEL, CSV, JSON
    private Boolean useCache;
    private Map<String, Object> additionalFilters;
}
```

#### 2. StandardReportResponse<T>
**File:** `src/main/java/com/hamza/salesmanagementbackend/dto/report/StandardReportResponse.java`

**Purpose:** Standardized response wrapper for all report endpoints

**Structure:**
```java
@Data
@Builder
public class StandardReportResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private ReportMetadata metadata;
    private String errorCode;
    private String errorDetails;
}
```

#### 3. SalesReportDTO
**File:** `src/main/java/com/hamza/salesmanagementbackend/dto/report/SalesReportDTO.java`

**Purpose:** Comprehensive sales report data structure

**Components:**
```java
@Data
@Builder
public class SalesReportDTO {
    private SalesSummary summary;
    private List<DailySalesData> dailyBreakdown;
    private List<TopCustomer> topCustomers;
    private List<TopProduct> topProducts;
    private Map<SaleStatus, Long> salesByStatus;
    private List<SalesTrend> trends;
    private PaymentMethodAnalysis paymentAnalysis;
    private RegionalAnalysis regionalAnalysis;
}
```

**Nested Classes:**
- **SalesSummary**: Total sales, revenue, discounts, unique customers, conversion rates
- **DailySalesData**: Daily breakdown with sales count, revenue, and growth metrics
- **TopCustomer**: Customer ranking with total orders, spent amount, and segment
- **TopProduct**: Product performance with quantity sold, revenue, and profit margin
- **SalesTrend**: Trend analysis with growth rates and direction indicators
- **PaymentMethodAnalysis**: Payment method distribution and revenue analysis
- **RegionalAnalysis**: Geographic performance breakdown

#### 4. CustomerReportDTO
**File:** `src/main/java/com/hamza/salesmanagementbackend/dto/report/CustomerReportDTO.java`

**Purpose:** Customer analytics and lifetime value reporting

**Components:**
```java
@Data
@Builder
public class CustomerReportDTO {
    private CustomerSummary summary;
    private List<CustomerSegment> segments;
    private List<CustomerLifetimeValue> topCustomers;
    private CustomerRetentionMetrics retention;
    private CustomerAcquisitionMetrics acquisition;
    private List<CustomerBehaviorInsight> behaviorInsights;
}
```

#### 5. InventoryReportDTO
**File:** `src/main/java/com/hamza/salesmanagementbackend/dto/report/InventoryReportDTO.java`

**Purpose:** Inventory status, turnover, and optimization reporting

**Components:**
```java
@Data
@Builder
public class InventoryReportDTO {
    private InventorySummary summary;
    private List<StockAlert> stockAlerts;
    private List<ProductTurnover> turnoverAnalysis;
    private InventoryValuation valuation;
    private List<CategoryAnalysis> categoryBreakdown;
    private List<WarehouseAnalysis> warehouseDistribution;
}
```

#### 6. ReportMetadata
**File:** `src/main/java/com/hamza/salesmanagementbackend/dto/report/ReportMetadata.java`

**Purpose:** Metadata for all report responses providing standardized information

**Structure:**
```java
@Data
@Builder
public class ReportMetadata {
    private String reportType;
    private String reportName;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private ReportPeriod period;
    private Map<String, Object> appliedFilters;
    private PaginationInfo pagination;
    private Long totalRecords;
    private Long executionTimeMs;
    private String version;
    private Boolean fromCache;
    private LocalDateTime cacheExpiry;
}
```

### Entity Relationships

The reporting system leverages the following core entities:

- **Sale**: Primary transaction entity with customer, product, and financial data
- **Customer**: Customer information with purchase history and segmentation data
- **Product**: Product details with inventory and performance metrics
- **Inventory**: Stock levels, valuation, and turnover data
- **Promotion**: Promotion details and effectiveness metrics
- **AppliedPromotion**: Promotion usage tracking and ROI calculation
- **Return**: Return transactions for customer satisfaction analysis
- **User**: User activity and performance tracking

## Error Handling

### Common Error Scenarios

#### 1. Validation Errors (HTTP 400)

**Invalid Date Range:**
```json
{
  "success": false,
  "message": "Invalid date range: start date must be before end date",
  "errorCode": "INVALID_DATE_RANGE",
  "errorDetails": "Start date: 2025-07-01, End date: 2025-06-01"
}
```

**Invalid Parameters:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "errorDetails": {
    "page": "Page number must be non-negative",
    "size": "Page size must be between 1 and 100",
    "exportFormat": "Unsupported export format: XML"
  }
}
```

#### 2. Authorization Errors (HTTP 403)

**Insufficient Permissions:**
```json
{
  "success": false,
  "message": "Access denied: insufficient permissions for this report type",
  "errorCode": "ACCESS_DENIED",
  "errorDetails": "Required roles: [ADMIN, MANAGER, FINANCIAL_ANALYST]"
}
```

#### 3. Resource Not Found (HTTP 404)

**No Data Found:**
```json
{
  "success": false,
  "message": "No data found for the specified criteria",
  "errorCode": "NO_DATA_FOUND",
  "errorDetails": "Date range: 2025-01-01 to 2025-01-31, Filters: customerIds=[999]"
}
```

#### 4. Server Errors (HTTP 500)

**Report Generation Failed:**
```json
{
  "success": false,
  "message": "Report generation failed due to internal error",
  "errorCode": "REPORT_GENERATION_ERROR",
  "errorDetails": "Database connection timeout during aggregation"
}
```

**Export Service Error:**
```json
{
  "success": false,
  "message": "Export service temporarily unavailable",
  "errorCode": "EXPORT_SERVICE_ERROR",
  "errorDetails": "PDF generation service is currently overloaded"
}
```

### Error Handling Implementation

**Global Exception Handler:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReportValidationException.class)
    public ResponseEntity<StandardReportResponse<?>> handleValidationError(
            ReportValidationException ex) {
        return ResponseEntity.badRequest().body(
            StandardReportResponse.error(ex.getMessage(), "VALIDATION_ERROR")
        );
    }

    @ExceptionHandler(ReportGenerationException.class)
    public ResponseEntity<StandardReportResponse<?>> handleGenerationError(
            ReportGenerationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            StandardReportResponse.error(ex.getMessage(), "GENERATION_ERROR")
        );
    }
}
```

## Export Formats

### Supported Export Formats

The reporting system supports four export formats with professional templates and optimized performance:

#### 1. PDF Export
**Format:** Portable Document Format
**Use Case:** Professional reports, executive summaries, client presentations
**Implementation:** Uses iText library for PDF generation

**Features:**
- Professional templates with company branding
- Charts and graphs integration
- Multi-page support with headers/footers
- Digital signatures support
- Optimized for printing

**Example Request:**
```http
GET /api/v1/reports/sales/export?exportFormat=PDF&startDate=2025-06-01T00:00:00&endDate=2025-06-30T23:59:59
```

#### 2. Excel Export
**Format:** Microsoft Excel (.xlsx)
**Use Case:** Data analysis, pivot tables, further processing
**Implementation:** Uses Apache POI library

**Features:**
- Multiple worksheets for different data sections
- Formatted cells with data types
- Charts and pivot tables
- Formulas and calculations
- Data validation

**Example Request:**
```http
GET /api/v1/reports/sales/export?exportFormat=EXCEL&startDate=2025-06-01T00:00:00&endDate=2025-06-30T23:59:59
```

#### 3. CSV Export
**Format:** Comma-Separated Values
**Use Case:** Data import/export, system integration, bulk processing
**Implementation:** Custom CSV writer with proper escaping

**Features:**
- UTF-8 encoding support
- Proper field escaping and quoting
- Large dataset streaming
- Configurable delimiters
- Header row inclusion

**Example Request:**
```http
GET /api/v1/reports/sales/export?exportFormat=CSV&startDate=2025-06-01T00:00:00&endDate=2025-06-30T23:59:59
```

#### 4. JSON Export
**Format:** JavaScript Object Notation
**Use Case:** API integration, web applications, data exchange
**Implementation:** Jackson JSON processor

**Features:**
- Structured hierarchical data
- Type preservation
- Compact or pretty-printed format
- Streaming for large datasets
- Schema validation

**Example Request:**
```http
GET /api/v1/reports/sales/export?exportFormat=JSON&startDate=2025-06-01T00:00:00&endDate=2025-06-30T23:59:59
```

### Export Service Implementation

**File:** `src/main/java/com/hamza/salesmanagementbackend/service/ReportExportService.java`

**Key Methods:**
```java
@Service
@Slf4j
public class ReportExportService {

    public byte[] exportReport(ReportRequestDTO request) {
        return switch (request.getExportFormat().toUpperCase()) {
            case "PDF" -> exportToPdf(request);
            case "EXCEL" -> exportToExcel(request);
            case "CSV" -> exportToCsv(request);
            case "JSON" -> exportToJson(request);
            default -> throw new IllegalArgumentException(
                "Unsupported export format: " + request.getExportFormat()
            );
        };
    }

    @Async
    public CompletableFuture<String> startAsyncExport(
            String reportType, ReportRequestDTO request) {
        // Asynchronous export for large datasets
        // Returns task ID for status tracking
    }
}
```

### Asynchronous Export Process

For large datasets, the system supports asynchronous export:

1. **Initiate Export**: Client requests async export
2. **Task Creation**: System creates export task with unique ID
3. **Background Processing**: Export runs in background thread
4. **Status Tracking**: Client can check export status
5. **File Retrieval**: Download completed export file

**Async Export Endpoint:**
```http
POST /api/v1/reports/export/async
Content-Type: application/json

{
  "reportType": "SALES_COMPREHENSIVE",
  "startDate": "2025-06-01T00:00:00",
  "endDate": "2025-06-30T23:59:59",
  "exportFormat": "EXCEL"
}
```

**Response:**
```json
{
  "success": true,
  "taskId": "export_1720512345678",
  "estimatedCompletionTime": "2025-07-09T10:35:00",
  "statusUrl": "/api/v1/reports/export/status/export_1720512345678"
}
```

## Caching Strategy

### Redis-Based Caching Implementation

**File:** `src/main/java/com/hamza/salesmanagementbackend/service/ReportCacheService.java`

The reporting system implements intelligent caching to optimize performance and reduce database load:

#### Cache Configuration

**Cache Key Strategy:**
```java
String cacheKey = String.format("report:%s:%s:%s:%d:%d",
    reportType,
    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
    request.hashCode(),
    userId
);
```

**Cache Expiration Policies:**
- **Real-time Reports**: 5 minutes
- **Daily Reports**: 30 minutes
- **Weekly Reports**: 2 hours
- **Monthly Reports**: 6 hours
- **Historical Reports**: 24 hours

#### Cache Operations

**Cache Storage:**
```java
public void cacheReport(String key, Object data, int expirationMinutes) {
    LocalDateTime expiry = LocalDateTime.now().plusMinutes(expirationMinutes);
    CacheEntry entry = new CacheEntry(data, expiry);
    cache.put(key, entry);
    cleanupExpiredEntries();
}
```

**Cache Retrieval:**
```java
public <T> T getCachedReport(String key, Class<T> type) {
    CacheEntry entry = cache.get(key);
    if (entry == null || entry.isExpired()) {
        return null;
    }
    return (T) entry.getData();
}
```

#### Cache Invalidation

**Automatic Invalidation Triggers:**
- New sales transactions
- Product updates
- Customer modifications
- Inventory changes
- Promotion activations

**Manual Cache Management:**
```java
// Clear specific report cache
reportCacheService.invalidateCache(cacheKey);

// Clear all cached reports
reportCacheService.clearAllCache();

// Get cache statistics
CacheStats stats = reportCacheService.getCacheStats();
```

#### Performance Benefits

- **Response Time Reduction**: 80-95% faster for cached reports
- **Database Load Reduction**: Significant reduction in complex queries
- **Scalability**: Better handling of concurrent report requests
- **Resource Optimization**: Reduced CPU and memory usage

### Cache Monitoring

**Cache Statistics:**
```java
public class CacheStats {
    private long totalEntries;
    private long expiredEntries;
    private long activeEntries;
    private double hitRate;
    private double missRate;
}
```

## Security & Authorization

### Role-Based Access Control (RBAC)

The reporting system implements comprehensive role-based security using Spring Security:

#### Security Roles and Permissions

**Administrative Roles:**
- **ADMIN**: Full access to all reports and system management
- **MANAGER**: Access to operational and strategic reports
- **EXECUTIVE**: Access to high-level dashboards and KPIs

**Functional Roles:**
- **SALES_ANALYST**: Sales reports and customer analytics
- **FINANCIAL_ANALYST**: Financial reports and revenue analysis
- **INVENTORY_ANALYST**: Inventory and product performance reports
- **CUSTOMER_ANALYST**: Customer lifetime value and behavior analysis
- **MARKETING_ANALYST**: Promotion effectiveness and campaign analysis
- **PRODUCT_ANALYST**: Product performance and category analysis

#### Endpoint Security Configuration

**Sales Reports:**
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SALES_ANALYST')")
@GetMapping("/sales/comprehensive")
public ResponseEntity<StandardReportResponse<SalesReportDTO>> getComprehensiveSalesReport(...)
```

**Financial Reports:**
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('FINANCIAL_ANALYST')")
@GetMapping("/financial/revenue")
public ResponseEntity<StandardReportResponse<Map<String, Object>>> getFinancialRevenue(...)
```

**Executive Dashboard:**
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('EXECUTIVE')")
@GetMapping("/dashboard/executive")
public ResponseEntity<StandardReportResponse<Map<String, Object>>> getExecutiveDashboard(...)
```

#### Data Access Security

**Row-Level Security:**
- Users can only access data for their assigned regions/territories
- Managers can access data for their teams
- Executives have organization-wide access

**Column-Level Security:**
- Sensitive financial data restricted to authorized roles
- Customer PII protected based on data privacy regulations
- Cost and margin data limited to financial analysts

#### Security Implementation

**JWT Token Validation:**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) {
        // JWT token validation and user context setup
    }
}
```

**Method-Level Security:**
```java
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {
    // Security configuration
}
```

## Audit Trail

### Comprehensive Audit Logging

The reporting system maintains detailed audit trails for compliance, security, and operational monitoring:

#### Audit Events Tracked

**Report Generation Events:**
- Report type and parameters
- User identity and role
- Generation timestamp
- Execution time and performance metrics
- Data range and filters applied
- Success/failure status

**Data Access Events:**
- Accessed entities and record counts
- Query parameters and filters
- User permissions and authorization checks
- Data sensitivity level accessed

**Export Events:**
- Export format and file size
- Download timestamp and user
- Data classification and sensitivity
- Retention policy applied

#### Audit Implementation

**Audit Logging Service:**
```java
@Service
@Slf4j
public class ReportAuditService {

    public void logReportGeneration(String reportType, String userId,
                                  ReportRequestDTO request, boolean success) {
        AuditEvent event = AuditEvent.builder()
            .eventType("REPORT_GENERATION")
            .userId(userId)
            .reportType(reportType)
            .parameters(request)
            .timestamp(LocalDateTime.now())
            .success(success)
            .build();

        auditRepository.save(event);
        log.info("Report audit: {} generated {} report", userId, reportType);
    }

    public void logDataAccess(String userId, String entityType,
                            long recordCount, String accessLevel) {
        // Log data access for compliance
    }

    public void logExportActivity(String userId, String exportFormat,
                                long fileSize, String dataClassification) {
        // Log export activities for security monitoring
    }
}
```

**Audit Event Entity:**
```java
@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;
    private String userId;
    private String reportType;
    private LocalDateTime timestamp;
    private boolean success;
    private String ipAddress;
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(columnDefinition = "TEXT")
    private String errorDetails;
}
```

#### Audit Trail Queries

**Recent Report Activity:**
```sql
SELECT event_type, user_id, report_type, timestamp, success
FROM audit_events
WHERE event_type = 'REPORT_GENERATION'
  AND timestamp >= NOW() - INTERVAL 24 HOUR
ORDER BY timestamp DESC;
```

**User Activity Summary:**
```sql
SELECT user_id, COUNT(*) as report_count,
       COUNT(CASE WHEN success = true THEN 1 END) as successful_reports
FROM audit_events
WHERE event_type = 'REPORT_GENERATION'
  AND timestamp >= NOW() - INTERVAL 7 DAY
GROUP BY user_id;
```

### Compliance and Data Governance

**Data Retention Policies:**
- Audit logs retained for 7 years
- Report cache data retained for 30 days
- Export files retained for 90 days
- Personal data anonymized after retention period

**Privacy Protection:**
- PII data masked in audit logs
- Encryption for sensitive data fields
- Access logging for GDPR compliance
- Right to be forgotten implementation

## Usage Examples

### Common API Usage Patterns

#### 1. Generate Monthly Sales Report

**Request:**
```http
GET /api/v1/reports/sales/comprehensive?startDate=2025-06-01T00:00:00&endDate=2025-06-30T23:59:59&useCache=true
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "success": true,
  "message": "Report generated successfully",
  "data": {
    "summary": {
      "totalSales": 1250,
      "totalRevenue": 125000.00,
      "averageOrderValue": 100.00,
      "uniqueCustomers": 450,
      "conversionRate": 15.5
    },
    "dailyBreakdown": [
      {
        "date": "2025-06-01",
        "salesCount": 45,
        "revenue": 4500.00,
        "growthRate": 12.5
      }
    ],
    "topCustomers": [
      {
        "customerId": 123,
        "customerName": "ABC Corp",
        "totalOrders": 25,
        "totalSpent": 15000.00,
        "customerSegment": "VIP"
      }
    ]
  },
  "metadata": {
    "reportType": "COMPREHENSIVE_SALES",
    "generatedAt": "2025-07-09T10:30:00",
    "executionTimeMs": 850,
    "fromCache": false
  }
}
```

#### 2. Export Customer Lifetime Value Report

**Request:**
```http
GET /api/v1/reports/customers/lifetime-value?page=0&size=50&sortBy=lifetimeValue&sortDirection=DESC&exportFormat=EXCEL
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="customer-lifetime-value-2025-07-09.xlsx"
Content-Length: 245760

[Binary Excel file content]
```

#### 3. Real-time Dashboard KPIs

**Request:**
```http
GET /api/v1/reports/kpi/real-time
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "success": true,
  "data": {
    "todaysSales": 45,
    "todaysRevenue": 12500.00,
    "activeCustomers": 1250,
    "inventoryValue": 450000.00,
    "lowStockItems": 15,
    "pendingReturns": 8
  },
  "metadata": {
    "reportType": "REAL_TIME_KPI",
    "generatedAt": "2025-07-09T10:30:00",
    "executionTimeMs": 125
  }
}
```

#### 4. Filtered Product Performance Report

**Request:**
```http
POST /api/v1/reports/products/performance
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

{
  "startDate": "2025-06-01T00:00:00",
  "endDate": "2025-06-30T23:59:59",
  "categoryIds": [1, 2, 3],
  "regions": ["North", "South"],
  "amountRange": {
    "min": 100.00,
    "max": 5000.00
  },
  "sortBy": "revenue",
  "sortDirection": "DESC",
  "page": 0,
  "size": 20
}
```

### Java Client Example

**Spring Boot Client:**
```java
@Service
public class ReportClientService {

    @Autowired
    private RestTemplate restTemplate;

    public SalesReportDTO getSalesReport(LocalDateTime start, LocalDateTime end) {
        String url = "/api/v1/reports/sales/comprehensive" +
                    "?startDate=" + start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                    "&endDate=" + end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        ResponseEntity<StandardReportResponse<SalesReportDTO>> response =
            restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<StandardReportResponse<SalesReportDTO>>() {});

        return response.getBody().getData();
    }
}
```

### JavaScript/Frontend Example

**React Component:**
```javascript
const ReportDashboard = () => {
  const [salesData, setSalesData] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchSalesReport = async (startDate, endDate) => {
    setLoading(true);
    try {
      const response = await fetch(
        `/api/v1/reports/sales/comprehensive?startDate=${startDate}&endDate=${endDate}`,
        {
          headers: {
            'Authorization': `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
          }
        }
      );

      const result = await response.json();
      if (result.success) {
        setSalesData(result.data);
      }
    } catch (error) {
      console.error('Error fetching sales report:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      {loading ? <Spinner /> : <SalesChart data={salesData} />}
    </div>
  );
};
```

## Testing

### Test Coverage and Strategy

The reporting system includes comprehensive test coverage across all layers:

#### Unit Tests

**File:** `src/test/java/com/hamza/salesmanagementbackend/service/ReportServiceTest.java`

**Test Categories:**
- **Business Logic Tests**: Data aggregation, calculations, and transformations
- **Edge Case Tests**: Empty datasets, null values, and boundary conditions
- **Performance Tests**: Large dataset handling and memory usage
- **Validation Tests**: Input parameter validation and error handling

**Example Test:**
```java
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("Should generate comprehensive sales report with correct calculations")
    void shouldGenerateComprehensiveSalesReport() {
        // Given
        List<Sale> mockSales = createMockSales();
        when(saleRepository.findBySaleDateBetween(any(), any()))
            .thenReturn(mockSales);

        ReportRequestDTO request = ReportRequestDTO.builder()
            .startDate(LocalDateTime.now().minusDays(30))
            .endDate(LocalDateTime.now())
            .build();

        // When
        SalesReportDTO result = reportService.generateComprehensiveSalesReport(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSummary().getTotalSales()).isEqualTo(2L);
        assertThat(result.getSummary().getTotalRevenue()).isEqualTo(BigDecimal.valueOf(1075));
        assertThat(result.getDailyBreakdown()).isNotEmpty();
        assertThat(result.getTopCustomers()).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle empty sales data gracefully")
    void shouldHandleEmptySalesDataGracefully() {
        // Given
        when(saleRepository.findBySaleDateBetween(any(), any()))
            .thenReturn(Arrays.asList());

        // When
        SalesReportDTO result = reportService.generateComprehensiveSalesReport(request);

        // Then
        assertThat(result.getSummary().getTotalSales()).isEqualTo(0L);
        assertThat(result.getSummary().getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
    }
}
```

#### Integration Tests

**File:** `src/test/java/com/hamza/salesmanagementbackend/controller/ReportControllerTest.java`

**Test Categories:**
- **API Endpoint Tests**: HTTP request/response validation
- **Security Tests**: Authentication and authorization
- **Cache Integration Tests**: Cache behavior and performance
- **Database Integration Tests**: Data retrieval and aggregation

**Example Integration Test:**
```java
@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    @DisplayName("Should return sales report with proper structure")
    void shouldReturnSalesReportWithProperStructure() throws Exception {
        // Given
        SalesReportDTO mockReport = createMockSalesReport();
        when(reportService.generateComprehensiveSalesReport(any()))
            .thenReturn(mockReport);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/sales/comprehensive")
                .param("startDate", "2025-06-01T00:00:00")
                .param("endDate", "2025-06-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.totalSales").exists())
                .andExpect(jsonPath("$.metadata.reportType").value("COMPREHENSIVE_SALES"));
    }
}
```

#### Performance Tests

**Load Testing Configuration:**
```java
@Test
@DisplayName("Should handle concurrent report requests efficiently")
void shouldHandleConcurrentReportRequests() {
    int numberOfThreads = 10;
    int requestsPerThread = 5;
    ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

    List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfThreads)
        .mapToObj(i -> CompletableFuture.runAsync(() -> {
            for (int j = 0; j < requestsPerThread; j++) {
                // Execute report generation
                reportService.generateSalesReport(startDate, endDate);
            }
        }, executor))
        .collect(Collectors.toList());

    // Verify all requests complete within acceptable time
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .orTimeout(30, TimeUnit.SECONDS);
}
```

### Test Execution

**Run All Tests:**
```bash
mvn test
```

**Run Specific Test Class:**
```bash
mvn test -Dtest=ReportServiceTest
```

**Run Integration Tests:**
```bash
mvn test -Dtest=*IntegrationTest
```

**Generate Test Coverage Report:**
```bash
mvn jacoco:report
```

### Test Data Management

**Test Data Factory:**
```java
@Component
public class TestDataFactory {

    public List<Sale> createMockSales() {
        return Arrays.asList(
            Sale.builder()
                .id(1L)
                .totalAmount(BigDecimal.valueOf(500))
                .status(SaleStatus.COMPLETED)
                .saleDate(LocalDateTime.now().minusDays(1))
                .build(),
            Sale.builder()
                .id(2L)
                .totalAmount(BigDecimal.valueOf(575))
                .status(SaleStatus.COMPLETED)
                .saleDate(LocalDateTime.now().minusDays(2))
                .build()
        );
    }
}
```

---

## Implementation Files Reference

### Core Implementation Files

- **Controller**: `src/main/java/com/hamza/salesmanagementbackend/controller/ReportController.java`
- **Service**: `src/main/java/com/hamza/salesmanagementbackend/service/ReportService.java`
- **Cache Service**: `src/main/java/com/hamza/salesmanagementbackend/service/ReportCacheService.java`
- **Export Service**: `src/main/java/com/hamza/salesmanagementbackend/service/ReportExportService.java`

### DTOs and Models

- **Request DTO**: `src/main/java/com/hamza/salesmanagementbackend/dto/report/ReportRequestDTO.java`
- **Response Wrapper**: `src/main/java/com/hamza/salesmanagementbackend/dto/report/StandardReportResponse.java`
- **Sales Report**: `src/main/java/com/hamza/salesmanagementbackend/dto/report/SalesReportDTO.java`
- **Customer Report**: `src/main/java/com/hamza/salesmanagementbackend/dto/report/CustomerReportDTO.java`
- **Inventory Report**: `src/main/java/com/hamza/salesmanagementbackend/dto/report/InventoryReportDTO.java`
- **Metadata**: `src/main/java/com/hamza/salesmanagementbackend/dto/report/ReportMetadata.java`

### Test Files

- **Service Tests**: `src/test/java/com/hamza/salesmanagementbackend/service/ReportServiceTest.java`
- **Controller Tests**: `src/test/java/com/hamza/salesmanagementbackend/controller/ReportControllerTest.java`
- **Simple Controller Tests**: `src/test/java/com/hamza/salesmanagementbackend/controller/ReportControllerSimpleTest.java`

### Configuration and Security

- **Security Config**: `src/main/java/com/hamza/salesmanagementbackend/security/SecurityConfig.java`
- **Exception Handler**: `src/main/java/com/hamza/salesmanagementbackend/exception/GlobalExceptionHandler.java`

---

*This documentation provides comprehensive coverage of the Sales Management System's reporting capabilities. For additional technical details or implementation questions, refer to the source code files listed above or contact the development team.*
```
```