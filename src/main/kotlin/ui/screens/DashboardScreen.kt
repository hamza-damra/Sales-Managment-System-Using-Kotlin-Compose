package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import data.SalesDataManager
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles
import java.text.NumberFormat
import java.util.*

@Composable
fun DashboardScreen(salesDataManager: SalesDataManager) {
    RTLProvider {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayStats = salesDataManager.getDailySalesStats(today)
        val topProducts = salesDataManager.getTopSellingProducts(5)
        val lowStockProducts = salesDataManager.getLowStockProducts(10)
        val recentSales = salesDataManager.sales.takeLast(5).reversed()

        val currencyFormatter = remember {
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("ar-SA")).apply {
                currency = Currency.getInstance("SAR")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            SectionHeader(
                title = "لوحة التحكم",
                subtitle = "نظرة عامة على أداء المبيعات اليوم"
            )

            // Today's Sales Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "مبيعات اليوم",
                    value = currencyFormatter.format(todayStats.totalSales),
                    subtitle = "${todayStats.totalTransactions} معاملة",
                    icon = Icons.Default.AttachMoney,
                    iconColor = AppTheme.colors.success
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "متوسط قيمة الطلب",
                    value = currencyFormatter.format(todayStats.averageOrderValue),
                    subtitle = "لكل معاملة",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    iconColor = AppTheme.colors.info
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "إجمالي الربح",
                    value = currencyFormatter.format(todayStats.totalProfit),
                    subtitle = "ربح اليوم",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconColor = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "المنتجات المباعة",
                    value = todayStats.totalItemsSold.toString(),
                    subtitle = "قطعة",
                    icon = Icons.Default.Inventory,
                    iconColor = AppTheme.colors.warning
                )
            }

            // Main Content Grid
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Quick Actions
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardStyles.defaultCardColors(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardStyles.defaultCardElevation()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "إجراءات سريعة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        QuickActionButton(
                            text = "بيع جديد",
                            icon = Icons.Default.Add,
                            onClick = { /* Navigate to sales */ }
                        )

                        QuickActionButton(
                            text = "إضافة منتج",
                            icon = Icons.Default.Inventory,
                            onClick = { /* Navigate to add product */ }
                        )

                        QuickActionButton(
                            text = "إضافة عميل",
                            icon = Icons.Default.PersonAdd,
                            onClick = { /* Navigate to add customer */ }
                        )
                    }
                }

                // Recent Sales
                Card(
                    modifier = Modifier.weight(1.5f),
                    colors = CardStyles.defaultCardColors(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardStyles.defaultCardElevation()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "آخر المبيعات",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            TextButton(
                                onClick = { /* Navigate to all sales */ }
                            ) {
                                Text(
                                    text = "عرض الكل",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (recentSales.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.ShoppingCart,
                                title = "لا توجد مبيعات",
                                description = "لم يتم تسجيل أي مبيعات حتى الآن"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(recentSales) { sale ->
                                    SaleItem(
                                        sale = sale,
                                        currencyFormatter = currencyFormatter
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Top Products
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardStyles.defaultCardColors(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardStyles.defaultCardElevation()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "أفضل المنتجات مبيعاً",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (topProducts.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.Inventory,
                                title = "لا توجد مبيعات",
                                description = "لم يتم بيع أي منتجات حتى الآن"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(topProducts) { productStats ->
                                    ProductStatsItem(
                                        productStats = productStats,
                                        currencyFormatter = currencyFormatter
                                    )
                                }
                            }
                        }
                    }
                }

                // Low Stock Alert
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardStyles.defaultCardColors(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardStyles.defaultCardElevation()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = AppTheme.colors.warning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنبيه المخزون",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (lowStockProducts.isEmpty()) {
                            EmptyState(
                                icon = Icons.Default.CheckCircle,
                                title = "الوضع جيد",
                                description = "جميع المنتجات متوفرة بكميات كافية"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(lowStockProducts) { product ->
                                    LowStockItem(product = product)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleItem(
    sale: data.Sale,
    currencyFormatter: NumberFormat
) {
    Card(
        colors = CardStyles.elevatedCardColors(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "فاتورة #${sale.id}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sale.customer?.name ?: "عميل مباشر",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = currencyFormatter.format(sale.total),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.success
                )
                StatusChip(
                    text = sale.paymentMethod.displayName,
                    color = AppTheme.colors.info
                )
            }
        }
    }
}

@Composable
private fun ProductStatsItem(
    productStats: data.ProductStats,
    currencyFormatter: NumberFormat
) {
    Card(
        colors = CardStyles.elevatedCardColors(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productStats.product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تم بيع ${productStats.totalSold} قطعة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = currencyFormatter.format(productStats.revenue),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LowStockItem(product: data.Product) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "المتبقي: ${product.stock}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }

            Button(
                onClick = { /* Create purchase order */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("إعادة طلب")
            }
        }
    }
}
