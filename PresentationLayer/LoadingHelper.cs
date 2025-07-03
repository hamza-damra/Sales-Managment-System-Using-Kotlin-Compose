using DevExpress.XtraEditors;
using DevExpress.XtraSplashScreen;
using System;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// مساعد التحميل - Loading Helper
    /// </summary>
    public static class LoadingHelper
    {
        private static bool _isLoading = false;

        /// <summary>
        /// تنفيذ عملية مع مؤشر التحميل - Execute operation with loading indicator
        /// </summary>
        /// <typeparam name="T">نوع النتيجة - Result type</typeparam>
        /// <param name="operation">العملية المراد تنفيذها - Operation to execute</param>
        /// <param name="loadingMessage">رسالة التحميل - Loading message</param>
        /// <param name="parentForm">النموذج الأب - Parent form</param>
        /// <returns>نتيجة العملية - Operation result</returns>
        public static async Task<T> ExecuteWithLoadingAsync<T>(
            Func<Task<T>> operation,
            string loadingMessage = "جاري التحميل...",
            Form? parentForm = null)
        {
            if (_isLoading) return default(T)!;

            _isLoading = true;
            
            try
            {
                // Show loading indicator
                ShowLoading(loadingMessage, parentForm);

                // Execute operation
                var result = await operation();

                return result;
            }
            catch (Exception ex)
            {
                // Show error message
                XtraMessageBox.Show(
                    $"حدث خطأ أثناء العملية:\n{ex.Message}",
                    "خطأ",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                
                return default(T)!;
            }
            finally
            {
                // Hide loading indicator
                HideLoading();
                _isLoading = false;
            }
        }

        /// <summary>
        /// تنفيذ عملية مع مؤشر التحميل بدون إرجاع قيمة - Execute operation with loading indicator without return value
        /// </summary>
        /// <param name="operation">العملية المراد تنفيذها - Operation to execute</param>
        /// <param name="loadingMessage">رسالة التحميل - Loading message</param>
        /// <param name="parentForm">النموذج الأب - Parent form</param>
        public static async Task ExecuteWithLoadingAsync(
            Func<Task> operation,
            string loadingMessage = "جاري التحميل...",
            Form? parentForm = null)
        {
            if (_isLoading) return;

            _isLoading = true;
            
            try
            {
                // Show loading indicator
                ShowLoading(loadingMessage, parentForm);

                // Execute operation
                await operation();
            }
            catch (Exception ex)
            {
                // Show error message
                XtraMessageBox.Show(
                    $"حدث خطأ أثناء العملية:\n{ex.Message}",
                    "خطأ",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
            }
            finally
            {
                // Hide loading indicator
                HideLoading();
                _isLoading = false;
            }
        }

        /// <summary>
        /// عرض مؤشر التحميل - Show loading indicator
        /// </summary>
        /// <param name="message">الرسالة - Message</param>
        /// <param name="parentForm">النموذج الأب - Parent form</param>
        private static void ShowLoading(string message, Form? parentForm = null)
        {
            try
            {
                if (parentForm != null)
                {
                    parentForm.Cursor = Cursors.WaitCursor;
                }

                SplashScreenManager.ShowForm(
                    parentForm,
                    typeof(LoadingForm),
                    true,
                    true,
                    false);

                SplashScreenManager.Default.SetWaitFormCaption(message);
                SplashScreenManager.Default.SetWaitFormDescription("يرجى الانتظار...");
            }
            catch
            {
                // Fallback to simple cursor change
                if (parentForm != null)
                {
                    parentForm.Cursor = Cursors.WaitCursor;
                }
            }
        }

        /// <summary>
        /// إخفاء مؤشر التحميل - Hide loading indicator
        /// </summary>
        private static void HideLoading()
        {
            try
            {
                if (SplashScreenManager.Default != null)
                {
                    SplashScreenManager.CloseForm();
                }
            }
            catch
            {
                // Ignore errors when closing
            }
            finally
            {
                // Reset cursor for all forms
                foreach (Form form in Application.OpenForms)
                {
                    if (form != null && !form.IsDisposed)
                    {
                        form.Cursor = Cursors.Default;
                    }
                }
            }
        }

        /// <summary>
        /// عرض رسالة نجاح - Show success message
        /// </summary>
        /// <param name="message">الرسالة - Message</param>
        /// <param name="title">العنوان - Title</param>
        public static void ShowSuccess(string message, string title = "نجح")
        {
            XtraMessageBox.Show(
                message,
                title,
                MessageBoxButtons.OK,
                MessageBoxIcon.Information);
        }

        /// <summary>
        /// عرض رسالة خطأ - Show error message
        /// </summary>
        /// <param name="message">الرسالة - Message</param>
        /// <param name="title">العنوان - Title</param>
        public static void ShowError(string message, string title = "خطأ")
        {
            XtraMessageBox.Show(
                message,
                title,
                MessageBoxButtons.OK,
                MessageBoxIcon.Error);
        }

        /// <summary>
        /// عرض رسالة تحذير - Show warning message
        /// </summary>
        /// <param name="message">الرسالة - Message</param>
        /// <param name="title">العنوان - Title</param>
        public static void ShowWarning(string message, string title = "تحذير")
        {
            XtraMessageBox.Show(
                message,
                title,
                MessageBoxButtons.OK,
                MessageBoxIcon.Warning);
        }

        /// <summary>
        /// عرض رسالة تأكيد - Show confirmation message
        /// </summary>
        /// <param name="message">الرسالة - Message</param>
        /// <param name="title">العنوان - Title</param>
        /// <returns>نتيجة التأكيد - Confirmation result</returns>
        public static bool ShowConfirmation(string message, string title = "تأكيد")
        {
            var result = XtraMessageBox.Show(
                message,
                title,
                MessageBoxButtons.YesNo,
                MessageBoxIcon.Question);

            return result == DialogResult.Yes;
        }
    }
}
