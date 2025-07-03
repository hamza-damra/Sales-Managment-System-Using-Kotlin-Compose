using System.ComponentModel.DataAnnotations;

namespace DXApplication1.Models
{
    /// <summary>
    /// كيان الدور - Role Entity
    /// </summary>
    public class Role : BaseEntity
    {
        [Required]
        [MaxLength(100)]
        public string RoleName { get; set; } = string.Empty;

        [MaxLength(500)]
        public string? Description { get; set; }

        // Permissions
        public bool CanManageUsers { get; set; } = false;
        public bool CanManageCustomers { get; set; } = false;
        public bool CanManageProducts { get; set; } = false;
        public bool CanCreateInvoices { get; set; } = false;
        public bool CanViewReports { get; set; } = false;
        public bool CanManageSettings { get; set; } = false;

        // Navigation properties
        public virtual ICollection<User> Users { get; set; } = new List<User>();
    }

    /// <summary>
    /// الأدوار الافتراضية في النظام - Default System Roles
    /// </summary>
    public static class DefaultRoles
    {
        public const string Admin = "مدير النظام";
        public const string Salesperson = "موظف مبيعات";
        public const string Viewer = "مستعرض";
    }
}
