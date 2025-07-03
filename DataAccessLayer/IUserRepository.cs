using DXApplication1.Models;

namespace DXApplication1.DataAccessLayer
{
    /// <summary>
    /// واجهة مستودع المستخدمين - User Repository Interface
    /// </summary>
    public interface IUserRepository : IRepository<User>
    {
        // عمليات المصادقة - Authentication Operations
        Task<User?> GetByUsernameAsync(string username);
        Task<User?> AuthenticateAsync(string username, string password);
        Task<bool> IsUsernameExistsAsync(string username);
        Task<bool> IsEmailExistsAsync(string email);

        // إدارة كلمات المرور - Password Management
        Task<bool> ChangePasswordAsync(int userId, string currentPassword, string newPassword);
        Task<bool> ResetPasswordAsync(int userId, string newPassword);

        // إدارة محاولات تسجيل الدخول - Login Attempts Management
        Task IncrementFailedLoginAttemptsAsync(int userId);
        Task ResetFailedLoginAttemptsAsync(int userId);
        Task LockUserAsync(int userId, DateTime lockoutEndDate);
        Task UnlockUserAsync(int userId);

        // الاستعلامات المتقدمة - Advanced Queries
        Task<IEnumerable<User>> GetUsersByRoleAsync(int roleId);
        Task<IEnumerable<User>> GetActiveUsersAsync();
        Task<IEnumerable<User>> GetLockedUsersAsync();
        Task UpdateLastLoginAsync(int userId);
    }
}
