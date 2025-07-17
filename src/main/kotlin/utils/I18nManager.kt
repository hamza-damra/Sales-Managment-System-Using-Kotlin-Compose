package utils

/**
 * Centralized internationalization manager for Arabic translations
 */
object I18nManager {
    
    /**
     * Get translated text by key
     */
    fun getString(key: String, vararg args: Any): String {
        val translation = Translations.get(key)
        return if (args.isNotEmpty()) {
            String.format(translation, *args)
        } else {
            translation
        }
    }
    
    /**
     * Get translated text by key with fallback
     */
    fun getString(key: String, fallback: String, vararg args: Any): String {
        return try {
            getString(key, *args)
        } catch (e: Exception) {
            if (args.isNotEmpty()) {
                String.format(fallback, *args)
            } else {
                fallback
            }
        }
    }
}

/**
 * Translation keys and Arabic translations
 */
object Translations {
    
    private val translations = mapOf(
        // Authentication
        "auth.login.title" to "تسجيل الدخول",
        "auth.signup.title" to "إنشاء حساب جديد",
        "auth.login.subtitle" to "نظام إدارة المبيعات المتطور",
        "auth.signup.subtitle" to "أنشئ حسابك للبدء في استخدام النظام",
        "auth.username" to "اسم المستخدم",
        "auth.password" to "كلمة المرور",
        "auth.email" to "البريد الإلكتروني",
        "auth.firstName" to "الاسم الأول",
        "auth.lastName" to "اسم العائلة",
        "auth.login.button" to "تسجيل الدخول",
        "auth.signup.button" to "إنشاء حساب",
        "auth.toggle.login" to "لديك حساب بالفعل؟ تسجيل الدخول",
        "auth.toggle.signup" to "ليس لديك حساب؟ إنشاء حساب جديد",
        "auth.password.show" to "إظهار كلمة المرور",
        "auth.password.hide" to "إخفاء كلمة المرور",
        
        // Authentication Errors
        "auth.error.invalid_credentials" to "اسم المستخدم أو كلمة المرور غير صحيحة",
        "auth.error.username_required" to "اسم المستخدم مطلوب",
        "auth.error.password_required" to "كلمة المرور مطلوبة",
        "auth.error.email_required" to "البريد الإلكتروني مطلوب",
        "auth.error.email_invalid" to "تنسيق البريد الإلكتروني غير صحيح",
        "auth.error.firstName_required" to "الاسم الأول مطلوب",
        "auth.error.lastName_required" to "اسم العائلة مطلوب",
        "auth.error.username_exists" to "اسم المستخدم موجود بالفعل",
        "auth.error.email_exists" to "البريد الإلكتروني مسجل بالفعل",
        "auth.error.network" to "خطأ في الاتصال بالخادم. تأكد من اتصالك بالإنترنت وأن الخادم يعمل.",
        "auth.error.timeout" to "انتهت مهلة الطلب. يرجى المحاولة مرة أخرى.",
        "auth.error.session_expired" to "انتهت صلاحية جلسة العمل. يرجى تسجيل الدخول مرة أخرى.",
        "auth.error.access_forbidden" to "ليس لديك صلاحية للوصول إلى هذه الميزة.",
        
        // Authentication Success
        "auth.success.login" to "تم تسجيل الدخول بنجاح",
        "auth.success.signup" to "تم إنشاء الحساب بنجاح",
        
        // Form Validation
        "validation.required" to "هذا الحقل مطلوب",
        "validation.name_required" to "الاسم مطلوب",
        "validation.price_required" to "سعر صحيح مطلوب",
        "validation.price_invalid" to "يجب أن يكون السعر رقماً صحيحاً",
        "validation.quantity_required" to "كمية صحيحة مطلوبة",
        "validation.quantity_invalid" to "يجب أن تكون الكمية رقماً صحيحاً",
        "validation.phone_required" to "رقم هاتف صحيح مطلوب",
        "validation.phone_invalid" to "تنسيق رقم الهاتف غير صحيح",
        "validation.address_required" to "العنوان مطلوب",
        "validation.category_required" to "الفئة مطلوبة",
        "validation.supplier_required" to "المورد مطلوب",
        "validation.decimal_invalid" to "يجب أن يكون الرقم عشرياً صحيحاً",
        "validation.positive_number" to "يجب أن يكون الرقم موجباً",
        
        // CRUD Operations Success
        "success.added" to "تم الإضافة بنجاح",
        "success.updated" to "تم التحديث بنجاح",
        "success.deleted" to "تم الحذف بنجاح",
        "success.saved" to "تم الحفظ بنجاح",
        "success.exported" to "تم التصدير بنجاح",
        "success.imported" to "تم الاستيراد بنجاح",
        
        // CRUD Operations Errors
        "error.add_failed" to "فشل في الإضافة",
        "error.update_failed" to "فشل في التحديث",
        "error.delete_failed" to "فشل في الحذف",
        "error.save_failed" to "فشل في الحفظ",
        "error.export_failed" to "فشل في التصدير",
        "error.import_failed" to "فشل في الاستيراد",
        "error.load_failed" to "فشل في تحميل البيانات",
        
        // Network Errors
        "error.network" to "خطأ في الاتصال بالشبكة",
        "error.server" to "حدث خطأ في الخادم. يرجى المحاولة مرة أخرى لاحقاً.",
        "error.timeout" to "انتهت مهلة الطلب. يرجى المحاولة مرة أخرى.",
        "error.not_found" to "العنصر المطلوب غير موجود",
        "error.conflict" to "تعارض في البيانات. لا يمكن تنفيذ العملية بسبب ارتباط البيانات بعناصر أخرى في النظام.",
        "error.validation" to "البيانات المدخلة غير صحيحة. يرجى مراجعة المعلومات والمحاولة مرة أخرى.",
        "error.unknown" to "حدث خطأ غير متوقع. يرجى المحاولة مرة أخرى أو الاتصال بالدعم الفني.",
        
        // Loading States
        "loading.default" to "جاري التحميل...",
        "loading.processing" to "جاري المعالجة...",
        "loading.saving" to "جاري الحفظ...",
        "loading.deleting" to "جاري الحذف...",
        "loading.exporting" to "جاري التصدير...",
        "loading.importing" to "جاري الاستيراد...",
        
        // Empty States
        "empty.no_data" to "لا توجد بيانات متاحة",
        "empty.no_items" to "لا توجد عناصر",
        "empty.no_results" to "لا توجد نتائج",
        "empty.no_products" to "لا توجد منتجات",
        "empty.no_customers" to "لا يوجد عملاء",
        "empty.no_categories" to "لا توجد فئات",
        "empty.no_suppliers" to "لا يوجد موردون",
        
        // Confirmation Messages
        "confirm.delete" to "هل أنت متأكد من الحذف؟",
        "confirm.delete.product" to "هل أنت متأكد من حذف هذا المنتج؟",
        "confirm.delete.customer" to "هل أنت متأكد من حذف هذا العميل؟",
        "confirm.delete.category" to "هل أنت متأكد من حذف هذه الفئة؟",
        "confirm.delete.supplier" to "هل أنت متأكد من حذف هذا المورد؟",
        "confirm.action" to "تأكيد العملية",
        "confirm.cancel" to "إلغاء",
        "confirm.proceed" to "متابعة",
        
        // Common Actions
        "action.add" to "إضافة",
        "action.edit" to "تعديل",
        "action.delete" to "حذف",
        "action.save" to "حفظ",
        "action.cancel" to "إلغاء",
        "action.close" to "إغلاق",
        "action.search" to "بحث",
        "action.filter" to "تصفية",
        "action.export" to "تصدير",
        "action.import" to "استيراد",
        "action.refresh" to "تحديث",
        "action.retry" to "إعادة المحاولة",
        
        // Product specific
        "product.name" to "اسم المنتج",
        "product.price" to "السعر",
        "product.quantity" to "الكمية",
        "product.category" to "الفئة",
        "product.supplier" to "المورد",
        "product.description" to "الوصف",
        "product.status" to "الحالة",
        "product.stock" to "المخزون",
        "product.low_stock" to "مخزون منخفض",
        "product.out_of_stock" to "نفد المخزون",
        "product.expired" to "منتهي الصلاحية",
        
        // Customer specific
        "customer.name" to "اسم العميل",
        "customer.email" to "البريد الإلكتروني",
        "customer.phone" to "رقم الهاتف",
        "customer.address" to "العنوان",
        "customer.city" to "المدينة",
        "customer.country" to "البلد",
        
        // Category specific
        "category.name" to "اسم الفئة",
        "category.description" to "وصف الفئة",
        "category.color" to "لون الفئة",
        
        // Supplier specific
        "supplier.name" to "اسم المورد",
        "supplier.contact" to "جهة الاتصال",
        "supplier.email" to "البريد الإلكتروني",
        "supplier.phone" to "رقم الهاتف",
        "supplier.address" to "العنوان",

        // Error messages for specific operations
        "error.product.delete_has_sales" to "لا يمكن حذف المنتج لأنه مرتبط بمبيعات في النظام. يرجى إكمال أو إلغاء جميع المبيعات المرتبطة بهذا المنتج أولاً.",
        "error.customer.delete_has_sales" to "لا يمكن حذف العميل لأنه مرتبط بمبيعات في النظام. يرجى إكمال أو إلغاء جميع المبيعات المرتبطة بهذا العميل أولاً.",
        "error.customer.delete_has_returns" to "لا يمكن حذف العميل لأنه مرتبط بمرتجعات في النظام. يرجى معالجة جميع المرتجعات المرتبطة بهذا العميل أولاً.",
        "error.category.delete_has_products" to "لا يمكن حذف الفئة لأنها مرتبطة بمنتجات في النظام. يرجى إعادة تصنيف أو حذف جميع المنتجات في هذه الفئة أولاً.",
        "error.supplier.delete_has_products" to "لا يمكن حذف المورد لأنه مرتبط بمنتجات في النظام. يرجى إعادة تعيين أو حذف جميع المنتجات المرتبطة بهذا المورد أولاً.",

        // Success messages for specific operations
        "success.product.added" to "تم إضافة المنتج بنجاح",
        "success.product.updated" to "تم تحديث المنتج بنجاح",
        "success.product.deleted" to "تم حذف المنتج بنجاح",
        "success.customer.added" to "تم إضافة العميل بنجاح",
        "success.customer.updated" to "تم تحديث العميل بنجاح",
        "success.customer.deleted" to "تم حذف العميل بنجاح",
        "success.category.added" to "تم إضافة الفئة بنجاح",
        "success.category.updated" to "تم تحديث الفئة بنجاح",
        "success.category.deleted" to "تم حذف الفئة بنجاح",
        "success.supplier.added" to "تم إضافة المورد بنجاح",
        "success.supplier.updated" to "تم تحديث المورد بنجاح",
        "success.supplier.deleted" to "تم حذف المورد بنجاح",

        // Loading messages for specific operations
        "loading.products" to "جاري تحميل المنتجات...",
        "loading.customers" to "جاري تحميل العملاء...",
        "loading.categories" to "جاري تحميل الفئات...",
        "loading.suppliers" to "جاري تحميل الموردين...",
        "loading.adding_product" to "جاري إضافة المنتج...",
        "loading.updating_product" to "جاري تحديث المنتج...",
        "loading.deleting_product" to "جاري حذف المنتج...",

        // Error messages for loading
        "error.load_products" to "حدث خطأ في تحميل المنتجات",
        "error.load_customers" to "حدث خطأ في تحميل العملاء",
        "error.load_categories" to "حدث خطأ في تحميل الفئات",
        "error.load_suppliers" to "حدث خطأ في تحميل الموردين",

        // Import/Export messages
        "success.export_excel" to "تم تصدير البيانات إلى Excel بنجاح",
        "success.export_csv" to "تم تصدير البيانات إلى CSV بنجاح",
        "success.export_json" to "تم تصدير البيانات إلى JSON بنجاح",
        "success.import_data" to "تم استيراد البيانات بنجاح",
        "error.export_failed" to "فشل في تصدير البيانات",
        "error.import_failed" to "فشل في استيراد البيانات",
        "error.file_not_selected" to "لم يتم اختيار ملف",
        "error.invalid_file_format" to "تنسيق الملف غير صحيح",

        // Dialog titles and buttons
        "dialog.add_product" to "إضافة منتج جديد",
        "dialog.edit_product" to "تعديل المنتج",
        "dialog.add_customer" to "إضافة عميل جديد",
        "dialog.edit_customer" to "تعديل العميل",
        "dialog.add_category" to "إضافة فئة جديدة",
        "dialog.edit_category" to "تعديل الفئة",
        "dialog.add_supplier" to "إضافة مورد جديد",
        "dialog.edit_supplier" to "تعديل المورد",
        "dialog.confirm_delete" to "تأكيد الحذف",
        "dialog.export_options" to "خيارات التصدير",
        "dialog.import_data" to "استيراد البيانات",

        // Status messages
        "status.active" to "نشط",
        "status.inactive" to "غير نشط",
        "status.discontinued" to "متوقف",
        "status.pending" to "في الانتظار",
        "status.completed" to "مكتمل",
        "status.cancelled" to "ملغي"
    )

    fun get(key: String): String {
        return translations[key] ?: key
    }

    fun contains(key: String): Boolean {
        return translations.containsKey(key)
    }
}
