using Microsoft.EntityFrameworkCore;
using System.Linq.Expressions;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// تنفيذ المستودع العام - Generic Repository Implementation
    /// </summary>
    /// <typeparam name="T">نوع الكيان</typeparam>
    public class Repository<T> : IRepository<T> where T : class
    {
        protected readonly SalesDbContext _context;
        protected readonly DbSet<T> _dbSet;

        public Repository(SalesDbContext context)
        {
            _context = context ?? throw new ArgumentNullException(nameof(context));
            _dbSet = _context.Set<T>();
        }

        // العمليات المتزامنة - Synchronous Operations
        public virtual T? GetById(int id)
        {
            return _dbSet.Find(id);
        }

        public virtual IEnumerable<T> GetAll()
        {
            return _dbSet.ToList();
        }

        public virtual IEnumerable<T> Find(Expression<Func<T, bool>> predicate)
        {
            return _dbSet.Where(predicate).ToList();
        }

        public virtual T? FirstOrDefault(Expression<Func<T, bool>> predicate)
        {
            return _dbSet.FirstOrDefault(predicate);
        }

        public virtual void Add(T entity)
        {
            if (entity == null)
                throw new ArgumentNullException(nameof(entity));

            _dbSet.Add(entity);
        }

        public virtual void AddRange(IEnumerable<T> entities)
        {
            if (entities == null)
                throw new ArgumentNullException(nameof(entities));

            _dbSet.AddRange(entities);
        }

        public virtual void Update(T entity)
        {
            if (entity == null)
                throw new ArgumentNullException(nameof(entity));

            _dbSet.Update(entity);
        }

        public virtual void Remove(T entity)
        {
            if (entity == null)
                throw new ArgumentNullException(nameof(entity));

            _dbSet.Remove(entity);
        }

        public virtual void RemoveRange(IEnumerable<T> entities)
        {
            if (entities == null)
                throw new ArgumentNullException(nameof(entities));

            _dbSet.RemoveRange(entities);
        }

        public virtual int Count()
        {
            return _dbSet.Count();
        }

        public virtual int Count(Expression<Func<T, bool>> predicate)
        {
            return _dbSet.Count(predicate);
        }

        public virtual bool Any(Expression<Func<T, bool>> predicate)
        {
            return _dbSet.Any(predicate);
        }

        // العمليات غير المتزامنة - Asynchronous Operations
        public virtual async Task<T?> GetByIdAsync(int id)
        {
            return await _dbSet.FindAsync(id);
        }

        public virtual async Task<IEnumerable<T>> GetAllAsync()
        {
            return await _dbSet.ToListAsync();
        }

        public virtual async Task<IEnumerable<T>> FindAsync(Expression<Func<T, bool>> predicate)
        {
            return await _dbSet.Where(predicate).ToListAsync();
        }

        public virtual async Task<T?> FirstOrDefaultAsync(Expression<Func<T, bool>> predicate)
        {
            return await _dbSet.FirstOrDefaultAsync(predicate);
        }

        public virtual async Task AddAsync(T entity)
        {
            if (entity == null)
                throw new ArgumentNullException(nameof(entity));

            await _dbSet.AddAsync(entity);
        }

        public virtual async Task AddRangeAsync(IEnumerable<T> entities)
        {
            if (entities == null)
                throw new ArgumentNullException(nameof(entities));

            await _dbSet.AddRangeAsync(entities);
        }

        public virtual async Task<int> CountAsync()
        {
            return await _dbSet.CountAsync();
        }

        public virtual async Task<int> CountAsync(Expression<Func<T, bool>> predicate)
        {
            return await _dbSet.CountAsync(predicate);
        }

        public virtual async Task<bool> AnyAsync(Expression<Func<T, bool>> predicate)
        {
            return await _dbSet.AnyAsync(predicate);
        }

        // عمليات الاستعلام المتقدمة - Advanced Query Operations
        public virtual IQueryable<T> GetQueryable()
        {
            return _dbSet.AsQueryable();
        }

        public virtual IEnumerable<T> GetPaged(int pageNumber, int pageSize, Expression<Func<T, bool>>? filter = null)
        {
            IQueryable<T> query = _dbSet;

            if (filter != null)
                query = query.Where(filter);

            return query.Skip((pageNumber - 1) * pageSize)
                       .Take(pageSize)
                       .ToList();
        }

        public virtual async Task<IEnumerable<T>> GetPagedAsync(int pageNumber, int pageSize, Expression<Func<T, bool>>? filter = null)
        {
            IQueryable<T> query = _dbSet;

            if (filter != null)
                query = query.Where(filter);

            return await query.Skip((pageNumber - 1) * pageSize)
                             .Take(pageSize)
                             .ToListAsync();
        }
    }
}
