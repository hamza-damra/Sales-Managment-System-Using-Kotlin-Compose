using DXApplication1.Utilities;
using DXApplication1.PresentationLayer;
using DXApplication1.DataAccessLayer;
using System.Globalization;
using System.Windows.Forms;
using Serilog;
using DevExpress.LookAndFeel;
using DevExpress.Skins;
using DevExpress.UserSkins;

namespace DXApplication1
{
    internal static class Program
    {
        /// <summary>
        /// نقطة الدخول الرئيسية للتطبيق - The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            try
            {
                // إعداد السجلات - Setup Logging
                SetupLogging();

                // إعداد الثقافة العربية - Setup Arabic Culture
                SetupCulture();

                // إعداد واجهة المستخدم - Setup UI
                SetupUI();

                // تهيئة قاعدة البيانات - Initialize Database
                InitializeDatabase();

                // تشغيل التطبيق - Run Application
                Log.Information("تم بدء تشغيل نظام إدارة المبيعات - Sales Management System Started");

                // Start with login form instead of Form1
                Application.Run(new LoginForm());
            }
            catch (Exception ex)
            {
                Log.Fatal(ex, "خطأ في بدء تشغيل التطبيق - Fatal error starting application");
                MessageBox.Show($"خطأ في بدء تشغيل التطبيق:\n{ex.Message}", "خطأ", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                Log.CloseAndFlush();
            }
        }

        /// <summary>
        /// إعداد نظام السجلات - Setup Logging System
        /// </summary>
        private static void SetupLogging()
        {
            Log.Logger = new LoggerConfiguration()
                .MinimumLevel.Information()
                .WriteTo.File(
                    path: "Logs/SalesManagement-.txt",
                    rollingInterval: RollingInterval.Day,
                    retainedFileCountLimit: 30,
                    encoding: System.Text.Encoding.UTF8)
                .CreateLogger();
        }

        /// <summary>
        /// إعداد الثقافة العربية - Setup Arabic Culture
        /// </summary>
        private static void SetupCulture()
        {
            var culture = ConfigurationManager.GetCulture();
            Thread.CurrentThread.CurrentCulture = culture;
            Thread.CurrentThread.CurrentUICulture = culture;
            CultureInfo.DefaultThreadCurrentCulture = culture;
            CultureInfo.DefaultThreadCurrentUICulture = culture;

            // إعداد RTL للتطبيق - Setup RTL for Application
            if (ConfigurationManager.IsRTLSupported())
            {
                Application.CurrentCulture = culture;
            }
        }

        /// <summary>
        /// إعداد واجهة المستخدم - Setup UI
        /// </summary>
        private static void SetupUI()
        {
            // إعداد Windows Forms - Setup Windows Forms
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            // إعداد DevExpress Theme - Setup DevExpress Theme
            DevExpress.LookAndFeel.UserLookAndFeel.Default.SetSkinStyle("WXI");

            // دعم RTL - RTL Support
            if (ConfigurationManager.IsRTLSupported())
            {
                Application.CurrentCulture = ConfigurationManager.GetCulture();
            }
        }

        /// <summary>
        /// تهيئة قاعدة البيانات - Initialize Database
        /// </summary>
        private static void InitializeDatabase()
        {
            try
            {
                using var context = new SalesDbContext();
                DatabaseInitializer.InitializeAsync(context).Wait();
                Log.Information("تم تهيئة قاعدة البيانات بنجاح - Database initialized successfully");
            }
            catch (Exception ex)
            {
                Log.Error(ex, "خطأ في تهيئة قاعدة البيانات - Error initializing database");
                MessageBox.Show($"خطأ في تهيئة قاعدة البيانات:\n{ex.Message}", "خطأ", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}
