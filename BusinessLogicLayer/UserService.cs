using DXApplication1.DataAccessLayer;
using DXApplication1.Models;
using DXApplication1.Utilities;
using System.Text.RegularExpressions;

namespace DXApplication1.BusinessLogicLayer
{
    /// <summary>
    /// تنفيذ خدمة المستخدمين - User Service Implementation
    /// </summary>
    public class UserService : IUserService
    {
        private readonly IUnitOfWork _unitOfWork;
        private readonly IUserRepository _userRepository;
        private User? _currentUser;

        public UserService(IUnitOfWork unitOfWork, IUserRepository userRepository)
        {
            _unitOfWork = unitOfWork ?? throw new ArgumentNullException(nameof(unitOfWork));
            _userRepository = userRepository ?? throw new ArgumentNullException(nameof(userRepository));
        }

        // المصادقة والتفويض - Authentication and Authorization
        public async Task<User?> AuthenticateAsync(string username, string password)
        {
            if (string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
                return null;

            try
            {
                var user = await _userRepository.AuthenticateAsync(username, password);
                if (user != null)
                {
                    await _unitOfWork.SaveChangesAsync();
                    SetCurrentUser(user);
                }
                return user;
            }
            catch (Exception ex)
            {
                // Log error
                throw new InvalidOperationException("خطأ في عملية المصادقة - Authentication error", ex);
            }
        }

        public async Task<bool> IsAuthorizedAsync(int userId, string permission)
        {
            var user = await GetUserByIdAsync(userId);
            if (user?.Role == null)
                return false;

            return permission.ToLower() switch
            {
                "manageusers" => user.Role.CanManageUsers,
                "managecustomers" => user.Role.CanManageCustomers,
                "manageproducts" => user.Role.CanManageProducts,
                "createinvoices" => user.Role.CanCreateInvoices,
                "viewreports" => user.Role.CanViewReports,
                "managesettings" => user.Role.CanManageSettings,
                _ => false
            };
        }

        public Task<User?> GetCurrentUserAsync()
        {
            return Task.FromResult(_currentUser);
        }

        public void SetCurrentUser(User user)
        {
            _currentUser = user;
        }

        public void Logout()
        {
            _currentUser = null;
        }

        // إدارة المستخدمين - User Management
        public async Task<IEnumerable<User>> GetAllUsersAsync()
        {
            return await _userRepository.GetAllAsync();
        }

        public async Task<User?> GetUserByIdAsync(int id)
        {
            return await _userRepository.GetByIdAsync(id);
        }

        public async Task<User?> GetUserByUsernameAsync(string username)
        {
            return await _userRepository.GetByUsernameAsync(username);
        }

        public async Task<bool> CreateUserAsync(User user, string password)
        {
            if (user == null || string.IsNullOrWhiteSpace(password))
                return false;

            try
            {
                // التحقق من صحة البيانات - Validate data
                if (!ValidateUserData(user, out var errors))
                    throw new ArgumentException($"بيانات المستخدم غير صحيحة: {string.Join(", ", errors)}");

                // التحقق من عدم وجود اسم المستخدم - Check username availability
                if (!await IsUsernameAvailableAsync(user.Username))
                    throw new ArgumentException("اسم المستخدم موجود بالفعل");

                // التحقق من عدم وجود البريد الإلكتروني - Check email availability
                if (!string.IsNullOrEmpty(user.Email) && !await IsEmailAvailableAsync(user.Email))
                    throw new ArgumentException("البريد الإلكتروني موجود بالفعل");

                // تشفير كلمة المرور - Hash password
                user.PasswordHash = SecurityHelper.HashPassword(password);
                user.CreatedDate = DateTime.Now;
                user.CreatedBy = _currentUser?.Username ?? "System";

                await _userRepository.AddAsync(user);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إنشاء المستخدم - Error creating user", ex);
            }
        }

        public async Task<bool> UpdateUserAsync(User user)
        {
            if (user == null)
                return false;

            try
            {
                // التحقق من صحة البيانات - Validate data
                if (!ValidateUserData(user, out var errors))
                    throw new ArgumentException($"بيانات المستخدم غير صحيحة: {string.Join(", ", errors)}");

                // التحقق من عدم وجود اسم المستخدم - Check username availability
                if (!await IsUsernameAvailableAsync(user.Username, user.Id))
                    throw new ArgumentException("اسم المستخدم موجود بالفعل");

                // التحقق من عدم وجود البريد الإلكتروني - Check email availability
                if (!string.IsNullOrEmpty(user.Email) && !await IsEmailAvailableAsync(user.Email, user.Id))
                    throw new ArgumentException("البريد الإلكتروني موجود بالفعل");

                user.ModifiedDate = DateTime.Now;
                user.ModifiedBy = _currentUser?.Username ?? "System";

                _userRepository.Update(user);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تحديث المستخدم - Error updating user", ex);
            }
        }

        public async Task<bool> DeleteUserAsync(int id)
        {
            try
            {
                var user = await GetUserByIdAsync(id);
                if (user == null)
                    return false;

                // منع حذف المستخدم الحالي - Prevent deleting current user
                if (_currentUser?.Id == id)
                    throw new InvalidOperationException("لا يمكن حذف المستخدم الحالي");

                // حذف منطقي - Soft delete
                user.IsDeleted = true;
                user.IsActive = false;
                user.ModifiedDate = DateTime.Now;
                user.ModifiedBy = _currentUser?.Username ?? "System";

                _userRepository.Update(user);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في حذف المستخدم - Error deleting user", ex);
            }
        }

        public async Task<bool> ActivateUserAsync(int id)
        {
            try
            {
                var user = await GetUserByIdAsync(id);
                if (user == null)
                    return false;

                user.IsActive = true;
                user.ModifiedDate = DateTime.Now;
                user.ModifiedBy = _currentUser?.Username ?? "System";

                _userRepository.Update(user);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تفعيل المستخدم - Error activating user", ex);
            }
        }

        public async Task<bool> DeactivateUserAsync(int id)
        {
            try
            {
                var user = await GetUserByIdAsync(id);
                if (user == null)
                    return false;

                // منع إلغاء تفعيل المستخدم الحالي - Prevent deactivating current user
                if (_currentUser?.Id == id)
                    throw new InvalidOperationException("لا يمكن إلغاء تفعيل المستخدم الحالي");

                user.IsActive = false;
                user.ModifiedDate = DateTime.Now;
                user.ModifiedBy = _currentUser?.Username ?? "System";

                _userRepository.Update(user);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إلغاء تفعيل المستخدم - Error deactivating user", ex);
            }
        }

        // إدارة كلمات المرور - Password Management
        public async Task<bool> ChangePasswordAsync(int userId, string currentPassword, string newPassword)
        {
            if (!ValidatePassword(newPassword))
                throw new ArgumentException("كلمة المرور الجديدة لا تلبي متطلبات الأمان");

            try
            {
                var result = await _userRepository.ChangePasswordAsync(userId, currentPassword, newPassword);
                if (result)
                    await _unitOfWork.SaveChangesAsync();

                return result;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تغيير كلمة المرور - Error changing password", ex);
            }
        }

        public async Task<bool> ResetPasswordAsync(int userId, string newPassword)
        {
            if (!ValidatePassword(newPassword))
                throw new ArgumentException("كلمة المرور الجديدة لا تلبي متطلبات الأمان");

            try
            {
                var result = await _userRepository.ResetPasswordAsync(userId, newPassword);
                if (result)
                    await _unitOfWork.SaveChangesAsync();

                return result;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إعادة تعيين كلمة المرور - Error resetting password", ex);
            }
        }

        public Task<string> GenerateTemporaryPasswordAsync()
        {
            const string chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
            var random = new Random();
            var password = new string(Enumerable.Repeat(chars, 8)
                .Select(s => s[random.Next(s.Length)]).ToArray());

            return Task.FromResult(password);
        }

        public bool ValidatePassword(string password)
        {
            if (string.IsNullOrWhiteSpace(password))
                return false;

            var minLength = ConfigurationManager.GetPasswordMinLength();
            if (password.Length < minLength)
                return false;

            // يجب أن تحتوي على حرف كبير وصغير ورقم - Must contain uppercase, lowercase, and digit
            return Regex.IsMatch(password, @"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$");
        }

        // إدارة الأدوار - Role Management
        public async Task<IEnumerable<Role>> GetAllRolesAsync()
        {
            return await _unitOfWork.Roles.GetAllAsync();
        }

        public async Task<Role?> GetRoleByIdAsync(int id)
        {
            return await _unitOfWork.Roles.GetByIdAsync(id);
        }

        public async Task<bool> AssignRoleAsync(int userId, int roleId)
        {
            try
            {
                var user = await GetUserByIdAsync(userId);
                var role = await GetRoleByIdAsync(roleId);

                if (user == null || role == null)
                    return false;

                user.RoleId = roleId;
                user.ModifiedDate = DateTime.Now;
                user.ModifiedBy = _currentUser?.Username ?? "System";

                _userRepository.Update(user);
                await _unitOfWork.SaveChangesAsync();

                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في تعيين الدور - Error assigning role", ex);
            }
        }

        // التحقق من صحة البيانات - Data Validation
        public async Task<bool> IsUsernameAvailableAsync(string username, int? excludeUserId = null)
        {
            return !await _userRepository.IsUsernameExistsAsync(username);
        }

        public async Task<bool> IsEmailAvailableAsync(string email, int? excludeUserId = null)
        {
            return !await _userRepository.IsEmailExistsAsync(email);
        }

        public bool ValidateUserData(User user, out List<string> errors)
        {
            errors = new List<string>();

            if (string.IsNullOrWhiteSpace(user.Username))
                errors.Add("اسم المستخدم مطلوب");
            else if (user.Username.Length < 3)
                errors.Add("اسم المستخدم يجب أن يكون 3 أحرف على الأقل");

            if (string.IsNullOrWhiteSpace(user.FullName))
                errors.Add("الاسم الكامل مطلوب");

            if (!string.IsNullOrEmpty(user.Email) && !IsValidEmail(user.Email))
                errors.Add("البريد الإلكتروني غير صحيح");

            if (user.RoleId <= 0)
                errors.Add("الدور مطلوب");

            return errors.Count == 0;
        }

        private bool IsValidEmail(string email)
        {
            return Regex.IsMatch(email, @"^[^@\s]+@[^@\s]+\.[^@\s]+$");
        }

        // الأمان - Security
        public async Task<bool> LockUserAsync(int userId, int durationMinutes)
        {
            try
            {
                var lockoutEndDate = DateTime.Now.AddMinutes(durationMinutes);
                await _userRepository.LockUserAsync(userId, lockoutEndDate);
                await _unitOfWork.SaveChangesAsync();
                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في قفل المستخدم - Error locking user", ex);
            }
        }

        public async Task<bool> UnlockUserAsync(int userId)
        {
            try
            {
                await _userRepository.UnlockUserAsync(userId);
                await _unitOfWork.SaveChangesAsync();
                return true;
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("خطأ في إلغاء قفل المستخدم - Error unlocking user", ex);
            }
        }

        public async Task<IEnumerable<User>> GetLockedUsersAsync()
        {
            return await _userRepository.GetLockedUsersAsync();
        }

        public async Task<bool> IsUserLockedAsync(int userId)
        {
            var user = await GetUserByIdAsync(userId);
            return user?.IsLocked ?? false;
        }

        // التقارير - Reports
        public async Task<int> GetTotalUsersCountAsync()
        {
            return await _userRepository.CountAsync(u => !u.IsDeleted);
        }

        public async Task<int> GetActiveUsersCountAsync()
        {
            return await _userRepository.CountAsync(u => u.IsActive && !u.IsDeleted);
        }

        public async Task<IEnumerable<User>> GetUsersByRoleAsync(int roleId)
        {
            return await _userRepository.GetUsersByRoleAsync(roleId);
        }

        public async Task<Dictionary<string, int>> GetUsersCountByRoleAsync()
        {
            var roles = await GetAllRolesAsync();
            var result = new Dictionary<string, int>();

            foreach (var role in roles)
            {
                var count = await _userRepository.CountAsync(u => u.RoleId == role.Id && !u.IsDeleted);
                result[role.RoleName] = count;
            }

            return result;
        }
    }
}
