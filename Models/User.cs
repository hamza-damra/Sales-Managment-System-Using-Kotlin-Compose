using System.ComponentModel.DataAnnotations;

namespace DXApplication1.Models
{
    /// <summary>
    /// كيان المستخدم - User Entity
    /// </summary>
    public class User : BaseEntity
    {
        [Required]
        [MaxLength(100)]
        public string Username { get; set; } = string.Empty;

        [Required]
        [MaxLength(255)]
        public string PasswordHash { get; set; } = string.Empty;

        [Required]
        [MaxLength(200)]
        public string FullName { get; set; } = string.Empty;

        [MaxLength(100)]
        public string? Email { get; set; }

        [MaxLength(20)]
        public string? Phone { get; set; }

        public int RoleId { get; set; }
        public virtual Role Role { get; set; } = null!;

        public DateTime? LastLoginDate { get; set; }

        public int FailedLoginAttempts { get; set; } = 0;

        public DateTime? LockoutEndDate { get; set; }

        public bool IsLocked => LockoutEndDate.HasValue && LockoutEndDate > DateTime.Now;

        // Navigation properties
        public virtual ICollection<Invoice> Invoices { get; set; } = new List<Invoice>();
    }
}
