# Sales Management System - Frontend API Documentation

## Table of Contents
1. [Overview](#overview)
2. [Base Configuration](#base-configuration)
3. [Authentication](#authentication)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)
7. [Best Practices](#best-practices)

## Overview

This document provides comprehensive API documentation for frontend developers working with the Sales Management System backend. The backend is built with Spring Boot and provides RESTful APIs for managing customers, products, sales, and generating reports.

## Base Configuration

### Base URL
```
http://localhost:8081/api
```

### Headers
All requests should include:
```javascript
{
  "Content-Type": "application/json",
  "Accept": "application/json"
}
```

For authenticated requests, include:
```javascript
{
  "Authorization": "Bearer <your-jwt-token>"
}
```

### CORS
The backend supports CORS with origins set to "*" for development. In production, this should be configured to specific domains.

## Authentication

### User Roles
The system supports three user roles:
- `USER` - Standard user with basic permissions
- `ADMIN` - Administrator with full system access
- `MANAGER` - Manager with elevated permissions

### Sign Up
**Endpoint:** `POST /auth/signup`

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "createdAt": "2025-07-03T10:00:00"
  }
}
```

### Sign In
**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Refresh Token
**Endpoint:** `POST /auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## API Endpoints

### Customers

#### Get All Customers
**Endpoint:** `GET /customers`

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10, max=100) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="asc") - Sort direction (asc/desc)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "John Doe",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "phone": "+1234567890",
      "address": "123 Main St",
      "customerType": "REGULAR",
      "customerStatus": "ACTIVE",
      "totalPurchases": 1500.00,
      "loyaltyPoints": 150,
      "createdAt": "2025-07-03T10:00:00",
      "updatedAt": "2025-07-03T10:00:00"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

#### Get Customer by ID
**Endpoint:** `GET /customers/{id}`

**Response (200 OK):** Single CustomerDTO object

#### Create Customer
**Endpoint:** `POST /customers`

**Request Body:**
```json
{
  "name": "Jane Smith",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com",
  "phone": "+1987654321",
  "address": "456 Oak Ave",
  "customerType": "PREMIUM",
  "billingAddress": "456 Oak Ave",
  "shippingAddress": "456 Oak Ave"
}
```

**Response (201 Created):** Created CustomerDTO object

#### Update Customer
**Endpoint:** `PUT /customers/{id}`

**Request Body:** Same as Create Customer

**Response (200 OK):** Updated CustomerDTO object

#### Delete Customer
**Endpoint:** `DELETE /customers/{id}`

**Response (204 No Content)**

#### Search Customers
**Endpoint:** `GET /customers/search`

**Query Parameters:**
- `query` (string, required) - Search term
- `page` (int, default=0)
- `size` (int, default=10)

**Response (200 OK):** Paginated CustomerDTO list

### Products

#### Get All Products
**Endpoint:** `GET /products`

**Query Parameters:**
- `page` (int, default=0)
- `size` (int, default=10, max=100)
- `sortBy` (string, default="id")
- `sortDir` (string, default="asc")
- `category` (string, optional) - Filter by category

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Pro",
      "description": "High-performance laptop",
      "price": 1299.99,
      "costPrice": 800.00,
      "stockQuantity": 50,
      "category": "Electronics",
      "sku": "LAP-001",
      "brand": "TechBrand",
      "productStatus": "ACTIVE",
      "minStockLevel": 10,
      "reorderPoint": 15,
      "isTaxable": true,
      "taxRate": 8.5,
      "createdAt": "2025-07-03T10:00:00",
      "updatedAt": "2025-07-03T10:00:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 10
}
```

#### Get Product by ID
**Endpoint:** `GET /products/{id}`

#### Create Product
**Endpoint:** `POST /products`

**Request Body:**
```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 29.99,
  "costPrice": 15.00,
  "stockQuantity": 100,
  "category": "Electronics",
  "sku": "MOU-001",
  "brand": "TechBrand",
  "minStockLevel": 20,
  "reorderPoint": 30,
  "isTaxable": true,
  "taxRate": 8.5
}
```

#### Update Product
**Endpoint:** `PUT /products/{id}`

#### Delete Product
**Endpoint:** `DELETE /products/{id}`

#### Search Products
**Endpoint:** `GET /products/search`

**Query Parameters:**
- `query` (string, required)
- `page`, `size` (pagination)

### Sales

#### Get All Sales
**Endpoint:** `GET /sales`

**Query Parameters:**
- `page`, `size`, `sortBy`, `sortDir` (pagination/sorting)
- `status` (SaleStatus, optional) - Filter by status
- `startDate` (ISO datetime, optional) - Filter from date
- `endDate` (ISO datetime, optional) - Filter to date

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "John Doe",
      "saleDate": "2025-07-03T14:30:00",
      "totalAmount": 159.98,
      "status": "COMPLETED",
      "paymentMethod": "CREDIT_CARD",
      "paymentStatus": "PAID",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Wireless Mouse",
          "quantity": 2,
          "unitPrice": 29.99,
          "totalPrice": 59.98
        }
      ]
    }
  ]
}
```

