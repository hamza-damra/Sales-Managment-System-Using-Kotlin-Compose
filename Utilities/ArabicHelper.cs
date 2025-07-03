using System.Globalization;
using System.Text;

namespace DXApplication1.Utilities
{
    /// <summary>
    /// مساعد اللغة العربية - Arabic Helper
    /// </summary>
    public static class ArabicHelper
    {
        private static readonly CultureInfo ArabicCulture = new CultureInfo("ar-SA");

        /// <summary>
        /// تحويل الأرقام الإنجليزية إلى عربية - Convert English Numbers to Arabic
        /// </summary>
        /// <param name="input">النص المدخل</param>
        /// <returns>النص مع الأرقام العربية</returns>
        public static string ConvertToArabicNumbers(string input)
        {
            if (string.IsNullOrEmpty(input))
                return input;

            var arabicNumbers = new string[] { "٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩" };
            var result = new StringBuilder(input);

            for (int i = 0; i < 10; i++)
            {
                result.Replace(i.ToString(), arabicNumbers[i]);
            }

            return result.ToString();
        }

        /// <summary>
        /// تحويل الأرقام العربية إلى إنجليزية - Convert Arabic Numbers to English
        /// </summary>
        /// <param name="input">النص المدخل</param>
        /// <returns>النص مع الأرقام الإنجليزية</returns>
        public static string ConvertToEnglishNumbers(string input)
        {
            if (string.IsNullOrEmpty(input))
                return input;

            var arabicNumbers = new string[] { "٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩" };
            var result = new StringBuilder(input);

            for (int i = 0; i < 10; i++)
            {
                result.Replace(arabicNumbers[i], i.ToString());
            }

            return result.ToString();
        }

        /// <summary>
        /// تنسيق المبلغ بالعملة العربية - Format Currency in Arabic
        /// </summary>
        /// <param name="amount">المبلغ</param>
        /// <param name="currencySymbol">رمز العملة</param>
        /// <returns>المبلغ منسق</returns>
        public static string FormatCurrency(decimal amount, string currencySymbol = "ر.س")
        {
            var formatted = amount.ToString("N2", ArabicCulture);
            return $"{formatted} {currencySymbol}";
        }

        /// <summary>
        /// تنسيق التاريخ بالعربية - Format Date in Arabic
        /// </summary>
        /// <param name="date">التاريخ</param>
        /// <param name="format">تنسيق التاريخ</param>
        /// <returns>التاريخ منسق</returns>
        public static string FormatDate(DateTime date, string format = "dd/MM/yyyy")
        {
            return date.ToString(format, ArabicCulture);
        }

        /// <summary>
        /// تنسيق التاريخ والوقت بالعربية - Format DateTime in Arabic
        /// </summary>
        /// <param name="dateTime">التاريخ والوقت</param>
        /// <param name="format">تنسيق التاريخ والوقت</param>
        /// <returns>التاريخ والوقت منسق</returns>
        public static string FormatDateTime(DateTime dateTime, string format = "dd/MM/yyyy hh:mm tt")
        {
            return dateTime.ToString(format, ArabicCulture);
        }

        /// <summary>
        /// تحويل الرقم إلى كلمات عربية - Convert Number to Arabic Words
        /// </summary>
        /// <param name="number">الرقم</param>
        /// <returns>الرقم بالكلمات</returns>
        public static string NumberToArabicWords(decimal number)
        {
            if (number == 0)
                return "صفر";

            var ones = new string[] { "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة" };
            var tens = new string[] { "", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون" };
            var teens = new string[] { "عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر", "سبعة عشر", "ثمانية عشر", "تسعة عشر" };

            var integerPart = (long)Math.Floor(number);
            var decimalPart = (int)((number - integerPart) * 100);

            var result = ConvertIntegerToWords(integerPart, ones, tens, teens);

            if (decimalPart > 0)
            {
                result += " و " + ConvertIntegerToWords(decimalPart, ones, tens, teens) + " قرش";
            }

            return result.Trim();
        }

        private static string ConvertIntegerToWords(long number, string[] ones, string[] tens, string[] teens)
        {
            if (number == 0)
                return "";

            if (number < 10)
                return ones[number];

            if (number < 20)
                return teens[number - 10];

            if (number < 100)
            {
                var tensDigit = (int)(number / 10);
                var onesDigit = (int)(number % 10);
                return tens[tensDigit] + (onesDigit > 0 ? " " + ones[onesDigit] : "");
            }

            if (number < 1000)
            {
                var hundreds = (int)(number / 100);
                var remainder = number % 100;
                var result = ones[hundreds] + " مائة";
                if (remainder > 0)
                    result += " " + ConvertIntegerToWords(remainder, ones, tens, teens);
                return result;
            }

            // For larger numbers, you would continue the pattern
            return number.ToString();
        }

        /// <summary>
        /// التحقق من صحة النص العربي - Validate Arabic Text
        /// </summary>
        /// <param name="text">النص</param>
        /// <returns>صحيح إذا كان النص يحتوي على أحرف عربية</returns>
        public static bool IsArabicText(string text)
        {
            if (string.IsNullOrEmpty(text))
                return false;

            return text.Any(c => c >= 0x0600 && c <= 0x06FF);
        }

        /// <summary>
        /// تنظيف النص العربي - Clean Arabic Text
        /// </summary>
        /// <param name="text">النص</param>
        /// <returns>النص منظف</returns>
        public static string CleanArabicText(string text)
        {
            if (string.IsNullOrEmpty(text))
                return text;

            // Remove extra spaces and trim
            text = System.Text.RegularExpressions.Regex.Replace(text, @"\s+", " ").Trim();

            return text;
        }
    }
}
