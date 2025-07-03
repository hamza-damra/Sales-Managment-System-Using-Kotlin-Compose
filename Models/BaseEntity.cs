using System.ComponentModel.DataAnnotations;

namespace DXApplication1.Models
{
    /// <summary>
    /// الكلاس الأساسي لجميع الكيانات في النظام
    /// Base entity class for all entities in the system
    /// </summary>
    public abstract class BaseEntity
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public DateTime CreatedDate { get; set; } = DateTime.Now;

        public DateTime? ModifiedDate { get; set; }

        [MaxLength(100)]
        public string? CreatedBy { get; set; }

        [MaxLength(100)]
        public string? ModifiedBy { get; set; }

        public bool IsActive { get; set; } = true;

        public bool IsDeleted { get; set; } = false;
    }
}
