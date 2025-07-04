package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.api.CustomerDTO
import data.api.ProductDTO
import data.api.SaleDTO
import data.api.SaleItemDTO
import data.repository.CustomerRepository
import data.repository.ProductRepository
import data.repository.SalesRepository
import kotlinx.coroutines.launch
import ui.components.*
import ui.theme.CardStyles
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreenNew(
    salesRepository: SalesRepository,
    customerRepository: CustomerRepository,
    productRepository: ProductRepository
) {
    val sales by salesRepository.sales.collectAsState()
    val customers by customerRepository.customers.collectAsState()
    val products by productRepository.products.collectAsState()
    val isLoading by salesRepository.isLoading.collectAsState()
    val error by salesRepository.error.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    
    var showNewSaleDialog by remember { mutableStateOf(false) }
    var selectedSale by remember { mutableStateOf<SaleDTO?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("ar", "SA")).apply {
            currency = Currency.getInstance("SAR")
        }
    }

    // Load data on first composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            salesRepository.loadSales()
            customerRepository.loadCustomers()
            productRepository.loadProducts()
        }
    }

    val filteredSales = remember(sales, searchQuery) {
        sales.filter { sale ->
            searchQuery.isEmpty() || 
            sale.id.toString().contains(searchQuery) ||
            sale.customerName?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    RTLProvider {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "إدارة المبيعات",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${sales.size} عملية بيع",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                RTLRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Refresh Button
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                salesRepository.loadSales()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // New Sale Button
                    Button(
                        onClick = { showNewSaleDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("بيع جديد")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardStyles.defaultCardColors(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardStyles.defaultCardElevation()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("البحث في المبيعات...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "مسح"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading State
            if (isLoading && sales.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@RTLProvider
            }

            // Error State
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "خطأ في تحميل المبيعات",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    salesRepository.loadSales()
                                }
                            }
                        ) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
                return@RTLProvider
            }

            // Sales List
            if (filteredSales.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ShoppingCart,
                    title = "لا توجد مبيعات",
                    description = "لم يتم العثور على مبيعات تطابق بحثك. ابدأ ببيع جديد.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(filteredSales) { sale ->
                        SaleCardNew(
                            sale = sale,
                            currencyFormatter = currencyFormatter,
                            onClick = { selectedSale = sale },
                            onComplete = {
                                coroutineScope.launch {
                                    salesRepository.completeSale(sale.id!!)
                                }
                            },
                            onCancel = {
                                coroutineScope.launch {
                                    salesRepository.cancelSale(sale.id!!)
                                }
                            }
                        )
                    }
                }
            }
        }

        // New Sale Dialog
        if (showNewSaleDialog) {
            NewSaleDialogNew(
                customers = customers,
                products = products,
                onDismiss = { showNewSaleDialog = false },
                onSave = { sale ->
                    coroutineScope.launch {
                        salesRepository.createSale(sale)
                    }
                    showNewSaleDialog = false
                }
            )
        }

        // Sale Details Dialog
        selectedSale?.let { sale ->
            SaleDetailsDialogNew(
                sale = sale,
                onDismiss = { selectedSale = null }
            )
        }
    }
}

@Composable
fun SaleCardNew(
    sale: SaleDTO,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardStyles.defaultCardColors(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardStyles.defaultCardElevation(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with sale ID and status
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فاتورة #${sale.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Badge(
                    containerColor = when (sale.status) {
                        "COMPLETED" -> MaterialTheme.colorScheme.primary
                        "PENDING" -> MaterialTheme.colorScheme.secondary
                        "CANCELLED" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outline
                    }
                ) {
                    Text(
                        text = when (sale.status) {
                            "COMPLETED" -> "مكتمل"
                            "PENDING" -> "معلق"
                            "CANCELLED" -> "ملغي"
                            else -> sale.status ?: "غير محدد"
                        }
                    )
                }
            }

            // Customer and date info
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "العميل: ${sale.customerName ?: "غير محدد"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = sale.saleDate ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Total amount
            Text(
                text = "الإجمالي: ${currencyFormatter.format(sale.totalAmount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Action buttons for pending sales
            if (sale.status == "PENDING") {
                RTLRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إكمال")
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

// TODO: Implement these dialogs
@Composable
fun NewSaleDialogNew(
    customers: List<CustomerDTO>,
    products: List<ProductDTO>,
    onDismiss: () -> Unit,
    onSave: (SaleDTO) -> Unit
) {
    // This is a placeholder - implement the full dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("بيع جديد") },
        text = { Text("سيتم تنفيذ نموذج البيع الجديد قريباً") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("موافق")
            }
        }
    )
}

@Composable
fun SaleDetailsDialogNew(
    sale: SaleDTO,
    onDismiss: () -> Unit
) {
    // This is a placeholder - implement the full dialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تفاصيل الفاتورة #${sale.id}") },
        text = { Text("سيتم تنفيذ عرض تفاصيل الفاتورة قريباً") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("موافق")
            }
        }
    )
}
