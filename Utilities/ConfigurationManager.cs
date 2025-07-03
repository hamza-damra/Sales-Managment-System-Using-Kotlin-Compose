using Microsoft.Extensions.Configuration;
using System.Globalization;

namespace DXApplication1.Utilities
{
    /// <summary>
    /// مدير الإعدادات - Configuration Manager
    /// </summary>
    public static class ConfigurationManager
    {
        private static IConfiguration? _configuration;

        public static IConfiguration Configuration
        {
            get
            {
                if (_configuration == null)
                {
                    var builder = new ConfigurationBuilder()
                        .SetBasePath(AppDomain.CurrentDomain.BaseDirectory)
                        .AddJsonFile("appsettings.json", optional: false, reloadOnChange: true);

                    _configuration = builder.Build();
                }
                return _configuration;
            }
        }

        public static string GetConnectionString(string name = "DefaultConnection")
        {
            return Configuration.GetConnectionString(name) ?? throw new InvalidOperationException($"Connection string '{name}' not found.");
        }

        public static string GetApplicationName()
        {
            return Configuration["ApplicationSettings:ApplicationName"] ?? "نظام إدارة المبيعات";
        }

        public static string GetVersion()
        {
            return Configuration["ApplicationSettings:Version"] ?? "1.0.0";
        }

        public static bool IsRTLSupported()
        {
            return bool.Parse(Configuration["ApplicationSettings:RTLSupport"] ?? "true");
        }

        public static CultureInfo GetCulture()
        {
            var cultureName = Configuration["ApplicationSettings:Culture"] ?? "ar-SA";
            return new CultureInfo(cultureName);
        }

        public static int GetPasswordMinLength()
        {
            return int.Parse(Configuration["SecuritySettings:PasswordMinLength"] ?? "8");
        }

        public static int GetSessionTimeoutMinutes()
        {
            return int.Parse(Configuration["SecuritySettings:SessionTimeoutMinutes"] ?? "30");
        }

        public static int GetMaxLoginAttempts()
        {
            return int.Parse(Configuration["SecuritySettings:MaxLoginAttempts"] ?? "3");
        }

        public static int GetLockoutDurationMinutes()
        {
            return int.Parse(Configuration["SecuritySettings:LockoutDurationMinutes"] ?? "15");
        }
    }
}
