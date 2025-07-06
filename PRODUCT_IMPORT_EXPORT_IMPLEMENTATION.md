# Product Import/Export Implementation

## Overview
This implementation adds comprehensive import and export functionality to the ProductsScreen in the Kotlin Compose Sales Management System. The solution supports multiple file formats (Excel, CSV, JSON) and follows the existing architecture patterns.

## Features Implemented

### 1. Export Functionality
- **Excel Export**: Uses existing `ExcelExportUtils` for formatted Excel files (.xlsx)
- **CSV Export**: Custom implementation for comma-separated values (.csv)
- **JSON Export**: Structured JSON format with all product fields (.json)
- **File Dialog Integration**: Native file save dialogs for each format
- **Progress Feedback**: Loading states and success/error messages

### 2. Import Functionality
- **Multi-format Support**: Imports from CSV and JSON files
- **Data Validation**: Validates required fields (name, price, stockQuantity)
- **Error Handling**: Comprehensive error reporting with line-by-line feedback
- **Batch Processing**: Efficiently processes multiple products
- **API Integration**: Creates products via existing backend API

### 3. UI Integration
- **Export Dialog**: Modal dialog with format selection options
- **Import Dialog**: User-friendly import interface with warnings
- **Quick Actions**: Added to existing Quick Actions section
- **Progress Indicators**: Visual feedback during operations
- **Snackbar Messages**: Success/error notifications

## Files Created/Modified

### New Files Created:

1. **`src/main/kotlin/utils/FileDialogUtils.kt`**
   - Centralized file dialog utilities
   - Support for Excel, CSV, JSON, and import file dialogs
   - File validation and extension handling

2. **`src/main/kotlin/utils/ProductImportUtils.kt`**
   - CSV and JSON import parsing
   - Data validation and error reporting
   - Export utilities for CSV and JSON formats
   - Comprehensive error handling

3. **`src/main/kotlin/services/ProductImportExportService.kt`**
   - Service layer for import/export operations
   - Integration with existing repositories
   - Async operation handling
   - Sample template generation

4. **`src/main/kotlin/TestProductImportExport.kt`**
   - Test application for import/export functionality
   - UI for testing all export/import features
   - Progress monitoring and error display

### Modified Files:

1. **`src/main/kotlin/ui/viewmodels/ProductViewModel.kt`**
   - Added export methods (Excel, CSV, JSON)
   - Added import method with progress tracking
   - Added sample template generation
   - New result classes (ExportResult, ImportResult)
   - Extension function for Product to ProductDTO conversion

2. **`src/main/kotlin/ui/screens/ProductsScreen.kt`**
   - Updated Quick Actions section with export/import buttons
   - Added export and import dialogs
   - Enhanced UI state management
   - Progress indicators and user feedback
   - Updated ModernQuickActionButton with enabled parameter

## Architecture Patterns Followed

### 1. **MVVM Pattern** (Consistent with existing code)
```
UI (ProductsScreen) → ViewModel (ProductViewModel) → Service (ProductImportExportService) → Repository (ProductRepository) → API
```

### 2. **Service Layer Pattern**
- `ProductImportExportService` handles business logic
- Separation of concerns between UI and data operations
- Async operation management with coroutines

### 3. **Repository Pattern** (Existing)
- Uses existing `ProductRepository` for API calls
- Maintains existing `ProductDTO` structure
- Leverages existing error handling patterns

### 4. **Dependency Injection** (Existing)
- Service created within ViewModel
- Uses existing repository injection
- Maintains lazy initialization patterns

## Technical Implementation Details

### Export Process:
1. User clicks export button → Shows export dialog
2. User selects format → Calls appropriate ViewModel method
3. ViewModel converts Product to ProductDTO (all 38 fields)
4. Service handles file dialog and export operation
5. Utils perform actual file writing with complete data
6. UI shows progress and result feedback

### Import Process:
1. User clicks import button → Shows import dialog
2. User confirms → Opens file selection dialog
3. Service validates file and parses content
4. Utils handle CSV/JSON parsing with validation
5. Service batch creates products via repository
6. UI shows detailed progress and results

### Data Validation:
- **Required Fields**: name, price, stockQuantity (3 fields)
- **Optional Fields**: All other ProductDTO fields (35 fields)
- **Total Fields Supported**: 38 comprehensive product attributes
- **Type Validation**: Numeric fields, string constraints, boolean values, date formats
- **Error Reporting**: Line-by-line error messages with field-specific validation

### File Format Support:

#### CSV Format:
- Header row with all 38 field names
- Quoted values for strings containing commas
- All ProductDTO fields supported including:
  - Basic info (name, description, price, category, etc.)
  - Physical properties (weight, dimensions)
  - Stock management (min/max levels, reorder points)
  - Supplier information
  - Product lifecycle (warranty, expiry dates)
  - Tags and images (semicolon-separated lists)
  - Product characteristics (serialized, digital, taxable)
  - Sales tracking data
- Sample template generation with example data

#### JSON Format:
- Array of ProductDTO objects
- Full field support with proper typing (all 38 fields)
- Preserves data types (strings, numbers, booleans, arrays)
- Lenient parsing with unknown field ignoring
- Complete product data structure maintained

#### Excel Format:
- Uses existing ExcelExportUtils
- Formatted output with styling
- Multiple sheets support

## Error Handling

### Import Errors:
- File format validation
- Required field validation
- Data type validation
- API error handling
- User-friendly error messages

### Export Errors:
- File permission issues
- Disk space problems
- Data serialization errors
- User cancellation handling

## User Experience Features

### Progress Feedback:
- Loading states during operations
- Progress indicators in UI
- Detailed success/error messages
- Operation cancellation support

### File Management:
- Native file dialogs
- Default file naming with timestamps
- File extension validation
- Overwrite confirmation

### Data Integrity:
- Validation before import
- Rollback on critical errors
- Detailed operation summaries
- Warning messages for data issues

## Testing

### Test Application:
- `TestProductImportExport.kt` provides comprehensive testing
- Tests all export formats
- Tests import functionality
- Tests sample template generation
- UI for monitoring operations

### Test Scenarios:
1. Export products to each format
2. Import valid CSV/JSON files
3. Import files with validation errors
4. Handle file permission issues
5. Test user cancellation scenarios

## Usage Instructions

### For Export:
1. Navigate to Products screen
2. Click "تصدير قائمة المنتجات" button
3. Select desired format (Excel/CSV/JSON)
4. Choose save location in file dialog
5. Wait for completion confirmation

### For Import:
1. Click "استيراد منتجات" button
2. Read the import dialog instructions
3. Click "اختيار ملف" to select import file
4. Review import results and any errors
5. Check imported products in the list

### For Sample Template:
1. Click "تحميل نموذج CSV" button
2. Choose save location for template
3. Use template to create import files
4. Fill in required fields (name, price, stockQuantity)

## Integration with Existing System

### Maintains Compatibility:
- Uses existing ProductRepository and API
- Follows existing error handling patterns
- Maintains existing UI design language
- Uses existing dependency injection

### Extends Functionality:
- Adds new capabilities without breaking changes
- Enhances user workflow
- Provides data portability
- Supports bulk operations

## Future Enhancements

### Potential Improvements:
- Excel import support
- Data mapping configuration
- Import preview functionality
- Scheduled export operations
- Cloud storage integration
- Advanced filtering for export

### Performance Optimizations:
- Streaming for large files
- Background processing
- Progress tracking for large imports
- Memory optimization for big datasets
