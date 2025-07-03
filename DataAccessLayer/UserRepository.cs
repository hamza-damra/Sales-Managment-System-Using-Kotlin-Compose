using Microsoft.EntityFrameworkCore;
using DXApplication1.Models;
using DXApplication1.Utilities;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// تنفيذ مستودع المستخدمين - User Repository Implementation
    /// </summary>
    public class UserRepository : Repository<User>, IUserRepository
    {
        public UserRepository(SalesDbContext context) : base(context)
        {
        }

        // عمليات المصادقة - Authentication Operations
        public async Task<User?> GetByUsernameAsync(string username)
        {
            if (string.IsNullOrWhiteSpace(username))
                return null;

            return await _dbSet
                .Include(u => u.Role)
                .FirstOrDefaultAsync(u => u.Username == username && u.IsActive && !u.IsDeleted);
        }

        public async Task<User?> AuthenticateAsync(string username, string password)
        {
            if (string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
                return null;

            var user = await GetByUsernameAsync(username);
            
            if (user == null || user.IsLocked)
                return null;

            if (!SecurityHelper.VerifyPassword(password, user.PasswordHash))
            {
                await IncrementFailedLoginAttemptsAsync(user.Id);
                return null;
            }

            // إعادة تعيين محاولات تسجيل الدخول الفاشلة - Reset failed login attempts
            await ResetFailedLoginAttemptsAsync(user.Id);
            await UpdateLastLoginAsync(user.Id);

            return user;
        }

        public async Task<bool> IsUsernameExistsAsync(string username)
        {
            if (string.IsNullOrWhiteSpace(username))
                return false;

            return await _dbSet.AnyAsync(u => u.Username == username && !u.IsDeleted);
        }

        public async Task<bool> IsEmailExistsAsync(string email)
        {
            if (string.IsNullOrWhiteSpace(email))
                return false;

            return await _dbSet.AnyAsync(u => u.Email == email && !u.IsDeleted);
        }

        // إدارة كلمات المرور - Password Management
        public async Task<bool> ChangePasswordAsync(int userId, string currentPassword, string newPassword)
        {
            var user = await GetByIdAsync(userId);
            if (user == null)
                return false;

            if (!SecurityHelper.VerifyPassword(currentPassword, user.PasswordHash))
                return false;

            user.PasswordHash = SecurityHelper.HashPassword(newPassword);
            user.ModifiedDate = DateTime.Now;
            
            Update(user);
            return true;
        }

        public async Task<bool> ResetPasswordAsync(int userId, string newPassword)
        {
            var user = await GetByIdAsync(userId);
            if (user == null)
                return false;

            user.PasswordHash = SecurityHelper.HashPassword(newPassword);
            user.ModifiedDate = DateTime.Now;
            user.FailedLoginAttempts = 0;
            user.LockoutEndDate = null;
            
            Update(user);
            return true;
        }

        // إدارة محاولات تسجيل الدخول - Login Attempts Management
        public async Task IncrementFailedLoginAttemptsAsync(int userId)
        {
            var user = await GetByIdAsync(userId);
            if (user == null)
                return;

            user.FailedLoginAttempts++;
            user.ModifiedDate = DateTime.Now;

            // قفل المستخدم إذا تجاوز الحد الأقصى - Lock user if exceeded max attempts
            var maxAttempts = ConfigurationManager.GetMaxLoginAttempts();
            if (user.FailedLoginAttempts >= maxAttempts)
            {
                var lockoutDuration = ConfigurationManager.GetLockoutDurationMinutes();
                user.LockoutEndDate = DateTime.Now.AddMinutes(lockoutDuration);
            }

            Update(user);
        }

        public async Task ResetFailedLoginAttemptsAsync(int userId)
        {
            var user = await GetByIdAsync(userId);
            if (user == null)
                return;

            user.FailedLoginAttempts = 0;
            user.LockoutEndDate = null;
            user.ModifiedDate = DateTime.Now;
            
            Update(user);
        }

        public async Task LockUserAsync(int userId, DateTime lockoutEndDate)
        {
            var user = await GetByIdAsync(userId);
            if (user == null)
                return;

            user.LockoutEndDate = lockoutEndDate;
            user.ModifiedDate = DateTime.Now;
            
            Update(user);
        }

        public async Task UnlockUserAsync(int userId)
        {
            var user = await GetByIdAsync(userId);
            if (user == null)
                return;

            user.LockoutEndDate = null;
            user.FailedLoginAttempts = 0;
            user.ModifiedDate = DateTime.Now;
            
            Update(user);
        }

        // الاستعلامات المتقدمة - Advanced Queries
        public async Task<IEnumerable<User>> GetUsersByRoleAsync(int roleId)
        {
            return await _dbSet
                .Include(u => u.Role)
                .Where(u => u.RoleId == roleId && u.IsActive && !u.IsDeleted)
                .ToListAsync();
        }

        public async Task<IEnumerable<User>> GetActiveUsersAsync()
        {
            return await _dbSet
                .Include(u => u.Role)
                .Where(u => u.IsActive && !u.IsDeleted)
                .ToListAsync();
        }

        public async Task<IEnumerable<User>> GetLockedUsersAsync()
        {
            return await _dbSet
                .Include(u => u.Role)
                .Where(u => u.LockoutEndDate.HasValue && u.LockoutEndDate > DateTime.Now && !u.IsDeleted)
                .ToListAsync();
        }

        public async Task UpdateLastLoginAsync(int userId)
        {
            var user = await GetByIdAsync(userId);
            if (user == null)
                return;

            user.LastLoginDate = DateTime.Now;
            user.ModifiedDate = DateTime.Now;
            
            Update(user);
        }

        // تجاوز العمليات الأساسية لتضمين العلاقات - Override base operations to include relationships
        public override async Task<User?> GetByIdAsync(int id)
        {
            return await _dbSet
                .Include(u => u.Role)
                .FirstOrDefaultAsync(u => u.Id == id);
        }

        public override async Task<IEnumerable<User>> GetAllAsync()
        {
            return await _dbSet
                .Include(u => u.Role)
                .Where(u => !u.IsDeleted)
                .ToListAsync();
        }
    }
}
