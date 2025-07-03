using DXApplication1.Models;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// واجهة وحدة العمل - Unit of Work Interface
    /// </summary>
    public interface IUnitOfWork : IDisposable
    {
        // المستودعات - Repositories
        IRepository<User> Users { get; }
        IRepository<Role> Roles { get; }
        IRepository<Customer> Customers { get; }
        IRepository<Category> Categories { get; }
        IRepository<Product> Products { get; }
        IRepository<Invoice> Invoices { get; }
        IRepository<InvoiceItem> InvoiceItems { get; }
        IRepository<Payment> Payments { get; }

        // العمليات - Operations
        int SaveChanges();
        Task<int> SaveChangesAsync();
        void BeginTransaction();
        void CommitTransaction();
        void RollbackTransaction();
    }
}
