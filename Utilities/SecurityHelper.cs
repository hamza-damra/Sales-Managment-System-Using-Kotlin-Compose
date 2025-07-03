using BCrypt.Net;
using System.Security.Cryptography;
using System.Text;

namespace DXApplication1.Utilities
{
    /// <summary>
    /// مساعد الأمان - Security Helper
    /// </summary>
    public static class SecurityHelper
    {
        /// <summary>
        /// تشفير كلمة المرور - Hash Password
        /// </summary>
        /// <param name="password">كلمة المرور</param>
        /// <returns>كلمة المرور المشفرة</returns>
        public static string HashPassword(string password)
        {
            if (string.IsNullOrEmpty(password))
                throw new ArgumentException("Password cannot be null or empty", nameof(password));

            return BCrypt.Net.BCrypt.HashPassword(password, BCrypt.Net.BCrypt.GenerateSalt(12));
        }

        /// <summary>
        /// التحقق من كلمة المرور - Verify Password
        /// </summary>
        /// <param name="password">كلمة المرور</param>
        /// <param name="hashedPassword">كلمة المرور المشفرة</param>
        /// <returns>صحيح إذا كانت كلمة المرور صحيحة</returns>
        public static bool VerifyPassword(string password, string hashedPassword)
        {
            if (string.IsNullOrEmpty(password) || string.IsNullOrEmpty(hashedPassword))
                return false;

            try
            {
                return BCrypt.Net.BCrypt.Verify(password, hashedPassword);
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// توليد رقم فاتورة فريد - Generate Unique Invoice Number
        /// </summary>
        /// <returns>رقم الفاتورة</returns>
        public static string GenerateInvoiceNumber()
        {
            var timestamp = DateTime.Now.ToString("yyyyMMdd");
            var random = new Random().Next(1000, 9999);
            return $"INV-{timestamp}-{random}";
        }

        /// <summary>
        /// توليد كود منتج فريد - Generate Unique Product Code
        /// </summary>
        /// <param name="categoryCode">كود الفئة</param>
        /// <returns>كود المنتج</returns>
        public static string GenerateProductCode(string? categoryCode = null)
        {
            var prefix = string.IsNullOrEmpty(categoryCode) ? "PRD" : categoryCode;
            var timestamp = DateTime.Now.ToString("yyyyMMdd");
            var random = new Random().Next(100, 999);
            return $"{prefix}-{timestamp}-{random}";
        }

        /// <summary>
        /// تشفير النص - Encrypt Text
        /// </summary>
        /// <param name="plainText">النص الأصلي</param>
        /// <param name="key">مفتاح التشفير</param>
        /// <returns>النص المشفر</returns>
        public static string EncryptText(string plainText, string key)
        {
            if (string.IsNullOrEmpty(plainText) || string.IsNullOrEmpty(key))
                return plainText;

            byte[] plainTextBytes = Encoding.UTF8.GetBytes(plainText);
            byte[] keyBytes = Encoding.UTF8.GetBytes(key);

            using (var aes = Aes.Create())
            {
                aes.Key = ResizeKey(keyBytes, aes.KeySize / 8);
                aes.IV = new byte[aes.BlockSize / 8];

                using (var encryptor = aes.CreateEncryptor())
                {
                    byte[] encryptedBytes = encryptor.TransformFinalBlock(plainTextBytes, 0, plainTextBytes.Length);
                    return Convert.ToBase64String(encryptedBytes);
                }
            }
        }

        /// <summary>
        /// فك تشفير النص - Decrypt Text
        /// </summary>
        /// <param name="encryptedText">النص المشفر</param>
        /// <param name="key">مفتاح التشفير</param>
        /// <returns>النص الأصلي</returns>
        public static string DecryptText(string encryptedText, string key)
        {
            if (string.IsNullOrEmpty(encryptedText) || string.IsNullOrEmpty(key))
                return encryptedText;

            try
            {
                byte[] encryptedBytes = Convert.FromBase64String(encryptedText);
                byte[] keyBytes = Encoding.UTF8.GetBytes(key);

                using (var aes = Aes.Create())
                {
                    aes.Key = ResizeKey(keyBytes, aes.KeySize / 8);
                    aes.IV = new byte[aes.BlockSize / 8];

                    using (var decryptor = aes.CreateDecryptor())
                    {
                        byte[] decryptedBytes = decryptor.TransformFinalBlock(encryptedBytes, 0, encryptedBytes.Length);
                        return Encoding.UTF8.GetString(decryptedBytes);
                    }
                }
            }
            catch
            {
                return encryptedText;
            }
        }

        private static byte[] ResizeKey(byte[] key, int size)
        {
            byte[] resizedKey = new byte[size];
            if (key.Length >= size)
            {
                Array.Copy(key, resizedKey, size);
            }
            else
            {
                Array.Copy(key, resizedKey, key.Length);
            }
            return resizedKey;
        }
    }
}
