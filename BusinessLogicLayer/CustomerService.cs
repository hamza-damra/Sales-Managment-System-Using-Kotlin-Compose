using DXApplication1.Models;
using DXApplication1.DataAccessLayer;
using Microsoft.EntityFrameworkCore;
using System.Text.RegularExpressions;

namespace DXApplication1.BusinessLogicLayer
{
    /// <summary>
    /// تنفيذ خدمة العملاء - Customer Service Implementation
    /// </summary>
    public class CustomerService : ICustomerService, IDisposable
    {
        private readonly IUnitOfWork? _unitOfWork;
        private readonly ICustomerRepository? _customerRepository;
        private readonly SalesDbContext? _context;

        // Constructor for dependency injection (existing)
        public CustomerService(IUnitOfWork unitOfWork, SalesDbContext context)
        {
            _unitOfWork = unitOfWork ?? throw new ArgumentNullException(nameof(unitOfWork));
            _context = context ?? throw new ArgumentNullException(nameof(context));
            _customerRepository = new CustomerRepository(context);
        }

        // Parameterless constructor for thread-safe operations
        public CustomerService()
        {
            // Services will create their own DbContext instances per operation
        }

        // إدارة العملاء - Customer Management
        public async Task<IEnumerable<Customer>> GetAllCustomersAsync()
        {
            if (_context != null)
            {
                return await _context.Customers
                    .Where(c => !c.IsDeleted)
                    .OrderBy(c => c.CustomerName)
                    .ToListAsync();
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            return await context.Customers
                .Where(c => !c.IsDeleted)
                .OrderBy(c => c.CustomerName)
                .ToListAsync();
        }

        public async Task<Customer?> GetCustomerByIdAsync(int id)
        {
            return await _context.Customers
                .FirstOrDefaultAsync(c => c.Id == id && !c.IsDeleted);
        }

        public async Task<IEnumerable<Customer>> SearchCustomersAsync(string searchTerm)
        {
            if (string.IsNullOrWhiteSpace(searchTerm))
                return await GetAllCustomersAsync();

            searchTerm = searchTerm.Trim().ToLower();

            if (_context != null)
            {
                return await _context.Customers
                    .Where(c => !c.IsDeleted &&
                        (c.CustomerName.ToLower().Contains(searchTerm) ||
                         (c.CompanyName != null && c.CompanyName.ToLower().Contains(searchTerm)) ||
                         (c.Phone != null && c.Phone.Contains(searchTerm)) ||
                         (c.Mobile != null && c.Mobile.Contains(searchTerm)) ||
                         (c.Email != null && c.Email.ToLower().Contains(searchTerm))))
                    .OrderBy(c => c.CustomerName)
                    .ToListAsync();
            }

            // Create new context for thread-safe operation
            using var context = new SalesDbContext();
            return await context.Customers
                .Where(c => !c.IsDeleted &&
                    (c.CustomerName.ToLower().Contains(searchTerm) ||
                     (c.CompanyName != null && c.CompanyName.ToLower().Contains(searchTerm)) ||
                     (c.Phone != null && c.Phone.Contains(searchTerm)) ||
                     (c.Mobile != null && c.Mobile.Contains(searchTerm)) ||
                     (c.Email != null && c.Email.ToLower().Contains(searchTerm))))
                .OrderBy(c => c.CustomerName)
                .ToListAsync();
        }

        public async Task<bool> CreateCustomerAsync(Customer customer)
        {
            try
            {
                if (customer == null)
                    return false;

                // التحقق من صحة البيانات - Validate data
                if (!ValidateCustomerData(customer, out List<string> errors))
                    throw new ArgumentException($"بيانات العميل غير صحيحة: {string.Join(", ", errors)}");

                // التحقق من عدم تكرار البريد الإلكتروني - Check email uniqueness
                if (!string.IsNullOrWhiteSpace(customer.Email) && 
                    !await IsEmailAvailableAsync(customer.Email))
                    throw new ArgumentException("البريد الإلكتروني مستخدم من قبل");

                // التحقق من عدم تكرار رقم الهاتف - Check phone uniqueness
                if (!string.IsNullOrWhiteSpace(customer.Phone) && 
                    !await IsPhoneAvailableAsync(customer.Phone))
                    throw new ArgumentException("رقم الهاتف مستخدم من قبل");

                customer.CreatedDate = DateTime.Now;
                customer.CreatedBy = "System"; // يجب تمرير المستخدم الحالي
                customer.IsActive = true;
                customer.IsDeleted = false;

                await _unitOfWork.Customers.AddAsync(customer);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إنشاء العميل", ex);
            }
        }

        public async Task<bool> UpdateCustomerAsync(Customer customer)
        {
            try
            {
                if (customer == null)
                    return false;

                var existingCustomer = await GetCustomerByIdAsync(customer.Id);
                if (existingCustomer == null)
                    return false;

                // التحقق من صحة البيانات - Validate data
                if (!ValidateCustomerData(customer, out List<string> errors))
                    throw new ArgumentException($"بيانات العميل غير صحيحة: {string.Join(", ", errors)}");

                // التحقق من عدم تكرار البريد الإلكتروني - Check email uniqueness
                if (!string.IsNullOrWhiteSpace(customer.Email) && 
                    !await IsEmailAvailableAsync(customer.Email, customer.Id))
                    throw new ArgumentException("البريد الإلكتروني مستخدم من قبل");

                // التحقق من عدم تكرار رقم الهاتف - Check phone uniqueness
                if (!string.IsNullOrWhiteSpace(customer.Phone) && 
                    !await IsPhoneAvailableAsync(customer.Phone, customer.Id))
                    throw new ArgumentException("رقم الهاتف مستخدم من قبل");

                // تحديث البيانات - Update data
                existingCustomer.CustomerName = customer.CustomerName;
                existingCustomer.CompanyName = customer.CompanyName;
                existingCustomer.Phone = customer.Phone;
                existingCustomer.Mobile = customer.Mobile;
                existingCustomer.Email = customer.Email;
                existingCustomer.Address = customer.Address;
                existingCustomer.City = customer.City;
                existingCustomer.Country = customer.Country;
                existingCustomer.PostalCode = customer.PostalCode;
                existingCustomer.TaxNumber = customer.TaxNumber;
                existingCustomer.CreditLimit = customer.CreditLimit;
                existingCustomer.CustomerType = customer.CustomerType;
                existingCustomer.Notes = customer.Notes;
                existingCustomer.ModifiedDate = DateTime.Now;
                existingCustomer.ModifiedBy = "System"; // يجب تمرير المستخدم الحالي

                _unitOfWork.Customers.Update(existingCustomer);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تحديث العميل", ex);
            }
        }

        public async Task<bool> DeleteCustomerAsync(int id)
        {
            try
            {
                var customer = await GetCustomerByIdAsync(id);
                if (customer == null)
                    return false;

                // التحقق من وجود فواتير مرتبطة - Check for related invoices
                var hasInvoices = await _context.Invoices
                    .AnyAsync(i => i.CustomerId == id && !i.IsDeleted);

                if (hasInvoices)
                    throw new InvalidOperationException("لا يمكن حذف العميل لوجود فواتير مرتبطة به");

                // حذف منطقي - Soft delete
                customer.IsDeleted = true;
                customer.ModifiedDate = DateTime.Now;
                customer.ModifiedBy = "System";

                _unitOfWork.Customers.Update(customer);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في حذف العميل", ex);
            }
        }

        public async Task<bool> ActivateCustomerAsync(int id)
        {
            try
            {
                var customer = await GetCustomerByIdAsync(id);
                if (customer == null)
                    return false;

                customer.IsActive = true;
                customer.ModifiedDate = DateTime.Now;
                customer.ModifiedBy = "System";

                _unitOfWork.Customers.Update(customer);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تفعيل العميل", ex);
            }
        }

        public async Task<bool> DeactivateCustomerAsync(int id)
        {
            try
            {
                var customer = await GetCustomerByIdAsync(id);
                if (customer == null)
                    return false;

                customer.IsActive = false;
                customer.ModifiedDate = DateTime.Now;
                customer.ModifiedBy = "System";

                _unitOfWork.Customers.Update(customer);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إلغاء تفعيل العميل", ex);
            }
        }

        // إدارة الرصيد - Balance Management
        public async Task<bool> UpdateCustomerBalanceAsync(int customerId, decimal newBalance)
        {
            try
            {
                var customer = await GetCustomerByIdAsync(customerId);
                if (customer == null)
                    return false;

                customer.CurrentBalance = newBalance;
                customer.ModifiedDate = DateTime.Now;
                customer.ModifiedBy = "System";

                _unitOfWork.Customers.Update(customer);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تحديث رصيد العميل", ex);
            }
        }

        public async Task<bool> AddPaymentAsync(int customerId, decimal amount, string notes = "")
        {
            try
            {
                var customer = await GetCustomerByIdAsync(customerId);
                if (customer == null)
                    return false;

                customer.CurrentBalance -= amount; // تقليل الرصيد (دفع)
                customer.ModifiedDate = DateTime.Now;
                customer.ModifiedBy = "System";

                _unitOfWork.Customers.Update(customer);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إضافة دفعة", ex);
            }
        }

        public async Task<bool> AddChargeAsync(int customerId, decimal amount, string notes = "")
        {
            try
            {
                var customer = await GetCustomerByIdAsync(customerId);
                if (customer == null)
                    return false;

                customer.CurrentBalance += amount; // زيادة الرصيد (رسوم)
                customer.ModifiedDate = DateTime.Now;
                customer.ModifiedBy = "System";

                _unitOfWork.Customers.Update(customer);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إضافة رسوم", ex);
            }
        }

        public async Task<IEnumerable<Customer>> GetCustomersWithBalanceAsync()
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted && c.CurrentBalance != 0)
                .OrderByDescending(c => c.CurrentBalance)
                .ToListAsync();
        }

