using DXApplication1.Models;

namespace DXApplication1.BusinessLogicLayer
{
    /// <summary>
    /// واجهة خدمة المستخدمين - User Service Interface
    /// </summary>
    public interface IUserService
    {
        // المصادقة والتفويض - Authentication and Authorization
        Task<User?> AuthenticateAsync(string username, string password);
        Task<bool> IsAuthorizedAsync(int userId, string permission);
        Task<User?> GetCurrentUserAsync();
        void SetCurrentUser(User user);
        void Logout();

        // إدارة المستخدمين - User Management
        Task<IEnumerable<User>> GetAllUsersAsync();
        Task<User?> GetUserByIdAsync(int id);
        Task<User?> GetUserByUsernameAsync(string username);
        Task<bool> CreateUserAsync(User user, string password);
        Task<bool> UpdateUserAsync(User user);
        Task<bool> DeleteUserAsync(int id);
        Task<bool> ActivateUserAsync(int id);
        Task<bool> DeactivateUserAsync(int id);

        // إدارة كلمات المرور - Password Management
        Task<bool> ChangePasswordAsync(int userId, string currentPassword, string newPassword);
        Task<bool> ResetPasswordAsync(int userId, string newPassword);
        Task<string> GenerateTemporaryPasswordAsync();
        bool ValidatePassword(string password);

        // إدارة الأدوار - Role Management
        Task<IEnumerable<Role>> GetAllRolesAsync();
        Task<Role?> GetRoleByIdAsync(int id);
        Task<bool> AssignRoleAsync(int userId, int roleId);

        // التحقق من صحة البيانات - Data Validation
        Task<bool> IsUsernameAvailableAsync(string username, int? excludeUserId = null);
        Task<bool> IsEmailAvailableAsync(string email, int? excludeUserId = null);
        bool ValidateUserData(User user, out List<string> errors);

        // الأمان - Security
        Task<bool> LockUserAsync(int userId, int durationMinutes);
        Task<bool> UnlockUserAsync(int userId);
        Task<IEnumerable<User>> GetLockedUsersAsync();
        Task<bool> IsUserLockedAsync(int userId);

        // التقارير - Reports
        Task<int> GetTotalUsersCountAsync();
        Task<int> GetActiveUsersCountAsync();
        Task<IEnumerable<User>> GetUsersByRoleAsync(int roleId);
        Task<Dictionary<string, int>> GetUsersCountByRoleAsync();
    }
}
