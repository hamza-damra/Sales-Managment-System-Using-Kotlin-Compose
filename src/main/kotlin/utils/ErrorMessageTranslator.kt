package utils

import data.api.ErrorDetails

/**
 * Utility class for translating error messages to Arabic
 */
object ErrorMessageTranslator {

    /**
     * Translates error messages from English to Arabic
     */
    fun translateToArabic(
        errorMessage: String,
        errorCode: String? = null,
        suggestions: String? = null,
        details: ErrorDetails? = null
    ): String {
        return when {
            // Authentication errors
            errorMessage.contains("Invalid username or password", ignoreCase = true) ||
            errorMessage.contains("Authentication failed", ignoreCase = true) -> {
                "اسم المستخدم أو كلمة المرور غير صحيحة"
            }

            errorMessage.contains("Username is required", ignoreCase = true) ||
            errorMessage.contains("Username cannot be empty", ignoreCase = true) -> {
                "اسم المستخدم مطلوب"
            }

            errorMessage.contains("Password is required", ignoreCase = true) ||
            errorMessage.contains("Password cannot be empty", ignoreCase = true) -> {
                "كلمة المرور مطلوبة"
            }

            errorMessage.contains("Email is required", ignoreCase = true) ||
            errorMessage.contains("Email cannot be empty", ignoreCase = true) -> {
                "البريد الإلكتروني مطلوب"
            }

            errorMessage.contains("Invalid email format", ignoreCase = true) ||
            errorMessage.contains("Email format is invalid", ignoreCase = true) -> {
                "تنسيق البريد الإلكتروني غير صحيح"
            }

            errorMessage.contains("First name is required", ignoreCase = true) ||
            errorMessage.contains("First name cannot be empty", ignoreCase = true) -> {
                "الاسم الأول مطلوب"
            }

            errorMessage.contains("Last name is required", ignoreCase = true) ||
            errorMessage.contains("Last name cannot be empty", ignoreCase = true) -> {
                "اسم العائلة مطلوب"
            }

            errorMessage.contains("Username already exists", ignoreCase = true) ||
            errorMessage.contains("Username is already taken", ignoreCase = true) -> {
                "اسم المستخدم موجود بالفعل"
            }

            errorMessage.contains("Email already exists", ignoreCase = true) ||
            errorMessage.contains("Email is already registered", ignoreCase = true) -> {
                "البريد الإلكتروني مسجل بالفعل"
            }

            errorMessage.contains("Registration successful", ignoreCase = true) ||
            errorMessage.contains("Account created successfully", ignoreCase = true) -> {
                "تم إنشاء الحساب بنجاح"
            }

            errorMessage.contains("Login successful", ignoreCase = true) -> {
                "تم تسجيل الدخول بنجاح"
            }

            // Customer deletion errors
            errorCode == "CUSTOMER_HAS_SALES" -> {
                val count = extractNumberFromMessage(errorMessage)
                if (count != null) {
                    I18nManager.getString("error.customer.delete_has_sales").replace("%d", count)
                } else {
                    I18nManager.getString("error.customer.delete_has_sales")
                }
            }

            errorCode == "CUSTOMER_HAS_RETURNS" -> {
                val count = extractNumberFromMessage(errorMessage)
                if (count != null) {
                    I18nManager.getString("error.customer.delete_has_returns").replace("%d", count)
                } else {
                    I18nManager.getString("error.customer.delete_has_returns")
                }
            }

            // Product deletion errors
            errorCode == "PRODUCT_HAS_SALES" -> {
                val count = extractNumberFromMessage(errorMessage)
                if (count != null) {
                    I18nManager.getString("error.product.delete_has_sales").replace("%d", count)
                } else {
                    I18nManager.getString("error.product.delete_has_sales")
                }
            }

            // Supplier deletion errors
            errorCode == "SUPPLIER_HAS_PRODUCTS" -> {
                val count = extractNumberFromMessage(errorMessage)
                if (count != null) {
                    I18nManager.getString("error.supplier.delete_has_products").replace("%d", count)
                } else {
                    I18nManager.getString("error.supplier.delete_has_products")
                }
            }

            // Category deletion errors
            errorCode == "CATEGORY_HAS_PRODUCTS" -> {
                val count = extractNumberFromMessage(errorMessage)
                if (count != null) {
                    I18nManager.getString("error.category.delete_has_products").replace("%d", count)
                } else {
                    I18nManager.getString("error.category.delete_has_products")
                }
            }

            // Business logic errors (new backend API)
            errorCode == "BUSINESS_LOGIC_ERROR" -> {
                when {
                    errorMessage.contains("BLACKLISTED", ignoreCase = true) -> {
                        "لا يمكن حذف العميل لأن حالته 'محظور'. العملاء المحظورون لا يمكن حذفهم من النظام."
                    }
                    errorMessage.contains("current status", ignoreCase = true) -> {
                        "لا يمكن حذف العميل بسبب حالته الحالية. يرجى تغيير حالة العميل أولاً."
                    }
                    errorMessage.contains("cannot be restored", ignoreCase = true) -> {
                        "لا يمكن استعادة العميل لأنه لم يتم حذفه مسبقاً."
                    }
                    else -> {
                        "لا يمكن تنفيذ العملية بسبب قواعد العمل. يرجى مراجعة حالة العميل والمحاولة مرة أخرى."
                    }
                }
            }

            // Resource not found errors
            errorCode == "RESOURCE_NOT_FOUND" -> {
                when {
                    errorMessage.contains("Customer not found", ignoreCase = true) -> {
                        "العميل غير موجود. قد يكون تم حذفه مسبقاً أو أن المعرف غير صحيح."
                    }
                    errorMessage.contains("Product not found", ignoreCase = true) -> {
                        "المنتج غير موجود. قد يكون تم حذفه مسبقاً أو أن المعرف غير صحيح."
                    }
                    else -> {
                        "العنصر المطلوب غير موجود في النظام."
                    }
                }
            }
            
            // Promotion endpoint routing errors
            errorMessage.contains("Promotion Endpoint Routing Error", ignoreCase = true) ||
            (errorMessage.contains("Invalid Parameter Type", ignoreCase = true) &&
             errorMessage.contains("Expected a valid long", ignoreCase = true)) -> {
                "خطأ في توجيه نقطة النهاية للعروض الترويجية. الخادم يفسر 'expired' أو 'scheduled' كمعرف رقمي بدلاً من مسار نقطة النهاية. سيتم استخدام طريقة بديلة لتحميل البيانات."
            }

            // Form validation errors
            errorMessage.contains("Name is required", ignoreCase = true) ||
            errorMessage.contains("Name cannot be empty", ignoreCase = true) -> {
                "الاسم مطلوب"
            }

            errorMessage.contains("Price is required", ignoreCase = true) ||
            errorMessage.contains("Invalid price", ignoreCase = true) -> {
                "سعر صحيح مطلوب"
            }

            errorMessage.contains("Quantity is required", ignoreCase = true) ||
            errorMessage.contains("Invalid quantity", ignoreCase = true) -> {
                "كمية صحيحة مطلوبة"
            }

            errorMessage.contains("Phone number is required", ignoreCase = true) ||
            errorMessage.contains("Invalid phone number", ignoreCase = true) -> {
                "رقم هاتف صحيح مطلوب"
            }

            errorMessage.contains("Address is required", ignoreCase = true) -> {
                "العنوان مطلوب"
            }

            errorMessage.contains("Category is required", ignoreCase = true) -> {
                "الفئة مطلوبة"
            }

            errorMessage.contains("Supplier is required", ignoreCase = true) -> {
                "المورد مطلوب"
            }

            // Success messages
            errorMessage.contains("Added successfully", ignoreCase = true) ||
            errorMessage.contains("Created successfully", ignoreCase = true) -> {
                "تم الإضافة بنجاح"
            }

            errorMessage.contains("Updated successfully", ignoreCase = true) ||
            errorMessage.contains("Modified successfully", ignoreCase = true) -> {
                "تم التحديث بنجاح"
            }

            errorMessage.contains("Deleted successfully", ignoreCase = true) ||
            errorMessage.contains("Removed successfully", ignoreCase = true) -> {
                "تم الحذف بنجاح"
            }

            errorMessage.contains("Saved successfully", ignoreCase = true) -> {
                "تم الحفظ بنجاح"
            }

            errorMessage.contains("Exported successfully", ignoreCase = true) -> {
                "تم التصدير بنجاح"
            }

            errorMessage.contains("Imported successfully", ignoreCase = true) -> {
                "تم الاستيراد بنجاح"
            }

            // Loading and status messages
            errorMessage.contains("Loading", ignoreCase = true) -> {
                "جاري التحميل..."
            }

            errorMessage.contains("Processing", ignoreCase = true) -> {
                "جاري المعالجة..."
            }

            errorMessage.contains("No data available", ignoreCase = true) ||
            errorMessage.contains("No items found", ignoreCase = true) -> {
                "لا توجد بيانات متاحة"
            }

            // Authentication errors (existing)
            errorMessage.contains("Authentication failed", ignoreCase = true) ||
            errorMessage.contains("Token invalid", ignoreCase = true) ||
            errorMessage.contains("Token expired", ignoreCase = true) -> {
                I18nManager.getString("auth.error.session_expired")
            }

            errorMessage.contains("Access forbidden", ignoreCase = true) ||
            errorMessage.contains("Insufficient permissions", ignoreCase = true) -> {
                I18nManager.getString("auth.error.access_forbidden")
            }

            // Network errors
            errorMessage.contains("Network error", ignoreCase = true) ||
            errorMessage.contains("Cannot connect to server", ignoreCase = true) -> {
                I18nManager.getString("auth.error.network")
            }

            errorMessage.contains("Request timeout", ignoreCase = true) -> {
                I18nManager.getString("auth.error.timeout")
            }

            // Validation errors
            errorMessage.contains("Validation failed", ignoreCase = true) -> {
                I18nManager.getString("error.validation")
            }

            // Server errors
            errorMessage.contains("Server error", ignoreCase = true) -> {
                I18nManager.getString("error.server")
            }

            // Not found errors
            errorMessage.contains("not found", ignoreCase = true) -> {
                I18nManager.getString("error.not_found")
            }

            // Generic conflict errors
            errorMessage.contains("Conflict", ignoreCase = true) ||
            errorMessage.contains("Data Integrity Violation", ignoreCase = true) -> {
                I18nManager.getString("error.conflict")
            }
            
            // Default fallback
            else -> {
                // Try to extract meaningful information from the original message
                when {
                    errorMessage.contains("customer", ignoreCase = true) &&
                    errorMessage.contains("delete", ignoreCase = true) -> {
                        I18nManager.getString("error.customer.delete_has_sales")
                    }
                    errorMessage.contains("product", ignoreCase = true) &&
                    errorMessage.contains("delete", ignoreCase = true) -> {
                        I18nManager.getString("error.product.delete_has_sales")
                    }
                    errorMessage.contains("supplier", ignoreCase = true) &&
                    errorMessage.contains("delete", ignoreCase = true) -> {
                        I18nManager.getString("error.supplier.delete_has_products")
                    }
                    else -> {
                        I18nManager.getString("error.unknown")
                    }
                }
            }
        }
    }
    
    /**
     * Extracts numbers from error messages (e.g., "3 associated sales" -> "3")
     */
    private fun extractNumberFromMessage(message: String): String? {
        val patterns = listOf(
            "they have (\\d+) associated".toRegex(),
            "because they have (\\d+)".toRegex(),
            "(\\d+) associated".toRegex(),
            "with (\\d+)".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }
    
    /**
     * Translates suggestions to Arabic
     */
    fun translateSuggestions(suggestions: String?): String? {
        if (suggestions.isNullOrBlank()) return null

        return when {
            suggestions.contains("complete, cancel, or reassign", ignoreCase = true) -> {
                "يرجى إكمال أو إلغاء أو إعادة تعيين جميع العمليات المرتبطة قبل الحذف."
            }
            suggestions.contains("reassign or delete", ignoreCase = true) -> {
                "يرجى إعادة تعيين أو حذف العناصر المرتبطة أولاً."
            }
            suggestions.contains("login again", ignoreCase = true) -> {
                I18nManager.getString("auth.error.session_expired")
            }
            else -> suggestions // Return original if no translation available
        }
    }
}
