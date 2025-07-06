import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.di.AppDependencies
import ui.theme.AppTheme
import ui.viewmodels.ProductViewModel
import ui.viewmodels.ExportResult
import ui.viewmodels.ImportResult
import ui.viewmodels.ParseResult
import data.api.ProductDTO
import utils.TestDataGenerator
import kotlinx.coroutines.launch

/**
 * Test application for product import/export functionality
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Sales Management System - Product Import/Export Test"
    ) {
        AppTheme {
            TestProductImportExportApp()
        }
    }
}

@Composable
fun TestProductImportExportApp() {
    var isInitialized by remember { mutableStateOf(false) }
    var initializationError by remember { mutableStateOf<String?>(null) }
    var productViewModel by remember { mutableStateOf<ProductViewModel?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            // Initialize services using dependency injection
            val container = AppDependencies.container
            productViewModel = container.productViewModel
            isInitialized = true
            println("✅ Product Import/Export test - Dependencies initialized successfully")
        } catch (e: Exception) {
            initializationError = "Failed to initialize services: ${e.message}"
            println("❌ Product Import/Export test - Initialization failed: ${e.message}")
            e.printStackTrace()
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            initializationError != null -> {
                Card(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "خطأ في التهيئة",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = initializationError!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            !isInitialized -> {
                Card(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري تهيئة التطبيق...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            else -> {
                // Show the import/export test interface
                ProductImportExportTestInterface(productViewModel!!)
            }
        }
    }
}

@Composable
fun ProductImportExportTestInterface(productViewModel: ProductViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var exportStatus by remember { mutableStateOf("") }
    var importStatus by remember { mutableStateOf("") }
    var isOperationInProgress by remember { mutableStateOf(false) }
    var parsedProducts by remember { mutableStateOf<List<ProductDTO>>(emptyList()) }
    var showUploadButton by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "اختبار وظائف الاستيراد والتصدير",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                text = "هذا التطبيق يختبر وظائف استيراد وتصدير المنتجات",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Divider()
            
            // Export Section
            Text(
                text = "تصدير المنتجات",
                style = MaterialTheme.typography.titleLarge
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isOperationInProgress = true
                            exportStatus = "جاري التصدير إلى Excel..."
                            productViewModel.exportProductsToExcel().collect { result ->
                                isOperationInProgress = false
                                when (result) {
                                    is ExportResult.Success -> {
                                        exportStatus = "✅ ${result.message}"
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ExportResult.Error -> {
                                        exportStatus = "❌ ${result.message}"
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ExportResult.Loading -> {
                                        exportStatus = "جاري التصدير..."
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isOperationInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تصدير Excel")
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isOperationInProgress = true
                            exportStatus = "جاري التصدير إلى CSV..."
                            productViewModel.exportProductsToCsv().collect { result ->
                                isOperationInProgress = false
                                when (result) {
                                    is ExportResult.Success -> {
                                        exportStatus = "✅ ${result.message}"
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ExportResult.Error -> {
                                        exportStatus = "❌ ${result.message}"
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ExportResult.Loading -> {
                                        exportStatus = "جاري التصدير..."
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isOperationInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تصدير CSV")
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isOperationInProgress = true
                            exportStatus = "جاري التصدير إلى JSON..."
                            productViewModel.exportProductsToJson().collect { result ->
                                isOperationInProgress = false
                                when (result) {
                                    is ExportResult.Success -> {
                                        exportStatus = "✅ ${result.message}"
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ExportResult.Error -> {
                                        exportStatus = "❌ ${result.message}"
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ExportResult.Loading -> {
                                        exportStatus = "جاري التصدير..."
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isOperationInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تصدير JSON")
                }
            }
            
            if (exportStatus.isNotEmpty()) {
                Card {
                    Text(
                        text = exportStatus,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Divider()

            // Test Data Generation Section
            Text(
                text = "إنشاء ملفات اختبار",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = {
                    try {
                        TestDataGenerator.generateAllTestFiles()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("تم إنشاء ملفات الاختبار بنجاح!")
                        }
                    } catch (e: Exception) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("خطأ في إنشاء ملفات الاختبار: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إنشاء ملفات اختبار (CSV و JSON)")
            }

            Divider()

            // Import Section
            Text(
                text = "استيراد المنتجات",
                style = MaterialTheme.typography.titleLarge
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isOperationInProgress = true
                            importStatus = "جاري تحليل الملف..."
                            showUploadButton = false
                            parsedProducts = emptyList()
                            productViewModel.parseProductsFromFile().collect { result ->
                                isOperationInProgress = false
                                when (result) {
                                    is ParseResult.Success -> {
                                        parsedProducts = result.products
                                        importStatus = "✅ ${result.message}"
                                        showUploadButton = true
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ParseResult.Error -> {
                                        importStatus = "❌ ${result.message}"
                                        showUploadButton = false
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ParseResult.Cancelled -> {
                                        importStatus = "تم إلغاء تحليل الملف"
                                        showUploadButton = false
                                    }
                                    is ParseResult.Loading -> {
                                        importStatus = "جاري تحليل الملف..."
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isOperationInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تحليل ملف الاستيراد")
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isOperationInProgress = true
                            importStatus = "جاري رفع المنتجات إلى قاعدة البيانات..."
                            productViewModel.uploadProductsToDatabase(parsedProducts).collect { result ->
                                isOperationInProgress = false
                                when (result) {
                                    is ImportResult.Success -> {
                                        importStatus = "✅ ${result.message}"
                                        showUploadButton = false
                                        parsedProducts = emptyList()
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ImportResult.Error -> {
                                        importStatus = "❌ ${result.message}"
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ImportResult.Cancelled -> {
                                        importStatus = "تم إلغاء الرفع"
                                    }
                                    is ImportResult.Loading -> {
                                        importStatus = "جاري الرفع..."
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isOperationInProgress && showUploadButton && parsedProducts.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("رفع إلى قاعدة البيانات")
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isOperationInProgress = true
                            exportStatus = "جاري إنشاء نموذج CSV..."
                            productViewModel.saveSampleCsvTemplate().collect { result ->
                                isOperationInProgress = false
                                when (result) {
                                    is ExportResult.Success -> {
                                        exportStatus = "✅ ${result.message}"
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ExportResult.Error -> {
                                        exportStatus = "❌ ${result.message}"
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                    is ExportResult.Loading -> {
                                        exportStatus = "جاري إنشاء النموذج..."
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isOperationInProgress,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تحميل نموذج CSV")
                }
            }
            
            if (importStatus.isNotEmpty()) {
                Card {
                    Text(
                        text = importStatus,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Divider()
            
            Text(
                text = "معلومات الحقول المدعومة:",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الحقول المطلوبة (Required):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• name (اسم المنتج)\n• price (السعر)\n• stockQuantity (كمية المخزون)",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "الحقول الاختيارية (Optional) - ${getOptionalFieldsCount()} حقل:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = """
                        • معلومات أساسية: description, category, sku, costPrice, brand, modelNumber, barcode
                        • خصائص فيزيائية: weight, length, width, height
                        • إدارة المخزون: minStockLevel, maxStockLevel, reorderPoint, reorderQuantity
                        • معلومات المورد: supplierName, supplierCode
                        • دورة حياة المنتج: warrantyPeriod, expiryDate, manufacturingDate
                        • الصور والعلامات: tags, imageUrl, additionalImages
                        • خصائص المنتج: isSerialized, isDigital, isTaxable
                        • التسعير والقياس: taxRate, unitOfMeasure, discountPercentage
                        • الموقع: locationInWarehouse
                        • تتبع المبيعات: totalSold, totalRevenue, lastSoldDate, lastRestockedDate
                        • معلومات إضافية: notes, createdAt, updatedAt, productStatus
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                text = "ملاحظات:",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = """
                • تأكد من وجود منتجات في النظام قبل اختبار التصدير
                • استخدم نموذج CSV لإنشاء ملف استيراد صحيح
                • يتم تصدير جميع الحقول المتاحة (${getTotalFieldsCount()} حقل)
                • يمكن استيراد ملفات CSV و JSON
                • القوائم (tags, additionalImages) تستخدم الفاصلة المنقوطة (;) في CSV
                • التواريخ بصيغة ISO (YYYY-MM-DD أو YYYY-MM-DDTHH:mm:ss)
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Get total number of fields supported
 */
private fun getTotalFieldsCount(): Int = 38

/**
 * Get number of optional fields
 */
private fun getOptionalFieldsCount(): Int = 35 // Total - 3 required fields

@Preview
@Composable
fun TestProductImportExportAppPreview() {
    AppTheme {
        TestProductImportExportApp()
    }
}