        public async Task<IEnumerable<Customer>> GetCustomersExceedingCreditLimitAsync()
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted && c.CurrentBalance > c.CreditLimit && c.CreditLimit > 0)
                .OrderByDescending(c => c.CurrentBalance - c.CreditLimit)
                .ToListAsync();
        }

        // التحقق من صحة البيانات - Data Validation
        public async Task<bool> IsEmailAvailableAsync(string email, int? excludeCustomerId = null)
        {
            if (string.IsNullOrWhiteSpace(email))
                return true;

            var query = _context.Customers.Where(c => !c.IsDeleted && c.Email == email);
            
            if (excludeCustomerId.HasValue)
                query = query.Where(c => c.Id != excludeCustomerId.Value);

            return !await query.AnyAsync();
        }

        public async Task<bool> IsPhoneAvailableAsync(string phone, int? excludeCustomerId = null)
        {
            if (string.IsNullOrWhiteSpace(phone))
                return true;

            var query = _context.Customers.Where(c => !c.IsDeleted && (c.Phone == phone || c.Mobile == phone));
            
            if (excludeCustomerId.HasValue)
                query = query.Where(c => c.Id != excludeCustomerId.Value);

            return !await query.AnyAsync();
        }

        public bool ValidateCustomerData(Customer customer, out List<string> errors)
        {
            errors = new List<string>();

            if (customer == null)
            {
                errors.Add("بيانات العميل مطلوبة");
                return false;
            }

            if (string.IsNullOrWhiteSpace(customer.CustomerName))
                errors.Add("اسم العميل مطلوب");

            if (customer.CustomerName?.Length > 200)
                errors.Add("اسم العميل يجب أن يكون أقل من 200 حرف");

            if (!string.IsNullOrWhiteSpace(customer.Email) && !IsValidEmail(customer.Email))
                errors.Add("البريد الإلكتروني غير صحيح");

            if (customer.CreditLimit < 0)
                errors.Add("حد الائتمان لا يمكن أن يكون سالباً");

            return errors.Count == 0;
        }

        private bool IsValidEmail(string email)
        {
            return Regex.IsMatch(email, @"^[^@\s]+@[^@\s]+\.[^@\s]+$");
        }

        // التقارير والإحصائيات - Reports and Statistics
        public async Task<int> GetTotalCustomersCountAsync()
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted)
                .CountAsync();
        }

        public async Task<int> GetActiveCustomersCountAsync()
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted && c.IsActive)
                .CountAsync();
        }

        public async Task<int> GetCustomersCountByTypeAsync(CustomerType customerType)
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted && c.CustomerType == customerType)
                .CountAsync();
        }

        public async Task<decimal> GetTotalCustomerBalancesAsync()
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted)
                .SumAsync(c => c.CurrentBalance);
        }

        public async Task<IEnumerable<Customer>> GetTopCustomersByBalanceAsync(int count = 10)
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted)
                .OrderByDescending(c => c.CurrentBalance)
                .Take(count)
                .ToListAsync();
        }

        public async Task<IEnumerable<Customer>> GetTopCustomersByPurchasesAsync(int count = 10)
        {
            return await _context.Customers
                .Where(c => !c.IsDeleted)
                .Include(c => c.Invoices)
                .OrderByDescending(c => c.Invoices.Where(i => !i.IsDeleted).Sum(i => i.TotalAmount))
                .Take(count)
                .ToListAsync();
        }

        // تاريخ المعاملات - Transaction History
        public async Task<IEnumerable<Invoice>> GetCustomerInvoicesAsync(int customerId)
        {
            return await _context.Invoices
                .Where(i => i.CustomerId == customerId && !i.IsDeleted)
                .Include(i => i.Customer)
                .Include(i => i.User)
                .Include(i => i.InvoiceItems)
                .ThenInclude(ii => ii.Product)
                .OrderByDescending(i => i.InvoiceDate)
                .ToListAsync();
        }

        public async Task<IEnumerable<Invoice>> GetCustomerInvoicesAsync(int customerId, DateTime fromDate, DateTime toDate)
        {
            return await _context.Invoices
                .Where(i => i.CustomerId == customerId && !i.IsDeleted &&
                           i.InvoiceDate >= fromDate && i.InvoiceDate <= toDate)
                .Include(i => i.Customer)
                .Include(i => i.User)
                .Include(i => i.InvoiceItems)
                .ThenInclude(ii => ii.Product)
                .OrderByDescending(i => i.InvoiceDate)
                .ToListAsync();
        }

        public async Task<decimal> GetCustomerTotalPurchasesAsync(int customerId)
        {
            return await _context.Invoices
                .Where(i => i.CustomerId == customerId && !i.IsDeleted)
                .SumAsync(i => i.TotalAmount);
        }

        public async Task<decimal> GetCustomerTotalPurchasesAsync(int customerId, DateTime fromDate, DateTime toDate)
        {
            return await _context.Invoices
                .Where(i => i.CustomerId == customerId && !i.IsDeleted &&
                           i.InvoiceDate >= fromDate && i.InvoiceDate <= toDate)
                .SumAsync(i => i.TotalAmount);
        }

        // تنظيف الموارد - Dispose Resources
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (disposing)
            {
                _unitOfWork?.Dispose();
                if (_context != null)
                {
                    _context.Dispose();
                }
            }
        }
    }
}
