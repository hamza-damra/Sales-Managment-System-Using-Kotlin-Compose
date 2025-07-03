using DXApplication1.Models;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// واجهة مستودع العملاء - Customer Repository Interface
    /// </summary>
    public interface ICustomerRepository : IRepository<Customer>
    {
        // البحث والتصفية - Search and Filter
        Task<IEnumerable<Customer>> SearchCustomersAsync(string searchTerm);
        Task<IEnumerable<Customer>> GetCustomersByTypeAsync(CustomerType customerType);
        Task<IEnumerable<Customer>> GetActiveCustomersAsync();

        // إدارة الرصيد - Balance Management
        Task<bool> UpdateCustomerBalanceAsync(int customerId, decimal amount);
        Task<bool> AddToCustomerBalanceAsync(int customerId, decimal amount);
        Task<bool> SubtractFromCustomerBalanceAsync(int customerId, decimal amount);
        Task<IEnumerable<Customer>> GetCustomersWithBalanceAsync();
        Task<IEnumerable<Customer>> GetCustomersExceedingCreditLimitAsync();

        // التقارير والإحصائيات - Reports and Statistics
        Task<decimal> GetTotalCustomerBalancesAsync();
        Task<int> GetCustomerCountByTypeAsync(CustomerType customerType);
        Task<IEnumerable<Customer>> GetTopCustomersByBalanceAsync(int count = 10);

        // معلومات الاتصال - Contact Information
        Task<bool> IsEmailExistsAsync(string email, int? excludeCustomerId = null);
        Task<bool> IsPhoneExistsAsync(string phone, int? excludeCustomerId = null);
        Task<Customer?> GetCustomerByEmailAsync(string email);
        Task<Customer?> GetCustomerByPhoneAsync(string phone);

        // تاريخ المعاملات - Transaction History
        Task<IEnumerable<Invoice>> GetCustomerInvoicesAsync(int customerId);
        Task<IEnumerable<Invoice>> GetCustomerInvoicesAsync(int customerId, DateTime fromDate, DateTime toDate);
        Task<decimal> GetCustomerTotalPurchasesAsync(int customerId);
        Task<decimal> GetCustomerTotalPurchasesAsync(int customerId, DateTime fromDate, DateTime toDate);
    }
}
