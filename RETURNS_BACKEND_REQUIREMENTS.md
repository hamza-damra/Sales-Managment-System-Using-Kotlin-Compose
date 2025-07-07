# Returns Management - Backend Implementation Requirements

## 📋 Overview

This document outlines the backend API endpoints and business logic that need to be implemented in the **separate backend project** to support the fully implemented Returns Management frontend in the Kotlin Compose application.

## 🎯 Frontend Implementation Status

✅ **FRONTEND COMPLETE** - The Kotlin Compose frontend has comprehensive returns management functionality implemented and ready to connect to the backend APIs listed below.

## 🔧 Required Backend API Endpoints

### 1. **Core Returns Management Endpoints**

#### **GET /api/returns**
**Purpose**: Retrieve all returns with pagination and filtering
**Query Parameters**:
```
- page (int, default=0) - Page number
- size (int, default=10) - Page size (max 100)
- sortBy (string, default="id") - Sort field (id, returnDate, status, totalRefundAmount)
- sortDir (string, default="desc") - Sort direction (asc, desc)
- status (string, optional) - Filter by status (PENDING, APPROVED, REJECTED, REFUNDED, EXCHANGED)
- search (string, optional) - Search in return number, customer name, notes
```

**Response Format**:
```json
{
  "content": [
    {
      "id": 1,
      "returnNumber": "RET-2025-001",
      "originalSaleId": 123,
      "originalSaleNumber": "SALE-2025-123",
      "customerId": 45,
      "customerName": "أحمد محمد",
      "returnDate": "2025-07-07T10:30:00",
      "reason": "DEFECTIVE",
      "status": "PENDING",
      "totalRefundAmount": 299.99,
      "notes": "المنتج معيب ولا يعمل بشكل صحيح",
      "processedBy": null,
      "processedDate": null,
      "refundMethod": null,
      "items": [
        {
          "id": 1,
          "productId": 67,
          "productName": "سماعات لاسلكية",
          "quantity": 1,
          "unitPrice": 299.99,
          "totalPrice": 299.99,
          "condition": "DEFECTIVE"
        }
      ],
      "createdAt": "2025-07-07T10:30:00",
      "updatedAt": "2025-07-07T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 156,
  "totalPages": 16,
  "first": true,
  "last": false,
  "numberOfElements": 10,
  "empty": false
}
```

#### **GET /api/returns/{id}**
**Purpose**: Retrieve a specific return by ID
**Response**: Single return object with same structure as above

#### **POST /api/returns**
**Purpose**: Create a new return request
**Request Body**:
```json
{
  "originalSaleId": 123,
  "customerId": 45,
  "reason": "DEFECTIVE",
  "notes": "المنتج معيب ولا يعمل بشكل صحيح",
  "items": [
    {
      "productId": 67,
      "quantity": 1,
      "unitPrice": 299.99,
      "condition": "DEFECTIVE"
    }
  ]
}
```
**Response**: Created return object with generated ID and return number

#### **PUT /api/returns/{id}**
**Purpose**: Update an existing return
**Request Body**: Same as POST but with updated fields
**Response**: Updated return object

#### **DELETE /api/returns/{id}**
**Purpose**: Delete/cancel a return request
**Response**: 204 No Content on success

### 2. **Advanced Return Processing Endpoints**

#### **POST /api/returns/{id}/approve**
**Purpose**: Approve a return request
**Request Body**:
```json
{
  "notes": "تم الموافقة على الإرجاع",
  "processedBy": "admin@example.com"
}
```
**Response**: Updated return with status "APPROVED"

#### **POST /api/returns/{id}/reject**
**Purpose**: Reject a return request
**Request Body**:
```json
{
  "notes": "لا يمكن قبول الإرجاع - تجاوز المدة المسموحة",
  "processedBy": "admin@example.com"
}
```
**Response**: Updated return with status "REJECTED"

#### **POST /api/returns/{id}/refund**
**Purpose**: Process refund for an approved return
**Request Body**:
```json
{
  "refundMethod": "ORIGINAL_PAYMENT",
  "notes": "تم الاسترداد إلى طريقة الدفع الأصلية",
  "processedBy": "admin@example.com"
}
```
**Response**: Updated return with status "REFUNDED"

## 📊 Required Data Models

