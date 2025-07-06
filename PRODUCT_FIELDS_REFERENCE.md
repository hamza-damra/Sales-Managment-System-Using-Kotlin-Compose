# Product Fields Reference for Import/Export

## Overview
This document provides a comprehensive reference for all product fields supported in the import/export functionality of the Sales Management System.

## Field Categories

### 1. Required Fields ⭐
These fields are mandatory for product creation and must be provided in import files:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `name` | String | Product name | "منتج تجريبي" |
| `price` | Double | Selling price | 100.0 |
| `stockQuantity` | Integer | Current stock quantity | 50 |

### 2. Basic Product Information
Core product details and identification:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `description` | String | Product description | "وصف المنتج التجريبي" |
| `category` | String | Product category | "إلكترونيات" |
| `sku` | String | Stock Keeping Unit | "SKU001" |
| `costPrice` | Double | Cost/purchase price | 80.0 |
| `brand` | String | Brand name | "علامة تجارية" |
| `modelNumber` | String | Model number | "MODEL001" |
| `barcode` | String | Barcode/UPC | "1234567890" |

### 3. Physical Properties
Product dimensions and weight:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `weight` | Double | Weight in kg | 1.5 |
| `length` | Double | Length in cm | 10.0 |
| `width` | Double | Width in cm | 5.0 |
| `height` | Double | Height in cm | 3.0 |

### 4. Product Status and Classification
Status and type information:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `productStatus` | String | Product status | "ACTIVE", "INACTIVE", "DISCONTINUED" |

### 5. Stock Management
Inventory control settings:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `minStockLevel` | Integer | Minimum stock alert level | 10 |
| `maxStockLevel` | Integer | Maximum stock capacity | 100 |
| `reorderPoint` | Integer | Reorder trigger point | 20 |
| `reorderQuantity` | Integer | Quantity to reorder | 50 |

### 6. Supplier Information
Supplier and sourcing details:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `supplierName` | String | Supplier name | "مورد تجريبي" |
| `supplierCode` | String | Supplier code | "SUP001" |

### 7. Product Lifecycle
Dates and warranty information:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `warrantyPeriod` | Integer | Warranty period in months | 12 |
| `expiryDate` | String | Expiry date (ISO format) | "2025-12-31" |
| `manufacturingDate` | String | Manufacturing date (ISO format) | "2024-01-15" |

### 8. Tags and Images
Visual and categorization data:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `tags` | List<String> | Product tags (semicolon-separated in CSV) | "إلكترونيات;جديد;مميز" |
| `imageUrl` | String | Main product image URL | "https://example.com/image.jpg" |
| `additionalImages` | List<String> | Additional images (semicolon-separated in CSV) | "https://example.com/img1.jpg;https://example.com/img2.jpg" |

### 9. Product Characteristics
Boolean flags for product properties:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `isSerialized` | Boolean | Has serial numbers | true/false |
| `isDigital` | Boolean | Digital product | true/false |
| `isTaxable` | Boolean | Subject to tax | true/false |

### 10. Pricing and Measurement
Pricing and unit information:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `taxRate` | Double | Tax rate percentage | 15.0 |
| `unitOfMeasure` | String | Unit of measurement | "PCS", "KG", "M" |
| `discountPercentage` | Double | Default discount percentage | 5.0 |

### 11. Warehouse and Location
Storage and location information:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `locationInWarehouse` | String | Warehouse location | "A1-B2-C3" |

### 12. Sales Tracking (Read-Only)
Sales performance data (typically not imported, but exported):

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `totalSold` | Integer | Total units sold | 25 |
| `totalRevenue` | Double | Total revenue generated | 2500.0 |
| `lastSoldDate` | String | Last sale date (ISO format) | "2024-01-10T14:30:00" |
| `lastRestockedDate` | String | Last restock date (ISO format) | "2024-01-05T09:00:00" |

### 13. Additional Information
Miscellaneous data:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `notes` | String | Additional notes | "ملاحظات إضافية حول المنتج" |
| `createdAt` | String | Creation timestamp (ISO format) | "2024-01-01T10:00:00" |
| `updatedAt` | String | Last update timestamp (ISO format) | "2024-01-10T15:30:00" |

## Import Guidelines

### Required Fields Validation
- **name**: Must not be empty
- **price**: Must be a positive number
- **stockQuantity**: Must be a non-negative integer

### Optional Fields Handling
- Empty values are treated as null/default
- Boolean fields accept: true/false, 1/0, yes/no
- Date fields should be in ISO format (YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss)
- List fields (tags, additionalImages) use semicolon (;) as separator in CSV

### Data Type Validation
- **String fields**: Any text value
- **Double fields**: Decimal numbers (use . as decimal separator)
- **Integer fields**: Whole numbers only
- **Boolean fields**: true/false values
- **Date fields**: ISO 8601 format

## Export Features

### CSV Export
- All fields included in export
- Headers in English for compatibility
- Values properly escaped for CSV format
- List fields joined with semicolons

### JSON Export
- Complete ProductDTO structure
- Proper data types preserved
- Null values included for completeness
- Arrays for list fields

### Excel Export
- Uses existing ExcelExportUtils
- Formatted output with styling
- All fields included in structured format

## Sample Data

### CSV Template
The system generates a comprehensive CSV template with:
- All field headers
- Sample data for each field type
- Proper formatting examples
- Comments explaining field usage

### Import Examples
```csv
name,price,stockQuantity,description,category,sku
"منتج تجريبي",100.0,50,"وصف المنتج","إلكترونيات","SKU001"
```

### JSON Example
```json
[
  {
    "name": "منتج تجريبي",
    "price": 100.0,
    "stockQuantity": 50,
    "description": "وصف المنتج",
    "category": "إلكترونيات",
    "sku": "SKU001"
  }
]
```

## Best Practices

### For Import
1. Always include required fields
2. Use consistent date formats
3. Validate data before import
4. Use sample template as reference
5. Test with small batches first

### For Export
1. Export regularly for backup
2. Include all data for complete records
3. Choose appropriate format for use case
4. Verify exported data completeness

### Data Quality
1. Maintain consistent naming conventions
2. Use standardized units of measure
3. Keep supplier codes consistent
4. Validate URLs for images
5. Use meaningful tags and categories

## Troubleshooting

### Common Import Issues
- **Missing required fields**: Ensure name, price, and stockQuantity are provided
- **Invalid data types**: Check numeric fields for proper formatting
- **Date format errors**: Use ISO format (YYYY-MM-DD)
- **Boolean value errors**: Use true/false or 1/0

### Export Issues
- **Large datasets**: Consider filtering or pagination
- **File permissions**: Ensure write access to target directory
- **Character encoding**: UTF-8 encoding for Arabic text

## Technical Notes

### Field Mapping
- Product domain model maps to ProductDTO for API operations
- All fields are preserved during conversion
- Null values handled gracefully
- Type conversions performed safely

### Performance Considerations
- Large imports processed in batches
- Export operations run asynchronously
- Progress feedback provided for long operations
- Memory optimization for large datasets
