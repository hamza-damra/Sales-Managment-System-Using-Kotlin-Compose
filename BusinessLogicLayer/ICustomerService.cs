using DXApplication1.Models;

namespace DXApplication1.BusinessLogicLayer
{
    /// <summary>
    /// واجهة خدمة العملاء - Customer Service Interface
    /// </summary>
    public interface ICustomerService
    {
        // إدارة العملاء - Customer Management
        Task<IEnumerable<Customer>> GetAllCustomersAsync();
        Task<Customer?> GetCustomerByIdAsync(int id);
        Task<IEnumerable<Customer>> SearchCustomersAsync(string searchTerm);
        Task<bool> CreateCustomerAsync(Customer customer);
        Task<bool> UpdateCustomerAsync(Customer customer);
        Task<bool> DeleteCustomerAsync(int id);
        Task<bool> ActivateCustomerAsync(int id);
        Task<bool> DeactivateCustomerAsync(int id);

        // إدارة الرصيد - Balance Management
        Task<bool> UpdateCustomerBalanceAsync(int customerId, decimal newBalance);
        Task<bool> AddPaymentAsync(int customerId, decimal amount, string notes = "");
        Task<bool> AddChargeAsync(int customerId, decimal amount, string notes = "");
        Task<IEnumerable<Customer>> GetCustomersWithBalanceAsync();
        Task<IEnumerable<Customer>> GetCustomersExceedingCreditLimitAsync();

        // التحقق من صحة البيانات - Data Validation
        Task<bool> IsEmailAvailableAsync(string email, int? excludeCustomerId = null);
        Task<bool> IsPhoneAvailableAsync(string phone, int? excludeCustomerId = null);
        bool ValidateCustomerData(Customer customer, out List<string> errors);

        // التقارير والإحصائيات - Reports and Statistics
        Task<int> GetTotalCustomersCountAsync();
        Task<int> GetActiveCustomersCountAsync();
        Task<int> GetCustomersCountByTypeAsync(CustomerType customerType);
        Task<decimal> GetTotalCustomerBalancesAsync();
        Task<IEnumerable<Customer>> GetTopCustomersByBalanceAsync(int count = 10);
        Task<IEnumerable<Customer>> GetTopCustomersByPurchasesAsync(int count = 10);

        // تاريخ المعاملات - Transaction History
        Task<IEnumerable<Invoice>> GetCustomerInvoicesAsync(int customerId);
        Task<IEnumerable<Invoice>> GetCustomerInvoicesAsync(int customerId, DateTime fromDate, DateTime toDate);
        Task<decimal> GetCustomerTotalPurchasesAsync(int customerId);
        Task<decimal> GetCustomerTotalPurchasesAsync(int customerId, DateTime fromDate, DateTime toDate);
    }
}
