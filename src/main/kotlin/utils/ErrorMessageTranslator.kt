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
            // Customer deletion errors
            errorCode == "CUSTOMER_HAS_SALES" -> {
                val count = extractNumberFromMessage(errorMessage)
                val customerName = details?.resourceType ?: "العميل"
                if (count != null) {
                    "لا يمكن حذف العميل لأنه مرتبط بـ $count من المبيعات. يرجى إكمال أو إلغاء جميع المبيعات المرتبطة بهذا العميل أولاً."
                } else {
                    "لا يمكن حذف العميل لأنه مرتبط بمبيعات في النظام. يرجى إكمال أو إلغاء جميع المبيعات المرتبطة بهذا العميل أولاً."
                }
            }
            
            errorCode == "CUSTOMER_HAS_RETURNS" -> {
                val count = extractNumberFromMessage(errorMessage)
                if (count != null) {
                    "لا يمكن حذف العميل لأنه مرتبط بـ $count من المرتجعات. يرجى معالجة جميع المرتجعات المرتبطة بهذا العميل أولاً."
                } else {
                    "لا يمكن حذف العميل لأنه مرتبط بمرتجعات في النظام. يرجى معالجة جميع المرتجعات المرتبطة بهذا العميل أولاً."
                }
            }
            
            // Product deletion errors
            errorCode == "PRODUCT_HAS_SALES" -> {
                val count = extractNumberFromMessage(errorMessage)
                if (count != null) {
                    "لا يمكن حذف المنتج لأنه مرتبط بـ $count من المبيعات. يرجى إكمال أو إلغاء جميع المبيعات المرتبطة بهذا المنتج أولاً."
                } else {
                    "لا يمكن حذف المنتج لأنه مرتبط بمبيعات في النظام. يرجى إكمال أو إلغاء جميع المبيعات المرتبطة بهذا المنتج أولاً."
                }
            }
            
            // Supplier deletion errors
            errorCode == "SUPPLIER_HAS_PRODUCTS" -> {
                val count = extractNumberFromMessage(errorMessage)
                if (count != null) {
                    "لا يمكن حذف المورد لأنه مرتبط بـ $count من المنتجات. يرجى إعادة تعيين أو حذف جميع المنتجات المرتبطة بهذا المورد أولاً."
                } else {
                    "لا يمكن حذف المورد لأنه مرتبط بمنتجات في النظام. يرجى إعادة تعيين أو حذف جميع المنتجات المرتبطة بهذا المورد أولاً."
                }
            }
            
            // Category deletion errors
            errorCode == "CATEGORY_HAS_PRODUCTS" -> {
                val count = extractNumberFromMessage(errorMessage)
                if (count != null) {
                    "لا يمكن حذف الفئة لأنها مرتبطة بـ $count من المنتجات. يرجى إعادة تصنيف أو حذف جميع المنتجات في هذه الفئة أولاً."
                } else {
                    "لا يمكن حذف الفئة لأنها مرتبطة بمنتجات في النظام. يرجى إعادة تصنيف أو حذف جميع المنتجات في هذه الفئة أولاً."
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

            // Authentication errors
            errorMessage.contains("Authentication failed", ignoreCase = true) ||
            errorMessage.contains("Token invalid", ignoreCase = true) ||
            errorMessage.contains("Token expired", ignoreCase = true) -> {
                "انتهت صلاحية جلسة العمل. يرجى تسجيل الدخول مرة أخرى."
            }
            
            errorMessage.contains("Access forbidden", ignoreCase = true) ||
            errorMessage.contains("Insufficient permissions", ignoreCase = true) -> {
                "ليس لديك صلاحية للوصول إلى هذه الميزة."
            }
            
            // Network errors
            errorMessage.contains("Network error", ignoreCase = true) ||
            errorMessage.contains("Cannot connect to server", ignoreCase = true) -> {
                "خطأ في الاتصال بالخادم. تأكد من اتصالك بالإنترنت وأن الخادم يعمل."
            }
            
            errorMessage.contains("Request timeout", ignoreCase = true) -> {
                "انتهت مهلة الطلب. يرجى المحاولة مرة أخرى."
            }
            
            // Validation errors
            errorMessage.contains("Validation failed", ignoreCase = true) -> {
                "البيانات المدخلة غير صحيحة. يرجى مراجعة المعلومات والمحاولة مرة أخرى."
            }
            
            // Server errors
            errorMessage.contains("Server error", ignoreCase = true) -> {
                "حدث خطأ في الخادم. يرجى المحاولة مرة أخرى لاحقاً."
            }
            
            // Not found errors
            errorMessage.contains("not found", ignoreCase = true) -> {
                "العنصر المطلوب غير موجود."
            }
            
            // Generic conflict errors
            errorMessage.contains("Conflict", ignoreCase = true) ||
            errorMessage.contains("Data Integrity Violation", ignoreCase = true) -> {
                "تعارض في البيانات. لا يمكن تنفيذ العملية بسبب ارتباط البيانات بعناصر أخرى في النظام."
            }
            
            // Default fallback
            else -> {
                // Try to extract meaningful information from the original message
                when {
                    errorMessage.contains("customer", ignoreCase = true) && 
                    errorMessage.contains("delete", ignoreCase = true) -> {
                        "لا يمكن حذف العميل. يرجى التأكد من عدم وجود بيانات مرتبطة به."
                    }
                    errorMessage.contains("product", ignoreCase = true) && 
                    errorMessage.contains("delete", ignoreCase = true) -> {
                        "لا يمكن حذف المنتج. يرجى التأكد من عدم وجود بيانات مرتبطة به."
                    }
                    errorMessage.contains("supplier", ignoreCase = true) && 
                    errorMessage.contains("delete", ignoreCase = true) -> {
                        "لا يمكن حذف المورد. يرجى التأكد من عدم وجود بيانات مرتبطة به."
                    }
                    else -> {
                        "حدث خطأ غير متوقع. يرجى المحاولة مرة أخرى أو الاتصال بالدعم الفني."
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
                "يرجى تسجيل الدخول مرة أخرى."
            }
            else -> suggestions // Return original if no translation available
        }
    }
}
