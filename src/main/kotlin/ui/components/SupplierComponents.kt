@file:OptIn(ExperimentalMaterial3Api::class)

package ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.api.SupplierDTO
import ui.theme.AppTheme
import ui.theme.CardStyles
import ui.viewmodels.SupplierData
import utils.SupplierMapper

/**
 * Enhanced Add Supplier Dialog
 */
@Composable
fun EnhancedAddSupplierDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (SupplierData) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var taxNumber by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("NET_30") }
    var deliveryTerms by remember { mutableStateOf("FOB_DESTINATION") }
    var notes by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf("") }
    var contactPersonError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }

    fun validateForm(): Boolean {
        nameError = if (name.isBlank()) "اسم المورد مطلوب" else ""
        contactPersonError = if (contactPerson.isBlank()) "اسم الشخص المسؤول مطلوب" else ""
        phoneError = if (phone.isBlank()) "رقم الهاتف مطلوب" 
                    else if (!phone.matches(Regex("^[+]?[0-9]{10,15}$"))) "رقم الهاتف غير صحيح" 
                    else ""
        emailError = if (email.isBlank()) "البريد الإلكتروني مطلوب"
                    else if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) "البريد الإلكتروني غير صحيح"
                    else ""
        addressError = if (address.isBlank()) "العنوان مطلوب" else ""
        
        return nameError.isEmpty() && contactPersonError.isEmpty() && 
               phoneError.isEmpty() && emailError.isEmpty() && addressError.isEmpty()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "إضافة مورد جديد",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Basic Information Section
                        Text(
                            text = "المعلومات الأساسية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { 
                                name = it
                                nameError = ""
                            },
                            label = { Text("اسم المورد *") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = nameError.isNotEmpty(),
                            supportingText = if (nameError.isNotEmpty()) {
                                { Text(nameError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = contactPerson,
                            onValueChange = { 
                                contactPerson = it
                                contactPersonError = ""
                            },
                            label = { Text("الشخص المسؤول *") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = contactPersonError.isNotEmpty(),
                            supportingText = if (contactPersonError.isNotEmpty()) {
                                { Text(contactPersonError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { 
                                    phone = it
                                    phoneError = ""
                                },
                                label = { Text("رقم الهاتف *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                isError = phoneError.isNotEmpty(),
                                supportingText = if (phoneError.isNotEmpty()) {
                                    { Text(phoneError, color = MaterialTheme.colorScheme.error) }
                                } else null,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { 
                                    email = it
                                    emailError = ""
                                },
                                label = { Text("البريد الإلكتروني *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                isError = emailError.isNotEmpty(),
                                supportingText = if (emailError.isNotEmpty()) {
                                    { Text(emailError, color = MaterialTheme.colorScheme.error) }
                                } else null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { 
                                address = it
                                addressError = ""
                            },
                            label = { Text("العنوان *") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            isError = addressError.isNotEmpty(),
                            supportingText = if (addressError.isNotEmpty()) {
                                { Text(addressError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("المدينة") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = country,
                                onValueChange = { country = it },
                                label = { Text("الدولة") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = taxNumber,
                            onValueChange = { taxNumber = it },
                            label = { Text("الرقم الضريبي") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        // Terms Section
                        Text(
                            text = "شروط التعامل",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Payment Terms Dropdown
                            var paymentTermsExpanded by remember { mutableStateOf(false) }
                            val paymentTermsOptions = listOf(
                                "NET_30" to "30 يوم",
                                "NET_15" to "15 يوم", 
                                "NET_7" to "7 أيام",
                                "COD" to "الدفع عند الاستلام",
                                "PREPAID" to "دفع مقدم"
                            )

                            ExposedDropdownMenuBox(
                                expanded = paymentTermsExpanded,
                                onExpandedChange = { paymentTermsExpanded = !paymentTermsExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = paymentTermsOptions.find { it.first == paymentTerms }?.second ?: "30 يوم",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("شروط الدفع") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentTermsExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = paymentTermsExpanded,
                                    onDismissRequest = { paymentTermsExpanded = false }
                                ) {
                                    paymentTermsOptions.forEach { (value, display) ->
                                        DropdownMenuItem(
                                            text = { Text(display) },
                                            onClick = {
                                                paymentTerms = value
                                                paymentTermsExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Delivery Terms Dropdown
                            var deliveryTermsExpanded by remember { mutableStateOf(false) }
                            val deliveryTermsOptions = listOf(
                                "FOB_DESTINATION" to "تسليم في الوجهة",
                                "FOB_ORIGIN" to "تسليم من المنشأ",
                                "CIF" to "التكلفة والتأمين والشحن",
                                "EXW" to "تسليم في المصنع"
                            )

                            ExposedDropdownMenuBox(
                                expanded = deliveryTermsExpanded,
                                onExpandedChange = { deliveryTermsExpanded = !deliveryTermsExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = deliveryTermsOptions.find { it.first == deliveryTerms }?.second ?: "تسليم في الوجهة",
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("شروط التسليم") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = deliveryTermsExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = deliveryTermsExpanded,
                                    onDismissRequest = { deliveryTermsExpanded = false }
                                ) {
                                    deliveryTermsOptions.forEach { (value, display) ->
                                        DropdownMenuItem(
                                            text = { Text(display) },
                                            onClick = {
                                                deliveryTerms = value
                                                deliveryTermsExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (validateForm()) {
                                onSave(
                                    SupplierData(
                                        name = name.trim(),
                                        contactPerson = contactPerson.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        address = address.trim(),
                                        paymentTerms = paymentTerms,
                                        deliveryTerms = deliveryTerms
                                    )
                                )
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Edit Supplier Dialog
 */
@Composable
fun EnhancedEditSupplierDialog(
    supplier: SupplierDTO,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (SupplierData) -> Unit
) {
    var name by remember { mutableStateOf(supplier.name) }
    var contactPerson by remember { mutableStateOf(supplier.contactPerson ?: "") }
    var phone by remember { mutableStateOf(supplier.phone ?: "") }
    var email by remember { mutableStateOf(supplier.email ?: "") }
    var address by remember { mutableStateOf(supplier.address ?: "") }
    var city by remember { mutableStateOf(supplier.city ?: "") }
    var country by remember { mutableStateOf(supplier.country ?: "") }
    var taxNumber by remember { mutableStateOf(supplier.taxNumber ?: "") }
    var paymentTerms by remember { mutableStateOf(supplier.paymentTerms ?: "NET_30") }
    var deliveryTerms by remember { mutableStateOf(supplier.deliveryTerms ?: "FOB_DESTINATION") }
    var notes by remember { mutableStateOf(supplier.notes ?: "") }

    var nameError by remember { mutableStateOf("") }
    var contactPersonError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }

    fun validateForm(): Boolean {
        nameError = if (name.isBlank()) "اسم المورد مطلوب" else ""
        contactPersonError = if (contactPerson.isBlank()) "اسم الشخص المسؤول مطلوب" else ""
        phoneError = if (phone.isBlank()) "رقم الهاتف مطلوب"
                    else if (!phone.matches(Regex("^[+]?[0-9]{10,15}$"))) "رقم الهاتف غير صحيح"
                    else ""
        emailError = if (email.isBlank()) "البريد الإلكتروني مطلوب"
                    else if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) "البريد الإلكتروني غير صحيح"
                    else ""
        addressError = if (address.isBlank()) "العنوان مطلوب" else ""

        return nameError.isEmpty() && contactPersonError.isEmpty() &&
               phoneError.isEmpty() && emailError.isEmpty() && addressError.isEmpty()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تعديل بيانات المورد",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form Content (similar to Add Dialog but with pre-filled values)
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "المعلومات الأساسية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = ""
                            },
                            label = { Text("اسم المورد *") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = nameError.isNotEmpty(),
                            supportingText = if (nameError.isNotEmpty()) {
                                { Text(nameError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = contactPerson,
                            onValueChange = {
                                contactPerson = it
                                contactPersonError = ""
                            },
                            label = { Text("الشخص المسؤول *") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = contactPersonError.isNotEmpty(),
                            supportingText = if (contactPersonError.isNotEmpty()) {
                                { Text(contactPersonError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = {
                                    phone = it
                                    phoneError = ""
                                },
                                label = { Text("رقم الهاتف *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                isError = phoneError.isNotEmpty(),
                                supportingText = if (phoneError.isNotEmpty()) {
                                    { Text(phoneError, color = MaterialTheme.colorScheme.error) }
                                } else null,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = ""
                                },
                                label = { Text("البريد الإلكتروني *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                isError = emailError.isNotEmpty(),
                                supportingText = if (emailError.isNotEmpty()) {
                                    { Text(emailError, color = MaterialTheme.colorScheme.error) }
                                } else null,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                addressError = ""
                            },
                            label = { Text("العنوان *") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            isError = addressError.isNotEmpty(),
                            supportingText = if (addressError.isNotEmpty()) {
                                { Text(addressError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (validateForm()) {
                                onSave(
                                    SupplierData(
                                        name = name.trim(),
                                        contactPerson = contactPerson.trim(),
                                        phone = phone.trim(),
                                        email = email.trim(),
                                        address = address.trim(),
                                        paymentTerms = paymentTerms,
                                        deliveryTerms = deliveryTerms
                                    )
                                )
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("حفظ التغييرات")
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Supplier Details Panel
 */
@Composable
fun EnhancedSupplierDetailsPanel(
    supplier: SupplierDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تفاصيل المورد",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Supplier Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = supplier.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        supplier.contactPerson?.let { contact ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = contact,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        supplier.phone?.let { phone ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        supplier.email?.let { email ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Statistics Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SupplierStatCard(
                        title = "إجمالي الطلبات",
                        value = SupplierMapper.formatTotalOrders(supplier.totalOrders),
                        icon = Icons.Default.ShoppingCart,
                        iconColor = AppTheme.colors.warning,
                        modifier = Modifier.weight(1f)
                    )

                    SupplierStatCard(
                        title = "إجمالي المبلغ",
                        value = SupplierMapper.formatTotalAmount(supplier.totalAmount),
                        icon = Icons.Default.AttachMoney,
                        iconColor = AppTheme.colors.success,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SupplierStatCard(
                        title = "التقييم",
                        value = SupplierMapper.formatRating(supplier.rating),
                        icon = Icons.Default.Star,
                        iconColor = AppTheme.colors.warning,
                        modifier = Modifier.weight(1f)
                    )

                    SupplierStatCard(
                        title = "الحالة",
                        value = SupplierMapper.getStatusDisplayName(supplier.status),
                        icon = if (supplier.status == "ACTIVE") Icons.Default.CheckCircle else Icons.Default.Cancel,
                        iconColor = if (supplier.status == "ACTIVE") AppTheme.colors.success else AppTheme.colors.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Additional Details
            if (supplier.address != null || supplier.city != null || supplier.country != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "معلومات العنوان",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            supplier.address?.let { address ->
                                Text(
                                    text = address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                supplier.city?.let { city ->
                                    Text(
                                        text = "المدينة: $city",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                supplier.country?.let { country ->
                                    Text(
                                        text = "الدولة: $country",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Terms and Conditions
            if (supplier.paymentTerms != null || supplier.deliveryTerms != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "شروط التعامل",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            supplier.paymentTerms?.let { terms ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Payment,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "شروط الدفع: ${SupplierMapper.getPaymentTermsDisplayName(terms)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            supplier.deliveryTerms?.let { terms ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "شروط التسليم: ${SupplierMapper.getDeliveryTermsDisplayName(terms)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notes
            supplier.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "ملاحظات",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Supplier Stat Card Component
 */
@Composable
private fun SupplierStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Enhanced Supplier Card Component
 */
@Composable
fun EnhancedSupplierCard(
    supplier: SupplierDTO,
    onClick: (SupplierDTO) -> Unit,
    onEdit: (SupplierDTO) -> Unit,
    onDelete: (SupplierDTO) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick(supplier) },
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 4.dp else 1.dp
        ),
        border = BorderStroke(
            width = if (isHovered) 1.5.dp else 1.dp,
            color = if (isHovered)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = supplier.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    supplier.contactPerson?.let { contact ->
                        Text(
                            text = contact,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Status Badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (supplier.status) {
                            "ACTIVE" -> AppTheme.colors.success.copy(alpha = 0.1f)
                            "SUSPENDED" -> AppTheme.colors.error.copy(alpha = 0.1f)
                            else -> AppTheme.colors.warning.copy(alpha = 0.1f)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = SupplierMapper.getStatusDisplayName(supplier.status),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (supplier.status) {
                            "ACTIVE" -> AppTheme.colors.success
                            "SUSPENDED" -> AppTheme.colors.error
                            else -> AppTheme.colors.warning
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Contact Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                supplier.phone?.let { phone ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                supplier.email?.let { email ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Statistics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(
                    icon = Icons.Default.ShoppingCart,
                    label = "الطلبات",
                    value = "${supplier.totalOrders ?: 0}",
                    color = AppTheme.colors.info
                )

                StatItem(
                    icon = Icons.Default.AttachMoney,
                    label = "المبلغ",
                    value = SupplierMapper.formatTotalAmount(supplier.totalAmount),
                    color = AppTheme.colors.success
                )

                StatItem(
                    icon = Icons.Default.Star,
                    label = "التقييم",
                    value = SupplierMapper.formatRating(supplier.rating),
                    color = AppTheme.colors.warning
                )
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                IconButton(
                    onClick = { onEdit(supplier) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "تعديل",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { onDelete(supplier) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Stat Item Component for Supplier Card
 */
@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Enhanced Purchase Order Card Component
 */
@Composable
fun EnhancedPurchaseOrderCard(
    orderId: String,
    supplierName: String,
    orderDate: String,
    totalAmount: Double,
    status: String,
    itemsCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = orderId,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (status) {
                            "تم التسليم" -> AppTheme.colors.success.copy(alpha = 0.1f)
                            "ملغي" -> AppTheme.colors.error.copy(alpha = 0.1f)
                            else -> AppTheme.colors.warning.copy(alpha = 0.1f)
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (status) {
                            "تم التسليم" -> AppTheme.colors.success
                            "ملغي" -> AppTheme.colors.error
                            else -> AppTheme.colors.warning
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Supplier and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = supplierName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = orderDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount and Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%,.2f ر.س", totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "$itemsCount عنصر",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
