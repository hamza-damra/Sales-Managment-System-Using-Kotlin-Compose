# Inventory Export Functionality

## Overview
This document describes the Excel and PDF export functionality added to the Sales Management System's Inventory Screen.

## Features Added

### 1. Excel Export
- **Library Used**: Apache POI 5.2.4
- **File Format**: .xlsx (Excel 2007+)
- **Features**:
  - Multiple worksheets (Overview, Products, Low Stock)
  - Professional styling with headers and data formatting
  - Warning colors for low stock items
  - Auto-sized columns
  - Summary statistics

### 2. PDF Export
- **Library Used**: iText7 Core 7.2.5
- **File Format**: .pdf
- **Features**:
  - Professional document layout
  - Arabic text support
  - Tables with proper formatting
  - Summary sections
  - Responsive design

### 3. Export Service
- **Service Class**: `InventoryExportService`
- **Features**:
  - Async operations using Coroutines
  - API integration for real-time data
  - Error handling and fallback mechanisms
  - Support for filtered exports

## Files Added/Modified

### New Files
1. `src/main/kotlin/utils/ExcelExportUtils.kt` - Excel export utilities
2. `src/main/kotlin/utils/PdfExportUtils.kt` - PDF export utilities
3. `src/main/kotlin/services/InventoryExportService.kt` - Export service
4. `src/main/kotlin/TestInventoryExport.kt` - Test application
5. `src/main/kotlin/TestExportUtils.kt` - Export utilities test

### Modified Files
1. `build.gradle.kts` - Added export dependencies
2. `src/main/kotlin/ui/screens/InventoryScreen.kt` - Added export UI and functionality

## Dependencies Added

```kotlin
// Excel Export - Apache POI
implementation("org.apache.poi:poi:5.2.4")
implementation("org.apache.poi:poi-ooxml:5.2.4")
implementation("org.apache.poi:poi-ooxml-schemas:4.1.2")

// PDF Generation - iText
implementation("com.itextpdf:itext7-core:7.2.5")
implementation("com.itextpdf:html2pdf:4.0.5")
```

## Usage

### In the Inventory Screen
1. **Desktop/Tablet**: Separate Excel and PDF export buttons in the header
2. **Mobile**: Dropdown menu with export options
3. **Export Types**:
   - Overview: Complete inventory overview with summary
   - Products: Filtered product list based on current filters
   - Movements: Stock movement history
   - Warehouses: Warehouse-specific data

### Export Process
1. User clicks export button
2. Loading indicator appears
3. Data is fetched from API
4. File dialog opens for save location
5. Export is processed
6. Success/error message is displayed

## API Integration

### Data Sources
- **Products**: `ProductRepository.loadProducts()`
- **Search**: `ProductRepository.searchProducts()`
- **Inventory Report**: `ReportsApiService.getInventoryReport()`
- **Low Stock**: Calculated from product data or API

### Error Handling
- Network errors with fallback to cached data
- File system errors with user notification
- Data validation with graceful degradation

## Export Formats

### Excel Export Structure
1. **Overview Sheet**:
   - Summary statistics (total products, value, alerts)
   
2. **Products Sheet**:
   - Product ID, Name, Category, Price, Cost
   - Current Stock, Minimum Stock, Barcode, Status
   - Color coding for low stock items
   
3. **Low Stock Sheet**:
   - Products below minimum stock level
   - Reorder recommendations
   - Required quantities

### PDF Export Structure
1. **Header**: Report title and date
2. **Summary Section**: Key statistics
3. **Products Table**: Detailed product information
4. **Low Stock Section**: Alert items (if any)

## Testing

### Manual Testing Steps
1. **Build the Application**:
   ```bash
   ./gradlew build
   ```

2. **Run the Test Application**:
   ```bash
   ./gradlew run -PmainClass=TestInventoryExportKt
   ```

3. **Test Export Functionality**:
   - Navigate to Inventory screen
   - Try Excel export on different tabs
   - Try PDF export on different tabs
   - Test with different filters applied
   - Verify file contents

### Test Scenarios
1. **Overview Export**: Full inventory overview
2. **Filtered Export**: Products with category filter
3. **Search Export**: Products matching search query
4. **Low Stock Export**: Only low stock items
5. **Error Handling**: Network failures, file system errors

## Integration with Main Application

### Service Initialization
```kotlin
val httpClient = HttpClientProvider.createHttpClient()
val productApiService = ProductApiService(httpClient)
val reportsApiService = ReportsApiService(httpClient)
val productRepository = ProductRepository(productApiService)
val inventoryExportService = InventoryExportService(productRepository, reportsApiService)
```

### Screen Integration
```kotlin
InventoryScreen(
    salesDataManager = salesDataManager,
    inventoryExportService = inventoryExportService
)
```

## Performance Considerations

### Memory Management
- Streaming for large datasets
- Proper resource disposal
- Background processing

### User Experience
- Loading indicators
- Progress feedback
- Cancellation support

## Future Enhancements

### Planned Features
1. **Custom Export Templates**
2. **Scheduled Exports**
3. **Email Integration**
4. **Cloud Storage Integration**
5. **Advanced Filtering Options**

### Technical Improvements
1. **Caching for Better Performance**
2. **Batch Processing for Large Datasets**
3. **Export History Tracking**
4. **User Preferences for Export Settings**

## Troubleshooting

### Common Issues
1. **File Permission Errors**: Ensure write access to selected directory
2. **Memory Issues**: Reduce export size or increase heap memory
3. **API Timeouts**: Check network connection and API availability
4. **Font Issues**: Ensure proper font support for Arabic text

### Debug Mode
Enable debug logging by setting:
```kotlin
// Add to application startup
System.setProperty("export.debug", "true")
```

## Support
For issues or questions regarding the export functionality, please refer to the main application documentation or contact the development team.
