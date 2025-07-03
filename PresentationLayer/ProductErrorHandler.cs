using DevExpress.XtraEditors;
using System;
using System.IO;
using System.Text;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// معالج أخطاء المنتجات - Product Error Handler
    /// </summary>
    public static class ProductErrorHandler
    {
        /// <summary>
        /// معالجة أخطاء قاعدة البيانات - Handle database errors
        /// </summary>
        public static void HandleDatabaseError(Exception ex, string operation)
        {
            var message = GetUserFriendlyMessage(ex, operation);
            
            XtraMessageBox.Show(message, "خطأ في قاعدة البيانات",
                MessageBoxButtons.OK, MessageBoxIcon.Error);
            
            // تسجيل الخطأ للمطورين - Log error for developers
            LogError(ex, operation);
        }

        /// <summary>
        /// معالجة أخطاء التحقق من صحة البيانات - Handle validation errors
        /// </summary>
        public static void HandleValidationError(string validationMessage, string field = "")
        {
            var title = string.IsNullOrEmpty(field) ? "خطأ في البيانات" : $"خطأ في {field}";
            
            XtraMessageBox.Show(validationMessage, title,
                MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }

        /// <summary>
        /// معالجة أخطاء الشبكة - Handle network errors
        /// </summary>
        public static void HandleNetworkError(Exception ex, string operation)
        {
            var message = $"خطأ في الاتصال أثناء {operation}.\n\n" +
                         "يرجى التحقق من:\n" +
                         "• اتصال الإنترنت\n" +
                         "• إعدادات الخادم\n" +
                         "• جدار الحماية\n\n" +
                         "إذا استمرت المشكلة، يرجى الاتصال بالدعم الفني.";

            XtraMessageBox.Show(message, "خطأ في الاتصال",
                MessageBoxButtons.OK, MessageBoxIcon.Error);
            
            LogError(ex, operation);
        }

        /// <summary>
        /// معالجة أخطاء الصلاحيات - Handle permission errors
        /// </summary>
        public static void HandlePermissionError(string operation)
        {
            var message = $"ليس لديك صلاحية لتنفيذ العملية: {operation}\n\n" +
                         "يرجى الاتصال بمدير النظام للحصول على الصلاحيات المطلوبة.";

            XtraMessageBox.Show(message, "خطأ في الصلاحيات",
                MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }

        /// <summary>
        /// معالجة أخطاء عامة - Handle general errors
        /// </summary>
        public static void HandleGeneralError(Exception ex, string operation)
        {
            var message = $"حدث خطأ غير متوقع أثناء {operation}.\n\n" +
                         "يرجى المحاولة مرة أخرى، وإذا استمرت المشكلة، " +
                         "يرجى الاتصال بالدعم الفني.";

            XtraMessageBox.Show(message, "خطأ",
                MessageBoxButtons.OK, MessageBoxIcon.Error);
            
            LogError(ex, operation);
        }

        /// <summary>
        /// عرض رسالة نجح - Show success message
        /// </summary>
        public static void ShowSuccess(string message, string title = "نجح")
        {
            XtraMessageBox.Show(message, title,
                MessageBoxButtons.OK, MessageBoxIcon.Information);
        }

        /// <summary>
        /// عرض رسالة تحذير - Show warning message
        /// </summary>
        public static void ShowWarning(string message, string title = "تحذير")
        {
            XtraMessageBox.Show(message, title,
                MessageBoxButtons.OK, MessageBoxIcon.Warning);
        }

        /// <summary>
        /// عرض رسالة تأكيد - Show confirmation message
        /// </summary>
        public static DialogResult ShowConfirmation(string message, string title = "تأكيد")
        {
            return XtraMessageBox.Show(message, title,
                MessageBoxButtons.YesNo, MessageBoxIcon.Question);
        }

        /// <summary>
        /// الحصول على رسالة مفهومة للمستخدم - Get user-friendly message
        /// </summary>
        private static string GetUserFriendlyMessage(Exception ex, string operation)
        {
            var message = new StringBuilder();
            message.AppendLine($"حدث خطأ أثناء {operation}:");
            message.AppendLine();

            // تحليل نوع الخطأ وإعطاء رسالة مناسبة
            if (ex.Message.Contains("timeout"))
            {
                message.AppendLine("انتهت مهلة الاتصال. يرجى المحاولة مرة أخرى.");
            }
            else if (ex.Message.Contains("connection"))
            {
                message.AppendLine("فشل في الاتصال بقاعدة البيانات.");
                message.AppendLine("يرجى التحقق من إعدادات الاتصال.");
            }
            else if (ex.Message.Contains("duplicate") || ex.Message.Contains("unique"))
            {
                message.AppendLine("البيانات المدخلة مكررة.");
                message.AppendLine("يرجى التحقق من كود المنتج أو الباركود.");
            }
            else if (ex.Message.Contains("foreign key") || ex.Message.Contains("reference"))
            {
                message.AppendLine("لا يمكن تنفيذ العملية بسبب وجود بيانات مرتبطة.");
                message.AppendLine("يرجى حذف البيانات المرتبطة أولاً.");
            }
            else if (ex.Message.Contains("permission") || ex.Message.Contains("access"))
            {
                message.AppendLine("ليس لديك صلاحية لتنفيذ هذه العملية.");
            }
            else
            {
                message.AppendLine("خطأ غير متوقع.");
                message.AppendLine("يرجى المحاولة مرة أخرى أو الاتصال بالدعم الفني.");
            }

            return message.ToString();
        }

        /// <summary>
        /// تسجيل الخطأ - Log error
        /// </summary>
        private static void LogError(Exception ex, string operation)
        {
            try
            {
                var logMessage = $"[{DateTime.Now:yyyy-MM-dd HH:mm:ss}] خطأ في {operation}:\n" +
                               $"النوع: {ex.GetType().Name}\n" +
                               $"الرسالة: {ex.Message}\n" +
                               $"التفاصيل: {ex.StackTrace}\n" +
                               new string('-', 80) + "\n";

                // كتابة إلى ملف السجل
                var logPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Logs");
                if (!Directory.Exists(logPath))
                    Directory.CreateDirectory(logPath);

                var logFile = Path.Combine(logPath, $"ProductErrors_{DateTime.Now:yyyyMMdd}.log");
                File.AppendAllText(logFile, logMessage);

                // كتابة إلى نافذة التشخيص في Visual Studio
                System.Diagnostics.Debug.WriteLine($"خطأ في {operation}: {ex}");
            }
            catch
            {
                // تجاهل أخطاء التسجيل لتجنب التكرار اللانهائي
            }
        }
    }

    /// <summary>
    /// مساعد مؤشرات التحميل - Loading Indicators Helper
    /// </summary>
    public static class ProductLoadingHelper
    {
        /// <summary>
        /// تنفيذ عملية مع مؤشر تحميل - Execute operation with loading indicator
        /// </summary>
        public static async Task ExecuteWithLoadingAsync(
            Func<Task> operation, 
            string loadingMessage = "جاري المعالجة...", 
            Control? parentControl = null)
        {
            var loadingForm = ShowLoading(loadingMessage, parentControl);
            
            try
            {
                await operation();
            }
            finally
            {
                HideLoading(loadingForm);
            }
        }

        /// <summary>
        /// تنفيذ عملية مع مؤشر تحميل وإرجاع نتيجة - Execute operation with loading and return result
        /// </summary>
        public static async Task<T> ExecuteWithLoadingAsync<T>(
            Func<Task<T>> operation, 
            string loadingMessage = "جاري المعالجة...", 
            Control? parentControl = null)
        {
            var loadingForm = ShowLoading(loadingMessage, parentControl);
            
            try
            {
                return await operation();
            }
            finally
            {
                HideLoading(loadingForm);
            }
        }

        /// <summary>
        /// عرض مؤشر التحميل - Show loading indicator
        /// </summary>
        private static Form? ShowLoading(string message, Control? parentControl)
        {
            try
            {
                var loadingForm = new Form
                {
                    Text = "",
                    Size = new Size(300, 100),
                    StartPosition = FormStartPosition.CenterParent,
                    FormBorderStyle = FormBorderStyle.None,
                    BackColor = Color.White,
                    ShowInTaskbar = false,
                    TopMost = true
                };

                var label = new Label
                {
                    Text = message,
                    Dock = DockStyle.Fill,
                    TextAlign = ContentAlignment.MiddleCenter,
                    Font = new Font("Segoe UI", 10F)
                };

                loadingForm.Controls.Add(label);

                if (parentControl?.FindForm() != null)
                {
                    loadingForm.Owner = parentControl.FindForm();
                }

                loadingForm.Show();
                Application.DoEvents();

                return loadingForm;
            }
            catch
            {
                return null;
            }
        }

        /// <summary>
        /// إخفاء مؤشر التحميل - Hide loading indicator
        /// </summary>
        private static void HideLoading(Form? loadingForm)
        {
            try
            {
                if (loadingForm != null && !loadingForm.IsDisposed)
                {
                    loadingForm.Close();
                    loadingForm.Dispose();
                }
            }
            catch
            {
                // تجاهل أخطاء الإغلاق
            }
        }
    }

    /// <summary>
    /// مساعد رسائل المستخدم - User Messages Helper
    /// </summary>
    public static class ProductUserMessages
    {
        // رسائل النجح - Success Messages
        public const string ProductCreatedSuccessfully = "تم إضافة المنتج بنجاح";
        public const string ProductUpdatedSuccessfully = "تم تحديث المنتج بنجاح";
        public const string ProductDeletedSuccessfully = "تم حذف المنتج بنجاح";
        public const string ProductsRefreshedSuccessfully = "تم تحديث قائمة المنتجات بنجاح";
        public const string StockUpdatedSuccessfully = "تم تحديث المخزون بنجاح";

        // رسائل التأكيد - Confirmation Messages
        public const string ConfirmDeleteProduct = "هل أنت متأكد من حذف المنتج '{0}'؟\nهذا الإجراء لا يمكن التراجع عنه.";
        public const string ConfirmDeleteMultipleProducts = "هل أنت متأكد من حذف {0} منتج؟\nهذا الإجراء لا يمكن التراجع عنه.";

        // رسائل التحذير - Warning Messages
        public const string NoProductSelected = "يرجى اختيار منتج أولاً";
        public const string LowStockWarning = "تحذير: كمية المخزون أقل من الحد الأدنى";
        public const string OutOfStockWarning = "تحذير: المنتج نفد من المخزون";

        // رسائل الخطأ - Error Messages
        public const string ProductNotFound = "المنتج غير موجود";
        public const string InvalidProductData = "بيانات المنتج غير صحيحة";
        public const string DuplicateProductCode = "كود المنتج مستخدم من قبل";
        public const string DuplicateBarcode = "الباركود مستخدم من قبل";
        public const string CategoryNotFound = "الفئة غير موجودة";
    }
}
