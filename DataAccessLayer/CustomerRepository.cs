using Microsoft.EntityFrameworkCore;
using DXApplication1.Models;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// تنفيذ مستودع العملاء - Customer Repository Implementation
    /// </summary>
    public class CustomerRepository : Repository<Customer>, ICustomerRepository
    {
        public CustomerRepository(SalesDbContext context) : base(context)
        {
        }

        // البحث والتصفية - Search and Filter
        public async Task<IEnumerable<Customer>> SearchCustomersAsync(string searchTerm)
        {
            if (string.IsNullOrWhiteSpace(searchTerm))
                return await GetActiveCustomersAsync();

            searchTerm = searchTerm.Trim().ToLower();

            return await _dbSet
                .Where(c => !c.IsDeleted && c.IsActive &&
                           (c.CustomerName.ToLower().Contains(searchTerm) ||
                            (c.CompanyName != null && c.CompanyName.ToLower().Contains(searchTerm)) ||
                            (c.Phone != null && c.Phone.Contains(searchTerm)) ||
                            (c.Mobile != null && c.Mobile.Contains(searchTerm)) ||
                            (c.Email != null && c.Email.ToLower().Contains(searchTerm))))
                .OrderBy(c => c.CustomerName)
                .ToListAsync();
        }

        public async Task<IEnumerable<Customer>> GetCustomersByTypeAsync(CustomerType customerType)
        {
            return await _dbSet
                .Where(c => c.CustomerType == customerType && c.IsActive && !c.IsDeleted)
                .OrderBy(c => c.CustomerName)
                .ToListAsync();
        }

        public async Task<IEnumerable<Customer>> GetActiveCustomersAsync()
        {
            return await _dbSet
                .Where(c => c.IsActive && !c.IsDeleted)
                .OrderBy(c => c.CustomerName)
                .ToListAsync();
        }

        // إدارة الرصيد - Balance Management
        public async Task<bool> UpdateCustomerBalanceAsync(int customerId, decimal amount)
        {
            var customer = await GetByIdAsync(customerId);
            if (customer == null)
                return false;

            customer.CurrentBalance = amount;
            customer.ModifiedDate = DateTime.Now;
            
            Update(customer);
            return true;
        }

        public async Task<bool> AddToCustomerBalanceAsync(int customerId, decimal amount)
        {
            var customer = await GetByIdAsync(customerId);
            if (customer == null)
                return false;

            customer.CurrentBalance += amount;
            customer.ModifiedDate = DateTime.Now;
            
            Update(customer);
            return true;
        }

        public async Task<bool> SubtractFromCustomerBalanceAsync(int customerId, decimal amount)
        {
            var customer = await GetByIdAsync(customerId);
            if (customer == null)
                return false;

            customer.CurrentBalance -= amount;
            customer.ModifiedDate = DateTime.Now;
            
            Update(customer);
            return true;
        }

        public async Task<IEnumerable<Customer>> GetCustomersWithBalanceAsync()
        {
            return await _dbSet
                .Where(c => c.CurrentBalance != 0 && c.IsActive && !c.IsDeleted)
                .OrderByDescending(c => c.CurrentBalance)
                .ToListAsync();
        }

        public async Task<IEnumerable<Customer>> GetCustomersExceedingCreditLimitAsync()
        {
            return await _dbSet
                .Where(c => c.CurrentBalance > c.CreditLimit && c.CreditLimit > 0 && c.IsActive && !c.IsDeleted)
                .OrderByDescending(c => c.CurrentBalance - c.CreditLimit)
                .ToListAsync();
        }

        // التقارير والإحصائيات - Reports and Statistics
        public async Task<decimal> GetTotalCustomerBalancesAsync()
        {
            return await _dbSet
                .Where(c => c.IsActive && !c.IsDeleted)
                .SumAsync(c => c.CurrentBalance);
        }

        public async Task<int> GetCustomerCountByTypeAsync(CustomerType customerType)
        {
            return await _dbSet
                .CountAsync(c => c.CustomerType == customerType && c.IsActive && !c.IsDeleted);
        }

        public async Task<IEnumerable<Customer>> GetTopCustomersByBalanceAsync(int count = 10)
        {
            return await _dbSet
                .Where(c => c.IsActive && !c.IsDeleted)
                .OrderByDescending(c => c.CurrentBalance)
                .Take(count)
                .ToListAsync();
        }

        // معلومات الاتصال - Contact Information
        public async Task<bool> IsEmailExistsAsync(string email, int? excludeCustomerId = null)
        {
            if (string.IsNullOrWhiteSpace(email))
                return false;

            var query = _dbSet.Where(c => c.Email == email && !c.IsDeleted);
            
            if (excludeCustomerId.HasValue)
                query = query.Where(c => c.Id != excludeCustomerId.Value);

            return await query.AnyAsync();
        }

        public async Task<bool> IsPhoneExistsAsync(string phone, int? excludeCustomerId = null)
        {
            if (string.IsNullOrWhiteSpace(phone))
                return false;

            var query = _dbSet.Where(c => (c.Phone == phone || c.Mobile == phone) && !c.IsDeleted);
            
            if (excludeCustomerId.HasValue)
                query = query.Where(c => c.Id != excludeCustomerId.Value);

            return await query.AnyAsync();
        }

        public async Task<Customer?> GetCustomerByEmailAsync(string email)
        {
            if (string.IsNullOrWhiteSpace(email))
                return null;

            return await _dbSet
                .FirstOrDefaultAsync(c => c.Email == email && c.IsActive && !c.IsDeleted);
        }

        public async Task<Customer?> GetCustomerByPhoneAsync(string phone)
        {
            if (string.IsNullOrWhiteSpace(phone))
                return null;

            return await _dbSet
                .FirstOrDefaultAsync(c => (c.Phone == phone || c.Mobile == phone) && c.IsActive && !c.IsDeleted);
        }

        // تاريخ المعاملات - Transaction History
        public async Task<IEnumerable<Invoice>> GetCustomerInvoicesAsync(int customerId)
        {
            return await _context.Invoices
                .Include(i => i.User)
                .Include(i => i.InvoiceItems)
                    .ThenInclude(ii => ii.Product)
                .Where(i => i.CustomerId == customerId && !i.IsDeleted)
                .OrderByDescending(i => i.InvoiceDate)
                .ToListAsync();
        }

        public async Task<IEnumerable<Invoice>> GetCustomerInvoicesAsync(int customerId, DateTime fromDate, DateTime toDate)
        {
            return await _context.Invoices
                .Include(i => i.User)
                .Include(i => i.InvoiceItems)
                    .ThenInclude(ii => ii.Product)
                .Where(i => i.CustomerId == customerId && 
                           i.InvoiceDate >= fromDate && 
                           i.InvoiceDate <= toDate && 
                           !i.IsDeleted)
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
                .Where(i => i.CustomerId == customerId && 
                           i.InvoiceDate >= fromDate && 
                           i.InvoiceDate <= toDate && 
                           !i.IsDeleted)
                .SumAsync(i => i.TotalAmount);
        }

        // تجاوز العمليات الأساسية - Override base operations
        public override async Task<IEnumerable<Customer>> GetAllAsync()
        {
            return await _dbSet
                .Where(c => !c.IsDeleted)
                .OrderBy(c => c.CustomerName)
                .ToListAsync();
        }
    }
}
