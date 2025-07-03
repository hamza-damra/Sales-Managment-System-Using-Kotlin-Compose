package ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.AssignmentReturn
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.*
import kotlinx.datetime.LocalDateTime
import UiUtils
import ui.components.*
import ui.theme.AppTheme
import ui.theme.CardStyles

@Composable
fun ReturnsScreen() {
    RTLProvider {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SectionHeader(
                title = "إدارة المرتجعات",
                subtitle = "معالجة وتتبع طلبات الإرجاع والاستبدال"
            )

            // Action buttons with RTL layout
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionButton(
                    text = "إرجاع جديد",
                    icon = Icons.Default.Add,
                    onClick = { /* Process new return */ }
                )
                QuickActionButton(
                    text = "تصدير التقرير",
                    icon = Icons.Default.FileDownload,
                    onClick = { /* Export returns report */ }
                )
            }

            // Returns statistics with RTL layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "المرتجعات اليوم",
                    value = "5",
                    subtitle = "طلب إرجاع",
                    icon = Icons.AutoMirrored.Filled.AssignmentReturn,
                    iconColor = AppTheme.colors.warning,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "قيمة المرتجعات",
                    value = "850 ر.س",
                    subtitle = "إجمالي اليوم",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    iconColor = AppTheme.colors.error,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "معدل الإرجاع",
                    value = "2.3%",
                    subtitle = "من إجمالي المبيعات",
                    icon = Icons.Default.Analytics,
                    iconColor = AppTheme.colors.info,
                    modifier = Modifier.weight(1f)
                )
            }

            // Returns list
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardStyles.defaultCardColors(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardStyles.defaultCardElevation()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "طلبات الإرجاع الحديثة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(5) { index ->
                            ReturnCard(
                                returnId = "RET-${1000 + index}",
                                customerName = "عميل ${index + 1}",
                                productName = "منتج ${index + 1}",
                                reason = "معيب",
                                amount = (100 + index * 50).toDouble(),
                                status = if (index % 2 == 0) "معلق" else "مكتمل",
                                date = "2024-01-${15 + index}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReturnCard(
    returnId: String,
    customerName: String,
    productName: String,
    reason: String,
    amount: Double,
    status: String,
    date: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(vertical = 4.dp),
        colors = CardStyles.elevatedCardColors(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RTLRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(
                        text = status,
                        color = if (status == "مكتمل") AppTheme.colors.success else AppTheme.colors.warning
                    )

                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = returnId,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = customerName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { /* View details */ }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "عرض التفاصيل",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            RTLRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RTLRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${amount.toInt()} ر.س",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.error
                        )
                        Text(
                            text = "مبلغ الإرجاع",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "السبب: $reason",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ReturnsStatsCards() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "إجمالي المرتجعات",
            value = "156",
            icon = Icons.AutoMirrored.Filled.AssignmentReturn,
            iconColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "في الانتظار",
            value = "23",
            icon = Icons.Default.Schedule,
            iconColor = AppTheme.colors.warning,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "تم الاسترداد",
            value = UiUtils.formatCurrency(12500.0),
            icon = Icons.Default.AccountBalance,
            iconColor = AppTheme.colors.success,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "معدل المرتجعات",
            value = "3.2%",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            iconColor = AppTheme.colors.info,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ReturnsListContent(
    searchQuery: String,
    selectedStatus: String,
    onReturnClick: (Return) -> Unit
) {
    val returns = remember {
        listOf(
            Return(1, 101, LocalDateTime(2024, 1, 15, 10, 0),
                listOf(ReturnItem(1, 2, 50.0, ItemCondition.DEFECTIVE)),
                ReturnReason.DEFECTIVE, ReturnStatus.APPROVED, 100.0, "منتج معيب"),
            Return(2, 102, LocalDateTime(2024, 1, 16, 14, 30),
                listOf(ReturnItem(2, 1, 75.0, ItemCondition.GOOD)),
                ReturnReason.CUSTOMER_CHANGE_MIND, ReturnStatus.PENDING, 75.0, "تغيير رأي العميل"),
            Return(3, 103, LocalDateTime(2024, 1, 17, 9, 15),
                listOf(ReturnItem(3, 3, 25.0, ItemCondition.DAMAGED)),
                ReturnReason.DAMAGED_SHIPPING, ReturnStatus.REFUNDED, 75.0, "تضرر أثناء الشحن"),
            Return(4, 104, LocalDateTime(2024, 1, 18, 16, 45),
                listOf(ReturnItem(4, 1, 120.0, ItemCondition.NEW)),
                ReturnReason.WRONG_ITEM, ReturnStatus.EXCHANGED, 120.0, "منتج خاطئ"),
            Return(5, 105, LocalDateTime(2024, 1, 19, 11, 20),
                listOf(ReturnItem(5, 2, 40.0, ItemCondition.DEFECTIVE)),
                ReturnReason.EXPIRED, ReturnStatus.REJECTED, 0.0, "منتج منتهي الصلاحية")
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(returns.filter { returnItem ->
            (selectedStatus == "الكل" || returnItem.status.displayName == selectedStatus) &&
            (searchQuery.isEmpty() || returnItem.id.toString().contains(searchQuery) ||
             returnItem.originalSaleId.toString().contains(searchQuery))
        }) { returnItem ->
            ReturnCard(
                returnItem = returnItem,
                onClick = { onReturnClick(returnItem) }
            )
        }
    }
}

@Composable
fun ReturnCard(
    returnItem: Return,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Status and Actions
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = getReturnStatusColor(returnItem.status).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = returnItem.status.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = getReturnStatusColor(returnItem.status),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { /* معالجة */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "معالجة",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { /* طباعة */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = "طباعة",
                                tint = AppTheme.colors.info,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Return Info
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "مرتجع #${returnItem.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "فاتورة أصلية #${returnItem.originalSaleId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = returnItem.date.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Return Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "عدد القطع: ${returnItem.items.sumOf { it.quantity }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "السبب: ${returnItem.reason.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = UiUtils.formatCurrency(returnItem.refundAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (returnItem.refundAmount > 0) AppTheme.colors.success else AppTheme.colors.error
                    )
                    Text(
                        text = "مبلغ الاسترداد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (returnItem.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ملاحظات: ${returnItem.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReturnsAnalyticsContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Returns by Reason Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "أسباب المرتجعات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val reasonData = listOf(
                        ReasonStats("معيب", 35, AppTheme.colors.error),
                        ReasonStats("تغيير رأي العميل", 28, AppTheme.colors.warning),
                        ReasonStats("منتج خاطئ", 20, AppTheme.colors.info),
                        ReasonStats("تضرر أثناء الشحن", 12, AppTheme.colors.purple),
                        ReasonStats("منتهي الصلاحية", 5, MaterialTheme.colorScheme.secondary)
                    )

                    reasonData.forEach { reason ->
                        ReasonStatsItem(reason = reason)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        item {
            // Monthly Returns Trend
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "اتجاه المرتجعات الشهرية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MonthlyTrendItem("يناير", 45, MaterialTheme.colorScheme.primary)
                        MonthlyTrendItem("فبراير", 38, MaterialTheme.colorScheme.secondary)
                        MonthlyTrendItem("مارس", 52, AppTheme.colors.info)
                        MonthlyTrendItem("أبريل", 41, AppTheme.colors.success)
                    }
                }
            }
        }

        item {
            // Return Processing Performance
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "أداء معالجة المرتجعات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PerformanceMetricItem(
                            title = "متوسط وقت المعالجة",
                            value = "2.5 يوم",
                            color = AppTheme.colors.info
                        )

                        PerformanceMetricItem(
                            title = "نسبة الموافقة",
                            value = "87%",
                            color = AppTheme.colors.success
                        )

                        PerformanceMetricItem(
                            title = "رضا العملاء",
                            value = "4.2/5",
                            color = AppTheme.colors.warning
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReasonStatsItem(reason: ReasonStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${reason.percentage}%",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = reason.color
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .fillMaxWidth(reason.percentage / 100f)
                        .background(reason.color, RoundedCornerShape(4.dp))
                )
            }

            Text(
                text = reason.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MonthlyTrendItem(month: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = month,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PerformanceMetricItem(title: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ReturnPoliciesContent() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "سياسات الإرجاع والاستبدال",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val policies = listOf(
                        PolicyItem("فترة الإرجاع", "30 يوم من تاريخ الشراء", Icons.Default.Schedule),
                        PolicyItem("شروط المنتج", "يجب أن يكون في حالته الأصلية", Icons.Default.CheckCircle),
                        PolicyItem("الإيصال", "وجود الإيصال الأصلي مطلوب", Icons.Default.Receipt),
                        PolicyItem("رسوم الإرجاع", "مجاني للمنتجات المعيبة، 10 ريال للتغيير", Icons.Default.AttachMoney),
                        PolicyItem("طريقة الاسترداد", "نفس طريقة الدفع الأصلية", Icons.Default.Payment),
                        PolicyItem("المنتجات المستثناة", "المنتجات الشخصية والمواد الغذائية", Icons.Default.Block)
                    )

                    policies.forEach { policy ->
                        PolicyItemCard(policy = policy)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PolicyItemCard(policy: PolicyItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                policy.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = policy.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = policy.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReturnDialog(
    onDismiss: () -> Unit,
    onConfirm: (ReturnData) -> Unit
) {
    var originalSaleId by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf(ReturnReason.DEFECTIVE) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مرتجع جديد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = originalSaleId,
                    onValueChange = { originalSaleId = it },
                    label = { Text("رقم الفاتورة الأصلية") },
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedReason.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("سبب الإرجاع") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ReturnReason.entries.forEach { reason ->
                            DropdownMenuItem(
                                text = { Text(reason.displayName) },
                                onClick = {
                                    selectedReason = reason
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val returnData = ReturnData(
                        originalSaleId = originalSaleId.toIntOrNull() ?: 0,
                        reason = selectedReason,
                        notes = notes
                    )
                    onConfirm(returnData)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnDetailsDialog(
    returnItem: Return,
    onDismiss: () -> Unit,
    onStatusUpdate: (ReturnStatus) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(returnItem.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "تفاصيل المرتجع #${returnItem.id}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ReturnDetailItem("رقم الفاتورة الأصلية", returnItem.originalSaleId.toString())
                ReturnDetailItem("تاريخ الإرجاع", returnItem.date.date.toString())
                ReturnDetailItem("السبب", returnItem.reason.displayName)
                ReturnDetailItem("مبلغ الاسترداد", UiUtils.formatCurrency(returnItem.refundAmount))

                Text(
                    text = "القطع المرتجعة:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                returnItem.items.forEach { item ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            ReturnDetailItem("الكمية", item.quantity.toString())
                            ReturnDetailItem("السعر", UiUtils.formatCurrency(item.unitPrice))
                            ReturnDetailItem("الحالة", item.condition.displayName)
                        }
                    }
                }

                if (returnItem.notes.isNotEmpty()) {
                    ReturnDetailItem("ملاحظات", returnItem.notes)
                }

                // Status Update
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedStatus.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("تحديث الحالة") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ReturnStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.displayName) },
                                onClick = {
                                    selectedStatus = status
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStatusUpdate(selectedStatus) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("تحديث")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun ReturnDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Text(
        text = "$label:",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

data class ReasonStats(
    val name: String,
    val percentage: Int,
    val color: Color
)

data class PolicyItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class ReturnData(
    val originalSaleId: Int,
    val reason: ReturnReason,
    val notes: String
)

enum class ReturnTab(val title: String) {
    RETURNS("قائمة المرتجعات"),
    ANALYTICS("تحليلات المرتجعات"),
    POLICIES("سياسات الإرجاع")
}

@Composable
fun getReturnStatusColor(status: ReturnStatus): Color {
    return when (status) {
        ReturnStatus.PENDING -> AppTheme.colors.warning
        ReturnStatus.APPROVED -> AppTheme.colors.info
        ReturnStatus.REJECTED -> AppTheme.colors.error
        ReturnStatus.REFUNDED -> AppTheme.colors.success
        ReturnStatus.EXCHANGED -> AppTheme.colors.purple
    }
}
