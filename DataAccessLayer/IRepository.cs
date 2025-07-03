using System.Linq.Expressions;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// واجهة المستودع العامة - Generic Repository Interface
    /// </summary>
    /// <typeparam name="T">نوع الكيان</typeparam>
    public interface IRepository<T> where T : class
    {
        // العمليات المتزامنة - Synchronous Operations
        T? GetById(int id);
        IEnumerable<T> GetAll();
        IEnumerable<T> Find(Expression<Func<T, bool>> predicate);
        T? FirstOrDefault(Expression<Func<T, bool>> predicate);
        void Add(T entity);
        void AddRange(IEnumerable<T> entities);
        void Update(T entity);
        void Remove(T entity);
        void RemoveRange(IEnumerable<T> entities);
        int Count();
        int Count(Expression<Func<T, bool>> predicate);
        bool Any(Expression<Func<T, bool>> predicate);

        // العمليات غير المتزامنة - Asynchronous Operations
        Task<T?> GetByIdAsync(int id);
        Task<IEnumerable<T>> GetAllAsync();
        Task<IEnumerable<T>> FindAsync(Expression<Func<T, bool>> predicate);
        Task<T?> FirstOrDefaultAsync(Expression<Func<T, bool>> predicate);
        Task AddAsync(T entity);
        Task AddRangeAsync(IEnumerable<T> entities);
        Task<int> CountAsync();
        Task<int> CountAsync(Expression<Func<T, bool>> predicate);
        Task<bool> AnyAsync(Expression<Func<T, bool>> predicate);

        // عمليات الاستعلام المتقدمة - Advanced Query Operations
        IQueryable<T> GetQueryable();
        IEnumerable<T> GetPaged(int pageNumber, int pageSize, Expression<Func<T, bool>>? filter = null);
        Task<IEnumerable<T>> GetPagedAsync(int pageNumber, int pageSize, Expression<Func<T, bool>>? filter = null);
    }
}