### **Return Entity**
```java
@Entity
@Table(name = "returns")
public class Return {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String returnNumber; // Auto-generated: RET-YYYY-XXX
    
    @Column(nullable = false)
    private Long originalSaleId;
    
    @Column(nullable = false)
    private Long customerId;
    
    @Column(nullable = false)
    private LocalDateTime returnDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnReason reason;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status = ReturnStatus.PENDING;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalRefundAmount;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    private String processedBy;
    private LocalDateTime processedDate;
    
    @Enumerated(EnumType.STRING)
    private RefundMethod refundMethod;
    
    @OneToMany(mappedBy = "return", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReturnItem> items = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### **ReturnItem Entity**
```java
@Entity
@Table(name = "return_items")
public class ReturnItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private Return return;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCondition condition;
}
```

### **Required Enums**
```java
public enum ReturnReason {
    DEFECTIVE("معيب"),
    WRONG_ITEM("منتج خاطئ"),
    CUSTOMER_CHANGE_MIND("تغيير رأي العميل"),
    EXPIRED("منتهي الصلاحية"),
    DAMAGED_SHIPPING("تضرر أثناء الشحن"),
    OTHER("أخرى");
}

public enum ReturnStatus {
    PENDING("في الانتظار"),
    APPROVED("موافق عليه"),
    REJECTED("مرفوض"),
    REFUNDED("تم الاسترداد"),
    EXCHANGED("تم الاستبدال");
}

public enum RefundMethod {
    ORIGINAL_PAYMENT("طريقة الدفع الأصلية"),
    STORE_CREDIT("رصيد المتجر"),
    CASH("نقداً");
}

public enum ItemCondition {
    NEW("جديد"),
    USED("مستعمل"),
    DEFECTIVE("معيب"),
    DAMAGED("تالف");
}
```

## 🔄 Required Business Logic

### 1. **Return Creation Logic**
- Validate that the original sale exists and belongs to the customer
- Check return policy (time limits, eligible products)
- Calculate refund amount based on return policy
- Generate unique return number (RET-YYYY-XXX format)
- Create return items from selected sale items
- Set initial status to PENDING

### 2. **Return Processing Logic**
- **Approve**: Change status to APPROVED, set processedBy and processedDate
- **Reject**: Change status to REJECTED, set processedBy and processedDate
- **Refund**: Change status to REFUNDED, process actual refund, restore inventory

### 3. **Inventory Management**
- When return is approved: Reserve returned items
- When return is refunded: Add items back to inventory
- When return is rejected: No inventory changes

### 4. **Validation Rules**
- Return must be within allowed time period (configurable)
- Customer must match original sale customer
- Return quantity cannot exceed original sale quantity
- Return amount cannot exceed original sale amount

## 🔐 Security Requirements

### **Authentication & Authorization**
- All endpoints require valid JWT token
- Role-based access control:
  - **CUSTOMER**: Can create returns for their own sales
  - **STAFF**: Can view and process all returns
  - **ADMIN**: Full access to all return operations

### **Data Validation**
- Validate all input data
- Sanitize text inputs
- Check business rules before processing
- Prevent duplicate return requests

## 📈 Performance Requirements

### **Database Optimization**
- Index on returnNumber for fast lookups
- Index on customerId for customer-specific queries
- Index on status for filtering
- Index on returnDate for date-based queries

### **Caching Strategy**
- Cache frequently accessed return data
- Cache customer and product information
- Implement proper cache invalidation

## 🧪 Testing Requirements

### **Unit Tests**
- Test all business logic methods
- Test validation rules
- Test enum conversions
- Test calculation logic

### **Integration Tests**
- Test complete return workflows
- Test API endpoints with various scenarios
- Test database transactions
- Test error handling

## 📝 Implementation Priority

### **Phase 1: Core Functionality** (HIGH PRIORITY)
1. Basic CRUD operations for returns
2. Return creation with validation
3. Return listing with pagination
4. Return details retrieval

### **Phase 2: Advanced Processing** (MEDIUM PRIORITY)
1. Approve/reject functionality
2. Refund processing
3. Inventory integration
4. Business rule validation

### **Phase 3: Analytics & Reporting** (LOW PRIORITY)
1. Return analytics endpoints
2. Performance metrics
3. Reporting features
4. Dashboard integration

## 🔗 Integration Points

### **Required Integrations**
- **Sales Service**: Validate original sales, get sale details
- **Customer Service**: Get customer information
- **Product Service**: Get product details, update inventory
- **Payment Service**: Process refunds
- **Notification Service**: Send return status updates

This comprehensive backend implementation will fully support the already-completed frontend returns management functionality.
