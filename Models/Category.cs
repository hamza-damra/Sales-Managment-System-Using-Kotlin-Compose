using System.ComponentModel.DataAnnotations;

namespace DXApplication1.Models
{
    /// <summary>
    /// كيان فئة المنتج - Product Category Entity
    /// </summary>
    public class Category : BaseEntity
    {
        [Required]
        [MaxLength(200)]
        public string CategoryName { get; set; } = string.Empty;

        [MaxLength(500)]
        public string? Description { get; set; }

        [MaxLength(100)]
        public string? CategoryCode { get; set; }

        public int? ParentCategoryId { get; set; }
        public virtual Category? ParentCategory { get; set; }

        public int SortOrder { get; set; } = 0;

        // Navigation properties
        public virtual ICollection<Category> SubCategories { get; set; } = new List<Category>();
        public virtual ICollection<Product> Products { get; set; } = new List<Product>();
    }
}
