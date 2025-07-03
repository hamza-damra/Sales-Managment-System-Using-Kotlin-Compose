using Microsoft.EntityFrameworkCore.Storage;
using DXApplication1.Models;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// تنفيذ وحدة العمل - Unit of Work Implementation
    /// </summary>
    public class UnitOfWork : IUnitOfWork
    {
        private readonly SalesDbContext _context;
        private IDbContextTransaction? _transaction;

        // المستودعات - Repositories
        private IRepository<User>? _users;
        private IRepository<Role>? _roles;
        private IRepository<Customer>? _customers;
        private IRepository<Category>? _categories;
        private IRepository<Product>? _products;
        private IRepository<Invoice>? _invoices;
        private IRepository<InvoiceItem>? _invoiceItems;
        private IRepository<Payment>? _payments;

        public UnitOfWork(SalesDbContext context)
        {
            _context = context ?? throw new ArgumentNullException(nameof(context));
        }

        // خصائص المستودعات - Repository Properties
        public IRepository<User> Users => _users ??= new Repository<User>(_context);
        public IRepository<Role> Roles => _roles ??= new Repository<Role>(_context);
        public IRepository<Customer> Customers => _customers ??= new Repository<Customer>(_context);
        public IRepository<Category> Categories => _categories ??= new Repository<Category>(_context);
        public IRepository<Product> Products => _products ??= new Repository<Product>(_context);
        public IRepository<Invoice> Invoices => _invoices ??= new Repository<Invoice>(_context);
        public IRepository<InvoiceItem> InvoiceItems => _invoiceItems ??= new Repository<InvoiceItem>(_context);
        public IRepository<Payment> Payments => _payments ??= new Repository<Payment>(_context);

        // العمليات - Operations
        public int SaveChanges()
        {
            try
            {
                return _context.SaveChanges();
            }
            catch (Exception ex)
            {
                // تسجيل الخطأ - Log error
                throw new InvalidOperationException("خطأ في حفظ البيانات - Error saving data", ex);
            }
        }

        public async Task<int> SaveChangesAsync()
        {
            try
            {
                return await _context.SaveChangesAsync();
            }
            catch (Exception ex)
            {
                // تسجيل الخطأ - Log error
                throw new InvalidOperationException("خطأ في حفظ البيانات - Error saving data", ex);
            }
        }

        public void BeginTransaction()
        {
            if (_transaction != null)
            {
                throw new InvalidOperationException("المعاملة قيد التنفيذ بالفعل - Transaction already in progress");
            }

            _transaction = _context.Database.BeginTransaction();
        }

        public void CommitTransaction()
        {
            if (_transaction == null)
            {
                throw new InvalidOperationException("لا توجد معاملة نشطة للتأكيد - No active transaction to commit");
            }

            try
            {
                SaveChanges();
                _transaction.Commit();
            }
            catch
            {
                RollbackTransaction();
                throw;
            }
            finally
            {
                _transaction.Dispose();
                _transaction = null;
            }
        }

        public void RollbackTransaction()
        {
            if (_transaction == null)
            {
                throw new InvalidOperationException("لا توجد معاملة نشطة للتراجع - No active transaction to rollback");
            }

            try
            {
                _transaction.Rollback();
            }
            finally
            {
                _transaction.Dispose();
                _transaction = null;
            }
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
                _transaction?.Dispose();
                _context?.Dispose();
            }
        }
    }
}