#### Get Sale by ID
**Endpoint:** `GET /sales/{id}`

#### Create Sale
**Endpoint:** `POST /sales`

**Request Body:**
```json
{
  "customerId": 1,
  "paymentMethod": "CREDIT_CARD",
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 29.99
    }
  ],
  "notes": "Customer requested express delivery"
}
```

#### Update Sale
**Endpoint:** `PUT /sales/{id}`

#### Delete Sale
**Endpoint:** `DELETE /sales/{id}`

#### Get Sales by Customer
**Endpoint:** `GET /sales/customer/{customerId}`

### Reports

#### Sales Report
**Endpoint:** `GET /reports/sales`

**Query Parameters:**
- `startDate` (ISO datetime, required)
- `endDate` (ISO datetime, required)

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-06-01T00:00:00",
    "endDate": "2025-07-03T23:59:59"
  },
  "summary": {
    "totalRevenue": 15000.00,
    "totalSales": 45,
    "averageOrderValue": 333.33
  },
  "salesByStatus": {
    "COMPLETED": 40,
    "PENDING": 3,
    "CANCELLED": 2
  },
  "dailyRevenue": {
    "2025-07-01": 500.00,
    "2025-07-02": 750.00,
    "2025-07-03": 600.00
  }
}
```

#### Revenue Trends
**Endpoint:** `GET /reports/revenue`

**Query Parameters:**
- `months` (int, default=6) - Number of months to analyze

#### Top Products Report
**Endpoint:** `GET /reports/top-products`

**Query Parameters:**
- `startDate` (ISO datetime, required)
- `endDate` (ISO datetime, required)

#### Customer Analytics
**Endpoint:** `GET /reports/customer-analytics`

#### Inventory Report
**Endpoint:** `GET /reports/inventory`

#### Dashboard Summary
**Endpoint:** `GET /reports/dashboard`

**Response (200 OK):**
```json
{
  "period": "Last 30 days",
  "sales": {
    "totalRevenue": 25000.00,
    "totalSales": 75
  },
  "customers": {
    "totalCustomers": 150,
    "activeCustomers": 120
  },
  "inventory": {
    "totalProducts": 200,
    "lowStockItems": 15
  },
  "generatedAt": "2025-07-03T15:30:00"
}
```

## Data Models

### CustomerDTO
```typescript
interface CustomerDTO {
  id?: number;
  name: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  address?: string;
  dateOfBirth?: string; // ISO date
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';
  customerType?: 'REGULAR' | 'PREMIUM' | 'VIP';
  customerStatus?: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  billingAddress?: string;
  shippingAddress?: string;
  preferredPaymentMethod?: string;
  creditLimit?: number;
  currentBalance?: number;
  loyaltyPoints?: number;
  taxNumber?: string;
  companyName?: string;
  website?: string;
  notes?: string;
  lastPurchaseDate?: string; // ISO datetime
  totalPurchases?: number;
  isEmailVerified?: boolean;
  isPhoneVerified?: boolean;
  createdAt?: string; // ISO datetime
  updatedAt?: string; // ISO datetime
}
```

### ProductDTO
```typescript
interface ProductDTO {
  id?: number;
  name: string;
  description?: string;
  price: number;
  costPrice?: number;
  stockQuantity?: number;
  category?: string;
  sku?: string;
  brand?: string;
  modelNumber?: string;
  barcode?: string;
  weight?: number;
  length?: number;
  width?: number;
  height?: number;
  productStatus?: 'ACTIVE' | 'INACTIVE' | 'DISCONTINUED';
  minStockLevel?: number;
  maxStockLevel?: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  supplierName?: string;
  supplierCode?: string;
  warrantyPeriod?: number;
  expiryDate?: string; // ISO date
  manufacturingDate?: string; // ISO date
  tags?: string[];
  imageUrl?: string;
  additionalImages?: string[];
  isSerialized?: boolean;
  isDigital?: boolean;
  isTaxable?: boolean;
  taxRate?: number;
  unitOfMeasure?: string;
  discountPercentage?: number;
  locationInWarehouse?: string;
  totalSold?: number;
  totalRevenue?: number;
  lastSoldDate?: string; // ISO datetime
  lastRestockedDate?: string; // ISO datetime
  notes?: string;
  createdAt?: string; // ISO datetime
  updatedAt?: string; // ISO datetime
}
```

### SaleDTO
```typescript
interface SaleDTO {
  id?: number;
  customerId: number;
  customerName?: string;
  saleDate?: string; // ISO datetime
  totalAmount: number;
  status?: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'REFUNDED';
  items: SaleItemDTO[];
  saleNumber?: string;
  referenceNumber?: string;
  subtotal?: number;
  discountAmount?: number;
  discountPercentage?: number;
  taxAmount?: number;
  taxPercentage?: number;
  shippingCost?: number;
  paymentMethod?: 'CASH' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'BANK_TRANSFER' | 'CHECK' | 'DIGITAL_WALLET';
  paymentStatus?: 'PENDING' | 'PAID' | 'PARTIAL' | 'OVERDUE' | 'REFUNDED';
  paymentDate?: string; // ISO datetime
  dueDate?: string; // ISO date
  billingAddress?: string;
  shippingAddress?: string;
  salesPerson?: string;
  salesChannel?: string;
  saleType?: 'REGULAR' | 'WHOLESALE' | 'RETAIL' | 'ONLINE' | 'RETURN';
  currency?: string;
  exchangeRate?: number;
  notes?: string;
  internalNotes?: string;
  termsAndConditions?: string;
  warrantyInfo?: string;
  deliveryDate?: string; // ISO datetime
  expectedDeliveryDate?: string; // ISO date
  deliveryStatus?: 'PENDING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
  trackingNumber?: string;
  isGift?: boolean;
  giftMessage?: string;
  loyaltyPointsEarned?: number;
  loyaltyPointsUsed?: number;
  isReturn?: boolean;
  originalSaleId?: number;
  returnReason?: string;
  profitMargin?: number;
  costOfGoodsSold?: number;
  createdAt?: string; // ISO datetime
  updatedAt?: string; // ISO datetime
}
```

### SaleItemDTO
```typescript
interface SaleItemDTO {
  id?: number;
  productId: number;
  productName?: string;
  quantity: number;
  unitPrice: number;
  originalUnitPrice?: number;
  costPrice?: number;
  discountPercentage?: number;
  discountAmount?: number;
  taxPercentage?: number;
  taxAmount?: number;
  subtotal?: number;
  totalPrice?: number;
  serialNumbers?: string;
  warrantyInfo?: string;
  notes?: string;
  isReturned?: boolean;
  returnedQuantity?: number;
  unitOfMeasure?: string;
}
```

### User Roles
```typescript
type UserRole = 'USER' | 'ADMIN' | 'MANAGER';
```

### Pagination Response
```typescript
interface PageResponse<T> {
  content: T[];
  pageable: {
    sort: {
      sorted: boolean;
      unsorted: boolean;
    };
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
```
